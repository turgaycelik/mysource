package com.atlassian.jira.applicationproperties;

import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.sal.api.websudo.WebSudoRequired;

/**
 * Application properties action, really here just for WebSudo and permissions check
 *
 * @since v4.4
 */
@WebSudoRequired
public class AdvancedApplicationProperties extends JiraWebActionSupport
{
    private final PermissionManager permissionManager;
    private final JiraAuthenticationContext authenticationContext;

    public AdvancedApplicationProperties(final PermissionManager permissionManager, final JiraAuthenticationContext authenticationContext)
    {
        this.permissionManager = permissionManager;
        this.authenticationContext = authenticationContext;
    }

    @Override
    protected String doExecute() throws Exception
    {
        if(!permissionManager.hasPermission(Permissions.SYSTEM_ADMIN, authenticationContext.getLoggedInUser()))
        {
            return "securitybreach";
        }
        else
        {
            return super.doExecute();
        }
    }

}
