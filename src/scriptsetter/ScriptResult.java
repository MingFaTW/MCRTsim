/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package scriptsetter;

import java.util.Vector;
import static RTSimulator.RTSimulator.println;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 * Represents the results produced by executing a {@code Script}.
 *
 * <p>This class stores identification information, workload and processor file
 * paths, per-core power consumption samples, schedulability flag and a set of
 * performance metrics such as completed / missed job counts and ratios,
 * average pending/response times, critical-section ratios and utilization
 * statistics. The result's numeric ID is derived from the size of the parent's
 * script result set at creation time.</p>
 *
 * @author YC
 */
public class ScriptResult 
{   
    private Script parent;
    private int ID;
    private String workloadFile,processorFile;
    /**
     * Flag indicating whether the schedule was determined to be schedulable.
     * This field is public in the original design so external code can read
     * or update it directly; note that some setters in this class also update
     * this flag (for example {@link #setTotalJobMissDeadlineCount(int)}).
     */
    public boolean isSchedulable;
    private Vector<Double> powerConsumptions;
    private int taskCount;
    private int totalJobCompeletedCount;
    private int totalJobMissDeadlineCount;
    private double completedRatio;
    private double deadlineMissRatio;
            
    private double averagePendingTime;
    private double averageResponseTime;
    private double beBlockedTimeRatio;
    private double maximumCriticalSectionRatio,actualCriticalSectionRatio,maximumUtilization,actualUtilization;
            

    /**
     * Constructs a new {@code ScriptResult} associated with the given parent
     * {@code Script}. The constructor initializes metric fields to sensible
     * defaults (schedulable = true, empty power list, zeroed counts and ratios).
     *
     * @param s the parent {@code Script} that produced this result
     */
    public ScriptResult(Script s)
    {
        this.parent = s;
        this.ID = s.getScriptResultSet().size()+1;
        this.isSchedulable = true;
        this.powerConsumptions = new Vector<>();
        this.totalJobCompeletedCount = 0;
        this.totalJobMissDeadlineCount = 0;
        this.completedRatio=0;
        this.deadlineMissRatio=0;
        this.averagePendingTime = 0;
        this.averageResponseTime = 0;
    }
    
    /**
     * Sets the workload file path associated with this result.
     *
     * @param str the workload file path (without implicit suffix handling)
     */
    public void setWorkloadFile(String str)
    {
        this.workloadFile = str;
    }
   
    /**
     * Sets the processor file path associated with this result.
     *
     * @param str the processor file path (without implicit suffix handling)
     */
    public void setProcessorFile(String str)
    {
        this.processorFile = str;
    }
    
    /**
     * Sets the schedulability flag for this result.
     *
     * @param b true if the schedule is considered schedulable; false otherwise
     */
    public void setSchedulable(boolean b)
    {
        this.isSchedulable = b;
    }
    
    /**
     * Sets the maximum critical section ratio observed in the run.
     *
     * @param ratio the maximum critical section ratio
     */
    public void setMaximumCriticalSectionRatio(double ratio)
    {
        this.maximumCriticalSectionRatio = ratio;
    }
    
    /**
     * Sets the actual critical section ratio observed in the run.
     *
     * @param ratio the actual critical section ratio
     */
    public void setActualCriticalSectionRatio(double ratio)
    {
        this.actualCriticalSectionRatio = ratio;
    }
    
    /**
     * Sets the theoretical or measured maximum utilization metric.
     *
     * @param U the maximum utilization value
     */
    public void setMaximumUtilization(double U)
    {
        this.maximumUtilization = U;
    }
    
    /**
     * Sets the actual utilization observed during the run.
     *
     * @param U the actual utilization value
     */
    public void setActualUtilization(double U)
    {
        this.actualUtilization = U;
    }
    
    /**
     * Adds a power consumption sample (for a core or measurement point) to the
     * internal list of power readings.
     *
     * @param p a power consumption sample (in the units used by the simulator)
     */
    public void addPowerConsumption(double p)
    {
        this.powerConsumptions.add(p);
    }
    
    /**
     * Sets the number of tasks considered in this result.
     *
     * @param i the task count
     */
    public void setTaskCount(int i)
    {
        this.taskCount = i;
    }
    
    /**
     * Sets the total number of completed jobs observed.
     *
     * @param i the completed job count
     */
    public void setTotalJobCompeletedCount(int i)
    {
        this.totalJobCompeletedCount = i;
    }
    
    /**
     * Sets the total number of jobs that missed their deadlines. If the value
     * is greater than zero this method also marks {@link #isSchedulable} as
     * {@code false} indicating the schedule is not fully schedulable.
     *
     * @param i the count of jobs that missed deadlines
     */
    public void setTotalJobMissDeadlineCount(int i)
    {
        this.totalJobMissDeadlineCount = i;
        if(i>0)
        {
            this.isSchedulable = false;
        }
    }
    
    /**
     * Sets the completed ratio (fraction of jobs completed successfully).
     *
     * @param i the completed ratio (0.0 - 1.0)
     */
    public void setCompletedRatio(double i)
    {
        this.completedRatio = i;
    }
    
    /**
     * Sets the deadline miss ratio (fraction of jobs that missed deadlines).
     *
     * @param i the deadline miss ratio (0.0 - 1.0)
     */
    public void setDeadlineMissRatio(double i)
    {
        this.deadlineMissRatio = i;
    }
    
    /**
     * Sets the average pending time observed across jobs.
     *
     * @param d average pending time (in the simulator's time units)
     */
    public void setAveragePendingTime(double d)
    {
        this.averagePendingTime = d;
    }
    
    /**
     * Sets the average response time observed across jobs.
     *
     * @param d average response time (in the simulator's time units)
     */
    public void setAverageResponseTime(double d)
    {
        this.averageResponseTime = d;
    }
    
    /**
     * Sets the ratio of time jobs spent blocked.
     *
     * @param d the blocked time ratio (0.0 - 1.0)
     */
    public void setBeBlockedTimeRatio(double d)
    {
        this.beBlockedTimeRatio = d;
    }
    
    /**
     * Returns the maximum critical section ratio recorded.
     *
     * @return the maximum critical section ratio
     */
    public double getMaximumCriticalSectionRatio()
    {
        return this.maximumCriticalSectionRatio;
    }
    
    /**
     * Returns the actual critical section ratio recorded.
     *
     * @return the actual critical section ratio
     */
    public double getActualCriticalSectionRatio()
    {
        return this.actualCriticalSectionRatio;
    }
    
    /**
     * Returns the maximum utilization metric.
     *
     * @return the maximum utilization
     */
    public double getMaximumUtilization()
    {
        return this.maximumUtilization;
    }
    
    /**
     * Returns the actual utilization observed.
     *
     * @return the actual utilization
     */
    public double getActualUtilization()
    {
        return this.actualUtilization;
    }
    
    /**
     * Returns the workload file path recorded for this result.
     *
     * @return the workload file path
     */
    public String getWorkloadFile()
    {
        return this.workloadFile;
    }
    
    /**
     * Returns the processor file path recorded for this result.
     *
     * @return the processor file path
     */
    public String getProcessorFile()
    {
        return this.processorFile;
    }
    
    /**
     * Returns the list of power consumption samples recorded for this result.
     *
     * @return a {@code Vector<Double>} of power consumption samples
     */
    public Vector<Double> getPowerConsumptions()
    {
        return this.powerConsumptions;
    }
    
    /**
     * Computes the total power consumption by summing all recorded samples.
     *
     * @return the sum of all power consumption samples
     */
    public double getTotalPowerConsumption()
    {
        double p = 0;
        for(int i = 0 ; i<this.powerConsumptions.size() ; i++)
        {
            p +=this.powerConsumptions.get(i);
        }
        return p;
    }
    
    /**
     * Returns the number of tasks considered in this result.
     *
     * @return the task count
     */
    public int getTaskCount()
    {
        return this.taskCount;
    }
    
    /**
     * Returns the total number of completed jobs.
     *
     * @return total completed job count
     */
    public int getTotalJobCompeletedCount()
    {
        return this.totalJobCompeletedCount;
    }
    
    /**
     * Returns the total number of jobs that missed deadlines.
     *
     * @return total missed-deadline job count
     */
    public int getTotalJobMissDeadlineCount()
    {
        return this.totalJobMissDeadlineCount;
    }
    
    /**
     * Returns the completed ratio (fraction of jobs completed).
     *
     * @return the completed ratio
     */
    public double getCompletedRatio()
    {
        return this.completedRatio;
    }
    
    /**
     * Returns the deadline miss ratio (fraction of jobs that missed deadlines).
     *
     * @return the deadline miss ratio
     */
    public double getDeadlineMissRatio()
    {
        return this.deadlineMissRatio;
    }
    
    /**
     * Returns the average pending time observed.
     *
     * @return average pending time
     */
    public double getAveragePendingTime()
    {
        return this.averagePendingTime;
    }
    
    /**
     * Returns the average response time observed.
     *
     * @return average response time
     */
    public double getAverageResponseTime()
    {
        return this.averageResponseTime;
    }
    
    /**
     * Returns the ratio of time jobs were blocked.
     *
     * @return blocked time ratio
     */
    public double getBeBlockedTimeRatio()
    {
        return this.beBlockedTimeRatio;
    }
    
    
    /**
     * Prints a brief summary of this result to the simulator's output using
     * {@code RTSimulator.println}. The output includes the parent's group ID,
     * parent's script ID, this result ID, the workload and processor file
     * names (each appended with ".xml" in the printed output) and each stored
     * power consumption sample on its own line.
     */
    public void showInfo()
    {
        //println("!!");
        println(this.parent.parent.getGroupID()+" - "+this.parent.getID()+" - "+this.ID);
        println("  WorkloadFile : "+this.workloadFile+".xml");
        println("  ProcessorFile : "+this.processorFile+".xml");
        
        for(int i = 0 ; i < this.powerConsumptions.size();i++)
        {
            //println("    Core("+(i+1)+") : "+this.powerConsumptions.get(i));
            println(""+this.powerConsumptions.get(i));
        }
    }
}
