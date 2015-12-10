package com.atlassian.jira.issue.fields.rest.json.beans;

import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;

/**
 * @since v5.0
 */
public class JiraBaseUrlsImpl implements JiraBaseUrls
{
    public static final String REST_2_PREFIX = "/rest/api/2/";
    private final VelocityRequestContextFactory velocityRequestContextFactory;


    public JiraBaseUrlsImpl(VelocityRequestContextFactory velocityRequestContextFactory)
    {
        this.velocityRequestContextFactory = velocityRequestContextFactory;
    }

    @Override
    public String restApi2BaseUrl()
    {
        return baseUrl() + REST_2_PREFIX;
    }
    @Override
    public String baseUrl()
    {
        return velocityRequestContextFactory.getJiraVelocityRequestContext().getCanonicalBaseUrl();
    }
}
