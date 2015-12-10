package com.atlassian.jira.util.system.check;

import com.atlassian.jira.security.login.JiraElevatedSecurityGuard;
import com.atlassian.jira.web.util.HelpUtil;

/**
 * http://jira.atlassian.com/browse/JRA-21205
 * <p/>
 * System check for missing JIRA Elevated Security Manager
 *
 * @since v4.1
 */
public class JRA21205Check implements SystemEnvironmentCheck
{
    public I18nMessage getWarningMessage()
    {
        if (isMissingElevatedSecurityManager())
        {
            HelpUtil helpUtil = new HelpUtil();

            I18nMessage warning = new I18nMessage("admin.warning.JRA21205.syscheck");
            warning.setLink(helpUtil.getHelpPath("JRA21205").getUrl());
            return warning;
        }
        return null;
    }

    private boolean isMissingElevatedSecurityManager()
    {
        return !JiraElevatedSecurityGuard.isInitialised();
    }
}