package PartitionAlgorithm.implementation;

import PartitionAlgorithm.PartitionAlgorithm;
import SystemEnvironment.Core;
import WorkLoad.Task;
import WorkLoadSet.TaskSet;
import java.util.Comparator;
import java.util.Vector;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * Best-Fit partitioning implementation.
 *
 * <p>This class implements a Best-Fit partitioning algorithm that assigns tasks
 * from a {@code TaskSet} to a list of {@code Core} instances. The algorithm
 * computes an initial remaining-capacity array (coreU) using each core's
 * maximum frequency and the task set processing speed, then repeatedly sorts
 * cores by their remaining capacity (non-decreasing) and places the next task
 * on the first core with sufficient remaining capacity.</p>
 *
 * @author ShiuJia
 */
public class BF extends PartitionAlgorithm
{
    /**
     * Constructs a Best-Fit partition algorithm instance and sets its name
     * to "Best-Fit".
     */
    public BF()
    {
        this.setName("Best-Fit");
    }
    
    /**
     * Assigns tasks from the given {@code taskSet} to the provided list of
     * {@code cores} using a Best-Fit strategy.
     *
     * <p>Procedure:</p>
     * <ul>
     *   <li>Create a working copy of the task set (decreasingTaskSet).</li>
     *   <li>Compute an initial remaining-capacity array {@code coreU} where each
     *       element is the ratio of the core's maximum frequency to the task set's
     *       processing speed.</li>
     *   <li>While tasks remain, sort the cores in non-decreasing order of their
     *       remaining capacity and assign the next task to the first core whose
     *       remaining capacity is greater than the task's utilization, then
     *       decrement that core's remaining capacity accordingly.</li>
     * </ul>
     * 
     *
     * @param cores a list of available cores to which tasks may be assigned;
     *              the method mutates the cores by calling {@code addTask} on them
     * @param taskSet the set of tasks to partition and assign to cores
     */
    @Override
    public void taskToCore(Vector<Core> cores, TaskSet taskSet)
    {
        TaskSet decreasingTaskSet = new TaskSet();
        Vector<Core> non_decreasingCores = new Vector<Core>();
        double[] coreU = new double[cores.size()];
        for(int i = 0; i < cores.size(); i++)
        {
            non_decreasingCores.add(cores.get(i));
            
            coreU[i] = cores.get(i).getParentCoreSet().getCoreSpeedSet().getMaxFrequencyOfSpeed()
                     / taskSet.getProcessingSpeed();
        }
        
        
        for(Task t : taskSet)
        {
            decreasingTaskSet.add(t);
        }
        
        while(decreasingTaskSet.size() > 0)
        {
            non_decreasingCores.sort
            (
                new Comparator<Core>()
                {
                    @Override
                    public int compare(Core c1, Core c2) 
                    {
                        if(coreU[c1.getID()-1] > coreU[c2.getID()-1])
                        {
                            
                            return 1;
                        }
                        else if(coreU[c1.getID()-1] < coreU[c2.getID()-1])
                        {
                            return -1;
                        }
                        return 0;
                        
                    }
                }
            );
            
            
            for(int i = 0; i < coreU.length; i++)
            {
                if(coreU[i] > decreasingTaskSet.get(0).getUtilization())
                {
                    cores.get(i).addTask(decreasingTaskSet.get(0));
                    coreU[i] -= decreasingTaskSet.get(0).getUtilization();
                    decreasingTaskSet.remove(0);
                    break;
                }
            }
        }
    }
}
