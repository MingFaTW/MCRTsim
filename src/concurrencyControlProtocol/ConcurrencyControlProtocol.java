/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package concurrencyControlProtocol;

import SystemEnvironment.ConcurrencyController;
import SystemEnvironment.Processor;
import WorkLoad.Job;
import WorkLoad.SharedResource;

/**
 * Abstract base class for concurrency control protocols.
 * <p>
 * This class defines lifecycle hooks and operations that a concurrency control
 * protocol must provide to interact with the scheduling environment and jobs.
 * Implementations should provide concrete behaviors for job arrival, preemption,
 * blocking/unblocking, completion, deadline misses, and any pre-scheduling setup.
 * </p>
 *
 * <p>Integrated notes from existing inline comments:</p>
 * <ul>
 *   <li>The {@link #preAction(Processor)} method is intended for pre-operations
 *       which must be done before scheduling begins (前置作業，在排程開始前就該做得事，就寫在這個function裡).</li>
 * </ul>
 *
 * @author ShiuJia
 */
public abstract class ConcurrencyControlProtocol
{
    private String name;
    private ConcurrencyController parentController;
    
    /**
     * Default constructor.
     * <p>
     * Initializes the protocol name and parent controller to {@code null}.
     * </p>
     */
    public ConcurrencyControlProtocol()
    {
        this.name = null;
        this.parentController = null;
    }
    
    /*Operating*/
    /**
     * Perform pre-scheduling actions for the given processor.
     * <p>
     * Use this hook to perform any initialization or setup that must occur
     * before scheduling begins on the specified processor.
     * (前置作業，在排程開始前就該做得事，就寫在這個function裡)
     * </p>
     *
     * @param p the {@link Processor} on which to perform pre-scheduling actions
     */
    public abstract void preAction(Processor p);

    /**
     * Handle the event of a job arriving to the system.
     *
     * @param j the arriving {@link Job}
     */
    public abstract void jobArrivesAction(Job j);
    
    /**
     * Handle the event where a job is preempted by another job.
     * <p>
     * The inline comment clarifies roles: {@code preemptedJob} is the job
     * that was preempted (typically the lower-priority job), and {@code nextJob}
     * is the job that preempted it (typically the higher-priority job).
     * </p>
     *
     * @param preemptedJob the {@link Job} that was preempted (lower priority)
     * @param nextJob the {@link Job} that caused the preemption (higher priority)
     */
    public abstract void jobPreemptedAction(Job preemptedJob , Job nextJob);//preemptedJob 被搶先的工作(Lower Priority Job)，newJob搶先的工作(Higher Priority Job)
    

    /**
     * Check whether the given job should be treated as its first execution.
     *
     * @param j the {@link Job} to check
     * @return {@code true} if the job is considered to be executing for the first time
     *         and any first-execution protocol actions should be applied; {@code false} otherwise
     */
    public abstract boolean checkJobFirstExecuteAction(Job j);
    
    /**
     * Perform actions associated with a job's first execution.
     *
     * @param j the {@link Job} that is executing for the first time
     */
    public abstract void jobFirstExecuteAction(Job j);
    
    
    /**
     * Check and/or attempt to lock a shared resource on behalf of a job.
     * <p>
     * Implementations may return the resource that was actually locked, a
     * different resource that affects locking decisions, or {@code null} to
     * indicate no lock was obtained. The exact meaning depends on the concrete
     * protocol implementation.
     * </p>
     *
     * @param j the {@link Job} requesting or checking the lock
     * @param r the {@link SharedResource} the job wants to lock
     * @return the {@link SharedResource} that represents the result of the lock check
     *         (e.g., the resource granted, or a resource indicating blocking), or {@code null}
     *         if no resource is granted or applicable
     */
    public abstract SharedResource checkJobLockAction(Job j, SharedResource r);
    
    
    /**
     * Handle the event of a job becoming blocked due to a shared resource.
     *
     * @param blockedJob the {@link Job} that is blocked
     * @param blockingRes the {@link SharedResource} that is causing the block
     */
    public abstract void jobBlockedAction(Job blockedJob,SharedResource blockingRes);
    
    /**
     * Handle unlocking of a shared resource by a job.
     *
     * @param j the {@link Job} that is unlocking the resource
     * @param r the {@link SharedResource} being unlocked
     */
    public abstract void jobUnlockAction(Job j, SharedResource r);
    
    /**
     * Handle the completion of a job.
     *
     * @param j the {@link Job} that has completed
     */
    public abstract void jobCompletedAction(Job j);
    
    /**
     * Handle the event that a job missed its deadline.
     *
     * @param j the {@link Job} that missed its deadline
     */
    public abstract void jobMissDeadlineAction(Job j);
    
    /*SetValue*/
    /**
     * Set the name of this concurrency control protocol instance.
     *
     * @param n the name to assign
     */
    public void setName(String n)
    {
        this.name = n;
    }
    
    /**
     * Set the parent {@link ConcurrencyController} that manages this protocol.
     *
     * @param c the parent {@link ConcurrencyController}
     */
    public void setParentController(ConcurrencyController c)
    {
        this.parentController = c;
    }
    
    /*GetValue*/
    /**
     * Get the name of this protocol instance.
     *
     * @return the protocol name, or {@code null} if not set
     */
    public String getName()
    {
        return this.name;
    }
    
    /**
     * Get the parent {@link ConcurrencyController} associated with this protocol.
     *
     * @return the parent {@link ConcurrencyController}, or {@code null} if none has been set
     */
    public ConcurrencyController getParentController()
    {
        return this.parentController;
    }
}
