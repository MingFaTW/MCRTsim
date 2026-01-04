/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package workloadgenerator;

import java.text.DecimalFormat;
import java.util.Vector;

/**
 * A collection of generator tasks used by the workload generator.
 * <p>
 * This class extends {@link Vector} to hold {@link wgTask} instances and
 * tracks aggregate statistics such as total utilization and average CSR.
 * It also provides convenience methods to add/remove tasks while keeping
 * the aggregate utilization up to date.
 * </p>
 *
 * @author YC
 */
public class wgTaskSet extends Vector<wgTask>
{
    /**
     * Back-reference to the parent workload that owns this task set.
     * <p>
     * Generator code uses this public field to access global configuration
     * values (for example, accuracy and CSR parameters) when producing tasks
     * and their critical sections.
     * </p>
     */
    public wgWorkload parent;
    private double totalUtilization = 0;
    
    /**
     * Create a wgTaskSet associated with the provided parent workload.
     *
     * @param p the owning {@link wgWorkload} instance
     */
    public wgTaskSet(wgWorkload p)
    {
        super();
        this.parent = p;
    }
    
    /**
     * Remove the specified task from this set and update the total utilization.
     * <p>
     * The method subtracts the removed task's utilization from the running
     * total and then removes the task from the collection.
     * </p>
     *
     * @param t the {@link wgTask} to remove
     */
    public void removeTask(wgTask t)
    {
        this.totalUtilization=wgMath.sub(this.totalUtilization,t.getUtilization());
        this.remove(t);
    }
    
/*setValue*/ 
    /**
     * Add a new task to this set and update the total utilization.
     * <p>
     * The task receives an ID equal to the new size of the set and the
     * aggregate utilization is increased by the task's utilization.
     * </p>
     *
     * @param t the {@link wgTask} to add
     */
    public void addTask(wgTask t)
    {
        this.add(t);
        t.setID(this.size());
        this.totalUtilization=wgMath.add(this.totalUtilization,t.getUtilization());
    }
/*getValue*/
    /**
     * Return the task at the given index.
     *
     * @param i index of the desired task
     * @return the {@link wgTask} at index {@code i}
     */
    public wgTask getTask(int i)
    {
        return this.get(i);
    }
    
    /**
     * Compute and return the mean Critical Section Ratio (CSR) across tasks.
     * <p>
     * The method sums the CSR of each task and returns the average. If the
     * set is empty this will return NaN per the underlying arithmetic; in
     * practice the generator ensures tasks exist before calling this.
     * </p>
     *
     * @return the average CSR for tasks in this set
     */
    public double getTotalCriticalSectionRatio()
    {
        double actualCSR = 0;
        for(wgTask t : this)
        {
            actualCSR = wgMath.add(actualCSR, t.getCriticalSectionRatio());
        }
        return wgMath.div(actualCSR, this.size());
    }
    
    /**
     * Return the accumulated utilization of all tasks in this set.
     *
     * @return the total utilization (sum of per-task utilizations)
     */
    public double getTotalUtilization()
    {
        return this.totalUtilization;
    }
    
}