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
 * Partitioned {@link RMS} scheduling algorithm implementation.
 *
 * <p>This class represents a partitioned scheduling algorithm that assigns the
 * Rate Monotonic Scheduling (RMS) local scheduler to each core. The original
 * file included the short note "Partitioned {@link RMS}." which is preserved
 * and expanded here to clarify that under this partitioned strategy every core
 * receives its own {@link RMS} instance as the local scheduling algorithm.</p>
 *
 * @author ShiuJia
 * @see RMS
 * @see PartitionedSchedulingAlgorithm
 */
public class PRMS extends PartitionedSchedulingAlgorithm
{
    /**
     * Constructs a new PRMS instance.
     *
     * <p>Initializes the algorithm display name to "Partitioned RMS", matching
     * the original constructor intent to identify this partitioned scheduling
     * strategy.</p>
     */
    public PRMS()
    {
        this.setName("Partitioned RMS");
    }
    
    /**
     * Sets the local scheduling algorithm of each core to a new {@link RMS} instance.
     *
     * <p>This method iterates over the provided {@code cores} vector and for each
     * {@link Core} invokes {@code c.setLocalSchedAlgorithm(new RMS())}, ensuring
     * that every core uses RMS as its local scheduler under this partitioned
     * strategy.</p>
     *
     * @param cores a {@link Vector} of {@link Core} objects whose local scheduling
     *              algorithm will be set; each element will receive a fresh
     *              {@link RMS} instance. The vector is expected to be non-null
     *              when called.
     */
    @Override
    public void setCoresLocalSchedulingAlgorithm(Vector<Core> cores)
    {
        for(Core c : cores)
        {
            c.setLocalSchedAlgorithm(new RMS());
        }
    }
}
