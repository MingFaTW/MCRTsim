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
 * Represents an abstract scheduling algorithm designed for single-core systems.
 * 
 * @author ShiuJia
 * <p>
 * This class extends the {@link PriorityDrivenSchedulingAlgorithm} and sets the scheduling type
 * to {@code Definition.SchedulingType.SingleCore}. It serves as the base class for implementing
 * priority-driven scheduling algorithms that operate in single-core environments.
 * </p>
 */
public abstract class SingleCoreSchedulingAlgorithm extends PriorityDrivenSchedulingAlgorithm
{
    /**
     * Constructs a new SingleCoreSchedulingAlgorithm instance.
     */
    public SingleCoreSchedulingAlgorithm()
    {
        this.setSchedulingType(Definition.SchedulingType.SingleCore);
    }
    /** 
     * Recalculates the priority of the TaskSet.
     * @param ts is a {@link TaskSet}
     * Calculate the priority for fixed-priority.
     */
    public abstract void calculatePriority(TaskSet ts); 
    /**
     * Recalculates the priority of the JobQueue.
     * @param jq is a {@link JobQueue}
     * For dynamic-priority.
     */
    public abstract JobQueue calculatePriority(JobQueue jq); 
}
