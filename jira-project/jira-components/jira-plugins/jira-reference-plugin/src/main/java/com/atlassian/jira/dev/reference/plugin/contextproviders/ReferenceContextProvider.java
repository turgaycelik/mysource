package com.atlassian.jira.dev.reference.plugin.contextproviders;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.plugin.webfragment.contextproviders.AbstractJiraContextProvider;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;

import java.util.Map;

public class ReferenceContextProvider extends AbstractJiraContextProvider
{
    @Override
    public Map getContextMap(User user, JiraHelper jiraHelper)
    {
        return EasyMap.build("test", "original");
    }
}
