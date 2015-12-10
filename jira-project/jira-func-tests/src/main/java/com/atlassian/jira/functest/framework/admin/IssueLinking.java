package com.atlassian.jira.functest.framework.admin;

/**
 * Represents the Issue Linking administration page.
 *
 * @since v4.0
 */
public interface IssueLinking
{
    /**
     * Enables issue linking across JIRA.
     */
    void enable();

    /**
     * Disables issue linking across JIRA.
     */
    void disable();

    /**
     * Adds an issue link type to JIRA.
     *
     * @param name the name of the issue link type to create.
     * @param outward the label to describe outward links.
     * @param inward the label to describe inward links.
     */
    void addIssueLink(String name, String outward, String inward);

    /**
     * Deletes an issue link type.
     *
     * @param name the name of the issue link type to delete.
     */
    void delete(String name);

    /**
     * Determines whether an issue link type exists or not.
     *
     * @param name The name of the issue type to test for.
     * @return true if the issue link type with the specified name exists; otherwise false.
     */
    boolean exists(String name);
}
