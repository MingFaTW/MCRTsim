/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ResultSet;

import WorkLoad.Job;
import WorkLoad.Task;
import static RTSimulator.Definition.magnificationFactor;

/**
 * Encapsulates information about a missed deadline detected during the simulation.
 * <p>
 * This object stores the time of the deadline miss (in the simulator's internal
 * time units), the {@link Job} that missed its deadline, and the {@link Task}
 * that the job belongs to. The stored time is converted to wall-clock/simulated
 * units by {@link #getMissTime()} which divides the internal time by
 * {@code magnificationFactor} from {@code RTSimulator.Definition}.
 * </p>
 *
 * @author ShiuJia
 */
public class MissDeadlineInfo
{
    private double missTime;
    private Job missJob;
    private Task missTask;
    
    
    /**
     * Creates an empty MissDeadlineInfo with default values.
     * <p>
     * The miss time is initialized to 0 and the job reference to {@code null}.
     * </p>
     */
    public MissDeadlineInfo()
    {
        this.missTime = 0;
        this.missJob = null;
    }
    
    /**
     * Creates a MissDeadlineInfo for a specific deadline miss event.
     *
     * @param t the miss time in the simulator's internal time units (ticks)
     * @param j the {@link Job} that missed its deadline; if non-null, the parent {@link Task}
     *          is captured via {@code j.getParentTask()}
     */
    public MissDeadlineInfo(int t, Job j)
    {
        this.missTime = t;
        this.missJob = j;
        this.missTask = j.getParentTask();
    }
    
    /**
     * Returns the recorded miss time converted from internal time units.
     * <p>
     * The stored {@code missTime} is divided by {@code magnificationFactor}
     * (from {@code RTSimulator.Definition}) to produce the returned value.
     * </p>
     *
     * @return the miss time adjusted by {@code magnificationFactor}
     */
    public double getMissTime()
    {
        return this.missTime / magnificationFactor;
    }
    
    /**
     * Returns the {@link Job} that missed its deadline.
     *
     * @return the job that missed the deadline, or {@code null} if none is set
     */
    public Job getMissJob()
    {
        return this.missJob;
    }
    
    /**
     * Returns the {@link Task} associated with the missed {@link Job}.
     *
     * @return the parent task of the missed job, or {@code null} if none is set
     */
    public Task getMissTask()
    {
        return this.missTask;
    }
}
