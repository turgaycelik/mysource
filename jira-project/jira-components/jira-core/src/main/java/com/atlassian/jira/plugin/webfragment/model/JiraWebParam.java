package com.atlassian.jira.plugin.webfragment.model;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.plugin.webfragment.JiraWebInterfaceManager;
import com.atlassian.plugin.web.descriptors.WebFragmentModuleDescriptor;
import com.atlassian.plugin.web.model.WebParam;

import java.util.Map;
import java.util.SortedMap;

/**
 * A jira specific wrapper for the {@link com.atlassian.plugin.web.model.DefaultWebParam}
 */
public class JiraWebParam implements WebParam
{
    WebParam webParam;

    public JiraWebParam(WebParam webParam)
    {
        this.webParam = webParam;
    }

    public SortedMap getParams()
    {
        return webParam.getParams();
    }

    public Object get(String key)
    {
        return webParam.get(key);
    }

    public String getRenderedParam(String paramKey, User user, JiraHelper jiraHelper)
    {
        return getRenderedParam(paramKey, EasyMap.build(JiraWebInterfaceManager.CONTEXT_KEY_USER, user, JiraWebInterfaceManager.CONTEXT_KEY_HELPER, jiraHelper));
    }

    public String getRenderedParam(String paramKey, Map context)
    {
        return webParam.getRenderedParam(paramKey, context);
    }

    public WebFragmentModuleDescriptor getDescriptor()
    {
        return webParam.getDescriptor();
    }
}
