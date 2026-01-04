/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package schedulingAlgorithm.implementation;

import PartitionAlgorithm.PartitionAlgorithm;
import SystemEnvironment.Core;
import WorkLoadSet.JobQueue;
import WorkLoadSet.TaskSet;
import java.util.Vector;
import schedulingAlgorithm.PartitionedSchedulingAlgorithm;

/**
 * Partitioned implementation of the LST scheduler.
 *
 * <p>Represents a partitioned scheduling algorithm that assigns an independent
 * local LST (Least Slack Time) scheduler to each core. This integrates the
 * original class note "Partitioned {@link LST}." and clarifies the class
 * responsibility within the {@code schedulingAlgorithm.implementation} package.</p>
 *
 * @author ShiuJia
 * @see LST
 * @see PartitionedSchedulingAlgorithm
 */
public class PLST extends PartitionedSchedulingAlgorithm
{
	
    /**
     * Constructs a new PLST instance.
     *
     * <p>Initializes the algorithm display name to "Partitioned LST", preserving
     * the original constructor intent to identify this partitioned scheduling
     * strategy.</p>
     */
    public PLST()
    {
        this.setName("Partitioned LST");
    }
    
    /**
     * Assigns the LST local scheduling algorithm to each core.
     *
     * <p>Iterates over the provided {@code cores} vector and sets each core's
     * local scheduler to a new {@link LST} instance by invoking
     * {@code c.setLocalSchedAlgorithm(new LST())}. Under this partitioned
     * strategy, every core operates with its own LST scheduler.</p>
     *
     * @param cores a {@link Vector} of {@link Core} objects whose local
     *              scheduling algorithm will be set; must not be {@code null}.
     *              Each element receives a new {@link LST} instance.
     */
    @Override
    public void setCoresLocalSchedulingAlgorithm(Vector<Core> cores)
    {
        for(Core c : cores)
        {
            c.setLocalSchedAlgorithm(new LST());
        }
    }
}
