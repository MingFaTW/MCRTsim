/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package scriptsetter;

import PartitionAlgorithm.PartitionAlgorithm;
import SystemEnvironment.Core;
import SystemEnvironment.DataReader;
import SystemEnvironment.Simulator;
import WorkLoad.Task;
import WorkLoadSet.DataSetting;
import concurrencyControlProtocol.ConcurrencyControlProtocol;
import dynamicVoltageAndFrequencyScalingMethod.DynamicVoltageAndFrequencyScalingMethod;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import static javax.swing.JOptionPane.WARNING_MESSAGE;
import static RTSimulator.Definition.magnificationFactor;
import static RTSimulator.RTSimulator.println;
import RTSimulator.RTSimulatorMath;
import schedulingAlgorithm.PriorityDrivenSchedulingAlgorithm;
import userInterface.frontEnd.SimulationViewer;

/**
 * A {@link Runnable} that executes a single script workload run.
 *
 * <p>This class is responsible for running a {@link Script} on a specific
 * workload file. When executed it:
 * <ol>
 *   <li>loads workload and processor data via {@link DataReader},</li>
 *   <li>configures a {@link Simulator} (simulation time, scheduling/partition
 *       algorithm, concurrency-control protocol and DVFS method),</li>
 *   <li>starts the simulation, collects per-core power samples and other
 *       metrics, builds a {@link ScriptResult}, and attaches it to the
 *       originating {@link Script}.</li>
 * </ol>
 *
 * <p>The class uses reflection to instantiate algorithm implementations by
 * name (factory-style methods at the bottom of the class). Exceptions thrown
 * by those reflection calls are propagated by the factory methods so callers
 * can handle them appropriately.</p>
 *
 * @author YC
 */
public class ScriptRunnable implements Runnable
{
    private Thread t;
    private Script script;
    private String workloadFileName;

    /**
     * Parent UI/controller that created this runnable. Public to match the
     * project's existing usage where external code may access the parent
     * field directly (e.g. for error dialogs or higher-level coordination).
     */
    public ScriptSetter parent;
    
    ScriptRunnable(ScriptSetter p, Script s, String name) 
    {
        this.parent = p;
        this.script = s;
        this.workloadFileName = name;
        System.out.println("creat : "+script.getID()+" , "+workloadFileName);
    }
   
    /**
     * Runs the simulation for the configured {@link Script} and workload file.
     *
     * <p>This method performs the following actions in sequence:
     * <ul>
     *   <li>Loads workload and processor definitions from XML files using
     *       {@link DataReader} (workload path is formed from the script's
     *       workload site and the provided workload filename).</li>
     *   <li>Configures the {@link Simulator} with simulation time (scaled by
     *       {@code magnificationFactor}), scheduling algorithm, partition
     *       algorithm, concurrency control protocol and DVFS method obtained
     *       from the {@link Script} configuration.</li>
     *   <li>Starts the simulator and, when finished, gathers power samples and
     *       other metrics into a new {@link ScriptResult} which is then added
     *       to the parent {@link Script}.</li>
     * </ul>
     *
     * <p>All runtime exceptions are caught; on error a dialog is shown to the
     * user and the exception is logged. The method does not propagate
     * checked exceptions because it handles them internally.</p>
     */
    public void run() 
    {
        
        System.out.println("Start : "+script.getID()+" , "+workloadFileName);
        try
        {
//            Vector<String> workloadFileNames = this.getFolderFile(script.getWorkloadSite());
            //Vector<String> processorfileNames = this.getFolderFile(script.getProcessorSite());
            String processorFileName = script.getProcessorSite();
            
            println(script.getWorkloadSite()+"/"+workloadFileName + ".xml");
            
            println(processorFileName);
            
            
            int i = 0;
            
                DataReader dataReader = new DataReader();
                Simulator simulator = new Simulator();
                println(script.getWorkloadSite()+"/"+workloadFileName + ".xml");
                println(processorFileName);
                
                dataReader.loadSource(script.getWorkloadSite()+"/"+workloadFileName + ".xml");
                dataReader.loadSource(processorFileName);
                simulator.setSimulationTime
                (
                    Double.valueOf
                    (
                        Double.valueOf
                        (
                            script.getSimulationTime()
                        )*magnificationFactor
                    ).longValue()
                );

                simulator.loadDataSetting(dataReader.getDataSetting());
                simulator.getProcessor().setSchedAlgorithm(this.getPrioritySchedulingAlgorithm(script.getSchedulingAlgorithm()));
                simulator.getProcessor().setPartitionAlgorithm(this.getPartitionAlgorithm(script.getPartitionAlgorithm()));
                simulator.getProcessor().setCCProtocol(this.getConcurrencyControlProtocol(script.getCCProtocol()));
                simulator.getProcessor().setDVFSMethod(this.getDynamicVoltageScalingMethod(script.getDVFSMethod()));

                dataReader.getDataSetting().getProcessor().showInfo();
                println("Workload:" + dataReader.getDataSetting().getTaskSet().getProcessingSpeed());
                for(Task t : dataReader.getDataSetting().getTaskSet())
                {
                    t.showInfo();
                }

                simulator.start();

            //setScriptResult---------{
                {
                    ScriptResult sr = new ScriptResult(script);
                    sr.setWorkloadFile(workloadFileName);
                    sr.setProcessorFile(processorFileName);
                    
                    RTSimulatorMath math = new RTSimulatorMath();
                    for(Core c : simulator.getProcessor().getAllCore())
                    {
                        sr.addPowerConsumption(math.changeDecimalFormatFor5((double)c.getPowerConsumption()/magnificationFactor));
                    }

                    DataSetting ds = dataReader.getDataSetting();

                    sr.setTaskCount(ds.getTaskSet().size());
                    sr.setTotalJobCompeletedCount(ds.getTaskSet().getTotalJobCompletedNumber());
                    // fix: DataSetting.getTaskSet() already returns the TaskSet; do not call getTaskSet() again
                    sr.setTotalJobMissDeadlineCount(ds.getTaskSet().getTotalJobMissDeadlineNumber());
                    sr.setCompletedRatio(ds.getTaskSet().getJobCompletedRatio());
                    sr.setDeadlineMissRatio(ds.getTaskSet().getJobMissDeadlineRatio());
                    sr.setAveragePendingTime(ds.getTaskSet().getAveragePendingTimeOfTask());
                    sr.setAverageResponseTime(ds.getTaskSet().getAverageResponseTimeOfTask());
                    sr.setMaximumUtilization(ds.getTaskSet().getMaximumUtilization());
                    sr.setActualUtilization(ds.getTaskSet().getActualUtilization());
                    sr.setMaximumCriticalSectionRatio(ds.getTaskSet().getMaximumCriticalSectionRatio());
                    sr.setActualCriticalSectionRatio(ds.getTaskSet().getActualCriticalSectionRatio());
                    sr.setBeBlockedTimeRatio(ds.getTaskSet().getAverageBeBlockedTimeRatioOfTask());
                    script.addScriptResult(sr);
                }
            //------------------------}
                
                System.out.println("End :"+script.getID()+" , "+workloadFileName);
//            }
        }
        catch (Exception ex) 
        {
            JOptionPane.showMessageDialog(parent.parent.parent.getFrame(), "Error!!" ,"Error!!" ,WARNING_MESSAGE);
            Logger.getLogger(SimulationViewer.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
   
    /**
     * Starts this runnable in a new thread if it is not already running.
     *
     * <p>This method creates a new {@link Thread} to execute {@link #run()} and
     * starts it. Subsequent calls while the thread is already running have no
     * effect.</p>
     */
    public void start () 
    {
        System.out.println("Starting ");
        if (t == null) 
        {
            t = new Thread (this);
            t.start ();
        }
    }
    
    /**
     * Creates a {@link PartitionAlgorithm} instance by class name using reflection.
     *
     * @param name simple class name (the implementation is expected under
     *             package "PartitionAlgorithm.implementation")
     * @return a new {@link PartitionAlgorithm} instance
     * @throws ClassNotFoundException if the implementation class cannot be found
     * @throws InstantiationException if the class cannot be instantiated
     * @throws IllegalAccessException if the no-arg constructor is not accessible
     * @throws NoSuchMethodException if the expected constructor cannot be found
     * @throws IllegalArgumentException if an invalid argument is passed to the
     *         reflective constructor invocation
     * @throws InvocationTargetException if the constructor throws an exception
     */
    public PartitionAlgorithm getPartitionAlgorithm(String name) throws ClassNotFoundException, InstantiationException, IllegalAccessException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException
    {
        return (PartitionAlgorithm)Class.forName("PartitionAlgorithm.implementation." + name).getDeclaredConstructor().newInstance();            
    }
    
    /**
     * Creates a {@link PriorityDrivenSchedulingAlgorithm} instance by class name
     * using reflection. The implementation is expected under
     * "schedulingAlgorithm.implementation".
     *
     * @param name simple class name of the scheduling algorithm implementation
     * @return a new {@link PriorityDrivenSchedulingAlgorithm} instance
     * @throws ClassNotFoundException if the implementation class cannot be found
     * @throws InstantiationException if the class cannot be instantiated
     * @throws IllegalAccessException if the no-arg constructor is not accessible
     */
    public PriorityDrivenSchedulingAlgorithm getPrioritySchedulingAlgorithm(String name) throws ClassNotFoundException, InstantiationException, IllegalAccessException
    {
        return (PriorityDrivenSchedulingAlgorithm)Class.forName("schedulingAlgorithm.implementation." + name).newInstance();                
    }
    /**
     * Creates a {@link DynamicVoltageAndFrequencyScalingMethod} instance by
     * class name using reflection. The implementation is expected under
     * "dynamicVoltageAndFrequencyScalingMethod.implementation".
     *
     * @param name simple class name of the DVFS implementation
     * @return a new {@link DynamicVoltageAndFrequencyScalingMethod} instance
     * @throws ClassNotFoundException if the implementation class cannot be found
     * @throws InstantiationException if the class cannot be instantiated
     * @throws IllegalAccessException if the no-arg constructor is not accessible
     * @throws NoSuchMethodException if the expected constructor cannot be found
     * @throws IllegalArgumentException if an invalid argument is passed to the
     *         reflective constructor invocation
     * @throws InvocationTargetException if the constructor throws an exception
     */
    public DynamicVoltageAndFrequencyScalingMethod getDynamicVoltageScalingMethod(String name) throws ClassNotFoundException, InstantiationException, IllegalAccessException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException
    {
        return (DynamicVoltageAndFrequencyScalingMethod)Class.forName("dynamicVoltageAndFrequencyScalingMethod.implementation." + name).getDeclaredConstructor().newInstance();
    }
    
    /**
     * Creates a {@link ConcurrencyControlProtocol} instance by class name using
     * reflection. The implementation is expected under
     * "concurrencyControlProtocol.implementation".
     *
     * @param name simple class name of the concurrency control protocol
     * @return a new {@link ConcurrencyControlProtocol} instance
     * @throws ClassNotFoundException if the implementation class cannot be found
     * @throws InstantiationException if the class cannot be instantiated
     * @throws IllegalAccessException if the no-arg constructor is not accessible
     * @throws NoSuchMethodException if the expected constructor cannot be found
     * @throws IllegalArgumentException if an invalid argument is passed to the
     *         reflective constructor invocation
     * @throws InvocationTargetException if the constructor throws an exception
     */
    public ConcurrencyControlProtocol getConcurrencyControlProtocol(String name) throws ClassNotFoundException, InstantiationException, IllegalAccessException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException
    {
        return (ConcurrencyControlProtocol)Class.forName("concurrencyControlProtocol.implementation." + name).getDeclaredConstructor().newInstance();
    }
}