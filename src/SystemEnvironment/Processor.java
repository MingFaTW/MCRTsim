/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package SystemEnvironment;

import PartitionAlgorithm.PartitionAlgorithm;
import ResultSet.MissDeadlineInfo;
import WorkLoad.CoreSpeed;
import WorkLoad.Job;
import WorkLoad.SharedResource;
import WorkLoad.Task;
import WorkLoadSet.CoreSet;
import WorkLoadSet.JobQueue;
import WorkLoadSet.SharedResourceSet;
import WorkLoadSet.TaskSet;
import concurrencyControlProtocol.ConcurrencyControlProtocol;
import dynamicVoltageAndFrequencyScalingMethod.DynamicVoltageAndFrequencyScalingMethod;
import java.util.Vector;
import RTSimulator.Definition.JobStatus;
import RTSimulator.Definition.PriorityType;
import RTSimulator.Definition.SchedulingType;
import static RTSimulator.RTSimulator.println;
import schedulingAlgorithm.HybridSchedulingAlgorithm;
import schedulingAlgorithm.PartitionedSchedulingAlgorithm;
import schedulingAlgorithm.PriorityDrivenSchedulingAlgorithm;
import schedulingAlgorithm.SingleCoreSchedulingAlgorithm;

/**
 * Central coordinator for the real-time simulator that owns cores, core sets,
 * the global scheduler, concurrency controller, DVFS regulator and the task
 * and resource sets.
 *
 * <p>Processor orchestrates the end-to-end simulation flow across different
 * scheduling modes (SingleCore, Partition, Global, Hybrid). It:
 * <ul>
 *   <li>manages all cores and core groups (CoreSet) and their IDs;</li>
 *   <li>accepts a {@link PriorityDrivenSchedulingAlgorithm} and wires it to
 *       per-core or global schedulers as appropriate;</li>
 *   <li>holds a {@link ConcurrencyController} to react to job arrivals,
 *       preemptions, deadlines and locks;</li>
 *   <li>holds a {@link DynamicVoltageRegulator} and forwards DVFS events
 *       (arrivals, first execute, periodic checks, completion, deadline miss);</li>
 *   <li>owns the global ready queue for Global/Hybrid scheduling;</li>
 *   <li>tracks simulation time and provides helper execution loops
 *       ({@link #globalExecute(long)} and {@link #execute(long)}).</li>
 * </ul>
 *
 * <p>Inline comments (including Chinese notes) were integrated where relevant:
 * grouping of cores into sets, priority recalculation for dynamic priority,
 * migration and waiting behavior, and the per-tick execution steps (choose,
 * check costs, ready, DVFS checks, run, block accounting, completion, cost
 * cleanup, and end-of-time actions).</p>
 *
 * @author ShiuJia
 */
public class Processor
{
    private String modelName;
    private Simulator parentSimlator;
    
    //--VVV
    private Vector<CoreSet> coreSets;//分群後之群組
    //---^^^
    private Vector<Core> allCore;
    
    private PriorityDrivenSchedulingAlgorithm schedulingAlgorithm = null;
    private Scheduler globalScheduler;
    private ConcurrencyController controller;
    private DynamicVoltageRegulator regulator;
    private PartitionDistributor distributor;
    private JobQueue globalReadyQueue;
    private TaskSet taskSet;
    private SharedResourceSet sharedResourceSet;
    private long systemTime;
    
    /**
     * Constructs a processor with empty core/core-set collections, initialized
     * global scheduler, concurrency controller, DVFS regulator and partition
     * distributor all wired back to this processor. The global ready queue is
     * empty and system time starts at 0.
     */
    public Processor()
    {
        this.coreSets = new Vector<>();
        this.allCore = new Vector<Core>();
        this.globalScheduler = new Scheduler();
        this.globalScheduler.setParentProcessor(this);
        this.controller = new ConcurrencyController();
        this.controller.setParentProcessor(this);
        this.regulator = new DynamicVoltageRegulator();
        this.regulator.setParentProcessor(this);
        this.distributor = new PartitionDistributor();
        this.distributor.setParentProcessor(this);
        this.globalReadyQueue = new JobQueue();
        this.systemTime = 0;
    }
    
    /*Operating*/
    /**
     * Adds a core to this processor and assigns it a 1-based ID.
     *
     * @param c the core to add
     */
    public void addCore(Core c)
    {
        this.allCore.add(c);
        c.setID(this.allCore.size());
    }
    
    /**
     * Adds a core set (group) to this processor and assigns it a 1-based group ID.
     *
     * @param coreSet the core set to add
     */
    public void addCoreSet(CoreSet coreSet)
    {
        this.coreSets.add(coreSet);
        coreSet.setGroupID(this.coreSets.size());
    }
    
    /**
     * Loads the task set handled by this processor (replaces any previous set).
     *
     * @param ts the task set to use
     */
    public void loadTaskSet(TaskSet ts)
    {
        this.taskSet = ts;
    }
    
    /**
     * Loads the shared resource set used by tasks (replaces any previous set).
     *
     * @param rs the shared resource set
     */
    public void loadResourceSet(SharedResourceSet rs)
    {
        this.sharedResourceSet = rs;
    }
    
    /**
     * Partitions tasks to cores when using SingleCore or Partition scheduling.
     *
     * <p>Invokes the configured {@link PartitionDistributor} and then logs the
     * mapping Task(ID) → Core(ID). This method is a no-op under Global/Hybrid
     * scheduling types.</p>
     */
    public void partitionTasks()
    {
        
        if(schedulingAlgorithm.getSchedulingType() == SchedulingType.Partition || schedulingAlgorithm.getSchedulingType() == SchedulingType.SingleCore)
        {
            println("PartitionTasks = " + this.distributor.getSPartitionAlgorithm().getName());
            
            this.distributor.split();
            
            for(Task t : this.taskSet)
            {
                println("Task(" + t.getID() + ") to Core(" + t.getLocalCore().getID() + ")");
            }
        }
    }
    
    /**
     * Executes a global-style scheduling step for a time slice, pushing jobs
     * from the global ready queue to eligible cores, preparing them to run and
     * advancing their execution.
     *
     * <p>High-level sequence (integrated from inline comments):</p>
     * <ol>
     *   <li>Check for job arrivals and update global priorities if dynamic.</li>
     *   <li>While possible, dispatch from global queue to the core whose head
     *       job is lowest priority or idle.</li>
     *   <li>Ask each core to choose the next job, then ready cores to run.</li>
     *   <li>Update core-set speeds and run each core for the given slice.</li>
     *   <li>Increment system time, then check completions and deadlines.</li>
     * </ol>
     * Some controller/DVFS/cost checks are commented in the source and can be
     * toggled as needed.
     *
     * @param t the time slice to run (simulation ticks)
     */
    public void globalExecute(long t)
    {
        this.checkArrivalSystemJob();
        this.schedulerCalculatePriorityForDynamic();//使用到globalScheduler才有用到
        
        Core c = null;
       
        while((c = this.getLowerPriorityCore()) != null)
        {
            //若globalReadyQueue無Job則跳出迴圈，若有Job則判斷c.JobToCore是否成功(true)，失敗(false)則跳出迴圈
            
            if(this.globalReadyQueue.peek() != null &&
                (c.getLocalReadyQueue().isEmpty() ||
                this.globalReadyQueue.peek().getCurrentProiority().isHigher(c.getLocalReadyQueue().peek().getCurrentProiority())))
            {
                c.JobToCore(this.globalReadyQueue.poll());
            }
            else
            {
                break;
            }
        }   
        
        
        
        for(Core gc : this.allCore)
        {   
            gc.chooseExecuteJob();
        }
        
//        for(Core gc : this.allCore)//
//        {
//            gc.checkCost();
//        }
        
        for(Core gc : this.allCore)
        {
            gc.readyRun();
        }
        //------------
//        for(Core gc : this.allCore)//
//        {
//            gc.checkCost();
//        }
        //DVSAction
//        this.regulator.checkCoresExecute();
        
        //set CoreSet Speed
        for(CoreSet coreSet : this.coreSets )
        {
            coreSet.setCurrentSpeed();
        }
        
        //執行前＾＾＾＾
        for(Core gc : this.allCore)
        {

            gc.run(t);
        }
        this.systemTime += t;
        //執行後VVV
        
//        for(Core gc : this.allCore)
//        {
//            gc.getLocalReadyQueue().setBlockingTime(c.getWorkingJob());
//        }
        
        
        
        for(Core gc : this.allCore)
        {
            gc.checkJobisCompleted();
        }
        
        this.checkMissDeadlineJob();
        
//        for(Core gc : this.allCore)
//        {
//            gc.lastCheckCost();
//        }

//        this.regulator.checkEndSystemTimeAction(this.systemTime);
        
    }
    
    /**
     * Schedules CPU execution for one time unit (or the provided slice) across
     * all cores with full controller/DVFS/cost handling.
     *
     * <p>Sequence (from inline notes by the original author): choose jobs,
     * check cost queues, ready cores, check costs again, perform DVFS checks,
     * set core-set speeds, run for t, update system time, account blocking in
     * local queues, mark completions, check deadline misses, perform final cost
     * cleanup, and notify DVFS of end-of-time.</p>
     *
     * @param t the time slice to run (simulation ticks)
     */
    public void execute(long t)
    {
        this.checkArrivalSystemJob();
        
        for(Core c : this.allCore)
        {   
            c.chooseExecuteJob();
        }
        
        for(Core c : this.allCore)//
        {
            c.checkCost();
        }
        
        for(Core c : this.allCore)
        {
            c.readyRun();
        }
        
        for(Core c : this.allCore)//
        {
            c.checkCost();
        }
        
        //DVSAction
        this.regulator.checkCoresExecute();
        
        //set CoreSet Speed
        for(CoreSet coreSet : this.coreSets )
        {
            coreSet.setCurrentSpeed();
        }
        
        //執行前＾＾＾＾
        for(Core c : this.allCore)
        {
            c.run(t);
        }
        this.systemTime += t;
        //執行後VVVV
        
        for(Core c : this.allCore)
        {
            c.setbeBlockedTimeOfJobByLocalQueue();
        }
        
        for(Core c : this.allCore)
        {
            c.checkJobisCompleted();
        }
        
        this.checkMissDeadlineJob();
        
        for(Core c : this.allCore)
        {
            c.lastCheckCost();
        }
        
        this.regulator.checkEndSystemTimeAction(this.systemTime);
        
    }
    
    private void checkArrivalSystemJob()
    {
        for(Task t : this.taskSet)
        {
            if(this.systemTime >= t.getEnterTime())
            {
                //需注意到達時間及週期算法
                if(((this.systemTime - t.getEnterTime()) % t.getPeriod()) == 0 )
                {
                    Job j = t.produceJob(this.systemTime);
                    j.setLocalProcessor(this);

                    switch(schedulingAlgorithm.getSchedulingType())
                    {
                        case SingleCore:
                            t.getLocalCore().JobToCore(j);
                        break;

                        case Partition:
                            t.getLocalCore().JobToCore(j);
                        break;

                        case Global:
                            this.globalReadyQueue.add(j);
                            this.schedulerCalculatePriorityForDynamic();
                        break;

                        case Hybrid://尚未驗證2017/10/27
                            //待加入
                        break;

                        default:      
                    }
                    
//                    //防止SRP到達繼承問題
//                    this.schedulerCalculatePriorityForDynamic();//使用到globalScheduler才有用到

                    //ControllerAction
                    this.controller.checkJobArrives(j);
                    
                    //DVSAction
                    this.regulator.checkJobArrivesProcessor(j, this);
                }
            }
        }
    }
    
    private void checkMissDeadlineJob()//
    {
        Job tempJob;
        JobQueue tempQueue = new JobQueue();
        /*Processor GlobalQueue*/
        
        
        while((tempJob = this.globalReadyQueue.poll()) != null)
        {
            if(tempJob.getAbsoluteDeadline() <= this.systemTime)
            {
                tempJob.setStatus(JobStatus.MISSDEADLINE, this.systemTime);//JOB的狀態改為MissDeadline
                
                //CortrollerAction
                this.controller.checkJobDeadline(tempJob);
                
                //DVSAction
                this.regulator.checkJobMissDeadline(tempJob);
                
                println("XXXXXXXXXXXXXXXX " + this.systemTime + " : MissDeadline : (" + tempJob.getParentTask().getID() + "," + tempJob.getID() + ")= " + tempJob.getReleaseTime());
                
                MissDeadlineInfo md = new MissDeadlineInfo((int)this.systemTime, tempJob);
                if(tempJob.getCurrentCore() != null && tempJob.getCurrentCore().getLocalReadyQueue().contains(tempJob))//確保在migration期間也能確實remove已MissDeadline的Job
                {
                    tempJob.getCurrentCore().getLocalReadyQueue().remove(tempJob);
                }
                this.parentSimlator.addMissDeadlineInfo(md);
            }
            else
            {
                tempQueue.add(tempJob);
            }
        }
        this.globalReadyQueue = tempQueue;
        
        /*Core LocalQueue*/
        for(Core c : this.allCore)
        {
            tempQueue = new JobQueue();
            while((tempJob = c.getLocalReadyQueue().poll()) != null)
            {
                if(tempJob.getAbsoluteDeadline() <= this.systemTime)
                {
                    tempJob.setStatus(JobStatus.MISSDEADLINE, this.systemTime);//JOB的狀態改為MissDeadline
                
                    //CortrollerAction
                    this.controller.checkJobDeadline(tempJob);

                    //DVSAction
                    this.regulator.checkJobMissDeadline(tempJob);

                    println("XXXXXXXXXXXXXXXX " + this.systemTime + " : MissDeadline : (" + tempJob.getParentTask().getID() + "," + tempJob.getID() + ")= " + tempJob.getReleaseTime());
                
                    MissDeadlineInfo md = new MissDeadlineInfo((int)this.systemTime, tempJob);
                    
                    if(tempJob.getCurrentCore().getLocalReadyQueue().contains(tempJob))//確保在migration期間也能確實remove已MissDeadline的Job
                    {
                        tempJob.getCurrentCore().getLocalReadyQueue().remove(tempJob);
                    }
                    this.parentSimlator.addMissDeadlineInfo(md);
                }
                else
                {
                    tempQueue.add(tempJob);
                }
            }
            c.setLocalReadyQueue(tempQueue);
        }
    }
    
    /**
     * Calculates fixed priorities for tasks according to the configured
     * scheduling type and installed algorithms.
     *
     * <p>For SingleCore and Partition, delegates to each core's local
     * scheduler; for Global, uses the processor-level scheduler. Hybrid is
     * currently marked as TODO in the source.</p>
     */
    public void schedulerCalculatePriorityForFixed()
    {
        switch(schedulingAlgorithm.getSchedulingType())
        {
            case SingleCore:
                if(this.allCore.firstElement().getLocalScheduler().getSchedAlgorithm().getPriorityType() == PriorityType.Fixed)
                {
                    this.allCore.firstElement().schedulerCalculatePriorityForFixed();
                }
            break;
                
            case Partition:
                for(Core c : this.allCore)
                {
                    if(c.getLocalScheduler().getSchedAlgorithm() != null)
                    {
                        if(c.getLocalScheduler().getSchedAlgorithm().getPriorityType() == PriorityType.Fixed)
                        {
                            c.schedulerCalculatePriorityForFixed();
                        }
                    }
                }
            break;
                
            case Global:
                if(schedulingAlgorithm.getPriorityType() == PriorityType.Fixed)
                {
                    this.globalScheduler.calculatePriority(this.taskSet);
                }
            break;
                
            case Hybrid://尚未驗證2017/10/27
                //待加入
            break;
                
            default:      
        }
    }
    
    private void schedulerCalculatePriorityForDynamic()
    {
        if(this.globalScheduler.getSchedAlgorithm().getPriorityType() == PriorityType.Dynamic)
        {
            this.globalReadyQueue = this.globalScheduler.calculatePriority(this.globalReadyQueue);
        }
    }
    
    /**
     * Adds an externally arriving job into the global ready queue.
     *
     * @param j the arriving job
     */
    public void JobArrives(Job j)
    {
        this.globalReadyQueue.add(j);
    }
    
    /**
     * Prints processor information: CoreSet grouping, per-core IDs, and power
     * model parameters (alpha, beta, gamma) along with available speeds and
     * corresponding power consumption per CoreSet.
     */
    public void showInfo()
    {
        for(CoreSet cSet : this.coreSets)
        {
            println("CoreSet(" + cSet.getGroupID() + "):");
            
            for(Core c : cSet)
            {
                println("    Core :" + c.getID());
            }
            
            println("        Alpha :" + cSet.getAlphaValue());
            println("        Beta :" + cSet.getBetaValue());
            println("        Gamma :" + cSet.getGammaValue());
            
            for(CoreSpeed cSpeed : cSet.getCoreSpeedSet())
            {
                println("        CoreSpeed :" + cSpeed.getSpeed());
                println("        PowerConsumption :" + cSpeed.getPowerConsumption()); 
            }
        }
        println();
    }
    
    /*SetValue*/
    /**
     * Sets the processor model name (as read from processor.xml).
     *
     * @param s the model name
     */
    public void setModelName(String s)
    {
        this.modelName = s;
    }
    
    /**
     * Sets the parent simulator reference used for reporting (e.g., deadline
     * misses) and global configuration.
     *
     * @param s the parent simulator
     */
    public void setParentSimulator(Simulator s)
    {
        this.parentSimlator = s;
    }
    
    /**
     * Installs the priority-driven scheduling algorithm and wires it to either
     * the global scheduler or per-core schedulers depending on the selected
     * scheduling type. Logs the configured schedulers.
     *
     * @param a the scheduling algorithm to use
     */
    public void setSchedAlgorithm(PriorityDrivenSchedulingAlgorithm a)
    {
        schedulingAlgorithm = a;
        
        
        switch(schedulingAlgorithm.getSchedulingType())
        {
            case SingleCore:
                SingleCoreSchedulingAlgorithm sa = (SingleCoreSchedulingAlgorithm) a;
                this.allCore.firstElement().setLocalSchedAlgorithm(a);
                println("Core(" + this.allCore.firstElement().getID() + ") Scheduler=" + this.allCore.firstElement().getLocalScheduler().getSchedAlgorithm().getName());
            break;
                
            case Partition:
                PartitionedSchedulingAlgorithm psa = (PartitionedSchedulingAlgorithm) a;
                psa.setCoresLocalSchedulingAlgorithm(this.allCore);

                for(Core c : this.allCore)
                {
                    println("Core(" + c.getID() + ") Scheduler=" + c.getLocalScheduler().getSchedAlgorithm().getName());
                }
            break;
                
            case Global:
                this.globalScheduler.setSchedAlgorithm(a);
                println("Processor SchedAlgorithm=" + this.globalScheduler.getSchedAlgorithm().getName());
            break;
                
            case Hybrid://尚未驗證2017/10/27
                HybridSchedulingAlgorithm hsa = (HybridSchedulingAlgorithm) a;
                hsa.setProcessorGlobalSchedulingAlgorithm(this);
                hsa.setCoresLocalSchedulingAlgorithm(this.allCore);
            break;
                
            default:      
        }
    }
    
    /**
     * Installs the concurrency-control protocol used by the controller and
     * logs its name.
     *
     * @param p the concurrency control protocol
     */
    public void setCCProtocol(ConcurrencyControlProtocol p)
    {
        this.controller.setConcurrencyControlProtocol(p);
        
        println("Processor ControlProtocol=" + this.controller.getConcurrencyControlProtocol().getName());
    }
    
    /**
     * Installs the DVFS method on the regulator and logs its name.
     *
     * @param m the dynamic voltage and frequency scaling method to use
     */
    public void setDVFSMethod(DynamicVoltageAndFrequencyScalingMethod m)
    {
        this.regulator.setDynamicVoltageAndFrequencyScalingMethod(m);
        
        println("Processor DVFSMethod=" + this.regulator.getDynamicVoltageAndFrequencyScalingMethod().getName());
    }
    
    /**
     * Installs the partition algorithm used to distribute tasks to cores.
     *
     * @param a the partition algorithm implementation
     */
    public void setPartitionAlgorithm(PartitionAlgorithm a)
    {
        this.distributor.setPartitionAlgorithm(a);
    }
    
    /*GetValue*/
    /**
     * Returns the processor's model name.
     *
     * @return the model name
     */
    public String getModelName()
    {
        return this.modelName;
    }
    
    /**
     * Returns the parent simulator reference.
     *
     * @return the parent simulator
     */
    public Simulator getParentSimulator()
    {
        return this.parentSimlator;
    }
    
    /**
     * Returns the DVFS regulator associated with this processor.
     *
     * @return the dynamic voltage regulator
     */
    public DynamicVoltageRegulator getDynamicVoltageRegulator()
    {
        return this.regulator;
    }
    
    /**
     * Returns the global scheduler used under Global/Hybrid modes.
     *
     * @return the global scheduler
     */
    public Scheduler getGlobalScheduler()
    {
        return this.globalScheduler;
    }
    
    /**
     * Returns the concurrency controller configured for this processor.
     *
     * @return the controller
     */
    public ConcurrencyController getController()
    {
        return this.controller;
    }
    
    /**
     * Returns the configured partition distributor facade.
     *
     * @return the partition distributor
     */
    public PartitionDistributor getPartitionDistributor()
    {
        return this.distributor;
    }
    
    /**
     * Returns a core by zero-based index in the internal list.
     *
     * @param x the index of the core
     * @return the core at the given index
     */
    public Core getCore(int x)
    {
        return this.allCore.get(x);
    }
    
    /**
     * Returns the list of all cores managed by this processor.
     *
     * @return a vector of cores
     */
    public Vector<Core> getAllCore()
    {
        return this.allCore;
    }
    
    /**
     * Returns a core set (group) by index.
     *
     * @param i the index within the core sets vector
     * @return the core set at the given index
     */
    public CoreSet getCoresSet(int i)
    {
        return this.coreSets.get(i);
    }
    
    /**
     * Returns all core sets (groups).
     *
     * @return a vector of core sets
     */
    public Vector<CoreSet> getCoresSets()
    {
        return this.coreSets;
    }
    
    /**
     * Returns the global ready queue used for dispatching under global modes.
     *
     * @return the global ready queue
     */
    public JobQueue getGlobalReadyQueue()
    {
        return this.globalReadyQueue;
    }
    
    /**
     * Returns the loaded task set.
     *
     * @return the task set
     */
    public TaskSet getTaskSet()
    {
        return this.taskSet;
    }
    
    /**
     * Returns the loaded shared resource set.
     *
     * @return the shared resource set
     */
    public SharedResourceSet getSharedResourceSet()
    {
        return this.sharedResourceSet;
    }
    
    /**
     * Computes total power consumption by summing values from all cores.
     *
     * @return total power consumption across cores
     */
    public double getTotalPowerConsumption()
    {
        double TPC = 0;
        for(Core c : this.allCore)
        {
            TPC += c.getPowerConsumption();
        }
        return TPC;
    }
    
    /**
     * Returns the scheduling algorithm currently installed.
     *
     * @return the priority-driven scheduling algorithm
     */
    public PriorityDrivenSchedulingAlgorithm getSchedulingAlgorithm()
    {
        return this.schedulingAlgorithm;
    }
    
    /**
     * Selects the core with the lowest-priority head job that can be preempted,
     * or an idle core if one exists. Used to decide where to place the next job
     * from the global ready queue in global scheduling.
     *
     * @return a target core for dispatch, or {@code null} if none is suitable
     */
    public Core getLowerPriorityCore()
    {
        Core core = null;
        
        for(Core c : this.allCore)
        {
            Job workingJob = c.getLocalReadyQueue().peek();
            
            if(workingJob == null)
            {
                core = c;
                break;
            }
            else if((workingJob != null && c.isPreemption) && (core == null || !workingJob.getCurrentProiority().isHigher(core.getLocalReadyQueue().peek().getCurrentProiority())))
            {  //若c內有Job而且可被搶先，則近一步判斷後面的條件 core == null || workingJob.getCurrentProiority().isHigher(core.getLocalReadyQueue().peek().getCurrentProiority())
                core = c;
            }
        }
        return core;
    }   
}
