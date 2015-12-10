package com.atlassian.jira.dev.backdoor.sal;

import com.atlassian.sal.api.timezone.TimeZoneManager;
import org.apache.log4j.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Servlet which is used in tests to test the sal {@link TimeZoneManager}.
 *
 * @since v4.4
 */
public class TimeZoneServlet extends HttpServlet
{
    private final TimeZoneManager timeZoneManager;
    private static final Logger log = Logger.getLogger(TimeZoneServlet.class);

    public TimeZoneServlet(TimeZoneManager timeZoneManager)
    {
        this.timeZoneManager = timeZoneManager;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        resp.setContentType("text/html");
        try
        {
            resp.getWriter().println("DefaultTimeZone=" + timeZoneManager.getDefaultTimeZone().getID());
            resp.getWriter().println("UserTimeZone=" + timeZoneManager.getUserTimeZone().getID());
        }
        catch (RuntimeException e)
        {
            Class<?>[] interfaces = timeZoneManager.getClass().getInterfaces();
            for (Class<?> anInterface : interfaces)
            {
                log.error("TIMEZONEMANAGER TEST FAILED. TimeZoneManager interface is '" + anInterface.getCanonicalName() + "'");
            }
            log.error("TIMEZONEMANAGER TEST FAILED. TimeZoneManager proxy class loader is '" + timeZoneManager.getClass().getClassLoader() + "'");
            log.error("TIMEZONEMANAGER TEST FAILED. TimeZoneManager.class class loader is '" + TimeZoneManager.class.getClassLoader() + "'");
            throw e;
        }
    }

}
