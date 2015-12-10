package com.atlassian.jira.plugin.webfragment.model;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.plugin.webfragment.JiraWebInterfaceManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.plugin.web.descriptors.WebFragmentModuleDescriptor;
import com.atlassian.plugin.web.model.WebLink;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * A jira specific wrapper for the {@link com.atlassian.plugin.web.model.DefaultWebLink}
 */
public class JiraWebLink implements WebLink, SettableWebLink
{
    private WebLink webLink;
    private final JiraAuthenticationContext authenticationContext;

    public JiraWebLink(JiraAuthenticationContext authenticationContext)
    {
        this.authenticationContext = authenticationContext;
    }

    public JiraWebLink(WebLink webLink, JiraAuthenticationContext authenticationContext)
    {
        this.webLink = webLink;
        this.authenticationContext = authenticationContext;
    }

    public String getRenderedUrl(User remoteUser, JiraHelper jiraHelper)
    {
        return webLink.getRenderedUrl(makeContext(remoteUser, jiraHelper));
    }

    public String getDisplayableUrl(User remoteUser, JiraHelper jiraHelper)
    {
        return webLink.getDisplayableUrl(jiraHelper.getRequest(), makeContext(remoteUser, jiraHelper));
    }

    public boolean hasAccessKey()
    {
        return webLink.hasAccessKey();
    }

    public String getAccessKey(User remoteUser, JiraHelper jiraHelper)
    {
        return webLink.getAccessKey(makeContext(remoteUser, jiraHelper));
    }

    public String getId()
    {
        return webLink.getId();
    }

    protected Map makeContext(User remoteUser, JiraHelper jiraHelper)
    {
        final Map params = EasyMap.build(JiraWebInterfaceManager.CONTEXT_KEY_USER, remoteUser,
                JiraWebInterfaceManager.CONTEXT_KEY_HELPER, jiraHelper,
                JiraWebInterfaceManager.CONTEXT_KEY_I18N, authenticationContext.getI18nHelper());
        params.putAll(jiraHelper.getContextParams());
        return params;
    }

    public String getRenderedUrl(Map context)
    {
        return webLink.getRenderedUrl(context);
    }

    public String getDisplayableUrl(HttpServletRequest req, Map context)
    {
        return webLink.getDisplayableUrl(req, context);
    }

    public String getAccessKey(Map context)
    {
        return webLink.getAccessKey(context);
    }

    public WebFragmentModuleDescriptor getDescriptor()
    {
        return webLink.getDescriptor();
    }

    public void setLink(WebLink link)
    {
        this.webLink = link;
    }
}
