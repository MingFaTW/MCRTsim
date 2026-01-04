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

/**
 * First\-Fit Decreasing partitioning algorithm implementation.
 *
 * <p>This class implements the First\-Fit Decreasing (FFD) partition algorithm.
 * It preserves the original file/template comment intent and author information.
 * The algorithm creates a working copy of the provided {@code TaskSet}, sorts
 * tasks in decreasing order of utilization, initializes per-core remaining
 * capacity based on each core's maximum frequency divided by the task set's
 * processing speed, and assigns each task to the first core that can
 * accommodate it. If no core can accommodate a task, the implementation
 * falls back to assigning the task to {@code cores.get(0)} (fallback behavior
 * preserved from the original implementation).</p>
 *
 * @author ShiuJia
 */
public class FFD extends PartitionAlgorithm
{
    /**
     * Constructs a First-Fit Decreasing partition algorithm instance and sets
     * the algorithm name to "First-Fit Decreasing".
     */
    public FFD()
    {
        this.setName("First-Fit Decreasing");
    }
    /**
     * Partition and assign tasks from {@code taskSet} to the provided {@code cores}
     * using the First-Fit Decreasing strategy.
     *
     * <p>Algorithm overview:</p>
     * <ol>
     *   <li>Create a working copy of {@code taskSet} (keeps original tasks intact).</li>
     *   <li>Initialize an array {@code coreU} of remaining capacities where each
     *       element is computed as {@code core.getParentCoreSet().getCoreSpeedSet().getMaxFrequencyOfSpeed() / taskSet.getProcessingSpeed()}.</li>
     *   <li>Sort the working task list in decreasing order of task utilization.</li>
     *   <li>For each task (largest first), scan cores in index order and assign the
     *       task to the first core whose remaining capacity is greater than the
     *       task's utilization, then decrement that core's remaining capacity.</li>
     *   <li>If no core can accommodate a task, assign it to {@code cores.get(0)}
     *       and decrement that core's remaining capacity (preserves original fallback behavior).</li>
     * </ol>
     * 
     *
     * @param cores a vector of available cores to which tasks will be assigned;
     *              this method mutates the cores by calling {@code addTask} on them
     * @param taskSet the set of tasks to partition and assign to cores; a working
     *                copy of this set is created and sorted in decreasing utilization
     */
    @Override
    public void taskToCore(Vector<Core> cores, TaskSet taskSet)
    {
        TaskSet decreasingTaskSet = new TaskSet();
        double[] coreU = new double[cores.size()];
        
        for(int i = 0; i < cores.size(); i++)
        {
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
            boolean isAssigned = false;
            for(int i = 0; i < coreU.length; i++)
            {
                if(coreU[i] > decreasingTaskSet.get(0).getUtilization())
                {
                    cores.get(i).addTask(decreasingTaskSet.get(0));
                    coreU[i] -= decreasingTaskSet.get(0).getUtilization();
                    decreasingTaskSet.remove(0);
                    isAssigned = true;
                    break;
                }
            }
            
            if(!isAssigned)
            {
                cores.get(0).addTask(decreasingTaskSet.get(0));
                coreU[0] -= decreasingTaskSet.get(0).getUtilization();
                decreasingTaskSet.remove(0);
                isAssigned = true;
            }
        }
    }
}
