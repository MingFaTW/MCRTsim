/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package WorkLoadSet;

import WorkLoadSet.CoreSet;
import WorkLoad.CoreSpeed;
import java.util.Comparator;
import java.util.Vector;

/**
 * Collection of DVFS (Dynamic Voltage and Frequency Scaling) operating points
 * for a core group. Extends {@link Vector} to maintain an ordered list of
 * {@link CoreSpeed} entries sorted by ascending speed.
 * <p>
 * The first element (index 0) represents the idle operating point (returned
 * by {@link #getIDELSpeed()}), the second (index 1) represents the minimum
 * active speed ({@link #getMinSpeed()}). The last element represents the
 * maximum speed ({@link #getMaxSpeed()}). Convenience accessors expose raw
 * speed values and frequency boundaries. Selection logic in
 * {@link #getCurrentSpeed(double)} integrates the parent core set's ideal/non-ideal
 * model flag: ideal mode clamps continuously within min/max; non-ideal mode
 * snaps upward to the next defined discrete speed (ceiling) or to max if above.
 * </p>
 *
 * @author ShiuJia
 */
public class CoreSpeedSet extends Vector<CoreSpeed>
{
    /**
     * Back-reference to the owning {@link CoreSet}; used to check whether
     * speed selection should be ideal (continuous clamp) or discretized to
     * the next available DVFS step.
     */
    public CoreSet parent;
    
    /**
     * Construct an empty CoreSpeedSet associated with the given core set.
     * The set starts unsorted and becomes sorted on first {@link #addSpeed} call.
     *
     * @param coreSet the owning core set whose ideal flag influences selection
     */
    public CoreSpeedSet(CoreSet coreSet)
    {
        super();
        this.parent = coreSet;
    }
    
    /**
     * Add a new DVFS operating point and resort the set by ascending speed.
     * Sorting is stable with respect to equal speeds (they keep insertion order).
     *
     * @param coreSpeed the CoreSpeed entry to add
     */
    public void addSpeed(CoreSpeed coreSpeed)
    {
        this.add(coreSpeed);
        this.sort
        (
            new Comparator<CoreSpeed>()
            {
                @Override
                public int compare(CoreSpeed s1, CoreSpeed s2)
                {
                    if(s1.getSpeed() < s2.getSpeed())
                    {
                        return -1;
                    }
                    else if(s1.getSpeed() > s2.getSpeed())
                    {
                        return 1;
                    }
                    return 0;
                }
            }
        );
    }
    
    /**
     * Return the idle operating point (index 0). Assumes the set contains
     * at least one element.
     *
     * @return idle CoreSpeed entry
     */
    public CoreSpeed getIDELSpeed()
    {
        return this.get(0);
    }
    
    /**
     * Return the minimum active (non-idle) operating point (index 1).
     * Caller must ensure the set has size >= 2.
     *
     * @return minimum active CoreSpeed entry
     */
    public CoreSpeed getMinSpeed()
    {
        return this.get(1);
    }
    
    /**
     * Convenience accessor for the numeric speed of the minimum active point.
     *
     * @return minimum active speed value
     */
    public double getMinFrequencyOfSpeed()
    {
        return this.get(1).getSpeed();
    }
    
    /**
     * Return the maximum defined operating point (last element).
     *
     * @return maximum CoreSpeed entry
     */
    public CoreSpeed getMaxSpeed()
    {
        return this.get(this.size()-1);
    }
    
    /**
     * Convenience accessor for the numeric speed of the maximum operating point.
     *
     * @return maximum speed value
     */
    public double getMaxFrequencyOfSpeed()
    {
        return this.get(this.size()-1).getSpeed();
    }
    
    /**
     * Select or clamp a target speed according to the parent's ideal flag.
     * <p>
     * Ideal mode: returns continuous value clamped to [minFrequency, maxFrequency].
     * Non-ideal mode: if above max returns max; otherwise returns the first
     * discrete DVFS step whose speed is >= target (ceil); if none found returns 0.
     * (Returning 0 indicates an unexpected empty set or mismatch.)
     * </p>
     *
     * @param s desired target speed (raw value)
     * @return chosen speed (continuous or discretized) within available range
     */
    public double getCurrentSpeed(double s)
    {
        if(this.parent.isIdeal)
        {
            if(s >= this.getMaxFrequencyOfSpeed())
            {
                return this.getMaxFrequencyOfSpeed();
            }
            else if(s <= this.getMinFrequencyOfSpeed())
            {
                return this.getMinFrequencyOfSpeed();
            }
            else
            {
                return s;
            }
        }
        else
        {
            if(s >= this.getMaxFrequencyOfSpeed())
            {
                return this.getMaxFrequencyOfSpeed();
            }
            else
            {
                for(CoreSpeed speed : this)
                {
                    if(speed.getSpeed() >= s)
                    {
                        return speed.getSpeed();
                    }
                }
            }
        }
        return 0;
    }
    
}