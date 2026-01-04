/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ResultSet;

import SystemEnvironment.Core;
import WorkLoad.CriticalSection;
import WorkLoad.Job;
import java.util.Stack;
import java.util.Vector;
import RTSimulator.Definition.CoreStatus;
import static RTSimulator.Definition.magnificationFactor;

/**
 * Represents scheduling information for a core at a specific time period.
 * 
 * @author ShiuJia
 */
public class SchedulingInfo
{
    private Core core;
    private CoreStatus status;
    private Job job;
    private double startTime;
    private double endTime;
    private double useSpeed;
    private double normalizationOfSpeed;
    private long totalPowerConsumption;
    private Stack<CriticalSection> enteredCriticalSectionSet;
    
    private int jobMissDeadlineNum;
    private int jobCompletedNum;
    /**
     * Constructs a new SchedulingInfo instance.
     */
    public SchedulingInfo()
    {
        this.core = null;
        this.status = null;
        this.job = null;
        this.startTime = 0;
        this.endTime = 0;
        this.useSpeed = 0;
        this.normalizationOfSpeed = 0;
        this.totalPowerConsumption = 0;
        this.enteredCriticalSectionSet = new Stack<>();
        this.jobMissDeadlineNum = 0;
        this.jobCompletedNum = 0;
    }
    
    /*SetValue*/
    /**
     * Sets the core.
     * @param c the core
     */
    public void setCore(Core c)
    {
        this.core = c;
    }
    
    /**
     * Sets the core status.
     * @param s the core status
     */
    public void setCoreStatus(CoreStatus s)
    {
        this.status = s;
    }
    
    /**
     * Sets the job.
     * @param j the job
     */
    public void setJob(Job j)
    {
        if(j != null)
        {
            this.job = j;
            this.setEnteredCriticalSectionSet(j.getEnteredCriticalSectionSet());

            this.jobMissDeadlineNum = j.getParentTask().getJobMissDeadlineCount();
            this.jobCompletedNum = j.getParentTask().getJobCompletedCount();
            j.addSchedulingInfo(this);
        }
    }
    
    /**
     * Sets the start time.
     * @param t the start time
     */
    public void setStartTime(double t)
    {
        this.startTime = t;
    }
    
    /**
     * Sets the end time.
     * @param t the end time
     */
    public void setEndTime(double t)
    {
        this.endTime = t;
    }
    
    /**
     * Sets the use speed.
     * @param f the frequency
     * @param n the normalization value
     */
    public void setUseSpeed(double f,double n)
    {
        this.useSpeed = f;
        this.setNormalizationOfSpeed(n);
    }
    
    /**
     * Sets the normalization of speed.
     * @param n the normalization value
     */
    public void setNormalizationOfSpeed(double n)
    {
        this.normalizationOfSpeed = n;
    }
    
    /**
     * Sets the total power consumption.
     * @param d the total power consumption
     */
    public void setTotalPowerConsumption(long d)
    {
        this.totalPowerConsumption = d;
    }
    
    /**
     * Sets the entered critical section set.
     * @param criticalSectionSet the critical section set
     */
    public void setEnteredCriticalSectionSet(Stack<CriticalSection> criticalSectionSet)
    {
        if(this.status.equals(CoreStatus.EXECUTION))
        {
            this.enteredCriticalSectionSet.addAll(criticalSectionSet);
        }
    }
    
    /**
     * Sets the job miss deadline count.
     * @param num the number of jobs that missed deadlines
     */
    public void setJobMissDeadlineNum(int num)
    {
        this.jobMissDeadlineNum = num;
    }
    
    /**
     * Sets the job completed count.
     * @param num the number of completed jobs
     */
    public void setJobCompletedNum(int num)
    {
        this.jobCompletedNum = num;
    }
    /*GetValue*/
    /**
     * Gets the core.
     * @return the core
     */
    public Core getCore()
    {
        return this.core;
    }
    
    /**
     * Gets the core status.
     * @return the core status
     */
    public CoreStatus getCoreStatus()
    {
        return this.status;
    }
    
    /**
     * Gets the job.
     * @return the job
     */
    public Job getJob()
    {
        return this.job;
    }
    
    /**
     * Gets the start time.
     * @return the start time
     */
    public double getStartTime()
    {
        return this.startTime / magnificationFactor;
    }
    
    /**
     * Gets the end time.
     * @return the end time
     */
    public double getEndTime()
    {
        return this.endTime / magnificationFactor;
    }
    
    /**
     * Gets the use speed.
     * @return the use speed
     */
    public double getUseSpeed()
    {
        return this.useSpeed;
    }
    
    /**
     * Gets the normalization of speed.
     * @return the normalization of speed
     */
    public Double getNormalizationOfSpeed()
    {
        return this.normalizationOfSpeed;
    }
    
    /**
     * Gets the total power consumption.
     * @return the total power consumption
     */
    public double getTotalPowerConsumption()
    {
        return (double)this.totalPowerConsumption / magnificationFactor;
    }
    
    /**
     * Gets the average power consumption.
     * @return the average power consumption
     */
    public double getAveragePowerConsumption()
    {
        return (double)this.totalPowerConsumption / (double)this.endTime;
    }
    
    /**
     * Gets the entered critical section set.
     * @return the entered critical section set
     */
    public Stack<CriticalSection> getEnteredCriticalSectionSet()
    {
        return this.enteredCriticalSectionSet;
    }
    
    /**
     * Gets the job miss deadline count.
     * @return the number of jobs that missed deadlines
     */
    public int getJobMissDeadlineNum()
    {
        return this.jobMissDeadlineNum;
    }
    
    /**
     * Gets the job completed count.
     * @return the number of completed jobs
     */
    public int getJobCompletedNum()
    {
        return this.jobCompletedNum;
    }
}
