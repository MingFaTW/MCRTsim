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
import WorkLoad.Task;
import RTSimulator.Definition.CoreStatus;
import static RTSimulator.Definition.magnificationFactor;

/**
 * Timeline renderer for a single Task used by the back-end visualization.
 *
 * <p>A {@code TaskTimeLine} draws one horizontal band per task where
 * execution-related intervals (EXECUTION, WAIT, CONTEXTSWITCH, MIGRATION,
 * WRONG) are rendered as colored rectangles. The origin point {@code o}
 * ("時間軸起始位置") defines the start of the time axis. When the parent viewer
 * is in multi-core mode, a secondary "Core" lane is rendered beneath the
 * task band to show the core ID associated with each interval.</p>
 *
 * <p>Integrated behavior from inline comments:
 * <ul>
 *   <li>Draws time axes and tick marks (畫時間軸/畫時間軸刻度), labeling fewer
 *       ticks when zoomed out to reduce clutter.</li>
 *   <li>Calls {@link #drawPeriod(Graphics)} to render period and relative
 *       deadline markers for the task.</li>
 *   <li>Uses a predefined HSB color palette to draw core or resource segments
 *       and {@link #reverseColor(Color)} for contrasting text.</li>
 *   <li>Labels fractional start/end times using two-decimal formatting when
 *       the first decimal digit is non-zero.</li>
 * </ul>
 *
 * @author ShiuJia
 */
public class TaskTimeLine
{
    int resourceHeight=13;
    int taskHeight = 80;
    Point o; //時間軸起始位置
    int ID;
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
     * Creates an empty timeline; callers typically use the parameterized
     * constructor to set parent, position and task ID.
     */
    public TaskTimeLine()
    {
        
    }

    /**
     * Constructs a task timeline bound to a {@link ScheduleResult}, assigns
     * a numeric ID and computes its vertical origin based on the parent's
     * task gap.
     *
     * @param sr the owning schedule result used for scale and data access
     * @param i  zero-based vertical index of this task band
     * @param id textual task identifier (parsed to integer and stored in {@code ID})
     * @throws NumberFormatException if {@code id} cannot be parsed as an integer
     */
    public TaskTimeLine(ScheduleResult sr, int i, String id)
    {
        parent = sr;
        //ID = new Integer(id).intValue();
        ID = Integer.parseInt(id);
        o = new Point(100, 200 + i * this.parent.getTaskGap());
        executions = new ArrayList<TaskExecution>();
    }

    /**
     * Appends an execution-related segment to this task's timeline.
     * Integrates the inline note (E, X 狀態) to indicate typical usage for
     * EXECUTION and other statuses.
     *
     * @param te the execution segment to add
     */
    public void addExecution(TaskExecution te) //E,X 狀態
    {
        executions.add(te);
    }
    
    /**
     * Paints the complete task timeline onto the given {@link Graphics} context.
     *
     * <p>Steps:</p>
     * <ol>
     *   <li>Draw the secondary "Core" lane when in multi-core mode (with arrow
     *       ends and tick marks).</li>
     *   <li>Draw the main time axis for the task band (with arrow ends).</li>
     *   <li>Render period/deadline markers via {@link #drawPeriod(Graphics)}.</li>
     *   <li>Render tick marks and numeric labels; label density depends on
     *       parent scale to avoid clutter.</li>
     *   <li>For each {@link TaskExecution}, draw a rectangle depending on
     *       {@link CoreStatus}: MIGRATION (blue, half height), CONTEXTSWITCH
     *       (light gray, half height), WAIT (filled), WRONG (red 'X'),
     *       EXECUTION (outlined). When multi-core, also draw a small bar in the
     *       core lane tinted by core ID and optionally print the core ID.
     *       Fractional start/end timestamps are labeled when significant.</li>
     * </ol>
     *
     * @param g the graphics context to draw into
     */
    public void drawItself(Graphics g)
    {
        int baseunit = this.parent.getBaseunit();
        double finalTime = this.parent.getFinalTime();
        int yHeight = o.y+65;
        
        if(this.parent.isMultiCore)
        {
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
            
            g.drawString("Core", o.x - 50, yHeight);
        }
        
        for(int i = -1 ; i<=1 ; i++) //畫時間軸
        {
            //println("X = "+o.x +", Y = " + o.y+", finalTime = "+finalTime);
            
            g.drawLine(o.x, o.y + i, (int)(o.x + baseunit * (finalTime + 1)), o.y + i);

            g.drawLine((int)(o.x + baseunit * (finalTime + 1)), o.y + i, 
                           (int)(o.x + baseunit * (finalTime + 1) - 10), o.y + i + 10);
            g.drawLine((int)(o.x + baseunit * (finalTime+1)),  o.y+i, 
                           (int)(o.x + baseunit * (finalTime+ 1 ) - 10), o.y + i - 10);
        }
        
        drawPeriod(g);

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
                
                if(this.parent.isMultiCore)//Coreline
                {
                    g.drawRect((int)(o.x + te.getStartTime() * baseunit ), yHeight - 16, (int)(te.getExecutionTime() * baseunit), 16);
                    g.drawLine((int)(o.x + te.getStartTime() * baseunit ), yHeight, (int)(o.x + te.getStartTime() * baseunit), yHeight + 5);
                    
                    g.setColor(resourceColor[19 - (te.getCoreID()%19)-1]);
                    g.fillRect((int)(o.x + te.getStartTime() * baseunit)+1, yHeight - 15, (int)(te.getExecutionTime() * baseunit) - 1, 14);
                    g.setColor(reverseColor(resourceColor[19 - (te.getCoreID()%19)-1]));
    
                    char[] data = String.valueOf(te.getCoreID()).toCharArray();

                    if((int)(te.getExecutionTime() * baseunit) > data.length * 8)
                    {
                        g.drawChars(data, 0, data.length, (int)(o.x + te.getStartTime() * baseunit) + 2, yHeight - 2);
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
                
                if(this.parent.isMultiCore)//Coreline
                {
                    g.drawRect((int)(o.x + te.getStartTime() * baseunit ), yHeight - 16, (int)(te.getExecutionTime() * baseunit), 16);
                    g.drawLine((int)(o.x + te.getStartTime() * baseunit ), yHeight, (int)(o.x + te.getStartTime() * baseunit), yHeight + 5);
                    
                    g.setColor(resourceColor[19 - (te.getCoreID()%19)-1]);
                    g.fillRect((int)(o.x + te.getStartTime() * baseunit)+1, yHeight - 15, (int)(te.getExecutionTime() * baseunit) - 1, 14);
                    g.setColor(reverseColor(resourceColor[19 - (te.getCoreID()%19)-1]));
    
                    char[] data = String.valueOf(te.getCoreID()).toCharArray();

                    if((int)(te.getExecutionTime() * baseunit) > data.length * 8)
                    {
                        g.drawChars(data, 0, data.length, (int)(o.x + te.getStartTime() * baseunit) + 2, yHeight - 2);
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
                
                if(this.parent.isMultiCore)//Coreline
                {
                    g.drawRect((int)(o.x + te.getStartTime() * baseunit ), yHeight - 16, (int)(te.getExecutionTime() * baseunit), 16);
                    g.drawLine((int)(o.x + te.getStartTime() * baseunit ), yHeight, (int)(o.x + te.getStartTime() * baseunit), yHeight + 5);
                    
                    g.setColor(resourceColor[19 - (te.getCoreID()%19)-1]);
                    g.fillRect((int)(o.x + te.getStartTime() * baseunit)+1, yHeight - 15, (int)(te.getExecutionTime() * baseunit) - 1, 14);
                    g.setColor(reverseColor(resourceColor[19 - (te.getCoreID()%19)-1]));
    
                    char[] data = String.valueOf(te.getCoreID()).toCharArray();

                    if((int)(te.getExecutionTime() * baseunit) > data.length * 8)
                    {
                        g.drawChars(data, 0, data.length, (int)(o.x + te.getStartTime() * baseunit) + 2, yHeight - 2);
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
               // println("!! MissDeadline !!" + te.getStartTime());
        
            }
            else if( te.getStatus() == CoreStatus.EXECUTION )
            {
                g.drawRect((int)(o.x + te.getStartTime() * baseunit ), o.y - this.taskHeight, (int)(te.getExecutionTime() * baseunit), this.taskHeight);
                
                if(this.parent.isMultiCore)//Coreline
                {
                    g.drawRect((int)(o.x + te.getStartTime() * baseunit ), yHeight - 16, (int)(te.getExecutionTime() * baseunit), 16);
                    g.drawLine((int)(o.x + te.getStartTime() * baseunit ), yHeight, (int)(o.x + te.getStartTime() * baseunit), yHeight + 5);
                    
                    g.setColor(resourceColor[19 - (te.getCoreID()%19)-1]);
                    g.fillRect((int)(o.x + te.getStartTime() * baseunit)+1, yHeight - 15, (int)(te.getExecutionTime() * baseunit) - 1, 14);
                    g.setColor(reverseColor(resourceColor[19 - (te.getCoreID()%19)-1]));
    
                    char[] data = String.valueOf(te.getCoreID()).toCharArray();

                    if((int)(te.getExecutionTime() * baseunit) > data.length * 8)
                    {
                        g.drawChars(data, 0, data.length, (int)(o.x + te.getStartTime() * baseunit) + 2, yHeight - 2);
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

        g.drawString("Task" + ID, o.x - 50, o.y);
        g.drawString("Time", (int)(o.x + baseunit * (finalTime + 1) + 15), o.y + 5);
    }

    /**
     * Draws period start markers (triangles) and relative deadline markers for
     * the task across the timeline using the parent's data setting. Period and
     * deadline values are converted from simulator time using
     * the magnification factor.
     *
     * @param g the graphics context to draw into
     */
    public void drawPeriod(Graphics g)
    {
        int baseunit = this.parent.getBaseunit();
        double finalTime = this.parent.getFinalTime();
        Task task = this.parent.parent.parent.getDataSetting().getTaskSet().getTask(this.ID-1);
        double curPeriod = 0 + ((double)task.getEnterTime() / magnificationFactor);
        double curDeadline = curPeriod - ((double)(task.getPeriod() - task.getRelativeDeadline()) / magnificationFactor);
        
        for(int j = 0 ; j < (finalTime / ((double)task.getPeriod() / magnificationFactor)) ; j++)
        {  
            if(curPeriod <= finalTime)
            {
                //g.setColor(g.getColor() == Color.BLUE ? Color.RED : Color.BLUE); //紅藍標記
                g.setColor(Color.BLUE);
                g.fillRect(o.x-1+(int)(baseunit * curPeriod), o.y-115, 3, 15);
                for(int k=1 ; k<3 ; k++)
                {
                    g.drawLine(o.x+(int)(baseunit * curPeriod), o.y-115-k, o.x-5 + (int)(baseunit * curPeriod), o.y-112-k);
                    g.drawLine(o.x+(int)(baseunit * curPeriod), o.y-115-k, o.x+5 + (int)(baseunit * curPeriod), o.y-112-k);
                }
            }

            curPeriod +=  ((double)task.getPeriod() / magnificationFactor);
            curDeadline = curPeriod - ((double)(task.getPeriod() - task.getRelativeDeadline()) / magnificationFactor);
            
            if(curDeadline <= finalTime)
            {
                g.fillRect(o.x-1 + (int)(baseunit * curDeadline), o.y-100, 3, 15);
                for(int k=1 ; k<3 ; k++)
                {
                    g.drawLine(o.x + (int)(baseunit * curDeadline), o.y-85-k, o.x-5+ (int)(baseunit * curDeadline), o.y-88-k);
                    g.drawLine(o.x + (int)(baseunit * curDeadline), o.y-85-k, o.x+5+ (int)(baseunit * curDeadline), o.y-88-k);
                }
            }
        }
        
        g.setColor(Color.BLACK);
    }

    /**
     * Adds resource panels (one per locked resource within an EXECUTION segment)
     * to the provided {@link TimeLineResult} container and positions them. These
     * panels are standard Swing components that will be painted by the UI.
     *
     * @param rv the timeline container which hosts resource panels
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
     * Repositions resource panels and immediately paints their rectangles and
     * labels using the supplied graphics context. Colors are selected from
     * {@link #resourceColor} by resource set ID; text color is inverted with
     * {@link #reverseColor(Color)} for readability inside narrow bars.
     *
     * @param rv the timeline container holding resource panels
     * @param g  graphics context used for immediate painting
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
                  
                    //g.setColor(resourceColor[ new Integer(reP.getResourcesID()).intValue() -1]);
                    g.setColor(resourceColor[Integer.parseInt(reP.getResourcesID()) - 1]);
                    g.fillRect((int)(o.x + te.getStartTime() * baseunit)+1, o.y-i * this.resourceHeight, (int)(te.getExecutionTime() * baseunit) - 1, this.resourceHeight-1);
                    //g.setColor(reverseColor(resourceColor[ new Integer(reP.getResourcesID()).intValue() -1]));
                    g.setColor(reverseColor(resourceColor[Integer.parseInt(reP.getResourcesID()) - 1]));
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
     * @return inverted color for drawing text or outlines
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

    /**
     * Returns the number of recorded execution segments in this timeline.
     *
     * @return execution segment count
     */
    public int getExecutionNumber()
    {
        return this.executions.size();
    }
}