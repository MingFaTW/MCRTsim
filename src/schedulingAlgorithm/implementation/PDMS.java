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
 * Partitioned implementation of the DMS scheduler.
 *
 * <p>This class represents a partitioned scheduling algorithm that assigns the
 * Distributed Minimum Slack (DMS) local scheduling algorithm to each core.
 * The original file included a short description: "<strong>Partitioned {@link DMS}.</strong>",
 * which is preserved and expanded here to explain the class responsibility.</p>
 *
 * @author ShiuJia
 * @see DMS
 */
public class PDMS extends PartitionedSchedulingAlgorithm
{
	
    /**
     * Constructs a new PDMS instance.
     *
     * <p>Initializes the algorithm name to "Partitioned DMS". This mirrors the
     * original constructor behavior which set the display name for this
     * partitioned scheduling algorithm.</p>
     */
    public PDMS()
    {
        this.setName("Partitioned DMS");
    }
    
    /**
     * Assigns the local scheduling algorithm for each core to a new {@link DMS} instance.
     *
     * <p>This method iterates over the provided {@code cores} collection and sets
     * each core's local scheduler using {@code c.setLocalSchedAlgorithm(new DMS())}.
     * It integrates the original implementation intent: every core in the partitioned
     * scheduler should use DMS as its local scheduling algorithm.</p>
     *
     * @param cores a {@link Vector} of {@link Core} objects whose local scheduling
     *              algorithm will be set; must not be {@code null}. Each element
     *              in the vector will receive a new {@link DMS} instance.
     */
    @Override
    public void setCoresLocalSchedulingAlgorithm(Vector<Core> cores)
    {
        for(Core c : cores)
        {
            c.setLocalSchedAlgorithm(new DMS());
        }
    }
}
