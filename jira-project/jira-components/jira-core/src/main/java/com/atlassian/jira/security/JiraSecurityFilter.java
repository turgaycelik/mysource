package com.atlassian.jira.security;

import java.util.Set;

import com.atlassian.jira.startup.JiraStartupChecklist;
import com.atlassian.sal.api.user.UserRole;
import com.atlassian.seraph.config.SecurityConfig;
import com.atlassian.seraph.config.SecurityConfigFactory;
import com.atlassian.seraph.filter.SecurityFilter;
import com.atlassian.seraph.util.RedirectUtils;

import org.apache.log4j.Logger;

import javax.servlet.FilterConfig;
import javax.servlet.http.HttpServletRequest;

/**
 * A wrapper around the Seraph SecurityFilter.
 */
public class JiraSecurityFilter extends SecurityFilter
{
    private static final Logger log = Logger.getLogger(JiraSecurityFilter.class);

    public void init(FilterConfig config)
    {
        log.debug("Initing JIRA security filter");
        init(config, true);
        log.debug("JIRA security filter inited");
    }

    protected void init(FilterConfig config, boolean startupCheck)
    {
        if (!startupCheck || JiraStartupChecklist.startupOK())
        {
            super.init(config);
        }
    }

    @Override
    protected String getLoginUrl(final HttpServletRequest httpServletRequest, final Set<String> missingRoles)
    {
        UserRole userRole = null;

        // Inform user that there is certain level of privileges required to access page
        // SecurityFilter is using roles-required attribute from actions.xml

        if (missingRoles.contains(Permissions.getShortName(Permissions.SYSTEM_ADMIN)))
        {
            userRole = UserRole.SYSADMIN;
        }
        else if(missingRoles.contains(Permissions.getShortName(Permissions.ADMINISTER)))
        {
            userRole = UserRole.ADMIN;
        }

        final SecurityConfig securityConfig = SecurityConfigFactory.getInstance();
        String loginURL = securityConfig.getLoginURL(userRole != null, false);

        if(userRole != null)
        {
            loginURL = loginURL.replaceAll("\\$\\{userRole\\}", userRole.toString());
        }

        return RedirectUtils.getLoginURL(loginURL, httpServletRequest);
    }
}
