/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package scriptsetter;
import static RTSimulator.RTSimulator.println;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import userInterface.frontEnd.SimulationViewer;


/**
 * Window and controller for creating and running script groups of
 * simulations.
 *
 * <p>This frame provides a UI to assemble one or more groups of scripts
 * (each group is represented by a {@link ScriptTable}) and a configuration
 * panel ({@link ScriptPanel}) to set workload/processor files and algorithm
 * choices. The class manages group/tab creation and removal, starts
 * concurrent simulation runs, collects results and writes them out as XML.
 *
 * <p>Notes from the original source (translated and integrated):
 * - The {@link #tableTabbedPane} is the tabbed pane used to switch between
 *   script tables ("切換scriptTable的標籤頁面物件").
 * - When the first ScriptTable is created it receives a default group ID
 *   of "1" ("預設第一個scriptTable之ID;"). The split panes are set up so
 *   the top/bottom and left/right orientations match the original layout
 *   (comments: "上下 切割方法", "左右 切割方法").</p>
 *
 * @author YC
 */
public class ScriptSetter extends JFrame
{
    private static final long serialVersionUID = 1L;

    /**
     * Reference to the parent {@link SimulationViewer} UI that contains this
     * frame. The parent is used to access helper methods such as
     * {@code getFolderFile(...)} and to propagate UI state (for example the
     * magnification/accuracy setting).
     */
	public SimulationViewer parent;
    private JSplitPane splitPane;
    private JSplitPane bottomSplitPane ;
    private ScriptPanel scriptPanel;

    /**
     * Tabbed pane used to switch between ScriptTable pages. Each tab holds a
     * {@link ScriptTable} representing a group of scripts.
     */
    private JTabbedPane tableTabbedPane = new JTabbedPane(); // 切換scriptTable的標籤頁面物件
   // private ScriptTable scriptTable;
    private SciptToolBar sciptToolBar;
   // private Vector<Script> scriptSet;
    
    /**
     * Creates a new ScriptSetter window attached to the given
     * {@link SimulationViewer}.
     *
     * @param SV the parent simulation viewer that hosts this frame
     */
    public ScriptSetter(SimulationViewer SV)
    {
        this.parent = SV;
        this.init();
    //    this.scriptSet = new Vector();
        this.revalidate();
        
        
        this.tableTabbedPane.addContainerListener
        (
            new ContainerListener() 
            {

                @Override
                public void componentAdded(ContainerEvent e) 
                {
                    for(int i = 0; i<tableTabbedPane.getComponentCount() ; i++)
                    {
                        tableTabbedPane.setTitleAt(i, Integer.toString(i+1)+"("+((ScriptTable)tableTabbedPane.getComponent(i)).getScriptCount()+")");
                        
                        ((ScriptTable)tableTabbedPane.getComponent(i)).setGroupID(Integer.toString(i+1));
                    }
                    
                    
                    println("A"+tableTabbedPane.getComponentCount());
                }

                @Override
                public void componentRemoved(ContainerEvent e) 
                {
                    for(int i = 0; i<tableTabbedPane.getComponentCount() ; i++)
                    {
                        tableTabbedPane.setTitleAt(i, Integer.toString(i+1)+"("+((ScriptTable)tableTabbedPane.getComponent(i)).getScriptCount()+")");
                        
                        ((ScriptTable)tableTabbedPane.getComponent(i)).setGroupID(Integer.toString(i+1));
                    }
                    
                    println("B"+tableTabbedPane.getComponentCount());
                }
                
            }
        );
    }
    
    private void init() 
    {
        this.setTitle("Script Setter");
        this.setBounds(100, 100, 800, 585);
        this.setMinimumSize(new Dimension(800, 585));
        this.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        this.setVisible(false);
        this.setLayout(new BorderLayout()); 
        
        
        this.splitPane = new JSplitPane();
        this.add(splitPane,BorderLayout.CENTER);
        this.splitPane.setOrientation(0); //上下 切割方法
        this.splitPane.setContinuousLayout(false);//??
        this.splitPane.setDividerLocation(this.getHeight()/3);
        
        this.bottomSplitPane = new JSplitPane();
        this.bottomSplitPane.setOrientation(1);//左右 切割方法
        this.bottomSplitPane.setContinuousLayout(false);
        this.bottomSplitPane.setDividerLocation(this.getWidth()/2);
        this.splitPane.setBottomComponent(bottomSplitPane);
        
        this.splitPane.setTopComponent(this.tableTabbedPane);
        ScriptTable scriptTable = new ScriptTable(this);
        scriptTable.setGroupID("1");//預設第一個scriptTable之ID;
        this.tableTabbedPane.addTab("1(0)",scriptTable);
        
        this.scriptPanel = new ScriptPanel(this);
        this.bottomSplitPane.setTopComponent(this.scriptPanel);
        
        this.sciptToolBar = new SciptToolBar(this);
        this.bottomSplitPane.setBottomComponent(this.sciptToolBar);
        
        
    }
    
    /**
     * Returns the main split pane that divides the window vertically (top/bottom).
     *
     * @return the top-level {@link JSplitPane}
     */
    public JSplitPane getSplitPane()
    {
        return this.splitPane;
    }
    
    /**
     * Returns the bottom split pane that divides the lower area horizontally
     * (left/right).
     *
     * @return the bottom {@link JSplitPane}
     */
    public JSplitPane getBottomSplitPane()
    {
        return this.bottomSplitPane;
    }
    
    /**
     * Returns the tabbed pane that holds {@link ScriptTable} groups.
     *
     * @return the tabbed pane used to switch between script groups
     */
    public JTabbedPane getTableTabbedPane()
    {
        return this.tableTabbedPane;
    }
    
    /**
     * Returns the {@link ScriptTable} at the specified index.
     *
     * @param i zero-based index of the tab
     * @return the {@link ScriptTable} at index {@code i}
     * @throws ArrayIndexOutOfBoundsException if {@code i} is out of range
     */
    public ScriptTable getScriptTable(int i)
    {
        return (ScriptTable)this.tableTabbedPane.getComponent(i);
    }
    
    /**
     * Returns the configuration panel used to edit script fields (workload,
     * processor, algorithm choices, etc.).
     *
     * @return the {@link ScriptPanel} instance used by this frame
     */
    public ScriptPanel getScriptPanel()
    {
        return this.scriptPanel;
    }
    
    /**
     * Returns the toolbar that contains script action controls.
     *
     * @return the {@link SciptToolBar} instance
     */
    public SciptToolBar getSciptToolBar()
    {
        return this.sciptToolBar;
    }
    
    /**
     * Returns the current accuracy/magnification setting as text. This value
     * is forwarded from the {@link ScriptPanel}.
     *
     * @return the accuracy string entered in the script panel
     */
    public String getAccuracy()
    {
        return this.scriptPanel.getAccuracy();
    }
    
    /**
     * Returns the number of concurrent threads to use when running scripts.
     *
     * <p>If the value in the UI is null or invalid (not a positive integer)
     * this method returns the default value of {@code 1}.</p>
     *
     * @return the thread amount parsed from the script panel or 1 if not set
     */
    public int getThreadAmount()
    {
        if(this.scriptPanel.getThreadAmount() != null && Integer.valueOf(this.scriptPanel.getThreadAmount()) > 0)
        {
            System.out.println("ThreadAmount = "+ Integer.valueOf(this.scriptPanel.getThreadAmount()));
            return Integer.valueOf(this.scriptPanel.getThreadAmount());
        }
        else
        {
            return 1;
        }
    }
    
    /**
     * Adds a new empty script group by appending a new {@link ScriptTable}
     * as a tab in the tabbed pane.
     */
    public void addGroup()
    {
        this.tableTabbedPane.addTab(Integer.toString(this.tableTabbedPane.getComponentCount()+1), new ScriptTable(this));
    }
    
    /**
     * Removes the currently selected script group tab if any exist.
     */
    public void removeGroup()
    {
        if(this.tableTabbedPane.getComponentCount()>0)
        {
            this.tableTabbedPane.remove(this.tableTabbedPane.getSelectedIndex());
        }
    }
    
    /**
     * Adds a new script to the currently selected {@link ScriptTable} using the
     * values from the {@link ScriptPanel}.
     *
     * If no tab is present this method does nothing.
     */
    public void addScript()
    {
        if(this.tableTabbedPane.getComponentCount()>0)
        {
            ScriptTable st = (ScriptTable)this.tableTabbedPane.getSelectedComponent();
            st.addScript(this.scriptPanel);
            this.tableTabbedPane.setTitleAt(Integer.valueOf(st.getGroupID())-1,st.getGroupID() + "("+st.getScriptCount()+")");
        }
    }
    
    /**
     * Modifies the currently selected script in the active {@link ScriptTable}
     * using values from the {@link ScriptPanel}.
     */
    public void modifyScript()
    {
        ((ScriptTable)this.tableTabbedPane.getSelectedComponent()).modifyScript(this.scriptPanel);
    }
    
    /**
     * Removes the currently selected script from the active {@link ScriptTable}
     * if any scripts exist in that table.
     */
    public void removeScript()
    {
        if(this.tableTabbedPane.getComponentCount()>0 && ((ScriptTable)this.tableTabbedPane.getSelectedComponent()).getScriptCount()>0)
        {
            ScriptTable st = (ScriptTable)this.tableTabbedPane.getSelectedComponent();
            st.removeScript();
            this.tableTabbedPane.setTitleAt(Integer.valueOf(st.getGroupID())-1,st.getGroupID() + "("+st.getScriptCount()+")");
        }
    }
    
    /**
     * Starts all configured scripts in all groups concurrently and writes the
     * collected results to an XML file chosen by the user via a save dialog.
     *
     * <p>Behavior summary (integrated from inline comments):
     * <ol>
     *   <li>Show a {@link FileDialog} save dialog; if the user cancels, no
     *       action is taken ("如果取消存檔則fileDialog.getFile()會是null").</li>
     *   <li>Set the global magnification factor using the current accuracy
     *       value from the UI.</li>
     *   <li>Create a fixed thread pool sized by {@link #getThreadAmount()} and
     *       submit one {@link ScriptRunnable} task per workload file for each
     *       script in every group. The workload filenames are obtained via
     *       {@link SimulationViewer#getFolderFile(String)}.</li>
     *   <li>Await termination of all tasks. The implementation polls
     *       {@link ExecutorService#awaitTermination(long, TimeUnit)} every
     *       10 seconds until completion.</li>
     *   <li>After completion collect and print summary information for each
     *       script and write an XML file using {@code XMLWriter.creatXML(this)}.
     *       Transformer settings include pretty-print indentation.</li>
     * </ol>
     *
     * @throws FileNotFoundException if the output file cannot be created
     *         when attempting to write the XML results
     */
    public void startScript() throws FileNotFoundException
    {
        try 
        {
            FileDialog fileDialog = new FileDialog(new JFrame(), "new", FileDialog.SAVE);
            
            fileDialog.setVisible(true);
            if(fileDialog.getFile() != null)//如果取消存檔則fileDialog.getFile()會是null
            {
                this.parent.setMagnificationFactor(this.getAccuracy());
                ExecutorService executor = Executors.newFixedThreadPool(this.getThreadAmount()); 
        
                for(int i = 0 ; i<this.tableTabbedPane.getComponentCount() ; i++)
                {
                    ScriptTable st = (ScriptTable)this.tableTabbedPane.getComponent(i);
                    for(Script s : st.getScriptSet())
                    {
                        s.removeAllScriptResult();
                        Vector<String> workloadFileNames = this.parent.getFolderFile(s.getWorkloadSite());
                        
                        for(String workloadFileName : workloadFileNames)
                        {
                            executor.execute(new ScriptRunnable(this, s, workloadFileName));
                        }
                    }
                }
                
                executor.shutdown();
                try  
                {  
                    // awaitTermination返回false即超时會繼續循環，返回true即线程池中的线程執行完成主线程跳出循環往下執行，每隔10秒循環一次  
                    while (!executor.awaitTermination(10, TimeUnit.SECONDS));  
                }  
                catch (InterruptedException e)  
                {  
                    e.printStackTrace();  
                }
                
                
                for(int i = 0 ; i<this.tableTabbedPane.getComponentCount() ; i++)
                {
                    ScriptTable st = (ScriptTable)this.tableTabbedPane.getComponent(i);
                    println("GroupID :"+st.getGroupID());
                    for(Script s : st.getScriptSet())
                    {
                        println(", ScriptID :"+s.getID() + ", ScriptResultSize :"+ s.getScriptResultSet().size());
                    }
                }
                
                //output showInfo
                for(int i = 0 ; i<this.tableTabbedPane.getComponentCount() ; i++)
                {
                    ScriptTable st = (ScriptTable)this.tableTabbedPane.getComponent(i);
                    for(Script s : st.getScriptSet())
                    {
                        for(ScriptResult sr : s.getScriptResultSet())
                        {
                            sr.showInfo();
                        }
                    }
                }
                
                Transformer transformer = TransformerFactory.newInstance().newTransformer();
                transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                transformer.setOutputProperty(OutputKeys.METHOD, "xml");
                transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4"); 

                DOMSource source = new DOMSource(XMLWriter.creatXML(this));
                File file = new File(fileDialog.getDirectory()+fileDialog.getFile()+".xml");
                StreamResult result = new StreamResult(file);
                transformer.transform(source, result);
                
            }
        }
        catch (TransformerException ex) 
        {
            Logger.getLogger(ScriptSetter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}