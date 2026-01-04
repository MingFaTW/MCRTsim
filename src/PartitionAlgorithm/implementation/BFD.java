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
import java.util.Comparator;
import java.util.Vector;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * Best-Fit Decreasing partitioning algorithm implementation.
 *
 * <p>This class implements the Best\-Fit Decreasing (BFD) partitioning strategy.
 * It first creates a working copy of the provided {@code TaskSet}, sorts tasks
 * in decreasing order of utilization, and then repeatedly assigns each task to
 * the first core (when cores are ordered by non\-decreasing remaining capacity)
 * that can accommodate the task. Remaining capacity per core is initialized
 * using each core's maximum frequency divided by the task set processing speed.</p>
 *
 * <p>Existing file-level comments are preserved. This implementation mutates
 * the provided {@code cores} by calling {@code addTask} on them.</p>
 *
 * @author ShiuJia
 */
public class BFD extends PartitionAlgorithm
{
    /**
     * Constructs a Best\-Fit Decreasing partition algorithm instance and sets
     * the algorithm name to "Best-Fit".
     */
    public BFD()
    {
        this.setName("Best-Fit");
    }
    
    /**
     * Assigns tasks from the given {@code taskSet} to the provided list of
     * {@code cores} using the Best\-Fit Decreasing strategy.
     *
     * <p>Procedure:</p>
     * <ol>
     *   <li>Create a working copy of the task set and sort it in decreasing
     *       order of task utilization.</li>
     *   <li>Initialize a remaining-capacity array {@code coreU} where each
     *       element is computed as the core's maximum frequency divided by the
     *       task set processing speed.</li>
     *   <li>While tasks remain, sort the cores in non\-decreasing order of
     *       their remaining capacity and place the next highest-utilization
     *       task on the first core with sufficient remaining capacity, then
     *       decrement that core's remaining capacity accordingly.</li>
     * </ol>
     * 
     *
     * @param cores a vector of available cores to which tasks may be assigned;
     *              this method mutates the cores by calling {@code addTask} on them
     * @param taskSet the set of tasks to partition and assign to cores; a sorted
     *                working copy is created and used by this method
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
        
        decreasingTaskSet.sort
        (
            new Comparator<Task>()
            {
                @Override
                public int compare(Task t1, Task t2)
                {
                    if(t1.getUtilization() > t2.getUtilization())
                    {
                        return -1;
                    }
                    else if(t1.getUtilization() <= t2.getUtilization())
                    {
                        return 1;
                    }
                    return 0;
                }
            }
        );
        
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
