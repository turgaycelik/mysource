package com.atlassian.jira.util.velocity;

/**
 * Default implementation
 *
 * @see com.atlassian.jira.util.velocity.VelocityRequestContext
 */
public class SimpleVelocityRequestContext implements VelocityRequestContext
{
    private final String baseUrl;
    private final String canonicalBaseUrl;
    private final VelocityRequestSession session;
    private final RequestContextParameterHolder requestContextParameterHolder;

    public SimpleVelocityRequestContext(String baseUrl)
    {
        this.baseUrl = baseUrl;
        this.canonicalBaseUrl = baseUrl;
        this.requestContextParameterHolder = null;
        this.session = null;
    }

    public SimpleVelocityRequestContext(String baseUrl, String canonicalBaseUrl, RequestContextParameterHolder requestContextParameterHolder, VelocityRequestSession session)
    {
        this.baseUrl = baseUrl;
        this.requestContextParameterHolder = requestContextParameterHolder;
        this.canonicalBaseUrl = canonicalBaseUrl;
        this.session = session;
    }

    /**
     * @see VelocityRequestContext#getBaseUrl()
     */
    public String getBaseUrl()
    {
        return baseUrl;
    }


    public String getCanonicalBaseUrl()
    {
        return canonicalBaseUrl;
    }

    /**
     * @see com.atlassian.jira.util.velocity.VelocityRequestContext#getRequestParameters()
     */
    public RequestContextParameterHolder getRequestParameters()
    {
        return requestContextParameterHolder;
    }

    public String getRequestParameter(String name)
    {
        if (requestContextParameterHolder == null || requestContextParameterHolder.getParameterMap() == null)
        {
            return null;
        }
        String[] values = (String[]) requestContextParameterHolder.getParameterMap().get(name);
        return values == null || values.length == 0 ? null : values[0];
    }

    public VelocityRequestSession getSession()
    {
        return session;
    }
}
