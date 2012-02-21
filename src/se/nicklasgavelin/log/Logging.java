package se.nicklasgavelin.log;

import java.util.*;

/**
 * Manages the logging of the application.
 * If the log4j logging class can't be found the logging will be
 * disabled by default. Otherwise the logging will follow
 * the settings in the Configuration class
 *
 * @author Nicklas Gavelin, nicklas.gavelin@gmail.com, Luleå University of
 * Technology
 * @version 2.0
 */
public class Logging
{
    private static final Logging log = new Logging();
    private static boolean initialized = false;
    private static Collection<Appender> logAppenders;
    private static boolean log4exists = true;
    private static final String log4logger = "site.nicklas.log.Log4JLogger";
    private static final String from = Logging.class.getName();
    private static final Collection<String> fromCollection = new ArrayList<String>();


    static
    {
        fromCollection.add( from );
    }

    /**
     * Different debug levels,
     * mirrors that of log4j levels
     */
    public enum Level
    {
        DEBUG,
        INFO,
        ERROR,
        WARN,
        FATAL;
    }


    /**
     * Create a logging object
     */
    private Logging()
    {
    }


    /**
     * Returns the log instance
     *
     * @return The log instance
     */
    public static Logging getInstance()
    {
        return log;
    }

    /*
     * ************************************
     * CLASSES
     */
    public static interface Appender
    {
        public void log( Level l, String message, Throwable t );


        public boolean isLogEnabled( Level l );
    }

    /*
     * *******************
     * INITIALIZE
     */

    /**
     * Initialize the debugger
     */
    private synchronized static void initialize()
    {
        // Check if we have initialized earlier
        if ( initialized )
            return;

        // Set initialized
        initialized = true;

        logAppenders = new ArrayList<Appender>();

        // Check if we can use log4j as debugger
        try
        {
            Appender log4jAppender = ( Appender ) Class.forName( Logging.log4logger ).newInstance();
            add( log4jAppender );

//            System.out.println( "[" + Logging.class.getCanonicalName() + "] Redirecting log to log4j (" + Configuration.debugEnabled + ")" );
        }
        catch ( Throwable e )
        {
            log4exists = false;
//            System.out.println( "[" + Logging.class.getCanonicalName() + "] Turning off debug as no log4j instance could be created" );
        }
    }


    /**
     * Enable or disable logging manually
     *
     * @param enabled True to enable, false to disable
     */
    public static void setDebugEnabled( boolean enabled )
    {
        initialize();
        Configuration.debugEnabled = enabled;
    }


    /**
     * Call all log appenders
     *
     * @param l   The log level
     * @param msg The message
     * @param t   Any throwable to log
     */
    private static void callAppenders( Level l, String msg, Throwable t )
    {
        initialize();

        if ( (!Configuration.debugEnabled && !l.equals( Level.FATAL )) )
            return;

        if ( !log4exists )
        {
            // Native debug
            nativeDebug( l, msg, t );
        }
        else
        {
            Iterator<Appender> i = logAppenders.iterator();

            while ( i.hasNext() )
            {
                Appender la = i.next();
                la.log( l, msg, t );
            }
        }
    }


    private static void nativeDebug( Level l, String msg, Throwable t )
    {
        try
        {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime( new Date( System.currentTimeMillis() ) );

            StringBuffer sb;
            sb = new StringBuffer();
            sb.append( "[ " );
            sb.append( (calendar.get( Calendar.HOUR_OF_DAY )) ).append( ":" );
            sb.append( (calendar.get( Calendar.MINUTE )) ).append( ":" );
            sb.append( (calendar.get( Calendar.SECOND )) ).append( "." );
            sb.append( (calendar.get( Calendar.MILLISECOND )) );
            sb.append( " ]");

            sb.append( "[ " );
            sb.append( l.toString() );
            sb.append( " ] ");

            sb.append( msg );

            if( t != null )
                System.err.println( sb.toString() );
            else
               System.out.println( sb.toString() );
        }
        catch ( Throwable ignore )
        {

        }

        // Check if we have something throwable
        if ( t != null )
        {
            UtilsJavaSE.StackTraceLocation s = UtilsJavaSE.getLocation( fromCollection );
            if ( s != null )
                System.err.println( "\t " + fromLocation( s ) );
        }

    }


    private static String fromLocation( UtilsJavaSE.StackTraceLocation s )
    {
        if ( s == null )
            return "";
        return s.className + "." + s.methodName + "(" + s.fileName + ":" + s.lineNumber + ")";
    }


    private String getDate()
    {
        return (new Date()).toString();
    }


    /**
     * Add an appender to call when running callAppenders
     *
     * @param loggerAppender The appender to add
     */
    private static void add( Appender loggerAppender )
    {
        logAppenders.add( loggerAppender );
    }

    /*
     * ***********************************
     * LOGGING
     */

    /**
     * Print a debug message
     *
     * @param msg The debug message
     */
    public static void debug( String msg )
    {
        callAppenders( Level.DEBUG, msg, null );
    }


    /**
     * Print a debug message with a throwable object
     *
     * @param msg The message to log
     * @param t   The throwable object to log
     */
    public static void debug( String msg, Throwable t )
    {
        callAppenders( Level.DEBUG, msg, t );
    }


    /**
     * Print a debug message with a specific extra value
     *
     * @param msg The message to log
     * @param v   The value to log
     */
    public static void debug( String msg, String v )
    {
        callAppenders( Level.DEBUG, msg + " " + v, null );
    }


    /**
     * Print a debug message with a specific object
     *
     * @param msg The message to log
     * @param o   The object to log
     */
    public static void debug( String msg, Object o )
    {
        callAppenders( Level.DEBUG, msg + " " + o.toString(), null );
    }


    /**
     * Print an error message
     *
     * @param msg The error message
     */
    public static void error( String msg )
    {
        callAppenders( Level.ERROR, msg, null );
    }


    /**
     * Print an error message with a specific throwable object
     *
     * @param msg The message to log
     * @param t   The throwable object to log
     */
    public static void error( String msg, Throwable t )
    {
        callAppenders( Level.ERROR, msg, t );
    }


    /**
     * Print an error message and a specific value
     *
     * @param msg The message to log
     * @param v   The value to log
     */
    public static void error( String msg, String v )
    {
        callAppenders( Level.ERROR, msg + " " + v, null );
    }


    /**
     * Print an error message and a specific object
     *
     * @param msg The message to log
     * @param o   The object to log
     */
    public static void error( String msg, Object o )
    {
        callAppenders( Level.ERROR, msg + " " + o.toString(), null );
    }


    /**
     * Print an info message
     *
     * @param msg The info message
     */
    public static void info( String msg )
    {
        callAppenders( Level.INFO, msg, null );
    }


    /**
     * Print a specific info message and a throwable object
     *
     * @param msg The message to log
     * @param t   The throwable object
     */
    public static void info( String msg, Throwable t )
    {
        callAppenders( Level.INFO, msg, t );
    }


    /**
     * Print a specific info message and a value
     *
     * @param msg The message to log
     * @param v   The value to log
     */
    public static void info( String msg, String v )
    {
        callAppenders( Level.INFO, msg + " " + v, null );
    }


    /**
     * Log a warning message with an object
     *
     * @param msg The warning message
     * @param o   The object to log
     */
    public static void info( String msg, Object o )
    {
        callAppenders( Level.INFO, msg + " " + o.toString(), null );
    }


    /**
     * Log a warning message
     *
     * @param msg The warning message
     */
    public static void warn( String msg )
    {
        callAppenders( Level.WARN, msg, null );
    }


    /**
     * Log a warning message and a throwable object
     *
     * @param msg The warning message
     * @param t   The throwable object
     */
    public static void warn( String msg, Throwable t )
    {
        callAppenders( Level.WARN, msg, t );
    }


    /**
     * Log a warning message with a value
     *
     * @param msg The warning message
     * @param v   The value to log
     */
    public static void warn( String msg, String v )
    {
        callAppenders( Level.WARN, msg + " " + v, null );
    }


    /**
     * Log a warning message with an object
     *
     * @param msg The warning message
     * @param o   The object to log
     */
    public static void warn( String msg, Object o )
    {
        callAppenders( Level.WARN, msg + " " + o.toString(), null );
    }


    /**
     * Log a fatal message
     *
     * @param msg The fatal message
     */
    public static void fatal( String msg )
    {
        callAppenders( Level.FATAL, msg, null );
    }


    /**
     * Log a fatal message with a value
     *
     * @param msg The fatal message
     * @param v   The value to log
     */
    public static void fatal( String msg, String v )
    {
        callAppenders( Level.FATAL, msg + " " + v, null );
    }


    /**
     * Log a fatal message with a throwable object
     *
     * @param msg The fatal message
     * @param e   Throwable to log
     */
    public static void fatal( String msg, Throwable e )
    {
        callAppenders( Level.FATAL, msg, e );
    }


    /**
     * Log a fatal message with an object
     *
     * @param msg The fatal message
     * @param o   The object to log
     */
    public static void fatal( String msg, Object o )
    {
        callAppenders( Level.FATAL, msg + " " + o.toString(), null );
    }
}