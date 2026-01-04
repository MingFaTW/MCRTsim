package WorkLoad;

/**
 * Represents a qualitative criticality level used in the workload model.
 * <p>
 * The higher the numeric value of {@code criticalityLevel}, the higher the
 * criticality (integrated from the original inline comment: "When the value
 * of criticalityLevel is higher, which means it has more higher criticality level.").
 * This lightweight value object is typically attached to tasks or jobs to
 * distinguish importance or assurance level in mixed-criticality analyses.
 * </p>
 */
public class CriticalityLevel 
{
	/*
	 * When the value of criticalityLevel is higher, which means it has more higher criticality level.
	 */
	private int criticalityLevel; 
	
	/**
	 * Create a CriticalityLevel with default level 0 (lowest / base criticality).
	 */
	public CriticalityLevel() { 
		this.criticalityLevel = 0;
	}
	
	/**
	 * Create a CriticalityLevel with the specified numeric level.
	 *
	 * @param criticalityLevel the integer criticality value (higher means more critical)
	 */
	public CriticalityLevel(int criticalityLevel) {
		setCriticalityLevel(criticalityLevel);
	}
	
	void setCriticalityLevel(int CriticalityLevel) {
		this.criticalityLevel = CriticalityLevel;
	}
	int getCriticalityLevel() {
		return this.criticalityLevel;
	}
	
	/**
	 * Return a human-readable string describing this criticality level.
	 *
	 * @return a string in the form "Criticality Level: {value}" representing the current level
	 */
	public String toString() {
		return "Criticality Level: " + this.criticalityLevel;
	}
}