package com.atlassian.jira.functest.framework.admin;

/**
 * Responsible for representing the Project Roles Browser.
 * </p>
 * <p>
 * The Project Roles Browser is an administrative interface on JIRA's project roles.
 * </p>
 * @since v3.13
 */
public interface Roles
{
    /**
     * Deletes a project role with the specified id.
     *
     * @param roleId The id of the role to delete.
     */
    void delete(long roleId);

    /**
     * Deletes a project role with the specified name.
     * @param name The name of the role to delete.
     */
    void delete(String name);

    /**
     * Adds a project role for a particular user.
     *
     * @param projectName the name of the project.
     * @param roleName      the name of the role to add.
     * @param userName    the name of the user.
     */
    void addProjectRoleForUser(String projectName,String roleName, String userName);

    /**
     * Creates a project role with the specified name and description.
     * @param name The name of the specified role.
     * @param description The description of the specified role.
     */
    void create(String name, String description);

    /**
     * Allows editing of a project role's details.
     * @param name The name of the project role to edit.
     * @return A {@link com.atlassian.jira.functest.framework.admin.RoleDetails} which allows to set the properties
     * (i.e. name, description ...) of the project role in play.
     */
    RoleDetails edit(String name);
}