package SystemEnvironment;

import ResultSet.SchedulingInfo;
import WorkLoad.Cost;
import WorkLoad.Job;
import WorkLoad.Task;
import WorkLoadSet.CoreSet;
import WorkLoadSet.JobQueue;
import WorkLoadSet.TaskSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Vector;
import RTSimulator.Definition.CoreStatus;
import RTSimulator.Definition.JobStatus;
import RTSimulator.Definition.PriorityType;
import RTSimulator.Definition.SchedulingType;
import static RTSimulator.Definition.magnificationFactor;
import static RTSimulator.RTSimulator.*;
import RTSimulator.RTSimulatorMath;
import schedulingAlgorithm.PriorityDrivenSchedulingAlgorithm;

/**
 * Represents a single processing core in the simulator.
 *
 * <p>This core maintains its local scheduler, task set, ready queue and a queue
 * of runtime costs (context switch and migration). It cooperates with the parent
 * {@link Processor} and {@link CoreSet} to:
 * <ul>
 *   <li>accept jobs and (for non-global scheduling) immediately re-calculate
 *       dynamic priorities to avoid SRP inheritance issues;</li>
 *   <li>choose the next job to execute considering preemption and whether a
 *       context switch or migration cost is required;</li>
 *   <li>record execution/idle/wait/context-switch/migration intervals into
 *       {@link SchedulingInfo} with power usage;</li>
 *   <li>invoke controller and DVS hooks on first execute, preemption, completion
 *       and periodic checks.</li>
 * </ul>
 *
 * <p>Inline comments about global scheduling, blocking/waiting behavior,
 * SRP-related ordering, and end-of-interval cost cleanup have been integrated.
 *
 * @author ShiuJia
 */
public class Core
{
    private int ID;
    private CoreSet parentCoreSet;
    private Processor parentProcessor;
    private Scheduler localScheduler;
    private TaskSet taskSet;
    private JobQueue localReadyQueue;
    private Queue<Cost> costQueue;
    private CoreStatus status;
    private Job workingJob;
    private double currentSpeed;
    private SchedulingInfo previousSchedulingInfo;
    private long currentTime;
    private Vector<SchedulingInfo> schedulingInfoSet;

    /**
     * Indicates whether this core allows preemption so that a newly arrived
     * higher-priority job can preempt the current job without delay.
     */
    public boolean isPreemption;

    /**
     * Flag set when a job enters or exits a critical section (lock use changes),
     * forcing an immediate scheduling record to reflect lock usage transitions.
     */
    public boolean isChangeLock;//

    /**
     * Flag set when the speed changes so that the next recording reflects
     * the new speed even if other state does not change.
     */
    public boolean isChangeSpeed;//

    private long powerConsumption;
    private int contactSwitchCount = 0;
    private int migrationCount = 0;

    /**
     * Constructs a new core with empty task/ready queues, an initialized local
     * scheduler, IDLE status, zeroed time and power counters, and preemption
     * enabled. Recording change flags are reset.
     */
    public Core()
    {
        this.parentCoreSet = null;
        this.parentProcessor = null;

        this.localScheduler = new Scheduler();
        this.localScheduler.setParentCore(this);
        this.taskSet = new TaskSet();
        this.localReadyQueue = new JobQueue();
        this.costQueue = new LinkedList<Cost>();
        this.status = CoreStatus.IDLE;
        this.workingJob = null;
        this.isPreemption = true;
        this.schedulingInfoSet = new Vector<SchedulingInfo>();
        this.currentSpeed = 0;
        this.previousSchedulingInfo = null;
        this.currentTime = 0;
        this.powerConsumption = 0;

        this.isChangeLock = false;
        this.isChangeSpeed = false;
    }

    /*Operating*/

    /**
     * Adds a task to this core and sets the task's local core reference.
     *
     * @param t the task to add to this core
     */
    public void addTask(Task t)
    {
        this.taskSet.add(t);
        t.setLocalCore(this);
    }

    /**
     * Calculates and assigns fixed priorities for all tasks in this core using
     * the local scheduler (prints results for debugging).
     */
    public void schedulerCalculatePriorityForFixed()
    {
        this.localScheduler.calculatePriority(this.taskSet);

        for(Task t : this.taskSet)
        {
            println("~Core ID:"+this.ID+", Task ID:"+t.getID()+", Priority:"+t.getPriority().getValue());
        }
    }

    private void schedulerCalculatePriorityForDynamic()
    {
        if(this.localScheduler.getSchedAlgorithm().getPriorityType() == PriorityType.Dynamic)
        {
            this.localReadyQueue = this.localScheduler.calculatePriority(this.localReadyQueue);
        }
    }

    /**
     * Accepts an arriving job into this core.
     *
     * <p>Integrates DVS checks and handles Global vs. single/partition
     * scheduling:
     * <ul>
     *   <li>Global: keeps the local ready queue consistent and sets current
     *       core;</li>
     *   <li>Non-global: initializes origin/current core and immediately
     *       recomputes dynamic priorities to avoid SRP inheritance issues.</li>
     * </ul>
     *
     * @param j the arriving job
     */
    public void JobToCore(Job j)
    {
        //DVSAction
        this.parentProcessor.getDynamicVoltageRegulator().checkJobArrivesCore(j, this);
        if(this.getParentProcessor().getSchedulingAlgorithm().getSchedulingType() == SchedulingType.Global)
        {
            if(!this.localReadyQueue.isEmpty())
            {
                this.parentProcessor.getGlobalReadyQueue().add(this.localReadyQueue.poll());
            }
            j.setCurrentCore(this);
            this.localReadyQueue.add(j);

        }
        else// If single-core or partitioned, use the logic below.
        {
            if(j.getOriginCore() == null)
            {
                j.setOriginCore(this);
            }
            j.setCurrentCore(this);

            this.localReadyQueue.add(j);
            // Avoid SRP inheritance issues: assign priority and sort immediately.
            this.schedulerCalculatePriorityForDynamic();
        }
    }

    /**
     * Chooses which job to execute next and, when needed, inserts context switch
     * costs or performs a direct switch. Handles:
     * <ul>
     *   <li>dynamic priority recalculation for non-global scheduling;</li>
     *   <li>preemption decisions (new job vs. current);</li>
     *   <li>context switch when resuming a previously COMPUTING job;</li>
     *   <li>IDLE transitions when the queue is empty.</li>
     * </ul>
     */
    public void chooseExecuteJob()
    {
        if(this.status != CoreStatus.STOP )//&& this.status != CoreStatus.CONTEXTSWITCH)
        {

            if(this.getParentProcessor().getSchedulingAlgorithm().getSchedulingType() != SchedulingType.Global)
            {
                if(this.localScheduler.getSchedAlgorithm() != null)
                {
                    if(this.localScheduler.getSchedAlgorithm().getPriorityType() == PriorityType.Dynamic)
                    {
                        this.schedulerCalculatePriorityForDynamic();
                    }
                }
            }


            if(this.isPreemption && this.costQueue.isEmpty())//If preemptive and no pending cost, evaluate working job switch.
            {
                if(this.workingJob != this.localReadyQueue.peek())//New job at head (higher priority or null) differs from current.
                {
                    if(this.localReadyQueue.peek() != null)//New job should not be COMPLETED or MISSDEADLINE.
                    {
                        if(this.localReadyQueue.peek().getStatus() == JobStatus.COMPUTING)//Resuming a COMPUTING job requires context switch.
                        {
                            this.setContextSwitchCost(this, this.localReadyQueue.peek());//Handles zero context-switch time internally.
                        }
                        // New job does not require "resume"; only consider preemption.
                        else if(this.workingJob != null)//Current job may be preempted.
                        {
                            if(this.workingJob.getStatus() == JobStatus.NONCOMPUTE
                               || this.workingJob.getStatus() == JobStatus.COMPLETED
                               || this.workingJob.getStatus() == JobStatus.MISSDEADLINE)
                            {
                                this.setWorkingJob(this.localReadyQueue.peek());
                            }
                            else if(this.workingJob.getStatus() == JobStatus.COMPUTING)//Preemption from COMPUTING requires context switch.
                            {
                                this.setContextSwitchCost(this, this.localReadyQueue.peek());//Handles zero context-switch time internally.
                            }

                            // Migration cost followed by "resume" is handled elsewhere when needed.
                        }
                        else if(this.workingJob == null) //Core is IDLE; no context switch needed.
                        {
                            this.setWorkingJob(this.localReadyQueue.peek());
                        }
                    }
                    else if(this.localReadyQueue.peek() == null)//No job available; core becomes IDLE.
                    {
                        this.setWorkingJob(this.localReadyQueue.peek());
                    }
                }
                else if(this.workingJob == this.localReadyQueue.peek())
                {       //Same job continues; only decide if a context switch is needed.
                    if(!this.schedulingInfoSet.isEmpty() && this.schedulingInfoSet.lastElement().getJob() != this.localReadyQueue.peek()
                       && this.localReadyQueue.peek().getStatus() == JobStatus.COMPUTING)//Resume COMPUTING job implies context switch.
                    {
                        this.setContextSwitchCost(this, workingJob);
                    }
                }
            }
        }
    }

    /**
     * Prepares the core to run the current working job.
     *
     * <p>Loops while the core is not ready and no runtime cost is in progress:
     * performs controller/DVS first-execute checks, handles WAIT vs. non-WAIT
     * states, and for Global scheduling may return jobs to the global queue and
     * fetch a new one. A potential infinite loop indicates missing PIP or
     * suspension usage at the time of blocking (per inline notes).
     */
    public void readyRun()
    {
        boolean isReady = false;

        while( (!isReady) && this.costQueue.isEmpty())//Skip while executing costs.
        {
            if(this.status != CoreStatus.STOP)
            {
                if(this.workingJob != null)
                {
                    //ControllerAction
                    if(this.parentProcessor.getController().checkFirstExecuteAction(this.workingJob))
                    {
                        if(this.parentProcessor.getController().checkJobLock(this.workingJob))//IF cannot be merged.
                        {
                            this.status = CoreStatus.EXECUTION;

                            if(this.workingJob.getProgressAmount() == 0)
                            {
                                this.parentProcessor.getController().JobFirstExecuteAction(workingJob);
                                this.parentProcessor.getDynamicVoltageRegulator().JobFirstExecuteAction(workingJob);
                            }

                            this.parentProcessor.getDynamicVoltageRegulator().checkJobEveryExecute(workingJob);

                            isReady = true;
                        }
                    }

                    if(!isReady)//isReady == false
                    {
                        if(this.isPreemption)
                        {
                            if(this.parentProcessor.getSchedulingAlgorithm().getSchedulingType() == SchedulingType.Global)
                            {
                                if(this.status == CoreStatus.WAIT)
                                {
                                    if(this.parentProcessor.getGlobalReadyQueue().peek() != null &&
                                       this.parentProcessor.getGlobalReadyQueue().peek().getCurrentProiority().isHigher(this.localReadyQueue.peek().getCurrentProiority()))
                                    {
                                        this.JobToCore(this.parentProcessor.getGlobalReadyQueue().poll());
                                        isReady = false;
                                    }
                                    else
                                    {
                                        isReady = true;
                                    }
                                }
                                else
                                {
                                    // Global: if a job is blocked (not waiting), return it to the global queue and get a new one.
                                    // An infinite loop here would indicate no PIP or Suspension mechanism was used when blocked.
                                    if(!this.localReadyQueue.isEmpty())
                                    {
                                        this.parentProcessor.getGlobalReadyQueue().add(this.localReadyQueue.poll());
                                    }


                                    if(this.parentProcessor.getGlobalReadyQueue().peek() != null)
                                    {
                                        this.JobToCore(this.parentProcessor.getGlobalReadyQueue().poll());
                                    }

                                    isReady = false;
                                }
                            }
                            else // Non-global
                            {
                                if(this.status == CoreStatus.WAIT && this.workingJob == this.localReadyQueue.peek())
                                {
                                    isReady = true;
                                }
                                else
                                {
                                    isReady = false;
                                }
                            }
                        }
                        else
                        {
                            isReady = true;
                        }

                        if(!isReady)
                        {
                            this.chooseExecuteJob();
                            isReady = false;
                            println("NO!!!!!NO!!!!!");
                        }

                    }
                }
                else
                {
                    this.status = CoreStatus.IDLE;
                    isReady = true;
                }
            }
            else
            {
                isReady = true;
            }
        }

        this.parentProcessor.getDynamicVoltageRegulator().checkCoreExecute(this);
    }

    /**
     * If there is a pending runtime cost in the queue, updates this core's
     * status to match the front cost (context switch or migration).
     */
    public void checkCost()
    {
        if(!this.costQueue.isEmpty())
        {
            this.status = this.costQueue.peek().getStatus();
        }
    }

    /**
     * Advances the simulation on this core by the specified time slice.
     *
     * <p>Records a scheduling point, updates current time and power, and
     * executes the appropriate action based on current status:
     * EXECUTION, IDLE, WAIT (records waiting time), CONTEXTSWITCH or MIGRATION
     * (delegates to the cost at the head of the queue).
     *
     * @param processedTime the time slice to process
     */
    public void run(long processedTime)
    {
        double t = this.currentTime + processedTime;

        while(this.currentTime < t)
        {
            if(this.status == CoreStatus.EXECUTION)
            {
                this.record();
                this.runJob(processedTime);
            }
            else if(this.status == CoreStatus.IDLE)
            {
                this.record();
                this.currentTime += processedTime;
                this.powerConsumption += this.parentCoreSet.getPowerConsumption() * processedTime;
            }
            else if(this.status == CoreStatus.WAIT)//Record the time the core spends waiting.
            {
                this.record();
                this.currentTime += processedTime;
                this.powerConsumption += this.getParentCoreSet().getPowerConsumption() * processedTime;
            }
            else if(this.status == CoreStatus.CONTEXTSWITCH)
            {
                this.record();
                this.currentTime += processedTime;
                this.powerConsumption += this.getParentCoreSet().getPowerConsumption() * processedTime;
                if(this.costQueue.peek().getStatus() == CoreStatus.CONTEXTSWITCH)
                {
                    this.costQueue.peek().execution(processedTime);
                }
            }
            else if(this.status == CoreStatus.MIGRATION)
            {
                this.record();
                this.currentTime += processedTime;
                this.powerConsumption += this.getParentCoreSet().getPowerConsumption() * processedTime;

                if(this.costQueue.peek().getStatus() == CoreStatus.MIGRATION)
                {
                    this.costQueue.peek().execution(processedTime);
                }
            }
        }

    }

    private void runJob(long processedTime)
    {
        if(((this.workingJob.getTargetAmount() - this.workingJob.getProgressAmount()) * this.workingJob.getMaxProcessingSpeed() ) >= processedTime * this.parentCoreSet.getCurrentSpeed())
        {
            this.workingJob.execute(RTSimulatorMath.mul(processedTime , RTSimulatorMath.div(this.getParentCoreSet().getCurrentSpeed() , this.workingJob.getMaxProcessingSpeed())),this.currentTime);

            this.powerConsumption += this.getParentCoreSet().getPowerConsumption() * processedTime;
            //CortrollerAction
            this.parentProcessor.getController().checkJobUnlock(workingJob);
            this.currentTime += processedTime;
        }
        else
        {
            this.workingJob.finalExecute();
            this.powerConsumption += this.getParentCoreSet().getPowerConsumption() * processedTime;

            //CortrollerAction
            this.parentProcessor.getController().checkJobUnlock(workingJob);
            this.currentTime += processedTime;
        }
        this.workingJob.setStatus(JobStatus.COMPUTING, this.currentTime);
    }

    /**
     * Checks whether the current working job has completed its target amount.
     * If completed (and not already marked), sets status to COMPLETED, triggers
     * controller/DVS actions, and removes the job from both the local and
     * current-core ready queues to handle migration scenarios.
     */
    public void checkJobisCompleted()//Check if the job is completed (201707).
    {
        if(this.workingJob != null)
        {
            if(this.workingJob.getProgressAmount() >= this.workingJob.getTargetAmount()
            && this.workingJob.getStatus() != JobStatus.COMPLETED && this.workingJob.getStatus() != JobStatus.MISSDEADLINE)//Avoid double-processing.
            {
                println("@~0workingJob ="+this.workingJob.getStatus());
                this.workingJob.setStatus(JobStatus.COMPLETED, this.currentTime);//Mark job as completed.
                println("@~1workingJob ="+this.workingJob.getStatus());
                //CortrollerAction
                this.parentProcessor.getController().jobCompletedAction(workingJob);
                //DVSAction
                this.parentProcessor.getDynamicVoltageRegulator().checkJobComplete(workingJob);
                println("0 "+ this.localReadyQueue.contains(this.workingJob));
                this.localReadyQueue.remove(this.workingJob);
                println("1 "+ this.localReadyQueue.contains(this.workingJob));
                if(this.workingJob.getCurrentCore().getLocalReadyQueue().contains(this.workingJob))//Ensure removal during migration.
                {
                    this.workingJob.getCurrentCore().getLocalReadyQueue().remove(this.workingJob);
                }
            }
        }
    }

    /**
     * At the end of the current execution interval, checks pending costs and
     * removes or re-queues them based on whether the request job has completed
     * or missed its deadline. Updates the core status accordingly.
     */
    public void lastCheckCost()//Final check: if a job completed or missed its deadline, cancel its pending costs.
    {
        if(!this.costQueue.isEmpty())
        {
            int count = this.costQueue.size();
            for(int i = 0 ; i< count;i++)
            {
                Cost cost = this.costQueue.poll();

                if(cost.getRequestJob().getStatus() != JobStatus.COMPLETED
                  && cost.getRequestJob().getStatus() != JobStatus.MISSDEADLINE)//Request job should not be NONCOMPUTE here.
                {
                    if(!cost.checkIsCompleted())//If true, Cost.checkIsCompleted will schedule the job.
                    {
                        this.costQueue.add(cost);
                    }
                }
            }

            if(this.costQueue.isEmpty())
            {
                this.status = CoreStatus.IDLE;
            }
            else
            {
                this.status = this.costQueue.peek().getStatus();
            }
        }
    }

    /**
     * Finalizes scheduling records by writing the accumulated total power
     * consumption into the last {@link SchedulingInfo}.
     */
    public void finalRecording()
    {
        this.previousSchedulingInfo.setTotalPowerConsumption(powerConsumption);
    }

    /**
     * Records a scheduling point if needed and creates a new
     * {@link SchedulingInfo} when:
     * <ul>
     *   <li>status changes (E/I/W/C/M),</li>
     *   <li>the job associated with the state changes,</li>
     *   <li>lock usage changes (isChangeLock), or</li>
     *   <li>speed changes (isChangeSpeed).</li>
     * </ul>
     *
     * <p>Only records within the simulation time window, unless a speed change
     * forces recording. Integrated inline logging is preserved.
     */
    public void record()
    {
        if((this.currentTime <= this.parentProcessor.getParentSimulator().getSimulationTime()) || this.isChangeSpeed)
        {
            if(this.isChangeSpeed)
            {
                println("this.isChangeSpeed = true");
            }

            if(this.status == CoreStatus.EXECUTION)
            {
                if(this.isChangeLock)
                {
                    println("Core(" + this.ID + ") : " + (double)this.currentTime/magnificationFactor + ": E : Job(" + this.workingJob.getParentTask().getID() + "," + this.workingJob.getID() + ") : " + this.getParentCoreSet().getCurrentSpeed());

                    if(!this.workingJob.getEnteredCriticalSectionSet().empty())
                    {
                        print("    Use Resource:");
                        for(int i = 0; i < this.workingJob.getEnteredCriticalSectionSet().size(); i++)
                        {
                            print(this.workingJob.getEnteredCriticalSectionSet().get(i).getUseSharedResource().getID() + ". ");
                        }
                        println();
                    }

                    if(this.previousSchedulingInfo != null)
                    {
                        this.previousSchedulingInfo.setEndTime(currentTime);
                        this.previousSchedulingInfo.setTotalPowerConsumption(powerConsumption);
                    }

                    this.newRecording();

                    this.isChangeLock = false;
                }
                else
                {
                    if(this.previousSchedulingInfo == null)//First record for the core.
                    {
                        println("Core(" + this.ID + ") : " + (double)this.currentTime/magnificationFactor + ": E : Job(" + this.workingJob.getParentTask().getID() + "," + this.workingJob.getID() + ") : " + this.getParentCoreSet().getCurrentSpeed());

                        if(!this.workingJob.getEnteredCriticalSectionSet().empty())
                        {
                            print("    Use Resource:");
                            for(int i = 0; i < this.workingJob.getEnteredCriticalSectionSet().size(); i++)
                            {
                                print(this.workingJob.getEnteredCriticalSectionSet().get(i).getUseSharedResource().getID() + ". ");
                            }
                            println();
                        }

                        this.newRecording();
                    }
                    else if(this.previousSchedulingInfo != null && ((this.previousSchedulingInfo.getCoreStatus() != CoreStatus.EXECUTION || (this.previousSchedulingInfo.getCoreStatus() == CoreStatus.EXECUTION && this.previousSchedulingInfo.getJob() != this.workingJob)) || this.isChangeSpeed))
                    {
                        println("Core(" + this.ID + ") : " + (double)this.currentTime/magnificationFactor + ": E : Job(" + this.workingJob.getParentTask().getID() + "," + this.workingJob.getID() + ") : " + this.getParentCoreSet().getCurrentSpeed());

                        if(!this.workingJob.getEnteredCriticalSectionSet().empty())
                        {
                            print("    Use Resource:");
                            for(int i = 0; i < this.workingJob.getEnteredCriticalSectionSet().size(); i++)
                            {
                                print(this.workingJob.getEnteredCriticalSectionSet().get(i).getUseSharedResource().getID() + ". ");
                            }
                            println();
                        }

                        this.previousSchedulingInfo.setEndTime(currentTime);
                        this.previousSchedulingInfo.setTotalPowerConsumption(powerConsumption);

                        this.newRecording();
                    }
                }
            }
            else if(this.status == CoreStatus.IDLE)
            {
                if(this.previousSchedulingInfo == null)//First record for the core.
                {
                    println("Core(" + this.ID + ") : " + (double)this.currentTime/magnificationFactor + " : I : " + this.getParentCoreSet().getCurrentSpeed());

                    this.newRecording();
                }
                else if(this.previousSchedulingInfo != null && (this.previousSchedulingInfo.getCoreStatus() != CoreStatus.IDLE || this.isChangeSpeed))
                {
                    println("Core(" + this.ID + ") : " + (double)this.currentTime/magnificationFactor + " : I : " + this.getParentCoreSet().getCurrentSpeed());

                    this.previousSchedulingInfo.setEndTime(currentTime);
                    this.previousSchedulingInfo.setTotalPowerConsumption(powerConsumption);

                    this.newRecording();
                }
            }
            else if(this.status == CoreStatus.WAIT)
            {
                if(this.previousSchedulingInfo == null)//First record for the core.
                {
                    println("Core(" + this.ID + ") : " + (double)this.currentTime/magnificationFactor + " : W : " + this.getParentCoreSet().getCurrentSpeed() );

                    this.newRecording();
                }
                else if(this.previousSchedulingInfo != null && ((this.previousSchedulingInfo.getCoreStatus() != CoreStatus.WAIT || (this.previousSchedulingInfo.getCoreStatus() == CoreStatus.WAIT && this.previousSchedulingInfo.getJob() != this.workingJob))|| this.isChangeSpeed))
                {
                    println("Core(" + this.ID + ") : " + (double)this.currentTime/magnificationFactor + " : W : Job(" + this.workingJob.getParentTask().getID() + "," + this.workingJob.getID() + ") :" + this.getParentCoreSet().getCurrentSpeed());

                    this.previousSchedulingInfo.setEndTime(currentTime);
                    this.previousSchedulingInfo.setTotalPowerConsumption(powerConsumption);
                    this.newRecording();
                }
            }
            else if(this.status == CoreStatus.CONTEXTSWITCH)
            {
                if(this.previousSchedulingInfo == null)//First record for the core.
                {
                    println("Core(" + this.ID + ") : " + (double)this.currentTime/magnificationFactor + " : C : " + this.getParentCoreSet().getCurrentSpeed() );

                    this.newRecording();
                }
                else if(this.previousSchedulingInfo != null && ((this.previousSchedulingInfo.getCoreStatus() != CoreStatus.CONTEXTSWITCH || (this.previousSchedulingInfo.getCoreStatus() == CoreStatus.CONTEXTSWITCH && this.previousSchedulingInfo.getJob() != this.costQueue.peek().getRequestJob())) || this.isChangeSpeed))
                {
                    println("Core(" + this.ID + ") : " + (double)this.currentTime/magnificationFactor + " : C : Job(" + this.costQueue.peek().getRequestJob().getParentTask().getID() + "," + this.costQueue.peek().getRequestJob().getID() + ") :" + this.getParentCoreSet().getCurrentSpeed());

                    this.previousSchedulingInfo.setEndTime(currentTime);
                    this.previousSchedulingInfo.setTotalPowerConsumption(powerConsumption);
                    this.newRecording();
                }
            }
            else if(this.status == CoreStatus.MIGRATION)
            {
                if(this.previousSchedulingInfo == null)//First record for the core.
                {
                    println("Core(" + this.ID + ") : " + (double)this.currentTime/magnificationFactor + " : M : " + this.getParentCoreSet().getCurrentSpeed() );

                    this.newRecording();
                }
                else if(this.previousSchedulingInfo != null && ((this.previousSchedulingInfo.getCoreStatus() != CoreStatus.MIGRATION || (this.previousSchedulingInfo.getCoreStatus() == CoreStatus.MIGRATION && this.previousSchedulingInfo.getJob() != this.costQueue.peek().getRequestJob()))|| this.isChangeSpeed))
                {
                    println("Core(" + this.ID + ") : " + (double)this.currentTime/magnificationFactor + " : M : Job(" + this.costQueue.peek().getRequestJob().getParentTask().getID() + "," + this.costQueue.peek().getRequestJob().getID() + ") :" + this.getParentCoreSet().getCurrentSpeed());

                    this.previousSchedulingInfo.setEndTime(currentTime);
                    this.previousSchedulingInfo.setTotalPowerConsumption(powerConsumption);
                    this.newRecording();
                }
            }
        }
    }

    private void newRecording()
    {
        SchedulingInfo newInfo = new SchedulingInfo();
        newInfo.setCore(this);
        newInfo.setCoreStatus(this.status);
        if(this.status == CoreStatus.CONTEXTSWITCH || this.status == CoreStatus.MIGRATION)
        {
            newInfo.setJob(this.costQueue.peek().getRequestJob());
        }
        else
        {
            newInfo.setJob(this.workingJob);
        }
        newInfo.setStartTime(this.currentTime);
        newInfo.setUseSpeed(this.getParentCoreSet().getCurrentSpeed(),this.getParentCoreSet().getNormalizationOfSpeed());

        this.schedulingInfoSet.add(newInfo);
        this.previousSchedulingInfo = newInfo;
        this.isChangeSpeed = false;
    }

    /*SetValue*/

    /**
     * Sets the currently working job on this core. If there is an existing
     * COMPUTING job, this indicates a preemption and triggers the controller
     * preemption action.
     *
     * @param j the job to run (or {@code null} to leave the core idle)
     */
    public void setWorkingJob(Job j)
    {
        if(j != null && this.workingJob != null && this.workingJob.getStatus() == JobStatus.COMPUTING)//Preemption case.
        {
            println("C"+this.ID+" ,j"+j.getParentTask().getID()+",CurrentProiority() = "+j.getCurrentProiority().getValue());
            println("Preemption");
            println("C"+this.ID+" ,j"+this.workingJob.getParentTask().getID()+",CurrentProiority() = "+this.workingJob.getCurrentProiority().getValue());
            this.parentProcessor.getController().jobPreemptedAction(workingJob, j);
        }

        this.workingJob = j;
    }

    /**
     * Sets the core identifier.
     *
     * @param id the core id
     */
    public void setID(int id)
    {
        this.ID = id;
    }

    /**
     * Sets the parent core set to which this core belongs.
     *
     * @param coreSet the parent {@link CoreSet}
     */
    public void setParentCoreSet(CoreSet coreSet)
    {
        this.parentCoreSet = coreSet;
    }

    /**
     * Sets the parent processor that owns this core.
     *
     * @param p the parent {@link Processor}
     */
    public void setParentProcessor(Processor p)
    {
        this.parentProcessor = p;
    }

    /**
     * Installs a local priority-driven scheduling algorithm for this core.
     *
     * @param a the scheduling algorithm to use
     */
    public void setLocalSchedAlgorithm(PriorityDrivenSchedulingAlgorithm a)
    {
        this.localScheduler = new Scheduler();
        this.localScheduler.setSchedAlgorithm(a);
    }

    /**
     * Replaces the task set used by this core.
     *
     * @param ts the new {@link TaskSet}
     */
    public void setTaskSet(TaskSet ts)
    {
        this.taskSet = ts;
    }

    /**
     * Sets the local ready queue for this core.
     *
     * @param jq the {@link JobQueue} to use as the local ready queue
     */
    public void setLocalReadyQueue(JobQueue jq)
    {
        this.localReadyQueue = jq;
    }

    /**
     * Sets the current processing speed value cached on this core.
     * Note: effective execution uses the speed from the parent {@link CoreSet}.
     *
     * @param s the speed value
     */
    public void setCurrentSpeed(double s)
    {
        this.currentSpeed = s;
    }

    /**
     * Updates the core status (IDLE, EXECUTION, WAIT, CONTEXTSWITCH, MIGRATION, STOP).
     *
     * @param c the new {@link CoreStatus}
     */
    public void setCoreStatus(CoreStatus c)
    {
        this.status = c;
    }

    /**
     * Inserts a context switch cost for the given request job if the simulator
     * configures a non-zero context switch time; otherwise directly switches
     * the working job. Also increments the context-switch counter.
     *
     * @param requestCore the core that requests the switch
     * @param requestJob the job causing the context switch
     */
    public void setContextSwitchCost(Core requestCore, Job requestJob)
    {
        this.contactSwitchCount += 1;
        if(this.parentProcessor.getParentSimulator().getContextSwitchTime() > 0)
        {
            Cost cost = new Cost(this, requestCore, requestJob, CoreStatus.CONTEXTSWITCH);
           	this.costQueue.add(cost);
        }
        else
        {
            this.setWorkingJob(requestJob);
        }
    }

    /**
     * Inserts migration (and preceding context switch) costs for a migrating job.
     *
     * <p>Behavior:
     * <ul>
     *   <li>If context-switch time &gt; 0, add a context-switch cost first;</li>
     *   <li>If migration time &gt; 0, add a migration cost;</li>
     *   <li>Link the two so migration follows the context switch;</li>
     *   <li>If both costs are zero and the request core is this core, requeue
     *       the job locally and set its current core.</li>
     * </ul>
     *
     * <p>Inline note: costs are produced only when the origin core is in a state
     * that allows starting migration (no pending costs).
     *
     * @param requestCore the core where the request originates
     * @param requestJob the job to migrate
     */
    public void setMigrationCost(Core requestCore, Job requestJob)//ex: Core1 -> Core2; costs appear on both cores only when migration can start.
    {

        Cost contextSwitchCost = null;
        Cost migrationCost = null;

        this.contactSwitchCount += 1;
        if(this.parentProcessor.getParentSimulator().getContextSwitchTime() >0)//Migration implies a context switch first.
        {
            contextSwitchCost = new Cost(this, requestCore, requestJob, CoreStatus.CONTEXTSWITCH);
            this.costQueue.add(contextSwitchCost);
        }

        this.migrationCount +=1;
        if(this.parentProcessor.getParentSimulator().getMigrationTime() > 0)
        {
            migrationCost = new Cost(this, requestCore, requestJob, CoreStatus.MIGRATION);
            this.costQueue.add(migrationCost);
        }

        if(contextSwitchCost != null)
        {
            if(migrationCost != null)
            {
                contextSwitchCost.setNextCost(migrationCost);
            }
            else
            {
                migrationCost = new Cost(this, requestCore, requestJob, CoreStatus.MIGRATION);
                migrationCost.setCostTime(0);
                contextSwitchCost.setNextCost(migrationCost);
            }
        }
        else if(contextSwitchCost == null && migrationCost == null && this == requestCore)
        {
            requestCore.getLocalReadyQueue().add(requestJob);
            requestJob.setCurrentCore(requestCore);
        }
    }

    /**
     * Accounts for blocking time of the current job in the local ready queue
     * when the core is in EXECUTION or WAIT state.
     */
    public void setbeBlockedTimeOfJobByLocalQueue()
    {
        if(this.status == CoreStatus.EXECUTION || this.status == CoreStatus.WAIT)
        {
            this.localReadyQueue.setBlockingTime(this.workingJob);
        }
    }

    /*GetValue*/

    /**
     * Returns this core's identifier.
     *
     * @return the core id
     */
    public int getID()
    {
        return this.ID;
    }

    /**
     * Returns the parent core set.
     *
     * @return the parent {@link CoreSet}
     */
    public CoreSet getParentCoreSet()
    {
        return this.parentCoreSet;
    }

    /**
     * Returns the parent processor.
     *
     * @return the parent {@link Processor}
     */
    public Processor getParentProcessor()
    {
        return this.parentProcessor;
    }

    /**
     * Returns the local scheduler instance.
     *
     * @return the {@link Scheduler} used by this core
     */
    public Scheduler getLocalScheduler()
    {
        return this.localScheduler;
    }

    /**
     * Returns the local ready queue.
     *
     * @return the local {@link JobQueue}
     */
    public JobQueue getLocalReadyQueue()
    {
        return this.localReadyQueue;
    }

    /**
     * Returns the currently working job, or {@code null} if none.
     *
     * @return the current working job or {@code null}
     */
    public Job getWorkingJob()
    {
        return this.workingJob;
    }

    /**
     * Returns the task set assigned to this core.
     *
     * @return the {@link TaskSet}
     */
    public TaskSet getTaskSet()
    {
        return this.taskSet;
    }

    /**
     * Returns the current core status.
     *
     * @return the {@link CoreStatus}
     */
    public CoreStatus getStatus()
    {
        return this.status;
    }

    /**
     * Returns the task at the specified index.
     *
     * @param i the index within the task set
     * @return the {@link Task} at the given index
     */
    public Task getTask(int i)
    {
        return this.taskSet.get(i);
    }

    /**
     * Returns the list of scheduling records produced by this core.
     *
     * @return a vector of {@link SchedulingInfo}
     */
    public Vector<SchedulingInfo> getSchedulingInfoSet()
    {
        return this.schedulingInfoSet;
    }

    /**
     * Returns the cached current speed of this core.
     * Note: execution uses the speed from {@link CoreSet}.
     *
     * @return the current speed value
     */
    public double getCurrentSpeed()
    {
        return this.currentSpeed;
    }

    /**
     * Returns the current simulated time on this core.
     *
     * @return the current time
     */
    public long getCurrentTime()
    {
        return this.currentTime;
    }

    /**
     * Returns the accumulated power consumption.
     *
     * @return the total power consumption
     */
    public long getPowerConsumption()
    {
        return this.powerConsumption;
    }

    /**
     * Returns the number of context switches counted for this core.
     *
     * @return context switch count
     */
    public int getContextSwitchCount()
    {
        return this.contactSwitchCount;
    }

    /**
     * Returns the number of migrations counted for this core.
     *
     * @return migration count
     */
    public long getMigrationCount()
    {
        return this.migrationCount;
    }

    /**
     * Returns the pending runtime cost queue (context switch/migration).
     *
     * @return the cost queue
     */
    public Queue<Cost> getCostQueue()
    {
        return this.costQueue;
    }

}
