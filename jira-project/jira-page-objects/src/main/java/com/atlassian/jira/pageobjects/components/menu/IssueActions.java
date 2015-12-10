package com.atlassian.jira.pageobjects.components.menu;

import org.openqa.selenium.By;

/**
 * An enum representing all the different actions that can be performed on an issue
 *
 * @since 5.0
 *
 * @deprecated use {@link com.atlassian.jira.pageobjects.model.IssueOperation} instead
 */
@Deprecated
public enum IssueActions
{
    ASSIGN_ISSUE("Assign", By.className("issueaction-assign-issue"), "a"),
    ASSIGN_TO_ME("Assign To Me", By.className("issueaction-assign-to-me"), "i"),
    CLOSE_ISSUE("Close Issue", By.linkText("Close Issue")),
    RESOLVE_ISSUE("Resolve Issue", By.linkText("Resolve Issue")),
    START_PROGRESS("Start Progress", By.linkText("Start Progress")),
    STOP_PROGRESS("Stop Progress", By.linkText("Stop Progress")),
    CREATE_SUBTASK("Create Subtask", By.className("issueaction-create-subtask")),
    EDIT_ISSUE("Edit", By.className("issueaction-edit-issue"), "e"),
    LABELS("Labels", By.className("issueaction-edit-labels"), "l"),
    COMMENT("Comment", By.className("issueaction-comment-issue"), "m"),
    START_WATCHING("Watch Issue", By.className("issueaction-watch-issue")),
    STOP_WATCHING("Stop Watching", By.className("issueaction-unwatch-issue")),
    DELETE("Delete", By.className("issueaction-delete-issue"));


    private final String label;
    private final By selector;
    private final CharSequence[] shortcut;

    /**
     * @param selector item that when clicked will invoke action
     */
    private IssueActions(final String label, final By selector)
    {
        this.label = label;
        this.selector = selector;
        this.shortcut = null;
    }

    /**
     * @param selector item that when clicked will invoke action
     */
    private IssueActions(final String label, final By selector, final CharSequence... shortcut)
    {
        this.label = label;
        this.selector = selector;
        this.shortcut = shortcut;
    }

    /**
     * Gets selector
     * @return selector
     */
    public By getSelector()
    {
        return selector;
    }

    /**
     * Gets label
     * @return label
     */
    public String getLabel()
    {
        return label;
    }

    /**
     * Gets keyboard shortcut for the action
     * @return keyboard shortcut for the action
     */
    public CharSequence[] getShortcut()
    {
        return shortcut;
    }
}
