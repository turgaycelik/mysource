package com.atlassian.jira.functest.framework.admin;

import java.util.Map;

/**
 * Framework for manipulating projects
 *
 * @since v3.13
 */
public interface Project
{
    /**
     * Checks whether or not the specified project exists.
     * @param project the name of the project to check e.g. "homosapien"
     * @return true if the project already exists; false otherwise
     */
    boolean projectWithKeyExists(String project);

    /**
     * Adds a project, or if a project with that name exists, does almost nothing.
     * Choose a project name that will not clash with operational links on the page
     * such as "View Projects" or "Add".
     *
     * @param name the name of the project.
     * @param key  the project key.
     * @param lead the username of the project lead.
     * @return the project id.
     */
    long addProject(String name, String key, String lead);

    /**
     * Change the parameters for the passed project.
     *
     * @param projectId the id of the project to change.
     * @param name the new name of the project. Set to null to keep current setting.
     * @param description the description of the project. Set to null to keep current setting.
     * @param url the url for the project. Set to null to keep current setting.
     */
    void editProject(long projectId, String name, String description, String url);

    /**
     * Delete a project
     *
     * @param projectId the id of the project
     */
    void deleteProject(long projectId);

    /**
     * Delete project with the given name
     *
     * @param project the project name.
     */
    public void deleteProject(String project);    

    /**
     * Adds a version to a project.
     *
     * @param projectKey the key of the project e.g. "HSP".
     * @param versionName the name of the version e.g. "New Version 1"
     * @param description the new description of the version; use null to not provide a description
     * @param releaseDate the new release date of the version; use null to not provide a release date
     * @return the version id.
     */
    String addVersion(String projectKey, String versionName, String description, String releaseDate);

    /**
     * Archives a version of a project.
     *
     * @param projectKey the name of the project e.g. "HSP"
     * @param versionName the name of the version e.g. "New Version 5"
     */
    void archiveVersion(String projectKey, String versionName);

    /**
     * Releases a version of a project.
     *
     * @param projectKey the name of the project e.g. "HSP". Must not be null.
     * @param versionName the name of the version e.g. "New Version 5". Must not be null.
     * @param releaseDate the release date of the version. Use null to ignore setting of this field.
     */
    void releaseVersion(String projectKey, String versionName, String releaseDate);

    /**
     * Unreleases a version of a project.
     *
     * @param projectKey the name of the project e.g. "HSP". Must not be null.
     * @param versionName the name of the version e.g. "New Version 5". Must not be null.
     * @param releaseDate the release date of the version. Use null to ignore setting of this field.
     */
    void unreleaseVersion(String projectKey, String versionName, String releaseDate);

    /**
     * Deletes a version of a project.
     *
     * @param projectKey the name of the project e.g. "HSP". Must not be null.
     * @param versionName the name of the version e.g. "New Version 5". Must not be null.
     */
    void deleteVersion(String projectKey, String versionName);

    /**
     * Edits a version of a project.
     *
     * @param projectName the name of the project e.g. "homosapien".
     * @param versionName the name of the version e.g. "New Version 1"
     * @param name the new name of the version; use null to retain previous value
     * @param description the new description of the version; use null to retain previous value
     * @param releaseDate the new release date of the version; use null to retain previous value
     */
    void editVersionDetails(String projectName, String versionName, String name, String description, String releaseDate);

    /**
     * Edits a component of a project.
     *
     * @param projectKey the key of the project e.g. "HSP".
     * @param componentName the name of the component e.g. "New Component 1"
     * @param name the new name of the component; use null to retain previous value
     * @param description the new description of the component; use null to retain previous value
     * @param leadUserName the new username of the component lead; use null to retain previous value
     */
    void editComponent(String projectKey, String componentName, String name, String description, String leadUserName);

    /**
     * Associates the specified configuration scheme with the project.
     *
     * @param projectName the name of the project to alter
     * @param newFieldConfigurationSchemeName the name of the new field configuration scheme; use <code>null</code> or
     * <code>"None"</code> to restore the System Default Field Configuration Scheme.
     */
    void associateFieldConfigurationScheme(String projectName, String newFieldConfigurationSchemeName);

    /**
     * Associates the specified configuration scheme with the project.
     *
     * @param projectName the name of the project to alter
     * @param newIssueLevelSecuritySchemeName the name of the new issue level security scheme; use <code>null</code> or
     * <code>"None"</code> to remove the scheme.
     */
    void associateIssueLevelSecurityScheme(String projectName, String newIssueLevelSecuritySchemeName);

    /**
     * Associates the specified workflow scheme with the project.
     *
     * @param projectName the name of the project to alter
     * @param workflowSchemeName name of the workflow scheme to associate.
     */
    void associateWorkflowScheme(String projectName, String workflowSchemeName);

    /**
     * Associates the specified workflow scheme with the project.
     *
     * @param projectName the name of the project to alter
     * @param workflowSchemeName name of the workflow scheme to associate.
     * @param statusMapping mapping for statuses
     */
    void associateWorkflowScheme(String projectName, String workflowSchemeName, Map<String, String> statusMapping, boolean wait);

    /**
     * Creates a draft workflow scheme of the project.
     *
     * @param projectKey the key of the project whose draft scheme will be created
     */
    void createWorkflowSchemeDraft(String projectKey);

    /**
     * Edits the workflow scheme draft of the given project.
     *
     * @param projectKey the key of the project whose draft scheme will be edited
     * @param workflowName name of the workflow to assign issue types to.
     * @param issueTypeIds issue types to assign.
     */
    void assignToDraftScheme(String projectKey, String workflowName, String... issueTypeIds);

    /**
     * Publishes the workflow scheme draft of the specified project.
     *
     * @param projectName the name of the project to alter
     * @param schemeId
     * @param statusMapping mapping for statuses
     */
    void publishWorkflowSchemeDraft(String projectName, Long schemeId, Map<String, String> statusMapping, boolean wait);

    /**
     * Waits for the asynchronous workflow migration to complete.
     *
     * @param sleepTime The time to sleep before refreshing the page again and checking for the operation to
     * be finished.
     * @param retryCount The number of times we will try to check for the operation to be finished.
     */
    void waitForWorkflowMigration(long sleepTime, int retryCount);

    /**
     * Associates the specified notification scheme with the project.
     *
     * @param projectName the name of the project to alter
     * @param notificationSchemeName name of the notification scheme to associate.
     */
    void associateNotificationScheme(String projectName, String notificationSchemeName);

    /**
     * Sets the lead for a project
     * @param projectName the project to change
     * @param userName the new project lead
     */
    void setProjectLead(String projectName, String userName);

    /**
     * Adds a component to the project
     *
     * @param projectKey the key of the project
     * @param componentName the component name
     * @param description the description; use <code>null</code> to not set one
     * @param leadUserName the lead user's name; use <code>null</code> to not set one
     * @return the id of the component
     */
    String addComponent(String projectKey, String componentName, String description, String leadUserName);
}
