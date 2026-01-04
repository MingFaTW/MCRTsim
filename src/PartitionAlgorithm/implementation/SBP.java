/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package PartitionAlgorithm.implementation;

import PartitionAlgorithm.PartitionAlgorithm;
import SystemEnvironment.Core;
import WorkLoad.SharedResource;
import WorkLoad.Task;
import WorkLoadSet.TaskSet;
import java.util.Vector;
import static RTSimulator.RTSimulator.println;

/**
 * Similarity\-Based Partitioning (SBP) algorithm implementation.
 *
 * <p>This partition algorithm groups tasks according to shared resources and
 * assigns groups (pairs and transitive expansions) to processor cores to
 * reduce inter\-core shared\-resource contention. The algorithm:
 * <ol>
 *   <li>Computes the total utilization of the provided {@code taskSet} and the
 *       target per\-core utilization {@code U*} (average per core).</li>
 *   <li>Builds similarity entries for every task pair (tasks that share
 *       one or more {@code SharedResource}).</li>
 *   <li>Repeatedly selects the task pair (or group) with the maximum number
 *       of shared resources and assigns the involved tasks to a core with
 *       currently minimal temporary utilization, maintaining per\-core
 *       temporary utilization in {@code tempU}.</li>
 *   <li>If no similarity remains, assigns any leftover tasks to the currently
 *       least loaded core.</li>
 * </ol>
 *
 *
 * <p>Existing template/file header comments are preserved in intent and the
 * original author tag is retained.</p>
 *
 * @author ShiuJia
 */
public class SBP extends PartitionAlgorithm
{
    /**
     * Represents similarity information for a pair (or group) of tasks.
     *
     * <p>Each instance contains:
     * <ul>
     *   <li>{@code similarityRes} - the list of {@code SharedResource} objects
     *       that are common to the tasks in the pair/group.</li>
     *   <li>{@code similarityTask} - the {@code TaskSet} containing the tasks
     *       that participate in this similarity entry (typically two tasks).</li>
     * </ul>
     */
    public class Similarity
    {
        /**
         * Shared resources common to the tasks represented by this similarity
         * entry.
         */
        public Vector<SharedResource> similarityRes = new Vector<SharedResource>();
        /**
         * The tasks participating in this similarity entry (usually two tasks).
         */
        public TaskSet similarityTask = new TaskSet(); 
    }
    
    /**
     * All discovered similarity entries for the current {@code taskSet}.
     *
     * <p>Populated by {@link #similarityForTwoTasks(Task, Task)} when task
     * pairs are compared. Each entry records the pair of tasks and their
     * common resources.</p>
     */
    public Vector<Similarity> similarityForAllTask = new Vector<Similarity>();
    
    /**
     * Temporary working set of similarity entries used when selecting the next
     * group to place on a core.
     *
     * <p>This vector holds similarity entries that are related (share tasks)
     * to the currently selected maximum similarity entry and is used to
     * prioritize placement of related groups before falling back to the
     * global similarity list.</p>
     */
    public Vector<Similarity> similarityForTemp;
    
    /**
     * Collection of task sets representing partitions (reserved for partition
     * bookkeeping; package\-private as in original file).
     */
    Vector<TaskSet> partitionTasks = new Vector<TaskSet>();
    
    /** Temporary per-core utilization array used during partitioning */
    double[] tempU;
            
    /**
     * Constructs a new SBP instance and sets the algorithm name to
     * "Similarity-Based Partitioning".
     */
    public SBP()
    {
        this.setName("Similarity-Based Partitioning");
    }
    
    /**
     * Partition tasks in {@code taskSet} to the provided {@code cores}
     * using the similarity-based strategy.
     *
     * <p>Behavior details:</p>
     * <ol>
     *   <li>Compute total utilization of {@code taskSet} and the average per
     *       core target {@code U*} (printed for debugging).</li>
     *   <li>Construct pairwise similarity entries for every pair of tasks
     *       (tasks that share at least one {@code SharedResource}).</li>
     *   <li>Select the similarity entry with the largest number of shared
     *       resources and assign all tasks in that entry to the core with
     *       the minimum current temporary utilization {@code tempU}.</li>
     *   <li>Use {@code similarityForTemp} to collect related similarity
     *       entries (sharing a task with the selected entry) and continue
     *       assignment from that set when possible; otherwise fall back to the
     *       global similarity list.</li>
     *   <li>After all similarity groups with resources are processed, assign
     *       leftover tasks to the least loaded core.</li>
     * </ol>
     * 
     *
     * @param cores the list of available cores to which tasks will be assigned;
     *              this method mutates the cores by calling {@code addTask}
     *              on them
     * @param taskSet the set of tasks to partition and assign to cores
     */
    @Override
    public void taskToCore(Vector<Core> cores, TaskSet taskSet)
    {
        TaskSet allTs = new TaskSet();
        tempU = new double[cores.size()];
        
        for(int i = 0; i < cores.size(); i++)
        {
            tempU[i] = 0;
        }
        
        double u = 0;
        for(Task t : taskSet)
        {
            allTs.add(t);
            u +=  ((double)t.getComputationAmount() / t.getPeriod());
        }
        
        println("U = " + u);
        u = u / (cores.size());
        println("U*= " + u);
        
        for(int i = 0; i < taskSet.size() - 1; i++)
        {
            for(int j = i + 1; j < taskSet.size(); j++)
            {
                this.similarityForTwoTasks(taskSet.get(i), taskSet.get(j));
            }
        }
        
        Similarity maxSim = this.findMaxSimilarityTasks(this.similarityForAllTask);
        this.similarityForTemp = new Vector<Similarity>();
        int i = this.findMinUCore(cores).getID() - 1;
        
        do
        {
            this.similarityForAllTask.remove(maxSim);
//
            for(int x = 0 ; x < this.similarityForAllTask.size(); x++)
            {
                if(this.similarityForAllTask.get(x).similarityTask.contains(maxSim.similarityTask.get(0)))
                {
                    //println("SBP= X*(" + this.similarityForAllTask.get(x).similarityTask.get(0).getID() + ", " + this.similarityForAllTask.get(x).similarityTask.get(1).getID() + ")");
                    if(!this.similarityForTemp.contains(this.similarityForAllTask.get(x)))
                    {
                        this.similarityForTemp.add(this.similarityForAllTask.get(x));
                    }
                }
            }

            for(int x = 0 ; x < this.similarityForAllTask.size(); x++)
            {
                if(this.similarityForAllTask.get(x).similarityTask.contains(maxSim.similarityTask.get(1)))
                {
                    //println("SBP= X*(" + this.similarityForAllTask.get(x).similarityTask.get(0).getID() + ", " + this.similarityForAllTask.get(x).similarityTask.get(1).getID() + ")");
                    if(!this.similarityForTemp.contains(this.similarityForAllTask.get(x)))
                    {
                        this.similarityForTemp.add(this.similarityForAllTask.get(x));
                    }
                }
            }

//            println("START===============================");
            for(Task t :maxSim.similarityTask)
            {
                if(allTs.contains(t))
                {
                    //println("SBP= Core(" + i + 1 + ") <= T(" + t.getID() + ")");
                    //t.setLocalCore(cores.get(i));
                    cores.get(i).addTask(t);
                    tempU[i] += (double)t.getComputationAmount() / t.getPeriod();
                    allTs.remove(t);
                    //println("SBP= U(" + i + 1 + ") = " + tempU);
                }
            }
//            println("E N D===============================");
            
            
            if(this.similarityForTemp.isEmpty())
            {
                maxSim = this.findMaxSimilarityTasks(this.similarityForAllTask);
                i = this.findMinUCore(cores).getID() - 1;
                this.similarityForTemp = new Vector<Similarity>();
            }
            else
            {
                maxSim = this.findMaxSimilarityTasks(this.similarityForTemp);
                this.similarityForTemp.remove(maxSim);
                if(maxSim.similarityRes.size() <= 0)
                {
                    maxSim = this.findMaxSimilarityTasks(this.similarityForAllTask);
                    i = this.findMinUCore(cores).getID() - 1;
                    this.similarityForTemp = new Vector<Similarity>();
                }
                else
                {
                    if(tempU[i] >= u)
                    {
                        i = this.findMinUCore(cores).getID() - 1;
                    }
                }
            }
        }while(maxSim != null && maxSim.similarityRes.size() > 0);
        
        for(Task t : allTs)
        {
            i = this.findMinUCore(cores).getID() - 1;
        //    t.setLocalCore(cores.get(i));
            cores.get(i).addTask(t);
            tempU[i] += (double)t.getComputationAmount() / t.getPeriod();
        }
        
    }
    
    /**
     * Computes the similarity (shared resources) between two tasks.
     *
     * <p>Creates a {@code Similarity} entry containing the set of shared
     * resources common to both tasks and adds it to
     * {@code similarityForAllTask}. Returns the list of shared resources.</p>
     *
     * @param t1 the first task
     * @param t2 the second task
     * @return the vector of shared resources common to both tasks
     */
    public Vector<SharedResource> similarityForTwoTasks(Task t1, Task t2)
    {
        Vector<SharedResource> sr = new Vector<SharedResource>();
        
        for(SharedResource r : t2.getResourceSet())
        {
            if(t1.getResourceSet().contains(r))
            {
                sr.add(r);
            }
        }
        
        Similarity sim = new Similarity();
        sim.similarityRes = sr;
        sim.similarityTask.add(t1);
        sim.similarityTask.add(t2);
        
        this.similarityForAllTask.add(sim);
        
//        println("====================");
//        println("Task" + t1.getID());
//        for(SharedResource r : t1.getResourceSet())
//        {
//            println("  Resource" + r.getID());
//        }
//        
//        println("Task" + t2.getID());
//        for(SharedResource r : t2.getResourceSet())
//        {
//            println("  Resource" + r.getID());
//        }
//        
//        println("Task" + t1.getID() + ":" + t2.getID());
//        for(SharedResource r : sr)
//        {
//            println("  Resource" + r.getID());
//        }
//        println("====================");
        
        return sr;
    }
    
    /**
     * Finds the similarity entry with the maximum number of shared resources.
     *
     * @param set the vector of similarity entries to search
     * @return the similarity entry with the most shared resources, or null
     *         if the set is empty
     */
    public Similarity findMaxSimilarityTasks(Vector<Similarity> set)
    {
        Similarity tempSim;
        if(set.size() > 0)
        {
            tempSim = set.get(0);
            for(int j = 0; j < set.size(); j++)
            {
                if(set.get(j).similarityRes.size() > tempSim.similarityRes.size())
                {
                    tempSim = set.get(j);
                }
            }
        }
        else
        {
            return null;
        }
        
        return tempSim;
    }
    
    /**
     * Finds the core with the minimum current temporary utilization.
     *
     * @param cores the vector of cores
     * @return the core with the minimum utilization in {@code tempU}
     */
    private Core findMinUCore(Vector<Core> cores)
    {
        double temp = Double.MAX_VALUE;
        int tempI = -1;
        for(int i = 0; i < cores.size(); i++)
        {
            if(tempU[i] < temp)
            {
                temp = tempU[i];
                tempI = i;
            }
        }
        
        return cores.get(tempI);
    }
}
