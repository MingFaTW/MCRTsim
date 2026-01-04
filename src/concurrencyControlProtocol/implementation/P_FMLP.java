package concurrencyControlProtocol.implementation;


import SystemEnvironment.Processor;
import WorkLoad.Job;
import WorkLoad.Nest;
import WorkLoad.Priority;
import WorkLoad.SharedResource;
import java.util.Vector;
import RTSimulator.Definition;
import static RTSimulator.Definition.Ohm;
import static RTSimulator.RTSimulator.*;

public class P_FMLP extends MSRP
{    
    /**FMLP*/
    /**
     * Inner class representing a resource with its properties.
     */
    class Resource
    {
        /** Whether this is a short resource */
        boolean isShort = true;
        /** The shared resource */
        SharedResource R = null;
        /** Parent resource group */
        ResourceGroup parentRG = null;
        /** Whether this resource is in a group */
        boolean isgroup = false;
        /**
         * Constructs a new Resource.
         * @param r the shared resource
         */
        public Resource(SharedResource r)
        {
            this.R = r;
        }
        
        /**
         * Checks if this resource is global.
         * @return true if the resource is global
         */
        public boolean isGlobal()
        {
            if(R!=null)
            {
                return   R.isGlobal();
            }
            else
            {
                println("R="+null);
                return false;
            }
        }
    }
    
    /**
     * Inner class representing a group of resources.
     */
    private class ResourceGroup extends Vector<Resource>
    {  
        /** FIFO queue of jobs waiting for resources in this group */
        Vector<Job> FIFOJobQueue = new Vector<Job>();
        /** Queue of suspended jobs */
        Vector<Job> suspensionQueue = new Vector<Job>();
        
        /**
         * Adds a resource to this group.
         * @param r the resource to add
         */
        public void addResource(Resource r)
        {
            this.add(r);
            r.parentRG = this;
        }
        
        /**
         * Adds a job to the FIFO job queue.
         * @param j the job to add
         */
        public void addJobForFIFOJobQueue(Job j)
        {
            if(!this.FIFOJobQueue.contains(j))
            {
                this.FIFOJobQueue.add(j);
            }
        }
        
        /**
         * Removes a job from the FIFO job queue.
         * @param j the job to remove
         */
        public void removeJobForFIFOJobQueue(Job j)
        {
            this.FIFOJobQueue.remove(j);
        }

        /**
         * Gets the first job from the FIFO job queue.
         * @return the first job, or null if queue is empty
         */
        public Job getFirstJobForFIFOJobQueue()
        {
            if(this.FIFOJobQueue.isEmpty())
            {
                return null;
            }
            return this.FIFOJobQueue.get(0);
        }
        
        /**
         * Adds a job to the suspension queue.
         * @param j the job to add
         */
        public void addJobForSuspensionQueue(Job j)
        {   
            j.setSuspended(true);
            this.suspensionQueue.add(j);
        }
        
        /**
         * Releases a job from the suspension queue.
         * @param job the job to release
         */
        public void releaseJobForSuspensionQueue(Job job)
        { 
            if(job != null)
            {
                job.setSuspended(false);
                this.suspensionQueue.remove(job);
            }
        } 

        /**
         * Gets a job from the suspension queue at the specified index.
         * @param i the index
         * @return the job at the specified index
         */
        public Job getJobForSuspensionQueue(int i)
        {
            return this.suspensionQueue.get(i);
        }
        
        /**
         * Checks if this resource group contains short resources.
         * @return true if the resources are short
         */
        public boolean isShort()
        {  
            return this.get(0).isShort;
        }
    }
    
    /**
     * Inner class representing a nest with FMLP-specific properties.
     */
    class FMLP_Nest
    {
        /** The nest */
        Nest nest = null;
        /** Whether this nest is in a group */
        boolean isgroup = false;
        
        /**
         * Constructs a new FMLP_Nest.
         * @param n the nest
         */
        public FMLP_Nest(Nest n)
        {
            this.nest = n;
        }
    }
    
    /**
     * Searches for the resource group containing the specified resource.
     * @param s the shared resource to search for
     * @return the resource group, or null if not found
     */
    public ResourceGroup searchResourceGroup(SharedResource s)
    {
        if(s.equals(allR.get(s.getID()-1).R))
        {
           return  allR.get(s.getID()-1).parentRG;
        }
        else 
        {
           return null;
        }
    }
    
    /**
     * Searches for a resource in the job's critical section array.
     * @param j the job
     * @param i the index
     * @return the shared resource
     */
    public SharedResource searchResourceForCriticalSectionArray(Job j,int i)
    {
        return j.getEnteredCriticalSectionArray().get(i).getUseSharedResource();
    }
    
    /**
     * Sets the status of resources (short or long).
     */
    public  void setResourceStatus()
    {   
        proportion=0.5;
        
        for(int i=0;i<allR.size();i++)
        {
            if(i<allR.size()*proportion)
            {
                allR.get(i).isShort=true;
                println("R"+allR.get(i).R.getID()+" = "+allR.get(i).isShort);
            }
            else
            {
                allR.get(i).isShort=false;
                println("R"+allR.get(i).R.getID()+" = "+allR.get(i).isShort);
            }
        }
    }
    
    /**
     * Modifies resource status based on task nesting patterns.
     * @param p the processor
     */
    public void modifyResource(Processor p)
    {
        boolean change=true;
        int run=0;
        
        while(change)
        {    
            change=false;
            println("run:"+ ++run);
            for(int i=0;i<p.getTaskSet().size();i++)//task
            {
                for(int j=0;j<p.getTaskSet().getTask(i).getNestSet().size();j++)//task allnests
                {
                    int temp=0;
                    print("Task "+p.getTaskSet().getTask(i).getID()+" : ");
                    for(int k=0;k<p.getTaskSet().getTask(i).getNestSet().get(j).size();k++)//task allnests nests
                    {
                        print(""+p.getTaskSet().getTask(i).getNestSet().get(j).getCriticalSection(k).getResourceID());
                        if(p.getTaskSet().getTask(i).getNestSet().get(j).getCriticalSection(k).getOutsideCriticalSection()!=null)
                        {   
                            SharedResource rOut=p.getTaskSet().getTask(i).getNestSet().get(j).getCriticalSection(k).getOutsideCriticalSection().getUseSharedResource();
                            SharedResource r=p.getTaskSet().getTask(i).getNestSet().get(j).getCriticalSection(k).getUseSharedResource();
                            if(allR.get(rOut.getID()-1).isShort&& !allR.get(r.getID()-1).isShort)
                            {
                                allR.get(rOut.getID()-1).isShort=false;
                                change=true;
                            }
                        }
                    }
                }
            }
        }
        
        println("===============");
        
        for(int i=0;i<p.getTaskSet().size();i++)//設定Task狀態
        {
            int temp=0;
            SharedResource r = p.getTaskSet().getTask(i).getCriticalSectionSet().get(0).getUseSharedResource();
            
            for(int j=0;j<p.getTaskSet().getTask(i).getCriticalSectionSet().size();j++)
            {
                SharedResource rj = p.getTaskSet().getTask(i).getCriticalSectionSet().get(j).getUseSharedResource();
                if(allR.get(r.getID()-1).isGlobal() && allR.get(r.getID()-1).isShort==allR.get(rj.getID()-1).isShort)
                {
                    temp++;
                }
            }
            
                if(temp==p.getTaskSet().getTask(i).getCriticalSectionSet().size())
                {
                    if(allR.get(r.getID()-1).isShort)
                    {
                        println("Task"+p.getTaskSet().getTask(i).getID()+" isShort");
                        status.add(allIsShort);
                    }
                    else
                    {
                        println("Task"+p.getTaskSet().getTask(i).getID()+" allIsLong");
                        status.add(allIsLong);
                    }
                }
                else
                {
                    println("Task"+p.getTaskSet().getTask(i).getID()+" complex");
                    status.add(complex);
                }
            
        }
        
        println("===============");
        
        for(int i=0;i<p.getTaskSet().size();i++)
        {
            println("Task:"+p.getTaskSet().getTask(i).getID()+" status:"+status.get(i).toString());
        }
    }
    
    /**
     * Sets resource groups based on task similarity.
     * @param p the processor
     */
    public void setResourceGroup(Processor p)
    {
        int rNum=0;
        int runtemp=0;
        println("size:"+allR.size());
        
        while(rNum<globalResourceNumber)
        {
            for(int i=0;i<allR.size();i++)
            {
                if(allR.get(i).isGlobal() && allR.get(i).parentRG!=null)
                {
                    print(" GlobalResource"+allR.get(i).R.getID());
                }
            }
            
            println("");
            
            for(int i=0;i<p.getTaskSet().size();i++)
            {
                if(runtemp==0)
                {
                    if(status.get(i)==allIsShort||status.get(i)==allIsLong)
                    {
                        for(int j=0;j<p.getTaskSet().getTask(i).getNestSet().size();j++)
                        {
                            ResourceGroup rg = new ResourceGroup();
                            if(p.getTaskSet().getTask(i).getNestSet().get(j).size()>1 && !allNests.get(i).get(j).isgroup)
                            {
                                for(int k=0;k<p.getTaskSet().getTask(i).getNestSet().get(j).size();k++)
                                {   
                                    SharedResource r =p.getTaskSet().getTask(i).getNestSet().get(j).getCriticalSection(k).getUseSharedResource();
                                    if(allR.get(r.getID()-1).isGlobal() && allR.get(r.getID()-1).parentRG==null && !allR.get(r.getID()-1).isgroup)
                                    {
                                        rg.add(allR.get(r.getID()-1));
                                        allR.get(r.getID()-1).parentRG=rg;
                                        ++rNum;
                                        print("Rnum:"+rNum +"   Task"+p.getTaskSet().getTask(i).getID()+"  R"+allR.get(r.getID()-1).R.getID());
                                        println("("+status.get(i)+")");
                                        mark(allR.get(r.getID()-1).R);
                                    }
                                }
                                if(rg.size()>0)
                                {
                                    allRG.add(rg);
                                }
                            }
                        }
                    }
                }
                else if(runtemp==1)
                {
                    for(int j=0;j<p.getTaskSet().getTask(i).getNestSet().size();j++)
                    {
                        ResourceGroup rg = new ResourceGroup();
                        for(int k=0;k<p.getTaskSet().getTask(i).getNestSet().get(j).size();k++)
                        {
                            SharedResource r =p.getTaskSet().getTask(i).getNestSet().get(j).getCriticalSection(k).getUseSharedResource();
                            if(allR.get(r.getID()-1).isGlobal() && allR.get(r.getID()-1).parentRG==null)
                            {
                                rg.add(allR.get(r.getID()-1));
                                allR.get(r.getID()-1).parentRG=rg;
                                ++rNum;
                                print("Rnum:"+rNum +"   Task"+p.getTaskSet().getTask(i).getID()+"  R"+allR.get(r.getID()-1).R.getID());
                                println("("+status.get(i)+")");
                                mark(allR.get(r.getID()-1).R);
                            }
                        }
                        if(rg.size()>0)
                        {
                            allRG.add(rg);
                        }
                    }
                }
            }
            
            runtemp=1;
        }
        
        for(int i=0;i<allRG.size();i++)
        {
            print("RG"+i+" : ");
            for(int j=0;j<allRG.get(i).size();j++)
            {
                print("  R"+allRG.get(i).get(j).R.getID());
            }
            println("");
        }
    }
    
    /**
     * Marks a resource and related nests as grouped.
     * @param r the shared resource
     */
    public void mark (SharedResource r)
    {
        if(!allR.get(r.getID()-1).isgroup && allR.get(r.getID()-1).isGlobal())
        {    
            allR.get(r.getID()-1).isgroup=true;
            for(int i=0;i<allNests.size();i++)
            {
                for(int j=0 ;j<allNests.get(i).size();j++)
                {
                    for(int k=0;k<allNests.get(i).get(j).nest.size();k++)
                    {
                        if(r.equals(allNests.get(i).get(j).nest.getCriticalSection(k).getUseSharedResource()))
                        {
                            allNests.get(i).get(j).isgroup=true;
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Sets up nests for all tasks.
     * @param p the processor
     */
    public void setNests(Processor p)
    {
        for(int i = 0;i<p.getTaskSet().size();i++)
        {
            taskNests = new Vector<FMLP_Nest>();
            for(int j =0;j<p.getTaskSet().getTask(i).getNestSet().size();j++)
            {
                println("i"+i+" j"+j+"   "+p.getTaskSet().getTask(i).getNestSet().get(j));
                FMLP_Nest n = new FMLP_Nest(p.getTaskSet().getTask(i).getNestSet().get(j));
                taskNests.add(n);
            }
            allNests.add(taskNests);
        }
    }
    
    /** All resources */
    Vector<Resource> allR = new Vector<Resource>();
    /** All resource groups */
    Vector<ResourceGroup> allRG = new Vector<ResourceGroup>();//只用於print
    /** Task status indicators */
    Vector<String> status = new Vector<String>();
    /** Task nests */
    Vector<FMLP_Nest> taskNests;
    /** All task nests */
    Vector<Vector<FMLP_Nest>> allNests = new Vector<Vector<FMLP_Nest>>();
    /** Number of global resources */
    int globalResourceNumber=0;
    /** Status constant for all short resources */
    String allIsShort="allIsShort",allIsLong="allIsLong" ,complex="complex";
    /** Proportion for resource classification */
    double proportion =0.0;
    
    /**
     * Constructs a new P_FMLP instance.
     */
    public P_FMLP()
    {
        this.setName("Flexible Multiprocessor Locking Protocol under P_EDF"); 
    }
    
    @Override
    public void preAction(Processor p)
    {
        super.preAction(p);
        
        for(int i = 0;i<p.getSharedResourceSet().size();i++)
        {
            Resource R = new Resource(p.getSharedResourceSet().get(i));
            allR.add(R);
            if(R.isGlobal())
            {
                println("R "+R.R.getID()+" is Global Resource.");
                globalResourceNumber++; 
            }
            
        }  
        setResourceStatus();//設置資源長短狀態
        setNests(p);//設置巢狀資源
        modifyResource(p);
        setResourceGroup(p);
    }

    @Override
    public boolean checkJobFirstExecuteAction(Job j) 
    {
        return super.checkJobFirstExecuteAction(j);
    }

    @Override
    public SharedResource checkJobLockAction(Job j, SharedResource r) 
    {   
        if(r.isGlobal())
        {
            ResourceGroup rg = searchResourceGroup(r);
            rg.addJobForFIFOJobQueue(j);
            if(r.getIdleResourceNum() > 0 //判斷是否還有閒置的資源
               && j.equals(rg.getFirstJobForFIFOJobQueue()))//是否在FIFOQ裡為第一個
            {
                j.lockSharedResource(r);
                j.getCurrentCore().isPreemption = false;
                return null;
            }   
            return r;
        }
        else
        {
            return super.checkJobLockAction(j,r);
        }
    }

    @Override
    public void jobBlockedAction(Job blockedJob, SharedResource blockingRes) 
    {
        if(blockingRes.isGlobal())
        {
            ResourceGroup rg = searchResourceGroup(blockingRes);
            
            if(rg.isShort())
            {//Busy Waiting
                blockedJob.getCurrentCore().isPreemption = false;
                blockedJob.getCurrentCore().setCoreStatus(Definition.CoreStatus.WAIT);
            }
            else
            {
                rg.addJobForSuspensionQueue(blockedJob);
            }
        }
        else
        {
            super.jobBlockedAction(blockedJob, blockingRes);
        }
    }

    @Override
    public void jobUnlockAction(Job j, SharedResource r) 
    {
        if(r.isGlobal())
        {
            ResourceGroup rg = searchResourceGroup(r);
            j.unLockSharedResource(r);

            println("J = "+j.getParentTask().getID());
            println("R = "+r.getID());

            //判斷需不需要解鎖群組 預設值為要解鎖
            int lockCount=0;
            for(int i = 0;i<rg.size();i++)
            {
                if(rg.get(i).R.getIdleResourceNum()==0)
                {
                    lockCount++; 
                }
            }

            if(lockCount==0)
            {
                println("lockCount==0");
                rg.removeJobForFIFOJobQueue(j);
                j.getCurrentCore().isPreemption = true;  

                for(int i=0;i<j.getEnteredCriticalSectionArray().size();i++)
                {
                    SharedResource s = searchResourceForCriticalSectionArray(j,i);
                    
                    if(searchResourceGroup(s)!=null && searchResourceGroup(s).isShort())
                    {    
                        j.getCurrentCore().isPreemption = false; 
                    }
                }
                
                if(!rg.isShort())
                {
                    rg.releaseJobForSuspensionQueue(rg.getFirstJobForFIFOJobQueue());
                }
            }   
            println("isPreemption = "+j.getCurrentCore().isPreemption);
        }
        else
        {
            super.jobUnlockAction(j, r);
        }
    }
    
    @Override
    public void jobMissDeadlineAction(Job j)
    {
        SharedResource blockingResource = j.getBlockingResource();
        
        if(blockingResource != null)
        {
            if(blockingResource.isGlobal())
            {
                ResourceGroup rg = this.searchResourceGroup(blockingResource);
                rg.FIFOJobQueue.remove(j);
                rg.suspensionQueue.remove(j);
            }
            else
            {
                super.jobMissDeadlineAction(j);
            }
        }
    }
}
