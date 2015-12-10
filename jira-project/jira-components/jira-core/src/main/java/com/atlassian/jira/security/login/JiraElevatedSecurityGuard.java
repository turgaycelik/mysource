package com.atlassian.jira.security.login;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.seraph.config.SecurityConfig;
import com.atlassian.seraph.elevatedsecurity.ElevatedSecurityGuard;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * The JIRA implementation of the {@link com.atlassian.seraph.elevatedsecurity.ElevatedSecurityGuard} interface. Its
 * elevated security is based on CAPTCHA.
 * <p/>
 * NOTE : This class is instatiated by Seraph at servlet context initialisation time hence it cant have its
 * dependencies injected.
 *
 * @since v4.0.1
 */
public class JiraElevatedSecurityGuard implements ElevatedSecurityGuard
{
    public boolean performElevatedSecurityCheck(final HttpServletRequest httpServletRequest, final String userName)
    {
        return getLoginManager().performElevatedSecurityCheck(httpServletRequest, userName);
    }

    public void onFailedLoginAttempt(final HttpServletRequest httpServletRequest, final String userName)
    {
        getLoginManager().onLoginAttempt(httpServletRequest, userName, false);
    }

    public void onSuccessfulLoginAttempt(final HttpServletRequest httpServletRequest, final String userName)
    {
        getLoginManager().onLoginAttempt(httpServletRequest, userName, true);
    }

    ///CLOVER:OFF
    public void init(final Map<String, String> params, final SecurityConfig config)
    {
       SECURITY_CONFIG.set(config);
    }

    LoginManager getLoginManager()
    {
        return ComponentAccessor.getComponentOfType(LoginManager.class);
    }

    private static final AtomicReference<SecurityConfig> SECURITY_CONFIG = new AtomicReference<SecurityConfig>();

    /**
     * @return will return TRUE if the JiraElevatedSecurityGuard is initialised and therefore in action
     */
    public static boolean isInitialised()
    {
        return SECURITY_CONFIG.get() != null;
    }
    ///CLOVER:ON
}
