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
 * Worst\-Fit partitioning algorithm implementation.
 *
 * <p>This class implements a simple Worst\-Fit partitioning strategy: tasks
 * from a provided {@code TaskSet} are assigned one-by-one to the core that
 * currently has the smallest accumulated utilization (i.e., the "worst"
 * or least-loaded core). The implementation creates a working copy of the
 * original {@code TaskSet} (preserving original task order), maintains a
 * per-core utilization accumulator, and mutates the supplied {@code cores}
 * by calling {@code addTask} on selected cores.</p>
 *
 * @author ShiuJia
 */
public class WF extends PartitionAlgorithm
{
    /**
     * Constructs a new WF instance.
     */
    public WF()
    {
        this.setName("Worst-Fit");
    }
    
    /**
     * Assigns tasks from {@code taskSet} to the provided {@code cores} using
     * the Worst-Fit strategy.
     *
     * <p>Algorithm details:</p>
     * <ol>
     *   <li>Create a working copy of {@code taskSet} to iterate without
     *       modifying the original set's structure.</li>
     *   <li>Initialize a {@code coreU} array to hold the accumulated
     *       utilization value for each core (starts at 0).</li>
     *   <li>For each task (in the working copy's order), find the core with
     *       the smallest current value in {@code coreU} and assign the task to
     *       that core via {@code addTask}, then increment the core's
     *       accumulated utilization by {@code task.getUtilization()}.</li>
     * </ol>
     * 
     *
     * @param cores a vector of available cores to which tasks will be assigned;
     *              this method mutates the cores by calling {@code addTask}
     *              on selected cores
     * @param taskSet the set of tasks to partition and assign to cores; a
     *                working copy of this set is created and used by the method
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
