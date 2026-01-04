/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package WorkLoad;

import java.util.Vector;


/**
 * Represents a critical section usage within a task's job.
 * <p>
 * A CriticalSection records which shared resource is used, its relative
 * start/end times (relative to the job or task), and derived execution time.
 * It also supports nesting: a CriticalSection can belong to a {@link Nest},
 * reference an outside critical section, and contain inner critical sections.
 * </p>
 * <p>
 * Inline Chinese comments in the original code are preserved and the Javadoc
 * integrates their meaning: for example, {@code useSharedResource} is the
 * resource being used, {@code relativeStartTime} and {@code relativeEndTime}
 * denote start and end times, and {@code executionTime} is calculated from
 * those times. This class implements {@link Comparable} to allow ordering by
 * relative times (used by priority queues or sorting).
 * </p>
 *
 * @author ShiuJia
 */
public class CriticalSection implements Comparable
{
    private SharedResource useSharedResource; //使用資源
    private long relativeStartTime; //開始時間
    private long relativeEndTime; //結束時間
    private long executionTime; //執行時間

    private int resourceID;
    
    private Nest nest = null;
    private CriticalSection outsideCriticalSection=null;
    private Vector<CriticalSection> innerCriticalSection = new Vector<CriticalSection>();
    
    /**
     * Constructs a new CriticalSection instance.
     */
    public CriticalSection()
    {
        this.useSharedResource = null;
        this.relativeStartTime = 0;
        this.relativeEndTime = 0;
        this.resourceID = 0;
    }
    
    /**
     * Compare this CriticalSection to another by relative start time, then
     * by relative end time. This ordering is used to sort or prioritize
     * critical sections within job/task analyses.
     *
     * @param o the other CriticalSection to compare against (expected type)
     * @return negative if this begins earlier than {@code o}, positive if
     *         later; when start times are equal the longer end time sorts
     *         before the shorter (consistent with original logic)
     * @throws ClassCastException if {@code o} is not a CriticalSection
     */
    @Override
    public int compareTo(Object o)
    {
        CriticalSection cs = (CriticalSection)o;
        if(this.relativeStartTime < cs.relativeStartTime)
        {
            return -1;
        }
        else if(this.relativeStartTime > cs.relativeStartTime)
        {
            return 1;
        }
        else
        {
            if(this.relativeEndTime < cs.relativeEndTime)
            {
                return 1;
            }
            else
            {
                return -1;
            }
        }
    }
    
    /*SetValue*/
    /**
     * Assign the shared resource used by this critical section.
     *
     * @param r the SharedResource instance used in this critical section
     */
    public void setUseSharedResource(SharedResource r)
    {
        this.useSharedResource = r;
    }
    
    /**
     * Set the relative start time of this critical section.
     *
     * @param t the start time relative to the job or task timeline
     */
    public void setRelativeStartTime(long t)
    {
        this.relativeStartTime = t;
    }
    
    /**
     * Set the relative end time of this critical section and update the
     * derived execution time (end - start).
     *
     * @param t the end time relative to the job or task timeline
     */
    public void setRelativeEndTime(long t)
    {
        this.relativeEndTime = t;
        
        this.executionTime = this.relativeEndTime - this.relativeStartTime;
    }
    
    /**
     * Set the identifier of the shared resource used by this critical section.
     *
     * @param rID integer resource identifier
     */
    public void setResourceID(int rID)
    {
        this.resourceID = rID;
    }
    
    /**
     * Associate this critical section with a Nest (a collection of nested
     * critical sections belonging to a task).
     *
     * @param n the Nest that owns this critical section
     */
    public void setNests(Nest n)
    {
        this.nest=n;
    }
    
    /**
     * Add an inner (nested) critical section to this critical section.
     *
     * @param c the inner CriticalSection to add
     */
    public void addInnerCriticalSection(CriticalSection c)
    {
        innerCriticalSection.add(c);
    }
    
    /**
     * Set the outside (enclosing) critical section for this nested critical
     * section.
     *
     * @param c the enclosing CriticalSection (may be null)
     */
    public void setOutsideCriticalSection(CriticalSection c)
    {
        outsideCriticalSection = c;
    }
    
    /*GetValue*/
    /**
     * Get the shared resource used by this critical section.
     *
     * @return the SharedResource instance (may be null)
     */
    public SharedResource getUseSharedResource()
    {
        return this.useSharedResource;
    }
    
    /**
     * Get the relative start time of this critical section.
     *
     * @return the start time relative to the job/task timeline
     */
    public long getRelativeStartTime()
    {
        return this.relativeStartTime;
    }
    
    /**
     * Get the relative end time of this critical section.
     *
     * @return the end time relative to the job/task timeline
     */
    public long getRelativeEndTime()
    {
        return this.relativeEndTime;
    }
    
    /**
     * Get the computed execution duration of this critical section.
     *
     * @return execution time = relativeEndTime - relativeStartTime
     */
    public long getExecutionTime()
    {
        return this.executionTime;
    }
    
    /**
     * Get the integer resource identifier for this critical section.
     *
     * @return resource ID
     */
    public int getResourceID()
    {
        return this.resourceID;
    }
    
    /**
     * Return the Nest that owns this critical section.
     *
     * @return the Nest instance or null if not assigned
     */
    public Nest getNsets()
    {
        return this.nest;
    }
    
    /**
     * Return an inner (nested) critical section by index.
     *
     * @param i index of the inner critical section
     * @return the indexed inner CriticalSection
     * @throws IndexOutOfBoundsException if {@code i} is out of range
     */
    public CriticalSection getInnerCriticalSection(int i)
    {
        return innerCriticalSection.get(i);
    }
    
    /**
     * Get the enclosing (outside) critical section if this is nested.
     *
     * @return the outside CriticalSection or null when this is top-level
     */
    public CriticalSection getOutsideCriticalSection()
    {
        return outsideCriticalSection;
    }
}