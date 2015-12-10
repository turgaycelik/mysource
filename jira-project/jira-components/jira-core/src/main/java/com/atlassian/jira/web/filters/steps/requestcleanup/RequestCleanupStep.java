package com.atlassian.jira.web.filters.steps.requestcleanup;

import com.atlassian.core.logging.ThreadLocalErrorCollection;
import com.atlassian.jira.dashboard.permission.JiraPermissionService;
import com.atlassian.jira.security.JiraAuthenticationContextImpl;
import com.atlassian.jira.util.ImportUtils;
import com.atlassian.jira.util.searchers.ThreadLocalSearcherCache;
import com.atlassian.jira.util.velocity.DefaultVelocityRequestContextFactory;
import com.atlassian.jira.web.filters.steps.FilterCallContext;
import com.atlassian.jira.web.filters.steps.FilterStep;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.TransactionUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Map;

/**
 *
 */
public class RequestCleanupStep implements FilterStep
{
    private static final Logger log = Logger.getLogger(RequestCleanupStep.class);

    @Override
    public FilterCallContext beforeDoFilter(FilterCallContext callContext)
    {
        // clear the request cache
        JiraAuthenticationContextImpl.clearRequestCache();

        // insert a velocity request context.  Fixes JRA-11038
        DefaultVelocityRequestContextFactory.cacheVelocityRequestContext(callContext.getHttpServletRequest());

        ThreadLocalErrorCollection.clear();

        return callContext;
    }

    @Override
    public FilterCallContext finallyAfterDoFilter(FilterCallContext callContext)
    {
        ThreadLocalErrorCollection.clear();

        JiraAuthenticationContextImpl.clearRequestCache();

        ThreadLocalSearcherCache.resetSearchers();

        // Editing the default dashboard should generally be possible, except when viewing the
        // dashboard from the home page. See com.atlassian.jira.web.action.dashboard.Dashboard
        JiraPermissionService.setAllowEditingOfDefaultDashboard(true);

        try
        {
            boolean printRequestDetails = false;
            if (!ImportUtils.isIndexIssues())
            {
                log.error("Indexing thread local not cleared. Clearing...");
                ImportUtils.setIndexIssues(true);
                printRequestDetails = true;
            }

            // Ensure that the connection thread local is cleared
            if (TransactionUtil.getLocalTransactionConnection() != null)
            {
                log.error("Connection not cleared from thread local.");
                // Close the connection and clear the thead local
                TransactionUtil.closeAndClearThreadLocalConnection();
                printRequestDetails = true;
            }

            if (printRequestDetails)
            {
                HttpServletRequest httpServletRequest = callContext.getHttpServletRequest();
                log.error("The URL of request that did not clear connection is: " + httpServletRequest.getRequestURL());
                HttpSession session = httpServletRequest.getSession(false);
                if (session != null)
                {
                    log.error("The User of request was: " + session.getAttribute("seraph_defaultauthenticator_user"));
                }
                else
                {
                    log.error("No session found. Cannot determine user.");
                }

                log.error("Content type:" + httpServletRequest.getContentType());

                Map parameterMap = httpServletRequest.getParameterMap();
                if (parameterMap != null)
                {
                    int j = 1;
                    for (Object o : parameterMap.entrySet())
                    {
                        Map.Entry entry = (Map.Entry) o;
                        log.error("Parameter " + j + " name: " + entry.getKey());
                        Object value = entry.getValue();

                        if (value != null && value instanceof String[])
                        {
                            String[] valueArray = (String[]) value;
                            for (String s : valueArray)
                            {
                                log.error("Parameter value: " + s);
                            }
                        }
                        else
                        {
                            log.error("Parameter value: " + value);
                        }

                        j++;
                    }
                }
                else
                {
                    log.error("Request did not have any parameters.");
                }
            }
        }
        catch (Exception t)
        {
            log.error("Error while inspecting thread locals.", t);
        }
        return callContext;
    }
}
