/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package WorkLoad;

import SystemEnvironment.Core;
import WorkLoadSet.CriticalSectionSet;
import WorkLoadSet.TaskSet;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Vector;
import RTSimulator.Definition.JobStatus;
import static RTSimulator.Definition.magnificationFactor;
import RTSimulator.RTSimulatorMath;
import static RTSimulator.RTSimulator.println;

/**
 * Represents a periodic or sporadic workload Task in the simulator.
 * <p>
 * A Task declares timing parameters (period, relative deadline), compute
 * demand (computationAmount), critical sections required during execution,
 * priority information and the set of generated Jobs. This class supports
 * multi-criticality features via lists of parameters indexed by
 * criticality/level; inline Chinese comments and editor notes were preserved
 * and integrated into the Javadoc where relevant (for example: period and
 * deadline lists are used for mixed-criticality modes such as OPA/SMC/AMC).
 * </p>
 *
 * @author ShiuJia
 */
public class Task
{
    private int ID; //代碼
    private int JobCount; //產生的Job個數
    private long enterTime; //初次進入系統的時間
    private long period; //週期
    private List<Long> periodList;
    private long relativeDeadline; //相對截止時間
    private List<Long> relativeDeadlineList;
    private long computationAmount; //所需工作量
    private List<Long> computationAmountList;
    private	int priorityLevel; //如有用到OPA之所需優先權層級（之後可新增其他關鍵性層級進行切換）(testing version)
    private Priority priority; //使用於靜態優先權分配所需屬性
    private Long criticalityLevel; 
    private List<Long> criticalityLevelList;
    private Core localCore; //使用於Partitioned Scheduling分配Core
    private CriticalSectionSet criticalSectionSet; //執行過程中所需的CriticalSection
    private long totalCriticalSectionTime;//整體的CriticalSectionTime
    private TaskSet parentTaskSet; //所屬工作集合
    private Job curJob;
    private int jobMissDeadlineCount = 0;
    private int jobCompletedCount = 0;
    private Vector<Job> jobSet;
    private Vector<SharedResource> sharedResourceSet;
    private Vector<Nest> nestSet;

    /**
     * Create a Task with default values (zeroed fields and empty collections).
     */
    public Task()
    {
        this.ID = 0;
        this.JobCount = 0;
        this.enterTime = 0;
        this.period = 0;
        this.relativeDeadline = 0;
        this.relativeDeadlineList = new ArrayList<>();
        this.computationAmount = 0;
        this.computationAmountList = new ArrayList<>();
        this.priorityLevel = 0;
        this.criticalityLevel = 0L;
        this.criticalityLevelList = new ArrayList<>();
        this.priority = new Priority(0);
        this.localCore = null;
        this.criticalSectionSet = new CriticalSectionSet();
        this.parentTaskSet = null;
        this.sharedResourceSet = new Vector<SharedResource>();
        this.jobSet = new Vector<>();
        this.curJob = new Job();
        this.nestSet = new Vector<Nest>();
    }
    
    /*Operating*/
    /**
     * Set the task's priority level (used by some partitioning/OPA modes).
     *
     * @param priorityLevel numeric priority level
     */
    public void addPriorityLevel(int priorityLevel) {
    	this.priorityLevel = priorityLevel;
    }
    
    /**
     * Set the task's criticality level.
     *
     * @param criticalityLevel the criticality level value
     */
    public void addCriticalityLevel(Long criticalityLevel) {
    	this.criticalityLevel = criticalityLevel;
    }
    
    /**
     * Add a critical section to this task and update the shared resource set.
     * <p>
     * The method also sorts critical sections so they are ordered by their
     * relative times (important for correct lock ordering and analysis).
     * </p>
     *
     * @param cs the CriticalSection to add
     */
    public void addCriticalSection(CriticalSection cs)
    {
        this.criticalSectionSet.add(cs);
        this.sharedResourceSet.add(cs.getUseSharedResource());
        
        //排序用
        PriorityQueue<CriticalSection> PCS = new PriorityQueue<CriticalSection>();
        PCS.addAll(this.criticalSectionSet);
        this.criticalSectionSet.removeAllElements();
        while(!PCS.isEmpty())
        {
            this.criticalSectionSet.add(PCS.poll());
        }
    }
    
    /**
     * Produce a new Job instance for this Task at the given production time.
     * <p>
     * The job is initialized with IDs, timing (release and absolute
     * deadline), target work amount and priority/criticality values derived
     * from this Task. The job is recorded in the task's job set and returned
     * to the caller for scheduling/placement.
     * </p>
     *
     * @param produceTime the absolute time at which the job is released
     * @return the newly created Job
     */
    public Job produceJob(long produceTime)
    {
        this.JobCount++;
        Job j = new Job();
        this.curJob = j;
        j.setID(this.JobCount);
        j.setParentTask(this);
        j.setReleaseTime(produceTime);
        j.setAbsoluteDeadline(produceTime + this.relativeDeadline);
        j.setTargetAmount(this.computationAmount);
        j.setPriorityLevel(this.priorityLevel);
        j.setCriticalityLevel(this.criticalityLevel);
        
        j.setOriginalPriority(this.priority);
        j.setCurrentProiority(this.priority);
        j.setCriticalSectionSet(this.criticalSectionSet);
        j.setMaxProcessingSpeed(this.ParentTaskSet().getProcessingSpeed());
        this.jobSet.add(j);
       // println("t("+ this.ID +","+ j.getID() +") :" + j.getReleaseTime() );
        return j;
    }
    
    /**
     * Print debug information about this Task and its configuration.
     */
    public void showInfo()
    {
        println("Task(" + this.ID + "):");
        println("    EnterTime: " + this.enterTime);
        println("    period: " + this.period);
        println("    periodList: " + this.periodList);
        println("    RelativeDeadline: " + this.relativeDeadline);
        println("    RelativeDeadlineList: " + this.relativeDeadlineList);
        println("    ComputationAmount: " + this.computationAmount);
        println("    ComputationAmountList: " + this.computationAmountList);
        println("    PriorityLevel: " + this.priorityLevel);
        println("    CriticalityLevel: " + this.criticalityLevel);
        println("    CriticalityLevelList: " + this.criticalityLevelList);
        println("    CriticalSection:");
        for(CriticalSection cs : this.criticalSectionSet)
        {
            println("        CriticalSection(R" + cs.getUseSharedResource().getID() + "):" + cs.getRelativeStartTime() + "/" + cs.getRelativeEndTime());
        }
        println();
    }
    
    /*SetValue*/
    /**
     * Set the task identifier.
     *
     * @param id task identifier
     */
    public void setID(int id)
    {
        this.ID = id;
    }
    
    /**
     * Set the time this task first entered the system.
     *
     * @param t the enter time
     */
    public void setEnterTime(long t)
    {
        this.enterTime = t;
    }
    
    /**
     * Set the primary period for this task.
     *
     * @param p the period value
     */
    public void setPeriod(long p)
    {
        this.period = p;
    }
    
    /**
     * Set the list of per-criticality periods used in multi-criticality modes.
     *
     * @param l the list of period values
     */
    public void setPeriodList(List<Long> l) {
    	this.periodList = l;
    }
    
    /**
     * Set the relative deadline for jobs of this task.
     *
     * @param d relative deadline
     */
    public void setRelativeDeadline(long d)
    {
        this.relativeDeadline = d;
    }
    
    /**
     * Set the list of per-criticality relative deadlines.
     *
     * @param l list of relative deadlines
     */
    public void setRelativeDeadlineList(List<Long> l) {
    	this.relativeDeadlineList = l;
    }
    
    /**
     * Set the computation amount (WCET) for this task.
     *
     * @param c computation amount
     */
    public void setComputationAmount(long c)
    {
        this.computationAmount = c;
    }
    
    /**
     * Set the list of per-criticality computation amounts.
     *
     * @param l list of computation amounts
     */
    public void setComputationAmountList(List<Long> l) {
    	this.computationAmountList = l;
    }
    
    /**
     * Set the static priority used by this task template.
     *
     * @param p the Priority instance
     */
    public void setPriority(Priority p)
    {
        this.priority = p;
    }
    
    /**
     * Set the numeric priority level (for OPA/testing modes).
     *
     * @param pl priority level
     */
    public void setPriorityLevel(int pl) {
    	this.priorityLevel = pl;
    }
    
    /**
     * Set the task's criticality level.
     *
     * @param cl criticality level
     */
    public void setCriticalityLevel(Long cl) {
    	this.criticalityLevel = cl;
    }
    
    /**
     * Set the list of criticality levels.
     *
     * @param cl list of criticality levels
     */
    public void setCriticalityLevelList(List<Long> cl) {
    	this.criticalityLevelList = cl;
    }
    
    /**
     * Assign the task a local core (used by partitioned scheduling).
     *
     * @param c the Core to assign
     */
    public void setLocalCore(Core c)
    {
        this.localCore = c;
    }
    
    /**
     * Set the parent TaskSet that owns this task.
     *
     * @param ts the owning TaskSet
     */
    public void setParentTaskSet(TaskSet ts)
    {
        this.parentTaskSet = ts;
    }
    
    /*設定TotalCriticalSectionTime*/
    /**
     * Compute and set the total critical section time for this task.
     * <p>
     * The routine excludes critical sections that are nested inside others
     * to avoid double-counting.
     * </p>
     */
    public void setTotalCriticalSectionTime()
    {
        CriticalSection criticalSection = null;
        for(CriticalSection cs : this.criticalSectionSet)
        {
            for(CriticalSection cs2 : this.criticalSectionSet)
            {
                if(cs != cs2 && cs2.getRelativeStartTime() <= cs.getRelativeStartTime() && cs.getRelativeEndTime() <= cs2.getRelativeEndTime())
                {
                    criticalSection = cs2;
                    break;
                }
            }
            
            if(criticalSection == null)
            {
                this.totalCriticalSectionTime += cs.getExecutionTime();
            }
            
            criticalSection = null;
        }   
    }
    
    /**
     * Add a nest (collection of nested critical sections) to this task.
     *
     * @param n the Nest to add
     */
    public void addNest(Nest n)
    {
        this.nestSet.add(n);
    }
    
    /*GetValue*/
    /**
     * Return the task identifier.
     *
     * @return the task ID
     */
    public int getID()
    {
        return this.ID;
    }
    
    /**
     * Return the time this task first entered the system.
     *
     * @return enter time
     */
    public long getEnterTime()
    {
        return this.enterTime;
    }
    
    /**
     * Return the primary period of the task.
     *
     * @return the period
     */
    public long getPeriod()
    {
        return this.period;
    }
    
    /**
     * Return the list of per-criticality periods.
     *
     * @return the period list
     */
    public List<Long> getPeriodList() {
    	return this.periodList;
    }
    
    /**
     * Return the task's relative deadline.
     *
     * @return relative deadline
     */
    public long getRelativeDeadline()
    {
        return this.relativeDeadline;
    }
    
    /**
     * Return the list of relative deadlines per criticality.
     *
     * @return relative deadline list
     */
    public List<Long> getRelativeDeadlineList()
    {
    	return this.relativeDeadlineList;
    }
    
    /**
     * Return the computation amount (WCET) for the task.
     *
     * @return computation amount
     */
    public long getComputationAmount()
    {
        return this.computationAmount;
    }
    
    /**
     * Return the list of computation amounts per criticality.
     *
     * @return computation amount list
     */
    public List<Long> getComputationAmountList()
    {
    	return this.computationAmountList;
    }
    
    /**
     * Return the static priority associated with this task.
     *
     * @return the Priority instance
     */
    public Priority getPriority()
    {
        return this.priority;
    }
    
    /**
     * Return the numeric priority level.
     *
     * @return the priority level
     */
    public int getPriorityLevel() {
    	return this.priorityLevel;
    }

    /**
     * Return the task's criticality level.
     *
     * @return criticality level
     */
    public Long getCriticalityLevel() { 
    	return this.criticalityLevel;
    }
    
    /**
     * Return the list of criticality levels.
     *
     * @return criticality level list
     */
    public List<Long> getCriticalityLevelList(){
    	return this.criticalityLevelList;
    }
    
    /**
     * Return the local core assigned to this task (partitioned scheduling).
     *
     * @return the Core or null if unassigned
     */
    public Core getLocalCore()
    {
        return this.localCore;
    }
    
    /**
     * Return the TaskSet that owns this task.
     *
     * @return the parent TaskSet
     */
    public TaskSet ParentTaskSet()
    {
        return this.parentTaskSet;
    }
    
    /**
     * Return the set of shared resources used by this task.
     *
     * @return a Vector of SharedResource
     */
    public Vector<SharedResource> getResourceSet()
    {
        return this.sharedResourceSet;
    }
    
    /**
     * Compute and return the utilization fraction (computationAmount / period).
     *
     * @return utilization as a double
     */
    public double getUtilization()
    {
        return (double)this.computationAmount / this.period;
    }
    
    /**
     * Return the task's CriticalSectionSet.
     *
     * @return the CriticalSectionSet
     */
    public CriticalSectionSet getCriticalSectionSet()
    {
        return this.criticalSectionSet;
    }
    
    /**
     * Return the currently produced Job for this task (latest job instance).
     *
     * @return the current Job
     */
    public Job getCurJob() 
    {
        return this.curJob;
    }
    
    /**
     * Increment the counter of missed deadlines for this task.
     */
    public void addJobMissDeadlineCount()
    {
        this.jobMissDeadlineCount++;
    }
    
    /**
     * Return the number of missed-deadline jobs.
     *
     * @return missed deadline count
     */
    public int getJobMissDeadlineCount()
    {
        return this.jobMissDeadlineCount;
    }
    
    /**
     * Increment the counter of completed jobs for this task.
     */
    public void addJobCompletedCount()
    {
        this.jobCompletedCount++;
    }
    
    /**
     * Return the number of completed jobs for this task.
     *
     * @return completed job count
     */
    public int getJobCompletedCount()
    {
        return this.jobCompletedCount;
    }
    
    /**
     * Return the vector of all jobs produced by this task.
     *
     * @return the job set vector
     */
    public Vector<Job> getJobSet()
    {
        return this.jobSet;
    }
        
    /**
     * Return the count of jobs (completed + missed).
     *
     * @return total job count
     */
    public int getJobCount()
    {
        return (this.jobCompletedCount + this.jobMissDeadlineCount);
    }
    
    /**
     * Return the precomputed total critical section time.
     *
     * @return total critical section time
     */
    public long getTotalCriticalSectionTime()
    {
        return this.totalCriticalSectionTime;
    }
    
    /**
     * Compute and return the average response time across completed/missed jobs.
     *
     * @return average response time or 0 when no finished jobs exist
     */
    public double getAverageResponseTimeOfJob()
    {
        double time = 0;
        
        for(Job j : this.jobSet)
        {
            if(j.getStatus() == JobStatus.COMPLETED || j.getStatus() == JobStatus.MISSDEADLINE)
            {
                time = RTSimulatorMath.add(time, j.getResponseTime());
            }
        }
        
        if(this.getJobCount() != 0)
        {
            return RTSimulatorMath.div(time,this.getJobCount());
        }
        else
        {
            return 0;
        }
    }
    
    /**
     * Compute and return the average pending time across completed/missed jobs.
     *
     * @return average pending time or 0 when no finished jobs exist
     */
    public double getAveragePendingTimeOfJob()
    {
        double time = 0;
        
        for(Job j : this.jobSet)
        {
            if(j.getStatus() == JobStatus.COMPLETED || j.getStatus() == JobStatus.MISSDEADLINE)
            {
                time = RTSimulatorMath.add(time, j.getPendingTime());
            }
        }
        
        if(this.getJobCount() != 0)
        {
            return RTSimulatorMath.div(time,this.getJobCount());
        }
        else
        {
            return 0;
        }
    }
    
    /**
     * Compute and return the average blocked-time ratio across completed/missed jobs.
     *
     * @return average ratio or 0 when no finished jobs exist
     */
    public double getAverageBeBlockedTimeRatioOfJob()
    {
        double ratio = 0;
        for(Job j : this.jobSet)
        {
            if(j.getStatus() == JobStatus.COMPLETED || j.getStatus() == JobStatus.MISSDEADLINE)
            {
                ratio = RTSimulatorMath.add(ratio,j.getBeBlockedTimeRatio());
            }
        }
        
        if(this.getJobCount() != 0)
        {
            return RTSimulatorMath.div(ratio,this.getJobCount());
        }
        else
        {
            return 0;
        }
    }
    
    /**
     * Return the list of nests (nested critical sections) for this task.
     *
     * @return a Vector of Nest objects
     */
    public Vector<Nest> getNestSet()
    {
        return this.nestSet;
    }
    
    
}