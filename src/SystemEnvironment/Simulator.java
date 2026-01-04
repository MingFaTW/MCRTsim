/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package SystemEnvironment;

import ResultSet.MissDeadlineInfo;
import ResultSet.ResultSet;
import WorkLoadSet.DataSetting;
import RTSimulator.Definition.SchedulingType;
import userInterface.frontEnd.SimulationViewer;

import static RTSimulator.RTSimulator.println;

/**
 * Top-level simulation orchestrator that ties together the {@link Processor},
 * workload/resource settings and result collection.
 *
 * <p>The {@code Simulator} performs the following high-level sequence in
 * {@link #start()}:
 * <ol>
 *   <li>Partition tasks (for SingleCore / Partition scheduling modes).</li>
 *   <li>Calculate fixed priorities if the chosen scheduling algorithm is fixed.</li>
 *   <li>Run controller pre-action hooks (lock / protocol initialization).</li>
 *   <li>Invoke DVFS speed definition on the regulator.</li>
 *   <li>Iteratively execute one simulation time unit until {@code simulationTime}
 *       is reached (using global or per-core execution path based on
 *       {@link SchedulingType}).</li>
 *   <li>Finalize per-core scheduling records and accumulate power usage.</li>
 *   <li>Collect miss-deadline information and produce a {@link ResultSet}.</li>
 * </ol>
 * Inline comments (including Chinese remarks) have been integrated: final
 * recording, power consumption printing, and deadline miss counting. Time
 * consumption of the whole run is measured using {@code System.currentTimeMillis()}.
 *
 * <p>Configuration setters allow adjustment of simulation time, context switch
 * time and migration time prior to start; after execution, getters expose
 * elapsed time and results.</p>
 *
 * @author ShiuJia
 */
public class Simulator
{
    //public SimulationViewer parentSimuationViewer;
    private Processor processor;
    //private TaskSet taskSet;
    //private SharedResourceSet sharedResourceSet;
    private long simulationTime;
    private long elapsedTime;
    private long contextSwitchTime = 0;
    private long migrationTime = 0;
    private ResultSet resultSet;
    
    /**
     * Constructs a simulator with no loaded processor, zero simulation and
     * elapsed time, and an empty {@link ResultSet}. Context switch and migration
     * times default to 0. Use {@link #loadDataSetting(DataSetting)} to populate
     * the processor and workload before calling {@link #start()}.
     */
    public Simulator()
    {
        //this.parentSimuationViewer = sv;
        this.processor = null;
        this.simulationTime = 0;
        this.elapsedTime = 0;
        this.resultSet = new ResultSet();
    }
    
    /*Operating*/
    /**
     * Loads a prepared {@link DataSetting} (processor, task set, shared resource
     * set) into the simulator. Also sets this simulator as the parent of the
     * processor so it can report events (e.g., deadline misses).
     *
     * @param ds the data setting containing processor, tasks and resources
     */
    public void loadDataSetting(DataSetting ds)
    {
        this.processor = ds.getProcessor();
        this.processor.setParentSimulator(this);
        this.processor.loadTaskSet(ds.getTaskSet());
        this.processor.loadResourceSet(ds.getSharedResourceSet());
    }
    
    /**
     * Adds all scheduling information associated with a core into the result set.
     * (Core info currently includes power consumption and scheduling timeline.)
     *
     * @param c the core whose information should be recorded
     */
    public void addCoreInfo(Core c)
    {
        this.resultSet.addCoreInfo(c);
    }
    
    /**
     * Records a deadline miss information object into the result set.
     *
     * @param md the miss-deadline info to add
     */
    public void addMissDeadlineInfo(MissDeadlineInfo md)
    {
        this.resultSet.addMissDeadlineInfo(md);
    }
    
    /**
     * Starts the simulation run loop until {@link #simulationTime} is reached.
     *
     * <p>Sequence: partition tasks, calculate fixed priorities, controller
     * pre-action, DVFS speed definition, then iterate executing 1 time unit at
     * a time using either global or local execution based on scheduling type.
     * After the loop it finalizes each core's last {@code SchedulingInfo},
     * records power consumption, aggregates core info and prints summary
     * statistics (missed deadlines, per-core power, total elapsed wall-clock
     * time).</p>
     */
    public void start()
    {
        long time1,time2;
        time1 = System.currentTimeMillis();
        println(("Start"));
        
        this.processor.partitionTasks();
        this.processor.schedulerCalculatePriorityForFixed();
        
        // Controller pre-initialization (lock / protocol state)
        this.processor.getController().preAction();
        
        // DVFS initial speed definition
        this.processor.getDynamicVoltageRegulator().definedSpeed();
        
        while(this.elapsedTime < this.simulationTime)
        {
            if(this.processor.getSchedulingAlgorithm().getSchedulingType()== SchedulingType.Global)
            {
                this.processor.globalExecute(1);
            }
            else
            {
                this.processor.execute(1);
            }
            this.elapsedTime += 1;
        }
        
        // Finalize core records and aggregate results
        for(Core c : this.processor.getAllCore())
        {
            c.getSchedulingInfoSet().get(c.getSchedulingInfoSet().size() - 1).setEndTime(this.simulationTime);
            c.finalRecording();
            this.resultSet.addCoreInfo(c);
            println("PowerConsumption(" + c.getID() + ")= " + c.getPowerConsumption());
        }
        println("MissDeadline= " + this.resultSet.getMissDeadlineInfoSet().size());
        println("End");
        
        time2 = System.currentTimeMillis();
        System.out.println(("！！！！ Spend：" + (double)(time2-time1)/1000 + " second. ！！！！"));
    }
    
    /*SetValue*/
    /**
     * Sets the total planned simulation time (in simulation time units) to execute.
     *
     * @param i the simulation duration
     */
    public void setSimulationTime(long i)
    {
        this.simulationTime = i;
    }
    
    /**
     * Sets the context switch duration applied when cores perform a context switch.
     *
     * @param i context switch time units
     */
    public void setContextSwitchTime(long i)
    {
        this.contextSwitchTime = i;
    }
    
    /**
     * Sets the migration duration applied when jobs migrate between cores.
     *
     * @param i migration time units
     */
    public void setMigrationTime(long i)
    {
        this.migrationTime = i;
    }
    
    /*GetValue*/
    /**
     * Returns the processor currently loaded into this simulator.
     *
     * @return the processor (may be {@code null} if not yet loaded)
     */
    public Processor getProcessor()
    {
        return this.processor;
    }
    
    /**
     * Returns the amount of simulated time already executed.
     *
     * @return elapsed simulation time
     */
    public long getElapsedTime()
    {
        return this.elapsedTime;
    }
    
    /**
     * Returns the total simulation time target configured.
     *
     * @return planned simulation duration
     */
    public long getSimulationTime()
    {
        return this.simulationTime;
    }
    
    /**
     * Returns the configured context switch time.
     *
     * @return context switch time units
     */
    public long getContextSwitchTime()
    {
        return this.contextSwitchTime;
    }
    
    /**
     * Returns the configured migration time.
     *
     * @return migration time units
     */
    public long getMigrationTime()
    {
        return this.migrationTime;
    }
    
    /**
     * Returns the accumulated result set containing core info and deadline miss data.
     *
     * @return simulation result set
     */
    public ResultSet getResultSet()
    {
        return this.resultSet;
    }
}