package com.atlassian.jira.web.action.admin.importer.project;

import com.atlassian.core.util.XMLUtils;
import com.atlassian.jira.web.action.JiraWebActionSupport;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Base class that exports a list of users to xml.
 *
 * @since v3.13
 */
public abstract class AbstractProjectImportUsersMissing extends JiraWebActionSupport
{
    private int userCountLimit = 100;

    public abstract Collection getUsers();

    public abstract String getTitle();

    public abstract String getDescription();

    protected AbstractProjectImportUsersMissing()
    {}

    public String doXmlExport()
    {
        return "xml";
    }

    public String getUserCountLimitMessage()
    {
        if (getUsers().size() > getUserCountLimit())
        {
            return "<p/><span class='note'>" + getText("admin.project.import.users.display.number", String.valueOf(getUserCountLimit())) + "</span>";
        }
        else
        {
            return "";
        }
    }

    public Collection getLimitedUsers()
    {
        final Collection users = getUsers();
        final int listEnd = (users.size() <= getUserCountLimit()) ? users.size() : getUserCountLimit();
        return new ArrayList(users).subList(0, listEnd);
    }

    public int getUserCountLimit()
    {
        return userCountLimit;
    }

    public void setUserCountLimit(final int userCountLimit)
    {
        this.userCountLimit = userCountLimit;
    }

    public String xmlEscape(final String text)
    {
        return XMLUtils.escape(text);
    }
}
