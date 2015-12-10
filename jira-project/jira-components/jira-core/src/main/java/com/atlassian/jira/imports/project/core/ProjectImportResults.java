package com.atlassian.jira.imports.project.core;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import com.atlassian.jira.project.Project;
import com.atlassian.jira.util.I18nHelper;

/**
 * Used to store the results of the actual importing of the project. This includes statistics on what was created
 * and any error messages that may have been generated during the import.
 *
 * @since v3.13
 */
public interface ProjectImportResults extends Serializable
{
    /**
     * This uses the start time and the end time to determine how long the actual import took.
     * @return number of milliseconds the import took to run.
     * @see #setEndTime(long)
     */
    long getImportDuration();

    /**
     * The time, as milliseconds, that the import ended. This must be set in order to get the import duration.
     *
     * @param endTime the time the import has stopped in milliseconds
     * @see #getImportDuration()
     */
    void setEndTime(long endTime);

    /**
     * Should be called to increment the count of users added to a specific role.
     *
     * @param roleName uniquely identifies the project role by its human readable name, this should not be the role id.
     * @see #getUsersCreatedCountForRole(String)
     * @see #incrementRoleGroupCreatedCount(String)
     */
    void incrementRoleUserCreatedCount(String roleName);

    /**
     * Should be called to increment the count of groups added to a specific role.
     *
     * @param roleName uniquely identifies the project role by its human readable name, this should not be the role id.
     * @see #getGroupsCreatedCountForRole(String)
     * @see #incrementRoleUserCreatedCount(String)
     */
    void incrementRoleGroupCreatedCount(String roleName);

    /**
     * Returns the role names of all roles that have had a group or user added to it.
     *
     * @return the role names of all roles that have had a group or user added to it.
     */
    Collection<String> getRoles();

    /**
     * Returns the count of all groups added to the specified role
     * @param roleName the role you want the count for, this should be the role name as provided in the increment method
     * @return the count of all groups added to the specified role
     */
    int getGroupsCreatedCountForRole(String roleName);

    /**
     * Returns the count of all users added to the specified role
     * @param roleName the role you want the count for, this should be the role name as provided in the increment method
     * @return the count of all users added to the specified role
     */
    int getUsersCreatedCountForRole(String roleName);

    /**
     * Adds the given Error message to the error list.
     * If the error limit is reached, then we throw an <code>AbortImportException</code> to stop the import.
     *
     * @param error the error message.
     */
    void addError(String error);

    /**
     * Returns a list of all the errors that were added to this result object.
     * @return a list of all the errors that were added to this result object.
     */
    List<String> getErrors();

    /**
     * Returns true if the import completed all steps of the import, false otherwise.
     * This method can return true and the getErrors can return errors.
     * This will not return true if the errors went over the allowable threshold, and the Import was aborted.
     *
     * @return true if the import completed all steps of the import, false otherwise.
     */
    boolean isImportCompleted();

    /**
     * Adds to the count of created issues.
     */
    void incrementIssuesCreatedCount();

    /**
     * Adds to the count of created users.
     */
    void incrementUsersCreatedCount();

    /**
     * Adds to the count of created attachments.
     */
    void incrementAttachmentsCreatedCount();

    /**
     * Returns the number of created issues.
     * @return the number of created issues.
     */
    int getIssuesCreatedCount();

    /**
     * Returns the number of created users.
     * @return the number of created users.
     */
    int getUsersCreatedCount();

    /**
     * Returns the number of created attachments.
     * @return the number of created attachments.
     */
    int getAttachmentsCreatedCount();

    /**
     * Returns the number of issues the import expected it could create before doing the actual import.
     * @return the number of issues the import expected it could create before doing the actual import.
     */
    int getExpectedIssuesCreatedCount();

    /**
     * Returns the number of users the import expected it could create before doing the actual import.
     * @return the number of users the import expected it could create before doing the actual import.
     */
    int getExpectedUsersCreatedCount();

    /**
     * Returns the number of attachments the import expected it could create before doing the actual import.
     * @return the number of attachments the import expected it could create before doing the actual import.
     */
    int getExpectedAttachmentsCreatedCount();

    /**
     * Should be called with true if all steps of the import were able to proceed (i.e. no AbortImportException was thrown).
     *
     * @param importCompleted true if completed, false otherwise.
     */
    void setImportCompleted(boolean importCompleted);

    /**
     * Returns the created/updated project that the import imported into.
     * @return the created/updated project that the import imported into.
     */
    Project getImportedProject();

    /**
     * Sets the imported project, this is the project that exists in JIRA.
     *
     * @param importedProject the imported project, this is the project that exists in JIRA.
     */
    void setImportedProject(Project importedProject);

    /**
     * Returns the <code>I18nHelper</code> associated with this Project Import.
     * This can be used to localise error messages before they are added in this ProjectImportResult.
     *
     * @return the <code>I18nHelper</code> associated with this Project Import.
     */
    I18nHelper getI18n();

    /**
     * Returns false if the number of allowed errors have been exceeded, true otherwise
     *
     * @return false if the number of allowed errors have been exceeded, true otherwise
     */
    boolean abortImport();
}
