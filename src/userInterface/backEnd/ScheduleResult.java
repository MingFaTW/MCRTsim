/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package userInterface.backEnd;

import ResultSet.MissDeadlineInfo;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Vector;
import SystemEnvironment.Core;
import WorkLoadSet.DataSetting;
import ResultSet.ResultSet;
import ResultSet.SchedulingInfo;
import RTSimulator.Definition;
import WorkLoad.Task;
import static RTSimulator.Definition.magnificationFactor;

/**
 * Builds and holds the processed scheduling timeline data for visualization.
 *
 * <p>{@code ScheduleResult} transforms raw per-core {@link SchedulingInfo}
 * records produced by the simulator into time-indexed lookup tables and lane
 * models (task or core timelines) that the UI can render. It supports two
 * modes:
 * <ul>
 *   <li><strong>Core timeline</strong> (isCoreTimeLine = true): one {@link CoreTimeLine}
 *       per core showing EXECUTION / WAIT / CONTEXTSWITCH / MIGRATION intervals.</li>
 *   <li><strong>Task timeline</strong> (isCoreTimeLine = false): one {@link TaskTimeLine}
 *       per task (multi‑core case) or a reduced set for single‑core, aggregating
 *       intervals where the task's job is active.</li>
 * </ul>
 * Inline comments (Chinese) have been integrated: 排程時間 (simulation time),
 * {@code Dictionary<ID, TaskTimeline / CoreTimeline>} describing maps from IDs to timelines,
 * baseunit 比例尺 (horizontal scaling in pixels per time unit), accuracy 時間精準度
 * (integer granularity used for indexing), and atbSet 儲存記錄佇列 (2‑D array that
 * stores references to scheduling info at each discrete time tick).
 *
 * <p>Time scaling: {@link #simulationTime} is derived from the simulator's raw
 * simulation time divided by {@code magnificationFactor}. The 2‑D array
 * {@code atbSet} is sized as [taskOrCoreIndex][simulationTime * accuracy + 1].
 * Each cell holds the applicable {@link SchedulingInfo} reference for constant‑time
 * retrieval in hover/highlight operations.</p>
 *
 * @author ShiuJia
 */
public class ScheduleResult 
{
    /** Parent viewer which owns this scheduling result and provides cores &amp; scale. */
    public ResultViewer parent;
    /** True when more than one core is present (multi‑core visualization). */
    public boolean isMultiCore;
    /** True when building per‑core timelines; false for task timelines. */
    public boolean isCoreTimeLine;
    /** Vertical gap between task lanes in single‑core task view. */
    private int singleTaskGap = 200;
    /** Vertical gap between task lanes in multi‑core task view. */
    private int MCTaskGap = 250;
    /** Total simulation time (scaled by magnificationFactor). */
    private double simulationTime; //排程時間
    /** Map: task ID (String) -> its TaskTimeLine lane model. */
    private Dictionary<String, TaskTimeLine> taskTimeLines; //Dictionary<ID, Task時間軸>
    /** Map: core ID (String) -> its CoreTimeLine lane model. */
    private Dictionary<String, CoreTimeLine> coreTimeLines; //Dictionary<ID, Core時間軸>
    /** 2‑D time indexed matrix of scheduling info references. */
    private SchedulingInfo[][] atbSet;//儲存記錄佇列
    /** Horizontal pixel scale factor (pixels per time unit). */
    private int baseunit = 40; //比例尺
    /** Time precision (number of discrete slices per time unit). */
    private int accuracy = 100; //時間精準度
    /** Result set containing miss‑deadline information used for timeline augmentation. */
    private ResultSet resultSet;
    
    /**
     * Constructs a scheduling result builder bound to a viewer. Initializes
     * mode flags (multi‑core detection), empty timeline maps, obtains the
     * simulator's {@link ResultSet} and computes scaled simulation time.
     *
     * @param rv parent result viewer supplying cores and simulator access
     */
    public ScheduleResult(ResultViewer rv) 
    {
        this.parent = rv;
        this.isMultiCore = this.parent.getCores().size() > 1 ? true : false;
        this.taskTimeLines = new Hashtable<String, TaskTimeLine>();
        this.coreTimeLines = new Hashtable<String, CoreTimeLine>();
        this.resultSet = this.parent.parent.parent.getSimulationViewer().getSimulator().getResultSet();
        this.simulationTime = (double)this.parent.parent.parent.getSimulationViewer().getSimulationTime() / magnificationFactor;
    }
    
    /**
     * Builds core timelines: allocates {@link CoreTimeLine} instances for every
     * core, constructs a time indexed matrix (atbSet) sized by tasks ×
     * (simulationTime * accuracy + 1), then iterates each core's scheduling info
     * assigning references into {@code atbSet}. Finally converts each EXECUTION,
     * WAIT, CONTEXTSWITCH and MIGRATION interval into a {@link TaskExecution} and
     * attaches it to the corresponding core timeline.
     */
    public void startCoreTimeLineSchedule()
    {
        this.isCoreTimeLine = true;
        DataSetting ds = this.parent.parent.getDataSetting();
        
        for(int i = 0; i < ds.getProcessor().getAllCore().size() ; i++)
        {
           String id = String.valueOf(ds.getProcessor().getCore(i).getID());
           coreTimeLines.put(id , new CoreTimeLine(this, i, id));
        }

        this.atbSet = new SchedulingInfo[ds.getTaskSet().size()][(int)(this.simulationTime * accuracy)+1];

        for(Core core : this.parent.getCores())
        {
            Vector<SchedulingInfo> schedulingInfoSet = core.getSchedulingInfoSet();

            for (int i=0;i<schedulingInfoSet.size()-1;i++)
            {
                for(int j=Double.valueOf(schedulingInfoSet.get(i).getStartTime() * accuracy).intValue();j<Double.valueOf(schedulingInfoSet.get(i).getEndTime() * accuracy).intValue();j++)
                {
                    this.atbSet[core.getID()-1][j] = schedulingInfoSet.get(i);
                }
            }
            for(int j = (int)(schedulingInfoSet.get(schedulingInfoSet.size()-1).getStartTime() * accuracy);j <= (int)(this.simulationTime * accuracy) ;j++)
            {
                this.atbSet[core.getID()-1][j] = schedulingInfoSet.get(schedulingInfoSet.size()-1);
            }
        }
        
        CoreTimeLine ctl;
        
        for(Core core : this.parent.getCores())
        {
            for(SchedulingInfo curResult : core.getSchedulingInfoSet())
            {  
                if(curResult.getCoreStatus() == Definition.CoreStatus.EXECUTION )
                {
                    ctl = this.coreTimeLines.get(String.valueOf(core.getID()));
                    ctl.addExecution(new TaskExecution(curResult));
                }
                else if(curResult.getCoreStatus() == Definition.CoreStatus.WAIT)
                {
                    ctl = this.coreTimeLines.get(String.valueOf(core.getID()));
                    ctl.addExecution(new TaskExecution(curResult));
                }
                else if(curResult.getCoreStatus() == Definition.CoreStatus.CONTEXTSWITCH)
                {
                    ctl = this.coreTimeLines.get(String.valueOf(core.getID()));
                    ctl.addExecution(new TaskExecution(curResult));
                }
                else if(curResult.getCoreStatus() == Definition.CoreStatus.MIGRATION)
                {
                    ctl = this.coreTimeLines.get(String.valueOf(core.getID()));
                    ctl.addExecution(new TaskExecution(curResult)); 
                }
            }
        }
    }
    
    /**
     * Builds task timelines: creates a {@link TaskTimeLine} for each task,
     * allocates the time matrix {@code atbSet} (multi‑core: size tasks × time;
     * single‑core: size 1 × time), populates references from scheduling info
     * (mapping each interval to the owning task's index), adds execution‑related
     * {@link TaskExecution} objects for relevant core statuses, adds deadline
     * miss markers, then prunes timelines with no recorded executions.
     */
    public void startTaskTimeLineSchedule()
    {
        this.isCoreTimeLine = false;
        DataSetting ds = this.parent.parent.getDataSetting();
       
        if(this.isMultiCore)
        {
            for(int i = 0; i < ds.getTaskSet().size() ; i++)
            {
               String id = String.valueOf(ds.getTask(i).getID());
               taskTimeLines.put(id , new TaskTimeLine(this, i, id));
            }
            
            this.atbSet = new SchedulingInfo[ds.getTaskSet().size()][(int)(this.simulationTime * accuracy)+1];
            
            SchedulingInfo nullSInfo = new SchedulingInfo();
            for(Task task : ds.getTaskSet())
            {
                for(int j=0;j<Double.valueOf(this.simulationTime * accuracy).intValue()+1;j++)
                {
                    this.atbSet[task.getID()-1][j] = nullSInfo;
                }
            }
            
            for(Core core : this.parent.getCores())
            {
                Vector<SchedulingInfo> schedulingInfoSet = core.getSchedulingInfoSet();
                for (int i=0;i<schedulingInfoSet.size()-1;i++)
                {
                    if(schedulingInfoSet.get(i).getJob() != null)
                    {
                        for(int j=Double.valueOf(schedulingInfoSet.get(i).getStartTime() * accuracy).intValue();j<Double.valueOf(schedulingInfoSet.get(i).getEndTime() * accuracy).intValue();j++)
                        {
                            this.atbSet[schedulingInfoSet.get(i).getJob().getParentTask().getID()-1][j] = schedulingInfoSet.get(i);
                        }
                    }
                }
                for(int j = (int)(schedulingInfoSet.get(schedulingInfoSet.size()-1).getStartTime() * accuracy);j <= (int)(this.simulationTime * accuracy) ;j++)
                {
                    if(schedulingInfoSet.get(schedulingInfoSet.size()-1).getJob() != null)
                    {
                        this.atbSet[schedulingInfoSet.get(schedulingInfoSet.size()-1).getJob().getParentTask().getID()-1][j] = schedulingInfoSet.get(schedulingInfoSet.size()-1);
                    }
                }
            }
        }
        else
        {
            Core core = this.parent.getCore(0);
            Vector<SchedulingInfo> record = core.getSchedulingInfoSet();
            for(int i = 0; i < ds.getTaskSet().size() ; i++)
            {
               String id = String.valueOf(ds.getTask(i).getID());
               taskTimeLines.put(id , new TaskTimeLine(this, i, id));
            }
            this.atbSet = new SchedulingInfo[1][(int)(this.simulationTime * accuracy)+1];
            for (int i=0;i<record.size()-1;i++)
            {
                for(int j=Double.valueOf(record.get(i).getStartTime() * accuracy).intValue();j<Double.valueOf(record.get(i).getEndTime() * accuracy).intValue();j++)
                {
                    this.atbSet[0][j] = record.get(i);
                }
            }
            for(int j = (int)(record.get(record.size()-1).getStartTime() * accuracy);j <= (int)(this.simulationTime * accuracy) ;j++)
            {
                this.atbSet[0][j] = record.get(record.size()-1);
            }
        }
        
        TaskTimeLine ttl;
        
        for(Core core : this.parent.getCores())
        {
            for(SchedulingInfo curResult : core.getSchedulingInfoSet())
            {  
                if(curResult.getCoreStatus() == Definition.CoreStatus.EXECUTION )
                {
                    ttl = this.taskTimeLines.get(String.valueOf(curResult.getJob().getParentTask().getID()));
                    ttl.addExecution(new TaskExecution(curResult));
                }
                else if(curResult.getCoreStatus() == Definition.CoreStatus.WAIT)
                {
                    ttl = this.taskTimeLines.get(String.valueOf(curResult.getJob().getParentTask().getID()));
                    ttl.addExecution(new TaskExecution(curResult));
                }
                else if(curResult.getCoreStatus() == Definition.CoreStatus.CONTEXTSWITCH)
                {
                    ttl = this.taskTimeLines.get(String.valueOf(curResult.getJob().getParentTask().getID()));
                    ttl.addExecution(new TaskExecution(curResult));
                }
                else if(curResult.getCoreStatus() == Definition.CoreStatus.MIGRATION)
                {
                    ttl = this.taskTimeLines.get(String.valueOf(curResult.getJob().getParentTask().getID()));
                    ttl.addExecution(new TaskExecution(curResult));
                }
            }
        }
        
        for(MissDeadlineInfo missDeadlineInfo : this.resultSet.getMissDeadlineInfoSet())
        {
            ttl = this.taskTimeLines.get(String.valueOf(missDeadlineInfo.getMissTask().getID()));
            ttl.addExecution(new TaskExecution(missDeadlineInfo));
        }
        this.removeNeedlessTaskTimeLine();
    }
    
    /**
     * Removes task timelines that have zero execution intervals (filter out
     * tasks that never became active during the simulation window).
     */
    private void removeNeedlessTaskTimeLine()
    {
        DataSetting ds = this.parent.parent.getDataSetting();
        for(int i = 0; i < ds.getTaskSet().size() ; i++)
        {
            String id = String.valueOf(ds.getTask(i).getID());
            if(taskTimeLines.get(id).getExecutionNumber() < 1)
            {
                this.taskTimeLines.remove(taskTimeLines.get(id));
            }
        }
    }
    
    /**
     * Retrieves the {@link SchedulingInfo} reference at a given task index and
     * accuracy‑scaled time tick.
     *
     * @param taskID zero‑based (for multi‑core task timelines: taskID-1 matches array row)
     * @param time discrete time index (scaled by {@link #accuracy})
     * @return scheduling info stored at that position (may be the last interval or a null placeholder)
     */
    public SchedulingInfo getAtbSet(int taskID , int time)
    {
        return this.atbSet[taskID][time];
    }
    
    /**
     * Returns the final (scaled) simulation time used for timeline construction.
     *
     * @return scaled simulation time in time units
     */
    public  double getFinalTime()
    {
        return this.simulationTime;
    }

    /**
     * Returns the map of task timelines keyed by task ID (String).
     *
     * @return dictionary of task ID to {@link TaskTimeLine}
     */
    public  Dictionary<String, TaskTimeLine> getTaskTimeLines()
    {
        return this.taskTimeLines;
    }
    
    /**
     * Returns the map of core timelines keyed by core ID (String).
     *
     * @return dictionary of core ID to {@link CoreTimeLine}
     */
    public  Dictionary<String, CoreTimeLine> getCoreTimeLines()
    {
        return this.coreTimeLines;
    }
    
    /**
     * Returns the horizontal scaling base unit in pixels per time unit.
     *
     * @return base unit (pixels per time unit)
     */
    public int getBaseunit()
    {
        return this.baseunit;
    }
    
    /**
     * Adjusts the horizontal scaling factor by multiplying the current base
     * unit by the supplied value.
     *
     * @param d scale multiplier (values &gt; 1 zoom in, &lt; 1 zoom out)
     */
    public void setBaseunit(Double d)
    {
        this.baseunit = (int)(this.baseunit * d);
    }
    
    /**
     * Returns the time accuracy (number of discrete ticks per time unit).
     *
     * @return accuracy integer
     */
    public int getAccuracy()
    {
        return this.accuracy;
    }
    
    /**
     * Returns the vertical gap between task lanes in single‑core mode.
     *
     * @return single‑core task lane gap (pixels)
     */
    public int getSingleTaskGap()
    {
        return this.singleTaskGap;
    }
    
    /**
     * Returns the vertical gap between task lanes in multi‑core mode.
     *
     * @return multi‑core task lane gap (pixels)
     */
    public int getMCTaskGap()
    {
        return this.MCTaskGap;
    }
    
    /**
     * Returns the effective vertical gap based on whether multi‑core mode is
     * active.
     *
     * @return chosen task lane gap (pixels)
     */
    public int getTaskGap()
    {
        return this.isMultiCore ? this.MCTaskGap :this.singleTaskGap;
    }
}