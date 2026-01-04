package RTSimulator;

import java.util.logging.Level;
import java.util.logging.Logger;
import userInterface.UserInterface;

/**
 * Main class for RTSimulator application.
 * <p>
 * This class serves as the entry point for the RTSimulator application and provides
 * static utility methods for debug output. It contains the main method that initializes
 * the user interface.
 * 
 * @author ShiuJia
 */
public class RTSimulator {

    // Logger for debug and information messages
    private static final Logger LOGGER = Logger.getLogger(RTSimulator.class.getName());
    private static final boolean DEBUG_MODE = true; // Set to true for debug messages

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private RTSimulator() {
        throw new UnsupportedOperationException("Utility class - do not instantiate");
    }

    /**
     * Prints the specified message to standard output when {@code DEBUG_MODE}
     * is enabled.
     *
     * <p>This is a convenience helper used for lightweight debug output that
     * should be suppressed when {@code DEBUG_MODE} is {@code false}.</p>
     *
     * @param message the message to print; may be {@code null}
     */
    public static void print(String message) {
        if (DEBUG_MODE) {
            System.out.print(message);
        }
    }


    /**
     * Prints the specified message followed by a newline to standard output
     * when {@code DEBUG_MODE} is enabled.
     *
     * <p>Use this helper to emit a line of debug output. The newline emitted is
     * the platform default supplied by {@link System#out}.</p>
     *
     * @param message the message to print; may be {@code null}
     */
    public static void println(String message) {
        if (DEBUG_MODE) {
            System.out.println(message);
        }
    }

    /**
     * Prints a blank line to standard output when {@code DEBUG_MODE} is enabled.
     *
     * <p>Useful for separating blocks of debug output for readability.</p>
     */
    public static void println() {
        if (DEBUG_MODE) {
            System.out.println();
        }
    }

    /**
     * Main method - entry point of the RTSimulator application.
     *
     * <p>Logs startup information, attempts to initialize the user interface,
     * and records success or failure using the class {@link Logger}.</p>
     *
     * @param args the command line arguments (currently ignored)
     */
    public static void main(String[] args) {
        LOGGER.log(Level.INFO, "Starting RTSimulator application...");

        try {
            UserInterface ui = new UserInterface();
            LOGGER.log(Level.INFO, "User interface initialized successfully.");
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Failed to initialize the user interface.", ex);
        }
    }
}
