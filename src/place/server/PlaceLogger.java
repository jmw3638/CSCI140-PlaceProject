package place.server;

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
     * Outputs a log message to the console.
     * @param type the type of log message
     * @param className the class the log message originated
     * @param msg the actual log message to output
     */
    public static void log(LogType type, String className, String msg) {
        switch(type) {
            case INFO:
                if(INFO) { System.out.println(ANSI_GREEN + "[INFO] " + className + " | " + msg + ANSI_RESET); }
                break;
            case WARN:
                if(WARN) { System.out.println(ANSI_YELLOW + "[WARN] " + className + " | " + msg + ANSI_RESET); }
                break;
            case ERROR:
                if(ERROR) { System.out.println(ANSI_RED + "[ERROR] " + className + " | " + msg + ANSI_RESET); }
                break;
            case FATAL:
                System.out.println(ANSI_PURPLE + "[ERROR] " + className + " | " + msg + ANSI_RESET);
                System.exit(1);
                break;
            case DEBUG:
                if(DEBUG) { System.out.println(ANSI_BLUE + "[DEBUG] " + className + " | " + msg + ANSI_RESET); }
                break;
            default:
                System.out.println(ANSI_RED + "[ERROR] " + PlaceLogger.class.getName() + " | Invalid logging type" + ANSI_RESET);
                break;
        }
    }
}
