package com.atlassian.jira.rest.auth;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Session information.
 *
 * @since v4.2
 */
@XmlRootElement (name = "session")
class SessionInfo
{
    public String name;
    public String value;

    public SessionInfo(final String name, final String value)
    {
        this.name = name;
        this.value = value;
    }

    static final SessionInfo DOC_EXAMPLE;
    static
    {
        DOC_EXAMPLE = new SessionInfo("JSESSIONID", "12345678901234567890");
    }
}
