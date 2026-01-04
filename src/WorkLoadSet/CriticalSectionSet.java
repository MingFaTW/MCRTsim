/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package WorkLoadSet;

import WorkLoad.CriticalSection;
import java.util.Vector;

/**
 * Container of critical sections belonging to tasks within a workload.
 * <p>
 * Extends {@link Vector} to hold {@link WorkLoad.CriticalSection} instances.
 * Currently acts as a simple typed collection; additional behaviors (search,
 * aggregation, nesting analysis) can be layered later if needed.
 * </p>
 *
 * @author ShiuJia
 */
public class CriticalSectionSet extends Vector<CriticalSection>
{
    /**
     * Create an empty CriticalSectionSet.
     */
    public CriticalSectionSet()
    {
        super();
    }
}