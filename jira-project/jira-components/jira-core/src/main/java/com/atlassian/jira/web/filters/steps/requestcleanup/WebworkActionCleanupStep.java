package com.atlassian.jira.web.filters.steps.requestcleanup;

import com.atlassian.jira.action.ActionContextKit;
import com.atlassian.jira.web.dispatcher.JiraWebworkActionDispatcher;
import com.atlassian.jira.web.filters.steps.FilterCallContext;
import com.atlassian.jira.web.filters.steps.FilterStep;
import org.apache.log4j.Logger;
import webwork.action.ActionContext;
import webwork.action.ServletActionContext;
import webwork.action.factory.SessionMap;
import webwork.dispatcher.GenericDispatcher;
import webwork.util.ServletValueStack;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 */
public class WebworkActionCleanupStep implements FilterStep
{
    private static final Logger log = Logger.getLogger(WebworkActionCleanupStep.class);

    @Override
    public FilterCallContext beforeDoFilter(FilterCallContext callContext)
    {
        //
        // Make sure we start with a clean slate before start the request
        blatActionContextWithRedMatter();

        //
        // Tell webwork not to clean up the value stack via this request variable.
        // See JiraServletDispatcher to see this in action.
        callContext.getHttpServletRequest().setAttribute(JiraWebworkActionDispatcher.CLEANUP, Boolean.FALSE);
        return callContext;

    }

    @Override
    public FilterCallContext finallyAfterDoFilter(final FilterCallContext callContext)
    {
        // Clean up afterwards. Do this in a a finally block to ensure that WebWork's thread local's are cleaned up
        // even if an exception is thrown while processing the request. If this is not done, and the next request processed by this thread
        // is not for a resource that is processed by the JiraServletDispatcher (i.e. not an action but e.g. a JSP that generates CSS)
        // the old request object will be used (as it was not cleaned up). This can lead to problems, see JRA-9057.

        HttpServletRequest httpServletRequest = callContext.getHttpServletRequest();
        boolean cleanedUp = false;
        try
        {
            final GenericDispatcher gd = (GenericDispatcher) httpServletRequest.getAttribute(JiraWebworkActionDispatcher.GD);
            if (gd != null)
            {
                cleanedUp = true;
                httpServletRequest.setAttribute(JiraWebworkActionDispatcher.STACK_HEAD, ServletValueStack.getStack(httpServletRequest).popValue());
                gd.finalizeContext();
            }
        }
        finally
        {
            //
            // We have bugs where HttpSession objects that are expired are being referenced by other threads. JRA-8009 for example.
            // As far was we can tell it caused by ActionContext caching that points to an old invalid session
            // so we have added this detection code to try and work out why this happens.
            //
            detectDirtyActionContext(httpServletRequest, cleanedUp);
        }
        return callContext;
    }

    /**
     * At this stage the ActionContext is meant to have be cleaned up BUT reality is much different to theory!
     *
     * @param httpServletRequest the request in play
     * @param attemptedCleanup whether the filter tried to cleanup the Webwork GenericDispatcher beforehand
     */
    private void detectDirtyActionContext(final HttpServletRequest httpServletRequest, final boolean attemptedCleanup)
    {
        final ActionContext currentContext = ActionContext.getContext();
        if (currentContext != null) // almost always true
        {
            final Map<?, ?> contextTable = currentContext.getTable();
            if ((contextTable != null) && !contextTable.isEmpty())
            {
                final String url = getRequestString(httpServletRequest);

                final Object sessionMap = contextTable.get(ActionContext.SESSION);
                if ((sessionMap != null) && SessionMap.class.equals(sessionMap.getClass()))
                {
                    log.error("Thread corrupted! ActionContext still references a HttpSession. URL: '" + url + "' ");
                }

                final Object requestObj = contextTable.get(ServletActionContext.REQUEST);
                if (requestObj != null)
                {
                    log.error("Thread corrupted! ActionContext still references a HttpRequest. URL: '" + url + "'. Attempted to clean up: " + attemptedCleanup);
                }

                if (log.isDebugEnabled())
                {
                    //
                    // OK we have items in the ActionContext map but we really should not so lets examine them
                    // for debugging purposes.
                    //
                    final StringBuilder nonNullKeys = new StringBuilder();
                    for (final Object key : contextTable.keySet())
                    {
                        nonNullKeys.append(key).append(", ");
                    }
                    log.debug("Thread corrupted! ActionContext has the following : " + nonNullKeys + " URL: '" + url + "' ");
                }
                //
                // ok the thread will stay poisoned unless we do something here!
                blatActionContextWithRedMatter();
            }
        }
    }

    private String getRequestString(final HttpServletRequest httpServletRequest)
    {
        final String queryString = httpServletRequest.getQueryString();
        return httpServletRequest.getRequestURL() + (queryString == null ? "" : ("?" + queryString));
    }

    /**
     * This will reset the ActionContext thread local back to an empty implementation where its lookup map table is
     * reset to null and gets to getXXX() return null!
     */
    private void blatActionContextWithRedMatter()
    {
        ActionContextKit.resetContext();
    }
}
