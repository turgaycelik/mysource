package com.atlassian.jira.plugin.webwork;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.security.login.LoginManager;
import com.atlassian.seraph.SecurityService;
import com.atlassian.seraph.config.SecurityConfig;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.Set;

/**
 * JiraSeraphSecurityService configures Seraph based on Webwork plugin module atlassian-plugin.xml
 *
 * This allows for the roles-required attribute to be used within plugins and for pluggable
 * Authorisation as well.
 *
 * @since v5.0
 */
public class JiraSeraphSecurityService implements SecurityService
{
     /**
     * Seraph Initable initialisation method.
     * As we rely on plugin events to setup our required roles, we don't do anything here
     *
     */
    public void init(Map<String, String> params, SecurityConfig config)
    {
    }

    /**
     * Seraph Initable cleanup method.
     */
    public void destroy()
    {
    }

    /**
     * This hands off to the LoginManager, who is able to live in the pico container
     *
     */
    public Set<String> getRequiredRoles(final HttpServletRequest request)
    {
        return ComponentAccessor.getComponent(LoginManager.class).getRequiredRoles(request);
    }
}
