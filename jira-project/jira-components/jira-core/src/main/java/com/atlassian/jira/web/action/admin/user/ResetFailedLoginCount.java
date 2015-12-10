package com.atlassian.jira.web.action.admin.user;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.security.login.LoginService;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import org.apache.commons.lang.StringUtils;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Resets the named users current failed login count.
 *
 * @since v4.0
 */
@WebSudoRequired
public class ResetFailedLoginCount extends JiraWebActionSupport
{
    private final UserManager userManager;
    private final LoginService loginService;
    private String name;
    private User user;

    public ResetFailedLoginCount(final UserManager userManager, final LoginService loginService)
    {
        this.userManager = notNull("userManager", userManager);
        this.loginService = notNull("loginService", loginService);
    }


    @Override
    protected void doValidation()
    {
        user = userManager.getUser(name);
        if (user == null)
        {
            addError("userName", getText("admin.resetfailedlogin.unknown.user", name));
        }
    }

    @RequiresXsrfCheck
    @Override
    protected String doExecute() throws Exception
    {
        loginService.resetFailedLoginCount(user);
        String returnUrl = getReturnUrl();
        if (StringUtils.isBlank(returnUrl))
        {
            returnUrl = "secure/admin/user/UserBrowser.jspa";
        }
        returnUrl = addNameParameter(returnUrl);
        setReturnUrl(returnUrl);
        return getRedirect(returnUrl);
    }

    private String addNameParameter(final String returnUrl)
    {
        StringBuilder sb = new StringBuilder(returnUrl);
        if (returnUrl.indexOf("?") == -1)
        {
            sb.append("?");
        }
        else
        {
            sb.append("&");
        }
        sb.append("name=").append(name);
        return sb.toString();
    }

    public String getName()
    {
        return name;
    }

    public void setName(final String name)
    {
        this.name = name;
    }
}

