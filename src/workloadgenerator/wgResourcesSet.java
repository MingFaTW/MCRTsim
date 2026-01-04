/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package workloadgenerator;

import java.util.Vector;

/**
 * Container for multiple wgResources groups used by the workload generator.
 * <p>
 * The set keeps a back-reference to the parent workload and assigns group
 * identifiers when resources groups are added. It exposes accessors to
 * retrieve a specific wgResources collection.
 * </p>
 *
 * @author YC
 */
public class wgResourcesSet extends Vector<wgResources>
{
    /**
     * Back-reference to the parent {@link wgWorkload} that owns this set.
     * <p>
     * This public field is used by generator code to navigate from resource
     * groups back to the overall workload configuration (for example when
     * choosing unused resources or when exporting the workload).
     * </p>
     */
    public wgWorkload parent;
    
    /**
     * Create an empty wgResourcesSet with no parent.
     */
    public wgResourcesSet ()
    {
        super();
    }
    
    /**
     * Create a wgResourcesSet associated with the provided parent workload.
     *
     * @param p the owning {@link wgWorkload} instance
     */
    public wgResourcesSet (wgWorkload p)
    {
        super();
        this.parent = p;
    }
    
    /**
     * Add a wgResources group to the set and assign it a sequential ID.
     *
     * @param r the wgResources group to add
     */
    public void addResources(wgResources r)
    {
        this.add(r);
        r.setID(this.size());
    }
    
    /*setValue*/ 
    
    /*getValue*/
    /**
     * Return the wgResources at the specified index.
     *
     * @param i index of the desired group
     * @return the wgResources instance
     */
    public wgResources getResources(int i)
    {
        return this.get(i);
    }
}