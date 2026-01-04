/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package schedulingAlgorithm;

import WorkLoadSet.JobQueue;
import WorkLoadSet.TaskSet;
import RTSimulator.Definition;

/**
 * Abstract base class for global scheduling algorithms.
 * 
 * @author ShiuJia
 */
public abstract class GlobalSchedulingAlgorithm extends PriorityDrivenSchedulingAlgorithm
{
    /**
     * Constructs a new GlobalSchedulingAlgorithm instance.
     */
    public GlobalSchedulingAlgorithm()
    {
        this.setSchedulingType(Definition.SchedulingType.Global);
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
}
