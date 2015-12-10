package com.atlassian.jira.plugin.webfragment.conditions;

import com.atlassian.annotations.PublicSpi;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.plugin.webfragment.JiraWebInterfaceManager;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.ApplicationUsers;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.web.Condition;

import java.util.Map;

/**
 * Convenient abstraction for jira specific {@link Condition}'s.
 *
 * @deprecated Extend {@link AbstractWebCondition} instead. Since v6.0.
 */
@Deprecated
@PublicSpi
public abstract class AbstractJiraCondition implements Condition
{
    public void init(Map params) throws PluginParseException
    {
    }

    public boolean shouldDisplay(Map context)
    {
        final JiraHelper jiraHelper = (JiraHelper)context.get(JiraWebInterfaceManager.CONTEXT_KEY_HELPER);
        ApplicationUser appUser = ApplicationUsers.from((User)context.get(JiraWebInterfaceManager.CONTEXT_KEY_USER));
        if (appUser == null)
        {
            // cross product plugins may not have access to the user object jira uses and will only be able to put the
            // username into the context.  Need to do a lookup here.
            final String username = (String) context.get(JiraWebInterfaceManager.CONTEXT_KEY_USERNAME);
            appUser = getUserUtil().getUserByName(username);
        }
        return shouldDisplay(ApplicationUsers.toDirectoryUser(appUser), jiraHelper);
    }

    UserUtil getUserUtil()
    {
        return ComponentAccessor.getUserUtil();
    }

    public abstract boolean shouldDisplay(User user, JiraHelper jiraHelper);
}
