package WorkLoad;

/**
 * <strong>Priority</strong> class represents a priority value for scheduling tasks.
 * The priority value is stored as a long integer and can be used to compare priorities
 * of different tasks or resources in scheduling algorithms.
 * <p>
 * The internal stored value is kept as a signed long; callers should use
 * {@link #getValue()} when comparing priorities since this method returns the
 * externally visible priority (positive higher == higher priority). The
 * class implements {@link Comparable} so instances can be sorted by priority.
 * </p>
 *
 * @author ShiuJia
 */
public class Priority implements Comparable<Priority> {

    private long value;

    /**
     * Create a Priority initialized to zero.
     */
    public Priority() {
        this.value = 0;
    }

    /**
     * Create a Priority with the specified raw stored value.
     * <p>
     * Note: the class stores the provided number directly; callers should
     * consider that {@link #getValue()} returns the negated value to expose
     * a consistent external ordering where larger return values mean higher
     * priority.
     * </p>
     *
     * @param v the raw value to store
     */
    public Priority(long v) {
        this.value = v;
    }

    /**
     * Set the raw stored value for this Priority.
     *
     * @param v the raw value to store
     */
    public void setValue(long v) {
        this.value = v;
    }

    /**
     * Return the external priority value used for comparisons.
     * <p>
     * The implementation negates the stored internal value so that higher
     * returned numbers represent higher scheduling priority.
     * </p>
     *
     * @return the externally visible priority value (higher means higher priority)
     */
    public long getValue() {
        return -(this.value);
    }

    /**
     * Check whether this priority is higher than another priority.
     *
     * @param p the other Priority to compare
     * @return {@code true} if this has higher scheduling precedence than {@code p}
     */
    public boolean isHigher(Priority p) {
        return this.getValue() > p.getValue();
    }

    /**
     * Compare this Priority to another and return an int suitable for ordering.
     *
     * @param p the other Priority to compare
     * @return negative if this&lt;p, zero if equal, positive if this&gt;p according to external priority
     */
    public int compare(Priority p) {
        return Long.compare(this.getValue(), p.getValue());  // 確保比較的是正確的值
    }

    /**
     * Compare this Priority to another for {@link Comparable}.
     *
     * @param p the other Priority to compare
     * @return negative if this&lt;p, zero if equal, positive if this&gt;p
     */
    @Override
    public int compareTo(Priority p) {
        return Long.compare(this.getValue(), p.getValue());  // 遵循 Comparable 規則
    }
}