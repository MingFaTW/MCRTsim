/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package userInterface.backEnd;

import WorkLoad.CriticalSection;
import javax.swing.JPanel;

/**
 * Swing panel representing a single shared resource instance accessed within a
 * {@link CriticalSection} on the scheduling timeline.
 *
 * <p>Each {@code ResourcePanel} derives three pieces of textual metadata from
 * the provided critical section when constructed:
 * <ul>
 *   <li><strong>resourcesID</strong>: the identifier of the shared resource set (e.g. R set ID).</li>
 *   <li><strong>resourceID</strong>: the specific instance index within that set acquired by the task.</li>
 *   <li><strong>resourcesAmount</strong>: total number of resource instances available in the set.</li>
 * </ul>
 * These values are composed into a tooltip string of the form
 * {@code R<setID>(<instanceID>/<totalAmount>)} to aid quick inspection in the UI.
 * The panel itself can be colorized externally (e.g., by the timeline renderer)
 * and positioned according to the execution interval it belongs to.
 *
 * @author ShiuJia
 */
public class ResourcePanel extends JPanel
{
    private String resourcesID;
    private String resourceID;
    private String resourcesAmount;
    
    /**
     * Constructs a resource panel using metadata from a {@link CriticalSection}.
     * Extracts the set ID, the instance ID actually locked by the job, and the
     * total number of available instances, then sets a descriptive tooltip.
     *
     * @param cs the critical section describing the resource usage
     */
    public ResourcePanel(CriticalSection cs)
    {
        super();
        resourcesID = String.valueOf(cs.getUseSharedResource().getID());
        resourcesAmount = String.valueOf(cs.getUseSharedResource().getResourcesAmount());
        resourceID = String.valueOf(cs.getResourceID());
        this.setToolTipText("R"+resourcesID+"(" + resourceID +"/" + resourcesAmount +")");
    }
    
    /**
     * Returns the identifier of the shared resource set (group) from which the
     * resource instance was acquired.
     *
     * @return shared resource set ID as a string
     */
    public String getResourcesID()
    {
        return this.resourcesID;
    }
    
    /**
     * Returns the specific resource instance identifier (index within the set)
     * that was locked by the task inside the critical section.
     *
     * @return resource instance ID as a string
     */
    public String getResourceID()
    {
        return this.resourceID;
    }
            
    /**
     * Returns the total number of instances available in the shared resource set.
     * Useful for displaying capacity alongside the acquired instance ID.
     *
     * @return total instance count as a string
     */
    public String getResourcesAmount()
    {
        return this.resourcesAmount;
    }        
}