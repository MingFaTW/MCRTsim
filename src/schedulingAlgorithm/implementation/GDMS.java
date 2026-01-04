/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package schedulingAlgorithm.implementation;

import WorkLoad.Priority;
import WorkLoad.Task;
import WorkLoadSet.JobQueue;
import WorkLoadSet.TaskSet;
import RTSimulator.Definition;
import schedulingAlgorithm.GlobalSchedulingAlgorithm;

/**
 * The Global Deadline Monotonic Scheduling Algorithm (GDMS).
 *
 * <p>GDMS is a global, fixed-priority scheduling algorithm that assigns
 * priorities to tasks based on their relative deadlines. Tasks with shorter
 * relative deadlines are given higher priorities to ensure that more
 * time-critical tasks are executed earlier. This implementation configures
 * the algorithm name and sets the priority handling type to {@link Definition.PriorityType#Fixed}.</p>
 *
 * @author ShiuJia
 */
public class GDMS extends GlobalSchedulingAlgorithm
{
	
    /**
     * Constructs a new GDMS scheduler instance.
     *
     * <p>Initializes the algorithm with a human-readable name and sets the
     * priority type to fixed, reflecting the Deadline Monotonic policy where
     * priorities are derived from task relative deadlines.</p>
     */
    public GDMS()
    {
        this.setName("Global Deadline Monotonic Scheduling Algorithm");
        this.setPriorityType(Definition.PriorityType.Fixed);
    }

    /**
     * Calculate and assign priorities for every task in the provided task set.
     *
     * <p>This method iterates through the given {@code ts} and assigns each
     * {@link Task} a {@link WorkLoad.Priority} constructed from the task's
     * relative deadline. According to Deadline Monotonic policy, tasks with
     * smaller relative deadlines receive higher priority values. The operation
     * updates each task's priority in-place.</p>
     *
     * @param ts the {@link TaskSet} whose tasks will receive deadlines-based priorities;
     *           must not be {@code null}. Each task's priority is set to a new {@link Priority}
     *           created from its relative deadline.
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
     * <p>Job-level priority calculation is not implemented in this class.
     * GDMS focuses on task-level, fixed-priority assignment derived from
     * relative deadlines. Calling this method will always result in an
     * {@link UnsupportedOperationException}.</p>
     *
     * @param jq the {@link JobQueue} for which per-job priorities would be calculated
     * @return nothing; this method always throws {@link UnsupportedOperationException}
     * @throws UnsupportedOperationException always thrown because job-queue based priority
     *         calculation is not supported in the current implementation
     */
    @Override
    public JobQueue calculatePriority(JobQueue jq) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
