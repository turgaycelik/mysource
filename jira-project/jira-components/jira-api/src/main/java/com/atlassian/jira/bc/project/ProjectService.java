package com.atlassian.jira.bc.project;

import com.atlassian.annotations.ExperimentalApi;
import com.atlassian.annotations.Internal;
import com.atlassian.annotations.PublicApi;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.ServiceOutcome;
import com.atlassian.jira.bc.ServiceResult;
import com.atlassian.jira.bc.ServiceResultImpl;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.SimpleErrorCollection;

import java.util.List;
import javax.annotation.Nullable;

@PublicApi
public interface ProjectService
{
    /**
     * The default name of HTML fields containing a Project's name. Validation methods on this service
     * (isValidAllProjectData) will return an {@link com.atlassian.jira.util.ErrorCollection} with error messages keyed
     * to this field name.
     */
    public static final String PROJECT_NAME = "projectName";

    /**
     * The default name of HTML fields containing a Project's key. Validation methods on this service
     * (isValidAllProjectData) will return an {@link com.atlassian.jira.util.ErrorCollection} with error messages keyed
     * to this field name.
     */
    public static final String PROJECT_KEY = "projectKey";

    /**
     * The default name of HTML fields containing a Project's lead. Validation methods on this service
     * (isValidAllProjectData) will return an {@link com.atlassian.jira.util.ErrorCollection} with error messages keyed
     * to this field name.
     */
    public static final String PROJECT_LEAD = "projectLead";

    /**
     * The default name of HTML fields containing a Project's URL. Validation methods on this service
     * (isValidAllProjectData) will return an {@link com.atlassian.jira.util.ErrorCollection} with error messages keyed
     * to this field name.
     */
    public static final String PROJECT_URL = "projectUrl";

    /**
     * The default name of HTML fields containing a Project's description. Validation methods on this service
     * (isValidAllProjectData) will return an {@link com.atlassian.jira.util.ErrorCollection} with error messages keyed
     * to this field name.
     */
    public static final String PROJECT_DESCRIPTION = "projectDescription";

    /**
     * The maximum length allowed for the project name field.
     *
     * @deprecated use getMaximumNameLength() instead;
     */
    public static final int MAX_NAME_LENGTH = 80;

    /**
     * The minimum length allowed for the project name field.
     */
    public static final int MIN_NAME_LENGTH = 2;

    /**
     * The maximum length allowed for the project key field.
     *
     * @deprecated use getMaximumKeyLength() instead
     */
    public static final int MAX_KEY_LENGTH = 10;
    /**
     * Default project name length
     */
    public static final int DEFAULT_NAME_LENGTH = 80;

    /**
     * This method needs to be called before creating a project to ensure all parameters are correct.  There are a
     * number of required parameters, such as a project name, key and lead. The validation will also check if a project
     * with the name or key provided already exists and throw an appropriate error.  The project key will be validated
     * that it matches the allowed key pattern, and it is not a reserved word. A validation error will also be added if no
     * user exists for the lead username provided.
     * <p/>
     * The default avatar will be used for the created project.
     * <p/>
     * Optional validation will be done for the url, assigneetype and avatarId parameters. The url needs to be a valid
     * URL and the assigneeType needs to be either {@link com.atlassian.jira.project.AssigneeTypes#PROJECT_LEAD},
     * {@link com.atlassian.jira.project.AssigneeTypes#UNASSIGNED}, or null to let JIRA decide on the best default
     * assignee.  UNASSIGNED will also only be valid, if unassigned issues are enabled in the General Configuration.
     * <p/>
     * The method will return a {@link com.atlassian.jira.bc.project.ProjectService.CreateProjectValidationResult} which
     * contains an ErrorCollection with any potential errors and all the project's details.
     *
     * @param user The user trying to create a project
     * @param name The name of the new project
     * @param key The project key of the new project
     * @param description An optional description for the project
     * @param leadName The username of the lead developer for the project
     * @param url An optional URL for the new project
     * @param assigneeType Optional default assignee for issues created in this project (null for default).
     * @return A validation result containing any errors and all project details
     */
    CreateProjectValidationResult validateCreateProject(User user, String name, String key, String description,
            String leadName, String url, @Nullable Long assigneeType);


    /**
     * This method needs to be called before creating a project to ensure all parameters are correct.  There are a
     * number of required parameters, such as a project name, key and lead. The validation will also check if a project
     * with the name or key provided already exists and throw an appropriate error. The project key will be validated
     * that it matches the allowed key pattern, and it is not a reserved word. A validation error will also be added if no
     * user exists for the lead username provided.
     * <p/>
     * Optional validation will be done for the url, assigneetype and avatarId parameters. The url needs to be a valid
     * URL and the assigneeType needs to be either {@link com.atlassian.jira.project.AssigneeTypes#PROJECT_LEAD},
     * {@link com.atlassian.jira.project.AssigneeTypes#UNASSIGNED}, or null to let JIRA decide on the best default
     * assignee.  UNASSIGNED will also only be valid, if unassigned issues are enabled in the General Configuration.
     * <p/>
     * The method will return a {@link com.atlassian.jira.bc.project.ProjectService.CreateProjectValidationResult} which
     * contains an ErrorCollection with any potential errors and all the project's details.
     *
     * @param user The user trying to create a project
     * @param name The name of the new project
     * @param key The project key of the new project
     * @param description An optional description for the project
     * @param leadName The username of the lead developer for the project
     * @param url An optional URL for the new project
     * @param assigneeType Optional default assignee for issues created in this project (null for default).
     * @param avatarId the id of an avatar.
     * @return A validation result containing any errors and all project details
     */
    CreateProjectValidationResult validateCreateProject(User user, String name, String key, String description,
            String leadName, String url, @Nullable Long assigneeType, Long avatarId);

    /**
     * Using the validation result from {@link #validateCreateProject(User, String, String,
     * String, String, String, Long)} a new project will be created.  This method will throw an IllegalStateException if
     * the validation result contains any errors.
     * <p/>
     * Project creation involves creating the project itself and setting some defaults for workflow schemes and issue
     * type screen schemes.
     *
     * @param createProjectValidationResult Result from the validation, which also contains all the project's details.
     * @return The new project
     * @throws IllegalStateException if the validation result contains any errors.
     */
    Project createProject(CreateProjectValidationResult createProjectValidationResult);

    /**
     * Validates that the given user is authorised to update a project. A project can be updated by any user with the
     * global admin permission or project admin permission for the project in question.
     *
     * @param user The user trying to update a project
     * @param key The project key of the project to update.
     * @return a ServiceResult, which will contain errors if the user is not authorised to update the project
     */
    ServiceResult validateUpdateProject(final User user, final String key);

    /**
     * Validates that the given user is authorised to update a project. A project can be updated by any user with the
     * global admin permission or project admin permission for the project in question.
     *
     * @param user The user trying to update a project
     * @param key The project key of the project to update.
     * @return a ServiceResult, which will contain errors if the user is not authorised to update the project
     */
    ServiceResult validateUpdateProject(final ApplicationUser user, final String key);

    /**
     * Validates updating a project's details.  The project is looked up by the key provided.  If no project with the
     * key provided can be found, an appropriate error will be added to the result.
     * <p/>
     * Validation performed will be the same as for the {@link #validateCreateProject(User,
     * String, String, String, String, String, Long)} method. The only difference is that the project key will obviously
     * not be validated.
     * <p/>
     * A project can be updated by any user with the global admin permission or project admin permission for the project
     * in question.
     *
     * @param user The user trying to update a project
     * @param name The name of the new project
     * @param key The project key of the project to update.
     * @param description An optional description for the project
     * @param leadName The username of the lead developer for the project
     * @param url An optional URL for the project
     * @param assigneeType The default assignee for issues created in this project.  May be either project lead, or
     * unassigned if unassigned issues are enabled.
     * @return A validation result containing any errors and all project details
     */
    UpdateProjectValidationResult validateUpdateProject(User user, String name, String key, String description,
            String leadName, String url, Long assigneeType);

    /**
     * Validates updating a project's details.  The project is looked up by the key provided.  If no project with the
     * key provided can be found, an appropriate error will be added to the result.
     * <p/>
     * Validation performed will be the same as for the {@link #validateCreateProject(User,
     * String, String, String, String, String, Long)} method. The only difference is that the project key will obviously
     * not be validated.
     * <p/>
     * A project can be updated by any user with the global admin permission or project admin permission for the project
     * in question.
     * <p/>
     * <strong>WARNING</strong>: In 6.0-6.0.5, this method is available but does not work properly for renamed users (JRA-33843).
     *
     * @param user The user trying to update a project
     * @param name The name of the new project
     * @param key The project key of the project to update.
     * @param description An optional description for the project
     * @param lead The lead developer for the project
     * @param url An optional URL for the project
     * @param assigneeType The default assignee for issues created in this project.  May be either project lead, or
     * unassigned if unassigned issues are enabled.
     * @return A validation result containing any errors and all project details
     * @since 6.0.6 (see warning in method description)
     */
    UpdateProjectValidationResult validateUpdateProject(ApplicationUser user, String name, String key, String description,
            ApplicationUser lead, String url, Long assigneeType);

    /**
     * Validates updating a project's details. The project is looked up by the project object. You may change the project key.
     * <p/>
     * Validation performed will be the same as for the {@link #validateCreateProject(User,
     * String, String, String, String, String, Long)} method.
     * <p/>
     * A project can be updated by any user with the global admin permission or project admin permission for the project
     * in question. A project key can be updated by any user with the global admin permission.
     *
     * @param user The user trying to update a project
     * @param originalProject The project to update with the values put in arguments ({@link Project} object should not be modified)
     * @param name The name of the new project
     * @param key The new project key
     * @param description An optional description for the project
     * @param lead The user key for the lead developer for the project
     * @param url An optional URL for the project
     * @param assigneeType The default assignee for issues created in this project.  May be either project lead, or
     * unassigned if unassigned issues are enabled.
     * @return A validation result containing any errors and all project details
     */
    UpdateProjectValidationResult validateUpdateProject(ApplicationUser user, Project originalProject, String name, String key, String description,
            ApplicationUser lead, String url, Long assigneeType, Long avatarId);

    /**
     * Validates updating a project's details.  The project is looked up by the key provided.  If no project with the
     * key provided can be found, an appropriate error will be added to the result.
     * <p/>
     * Validation performed will be the same as for the {@link #validateCreateProject(User,
     * String, String, String, String, String, Long)} method. The only difference is that the project key will obviously
     * not be validated.
     * <p/>
     * A project can be updated by any user with the global admin permission or project admin permission for the project
     * in question.
     *
     * @param user The user trying to update a project
     * @param name The name of the new project
     * @param key The project key of the project to update.
     * @param description An optional description for the project
     * @param leadName The username of the lead developer for the project
     * @param url An optional URL for the project
     * @param assigneeType The default assignee for issues created in this project.  May be either project lead, or
     * unassigned if unassigned issues are enabled.
     * @param avatarId the id of an existing avatar.
     * @return A validation result containing any errors and all project details
     */
    UpdateProjectValidationResult validateUpdateProject(User user, String name, String key, String description,
            String leadName, String url, Long assigneeType, Long avatarId);

    /**
     * Validates updating a project's details.  The project is looked up by the key provided.  If no project with the
     * key provided can be found, an appropriate error will be added to the result.
     * <p/>
     * Validation performed will be the same as for the {@link #validateCreateProject(User,
     * String, String, String, String, String, Long)} method. The only difference is that the project key will obviously
     * not be validated.
     * <p/>
     * A project can be updated by any user with the global admin permission or project admin permission for the project
     * in question.
     * <p/>
     * <strong>WARNING</strong>: In 6.0-6.0.5, this method is available but does not work properly for renamed users (JRA-33843).
     *
     * @param user The user trying to update a project
     * @param name The name of the new project
     * @param key The project key of the project to update.
     * @param description An optional description for the project
     * @param lead The lead developer for the project
     * @param url An optional URL for the project
     * @param assigneeType The default assignee for issues created in this project.  May be either project lead, or
     * unassigned if unassigned issues are enabled.
     * @param avatarId the id of an existing avatar.
     * @return A validation result containing any errors and all project details
     * @since 6.0.6 (see warning in method description)
     */
    UpdateProjectValidationResult validateUpdateProject(ApplicationUser user, String name, String key, String description,
            ApplicationUser lead, String url, Long assigneeType, Long avatarId);

    /**
     * Using the validation result from {@link #validateUpdateProject(User, String, String,
     * String, String, String, Long)} this method performs the actual update on the project.
     *
     * @param updateProjectValidationResult Result from the validation, which also contains all the project's details.
     * @return The updated project
     * @throws IllegalStateException if the validation result contains any errors.
     */
    Project updateProject(UpdateProjectValidationResult updateProjectValidationResult);

    /**
     * Validation to delete a project is quite straightforward.  The user must have global admin rights and the project
     * about to be deleted needs to exist.
     *
     * @param user The user trying to delete a project
     * @param key The key of the project to delete
     * @return A validation result containing any errors and all project details
     */
    DeleteProjectValidationResult validateDeleteProject(User user, String key);

    /**
     * Validation to delete a project is quite straightforward.  The user must have global admin rights and the project
     * about to be deleted needs to exist.
     *
     * @param user The user trying to delete a project
     * @param key The key of the project to delete
     * @return A validation result containing any errors and all project details
     */
    DeleteProjectValidationResult validateDeleteProject(ApplicationUser user, String key);

    /**
     * Deletes the project provided by the deleteProjectValidationResult.  There's a number of steps involved in
     * deleting a project, which are carried out in the following order:
     * <ul>
     * <li>Delete all the issues in the project</li>
     * <li>Remove any custom field associations for the project</li>
     * <li>Remove the IssueTypeScreenSchemeAssocation for the project</li>
     * <li>Remove any other associations of this project (to permission schemes, notification schemes...)</li>
     * <li>Remove any versions in this project</li>
     * <li>Remove any components in this project</li>
     * <li>Delete all portlets that rely on this project (either directly or via filters)</li>
     * <li>Delete all the filters for this project</li>
     * <li>Delete the project itself in the database</li>
     * <li>Flushing the issue, project and workflow scheme caches</li>
     * </ul>
     *
     * @param user The user trying to delete a project
     * @param deleteProjectValidationResult Result from the validation, which also contains all the project's details.
     * @return A result containing any errors.  Users of this method should check the result.
     */
    DeleteProjectResult deleteProject(User user, DeleteProjectValidationResult deleteProjectValidationResult);

    /**
     * Deletes the project provided by the deleteProjectValidationResult.  There's a number of steps involved in
     * deleting a project, which are carried out in the following order:
     * <ul>
     * <li>Delete all the issues in the project</li>
     * <li>Remove any custom field associations for the project</li>
     * <li>Remove the IssueTypeScreenSchemeAssocation for the project</li>
     * <li>Remove any other associations of this project (to permission schemes, notification schemes...)</li>
     * <li>Remove any versions in this project</li>
     * <li>Remove any components in this project</li>
     * <li>Delete all portlets that rely on this project (either directly or via filters)</li>
     * <li>Delete all the filters for this project</li>
     * <li>Delete the project itself in the database</li>
     * <li>Flushing the issue, project and workflow scheme caches</li>
     * </ul>
     *
     * @param user The user trying to delete a project
     * @param deleteProjectValidationResult Result from the validation, which also contains all the project's details.
     * @return A result containing any errors.  Users of this method should check the result.
     */
    DeleteProjectResult deleteProject(ApplicationUser user, DeleteProjectValidationResult deleteProjectValidationResult);

    /**
     * If the scheme ids are not null or -1 (-1 is often used to reset schemes), then an attempt will be made to
     * retrieve the scheme.  If this attempt fails an error will be added.  IssueSecuritySchemes will only be validated
     * in enterprise edition.
     *
     * @param permissionSchemeId The permission scheme that the new project should use
     * @param notificationSchemeId The notification scheme that the new project should use. Optional.
     * @param issueSecuritySchemeId The issue security scheme that the new project should use. Optional.
     * @return A validation result containing any errors and all scheme ids
     */
    UpdateProjectSchemesValidationResult validateUpdateProjectSchemes(User user, final Long permissionSchemeId,
            final Long notificationSchemeId, final Long issueSecuritySchemeId);

    /**
     * If the scheme ids are not null or -1 (-1 is often used to reset schemes), then an attempt will be made to
     * retrieve the scheme.  If this attempt fails an error will be added.  IssueSecuritySchemes will only be validated
     * in enterprise edition.
     *
     * @param permissionSchemeId The permission scheme that the new project should use
     * @param notificationSchemeId The notification scheme that the new project should use. Optional.
     * @param issueSecuritySchemeId The issue security scheme that the new project should use. Optional.
     * @return A validation result containing any errors and all scheme ids
     */
    UpdateProjectSchemesValidationResult validateUpdateProjectSchemes(ApplicationUser user, final Long permissionSchemeId,
            final Long notificationSchemeId, final Long issueSecuritySchemeId);

    /**
     * Updates the project schemes for a particular project, given a validation result and project to update.
     *
     * @param result Result from the validation, which also contains all the schemes details.
     * @param project The project which will have its schemes updated.
     * @throws IllegalStateException if the validation result contains any errors.
     */
    void updateProjectSchemes(UpdateProjectSchemesValidationResult result, Project project);

    /**
     * Validates the given project fields. Any errors will be added to the
     * {@link com.atlassian.jira.bc.JiraServiceContext}.
     *
     * @param serviceContext containing the ErrorCollection that will be populated with any validation errors that are
     * encountered
     * @param name the name of the project @Nonnull
     * @param key the key of the project @Nonnull
     * @param leadName the username of the project lead @Nonnull
     * @param url the project URL (optional)
     * @param assigneeType the default assignee type (optional - only appears on some forms)
     * @return true if project data is valid, false otherwise
     */
    boolean isValidAllProjectData(JiraServiceContext serviceContext, String name, String key, String leadName, String url, Long assigneeType);

    /**
     * Validates the given project fields. Any errors will be added to the
     * {@link com.atlassian.jira.bc.JiraServiceContext}.
     *
     * @param serviceContext containing the ErrorCollection that will be populated with any validation errors that are
     * encountered
     * @param name the name of the project @Nonnull
     * @param key the key of the project @Nonnull
     * @param leadName the username of the project lead @Nonnull
     * @param url the project URL (optional)
     * @param assigneeType the default assignee type (optional - only appears on some forms)
     * @param avatarId the id of the avatar (null indicates default avatar)
     * @return true if project data is valid, false otherwise
     */
    boolean isValidAllProjectData(JiraServiceContext serviceContext, String name, String key, String leadName, String url, Long assigneeType, Long avatarId);

    /**
     * Validate the fields required for creating a project. Any errors will be added to the
     * {@link com.atlassian.jira.bc.JiraServiceContext}.
     *
     * @param serviceContext containing the ErrorCollection that will be populated with any validation errors that are
     * encountered
     * @param name the name of the project @Nonnull
     * @param key the key of the project @Nonnull
     * @param leadName the username of the project lead @Nonnull
     * @return true if project data is valid, false otherwise
     */
    boolean isValidRequiredProjectData(JiraServiceContext serviceContext, String name, String key, String leadName);

    /**
     * Validates the given project key. Any errors will be added to the
     * {@link com.atlassian.jira.bc.JiraServiceContext}.
     *
     * @param serviceContext containing the ErrorCollection that will be populated with any validation errors that are
     * encountered
     * @param key The key to validate @Nonnull
     * @return true if project key is valid, false otherwise
     */
    boolean isValidProjectKey(JiraServiceContext serviceContext, String key);

    /**
     * Get the project key description from the properties file. If the user has specified a custom regex that project
     * keys must conform to and a description for that regex, this method should return the description.
     * <p/>
     * If the user has not specified a custom regex, this method will return the default project key description:
     * <p/>
     * "Usually the key is just 3 letters - i.e. if your project name is Foo Bar Raz, a key of FBR would make sense.<br>
     * The key must contain only uppercase alphabetic characters, and be at least 2 characters in length.<br> <i>It is
     * recommended to use only ASCII characters, as other characters may not work."
     *
     * @return a String description of the project key format
     */
    String getProjectKeyDescription();

    /**
     * Used to retrieve a {@link com.atlassian.jira.project.Project} object by id.  This method returns a {@link
     * com.atlassian.jira.bc.project.ProjectService.GetProjectResult}. The project will be null if no project for the id
     * specified can be found, or if the user making the request does not have the BROWSE project permission for the
     * project. In both of these cases, the errorCollection in the result object will contain an appropriate error
     * message.
     *
     * @param user The user retrieving the project.
     * @param id The id of the project.
     * @return A ProjectResult object
     */
    GetProjectResult getProjectById(User user, Long id);

    /**
     * Used to retrieve a {@link com.atlassian.jira.project.Project} object by id.  This method returns a {@link
     * com.atlassian.jira.bc.project.ProjectService.GetProjectResult}. The project will be null if no project for the id
     * specified can be found, or if the user making the request does not have the BROWSE project permission for the
     * project. In both of these cases, the errorCollection in the result object will contain an appropriate error
     * message.
     *
     * @param user The user retrieving the project.
     * @param id The id of the project.
     * @return A ProjectResult object
     */
    GetProjectResult getProjectById(ApplicationUser user, Long id);

    /**
     * Used to retrieve a {@link com.atlassian.jira.project.Project} object by id providing the user can perform the
     * passed action on the project. This method returns a {@link
     * com.atlassian.jira.bc.project.ProjectService.GetProjectResult}. The project will be null if no project for the
     * id specified can be found, or if the user making the request cannot perform the passed action on the project.
     * In both of these cases, the errorCollection in the result object will contain an appropriate error message.
     *
     * @param user The user retrieving the project.
     * @param id The id of the project.
     * @param action the action the user must be able to perform on the project.
     * @return A ProjectResult object
     */
    GetProjectResult getProjectByIdForAction(User user, Long id, ProjectAction action);

    /**
     * Used to retrieve a {@link com.atlassian.jira.project.Project} object by id providing the user can perform the
     * passed action on the project. This method returns a {@link
     * com.atlassian.jira.bc.project.ProjectService.GetProjectResult}. The project will be null if no project for the
     * id specified can be found, or if the user making the request cannot perform the passed action on the project.
     * In both of these cases, the errorCollection in the result object will contain an appropriate error message.
     *
     * @param user The user retrieving the project.
     * @param id The id of the project.
     * @param action the action the user must be able to perform on the project.
     * @return A ProjectResult object
     */
    GetProjectResult getProjectByIdForAction(ApplicationUser user, Long id, ProjectAction action);

    /**
     * Used to retrieve a {@link com.atlassian.jira.project.Project} object by key.  This method returns a {@link
     * com.atlassian.jira.bc.project.ProjectService.GetProjectResult}. The project will be null if no project for the
     * key specified can be found, or if the user making the request does not have the BROWSE project permission for the
     * project. In both of these cases, the errorCollection in the result object will contain an appropriate error
     * message.
     *
     * @param user The user retrieving the project.
     * @param key The key of the project.
     * @return A GetProjectResult object
     */
    GetProjectResult getProjectByKey(User user, String key);

    /**
     * Used to retrieve a {@link com.atlassian.jira.project.Project} object by key.  This method returns a {@link
     * com.atlassian.jira.bc.project.ProjectService.GetProjectResult}. The project will be null if no project for the
     * key specified can be found, or if the user making the request does not have the BROWSE project permission for the
     * project. In both of these cases, the errorCollection in the result object will contain an appropriate error
     * message.
     *
     * @param user The user retrieving the project.
     * @param key The key of the project.
     * @return A GetProjectResult object
     */
    GetProjectResult getProjectByKey(ApplicationUser user, String key);

    /**
     * Used to retrieve a {@link com.atlassian.jira.project.Project} object by key providing the user can perform the
     * passed action on the project. This method returns a {@link
     * com.atlassian.jira.bc.project.ProjectService.GetProjectResult}. The project will be null if no project for the
     * key specified can be found, or if the user making the request cannot perform the passed action on the project.
     * In both of these cases, the errorCollection in the result object will contain an appropriate error message.
     *
     * @param user The user retrieving the project.
     * @param key The key of the project.
     * @param action the action the user must be able to perform on the project.
     * @return A GetProjectResult object
     */
    GetProjectResult getProjectByKeyForAction(User user, String key, ProjectAction action);

    /**
     * Used to retrieve the maximum length allowed for new project names.
     *
     * @return The configured maximum project length
     */
    int getMaximumNameLength();

    /**
     * Used to retrieve a {@link com.atlassian.jira.project.Project} object by key providing the user can perform the
     * passed action on the project. This method returns a {@link
     * com.atlassian.jira.bc.project.ProjectService.GetProjectResult}. The project will be null if no project for the
     * key specified can be found, or if the user making the request cannot perform the passed action on the project.
     * In both of these cases, the errorCollection in the result object will contain an appropriate error message.
     *
     * @param user The user retrieving the project.
     * @param key The key of the project.
     * @param action the action the user must be able to perform on the project.
     * @return A GetProjectResult object
     */
    GetProjectResult getProjectByKeyForAction(ApplicationUser user, String key, ProjectAction action);

    /**
     * Used to retrieve the maximum length allowed for new project keys.
     *
     * @return The configured maximum project length
     */
    int getMaximumKeyLength();

    /**
     * Used to retrieve the total number of {@link Project}s, regardless of the permission to see those projects.
     *
     * @return A long value representing the total number of projects.
     */
    long getProjectCount();

    /**
     * Used to retrieve a list of {@link com.atlassian.jira.project.Project} objects. This method returns a
     * {@link com.atlassian.jira.bc.ServiceOutcome} containing a list of projects. The list will be empty, if the user does not have
     * the BROWSE project permission for any project or no projects are visible when using anonymous access.
     *
     * @param user The user retrieving the list of projects or NULL when using anonymous access.
     *
     * @return A ServiceOutcome containing a list of projects
     * @since v4.3
     */
    ServiceOutcome<List<Project>> getAllProjects(User user);

    /**
     * Used to retrieve a list of {@link com.atlassian.jira.project.Project} objects. This method returns a
     * {@link com.atlassian.jira.bc.ServiceOutcome} containing a list of projects. The list will be empty, if the user does not have
     * the BROWSE project permission for any project or no projects are visible when using anonymous access.
     *
     * @param user The user retrieving the list of projects or NULL when using anonymous access.
     *
     * @return A ServiceOutcome containing a list of projects
     * @since v6.0
     */
    ServiceOutcome<List<Project>> getAllProjects(ApplicationUser user);

    /**
     * Used to retrieve a list of {@link com.atlassian.jira.project.Project} objects. This method returns a
     * {@link com.atlassian.jira.bc.ServiceOutcome} containing a list of projects that the user can perform the passed
     * action on. The list will be empty if no projects match the passed action.
     *
     * @param user The user retrieving the list of projects or NULL when using anonymous access.
     * @param action the action the user must be able to perform on the returned projects.
     * @return A ServiceOutcome containing a list of projects the user can perform the passed action on.
     * @since v4.3
     */
    ServiceOutcome<List<Project>> getAllProjectsForAction(User user, ProjectAction action);

    /**
     * Used to retrieve a list of {@link com.atlassian.jira.project.Project} objects. This method returns a
     * {@link com.atlassian.jira.bc.ServiceOutcome} containing a list of projects that the user can perform the passed
     * action on. The list will be empty if no projects match the passed action.
     *
     * @param user The user retrieving the list of projects or NULL when using anonymous access.
     * @param action the action the user must be able to perform on the returned projects.
     * @return A ServiceOutcome containing a list of projects the user can perform the passed action on.
     * @since v6.0
     */
    ServiceOutcome<List<Project>> getAllProjectsForAction(ApplicationUser user, ProjectAction action);

    @PublicApi
    public static class UpdateProjectSchemesValidationResult extends ServiceResultImpl
    {
        private Long permissionSchemeId;
        private Long notificationSchemeId;
        private Long issueSecuritySchemeId;

        @Internal
        public UpdateProjectSchemesValidationResult(ErrorCollection errorCollection)
        {
            super(errorCollection);
        }

        @Internal
        public UpdateProjectSchemesValidationResult(ErrorCollection errorCollection, Long permissionSchemeId,
                Long notificationSchemeId, Long issueSecuritySchemeId)
        {
            super(errorCollection);
            this.permissionSchemeId = permissionSchemeId;
            this.notificationSchemeId = notificationSchemeId;
            this.issueSecuritySchemeId = issueSecuritySchemeId;
        }

        public Long getPermissionSchemeId()
        {
            return permissionSchemeId;
        }

        public Long getNotificationSchemeId()
        {
            return notificationSchemeId;
        }

        public Long getIssueSecuritySchemeId()
        {
            return issueSecuritySchemeId;
        }
    }

    @PublicApi
    public abstract static class AbstractProjectValidationResult extends ServiceResultImpl
    {
        private final String name;
        private final String key;
        private final String description;
        private final String leadName;
        private final String url;
        private final Long assigneeType;
        private final Long avatarId;
        private final boolean keyChanged;
        private final ApplicationUser user;

        @Internal
        public AbstractProjectValidationResult(ErrorCollection errorCollection)
        {
            super(errorCollection);
            name = null;
            key = null;
            description = null;
            leadName = null;
            url = null;
            assigneeType = null;
            avatarId = null;
            keyChanged = false;
            user = null;
        }

        @Internal
        public AbstractProjectValidationResult(ErrorCollection errorCollection, String name, String key,
                String description, String leadName, String url, Long assigneeType, final Long avatarId, final boolean keyChanged,
                final ApplicationUser user)
        {
            super(errorCollection);
            this.name = name;
            this.key = key;
            this.description = description;
            this.leadName = leadName;
            this.url = url;
            this.assigneeType = assigneeType;
            this.avatarId = avatarId;
            this.keyChanged = keyChanged;
            this.user = user;
        }

        public String getName()
        {
            return name;
        }

        public String getKey()
        {
            return key;
        }

        public String getDescription()
        {
            return description;
        }

        public String getLeadUsername()
        {
            return leadName;
        }

        /**
         * @deprecated Use {@link #getLeadUsername()} instead. Since v6.0.
         * @return the username of the requested project lead
         */
        public String getLead()
        {
            return leadName;
        }

        public String getUrl()
        {
            return url;
        }

        public Long getAssigneeType()
        {
            return assigneeType;
        }

        public Long getAvatarId()
        {
            return avatarId;
        }

        @ExperimentalApi
        public boolean isKeyChanged()
        {
            return keyChanged;
        }

        /**
         * @since v6.1
         * @return the user that initiated the action
         */
        @ExperimentalApi
        @Nullable
        public ApplicationUser getUser()
        {
            return user;
        }
    }

    @PublicApi
    public static class CreateProjectValidationResult extends AbstractProjectValidationResult
    {
        @Internal
        public CreateProjectValidationResult(ErrorCollection errorCollection)
        {
            super(errorCollection);
        }

        @Internal
        public CreateProjectValidationResult(ErrorCollection errorCollection, String name, String key,
                String description, String lead, String url, Long assigneeType, Long avatarId, ApplicationUser user)
        {
            super(errorCollection, name, key, description, lead, url, assigneeType, avatarId, false, user);
        }

        @Deprecated
        @Internal
        public CreateProjectValidationResult(ErrorCollection errorCollection, String name, String key,
                String description, String lead, String url, Long assigneeType, Long avatarId)
        {
            this(errorCollection, name, key, description, lead, url, assigneeType, avatarId, null);
        }
    }

    @PublicApi
    public static class UpdateProjectValidationResult extends AbstractProjectValidationResult
    {
        private final Project originalProject;

        @Internal
        public UpdateProjectValidationResult(ErrorCollection errorCollection)
        {
            super(errorCollection);
            this.originalProject = null;
        }

        @Deprecated
        @Internal
        public UpdateProjectValidationResult(ErrorCollection errorCollection, String name, String key,
                String description, String lead, String url, Long assigneeType, Long avatarId, Project originalProject)
        {
            this(errorCollection, name, key, description, lead, url, assigneeType, avatarId, originalProject, false, null);
        }

        @Internal
        public UpdateProjectValidationResult(ErrorCollection errorCollection, String name, String key,
                String description, String lead, String url, Long assigneeType, Long avatarId, Project originalProject,
                boolean keyChanged, ApplicationUser user)
        {
            super(errorCollection, name, key, description, lead, url, assigneeType, avatarId, keyChanged, user);
            this.originalProject = originalProject;
        }

        public Project getOriginalProject()
        {
            return originalProject;
        }
    }

    @PublicApi
    public static abstract class AbstractProjectResult extends ServiceResultImpl
    {
        private Project project;

        @Internal
        public AbstractProjectResult(ErrorCollection errorCollection)
        {
            super(errorCollection);
        }

        @Internal
        public AbstractProjectResult(ErrorCollection errorCollection, Project project)
        {
            super(errorCollection);
            this.project = project;
        }

        public Project getProject()
        {
            return project;
        }
    }

    @PublicApi
    public static class GetProjectResult extends AbstractProjectResult
    {
        @Internal
        public GetProjectResult(ErrorCollection errorCollection)
        {
            super(errorCollection);
        }

        @Internal
        public GetProjectResult(Project project)
        {
            super(new SimpleErrorCollection(), project);
        }

        @Internal
        public GetProjectResult(ErrorCollection errorCollection, Project project)
        {
            super(errorCollection, project);
        }
    }

    @Internal
    /**
     * @deprecated This is not actually used and should be removed. Since v5.1.
     */
    public static class CreateProjectResult extends AbstractProjectResult
    {
        @Internal
        public CreateProjectResult(ErrorCollection errorCollection)
        {
            super(errorCollection);
        }

        @Internal
        public CreateProjectResult(ErrorCollection errorCollection, Project project)
        {
            super(errorCollection, project);
        }
    }

    @PublicApi
    public static class DeleteProjectValidationResult extends AbstractProjectResult
    {
        @Internal
        public DeleteProjectValidationResult(final ErrorCollection errorCollection)
        {
            super(errorCollection);
        }

        @Internal
        public DeleteProjectValidationResult(final ErrorCollection errorCollection, final Project project)
        {
            super(errorCollection, project);
        }
    }

    @PublicApi
    public static class DeleteProjectResult extends ServiceResultImpl
    {
        @Internal
        public DeleteProjectResult(final ErrorCollection errorCollection)
        {
            super(errorCollection);
        }
    }
}
