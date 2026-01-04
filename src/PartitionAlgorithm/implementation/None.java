/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package PartitionAlgorithm.implementation;

import PartitionAlgorithm.PartitionAlgorithm;
import SystemEnvironment.Core;
import WorkLoad.Task;
import WorkLoadSet.TaskSet;
import java.util.Vector;
import RTSimulator.Definition.SchedulingType;

/**
 * A partition algorithm implementation that performs no partitioning.
 *
 * <p>This algorithm preserves the original file/template comment intent and
 * provides a "none" strategy: when the parent processor's scheduling type is
 * {@code SingleCore} or {@code Partition}, all tasks from the provided
 * {@code TaskSet} are assigned to the first core in the supplied {@code cores}
 * vector (index 0). For other scheduling types no assignment is made by this
 * algorithm.</p>
 *
 * @author ShiuJia
 */
public class None extends PartitionAlgorithm
{
    /**
     * Constructs a new None partition algorithm instance and sets its name to
     * "None".
     */
    public None()
    {
        this.setName("None");
    }
    /**
     * Assigns tasks from {@code taskSet} to cores according to the "None"
     * strategy.
     *
     * <p>This method obtains the scheduling type from the first core's parent
     * processor. If the scheduling type equals {@code SchedulingType.SingleCore}
     * or {@code SchedulingType.Partition}, every task in {@code taskSet} is
     * added to {@code cores.get(0)} via {@code addTask}. No action is taken
     * for other scheduling types.</p>
     *
     * @param cores a vector of available cores; this method may mutate the
     *              vector's elements by calling {@code addTask} on the first core
     * @param taskSet the set of tasks to be assigned
     */
    @Override
    public void taskToCore(Vector<Core> cores, TaskSet taskSet) 
    {
        SchedulingType schedulingType = cores.get(0).getParentProcessor().getSchedulingAlgorithm().getSchedulingType();
        
        if(schedulingType == SchedulingType.SingleCore || schedulingType == SchedulingType.Partition)
        {
            for(Task t : taskSet)
            {
                cores.get(0).addTask(t);
            }
        }
    }
    
}
