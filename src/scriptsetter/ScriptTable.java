/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package scriptsetter;

import java.awt.Color;
import java.awt.GridLayout;
import java.util.Vector;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import static RTSimulator.RTSimulator.println;

/**
 * A table panel for displaying and managing script information.
 * <p>
 * This panel contains a JTable that displays simulation scripts with their
 * configurations. It supports adding, modifying, and removing scripts.
 * 
 * @author YC
 */
public class ScriptTable extends JPanel
{
    /** Reference to the parent ScriptSetter for accessing shared functionality. */
    public ScriptSetter parent;
    
    /** Unique identifier for this script group. */
    private String groupID = "";
    
    /** The JTable component displaying script information. */
    private JTable table;
    
    /** The table model managing the data displayed in the table. */
    private TableModel tableModel;
    
    /** Collection of scripts managed by this table. */
    private Vector<Script> scriptSet;
    
    /**
     * Constructs a new ScriptTable with the specified parent.
     *
     * @param s the parent ScriptSetter
     */
    public ScriptTable(ScriptSetter s)
    {
        this.parent = s;
        this.scriptSet = new Vector<>();
        this.init();
    }
    
    private void init()
    {
        this.setLayout(new GridLayout(1,1));
//        tableModel = new DefaultTableModel() 
//        { 
//            String[] str = {"ScripID","Workload","Processor","TaskToCore","DVFSMethod","SchedAlorithm","CCProtocol","SimulationTime"};
//        
//            @Override 
//            public int getColumnCount() 
//            { 
//                return str.length; 
//            } 
//
//            @Override 
//            public String getColumnName(int index) 
//            { 
//                return str[index]; 
//            } 
//            
//            @Override
//            public boolean isCellEditable(int row, int col)
//            {
//                return false;
//            }
//        };
        this.tableModel = new TableModel();
        table = new JTable(tableModel);
        
        this.add(new JScrollPane(table));
        //table.set
        table.setBackground(Color.white);
        table.setGridColor(Color.BLACK);
        
        
    }
    
    /**
     * Gets the table model used by this table.
     *
     * @return the DefaultTableModel managing the table data
     */
    public DefaultTableModel getTableModel()
    {
        return this.tableModel;
    }
    
    /**
     * Gets the JTable component.
     *
     * @return the JTable displaying the scripts
     */
    public JTable getTable()
    {
        return this.table;
    }
    
    /**
     * Sets the group identifier for this script table.
     *
     * @param s the group ID to set
     */
    public void setGroupID(String s)
    {
        this.groupID = s;
    }
    
    /**
     * Gets the group identifier for this script table.
     *
     * @return the group ID
     */
    public String getGroupID()
    {
        return this.groupID;
    }
    
    /**
     * Gets the number of scripts in this table.
     *
     * @return the count of scripts
     */
    public int getScriptCount()
    {
       return this.scriptSet.size();
    }
    
    /**
     * Gets the collection of scripts managed by this table.
     *
     * @return the vector containing all scripts
     */
    public Vector<Script> getScriptSet()
    {
        return this.scriptSet;
    }
    
    /**
     * Adds a new script based on the configuration in the script panel.
     *
     * @param scriptPanel the panel containing script configuration
     */
    public void addScript(ScriptPanel scriptPanel)
    {
        int row = this.table.getSelectedRow();
        
        Script script = new Script(this,scriptPanel);
        this.scriptSet.add(script);
        this.updateTable();
        
        if(row != -1)
        {
            this.table.setRowSelectionInterval(row,row);
        }
    }
    
    /**
     * Modifies the currently selected script with new configuration from the script panel.
     *
     * @param scriptPanel the panel containing updated script configuration
     */
    public void modifyScript(ScriptPanel scriptPanel)
    {
        int row = this.table.getSelectedRow();
        if(row != -1)
        {
            this.scriptSet.get(row).modifyScript(scriptPanel);
            this.updateTable();
            this.table.setRowSelectionInterval(row,row);
        }
    }
    
    /**
     * Removes the currently selected script(s) from the table.
     */
    public void removeScript()
    {
        int[] row = this.table.getSelectedRows();
        
        if(row.length !=0)
        {
            for(int i = 0; i<row.length ; i++)
            {
                this.scriptSet.remove(row[0]);//重複刪除同一個位置，就可以達到刪除所選取之範圍。
            }
            
            this.updateTable();

            if(this.table.getRowCount()>0)
            {
                if(row[0] <= this.table.getRowCount()-1)
                {
                    this.table.setRowSelectionInterval(row[0],row[0]);
                }
                else
                {
                    this.table.setRowSelectionInterval(this.table.getRowCount()-1,this.table.getRowCount()-1);
                }
            }
        }
        
        println("!!!!!!!!!!");
        for(Script s : this.scriptSet)
        {
            println(s.getID());
        }
        println("!!!!!!!!!!");
        
    }
    
    /**
     * Updates the table display to reflect the current script set.
     * <p>
     * This method removes all existing rows and re-adds them from the script collection.
     */
    public void updateTable()
    {
        this.removeAllRow();
        
        for(Script script : this.scriptSet)
        {
            this.addRow(script);
        }
    }
    
    private void removeAllRow()
    {
        while(this.tableModel.getRowCount()!=0)
        {
            this.tableModel.removeRow(0);
        }
    }
        
    private void addRow(Script script)
    {
        Object[] object = new Object[]{script.getID(),script.getWorkloadSite()
                ,script.getProcessorSite(),script.getPartitionAlgorithm(),script.getDVFSMethod(),script.getSchedulingAlgorithm()
                ,script.getCCProtocol(),script.getSimulationTime()};
        this.tableModel.addRow(object);
    }
    
    /**
     * Modifies a specific row in the table to match the given script.
     * <p>
     * Note: This method is currently not fully implemented.
     *
     * @param script the script whose data should be reflected in the row
     */
    public void modifyRow(Script script)
    {
//        int row = this.table.getSelectedRow();
//        if(row != -1)
//        {
//            this.tableModel.setValueAt(script.ID(), row,0);
//            this.tableModel.setValueAt(script.getWorkloadSite(), row,1);
//            this.tableModel.setValueAt(script.getProcessorSite(), row,2);
//            this.tableModel.setValueAt(script.getTaskToCore(), row,3);
//            this.tableModel.setValueAt(script.getDVFSMethod(), row,4);
//            this.tableModel.setValueAt(script.getSchedAlorithm(), row,5);
//            this.tableModel.setValueAt(script.getCCProtocol(), row,6);
//            this.tableModel.setValueAt(script.getSimulationTime(), row,7);
//        }
//    }
//    
//    public void removeRow(int row)
//    {
//        if(row != -1)
//        {
//            this.tableModel.removeRow(row);
//            
//            if(this.table.getRowCount()>0)
//            {
//                if(row <= this.table.getRowCount()-1)
//                {
//                    this.table.setRowSelectionInterval(row,row);
//                }
//                else
//                {
//                    this.table.setRowSelectionInterval(this.table.getRowCount()-1,this.table.getRowCount()-1);
//                }
//            }
//        }
    }
}

class TableModel extends DefaultTableModel
{
    String[] str = {"ScripID","Workload","Processor","TaskToCore","DVFSMethod","SchedAlorithm","CCProtocol","SimulationTime"};

    TableModel() 
    { 

    };
        
    public int getColumnCount() 
    { 
        return str.length; 
    } 

    public String getColumnName(int index) 
    { 
        return str[index]; 
    } 

    public boolean isCellEditable(int row, int col)
    {
        return false;
    }
}
