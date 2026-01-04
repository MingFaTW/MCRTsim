/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dynamicVoltageAndFrequencyScalingMethod;

import SystemEnvironment.Core;
import SystemEnvironment.DynamicVoltageRegulator;
import SystemEnvironment.Processor;
import WorkLoad.Job;
import WorkLoad.SharedResource;

/**
 * Abstract base class for dynamic voltage and frequency scaling methods.
 * 
 * @author ShiuJia
 */
public abstract class DynamicVoltageAndFrequencyScalingMethod
{
    private String name;
    private DynamicVoltageRegulator parentRegulator;
    
    /**
     * Constructs a new DynamicVoltageAndFrequencyScalingMethod instance.
     */
    public DynamicVoltageAndFrequencyScalingMethod()
    {
        this.name = null;
        this.parentRegulator = null;
    }
    
    /*Operating*/
    /**
     * Define speed for processor.
     * @param p the processor
     */
    public abstract void definedSpeed(Processor p);
    //public abstract boolean scalingVoltage();
    /**
     * Action when job arrives at processor.
     * @param j the job
     * @param p the processor
     */
    public abstract void jobArrivesProcessorAction(Job j, Processor p);
    /**
     * Action when job arrives at core.
     * @param j the job
     * @param c the core
     */
    public abstract void jobArrivesCoreAction(Job j, Core c);
    /**
     * Action for cores execution.
     */
    public abstract void coresExecuteAction();
    /**
     * Action for core execution.
     * @param c the core
     */
    public abstract void coreExecuteAction(Core c);
    /**
     * Action when job first executes.
     * @param j the job
     */
    public abstract void jobFirstExecuteAction(Job j);
    /**
     * Action when job executes every time.
     * @param j the job
     */
    public abstract void jobEveryExecuteAction(Job j);
    /**
     * Action when job locks a resource.
     * @param j the job
     * @param r the shared resource
     */
    public abstract void jobLockAction(Job j, SharedResource r);
    /**
     * Action when job unlocks a resource.
     * @param j the job
     * @param r the shared resource
     */
    public abstract void jobUnlockAction(Job j, SharedResource r);
    /**
     * Action when job completes.
     * @param j the job
     */
    public abstract void jobCompleteAction(Job j);
    /**
     * Check end system time action.
     * @param systemTime the system time
     */
    public abstract void checkEndSystemTimeAction(long systemTime);
    /**
     * Action when job misses deadline.
     * @param j the job
     */
    public abstract void jobMissDeadlineAction(Job j);
    /**
     * Action when job is blocked.
     * @param blockedJob the blocked job
     * @param blockingRes the blocking resource
     */
    public abstract void jobBlockedAction(Job blockedJob, SharedResource blockingRes);
    
    /*SetValue*/
    /**
     * Sets the name.
     * @param n the name
     */
    public void setName(String n)
    {
        this.name = n;
    }
    
    /**
     * Sets the parent regulator.
     * @param r the parent regulator
     */
    public void setParentRegulator(DynamicVoltageRegulator r)
    {
        this.parentRegulator = r;
    }
    
    /*GetValue*/
    /**
     * Gets the name.
     * @return the name
     */
    public String getName()
    {
        return this.name;
    }
    
    /**
     * Gets the parent regulator.
     * @return the parent regulator
     */
    public DynamicVoltageRegulator getParentRegulator()
    {
        return this.parentRegulator;
    }
}