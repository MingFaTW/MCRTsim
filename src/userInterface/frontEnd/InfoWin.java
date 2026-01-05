/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package userInterface.frontEnd;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import SystemEnvironment.Core;
import WorkLoadSet.DataSetting;
import userInterface.UserInterface;
import userInterface.backEnd.ResultViewer;
import userInterface.backEnd.TimeLineResult;
import userInterface.backEnd.ScheduleResult;
import userInterface.backEnd.ViewerStatus;

/**
 * InfoWin is a small control panel and container used by the user interface
 * to display and manage simulation result viewers and timeline controls.
 * <p>
 * It contains the toolbar buttons for window extension, zooming, toggling the
 * timeline interaction mode, and opening the simulation results popup. It also
 * hosts a tabbed pane of ResultViewer instances (one per core or combined
 * views) and status labels for messages and timeline-related messages.
 * </p>
 * <p>
 * Integrates behaviour described by inline comments: buttons are disabled until
 * results are drawn, the timeline button toggles the timeline mouse status
 * and its foreground color, and zoom buttons adjust the ResultViewer scale
 * and underlying ScheduleResult base unit and preferred timeline size.
 * </p>
 *
 * @author ShiuJia
 */
public class InfoWin extends JPanel
{
        
    /**
     * Reference to the parent user interface that created this InfoWin.
     * Used to access simulation viewer, attributes and other UI components.
     */
    public UserInterface parent;
    private JLabel message,timeMessage;
    private JTabbedPane tabbedPane = new JTabbedPane();
    private JButton leftWinBtn,rightWinBtn ,btnZoomIn, btnZoomOut, timeLineBtn, experimentResultWinBtn;
    private DataSetting ds;
    private ResultViewer curResultViewer;
    
    /**
     * Create a new InfoWin associated with the given parent UserInterface.
     * <p>
     * The constructor sets up the layout and initializes UI components and
     * listeners. It wires buttons for window extension, zoom in/out, timeline
     * toggling and opening the simulation results popup. Change listeners on
     * the tabbed pane synchronize the active ResultViewer and timeline state.
     * </p>
     *
     * @param ui the owning UserInterface instance (must not be null)
     */
    public InfoWin(UserInterface ui) 
    {
        super();
        this.parent = ui;
        this.setLayout(new BorderLayout()); 
        this.initialize();
        
        this.experimentResultWinBtn.addMouseListener
        (
            new MouseAdapter()
            {
                public void mouseClicked(MouseEvent e)
                {
                    if(InfoWin.this.parent.getSimulationViewer().getTotalDataPopupWin() != null)
                        InfoWin.this.parent.getSimulationViewer().getTotalDataPopupWin().changeVisible();
                }
            }
        );
        
        this.timeLineBtn.addMouseListener
        (
            new MouseAdapter()
            {
                public void mouseClicked(MouseEvent e)
                {
                    try
                    {
                        InfoWin.this.pressTimeLineButton();
                        // Toggle color only when active
                        if(InfoWin.this.timeLineBtn.isEnabled())
                        {
                            InfoWin.this.timeLineBtn.setForeground(InfoWin.this.timeLineBtn.getForeground()==Color.red? Color.GREEN : Color.red);
                        }
                    }
                    catch(Exception ex)
                    {
                        // swallow; guards already in pressTimeLineButton
                    }
                }
            }
        );
        
        btnZoomIn.addMouseListener
        (
            new MouseAdapter()
            {
                public void mouseClicked(MouseEvent e)
                {
                    try
                    {
                        InfoWin.this.pressZoomInButton();
                    }
                    catch(Exception ex)
                    {
                        
                    }  
                }
            }
        );
    	
        btnZoomOut.addMouseListener
        (
            new MouseAdapter()
            {
                public void mouseClicked(MouseEvent e)
                {
                    try
                    {
                        InfoWin.this.pressZoomOutButton();
                    }
                    catch(Exception ex)
                    {
                        
                    }   
                }

            }
        );
        
        this.leftWinBtn.addMouseListener
        (
            new MouseAdapter()
            {
                public void mouseClicked(MouseEvent e)
                {
                    try
                    {
                        InfoWin.this.parent.extendSimulationViewer();
                    }
                    catch(Exception ex)
                    {
                        
                    }       
                }
            }
        );
        
        this.rightWinBtn.addMouseListener
        (
            new MouseAdapter()
            {
                public void mouseClicked(MouseEvent e)
                {
                    try
                    {
                        InfoWin.this.parent.extendAttributeViewer();
                    }
                    catch(Exception ex)
                    {
                        
                    }   
                }
            }
        );
        
        this.tabbedPane.addChangeListener
        (new ChangeListener() 
            {
                @Override
                public void stateChanged(ChangeEvent e) 
                {
                    InfoWin.this.timeLineBtn.setForeground(Color.red);
                    InfoWin.this.curResultViewer = (ResultViewer) InfoWin.this.tabbedPane.getSelectedComponent();
                    
                    if(InfoWin.this.curResultViewer != null)
                    {
                        InfoWin.this.curResultViewer.getTimeLineResult().reSetAttributes();
                        InfoWin.this.curResultViewer.getTimeLineResult().mouseStatus.setMouseStatus(ViewerStatus.IDLE);
                        InfoWin.this.parent.getAttributes().setTimeLineSet(InfoWin.this.curResultViewer.getTimeLineResult().getTimeLineSet());
                        InfoWin.this.timeLineBtn.setEnabled(true);
                        int scale = InfoWin.this.curResultViewer.getScale();
                        if(scale<0)
                            InfoWin.this.message.setText("1/" + Math.abs(scale) + "x");
                        else
                            InfoWin.this.message.setText("" + scale + "x");
                    }
                    else
                    {
                        InfoWin.this.timeLineBtn.setEnabled(false);
                    }
                }
            }
        );
    }
    
    /**
     * Initialize the contents of the frame.
     * <p>
     * This private helper constructs the top toolbar (window controls, zoom,
     * timeline toggle, simulation results button), the status labels and the
     * center tabbed pane. The timeline button is initially disabled until
     * results are drawn.
     * </p>
     */
    private void initialize() 
    {
        JPanel nPanel = new JPanel();
        this.add(nPanel, BorderLayout.NORTH);
        nPanel.setLayout(new BorderLayout(0, 0));
        this.leftWinBtn = new JButton("|<");
        this.rightWinBtn = new JButton(">|");
        this.btnZoomIn = new JButton("Zoom In"); 
        this.btnZoomOut= new JButton("Zoom Out");
        this.timeLineBtn= new JButton("Time Line");
        this.timeLineBtn.setForeground(Color.red);
        this.timeLineBtn.setEnabled(false); // disabled until results are drawn
        this.experimentResultWinBtn = new JButton("Simulation Results");
        
        JToolBar WJTB = new JToolBar();
        JToolBar EJTB = new JToolBar();
        JToolBar CJTB = new JToolBar();
        CJTB.setLayout(new GridBagLayout());
        
        WJTB.setFloatable(false);
        EJTB.setFloatable(false);
        CJTB.setFloatable(false);
        WJTB.add(this.leftWinBtn);
        EJTB.add(this.rightWinBtn);
        CJTB.add(this.btnZoomOut);
        CJTB.add(this.btnZoomIn);
        CJTB.add(this.timeLineBtn);
        CJTB.add(this.experimentResultWinBtn);
        nPanel.add(WJTB,BorderLayout.WEST);
        nPanel.add(EJTB,BorderLayout.EAST);
        nPanel.add(CJTB,BorderLayout.CENTER);
        this.add(nPanel,BorderLayout.NORTH);
        
        JPanel sPanel = new JPanel();
        this.add(sPanel, BorderLayout.SOUTH);
        sPanel.setLayout(new BorderLayout(0, 0));

        message = new JLabel("Message Here");
        message.setHorizontalAlignment(SwingConstants.CENTER);
        sPanel.add(message, BorderLayout.WEST);
        timeMessage = new JLabel("Message Here");
        timeMessage.setHorizontalAlignment(SwingConstants.CENTER);
        sPanel.add(timeMessage, BorderLayout.CENTER);
        this.add(this.tabbedPane, BorderLayout.CENTER);
    }

    /**
     * Prepare and display the result viewers for the current DataSetting.
     * <p>
     * This method clears any existing tabs and creates a new set of
     * ResultViewer tabs based on the processor/core information in the
     * current DataSetting. It adds an "AllCores" combined view and, when
     * appropriate, an "AllTasks" view. For each Core in the processor, a
     * dedicated tab is added. After creating tabs it initializes the current
     * viewer, synchronizes the parent's timeline combo box, and enables the
     * timeline button. The status message label is also set to the default
     * scale text ("1x").
     * </p>
     */
    public void pressDrawButton()
    {
        this.timeLineBtn.setForeground(Color.red);
        this.ds = parent.getSimulationViewer().getDataSetting();
        this.tabbedPane.removeAll();
        this.curResultViewer = null;
        this.timeLineBtn.setEnabled(false);
        
        this.tabbedPane.addTab("AllCores",new ResultViewer(this,ds.getProcessor().getAllCore(),"AllCores"));
            
        if(ds.getProcessor().getAllCore().size() > 1)
        {
            this.tabbedPane.addTab("AllTasks",new ResultViewer(this,ds.getProcessor().getAllCore(),"AllTasks"));
        }
        
        for(Core c : ds.getProcessor().getAllCore())
        {
            this.tabbedPane.addTab("Core "+c.getID(),new ResultViewer(this,c));
        }
        
        this.message.setText("1x");
        this.parent.getAttributes().getTimeLineSet().removeAllItems();
        
        // Initialize current viewer & synchronize timeline combo
        if(this.tabbedPane.getTabCount() > 0)
        {
            this.tabbedPane.setSelectedIndex(0);
            this.curResultViewer = (ResultViewer)this.tabbedPane.getComponentAt(0);
            if(this.curResultViewer != null)
            {
                TimeLineResult tlr = this.curResultViewer.getTimeLineResult();
                tlr.reSetAttributes();
                tlr.mouseStatus.setMouseStatus(ViewerStatus.IDLE);
                this.parent.getAttributes().setTimeLineSet(tlr.getTimeLineSet());
                this.timeLineBtn.setEnabled(true);
            }
        }
    }
    
    /**
     * Toggle the timeline interaction mode for the currently selected viewer.
     * <p>
     * If no ResultViewer is active (for example, before any results are
     * drawn) this method returns immediately. Otherwise it toggles the
     * TimeLineResult's mouse status (via chengeMouseStatus()) and requests a
     * repaint of the timeline component.
     * </p>
     */
    public void pressTimeLineButton()
    {
        if(this.curResultViewer == null)
        {
            return; // nothing to toggle yet
        }
        this.curResultViewer.getTimeLineResult().mouseStatus.chengeMouseStatus();
        this.curResultViewer.getTimeLineResult().repaint();
    }
    
    /**
     * Zoom in the timeline for the currently selected ResultViewer.
     * <p>
     * This adjusts the viewer's integer "scale" setting and the
     * ScheduleResult.baseunit to make timeline items appear larger. The
     * method updates the message label to reflect the new scale (e.g.
     * "2x" or "1/2x" for fractional scales), recomputes the preferred size
     * of the TimeLineResult component based on the final time and base unit,
     * and revalidates the timeline so the UI updates.
     * </p>
     * <p>
     * The zooming logic handles negative scales (used for fractional zooms)
     * and clamps certain bounds so scale stays within the supported range.
     * </p>
     */
    public void pressZoomInButton()
    {
        int scale = this.curResultViewer.getScale();
        ScheduleResult sr = this.curResultViewer.getScheduleResult();
        TimeLineResult tlr = this.curResultViewer.getTimeLineResult();
        
        if(scale<0)
        {
            scale/=2;
            if(scale==(-1))
            {
                scale=1;
            }
            sr.setBaseunit((double)2);
            tlr.repaint();
        }
        else if(scale < 16)
        {
            sr.setBaseunit((double)2);
            tlr.repaint();
            scale*=2;
            tlr.repaint();
            
        }

        if(scale <= 16)
        {
            if(scale<0)
                InfoWin.this.message.setText("1/" + Math.abs(scale) + "x");
            else
                InfoWin.this.message.setText("" + scale + "x");
        }
        
        this.curResultViewer.setScale(scale);
        
        if(sr.isCoreTimeLine)
        {
            tlr.setPreferredSize(new Dimension((int)((sr.getFinalTime()+1)*sr.getBaseunit()+200), 
                                                    sr.getCoreTimeLines().size() * sr.getTaskGap() + 100));
        }
        else
        {
            tlr.setPreferredSize(new Dimension((int)((sr.getFinalTime()+1)*sr.getBaseunit()+200), 
                                                    sr.getTaskTimeLines().size() * sr.getTaskGap() + 100));
        }
        
        tlr.revalidate();
    }
    
    /**
     * Zoom out the timeline for the currently selected ResultViewer.
     * <p>
     * This decreases the viewer's integer "scale" (or switches into negative
     * fractional scale territory) and sets the ScheduleResult.baseunit to
     * make timeline items appear smaller. The method updates the message
     * label to show the new scale, recomputes the preferred size of the
     * timeline component and revalidates it.
     * </p>
     */
    public void pressZoomOutButton()
    {
        int scale = this.curResultViewer.getScale();
        ScheduleResult sr = InfoWin.this.curResultViewer.getScheduleResult();
        TimeLineResult tlr = InfoWin.this.curResultViewer.getTimeLineResult();
        
        if(scale>1)
        {
            scale/=2;
            sr.setBaseunit(0.5);
            tlr.repaint();
        }
        else if(scale==1)
        {
            scale=(-2);
            sr.setBaseunit(0.5);
            tlr.repaint();
        }
        else if((scale<0)&&(scale>(-8)))
        {
            scale*=2;
            sr.setBaseunit(0.5);
            tlr.repaint();
        }

        if(scale>=(-8))
        {
            if(scale<0)
                InfoWin.this.message.setText("1/" + Math.abs(scale) + "x");
            else
                InfoWin.this.message.setText("" + scale + "x");
        }
        
        this.curResultViewer.setScale(scale);
        
        if(sr.isCoreTimeLine)
        {
            tlr.setPreferredSize(new Dimension((int)((sr.getFinalTime()+1)*sr.getBaseunit()+200), 
                                                    sr.getCoreTimeLines().size() * sr.getTaskGap() + 100));
        }
        else
        {
            tlr.setPreferredSize(new Dimension((int)((sr.getFinalTime()+1)*sr.getBaseunit()+200), 
                                                    sr.getTaskTimeLines().size() * sr.getTaskGap() + 100));
        }
        
        tlr.revalidate();           
    }
    
    /**
     * Return the currently loaded DataSetting used to create result viewers.
     *
     * @return the DataSetting instance used by this InfoWin, or null if not set
     */
    public DataSetting getDataSetting()
    {
        return this.ds;
    }
    
    /**
     * Return the main message label used to show scale and status messages.
     *
     * @return the JLabel that displays the primary message text
     */
    public JLabel getMessage()
    {
        return this.message;
    }
    
    /**
     * Return the timeline message label used to show timeline-specific messages.
     *
     * @return the JLabel that displays the timeline-related message text
     */
    public JLabel getTimeMessage()
    {
        return this.timeMessage;
    }
    
    /**
     * Return the tabbed pane that holds ResultViewer tabs.
     *
     * @return the JTabbedPane containing ResultViewer components
     */
    public JTabbedPane getTabbedPane()
    {
        return this.tabbedPane;
    }
    
    /**
     * Get the currently selected ResultViewer (or null if none selected).
     *
     * @return the active ResultViewer or null when no result viewer exists
     */
    public ResultViewer getCurCoreResult()
    {
        return this.curResultViewer;
    }
}