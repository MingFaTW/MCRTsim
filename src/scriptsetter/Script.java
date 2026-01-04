/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package scriptsetter;


import java.util.Vector;
import RTSimulator.RTSimulatorMath;

/**
 * Represents a saved simulation script and its accumulated results.
 *
 * <p>This class encapsulates script configuration values (IDs, file locations,
 * selected algorithms and protocols, simulation time) extracted from a
 * {@link ScriptPanel} and stores a collection of {@link ScriptResult} produced
 * by running the script. The original file contained section markers such as
 * "setValue", "getValue" and "get average result"; those semantics are
 * preserved: methods group into mutators, accessors, and average/result
 * calculators.</p>
 *
 * @author YC
 */
public class Script 
{
    /**
     * The owning {@link ScriptTable} that contains this script entry.
     *
     * <p>Accessed publicly by UI code to update or remove this script from the
     * table.</p>
     */
    public ScriptTable parent;
    private String ID,workloadSite,processorSite,partitionAlgorithm,DVFSMethod,schedAlgorithm,CCProtocol,simulationTime;
            
    private Vector<ScriptResult> scriptResultSet;
    private  RTSimulatorMath math = new RTSimulatorMath();
    
    /**
     * Constructs a new Script instance by reading values from the provided
     * {@link ScriptPanel} and registering the parent table.
     *
     * @param ST the parent {@link ScriptTable} that will contain this script
     * @param SP the {@link ScriptPanel} UI object from which script fields are read
     */
    public Script(ScriptTable ST,ScriptPanel SP)
    {
        this.parent = ST;
        this.scriptResultSet = new Vector<>();
        this.ID = SP.getScriptID();
        this.workloadSite = SP.getWorkloadSite();
        this.processorSite = SP.getProcessorSite();
        this.partitionAlgorithm = SP.getPartitionMethod();
        this.DVFSMethod = SP.getDVFSMethod();
        this.schedAlgorithm = SP.getSchedAlorithm();
        this.CCProtocol = SP.getCCProtocol();
        this.simulationTime = SP.getSimulationTime();
    }

//setValue
    /**
     * Update this script's configuration using values from the given panel.
     *
     * <p>Clears previously stored {@link ScriptResult} entries and copies all
     * configuration fields (ID, workload/processor locations, partition/DVFS
     * methods, scheduling algorithm, concurrency control protocol and
     * simulation time) from {@code SP}.</p>
     *
     * @param SP the {@link ScriptPanel} whose values will overwrite this script's configuration
     */
    public void modifyScript(ScriptPanel SP)
    {
        this.scriptResultSet.removeAllElements();
        this.ID = SP.getScriptID();
        this.workloadSite = SP.getWorkloadSite();
        this.processorSite = SP.getProcessorSite();
        this.partitionAlgorithm = SP.getPartitionMethod();
        this.DVFSMethod = SP.getDVFSMethod();
        this.schedAlgorithm = SP.getSchedAlorithm();
        this.CCProtocol = SP.getCCProtocol();
        this.simulationTime = SP.getSimulationTime();
    }
    
    /**
     * Appends a {@link ScriptResult} produced by running this script.
     *
     * @param SR the {@link ScriptResult} to add
     */
    public void addScriptResult(ScriptResult SR)
    {
        this.scriptResultSet.add(SR);
    }
    
    /**
     * Removes all recorded {@link ScriptResult} entries for this script.
     *
     * <p>Use this to reset accumulated results before re-running or modifying
     * the script configuration.</p>
     */
    public void removeAllScriptResult()
    {
        this.scriptResultSet.removeAllElements();
    }
    
    
    /**
     * Sets the workload source path or identifier for this script.
     *
     * @param s workload site (file path or identifier)
     */
    public void setWorkloadSite(String s)
    {
        this.workloadSite = s;
    }
    
    /**
     * Sets the processor description/source for this script.
     *
     * @param s processor site (file path or identifier)
     */
    public void setProcessorSite(String s)
    {
        this.processorSite = s;
    }
    
    /**
     * Sets the partitioning algorithm name selected for this script.
     *
     * @param s partition algorithm identifier
     */
    public void setPartitionAlgorithm(String s)
    {
        this.partitionAlgorithm = s;
    }
    
    /**
     * Sets the DVFS method name selected for this script.
     *
     * @param s DVFS method identifier
     */
    public void setDVFSMethod(String s)
    {
        this.DVFSMethod = s;
    }
    
    /**
     * Sets the scheduling algorithm name for this script.
     *
     * @param s scheduling algorithm identifier
     */
    public void setSchedulingAlgorithm(String s)
    {
        this.schedAlgorithm = s;
    }
    
    /**
     * Sets the concurrency control protocol name used by this script.
     *
     * @param s concurrency control protocol identifier
     */
    public void setCCProtocol(String s)
    {
        this.CCProtocol = s;
    }
    
    /**
     * Sets the simulation time value for this script.
     *
     * @param s simulation time representation (string)
     */
    public void setSimulationTime(String s)
    {
        this.simulationTime = s;
    }
    
//getValue
    
    /**
     * Returns the script identifier.
     *
     * @return the script ID string
     */
    public String getID()
    {
        return this.ID;
    }
    
    /**
     * Returns the configured workload site for this script.
     *
     * @return workload site string
     */
    public String getWorkloadSite()
    {
        return this.workloadSite;
    }
    
    /**
     * Returns the configured processor site for this script.
     *
     * @return processor site string
     */
    public String getProcessorSite()
    {
        return this.processorSite;
    }
    
    /**
     * Returns the chosen partition algorithm name.
     *
     * @return partition algorithm identifier
     */
    public String getPartitionAlgorithm()
    {
        return this.partitionAlgorithm;
    }
    
    /**
     * Returns the chosen DVFS method name.
     *
     * @return DVFS method identifier
     */
    public String getDVFSMethod()
    {
        return this.DVFSMethod;
    }
    
    /**
     * Returns the chosen scheduling algorithm name.
     *
     * @return scheduling algorithm identifier
     */
    public String getSchedulingAlgorithm()
    {
        return this.schedAlgorithm;
    }
    
    /**
     * Returns the chosen concurrency control protocol name.
     *
     * @return concurrency control protocol identifier
     */
    public String getCCProtocol()
    {
        return this.CCProtocol;
    }
    
    /**
     * Returns the simulation time configured for this script.
     *
     * @return simulation time string
     */
    public String getSimulationTime()
    {
        return this.simulationTime;
    }
    
    /**
     * Returns the collection of {@link ScriptResult} accumulated for this script.
     *
     * @return vector of {@link ScriptResult}
     */
    public Vector<ScriptResult> getScriptResultSet()
    {
        return this.scriptResultSet;
    }
    
    //get average result
    /**
     * Returns the number of workload runs recorded for this script.
     *
     * @return count of {@link ScriptResult} entries
     */
    public int getWorkloadCount()
    {
        return this.scriptResultSet.size();
    }
    
    /**
     * Counts how many recorded runs were schedulable.
     *
     * @return number of schedulable runs
     */
    public int getSchedulableCount()
    {
        int n = 0;
        for(int i = 0 ; i<this.scriptResultSet.size() ; i++)
        {
            if(this.scriptResultSet.get(i).isSchedulable)
            {
                n += 1;
            }
        }
        return n;
    }
            
    /**
     * Counts how many recorded runs were not schedulable.
     *
     * @return number of non-schedulable runs
     */
    public int getNonSchedulableCount()
    {
        int n = 0;
        for(int i = 0 ; i<this.scriptResultSet.size() ; i++)
        {
            if(!this.scriptResultSet.get(i).isSchedulable)
            {
                n += 1;
            }
        }
        return n;
    }
    
    /**
     * Computes the average task count across all recorded results.
     *
     * @return average task count (formatted to 5 decimal places via {@link RTSimulatorMath})
     */
    public double getAverageTaskCount()
    {
        double p = 0;
        
        for(int i = 0 ; i<this.scriptResultSet.size() ; i++)
        {
            p = RTSimulatorMath.add(p,this.scriptResultSet.get(i).getTaskCount());
        }
        
        return math.changeDecimalFormatFor5(RTSimulatorMath.div(p,this.scriptResultSet.size()));
    }
    
    /**
     * Computes the average total power consumption across recorded runs.
     *
     * @return average power consumption (formatted)
     */
    public double getAveragePowerConsumption()
    {
        double p = 0;
        
        for(int i = 0 ; i<this.scriptResultSet.size() ; i++)
        {
            p = RTSimulatorMath.add(p,this.scriptResultSet.get(i).getTotalPowerConsumption());
        }
        
        return math.changeDecimalFormatFor5(RTSimulatorMath.div(p,this.scriptResultSet.size()));                
    }
    
    /**
     * Computes the average number of completed jobs across recorded runs.
     *
     * @return average completed job count (formatted)
     */
    public double getAverageJobCompeletedCount()
    {
        double p = 0;
        
        for(int i = 0 ; i<this.scriptResultSet.size() ; i++)
        {
            p = RTSimulatorMath.add(p,this.scriptResultSet.get(i).getTotalJobCompeletedCount());
        }
        
        return math.changeDecimalFormatFor5(RTSimulatorMath.div(p,this.scriptResultSet.size()));                
    }
    
    /**
     * Computes the average number of jobs that missed deadlines across runs.
     *
     * @return average missed-deadline job count (formatted)
     */
    public double getAverageJobMissDeadlineCount()
    {
        double p = 0;
        
        for(int i = 0 ; i<this.scriptResultSet.size() ; i++)
        {
            p = RTSimulatorMath.add(p,this.scriptResultSet.get(i).getTotalJobMissDeadlineCount());
        }
        
        return math.changeDecimalFormatFor5(RTSimulatorMath.div(p,this.scriptResultSet.size()));                
    }
    
    /**
     * Computes the average completed ratio across recorded runs.
     *
     * @return average completed ratio (formatted)
     */
    public double getAverageCompletedRatio()
    {
        double p = 0;
        
        for(int i = 0 ; i<this.scriptResultSet.size() ; i++)
        {
            p = RTSimulatorMath.add(p,this.scriptResultSet.get(i).getCompletedRatio());
        }
        
        return math.changeDecimalFormatFor5(RTSimulatorMath.div(p,this.scriptResultSet.size()));                
    }
    
    /**
     * Computes the average deadline miss ratio across recorded runs.
     *
     * @return average deadline miss ratio (formatted)
     */
    public double getAverageDeadlineMissRatio()
    {
        double p = 0;
        
        for(int i = 0 ; i<this.scriptResultSet.size() ; i++)
        {
            p = RTSimulatorMath.add(p,this.scriptResultSet.get(i).getDeadlineMissRatio());
        }
        
        return math.changeDecimalFormatFor5(RTSimulatorMath.div(p,this.scriptResultSet.size()));                
    }
    
    
    /**
     * Computes the average pending time across recorded runs.
     *
     * @return average pending time (formatted)
     */
    public double getAveragePendingTime()
    {
        double p = 0;
        
        for(int i = 0 ; i<this.scriptResultSet.size() ; i++)
        {
            p = RTSimulatorMath.add(p,this.scriptResultSet.get(i).getAveragePendingTime());
        }
        return math.changeDecimalFormatFor5(RTSimulatorMath.div(p,this.scriptResultSet.size()));                
    }
    
    /**
     * Computes the average response time across recorded runs.
     *
     * @return average response time (formatted)
     */
    public double getAverageResponseTime()
    {
        double p = 0;
        
        for(int i = 0 ; i<this.scriptResultSet.size() ; i++)
        {
            p = RTSimulatorMath.add(p,this.scriptResultSet.get(i).getAverageResponseTime());
        }
        
        return math.changeDecimalFormatFor5(RTSimulatorMath.div(p,this.scriptResultSet.size()));                
    }
    
    /**
     * Computes the average maximum critical section ratio across recorded runs.
     *
     * @return average maximum critical section ratio (formatted)
     */
    public double getAverageMaximumCriticalSectionRatio()
    {
        double p = 0;
        
        for(int i = 0 ; i<this.scriptResultSet.size() ; i++)
        {
            p = RTSimulatorMath.add(p,this.scriptResultSet.get(i).getMaximumCriticalSectionRatio());
        }
        
        return math.changeDecimalFormatFor5(RTSimulatorMath.div(p,this.scriptResultSet.size()));    
    }
    
    /**
     * Computes the average actual critical section ratio across recorded runs.
     *
     * @return average actual critical section ratio (formatted)
     */
    public double getAverageActualCriticalSectionRatio()
    {
        double p = 0;
        
        for(int i = 0 ; i<this.scriptResultSet.size() ; i++)
        {
            p = RTSimulatorMath.add(p,this.scriptResultSet.get(i).getActualCriticalSectionRatio());
        }
        
        return math.changeDecimalFormatFor5(RTSimulatorMath.div(p,this.scriptResultSet.size()));    
    }
    
    /**
     * Computes the average maximum utilization reported across runs.
     *
     * @return average maximum utilization (formatted)
     */
    public double getAverageMaximumUtilization()
    {
        double p = 0;
        
        for(int i = 0 ; i<this.scriptResultSet.size() ; i++)
        {
            p = RTSimulatorMath.add(p,this.scriptResultSet.get(i).getMaximumUtilization());
        }
        
        return math.changeDecimalFormatFor5(RTSimulatorMath.div(p,this.scriptResultSet.size())); 
    }
    
    /**
     * Computes the average actual utilization across recorded runs.
     *
     * @return average actual utilization (formatted)
     */
    public double getAverageActualUtilization()
    {
        double p = 0;
        
        for(int i = 0 ; i<this.scriptResultSet.size() ; i++)
        {
            p = RTSimulatorMath.add(p,this.scriptResultSet.get(i).getActualUtilization());
        }
        
        return math.changeDecimalFormatFor5(RTSimulatorMath.div(p,this.scriptResultSet.size()));    
    }
    
    /**
     * Computes the average ratio of time tasks were blocked across runs.
     *
     * @return average blocked-time ratio (formatted)
     */
    public double getAverageActualBeBlockedTimeRatio()
    {
        double p = 0;
        
        for(int i = 0 ; i<this.scriptResultSet.size() ; i++)
        {
            p = RTSimulatorMath.add(p,this.scriptResultSet.get(i).getBeBlockedTimeRatio());
        }
        
        return math.changeDecimalFormatFor5(RTSimulatorMath.div(p,this.scriptResultSet.size()));  
    }
}
