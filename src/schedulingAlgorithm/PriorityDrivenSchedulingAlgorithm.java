/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package schedulingAlgorithm;

import WorkLoadSet.JobQueue;
import WorkLoadSet.TaskSet;
import RTSimulator.Definition.PriorityType;
import RTSimulator.Definition.SchedulingType;

/**
 * Base class for all priority-driven scheduling algorithms.
 * <p>Provides common properties such as a human-readable name, scheduling type,
 * and priority type, with setters/getters for each. Subclasses override
 * {@link #calculatePriority(WorkLoadSet.TaskSet)} and
 * {@link #calculatePriority(WorkLoadSet.JobQueue)} to implement concrete
 * strategies.</p>
 *
 * @author ShiuJia
 */
public class PriorityDrivenSchedulingAlgorithm
{
    private String name;
    private SchedulingType schedulingType;
    private PriorityType priorityType;
    
    /**
     * Constructs a new PriorityDrivenSchedulingAlgorithm instance.
     */
    public PriorityDrivenSchedulingAlgorithm()
    {
        this.name = null;
        this.priorityType = null;
        this.schedulingType = null;
    }
    /**
     * Sets the display name of this scheduling algorithm.
     * @param n a String for name
     */
    public void setName(String n)
    {
        this.name = n;
    }
    
    /**
     * <p>This function setting the scheduling type for PriorityDrivenSchedulingAlgorithm.
     * @param t The scheduling type.
     */
    public void setSchedulingType(SchedulingType t)
    {
        this.schedulingType = t;
    }
    
    /**
     * <p>This function setting the priority type for PriorityDrivenSchedulingAlgorithm.
     * @param t The priority type.
     */
    public void setPriorityType(PriorityType t)
    {
        this.priorityType = t;
    }
    /**
     * <p>This function return the name for the priority driven scheduling algorithm.
     * @return name The name of priority driven scheduling algorithm.
     */
    public String getName()
    {
        return this.name;
    }
    /**
     * <p>This function return the scheduling type for the priority driven scheduling algorithm.
     * @return schedulingType The scheduling type of priority driven scheduling algorithm.
     */
    public SchedulingType getSchedulingType()
    {
        return this.schedulingType;
    }
    /**
     * <p>This function return the priority type 
     * @return priorityType The priority type of priority driven scheduling algorithm.
     */
    public PriorityType getPriorityType()
    {
        return this.priorityType;
    }
    /**
     * Calculates priorities for a set of tasks (fixed-priority algorithms may precompute here).
     * @param ts the task set to process
     */
    public void calculatePriority(TaskSet ts){}
    
    /**
     * Calculates or updates priorities for a queue of jobs.
     * @param jq the job queue to process
     * @return a job queue containing jobs with updated priorities (or null if not applicable)
     */
    public JobQueue calculatePriority(JobQueue jq)
    {
        return null;
    }
}