package com.atlassian.jira.util.log;

import com.atlassian.jira.util.dbc.Null;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Appender;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.MDC;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A class to help with log4j related code
 *
 * @since v4.0
 */
public class Log4jKit
{
    public static final String MDC_JIRA_USERNAME = "jira.username";
    public static final String MDC_JIRA_REQUEST_ID = "jira.request.id";
    public static final String MDC_JIRA_ASSESSION_ID = "jira.request.assession.id";
    public static final String MDC_JIRA_REQUEST_URL = "jira.request.url";
    public static final String MDC_JIRA_REQUEST_IPADDR = "jira.request.ipaddr";

    static final String ANONYMOUS = "anonymous";

    /**
     * Returns the full log file name for the given appender.  The appender must be a FileAppender for this to work.
     *
     * @param appenderName the name of the appender in the log4j configuration
     * @return null if one cant be found or the absolute file name of the appender
     */
    public static File getLogFileName(String appenderName)
    {
        Null.not("appenderName", appenderName);

        final Enumeration currentLoggers = LogManager.getCurrentLoggers();
        while (currentLoggers.hasMoreElements())
        {
            Logger logger = (Logger) currentLoggers.nextElement();
            final Enumeration allAppenders = logger.getAllAppenders();
            while (allAppenders.hasMoreElements())
            {
                Appender appender = (Appender) allAppenders.nextElement();
                if (appenderName.equals(appender.getName()))
                {
                    File file = getFile(appender);
                    if (file != null)
                    {
                        return file;
                    }
                }
            }
        }
        return null;
    }

    /**
     * @return Returns the set of all file appenders configured
     */
    public static Set<File> getAllLogFiles()
    {
        final Set<File> logFileNames = new HashSet<File>();
        final Enumeration currentLoggers = LogManager.getCurrentLoggers();
        while (currentLoggers.hasMoreElements())
        {
            Logger logger = (Logger) currentLoggers.nextElement();
            final Enumeration allAppenders = logger.getAllAppenders();
            while (allAppenders.hasMoreElements())
            {
                Appender appender = (Appender) allAppenders.nextElement();
                File file = getFile(appender);
                if (file != null)
                {
                    logFileNames.add(file);
                }
            }
        }
        return logFileNames;
    }

    private static File getFile(Appender appender)
    {
        try
        {
            final Method file = appender.getClass().getMethod("getFile");
            final Object o = file.invoke(appender);
            if (o instanceof String)
            {
                return new File((String)o).getAbsoluteFile();
            }
            else if (o instanceof File)
            {
                return ((File)o).getAbsoluteFile();
            }
            else
            {
                return null;
            }
        }
        catch (NoSuchMethodException e)
        {
            return null;
        }
        catch (InvocationTargetException e)
        {
            return null;
        }
        catch (IllegalAccessException e)
        {
            return null;
        }
    }

    /**
     * This squirels away request information into the log4j {@link org.apache.log4j.MDC}.
     *
     * @param userName the user name in play
     * @param requestId the request id
     * @param asessionId the Atlassian Session ID (hash of session id)
     * @param requestURL the request URL
     * @param ipAddr the ipaddress of the clint making the request
     */
    public static void putToMDC(final String userName, final String requestId, final String asessionId, final String requestURL, final String ipAddr)
    {
        putUserToMDC(userName);
        nvlput(MDC_JIRA_REQUEST_ID, requestId);
        nvlput(MDC_JIRA_ASSESSION_ID, asessionId);
        nvlput(MDC_JIRA_REQUEST_URL, requestURL);
        nvlput(MDC_JIRA_REQUEST_IPADDR, ipAddr);
    }

    /**
     * This will add the user name to the log4j {@link org.apache.log4j.MDC}
     *
     * @param userName the user name in play
     */
    public static void putUserToMDC(final String userName)
    {
        nvlput(MDC_JIRA_USERNAME, StringUtils.isBlank(userName) ? ANONYMOUS : userName);
    }

    /**
     * This will set the Atlassian Session Id into the log4j {@link org.apache.log4j.MDC}
     *
     * @param atlassianSessionId the session id in play
     */
    public static void putASessionIdToMDC(final String atlassianSessionId)
    {
        nvlput(MDC_JIRA_ASSESSION_ID, atlassianSessionId);
    }

    private static void nvlput(String key, Object value)
    {
        MDC.put(key, value == null ? "-" : value);
    }

    /**
     * This will clear out all the values in the log4j {@link org.apache.log4j.MDC}. This should be done in a finally
     * block perhaps on the outer edge of a request and probably in the start of the request as well.
     */
    public static void clearMDC()
    {
        List<String> keys = new ArrayList<String>();
        Map mdcMap = MDC.getContext();

        if (mdcMap != null)
        {
            keys.addAll(mdcMap.keySet());
            for (String key : keys)
            {
                MDC.remove(key);
            }
        }
    }
}
