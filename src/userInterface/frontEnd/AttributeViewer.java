/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package userInterface.frontEnd;

import ResultSet.SchedulingInfo;
import java.awt.BorderLayout;
import java.awt.Color;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import static RTSimulator.Definition.magnificationFactor;
import RTSimulator.RTSimulatorMath;
//import simulation.Result;
import userInterface.UserInterface;
import userInterface.backEnd.MouseTimeLine;

/**
 * A front-end panel component that displays a collection of scheduling and runtime
 * attributes related to the currently selected job, task, and core in the real-time
 * simulation environment. The information is presented in a two-column {@link JTable}
 * whose left column lists attribute names (e.g., StartTime, CoreID, JobStatus) and
 * whose right column lists their dynamically updated values.
 * <p>
 * The viewer supports updating its data through {@link #setAtbData(SchedulingInfo)},
 * which extracts rich details from a {@link SchedulingInfo} instance including timing,
 * core speed normalization, power consumption statistics, pending/response times, and
 * locked resources. The table row height is adjusted automatically to accommodate
 * multiple locked resources when present.
 * </p>
 * <p>
 * A toolbar at the top contains a time line selection combo box. (Earlier versions
 * indicated by the commented code used a generic {@code JComboBox<Double>} for timeline
 * values; it now uses {@code JComboBox<MouseTimeLine>} to provide richer time navigation
 * semantics.)
 * </p>
 *
 * @author ShiuJia
 */
public class AttributeViewer extends JPanel
{
    private UserInterface parent;
    private JComboBox<MouseTimeLine> timeLineSet;
    private JToolBar toolBar;
    private JTable table;
    
    private String StartTime = "", EndTime = "", Speed = "",PowerConsumption = "", CoreStatus = "", LockedResource = "",
                    TaskID = "", JobID = "", CoreID = "", JobMissDeadlineNum="", JobCompletedNum="", AveragePowerConsumption="",
                    JobStatus="", pendingTime="",responseTime="";
    private int numAbt;

    /**
     * Creates the attribute viewer panel and initializes its internal UI components,
     * including the toolbar with the time line selection combo box and the attribute
     * table. After construction, the panel is ready to receive scheduling data via
     * {@link #setAtbData(SchedulingInfo)}.
     *
     * @param ui the parent {@link UserInterface} through which this viewer can
     *           interact with broader application context (e.g., simulation state).
     */
    public AttributeViewer(UserInterface ui)
    {
        super();
        parent = ui;
        init();
    }

    /**
     * Updates the table's displayed attribute values based on the provided
     * {@link SchedulingInfo}. Each field is carefully extracted with fallbacks to
     * "Null" if unavailable (guarded by individual {@code try/catch} blocks). The method
     * formats numeric values (e.g., speeds, power consumption, pending/response times)
     * using helper methods in {@link RTSimulatorMath}, normalizing by
     * {@link RTSimulator.Definition#magnificationFactor} where appropriate.
     * <p>
     * Locked resources are rendered as HTML within the cell to allow multi-line display;
     * the corresponding row height is increased proportionally to the number of entered
     * critical sections so all resource identifiers remain visible. Core speed is shown
     * with both raw and normalized values. Job status includes the time at which the
     * status was recorded in seconds.
     * </p>
     *
     * @param schedulingInfo the scheduling information snapshot from which attributes
     *                       (core ID/status, task/job identifiers and statistics, timing
     *                       metrics, power consumption, and locked resource set) are
     *                       derived.
     */
    public void setAtbData(SchedulingInfo schedulingInfo)
    {
//        DecimalFormat df = new DecimalFormat("##.00000");
        RTSimulatorMath math = new RTSimulatorMath();
        try
        {
            this.CoreID="" + schedulingInfo.getCore().getID();
        }
        catch (Exception ex)
        {
            this.CoreID="Null";
        }
        
        try
        {
            this.TaskID="" + schedulingInfo.getJob().getParentTask().getID()+" (" + (double)schedulingInfo.getJob().getParentTask().getComputationAmount()/magnificationFactor 
                    + "," + schedulingInfo.getJob().getParentTask().getPeriod()/magnificationFactor+")";
            this.JobMissDeadlineNum = ""+schedulingInfo.getJobMissDeadlineNum();
            this.JobCompletedNum = ""+schedulingInfo.getJobCompletedNum();
        }
        catch (Exception ex)
        {
            this.TaskID="Null";
            this.JobMissDeadlineNum = "Null";
            this.JobCompletedNum = "Null";
        }
        
        try
        {
            this.JobID="" + schedulingInfo.getJob().getID();
            this.JobStatus=""+schedulingInfo.getJob().getStatusString() + " at " + schedulingInfo.getJob().getTimeOfStatus() + " (s)";
            this.responseTime=""+math.changeDecimalFormat((double)schedulingInfo.getJob().getResponseTime()/magnificationFactor);
            this.pendingTime=""+math.changeDecimalFormat((double)schedulingInfo.getJob().getPendingTime()/magnificationFactor);
        }
        catch (Exception ex)
        {
            this.JobID="Null";
            this.JobStatus="Null";
            this.responseTime="Null";
            this.pendingTime="Null";
        }

        this.StartTime="" + schedulingInfo.getStartTime()+" (s)" ;
        this.EndTime="" + schedulingInfo.getEndTime()+" (s)" ;
        
        try
        {
            this.Speed="" + schedulingInfo.getUseSpeed() + "_(" + math.changeDecimalFormatFor5(schedulingInfo.getNormalizationOfSpeed()) + ")";
        }
        catch (Exception ex)
        {
            this.Speed="Null";
        }
        
        try
        {
            this.PowerConsumption = ""+math.changeDecimalFormatFor5(schedulingInfo.getTotalPowerConsumption()) +" (mW)";//+ rd.getTotalPowerConsumption() + " (mW)";
            this.AveragePowerConsumption = ""+math.changeDecimalFormatFor5(schedulingInfo.getAveragePowerConsumption())+ " (mW/s)";
            this.CoreStatus="" + schedulingInfo.getCoreStatus();
        }
        catch (Exception ex)
        {
             this.PowerConsumption = ""+"Null";
            this.AveragePowerConsumption = ""+"Null";
            this.CoreStatus="" + "Null";   
        }
        
        String str = "";
        if (schedulingInfo.getEnteredCriticalSectionSet().size() != 0)
        {
            for (int i = 0; i < schedulingInfo.getEnteredCriticalSectionSet().size(); i++)
            {
                str = str + 'R' + schedulingInfo.getEnteredCriticalSectionSet().get(i).getUseSharedResource().getID() +
                        "(" + schedulingInfo.getEnteredCriticalSectionSet().get(i).getResourceID() +"/" + 
                        schedulingInfo.getEnteredCriticalSectionSet().get(i).getUseSharedResource().getResourcesAmount() +")" + "<br>";
            }
        }
        else
        {
            str = "Null";
        }
        this.LockedResource="<html>" + str + "<html>";
        
        int rowHeightScale = schedulingInfo.getEnteredCriticalSectionSet().size() > 1 ? schedulingInfo.getEnteredCriticalSectionSet().size()-1 : 0;
        this.table.setRowHeight(17, 30 + 20*(rowHeightScale));
        
        table.getModel().setValueAt(this.StartTime,0,1);
        table.getModel().setValueAt(this.EndTime,1,1);
        
        table.getModel().setValueAt(this.CoreID,3,1);
        table.getModel().setValueAt(this.CoreStatus,4,1);
        table.getModel().setValueAt(this.Speed,5,1);
        table.getModel().setValueAt(this.PowerConsumption,6,1);
        table.getModel().setValueAt(this.AveragePowerConsumption,7,1);
        
        table.getModel().setValueAt(this.TaskID,9,1);
        table.getModel().setValueAt(this.JobCompletedNum,10,1);
        table.getModel().setValueAt(this.JobMissDeadlineNum,11,1);
        
        table.getModel().setValueAt(this.JobID,13,1);
        table.getModel().setValueAt(this.pendingTime,14,1);
        table.getModel().setValueAt(this.responseTime,15,1);
        table.getModel().setValueAt(this.JobStatus,16,1);
        table.getModel().setValueAt(this.LockedResource,17,1);
    }

    private void init()
    {
        this.setLayout(new BorderLayout());
        toolBar = new JToolBar();
        timeLineSet = new JComboBox<MouseTimeLine>();

        toolBar.add(new JLabel("Time:"));
        toolBar.add(timeLineSet);
        this.add(toolBar, BorderLayout.NORTH);

        
        String[] str = {"StartTime:", "EndTime:", "", "CoreID:", "CoreStatus:", "Speed:","PowerConsumption:", "AveragePowerConsumption:",
                    "", "TaskID(C,P):", "JobCompletedNum:","JobMissDeadlineNum:", "", "JobID:" ,"PendingTime:","ResponseTime:",
                    "JobStatus:", "LockedResource:"};
        
        table = new JTable(str.length, 2)
        {
            public boolean isCellEditable(int row, int col)
            {
                return false;
            }
        };

        table.setBackground(Color.white);
        table.setTableHeader(null);
        table.getColumnModel().getColumn(0).setMinWidth(170);
        table.getColumnModel().getColumn(0).setMaxWidth(170);
        table.getColumnModel().getColumn(1).setMinWidth(90);
        table.setRowHeight(30);
        table.setGridColor(Color.BLACK);

        for (numAbt = 0; numAbt < str.length; numAbt++)
        {
            table.getModel().setValueAt(str[numAbt], numAbt, 0);
        }
        
        MyCellRenderer renderer = new MyCellRenderer();
        renderer.setHorizontalAlignment(JLabel.RIGHT);
        table.setDefaultRenderer(Object.class, renderer);

        table.setRowHeight(str.length-1, 100);
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setViewportView(table);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(16);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getViewport().setBackground(Color.getHSBColor((float)0.7,(float)0,(float)0.9));
        
        this.add(scrollPane, BorderLayout.CENTER);
    }

    /**
     * Returns the combo box used to select or navigate the time line for viewing
     * attributes. (Earlier commented code shows this was once a {@code JComboBox<Double>};
     * it is now strongly typed to {@link MouseTimeLine} for enhanced interaction.)
     *
     * @return the time line selection {@link JComboBox} instance.
     */
    public JComboBox<MouseTimeLine> getTimeLineSet()
    {
        return this.timeLineSet;
    }
    
    /**
     * Replaces the current time line combo box with the provided one, updating the
     * toolbar to reflect the new component and triggering a repaint so the UI shows
     * the change immediately. (Historically this accepted a {@code JComboBox<Double>} as
     * indicated by the commented line; it now works with {@code JComboBox<MouseTimeLine>}).
     *
     * @param jb the new {@link JComboBox} instance supplying {@link MouseTimeLine}
     *           entries for time navigation.
     */
    public void setTimeLineSet(JComboBox<MouseTimeLine> jb)
    {
        this.toolBar.remove(this.timeLineSet);
        this.timeLineSet = jb;
        this.toolBar.add(timeLineSet);
        this.toolBar.repaint();
    }
    
    /**
     * Custom table cell renderer for the attribute viewer's table. It applies
     * alternating background coloring based on whether the left-hand attribute
     * label cell is blank (pure white for spacing rows, light gray for labeled
     * rows), while preserving selection foreground/background when a row is
     * selected. Alignment is handled externally by setting horizontal alignment
     * to {@code JLabel.RIGHT} in the initialization sequence.
     */
    public class MyCellRenderer extends javax.swing.table.DefaultTableCellRenderer 
    {
        /**
         * Configures the rendering component for a table cell, applying color
         * logic based on whether the attribute label (first column of the row)
         * is empty. Selection colors override the default shading when the cell
         * is selected, ensuring consistency with table selection behavior.
         *
         * @param table the {@link JTable} being rendered.
         * @param value the cell value to display.
         * @param isSelected true if the cell is currently selected.
         * @param hasFocus true if the cell has input focus.
         * @param row the row index of the cell being rendered.
         * @param column the column index of the cell being rendered.
         * @return the component used to render the cell.
         */
        public java.awt.Component getTableCellRendererComponent(javax.swing.JTable table, java.lang.Object value, boolean isSelected, boolean hasFocus, int row, int column) 
        {
            final java.awt.Component cellComponent = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, 0, column);

            Object val = table.getValueAt(row, 0);
            String sval = val.toString();
            sval = sval.replaceAll(":", "");
            if (sval == "") 
            {   
                cellComponent.setBackground(Color.white);
                cellComponent.setForeground(Color.black);
            } 
            else 
            {
                cellComponent.setBackground(new Color(230, 230, 230));
                cellComponent.setForeground(Color.black);
            }
            
            if (isSelected) 
            {
                cellComponent.setForeground(table.getSelectionForeground());
                cellComponent.setBackground(table.getSelectionBackground());
            }
            return cellComponent;
        }
    }
}