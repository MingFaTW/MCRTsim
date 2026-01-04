/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package WorkLoad;

import WorkLoadSet.TaskSet;
import java.util.Vector;
import static RTSimulator.RTSimulator.println;

/**
 * Represents a pool of identical resources that can be locked by Jobs.
 * <p>
 * A {@code SharedResource} contains multiple {@link Resource} instances and
 * maintains bookkeeping such as the set of tasks that may access the
 * resource, a priority-inheritance waiting queue ({@code PIPQueue}), and the
 * number of currently idle resource instances. It is used by the simulator to
 * manage lock/unlock operations, resource creation and protocol-specific
 * behavior (e.g., MSRP/global detection).
 * </p>
 * <p>
 * Inline comments in the original file indicate this class is used by MSRP
 * detection (isGlobal) and that {@code Resource} objects are created and
 * assigned IDs when {@link #createResources(int)} is invoked.
 * </p>
 *
 * @author ShiuJia
 */
public class SharedResource extends Vector<Resource>
{
    private int ID;
    private TaskSet accessTaskSet;
    private Vector<Job> PIPQueue;
    private int idleResourceNum;
    private boolean isGlobal = false;//for MSRP
    
    /**
     * Constructs a new SharedResource instance.
     */
    public SharedResource()
    {
        super();
        this.ID = 0;
        this.accessTaskSet = new TaskSet();
        this.PIPQueue = new Vector<Job>();
        this.idleResourceNum = 0;
    }
    
    /**
     * Create and add {@code n} Resource instances to this pool.
     * <p>
     * Each Resource receives a sequential ID starting at 1 and a back-reference
     * to this SharedResource. The idle resource counter is initialized to
     * {@code n}.
     * </p>
     *
     * @param n the number of Resource instances to create
     */
    public void createResources(int n)
    {
        for(int i = 0; i < n; i++)
        {
            Resource r = new Resource();
            r.setID(i + 1);
            r.setParentResource(this);
            this.add(r);
        }
        this.idleResourceNum = n;
    }
    
    /**
     * Register a Task as an accessor of this shared resource.
     *
     * @param t the Task that may access the resource
     */
    public void addAccessTask(Task t)
    {
        this.accessTaskSet.add(t);
    }
    
    /**
     * Attempt to lock a free Resource for the given Job and mark it with the
     * specified critical section end time.
     * <p>
     * The method iterates resources in the pool and assigns the first free
     * Resource it finds to the Job. The resource's relative end time is set
     * from the critical section, and the job is recorded as having locked that
     * resource. The idle resource count is decremented.
     * </p>
     *
     * @param j the Job acquiring a lock
     * @param cs the CriticalSection describing the lock duration
     */
    public void setLock(Job j, CriticalSection cs)
    {
        for(Resource r : this)
        {
            if(r.whoLocked() == null)
            {
                r.setLockedBy(j);
                r.setRelativeEndTime(cs.getRelativeEndTime());
                cs.setResourceID(r.getID());
                this.idleResourceNum--;
                break;
            }
        }
    }
    
    /**
     * Release resources locked by the specified Job.
     * <p>
     * All Resource instances whose {@link Resource#whoLocked()} equals {@code j}
     * are unlocked and the idle resource counter is incremented.
     * </p>
     *
     * @param j the Job releasing locks
     */
    public void setUnlock(Job j)
    {
        for(Resource r : this)
        {
            if(r.whoLocked() == j)
            {
                r.unlock();
                this.idleResourceNum++;
            }
        }
    }
    
    /**
     * Clear the priority-inheritance (PIP) waiting queue.
     */
    public void releasePIPQueueJob()
    {   
        this.PIPQueue.removeAllElements();
    }
    
    /**
     * Add a Job to the priority-inheritance (PIP) waiting queue.
     *
     * @param j the Job to enqueue
     */
    public void addJob2PIPQueue(Job j)
    {
        this.PIPQueue.add(j);
    }

    /**
     * Return the last locked resource's owner whose current core equals the
     * provided Job's current core. This helps resolve ownership when mixing
     * local/global resources.
     *
     * @param j the Job used to compare the owner core
     * @return the Job that locked the most recently used resource on the same core, or null
     */
    public Job getWhoLockedLastResource(Job j)//between local and global resource 會有問題
    {
        for (int i = this.getResourcesAmount()-1 ; i>=0 ; i--)
        {
            if(this.getResource(i).whoLocked() != null && this.getResource(i).whoLocked().getCurrentCore() == j.getCurrentCore())
            {
                return this.getResource(i).whoLocked();
            }
        }
        return null;
    }
    
    /**
     * Mark this shared resource as global when tasks that access it are
     * located on different cores (MSRP detection heuristic).
     * <p>
     * The method examines the task set and sets the {@code isGlobal} flag if
     * more than one local core is involved or if a task has no local core.
     * </p>
     */
    public void setIsGlobal()//for MSRP
    {
        //isGlobal預設是false
        for(int i = 0; i < this.accessTaskSet.size()-1; i++)
        {
            if(this.accessTaskSet.get(i).getLocalCore() != null)
            {
                for(int j = i + 1; j < this.accessTaskSet.size(); j++)
                {
                    if(this.accessTaskSet.get(j).getLocalCore() == null 
                    || this.accessTaskSet.get(i).getLocalCore() != this.accessTaskSet.get(j).getLocalCore())
                    {
                        isGlobal = true;
                        break;
                    }
                }
            }
            else
            {
                isGlobal = true;
                break;
            }
        }
        
    }
    /**
     * Return whether this resource is considered global (MSRP usage).
     *
     * @return {@code true} if global, {@code false} otherwise
     */
    public boolean isGlobal()//for MSRP
    {
        return this.isGlobal;
    }
    
    /**
     * Print debug information about this SharedResource and its access set.
     */
    public void showInfo()
    {
        println("Resource(" + this.ID + "):");
        println("    AccessSet:");
        for(Task t : this.accessTaskSet)
        {
            println("        Task(" + t.getID() + ")");
        }
        println();
    }
    
    /**
     * Set the integer identifier for this SharedResource.
     *
     * @param id integer id to assign
     */
    public void setID(int id)
    {
        this.ID = id;
    }
    
    /**
     * Return the integer identifier for this SharedResource.
     *
     * @return the resource ID
     */
    public int getID()
    {
        return this.ID;
    }
    
    /**
     * Return the PIP waiting queue as a Vector of Jobs.
     *
     * @return the PIPQueue vector
     */
    public Vector<Job> getPIPQueue()
    {
        return this.PIPQueue;
    }
    
    /**
     * Return the Resource at the given index within the pool.
     *
     * @param i index of the resource
     * @return the Resource instance
     */
    public Resource getResource(int i)
    {
        return this.get(i);
    }

    /**
     * Return the number of Resource instances in this pool.
     *
     * @return the pool size
     */
    public int getResourcesAmount() 
    {
        return this.size();
    }
    
    /**
     * Return the set of Tasks that may access this SharedResource.
     *
     * @return the TaskSet of accessing tasks
     */
    public TaskSet getAccessTaskSet()
    {
        return this.accessTaskSet;
    }
    
    /**
     * Return the currently idle resource count.
     *
     * @return number of free resources
     */
    public int getIdleResourceNum()
    {
        return this.idleResourceNum;
    }
    
    /**
     * Count and return the number of currently free Resource instances.
     *
     * @return the count of resources not currently locked
     */
    public int getLeftResourceAmount()
    {
        int i = 0;
        for(Resource res : this)
        {
            if(res.whoLocked() == null)
            {
                i += 1;
            }
        }
        return i ;
        
    }

}