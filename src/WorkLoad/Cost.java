/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package WorkLoad;

import SystemEnvironment.Core;
import RTSimulator.Definition.CoreStatus;

/**
 * Represents a scheduling-related cost (delay) that affects a core while a
 * requested job or migration is being processed.
 * <p>
 * A Cost encapsulates the core currently affected (`curCore`), the core that
 * requested the action (`requestCore`), the Job that triggered the cost
 * (`requestJob`), the cost type (`status` - e.g. CONTEXTSWITCH or MIGRATION),
 * an optional linked {@code nextCost} (used when a context switch precedes a
 * migration), and the remaining {@code costTime} in internal time units.
 * </p>
 * <p>
 * Inline comments present in the original file are integrated here: the
 * Chinese comments explain that `curCore` is the current core, `requestCore`
 * is the core that caused the cost, and `requestJob` is the job that caused
 * the cost. The class is used by the simulator's cost queue to track and
 * apply context-switch and migration delays.
 * </p>
 *
 * @author YC
 */
public class Cost 
{
    private Core curCore = null; //當前的Core
    private Core requestCore = null; //發出請求造成Cost的Core
    private Job requestJob = null;//造成Cost的Job
    private CoreStatus status = null;//屬於哪一種Cost狀態
    private Cost nextCost = null;
    private long costTime = 0;
    
    /**
     * Create a new Cost describing a delay affecting {@code curCore} caused by
     * an action from {@code requestCore} on behalf of {@code requestJob}.
     * <p>
     * The constructor sets the cost type and initializes the remaining
     * {@code costTime} according to the current simulator settings. For
     * {@link CoreStatus#CONTEXTSWITCH} the cost time is set to the simulator's
     * context switch time; for {@link CoreStatus#MIGRATION} it is set to the
     * simulator's migration time. Other statuses leave {@code costTime} at
     * zero unless modified later.
     * </p>
     *
     * @param curCore the core that will experience the cost (current core)
     * @param requestCore the core that initiated the request causing the cost
     * @param requestJob the job that caused this cost
     * @param status the type of cost (e.g. CONTEXTSWITCH or MIGRATION)
     */
    public Cost(Core curCore, Core requestCore, Job requestJob, CoreStatus status)
    {
        this.curCore = curCore;
        this.requestCore = requestCore;
        this.requestJob = requestJob;
        this.status = status;

        switch(this.status)
        {
            case CONTEXTSWITCH:
                this.costTime = this.curCore.getParentProcessor().getParentSimulator().getContextSwitchTime();
            break;
                
            case MIGRATION:
                this.costTime = this.curCore.getParentProcessor().getParentSimulator().getMigrationTime();
            break;
                
            default:
        }  
    }
    
    /**
     * Advance the execution of this cost by the specified time amount.
     * <p>
     * This subtracts {@code time} from the remaining {@code costTime}. Callers
     * (typically the simulator's time-stepping loop) should invoke this to
     * reduce the outstanding delay; once {@code costTime} reaches zero the
     * cost is considered completed and {@link #checkIsCompleted()} can be used
     * to apply completion side-effects.
     * </p>
     *
     * @param time the amount of time to consume from this cost (in same units as costTime)
     */
    public void execution(long time)
    {
        this.costTime -= time;
    }
    
    /**
     * Link a subsequent cost to be processed after this one completes.
     * <p>
     * This method is used for the migration workflow where a context-switch
     * cost may be followed immediately by a migration cost. Linking the
     * migration Cost via {@code nextCost} ensures the simulator can
     * properly sequence and handle both delays.
     * </p>
     *
     * @param migrationCost the Cost to execute after this Cost completes
     */
    public void setNextCost(Cost migrationCost)//做給migration cost使用的函式，在發生migration cost之前必定會產生 context switch cost
    {
        this.nextCost = migrationCost;
    }
    
    /**
     * Return the core currently affected by this cost.
     *
     * @return the current Core that experiences the delay
     */
    public Core getCurrentCore()
    {
        return this.curCore;
    }
    
    /**
     * Return the core that requested the action causing this cost.
     *
     * @return the requesting Core
     */
    public Core getRequestCore()
    {
        return this.requestCore;
    }
    
    /**
     * Return the job that triggered this cost.
     *
     * @return the Job responsible for this cost
     */
    public Job getRequestJob()
    {
        return this.requestJob;
    }
    
    /**
     * Return the cost type/status.
     *
     * @return the CoreStatus categorizing this cost
     */
    public CoreStatus getStatus()
    {
        return this.status;
    }
    
    /**
     * Explicitly set the remaining cost time.
     *
     * @param time the remaining cost time to assign (in same units as the simulator uses)
     */
    public void setCostTime(long time)
    {
        this.costTime = time;
    }
    
    /**
     * Check whether this cost has completed and apply completion side-effects.
     * <p>
     * When the internal {@code costTime} reaches zero this method applies the
     * appropriate actions depending on the cost {@code status}:
     * <ul>
     *   <li>For {@link CoreStatus#CONTEXTSWITCH}: if there is no {@code nextCost}
     *       it ensures the {@code requestJob} is set as the working job on its
     *       current core (avoiding redundant set operations). If a linked
     *       {@code nextCost} exists, it will only take effect after that cost
     *       also completes (the method checks {@code nextCost.checkIsCompleted()}).</li>
     *   <li>For {@link CoreStatus#MIGRATION}: if the {@code requestCore} equals
     *       {@code curCore} the method enqueues the {@code requestJob} on the
     *       request core's local ready queue and updates the job's current core
     *       reference.</li>
     * </ul>
     * The method returns {@code true} when this Cost is finished (and can be
     * removed from the simulator's cost queue); otherwise it returns
     * {@code false}. Callers are responsible for removing completed costs from
     * the queue.
     *
     *
     * @return {@code true} if the cost is completed and completion effects were applied; {@code false} otherwise
     */
    public boolean checkIsCompleted()//只要成功完成之後，此Cost就會被剔除CostQuequ
    {
        if(this.costTime == 0)
        {
            if(this.status == CoreStatus.CONTEXTSWITCH)
            {
                if(this.nextCost == null)
                {
                    if(this.requestJob.getCurrentCore().getWorkingJob() != requestJob)//若 requestJob == this.requestJob.getCurrentCore().getWorkingJob()
                    {                                                                 //則不需要重新setWorkingJob
                        this.requestJob.getCurrentCore().setWorkingJob(requestJob);
                    }
                }
                else if(this.nextCost.checkIsCompleted())//避免migration cost == 0時忽略執行Job在 requestCore.getLocalReadyQueue的轉移
                {
                    //不需要加東西
                }
            }
            else if(this.status == CoreStatus.MIGRATION && this.requestCore == this.curCore)
            {
                this.requestCore.getLocalReadyQueue().add(requestJob);
                requestJob.setCurrentCore(this.requestCore);
            }
            return true;
        }
        return false;
    }
    
}