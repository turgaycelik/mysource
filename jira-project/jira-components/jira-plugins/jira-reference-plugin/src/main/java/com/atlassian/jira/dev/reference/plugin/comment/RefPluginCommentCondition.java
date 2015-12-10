package com.atlassian.jira.dev.reference.plugin.comment;

import java.util.Map;

import com.atlassian.jira.issue.Issue;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.web.Condition;

public class RefPluginCommentCondition implements Condition
{
    @Override
    public void init(final Map<String, String> params) throws PluginParseException
    {
    }

    @Override
    public boolean shouldDisplay(final Map<String, Object> context)
    {
        if (context.containsKey("issue"))
        {
            final Issue issue = (Issue) context.get("issue");
            return !issue.getProjectObject().getKey().equalsIgnoreCase("mky");
        }
        return true;
    }
}
