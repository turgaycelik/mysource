/*
 * Copyright (c) 2002-2006
 * All rights reserved.
 */

package com.atlassian.jira.security;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.security.login.LoginManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.seraph.auth.RoleMapper;
import com.atlassian.seraph.config.SecurityConfig;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.util.Map;

/**
 * A Seraph RoleMapper which maps from group membership to JIRA permissions via a permission scheme. Eg, the permission
 * scheme typically allocates members of the "jira-users" group the {@link Permissions#USE} role.
 */
public class JiraRoleMapper implements RoleMapper
{
    public boolean hasRole(@Nullable Principal user, HttpServletRequest httpServletRequest, String role)
    {
        return getLoginManager().authoriseForRole(toApplicationUser(user), httpServletRequest, role);
    }

    public boolean canLogin(@Nullable Principal user, HttpServletRequest httpServletRequest)
    {
        if (user != null)
        {
            return getLoginManager().authoriseForLogin(toApplicationUser(user), httpServletRequest);
        }

        // if the principal is null, then no login is possible
        return false;
    }

    protected ApplicationUser toApplicationUser(Principal user)
    {
        return user != null ? getUserManager().getUserByName(user.getName()) : null;
    }

    ///CLOVER:OFF
    public void init(Map map, SecurityConfig securityConfig)
    {
    }

    LoginManager getLoginManager()
    {
        return ComponentAccessor.getComponentOfType(LoginManager.class);
    }

    UserManager getUserManager()
    {
        return ComponentAccessor.getUserManager();
    }
    ///CLOVER:ON
}
