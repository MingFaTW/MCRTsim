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
 * Worst-Fit Decreasing (WFD) partitioning algorithm implementation.
 *
 * <p>
 * This class implements the Worst-Fit Decreasing strategy: it creates a
 * working copy of the provided {@code TaskSet}, sorts the tasks in decreasing
 * order of utilization, and iteratively assigns each task to the currently
 * least-loaded core (the "worst" core) according to a per-core accumulator
 * ({@code coreU}).
 * </p>
 *
 * <p>Behavior summary:
 * <ul>
 *   <li>A working {@code TaskSet} is created from the input {@code taskSet} so
 *       the original set is not reordered.</li>
 *   <li>Tasks are sorted in decreasing order of utilization (largest first).</li>
 *   <li>Each task is assigned to the core with minimum accumulated utilization
 *       stored in {@code coreU}, then that core's accumulator is incremented
 *       by the task's utilization.</li>
 *   <li>The method mutates the provided {@code cores} by invoking {@code addTask}.</li>
 * </ul>
 *
 *
 * <p>
 * The header comments from the original file (license/template notes) are
 * preserved in the file top block; this class Javadoc retains and expands on
 * the original author information and intended purpose.
 * </p>
 *
 * @author ShiuJia
 */
public class WFD extends PartitionAlgorithm
{
	
    /**
     * Constructs a Worst-Fit Decreasing partition algorithm instance.
     *
     * <p>
     * Sets the human-readable name of the partition algorithm to
     * "Worst-Fit Decreasing". This constructor performs no other logic.
     * </p>
     */
    public WFD()
    {
        this.setName("Worst-Fit Decreasing");
    }
    
    /**
     * Partition and assign tasks from {@code taskSet} to the provided {@code cores}
     * using the Worst-Fit Decreasing strategy.
     *
     * <p>Detailed steps performed by this method:</p>
     * <ol>
     *   <li>Create a working copy {@code decreasingTaskSet} and copy all tasks
     *       from {@code taskSet} into it to preserve the original ordering in
     *       the caller's {@code TaskSet}.</li>
     *   <li>Initialize {@code coreU} as a zeroed array representing the current
     *       accumulated utilization for each core.</li>
     *   <li>Sort {@code decreasingTaskSet} in decreasing order by
     *       {@code Task#getUtilization()}.</li>
     *   <li>While tasks remain, find the core index with the smallest
     *       {@code coreU} value, assign the first task in the sorted list to that
     *       core via {@code Core#addTask}, add the task's utilization to that
     *       core's accumulator, and remove the task from the working set.</li>
     * </ol>
     * 
     *
     * <p>Notes:</p>
     * <ul>
     *   <li>The method mutates the passed {@code cores} vector by calling
     *       {@code addTask} on selected {@code Core} instances.</li>
     *   <li>The original {@code taskSet} passed by the caller is not reordered;
     *       a separate working copy is used.</li>
     * </ul>
     * 
     *
     * @param cores a {@code Vector<Core>} of available cores; this method will
     *              mutate the cores by calling {@code Core#addTask} on selected cores.
     * @param taskSet the {@code TaskSet} of tasks to be partitioned and assigned;
     *                a working copy of this set is created and sorted in
     *                decreasing utilization.
     */
    @Override
    public void taskToCore(Vector<Core> cores, TaskSet taskSet)
    {
        TaskSet decreasingTaskSet = new TaskSet();
        double[] coreU = new double[cores.size()];
        for(int i = 0; i < cores.size(); i++)
        {
            coreU[i] = 0;
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
            int maxCID = 0;
            for(int i = 0; i < coreU.length; i++)
            {
                if(coreU[i] < coreU[maxCID])
                {
                    maxCID = i;
                }
            }
            
            cores.get(maxCID).addTask(decreasingTaskSet.get(0));
            coreU[maxCID] += decreasingTaskSet.get(0).getUtilization();
            decreasingTaskSet.remove(0);
        }
    }
}
