/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package WorkLoadSet;


import WorkLoad.Job;
import WorkLoad.Nest;
import WorkLoad.Task;
import java.util.Vector;
import RTSimulator.Definition;
import RTSimulator.Definition.JobStatus;
import static RTSimulator.Definition.magnificationFactor;
import static RTSimulator.RTSimulator.print;
import static RTSimulator.RTSimulator.println;
import RTSimulator.RTSimulatorMath;

/**
 * Collection of tasks participating in the workload. Extends {@link Vector}
 * to store {@link WorkLoad.Task} instances and provides aggregate statistics
 * (utilization, critical section ratios) and performance metrics (average
 * pending/response/blocking times) across all tasks.
 * <p>
 * maxProcessingSpeed is expressed in MHz (單位=MHz). Scheduling horizon
 * calculation integrates least common multiple (LCM) of periods and may fall
 * back to a scaled constant (200 * magnificationFactor) when tasks have
 * non-zero arrival offsets. Nest set construction (see {@link #setNestSetForTask()})
 * groups critical sections into nested structures based on start/end inclusion
 * forming hierarchical resource usage patterns.
 * </p>
 *
 * @author ShiuJia
 */
public class TaskSet extends Vector<Task>
{
    private double maxProcessingSpeed; // 單位=MHz
    private double maximumCriticalSectionRatio,actualCriticalSectionRatio,maximumUtilization,actualUtilization;
    
    
    /**
     * Create an empty TaskSet with initial max processing speed set to 0 MHz.
     */
    public TaskSet()
    {
        super();
        this.maxProcessingSpeed = 0;
    }
    
    /**
     * Set the processor maximum speed in MHz for this task set.
     * @param s speed (MHz)
     */
    public void setProcessingSpeed(double s)
    {
        this.maxProcessingSpeed = s;
    }
    
    /**
     * Set configured maximum critical section ratio (CSR) bound.
     * @param ratio maximum CSR
     */
    public void setMaximumCriticalSectionRatio(double ratio)
    {
        this.maximumCriticalSectionRatio = ratio;
    }
    
    /**
     * Record actual critical section ratio measured after generation.
     * @param ratio actual CSR
     */
    public void setActualCriticalSectionRatio(double ratio)
    {
        this.actualCriticalSectionRatio = ratio;
    }
    
    /**
     * Set configured maximum total utilization.
     * @param U maximum utilization
     */
    public void setMaximumUtilization(double U)
    {
        this.maximumUtilization = U;
    }
    
    /**
     * Record actual total utilization measured after generation/simulation.
     * @param U actual utilization
     */
    public void setActualUtilization(double U)
    {
        this.actualUtilization = U;
    }
    
    /**
     * Get a task by index.
     * @param i zero-based index
     * @return task instance
     */
    public Task getTask(int i)
    {
        return this.get(i);
    }
    
    /**
     * Compute scheduling horizon for the task set.
     * <p>
     * Returns LCM of all task periods when all arrival times are zero;
     * otherwise returns a fallback constant (200 * magnificationFactor).
     * </p>
     * @return schedule horizon time units
     */
    public long getScheduleTimeForTaskSet()//取得排程所需的時間
    {
        long lcmPeriod = this.getLcmOfPeriodForTaskSet();
        
        long biggestEnterTime = getBiggestEnterTime();
        
        return biggestEnterTime == 0 ? lcmPeriod : 200*magnificationFactor;
        
    }
    
    private long getBiggestEnterTime()
    {
        long biggestEnterTime = 0;
        for(Task task : this)
        {
            biggestEnterTime = biggestEnterTime > task.getEnterTime() ? biggestEnterTime : task.getEnterTime();
        }
        return biggestEnterTime;
    }
    
    /*
     * <p>取得TaskSet中所有工作的週期之最小公倍數
     */
    private long getLcmOfPeriodForTaskSet() // 
    {
        RTSimulatorMath e = new RTSimulatorMath();
        long lcm = this.get(0).getPeriod();
        for(int i = 1; i < this.size(); i++)
        {
            lcm = e.Math_lcm(lcm, this.get(i).getPeriod());
        }
        
        return lcm;
    }
    
    /**
     * Get current maximum processing speed (MHz).
     * @return speed in MHz
     */
    public double getProcessingSpeed()
    {
        return this.maxProcessingSpeed;
    }
    
    /**
     * Total number of jobs across all tasks.
     * @return job count
     */
    public int getTotalJobNumber()
    {
        int num = 0;
        for(Task t : this)
        {
            num += t.getJobCount();
        }
        return num;
    }
    
    /**
     * Total number of completed jobs across all tasks.
     * @return completed job count
     */
    public int getTotalJobCompletedNumber()
    {
        int num = 0;
        for(Task t : this)
        {
            num += t.getJobCompletedCount();
        }
        return num;
    }
    
    /**
     * Ratio of jobs completed to total jobs (0 if no jobs exist).
     * @return completion ratio
     */
    public double getJobCompletedRatio()
    {
        if(this.getTotalJobNumber() !=0)
        {
            return RTSimulatorMath.div(this.getTotalJobCompletedNumber(), this.getTotalJobNumber());
        }
        else
        {
            return 0;
        }
    }
    
    /**
     * Total number of missed deadline jobs across all tasks.
     * @return missed deadline job count
     */
    public int getTotalJobMissDeadlineNumber()
    {
        int num = 0;
        for(Task t : this)
        {
            num += t.getJobMissDeadlineCount();
        }
        return num;
    }
    
    /**
     * Ratio of jobs missing deadline to total jobs (0 if no jobs exist).
     * @return miss deadline ratio
     */
    public double getJobMissDeadlineRatio()
    {
        if(this.getTotalJobNumber() !=0)
        {
            return RTSimulatorMath.div(this.getTotalJobMissDeadlineNumber(), this.getTotalJobNumber());
        }
        else
        {
            return 0;
        }
    }
    
    /**
     * Average pending time across tasks (scaled by magnificationFactor).
     * @return average pending time
     */
    public double getAveragePendingTimeOfTask()
    {
        double time = 0;
        for(Task t : this)
        {
            time = RTSimulatorMath.add(time,t.getAveragePendingTimeOfJob());
        }
        
        return RTSimulatorMath.div(RTSimulatorMath.div(time, this.size()),magnificationFactor);
    }
    
    /**
     * Average response time across tasks (scaled by magnificationFactor).
     * @return average response time
     */
    public double getAverageResponseTimeOfTask()
    {
        double time = 0;
        for(Task t : this)
        {
            time = RTSimulatorMath.add(time,t.getAverageResponseTimeOfJob());
        }
        
        return RTSimulatorMath.div(RTSimulatorMath.div(time, this.size()),magnificationFactor);
    }
    
    /**
     * Average ratio of blocked time across tasks (scaled by magnificationFactor).
     * @return average blocked time ratio
     */
    public double getAverageBeBlockedTimeRatioOfTask()
    {
        double ratio = 0;
        for(Task t : this)
        {
            ratio = RTSimulatorMath.add(ratio,t.getAverageBeBlockedTimeRatioOfJob());  
        }
        
        return RTSimulatorMath.div(RTSimulatorMath.div(ratio, this.size()),magnificationFactor);
    }
    
    /**
     * Total utilization accumulation across all tasks.
     * @return total utilization
     */
    public double getTotalUtilization()
    {
        double U = 0;
        
        for(Task t : this)
        {
            U+=t.getUtilization();
        }
        
        return U;
    }
    
    /**
     * Return configured maximum CSR.
     * @return maximum CSR
     */
    public double getMaximumCriticalSectionRatio()
    {
        return this.maximumCriticalSectionRatio;
    }
    
    /**
     * Return measured actual CSR.
     * @return actual CSR
     */
    public double getActualCriticalSectionRatio()
    {
        return this.actualCriticalSectionRatio;
    }
    
    /**
     * Return configured maximum utilization.
     * @return maximum utilization
     */
    public double getMaximumUtilization()
    {
        return this.maximumUtilization;
    }
    
    /**
     * Return measured actual utilization.
     * @return actual utilization
     */
    public double getActualUtilization()
    {
        return this.actualUtilization;
    }
    
    /**
     * Build nested critical section relationships for each task.
     * <p>
     * Iterates critical sections in order, assigning inner/outer relationships
     * when start/end bounds are fully included. Creates a {@link WorkLoad.Nest}
     * per top-level chain and attaches critical sections accordingly.
     * Prints diagnostic information mapping tasks to resource nesting.
     * </p>
     */
    public void setNestSetForTask()
    {
        for(int i=0; i<this.size() ; i++)
        {
            Nest nest = null;
            for(int j=0;j<this.getTask(i).getCriticalSectionSet().size();j++)
            {
                if(j!=0)
                {
                    if(this.getTask(i).getCriticalSectionSet().get(j-1).getRelativeStartTime()<=this.getTask(i).getCriticalSectionSet().get(j).getRelativeStartTime())
                    {
                        if(this.getTask(i).getCriticalSectionSet().get(j-1).getRelativeEndTime()>=this.getTask(i).getCriticalSectionSet().get(j).getRelativeEndTime())
                        {
                            this.getTask(i).getCriticalSectionSet().get(j-1).addInnerCriticalSection(this.getTask(i).getCriticalSectionSet().get(j));
                            this.getTask(i).getCriticalSectionSet().get(j).setOutsideCriticalSection(this.getTask(i).getCriticalSectionSet().get(j-1));
                            if(this.getTask(i).getCriticalSectionSet().get(j).getOutsideCriticalSection()==null)
                            {
                                nest = new Nest(getTask(i));
                                nest.addCriticalSection(this.getTask(i).getCriticalSectionSet().get(j));
                                this.getTask(i).addNest(nest);
                            }
                            else 
                            {
                                nest.addCriticalSection(this.getTask(i).getCriticalSectionSet().get(j));
                            }
                        }
                        else
                        {
                            int temp=2;
                            boolean run=true;
                            
                            while(j-temp>=0 && run)
                            {
                                
                                if(this.getTask(i).getCriticalSectionSet().get(j-temp).getRelativeEndTime() >= this.getTask(i).getCriticalSectionSet().get(j).getRelativeEndTime())
                                {
                                    
                                    this.getTask(i).getCriticalSectionSet().get(j-temp).addInnerCriticalSection(this.getTask(i).getCriticalSectionSet().get(j));
                                    this.getTask(i).getCriticalSectionSet().get(j).setOutsideCriticalSection(this.getTask(i).getCriticalSectionSet().get(j-temp));
                                    if(this.getTask(i).getCriticalSectionSet().get(j).getOutsideCriticalSection()==null)
                                    {
                                        nest = new Nest(this.getTask(i));
                                        nest.addCriticalSection(this.getTask(i).getCriticalSectionSet().get(j));
                                        this.getTask(i).addNest(nest);
                                        run=false;
                                    }
                                    else
                                    {
                                        nest.addCriticalSection(this.getTask(i).getCriticalSectionSet().get(j));
                                        run=false;
                                    }
                                }
                                temp++;
                            }
                            if(run)
                            {
                                nest = new Nest(this.getTask(i));
                                nest.addCriticalSection(this.getTask(i).getCriticalSectionSet().get(j));
                                this.getTask(i).addNest(nest);
                            }
                        }
                    }
                    
                }
                else
                {
                    nest = new Nest(this.getTask(i));
                    nest.addCriticalSection(this.getTask(i).getCriticalSectionSet().get(j));
                    this.getTask(i).addNest(nest);
                } 
            } 
        }
        
        
        for(int i=0;i<this.size();i++)
        {
            print("Task"+this.get(i).getID());
            for(int j=0;j<this.get(i).getNestSet().size();j++)
            {
                print(" have "+this.get(i).getNestSet().size()+" :");
                for(int k=0;k<this.get(i).getNestSet().get(j).size();k++)
                {
                    print(" R"+this.get(i).getNestSet().get(j).get(k).getUseSharedResource().getID());
                }
            }
            println("");
        } 
        println("=======================");
        for(int i=0;i<this.size();i++)
        {
            print("Task"+this.get(i).getID());
            for(int j=0;j<this.get(i).getNestSet().size();j++)
            {
                print(" have "+this.get(i).getNestSet().size()+" :");
                for(int k=0;k<this.get(i).getNestSet().get(j).size();k++)
                {
                    print(" R"+this.get(i).getNestSet().get(j).get(k).getUseSharedResource().getID());
                    if(this.get(i).getNestSet().get(j).get(k).getOutsideCriticalSection()!=null)
                    {
                        print("( R"+this.get(i).getNestSet().get(j).get(k).getUseSharedResource().getID());
                        print(" is inner R"+this.get(i).getNestSet().get(j).get(k).getOutsideCriticalSection().getUseSharedResource().getID()+")");
                    }
                }
            }
            println("");
        } 
    }
    
    
    
}