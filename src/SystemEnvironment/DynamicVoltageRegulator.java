/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package SystemEnvironment;


import WorkLoad.Job;
import WorkLoad.SharedResource;
import dynamicVoltageAndFrequencyScalingMethod.DynamicVoltageAndFrequencyScalingMethod;
import RTSimulator.Definition.DVFSType;

/**
 * Dynamic Voltage Regulator facade that coordinates DVFS-related events.
 *
 * <p>This component owns a {@link DynamicVoltageAndFrequencyScalingMethod}
 * implementation and delegates all DVFS decisions and actions to it. The
 * regulator is notified of system events (job arrival, first execution,
 * lock/unlock, completion, deadline miss, blocking, core(s) executing, end of
 * system time) and forwards them to the underlying method together with the
 * contextual objects (Processor, Core, Job, SharedResource). The previous
 * inline comments indicated where debug prints used to be; those calls are now
 * captured in this documentation as the event flow.</p>
 *
 * <p>The regulator also records the DVFS scope via {@link DVFSType}
 * (e.g., full-chip, per-core, VFI) and keeps a reference to its parent
 * {@link Processor} for method callbacks.</p>
 *
 * @author ShiuJia
 */
public class DynamicVoltageRegulator
{
    private DynamicVoltageAndFrequencyScalingMethod method;
    private Processor parentProcessor;
    private DVFSType type;
    

    
    
    /**
     * Constructs a regulator with no DVFS method, no parent processor and no
     * DVFS type. These must be set via the corresponding setters before use.
     */
    public DynamicVoltageRegulator()
    {
        this.method = null;
        this.parentProcessor = null;
        this.type = null;
    }
    
    /**
     * Initializes or updates available speeds by delegating to the configured
     * {@link DynamicVoltageAndFrequencyScalingMethod} with the current parent
     * {@link Processor}. This is typically called once the processor model and
     * core sets are constructed.
     */
    public void definedSpeed()
    {
        this.method.definedSpeed(this.parentProcessor);
        
        //println("Regulator:DefinedSpeed");
    }
    
    /**
     * Notifies the regulator that a job arrived at the processor level, so the
     * DVFS method can react (e.g., global scaling or queue-aware policies).
     *
     * @param j the arriving {@link Job}
     * @param p the {@link Processor} to which the job arrives
     */
    public void checkJobArrivesProcessor(Job j, Processor p)
    {
        //println("Regulator:JobArrivesProcessor");
        this.method.jobArrivesProcessorAction(j, p);
    }
    
    /**
     * Notifies the regulator that a job arrived at a specific core, enabling
     * per-core or local DVFS reactions when applicable.
     *
     * @param j the arriving {@link Job}
     * @param c the {@link Core} where the job arrives
     */
    public void checkJobArrivesCore(Job j, Core c)
    {
        //println("Regulator:JobArrivesCore");
        this.method.jobArrivesCoreAction(j, c);
    }
    
    /**
     * Notifies that cores are executing so the DVFS method can perform
     * periodic/global checks across all cores (e.g., reevaluate speeds).
     */
    public void checkCoresExecute()
    {
        //println("Regulator:CoresExecute");
        this.method.coresExecuteAction();
    }
    
    /**
     * Notifies that a specific core is executing, allowing per-core DVFS
     * checks/adjustments.
     *
     * @param c the {@link Core} that is currently executing
     */
    public void checkCoreExecute(Core c)
    {
        //println("Regulator:CoreExecute");
        this.method.coreExecuteAction(c);
    }
    
    /**
     * Notifies that the given job is about to execute for the first time, so
     * the DVFS method can perform any first-execution actions (e.g., speed
     * boost or logging).
     *
     * @param j the {@link Job} beginning its first execution
     */
    public void JobFirstExecuteAction(Job j)
    {
        this.method.jobFirstExecuteAction(j);
    }
    
    /**
     * Notifies for every execution step of a job, enabling continuous DVFS
     * monitoring and adjustments.
     *
     * @param j the {@link Job} currently executing
     */
    public void checkJobEveryExecute(Job j)
    {
        this.method.jobEveryExecuteAction(j);
    }
    
    /**
     * Notifies that a job attempted to lock a shared resource, allowing the
     * DVFS method to react to blocking/locking behavior if needed.
     *
     * @param j the {@link Job} that locks or attempts to lock the resource
     * @param r the {@link SharedResource} being locked
     */
    public void checkJobLock(Job j, SharedResource r)
    {
        //println("Regulator:JobLock");
        this.method.jobLockAction(j, r);
    }
    
    /**
     * Notifies that a job has unlocked a shared resource, which may influence
     * DVFS decisions (e.g., contention is relieved).
     *
     * @param j the {@link Job} unlocking the resource
     * @param r the {@link SharedResource} that was unlocked
     */
    public void checkJobUnlock(Job j, SharedResource r)
    {
        //println("Regulator:JobUnlock");
        this.method.jobUnlockAction(j, r);
    }
    
    /**
     * Notifies that a job has completed; the DVFS method can finalize any
     * per-job accounting and possibly adjust speeds.
     *
     * @param j the completed {@link Job}
     */
    public void checkJobComplete(Job j)
    {
        //println("Regulator:JobComplete");
        this.method.jobCompleteAction(j);
    }
    
    /**
     * Notifies at the end of the system time horizon so the DVFS method can
     * perform end-of-simulation or end-of-interval actions.
     *
     * @param systemTime the system time (ticks) at which to perform final checks
     */
    public void checkEndSystemTimeAction(long systemTime)
    {
        this.method.checkEndSystemTimeAction(systemTime);
    }
    
    /**
     * Notifies that a job has missed its deadline, enabling the DVFS method to
     * react for logging or remedial speed policies.
     *
     * @param j the {@link Job} that missed its deadline
     */
    public void checkJobMissDeadline(Job j)
    {
        //println("Regulator:JobDeadline");
        this.method.jobMissDeadlineAction(j);
    }
    
    /**
     * Notifies that a job is blocked by a shared resource, which may trigger
     * DVFS changes (e.g., reduce speed while waiting).
     *
     * @param blockedJob the {@link Job} that is currently blocked
     * @param blockingRes the {@link SharedResource} causing the block
     */
    public void checkBlockAction(Job blockedJob, SharedResource blockingRes)
    {
        //println("Regulator:Block");
        this.method.jobBlockedAction(blockedJob, blockingRes);
    }
    
    /*SetValue*/
    /**
     * Installs the concrete DVFS method implementation and sets the back
     * reference from the method to this regulator.
     *
     * @param m the {@link DynamicVoltageAndFrequencyScalingMethod} to use
     */
    public void setDynamicVoltageAndFrequencyScalingMethod(DynamicVoltageAndFrequencyScalingMethod m)
    {
        this.method = m;
        this.method.setParentRegulator(this);
    }
    
    /**
     * Sets the parent processor reference used by the DVFS method when making
     * system-wide decisions.
     *
     * @param p the parent {@link Processor}
     */
    public void setParentProcessor(Processor p)
    {
        this.parentProcessor = p;
    }
    
    /**
     * Sets the DVFS scope/type from a string value.
     *
     * <p>Accepted values (case-sensitive as used in the original code):
     * <ul>
     *   <li>"full-chip" → {@link DVFSType#FullChip}</li>
     *   <li>"per-core" → {@link DVFSType#PerCore}</li>
     *   <li>"VFI" → {@link DVFSType#VFI}</li>
     * </ul>
     * <span>Values outside this set are ignored (type remains unchanged).</span>
     *
     * @param s the DVFS type as a string label
     */
    public void setDVFSType(String s)
    {
        switch(s)
        {
            case "full-chip":
            {
                this.type = DVFSType.FullChip;
                break;
            }
            
            case "per-core":
            {
                this.type = DVFSType.PerCore;
                break;
            }
            
            case "VFI":
            {
                this.type = DVFSType.VFI;
                break;
            }
        }
    }

    
    /*GetValue*/
    /**
     * Returns the currently installed DVFS method implementation.
     *
     * @return the {@link DynamicVoltageAndFrequencyScalingMethod} in use, or
     *         {@code null} if none has been set
     */
    public DynamicVoltageAndFrequencyScalingMethod getDynamicVoltageAndFrequencyScalingMethod()
    {
        return this.method;
    }
    
    /**
     * Returns the parent processor used by the DVFS method.
     *
     * @return the parent {@link Processor}, or {@code null} if unset
     */
    public Processor getParentProcessor()
    {
        return this.parentProcessor;
    }
    
    /**
     * Returns the DVFS scope/type recorded by this regulator.
     *
     * @return the {@link DVFSType} (may be {@code null} if not yet set)
     */
    public DVFSType getDVFSType()
    {
        return this.type;
    }
    
}