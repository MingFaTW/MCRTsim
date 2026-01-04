/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package schedulingAlgorithm;

import SystemEnvironment.Core;
import SystemEnvironment.Processor;
import WorkLoadSet.JobQueue;
import WorkLoadSet.TaskSet;
import java.util.Vector;
import RTSimulator.Definition;

/**
 * Abstract base class for hybrid scheduling algorithms.
 * 
 * @author ShiuJia
 */
public abstract class HybridSchedulingAlgorithm extends PriorityDrivenSchedulingAlgorithm
{
    /**
     * Constructs a new HybridSchedulingAlgorithm instance.
     */
    public HybridSchedulingAlgorithm()
    {
        this.setSchedulingType(Definition.SchedulingType.Hybrid);
    }
    /**
     * Calculate priority for tasks.
     * @param ts the task set
     */
    public abstract void calculatePriority(TaskSet ts);
    /**
     * Calculate priority for job queue.
     * @param jq the job queue
     * @return the prioritized job queue
     */
    public abstract JobQueue calculatePriority(JobQueue jq);
    /**
     * Set the processor global scheduling algorithm.
     * @param p the processor
     */
    public abstract void setProcessorGlobalSchedulingAlgorithm(Processor p);
    /**
     * Set the cores local scheduling algorithm.
     * @param cores the cores
     */
    public abstract void setCoresLocalSchedulingAlgorithm(Vector<Core> cores);
}
