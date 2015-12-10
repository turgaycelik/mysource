package com.atlassian.jira.soap.axis;

import org.apache.axis.transport.http.AxisHTTPSessionListener;
import org.apache.log4j.Logger;

import javax.servlet.http.HttpSessionEvent;

/**
 * This listener is a workaround for JRA-5199. A candidate to be removed
 */
public class JiraAxisHttpListener extends AxisHTTPSessionListener
{
    private static final Logger log = Logger.getLogger(JiraAxisHttpListener.class);

    public void sessionDestroyed(HttpSessionEvent event)
    {
        try
        {
            super.sessionDestroyed(event);
        }
        catch (IllegalStateException e)
        {
            // Illegal state caught. Means that session has already been invalidated
            log.debug("Axis session listener attempted to clear lifecycle objects from invalid session");
        }
    }
}
