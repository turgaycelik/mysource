package com.atlassian.jira.pageobjects.framework;

/**
 * Enumeration of existing types of message boxes in JIRA forms.
 *
 * @since v4.4
 */
public enum MessageType
{
    INFO("info"),
    WARNING("warning"),
    ERROR("error");

    private final String cssClass;

    MessageType(String cssClass)
    {
        this.cssClass = cssClass;
    }

    public String cssClass()
    {
        return cssClass;
    }
}
