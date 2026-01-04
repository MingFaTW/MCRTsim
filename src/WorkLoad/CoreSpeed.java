/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package WorkLoad;

/**
 * Represents the operating speed and power consumption of a processing
 * core at a specific operating point (for example, a DVFS level).
 * <p>
 * This simple value object stores a numeric speed and the corresponding
 * power consumption. Values are initialized to -1 to indicate an unspecified
 * or uninitialized state (matching the original constructor behavior).
 * </p>
 *
 * @author ShiuJia
 */
public class CoreSpeed
{
    private double speed;
    private double powerConsumption;
    
    /**
     * Construct a CoreSpeed with uninitialized values.
     * <p>
     * Both speed and powerConsumption are set to -1 to indicate that no
     * valid operating point has been assigned yet.
     * </p>
     */
    public CoreSpeed()
    {
        this.speed = -1;
        this.powerConsumption = -1;
    }
    
    /*SetValue*/
    /**
     * Set the core operating speed.
     * <p>
     * Typical callers will provide a positive double indicating processing
     * capacity (units depend on the rest of the system, e.g. MIPS or MHz).
     * </p>
     *
     * @param f the speed to assign to this core (may be negative to indicate uninitialized)
     */
    public void setSpeed(double f)
    {
        this.speed = f;
    }
    
    /**
     * Set the core's power consumption for the current operating point.
     *
     * @param p the power consumption value to assign (units as used by the system, e.g. mW)
     */
    public void setPowerConsumption(double p)
    {
        this.powerConsumption = p;
    }
    
    /*GetValue*/
    /**
     * Get the configured core operating speed.
     *
     * @return the speed value previously set, or -1 if uninitialized
     */
    public double getSpeed()
    {
        return this.speed;
    }
    
    /**
     * Get the configured power consumption for this core's operating point.
     *
     * @return the power consumption value previously set, or -1 if uninitialized
     */
    public double getPowerConsumption()
    {
        return this.powerConsumption;
    }
}