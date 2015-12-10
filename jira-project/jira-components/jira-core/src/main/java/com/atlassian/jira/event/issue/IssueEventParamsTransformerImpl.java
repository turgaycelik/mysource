package com.atlassian.jira.event.issue;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;

public class IssueEventParamsTransformerImpl implements IssueEventParamsTransformer
{
    private ApplicationProperties applicationProperties;

    public IssueEventParamsTransformerImpl(final ApplicationProperties applicationProperties)
    {
        this.applicationProperties = applicationProperties;
    }

    @Override
    @Nonnull
    public Map<String, Object> transformParams(@Nullable final Map<String, Object> issueEventParams)
    {
        Map<String, Object> copyOfParams = copyParams(issueEventParams);
        copyOfParams.put(IssueEvent.BASE_URL_PARAM_NAME, applicationProperties.getString(APKeys.JIRA_BASEURL));
        return copyOfParams;
    }

    private static Map<String, Object> copyParams(final Map params)
    {
        Map<String,Object> copyOfParams = new HashMap<String,Object>();
        if (params != null)
        {
            copyOfParams.putAll(params);
        }
        return copyOfParams;
    }
}
