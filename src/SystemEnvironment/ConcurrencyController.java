/**
 * 此類別是用以處理「同步控制方法」的「同步控制器」
 * 由於排程時可以使用不同的同步控制方法，此處只是同步法的框架
 * 負責處理同步方法在排程前、排程時與排程後所要進行的工作
 * 其中有名為protocol的ConcurrencyControlProtocol類別的物件，就是指向真正要執行的同步方法
 * 
 */
package SystemEnvironment;

import WorkLoad.CriticalSection;
import WorkLoad.Job;
import WorkLoad.SharedResource;
import concurrencyControlProtocol.ConcurrencyControlProtocol;
import RTSimulator.Definition;
import RTSimulator.Definition.JobStatus;

/**
 * Manages concurrency control operations for a processor.
 * <p>
 * This class serves as a framework that delegates to an underlying
 * {@link ConcurrencyControlProtocol} implementation to handle resource
 * synchronization during scheduling.
 * 
 * @author ShiuJia
 */
public class ConcurrencyController
{
	/** 
	 * 真正要執行的同步控制方法
	 * 預設為null
	 */
    private ConcurrencyControlProtocol protocol;
    
    /**
     * parent指標
     * 此處設計上由Processor做為其parent，還可以思考修改為其它類別
     */
    private Processor parentProcessor;
    
    /**
     * Constructs a new ConcurrencyController instance.
     */
    public ConcurrencyController()
    {
        this.protocol = null;
        this.parentProcessor = null;
    }
    
    /**
     * 此處為同步方法在排程啟動前，所要進行的工作
     * 目前沒有特別寫什麼，但有設定parent指標
     * 未來可以考慮加上 protocol.preAction()的呼叫
     *      */
    /*Operating*/
    public void preAction()
    {
        this.protocol.preAction(this.parentProcessor);
        
        //println("Controll:PreAction");
    }

    /**
     * 此處為在進行排程時，定義當job到達系統時，protocol所要進行的工作
     * 若一個同步控制方法在job到達系統時，有必須完成的工作，就可以寫在
     * protocol的jobArrivesAction()方法裡
     * @param j the job that arrives
     */
    public void checkJobArrives(Job j)
    {
        //println("Controll:JobArrives");
        this.protocol.jobArrivesAction(j);
    }
    
    /**
     * 待釐清
     * 此處為在進行排程時，定義某個job「被搶先」或「搶先其它job」時，protocol所要進行的工作
     * what is nextJob?
     * @param preemptedJob the job being preempted
     * @param nextJob the job doing the preemption
     */
    public  void jobPreemptedAction(Job preemptedJob, Job nextJob)//需要釐清BUG情形
    {
        this.protocol.jobPreemptedAction(preemptedJob, nextJob);
    }
    
    /**
     * 待釐清
     * 此處為在進行排程時，第一次執行「job」前，protocol所要進行的工作
     * @param j the job to check
     * @return true if the job can execute
     */

    public boolean checkFirstExecuteAction(Job j)
    {
        if(j.getProgressAmount() == 0)
        {
            return this.protocol.checkJobFirstExecuteAction(j);
        }
        return true;
    }
    
    /**
     * Checks if a job can lock a resource.
     * @param j the job
     * @return true if the job can continue, false if blocked
     */
    public boolean checkJobLock(Job j)
    {
        //println("Controll:JobLock");
        
        while(j.getNotEnteredCriticalSectionSet().peek() != null && (j.getNotEnteredCriticalSectionSet().peek().getRelativeStartTime() <= Math.floor(j.getProgressAmount())))
        {
            CriticalSection cs = j.getNotEnteredCriticalSectionSet().peek();

            SharedResource blockingRes = this.protocol.checkJobLockAction(j, cs.getUseSharedResource());

            if(blockingRes != null)
            {   
                this.checkBlockedAction(j, blockingRes);
                return false;
            }
            j.getCurrentCore().isChangeLock=true;

            //DVSAction
            this.parentProcessor.getDynamicVoltageRegulator().checkJobLock(j, cs.getUseSharedResource());
        }
        return true;
    }
    
    /**
     * Executes the first-time execute action for a job.
     * @param j the job
     */
    public void JobFirstExecuteAction(Job j)
    {
        this.protocol.jobFirstExecuteAction(j);
    }
    
    /**
     * Checks if a job can unlock resources.
     * @param j the job
     */
    public void checkJobUnlock(Job j)
    {
        //println("Controll:JobUnlock");
        while(!j.getEnteredCriticalSectionSet().empty() && (j.getEnteredCriticalSectionSet().peek().getRelativeEndTime() <= Math.floor(j.getProgressAmount())))
        {
            CriticalSection cs = j.getEnteredCriticalSectionSet().peek();
            j.getCurrentCore().isChangeLock=true;
            //DVSAction
            this.parentProcessor.getDynamicVoltageRegulator().checkJobUnlock(j, cs.getUseSharedResource());
            this.protocol.jobUnlockAction(j, cs.getUseSharedResource());
        }
    }
    
    /**
     * Handles job completion.
     * @param j the completed job
     */
    public void jobCompletedAction(Job j)
    {
        //println("Controll:JobComplete");
        while(j.getEnteredCriticalSectionSet().size() != 0)
        {
            CriticalSection cs = j.getEnteredCriticalSectionSet().peek();
            this.protocol.jobUnlockAction(j, cs.getUseSharedResource());
        }
        
        this.protocol.jobCompletedAction(j);
    }
    
    /**
     * Checks if a job has missed its deadline.
     * @param j the job
     */
    public void checkJobDeadline(Job j)
    {
        //println("Controll:JobDeadline");
        while(j.getEnteredCriticalSectionSet().size() != 0)
        {
            CriticalSection cs = j.getEnteredCriticalSectionSet().peek();
            this.protocol.jobUnlockAction(j, cs.getUseSharedResource());
        }
        
        this.protocol.jobMissDeadlineAction(j);
    }
    
    /**
     * Handles job blocking.
     * @param blockedJob the job that is blocked
     * @param blockingRes the resource causing the block
     * @return true if blocked successfully
     */
    public boolean checkBlockedAction(Job blockedJob, SharedResource blockingRes)
    {
        blockedJob.setBlockingResource(blockingRes);
        
        //CCAction
        this.protocol.jobBlockedAction(blockedJob, blockingRes);
        
        //DVSAction
        this.parentProcessor.getDynamicVoltageRegulator().checkBlockAction(blockedJob, blockingRes);
        
        return true;
    }
    
    /*SetValue*/
    /**
     * Sets the concurrency control protocol.
     * @param p the concurrency control protocol
     */
    public void setConcurrencyControlProtocol(ConcurrencyControlProtocol p)
    {
        p.setParentController(this);
        this.protocol = p;
    }
    
    /**
     * Sets the parent processor.
     * @param p the parent processor
     */
    public void setParentProcessor(Processor p)
    {
        this.parentProcessor = p;
    }
    
    /*GetValue*/
    /**
     * Gets the concurrency control protocol.
     * @return the concurrency control protocol
     */
    public ConcurrencyControlProtocol getConcurrencyControlProtocol()
    {
        return this.protocol;
    }
    
    /**
     * Gets the parent processor.
     * @return the parent processor
     */
    public Processor getParentProcessor()
    {
        return this.parentProcessor;
    }
}
