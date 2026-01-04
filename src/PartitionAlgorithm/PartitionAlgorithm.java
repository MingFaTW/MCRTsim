/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package PartitionAlgorithm;

import SystemEnvironment.Core;
import SystemEnvironment.Processor;
import WorkLoadSet.TaskSet;
import java.util.Vector;

/**
 * Abstract base class for partition algorithms used to allocate tasks to cores.
 * <p>
 * This class provides a common abstraction for different partitioning strategies
 * that assign tasks from a {@link TaskSet} to available {@link Core} objects
 * (possibly represented by {@link Processor} containers). Implementations of
 * this abstract class should implement {@link #taskToCore(Vector, TaskSet)} to
 * perform the actual partitioning and assignment logic.
 * </p>
 * 
 * @author ShiuJia
 */
public abstract class PartitionAlgorithm
{
    private String name;
    /**
     * Default constructor. Initializes the algorithm name to {@code null}.
     */
    public PartitionAlgorithm()
    {
        this.name = null;
    }

    /**
     * Sets the name of the partition algorithm.
     * <p>
     * Use this to give a human-readable identifier to the algorithm
     * (for logging, reporting or UI purposes).
     * </p>
     * 
     * @param n the name to be set for the algorithm
     */
    public void setName(String n)
    {
        this.name = n;
    }
    
    /**
     * Retrieves the name of the partition algorithm.
     * <p>
     * Returns the name previously set via {@link #setName(String)}, or
     * {@code null} if no name has been set.
     * </p>
     * 
     * @return the name of the algorithm, or {@code null} if not set
     */
    public String getName()
    {
        return this.name;
    }
    
    /**
     * Abstract method for assigning tasks to cores.
     * <p>
     * Implementations should partition tasks from the provided {@code taskSet}
     * among the provided {@code cores}. The partitioning strategy (best-fit,
     * first-fit, worst-fit, etc.) is determined by concrete subclasses. The
     * method is expected to modify the {@code cores} and/or tasks as needed to
     * reflect the assignment.
     * </p>
     * 
     * @param cores a {@code Vector} of {@link Core} objects representing the cores to which tasks can be assigned
     * @param taskSet the {@link TaskSet} containing tasks to be partitioned among the cores
     */
    public abstract void taskToCore(Vector<Core> cores, TaskSet taskSet);
}
