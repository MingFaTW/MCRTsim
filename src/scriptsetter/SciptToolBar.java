/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package scriptsetter;

import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.FileNotFoundException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JToolBar;

/**
 * A toolbar used by the {@link ScriptSetter} UI that provides quick actions to
 * manage script groups and scripts and to start script execution.
 *
 * <p>This toolbar contains buttons for adding/removing groups, adding/modifying/removing
 * scripts, and starting the selected script. Each button's mouse listener delegates
 * the action to the associated {@link ScriptSetter} instance referenced by the
 * {@link #parent} field. The original file header template and the {@code @author}
 * tag are preserved.</p>
 *
 * @author YC
 */
public class SciptToolBar extends JToolBar
{
	 /**
     * The parent {@link ScriptSetter} instance that receives delegated actions from
     * this toolbar (for example {@code addGroup()}, {@code removeGroup()},
     * {@code addScript()}, {@code modifyScript()}, {@code removeScript()},
     * and {@code startScript()}).
     *
     * <p>This field is public to allow the containing UI code to access or replace
     * the parent reference directly when necessary.</p>
     */
    public ScriptSetter parent;
    /** Button for adding a new script group. */
    private JButton addGroupBtn;
    /** Button for removing a script group. */
    private JButton removeGroupBtn;
    /** Button for adding a new script. */
    private JButton addSciptBtn;
    /** Button for modifying an existing script. */
    private JButton modifySciptBtn;
    /** Button for removing a script. */
    private JButton removeSciptBtn;
    /** Button for starting script execution. */
    private JButton startBtn;
    
    /**
     * Constructs a new {@code SciptToolBar} attached to the given {@link ScriptSetter}.
     *
     * <p>This constructor stores the provided parent reference, initializes the
     * toolbar UI (buttons, layout, colors), registers mouse listeners on each
     * button that delegate the corresponding action to {@code parent}, and
     * calls {@code revalidate()} to refresh the UI. Note that the listener for
     * the Start button calls {@link ScriptSetter#startScript()} and catches
     * {@link FileNotFoundException}, logging it via {@link Logger}.</p>
     *
     * @param s the parent {@link ScriptSetter} that will receive delegated toolbar actions; must not be {@code null}
     */
    public SciptToolBar(ScriptSetter s)
    {
        this.parent = s;
        this.init();
        this.revalidate();
        
        this.addGroupBtn.addMouseListener
        (
            new MouseAdapter()
            {
                public void mouseClicked(MouseEvent e)
                {
                    SciptToolBar.this.parent.addGroup();
                }
            }
        );
        
        this.removeGroupBtn.addMouseListener
        (
            new MouseAdapter()
            {
                public void mouseClicked(MouseEvent e)
                {
                    SciptToolBar.this.parent.removeGroup();
                }
            }
        );
        
        
        this.addSciptBtn.addMouseListener
        (
            new MouseAdapter()
            {
                public void mouseClicked(MouseEvent e)
                {
                    SciptToolBar.this.parent.addScript();
                }
            }
        );
        
        this.modifySciptBtn.addMouseListener
        (
            new MouseAdapter()
            {
                public void mouseClicked(MouseEvent e)
                {
                    SciptToolBar.this.parent.modifyScript();
                }
            }
        );
        
        this.removeSciptBtn.addMouseListener
        (
            new MouseAdapter()
            {
                public void mouseClicked(MouseEvent e)
                {
                    SciptToolBar.this.parent.removeScript();
                }
            }
        );
        
        this.startBtn.addMouseListener
        (
            new MouseAdapter()
            {
                public void mouseClicked(MouseEvent e)
                {
                    try {
                        SciptToolBar.this.parent.startScript();
                    } catch (FileNotFoundException ex) {
                        Logger.getLogger(SciptToolBar.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        );
    }
    
    private void init()
    {
        this.setFloatable(false);
        this.setLayout(new GridLayout(3,2));
        
        this.addGroupBtn = new JButton("AddGroup");
        this.removeGroupBtn = new JButton("RemoveGroup");
        this.addSciptBtn = new JButton("AddScipt");
        this.modifySciptBtn = new JButton("ModifyScipt");
        this.removeSciptBtn = new JButton("RemoveScipt");
        this.startBtn = new JButton("Start Script");
        this.startBtn.setForeground(Color.red);
        this.add(this.addGroupBtn);
        this.add(this.removeGroupBtn);
        this.add(this.addSciptBtn);
        this.add(this.removeSciptBtn);
        this.add(this.modifySciptBtn);
        this.add(this.startBtn);
    }
}
