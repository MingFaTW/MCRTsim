/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package schedulingAlgorithm;

import SystemEnvironment.Core;
import WorkLoadSet.JobQueue;
import WorkLoadSet.TaskSet;
import java.util.Vector;
import RTSimulator.Definition;

/**
 * Abstract base class for partitioned scheduling algorithms.
 * 
 * @author ShiuJia
 */
public abstract class PartitionedSchedulingAlgorithm extends PriorityDrivenSchedulingAlgorithm
{
    /**
     * Constructs a new PartitionedSchedulingAlgorithm instance.
     */
    public PartitionedSchedulingAlgorithm()
    {
        this.setSchedulingType(Definition.SchedulingType.Partition);
    }
    /**
     * Set the cores local scheduling algorithm.
     * @param cores the cores
     */
    public abstract void setCoresLocalSchedulingAlgorithm(Vector<Core> cores);
}
