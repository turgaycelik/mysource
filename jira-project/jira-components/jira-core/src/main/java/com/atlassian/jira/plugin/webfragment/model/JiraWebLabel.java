package com.atlassian.jira.plugin.webfragment.model;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.plugin.webfragment.JiraWebInterfaceManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.plugin.web.descriptors.WebFragmentModuleDescriptor;
import com.atlassian.plugin.web.model.WebLabel;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.SortedMap;

/**
 * A jira specific wrapper for the {@link com.atlassian.plugin.web.model.DefaultWebLabel}
 */
public class JiraWebLabel implements WebLabel
{
    private WebLabel webLabel;
    private final JiraAuthenticationContext authenticationContext;

    public JiraWebLabel(WebLabel webLabel, JiraAuthenticationContext authenticationContext)
    {
        this.webLabel = webLabel;
        this.authenticationContext = authenticationContext;
    }

    public String getKey()
    {
        return webLabel.getKey();
    }

    public String getNoKeyValue()
    {
        return webLabel.getNoKeyValue();
    }

    public String getDisplayableLabel(User remoteUser, JiraHelper jiraHelper)
    {
        return getDisplayableLabel(jiraHelper.getRequest(), buildContext(remoteUser, jiraHelper));
    }

    public String getDisplayableLabel(HttpServletRequest req, Map context)
    {
        return webLabel.getDisplayableLabel(req, context);
    }

    public SortedMap getParams()
    {
        return webLabel.getParams();
    }

    public Object get(String key)
    {
        return webLabel.get(key);
    }

    public String getRenderedParam(String paramKey, Map context)
    {
        return webLabel.getRenderedParam(paramKey, context);
    }

    private Map buildContext(User remoteUser, JiraHelper jiraHelper)
    {
        final Map params = EasyMap.build(JiraWebInterfaceManager.CONTEXT_KEY_USER, remoteUser,
                JiraWebInterfaceManager.CONTEXT_KEY_HELPER, jiraHelper,
                JiraWebInterfaceManager.CONTEXT_KEY_I18N, authenticationContext.getI18nHelper());
        params.putAll(jiraHelper.getContextParams());
        return params;
    }

    public WebFragmentModuleDescriptor getDescriptor()
    {
        return webLabel.getDescriptor();
    }
}
