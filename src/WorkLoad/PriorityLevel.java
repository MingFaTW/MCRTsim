package WorkLoad;

/**
 * Represents a simple priority level for a task.
 * <p>
 * Lower numeric values indicate higher scheduling precedence (for example,
 * level 1 is higher priority than level 2). This lightweight wrapper stores
 * an integer level and provides comparators and accessors used by schedulers
 * and configuration code.
 * </p>
 *
 * @author mingfa
 */
public class PriorityLevel {
    
    /** The priority level indicating the priority of a task. Lower number = higher priority. */
    private int priorityLevel;
    
    /**
     * Constructs a PriorityLevel with the specified priority level number.
     *
     * @param priorityLevel the priority level number to be assigned (must be positive)
     * @throws IllegalArgumentException if {@code priorityLevel} is less than 1
     */
    public PriorityLevel(int priorityLevel) {
        if(priorityLevel < 1) {
            throw new IllegalArgumentException("The Priority is smaller than positive integer");
        }
        this.priorityLevel = priorityLevel;
    }
    
    /**
     * Get the numeric priority level.
     *
     * @return the priority level number (lower means higher priority)
     */
    public int getPriorityLevel() {
        return priorityLevel;
    }
    
    /**
     * Compare this priority level with another.
     * <p>
     * The comparison returns a negative number if this has higher priority
     * (i.e., lower numeric value), positive if lower priority, or zero if equal.
     * </p>
     *
     * @param other the other PriorityLevel to compare
     * @return negative if this has higher scheduling precedence, positive if lower, zero if equal
     */
    public int compareTo(PriorityLevel other) {
        // Note: lower numeric value = higher priority, so invert compare order
        return Integer.compare(other.priorityLevel, this.priorityLevel);
    }
    
    /**
     * Set the priority level number.
     *
     * @param priorityLevel the new priority level number to be set (must be >= 1)
     * @throws IllegalArgumentException if {@code priorityLevel} is less than 1
     */
    public void setPriorityLevel(int priorityLevel) {
        if(priorityLevel<1) {
            throw new IllegalArgumentException("The Priority is smaller than positive integer");
        }
        this.priorityLevel = priorityLevel;
    }

}