package com.atlassian.jira.util;

import com.atlassian.jira.security.JiraAuthenticationContext;

import java.util.Map;

/**
 * @since v5.0
 */
public class VelocityParamFactoryImpl implements VelocityParamFactory
{
    private final JiraAuthenticationContext jac;

    public VelocityParamFactoryImpl(JiraAuthenticationContext jac)
    {
        this.jac = jac;
    }

    @Override
    public Map<String, Object> getDefaultVelocityParams()
    {
        return getDefaultVelocityParams(jac);
    }

    @Override
    public Map<String, Object> getDefaultVelocityParams(Map<String, Object> startingParams)
    {
        return getDefaultVelocityParams(startingParams, jac);
    }

    @Override
    public Map<String, Object> getDefaultVelocityParams(final JiraAuthenticationContext authenticationContext)
    {
        return JiraVelocityUtils.getDefaultVelocityParams(authenticationContext);
    }

    @Override
    public Map<String, Object> getDefaultVelocityParams(Map<String, Object> startingParams, final JiraAuthenticationContext authenticationContext)
    {
        return JiraVelocityUtils.getDefaultVelocityParams(startingParams, authenticationContext);
    }

}
