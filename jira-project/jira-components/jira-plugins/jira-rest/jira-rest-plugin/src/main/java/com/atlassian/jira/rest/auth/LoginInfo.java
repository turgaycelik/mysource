package com.atlassian.jira.rest.auth;

import com.atlassian.jira.rest.bind.DateTimeAdapter;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.Date;

/**
 * Login information.
 *
 * @since v4.2
 */
@XmlRootElement (name = "loginInfo")
class LoginInfo
{
    public Long failedLoginCount;

    public Long loginCount;

    @XmlJavaTypeAdapter (DateTimeAdapter.class)
    public Date lastFailedLoginTime;

    @XmlJavaTypeAdapter (DateTimeAdapter.class)
    public Date previousLoginTime;

    public LoginInfo(com.atlassian.jira.bc.security.login.LoginInfo loginInfo)
    {
        this.failedLoginCount = loginInfo.getTotalFailedLoginCount();
        this.loginCount = loginInfo.getLoginCount();
        final Long failedLoginTime = loginInfo.getLastFailedLoginTime();
        if (failedLoginTime != null)
        {
            this.lastFailedLoginTime = new Date(failedLoginTime);
        }
        final Long previousLoginTime = loginInfo.getPreviousLoginTime();
        if (previousLoginTime != null)
        {
            this.previousLoginTime = new Date(previousLoginTime);
        }
    }

    LoginInfo() {}

    static final LoginInfo DOC_EXAMPLE;
    static
    {
        DOC_EXAMPLE = new LoginInfo();
        DOC_EXAMPLE.failedLoginCount = 10L;
        DOC_EXAMPLE.loginCount = 127L;
        DOC_EXAMPLE.lastFailedLoginTime = new Date();
        DOC_EXAMPLE.previousLoginTime = new Date();
    }
}
