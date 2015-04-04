package gov.noaa.ncdc.common;

import java.io.Console;
import java.io.PrintWriter;
import java.util.Date;
import java.util.Locale;


/**
 * Perform debugging-related operations.
 */
public final class Debug
{
    /** time format string (yyyy-MM-dd HH:mm:ss.SSS) */
    private static final String TIMESTAMP_FORMAT_STRING = "\n[%1$tF %1$tT.%1$tL] ";

    /** standard error output stream */
    private static PrintWriter err;


    /**
    * Static initialization block.
    */
    static
    {
        final Console c = System.console();

        if (c == null)
        {
            err = new PrintWriter(System.out);
        }
        else
        {
            err = c.writer();
        }
    }


    /**
    * Private constructor that should never be called.
    */
    private Debug()
    {}


    /**
    * Print to the standard error output stream immediately.
    *
    * @param format
    *     format string
    * @param args
    *     arguments referenced by the format specifiers in the format string
    */
    public static void p(
            final String format,
            final Object... args)
    {
        err.format(format, args);
        err.flush();
    }


    /**
    * Print to the standard error output stream immediately, with a timestamp.
    *
    * @param format
    *     format string
    * @param args
    *     arguments referenced by the format specifiers in the format string
    */
    public static void pt(
            final String format,
            final Object... args)
    {
        err.format(Locale.ENGLISH, TIMESTAMP_FORMAT_STRING, new Date());
        err.format(format, args);
        err.flush();
    }


    /**
    * Get a string representation of the stack trace for the specified exception.
    *
    * @param ex
    *      exception
    * @return
    *      string representation of the stack trace
    */
    public static String getStackTraceString(
            final Exception ex)
    {
        final StringBuilder sb = new StringBuilder("Exception stack trace:");

        for (StackTraceElement e : ex.getStackTrace())
        {
            sb.append("\n  ");
            sb.append(e);
        }

        return sb.toString();
    }


    /**
    * Get a string representation of the system information (OS, JRE, JVM).
    *
    * @return
    *     string representation of the system information
    */
    public static String getSystemInformationString()
    {
        final StringBuilder sb = new StringBuilder();
        sb.append("System Information:\n  ");
        sb.append(getSystemProperty("os.name"));
        sb.append(" (");
        sb.append(getSystemProperty("os.version"));
        sb.append(", ");
        sb.append(getSystemProperty("os.arch"));
        sb.append(")\n  JRE ");
        sb.append(getSystemProperty("java.version"));
        sb.append(" (");
        sb.append(getSystemProperty("java.vendor"));
        sb.append(")\n  ");
        sb.append(getSystemProperty("java.vm.name"));
        sb.append(' ');
        sb.append(getSystemProperty("java.vm.version"));
        sb.append(" (");
        sb.append(getSystemProperty("java.vm.vendor"));
        sb.append(')');
        return sb.toString();
    }


    /**
    * Get the specified system property, without throwing any exceptions.
    *
    * @param key
    *     key of the system property to be obtained
    * @return
    *     String representation of the specified system property
    */
    public static String getSystemProperty(
            final String key)
    {
        try
        {
            return System.getProperty(key);
        }
        catch (Exception e)
        {
            return String.format("Failed to get system property \"%s\" (%s)", key, e.toString());
        }
    }


    /**
    * Set the specified system property, without throwing any exceptions.
    *
    * @param key
    *     key of the system property to be set
    * @param value
    *     value of the system property
    */
    public static void setSystemProperty(
            final String key,
            final String value)
    {
        try
        {
            System.setProperty(key, value);
        }
        catch (Exception e)
        {
            /* ignore */
        }
    }


    /**
    * Sleep for the specified number of milliseconds.
    * This method uses the Thread.sleep() method, but will ignore the InterruptedException
    * exception if thrown.
    *
    * @param millis
    *     number of milliseconds to sleep
    */
    public static void sleep(
            final long millis)
    {
        try
        {
            Thread.sleep(millis);
        }
        catch (InterruptedException e)
        {
            /* ignore */
        }
    }

    /******************
    * NESTED CLASSES *
    ******************/

    /**
    * Represent a "capsule" for passing a single value in a thread-safe manner.
    * The capsule value is set by calling <code>set()</code>, and read by calling <code>get()</code>.
    * The <code>get()</code> method blocks until the value is set.
    * The original state of the capsule can be restored by calling <code>reset()</code>.
    */
    public static class ValueCapsule<T>
    {
        /** refresh interval in milliseconds */
        private static final long REFRESH_INTERVAL_MILLISECONDS = 50L;

        /** capsule value to be passed */
        private T value;

        /** mutex lock for <code>value</code> */
        private final Object valueLock = new Object();

        /** has the capsule value been set by calling the <code>set()</code> method? */
        private boolean setCalled;


        /**
        * Constructor.
        */
        public ValueCapsule()
        {
            reset();
        }


        /**
        * Restore the original state of the capsule.
        */
        public void reset()
        {
            synchronized (valueLock)
            {
                value = null;
                setCalled = false;
            }
        }


        /**
        * Set the capsule value.
        *
        * @param value
        *     new capsule value
        */
        public void set(
                final T value)
        {
            synchronized (valueLock)
            {
                this.value = value;
                setCalled = true;
            }
        }


        /**
        * Get the capsule value.
        * This method blocks until the value is set.
        *
        * @return
        *     capsule value
        */
        public T get()
        {
            while (true)
            {
                synchronized (valueLock)
                {
                    if (setCalled)
                    {
                        return value;
                    }
                }

                Debug.sleep(REFRESH_INTERVAL_MILLISECONDS);
            }
        }
    }
}