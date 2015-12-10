package com.atlassian.jira.pageobjects.model;

/**
 * Represents an operation that can be performed on an issue. Operations in the JIRA UI are identifiable by their
 * unique ID, name (visible to users) and CSS class.
 *
 * @since v4.3
 * @see com.atlassian.jira.pageobjects.model.DefaultIssueActions
 * @see com.atlassian.jira.pageobjects.model.WorkflowIssueAction
 */
public interface IssueOperation
{
    /**
     * Unique ID of the action used in the UI.
     *
     * @return ID of the issue operation
     */
    String id();

    /**
     * UI-visible name of the operation
     *
     * @return name of the operation
     */
    String uiName();

    /**
     * CSS class of the operation.
     *
     * @return unique CSS class of the operation
     */
    String cssClass();

    /**
     * whether or not this action has a shortcut associated with it.
     *
     * @return <code>true</code>, if this action has a keyboard shortcut
     */
    boolean hasShortcut();

    /**
     * Shortcut of this action (if any). It must be compatible with the
     * {@link org.openqa.selenium.WebElement#sendKeys(CharSequence...)}
     * and {@link com.atlassian.pageobjects.framework.element.PageElement#type(CharSequence...)} methods.
     *
     * @return keyboard shortcut associated with this issue operation.
     * @throws IllegalStateException if this action has no keyboard shortcut, which may be verified by means of
     * {@link #hasShortcut()}
     * @see #hasShortcut()
     * @see org.openqa.selenium.WebElement#sendKeys(CharSequence...)
     * @see org.openqa.selenium.Keys
     */
    CharSequence shortcut();
}
