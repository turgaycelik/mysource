package com.atlassian.jira.bc.project.version;

import com.atlassian.annotations.PublicApi;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.ServiceOutcome;
import com.atlassian.jira.bc.ServiceResultImpl;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.project.version.VersionKeys;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.ErrorCollection;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Set;

/**
 * Service for {@link com.atlassian.jira.bc.project.version.VersionService}
 *
 * @since v3.13
 */
@PublicApi
public interface VersionService
{

    /**
     * Indicates that the {@link com.atlassian.jira.project.version.Version} should be removed from affected issues.
     */
    public VersionAction REMOVE = new RemoveVersionAction();

    /**
     * Validate the name and description of a version, if you have edit permission.
     * @param user the user who is performing the edit operation
     * @param version the version that they want to edit
     * @param name the new name for the version (must not be null or already in use)
     * @param description the new description for the version
     * @return an ErrorCollection that contains the success or failure of the update
     * @throws IllegalArgumentException if the name is null or duplicates an existing name
     *
     * @deprecated Use {@link #validateUpdate(com.atlassian.crowd.embedded.api.User, com.atlassian.jira.bc.project.version.VersionService.VersionBuilder)}
     * and {@link #update(com.atlassian.crowd.embedded.api.User, com.atlassian.jira.bc.project.version.VersionService.VersionBuilderValidationResult)}.
     */
    ErrorCollection validateVersionDetails(final User user, final Version version, final String name, final String description);

    /**
     * Set the name and description of a version, if you have edit permission.
     * @param user the user who is performing the edit operation
     * @param version the version that they want to edit
     * @param name the new name for the version (must not be null or already in use)
     * @param description the new description for the version
     * @return a ServiceOutcome that contains the success or failure of the update
     * @throws IllegalArgumentException if the name is null or duplicates an existing name
     *
     * @deprecated Use {@link #validateUpdate(com.atlassian.crowd.embedded.api.User, com.atlassian.jira.bc.project.version.VersionService.VersionBuilder)}
     * and {@link #update(com.atlassian.crowd.embedded.api.User, com.atlassian.jira.bc.project.version.VersionService.VersionBuilderValidationResult)}.
     */
    ServiceOutcome<Version> setVersionDetails(final User user, final Version version, final String name, final String description);

    /**
     * Modify the release date of a version without performing a release/unrelease.
     * @param user the user who is changing the release date
     * @param version the version they want to modify
     * @param releaseDate the new release date to use
     * @return a ServiceOutcome describing the success/failure of the edit, along with the new Version if successful

     * @deprecated Use {@link #validateUpdate(com.atlassian.crowd.embedded.api.User, com.atlassian.jira.bc.project.version.VersionService.VersionBuilder)}
     * and {@link #update(com.atlassian.crowd.embedded.api.User, com.atlassian.jira.bc.project.version.VersionService.VersionBuilderValidationResult)}.
     */
    ServiceOutcome<Version> setReleaseDate(final User user, final Version version, final Date releaseDate);

    /**
     * Validate the release date of a version without performing a release/unrelease.
     * @param user the user who is changing the release date
     * @param version the version they want to modify
     * @param releaseDate the new release date to use
     * @return a ServiceOutcome describing the success/failure of the edit, along with the new Version if successful

     * @deprecated Use {@link #validateUpdate(com.atlassian.crowd.embedded.api.User, com.atlassian.jira.bc.project.version.VersionService.VersionBuilder)}
     * and {@link #update(com.atlassian.crowd.embedded.api.User, com.atlassian.jira.bc.project.version.VersionService.VersionBuilderValidationResult)}.
     */
    ServiceOutcome<Version> validateReleaseDate(final User user, final Version version, final String releaseDate);

    /**
     * Modify the release date of a version without performing a release/unrelease.
     * @param user the user who is changing the release date
     * @param version the version they want to modify
     * @param releaseDate the new release date to use
     * @return a ServiceOutcome describing the success/failure of the edit, along with the new Version if successful

     * @deprecated Use {@link #validateUpdate(com.atlassian.crowd.embedded.api.User, com.atlassian.jira.bc.project.version.VersionService.VersionBuilder)}
     * and {@link #update(com.atlassian.crowd.embedded.api.User, com.atlassian.jira.bc.project.version.VersionService.VersionBuilderValidationResult)}.
     */
    ServiceOutcome<Version> setReleaseDate(final User user, final Version version, final String releaseDate);

    /**
     * Validates an attempt to delete a version from a project. When deleting a version, we need to decide what to do
     * with issues that reference the version in their Affects of Fix Version fields. The action taken is specified as a
     * flag for each field.
     *
     * @param context The context for this service call.
     * @param versionId The id of the version to be deleted.
     * @param affectsAction Used to decide whether to move all the issues to a different 'affects' version or just
     * remove them. See {@link com.atlassian.jira.project.version.VersionKeys#REMOVE_ACTION}, {@link
     * com.atlassian.jira.project.version.VersionKeys#SWAP_ACTION}
     * @param fixAction Used to decide wether to move all the issues to a different 'fix' version or just remove them.
     * See {@link com.atlassian.jira.project.version.VersionKeys#REMOVE_ACTION}, {@link
     * com.atlassian.jira.project.version.VersionKeys#SWAP_ACTION}
     * @return a {@link ValidationResult} object which contains the version to delete, and the versions to swap to for
     *         Affects and Fix versions, or null if the action to be taken is {@link VersionKeys#REMOVE_ACTION}
     */
    ValidationResult validateDelete(JiraServiceContext context, Long versionId, VersionAction affectsAction, VersionAction fixAction);

    /**
     * Deletes a version from a project. When deleting a version, we need to decide what to do with issues that
     * reference the version in their Affects of Fix Version fields. The action taken is specified as a flag for each
     * field.
     *
     * @param context The context for this service call.
     * @param result The result of validation, which contains the version to be deleted, and the swap versions for
     * Affects and Fix fields
     */
    void delete(JiraServiceContext context, ValidationResult result);

    /**
     * Validates an attempt to merge a version into another. Merging is essentially the same as Deleting with the
     * actions set to {@link VersionKeys#SWAP_ACTION}.
     *
     * @param context The context for this service call.
     * @param versionId The original version to be merged and removed.
     * @param swapVersionId The target version of the merge operation. Must be from the same project.
     * @return a {@link ValidationResult} object which contains the version to delete, and the versions to swap to for
     *         Affects and Fix versions, or null if the action to be taken is {@link VersionKeys#REMOVE_ACTION}
     */
    ValidationResult validateMerge(JiraServiceContext context, Long versionId, Long swapVersionId);

    /**
     * Merges a version into another, then removes the original version.
     *
     * @param context The context for this service call.
     * @param result The result of validation, which contains the version to be deleted, and the swap versions for
     * Affects and Fix fields
     */
    void merge(JiraServiceContext context, ValidationResult result);

    /**
     * Used to retrieve a {@link com.atlassian.jira.project.version.Version} object by version id within project
     * specified by {@link com.atlassian.jira.project.Project} object.
     * This method returns a {@link com.atlassian.jira.bc.project.version.VersionService.VersionResult}.
     * The version will be null if no version for the id specified can be found, or if the user making the request
     * does not have the ADMIN, PROJECT_ADMIN or BROWSE project permission for the project.
     * In both of these cases, the errorCollection in the result object will contain an appropriate error message.
     *
     * @param user The user trying to get a version
     * @param project The project object containing requested version
     * @param versionId The id of requested version
     * @return VersionResult object
     * @deprecated Use {@link #getVersionById(ApplicationUser, Project, Long)} instead. Since v6.1.1.
     */
    VersionResult getVersionById(final User user, final Project project, final Long versionId);


    /**
     * Used to retrieve a {@link com.atlassian.jira.project.version.Version} object by version id within project
     * specified by {@link com.atlassian.jira.project.Project} object.
     * This method returns a {@link com.atlassian.jira.bc.project.version.VersionService.VersionResult}.
     * The version will be null if no version for the id specified can be found, or if the user making the request
     * does not have the ADMIN, PROJECT_ADMIN or BROWSE project permission for the project.
     * In both of these cases, the errorCollection in the result object will contain an appropriate error message.
     *
     * @param user The user trying to get a version
     * @param project The project object containing requested version
     * @param versionId The id of requested version
     * @return VersionResult object
     * @since 6.1.1
     */
    VersionResult getVersionById(final ApplicationUser user, final Project project, final Long versionId);

    /**
     * Used to retrieve a {@link com.atlassian.jira.project.version.Version} object by version id.
     * This method returns a {@link com.atlassian.jira.bc.project.version.VersionService.VersionResult}.
     * The version will be null if no version for the id specified can be found, or if the user making the request
     * does not have the ADMIN, PROJECT_ADMIN or BROWSE project permission for the project.
     * In both of these cases, the errorCollection in the result object will contain an appropriate error message.
     *
     * @param user The user trying to get a version
     * @param versionId The id of requested version
     * @return VersionResult object
     * @since 4.2
     * @deprecated Use {@link #getVersionById(ApplicationUser, Long)} instead. Since v6.1.1.
     */
    VersionResult getVersionById(final User user, final Long versionId);

     /**
     * Used to retrieve a {@link com.atlassian.jira.project.version.Version} object by version id.
     * This method returns a {@link com.atlassian.jira.bc.project.version.VersionService.VersionResult}.
     * The version will be null if no version for the id specified can be found, or if the user making the request
     * does not have the ADMIN, PROJECT_ADMIN or BROWSE project permission for the project.
     * In both of these cases, the errorCollection in the result object will contain an appropriate error message.
     *
     * @param user The user trying to get a version
     * @param versionId The id of requested version
     * @return VersionResult object
     * @since 6.1.1
     */
    VersionResult getVersionById(final ApplicationUser user, final Long versionId);

    /**
     * Used to retrieve a {@link com.atlassian.jira.project.version.Version} object by version name within project
     * specified by {@link com.atlassian.jira.project.Project} object.
     * This method returns a {@link com.atlassian.jira.bc.project.version.VersionService.VersionResult}.
     * The version will be null if no version for the versionName specified can be found, or if the user making the request
     * does not have the ADMIN, PROJECT_ADMIN or BROWSE project permission for the project.
     * In both of these cases, the errorCollection in the result object will contain an appropriate error message.
     *
     * @param user The user trying to get a version
     * @param project The project object containing requested version
     * @param versionName The name of requested version
     * @return VerionResult object
     */
    VersionResult getVersionByProjectAndName(final User user, final Project project, final String versionName);

    /**
     * Used to retrieve a {@link com.atlassian.jira.project.version.Version} collection within project
     * specified by {@link com.atlassian.jira.project.Project} object.
     * This method returns a {@link com.atlassian.jira.bc.project.version.VersionService.VersionsResult}.
     * The versions collection will be empty if the user making the request
     * does not have the ADMIN, PROJECT_ADMIN or BROWSE project permission for the project.
     * In this case, the errorCollection in the result object will contain an appropriate error message.
     *
     * @param user The user trying to get a version
     * @param project The project object containing requested version
     * @return VerionsResult object
     */
    VersionsResult getVersionsByProject(final User user, final Project project);

    /**
     * This method needs to be called before creating a version to ensure all parameters are correct.  There are a
     * number of required parameters, such as a project object and versionName.
     * An error will be returned if the user making the request does not have the ADMIN or PROJECT_ADMIN
     * permission for the project.
     * The validation will also check if a version with the name provided already exists and throw an appropriate error.
     * <p/>
     * Optional validation will be done for the release date, if provided. An error will be returned,
     * if date format is valid.
     * <p/>
     * The method will return a {@link com.atlassian.jira.bc.project.version.VersionService.CreateVersionValidationResult}
     * which contains an ErrorCollection with any potential errors and all the version's details.
     *
     * @param user The user trying to create a version
     * @param project The project object containing requested version
     * @param versionName The name of created version
     * @param releaseDate The release date for a version (optional)
     * @param description The description for a version (optional)
     * @param scheduleAfterVersion The version after which created version should be scheduled (optional)
     * @return CreateVersionValidationResult object

     * @deprecated Use {@link #validateCreate(com.atlassian.crowd.embedded.api.User, com.atlassian.jira.bc.project.version.VersionService.VersionBuilder)}
     * and {@link #create(com.atlassian.crowd.embedded.api.User, com.atlassian.jira.bc.project.version.VersionService.VersionBuilderValidationResult)}
     */
    CreateVersionValidationResult validateCreateVersion(final User user, final Project project, final String versionName,
            final String releaseDate, final String description, final Long scheduleAfterVersion);

    /**
     * This method needs to be called before creating a version to ensure all parameters are correct.  There are a
     * number of required parameters, such as a project object and versionName.
     * An error will be returned if the user making the request does not have the ADMIN or PROJECT_ADMIN
     * permission for the project.
     * The validation will also check if a version with the name provided already exists and throw an appropriate error.
     * <p/>
     * Optional validation will be done for the release date, if provided. An error will be returned,
     * if date format is valid.
     * <p/>
     * The method will return a {@link com.atlassian.jira.bc.project.version.VersionService.CreateVersionValidationResult}
     * which contains an ErrorCollection with any potential errors and all the version's details.
     *
     * @param user The user trying to create a version
     * @param project The project object containing requested version
     * @param versionName The name of created version
     * @param releaseDate The release date for a version (optional)
     * @param description The description for a version (optional)
     * @param scheduleAfterVersion The version after which created version should be scheduled (optional)
     * @return CreateVersionValidationResult object

     * @deprecated Use {@link #validateCreate(com.atlassian.crowd.embedded.api.User, com.atlassian.jira.bc.project.version.VersionService.VersionBuilder)}
     * and {@link #create(com.atlassian.crowd.embedded.api.User, com.atlassian.jira.bc.project.version.VersionService.VersionBuilderValidationResult)}
     */
    CreateVersionValidationResult validateCreateVersion(final User user, final Project project, final String versionName,
            final Date releaseDate, final String description, final Long scheduleAfterVersion);

     /**
     * Using the validation result from {@link #validateCreateVersion(User, com.atlassian.jira.project.Project,
     * String, String, String, Long)} a new version will be created.  This method will throw an RuntimeException if
     * the version could not be created.
     *
     * @param user The user trying to get a version
     * @param request The {@link com.atlassian.jira.bc.project.version.VersionService.CreateVersionValidationResult} object
     *  containg all required data
     * @return created Version object

      * @deprecated Use {@link #validateCreate(com.atlassian.crowd.embedded.api.User, com.atlassian.jira.bc.project.version.VersionService.VersionBuilder)}
      * and {@link #create(com.atlassian.crowd.embedded.api.User, com.atlassian.jira.bc.project.version.VersionService.VersionBuilderValidationResult)}
     */
    Version createVersion(User user, CreateVersionValidationResult request);

    /**
     * This method needs to be called before releasing a version to ensure all parameters are correct.  There is
     * required parameter, version object.
     * An error will be returned if the user making the request does not have the ADMIN or PROJECT_ADMIN
     * permission for the project.
     * The validation will also check if a version provided has a valid name and if is not released already.
     * <p/>
     * The method will return a {@link com.atlassian.jira.bc.project.version.VersionService.ReleaseVersionValidationResult}
     * which contains an ErrorCollection with any potential errors and all the version's details.
     *
     * @param user The user trying to release a version
     * @param version The version to release
     * @param releaseDate The version release date (optional)
     * @return ReleaseVersionValidationResult object
     */
    ReleaseVersionValidationResult validateReleaseVersion(final User user, final Version version, final Date releaseDate);

    /**
     * This method needs to be called before releasing a version to ensure all parameters are correct.  There is
     * required parameter, version object.
     * An error will be returned if the user making the request does not have the ADMIN or PROJECT_ADMIN
     * permission for the project.
     * The validation will also check if a version provided has a valid name and if is not released already.
     * <p/>
     * The method will return a {@link com.atlassian.jira.bc.project.version.VersionService.ReleaseVersionValidationResult}
     * which contains an ErrorCollection with any potential errors and all the version's details.
     *
     * @param user The user trying to release a version
     * @param version The version to release
     * @param releaseDate The version release date (optional)
     * @return ReleaseVersionValidationResult object
     */
    ReleaseVersionValidationResult validateReleaseVersion(final User user, final Version version, final String releaseDate);

    /**
     * This method needs to be called before unreleasing a version to ensure all parameters are correct.  There is
     * required parameter, version object.
     * An error will be returned if the user making the request does not have the ADMIN or PROJECT_ADMIN
     * permission for the project.
     * The validation will also check if a version provided has a valid name and if is released already.
     * <p/>
     * The method will return a {@link com.atlassian.jira.bc.project.version.VersionService.ReleaseVersionValidationResult}
     * which contains an ErrorCollection with any potential errors and all the version's details.
     *
     * @param user The user trying to unrelease a version
     * @param version The version to release
     * @param releaseDate The version release date (optional)
     * @return ReleaseVersionValidationResult object
     */
    ReleaseVersionValidationResult validateUnreleaseVersion(final User user, final Version version, final Date releaseDate);

    /**
     * This method needs to be called before unreleasing a version to ensure all parameters are correct.  There is
     * required parameter, version object.
     * An error will be returned if the user making the request does not have the ADMIN or PROJECT_ADMIN
     * permission for the project.
     * The validation will also check if a version provided has a valid name and if is released already.
     * <p/>
     * The method will return a {@link com.atlassian.jira.bc.project.version.VersionService.ReleaseVersionValidationResult}
     * which contains an ErrorCollection with any potential errors and all the version's details.
     *
     * @param user The user trying to unrelease a version
     * @param version The version to release
     * @param releaseDate The version release date (optional)
     * @return ReleaseVersionValidationResult object
     */
    ReleaseVersionValidationResult validateUnreleaseVersion(final User user, final Version version, final String releaseDate);

    /**
     * Using the validation result from {@link #validateReleaseVersion(User,
     * com.atlassian.jira.project.version.Version, Date)} a version will be released.
     * This method will throw an IllegalArgumentException if the provied data are invalid and version could not be released.
     *
     * @param result a ReleaseVersionValidationResult containg required data
     * @return a released version object
     */
    Version releaseVersion(final ReleaseVersionValidationResult result);

    /**
     * Using the validation result from {@link #validateReleaseVersion(User,
     * com.atlassian.jira.project.version.Version, Date)} a version will be released.
     * This method will throw an IllegalArgumentException if the provied data are invalid and version could not be released.
     *
     * @param user The user trying to release a version
     * @param currentVersion The current version being released.
     * @param newVersion The version to move issues to.
     */
    void moveUnreleasedToNewVersion(final User user, final Version currentVersion, final Version newVersion);

    /**
     * Using the validation result from {@link #validateUnreleaseVersion(User,
     * com.atlassian.jira.project.version.Version, Date)} a version will be unreleased.
     * This method will throw an IllegalArgumentException if the provied data are invalid and version could not be unreleased.
     *
     * @param result a ReleaseVersionValidationResult containg required data
     * @return a unreleased version object
     */
    Version unreleaseVersion(final ReleaseVersionValidationResult result);

    /**
     * This method should be called before archiving a version. It performs some basic validation of the version that
     * was passed in. This includes a null check, checking that the version name isn't empty, and checking that the
     * version is linked against a valid project.
     * <p/>
     * The method also validates that the user passed in is either a global admin, or has project admin rights for the
     * project that the version is linked to.
     * <p/>
     * Finally, this method checks that the version that was passed in hasn't already been archived. If there's any
     * errors, the validationResult will contain appropriate errors and wont be valid.
     *
     * @param user The user performing this operation
     * @param version The version to be archived
     * @return a validation result, containing any errors or the version details on success
     */
    ArchiveVersionValidationResult validateArchiveVersion(final User user, final Version version);

    /**
     * This method should be called before unarchiving a version. It performs some basic validation of the version that
     * was passed in. This includes a null check, checking that the version name isn't empty, and checking that the
     * version is linked against a valid project.
     * <p/>
     * The method also validates that the user passed in is either a global admin, or has project admin rights for the
     * project that the version is linked to.
     * <p/>
     * Finally, this method checks that the version that was passed is currently archived. If there's any errors, the
     * validationResult will contain appropriate errors and wont be valid.
     *
     * @param user The user performing this operation
     * @param version The version to be archived
     * @return a validation result, containing any errors or the version details on success
     */
    ArchiveVersionValidationResult validateUnarchiveVersion(final User user, final Version version);

    /**
     * Takes a validation result and performs the archive operation.
     *
     * @param result The result from the validation
     * @return The version that was archived.  Ideally this version should have been retrieved from the store for
     *         consistency
     * @throws IllegalStateException if the result passed in is not valid.
     */
    Version archiveVersion(final ArchiveVersionValidationResult result);

    /**
     * Takes a validation result and performs the unarchive operation.
     *
     * @param result The result from the validation
     * @return The version that was unarchived.  Ideally this version should have been retrieved from the store for
     *         consistency
     * @throws IllegalStateException if the result passed in is not valid.
     */
    Version unarchiveVersion(final ArchiveVersionValidationResult result);

    /**
     * Is the passed version overdue? This method does no permission checks on the passed version.
     *
     * @param version the version to check.
     *
     * @return true if the passed version is overdue.
     */
    boolean isOverdue(Version version);

    /**
     * Validate Move a version to the start of the version list.
     *
     * @param user  The user trying to move a version
     * @param versionId
     * @return a validation result, containing any errors or the version details on success
     */
    MoveVersionValidationResult validateMoveToStartVersionSequence(final User user, long versionId);

    /**
     * Validate Move a version to have a lower sequence number - ie make it earlier.
     *
     * @param user  The user trying to move a version
     * @param versionId
     * @return a validation result, containing any errors or the version details on success
     */
    MoveVersionValidationResult validateIncreaseVersionSequence(final User user, long versionId);

    /**
     * Validate Move a version to have a higher sequence number - ie make it later.
     *
     * @param user  The user trying to move a version
     * @param versionId
     * @return a validation result, containing any errors or the version details on success
     */
    MoveVersionValidationResult validateDecreaseVersionSequence(final User user, long versionId);

    /**
     * Validate Move a version to the end of the version sequence.
     *
     * @param user  The user trying to move a version
     * @param versionId
     * @return a validation result, containing any errors or the version details on success
     */
    MoveVersionValidationResult validateMoveToEndVersionSequence(final User user, long versionId);

    /**
     * Validate Move a version after another version.
     * @param user  The user trying to move a version
     * @param versionId version to reschedule
     * @param scheduleAfterVersion id of the version to schedule after the given version object
     * @return a validation result, containing any errors or the version details and schedule after target on success
     */
    MoveVersionValidationResult validateMoveVersionAfter(final User user, long versionId, Long scheduleAfterVersion);

    /**
     * Move a version to the start of the version list.
     *
     * @param moveVersionValidationResult Move Version Validation Result
     */
    void moveToStartVersionSequence(MoveVersionValidationResult moveVersionValidationResult);

    /**
     * Move a version to have a lower sequence number - ie make it earlier.
     *
     * @param moveVersionValidationResult Move Version Validation Result
     */
    void increaseVersionSequence(MoveVersionValidationResult moveVersionValidationResult);

    /**
     * Move a version to have a higher sequence number - ie make it later.
     *
     * @param moveVersionValidationResult Move Version Validation Result
     */
    void decreaseVersionSequence(MoveVersionValidationResult moveVersionValidationResult);

    /**
     * Move a version to the end of the version sequence.
     *
     * @param moveVersionValidationResult Move Version Validation Result
     */
    void moveToEndVersionSequence(MoveVersionValidationResult moveVersionValidationResult);

    /**
     * Move a version after another version.
     * @param moveVersionValidationResult Move Version Validation Result
     */
    void moveVersionAfter(MoveVersionValidationResult moveVersionValidationResult);

    /**
     * Return the count of Issues Fixed in this version.
     *
     * @param version
     * @return A count of issues
     */
    public long getFixIssuesCount(Version version);

    /**
     * Return the count of Issues that affect this version.
     *
     * @param version
     * @return A count of issues
     */
    public long getAffectsIssuesCount(Version version);

    /**
     * Return the count of Issues that are unresolved in this version. Used when
     * releasing a version to get user confirmation about what to do with
     * the unresolved issues.
     *
     * @param user the user trying to release the version
     * @param version which version to check for unresolved issues
     * @return A count of issues
     */
    public long getUnresolvedIssuesCount(User user, Version version);

    /**
     * Represents the results of performing a validation call for a single merge or delete operation.
     */
    @PublicApi
    interface ValidationResult
    {
        boolean isValid();

        Version getVersionToDelete();

        Version getFixSwapVersion();

        Version getAffectsSwapVersion();

        Set<Reason> getReasons();

        ErrorCollection getErrorCollection();

        public static enum Reason
        {
            /**
             * Not allowed to create a version.
             */
            FORBIDDEN,
            /**
             * Version not found
             */
            NOT_FOUND,
            /**
             * The version specified to swap to is invalid
             */
            SWAP_TO_VERSION_INVALID
        }

    }

    @PublicApi
    public static class CreateVersionValidationResult extends ServiceResultImpl
    {
        private final Project project;
        private final String versionName;
        private final Date startDate;
        private final Date releaseDate;
        private final String description;
        private final Long scheduleAfterVersion;
        private final Set<Reason> reasons;

        public static enum Reason
        {
            /**
             * Not allowed to create a version.
             */
            FORBIDDEN,
            /**
             * Project was not specified.
             */
            BAD_PROJECT,
            /**
             * Version name is not valid.
             */
            BAD_NAME,
            /**
             * Version name already exists for that project.
             */
            DUPLICATE_NAME,
            /**
             * The start date specified was invalid.
             *
             * @since v6.0
             */
            BAD_START_DATE,
            /**
             * The release date specified was invalid.
             */
            BAD_RELEASE_DATE,
            /**
             * The start date is after the release date.
             *
             * @since v6.0
             */
            BAD_START_RELEASE_DATE_ORDER,
            /**
             * The value was beyond specified length
             */
            VERSION_NAME_TOO_LONG
        }

        public CreateVersionValidationResult(ErrorCollection errorCollection, Set<Reason> reasons)
        {
            super(errorCollection);
            this.reasons = Collections.unmodifiableSet(reasons);
            this.project = null;
            this.versionName = null;
            this.startDate = null;
            this.releaseDate = null;
            this.description = null;
            this.scheduleAfterVersion = null;
        }

        public CreateVersionValidationResult(ErrorCollection errorCollection, Project project, String versionName,
                Date releaseDate, String description, Long scheduleAfterVersion)
        {
            super(errorCollection);
            this.reasons = Collections.emptySet();
            this.project = project;
            this.versionName = versionName;
            this.startDate = null;
            this.releaseDate = releaseDate;
            this.description = description;
            this.scheduleAfterVersion = scheduleAfterVersion;
        }

        public CreateVersionValidationResult(ErrorCollection errorCollection, Project project, String versionName,
                Date startDate, Date releaseDate, String description, Long scheduleAfterVersion)
        {
            super(errorCollection);
            this.reasons = Collections.emptySet();
            this.project = project;
            this.versionName = versionName;
            this.startDate = startDate;
            this.releaseDate = releaseDate;
            this.description = description;
            this.scheduleAfterVersion = scheduleAfterVersion;
        }

        public Project getProject()
        {
            return project;
        }

        public String getVersionName()
        {
            return versionName;
        }

        public Date getStartDate()
        {
            return startDate;
        }

        public Date getReleaseDate()
        {
            return releaseDate;
        }

        public String getDescription()
        {
            return description;
        }

        public Long getScheduleAfterVersion()
        {
            return scheduleAfterVersion;
        }

        public Set<Reason> getReasons()
        {
            return reasons;
        }
    }

    @PublicApi
    public abstract static class AbstractVersionResult extends ServiceResultImpl
    {
        private final Version version;

        public AbstractVersionResult(ErrorCollection errorCollection)
        {
            this(errorCollection, null);
        }

        public AbstractVersionResult(ErrorCollection errorCollection, Version version)
        {
            super(errorCollection);
            this.version = version;
        }

        public Version getVersion()
        {
            return version;
        }
    }

    @PublicApi
    public static class VersionResult extends AbstractVersionResult
    {
        public VersionResult(ErrorCollection errorCollection)
        {
            super(errorCollection);
        }

        public VersionResult(ErrorCollection errorCollection, Version version)
        {
            super(errorCollection, version);
        }
    }

    @PublicApi
    public static class VersionsResult extends ServiceResultImpl
    {
        private final Collection<Version> versions;

        public VersionsResult(ErrorCollection errorCollection)
        {
            this(errorCollection, Collections.<Version>emptyList());
        }

        public VersionsResult(ErrorCollection errorCollection, Collection<Version> versions)
        {
            super(errorCollection);
            this.versions = versions;
        }

        public Collection<Version> getVersions()
        {
            return versions;
        }
    }

    @PublicApi
    public static class ReleaseVersionValidationResult extends AbstractVersionResult
    {
        private final Date releaseDate;

        public ReleaseVersionValidationResult(ErrorCollection errorCollection)
        {
            this(errorCollection, null, null);
        }

        public ReleaseVersionValidationResult(ErrorCollection errorCollection, Version version, Date releaseDate)
        {
            super(errorCollection, version);
            this.releaseDate = releaseDate;
        }

        public Date getReleaseDate()
        {
            return releaseDate;
        }
    }

    @PublicApi
    public static class ArchiveVersionValidationResult extends AbstractVersionResult
    {
        public ArchiveVersionValidationResult(ErrorCollection errorCollection)
        {
            super(errorCollection);
        }

        public ArchiveVersionValidationResult(ErrorCollection errorCollection, Version version)
        {
            super(errorCollection, version);
        }
    }

    @PublicApi
    public static class MoveVersionValidationResult extends AbstractVersionResult
    {
        private Long scheduleAfterVersion;
        private final Set<Reason> reasons;

        public static enum Reason
        {
            /**
             * Not allowed to create a version.
             */
            FORBIDDEN,
            /**
             * Version not found
             */
            NOT_FOUND,
            /**
             * schedule after version not found
             */
            SCHEDULE_AFTER_VERSION_NOT_FOUND
        }


        public MoveVersionValidationResult(ErrorCollection errorCollection,  Set<Reason> reasons)
        {
            super(errorCollection);
            this.reasons = Collections.unmodifiableSet(reasons);
        }

        public MoveVersionValidationResult(ErrorCollection errorCollection, Version version)
        {
            super(errorCollection, version);
            this.reasons = Collections.emptySet();
        }

        public MoveVersionValidationResult(ErrorCollection errorCollection, Version version, Long scheduleAfterVersion)
        {
            super(errorCollection, version);
            this.reasons = Collections.emptySet();
            this.scheduleAfterVersion = scheduleAfterVersion;
        }

        public Set<Reason> getReasons()
        {
            return reasons;
        }

        public Long getScheduleAfterVersion()
        {
            return scheduleAfterVersion;
        }
    }

    @PublicApi
    interface VersionAction
    {
        boolean isSwap();

        Long getSwapVersionId();
    }

    /**
     * New style API for create/update of version
     */

    /**
     * Creates a builder to be used when creating a new {@link Version}. The builder encapsulates all the fields
     * which need to be specified on create.
     *
     * @see #newBuilder(Version) for updating an existing version
     * @return the builder instance
     * @since v6.0
     */
    VersionBuilder newBuilder();

    /**
     * Creates a builder to be used when updating an existing {@link Version}. The builder encapsulates all the fields
     * which need to be specified on update.
     *
     * @see #newBuilder() for creating a new version
     * @param version the Version object to update
     * @return the builder instance
     * @since v6.0
     */
    VersionBuilder newBuilder(Version version);

    /**
     * Validates the creation of a new {@link Version} object, specified with a {@link VersionBuilder}.
     * <p/>
     * This replaces the deprecated methods: {@link #validateCreateVersion(User, Project, String, Date, String, Long)}
     * and {@link #validateCreateVersion(User, Project, String, String, String, Long)}.
     *
     * @see #newBuilder()
     * @param user the user who is performing the create
     * @param versionBuilder the builder which specified the new Version to be created
     * @return the result
     * @since v6.0
     */
    VersionBuilderValidationResult validateCreate(User user, VersionBuilder versionBuilder);

    /**
     * Creates a new {@link Version}, based on the validation result from calling {@link #validateCreate(User, VersionBuilder)}.
     *
     * @see #validateCreate(com.atlassian.crowd.embedded.api.User, com.atlassian.jira.bc.project.version.VersionService.VersionBuilder)
     * @param user the user
     * @param validationResult the result of validation
     * @return the new Version object; errors if not successful.
     * @since v6.0
     */
    ServiceOutcome<Version> create(User user, VersionBuilderValidationResult validationResult);

    /**
     * Validates the update of an existing {@link Version} object, specified with a {@link VersionBuilder}.
     * <p/>
     * This replaces the deprecated methods: {@link #validateReleaseDate(User, Version, String)}
     * and {@link #validateVersionDetails(User, Version, String, String)}.
     *
     * @see #newBuilder(com.atlassian.jira.project.version.Version)
     * @param user the user who is performing the update
     * @param versionBuilder the builder which specified the update to the existing Version
     * @return the result
     * @since v6.0
     */
    VersionBuilderValidationResult validateUpdate(User user, VersionBuilder versionBuilder);

    /**
     * Updates the existing {@link Version}, based on the validation result from calling {@link #validateUpdate(User, VersionBuilder)}.
     *
     * @see #validateUpdate(com.atlassian.crowd.embedded.api.User, com.atlassian.jira.bc.project.version.VersionService.VersionBuilder)
     * @param user the user
     * @param validationResult the result of validation
     * @return the updated Version object; errors if not successful.
     * @since v6.0
     */
    ServiceOutcome<Version> update(User user, VersionBuilderValidationResult validationResult);

    /**
     * A builder class to specify a new Version to create or an existing Version to update.
     *
     * @since v6.0
     */
    public static class VersionBuilder
    {
        final Version version;

        Long projectId;

        String name;
        String description;

        Date startDate;
        Date releaseDate;

        Long scheduleAfterVersion;

        VersionBuilder()
        {
            this.version = null;
        }

        VersionBuilder(Version version)
        {
            this.version = version;

            this.projectId = version.getProjectId();

            this.name = version.getName();
            this.description = version.getDescription();

            this.startDate = version.getStartDate();
            this.releaseDate = version.getReleaseDate();
        }

        public VersionBuilder projectId(Long projectId)
        {
            this.projectId = projectId;
            return this;
        }

        public VersionBuilder name(String name)
        {
            this.name = name;
            return this;
        }

        public VersionBuilder description(String description)
        {
            this.description = description;
            return this;
        }

        public VersionBuilder startDate(Date startDate)
        {
            this.startDate = startDate;
            return this;
        }

        public VersionBuilder releaseDate(Date releaseDate)
        {
            this.releaseDate = releaseDate;
            return this;
        }

        public VersionBuilder scheduleAfterVersion(Long scheduleAfterVersion)
        {
            this.scheduleAfterVersion = scheduleAfterVersion;
            return this;
        }

        /**
         * @deprecated use {@link #build()} instead.
         */
        Version save()
        {
            return build();
        }

        Version build()
        {
            final Version newVersion = version.clone();
            newVersion.setName(this.name);
            newVersion.setDescription(this.description);

            newVersion.setStartDate(this.startDate);
            newVersion.setReleaseDate(this.releaseDate);

            return newVersion;
        }
    }

    /**
     * A generified {@link ServiceResultImpl} that allows Service-specified Reasons to be set as part of the result (in
     * addition to Reasons specified inside the {@link ErrorCollection}.
     *
     * @param <R> the type of Reasons
     * @param <T> the type of the Result object if successful
     * @since v6.0
     */
    public static class ReasonsServiceResult<R, T> extends ServiceResultImpl
    {
        private final Set<R> specificReasons;

        private final T result;

        public ReasonsServiceResult(ErrorCollection errorCollection, Set<R> specificReasons)
        {
            super(errorCollection);

            this.specificReasons = Collections.unmodifiableSet(specificReasons);
            this.result = null;
        }

        public ReasonsServiceResult(ErrorCollection errorCollection, Set<R> specificReasons, T result)
        {
            super(errorCollection);

            this.specificReasons = Collections.unmodifiableSet(specificReasons);
            this.result = result;
        }

        public Set<R> getSpecificReasons()
        {
            return specificReasons;
        }

        public T getResult()
        {
            return result;
        }
    }

    /**
     * Result object that relates to new VersionService methods that take a {@link VersionBuilder} as a parameter.
     *
     * @since v6.0
     */
    public static class VersionBuilderValidationResult extends ReasonsServiceResult<VersionService.CreateVersionValidationResult.Reason, VersionBuilder>
    {
        VersionBuilderValidationResult(ErrorCollection errorCollection, Set<VersionService.CreateVersionValidationResult.Reason> reasons)
        {
            super(errorCollection, reasons);
        }

        VersionBuilderValidationResult(ErrorCollection errorCollection, Set<VersionService.CreateVersionValidationResult.Reason> reasons, VersionBuilder versionBuilder)
        {
            super(errorCollection, reasons, versionBuilder);
        }
    }
}
