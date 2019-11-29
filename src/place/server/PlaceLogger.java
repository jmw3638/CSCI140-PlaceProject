package place.server;

public class PlaceLogger {
    private static final boolean DEBUG = true;
    private static final boolean ERROR = true;
    private static final boolean WARN = true;
    private static final boolean INFO = true;

    public enum LogType {
        INFO,
        WARN,
        ERROR,
        DEBUG
    }

    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_GREEN = "\u001B[32m";
    private static final String ANSI_YELLOW = "\u001B[33m";
    private static final String ANSI_BLUE = "\u001B[34m";
    private static final String ANSI_PURPLE = "\u001B[35m";

    public static void log(LogType type, String className, String msg) {
        switch(type) {
            case INFO:
                if(INFO) { System.out.println(ANSI_GREEN + "INFO : " + className + " : " + msg + ANSI_RESET); }
                break;
            case WARN:
                if(WARN) { System.out.println(ANSI_YELLOW + "WARN : " + className + " : " + msg + ANSI_RESET); }
                break;
            case ERROR:
                if(ERROR) { System.out.println(ANSI_RED + "ERROR : " + className + " : " + msg + ANSI_RESET); }
                break;
            case DEBUG:
                if(DEBUG) { System.out.println(ANSI_BLUE + "DEBUG : " + className + " : " + msg + ANSI_RESET); }
                break;
            default:
                System.out.println(ANSI_PURPLE + "ERROR : " + PlaceLogger.class.getName() + " : Invalid logging type" + ANSI_RESET);
                break;
        }
    }
}
