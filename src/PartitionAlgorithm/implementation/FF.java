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
 * First-Fit partitioning algorithm implementation.
 *
 * <p>This class implements a First-Fit partition algorithm that assigns tasks
 * from a {@code TaskSet} to available {@code Core} instances. It initializes
 * per-core remaining capacity using each core's maximum frequency divided by
 * the task set processing speed, then iterates tasks in their original order
 * and places each task on the first core whose remaining capacity can
 * accommodate the task's utilization.</p>
 *
 * <p>When no core has sufficient capacity, the implementation falls back to
 * assigning the task to {@code cores.get(0)} (fallback behavior preserved
 * from the original implementation). Existing file/template comments are
 * preserved in intent; the author tag is retained from the original file.</p>
 *
 * @author ShiuJia
 */
public class FF extends PartitionAlgorithm
{
    /**
     * Constructs a First-Fit partition algorithm instance and sets its name to
     * "First-Fit".
     */
    public FF()
    {
        this.setName("First-Fit");
    }
    
    /**
     * Assigns tasks from the given {@code taskSet} to the provided list of
     * {@code cores} using the First-Fit strategy.
     *
     * <p>Behavior details:</p>
     * <ol>
     *   <li>Create a working copy of the provided {@code taskSet} preserving the
     *       original order of tasks.</li>
     *   <li>Compute an initial remaining-capacity array {@code coreU} where each
     *       element equals the core's maximum frequency divided by the task set's
     *       processing speed.</li>
     *   <li>For each task in the working list, scan cores in index order and
     *       assign the task to the first core with sufficient remaining capacity,
     *       decrementing that core's remaining capacity accordingly.</li>
     *   <li>If no core can accommodate the task, assign it to {@code cores.get(0)}
     *       as a fallback (preserved behavior).</li>
     * </ol>
     * 
     *
     * @param cores a vector of available cores to which tasks may be assigned;
     *              the method mutates the cores by calling {@code addTask} on them
     * @param taskSet the set of tasks to partition and assign to cores
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
