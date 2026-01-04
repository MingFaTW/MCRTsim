/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package workloadgenerator;

/**
 * Represents a generated critical section inside a workload task's job.
 * <p>
 * A wgCriticalSection stores the start and end time (internal units) for a
 * critical section, references the resource set used, and exposes formatted
 * export helpers that scale times according to the generator's configured
 * accuracy. Inline constants provide XML/serialization header keys used by
 * the workload exporter.
 * </p>
 *
 * @author YC
 */
public class wgCriticalSection 
{
    /**
     * Parent container that owns this critical section.
     */
    public wgCriticalSectionSet parent;

    private final String criticalSectionHeader = "criticalSection";
    private final String resourceIDHeader = "resourceID";
    private final String startTimeHeader = "startTime";
    private long startTime = 0;
    private final String endTimeHeader = "endTime";
    private long endTime = 0;
    private wgResources resources;
    
    /**
     * Create a wgCriticalSection with the provided owning set.
     *
     * @param p the owning wgCriticalSectionSet
     */
    public wgCriticalSection(wgCriticalSectionSet p)
    {
        this.parent = p;
    }
    
    /*setValue*/
    /**
     * Set the start time (internal units) for this critical section.
     *
     * @param StartTime the start time to assign
     */
    public void setStartTime(long StartTime)
    {
        this.startTime = StartTime;
    }
    
    /**
     * Set the end time (internal units) for this critical section.
     *
     * @param EndTime the end time to assign
     */
    public void setEndTime(long EndTime)
    {
        this.endTime = EndTime;
    }
    
    /**
     * Set the resource group used by this critical section.
     *
     * @param Resources the wgResources instance representing the resource set
     */
    public void setResources(wgResources Resources)
    {
        this.resources = Resources;
    }
    
    /*getValue*/    
    /**
     * Get the start time in internal units.
     *
     * @return the start time
     */
    public long getStartTime()
    {
        return this.startTime;
    }
    
    /**
     * Get the end time in internal units.
     *
     * @return the end time
     */
    public long getEndTime()
    {
        return this.endTime;
    }
    
    /**
     * Get the duration of this critical section in internal units.
     *
     * @return endTime - startTime
     */
    public long getCriticalSectionTime()
    {
        return this.endTime - this.startTime;
    }
    
    /**
     * Export the start time scaled by the generator's criticalSectionAccuracy.
     *
     * @return formatted start time as a double
     */
    public double exporeStartTime()
    {
        return wgMath.div(this.startTime , this.parent.parent.parent.parent.parent.criticalSectionAccuracy);
    }
    
    /**
     * Export the end time scaled by the generator's criticalSectionAccuracy.
     *
     * @return formatted end time as a double
     */
    public double exporeEndTime()
    {
        return wgMath.div(this.endTime , this.parent.parent.parent.parent.parent.criticalSectionAccuracy);
    }
    
    /**
     * Export the critical section duration scaled by the configured accuracy.
     *
     * @return formatted duration as a double
     */
    public double exporeCriticalSectionTime()
    {
        return wgMath.div(this.endTime - this.startTime , this.parent.parent.parent.parent.parent.criticalSectionAccuracy);
    }
    
    /**
     * Get the resource group associated with this critical section.
     *
     * @return the wgResources instance
     */
    public wgResources getResources()
    {
        return this.resources;
    }
    
    /**
     * Header string used when exporting this critical section.
     *
     * @return the header key
     */
    public String getCriticalSectionHeader()
    {
        return this.criticalSectionHeader;
    }
    
    /**
     * Header string used for the resource ID field during export.
     *
     * @return the resource ID header key
     */
    public String getResourceIDHeader()
    {
        return this.resourceIDHeader;
    }
    
    /**
     * Header string used for the start time field during export.
     *
     * @return the start time header key
     */
    public String getStartTimeHeader()
    {
        return this.startTimeHeader;
    }
    
    /**
     * Header string used for the end time field during export.
     *
     * @return the end time header key
     */
    public String getEndTimeHeader()
    {
        return this.endTimeHeader;
    }
}