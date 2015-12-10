package com.atlassian.jira.security.login;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.security.login.LoginInfo;
import com.atlassian.jira.bc.security.login.LoginResult;
import com.atlassian.jira.user.ApplicationUser;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.Set;

/**
 * An implementation of LoginManager that is suitable for bootstrap.  There is no security needed so this guy does
 * bugger all.
 *
 * @since v5.2
 */
public class BootstrapLoginManagerImpl implements LoginManager
{

    @Override
    public Set<String> getRequiredRoles(HttpServletRequest httpServletRequest)
    {
        return Collections.emptySet();
    }

    @Override
    public LoginInfo getLoginInfo(String userName)
    {
        throw new UnsupportedOperationException("Not implemented for JIRA bootstrap time");
    }

    @Override
    public boolean performElevatedSecurityCheck(HttpServletRequest httpServletRequest, String userName)
    {
        throw new UnsupportedOperationException("Not implemented for JIRA bootstrap time");
    }

    @Override
    public LoginInfo onLoginAttempt(HttpServletRequest httpServletRequest, String userName, boolean loginSuccessful)
    {
        throw new UnsupportedOperationException("Not implemented for JIRA bootstrap time");
    }

    @Override
    public LoginResult authenticate(User user, String password)
    {
        throw new UnsupportedOperationException("Not implemented for JIRA bootstrap time");
    }

    @Override
    public LoginResult authenticateWithoutElevatedCheck(User user, String password)
    {
        throw new UnsupportedOperationException("Not implemented for JIRA bootstrap time");
    }

    @Override
    public boolean authoriseForLogin(@Nonnull ApplicationUser user, HttpServletRequest httpServletRequest)
    {
        throw new UnsupportedOperationException("Not implemented for JIRA bootstrap time");
    }


    @Override
    public boolean authoriseForRole(@Nullable ApplicationUser user, HttpServletRequest httpServletRequest, String role)
    {
        throw new UnsupportedOperationException("Not implemented for JIRA bootstrap time");
    }

    @Override
    public void logout(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse)
    {
        throw new UnsupportedOperationException("Not implemented for JIRA bootstrap time");
    }

    @Override
    public boolean isElevatedSecurityCheckAlwaysShown()
    {
        throw new UnsupportedOperationException("Not implemented for JIRA bootstrap time");
    }

    @Override
    public void resetFailedLoginCount(User user)
    {
        throw new UnsupportedOperationException("Not implemented for JIRA bootstrap time");
    }
}
