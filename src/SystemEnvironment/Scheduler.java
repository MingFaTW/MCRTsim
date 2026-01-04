/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package SystemEnvironment;

import WorkLoad.Job;
import WorkLoadSet.JobQueue;
import WorkLoadSet.TaskSet;
import RTSimulator.Definition;
import schedulingAlgorithm.PriorityDrivenSchedulingAlgorithm;

/**
 * Facade over a priority-driven scheduling algorithm for either a global or a local scheduler.
 *
 * <p>This component delegates priority computation to a pluggable
 * {@link PriorityDrivenSchedulingAlgorithm} and attaches to either:</p>
 * <ul>
 *   <li>a processor-wide "global" scheduler via {@link #setParentProcessor(Processor)}, or</li>
 *   <li>a per-core "local" (or single-core) scheduler via {@link #setParentCore(Core)}.</li>
 * </ul>
 *
 * <p>For dynamic-priority algorithms, {@link #calculatePriority(JobQueue)} is used on
 * each tick to recompute priorities. In that path the original priority is updated to
 * reflect the current dynamic priority, inherited priorities override the current one,
 * and suspended jobs are assigned {@link Definition#Ohm}.</p>
 *
 * @author ShiuJia
 */
public class Scheduler
{
	
    private PriorityDrivenSchedulingAlgorithm algorithm;
    private Processor parentProcessor; //當此為一多核心Global Scheduler
    private Core parentCore; //當此為一多核心Local Scheduler或者單核心Scheduler
    
    /**
     * Creates an unconfigured scheduler facade with no algorithm and no parent set.
     * Use {@link #setSchedAlgorithm(PriorityDrivenSchedulingAlgorithm)} and either
     * {@link #setParentProcessor(Processor)} or {@link #setParentCore(Core)} before use.
     */
    public Scheduler()
    {
        this.algorithm = null;
        this.parentProcessor = null;
        this.parentCore = null;
    }
    
    /*Operating*/
    void calculatePriority(TaskSet ts)
    {
        this.algorithm.calculatePriority(ts);
    }
    
    /**
     * Recalculates the priority of jobs in the given job queue using a dynamic scheduling algorithm.
     *
     * <p>Integrated behavior from inline comments:
     * <ul>
     *   <li>Dynamic algorithms recompute priorities every time unit.</li>
     *   <li>Each job's original priority is set to the newly computed current priority.</li>
     *   <li>If a job has an inherited priority, it replaces the current priority.</li>
     *   <li>Suspended jobs receive {@link Definition#Ohm} as their current priority.</li>
     * </ul>
     * Only dynamic-priority algorithms should call this method.
     *
     * @param jp the {@link JobQueue} containing the jobs to update
     * @return a new {@link JobQueue} whose jobs have updated priorities (inheritance and suspension applied)
     */
    public JobQueue calculatePriority(JobQueue jp)//此方法只有在動態排程方法時被呼叫
    {
        JobQueue newJQ = this.algorithm.calculatePriority(jp);//動態的排程方法每個時間單位都會重新計算新的Proiority
        JobQueue inheritJQ = new JobQueue();
        Job tempJob;
        while((tempJob = newJQ.poll()) != null)
        {
            tempJob.setOriginalPriority(tempJob.getCurrentProiority());//因為是動態的排程方法，因此新的Proiority也需要更改到OriginalPriority
            
            if(tempJob.isInherit)
            {
                tempJob.setCurrentProiority(tempJob.getInheritPriority());
            }
            
            if(tempJob.isSuspended)
            {
                tempJob.setCurrentProiority(Definition.Ohm);
            }
            
            inheritJQ.add(tempJob);
        }
        return inheritJQ;
    }
    
    /*SetValue*/
    /**
     * Sets the scheduling algorithm that this facade delegates to.
     *
     * @param a the {@link PriorityDrivenSchedulingAlgorithm} implementation
     */
    public void setSchedAlgorithm(PriorityDrivenSchedulingAlgorithm a)
    {
        this.algorithm = a;
    }
    
    /**
     * Associates this scheduler with a processor as a global scheduler instance.
     *
     * @param p the {@link Processor} that owns this global scheduler
     */
    public void setParentProcessor(Processor p)
    {
        this.parentProcessor = p;
    }
    
    /**
     * Associates this scheduler with a core as a local (or single-core) scheduler instance.
     *
     * @param c the {@link Core} that owns this local scheduler
     */
    public void setParentCore(Core c)
    {
        this.parentCore = c;
    }
    
    /*GetValue*/
    /**
     * Returns the configured scheduling algorithm.
     *
     * @return the {@link PriorityDrivenSchedulingAlgorithm}, or {@code null} if unset
     */
    public PriorityDrivenSchedulingAlgorithm getSchedAlgorithm()
    {
        return this.algorithm;
    }
    
    /**
     * Returns the parent processor when acting as a global scheduler.
     *
     * @return the {@link Processor} parent, or {@code null} if not set
     */
    public Processor getParentProcessor()
    {
        return this.parentProcessor;
    }
    
    /**
     * Returns the parent core when acting as a local/single-core scheduler.
     *
     * @return the {@link Core} parent, or {@code null} if not set
     */
    public Core getParentCore()
    {
        return this.parentCore;
    }
}