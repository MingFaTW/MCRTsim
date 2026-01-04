/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package workloadgenerator;
/**
 * Workload model used by the generator to produce synthetic real-time task sets.
 * <p>
 * This class encapsulates configuration ranges (utilization, periods,
 * computation amounts, resource counts, and critical section ratios) and
 * provides routines to create resources, tasks, and their critical sections.
 * It also exposes quality checks and summary printing for debugging/inspection.
 * Existing inline comments and println diagnostics (including Chinese notes such
 * as unit hints like "單位:MHZ") have been incorporated into the descriptions
 * below to preserve intent.
 * </p>
 *
 * @author YC
 */

import static RTSimulator.RTSimulator.println;


public class wgWorkload 
{
    /**
     * Back-reference to the owning {@link WorkloadGenerator} window that
     * collected the user inputs. Used for options such as extra task toggling
     * and accuracy scaling.
     */
    public WorkloadGenerator parent;
    private final String XMLVersion = "1.0";
    private final String XMLEncoding = "UTF-8";
    private final String workloadHeader = "workload";
    private final String maximumUtilizationHeader = "maximumUtilization";
    private final String actualUtilizationHeader = "actualUtilization";
    private final String maximumCriticalSectionRatioHeader = "maximumCriticalSectionRatio";
    private final String actualCriticalSectionRatioHeader = "actualCriticalSectionRatio";
    private final String frequencyHeader = "baseSpeed";
    private int frequency;//單位:MHZ
    
/*Task*/    
    private double maximumUtilization = 0;
    private final String  taskNumberHeader = "numTask";
    private int minNumOfTask = 0;
    private int maxNumOfTask = 0;
    private int taskNumber = 0;
    private wgTaskSet taskSet = new wgTaskSet(this);
    private long mintaskPeriod = 0;
    private long maxTaskPeriod = 0;
    private long minTaskComputationAmount = 0;
    private long maxTaskComputationAmount = 0;
    private int minAccessedResourcesNumber = 0;
    private int maxAccessedResourcesNumber = 0;
    private double minCriticalSectionRatio = 0;
    private double maxCriticalSectionRatio = 0;
    private double extraTaskU=0;
    
/*Resources*/
    private final String  resourcesNumberHeader = "numResource";
    private int minNumOfresources = 0;
    private int maxNumOfresources = 0;
    private int resourcesNumber = 0;
    private wgResourcesSet resourcesSet = new wgResourcesSet(this);
    
    
    
    /**
     * Create a workload wrapper with a link back to the UI/controller.
     *
     * @param p the owning {@link WorkloadGenerator}
     */
    public  wgWorkload(WorkloadGenerator p)
    {
        
        this.parent = p;
        
    }
    
    /**
     * Print initial configuration info for debugging. Outputs period and
     * computation amount bounds to the console via println.
     */
    public void showInitInfo()
    {
        println("!!!!!!!!!!!!!!InitInfo!!!!!!!!!!!!!!!");
        
        println("    TaskPeriodMax = " + this.getTaskPeriodMax());
        println("    TaskPeriodMin = " + this.getTaskPeriodMin());
        println("    ComputationAmountMax = " + this.getTaskComputationAmountMax());
        println("    ComputationAmountMin = " + this.getTaskComputationAmountMin());
      
        println("!!!!!!!!!!!!!!InitInfo!!!!!!!!!!!!!!!");
        
    }
    
    /**
     * Print a verbose summary of the generated workload including resource
     * counts, each task's parameters, and total utilization. Intended for
     * console inspection after generation.
     */
    public void showInfo()
    {
        println("!!!!!!!!!!!!!!Workload!!!!!!!!!!!!!!!");
        println("    ResourcesNumber = " + this.resourcesSet.size());
        println("    ResourceNumber = " + this.resourcesSet.getResources(0).size());
        println("    TaskNumber = " + this.taskSet.size());
        
        for(wgTask t : this.taskSet)
        {
            println("    TaskID = " + t.getID());
            println("        Period = " + t.getPeriod());
            println("        ComputationAmount = " + t.getComputationAmount());
            println("        CriticalSectionNumber = " + t.getCriticalSectionSet().size());
            
            for(wgCriticalSection cs : t.getCriticalSectionSet())
            {
                println("        ResourcesID = " + cs.getResources().getID());
                println("            StartTime = " + cs.getStartTime());
                println("            EndTime = " + cs.getEndTime());
                println("            CriticalSectionTime = " + cs.getCriticalSectionTime());
            }
//            println("        CriticalSectionRatio = " + t.getCriticalSectionSet().getCriticalSectionRatio());
            println("        Utilization = "+ t.getUtilization());
            
        }
        println("TotalUtilization = "+ this.taskSet.getTotalUtilization());
    }
    
    /**
     * Create and register the resource groups for this workload.
     * <p>
     * Currently each wgResources group is initialized with a single
     * {@link wgResource}. The number of groups equals {@code resourcesNumber}.
     * </p>
     */
    public void creatResources() 
    {
    /*creatResources*/
        for(int num = 0; num < this.resourcesNumber ; num++)
        {
            wgResources r = new wgResources(this.resourcesSet);
        /*creatResource目前預設一個*/   
            for(int n = 0; n < 1 ; n++)
            {
                r.addResource(new wgResource(r));
            }
            
            this.resourcesSet.addResources(r);
        }
        println("    ResourcesNumber = " + resourcesNumber);
    }

    /**
     * Create tasks with random parameters within configured ranges until either
     * the task count limit is reached or the total utilization would exceed the
     * maximum utilization. If exceeding, the last task is removed and an extra
     * task may be created to complement utilization if enabled.
     */
    public void creatTask()
    {
        println("TaskNuber = " + taskNumber);
        println("MaximumUtilization = " + maximumUtilization);
        
        for(int num = 0; num < this.taskNumber ; num++)
        {
            wgTask task = new wgTask(this.taskSet); 
            task.setArrivalTime(0);
            task.setPeriod(wgMath.rangeRandom(this.mintaskPeriod, this.maxTaskPeriod));
            task.setRelativeDeadline(task.getPeriod());
            task.setComputationAmount(wgMath.rangeRandom(this.minTaskComputationAmount, this.maxTaskComputationAmount < task.getPeriod() ? this.maxTaskComputationAmount : task.getPeriod()));
            task.setUtilization();
            
            println("Utilization"+task.getUtilization());
            
            this.taskSet.addTask(task);
            
            
            if(this.taskSet.getTotalUtilization() > this.maximumUtilization)
            {
                this.taskSet.removeTask(task);
                if(parent.isExtraTask()) 
                {    
                    this.creatExtraTask();
                }
                break;
            }
        }
        
        this.taskNumber = taskSet.size();
    }
    
    /**
     * Create one extra task to complement the utilization up to the configured
     * maximum, if the gap is greater than 0.02. The extra task sets its period
     * randomly and its computation amount based on the utilization gap.
     */
    public void creatExtraTask()
    {//做補數
        
        extraTaskU=wgMath.sub(this.maximumUtilization,this.taskSet.getTotalUtilization());
        if(extraTaskU > 0.02)
        {
            wgTask task = new wgTask(this.taskSet); 
            task.setArrivalTime(0);
            task.setPeriod(wgMath.rangeRandom(this.mintaskPeriod, this.maxTaskPeriod));
            task.setRelativeDeadline(task.getPeriod());
            task.setComputationAmount((int)wgMath.mul(extraTaskU,task.getPeriod()));
            task.setUtilization();
            this.taskSet.addTask(task);

//            println("complement="+extraTaskU+" task.getPeriod()="+task.getPeriod()
//            +" task.getUtilization()="+task.getUtilization());
//            println("UUUU"+utilization); 
//            println("ttttttt"+taskSet.getTotalUtilization());
        }
    }
    
    /**
     * Generate critical sections for all tasks in this workload by delegating
     * to {@link wgTask#creatCriticalSection()}.
     */
    public void creatCriticalSection()
    {
        for(wgTask t : this.taskSet)
        {
            t.creatCriticalSection();
        }
    }
    
    /**
     * Check whether the generated workload meets basic quality constraints:
     * - Total utilization within [maximumUtilization - 0.02, maximumUtilization]
     * - Number of tasks within configured min/max
     * - Number of resources within configured min/max
     *
     * @return true if all constraints are satisfied; false otherwise
     */
    public boolean checkQuality()
    {
        return 
        (
            (this.maximumUtilization-0.02 <= this.taskSet.getTotalUtilization() && this.taskSet.getTotalUtilization() <= this.maximumUtilization)
            &&(this.minNumOfTask <= this.taskNumber && this.taskNumber <= this.maxNumOfTask)
            &&(this.minNumOfresources <= this.resourcesSet.size() && this.resourcesSet.size() <= this.maxNumOfresources)
        );
        
    }
    
    /**
     * Build an HTML snippet summarizing workload quality and highlighting in red
     * any attribute that is outside its configured range.
     *
     * @return an HTML string suitable for embedding in a Swing JLabel
     */
    public String showQuality()
    {
        String str = "<html>";
        str+= "<text> The generated workload has the following attributes : <text><br><br>";
                
        if( this.maximumUtilization-0.02 <= this.taskSet.getTotalUtilization() && this.taskSet.getTotalUtilization() <= this.maximumUtilization)
        { 
                str += "<text>Utilization = " + this.taskSet.getTotalUtilization() +"<text><br>";
        }
        else
        {
            str += "<text>Utilization = <font color='red'>" + this.taskSet.getTotalUtilization() +"</font><text><br>";
        }
            
        if( this.minNumOfTask <= this.taskNumber && this.taskNumber <= this.maxNumOfTask)
        {
            str += "<text>The Number of Task = " + this.taskNumber +"<text><br>";
        }
        else
        {
            str += "<text>The Number of Task = <font color='red'>" + this.taskNumber +"</font><text><br>";
        }
        
        if( this.minNumOfresources <= this.resourcesSet.size() && this.resourcesSet.size() <= this.maxNumOfresources)
        {
            str += "<text>The Number of Resources = " + this.resourcesSet.size() +"<text><br>";
        }
        else
        {
            str += "<text>The Number of Resources = <font color='red'>" + this.resourcesSet.size() +"</font><text><br>";
        }
        
        str+= "<br><text> Re-generate it or Save ? <text><br>";
        
        return str;
    }
    
/*setValue*/
    /**
     * Set the maximum total utilization for the generated task set.
     *
     * @param u utilization upper bound (0..n)
     */
    public void setUtilization(double u)
    {
        this.maximumUtilization = u;
    }
    
    /**
     * Set the minimum number of tasks to attempt to create.
     *
     * @param number lower bound for task count
     */
    public void setTaskNumberMin(int number) 
    {
        this.minNumOfTask = number;
    }

    /**
     * Set the maximum number of tasks to attempt to create.
     *
     * @param number upper bound for task count
     */
    public void setTaskNumberMax(int number) 
    {
        this.maxNumOfTask = number;
    }
    
    /**
     * Determine and set the target task count based on utilization and
     * computation/period bounds. The computed value is clamped to the
     * configured min/max task numbers.
     */
    public void setTaskNumber()
    {
        int maximumTaskNum = (int)Math.floor(maximumUtilization / (minTaskComputationAmount / maxTaskPeriod));
        println("MaximumTaskNum = " + maximumTaskNum);
        
        if(maximumTaskNum < minNumOfTask)
        {
            this.taskNumber = maximumTaskNum;
        }       
        else
        {
            this.taskNumber = maxNumOfTask;
        }
    }
    
    /**
     * Set minimum task period.
     *
     * @param minimun minimum period (internal scaled units)
     */
    public void setTaskPeriodMin(long minimun) 
    {
        this.mintaskPeriod = minimun;
    }
    
    /**
     * Set maximum task period.
     *
     * @param maximun maximum period (internal scaled units)
     */
    public void setTaskPeriodMax(long maximun) 
    {
        this.maxTaskPeriod = maximun;
    }
    
    /**
     * Set minimum computation amount (WCET) per task.
     *
     * @param minimun lower bound for computation amount
     */
    public void setTaskComputationTimeMin(long minimun) 
    {
        this.minTaskComputationAmount = minimun;
    }

    /**
     * Set maximum computation amount (WCET) per task.
     *
     * @param maximun upper bound for computation amount
     */
    public void setTaskComputationTimeMax(long maximun) 
    {
        this.maxTaskComputationAmount = maximun;
    }
    
    /**
     * Set the minimum number of resource groups to create.
     *
     * @param number lower bound for resource group count
     */
    public void setResourcesNumbermin(int number) 
    {
        this.minNumOfresources = number;
    }

    /**
     * Set the maximum number of resource groups to create.
     *
     * @param number upper bound for resource group count
     */
    public void setResourcesNumbermax(int number) 
    {
        this.maxNumOfresources = number;
    }
    
    /**
     * Randomly choose and set the number of resource groups to create within
     * the configured min/max range.
     */
    public void setResourcesNumber()
    {
        this.resourcesNumber = (int)wgMath.rangeRandom(minNumOfresources, maxNumOfresources);
    }
    
    /**
     * Set the base CPU speed in MHz (單位: MHZ).
     *
     * @param MHZ CPU frequency in MHz
     */
    public void setFrequency(int MHZ)
    {
        this.frequency = MHZ;
    }
    
    /**
     * Set the minimum number of distinct resources a task may access.
     *
     * @param Number lower bound on accessed resource groups
     */
    public void setAccessedResourceNumberMin(int Number)
    {
        this.minAccessedResourcesNumber = Number;
    }
    
    /**
     * Set the maximum number of distinct resources a task may access. If the
     * provided value exceeds the number of resources configured for the
     * workload, it is clamped to that number.
     *
     * @param Number upper bound on accessed resource groups
     */
    public void setAccessedResourceNumberMax(int Number)
    {
        if(Number <= this.resourcesNumber)
        {
            this.maxAccessedResourcesNumber = Number;
        }
        else
        {
            this.maxAccessedResourcesNumber = this.resourcesNumber;
        }
    }
    
    /**
     * Set the minimum critical section ratio (CSR) applied when generating CS.
     *
     * @param ratio minimum CSR
     */
    public void setMinCriticalSectionRatio(double ratio)
    {
        this.minCriticalSectionRatio = ratio;
    }
    
    /**
     * Set the maximum critical section ratio (CSR) applied when generating CS.
     *
     * @param ratio maximum CSR
     */
    public void setMaxCriticalSectionRatio(double ratio)
    {
        this.maxCriticalSectionRatio = ratio;
    }
    
    
    
/*getValue*/
    /**
     * Return the XML version string used in exports.
     *
     * @return XML version
     */
    public String getXMLVersion()
    {
        return this.XMLVersion;
    }
    
    /**
     * Return the XML encoding string used in exports.
     *
     * @return XML encoding
     */
    public String getXMLEncoding()
    {
        return this.XMLEncoding;
    }
    
    /**
     * Return the configured maximum utilization.
     *
     * @return maximum utilization
     */
    public double getMaximumUtilization()
    {
        return this.maximumUtilization;
    }
    
    /**
     * Return the actual utilization of the generated task set.
     *
     * @return actual utilization (sum of task utilizations)
     */
    public double getActualUtilization()
    {
        return this.taskSet.getTotalUtilization();
    }
    
    /**
     * Return the configured minimum number of tasks.
     *
     * @return minimum task count
     */
    public int getTaskNumberMin() 
    {
        return this.minNumOfTask;
    }

    /**
     * Return the configured maximum number of tasks.
     *
     * @return maximum task count
     */
    public int getTaskNumberMax() 
    {
        return this.maxNumOfTask;
    }
    
    /**
     * Return the current number of generated tasks.
     *
     * @return task count
     */
    public int getTaskNumber()
    {
        return this.taskNumber;
    }
    
    /**
     * Return the minimum allowed task period.
     *
     * @return minimum period (internal scaled units)
     */
    public long getTaskPeriodMin() 
    {
        return this.mintaskPeriod;
    }
    
    /**
     * Return the maximum allowed task period.
     *
     * @return maximum period (internal scaled units)
     */
    public long getTaskPeriodMax() 
    {
        return this.maxTaskPeriod;
    }
    
    /**
     * Return the minimum allowed computation amount per task.
     *
     * @return minimum computation amount
     */
    public long getTaskComputationAmountMin() 
    {
        return this.minTaskComputationAmount;
    }

    /**
     * Return the maximum allowed computation amount per task.
     *
     * @return maximum computation amount
     */
    public long getTaskComputationAmountMax() 
    {
        return this.maxTaskComputationAmount;
    }
    
    /**
     * Return the minimum number of resource groups to create.
     *
     * @return minimum resources count
     */
    public int getResourceNumbermin() 
    {
        return this.minNumOfresources;
    }

    /**
     * Return the maximum number of resource groups to create.
     *
     * @return maximum resources count
     */
    public int getMaxResourceNumber() 
    {
        return this.maxNumOfresources;
    }
    
    /**
     * Return the number of resource groups chosen for this workload.
     *
     * @return resources group count
     */
    public int getResourcesNumber()
    {
        return this.resourcesNumber;
    }
    
    /**
     * Return the base CPU frequency in MHz.
     *
     * @return base speed (MHz)
     */
    public int getFrequency()
    {
        return this.frequency;
    }
    
    /**
     * Header key for the root workload element used during export.
     *
     * @return workload header
     */
    public String getWorkloadHeader()
    {
        return this.workloadHeader;
    }
    
    /**
     * Header key for the maximum utilization attribute.
     *
     * @return maximum utilization header
     */
    public String getMaximumUtilizationHeader()
    {
        return this.maximumUtilizationHeader;
    }
    
    /**
     * Header key for the actual utilization attribute.
     *
     * @return actual utilization header
     */
    public String getActualUtilizationHeader()
    {
        return this.actualUtilizationHeader;
    }
    
    /**
     * Header key for the maximum CSR attribute.
     *
     * @return maximum CSR header
     */
    public String getMaximumCriticalSectionRatioHeader()
    {
        return this.maximumCriticalSectionRatioHeader;
    }
    
    /**
     * Header key for the actual CSR attribute.
     *
     * @return actual CSR header
     */
    public String getActualCriticalSectionRatioHeader()
    {
        return this.actualCriticalSectionRatioHeader;
    }
    
    /**
     * Header key for the number of tasks attribute.
     *
     * @return task number header
     */
    public String getTaskNumberHeader()
    {
        return this.taskNumberHeader;
    }
    
    /**
     * Header key for the number of resources attribute.
     *
     * @return resources number header
     */
    public String getResourcesNumberHeader()
    {
        return this.resourcesNumberHeader;
    }
    
    /**
     * Header key for the base speed attribute.
     *
     * @return base speed header
     */
    public String getFrequencyHeader()
    {
        return this.frequencyHeader;
    }
    
    /**
     * Return the set of generated tasks.
     *
     * @return task set
     */
    public wgTaskSet getTaskSet()
    {
        return this.taskSet;
    }
    
    /**
     * Return the set of generated resource groups.
     *
     * @return resources set
     */
    public wgResourcesSet getResourcesSet()
    {
        return this.resourcesSet;
    }
    
    /**
     * Return a random number of accessed resource groups for a task within the
     * configured min/max bounds.
     *
     * @return random accessed resource count
     */
    public int getRandomAccessedResourceNum()
    {
        return (int)wgMath.rangeRandom(minAccessedResourcesNumber, maxAccessedResourcesNumber);
    }
    
    /**
     * Return the minimum Critical Section Ratio (CSR).
     *
     * @return minimum CSR
     */
    public double getMinCriticalSectionRatio()
    {
        return this.minCriticalSectionRatio;
    }
    
    /**
     * Return the maximum Critical Section Ratio (CSR).
     *
     * @return maximum CSR
     */
    public double getMaxCriticalSectionRatio()
    {
        return this.maxCriticalSectionRatio;
    }
    
    /**
     * Return the actual Critical Section Ratio (CSR) for the generated tasks
     * as computed by the task set.
     *
     * @return actual CSR
     */
    public double getActualCriticalSectionRatio()
    {
        return this.taskSet.getTotalCriticalSectionRatio();
    }
}