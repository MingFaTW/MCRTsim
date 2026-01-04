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
 * Least Slack Time Scheduling Algorithm (LST).
 *
 * <p>
 * LST selects the job with the least slack time to execute next. Slack time is
 * typically computed as the remaining time until the job's deadline minus the
 * remaining execution time. This implementation sets dynamic priorities based
 * on a job's computed slack: smaller slack values map to higher scheduling
 * precedence by assigning a {@link WorkLoad.Priority} derived from the job's
 * deadline, current core time, and remaining work.
 * </p>
 *
 * @author ShiuJia
 */
public class LST extends SingleCoreSchedulingAlgorithm
{
	
    /**
     * Constructs a new LST scheduler instance.
     *
     * <p>
     * Initializes the algorithm name to a human-readable label and configures
     * the scheduler to use dynamic priorities because LST computes priorities
     * at runtime based on job slack times.
     * </p>
     */
    public LST()
    {
        this.setName("Least Slack Time Scheduling Algorithm");
        this.setPriorityType(Definition.PriorityType.Dynamic);
    }

    /**
     * Recalculates and assigns job-level priorities for the provided job queue
     * according to the Least Slack Time policy.
     *
     * <p>
     * For each {@link Job} polled from {@code jq}, this method computes the
     * remaining work amount as {@code ceil(targetAmount - progressAmount)} and
     * computes slack as {@code absoluteDeadline - currentCore.currentTime - remainingWork}.
     * The job's current priority is then set to a {@link WorkLoad.Priority}
     * constructed from that slack value. Polled jobs are added into and
     * returned in a new {@link JobQueue}. Jobs with smaller slack receive
     * higher scheduling precedence.
     * </p>
     *
     * @param jq the source {@link JobQueue} containing jobs whose priorities will be recalculated;
     *           must not be {@code null}
     * @return a new {@link JobQueue} containing the same jobs with updated current priorities
     */
    @Override
    public JobQueue calculatePriority(JobQueue jq)
    {
        JobQueue newJQ = new JobQueue();
        Job j;
        while((j = jq.poll()) != null)
        {
            long lastAmount = (long) Math.ceil(j.getTargetAmount() - j.getProgressAmount());
            j.setCurrentProiority(new Priority(j.getAbsoluteDeadline() - j.getCurrentCore().getCurrentTime() - lastAmount));
            newJQ.add(j);
        }
        return newJQ;
    }

    /**
     * Task-set level priority calculation is not supported for LST.
     *
     * <p>
     * LST operates at the job level using dynamic slack calculations. Assigning
     * priorities at the {@link TaskSet} level is not implemented in this class.
     * Calling this method will always result in an {@link UnsupportedOperationException}.
     * </p>
     *
     * @param ts the {@link TaskSet} for which task-level priorities would be calculated (not used)
     * @throws UnsupportedOperationException always thrown because task-set based priority
     *         calculation is not supported by this implementation
     */
    @Override
    public void calculatePriority(TaskSet ts) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
