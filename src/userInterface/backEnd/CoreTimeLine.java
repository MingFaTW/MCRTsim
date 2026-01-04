/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package userInterface.backEnd;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.text.DecimalFormat;
import java.util.ArrayList;
import RTSimulator.Definition.CoreStatus;

/**
 * Visual timeline model for a single core used by the back‑end UI renderer.
 * <p>
 * A {@code CoreTimeLine} collects {@link TaskExecution} segments (execution,
 * waiting, context switch, migration, error markers) and knows how to paint
 * itself and associated per‑segment resource usage rectangles onto a supplied
 * {@link Graphics} context. The horizontal axis is time (scaled by the parent
 * {@link ScheduleResult}'s base unit) and two stacked lanes are drawn: the
 * core status lane and a parallel task lane (labelled "Task").
 * </p>
 * <p>
 * Integrated original inline comments (Chinese):
 * <ul>
 *   <li>"時間軸起始位置" – the point {@code o} is the origin (start position) of
 *       the time axis.</li>
 *   <li>"畫時間軸" / "畫時間軸刻度" – loops draw the timeline line(s) and tick marks.</li>
 *   <li>Status handling (E / X, MIGRATION, CONTEXTSWITCH, WAIT, WRONG, EXECUTION)
 *       determines fill colors, half‑height vs full‑height bars, and label
 *       formatting (taskID, jobID, times with fractional portions).</li>
 * </ul>
 * Colors for tasks and resources are selected from a predefined HSB palette and
 * inverted (via {@link #reverseColor(Color)}) for contrasting text to maximize
 * readability inside narrow execution rectangles. Time labels are drawn only
 * when fractional (two decimals) and optionally throttled depending on parent
 * scale to avoid clutter.
 *
 * @author ShiuJia
 */
public class CoreTimeLine
{
    int resourceHeight=13;
    int taskHeight = 80;
    int ID;
    Point o; //時間軸起始位置 (origin of the time axis)
    ScheduleResult parent;
    ArrayList<TaskExecution> executions; 
    
    Color[] resourceColor = new Color[]
            {
                Color.getHSBColor((float) 0.1, 1, 1),Color.getHSBColor((float) 0.2, 1, 1),
                Color.getHSBColor((float) 0.3, 1, 1),Color.getHSBColor((float) 0.45,(float) 0.2, 1),
                Color.getHSBColor((float) 0.5, 1, 1),Color.getHSBColor((float) 0.58, 1, 1),
                Color.getHSBColor((float) 0.7, 1, 1),Color.getHSBColor((float) 0.8, 1, 1),
                Color.getHSBColor((float) 0.15, 1, 1),Color.getHSBColor((float) 1, 1, 1),

                Color.getHSBColor((float) 0.1, (float)0.5, (float)0.8),Color.getHSBColor((float) 0.2, (float)0.5, (float)0.8),
                Color.getHSBColor((float) 0.3, (float)0.5, (float)0.8),Color.getHSBColor((float) 0.45, (float)0.5, (float)0.8),
                Color.getHSBColor((float) 0.5, (float)0, (float)0.8),Color.getHSBColor((float) 0.6, (float)0.3, (float)1),
                Color.getHSBColor((float) 0.7, (float)0.2, (float)0.8),Color.getHSBColor((float) 0.17, (float)0.5, (float)1),
                Color.getHSBColor((float) 0.85, (float)0.3, (float)0.9),Color.getHSBColor((float) 1, (float)0.8, (float)0.6)
            };

    /**
     * Creates an empty core timeline with no parent or executions. The origin
     * and identifier must be set later (typically by using the other
     * constructor) before drawing; adding executions without a parent base unit
     * will yield undefined placement.
     */
    public CoreTimeLine()
    {
        
    }

    /**
     * Constructs a core timeline bound to a {@link ScheduleResult} parent and
     * logically identifies it with the provided string ID.
     *
     * @param sr the owning scheduling result that supplies scaling (base unit),
     *           final time, and vertical spacing parameters
     * @param i  zero‑based vertical index of this core; used to compute the y
     *           offset from the common origin via {@code parent.getTaskGap()}
     * @param id textual core identifier (parsed to integer and stored in {@code ID})
     * @throws NumberFormatException if {@code id} cannot be parsed as an integer
     */
    public CoreTimeLine(ScheduleResult sr, int i, String id)
    {
        parent = sr;
        ID = Integer.parseInt(id);
        o = new Point(100, 200 + i * this.parent.getTaskGap());
        executions = new ArrayList<TaskExecution>();
    }

    /**
     * Appends a {@link TaskExecution} segment (execution, wait, context switch,
     * migration, error marker) to this timeline. The segment will later be
     * rendered in {@link #drawItself(Graphics)} and resources shown via
     * {@link #drawResources(TimeLineResult)}.
     *
     * @param te the execution segment to add (order determines draw layering)
     */
    public void addExecution(TaskExecution te) //E,X 狀態 (execution or other status segment)
    {
        executions.add(te);
    }
    
    /**
     * Renders the entire core timeline onto the provided graphics context.
     * <p>
     * Drawing steps:
     * <ol>
     *   <li>Draws auxiliary task lane ("Task") and primary time axis, including
     *       arrowheads and tick marks, respecting parent scaling and label
     *       density (scale heuristic).</li>
     *   <li>Iterates each {@link TaskExecution} and paints a colored rectangle
     *       whose style (height, color, half vs full bar) depends on its
     *       {@link CoreStatus} (MIGRATION=blue half bar, CONTEXTSWITCH=light gray
     *       half bar, WAIT=filled full bar, EXECUTION=outlined bar, WRONG=red X).</li>
     *   <li>Overlays task/job identifiers within bars when width permits; short
     *       bars show truncated identifiers.</li>
     *   <li>Labels fractional start/end times when their first decimal digit is
     *       non-zero (reduces clutter).</li>
     * </ol>
     * Resource usage rectangles are not drawn here; see
     * {@link #drawResources(TimeLineResult)} and
     * {@link #reDrawResources(TimeLineResult, Graphics)}.
     *
     * @param g the graphics context to draw into (coordinates relative to UI component)
     */
    public void drawItself(Graphics g)
    {
        int baseunit = this.parent.getBaseunit();
        double finalTime = this.parent.getFinalTime();
        int yHeight = o.y+65;
        
        { //TaskLine
            for(int i = -1 ; i<=1 ; i++) //畫時間軸
            {
                //println("X = "+o.x +", Y = " + o.y+", finalTime = "+finalTime);

                g.drawLine(o.x, yHeight + i, (int)(o.x + baseunit * (finalTime + 1)), yHeight + i);

                g.drawLine((int)(o.x + baseunit * (finalTime + 1)), yHeight + i, 
                               (int)(o.x + baseunit * (finalTime + 1) - 10), yHeight + i + 10);
                g.drawLine((int)(o.x + baseunit * (finalTime+1)),  yHeight+i, 
                               (int)(o.x + baseunit * (finalTime+ 1 ) - 10), yHeight + i - 10);
            }

            for(int i = 0 ; i <= finalTime ; i++) //畫時間軸刻度
            { 
                g.drawLine( o.x + i * baseunit, yHeight, o.x + i * baseunit, yHeight + 5);
            }

            g.drawString("Task", o.x - 50, yHeight);
        }
        
        for(int i = -1 ; i<=1 ; i++) //畫時間軸
        {
            g.drawLine(o.x, o.y + i, (int)(o.x + baseunit * (finalTime + 1)), o.y + i);

            g.drawLine((int)(o.x + baseunit * (finalTime + 1)), o.y + i, 
                           (int)(o.x + baseunit * (finalTime + 1) - 10), o.y + i + 10);
            g.drawLine((int)(o.x + baseunit * (finalTime+1)),  o.y+i, 
                           (int)(o.x + baseunit * (finalTime+ 1 ) - 10), o.y + i - 10);
        }
        
        for(int i = 0 ; i <= finalTime ; i++) //畫時間軸刻度
        { 
            g.drawLine( o.x + i * baseunit, o.y, o.x + i * baseunit, o.y + 5);
            
            if(parent.parent.scale >= 1)
            {
                g.drawString("" + i, o.x + i * baseunit - 2, o.y + 20);
            }
            else if(parent.parent.scale == (-8))
            {
                if(i % 10 == 0)
                {
                    g.drawString("" + i, o.x + i * baseunit - 2, o.y + 20);
                }
            }
            else if(i%5==0)
            {
                g.drawString("" + i, o.x + i * baseunit - 2, o.y + 20);			
            }
        }

        for(TaskExecution te : executions)
        {
            if(te.getStatus() == CoreStatus.MIGRATION)
            {
                g.setColor(Color.BLUE);
                g.fillRect((int)(o.x + te.getStartTime() * baseunit ), o.y - (this.taskHeight/2), (int)(te.getExecutionTime() * baseunit), this.taskHeight/2);
                g.setColor(Color.black);
                g.drawRect((int)(o.x + te.getStartTime() * baseunit ), o.y - (this.taskHeight/2), (int)(te.getExecutionTime() * baseunit), this.taskHeight/2);
                
                { //TaskLine
                    g.drawRect((int)(o.x + te.getStartTime() * baseunit ), yHeight - 16, (int)(te.getExecutionTime() * baseunit), 16);
                    g.drawLine((int)(o.x + te.getStartTime() * baseunit ), yHeight, (int)(o.x + te.getStartTime() * baseunit), yHeight + 5);
                    g.setColor(resourceColor[19 - (te.getTaskID()%19)-1]);
                    g.fillRect((int)(o.x + te.getStartTime() * baseunit)+1, yHeight - 15, (int)(te.getExecutionTime() * baseunit) - 1, 14);
                    g.setColor(reverseColor(resourceColor[19 - (te.getTaskID()%19)-1]));

                    char[] data = String.valueOf(te.getTaskID()+","+te.getJobID()).toCharArray();

                    if((int)(te.getExecutionTime() * baseunit) > data.length * 8)
                    {
                        g.drawChars(data, 0, data.length, (int)(o.x + te.getStartTime() * baseunit) + 2, yHeight - 2);
                    }
                    else if((int)((te.getExecutionTime() * baseunit) / 8) > 1)
                    {
                        g.drawChars(data, 0, (int)((te.getExecutionTime() * baseunit) / 8), (int)(o.x + te.getStartTime() * baseunit) + 2, yHeight - 2);
                    }
                    g.setColor(Color.black);
                }
                
                DecimalFormat df = new DecimalFormat("##.00");
                double time = Double.parseDouble(df.format(te.getStartTime()));
                
                if(((int)(time*10)%10)!=0)
                {
                    g.drawString(""+ time, (int)(o.x - 4 + te.getStartTime() * baseunit), o.y + 40);
                    g.drawLine((int)(o.x + te.getStartTime() * baseunit), o.y, (int)(o.x + te.getStartTime() * baseunit), o.y + 25);
                }

                time = Double.parseDouble(df.format(te.getEndTime()));

                if(( (int)(time * 10) % 10) != 0)
                {
                    g.drawString(""+ time, (int)(o.x - 4 + te.getEndTime() * baseunit), o.y + 40);
                    g.drawLine((int)(o.x + te.getEndTime() * baseunit), o.y, (int)(o.x + te.getEndTime() * baseunit), o.y + 25);
                }
            }
            else if(te.getStatus() == CoreStatus.CONTEXTSWITCH)
            {
                g.setColor(Color.LIGHT_GRAY);
                g.fillRect((int)(o.x + te.getStartTime() * baseunit ), o.y - (this.taskHeight/2), (int)(te.getExecutionTime() * baseunit), this.taskHeight/2);
                g.setColor(Color.black);
                g.drawRect((int)(o.x + te.getStartTime() * baseunit ), o.y - (this.taskHeight/2), (int)(te.getExecutionTime() * baseunit), this.taskHeight/2);
                
                { //TaskLine
                    g.drawRect((int)(o.x + te.getStartTime() * baseunit ), yHeight - 16, (int)(te.getExecutionTime() * baseunit), 16);
                    g.drawLine((int)(o.x + te.getStartTime() * baseunit ), yHeight, (int)(o.x + te.getStartTime() * baseunit), yHeight + 5);
                    g.setColor(resourceColor[19 - (te.getTaskID()%19)-1]);
                    g.fillRect((int)(o.x + te.getStartTime() * baseunit)+1, yHeight - 15, (int)(te.getExecutionTime() * baseunit) - 1, 14);
                    g.setColor(reverseColor(resourceColor[19 - (te.getTaskID()%19)-1]));

                    char[] data = String.valueOf(te.getTaskID()+","+te.getJobID()).toCharArray();

                    if((int)(te.getExecutionTime() * baseunit) > data.length * 8)
                    {
                        g.drawChars(data, 0, data.length, (int)(o.x + te.getStartTime() * baseunit) + 2, yHeight - 2);
                    }
                    else if((int)((te.getExecutionTime() * baseunit) / 8) > 1)
                    {
                        g.drawChars(data, 0, (int)((te.getExecutionTime() * baseunit) / 8), (int)(o.x + te.getStartTime() * baseunit) + 2, yHeight - 2);
                    }
                    g.setColor(Color.black);
                }
                
                DecimalFormat df = new DecimalFormat("##.00");
                double time = Double.parseDouble(df.format(te.getStartTime()));
                
                if(((int)(time*10)%10)!=0)
                {
                    g.drawString(""+ time, (int)(o.x - 4 + te.getStartTime() * baseunit), o.y + 40);
                    g.drawLine((int)(o.x + te.getStartTime() * baseunit), o.y, (int)(o.x + te.getStartTime() * baseunit), o.y + 25);
                }

                time = Double.parseDouble(df.format(te.getEndTime()));

                if(( (int)(time * 10) % 10) != 0)
                {
                    g.drawString(""+ time, (int)(o.x - 4 + te.getEndTime() * baseunit), o.y + 40);
                    g.drawLine((int)(o.x + te.getEndTime() * baseunit), o.y, (int)(o.x + te.getEndTime() * baseunit), o.y + 25);
                }
            }
            else if(te.getStatus() == CoreStatus.WAIT)
            {
                g.fillRect((int)(o.x + te.getStartTime() * baseunit ), o.y - this.taskHeight, (int)(te.getExecutionTime() * baseunit), this.taskHeight);
                
                { //TaskLine
                    g.drawRect((int)(o.x + te.getStartTime() * baseunit ), yHeight - 16, (int)(te.getExecutionTime() * baseunit), 16);
                    g.drawLine((int)(o.x + te.getStartTime() * baseunit ), yHeight, (int)(o.x + te.getStartTime() * baseunit), yHeight + 5);
                    g.setColor(resourceColor[19 - (te.getTaskID()%19)-1]);
                    g.fillRect((int)(o.x + te.getStartTime() * baseunit)+1, yHeight - 15, (int)(te.getExecutionTime() * baseunit) - 1, 14);
                    g.setColor(reverseColor(resourceColor[19 - (te.getTaskID()%19)-1]));

                    char[] data = String.valueOf(te.getTaskID()+","+te.getJobID()).toCharArray();

                    if((int)(te.getExecutionTime() * baseunit) > data.length * 8)
                    {
                        g.drawChars(data, 0, data.length, (int)(o.x + te.getStartTime() * baseunit) + 2, yHeight - 2);
                    }
                    else if((int)((te.getExecutionTime() * baseunit) / 8) > 1)
                    {
                        g.drawChars(data, 0, (int)((te.getExecutionTime() * baseunit) / 8), (int)(o.x + te.getStartTime() * baseunit) + 2, yHeight - 2);
                    }
                    

                    g.setColor(Color.black);
                }
                
                DecimalFormat df = new DecimalFormat("##.00");
                double time = Double.parseDouble(df.format(te.getStartTime()));
                
                if(((int)(time*10)%10)!=0)
                {
                    g.drawString(""+ time, (int)(o.x - 4 + te.getStartTime() * baseunit), o.y + 40);
                    g.drawLine((int)(o.x + te.getStartTime() * baseunit), o.y, (int)(o.x + te.getStartTime() * baseunit), o.y + 25);
                }

                time = Double.parseDouble(df.format(te.getEndTime()));

                if(( (int)(time * 10) % 10) != 0)
                {
                    g.drawString(""+ time, (int)(o.x - 4 + te.getEndTime() * baseunit), o.y + 40);
                    g.drawLine((int)(o.x + te.getEndTime() * baseunit), o.y, (int)(o.x + te.getEndTime() * baseunit), o.y + 25);
                }
            }
            else if(te.getStatus() == CoreStatus.WRONG)
            {
                g.setColor(Color.red);
                
                for(int i = 0 ; i<3 ; i++)
                {
                    g.drawString("X", (int)(o.x - 3 + te.getStartTime() * baseunit), o.y - this.taskHeight - i);
                }
                
                g.setColor(Color.BLACK);
            }
            else if( te.getStatus() == CoreStatus.EXECUTION)
            {
                g.drawRect((int)(o.x + te.getStartTime() * baseunit ), o.y - this.taskHeight, (int)(te.getExecutionTime() * baseunit), this.taskHeight);
                
                { //TaskLine
                    g.drawRect((int)(o.x + te.getStartTime() * baseunit ), yHeight - 16, (int)(te.getExecutionTime() * baseunit), 16);
                    g.drawLine((int)(o.x + te.getStartTime() * baseunit ), yHeight, (int)(o.x + te.getStartTime() * baseunit), yHeight + 5);
                    g.setColor(resourceColor[19-(te.getTaskID()%19)-1]);
                    g.fillRect((int)(o.x + te.getStartTime() * baseunit)+1, yHeight - 15, (int)(te.getExecutionTime() * baseunit) - 1, 14);
                    g.setColor(reverseColor(resourceColor[19 - (te.getTaskID()%19)-1]));

                    char[] data = String.valueOf(te.getTaskID()+","+te.getJobID()).toCharArray();

                    if((int)(te.getExecutionTime() * baseunit) > data.length * 8)
                    {
                        g.drawChars(data, 0, data.length, (int)(o.x + te.getStartTime() * baseunit) + 2, yHeight - 2);
                    }
                    else if((int)((te.getExecutionTime() * baseunit) / 8) > 1)
                    {
                        g.drawChars(data, 0, (int)((te.getExecutionTime() * baseunit) / 8), (int)(o.x + te.getStartTime() * baseunit) + 2, yHeight - 2);
                    }

                    g.setColor(Color.black);
                }
                
                DecimalFormat df = new DecimalFormat("##.00");
                double time = Double.parseDouble(df.format(te.getStartTime()));
                
                if(((int)(time*10)%10)!=0)
                {
                    g.drawString(""+ time, (int)(o.x - 4 + te.getStartTime() * baseunit), o.y + 40);
                    g.drawLine((int)(o.x + te.getStartTime() * baseunit), o.y, (int)(o.x + te.getStartTime() * baseunit), o.y + 25);
                }

                time = Double.parseDouble(df.format(te.getEndTime()));

                if(( (int)(time * 10) % 10) != 0)
                {
                    g.drawString(""+ time, (int)(o.x - 4 + te.getEndTime() * baseunit), o.y + 40);
                    g.drawLine((int)(o.x + te.getEndTime() * baseunit), o.y, (int)(o.x + te.getEndTime() * baseunit), o.y + 25);
                }
            }
        }
        g.drawString("Core" + ID, o.x - 50, o.y);
        g.drawString("Time", (int)(o.x + baseunit * (finalTime + 1) + 15), o.y + 5);
    }

    /**
     * Adds visual resource usage panels for each EXECUTION segment into the
     * provided timeline result container and positions them (without drawing
     * their contents). Invoked prior to a paint pass so that the UI components
     * are laid out.
     *
     * @param rv the container accumulating resource panels for later rendering
     */
    public void drawResources(TimeLineResult rv)
    {
        for(TaskExecution te : executions)
        {
            if(te.getStatus() == CoreStatus.EXECUTION)
            {
                int i=0;
                for(ResourcePanel reP : te.getResourcePanels())
                {
                    i++;
                    rv.add(reP);
                    reP.setBounds((int)(o.x + te.getStartTime() * parent.getBaseunit()), o.y - i * this.resourceHeight, (int)(te.getExecutionTime() * parent.getBaseunit()), this.resourceHeight);
                }
            }
        }
    }

    /**
     * Repositions and redraws resource usage rectangles for EXECUTION segments
     * directly onto the supplied graphics context, also writing textual labels
     * (resource set ID, instance number, and total amount) when width allows.
     * Typically called during repaint to refresh visual alignment after scale
     * or size changes.
     *
     * @param rv the timeline result container holding existing resource panels
     * @param g  the graphics context used for immediate painting
     */
    public void reDrawResources(TimeLineResult rv ,Graphics g)
    {
        int baseunit = parent.getBaseunit();
        
        for(TaskExecution te : executions)
        {
            if(te.getStatus() == CoreStatus.EXECUTION)
            {
                int i=0;
                for(ResourcePanel reP : te.getResourcePanels())
                {
                    i++;
                    reP.setBounds((int)(o.x + te.getStartTime() * baseunit), o.y-i * this.resourceHeight, (int)(te.getExecutionTime() * baseunit), this.resourceHeight);
                  
                    g.setColor(resourceColor[ Integer.parseInt(reP.getResourcesID()) -1]);
                    
                    g.fillRect((int)(o.x + te.getStartTime() * baseunit)+1, o.y-i * this.resourceHeight, (int)(te.getExecutionTime() * baseunit) - 1, this.resourceHeight-1);
                    g.setColor(reverseColor(resourceColor[ Integer.parseInt(reP.getResourcesID()) -1]));
    
                    char[] data = String.valueOf("R"+reP.getResourcesID()+"(" + reP.getResourceID() +"/" + reP.getResourcesAmount() +")").toCharArray();

                    if((int)(te.getExecutionTime() * baseunit) > data.length * 8)
                    {
                        g.drawChars(data, 0, data.length, (int)(o.x + te.getStartTime() * baseunit) + 2, o.y - i * this.resourceHeight + this.resourceHeight - 2);
                    }
                    else if((int)((te.getExecutionTime() * baseunit) / 8) > 1)
                    {
                            g.drawChars(data, 0, (int)((te.getExecutionTime() * baseunit) / 8), (int)(o.x + te.getStartTime() * baseunit) + 2, o.y - i * this.resourceHeight + this.resourceHeight - 2);
                    }
                    g.setColor(Color.BLACK);
                }
            }
        }
    }

    /**
     * Produces a contrasting color by inverting the RGB channels of the given
     * color (simple {@code 255 - component} inversion) to ensure readable text
     * over filled rectangles.
     *
     * @param color the original color
     * @return the inverted (reverse) color
     */
    public Color reverseColor(Color color)
    {  
        int r = color.getRed();  
        int g = color.getGreen();  
        int b = color.getBlue();  
        int r_ = 255-r;  
        int g_ = 255-g;  
        int b_ = 255-b;  

        Color newColor = new Color(r_,g_,b_);  

        return newColor;  
    }  
}
