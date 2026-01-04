/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package WorkLoadSet;

import SystemEnvironment.Processor;
import WorkLoad.SharedResource;
import WorkLoad.Task;

/**
 * Aggregates fundamental simulation data structures: a processor, task set,
 * and shared resource set. Provides convenience mutators and accessors for
 * adding tasks and shared resources to their respective collections.
 * <p>
 * Acts as a lightweight holder to pass around cohesive configuration/state
 * during initialization or workload loading phases.
 * </p>
 *
 * @author ShiuJia
 */
public class DataSetting
{
    Processor processor;
    TaskSet taskSet;
    SharedResourceSet sharedResourceSet;
    
    /**
     * Construct a DataSetting with fresh Processor, TaskSet and SharedResourceSet instances.
     */
    public DataSetting()
    {
        this.processor = new Processor();
        this.taskSet = new TaskSet();
        this.sharedResourceSet = new SharedResourceSet();
    }
    
    /*Operating*/
    /**
     * Add a task to the internal task set.
     * @param t task to add
     */
    public void addTask(Task t)
    {
        this.taskSet.add(t);
    }
    
    /**
     * Add a shared resource to the internal shared resource set.
     * @param r shared resource to add
     */
    public void addSharedResource(SharedResource r)
    {
        this.sharedResourceSet.add(r);
    }
    
    /*GetValue*/
    /**
     * Get the processor instance.
     * @return processor
     */
    public Processor getProcessor()
    {
        return this.processor;
    }
    
    /**
     * Get a task by index.
     * @param i zero-based index
     * @return the Task at index i
     */
    public Task getTask(int i)
    {
        return this.taskSet.get(i);
    }
    
    /**
     * Get the task set.
     * @return task set
     */
    public TaskSet getTaskSet()
    {
        return this.taskSet;
    }
    
    /**
     * Get a shared resource by index.
     * @param i zero-based index
     * @return shared resource at index i
     */
    public SharedResource getSharedResource(int i)
    {
        return this.sharedResourceSet.get(i);
    }
    
    /**
     * Get the shared resource set.
     * @return shared resource set
     */
    public SharedResourceSet getSharedResourceSet()
    {
        return this.sharedResourceSet;
    }
    
 
    
}