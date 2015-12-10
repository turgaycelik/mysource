package com.atlassian.jira.web.filters;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.permission.GlobalPermissionKey;
import com.atlassian.jira.security.GlobalPermissionManager;
import com.atlassian.jira.security.login.LoginManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.sal.api.user.UserRole;
import com.atlassian.seraph.config.SecurityConfig;
import com.atlassian.seraph.interceptor.LoginInterceptor;

public class JiraLoginInterceptor implements LoginInterceptor
{

    @Override
    public void beforeLogin(final HttpServletRequest request, final HttpServletResponse response, final String username, final String password, final boolean cookieLogin)
    {

    }

    @Override
    public void afterLogin(final HttpServletRequest request, final HttpServletResponse response, final String username, final String password, final boolean cookieLogin, final String loginStatus)
    {
        if(!"success".equals(loginStatus))
        {
            return;
        }

        UserRole userRole = getUserRole(request);
        if (userRole  != null)
        {
            final UserManager userManager = ComponentAccessor.getUserManager();
            final GlobalPermissionManager globalPermissionManager = ComponentAccessor.getGlobalPermissionManager();
            final LoginManager loginManager = ComponentAccessor.getComponent(LoginManager.class);

            ApplicationUser currentUser = userManager.getUserByName(username);
            if (userRole.equals(UserRole.SYSADMIN) && !globalPermissionManager.hasPermission(GlobalPermissionKey.SYSTEM_ADMIN, currentUser) ||
                userRole.equals(UserRole.ADMIN) && !globalPermissionManager.hasPermission(GlobalPermissionKey.ADMINISTER, currentUser))
            {
                loginManager.logout(request, response);
            }

        }
    }

    @Override
    public void destroy()
    {

    }

    @Override
    public void init(final Map<String, String> params, final SecurityConfig config)
    {
    }

    public static UserRole getUserRole(HttpServletRequest request)
    {
        final String requestedUserRole = request.getParameter("user_role");

        final UserRole userRole;
        if (requestedUserRole != null)
        {
            try
            {
                userRole = UserRole.valueOf(requestedUserRole);
            }
            catch (IllegalArgumentException e)
            {
                return null;
            }
        }
        else
        {
            userRole = null;
        }

        return userRole;


    }
}
