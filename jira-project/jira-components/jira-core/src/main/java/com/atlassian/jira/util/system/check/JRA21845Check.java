package com.atlassian.jira.util.system.check;

import com.atlassian.jira.web.util.HelpUtil;
import com.atlassian.seraph.config.SecurityConfig;
import com.atlassian.seraph.config.SecurityConfigFactory;

/**
 * Check for new Seraph Remember Me support
 * @since v4.2
 */
public class JRA21845Check implements SystemEnvironmentCheck
{
    public static final String JRA = "JRA21845";

    public I18nMessage getWarningMessage()
    {
        if (isWrongLoginCookieKey())
        {
            HelpUtil helpUtil = new HelpUtil();

            I18nMessage warning = new I18nMessage(String.format("admin.warning.%s.logincookie.syscheck", JRA));
            warning.setLink(helpUtil.getHelpPath(JRA).getUrl());
            return warning;
        }
        return null;
    }

    private boolean isWrongLoginCookieKey()
    {
        final SecurityConfig securityConfig = SecurityConfigFactory.getInstance();
        final String loginCookieKey = securityConfig.getLoginCookieKey();
        return loginCookieKey == null || !loginCookieKey.equals("seraph.rememberme.cookie");
    }

}
