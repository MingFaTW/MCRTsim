/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package SystemEnvironment;

import PartitionAlgorithm.PartitionAlgorithm;

/**
 * Distributes tasks across processor cores using a pluggable partition algorithm.
 *
 * <p>This lightweight facade holds a {@link PartitionAlgorithm} strategy and a
 * reference to the owning {@link Processor}. When {@link #split()} is invoked,
 * it delegates to the algorithm to assign tasks in the processor's TaskSet to
 * its available cores (i.e., using Processor#getAllCore and Processor#getTaskSet).</p>
 *
 * <p>The original inline section markers (Operating/SetValue/GetValue) are
 * reflected here: set the algorithm and the parent processor first, then
 * operate; finally, getters expose the current configuration.</p>
 *
 * @author ShiuJia
 */
public class PartitionDistributor
{
    private PartitionAlgorithm algorithm;
    private Processor parentProcessor;
    
    /**
     * Creates a distributor with no algorithm and no parent processor set.
     * Call {@link #setPartitionAlgorithm(PartitionAlgorithm)} and
     * {@link #setParentProcessor(Processor)} before {@link #split()}.
     */
    public PartitionDistributor()
    {
        this.algorithm = null;
        this.parentProcessor = null;
    }
    
    /*Operating*/
    /**
     * Executes the configured partition algorithm to assign tasks to cores.
     *
     * <p>Delegates to the underlying algorithm (typically via
     * {@code taskToCore(...)}) using the parent processor's current list of
     * cores and its TaskSet.</p>
     *
     * <p>Preconditions: both the partition algorithm and the parent processor
     * should be set prior to calling this method.</p>
     */
    public void split()
    {
        this.algorithm.taskToCore(this.parentProcessor.getAllCore(), this.parentProcessor.getTaskSet());
    }
    
    /*SetValue*/
    /**
     * Installs the partitioning strategy used to distribute tasks to cores.
     *
     * @param a the {@link PartitionAlgorithm} implementation to use
     */
    public void setPartitionAlgorithm(PartitionAlgorithm a)
    {
        this.algorithm = a;
    }
    
    /**
     * Sets the parent processor whose cores and task set are used during
     * partitioning.
     *
     * @param p the target {@link Processor}
     */
    public void setParentProcessor(Processor p)
    {
        this.parentProcessor = p;
    }
    
    /*GetValue*/
    /**
     * Returns the currently configured partition algorithm.
     *
     * @return the {@link PartitionAlgorithm}, or {@code null} if none is set
     */
    public PartitionAlgorithm getSPartitionAlgorithm()
    {
        return this.algorithm;
    }
    
    /**
     * Returns the parent processor that this distributor operates on.
     *
     * @return the {@link Processor}, or {@code null} if not set
     */
    public Processor getParentProcessor()
    {
        return this.parentProcessor;
    }
}