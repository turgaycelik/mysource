package com.atlassian.jira.util;

import com.atlassian.jira.security.JiraAuthenticationContext;

import java.util.Map;

/**
 * Factory that can create default Velocity Parameters for use in Velocity templates.
 *
 * @since v5.0
 */
@InjectableComponent
public interface VelocityParamFactory
{
    /**
     * Method to construct a map with a number of common parameters used by velocity templates.
     *
     * @param authenticationContext JiraAuthenticationContext
     * @return a Map with common velocity parameters
     */
    Map<String, Object> getDefaultVelocityParams(JiraAuthenticationContext authenticationContext);

    /**
     * Method to construct a map with a number of common parameters used by velocity templates.
     *
     * @param startingParams        Map of parameters that may be used to override any of the parameters set here.
     * @param authenticationContext JiraAuthenticationContext
     * @return a Map with common velocity parameters
     */
    Map<String, Object> getDefaultVelocityParams(Map<String, Object> startingParams, JiraAuthenticationContext authenticationContext);


    /**
     * Method to construct a map with a number of common parameters used by velocity templates. It uses the user
     * from the current request.
     *
     * @return a Map with common velocity parameters
     */
    Map<String, Object> getDefaultVelocityParams();

    /**
     * Method to construct a map with a number of common parameters used by velocity templates. It uses the user
     * from the current request.
     *
     * @param startingParams        Map of parameters that may be used to override any of the parameters set here.
     *
     * @return a Map with common velocity parameters
     */
    Map<String, Object> getDefaultVelocityParams(Map<String, Object> startingParams);
}
