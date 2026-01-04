/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package WorkLoad;

/**
 * Represents a single physical (or logical) resource instance managed by a
 * {@link SharedResource} pool.
 * <p>
 * A Resource carries an identifier, a back-reference to its parent
 * {@link SharedResource}, the Job that currently holds the lock (if any),
 * and a relative end time indicating when the current lock expires.
 * </p>
 * <p>
 * Inline comments in the original file are respected: Resource objects are
 * short-lived holders used by locking/unlocking operations in the simulator's
 * resource management routines.
 * </p>
 *
 * @author ShiuJia
 */
public class Resource
{
    private int ID;
    private SharedResource parentResource;
    private Job lockedBy;
    private double relativeEndTime;
    
    /**
     * Constructs a new Resource instance.
     */
    public Resource()
    {
        this.ID = 0;
        this.parentResource = null;
        this.lockedBy = null;
        this.relativeEndTime = 0;
    }
    
    /*Operating*/
    /**
     * Mark this resource as locked by a specific job.
     *
     * @param j the Job acquiring the lock on this resource
     */
    public void setLockedBy(Job j)
    {
        this.lockedBy = j;
    }
    
    /**
     * Release this resource, clearing any lock and resetting the relative end time.
     */
    public void unlock()
    {
        this.lockedBy = null;
        this.relativeEndTime = 0;
    }
    
    /**
     * Return the Job that currently holds this resource lock, or null when
     * the resource is free.
     *
     * @return the Job holding the lock or null
     */
    public Job whoLocked()
    {
        return this.lockedBy;
    }
    
    /*SetValue*/
    /**
     * Set the integer identifier for this resource.
     *
     * @param id the id to assign
     */
    public void setID(int id)
    {
        this.ID = id;
    }
    
    /**
     * Assign the owning SharedResource container for this resource.
     *
     * @param r the parent SharedResource
     */
    public void setParentResource(SharedResource r)
    {
        this.parentResource = r;
    }
    
    /**
     * Set the relative end time for the current lock on this resource.
     *
     * @param t the relative end time (units consistent with the simulator)
     */
    public void setRelativeEndTime(double t)
    {
        this.relativeEndTime = t;
    }
    
    /*GetValue*/
    /**
     * Return this resource's identifier.
     *
     * @return the resource ID
     */
    public int getID()
    {
        return this.ID;
    }
    
    /**
     * Return the SharedResource that owns this Resource instance.
     *
     * @return the parent SharedResource
     */
    public SharedResource getParentResource()
    {
        return this.parentResource;
    }
    
    /**
     * Return the relative end time for the current lock.
     *
     * @return the relative end time (0 if unlocked)
     */
    public double getRelativeEndTime()
    {
        return this.relativeEndTime;
    }
}