/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package userInterface;

import java.awt.Dimension;
import javax.swing.JFrame;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;
import userInterface.frontEnd.AttributeViewer;
import userInterface.frontEnd.InfoWin;
import userInterface.frontEnd.SimulationViewer;
import static RTSimulator.RTSimulator.println;

/**
 * <strong>UserInterface</strong>
 * <p>
 * Initializes and manages the main application window using two nested horizontal
 * {@link javax.swing.JSplitPane} layouts:
 * <ul>
 *   <li>Main split (left/right): left side shows the {@link userInterface.frontEnd.SimulationViewer}, right side hosts a secondary split.</li>
 *   <li>Secondary split (left/right): left side shows the {@link userInterface.frontEnd.InfoWin} (results/status tabs); right side shows the {@link userInterface.frontEnd.AttributeViewer}.</li>
 * </ul>
 * Provides toggle helpers to collapse/expand the simulation viewer and the attribute
 * viewer regions.
 * 
 * @author ShiuJia
 */
public class UserInterface
{
    private JFrame frame;
    private SimulationViewer simView;
    private InfoWin result;
    private JSplitPane splitPane;
    private JSplitPane bottomSplitPane ;
    private AttributeViewer atb; 
    
    /**
     * Constructs a new UserInterface instance, initializes its components,
     * and makes the main application window visible.
     */
    public UserInterface()
    {
        this.initialize();
        this.frame.setVisible(true);
        // Ensure divider positions are applied after frame becomes visible — run twice to be robust against layout timing
        SwingUtilities.invokeLater(() -> {
            adjustInitialDividers();
            SwingUtilities.invokeLater(() -> adjustInitialDividers());
        });

        // Also listen for the frame being shown/resized and run adjustInitialDividers once more to be robust
        this.frame.addComponentListener(new java.awt.event.ComponentAdapter()
        {
            @Override
            public void componentShown(java.awt.event.ComponentEvent e)
            {
                adjustInitialDividers();
                frame.removeComponentListener(this);
            }

            @Override
            public void componentResized(java.awt.event.ComponentEvent e)
            {
                adjustInitialDividers();
                frame.removeComponentListener(this);
            }
        });
    }
    
    /**
     * Initializes the main window and its components, setting up the layout
     * and organizing components using split panes.
     */
    private void initialize()
    {
        this.frame = new JFrame("RTSimulator v2.8");
        this.frame.setBounds(100, 100, 1200, 700); // initial size
        this.frame.setMinimumSize(new Dimension(900, 600));
        this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        this.frame.getContentPane().add(splitPane);
        splitPane.setContinuousLayout(true);
        // target approx 30% of total width for SimulationViewer
        splitPane.setResizeWeight(0.30);
        simView = new SimulationViewer(this);
        splitPane.setLeftComponent(simView);
        result = new InfoWin(this);
        bottomSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        bottomSplitPane.setContinuousLayout(true);
        // when right side (70% of total) gets extra space, keep InfoWin at ~30% of total
        // infoWeight = 0.30 / 0.70 = ~0.428571
        bottomSplitPane.setResizeWeight(0.428571);
        splitPane.setRightComponent(bottomSplitPane);
        bottomSplitPane.setLeftComponent(result);
        atb = new AttributeViewer(this);
        // modest minimum to allow right panel to shrink
        atb.setMinimumSize(new Dimension(120, atb.getMinimumSize().height));
        // set a modest preferred size so AttributeViewer doesn't expand to occupy most space on startup
        atb.setPreferredSize(new Dimension(200, atb.getPreferredSize().height));
        bottomSplitPane.setRightComponent(atb);
        // After components laid out, set divider positions so each main region targets ~30% of total width
        SwingUtilities.invokeLater(() -> {
            int totalW = frame.getContentPane().getWidth();
            int simMin = simView.getMinimumSize() != null ? simView.getMinimumSize().width : 400;
            int atbMin = atb.getMinimumSize() != null ? atb.getMinimumSize().width : 120;

            // target left width = 30% of total, but ensure >= simMin and <= totalW - atbMin - 50 (reserve minimal space for InfoWin)
            int targetSimW = (int)(totalW * 0.30);
            int maxAllowedSimW = Math.max(0, totalW - atbMin - 50); // leave >=50px for InfoWin as a sensible lower bound
            int simW = Math.max(targetSimW, simMin);
            simW = Math.min(simW, maxAllowedSimW);
            simW = Math.max(0, Math.min(simW, totalW));
            splitPane.setDividerLocation(simW);

            // compute bottom (right area) width after reserving simW
            int bottomW = Math.max(0, totalW - simW);
            int infoTarget = (int)(totalW * 0.30);
            // ensure InfoWin doesn't ask for more than bottomW - atbMin
            int maxLeftForBottom = Math.max(0, bottomW - atbMin);
            int bottomDividerLoc = Math.min(infoTarget, maxLeftForBottom);
            bottomDividerLoc = Math.max(1, Math.min(bottomDividerLoc, bottomW - 1));
            bottomSplitPane.setDividerLocation(bottomDividerLoc);
        });
    }

    /**
     * Toggles the visibility of the simulation viewer area by adjusting the main split pane divider (horizontal).
     */
    public void extendSimulationViewer()
    {
        boolean collapsedRight = false;
        int rightWidth = 0;
        // For horizontal split, use width
        if(this.bottomSplitPane.getDividerLocation() > this.bottomSplitPane.getWidth()-20)
        {
            collapsedRight = true;
        }
        else
        {
            rightWidth = this.bottomSplitPane.getWidth() - this.bottomSplitPane.getDividerLocation();
        }
        
        if(splitPane.getDividerLocation() < 5)
        {
            splitPane.setDividerLocation(splitPane.getMinimumDividerLocation());
        }
        else
        {
            // Collapse left (simulation) panel to zero width
            splitPane.setDividerLocation(0);
        }
        
        this.frame.revalidate();
        
        if(collapsedRight)
        {
            println("3"+","+this.bottomSplitPane.getWidth());
            this.bottomSplitPane.setDividerLocation(this.bottomSplitPane.getWidth()); // collapse right side
        }
        else
        {
            println("4"+","+this.bottomSplitPane.getWidth() + "," + rightWidth);
            this.bottomSplitPane.setDividerLocation(this.bottomSplitPane.getWidth() - rightWidth);
        }
    }
    
    /**
     * Toggles the visibility of the attribute viewer by adjusting the divider of the right split pane (horizontal).
     * If currently expanded, collapse completely; if collapsed, expand leaving a fixed 200px width for the attribute viewer.
     */
    public void extendAttributeViewer()
    {
        int usableWidth = this.bottomSplitPane.getWidth() - this.bottomSplitPane.getDividerSize();
        println(""+this.bottomSplitPane.getDividerLocation()+","+usableWidth);
        if(this.bottomSplitPane.getDividerLocation() < usableWidth - 5)
        {
            // Collapse attribute viewer
            this.bottomSplitPane.setDividerLocation(this.bottomSplitPane.getWidth());
        }
        else
        {
            // Restore equal split of right side
            this.bottomSplitPane.setDividerLocation(this.bottomSplitPane.getWidth()/2);
        }
    }
    
    /**
     * Returns the simulation viewer component (top area) that renders the
     * simulation content.
     *
     * @return the {@link SimulationViewer} instance
     */
    public SimulationViewer getSimulationViewer()
    {
        return this.simView;
    }

    /**
     * Returns the main application window frame.
     *
     * @return the {@link JFrame} hosting the UI
     */
    public JFrame getFrame()
    {
        return this.frame;
    }
    
    /**
     * Returns the attributes panel component placed at the bottom split pane.
     *
     * @return the {@link AttributeViewer} instance
     */
    public AttributeViewer getAttributes()
    {
        return this.atb;
    }
    
    /**
     * Returns the information window component (results/status) placed on the left side of the
     * nested (right) split pane.
     *
     * @return the {@link InfoWin} instance
     */
    public InfoWin getInfoWin()
    {
        return this.result;
    }
    
    /**
     * Returns the nested right-hand horizontal split pane that contains the results window (left)
     * and the attribute viewer (right). External callers may further customize divider positions
     * or component visibility.
     *
     * @return the nested {@link JSplitPane} (right side of the main layout)
     */
    public JSplitPane getBottomSplitpane()
    {
        return this.bottomSplitPane;
    }
    
    /**
     * Allows external/runtime adjustment of the minimum width of the SimulationViewer panel.
     * Also ensures current divider location is not left smaller than the new minimum.
     * @param minWidthPx desired minimum width in pixels (clamped to >=50)
     */
    public void setSimulationViewerMinimumWidth(int minWidthPx)
    {
        int w = Math.max(400, minWidthPx); // enforce project-wide minimum of 400px
        Dimension cur = simView.getMinimumSize();
        simView.setMinimumSize(new Dimension(w, cur != null ? cur.height : 200));
        // If current divider < new min width, move it right
        if(splitPane.getDividerLocation() < w)
        {
            splitPane.setDividerLocation(w);
        }
        frame.revalidate();
    }
    
    private void adjustInitialDividers()
    {
        if(this.frame == null || this.splitPane == null || this.bottomSplitPane == null || this.simView == null || this.atb == null)
            return;
        int totalW = frame.getContentPane().getWidth();
        if(totalW <= 0)
            return;
        int simMin = simView.getMinimumSize() != null ? simView.getMinimumSize().width : 400;
        int atbMin = atb.getMinimumSize() != null ? atb.getMinimumSize().width : 120;

        int targetSimW = (int)(totalW * 0.30);
        int maxAllowedSimW = Math.max(0, totalW - atbMin - 50);
        int simW = Math.max(targetSimW, simMin);
        simW = Math.min(simW, maxAllowedSimW);
        simW = Math.max(0, Math.min(simW, totalW));
        splitPane.setDividerLocation(simW);

        int bottomW = Math.max(0, totalW - simW);
        int infoTarget = (int)(totalW * 0.30);
        int maxLeftForBottom = Math.max(0, bottomW - atbMin);
        int bottomDividerLoc = Math.min(infoTarget, maxLeftForBottom);
        bottomDividerLoc = Math.max(1, Math.min(bottomDividerLoc, bottomW - 1));
        bottomSplitPane.setDividerLocation(bottomDividerLoc);
    }
}