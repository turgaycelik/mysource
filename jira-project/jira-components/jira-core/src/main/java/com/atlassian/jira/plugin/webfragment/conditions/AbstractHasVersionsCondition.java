package com.atlassian.jira.plugin.webfragment.conditions;

import com.atlassian.plugin.PluginParseException;

import java.util.Map;

public abstract class AbstractHasVersionsCondition extends AbstractJiraCondition
{
    protected Boolean includeArchived;

    public void init(Map params) throws PluginParseException
    {
        super.init(params);
        includeArchived = Boolean.valueOf((String) params.get("includeArchived"));
        if (includeArchived == null)
        {
            includeArchived = Boolean.FALSE;
        }
    }
}
