/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package WorkLoad;

import java.util.Vector;

/**
 * A simple container representing a collection of nested critical sections
 * that belong to a Task.
 * <p>
 * This class extends {@link Vector} and provides convenience methods to add
 * and retrieve critical sections. The public {@code parentTask} field links
 * the nest back to its owning Task (preserved from the original source).
 * </p>
 *
 * @author admin
 */
public class Nest extends Vector<CriticalSection>
{
    /**
     * The Task that owns this nest of critical sections.
     */
    public Task parentTask = null;

    /**
     * Create a Nest associated with the given Task.
     *
     * @param t the owning Task
     */
    public Nest(Task t)
    {
        this.parentTask = t;
    }
    
    /**
     * Add a CriticalSection to this nest.
     *
     * @param c the CriticalSection to add
     */
    public void addCriticalSection(CriticalSection c)
    {
        this.add(c);
    }

    /**
     * Return the CriticalSection at the given index.
     *
     * @param i the index of the desired CriticalSection
     * @return the indexed CriticalSection
     * @throws IndexOutOfBoundsException if {@code i} is out of range
     */
    public CriticalSection getCriticalSection(int i)
    {
        return this.get(i);
    }
}