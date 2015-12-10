package com.atlassian.jira.rest.auth;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Current user.
 *
 * @since v4.2
 */
@XmlRootElement (name = "currentUser")
class CurrentUser
{
    @XmlElement
    private URI self;

    @XmlElement
    private String name;

    @XmlElement
    private LoginInfo loginInfo;

    public CurrentUser userName(final String userName)
    {
        this.name = userName;
        return this;
    }

    public CurrentUser loginInfo(final LoginInfo loginInfo)
    {
        this.loginInfo = loginInfo;
        return this;
    }

    public CurrentUser self(final URI uri)
    {
        this.self = uri;
        return this;
    }

    static final CurrentUser DOC_EXAMPLE;
    static
    {
        try
        {
            DOC_EXAMPLE = new CurrentUser();
            DOC_EXAMPLE.name = "fred";
            DOC_EXAMPLE.loginInfo = LoginInfo.DOC_EXAMPLE;
            DOC_EXAMPLE.self = new URI("http://www.example.com/jira/rest/api/2.0/user/fred");
        }
        catch (URISyntaxException e)
        {
            throw new RuntimeException(e);
        }
    }
}
