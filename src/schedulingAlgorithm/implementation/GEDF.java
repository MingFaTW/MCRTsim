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
import schedulingAlgorithm.GlobalSchedulingAlgorithm;

/**
 * Global Earliest Deadline First Scheduling Algorithm (GEDF).
 *
 * <p>GEDF is a global, dynamic-priority scheduling algorithm for real-time
 * systems. It selects the job with the earliest absolute deadline across all
 * cores to execute next. Priorities are assigned based on each job's absolute
 * deadline so that jobs with earlier deadlines receive higher scheduling
 * precedence. This implementation updates each job's current priority using
 * the job's absolute deadline.</p>
 *
 * @author ShiuJia
 */
public class GEDF extends GlobalSchedulingAlgorithm
{
    /**
     * Constructs a new GEDF scheduler instance.
     *
     * <p>This constructor sets a human-readable name for the algorithm and
     * configures the scheduler to use dynamic priorities, since GEDF derives
     * priorities from job deadlines at runtime.</p>
     */
    public GEDF()
    {
        this.setName("Global Earliest Deadline First Scheduling Algorithm");
        this.setPriorityType(Definition.PriorityType.Dynamic);
    }

    /**
     * Recalculates and assigns job-level priorities for the provided job queue.
     *
     * <p>This method iterates through the supplied {@code jq}, polling each
     * {@link Job} and setting its current priority to a new {@link Priority}
     * constructed from the job's absolute deadline. Polled jobs are added to a
     * newly created {@link JobQueue} which is returned. Jobs with earlier
     * absolute deadlines will receive higher scheduling priority values.</p>
     *
     * @param jq the source {@link JobQueue} containing jobs whose current priorities
     *           will be recalculated; must not be {@code null}
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
     * Task-set level priority calculation is not supported for GEDF.
     *
     * <p>GEDF operates at the job level (dynamic priorities based on absolute
     * deadlines). Assigning priorities at the {@link TaskSet} level is not
     * implemented in this class and will always result in an
     * {@link UnsupportedOperationException}.</p>
     *
     * @param ts the {@link TaskSet} for which task-level priorities would be calculated (not used)
     * @throws UnsupportedOperationException always thrown because task-set based priority
     *         calculation is not supported in this implementation
     */
    @Override
    public void calculatePriority(TaskSet ts) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
