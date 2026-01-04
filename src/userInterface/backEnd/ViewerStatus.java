/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package userInterface.backEnd;

/**
 * Defines the runtime status of a UI viewer component in the back-end layer.
 * <p>
 * This enumeration is typically used to indicate whether the viewer is idle or
 * currently executing a task (for example, running a simulation or rendering a
 * result). It can be consulted by UI controls to enable/disable interactions or
 * to update progress and status indicators accordingly.
 * </p>
 *
 * @author ShiuJia
 */
public enum ViewerStatus
{
    /**
     * The viewer is not performing any processing or simulation and is ready to
     * accept new commands or inputs.
     */
    IDLE,

    /**
     * The viewer is actively executing an operation, such as processing data,
     * running a simulation, or rendering results; some interactions may be
     * restricted while execution is in progress.
     */
    EXECUTION
}