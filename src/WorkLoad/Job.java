/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package WorkLoad;

import ResultSet.SchedulingInfo;
import SystemEnvironment.Core;
import SystemEnvironment.Processor;
import WorkLoadSet.CriticalSectionSet;
import java.util.PriorityQueue;
import java.util.Stack;
import java.util.Vector;

import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;

import RTSimulator.Definition;
import RTSimulator.Definition.JobStatus;
import static RTSimulator.Definition.magnificationFactor;
import RTSimulator.RTSimulatorMath;
import static RTSimulator.RTSimulator.println;
/**
 * Represents an executable Job derived from a Task in the workload.
 * <p>
 * A Job encapsulates release/deadline times, remaining work (target/progress),
 * priority handling (original/current/inherited), critical section sets and
 * runtime placement information such as assigned core/processor. Inline
 * Chinese comments in the original source are integrated into this Javadoc
 * (for example: comments explaining that a Job is blocked by a single
 * resource at a time, and that migrateTo must be called before the core runs).
 * </p>
 * <p>
 * The class implements {@code Comparable} to allow ordering in ready queues
 * according to current priority and tiebreak rules (inheritance and release
 * time are considered).
 * </p>
 *
 * @author ShiuJia
 */
public class Job implements Comparable
{
    private int ID; //代碼，J{parentTask.ID , ID}
    private Task parentTask; //所屬Task
    private long releaseTime; //被產生的時間
    private long absoluteDeadline; //絕對截止時間
    private long pendingTime;//待機時間
    private long responseTime;//回應時間
    private long beBlockedTime;//被阻擋的時間
    private double targetAmount; //目標工作量
    private double progressAmount; //目前工作量
    private Priority originalPriority; //最初的優先權(來自Task)
    private Priority currentPriority; //目前的優先權
    private Priority inheritPriority;
    private int priorityLevel;
    private Long criticalityLevel;
    private Core originalCore;//第一次分配的Core
    private Core currentCore;//當前的分配的Core
    private Processor localProcessor;
    private Vector<CriticalSection> criticalSectionSet;
    private PriorityQueue<CriticalSection> notEnteredCriticalSectionSet;
    private Stack<CriticalSection> enteredCriticalSectionSet;
    private double maxProcessingSpeed;
    private Vector<SharedResource> resourceSet;
    private Vector<SchedulingInfo> schedulingInfoSet;
    private JobStatus status = JobStatus.NONCOMPUTE;
    private long timeOfStatus = 0;//改變狀態的當前時間
    
    /**
     * In this system a Job can only be blocked by one shared resource at a
     * time. The {@code blockingResource} remains non-null until the job
     * actually executes (progressAmount increases).
     */
    private SharedResource blockingResource = null;
    
    /** Whether this job has inherited priority */        
    public boolean isInherit;
    /** Whether this job is suspended */
    public boolean isSuspended;
    
    /**
     * Create an empty Job with default, zeroed or null fields.
     * Consumers should set the task, timing and workload values before use.
     */
    public Job()
    {
        this.ID = 0;
        this.parentTask = null;
        this.releaseTime = 0;
        this.absoluteDeadline = 0;
        this.targetAmount = 0;
        this.progressAmount = 0;
        this.originalPriority = null;
        this.currentPriority = null;
        this.originalCore = null;
        this.currentCore = null;
        this.localProcessor = null;
        this.criticalSectionSet = new Vector<CriticalSection>();
        this.notEnteredCriticalSectionSet = new PriorityQueue<CriticalSection>();
        this.enteredCriticalSectionSet = new Stack<CriticalSection>();
        this.maxProcessingSpeed = 0;
        this.schedulingInfoSet = new Vector<SchedulingInfo>();
        this.resourceSet = new Vector<SharedResource>();
        this.isInherit = false;
        this.inheritPriority = null;
        
        this.isSuspended = false;
    }

    /*Operating*/
    /**
     * Compare this Job to another for queue ordering.
     * <p>
     * Jobs are ordered primarily by current priority (higher value means
     * higher scheduling precedence). Ties are broken by inheritance status
     * (inherited jobs win) and then by earlier release time.
     * </p>
     *
     * @param o another Job to compare
     * @return negative if this has higher scheduling precedence, positive if lower, zero if equal
     * @throws ClassCastException if {@code o} is not a Job
     */
    @Override
    public int compareTo(Object o)
    {
        Job j = (Job)o;
        
        if(this.currentPriority.getValue() > j.currentPriority.getValue())
        {
            return -1;
        }
        else if(this.currentPriority.getValue() < j.currentPriority.getValue())
        {
            return 1;
        }
        else if(this.currentPriority.getValue() == j.currentPriority.getValue())
        {
            if(this.isInherit)
            {
                return -1;
            }
            else if(this.getReleaseTime() < j.getReleaseTime())
            {
                return -1;
            }
            
            return 1;
        }
        
        return 0;
    }
    
    /**
     * Execute a portion of this job when there is remaining work larger than
     * the provided execution quantum.
     * <p>
     * If this is the first execution (progressAmount == 0) the pending time
     * is set based on current time minus release time. The blocking resource
     * is cleared and progress is advanced by {@code executionTime}.
     * </p>
     *
     * @param executionTime amount of work/time to execute (same units as targetAmount)
     * @param curTime the current global time used to compute pending time when needed
     */
    public void execute(double executionTime, long curTime)//當剩餘的工作量大於executionTime時被呼叫 (2017/7/22)
    {                                                        //目前每次執行的時間單位為executionTime = 1  (2017/7/22)
        if(this.progressAmount == 0)
        {
            this.setPendingTime(curTime - this.releaseTime);
        }
        
        this.setBlockingResource(null);
        
        this.progressAmount = RTSimulatorMath.add(this.progressAmount,executionTime);//每次執行executionTime的時間單位 (2017/7/22)
    }
    
    /**
     * Complete the job by setting progress to the target amount.
     * <p>
     * Called when remaining work is less than the usual execution quantum; it
     * clears the blocking resource and marks the job as fully progressed.
     * </p>
     */
    public void finalExecute()//當剩餘的工作量小於executionTime時被呼叫，意味著在最後一次執行的時候被呼叫 (2017/7/22)
    {
        this.setBlockingResource(null);
        this.progressAmount = this.targetAmount;
    }
    
    /**
     * Lock the next not-entered critical section on the provided shared
     * resource and move it into the entered stack.
     *
     * @param sr the SharedResource to lock
     */
    public void lockSharedResource(SharedResource sr)
    {
        CriticalSection enterCS = this.notEnteredCriticalSectionSet.poll();
        sr.setLock(this, enterCS);
        this.enteredCriticalSectionSet.add(enterCS);
    }
    
    /**
     * Unlock a shared resource and pop the most recent entered critical
     * section from the entered stack.
     *
     * @param sr the SharedResource to unlock
     */
    public void unLockSharedResource(SharedResource sr)
    {
        sr.setUnlock(this);
        this.enteredCriticalSectionSet.pop();
    }
    
    /**
     * Increase this job's priority by creating and setting a higher Priority
     * instance derived from the provided one.
     *
     * @param p the Priority value prompting the raise
     * @param i additional offset used when computing the raised priority
     */
    public void raisePriority(Priority p, int i)
    {
        Priority priority = new Priority(-p.getValue()-i);
        this.setCurrentProiority(priority);
    }
    
    /**
     * Apply priority inheritance from another job's priority.
     * <p>
     * This method computes an inherited priority and sets it if superior to
     * the current one. If the job is not suspended the current priority is
     * updated immediately.
     * </p>
     *
     * @param p the priority to inherit from
     */
    public void inheritPriority(Priority p)
    {
        Priority priority = new Priority((-p.getValue())-1);
        if(!this.isInherit || priority.isHigher(this.currentPriority))
        {
            this.isInherit = true;
            this.inheritPriority = priority;
            if(!this.isSuspended)
            {
                this.setCurrentProiority(this.inheritPriority);
            }
        }
    }
    
    /**
     * End any active priority inheritance and restore the original priority
     * if appropriate.
     */
    public void endInheritance()
    {
        /*檢查是否還有使用資源，若有，則進一步檢查是否有阻擋其他Job*/
        if(this.enteredCriticalSectionSet.isEmpty())
        {
            this.isInherit = false;
            this.inheritPriority = null;
            this.setCurrentProiority(this.getOriginalPriority());//還原優先權的部分
        }
    }
    
    /**
     * Re-evaluate inheritance state after changes to entered critical sections
     * and possibly re-apply inheritance from waiting jobs.
     */
    public void recoverInheritance()
    {
        this.isInherit = false;
        if(!this.enteredCriticalSectionSet.isEmpty())
        {
            Object[] css = this.enteredCriticalSectionSet.toArray();
            Priority p = Definition.Ohm;
            
            for(int i = 0 ; i < css.length ; i++)
            {
                Vector<Job> waittingJobs = ((CriticalSection)css[i]).getUseSharedResource().getPIPQueue();
                
                for(Job j : waittingJobs)
                {
                    if(j.isInherit)
                    {
                        if(this.currentCore.equals(j.getCurrentCore()) && j.getInheritPriority().isHigher(p))
                        {
                            p = j.getInheritPriority();
                        }
                    }
                    else
                    {
                        if(this.currentCore.equals(j.getCurrentCore()) && j.getOriginalPriority().isHigher(p))
                        {
                            p = j.getOriginalPriority();
                        }
                    }
                }
            }
            
            if(p.equals(Definition.Ohm))
            {
                this.inheritPriority = null;
                this.setCurrentProiority(this.getOriginalPriority());
            }
            else
            {
                this.inheritPriority(p);
            }
        }
    }
    
    /**
     * Print debug information about this job to the simulator output.
     * <p>
     * Includes release time, deadline, target amount and the list of
     * not-entered critical sections with their relative times.
     * </p>
     */
    public void showInfo()
    {
        println("Job(" + this.parentTask.getID() + ", " + this.ID + "):");
        println("    ReleaseTime: " + this.releaseTime);
        println("    AbsoluteDeadline: " + this.absoluteDeadline);
        println("    TargetAmount: " + this.targetAmount);
        println("    OriginalPriority: " + this.originalPriority);
        println("    CriticalSection:");
        for(CriticalSection cs : this.notEnteredCriticalSectionSet)
        {
            println("        CriticalSection(" + cs.getUseSharedResource() + "):" + cs.getRelativeStartTime() + "/" + cs.getRelativeEndTime());
        }
        println();
    }
    
    /*SetValue*/
    /**
     * Set the job identifier.
     *
     * @param id the integer ID for this job
     */
    public void setID(int id)
    {
        this.ID = id;
    }
    
    /**
     * Set the numeric priority level for external use.
     *
     * @param priorityLevel the integer priority level
     */
    public void setPriorityLevel(int priorityLevel) {
    	this.priorityLevel = priorityLevel;
    }
    
    /**
     * Set the job's criticality level.
     *
     * @param criticalityLevel the criticality level value
     */
    public void setCriticalityLevel(Long criticalityLevel) {
    	this.criticalityLevel = criticalityLevel;
    }
    
    /**
     * Associate this job with its parent Task.
     *
     * @param t the parent Task
     */
    public void setParentTask(Task t)
    {
        this.parentTask = t;
    }
    
    /**
     * Set the release time for the job and initialize the time-of-status.
     *
     * @param t the absolute release time
     */
    public void setReleaseTime(long t)
    {
        this.releaseTime = t;
        this.setTimeOfStatus(t);//初始狀態的時間為產生之時間
    }
    
    /**
     * Set the absolute deadline and initialize response/pending time
     * estimates based on the release time.
     *
     * @param d the absolute deadline time
     */
    public void setAbsoluteDeadline(long d)
    {
        this.absoluteDeadline = d;
        
        //設置PendingTime,ResponseTime初始值
        this.setPendingTime(this.absoluteDeadline - this.releaseTime);
        this.setResponseTime(this.absoluteDeadline - this.releaseTime);
        //println("1Job("+this.ID+"): "+" pendingTime = "+this.pendingTime);
    }
    
    /**
     * Set the target amount (total work) for this job.
     *
     * @param a the target work amount
     */
    public void setTargetAmount(long a)
    {
        this.targetAmount = a;
    }
    
    /**
     * Set the job's original priority as defined by the task.
     *
     * @param p the original Priority instance
     */
    public void setOriginalPriority(Priority p)
    {
        this.originalPriority = p;
    }
    
    /**
     * Update the current priority and refresh the job's position in ready
     * queues if it is present.
     *
     * @param p the new current Priority to assign
     */
    public void setCurrentProiority(Priority p)
    {
        this.currentPriority = p;
        
        if(this.localProcessor != null && this.localProcessor.getGlobalReadyQueue().contains(this))
        {
            this.localProcessor.getGlobalReadyQueue().remove(this);
            this.localProcessor.getGlobalReadyQueue().add(this);
        }
        
        if(this.currentCore != null && this.currentCore.getLocalReadyQueue().contains(this))
        {
            this.currentCore.getLocalReadyQueue().remove(this);
            this.currentCore.getLocalReadyQueue().add(this);
        }
    }
    
    /**
     * Set the original core where the job was first allocated.
     *
     * @param c the original Core
     */
    public void setOriginCore(Core c)
    {
        this.originalCore = c;
    }
    
    /**
     * Update the currently assigned core for this job.
     *
     * @param c the Core to assign as current
     */
    public void setCurrentCore(Core c)
    {
        this.currentCore = c;
    }
    
    /**
     * Set the local processor reference for this job (used for global ready queue).
     *
     * @param p the Processor that owns the global ready queue
     */
    public void setLocalProcessor(Processor p)
    {
        this.localProcessor = p;
    }
    
    /**
     * Populate this job's critical section collections from a CriticalSectionSet.
     * <p>
     * Adds each CriticalSection to both the full set and the not-entered
     * priority queue, and records the used shared resources.
     * </p>
     *
     * @param css the CriticalSectionSet to import
     */
    public void setCriticalSectionSet(CriticalSectionSet css)
    {
        for(CriticalSection cs : css)
        {
            this.criticalSectionSet.add(cs);
            this.notEnteredCriticalSectionSet.add(cs);
            this.resourceSet.add(cs.getUseSharedResource());
        }
    }
    
    /**
     * Set the maximum processing speed available to this job (derived from core speeds).
     *
     * @param s the max processing speed
     */
    public void setMaxProcessingSpeed(double s)
    {
        this.maxProcessingSpeed = s;
    }
    
    /**
     * Add a scheduling info record for result reporting.
     *
     * @param s SchedulingInfo object to record
     */
    public void addSchedulingInfo(SchedulingInfo s)
    {
        this.schedulingInfoSet.add(s);
    }
    
    /*GetValue*/
    /**
     * Return the integer job identifier.
     *
     * @return the job ID
     */
    public int getID()
    {
        return this.ID;
    }
    
    /**
     * Return the parent Task of this job.
     *
     * @return the parent Task
     */
    public Task getParentTask()
    {
        return this.parentTask;
    }
    
    /**
     * Return the release time of the job.
     *
     * @return the absolute release time
     */
    public long getReleaseTime()
    {
        return this.releaseTime;
    }
    
    /**
     * Return the numeric priority level for external reporting.
     *
     * @return the priority level
     */
    public int getPriorityLevel() {
    	return this.priorityLevel;
    }

    
    /**
     * Return the job's criticality level, if assigned.
     *
     * @return the criticality level value or null
     */
    public Long getCriticalityLevel() {
    	return this.criticalityLevel;
    }
    
    /**
     * Return the absolute deadline of the job.
     *
     * @return the absolute deadline
     */
    public long getAbsoluteDeadline()
    {
        return this.absoluteDeadline;
    }
    
    /**
     * Return the total target work amount.
     *
     * @return the target amount
     */
    public double getTargetAmount()
    {
        return this.targetAmount;
    }
    
    /**
     * Return the current progress amount performed so far.
     *
     * @return the progress amount
     */
    public double getProgressAmount()
    {
        return this.progressAmount;
    }
    
    /**
     * Return the original priority assigned from the task template.
     *
     * @return the original Priority
     */
    public Priority getOriginalPriority()
    {
        return this.originalPriority;
    }
    
    /**
     * Return the current priority (may be inherited).
     *
     * @return the current Priority
     */
    public Priority getCurrentProiority()
    {
        return this.currentPriority;
    }
    
    /**
     * Return the originally assigned core (first allocation).
     *
     * @return the original Core
     */
    public Core getOriginCore()
    {
        return this.originalCore;
    }
    
    /**
     * Return the currently assigned core.
     *
     * @return the current Core
     */
    public Core getCurrentCore()
    {
        return this.currentCore;
    }
    
    /*找出cs下面一個的CriticalSection*/

    
    /**
     * Return the processor that owns global ready queue for this job.
     *
     * @return the local Processor
     */
    public Processor getLocalProcessor()
    {
        return this.localProcessor;
    }
    
    /**
     * Find the CriticalSection that uses the given SharedResource.
     *
     * @param r the SharedResource to search for
     * @return the matching CriticalSection or null if none
     */
    public CriticalSection getCriticalSection(SharedResource r)
    {
        for(CriticalSection cs : this.criticalSectionSet)
        {
            if(cs.getUseSharedResource().equals(r))
            {
                return cs;
            }
        }
        return null;
    }
    
    /**
     * Return the full list of critical sections for this job.
     *
     * @return a Vector of CriticalSection objects
     */
    public Vector<CriticalSection> getCriticalSectionSet()
    {
        return this.criticalSectionSet;
    }
    
    /**
     * Return the priority queue of critical sections not yet entered.
     *
     * @return a PriorityQueue of CriticalSection
     */
    public PriorityQueue<CriticalSection> getNotEnteredCriticalSectionSet()
    {
        return this.notEnteredCriticalSectionSet;
    }
    
    /**
     * Extract and return a snapshot Vector of not-entered critical sections
     * while preserving the underlying priority queue.
     *
     * @return a Vector copy of not-entered CriticalSection elements
     */
    public Vector<CriticalSection> getNotEnteredCriticalSectionArray()
    {
        Vector<CriticalSection> newCS = new Vector<CriticalSection>();
        PriorityQueue<CriticalSection> tempCS = new PriorityQueue<CriticalSection>();
        
        while(this.getNotEnteredCriticalSectionSet().size() != 0)
        {
            CriticalSection cs = this.getNotEnteredCriticalSectionSet().poll();
            newCS.add(cs);
            tempCS.add(cs);
        }
        this.notEnteredCriticalSectionSet = tempCS;
        
        return newCS;
    }
    
    /**
     * Return the stack of already-entered critical sections.
     *
     * @return a Stack of CriticalSection representing nested entries
     */
    public Stack<CriticalSection> getEnteredCriticalSectionSet()
    {
        return this.enteredCriticalSectionSet;
    }
    
    /**
     * Return a Vector snapshot of entered critical sections in stack order.
     *
     * @return a Vector of entered CriticalSection elements
     */
    public Vector<CriticalSection> getEnteredCriticalSectionArray()
    {
        Vector<CriticalSection> newCSs = new Vector<CriticalSection>();
        
        Object[] css = this.getEnteredCriticalSectionSet().toArray();
        for(int i = 0 ; i< css.length ; i++)
        {
            newCSs.add((CriticalSection)css[i]);
        }
        
        return newCSs;
    }
    
    /**
     * Return the maximum processing speed recorded for this job.
     *
     * @return max processing speed
     */
    public double getMaxProcessingSpeed()
    {
        return this.maxProcessingSpeed;
    }
    
    /**
     * Return the currently inherited priority, if any.
     *
     * @return the inherited Priority or null
     */
    public Priority getInheritPriority()
    {
        return this.inheritPriority;
    }
    
    /**
     * Return the set of shared resources referenced by this job's critical sections.
     *
     * @return a Vector of SharedResource
     */
    public Vector<SharedResource> getResourceSet()
    {
        return this.resourceSet;
    }
    
    /**
     * Update job status and apply bookkeeping side-effects (completed/miss counters).
     * <p>
     * When marking COMPLETED or MISSDEADLINE this method updates the parent
     * task counters and notifies attached SchedulingInfo records of the
     * new completed/missed totals.
     * </p>
     *
     * @param sta the new JobStatus value
     * @param time the time at which the status change occurs
     */
    public void setStatus(JobStatus sta , long time)
    {
        this.status = sta;
        
        switch(sta)
        {
            case NONCOMPUTE:
                //" NonCompleted"
                break;
            case COMPUTING:
                //" Computing"
                break;
            case COMPLETED:
//                if(this.status != JobStatus.COMPLETED)
//                {
                    this.parentTask.addJobCompletedCount();
                    println("@@COMPLETED  --  Job("+this.parentTask.getID()+","+this.getID()+")");
//                }
                break;
            case MISSDEADLINE:
//                if(this.status != JobStatus.MISSDEADLINE)
//                {    
                    println("@@MISSDEADLINE  --  Job("+this.parentTask.getID()+","+this.getID()+")");
                    this.parentTask.addJobMissDeadlineCount();
//                }
                break;
            default:
        }
        
        for(SchedulingInfo schedulingInfo: this.schedulingInfoSet)
        {
            schedulingInfo.setJobCompletedNum(this.parentTask.getJobCompletedCount());
            schedulingInfo.setJobMissDeadlineNum(this.parentTask.getJobMissDeadlineCount());
        }
        
        this.setTimeOfStatus(time);
        this.setResponseTime(this.timeOfStatus - this.releaseTime);
    }
    
    /**
     * Return a human-readable status string for reporting purposes.
     *
     * @return a string describing the current JobStatus
     */
    public String getStatusString()
    {
        switch(this.status)
        { 
            case NONCOMPUTE:
                return " NonCompute ";
            case COMPUTING:
                return " Completed ";
            case COMPLETED:
                return " Completed ";
            case MISSDEADLINE:
                return " MissDeadline ";
            default:
                return " Error!!! ";
        }
    }
    
    /**
     * Return the current JobStatus enum value.
     *
     * @return the JobStatus
     */
    public JobStatus getStatus()
    {
        return this.status;
    }
    
    /**
     * Set the internal timestamp for when the job entered its current status.
     *
     * @param time the time of status change
     */
    public void setTimeOfStatus(long time)
    {
        this.timeOfStatus = time;
    }
    
    /**
     * Return a formatted representation of the time the job entered its current status.
     * <p>
     * If {@code timeOfStatus} is non-zero it is formatted using the global
     * magnification factor; otherwise the last scheduling info end time is used.
     * </p>
     *
     * @return formatted time-of-status string
     */
    public String getTimeOfStatus()
    {
        
        RTSimulatorMath math = new RTSimulatorMath();
        if(this.timeOfStatus != 0)
        {
            return ""+ math.changeDecimalFormat((double)this.timeOfStatus/magnificationFactor);
        }
        else
        {
            return ""+this.schedulingInfoSet.lastElement().getEndTime();
        }
    }
    
    /**
     * Set the measured response time for this job.
     *
     * @param time the response time to assign
     */
    public void setResponseTime(long time)
    {
        this.responseTime = time;
    }
    
    /**
     * Return the measured response time.
     *
     * @return the response time
     */
    public long getResponseTime()
    {
        return this.responseTime;
    }
    
    /**
     * Set the pending (waiting) time for this job.
     *
     * @param time the pending time to assign
     */
    public void setPendingTime(long time)
    {
        this.pendingTime = time;
    }
    
    /**
     * Return the pending (waiting) time for this job.
     *
     * @return the pending time
     */
    public long getPendingTime()
    {
        return this.pendingTime;
    }
    
    /**
     * Request migration of this job to another core.
     * <p>
     * This method should be called before the core begins running; it enqueues
     * migration costs on the source and destination cores and updates the
     * job's current core reference. Returns true when migration was scheduled.
     * </p>
     *
     * @param nextCore the Core to migrate to
     * @return {@code true} if migration was scheduled, {@code false} otherwise
     */
    public boolean migrateTo(Core nextCore)//只能在Core run之前被呼叫
    {
        if(this.currentCore.getCostQueue().isEmpty() 
                && this.status != JobStatus.COMPLETED 
                && this.status != JobStatus.MISSDEADLINE)
        {
            println("!!");
            println("currentCore:"+currentCore.getCurrentTime()+", getLocalReadyQueue: "+currentCore.getLocalReadyQueue().peek().getParentTask().getID()+","+currentCore.getLocalReadyQueue().peek().getID());
            println("nextCore:"+nextCore.getCurrentTime()+", getLocalReadyQueue: "+currentCore.getLocalReadyQueue().peek().getParentTask().getID()+","+currentCore.getLocalReadyQueue().peek().getID());
            println("Migration ~~~~~ :"+"Job("+this.parentTask.getID()+","+this.getID()+")"+", Core: "+this.currentCore.getID()+" to "+ nextCore.getID());
            println("!!");
            
            this.currentCore.getLocalReadyQueue().remove(this);
            this.currentCore.setMigrationCost(nextCore,this);
            nextCore.setMigrationCost(nextCore,this);
            
            this.setCurrentCore(nextCore);//需要等migration cost完成之後才加入nextCore LocalReadyQueue
            return true;
        }
        return false;
    }
    
    /**
     * Increment the total time this job has been blocked by resources.
     *
     * @param l the amount of time to add to beBlockedTime
     */
    public void setBeBlockedTime(long l)
    {
        this.beBlockedTime += l;
    }
    
    /**
     * Return the accumulated time this job was blocked.
     *
     * @return blocked time total
     */
    public long getBeBlockedTime()
    {
        
        return this.beBlockedTime;
    }
    
    /**
     * Return the ratio of blocked time to task period (useful for analysis).
     *
     * @return the blocked time ratio
     */
    public double getBeBlockedTimeRatio()
    {
        println("T"+this.parentTask.getID()+" J"+this.ID+", beBlockedTime="+this.beBlockedTime);
        println("T"+this.parentTask.getID()+" J"+this.ID+", beBlockedTimeRatio="+RTSimulatorMath.div(this.beBlockedTime,this.parentTask.getPeriod()) );
        
        return RTSimulatorMath.div(this.beBlockedTime,this.parentTask.getPeriod()) ;
    }
    
    /**
     * Suspend or resume the job; when suspended it receives the special
     * priority {@code Definition.Ohm}. When resumed, original or inherited
     * priority is restored as appropriate.
     *
     * @param b {@code true} to suspend, {@code false} to resume
     */
    public void setSuspended(boolean b)
    {
        if(this.status != JobStatus.MISSDEADLINE || this.status != JobStatus.COMPLETED)
        {
            this.isSuspended = b;

            if(b)
            {   
                this.setCurrentProiority(Definition.Ohm);
            }
            else
            {
                if(this.isInherit)
                {
                    this.setCurrentProiority(this.inheritPriority);
                }
                else
                {
                    this.setCurrentProiority(this.originalPriority);
                }
            }
        }
    }
    
    /**
     * Return the resource currently blocking this job, if any.
     *
     * @return the blocking SharedResource, or null
     */
    public SharedResource getBlockingResource()
    {
        return this.blockingResource;
    }
    
    /**
     * Set or clear the resource currently blocking this job.
     *
     * @param r the SharedResource causing blocking, or null to clear
     */
    public void setBlockingResource(SharedResource r)
    {
        this.blockingResource = r;
    }
}