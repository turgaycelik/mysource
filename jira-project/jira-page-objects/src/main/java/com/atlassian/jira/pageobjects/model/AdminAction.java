package com.atlassian.jira.pageobjects.model;

import com.atlassian.jira.pageobjects.pages.admin.customfields.FieldWisherDialog;

/**
 * @since v6.1
 */
public class AdminAction<T>
{
    public final static AdminAction<FieldWisherDialog> ADD_FIELD =
            new AdminAction<FieldWisherDialog>("com.atlassian.jira.jira-project-config-plugin:add-custom-field", "Add field", FieldWisherDialog.class);

    private final String id;
    private final String name;
    private final Class<T> pageClass;

    public AdminAction(String id, String name, final Class<T> pageClass)
    {
        this.id = id;
        this.name = name;
        this.pageClass = pageClass;
    }

    public String id()
    {
        return id;
    }

    public String uiName()
    {
        return name;
    }

    public Class<T> getPageClass()
    {
        return pageClass;
    }
}
