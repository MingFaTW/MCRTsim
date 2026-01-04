/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package userInterface.backEnd;

import ResultSet.SchedulingInfo;
import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JPanel;

/**
 * Lightweight overlay component representing a selectable time marker (or interval
 * highlight) associated with a specific {@link SchedulingInfo} on a timeline.
 *
 * <p>A {@code MouseTimeLine} is rendered as a small square positioned along the
 * horizontal time axis of its parent {@link TimeLineResult}. It stores the logical
 * time (curTime) and underlying scheduling record (result). When clicked, it removes
 * itself from the parent timeline collection and triggers a repaint.</p>
 *
 * <p>Color selection integrates existing inline logic: if the parent view is in
 * "core timeline" mode (parent.parent.isCoreTimeLine), the background color is chosen
 * based on the job's parent task ID; otherwise it uses the core ID. If any lookup
 * fails, the background falls back to black.</p>
 *
 * @author ShiuJia
 */
public class MouseTimeLine extends JPanel
{
    /**
     * Reference to the timeline container that owns this marker. Used for color
     * palette access, coordinate scaling (via parent.parent.getBaseunit()) and
     * removal on user interaction.
     */
    public TimeLineResult parent;
    private Double curTime;
    private SchedulingInfo result; 
    
    /**
     * Creates an uninitialized marker component. Properties must be set manually
     * before use; typically the parameterized constructor is preferred.
     */
    public MouseTimeLine ()
    {
       super(); 
    }
    
    /**
     * Constructs a marker bound to a timeline result, a logical time and a scheduling record.
     *
     * <p>Side effects: determines background color from the parent's resourceColor palette
     * using either task ID or core ID depending on whether the timeline is in core view.
     * Adds a mouse listener that removes this marker from the parent timeline when clicked.</p>
     *
     * @param rv parent timeline container
     * @param x logical time value (in the same units the parent uses for scaling)
     * @param re associated scheduling record for metadata display or color derivation
     */
    public MouseTimeLine (TimeLineResult rv, Double x, SchedulingInfo re)
    {
        super();
        this.parent = rv;
        this.curTime = x;
        this.result = re;
        
        if(this.parent.parent.isCoreTimeLine)
        {   
            try
            {
                this.setBackground(this.parent.getResourceColor()[20 - re.getJob().getParentTask().getID()]);
            }
            catch(Exception ex)
            {
                this.setBackground(Color.BLACK);
            }
        }
        else
        {   
            try
            {
                this.setBackground(this.parent.getResourceColor()[20 - re.getCore().getID()]);
            }
            catch(Exception ex)
            {
                this.setBackground(Color.BLACK);
            }
        }
        
        
        this.addMouseListener(new MouseAdapter()
        {
            public void mouseClicked(MouseEvent e)
            {
                MouseTimeLine.this.parent.getTimeLineSet().removeItem(MouseTimeLine.this);
                MouseTimeLine.this.parent.remove(MouseTimeLine.this);
                MouseTimeLine.this.parent.repaint();
            }
        });
    }
    
    /**
     * Recomputes and applies this marker's bounds based on its stored logical time
     * and the parent's current base unit scale. The X position is (curTime * baseunit)
     * offset by a fixed margin (100) and centered by subtracting half the width (5).
     */
    public void reSetItself()
    {
        this.setBounds(Double.valueOf(curTime * this.parent.parent.getBaseunit()).intValue() + 100 - 5, 10, 10, 10);
    }
    
    /**
     * Returns the logical time value represented by this marker.
     *
     * @return the time (Double) used for positioning on the timeline
     */
    public Double getCurTime()
    {
        return this.curTime;
    }
    
    /**
     * Returns the pixel X coordinate (left edge) corresponding to the current time,
     * computed as (curTime * baseunit + fixed margin 100).
     *
     * @return the X coordinate in pixels
     */
    public int getCurPoint()
    {
        return Double.valueOf(curTime * this.parent.parent.getBaseunit()).intValue() + 100;
    } 
    
    /**
     * Produces a string representation combining the logical time and either the
     * associated core ID or the literal "Null" when the scheduling info has no core.
     *
     * @return formatted string "time , coreID" or "time , Null"
     */
    public String toString()
    {
        String str = "";
        if(result.getCore() == null)
        {
            str += this.curTime+ " , " + "Null";
        }
        else
        {
            str += this.curTime+ " , " +  result.getCore().getID();
        }
        return str;
    }
    
    /**
     * Returns the scheduling record associated with this marker (may be used for
     * additional metadata or tooltip display in the UI layer).
     *
     * @return the {@link SchedulingInfo} bound to this marker
     */
    public SchedulingInfo getResult()
    {
        return this.result;
    }
    
}