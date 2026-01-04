/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package workloadgenerator;

import java.util.Vector;
import static RTSimulator.RTSimulator.*;

/**
 * Generator representation of a workload Task used by the workload generator.
 * <p>
 * This class models the parameters and generated elements for a single task
 * in the synthetic workload: arrival time, period, relative deadline,
 * computation amount (and a scaled computation amount used for critical
 * section placement), the set of generated critical sections, and associated
 * resource usage. The class contains helpers to produce jobs' critical
 * sections and to export timing values scaled by generator accuracy.
 * </p>
 * <p>
 * The original inline debugging printouts and comments have been integrated
 * into the method Javadocs where they explain behaviour (for example, how
 * critical section maxima/minima are computed and how nested critical
 * sections are handled when choosing start/end boundaries).
 * </p>
 *
 * @author YC
 */
public class wgTask 
{
    /**
     * Back-reference to the owning wgTaskSet.
     * <p>
     * This public field is used by generator routines to navigate from the
     * task to its containing task set and further to the global workload
     * configuration when computing scaled export values and selecting
     * resources.
     * </p>
     */
    public wgTaskSet parent;
    
    private final String taskHeader = "task";
    private final String IDHeader = "ID";
    private int ID = 0;
    private final String enterTimeHeader = "arrivalTime";
    private long arrivalTime = 0;
    private final String periodHeader = "period";
    private long period = 0;
    private final String relativeDeadlineHeader = "relativeDeadline";
    private long relativeDeadline = 0;
    private final String computationAmountHeader = "computationAmount";
    private long computationAmount = 0;
    private long computationAmountForCriticalSection = 0;
    
    private wgCriticalSectionSet criticalSectionSet = new wgCriticalSectionSet(this);
    private double utilization = 0;
    
    /**
     * Create a wgTask associated with the provided wgTaskSet.
     *
     * @param p the owning wgTaskSet used to access global generator settings
     */
    public wgTask(wgTaskSet p)
    {
        this.parent = p;
    }
    
    /**
     * Generate critical sections for this task according to configured
     * generator parameters.
     * <p>
     * Behaviour summary (integrates inline comments):
     * - Computes min/max critical section times by scaling
     *   {@code computationAmountForCriticalSection} with generator CSR ratios.
     * - If the maximum CS time is zero, no critical sections are generated.
     * - Randomly selects a number of accessed resources, then repeatedly
     *   creates {@link wgCriticalSection} instances, assigns a resource, and
     *   chooses start/end times while ensuring nesting and collision rules
     *   (the code handles removing non-nested sections from candidate sets,
     *   and adjusts chosen end times to avoid overlap/conflict with existing
     *   sections). The method then calls
     *   {@link wgCriticalSectionSet#zoomInCriticalSection()} to expand CS
     *   boundaries until total CS time meets configured minima.
     * </p>
     * <p>
     * This method emits debug printouts (original inline println calls) to
     * trace chosen start/end values and the final CS set; callers expect
     * those side-effect prints for visibility during generation.
     * </p>
     */
    public void creatCriticalSection()
    {
        println("Task ID = " + this.ID);
        println("    ComputationAmount = "+this.computationAmount);
        println("    ComputationAmountForCriticalSection = "+this.computationAmountForCriticalSection);
        
        
        this.criticalSectionSet.setMaxCriticalSectionTime((long) wgMath.mul(this.computationAmountForCriticalSection, this.parent.parent.getMaxCriticalSectionRatio()));
        this.criticalSectionSet.setMinCriticalSectionTime((long) wgMath.mul(this.computationAmountForCriticalSection, this.parent.parent.getMinCriticalSectionRatio()));
        
        println("    MaxCriticalSectionTime = "+ this.criticalSectionSet.getMaxCriticalSectionTime());
        
        if(this.criticalSectionSet.getMaxCriticalSectionTime() == 0)
        {
            return;
        }
        
        int accessedResourcesNumber = this.parent.parent.getRandomAccessedResourceNum();
        println("    AccessedResourcesNumber = " + accessedResourcesNumber);
        
        if(accessedResourcesNumber == 0)return;
        
        
        for(int i = 0 ; i < accessedResourcesNumber ; i++)
        {
            println("");
            wgCriticalSection cs = new wgCriticalSection(this.criticalSectionSet);
            cs.setResources(cs.parent.getUnusedResources());
            println("    CS(" + (i+1) +"):R("+cs.getResources().getID()+")");
            
            long maximunEndTime = 0;
            
            do
            {
                
                cs.setStartTime(wgMath.rangeRandom(0,this.computationAmountForCriticalSection-1));
                println("V        StartTime = " + cs.getStartTime());

                maximunEndTime = this.getCriticalSectionMaximunEndTime(cs.getStartTime());
                println("^        FinalMaximunEndTime = " + maximunEndTime);
                
            }while(cs.getStartTime() == maximunEndTime || maximunEndTime == 0);
            
            
            long endTime = wgMath.rangeRandom(cs.getStartTime()+1,maximunEndTime);
    println("        Original EndTime = "+endTime);
            
            
            /*csSet為與cs同階層之CriticalSectionSet*/
            Vector<wgCriticalSection> csSet = this.criticalSectionSet.getCriticalSectionSetOn
                           (this.criticalSectionSet.getCriticalSectionUnder(cs.getStartTime(),"S"));
    
    print("      Original cs同階層之csSet:");
    for(wgCriticalSection csss :csSet)
    {
        print("cs:R("+csss.getResources().getID()+"), ");
    }
    println();
    
            
            /*在csSet中移除非巢狀的CriticalSection*/
    
    println("          在csSet中移除非巢狀的cs:");
    print("          Remove: ");
            for(int j = 0 ; j < csSet.size() ;j++)
            {
                if(csSet.get(j).getEndTime() <= cs.getStartTime() || maximunEndTime <= csSet.get(j).getStartTime())
                {
    print("cs:R("+csSet.get(j).getResources().getID()+"), ");
                    csSet.remove(csSet.get(j));
                    j--;
                }
            }
    println("");
    
    print("      cs同階層之csSet:");
    for(wgCriticalSection csss :csSet)
    {
        print("cs:R("+csss.getResources().getID()+"), ");
    }
    println();
    
            
    //
    //    if(this.criticalSectionSet.getCriticalSectionUnder(cs.getStartTime(),"S")!=null)
    //    {
    //        println("Start csID = "+this.criticalSectionSet.getCriticalSectionUnder(cs.getStartTime(),"S").getResources().getID());
    //        println("             "+ this.criticalSectionSet.getCriticalSectionUnder(cs.getStartTime(),"S").getStartTime()+ " , "+ this.criticalSectionSet.getCriticalSectionUnder(cs.getStartTime(),"S").getEndTime());
    //    }
    //    else
    //    {
    //        println("Start csID = null");
    //    }   
    //    if(this.criticalSectionSet.getCriticalSectionUnder(endTime,"E")!=null)
    //    {
    //        println("End csID = "+this.criticalSectionSet.getCriticalSectionUnder(endTime,"E").getResources().getID());
    //        println("           "+ this.criticalSectionSet.getCriticalSectionUnder(endTime,"E").getStartTime()+ " , "+ this.criticalSectionSet.getCriticalSectionUnder(endTime,"E").getEndTime());
    //
    //    }
    //    else
    //    {
    //        println("End csID = null");
    //    }
    
            if(this.criticalSectionSet.getCriticalSectionUnder(cs.getStartTime(),"S") == this.criticalSectionSet.getCriticalSectionUnder(endTime,"E"))
            {
                cs.setEndTime(endTime);
    println("0, S"+cs.getStartTime()+" , E"+cs.getEndTime()+", ME"+maximunEndTime+" , "+ this.criticalSectionSet.getMaxCriticalSectionTime());
            
            }
            else
            {
                println("0.5, S"+ cs.getStartTime()+" , E"+endTime+" , ME"+maximunEndTime+" , "+ this.criticalSectionSet.getMaxCriticalSectionTime());
                
                wgCriticalSection cs2 = this.criticalSectionSet.getCriticalSectionUnder(endTime,"E",csSet);
                
                
                if(cs2 == null)
                {
                    println("cs2 = null");
                }
                
                if((cs2.getEndTime() - endTime) <= (endTime - cs2.getStartTime()))
                {
                    if(cs2 == csSet.lastElement())
                    {
                        cs.setEndTime(wgMath.rangeRandom(cs2.getEndTime(),maximunEndTime));
                        println("1, "+cs.getStartTime()+" , "+cs.getEndTime());
                    }
                    else
                    {
                        cs.setEndTime(wgMath.rangeRandom(cs2.getEndTime(),csSet.get(csSet.indexOf(cs2)+1).getStartTime()));
                        println("2, "+cs.getStartTime()+" , "+cs.getEndTime());
                    }
                }
                else
                {
                    if(cs2 == csSet.firstElement())
                    {
                        cs.setEndTime(wgMath.rangeRandom(cs.getStartTime()+1,cs2.getStartTime()));
                        println("3, "+cs.getStartTime()+" , "+cs.getEndTime());
                    }
                    else
                    {
                        cs.setEndTime(wgMath.rangeRandom(csSet.get(csSet.indexOf(cs2)-1).getEndTime(),cs2.getStartTime()));
                        println("4,"+cs.getEndTime());
                    }
                }
            }
            
            this.criticalSectionSet.addCriticalSection(cs);
        }
        
        this.criticalSectionSet.zoomInCriticalSection();
        
        
        println("TaskID = "+ this.getID());
        println("ComputationAmount = "+ this.computationAmount);
        
        println("TotalCriticalSectionTime = " + this.criticalSectionSet.getTotalCriticalSectionTime());
        
        println("CSR = " + ((double)this.criticalSectionSet.getTotalCriticalSectionTime())/((double)this.computationAmountForCriticalSection));
        println("ALLcs = ");
        for(wgCriticalSection csss: this.criticalSectionSet)
        {
            println("cs:R("+csss.getResources().getID()+"), "+csss.getStartTime()+", "+csss.getEndTime()+", "+csss.getCriticalSectionTime());
        }
        println("--");
        println("");
    }
    
    
    /**
     * 取得 新CriticalSection的最大EndTime
    */
    private long getCriticalSectionMaximunEndTime(long csStartTime)
    {
        long maximunEndTime = 0;
        
        wgCriticalSection cs2 = this.criticalSectionSet.getCriticalSectionUnder(csStartTime,"S");
        
        if(cs2 != null)
        {
            maximunEndTime = cs2.getEndTime();
            println("        OriginalMaximunEndTime = " + maximunEndTime+" , cs: R("+cs2.getResources().getID()+")");
        }
        else//進到這裡代表csStartTime在最底層
        {
            Vector<wgCriticalSection> csSet = this.criticalSectionSet.getCriticalSectionSetOn(null);
            maximunEndTime = csStartTime + this.criticalSectionSet.getMaxCriticalSectionTime();
            
            if(maximunEndTime == csStartTime)//若相等，直接跳出此函式，並重新產生start time
            {
          println("");
          println("--reCreat--");
                return maximunEndTime;
            }
            println("        OriginalMaximunEndTime = " + maximunEndTime+" , cs: null");
    print("          csSet :");
    for(wgCriticalSection csss :csSet)
    {
        print("cs: R("+csss.getResources().getID()+"),");
    }
    println();
            println("        第一次調整:");
            
            for(int i = 0 ; i < csSet.size() ;i++)
            {
                if(csSet.get(i).getEndTime() <= csStartTime)
                {
                    maximunEndTime-=csSet.get(i).getCriticalSectionTime();
                    println("            cs:R("+csSet.get(i).getResources().getID()+") ,"+csSet.get(i).getCriticalSectionTime());
                    csSet.remove(csSet.get(i));
                    i--;
    print("          csSet :");
    for(wgCriticalSection csss :csSet)
    {
        print("cs:R("+csss.getResources().getID()+"), ");
    }
    println();
                }
            }
            println("        1MaximunEndTime = " + maximunEndTime);
            
            
            println("        第二次調整:");
            if(this.computationAmountForCriticalSection < maximunEndTime)
            {
                maximunEndTime = this.computationAmountForCriticalSection;
            }
            println("        2MaximunEndTime = " + maximunEndTime);
            

            
            println("        第三次調整:");
            do
            {
                for(int i = 0 ; i < csSet.size() ;i++)
                {
                    if(csSet.get(i).getEndTime() > maximunEndTime)
                    {
                        maximunEndTime-=csSet.get(i).getCriticalSectionTime();
                        println("            cs:R("+csSet.get(i).getResources().getID()+") ,"+csSet.get(i).getCriticalSectionTime());
                        csSet.remove(csSet.get(i));
                        i--;
    print("          csSet :");
    for(wgCriticalSection csss :csSet)
    {
        print("cs:R("+csss.getResources().getID()+"), ");
    }
    println();
    
                        
                    }
                }
                
                if(csSet.isEmpty())break;
            }
            while(!(csSet.lastElement().getEndTime() <= maximunEndTime));
            println("        3MaximunEndTime = " + maximunEndTime);
        }
        
        return maximunEndTime;
    }
    
    
/*setValue*/
    /**
     * Set the numeric identifier for this task.
     * Normally assigned by {@link wgTaskSet#addTask(wgTask)} using the set size.
     *
     * @param id the new task ID
     */
    public void setID(int id)
    {
        this.ID = id;
    }
    
    /**
     * Set the arrival (enter) time for this task (internal units before export scaling).
     *
     * @param arrivalTime the arrival time value
     */
    public void setArrivalTime(long arrivalTime)
    {
        this.arrivalTime = arrivalTime;
    }
    
    /**
     * Set the task period (internal units).
     *
     * @param Period the period value
     */
    public void setPeriod(long Period)
    {
        this.period = Period;
    }
    
    /**
     * Set the relative deadline (internal units), typically equal to the period.
     *
     * @param RelativeDeadline the relative deadline value
     */
    public void setRelativeDeadline(long RelativeDeadline)
    {
        this.relativeDeadline = RelativeDeadline;
    }
    
    /**
     * Set the computation amount (WCET) in internal units and derive the scaled
     * value used specifically for critical section placement according to
     * generator accuracy ratios.
     *
     * @param ComputationAmount the WCET for the task
     */
    public void setComputationAmount(long ComputationAmount)
    {
        this.computationAmount = ComputationAmount;
        
        double zoomIn = wgMath.div(this.parent.parent.parent.criticalSectionAccuracy,this.parent.parent.parent.taskAccuracy);
        
        this.computationAmountForCriticalSection = (long)wgMath.mul(ComputationAmount,zoomIn);
    }
    
    /**
     * Compute and set the utilization value for this task (computation/period).
     */
    public void setUtilization()
    {
        this.utilization = wgMath.div(this.computationAmount, this.period);
    }

/*getValue*/    
    /**
     * Return the task identifier.
     *
     * @return the task ID
     */
    public int getID()
    {
        return this.ID;
    }
    
    /**
     * Return the arrival (enter) time for this task.
     *
     * @return arrival time
     */
    public long getEnterTime()
    {
        return this.arrivalTime;
    }
    
    /**
     * Return the task period.
     *
     * @return period
     */
    public long getPeriod()
    {
        return this.period;
    }
    
    /**
     * Return the relative deadline for the task.
     *
     * @return relative deadline
     */
    public long getRelativeDeadline()
    {
        return this.relativeDeadline;
    }
    
    /**
     * Return the configured computation amount (WCET) for this task.
     *
     * @return computation amount
     */
    public long getComputationAmount()
    {
        return this.computationAmount;
    }
    
    /**
     * Return the scaled computation amount used when placing critical sections.
     *
     * @return computation amount scaled for critical sections
     */
    public long getComputationAmountForCriticalSection()
    {
        return this.computationAmountForCriticalSection;
    }
    
    /**
     * Export the arrival time scaled by generator task accuracy.
     *
     * @return scaled arrival time as double
     */
    public double exporeEnterTime()
    {
        return wgMath.div(this.arrivalTime , this.parent.parent.parent.taskAccuracy);
    }
    
    /**
     * Export the period scaled by generator task accuracy.
     *
     * @return scaled period as double
     */
    public double exporePeriod()
    {
        return wgMath.div(this.period , this.parent.parent.parent.taskAccuracy);
    }
    
    /**
     * Export the relative deadline scaled by generator task accuracy.
     *
     * @return scaled relative deadline as double
     */
    public double exporeRelativeDeadline()
    {
        return wgMath.div(this.relativeDeadline , this.parent.parent.parent.taskAccuracy);
    }
    
    /**
     * Export the computation amount scaled by generator task accuracy.
     *
     * @return scaled computation amount as double
     */
    public double exporeComputationAmount()
    {
        return wgMath.div(this.computationAmount, this.parent.parent.parent.taskAccuracy);
    }
    
    /**
     * Header string for the task block used during export.
     *
     * @return task header key
     */
    public String getTaskHeader()
    {
        return this.taskHeader;
    }
    
    /**
     * Header string for the task ID used during export.
     *
     * @return ID header key
     */
    public String getIDHeader()
    {
        return this.IDHeader;
    }
    
    /**
     * Header string for arrival time used during export.
     *
     * @return arrival time header key
     */
    public String getEnterTimeHeader()
    {
        return this.enterTimeHeader;
    }
    
    /**
     * Header string for period used during export.
     *
     * @return period header key
     */
    public String getPeriodHeader()
    {
        return this.periodHeader;
    }
    
    /**
     * Header string for relative deadline used during export.
     *
     * @return relative deadline header key
     */
    public String getRelativeDeadlineHeader()
    {
        return this.relativeDeadlineHeader;
    }
    
    /**
     * Header string for computation amount used during export.
     *
     * @return computation amount header key
     */
    public String getComputationAmountHeader()
    {
        return this.computationAmountHeader;
    }
    
    /**
     * Return the computed utilization (computationAmount / period) for this task.
     *
     * @return utilization fraction
     */
    public double getUtilization()
    {
        return this.utilization;
    }
    
    /**
     * Return the ratio of total critical section time to the computation amount
     * used for critical section placement.
     *
     * @return critical section ratio (CSR)
     */
    public double getCriticalSectionRatio()
    {
        return wgMath.div(this.criticalSectionSet.getTotalCriticalSectionTime(),this.computationAmountForCriticalSection);
    }
    
    /**
     * Return the set of generated critical sections for this task.
     *
     * @return the wgCriticalSectionSet instance
     */
    public wgCriticalSectionSet getCriticalSectionSet()
    {
        return this.criticalSectionSet;
    }
}
