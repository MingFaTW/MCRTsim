/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package schedulingAlgorithm.implementation;

import WorkLoad.Job;
import WorkLoad.Priority;
import WorkLoadSet.JobQueue;
import WorkLoadSet.TaskSet;
import RTSimulator.Definition;
import schedulingAlgorithm.SingleCoreSchedulingAlgorithm;

/**
 * <strong>The Earliest-Deadline-First scheduling algorithm (EDF).</strong>
 * <p>
 * This algorithm prioritizes tasks or jobs based on their deadlines, selecting
 * the one with the earliest deadline to execute first.
 * </p>
 *
 * @author ShiuJia
 */
public class EDF extends SingleCoreSchedulingAlgorithm
{
	
    /**
     * Constructs a new EDF scheduler instance.
     *
     * <p>Initializes the algorithm name to a human-readable label and configures
     * the priority type as dynamic, since EDF assigns priorities based on
     * job/task deadlines at runtime.</p>
     */
    public EDF()
    {
        this.setName("Earliest Deadline First Scheduling Algorithm");
        this.setPriorityType(Definition.PriorityType.Dynamic);
    }
    
    /**
     * Calculate and assign priorities for every job in the provided job queue.
     *
     * <p>This method iterates through the supplied {@code jq}, polling each
     * {@link Job} and assigning its current priority to a {@link Priority}
     * constructed from the job's absolute deadline (earlier deadlines map to
     * higher scheduling priority). Polled jobs are added to and returned in a
     * newly created {@link JobQueue}.</p>
     *
     * @param jq the source {@link JobQueue} containing jobs whose priorities will be recalculated;
     *           may be empty but should not be {@code null}
     * @return a new {@link JobQueue} containing the same jobs with updated current priorities
     */
    @Override
    public JobQueue calculatePriority(JobQueue jq)
    {
        JobQueue newJQ = new JobQueue();
        Job j;
        while((j = jq.poll()) != null)
        {
            j.setCurrentProiority(new Priority(j.getAbsoluteDeadline()));
            newJQ.add(j);
        }
        return newJQ;
    }

    /**
     * Calculate and assign priorities for every task in the provided task set.
     *
     * <p>Task-level priority calculation is not implemented in this class.
     * EDF typically operates at the job level (dynamic priorities based on
     * absolute deadlines), so this operation is unsupported here.</p>
     *
     * @param ts the {@link TaskSet} for which task-level priorities would be calculated
     * @throws UnsupportedOperationException always thrown because task-set based priority
     *         calculation is not supported in this implementation
     */
    @Override
    public void calculatePriority(TaskSet ts) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
