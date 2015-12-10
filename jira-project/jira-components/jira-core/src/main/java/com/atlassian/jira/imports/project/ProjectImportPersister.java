package com.atlassian.jira.imports.project;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.project.component.ProjectComponent;
import com.atlassian.jira.external.ExternalException;
import com.atlassian.jira.external.beans.ExternalAttachment;
import com.atlassian.jira.external.beans.ExternalIssue;
import com.atlassian.jira.external.beans.ExternalNodeAssociation;
import com.atlassian.jira.external.beans.ExternalProject;
import com.atlassian.jira.external.beans.ExternalUser;
import com.atlassian.jira.external.beans.ExternalVoter;
import com.atlassian.jira.external.beans.ExternalWatcher;
import com.atlassian.jira.imports.project.core.BackupProject;
import com.atlassian.jira.imports.project.core.EntityRepresentation;
import com.atlassian.jira.imports.project.mapper.ProjectImportMapper;
import com.atlassian.jira.imports.project.mapper.UserMapper;
import com.atlassian.jira.imports.project.taskprogress.TaskProgressInterval;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.attachment.Attachment;
import com.atlassian.jira.issue.index.IndexException;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.util.I18nHelper;

import java.util.Date;
import java.util.Map;

/**
 * Stores entities related to project import.
 *
 * @since v3.13
 */
public interface ProjectImportPersister
{
    /**
     * Creates an issue based off of the provided ExternalIssue. This method will store the issue and make sure that
     * it is in the correct workflow state. This will preserve the key provided in the issue.
     *
     * NOTE: If you are using this method you will, at a later time, need to fix the projects pcounter to be correct
     * in relation to the manually stored issue keys.
     *
     * @param externalIssue contains the issue details that will be persisted and the project id that the issue will
     * be persisted against.
     * @param importDate a date that will be used as the value of the marker change item that is added to the issue
     * to indicate that the issue was created via a project import.
     * @param importAuthor the user who is performing the project import, this is used as the author of the change item marker for created issues
     * @return an Issue representation of the newly stored issue, null if there is a problem creating the issue.
     */
    Issue createIssue(ExternalIssue externalIssue, Date importDate, User importAuthor);

    /**
     * This will store the provided entity in the database. No validation will be performed so it is up to the
     * caller to make certain that the data has integrity.
     * If a DataAccessException occurs during this operation, then we return a <code>null</code> id.
     *
     * @param entityRepresentation the data that will be persisted.
     * @return the id of the newly created entity, or <code>null</code> if the Entity could not be created.
     */
    Long createEntity(EntityRepresentation entityRepresentation);

    /**
     * This will look at the issue specified by issueId and add a changeItem for the issue link if it needs to.
     * If a change item is added then the issues updated date will also be updated.
     * If the issue already has a change item then one will not be added. If the issue has one but also has a
     * change item that says the link was deleted this method will add a new one. If the issue has no change item
     * for this link then one will be added.
     *
     * Please note: this method will add the change item but it does not re-index the issue, this should be handled
     * elsewhere.
     *
     * @param issueId the id of the issue that should exist in JIRA. This is the issue that will have the change item
     * added to it if it does not already have one.
     * @param issueLinkTypeId the id of the issue link type in JIRA, this must exist.
     * @param linkedIssueKey the issue key that is part of the created link.
     * @param isSource true if the issue specified by issueId is the source of the issue link, false if it is the
     * destination.
     * @param importAuthor the user who is performing the project import, this is used as the author of the change item marker for created issues.
     * @return the issue id that was changed if a change item was created, null otherwise.
     */
    String createChangeItemForIssueLinkIfNeeded(String issueId, String issueLinkTypeId, String linkedIssueKey, boolean isSource, User importAuthor);

    /**
     * Re-indexes all the new Issues that have just been created in this Project Import.
     * The list of Issues to be indexed is taken from the {@link com.atlassian.jira.imports.project.mapper.ProjectImportIdMapper#getAllMappedIds()}.
     *
     * @param projectImportMapper ProjectImportMapper which is used to get the new Issue ID
     * @param taskProgressInterval Used to provide progress feedback, can be null.
     * @param i18n used to i18n the task progress messages.
     * @throws IndexException If an error occurs in the IndexManager.
     */
    void reIndexProject(ProjectImportMapper projectImportMapper, TaskProgressInterval taskProgressInterval, I18nHelper i18n) throws IndexException;

    /**
     * Creates a project, with details based off of the ExternalProject. The project will
     * be associated with the default schemes.
     *
     * @param project holds the details the project will be created with.
     *
     * @return a Project object representing the project we just created
     *
     * @throws ExternalException if there is an error creating the project.
     */
    Project createProject(ExternalProject project) throws ExternalException;

    /**
     * Creates a NodeAssocation which is used to link the issue to external values such as versions and components.
     *
     * @param nodeAssociation the externalObject that contains the correct values to be persisted to the datastore.
     * @return true if the association was created false otherwise
     */
    boolean createAssociation(ExternalNodeAssociation nodeAssociation);

    /**
     * Creates a UserAssociation which is used to link the user to the issue as a watcher.
     *
     * @param watcher contains the issue and user information
     * @return true if created, false otherwise.
     */
    boolean createWatcher(ExternalWatcher watcher);

    /**
     * Creates a UserAssociation which is used to link the user to the issue as a voter.
     *
     * @param voter contains the issue and user information
     * @return true if created, false otherwise.
     */
    boolean createVoter(ExternalVoter voter);

    /**
     * Creates the versions, specified by {@link com.atlassian.jira.imports.project.core.BackupProject#getProjectVersions()}
     * for the named backup project.
     *
     * @param backupProject specifies the name of the project to create the versions for and the versions to create
     * @return a map of {@link com.atlassian.jira.project.version.Version}'s that represent the newly created
     * versions keyed by the old version id they were created from.
     */
    Map<String,Version> createVersions(BackupProject backupProject);

    /**
     * Creates the components, specified by {@link com.atlassian.jira.imports.project.core.BackupProject#getProjectComponents()}
     * for the named backup project.
     *
     * @param backupProject specifies the name of the project to create the components for and the components to create
     * @param projectImportMapper ProjectImportMapper used to map the component leads user key
     * @return a map of {@link com.atlassian.jira.bc.project.component.ProjectComponent}'s that represent the newly created
     * components keyed by the old component id they were created from.
     */
    Map<String,ProjectComponent> createComponents(BackupProject backupProject, ProjectImportMapper projectImportMapper);

    /**
     * Updates a projects issue counter, which is used to determine the numeric portion of the issue key.
     *
     * @param backupProject contains the {@link com.atlassian.jira.external.beans.ExternalProject} that holds the details
     * @param counter this is a long that the value will be set to in the stored project.
     */
    void updateProjectIssueCounter(BackupProject backupProject, long counter);

    /**
     * Creates an attachment specified by the ExternalAttachment. This will create a database entry for the attachment
     * and will copy the file from its current path, specified by {@link com.atlassian.jira.external.beans.ExternalAttachment#getAttachedFile()}
     * to the JIRA attachment directory.
     *
     * @param externalAttachment specifies the issue id and attachment details for the attachment to create.
     * @return the created Attachment, null if the attachment was not created.
     * @throws IllegalArgumentException if the provided externalAttachment does not contain the needed and valid details
     * to create the attachment. This is the issue id, the file name, and the actual File.
     */
    Attachment createAttachment(ExternalAttachment externalAttachment);

    /**
     * Creates a User in JIRA from the given ExternalUser object.
     * This includes the custom properties as well as the standard ones.
     *
     * @param userMapper contains mapping between old and new user keys
     * @param externalUser ExternalUser containing the data to use to create the new User.
     * @return True If the user was created else false.
     */
    boolean createUser(final UserMapper userMapper, final ExternalUser externalUser);

    /**
     * Updates the details of an existing Project in JIRA from the given ExternalProject object.
     * This includes the following:
     * <ul>
     * <li>name</li>
     * <li>description</li>
     * <li>URL</li>
     * <li>lead</li>
     * <li>assignee type</li>
     * <li>email sender</li>
     * </ul>
     *
     * @param project ExternalProject containing the data to use to update.
     * @return a Project object representing the project we just updated
     */
    Project updateProjectDetails(ExternalProject project);
}
