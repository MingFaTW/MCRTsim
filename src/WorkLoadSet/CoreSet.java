/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package WorkLoadSet;

import SystemEnvironment.*;
import WorkLoad.CoreSpeed;
import java.util.Vector;
import static RTSimulator.RTSimulator.println;
import RTSimulator.RTSimulatorMath;

/**
 * A set of cores that share speed/power configuration and belong to a processor.
 * <p>
 * This container extends Vector to hold Core instances and maintains group-level
 * metadata such as group ID, current speed, and current power consumption.
 * It also manages a CoreSpeedSet (DVFS operating points) and a power consumption
 * model, which can be either:
 * - Ideal: always use the analytical power function; or
 * - Non-ideal: use the analytical function when coefficients are provided, else
 *   fall back to a per-speed lookup from the CoreSpeedSet.
 * </p>
 * <p>
 * The setCurrentSpeed method integrates the current statuses of contained cores
 * to decide the group speed. Existing inline notes (in Chinese) indicate that
 * speed for CONTEXTSWITCH and MIGRATION could be treated specially in the future.
 * </p>
 *
 * @author YC
 */
public class CoreSet extends Vector<Core>
{
    private CoreSpeedSet coreSpeedSet;
    private Processor parentProcessor;
    private int groupID;
    private double currentSpeed;
    private double currentPowerConsumption;
    private double alpha;
    private double beta;
    private double gamma;
    private boolean hasPowerConsumptionFunction;
    /**
     * Whether this group uses an idealized power model.
     * <p>
     * If true, the power is always computed via the analytical function
     * alpha + beta * (speed/1000)^gamma (then scaled by 1000) regardless of
     * per-speed lookup values. If false, the setPowerConsumption() method will
     * prefer the analytical function only when coefficients have been provided;
     * otherwise it will fall back to the CoreSpeedSet lookup.
     * </p>
     */
    public boolean isIdeal;
    
    /**
     * Construct an empty CoreSet with default parameters and an empty
     * CoreSpeedSet.
     */
    public CoreSet()
    {
        super();
        this.coreSpeedSet = new CoreSpeedSet(this);
        this.groupID = 0;
        this.currentSpeed = 0;
        this.currentPowerConsumption = 0;
        this.alpha = 0;
        this.beta = 0;
        this.gamma = 0;
        this.hasPowerConsumptionFunction = false;
        this.isIdeal = false;
    }
    
    /**
     * Copy constructor that clones coefficients, flags, and CoreSpeedSet entries
     * from the provided CoreSet. The new CoreSet has its own CoreSpeedSet owned
     * by this instance.
     *
     * @param cSet the source CoreSet to copy from
     */
    public CoreSet(CoreSet cSet)
    {
        super();
        this.coreSpeedSet = new CoreSpeedSet(this);
        this.groupID = 0;
        this.currentSpeed = 0;
        this.currentPowerConsumption = 0;
        this.alpha = cSet.getAlphaValue();
        this.beta = cSet.getBetaValue();
        this.gamma = cSet.getGammaValue();
        this.isIdeal = cSet.isIdeal;
        this.hasPowerConsumptionFunction = cSet.hasPowerConsumptionFunction;
        
        for(CoreSpeed cSpeed : cSet.getCoreSpeedSet())
        {
            this.coreSpeedSet.addSpeed(cSpeed);
        }
    }
    
    /**
     * Add a core to this set and set the core's parent set reference.
     *
     * @param core the core to add
     */
    public void addCore(Core core) 
    {
        this.add(core);
        core.setParentCoreSet(this);
    }
    
    /**
     * Add a DVFS speed entry to this set's CoreSpeedSet.
     *
     * @param s the CoreSpeed entry to add
     */
    public void addCoreSpeed(CoreSpeed s)
    {
        this.coreSpeedSet.addSpeed(s);
    }
    
    /**
     * Placeholder for adding a CoreSpeed entry at the specified index/slot
     * in the CoreSpeedSet. Currently a no-op.
     *
     * @param s the CoreSpeed entry
     * @param i the position or identifier at which to add
     */
    public void addCoreSpeedToSet(CoreSpeed s, int i)
    {
        
    }
    
    
    /*SetValue*/
    /**
     * Set the group ID identifying this set within a processor.
     *
     * @param id the group identifier
     */
    public void setGroupID(int id)
    {
        this.groupID = id;
    }
    
    /**
     * Set the parent processor that owns this set.
     *
     * @param p the Processor instance
     */
    public void setParentProcessor(Processor p)
    {
        this.parentProcessor = p;
    }
    
    /**
     * Set the core type by string flag.
     * <p>
     * Accepts "Ideal" to enable the ideal model and "nonIdeal" to disable it.
     * Any other string value will trigger a console warning ("CoreType Error!!!!!").
     * </p>
     *
     * @param s the type flag ("Ideal" or "nonIdeal")
     */
    public void setCoreType(String s)
    {
        if(s.equals("Ideal"))
        {
            this.isIdeal = true;
        }
        else if(s.equals("nonIdeal"))
        {
            this.isIdeal = false;
        }
        else
        {
            println("CoreType Error!!!!!");
        }
    }
    
    /**
     * Set the alpha coefficient of the analytical power model and mark the
     * model as available.
     *
     * @param a the alpha value
     */
    public void setAlphaValue(double a)
    {
        this.alpha = a;
        this.hasPowerConsumptionFunction = true;
    }
    
    /**
     * Set the beta coefficient of the analytical power model and mark the
     * model as available.
     *
     * @param b the beta value
     */
    public void setBetaValue(double b)
    {
        this.beta = b;
        this.hasPowerConsumptionFunction = true;
    }
    
    /**
     * Set the gamma exponent of the analytical power model and mark the model
     * as available.
     *
     * @param r the gamma (exponent) value
     */
    public void setGammaValue(double r)
    {
        this.gamma = r;
        this.hasPowerConsumptionFunction = true;
    }
    
    /**
     * Replace the CoreSpeedSet used by this group.
     *
     * @param ss the new CoreSpeedSet
     */
    public void setCoreSpeedSet(CoreSpeedSet ss)
    {
        this.coreSpeedSet = ss;
    }
    
    /**
     * Recompute and set the current group speed based on member core statuses
     * and the CoreSpeedSet mapping.
     * <p>
     * Behavior summary (integrates inline notes):
     * - For each core, set its current speed depending on status; currently
     *   WAIT uses the minimal speed and IDLE uses the idle speed. The comment
     *   suggests future handling for CONTEXTSWITCH and MIGRATION statuses.
     * - The group currentSpeed is set to the mapped speed equal to the maximum
     *   core speed seen. If the group speed changed, each core is flagged
     *   with isChangeSpeed = true.
     * - Finally, power consumption is updated via setPowerConsumption().
     * </p>
     */
    public void setCurrentSpeed()
    {
        double speed = -1;
        
        for(Core core : this)
        {
            switch(core.getStatus())
            {
                //請加入CONTEXTSWITCH ＆ MIGRATION 的使用速度
                
                case WAIT :
                    core.setCurrentSpeed(this.getCoreSpeedSet().getMinSpeed().getSpeed());
                break;
                case IDLE : 
                    core.setCurrentSpeed(this.getCoreSpeedSet().getIDELSpeed().getSpeed());
                break;  
                default:
            }
        }
        
        for(Core core : this)
        {
            speed = core.getCurrentSpeed() > speed ? core.getCurrentSpeed() : speed;
        }
        double s = this.coreSpeedSet.getCurrentSpeed(speed);
        
        if(this.currentSpeed != s)
        {
            this.currentSpeed = s;
            
            for(Core core : this)
            {
                core.isChangeSpeed = true;
            }
        }
        this.setPowerConsumption();
    }
    
    /**
     * Compute the current power consumption based on the ideal flag and the
     * availability of analytical model coefficients.
     * <p>
     * - If ideal, always use: (alpha + beta * (currentSpeed/1000)^gamma) * 1000
     * - Else if coefficients are provided, use the same analytical function.
     * - Otherwise, attempt to find a matching per-speed power from CoreSpeedSet;
     *   if not found, print a warning and set to -1 (Chinese inline note:
     *   "檢查是否取得相對應的 PowerConsumption").
     * </p>
     */
    public void setPowerConsumption()
    {
        if(this.isIdeal)
        {
            this.currentPowerConsumption = RTSimulatorMath.mul((this.alpha + (this.beta * Math.pow(RTSimulatorMath.div(this.currentSpeed, 1000), this.gamma) ) ),1000);
        }
        else
        {
            if(this.hasPowerConsumptionFunction)
            {
                this.currentPowerConsumption = RTSimulatorMath.mul((this.alpha + (this.beta * Math.pow(RTSimulatorMath.div(this.currentSpeed, 1000), this.gamma) ) ),1000);
            }
            else
            {
                boolean isFound = false;
                for(CoreSpeed s : this.coreSpeedSet)
                {
                    if( this.currentSpeed == s.getSpeed() )
                    {
                        this.currentPowerConsumption = s.getPowerConsumption();
                        isFound = true;
                    }
                }
                if(!isFound)//檢查是否取得相對應的 PowerConsumption
                {
                    println("Not found PowerConsumption of Speed");
                    this.currentPowerConsumption = -1;
                }
            }
        }
    }
    
    /*GetValue*/
    /**
     * Return the group ID.
     *
     * @return the group identifier
     */
    public int getGroupID()
    {
        return this.groupID;
    }
    
    /**
     * Return the parent processor.
     *
     * @return the owning Processor
     */
    public Processor getParentProcessor()
    {
        return this.parentProcessor;
    }
    
    /**
     * Return the alpha coefficient of the analytical power model.
     *
     * @return alpha value
     */
    public double getAlphaValue()
    {
        return this.alpha;
    }
    
    /**
     * Return the beta coefficient of the analytical power model.
     *
     * @return beta value
     */
    public double getBetaValue()
    {
        return this.beta;
    }
    
    /**
     * Return the gamma exponent of the analytical power model.
     *
     * @return gamma value
     */
    public double getGammaValue()
    {
        return this.gamma;
    }
    
    /**
     * Return the Core at the specified index in this set.
     *
     * @param i zero-based index
     * @return the Core instance
     */
    public Core getCore(int i)
    {
        return this.get(i);
    }
    
    /**
     * Return the CoreSpeedSet associated with this group.
     *
     * @return the CoreSpeedSet
     */
    public CoreSpeedSet getCoreSpeedSet()
    {
        return this.coreSpeedSet;
    }
    
    /**
     * Return the current speed selected for this group.
     *
     * @return current speed value
     */
    public double getCurrentSpeed()
    {
        return this.currentSpeed;
    }
    
    /**
     * Return the normalized speed relative to the maximum speed available in
     * the CoreSpeedSet.
     *
     * @return normalized speed in [0, 1] if max is non-zero; otherwise unbounded
     */
    public double getNormalizationOfSpeed()
    {
        return this.currentSpeed / this.coreSpeedSet.getMaxFrequencyOfSpeed();
    }
    
    /**
     * Return the current power consumption for this group.
     *
     * @return power consumption value
     */
    public double getPowerConsumption()
    {
        return this.currentPowerConsumption;
    }
}