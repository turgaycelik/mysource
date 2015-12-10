package com.atlassian.jira.plugins.share;

import java.util.LinkedHashSet;
import java.util.Set;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Represents information necessary to share a an issue, filter or search.
 *
 * @since v5.0
 */
@XmlRootElement (name = "share-search")
public class ShareBean
{
    @XmlElement
    private Set<String> usernames = new LinkedHashSet<String>();

    @XmlElement
    private Set<String> emails = new LinkedHashSet<String>();

    @XmlElement
    private String message;

    @XmlElement
    private String jql;

    public ShareBean() { }

    public ShareBean(Set<String> usernames, Set<String> emails, String message, String jql)
    {
        if (usernames != null)
        {
            this.usernames.addAll(usernames);
        }
        if (emails != null)
        {
            this.emails.addAll(emails);
        }
        this.message = message;
        this.jql = jql;
    }

    public Set<String> getUsernames()
    {
        return usernames;
    }

    public Set<String> getEmails()
    {
        return emails;
    }

    public String getMessage()
    {
        return message;
    }

    public String getJql()
    {
        return jql;
    }
}
