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
 * Partitioned implementation of the FCFS scheduler.
 *
 * <p>This class represents a partitioned scheduling algorithm that assigns the
 * First-Come-First-Served (FCFS) local scheduling algorithm to each core.
 * The original file contained the short note: {@code Partitioned {@link FCFS}}.
 * That intent is preserved: under this partitioned strategy, every core uses
 * an independent instance of {@link FCFS} as its local scheduler.</p>
 *
 * @author ShiuJia
 * @see FCFS
 * @see PartitionedSchedulingAlgorithm
 */
public class PFCFS extends PartitionedSchedulingAlgorithm
{
    /**
     * Constructs a new PFCFS instance.
     *
     * <p>Initializes the algorithm name to "Partitioned FCFS", matching the
     * original behavior which sets the display name for this partitioned
     * scheduling algorithm.</p>
     */
    public PFCFS()
    {
        this.setName("Partitioned FCFS");
    }
    
    /**
     * Sets the local scheduling algorithm of each core to a new {@link FCFS} instance.
     *
     * <p>This method iterates over the provided {@code cores} vector and for each
     * {@link Core} invokes {@code c.setLocalSchedAlgorithm(new FCFS())}, ensuring
     * that each core uses FCFS as its local scheduler under this partitioned
     * strategy.</p>
     *
     * @param cores a {@link Vector} of {@link Core} objects whose local scheduling
     *              algorithm will be set; each element will receive a fresh
     *              {@link FCFS} instance. The vector itself is expected to be
     *              non-null when called.
     */
    @Override
    public void setCoresLocalSchedulingAlgorithm(Vector<Core> cores)
    {
        for(Core c : cores)
        {
            c.setLocalSchedAlgorithm(new FCFS());
        }
    }
}
