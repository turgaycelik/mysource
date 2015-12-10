package com.atlassian.jira.functest.framework.admin;

/**
 * Responsible for editing the details (name and description) of a project role.
 *
 * Assumes that the current page is the edit project role page for the role that needs to be edited.
 *
 * @since v4.2
 */
public interface RoleDetails
{
    /**
     * Sets the name of the project role in play.
     * @param name The name that will be set on the project role.
     */
    void setName(String name);

    /**
     * Sets the description of the project role in play.
     * @param description The description that will be set on the project role.
     */
    void setDescription(String description);
}