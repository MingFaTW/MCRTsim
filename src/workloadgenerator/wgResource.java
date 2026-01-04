/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package workloadgenerator;

/**
 * Represents a single resource entry used by the workload generator.
 * <p>
 * A wgResource contains a reference to its parent {@link wgResources} group,
 * an integer ID and header strings used for export. The generator assigns
 * sequential IDs when resources are added to a group.
 * </p>
 *
 * @author YC
 */
public class wgResource 
{
    /** Parent resources group */
    public wgResources parent;
    private final String resourceHeader = "Resource";
    private final String IDHeader = "ID";
    private int ID = 0;
    
    /**
     * Create a wgResource belonging to the given resources group.
     *
     * @param p the owning wgResources
     */
    public wgResource (wgResources p)
    {
        this.parent = p;
    }
    
    /*setValue*/
    /**
     * Assign an integer id to this resource.
     *
     * @param id the id to assign
     */
    public void setID(int id)
    {
        this.ID = id;
    }
    
    /*getValue*/    
    /**
     * Return the resource id.
     *
     * @return the integer id
     */
    public int getID()
    {
        return this.ID;
    }
    
    /**
     * Header string used when exporting resource entries.
     *
     * @return the resource header key
     */
    public String getResourcAHeader()
    {
        return this.resourceHeader;
    }
    
    /**
     * Header string used for the ID field during export.
     *
     * @return the ID header key
     */
    public String getIDHeader()
    {
        return this.IDHeader;
    }
 
}