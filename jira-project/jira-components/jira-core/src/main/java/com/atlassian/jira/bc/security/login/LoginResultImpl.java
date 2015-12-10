package com.atlassian.jira.bc.security.login;

import org.apache.commons.lang.builder.ToStringBuilder;

import java.util.Collections;
import java.util.Set;

/**
 * Simple Implementation of {@link LoginResult}
 *
 * @since v4.0.1
 */
public class LoginResultImpl implements LoginResult
{
    private final LoginReason reason;
    private final LoginInfo loginInfo;
    private final String userName;
    private final Set<DeniedReason> deniedReasons;

    public LoginResultImpl(final LoginReason reason, final LoginInfo loginInfo, final String userName)
    {
        this(reason, loginInfo, userName, Collections.<DeniedReason>emptySet());
    }

    public LoginResultImpl(LoginReason reason, LoginInfo loginInfo, String userName, Set<DeniedReason> deniedReasons)
    {
        this.reason = reason;
        this.loginInfo = loginInfo;
        this.userName = userName;
        this.deniedReasons = deniedReasons;
    }

    @Override
    public boolean isOK()
    {
        return reason == LoginReason.OK;
    }

    @Override
    public String getUserName()
    {
        return userName;
    }

    @Override
    public LoginReason getReason()
    {
        return reason;
    }

    @Override
    public LoginInfo getLoginInfo()
    {
        return loginInfo;
    }

    @Override
    public Set<DeniedReason> getDeniedReasons()
    {
        return deniedReasons;
    }

    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this);
    }
}
