/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package userInterface.frontEnd;
import static RTSimulator.RTSimulator.println;

import PartitionAlgorithm.PartitionAlgorithm;
import SystemEnvironment.Core;
import concurrencyControlProtocol.ConcurrencyControlProtocol;
import dynamicVoltageAndFrequencyScalingMethod.DynamicVoltageAndFrequencyScalingMethod;
import java.lang.reflect.InvocationTargetException;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FileDialog;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import static javax.swing.JOptionPane.WARNING_MESSAGE;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import schedulingAlgorithm.PriorityDrivenSchedulingAlgorithm;
import SystemEnvironment.DataReader;
import SystemEnvironment.Simulator;
import WorkLoad.Task;
import WorkLoadSet.DataSetting;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Vector;
import RTSimulator.Definition;
import static RTSimulator.Definition.magnificationFactor;
import RTSimulator.RTSimulator;
import RTSimulator.RTSimulatorMath;
import org.apache.commons.io.IOUtils;
import scriptsetter.Script;
import scriptsetter.ScriptResult;
import scriptsetter.ScriptSetter;

import userInterface.UserInterface;
import userInterface.backEnd.TimeLineResult;
import workloadgenerator.WorkloadGenerator;

/**
 * Main viewer panel that controls simulation setup, execution and result
 * exporting within the front-end UI.
 * <p>
 * This class provides a set of UI controls to load workload and processor
 * definition files, select algorithms (partition, scheduling, DVFS,
 * synchronization protocol), configure simulation time and cost values,
 * start the simulator and draw or export the timeline results. It also
 * provides helper methods for loading algorithm implementations by name
 * via reflection and for querying the active simulation state.
 * </p>
 * <p>
 * Notes from inline comments: the viewer initializes a script setter,
 * provides a workload generator button, ensures the viewer starts with a
 * minimum width (400px) and offers a small UI to control that minimum
 * width at runtime. The class displays a temporary popup while scheduling
 * is in progress (labelled "排程中...").
 * </p>
 *
 * @author ShiuJia
 */
public class SimulationViewer extends JPanel
{        
    /**
     * Owning top-level UserInterface instance.
     * <p>
     * Used throughout the viewer to access the parent frame, InfoWin and
     * other shared UI components. This field is intentionally public so
     * other front-end components can reference the viewer directly.
     * </p>
     */
    public UserInterface parent;
    private JButton startBtn, exportBtn, drawBtn;
    private JButton workloadGeneratorBtn;
    private JButton scriptSetterBtn;
    private JButton readSourceFileBtn;
    private JButton readEnvironmentFileBtn;
    private JTextField workloadTextField;
    private JTextField processorTextField;
    private JTextField simTimeField;
    private JTextField accuracyField;
    private JTextField contextSwitchCost;
    private JTextField migrationCost;
    // New: UI to control SimulationViewer minimum width
    private JTextField simMinWidthField;
    private JButton simMinWidthApplyBtn;
    private JComboBox<String> partitionComboBox;
    private JComboBox<String> schedulingComboBox;
    private JComboBox<String> CCPComboBox;
    private JComboBox<String> DVFSComboBox;
    private JFrame popupWin;
    private ButtonGroup simTimeBG;
    private JRadioButton lcmRB, customRB;
    private DataReader dr;
    private Simulator sim;
    private SimulationResultPopupWin experimentResultPopupWin;
    private ScriptSetter scriptSetter;
    
    /**
     * Create a SimulationViewer attached to the provided parent UI.
     * <p>
     * The constructor initializes UI widgets (via {@code initialize()}),
     * hooks up listeners for file loading, script setter, workload generator,
     * start/draw/export actions and applies a minimum width constraint
     * (ensuring at least 400px as noted in inline comments).
     * </p>
     *
     * @param ui the top-level UserInterface that contains this viewer
     */
    public SimulationViewer(UserInterface ui)
    {
        super();
        this.parent = ui;
        this.initialize();
        // Ensure SimulationViewer starts with a minimum width of 400px
        this.setMinimumSize(new java.awt.Dimension(400, this.getMinimumSize() != null ? this.getMinimumSize().height : 200));
        this.scriptSetter = new ScriptSetter(SimulationViewer.this);
        
        this.workloadGeneratorBtn.addMouseListener
        (new MouseAdapter()
            {
                public void mouseClicked(MouseEvent e)
                {
                    new WorkloadGenerator(SimulationViewer.this);
                }
            }
        );
        
        this.scriptSetterBtn.addMouseListener
        (new MouseAdapter()
            {
                public void mouseClicked(MouseEvent e)
                {
                    scriptSetter.setVisible(!scriptSetter.isVisible());
                }
            }
        );
        
        this.readSourceFileBtn.addMouseListener
        (new MouseAdapter()
            {
                public void mouseClicked(MouseEvent e)
                {
                    FileDialog fileDialog = new FileDialog(parent.getFrame(), "new", FileDialog.LOAD);
                    fileDialog.setVisible(true);
                    if(fileDialog.getDirectory() != null)
                    {
                        workloadTextField.setText(fileDialog.getDirectory() + fileDialog.getFile());
                    }
                }
            }
        );
        
        this.readEnvironmentFileBtn.addMouseListener
        (new MouseAdapter()
            {
                public void mouseClicked(MouseEvent e)
                {
                    FileDialog fileDialog = new FileDialog(parent.getFrame(), "new", FileDialog.LOAD);
                    fileDialog.setVisible(true);
                    if(fileDialog.getDirectory() != null)
                    {
                        processorTextField.setText(fileDialog.getDirectory() + fileDialog.getFile());
                    }
                }
            }
        ); 
        
        this.lcmRB.addItemListener
        (
            new ItemListener()
            {
                @Override
                public void itemStateChanged(ItemEvent e)
                {
                    simTimeField.setEditable(false);
                }
            }
        );
        
        this.customRB.addItemListener
        (
            new ItemListener()
            {
                @Override
                public void itemStateChanged(ItemEvent e)
                {
                    simTimeField.setEditable(true);
                }
            }
        );
        
        
        this.startBtn.addMouseListener
        (new MouseAdapter()
            {
                public void mousePressed(MouseEvent e)
                {
                   popupWin.setVisible(true);
                   if(experimentResultPopupWin != null)
                   {
                       experimentResultPopupWin.setVisible(false);
                   }
                }
                
                public void mouseReleased(MouseEvent e)
                {
                    try
                    {
                        setMagnificationFactor(accuracyField.getText());
                        
                        dr = new DataReader();
                        dr.loadSource(workloadTextField.getText());
                        dr.loadSource(processorTextField.getText());
//                        sim = new Simulator(SimulationViewer.this);
                        sim = new Simulator();
                        sim.setSimulationTime(getSimulationTime());
                        sim.setContextSwitchTime(getContextSwitchTime());
                        sim.setMigrationTime(getMigrationTime());
                        sim.loadDataSetting(dr.getDataSetting());
                        
                        sim.getProcessor().setSchedAlgorithm(getPrioritySchedulingAlgorithm(schedulingComboBox.getSelectedItem().toString()));
                        sim.getProcessor().setPartitionAlgorithm(getPartitionAlgorithm(partitionComboBox.getSelectedItem().toString()));
                        sim.getProcessor().setCCProtocol(getConcurrencyControlProtocol(CCPComboBox.getSelectedItem().toString()));
                        sim.getProcessor().setDVFSMethod(getDynamicVoltageScalingMethod(DVFSComboBox.getSelectedItem().toString()));
                        
                        dr.getDataSetting().getProcessor().showInfo();
                        println("Workload:" + dr.getDataSetting().getTaskSet().getProcessingSpeed());
                        for(Task t : dr.getDataSetting().getTaskSet())
                        {
                            t.showInfo();
                        }
                        
                        sim.start();
                        
                        experimentResultPopupWin = new SimulationResultPopupWin(SimulationViewer.this);
                    }
                    catch (Exception ex) 
                    {
                        popupWin.dispose();
                        JOptionPane.showMessageDialog(parent.getFrame(), "Error!!" ,"Error!!" ,WARNING_MESSAGE);
                        Logger.getLogger(SimulationViewer.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    
                    popupWin.dispose();
                }
            }
        );

        this.drawBtn.addMouseListener
        (new MouseAdapter()
            {
                public void mouseClicked(MouseEvent e)
                {
                    //SimulationViewer.this.timeLineBtn.setForeground(Color.red);
                    SimulationViewer.this.parent.getInfoWin().pressDrawButton();
                }
            }
        );

        this.exportBtn.addMouseListener
        (new MouseAdapter()
            {
                public void mouseClicked(MouseEvent e)
                {
                    TimeLineResult panel = SimulationViewer.this.parent.getInfoWin().getCurCoreResult().getTimeLineResult();
                    BufferedImage image = new BufferedImage(panel.getWidth(), panel.getHeight(), BufferedImage.TYPE_INT_RGB);
                    panel.paint(image.createGraphics());
                    
                    FileDialog fileDialog = new FileDialog(parent.getFrame(), "new", FileDialog.SAVE);
                    fileDialog.setVisible(true);
                    try 
                    {
                        ImageIO.write(image,"jpg", new File(fileDialog.getDirectory()+fileDialog.getFile()+".jpg"));
                    } 
                    catch (IOException ex) 
                    {
                        Logger.getLogger(SimulationViewer.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }

            }
        );
        
        // Hook up Apply for min width control
        this.simMinWidthApplyBtn.addMouseListener
        (new MouseAdapter()
            {
                public void mouseClicked(MouseEvent e)
                {
                    try
                    {
                        int px = Integer.parseInt(simMinWidthField.getText().trim());
                        if(px < 400) {
                            JOptionPane.showMessageDialog(parent.getFrame(), "Minimum allowed is 400px. Setting to 400.");
                            px = 400;
                        }
                        SimulationViewer.this.parent.setSimulationViewerMinimumWidth(px);
                    }
                    catch (Exception ex)
                    {
                        JOptionPane.showMessageDialog(parent.getFrame(), "Please enter a valid integer for min width.");
                    }
                }
            }
        );
        
        popupWin = new JFrame("排程中...");
        popupWin.setBounds(parent.getFrame().getX()+parent.getFrame().getWidth()/2 -100,
                            parent.getFrame().getY()+parent.getFrame().getHeight()/2 -100, 100, 100);
        popupWin.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        popupWin.setLayout(new BorderLayout());
        popupWin.add(new JLabel("排程中..."),BorderLayout.CENTER);
        //popupWin.setVisible(true);
    }
    
    private void initialize()
    {
        this.setLayout(new BorderLayout());
        JToolBar jt = new JToolBar();
        jt.setFloatable(false);
        this.add(jt,BorderLayout.NORTH);
        jt.setLayout(new GridBagLayout());
        GridBagConstraints d = new GridBagConstraints();
        
        d.fill = GridBagConstraints.HORIZONTAL;
        d.weightx = 0.5;
        d.ipady = 0;
        d.gridx = 0;
        d.gridy = 0;
        
        JToolBar dataToolBar = new JToolBar();
        dataToolBar.setFloatable(false);
        JLabel dataLabel = new JLabel("Workload: ");
        this.workloadGeneratorBtn = new JButton("Generator");
        this.readSourceFileBtn = new JButton("Open File...");
        this.workloadTextField = new JTextField();
        
        jt.add(dataToolBar,d);
        dataToolBar.setLayout(new GridBagLayout());
        {
            GridBagConstraints c = new GridBagConstraints();
            c.weightx = 0.5;
            c.fill = GridBagConstraints.HORIZONTAL;
            c.anchor = GridBagConstraints.WEST;
            c.gridx = 0;
            c.gridy = 0;
            dataToolBar.add(dataLabel,c);
            c.gridx = 0;
            c.gridy = 1;
            JToolBar toolBar2 = new JToolBar();
            toolBar2.setFloatable(false);
            toolBar2.add(this.readSourceFileBtn);
            toolBar2.add(this.workloadGeneratorBtn);
            toolBar2.add(this.workloadTextField);
            
            dataToolBar.add(toolBar2,c);
        }
        //----
        JToolBar processorToolBar = new JToolBar();
        processorToolBar.setFloatable(false);
        JLabel environmentLabel = new JLabel("Processor： ");
        this.readEnvironmentFileBtn = new JButton("Open File...");
        this.processorTextField = new JTextField();
        
        d.gridx = 0;
        d.gridy = 1;
        jt.add(processorToolBar,d);
        processorToolBar.setLayout(new GridBagLayout());
        {
            GridBagConstraints c = new GridBagConstraints();
            c.weightx = 0.5;
            c.fill = GridBagConstraints.HORIZONTAL;
            c.anchor = GridBagConstraints.WEST;
            c.gridx = 0;
            c.gridy = 0;
            processorToolBar.add(environmentLabel,c);
            
            c.gridx = 0;
            c.gridy = 1;
            JToolBar toolBar2 = new JToolBar();
            toolBar2.setFloatable(false);
            toolBar2.add(this.readEnvironmentFileBtn);
            toolBar2.add(this.processorTextField);
            processorToolBar.add(toolBar2,c);
        }
        
        //----
        JToolBar configToolBar = new JToolBar();
        configToolBar.setFloatable(false);
        d.gridx = 0;
        d.gridy = 2;
        jt.add(configToolBar,d);
        
        JLabel taskToCoreLabel = new JLabel("PartitionAlgorithm: ");
        this.partitionComboBox = new JComboBox<String>();
        // reduce combo box width so the panel doesn't expand too much
        this.partitionComboBox.setPreferredSize(new java.awt.Dimension(140, this.partitionComboBox.getPreferredSize().height));
        
        JLabel energyLabel = new JLabel("DVFSMethod: ");
        this.DVFSComboBox = new JComboBox<String>();
        this.DVFSComboBox.setPreferredSize(new java.awt.Dimension(140, this.DVFSComboBox.getPreferredSize().height));
        
        JLabel schedulerLabel = new JLabel("SchedulingAlgorithm: ");
        this.schedulingComboBox = new JComboBox<String>();
        this.schedulingComboBox.setPreferredSize(new java.awt.Dimension(140, this.schedulingComboBox.getPreferredSize().height));

        JLabel controllerLabel = new JLabel("SynchronizationProtocol:");
        this.CCPComboBox = new JComboBox<String>();
        this.CCPComboBox.setPreferredSize(new java.awt.Dimension(140, this.CCPComboBox.getPreferredSize().height));

        
        this.setComboBox();
        
        this.CCPComboBox.setSelectedItem("None");
        this.partitionComboBox.setSelectedItem("None");
        this.DVFSComboBox.setSelectedItem("None");
        
        configToolBar.setLayout(new GridBagLayout());
        {
            GridBagConstraints c = new GridBagConstraints();
            c.weightx = 0.5;
            c.fill = GridBagConstraints.HORIZONTAL;
            c.gridx = 0;
            c.gridy = 0;
            configToolBar.add(taskToCoreLabel,c);
            c.gridy+=1;
            configToolBar.add(energyLabel,c);
            c.gridy+=1;
            configToolBar.add(schedulerLabel,c);
            c.gridy+=1;
            configToolBar.add(controllerLabel,c);
            c.gridx = 1;
            c.gridy = 0;
            configToolBar.add(this.partitionComboBox,c);
            c.gridy+=1;
            configToolBar.add(this.DVFSComboBox,c);
            c.gridy+=1;
            configToolBar.add(this.schedulingComboBox,c);
            c.gridy+=1;
            configToolBar.add(this.CCPComboBox,c);
        }
        //-----
        JToolBar simTimeToolBar = new JToolBar();
        simTimeToolBar.setFloatable(false);
        JLabel simTimeLabel = new JLabel("SimulationTime: ");
        this.lcmRB = new JRadioButton();
        this.lcmRB.setText("Lcm of Period for TaskSet");
        this.lcmRB.setSelected(true);
        this.customRB = new JRadioButton();
        this.customRB.setText("Custom Time");
        this.simTimeBG = new ButtonGroup();
        this.simTimeBG.add(this.lcmRB);
        this.simTimeBG.add(this.customRB);
        this.simTimeField = new JTextField();
        this.simTimeField.setEditable(false);

        
        simTimeToolBar.setLayout(new GridBagLayout());
        {
            GridBagConstraints c = new GridBagConstraints();
            c.fill = GridBagConstraints.HORIZONTAL;
            c.anchor = GridBagConstraints.WEST;
            c.weightx = 100;
            c.gridx = 0;
            c.gridy = 0;
            
            simTimeToolBar.add(simTimeLabel,c);
            c.gridx = 0;
            c.gridy = 1;
            JToolBar toolBar1 = new JToolBar();
            toolBar1.add(this.lcmRB);
            toolBar1.setFloatable(false);
            simTimeToolBar.add(toolBar1,c);
            
            c.gridx = 0;
            c.gridy = 2;
            JToolBar toolBar2 = new JToolBar();
            toolBar2.setFloatable(false);
            toolBar2.add(this.customRB);
            toolBar2.add(this.simTimeField);
            
            simTimeToolBar.add(toolBar2,c);
        }
        
        d.gridx = 0;
        d.gridy = 3;
        jt.add(simTimeToolBar,d);
        
        //-----
        
        JToolBar costToolBar = new JToolBar();
        costToolBar.setFloatable(false);
        d.gridx = 0;
        d.gridy = 4;
        jt.add(costToolBar,d);
        
        
        JLabel contextSwitchCostLabel = new JLabel("ContextSwitchCost:");
        this.contextSwitchCost = new JTextField();
        JLabel migrationCostLabel = new JLabel("MigrationCost:");
        this.migrationCost = new JTextField();
        
        JLabel accuracyLabel = new JLabel("Accuracy:");
        this.accuracyField = new JTextField();
        
        costToolBar.setLayout(new GridBagLayout());
        {
            GridBagConstraints c = new GridBagConstraints();
            c.fill = GridBagConstraints.HORIZONTAL;
            c.anchor = GridBagConstraints.WEST;
            c.weightx = 100;
            c.gridx = 0;
            c.gridy = 0;
            JToolBar toolBar1 = new JToolBar();
            toolBar1.setFloatable(false);
            toolBar1.add(contextSwitchCostLabel,c);
            toolBar1.add(this.contextSwitchCost);
            costToolBar.add(toolBar1,c);
            
            c.gridx = 0;
            c.gridy = 1;
            JToolBar toolBar2 = new JToolBar();
            toolBar2.setFloatable(false);
            toolBar2.add(migrationCostLabel,c);
            toolBar2.add(this.migrationCost);
            costToolBar.add(toolBar2,c);
            
            c.gridx = 0;
            c.gridy = 2;
            JToolBar toolBar3 = new JToolBar();
            toolBar3.setFloatable(false);
            toolBar3.add(accuracyLabel,c);
            toolBar3.add(this.accuracyField);
            costToolBar.add(toolBar3,c);
        }
        
        //--- View settings: SimulationViewer min width control
        JToolBar viewToolBar = new JToolBar();
        viewToolBar.setFloatable(false);
        d.gridx = 0;
        d.gridy = 5;
        jt.add(viewToolBar, d);
        viewToolBar.setLayout(new GridBagLayout());
        {
            GridBagConstraints c = new GridBagConstraints();
            c.fill = GridBagConstraints.HORIZONTAL;
            c.anchor = GridBagConstraints.WEST;
            c.weightx = 100;
            c.gridx = 0;
            c.gridy = 0;
            // 測試前端的伸縮功能需要多少px
            JLabel minLabel = new JLabel("Sim Min Width (px): ");
            this.simMinWidthField = new JTextField("400");
            this.simMinWidthApplyBtn = new JButton("Apply");
            JToolBar inner = new JToolBar();
            inner.setFloatable(false);
            inner.add(minLabel);
            inner.add(this.simMinWidthField);
            inner.add(this.simMinWidthApplyBtn);
            viewToolBar.add(inner, c);
        }
        
        //-----
        
        JToolBar scheduleToolBar = new JToolBar();
        scheduleToolBar.setLayout(new GridLayout(1,3));
        
        this.add(scheduleToolBar,BorderLayout.SOUTH);

        this.startBtn = new JButton("Start");
        scheduleToolBar.add(this.startBtn);
        this.startBtn.setForeground(Color.red);

        this.drawBtn = new JButton("Draw");
        scheduleToolBar.add(this.drawBtn);
        this.drawBtn.setForeground(Color.BLUE);

        this.exportBtn = new JButton("Export");
        scheduleToolBar.add(this.exportBtn);
        
        this.scriptSetterBtn = new JButton("Script");
        scheduleToolBar.add(this.scriptSetterBtn);
        
        popupWin = new JFrame("排程中...");
        popupWin.setBounds(parent.getFrame().getX()+parent.getFrame().getWidth()/2 -100,
                            parent.getFrame().getY()+parent.getFrame().getHeight()/2 -100, 100, 100);
        popupWin.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        popupWin.setLayout(new BorderLayout());
        popupWin.add(new JLabel("排程中..."),BorderLayout.CENTER);
        //popupWin.setVisible(true);
    }
    
    private void setComboBox()
    {
        
        String path = RTSimulator.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        println("" + RTSimulator.class.getProtectionDomain().getCodeSource().getLocation().getPath());
        //IDE : /Users/YC/Documents/LabResearch/RTSimulator/RTSimulatorV2/RTSimulator/build/classes/
        //jar : /Users/YC/Documents/LabResearch/RTSimulator/RTSimulatorV2/RTSimulator/dist/RTSimulator2.0.jar
        
        
        InputStream is = RTSimulator.class.getClass().getResourceAsStream("/AlgorithmName/algorithmName");
        
        if(path.contains("jar"))//路徑包含jar代表是執行jar檔的情況
        {
            try 
            {
                String fileContent = IOUtils.toString(is, "UTF-8");
                String[] algorithmType = fileContent.split("\r|\n");
                //println(name[0]);

                for(int i = 0 ; i<algorithmType.length ; i++)
                {
                    String[] algorithmName = algorithmType[i].split(",");
                    for(int j = 1; j<algorithmName.length ; j++)
                    {
                        switch(algorithmName[0])
                        {
                            case "PartitionAlgorithm":
                                this.partitionComboBox.addItem(algorithmName[j]);
                            break;

                            case "DVFSMethod":
                                this.DVFSComboBox.addItem(algorithmName[j]);
                            break;

                            case "SchedulingAlgorithm":
                                this.schedulingComboBox.addItem(algorithmName[j]);
                            break;

                            case "CCProtocol":
                                this.CCPComboBox.addItem(algorithmName[j]);
                            break;
                            default:
                        }
                    }
                }

            } 
            catch (IOException ex) 
            {
                Logger.getLogger(RTSimulator.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        else
        {
            try 
            {
                String dirPath = System.getProperties().getProperty("user.dir");  
                System.out.println("dirPath: "+dirPath);
                                                          //資料夾路徑                      創建的檔名
                PrintStream out = new PrintStream(new File(dirPath+"/src/AlgorithmName/"+"algorithmName"));
                String str = "";
                
                Vector<String> fileName = new Vector<String>();
                
                str += "PartitionAlgorithm";
                fileName = this.getFolderFile(dirPath+"/src/PartitionAlgorithm/implementation");
                for(int i = 0; i < fileName.size(); i++)
                {
                    str += ","+fileName.get(i);
                    this.partitionComboBox.addItem(fileName.get(i));
                }
                out.println(str);
                str="";
                
                str += "DVFSMethod";
                fileName = this.getFolderFile(dirPath+"/src/dynamicVoltageAndFrequencyScalingMethod/implementation");
                for(int i = 0; i < fileName.size(); i++)
                {
                    str += ","+fileName.get(i);
                    this.DVFSComboBox.addItem(fileName.get(i));
                }
                out.println(str);
                str="";

                str += "SchedulingAlgorithm";
                fileName = this.getFolderFile(dirPath+"/src/schedulingAlgorithm/implementation");
                for(int i = 0; i < fileName.size(); i++)
                {
                    str += ","+fileName.get(i);
                    this.schedulingComboBox.addItem(fileName.get(i));
                }  
                out.println(str);
                str="";

                str += "CCProtocol";
                fileName = this.getFolderFile(dirPath+"/src/concurrencyControlProtocol/implementation");
                for(int i = 0; i < fileName.size(); i++)
                {
                    str += ","+fileName.get(i);
                    this.CCPComboBox.addItem(fileName.get(i));
                }
                out.println(str);
                str="";

                out.close();
            }
            catch (FileNotFoundException ex) 
            {
                Logger.getLogger(SimulationViewer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    /**
     * Return the DataReader used by this SimulationViewer.
     *
     * @return the DataReader instance used to load workload and processor data
     */
    public DataReader getDataReader()
    {
        return this.dr;
    }
    
    /**
     * Return the list of file base names (no extension) located in the given
     * directory path.
     * <p>
     * The method filters out inner-class/anonymous class entries (containing
     * '$') and empty names. It is used to populate algorithm lists for the
     * UI when running from a development directory (non-jar case).
     * </p>
     *
     * @param path the directory path to scan for files
     * @return a Vector of base filenames (without extensions)
     */
    public Vector<String> getFolderFile(String path)
    {
        Vector<String> fileName = new Vector<String>();
        File folder = new File(path);
        String[] list = folder.list();
        
        for(int i = 0; i < list.length; i++)
        {
            String name = list[i].split("\\.")[0];
            
            if(name != null && !name.contains("$") && !name.isEmpty())
            {
                fileName.add(name);
            }
        }
        return fileName;
    }
    
    /**
     * Instantiate a PartitionAlgorithm implementation by class name using
     * reflection.
     *
     * @param name the simple class name of the partition algorithm implementation
     * @return a new instance of the requested PartitionAlgorithm
     * @throws ClassNotFoundException if the implementation class cannot be found
     * @throws NoSuchMethodException if the no-arg constructor is not available
     * @throws InvocationTargetException if constructor invocation fails
     * @throws InstantiationException if the class represents an abstract class
     * @throws IllegalAccessException if the constructor is not accessible
     */
    public PartitionAlgorithm getPartitionAlgorithm(String name)
            throws ClassNotFoundException, NoSuchMethodException,
                   InvocationTargetException, InstantiationException, IllegalAccessException {
        return (PartitionAlgorithm) Class
                .forName("PartitionAlgorithm.implementation." + name)
                .getDeclaredConstructor()
                .newInstance();
    }

    /**
     * Instantiate a PriorityDrivenSchedulingAlgorithm implementation by name
     * using reflection.
     *
     * @param name the simple class name of the scheduling algorithm implementation
     * @return a new instance of the requested PriorityDrivenSchedulingAlgorithm
     * @throws ClassNotFoundException if the implementation class cannot be found
     * @throws NoSuchMethodException if the no-arg constructor is not available
     * @throws InvocationTargetException if constructor invocation fails
     * @throws InstantiationException if the class represents an abstract class
     * @throws IllegalAccessException if the constructor is not accessible
     */
    public PriorityDrivenSchedulingAlgorithm getPrioritySchedulingAlgorithm(String name)
            throws ClassNotFoundException, NoSuchMethodException,
                   InvocationTargetException, InstantiationException, IllegalAccessException {
        return (PriorityDrivenSchedulingAlgorithm) Class
                .forName("schedulingAlgorithm.implementation." + name)
                .getDeclaredConstructor()
                .newInstance();
    }

    /**
     * Instantiate a DVFS method implementation by class name via reflection.
     *
     * @param name the simple class name of the DVFS method implementation
     * @return a new instance of the requested DynamicVoltageAndFrequencyScalingMethod
     * @throws ClassNotFoundException if the implementation class cannot be found
     * @throws NoSuchMethodException if the no-arg constructor is not available
     * @throws InvocationTargetException if constructor invocation fails
     * @throws InstantiationException if the class represents an abstract class
     * @throws IllegalAccessException if the constructor is not accessible
     */
    public DynamicVoltageAndFrequencyScalingMethod getDynamicVoltageScalingMethod(String name)
            throws ClassNotFoundException, NoSuchMethodException,
                   InvocationTargetException, InstantiationException, IllegalAccessException {
        return (DynamicVoltageAndFrequencyScalingMethod) Class
                .forName("dynamicVoltageAndFrequencyScalingMethod.implementation." + name)
                .getDeclaredConstructor()
                .newInstance();
    }

    /**
     * Instantiate a concurrency control protocol implementation by name via
     * reflection.
     *
     * @param name the simple class name of the concurrency control implementation
     * @return a new instance of the requested ConcurrencyControlProtocol
     * @throws ClassNotFoundException if the implementation class cannot be found
     * @throws NoSuchMethodException if the no-arg constructor is not available
     * @throws InvocationTargetException if constructor invocation fails
     * @throws InstantiationException if the class represents an abstract class
     * @throws IllegalAccessException if the constructor is not accessible
     */
    public ConcurrencyControlProtocol getConcurrencyControlProtocol(String name)
            throws ClassNotFoundException, NoSuchMethodException,
                   InvocationTargetException, InstantiationException, IllegalAccessException {
        return (ConcurrencyControlProtocol) Class
                .forName("concurrencyControlProtocol.implementation." + name)
                .getDeclaredConstructor()
                .newInstance();
    }
    
    /**
     * Compute the simulation time used for the Simulator.
     * <p>
     * If the "Lcm of Period for TaskSet" radio button is selected the method
     * returns the task set's schedule time as provided by the DataReader.
     * If the custom radio button is selected the method parses the custom
     * simulation time text field and applies the magnification factor.
     * </p>
     *
     * @return the simulation time in internal time units (scaled by magnification)
     */
    public long getSimulationTime()
    {
        if(this.lcmRB.isSelected())
        {
            return this.dr.getDataSetting().getTaskSet().getScheduleTimeForTaskSet();
        }
        else if(this.customRB.isSelected())
        {
            return  Double.valueOf(
                        Double.valueOf(
                            this.simTimeField.getText().toString()
                        )*magnificationFactor
                    ).longValue();
        }
        return 0;
    }
    
    private long getContextSwitchTime()
    {
        RTSimulatorMath math = new RTSimulatorMath();
        
        double time = 0;
        try
        {
            time = Double.valueOf(this.contextSwitchCost.getText());
            if(time < 0)
            {
                time = 0;
            }
        }
        catch (Exception ex) 
        {
        }
        return (long)(time*magnificationFactor);
    }
    
    private long getMigrationTime()
    {
        RTSimulatorMath math = new RTSimulatorMath();
        double time = 0;
        try
        {
            time = Double.valueOf(this.migrationCost.getText());
            if(time < 0)
            {
                time = 0;
            }
        }
        catch (Exception ex) 
        {
        }
        
        return (long)(time * magnificationFactor);
    }
    
    /**
     * Return the simulator instance started by this viewer, or null if none.
     *
     * @return the Simulator instance used/executed by this viewer
     */
    public Simulator getSimulator()
    {
        return this.sim;
    }
    
    /**
     * Return the source workload text field control used by the UI.
     *
     * @return the JTextField containing the currently selected workload path
     */
    public JTextField getSourceTextField()
    {
        return this.workloadTextField;
    }
    
    /**
     * Return the popup window that displays aggregate simulation results.
     *
     * @return the SimulationResultPopupWin instance or null if not created
     */
    public SimulationResultPopupWin getTotalDataPopupWin()
    {
        return this.experimentResultPopupWin;
    }
    
    /**
     * Return the partition algorithm selection combo box used in the UI.
     *
     * @return the JComboBox containing partition algorithm names
     */
    public JComboBox<String> getPartitionComboBox()
    {
        return this.partitionComboBox;
    }
    /**
     * Return the scheduling algorithm selection combo box used in the UI.
     *
     * @return the JComboBox containing scheduling algorithm names
     */
    public JComboBox<String> getSchedulingComboBox()
    {
        return this.schedulingComboBox;
    }
    /**
     * Return the synchronization (CCP) selection combo box used in the UI.
     *
     * @return the JComboBox containing concurrency control protocol names
     */
    public JComboBox<String> getCCPComboBox()
    {
        return this.CCPComboBox;
    }
    /**
     * Return the DVFS method selection combo box used in the UI.
     *
     * @return the JComboBox containing DVFS method names
     */
    public JComboBox<String> getDVFSComboBox()
    {
        return this.DVFSComboBox;
    }
    
    /**
     * Parse a magnification/accuracy specification and update the global
     * Definition.magnificationFactor and magnificationFormat.
     * <p>
     * The method reads an integer exponent from the string, clamps negative
     * values to zero, computes 10^exponent for the magnification factor and
     * constructs a simple format string (e.g. "##.00" for exponent=2).
     * </p>
     *
     * @param str the string representing the magnification exponent (e.g. "2")
     */
    public void setMagnificationFactor(String str)
    {
        String s = "##";

        int magnificationFactor = 5;
        
        try
        {
            magnificationFactor = Integer.valueOf(str);
            if(magnificationFactor < 0)
            {
                magnificationFactor = 0;
            }
        }
        catch (Exception ex) 
        {
            
        }
    
        
        Definition.magnificationFactor = (long)Math.pow(10, magnificationFactor);

        s += ".";
        for(int i = 0 ; i < magnificationFactor ; i++)
        {
            s += "0";
        }
        Definition.magnificationFormat = s;
    }
}
