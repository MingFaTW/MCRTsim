/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package WorkLoadSet;

import WorkLoad.Job;
import WorkLoad.Task;

import java.util.PriorityQueue;
import java.util.Vector;

/**
 * Priority queue of jobs ordered according to their natural ordering
 * (as defined in {@link WorkLoad.Job}). Provides helper operations for
 * resorting after in-place priority mutations and setting blocking time
 * indicators for higher-priority jobs.
 * <p>
 * The {@link #peek()} override returns null when the head job is suspended,
 * allowing callers to treat a suspended head as an empty queue condition.
 * </p>
 *
 * @author ShiuJia
 */
public class JobQueue extends PriorityQueue<Job>
{
    /**
     * Create an empty job priority queue.
     */
    public JobQueue()
    {
        super();
    }
    
    /**
     * Rebuild ordering by polling all elements into a fresh queue and adding
     * them back. Useful when job priorities change in-place and the underlying
     * heap needs to be re-heapified.
     */
    public void reSort()
    {
        JobQueue newQ = new JobQueue();
        while(this.size()!=0)
        {
            newQ.add(this.poll());
        } 
        this.addAll(newQ);
    }
    
    /**
     * Mark jobs with higher original priority than the blocking job as having
     * been blocked for one time unit (setBeBlockedTime(1)). Iterates in current
     * queue order until a job with lower or equal priority is encountered.
     *
     * @param blockingJob the job causing potential blocking of higher-priority jobs
     */
    public void setBlockingTime(Job blockingJob)
    {
        if(!this.isEmpty())
        {
            Object[] jobs = this.toArray();
            for(int i = 0 ; i<jobs.length ; i++)
            {
                if(blockingJob != ((Job)jobs[i]))
                {
                    if(((Job)jobs[i]).getOriginalPriority().compare(blockingJob.getOriginalPriority()) == 1)
                    {
                        ((Job)jobs[i]).setBeBlockedTime(1);
                    }
                    else 
                    {
                        break;
                    }
                }
            }
        }
    }
    
    /**
     * Return the head job unless it is suspended, in which case null is returned.
     * Overrides {@link PriorityQueue#peek()} to filter suspended jobs.
     *
     * @return head job or null if queue empty or head is suspended
     */
    @Override
    public Job peek() 
    {
        Job j = super.peek();
        if(j==null || j.isSuspended)
        {
            return null;
        }
        return j;
    }


}