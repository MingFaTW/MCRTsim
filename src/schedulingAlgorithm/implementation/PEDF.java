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
 * Partitioned EDF scheduling algorithm implementation.
 *
 * <p>Partitioned {@link EDF}. This class represents a partitioned scheduling
 * algorithm that assigns the Earliest Deadline First (EDF) local scheduling
 * algorithm to each core in the system. The original file included an IDE
 * template header and a short class note; that intent is preserved and
 * expanded here to clarify the class responsibility within the
 * schedulingAlgorithm.implementation package.</p>
 *
 * @author ShiuJia
 * @see EDF
 * @see PartitionedSchedulingAlgorithm
 */
public class PEDF extends PartitionedSchedulingAlgorithm
{
	
    /**
     * Constructs a new PEDF instance.
     *
     * <p>Initializes the algorithm name to "Partitioned EDF", matching the
     * original constructor behavior which sets the display name for this
     * partitioned scheduling algorithm.</p>
     */
    public PEDF()
    {
        this.setName("Partitioned EDF");
    }
    
    /**
     * Sets each core's local scheduling algorithm to a new {@link EDF} instance.
     *
     * <p>This method iterates over the provided {@code cores} vector and for
     * each {@link Core} invokes {@code c.setLocalSchedAlgorithm(new EDF())},
     * ensuring that each core uses EDF as its local scheduler under this
     * partitioned strategy.</p>
     *
     * @param cores a {@link Vector} of {@link Core} objects whose local
     *              scheduling algorithm will be set; must not be {@code null}.
     *              Each element in the vector will receive a fresh {@link EDF}
     *              instance as its local scheduler.
     */
    @Override
    public void setCoresLocalSchedulingAlgorithm(Vector<Core> cores)
    {
        for(Core c : cores)
        {
            c.setLocalSchedAlgorithm(new EDF());
        }
    }
}
