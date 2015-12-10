package com.atlassian.jira.rest.auth;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * The authentication parameters passed by clients during log-in.
 *
 * @since v4.2
 */
@XmlRootElement (name = "session")
class AuthParams
{
    public String username;
    public String password;

    static final AuthParams DOC_EXAMPLE;
    static
    {
        DOC_EXAMPLE = new AuthParams();
        DOC_EXAMPLE.username = "fred";
        DOC_EXAMPLE.password = "freds_password";
    }
}
