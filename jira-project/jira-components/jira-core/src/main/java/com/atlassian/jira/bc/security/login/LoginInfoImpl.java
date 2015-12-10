package com.atlassian.jira.bc.security.login;

import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * @since v4.0.1
 */
public class LoginInfoImpl implements LoginInfo
{
    private final Long lastLoginTime;
    private final Long previousLoginTime;
    private final Long loginCount;
    private final Long currentFailedLoginCount;
    private final Long totalFailedLoginCount;
    private final Long lastFailedLoginTime;
    private final boolean elevatedSecurityCheckRequired;
    private final Long maxAuthenticationAttemptsAllowed;

    public LoginInfoImpl(final Long lastLoginTime, final Long previousLoginTime, final Long lastFailedLoginTime, final Long loginCount,
            final Long currentFailedLoginCount, final Long totalFailedLoginCount, final Long maxAuthenticationAttemptsAllowed, final boolean elevatedSecurityCheckRequired)
    {
        this.lastLoginTime = lastLoginTime;
        this.previousLoginTime = previousLoginTime;
        this.lastFailedLoginTime = lastFailedLoginTime;
        this.loginCount = loginCount;
        this.currentFailedLoginCount = currentFailedLoginCount;
        this.totalFailedLoginCount = totalFailedLoginCount;
        this.maxAuthenticationAttemptsAllowed = maxAuthenticationAttemptsAllowed;
        this.elevatedSecurityCheckRequired = elevatedSecurityCheckRequired;
    }

    public LoginInfoImpl(final LoginInfo loginInfo, final boolean elevatedSecurityCheckRequired)
    {
        this(loginInfo.getLastLoginTime(), loginInfo.getPreviousLoginTime(), loginInfo.getLastFailedLoginTime(), loginInfo.getLoginCount(),
                loginInfo.getCurrentFailedLoginCount(), loginInfo.getTotalFailedLoginCount(), loginInfo.getMaxAuthenticationAttemptsAllowed(), elevatedSecurityCheckRequired);
    }

    public Long getMaxAuthenticationAttemptsAllowed()
    {
        return maxAuthenticationAttemptsAllowed;
    }

    public Long getLastLoginTime()
    {
        return lastLoginTime;
    }

    public Long getPreviousLoginTime()
    {
        return previousLoginTime;
    }

    public Long getLoginCount()
    {
        return loginCount;
    }

    public Long getCurrentFailedLoginCount()
    {
        return currentFailedLoginCount;
    }

    public Long getTotalFailedLoginCount()
    {
        return totalFailedLoginCount;
    }

    public Long getLastFailedLoginTime()
    {
        return lastFailedLoginTime;
    }

    public boolean isElevatedSecurityCheckRequired()
    {
        return elevatedSecurityCheckRequired;
    }

    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this);
    }
}
