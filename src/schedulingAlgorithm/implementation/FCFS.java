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
 * The First-Come First-Served scheduling algorithm (FCFS).
 *
 * <p>FCFS is one of the simplest scheduling algorithms in which tasks or jobs
 * are executed in the order they arrive. It operates on a "first-come,
 * first-served" basis, with no preemption. This implementation assigns dynamic
 * priorities based on job release times so that earlier arrivals receive higher
 * scheduling priority values (lower numeric release times map to higher
 * scheduling precedence via the created {@link WorkLoad.Priority}).</p>
 *
 * @author ShiuJia
 */
public class FCFS extends SingleCoreSchedulingAlgorithm
{
    /**
     * Constructs a new FCFS scheduler instance.
     *
     * <p>Initializes the algorithm name to "First-Come First-Served" and sets the
     * priority type to {@link RTSimulator.Definition.PriorityType#Dynamic}
     * because priorities are derived from job arrival times at runtime.</p>
     */
    public FCFS()
    {
        this.setName("First-Come First-Served");
        this.setPriorityType(Definition.PriorityType.Dynamic);
    }

    /**
     * Recalculates job-level priorities for the provided job queue according to FCFS.
     *
     * <p>This method iterates through {@code jq}, polling each {@link Job} and
     * setting its current priority to a new {@link WorkLoad.Priority} constructed
     * from the job's release time. Polled jobs are added into a new
     * {@link JobQueue} which is returned. Effectively, jobs with earlier release
     * times will receive higher scheduling precedence.</p>
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
            j.setCurrentProiority(new Priority(j.getReleaseTime()));
            newJQ.add(j);
        }
        return newJQ;
    }

    /**
     * Task-set level priority calculation is not supported for FCFS.
     *
     * <p>FCFS operates at the job arrival level rather than assigning static
     * priorities to tasks in a {@link TaskSet}. This method throws
     * {@link UnsupportedOperationException} to indicate that task-level priority
     * calculation is not implemented in this class.</p>
     *
     * @param ts the {@link TaskSet} for which priorities would be calculated (not used)
     * @throws UnsupportedOperationException always thrown because task-set based priority
     *         calculation is not supported by this implementation
     */
    @Override
    public void calculatePriority(TaskSet ts) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
