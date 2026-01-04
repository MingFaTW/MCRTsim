/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package WorkLoadSet;

import WorkLoad.SharedResource;
import java.util.Vector;

/**
 * Typed collection of shared resources used for synchronization among tasks.
 * Extends {@link Vector} to store {@link WorkLoad.SharedResource} instances.
 * Provides a convenience accessor by index; additional search utilities can
 * be layered later.
 *
 * @author ShiuJia
 */
public class SharedResourceSet extends Vector<SharedResource>
{
    /**
     * Create an empty SharedResourceSet.
     */
    public SharedResourceSet()
    {
        super();
    }

    /**
     * Get the shared resource at index i.
     * @param i zero-based index
     * @return shared resource instance
     */
    public SharedResource getSharedResource(int i) 
    {
        return this.get(i);
    }
}