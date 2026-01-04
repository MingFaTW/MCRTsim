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
import schedulingAlgorithm.SingleCoreSchedulingAlgorithm;

/**
 * Rate Monotonic Scheduling Algorithm (RMS).
 *
 * <p>Implements rate-monotonic scheduling for a single core by assigning fixed
 * priorities to tasks based on their periods: shorter period => higher priority.
 * The original file included a brief header and an inline note "Rate-monotonic
 * scheduling", which are integrated here to clarify the class responsibility.</p>
 *
 * @author ShiuJia
 */
public class RMS extends SingleCoreSchedulingAlgorithm
{
    /**
     * Constructs a new RMS instance.
     *
     * <p>Initializes the algorithm name to "Rate Monotonic Scheduling Algorithm"
     * and sets the priority type to {@link Definition.PriorityType#Fixed} so that
     * priorities remain constant once assigned.</p>
     */
    public RMS()
    {
        this.setName("Rate Monotonic Scheduling Algorithm");
        this.setPriorityType(Definition.PriorityType.Fixed);
    }

    /**
     * Calculates and assigns fixed priorities for each task in the given task set
     * according to the Rate Monotonic policy.
     *
     * <p>For each {@link Task} in {@code ts}, this method sets the task's
     * priority based on its period (smaller period results in higher priority).
     * This implements the standard rate-monotonic assignment: priority is a
     * function of the task period.</p>
     *
     * @param ts the {@link TaskSet} containing tasks whose priorities will be
     *           computed and assigned; must not be {@code null}
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
     * Calculates and returns a job queue with priorities assigned according to
     * Rate Monotonic policy.
     *
     * <p>This operation is not implemented in the current class. The original
     * stub throws {@link UnsupportedOperationException} and that behavior is
     * preserved here.</p>
     *
     * @param jq the {@link JobQueue} for which priorities would be calculated
     * @return a {@link JobQueue} with priorities assigned (not implemented)
     * @throws UnsupportedOperationException always, as this method is not yet supported
     */
    @Override
    public JobQueue calculatePriority(JobQueue jq) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
