package com.atlassian.jira.pageobjects.framework.fields;

/**
 * Custom fields stuff.
 *
 * @since v5.2
 */
public final class CustomFields
{

    private CustomFields()
    {
        throw new AssertionError("No");
    }

    public static String jiraCustomFieldId(int id)
    {
        return "customfield_" + id;
    }
}
