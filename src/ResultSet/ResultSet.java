/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ResultSet;

import SystemEnvironment.Core;
import java.util.Vector;

/**
 * Container for simulation result data.
 * <p>
 * This class collects per-core information and a set of missed-deadline
 * records (instances of {@link MissDeadlineInfo}) produced during a simulation
 * run. It provides basic operations to add and retrieve core information and
 * missed-deadline entries.
 * </p>
 * <p>
 * Note: some scheduling-related methods were present in the source as commented
 * out code (e.g., scheduling info helpers); this class currently focuses on
 * core info and missed-deadline records.
 * </p>
 *
 * @author ShiuJia
 */
public class ResultSet
{
    private Vector<Core> cores;
    private Vector<MissDeadlineInfo> missDeadlineInfoSet;
    
    /**
     * Creates an empty ResultSet.
     * <p>
     * Initializes internal containers for core information and missed-deadline
     * records.
     * </p>
     */
    public ResultSet()
    {
        this.cores = new Vector<Core>();
        this.missDeadlineInfoSet = new Vector<MissDeadlineInfo>();
    }
    
    //public void addSchedulingInfo(Vector<SchedulingInfo> s)
    /**
     * Adds core information to this result set.
     *
     * @param c the {@link Core} instance containing per-core result data to add;
     *          must not be {@code null}
     */
    public void addCoreInfo(Core c)
    {
        this.cores.add(c);
    }
    
    /**
     * Records a missed-deadline event in this result set.
     *
     * @param md the {@link MissDeadlineInfo} describing the missed deadline;
     *           must not be {@code null}
     */
    public void addMissDeadlineInfo(MissDeadlineInfo md)
    {
        this.missDeadlineInfoSet.add(md);
    }
    
    //public SchedulingInfo getSchedulingInfo(int n)
    /**
     * Returns the core information at the specified index.
     *
     * @param n the index of the core information to retrieve (zero-based)
     * @return the {@link Core} stored at the specified index
     * @throws IndexOutOfBoundsException if the index is out of range
     */
    public Core getCoreInfo(int n)
    {
        return this.cores.get(n);
    }
    
    /**
     * Returns the live set of recorded missed-deadline information.
     * <p>
     * The returned {@link Vector} is the internal container used by this
     * instance; callers that modify it will affect this ResultSet.
     * </p>
     *
     * @return a {@link Vector} of {@link MissDeadlineInfo} entries (possibly empty)
     */
    public Vector<MissDeadlineInfo> getMissDeadlineInfoSet()
    {
        return this.missDeadlineInfoSet;
    }
    
    /*public SchedulingInfo getLastSchedulingInfo()
    {
        return this.coreLocalSchedulingInfoSet.lastElement();
    }
    
    public int getSchedulingInfoNum()
    {
        return this.coreLocalSchedulingInfoSet.size();
    }*/
}
