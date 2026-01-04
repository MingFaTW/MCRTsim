/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package userInterface.backEnd;

/**
 * Tracks the current mouse interaction status used by the back-end viewer.
 *
 * <p>This lightweight state holder exposes a simple API to:
 * <ul>
 *   <li>initialize the mouse status (defaults to {@link ViewerStatus#IDLE}),</li>
 *   <li>toggle between {@link ViewerStatus#IDLE} and {@link ViewerStatus#EXECUTION},</li>
 *   <li>explicitly set a given {@link ViewerStatus}, and</li>
 *   <li>retrieve the current status.</li>
 * </ul>
 * It is typically referenced by UI components to decide how mouse events should
 * be interpreted (e.g., idle vs. execution timeline interactions).
 *
 * @author ShiuJia
 */
public class MouseStatus
{
    private ViewerStatus sta;
    
    /**
     * Creates a new {@code MouseStatus} with the status set to
     * {@link ViewerStatus#IDLE}.
     */
    public  MouseStatus()
    {
       sta = ViewerStatus.IDLE;
    }
    
    /**
     * Toggles the mouse status between {@link ViewerStatus#IDLE} and
     * {@link ViewerStatus#EXECUTION}.
     */
    public void chengeMouseStatus()
    {
       sta = sta==ViewerStatus.IDLE ? ViewerStatus.EXECUTION : ViewerStatus.IDLE;
    }
    
    /**
     * Sets the current mouse status to the specified value.
     *
     * @param vs the new status to apply
     */
    public void setMouseStatus(ViewerStatus vs)
    {
       sta = vs;
    }
    
    /**
     * Returns the current mouse status.
     *
     * @return the current {@link ViewerStatus}
     */
    public ViewerStatus getMouseStatus()
    {
        return sta;
    }
}