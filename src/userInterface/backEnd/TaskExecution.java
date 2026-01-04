/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package userInterface.backEnd;

import ResultSet.MissDeadlineInfo;
import ResultSet.SchedulingInfo;
import WorkLoad.CriticalSection;
import java.util.ArrayList;
import RTSimulator.Definition.CoreStatus;

/**
 * Immutable-like data carrier describing one contiguous execution-related interval
 * for a task/job on a core, or a deadline miss marker.
 * <p>
 * Built from either a {@link SchedulingInfo} (normal execution / wait / context switch /
 * migration segment) or a {@link MissDeadlineInfo} (deadline miss recorded as a zero-length
 * WRONG status interval). When constructed from a scheduling segment, the instance captures:
 * </p>
 * <ul>
 *   <li>coreID: the core that executed / waited / switched / migrated</li>
 *   <li>taskID / jobID: identifiers of the job's parent task and the job itself</li>
 *   <li>startTime / endTime: segment time bounds (end exclusive for duration math)</li>
 *   <li>executionTime: computed as endTime - startTime (zero for miss markers)</li>
 *   <li>speed: processing speed used during the segment (0 for WRONG)</li>
 *   <li>status: {@link CoreStatus} (EXECUTION, WAIT, CONTEXTSWITCH, MIGRATION, or WRONG)</li>
 *   <li>resourcePanels: UI helper panels representing resources locked in entered critical sections</li>
 * </ul>
 * Resource panels are created from each {@link CriticalSection} present in the scheduling info's
 * entered critical section set at the moment of the segment start.
 *
 * @author ShiuJia
 */
public class TaskExecution
{
    private int coreID;
    private int taskID;
    private int jobID;
    private double executionTime;
    private double startTime;
    private double endTime;
    private double speed;
    private CoreStatus status;
    private ArrayList<ResourcePanel> resourcePanels;

    /**
     * Creates an empty TaskExecution with default values. Intended primarily
     * for frameworks or serialization; typical usage prefers the parameterized constructors.
     */
    public TaskExecution()
    {
        
    }
    
    /**
     * Builds a TaskExecution segment from a {@link SchedulingInfo} describing a core interval.
     * Populates IDs, times, status, speed and creates resource panels for each entered critical section.
     *
     * @param schedulingInfo the source scheduling record (must contain core, job, task, and status)
     */
    public TaskExecution(SchedulingInfo schedulingInfo)
    {
        this.coreID = schedulingInfo.getCore().getID();
        this.taskID = schedulingInfo.getJob().getParentTask().getID();
        this.jobID = schedulingInfo.getJob().getID();
        this.startTime = schedulingInfo.getStartTime();
        this.endTime= schedulingInfo.getEndTime();
        
        if(schedulingInfo.getCoreStatus() == CoreStatus.EXECUTION)
        {
            status = schedulingInfo.getCoreStatus();
            speed = schedulingInfo.getUseSpeed();
        }
        else if(schedulingInfo.getCoreStatus() == CoreStatus.WAIT)
        {
            status = schedulingInfo.getCoreStatus();
            speed = schedulingInfo.getUseSpeed();
        }
        else if(schedulingInfo.getCoreStatus() == CoreStatus.CONTEXTSWITCH)
        {
            status = schedulingInfo.getCoreStatus();
            speed = schedulingInfo.getUseSpeed();
        }
        else if(schedulingInfo.getCoreStatus() == CoreStatus.MIGRATION)
        {
            status = schedulingInfo.getCoreStatus();
            speed = schedulingInfo.getUseSpeed();
        }
        
        resourcePanels = new ArrayList<ResourcePanel>();
        executionTime=endTime-startTime;
        
        for(CriticalSection cs : schedulingInfo.getEnteredCriticalSectionSet())
        {
            resourcePanels.add(new ResourcePanel(cs));
        }
    }
    
    /**
     * Builds a zero-length (start == end) TaskExecution representing a deadline miss.
     * Assigns WRONG status, zero speed, and task/job IDs from the miss info.
     *
     * @param missDeadlineInfo the miss-deadline descriptor
     */
    public TaskExecution(MissDeadlineInfo missDeadlineInfo)
    {
        this.coreID = 0;
        this.taskID = missDeadlineInfo.getMissTask().getID();
        this.jobID = missDeadlineInfo.getMissJob().getID();
        this.startTime = missDeadlineInfo.getMissTime();
        this.endTime = this.startTime;
        this.status = CoreStatus.WRONG;
        this.speed = 0;
        resourcePanels = new ArrayList<ResourcePanel>();
        executionTime=endTime-startTime;
        
    }
    
    /**
     * Returns the duration of the interval in time units (zero for deadline miss markers).
     *
     * @return interval execution time (endTime - startTime)
     */
    public double getExecutionTime()
    {
        return this.executionTime;
    }
    
    /**
     * Returns the inclusive start time of the interval.
     *
     * @return start time
     */
    public double getStartTime()
    {
        return this.startTime;
    }
            
    /**
     * Returns the exclusive (logical) end time of the interval (equal to start time for deadline miss markers).
     *
     * @return end time
     */
    public  double getEndTime()
    {
        return this.endTime;
    }
    
    /**
     * Returns the core status classification of the interval.
     *
     * @return interval status (EXECUTION, WAIT, CONTEXTSWITCH, MIGRATION, WRONG)
     */
    public CoreStatus getStatus()
    {
        return this.status;
    }
    
    /**
     * Returns the processing speed applied during the interval (0 for WRONG state).
     *
     * @return speed value
     */
    public double getSpeed()
    {
        return this.speed;
    }
    
    /**
     * Returns the identifier of the core related to this interval (0 for deadline miss placeholder).
     *
     * @return core ID
     */
    public int getCoreID()
    {
        return this.coreID;
    }
    
    /**
     * Returns the parent task identifier.
     *
     * @return task ID
     */
    public int getTaskID()
    {
        return this.taskID;
    }
    
    /**
     * Returns the job identifier.
     *
     * @return job ID
     */
    public int getJobID()
    {
        return this.jobID;
    }
    
    /**
     * Returns a list of resource panels representing each locked resource during the interval.
     *
     * @return list of resource panels (may be empty, never null)
     */
    public ArrayList<ResourcePanel> getResourcePanels()
    {
        return this.resourcePanels;
    }
}