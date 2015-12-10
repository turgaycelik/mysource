package com.atlassian.jira.bc.project.version.remotelink;

import java.util.List;

import com.atlassian.annotations.ExperimentalApi;
import com.atlassian.annotations.PublicApi;
import com.atlassian.jira.bc.ServiceResultImpl;
import com.atlassian.jira.entity.remotelink.RemoteEntityLink;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.SimpleErrorCollection;

/**
 * @since v6.1.1
 */
@ExperimentalApi
public interface RemoteVersionLinkService
{
    /**
     * Retrieves all of the remote version links that link with the given JIRA version.
     *
     * @param user user who the permission checks will be run against (may be {@code null}, indicating
     *          an anonymous user).
     * @param versionId the ID of the version to get the remote version links for
     * @return a result object containing a {@code List} of {@link RemoteVersionLink}s if all went well.
     *      If no remote version links are stored against the version, then the list will be empty.
     *      If there was an error, the remote version links list will be {@code null} and the error
     *      collection will contain details of what went wrong.  Some possible error cases include
     *      the given version not existing, or the user not having
     *      {@link com.atlassian.jira.security.Permissions#BROWSE} permission for the version's project.
     */
    RemoteVersionLinkListResult getRemoteVersionLinksByVersionId(ApplicationUser user, Long versionId);

    /**
     * Retrieves the remote version links that are associated with the given remote resource.
     *
     * @param user user who the permission checks will be run against (may be {@code null}, indicating
     *          an anonymous user).
     * @param globalId the globalId to get the remote version links for
     * @return a result object containing a {@code List} of {@link RemoteVersionLink}s if all went well.
     *      If no remote version links are stored against the global ID, then the list will be empty.
     *      If there was an error, the remote version links list will be {@code null} and the error
     *      collection will contain details of what went wrong.
     */
    RemoteVersionLinkListResult getRemoteVersionLinksByGlobalId(ApplicationUser user, String globalId);

    /**
     * Retrieves the count of remote version links that are associated with the given remote resource.
     * This method does not perform a permission check.
     *
     * @param globalId the globalId to get the remote version links for
     * @return The count of {@link RemoteVersionLink}s associated with the given remote resource global id.
     */
    Long getRemoteVersionLinkCountByGlobalId(String globalId);

    /**
     * Retrieves the remote version link that links with the given JIRA version and has the given globalId.
     *
     * @param user user who the permission checks will be run against (may be {@code null}, indicating
     *          an anonymous user).
     * @param versionId the ID of the version to get the remote version link for
     * @param globalId the globalId to get the remote version link for
     * @return a result object containing a {@link RemoteVersionLink} if all went well.
     *      If there was an error the remote version link will be {@code null} and the error
     *      collection will contain details of what went wrong.  Some possible error cases include
     *      a remote version link with the given {@code globalId} not existing or the user not having
     *      {@link com.atlassian.jira.security.Permissions#BROWSE} permission for the version's project.
     */
    RemoteVersionLinkResult getRemoteVersionLinkByVersionIdAndGlobalId(ApplicationUser user,
            Long versionId, String globalId);

    /**
     * Validates parameters and checks permissions, and if all checks pass it will create a
     * {@link PutValidationResult} that can be passed to the
     * {@link #put(ApplicationUser, PutValidationResult)} method.
     * <p>
     * If any validation fails the result will contain a {@code null} {@code String}.
     *
     * @param user user who the permission checks will be run against (may be {@code null}, indicating
     *          an anonymous user).
     * @param versionId the ID of the version for which to create/update this remote version link
     * @param globalId the global id to associate with the remote version link.  If this is {@code null},
     *          then the JSON value will be checked for a field with {@link RemoteEntityLink#GLOBAL_ID}
     *          as the key.  If that does not exist, then a new global ID is generated.  After a
     *          successful call to {@link #put(ApplicationUser, PutValidationResult)}, the
     *          {@link RemoteVersionLinkResult} that it returns can be used to obtain the newly
     *          created {@link RemoteVersionLink}
     * @param json the JSON representation of the remote version link
     * @return a {@link PutValidationResult}.  If all validation and permission checks pass, then it
     *          will contain the information required to create the link; otherwise, the result will
     *          contain an error collection with any error messages that may have been generated when
     *          performing the operation.
     */
    PutValidationResult validatePut(ApplicationUser user, Long versionId, String globalId, String json);

    /**
     * Creates a remote version link using the validated request returned by a prior call to
     * {@link #validatePut(ApplicationUser, Long, String, String)}
     *
     * @param user user who the permission checks will be run against (may be {@code null},
     *          indicating an anonymous user).
     * @param putValidationResult contains the remote version link to store.  This should have been
     *          created by the {@link #validatePut(ApplicationUser, Long, String, String)} method.
     *          The result must be {@link PutValidationResult#isValid() valid}.
     * @return a result object containing the persisted {@code String} if all went well.
     *          If there was an error creating the remote version link then the remote version link
     *          will be {@code null} and the error collection will contain details of what went wrong.
     * @throws IllegalStateException if the {@link PutValidationResult} is not valid
     */
    RemoteVersionLinkResult put(ApplicationUser user, PutValidationResult putValidationResult);

    /**
     * Validates parameters and checks permissions, and if all checks pass it will create a
     * {@link DeleteValidationResult} that can be passed to the
     * {@link #delete(ApplicationUser, DeleteValidationResult)} method to delete the specified
     * remote version link.
     *
     * @param user who the permission checks will be run against (can be null, indicating an anonymous user).
     * @param versionId the ID of the version associated with the remote version link to be deleted
     * @param globalId the global id associated with the remote version link to be deleted
     * @return a DeleteValidationResult, if all validation and permission passes it will contain the
     *          information relevant to the deletion; otherwise, the error collection will contain
     *          the errors generated during validation.
     */
    DeleteValidationResult validateDelete(ApplicationUser user, Long versionId, String globalId);

    /**
     * Validates parameters and checks permissions, and if all checks pass it will create a
     * {@link DeleteValidationResult} that can be passed to the
     * {@link #delete(ApplicationUser, DeleteValidationResult)} method to delete all remote version
     * links for the specified version.
     *
     * @param user who the permission checks will be run against (can be null, indicating an anonymous user).
     * @param versionId the version associated with the remote version links to be deleted
     * @return a DeleteValidationResult, if all validation and permission passes it will contain the
     *          information relevant to the deletion; otherwise, the error collection will contain
     *          the errors generated during validation.
     */
    DeleteValidationResult validateDeleteByVersionId(ApplicationUser user, Long versionId);

    /**
     * Deletes the remote version link that links with the given JIRA version and has the given global id, if the current
     * user has permission to do so.
     *
     * @param user user user who the permission checks will be run against (can be null, indicating an anonymous user).
     * @param deleteValidationResult contains the remote version link id to delete.  This should have been
     *          created by one of the {@code validateDeleteBy...} methods.  The result must be
     *          {@link DeleteValidationResult#isValid() valid}.
     * @throws IllegalStateException if the {@link DeleteValidationResult} is not valid
     */
    void delete(ApplicationUser user, DeleteValidationResult deleteValidationResult);

    /**
     * Holds the information about performing a remote version link operation.
     * This object should not be constructed directly; you should invoke
     * {@link #put(ApplicationUser, PutValidationResult)} or
     * {@link #getRemoteVersionLinkByVersionIdAndGlobalId(ApplicationUser,Long,String)}
     * to obtain this.
     */
    @PublicApi
    public static class RemoteVersionLinkResult extends ServiceResultImpl
    {
        private final RemoteVersionLink remoteVersionLink;

        RemoteVersionLinkResult(final RemoteVersionLink remoteVersionLink)
        {
            super(new SimpleErrorCollection());
            this.remoteVersionLink = remoteVersionLink;
        }

        RemoteVersionLinkResult(final ErrorCollection errorCollection)
        {
            super(errorCollection);
            this.remoteVersionLink = null;
        }

        public RemoteVersionLink getRemoteVersionLink()
        {
            return remoteVersionLink;
        }
    }

    /**
     * Holds the information about performing a remote version link operation, when a list result is expected.
     * This object should not be constructed directly; you should invoke
     * {@link #getRemoteVersionLinksByVersionId(ApplicationUser, Long)} or
     * {@link #getRemoteVersionLinksByGlobalId(ApplicationUser, String)}
     * to obtain this.
     */
    public static class RemoteVersionLinkListResult extends ServiceResultImpl
    {
        private final List<RemoteVersionLink> remoteVersionLinks;

        RemoteVersionLinkListResult(final List<RemoteVersionLink> remoteVersionLinks)
        {
            super(new SimpleErrorCollection());
            this.remoteVersionLinks = remoteVersionLinks;
        }

        RemoteVersionLinkListResult(final ErrorCollection errorCollection)
        {
            super(errorCollection);
            this.remoteVersionLinks = null;
        }

        public List<RemoteVersionLink> getRemoteVersionLinks()
        {
            return remoteVersionLinks;
        }
    }

    /**
     * Holds the information about validating a create remote version link operation.
     * This object should not be constructed directly; you should invoke the
     * {@link #validatePut(ApplicationUser, Long, String, String)}
     * method to obtain this.
     */
    @PublicApi
    public static class PutValidationResult extends ServiceResultImpl
    {
        final Version version;
        final String globalId;
        final String json;

        PutValidationResult(final Version version, final String globalId, final String json)
        {
            super(new SimpleErrorCollection());
            this.version = version;
            this.globalId = globalId;
            this.json = json;
        }

        PutValidationResult(final ErrorCollection errorCollection)
        {
            super(errorCollection);
            this.version = null;
            this.globalId = null;
            this.json = null;
        }

    }


    /**
     * Holds the information about validating a delete remote version link operation.
     * This object should not be constructed directly; you should invoke either
     * {@link #validateDeleteByVersionId(ApplicationUser, Long)} or
     * {@link #validateDelete(ApplicationUser, Long, String)} to obtain this.
     */
    @PublicApi
    public static class DeleteValidationResult extends ServiceResultImpl
    {
        final Version version;
        final Long versionId;
        final String globalId;

        DeleteValidationResult(final Version version, final String globalId)
        {
            super(new SimpleErrorCollection());
            this.version = version;
            this.versionId = version.getId();
            this.globalId = globalId;
        }

        DeleteValidationResult(final ErrorCollection errorCollection)
        {
            super(errorCollection);
            this.versionId = null;
            this.version = null;
            this.globalId = null;
        }
    }
}
