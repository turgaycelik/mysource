package com.atlassian.jira.functest.framework.page;

import com.atlassian.jira.functest.framework.LocatorFactory;
import org.w3c.dom.Element;

/**
 * @since v6.0
 */
public class ViewProfilePage
{
    private final LocatorFactory locator;

    public ViewProfilePage(LocatorFactory locator)
    {
        this.locator = locator;
    }

    public String getUsername()
    {
        return getFieldValue(Section.DETAILS.field("username"));
    }

    public String getFullName()
    {
        return getFieldValue(Section.DETAILS.field("fullname"));
    }

    public String getEmailAddress()
    {
        return getFieldValue(Section.DETAILS.field("email"));
    }

    public String getChangePasswordLink()
    {
        final Element link = (Element)locator.id("view_change_password").getNode();
        return (link != null) ? link.getAttribute("href") : null;
    }

    public int getPageSize()
    {
        return Integer.parseInt(getFieldValue(Section.PREFERENCES.field("pagesize")));
    }

    public String getEmailType()
    {
        return getFieldValue(Section.PREFERENCES.field("mimetype"));
    }

    public String getLanguage()
    {
        return getFieldValue(Section.PREFERENCES.field("locale"));
    }

    /**
     * Returns the current value of the named element as appearing on this View Profile Page.
     * @param id the HTML id value of the desired text field
     * @return the current value of the named element as appearing on this View Profile Page.
     */
    public String getFieldValue(String id)
    {
        String value = locator.id(id).getText();
        return value == null ? null : value.trim();
    }

    static enum Section
    {
        DETAILS('d'),
        PREFERENCES('p');

        private final char sectionKey;

        Section(char sectionKey)
        {
            this.sectionKey = sectionKey;
        }

        String field(String fieldName)
        {
            return "up-" + sectionKey + '-' + fieldName;
        }
    }

}
