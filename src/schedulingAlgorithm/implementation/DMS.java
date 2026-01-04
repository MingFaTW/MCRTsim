/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package schedulingAlgorithm.implementation;

import WorkLoad.Job;
import WorkLoad.Priority;
import WorkLoad.Task;
import WorkLoadSet.JobQueue;
import WorkLoadSet.TaskSet;
import RTSimulator.Definition;
import schedulingAlgorithm.SingleCoreSchedulingAlgorithm;

/**
 * The Deadline Monotonic Scheduling Algorithm (DMS).
 *
 * <p>DMS is a fixed-priority scheduling algorithm specifically designed for
 * hard real-time systems. It assigns priorities to tasks based on their
 * relative deadlines: tasks with shorter deadlines are given higher priorities.
 * This implementation sets the algorithm name and marks the priority type as
 * fixed during construction.</p>
 *
 * @author ShiuJia
 */
public class DMS extends SingleCoreSchedulingAlgorithm
{
    /**
     * Constructs a new DMS scheduler instance.
     *
     * <p>The constructor sets a human-readable name for this algorithm and
     * configures it to use fixed priorities (as required by Deadline Monotonic
     * scheduling).</p>
     */
    public DMS()
    {
        this.setName("Deadline Monotonic Scheduling Algorithm");
        this.setPriorityType(Definition.PriorityType.Fixed);
    }

    /**
     * Calculate and assign priorities for every task in the provided task set.
     *
     * <p>For each {@link Task} in {@code ts}, this method assigns a
     * {@link WorkLoad.Priority} constructed from the task's relative deadline.
     * According to Deadline Monotonic policy, tasks with smaller relative
     * deadlines receive higher priority values.</p>
     *
     * @param ts the {@link TaskSet} whose tasks will receive deadlines-based priorities;
     *           must not be {@code null}. Each task's priority will be set to a new
     *           {@link Priority} created from its relative deadline.
     */
    @Override
    public void calculatePriority(TaskSet ts)
    {
        for(Task t : ts)
        {
            t.setPriority(new Priority(t.getRelativeDeadline())); 
            
        }
    }

    /**
     * Calculate and assign priorities for every job in the provided job queue.
     *
     * <p>Not implemented in this class: job-level priority calculation is not
     * supported yet and will throw {@link UnsupportedOperationException}.</p>
     *
     * @param jq the {@link JobQueue} for which per-job priorities would be calculated
     * @return a {@link JobQueue} with updated priorities (not implemented)
     * @throws UnsupportedOperationException always thrown because this operation is
     *         not supported in the current implementation
     */
    @Override
    public JobQueue calculatePriority(JobQueue jq) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
