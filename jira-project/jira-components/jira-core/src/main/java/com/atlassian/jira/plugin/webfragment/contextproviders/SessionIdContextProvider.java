package com.atlassian.jira.plugin.webfragment.contextproviders;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.web.ExecutingHttpRequest;
import com.atlassian.jira.web.util.CookieUtils;
import webwork.action.ServletActionContext;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * Context provider that inserts the currents session's id into the param map with the key - "sessionId"
 *
 * @since v4.1
 */
public class SessionIdContextProvider extends AbstractJiraContextProvider
{
    @Override
    public Map getContextMap(User user, JiraHelper jiraHelper)
    {
        HttpServletRequest servletRequest = ExecutingHttpRequest.get();
        if (servletRequest == null)
        {
            ServletActionContext.getRequest();
        }

        if (servletRequest == null)
        {
            return EasyMap.build();
        }
        
        String sessionId = CookieUtils.getSingleSessionId(servletRequest.getCookies());
        return EasyMap.build("sessionId", sessionId);
    }
}
