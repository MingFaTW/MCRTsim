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
 * Global Rate Monotonic Scheduling Algorithm (GRMS).
 *
 * <p>Global, fixed-priority scheduling algorithm that assigns priorities based
 * on task periods (rate-monotonic policy). Tasks with shorter periods receive
 * higher priorities. This implementation configures the algorithm name and
 * sets the scheduler to use fixed priorities.</p>
 *
 * @author ShiuJia
 */
public class GRMS extends GlobalSchedulingAlgorithm
{
    /**
     * Constructs a new GRMS scheduler instance.
     *
     * <p>Initializes the algorithm with a human-readable name and sets the
     * priority handling to {@link RTSimulator.Definition.PriorityType#Fixed},
     * reflecting the rate-monotonic (period-based) fixed-priority policy.</p>
     */
    public GRMS()
    {
        this.setName("Global Rate Monotonic Scheduling Algorithm");
        this.setPriorityType(Definition.PriorityType.Fixed);
    }

    /**
     * Calculate and assign priorities for every task in the provided task set.
     *
     * <p>Iterates through the given {@code ts} and assigns each {@link Task} a
     * {@link Priority} constructed from the task's period. Under Rate Monotonic
     * Scheduling, tasks with smaller periods are given higher priority values.
     * This operation updates each task's priority in-place.</p>
     *
     * @param ts the {@link TaskSet} whose tasks will receive period-based priorities;
     *           must not be {@code null}. Each task's priority is set to a new
     *           {@link Priority} created from {@link Task#getPeriod()}.
     */
    @Override
    public void calculatePriority(TaskSet ts)
    {
        for(Task t : ts)
        {
            t.setPriority(new Priority(t.getPeriod())); 
        }
    }

    /**
     * Calculate and assign priorities for every job in the provided job queue.
     *
     * <p>Job-level priority calculation is not implemented in this class.
     * GRMS focuses on task-level, fixed-priority assignment derived from
     * periods. Calling this method will always result in an
     * {@link UnsupportedOperationException}.</p>
     *
     * @param jq the {@link JobQueue} for which per-job priorities would be calculated (not used)
     * @return never returns normally; this method always throws an exception
     * @throws UnsupportedOperationException always thrown because job-queue based priority
     *         calculation is not supported in this implementation
     */
    @Override
    public JobQueue calculatePriority(JobQueue jq) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
