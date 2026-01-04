/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package workloadgenerator;

import java.util.Comparator;
import java.util.Random;
import java.util.Vector;
import static RTSimulator.RTSimulator.println;

/**
 * A collection of generated critical sections for a workload task.
 * <p>
 * This class manages a list of {@link wgCriticalSection} objects, provides
 * nesting queries (find sections under/above others), supports expanding
 * critical sections to meet configured totals, and keeps track of used
 * resources. Inline Chinese comments that explain nesting and selection
 * behavior have been integrated into the method descriptions.
 * </p>
 *
 * @author YC
 */
public class wgCriticalSectionSet extends Vector<wgCriticalSection>
{
    private String criticalSectionSetHeader = "criticalSections";
    /**
     * Back-reference to the owning {@link wgTask} for which these critical
     * sections are generated. Generator code navigates through this parent to
     * access workload-level settings (e.g., CSR bounds and accuracy scaling).
     */
    public wgTask parent;
    private long maxCriticalSectionTime = 0;//最大的maxCriticalSectionTime ＝ 使用者設定之csr * Job之計算時間
    private long minCriticalSectionTime = 0;
    private long totalCriticalSectionTime = 0;//當前累計之CriticalSectionTime
//    private double csr = 0;
    private Vector<wgResources> userdResources = new Vector<>();
    
    public wgCriticalSectionSet(wgTask p)
    {
        super();
        this.parent = p;
    }
    
    /*找出time點下方一層的一個CriticalSection並回傳，回傳null代表此time在最底層*/
    /*timeisSorE 輸入 S or E 字串，讓此函式辨別輸入之time為StartTime or EndTime*/
    /**
     * Find the smallest (innermost) critical section that contains the given time.
     *
     * @param time the time to query (export-scaled units expected by callers)
     * @param timeisSorE "S" to treat time as start-time inclusion rule or "E" for end-time inclusion
     * @return an inner wgCriticalSection that contains {@code time}, or null if none
     */
    public wgCriticalSection getCriticalSectionUnder(double time, String timeisSorE)
    {
        return this.getCriticalSectionUnder(time, timeisSorE, this);
    }
    
    /*在csSet中找出time點下方一層的一個CriticalSection並回傳，回傳null代表此time不在csSet內之所有cs的範圍中*/
    /*timeisSorE 輸入 S or E 字串，讓此函式辨別輸入之time為StartTime or EndTime*/
    /**
     * Search within a provided csSet for the smallest critical section enclosing time.
     *
     * @param time the time value to test
     * @param timeisSorE "S" or "E" deciding the inclusive bounds used in the test
     * @param csSet the vector of wgCriticalSection to search
     * @return the matched wgCriticalSection or null when none matches
     */
    public wgCriticalSection getCriticalSectionUnder(double time, String timeisSorE, Vector<wgCriticalSection> csSet)
    {
        wgCriticalSection criticalSection = null;
        for(wgCriticalSection cs : csSet)
        {
            switch(timeisSorE)
            {
                case "S":
                    if(cs.getStartTime() <= time && time < cs.getEndTime())
                    {
                        if(criticalSection == null || cs.getCriticalSectionTime() < criticalSection.getCriticalSectionTime())
                        {
                            criticalSection = cs;
                        }
                    }
                break;
                case "E":
                    if(cs.getStartTime() < time && time <= cs.getEndTime())
                    {
                        if(criticalSection == null || cs.getCriticalSectionTime() < criticalSection.getCriticalSectionTime())
                        {
                            criticalSection = cs;
                        }
                    }
                break;
            }
        }
        return criticalSection;
    }
    
    /**
     * Find one critical section that is directly above the given wgCriticalSection
     * in the nesting hierarchy (i.e., the smallest parent). Returns null when the
     * provided wgCS is at the outermost level.
     *
     * @param wgCS non-null wgCriticalSection to query
     * @return a wgCriticalSection that contains wgCS, or null if none
     */
    public wgCriticalSection getCriticalSectionUnder(wgCriticalSection wgCS)
    {
        wgCriticalSection criticalSection = null;
        for(wgCriticalSection cs : this)
        {
            /*避免cs 與 wgCS同時使用時，互相選到對方 */
            if(wgCS != cs && ((cs.getStartTime() <= wgCS.getStartTime() && wgCS.getEndTime() < cs.getEndTime())
                    ||(cs.getStartTime() < wgCS.getStartTime() && wgCS.getEndTime() <= cs.getEndTime())))
            {
                if(criticalSection == null || cs.getCriticalSectionTime() < criticalSection.getCriticalSectionTime())
                {
                    criticalSection = cs;
                }
            }
        }
        return criticalSection;
    }
    
    /**
     * Return the set of critical sections that are directly above the provided
     * criticalSection (i.e., its siblings in the parent level). Passing null
     * returns the top-level critical sections.
     *
     * @param criticalSection the reference wgCriticalSection (may be null)
     * @return a Vector of wgCriticalSection in the requested level, sorted by start time
     */
    public Vector<wgCriticalSection> getCriticalSectionSetOn(wgCriticalSection criticalSection)
    {
        Vector<wgCriticalSection> csSet = new Vector<wgCriticalSection>();
        
        
        for(wgCriticalSection cs : this)
        {
            if(cs != criticalSection && criticalSection == this.getCriticalSectionUnder(cs))
            {
                csSet.add(cs);
            }
        }
        
        /*csSet依照其Start Time遞增排序*/
        csSet.sort
        (
            new Comparator<wgCriticalSection>()
            {
                @Override
                public int compare(wgCriticalSection cs1, wgCriticalSection cs2)
                {
                    if(cs1.getStartTime() < cs2.getStartTime())
                    {
                        return -1;
                    }
                    else if(cs1.getStartTime() > cs2.getStartTime())
                    {
                        return 1;
                    }
                    else if(cs2.getCriticalSectionTime() > cs1.getCriticalSectionTime())
                    {
                        return -1;
                    }
                    return 0;
                }
            }
        );
        
        /*過濾掉同時間開始與結束的criticalSection，僅保留一個*/
        for(int i=0 ; i<csSet.size() ; i++)
        {
            for(int j=i+1 ; j < csSet.size() ; j++)
            {
                if(csSet.get(i).getStartTime() == csSet.get(j).getStartTime() && csSet.get(i).getEndTime() == csSet.get(j).getEndTime())
                {
                    csSet.remove(csSet.get(j));
                    j--;
                }
                else
                {
                    break;
                }
            }
        }
        
        return csSet;
    }
    
    /**
     * Expand randomly selected critical sections (either start or end) until
     * the accumulated critical section time reaches the configured minimum.
     * This method mutates critical section boundaries and updates totals.
     */
    public void zoomInCriticalSection()
    {
        Random ran = new Random();
        Vector<wgCriticalSection> csSet = this.getCriticalSectionSetOn(null);
        
        while(this.totalCriticalSectionTime < this.minCriticalSectionTime)
        {
            long gapTime = this.maxCriticalSectionTime - this.totalCriticalSectionTime;
            
            wgCriticalSection cs = csSet.get(ran.nextInt(csSet.size()));//取得要更改的CS
            
            if(ran.nextInt(2) == 0)//改變startTime
            {
                if(cs == csSet.firstElement())
                {
                    long time = cs.getStartTime() < gapTime ? cs.getStartTime() : gapTime;
                    cs.setStartTime(cs.getStartTime()-wgMath.rangeRandom(0, time));
                
                }
                else
                {
                    wgCriticalSection previousCS = csSet.get(csSet.indexOf(cs)-1);
                    long time = cs.getStartTime()-previousCS.getEndTime() < gapTime 
                              ? cs.getStartTime()-previousCS.getEndTime() : gapTime;
                    
                    cs.setStartTime(cs.getStartTime()-wgMath.rangeRandom(0, time));
                }
            }
            else//改變EndTime
            {
                if(cs == csSet.lastElement())
                {
                    
                    long time = this.parent.getComputationAmountForCriticalSection()-cs.getEndTime() < gapTime
                              ? this.parent.getComputationAmountForCriticalSection()-cs.getEndTime() : gapTime;
                    cs.setEndTime(cs.getEndTime()+wgMath.rangeRandom(0, time));
                }
                else
                {
                    wgCriticalSection nextCS = csSet.get(csSet.indexOf(cs)+1);
                    
                    long time = nextCS.getStartTime()-cs.getEndTime() < gapTime
                              ? nextCS.getStartTime()-cs.getEndTime() : gapTime;
                    cs.setStartTime(cs.getEndTime()+wgMath.rangeRandom(0, time));
                }
            }
            
            this.setTotalCriticalSectionTime();
        }
    }
    
    /*setValue*/ 
    /**
     * Add a critical section to this set and record its resource usage.
     * Updates the cached total critical section time accordingly.
     *
     * @param cs the critical section to add
     */
    public void addCriticalSection(wgCriticalSection cs)
    {
        this.add(cs);
        this.userdResources.add(cs.getResources());
        this.setTotalCriticalSectionTime();
    }
    
    /**
     * Remove a critical section from this set and release its resource from
     * the used list. Updates the cached total critical section time.
     *
     * @param cs the critical section to remove
     */
    public void removeCriticalSection(wgCriticalSection cs)
    {
        this.remove(cs);
        this.userdResources.remove(cs.getResources());
        this.setTotalCriticalSectionTime();
    }
    
    /**
     * Set the maximum accumulated critical section time for this task.
     * Typically computed as CSR(max) * job computation time (see inline note).
     *
     * @param time maximum total CS time in internal units
     */
    public void setMaxCriticalSectionTime(long time)
    {
        this.maxCriticalSectionTime = time;
    }
    
    /**
     * Set the minimum accumulated critical section time for this task.
     *
     * @param time minimum total CS time in internal units
     */
    public void setMinCriticalSectionTime(long time)
    {
        this.minCriticalSectionTime = time;
    }

    private void setTotalCriticalSectionTime()
    {
        Vector<wgCriticalSection> csSet = this.getCriticalSectionSetOn(null);
        this.totalCriticalSectionTime = 0;
        for(wgCriticalSection cs : csSet)
        {
            this.totalCriticalSectionTime += cs.getCriticalSectionTime();
            println("!!!!!CriticalSectionTime = "+cs.getCriticalSectionTime());
        }
        
        println("!!!!!totalCriticalSectionTime = "+totalCriticalSectionTime);
    }
    /*getValue*/
    /**
     * Return the wgCriticalSection at the given index.
     *
     * @param i zero-based index
     * @return the critical section instance
     */
    public wgCriticalSection getCriticalSection(int i)
    {
        return this.get(i);
    }
    
    /**
     * Return one unused resource group from the workload, if any.
     *
     * @return a wgResources not yet used in this set; null when none available
     */
    public wgResources getUnusedResources()
    {
        wgResourcesSet rsSet = this.parent.parent.parent.getResourcesSet();
        wgResourcesSet rsSet_Unused = new wgResourcesSet();
        rsSet_Unused.addAll(rsSet);
        rsSet_Unused.removeAll(this.userdResources);
        
        if(rsSet_Unused.size() == 0)
        {
            return null;
        }
        
        return rsSet_Unused.getResources((int)wgMath.rangeRandom(0, rsSet_Unused.size()-1));
    }
    
    /**
     * Header key string used for the criticalSections element during export.
     *
     * @return the header key for the set
     */
    public String getCriticalSectionSetHeader()
    {
        return this.criticalSectionSetHeader;
    }
    
    /**
     * Return the configured maximum total CS time.
     *
     * @return maximum CS time in internal units
     */
    public long getMaxCriticalSectionTime()
    {
        return this.maxCriticalSectionTime;
    }
    
    /**
     * Return the configured minimum total CS time.
     *
     * @return minimum CS time in internal units
     */
    public long getMinCriticalSectionTime()
    {
        return this.minCriticalSectionTime;
    }
    
    /**
     * Return the current total CS time accumulated by the (top-level) set.
     *
     * @return total CS time in internal units
     */
    public long getTotalCriticalSectionTime()
    {
        return this.totalCriticalSectionTime;
    }
}