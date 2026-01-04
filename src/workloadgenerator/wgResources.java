/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package workloadgenerator;

import java.util.Vector;

/**
 * Represents a group of identical resources and metadata used by the
 * workload generator (for example, a resource pool with a quantity).
 * <p>
 * The class extends Vector of {@link wgResource} and exposes helper methods
 * to add resources, query counts and provide export header keys.
 * </p>
 *
 * @author YC
 */
public class wgResources extends Vector<wgResource>
{
    /**
     * Back-reference to the owning {@link wgResourcesSet}. This is used by
     * generator routines to navigate the resource hierarchy and assign IDs.
     */
    public wgResourcesSet parent;
    private final String resourcesHeader = "resources";
    private final String IDHeader = "ID";
    private int ID = 0;
    private final String resourceAmountHeader = "quantity";
    private int resourceAmount = 0;
    
    /**
     * Create a wgResources collection owned by a parent set.
     *
     * @param p the owning wgResourcesSet
     */
    public wgResources(wgResourcesSet p)
    {
        super();
        this.parent = p;
    }
    
    /**
     * Add a wgResource to this collection and assign it a sequential ID.
     *
     * @param resource the wgResource to add
     */
    public void addResource(wgResource resource)
    {
        this.add(resource);
        resource.setID(this.size());
        this.resourceAmount = this.size();
    }
    
    /*setValue*/
    /**
     * Set this group's identifier.
     *
     * @param id identifier to set
     */
    public void setID(int id)
    {
        this.ID = id;
    }
    
    /**
     * Set the declared quantity of resources in this group.
     *
     * @param ResourceAmount the resource quantity
     */
    public void setResourceAmount(int ResourceAmount)
    {
        this.resourceAmount = ResourceAmount;
    }
    
    /*getValue*/    
    /**
     * Return this group's ID.
     *
     * @return group ID
     */
    public int getID()
    {
        return this.ID;
    }
    
    /**
     * Return the configured resource amount (quantity).
     *
     * @return resource quantity
     */
    public int getResourceAmount()
    {
        return this.resourceAmount;
    }
    
    /**
     * Header used when exporting the resources block.
     *
     * @return resources header key
     */
    public String getResourcesHeader()
    {
        return this.resourcesHeader;
    }
    
    /**
     * Header used for the ID field in exports.
     *
     * @return ID header key
     */
    public String getIDHeader()
    {
        return this.IDHeader;
    }
    
    /**
     * Header used for the resource amount (quantity) field.
     *
     * @return quantity header key
     */
    public String getResourceAmountHeader()
    {
        return this.resourceAmountHeader;
    }
}