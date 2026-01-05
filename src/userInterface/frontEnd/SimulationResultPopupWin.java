/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package userInterface.frontEnd;

import WorkLoadSet.TaskSet;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import static RTSimulator.Definition.magnificationFactor;
import RTSimulator.RTSimulatorMath;

/**
 * A popup window that displays summary statistics and configuration
 * information for the current simulation run.
 * <p>
 * The window constructs a two-column grid of labels (left column is the
 * static label names such as "Simulation Time" and the right column shows
 * the computed values). The set of displayed fields is defined by the
 * {@code str} array and includes items such as total utilization, partition
 * algorithm, DVFS method, scheduling algorithm, completed/missed jobs,
 * and energy consumption statistics.
 * </p>
 * <p>
 * The class relies on a parent {@link SimulationViewer} reference to obtain
 * the simulation time, data settings, task set and UI selections. The
 * displayed numeric values are formatted via {@link RTSimulatorMath}.
 * </p>
 *
 * @author YC
 */
public class SimulationResultPopupWin extends JFrame
{
    /**
     * Reference to the owning SimulationViewer.
     * <p>
     * This is used to access simulation state (time, data reader, selected
     * partition/DVFS/scheduling/CCP combobox values) required to populate the
     * popup's labels. It is intentionally public so callers can toggle or
     * query the popup from the viewer code.
     * </p>
     */
    public SimulationViewer parent;
    
    private Vector<JLabel> data;
    String[] str ={"Simulation Time : ","The Number Of Cores : ","The Number Of Tasks : ","The Number Of Resources : ","Total Utilization Of Tasks : ","Patition Algorithm : "
        ,"DVFS Method : ","Schduling Algorithm : ","Synchronization Protocol : ","The Number Of Jobs Compeleted : "
        ,"The Number Of Jobs Missed Deadline : ","Energy Consumption : ", "Average Energy Consumption Per Job : "
        ,"Average Pending Time Per Job: ","Average Response Time Per Job: "};
    private JLabel simulationTime,coreCount,taskCount,resourceCount,totalUtilization,patitionMethod,DVFSMethod,schdulingAlgorithm
                ,concurrencyControlProtocol,jobCompeletedCount,jobMissDeadlineCount,energyConsumption
                ,averageEnergyConsumption,averagePendingTime,averageResponseTime;
    
    /**
     * Create and show a SimulationResultPopupWin associated with the given
     * SimulationViewer.
     * <p>
     * The constructor stores the provided parent reference, initializes the
     * UI components by calling {@link #init()}, and triggers a revalidation so
     * the window layout is updated before use.
     * </p>
     *
     * @param sv the owning SimulationViewer used to obtain simulation data
     */
    public SimulationResultPopupWin(SimulationViewer sv)
    {
        super();
        this.parent = sv;
        init();
        this.revalidate();
    }
    
    private void init()
    {
        data = new Vector<>();
        simulationTime = new JLabel();
        coreCount = new JLabel();
        taskCount = new JLabel();
        resourceCount = new JLabel();
        totalUtilization = new JLabel();
        patitionMethod = new JLabel();
        DVFSMethod = new JLabel();
        schdulingAlgorithm = new JLabel();
        concurrencyControlProtocol = new JLabel();
        jobCompeletedCount = new JLabel();
        jobMissDeadlineCount = new JLabel();
        energyConsumption = new JLabel();
        averageEnergyConsumption = new JLabel();
        averagePendingTime = new JLabel();
        averageResponseTime = new JLabel();
        
        data.add(simulationTime);
        data.add(coreCount);
        data.add(taskCount);
        data.add(resourceCount);
        data.add(totalUtilization);
        data.add(patitionMethod);
        data.add(DVFSMethod);
        data.add(schdulingAlgorithm);
        data.add(concurrencyControlProtocol);
        data.add(jobCompeletedCount);
        data.add(jobMissDeadlineCount);
        data.add(energyConsumption);
        data.add(averageEnergyConsumption);
        data.add(averagePendingTime);
        data.add(averageResponseTime);
        
        this.setTitle("Simulation Results");
        this.setBounds(300, 300, 600, 350);
        this.setMinimumSize(new Dimension(600, 350));
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setVisible(true);
        this.setLayout(new GridBagLayout());
        
        GridBagConstraints bag = new GridBagConstraints();
        bag.gridx = 0;
        bag.gridy = 0;
        bag.gridwidth = 1;
        bag.gridheight = 1;
        bag.weightx = 0;
        bag.weighty = 1;
        bag.fill = GridBagConstraints.NONE;
        bag.anchor = GridBagConstraints.EAST;
        
        for(int i = 0 ; i<str.length ; i++)
        {
            this.add(new JLabel(str[i]), bag);
            bag.gridy +=1;
        }
        
        this.setData();
        bag.anchor = GridBagConstraints.WEST;
        bag.gridx = 1;
        bag.gridy = 0;
        for(int i = 0 ; i<data.size() ; i++)
        {
            this.add(data.get(i), bag);
            bag.gridy +=1;
        }
        
        JButton closeBtn = new JButton("Close");
        closeBtn.setForeground(Color.red);
        closeBtn.addMouseListener
        (new MouseAdapter()
            {
                public void mouseClicked(MouseEvent e)
                {
                    SimulationResultPopupWin.this.setVisible(false);
                }
            }
        );
        
        bag.anchor = GridBagConstraints.EAST;
        bag.gridy +=1;
        this.add(closeBtn,bag);   
    }
    
    /**
     * Populate the popup labels with computed statistics from the parent
     * SimulationViewer's data settings and TaskSet.
     * <p>
     * This method queries the parent's DataSetting and TaskSet to compute
     * values such as simulation time (formatted using {@link RTSimulatorMath}),
     * number of cores, number of tasks, shared resource count, total
     * utilization, selected algorithm names from the parent's comboboxes,
     * completed/missed job counts, total energy consumption and average
     * metrics per job. The right-hand column labels are updated with the
     * formatted values.
     * </p>
     * <p>
     * Any exceptions encountered while reading the simulation data are caught
     * and logged; the method does not propagate checked exceptions.
     * </p>
     */
    public void setData()
    {
        
        try 
        {
            RTSimulatorMath math = new RTSimulatorMath();
        
            TaskSet ts = this.parent.getDataSetting().getTaskSet();
        
            simulationTime.setText(""+math.changeDecimalFormat((double)this.parent.getSimulationTime()/magnificationFactor));
            coreCount.setText(""+this.parent.getDataSetting().getProcessor().getAllCore().size());
            taskCount.setText(""+ts.size());
            resourceCount.setText(""+this.parent.getDataSetting().getSharedResourceSet().size());
            totalUtilization.setText("" + math.changeDecimalFormatFor5(ts.getTotalUtilization()));
            patitionMethod.setText(""+this.parent.getPartitionComboBox().getSelectedItem().toString());
            DVFSMethod.setText(""+this.parent.getDVFSComboBox().getSelectedItem().toString());
            schdulingAlgorithm.setText(""+this.parent.getSchedulingComboBox().getSelectedItem().toString());
            concurrencyControlProtocol.setText(""+this.parent.getCCPComboBox().getSelectedItem().toString());
            
            jobCompeletedCount.setText(""+ts.getTotalJobCompletedNumber()+"/"+ts.getTotalJobNumber());
            jobMissDeadlineCount.setText(""+ts.getTotalJobMissDeadlineNumber()+"/"+ts.getTotalJobNumber());
            energyConsumption.setText(""+math.changeDecimalFormatFor5(this.parent.getDataSetting().getProcessor().getTotalPowerConsumption()/magnificationFactor) + " (mW)");
            averageEnergyConsumption.setText(""+math.changeDecimalFormatFor5(((this.parent.getDataSetting().getProcessor().getTotalPowerConsumption()/magnificationFactor)
                                                    /((double)this.parent.getSimulationTime()/magnificationFactor)))+" (mW)");
            averagePendingTime.setText(""+math.changeDecimalFormatFor5(this.parent.getDataSetting().getTaskSet().getAveragePendingTimeOfTask()));
            averageResponseTime.setText(""+math.changeDecimalFormatFor5(this.parent.getDataSetting().getTaskSet().getAverageResponseTimeOfTask()));
        }
        catch (Exception ex) 
        {
            Logger.getLogger(SimulationResultPopupWin.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Toggle the popup's visibility state.
     * <p>
     * If the window is currently visible it will be hidden; if it is hidden
     * it will be shown. This is a convenience wrapper around
     * {@code setVisible(!isVisible())} used by the parent viewer.
     * </p>
     */
    public void changeVisible()
    {
            this.setVisible(!this.isVisible());
    }
    
}