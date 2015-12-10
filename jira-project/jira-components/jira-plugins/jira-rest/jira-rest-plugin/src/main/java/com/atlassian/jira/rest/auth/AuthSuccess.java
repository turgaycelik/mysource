package com.atlassian.jira.rest.auth;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Authentication success.
 *
 * @since v4.2
 */
@XmlRootElement (name = "authSuccess")
class AuthSuccess
{
    public SessionInfo session;
    public LoginInfo loginInfo;

    public AuthSuccess(final SessionInfo sessionInfo, final LoginInfo loginInfo)
    {
        this.session = sessionInfo;
        this.loginInfo = loginInfo;
    }

    static final AuthSuccess DOC_EXAMPLE;
    static
    {
        DOC_EXAMPLE = new AuthSuccess(SessionInfo.DOC_EXAMPLE, LoginInfo.DOC_EXAMPLE);
    }
}
