package com.atlassian.jira.plugin.webfragment.contextproviders;

import com.atlassian.annotations.PublicSpi;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.plugin.webfragment.JiraWebInterfaceManager;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.web.ContextProvider;

import java.util.Map;

@PublicSpi
public abstract class AbstractJiraContextProvider implements ContextProvider
{
    public void init(Map params) throws PluginParseException
    {
    }

    public Map getContextMap(Map context)
    {
        User user = (User) context.get(JiraWebInterfaceManager.CONTEXT_KEY_USER);
        if (user == null)
        {
            // cross product plugins may not have access to the user object jira uses and will only be able to put the
            // username into the context.  Need to do a lookup here.
            final String username = (String) context.get(JiraWebInterfaceManager.CONTEXT_KEY_USERNAME);
            user = getUserUtil().getUserObject(username);
        }
        JiraHelper jiraHelper = (JiraHelper) context.get(JiraWebInterfaceManager.CONTEXT_KEY_HELPER);
        return getContextMap(user, jiraHelper);
    }

    private UserUtil getUserUtil()
    {
        return ComponentAccessor.getUserUtil();
    }

    public abstract Map getContextMap(User user, JiraHelper jiraHelper);
}
