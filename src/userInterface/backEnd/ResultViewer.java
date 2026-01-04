/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package userInterface.backEnd;

import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.util.Vector;
import javax.swing.JScrollPane;
import SystemEnvironment.Core;
import userInterface.frontEnd.InfoWin;

/**
 * Scrollable container that hosts timeline visualization panels for scheduling results.
 *
 * <p>{@code ResultViewer} wraps a {@link TimeLineResult} (placed in the viewport)
 * and coordinates creation of a {@link ScheduleResult} from a set of cores. Depending
 * on the supplied type parameter ("AllTasks" or "AllCores") it initializes either
 * a task-centric timeline (aggregating execution by tasks/jobs) or a core-centric
 * timeline (one lane per core). A second constructor supports viewing a single core.</p>
 *
 * <p>Scrolling behavior: horizontal and vertical scroll bars are always shown; unit
 * increments are set for smoother navigation, and repaint listeners are attached so
 * the timeline graphics are refreshed whenever the user scrolls.</p>
 *
 * <p>The public field {@link #parent} references the owning {@link InfoWin}, enabling
 * higher-level UI components to interact (e.g., update or retrieve scale). The public
 * {@link #scale} field represents a zoom factor used by downstream rendering code
 * (initial value = 1). Getters expose cores, schedule and timeline objects for
 * further customization or inspection.</p>
 *
 * @author YC
 */
public class ResultViewer extends JScrollPane {
    /**
     * Parent information window that owns this viewer; used for coordination with
     * front-end UI components (logging/output panels).
     */
    public InfoWin parent;
    private Vector<Core> Cores;
    private ScheduleResult sr;
    private TimeLineResult tlr;
    /**
     * Horizontal/vertical scaling factor applied by timeline drawing logic (e.g.
     * base unit multiplication). Starts at {@code 1}; may be adjusted through
     * {@link #setScale(int)}.
     */
    public int scale;

    /**
     * Constructs a result viewer displaying either all tasks or all cores aggregated
     * from the provided core list, and initializes the appropriate timeline schedule.
     *
     * <p>Type handling:
     * <ul>
     *   <li>"AllTasks" → invokes {@link ScheduleResult#startTaskTimeLineSchedule()}.</li>
     *   <li>"AllCores" → invokes {@link ScheduleResult#startCoreTimeLineSchedule()}.</li>
     * </ul>
     * After schedule initialization, a {@link TimeLineResult} is created and set as
     * the viewport view.
     *
     * @param win owning information window
     * @param cores collection of cores supplying scheduling information
     * @param type visualization mode selector ("AllTasks" or "AllCores")
     */
    public ResultViewer(InfoWin win , Vector<Core> cores , String type)
    {
        this.parent = win;
        this.init();
        this.Cores = new Vector<>();
        this.Cores.addAll(cores);
        this.sr = new ScheduleResult(this);
        
        if(type.equals("AllTasks"))
        {
            this.sr.startTaskTimeLineSchedule();
        }
        else if(type.equals("AllCores"))
        {
            this.sr.startCoreTimeLineSchedule();
        }
        
        this.tlr = new TimeLineResult(sr);
        this.setViewportView(tlr);
    }
    
    /**
     * Constructs a result viewer focused on a single core (task-centric schedule).
     * Internally initializes a {@link ScheduleResult} and starts a task timeline
     * schedule, then wraps it in a {@link TimeLineResult}.
     *
     * @param win owning information window
     * @param c the single core to visualize
     */
    public ResultViewer(InfoWin win , Core c)
    {
        this.parent = win;
        this.init();
        this.Cores = new Vector<>();
        this.Cores.add(c);
        this.sr = new ScheduleResult(this);
        this.sr.startTaskTimeLineSchedule();
        this.tlr = new TimeLineResult(sr);
        this.setViewportView(tlr);
    }
    
    /**
     * Initializes scroll pane policies, unit increments, default scale, and attaches
     * listeners to repaint the timeline when scroll positions change.
     */
    private void init()
    {
        this.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        this.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        this.getHorizontalScrollBar().setUnitIncrement(16);
        this.getVerticalScrollBar().setUnitIncrement(16);
        this.scale = 1 ;
        
        this.getHorizontalScrollBar().addAdjustmentListener
        (new AdjustmentListener()
            {
                public void adjustmentValueChanged(AdjustmentEvent e)
                {
                    ResultViewer.this.tlr.repaint();
                }
            }
        );

        this.getVerticalScrollBar().addAdjustmentListener
        (new AdjustmentListener()
            {
                public void adjustmentValueChanged(AdjustmentEvent e)
                {
                    ResultViewer.this.tlr.repaint();
                }
            }
        );
    }
    
    /**
     * Returns the vector of cores currently represented in this viewer.
     *
     * @return cores collection (modifiable via returned {@link Vector})
     */
    public Vector<Core> getCores()
    {
        return this.Cores;
    }
    
    /**
     * Returns a specific core by index from the internal core collection.
     *
     * @param i zero-based index into the core vector
     * @return the core at the specified position
     * @throws IndexOutOfBoundsException if {@code i} is outside collection bounds
     */
    public Core getCore(int i)
    {
        return this.Cores.get(i);
    }
    
    /**
     * Returns the schedule result model backing this viewer.
     *
     * @return scheduling result instance
     */
    public ScheduleResult getScheduleResult()
    {
        return this.sr;
    }
    
    /**
     * Returns the timeline result component currently displayed in the viewport.
     *
     * @return timeline result panel
     */
    public TimeLineResult getTimeLineResult()
    {
        return this.tlr;
    }
    
    /**
     * Returns the current zoom/scale factor used by timeline drawing logic.
     *
     * @return integer scale value
     */
    public int getScale()
    {
        return this.scale;
    }
    
    /**
     * Sets the zoom/scale factor for timeline rendering.
     *
     * @param i new scale value (positive integers expected by drawing code)
     */
    public void setScale(int i)
    {
        this.scale = i;
    }
   
}