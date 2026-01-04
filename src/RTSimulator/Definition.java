/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package RTSimulator;

import WorkLoad.Priority;

/**
 * Central definitions and constants used across the RTSimulator project.
 *
 * <p>This class provides shared, public configuration values and enumerations
 * used to represent core/job statuses, DVFS modeling choices, scheduling
 * strategies, and priority types. Existing inline comments have been
 * incorporated into the field documentation (for example, the magnification
 * format).</p>
 *
 * @author ShiuJia
 */
public class Definition
{
	
    /**
     * A sentinel priority representing an effectively infinite priority.
     *
     * <p>Constructed with {@code Long.MAX_VALUE} to ensure it is higher than
     * any other normal priority values. Use this when an operation requires a
     * priority that should never be preempted.</p>
     */
    public static final Priority Ohm = new Priority(Long.MAX_VALUE);
    
    /**
     * Multiplicative magnification factor applied to timing or measurement
     * values within the simulator.
     *
     * <p>Default is {@code 1}. This value can be adjusted to scale simulated
     * durations for scenarios or output presentation.</p>
     */
    public static long magnificationFactor = 1;
    
    
    /**
     * Format string used to present magnified values.
     *
     * <p>Originally documented in the source as "//格式" (format). The default
     * pattern is {@code "##.#####"} which controls numeric formatting where
     * magnified values are displayed.</p>
     */
    public static String magnificationFormat = "##.#####";//格式
    
    /**
     * Enumeration of possible core statuses used by the simulator to
     * represent a CPU core's current condition.
     */
    public enum CoreStatus
    {
        /** Core is idle and not executing any job */
        IDLE,
        /** Core is executing a job */
        EXECUTION,
        /** Core is in an error state */
        WRONG,
        /** Core is waiting for a resource */
        WAIT,
        /** Core has stopped */
        STOP,
        /** Core is performing a context switch */
        CONTEXTSWITCH,
        /** Core is performing job migration */
        MIGRATION
    }
    
    /**
     * Enumeration of possible job statuses during execution.
     */
    public enum JobStatus
    {
        /** Job is not currently computing */
        NONCOMPUTE,
        /** Job is actively computing */
        COMPUTING,
        /** Job has completed successfully */
        COMPLETED,
        /** Job has missed its deadline */
        MISSDEADLINE
    }
    
    /**
     * Enumeration of Dynamic Voltage and Frequency Scaling (DVFS) types.
     */
    public enum DVFSType
    {
        /** Full chip DVFS - entire chip shares one voltage/frequency */
        FullChip,
        /** Per-core DVFS - each core has independent voltage/frequency */
        PerCore,
        /** Voltage-Frequency Island (VFI) DVFS */
        VFI
    }
    
    /**
     * Enumeration of scheduling algorithm types.
     */
    public enum SchedulingType
    {
        /** Single-core scheduling */
        SingleCore,
        /** Partitioned scheduling - tasks assigned to specific cores */
        Partition,
        /** Global scheduling - tasks can run on any core */
        Global,
        /** Hybrid scheduling - combination of partitioned and global */
        Hybrid
    }
    
    /**
     * Enumeration of priority assignment types.
     */
    public enum PriorityType
    {
        /** Fixed priority - priority does not change over time */
        Fixed,
        /** Dynamic priority - priority can change during execution */
        Dynamic
    }
}
