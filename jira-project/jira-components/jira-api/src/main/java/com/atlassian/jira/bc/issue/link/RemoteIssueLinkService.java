package com.atlassian.jira.bc.issue.link;

import com.atlassian.annotations.PublicApi;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.ServiceResultImpl;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.link.RemoteIssueLink;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.ErrorCollection;

import java.util.Collection;
import java.util.List;

/**
 * This is used to perform create, update and delete operations in JIRA with {@link RemoteIssueLink remote issue links}. This
 * service's methods will make sure that when dealing with {@link RemoteIssueLink remote issue links} that all of JIRA's business
 * rules are enforced. This means that permissions and data validation will be checked, proper events will be fired,
 * and notifications will be triggered.
 *
 * @since v5.0
 */
@PublicApi
public interface RemoteIssueLinkService
{
    /**
     * Retrieves the remote issue link with the given id.
     *
     * @param user who the permission checks will be run against (can be null, indicating an anonymous user).
     * @param remoteIssueLinkId the database ID of the remote issue link
     * @return a result object containing a {@link RemoteIssueLink} if all went well. If there was an error the remote issue link
     * will be null and the error collection will contain details of what went wrong. Some possible error cases include
     * a remote issue link with the given id not existing, issue linking being disabled, or the user not having
     * {@link com.atlassian.jira.security.Permissions#BROWSE} permission for the linked issue.
     * @deprecated as of v6.1. Use {@link #getRemoteIssueLink(com.atlassian.jira.user.ApplicationUser, Long)} instead.
     */
    RemoteIssueLinkResult getRemoteIssueLink(User user, Long remoteIssueLinkId);

    /**
     * Retrieves the remote issue link with the given id.
     *
     * @param user who the permission checks will be run against (can be null, indicating an anonymous user).
     * @param remoteIssueLinkId the database ID of the remote issue link
     * @return a result object containing a {@link RemoteIssueLink} if all went well. If there was an error the remote issue link
     * will be null and the error collection will contain details of what went wrong. Some possible error cases include
     * a remote issue link with the given id not existing, issue linking being disabled, or the user not having
     * {@link com.atlassian.jira.security.Permissions#BROWSE} permission for the linked issue.
     */
    RemoteIssueLinkResult getRemoteIssueLink(ApplicationUser user, Long remoteIssueLinkId);

    /**
     * Retrieves the remote issue links that link with the given JIRA issue.
     *
     * @param user user who the permission checks will be run against (can be null, indicating an anonymous user).
     * @param issue the issue to get the remote issue links for
     * @return a result object containing a List of {@link RemoteIssueLink}s if all went well. If no remote issue links are
     * stored against the issue, the list will be empty. If there was an error the remote issue links list will be null
     * and the error collection will contain details of what went wrong. Some possible error cases include the given
     * issue not existing, linking being disabled, or the user not having
     * {@link com.atlassian.jira.security.Permissions#BROWSE} permission for the linked issue.
     * @deprecated as of v6.1. Use {@link #getRemoteIssueLinksForIssue(com.atlassian.jira.user.ApplicationUser, com.atlassian.jira.issue.Issue)} instead.
     */
    RemoteIssueLinkListResult getRemoteIssueLinksForIssue(User user, Issue issue);
    
    /**
     * Retrieves the remote issue links that link with the given JIRA issue.
     *
     * @param user user who the permission checks will be run against (can be null, indicating an anonymous user).
     * @param issue the issue to get the remote issue links for
     * @return a result object containing a List of {@link RemoteIssueLink}s if all went well. If no remote issue links are
     * stored against the issue, the list will be empty. If there was an error the remote issue links list will be null
     * and the error collection will contain details of what went wrong. Some possible error cases include the given
     * issue not existing, linking being disabled, or the user not having
     * {@link com.atlassian.jira.security.Permissions#BROWSE} permission for the linked issue.
     */
    RemoteIssueLinkListResult getRemoteIssueLinksForIssue(ApplicationUser user, Issue issue);

    /**
     * Retrieves the remote issue link that links with the given JIRA issue and has the given globalId.
     *
     * @param user user who the permission checks will be run against (can be null, indicating an anonymous user).
     * @param issue the issue to get the remote issue link for
     * @param globalId the globalId to get the remote issue link for
     * @return a result object containing a {@link RemoteIssueLink} if all went well. If there was an error the remote issue link
     * will be null and the error collection will contain details of what went wrong. Some possible error cases include
     * a remote issue link with the given globalId not existing, issue linking being disabled, or the user not having
     * {@link com.atlassian.jira.security.Permissions#BROWSE} permission for the linked issue.
     * @deprecated as of v6.1. Use {@link #getRemoteIssueLinkByGlobalId(com.atlassian.jira.user.ApplicationUser, com.atlassian.jira.issue.Issue, String)} instead.
     */
    RemoteIssueLinkResult getRemoteIssueLinkByGlobalId(User user, Issue issue, String globalId);

    /**
     * Retrieves the remote issue link that links with the given JIRA issue and has the given globalId.
     *
     * @param user user who the permission checks will be run against (can be null, indicating an anonymous user).
     * @param issue the issue to get the remote issue link for
     * @param globalId the globalId to get the remote issue link for
     * @return a result object containing a {@link RemoteIssueLink} if all went well. If there was an error the remote issue link
     * will be null and the error collection will contain details of what went wrong. Some possible error cases include
     * a remote issue link with the given globalId not existing, issue linking being disabled, or the user not having
     * {@link com.atlassian.jira.security.Permissions#BROWSE} permission for the linked issue.
     */
    RemoteIssueLinkResult getRemoteIssueLinkByGlobalId(ApplicationUser user, Issue issue, String globalId);

    /**
     * Retrieves the list of remote issue links that have any of the given globalIds.
     *
     * @param user user who the permission checks will be run against (can be null, indicating an anonymous user).
     * @param globalIds the globalIds to get the remote issue links for. Should not contain null values.
     * @return a result object containing a List of {@link RemoteIssueLink}s if all went well. If no remote issue links are
     * stored with any of the given globalIds, the list will be empty. If there was an error the remote issue links list will be null
     * and the error collection will contain details of what went wrong. Some possible error cases include the given collection
     * of globalIds is empty, issue linking being disabled, the user not having permissions, or any of the globalIds is null.
     * {@link com.atlassian.jira.security.Permissions#BROWSE} permission for the linked issue.
     * @since v6.1
     */
    RemoteIssueLinkListResult findRemoteIssueLinksByGlobalId(ApplicationUser user, Collection<String> globalIds);

    /**
     * Validates parameters and checks permissions, and if all checks pass it will create a
     * {@link CreateValidationResult} that can be passed to the {@link #create(User, CreateValidationResult)} method.
     * <p>
     * If any validation fails the result will contain a null RemoteIssueLink.
     * 
     * @param user user who the permission checks will be run against (can be null, indicating an anonymous user).
     * @param remoteIssueLink the remote issue link to create
     * @return a CreateValidationResult, if all validation and permission passes it will contains a validated
     * {@link RemoteIssueLink}, otherwise the RemoteIssueLink will be null.
     * The result also contains an error collection that will contain any error messages that may have been generated
     * when performing the operation.
     * @deprecated as of v6.1. Use {@link #validateCreate(com.atlassian.jira.user.ApplicationUser, com.atlassian.jira.issue.link.RemoteIssueLink)} instead.
     */
    CreateValidationResult validateCreate(User user, RemoteIssueLink remoteIssueLink);

    /**
     * Validates parameters and checks permissions, and if all checks pass it will create a
     * {@link CreateValidationResult} that can be passed to the {@link #create(User, CreateValidationResult)} method.
     * <p>
     * If any validation fails the result will contain a null RemoteIssueLink.
     *
     * @param user user who the permission checks will be run against (can be null, indicating an anonymous user).
     * @param remoteIssueLink the remote issue link to create
     * @return a CreateValidationResult, if all validation and permission passes it will contains a validated
     * {@link RemoteIssueLink}, otherwise the RemoteIssueLink will be null.
     * The result also contains an error collection that will contain any error messages that may have been generated
     * when performing the operation.
     */
    CreateValidationResult validateCreate(ApplicationUser user, RemoteIssueLink remoteIssueLink);

    /**
     * Stores the given remote issue link in the JIRA datastore, if the current user has permission to do so.
     *
     * @param user user who the permission checks will be run against (can be null, indicating an anonymous user).
     * @param createValidationResult contains the remote issue link to store. This should have been created by the
     * {@link #validateCreate(User, RemoteIssueLink)} method. The result must have
     * {@link com.atlassian.jira.bc.ServiceResult#isValid()} return true. If false this method will throw an
     * IllegalStateException.
     * @return a result object containing the persisted {@link RemoteIssueLink} if all went well. If there was an error
     * creating the remote issue link then the remote issue link will be null and the error collection will contain details of what
     * went wrong.
     * @deprecated as of v6.1. Use {@link #create(com.atlassian.jira.user.ApplicationUser, com.atlassian.jira.bc.issue.link.RemoteIssueLinkService.CreateValidationResult)} instead.
     */
    RemoteIssueLinkResult create(User user, CreateValidationResult createValidationResult);

    /**
     * Stores the given remote issue link in the JIRA datastore, if the current user has permission to do so.
     *
     * @param user user who the permission checks will be run against (can be null, indicating an anonymous user).
     * @param createValidationResult contains the remote issue link to store. This should have been created by the
     * {@link #validateCreate(User, RemoteIssueLink)} method. The result must have
     * {@link com.atlassian.jira.bc.ServiceResult#isValid()} return true. If false this method will throw an
     * IllegalStateException.
     * @return a result object containing the persisted {@link RemoteIssueLink} if all went well. If there was an error
     * creating the remote issue link then the remote issue link will be null and the error collection will contain details of what
     * went wrong.
     */
    RemoteIssueLinkResult create(ApplicationUser user, CreateValidationResult createValidationResult);

    /**
     * Validates parameters and checks permissions, and if all checks pass it will create a
     * {@link UpdateValidationResult} that can be passed to the {@link #update(User, UpdateValidationResult)} method.
     * <p>
     * Remote link are updated using all of the values in the given remote issue link object. Null values are written as null,
     * and must adhere to the required field constraints.
     * <p>
     * If any validation fails the result will contain a null RemoteIssueLink.
     *
     * @param user user who the permission checks will be run against (can be null, indicating an anonymous user).
     * @param remoteIssueLink the remote issue link to update
     * @return an UpdateValidationResult, if all validation and permission passes it will contains a validated
     * {@link RemoteIssueLink}, otherwise the RemoteIssueLink will be null.
     * The result also contains an error collection that will contain any error messages that may have been generated
     * when performing the operation.
     * @deprecated as of v6.1. Use {@link #validateUpdate(com.atlassian.jira.user.ApplicationUser, com.atlassian.jira.issue.link.RemoteIssueLink)} instead.
     */
    UpdateValidationResult validateUpdate(User user, RemoteIssueLink remoteIssueLink);

    /**
     * Validates parameters and checks permissions, and if all checks pass it will create a
     * {@link UpdateValidationResult} that can be passed to the {@link #update(User, UpdateValidationResult)} method.
     * <p>
     * Remote link are updated using all of the values in the given remote issue link object. Null values are written as null,
     * and must adhere to the required field constraints.
     * <p>
     * If any validation fails the result will contain a null RemoteIssueLink.
     *
     * @param user user who the permission checks will be run against (can be null, indicating an anonymous user).
     * @param remoteIssueLink the remote issue link to update
     * @return an UpdateValidationResult, if all validation and permission passes it will contains a validated
     * {@link RemoteIssueLink}, otherwise the RemoteIssueLink will be null.
     * The result also contains an error collection that will contain any error messages that may have been generated
     * when performing the operation.
     */
    UpdateValidationResult validateUpdate(ApplicationUser user, RemoteIssueLink remoteIssueLink);

    /**
     * Updates the given remote issue link in the JIRA datastore, if the current user has permission to do so.
     *
     * @param user user user who the permission checks will be run against (can be null, indicating an anonymous user).
     * @param updateValidationResult contains the remote issue link to update. This should have been created by the
     * {@link #validateUpdate(User, RemoteIssueLink)} method. The result must have
     * {@link com.atlassian.jira.bc.ServiceResult#isValid()} return true. If false this method will throw an
     * IllegalStateException. The remote issue link is updated using all of the values in this object. Null values are written
     * as null, and must adhere to the required field constraints.
     * @return a result object containing the updated {@link RemoteIssueLink} if all went well. If there was an error
     * updating the remote issue link then the remote issue link will be null and the error collection will contain details of what
     * went wrong.
     * @deprecated as of v6.1. Use {@link #update(com.atlassian.jira.user.ApplicationUser, com.atlassian.jira.bc.issue.link.RemoteIssueLinkService.UpdateValidationResult)} instead.
     */
    RemoteIssueLinkResult update(User user, UpdateValidationResult updateValidationResult);

    /**
     * Updates the given remote issue link in the JIRA datastore, if the current user has permission to do so.
     *
     * @param user user user who the permission checks will be run against (can be null, indicating an anonymous user).
     * @param updateValidationResult contains the remote issue link to update. This should have been created by the
     * {@link #validateUpdate(User, RemoteIssueLink)} method. The result must have
     * {@link com.atlassian.jira.bc.ServiceResult#isValid()} return true. If false this method will throw an
     * IllegalStateException. The remote issue link is updated using all of the values in this object. Null values are written
     * as null, and must adhere to the required field constraints.
     * @return a result object containing the updated {@link RemoteIssueLink} if all went well. If there was an error
     * updating the remote issue link then the remote issue link will be null and the error collection will contain details of what
     * went wrong.
     */
    RemoteIssueLinkResult update(ApplicationUser user, UpdateValidationResult updateValidationResult);

    /**
     * Validates parameters and checks permissions, and if all checks pass it will create a
     * {@link DeleteValidationResult} that can be passed to the {@link #delete(User, DeleteValidationResult)} method.
     *
     * @param user who the permission checks will be run against (can be null, indicating an anonymous user).
     * @param remoteIssueLinkId the id of the remote issue link to delete
     * @return a DeleteValidationResult, if all validation and permission passes it will contains a validated
     * remote issue link id, otherwise the remote issue link id will be null.
     * @deprecated as of v6.1. Use {@link #validateDelete(com.atlassian.jira.user.ApplicationUser, Long)} instead.
     */
    DeleteValidationResult validateDelete(User user, Long remoteIssueLinkId);

    /**
     * Validates parameters and checks permissions, and if all checks pass it will create a
     * {@link DeleteValidationResult} that can be passed to the {@link #delete(User, DeleteValidationResult)} method.
     *
     * @param user who the permission checks will be run against (can be null, indicating an anonymous user).
     * @param remoteIssueLinkId the id of the remote issue link to delete
     * @return a DeleteValidationResult, if all validation and permission passes it will contains a validated
     * remote issue link id, otherwise the remote issue link id will be null.
     */
    DeleteValidationResult validateDelete(ApplicationUser user, Long remoteIssueLinkId);

    /**
     * Deletes the remote issue link with the given id from the JIRA datastore, if the current user has permission to do so.
     *
     * @param user user user who the permission checks will be run against (can be null, indicating an anonymous user).
     * @param deleteValidationResult contains the remote issue link id to delete. This should have been created by the
     * {@link #validateDelete(User, Long)} method. The result must have
     * {@link com.atlassian.jira.bc.ServiceResult#isValid()} return true. If false this method will throw an
     * IllegalStateException.
     * @deprecated as of v6.1. Use {@link #delete(com.atlassian.jira.user.ApplicationUser, com.atlassian.jira.bc.issue.link.RemoteIssueLinkService.DeleteValidationResult)} instead.
     */
    void delete(User user, DeleteValidationResult deleteValidationResult);

    /**
     * Deletes the remote issue link with the given id from the JIRA datastore, if the current user has permission to do so.
     *
     * @param user user user who the permission checks will be run against (can be null, indicating an anonymous user).
     * @param deleteValidationResult contains the remote issue link id to delete. This should have been created by the
     * {@link #validateDelete(User, Long)} method. The result must have
     * {@link com.atlassian.jira.bc.ServiceResult#isValid()} return true. If false this method will throw an
     * IllegalStateException.
     */
    void delete(ApplicationUser user, DeleteValidationResult deleteValidationResult);

    /**
     * Validates parameters and checks permissions, and if all checks pass it will create a
     * {@link DeleteByGlobalIdValidationResult} that can be passed to the
     * {@link #deleteByGlobalId(User, DeleteByGlobalIdValidationResult)} method.
     *
     * @param user who the permission checks will be run against (can be null, indicating an anonymous user).
     * @param issue the issue to get the remote issue link for
     * @param globalId the global id to get the remote issue link for
     * @return a DeleteValidationResult, if all validation and permission passes it will contains a validated
     * remote issue link global id, otherwise the remote issue link global id will be null.
     * @deprecated as of v6.1. Use {@link #validateDeleteByGlobalId(com.atlassian.jira.user.ApplicationUser, com.atlassian.jira.issue.Issue, String)} instead.
     */
    DeleteByGlobalIdValidationResult validateDeleteByGlobalId(User user, Issue issue, String globalId);

    /**
     * Validates parameters and checks permissions, and if all checks pass it will create a
     * {@link DeleteByGlobalIdValidationResult} that can be passed to the
     * {@link #deleteByGlobalId(User, DeleteByGlobalIdValidationResult)} method.
     *
     * @param user who the permission checks will be run against (can be null, indicating an anonymous user).
     * @param issue the issue to get the remote issue link for
     * @param globalId the global id to get the remote issue link for
     * @return a DeleteValidationResult, if all validation and permission passes it will contains a validated
     * remote issue link global id, otherwise the remote issue link global id will be null.
     */
    DeleteByGlobalIdValidationResult validateDeleteByGlobalId(ApplicationUser user, Issue issue, String globalId);

    /**
     * Deletes the remote issue link that links with the given JIRA issue and has the given global id, if the current
     * user has permission to do so.
     *
     * @param user user user who the permission checks will be run against (can be null, indicating an anonymous user).
     * @param deleteValidationResult contains the remote issue link id to delete. This should have been created by the
     * {@link #validateDeleteByGlobalId(User, Issue, String)} method. The result must have
     * {@link com.atlassian.jira.bc.ServiceResult#isValid()} return true. If false this method will throw an
     * IllegalStateException.
     * @deprecated as of v6.1. Use {@link #deleteByGlobalId(com.atlassian.jira.user.ApplicationUser, com.atlassian.jira.bc.issue.link.RemoteIssueLinkService.DeleteByGlobalIdValidationResult)} instead.
     */
    void deleteByGlobalId(User user, DeleteByGlobalIdValidationResult deleteValidationResult);

    /**
     * Deletes the remote issue link that links with the given JIRA issue and has the given global id, if the current
     * user has permission to do so.
     *
     * @param user user user who the permission checks will be run against (can be null, indicating an anonymous user).
     * @param deleteValidationResult contains the remote issue link id to delete. This should have been created by the
     * {@link #validateDeleteByGlobalId(User, Issue, String)} method. The result must have
     * {@link com.atlassian.jira.bc.ServiceResult#isValid()} return true. If false this method will throw an
     * IllegalStateException.
     */
    void deleteByGlobalId(ApplicationUser user, DeleteByGlobalIdValidationResult deleteValidationResult);

    /**
     * Holds the information about performing a remote issue link operation.
     * This object should not be constructed directly, you should invoke the
     * {@link #getRemoteIssueLink(User, Long)} or
     * {@link #create(User, CreateValidationResult)} or
     * {@link #update(User, UpdateValidationResult)}
     * method to obtain this.
     */
    @PublicApi
    public static class RemoteIssueLinkResult extends ServiceResultImpl
    {
        private final RemoteIssueLink remoteIssueLink;

        RemoteIssueLinkResult(final RemoteIssueLink remoteIssueLink, final ErrorCollection errorCollection)
        {
            super(errorCollection);
            this.remoteIssueLink = remoteIssueLink;
        }

        public RemoteIssueLink getRemoteIssueLink()
        {
            return remoteIssueLink;
        }
    }

    /**
     * Holds the information about performing a remote issue link operation, when a list result is expected.
     * This object should not be constructed directly, you should invoke the
     * {@link #getRemoteIssueLinksForIssue(User, Issue)}
     * method to obtain this.
     */
    public static class RemoteIssueLinkListResult extends ServiceResultImpl
    {
        private final List<RemoteIssueLink> remoteIssueLinks;

        RemoteIssueLinkListResult(final List<RemoteIssueLink> remoteIssueLinks, final ErrorCollection errorCollection)
        {
            super(errorCollection);
            this.remoteIssueLinks = remoteIssueLinks;
        }

        public List<RemoteIssueLink> getRemoteIssueLinks()
        {
            return remoteIssueLinks;
        }
    }

    /**
     * Holds the information about validating a create remote issue link operation.
     * This object should not be constructed directly, you should invoke the
     * {@link #validateCreate(User, RemoteIssueLink)}
     * method to obtain this.
     */
    @PublicApi
    public static class CreateValidationResult extends RemoteIssueLinkResult
    {
        CreateValidationResult(final RemoteIssueLink remoteIssueLink, final ErrorCollection errorCollection)
        {
            super(remoteIssueLink, errorCollection);
        }
    }

    /**
     * Holds the information about validating an update remote issue link operation.
     * This object should not be constructed directly, you should invoke the
     * {@link #validateUpdate(User, RemoteIssueLink)}
     * method to obtain this.
     */
    @PublicApi
    public static class UpdateValidationResult extends RemoteIssueLinkResult
    {
        UpdateValidationResult(final RemoteIssueLink remoteIssueLink, final ErrorCollection errorCollection)
        {
            super(remoteIssueLink, errorCollection);
        }
    }

    /**
     * Holds the information about validating a delete remote issue link operation.
     * This object should not be constructed directly, you should invoke the
     * {@link #validateDelete(User, Long)}
     * method to obtain this.
     */
    @PublicApi
    public static class DeleteValidationResult extends ServiceResultImpl
    {
        final Long remoteIssueLinkId;

        DeleteValidationResult(final Long remoteIssueLinkId, final ErrorCollection errorCollection)
        {
            super(errorCollection);
            this.remoteIssueLinkId = remoteIssueLinkId;
        }

        public Long getRemoteIssueLinkId()
        {
            return remoteIssueLinkId;
        }
    }

    /**
     * Holds the information about validating a delete remote issue link operation.
     * This object should not be constructed directly, you should invoke the
     * {@link #validateDeleteByGlobalId} method to obtain this.
     */
    @PublicApi
    public static class DeleteByGlobalIdValidationResult extends ServiceResultImpl
    {
        final Issue issue;
        final String globalId;

        DeleteByGlobalIdValidationResult(final Issue issue, final String globalId, final ErrorCollection errorCollection)
        {
            super(errorCollection);
            if (!errorCollection.hasAnyErrors())
            {
                this.issue = issue;
                this.globalId = globalId;
            }
            else
            {
                this.issue = null;
                this.globalId = null;
            }
        }

        public Issue getIssue()
        {
            return issue;
        }

        public String getGlobalId()
        {
            return globalId;
        }
    }
}
