package place;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * A custom class for logging and debugging the place project.
 * Different types of console outputs can be enabled or disabled
 * for different levels of debugging.
 *
 * @author Jake Waclawski
 */
public class PlaceLogger {
    /** enable or disable debugging messages */
    private static final boolean DEBUG = true;
    /** enable or disable error messages */
    private static final boolean ERROR = true;
    /** enable or disable warning messages */
    private static final boolean WARN = true;
    /** enable or disable general information messages */
    private static final boolean INFO = true;

    /** the time format for displaying the current time */
    private static final DateFormat TIME_FORMAT = new SimpleDateFormat("h:mm:ss.SSS");

    /**
     * Represents different console log output types. Each
     * type prints out the message in a different color to
     * distinguish it from the rest.
     */
    public enum LogType {
        /**
         * Represents a general information log output
         */
        INFO,

        /**
         * Represents a warning log output
         */
        WARN,

        /**
         * Represents a error log output
         */
        ERROR,

        /**
         * Represents a fatal error log output
         */
        FATAL,

        /**
         * Represents a debugging log output
         */
        DEBUG
    }

    /** Resets the console text color */
    private static final String ANSI_RESET = "\u001B[0m";
    /** Sets the console text color to red */
    private static final String ANSI_RED = "\u001B[31m";
    /** Sets the console text color to green */
    private static final String ANSI_GREEN = "\u001B[32m";
    /** Sets the console text color to yellow */
    private static final String ANSI_YELLOW = "\u001B[33m";
    /** Sets the console text color to blue */
    private static final String ANSI_BLUE = "\u001B[34m";
    /** Sets the console text color to purple */
    private static final String ANSI_PURPLE = "\u001B[35m";

    /**
     * Outputs a log message to the console, without a line number given (setting it to 0 by default)
     * @param type the type of log message
     * @param className the class the log message originated
     * @param msg the actual log message to output
     */
    public static void log(LogType type, String className, String msg) { log(type, className, 0, msg); }

    /**
     * Outputs a log message to the console.
     * @param type the type of log message
     * @param className the class the log message originated
     * @param line the line number the log message originated
     * @param msg the actual log message to output
     */
    public static void log(LogType type, String className, int line, String msg) {
        String lineNumber = ":" + line;
        if(line == 0) { lineNumber = ""; }
        String time = TIME_FORMAT.format(new Date());
        switch(type) {
            case INFO:
                if(INFO) { System.out.println(ANSI_GREEN + "INFO  | " + time + " | " + className + lineNumber + " > " + msg + ANSI_RESET); }
                break;
            case WARN:
                if(WARN) { System.out.println(ANSI_YELLOW + "WARN  | " + time + " | " + className + lineNumber + " > " + msg + ANSI_RESET); }
                break;
            case ERROR:
                if(ERROR) { System.out.println(ANSI_RED + "ERROR | " + time + " | " + className + lineNumber + " > " + msg + ANSI_RESET); }
                break;
            case FATAL:
                System.out.println(ANSI_PURPLE + "ERROR | " + time + " | " + className + lineNumber + " > " + msg + ANSI_RESET);
                System.exit(1);
                break;
            case DEBUG:
                if(DEBUG) { System.out.println(ANSI_BLUE + "DEBUG | " + time + " | " + className + lineNumber + " > " + msg + ANSI_RESET); }
                break;
            default:
                System.out.println(ANSI_RED + "UNKNOWN | " + time + " | " + className + lineNumber + " > " + msg + ANSI_RESET);
                break;
        }
    }

    /**
     * Get the current line number
     * @return the line number
     */
    public static int getLineNumber() { return Thread.currentThread().getStackTrace()[2].getLineNumber(); }
}
