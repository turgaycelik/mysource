/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.issue;

import com.atlassian.annotations.ExperimentalApi;
import com.atlassian.annotations.Internal;
import com.atlassian.annotations.PublicApi;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.JiraManager;
import com.atlassian.jira.event.type.EventDispatchOption;
import com.atlassian.jira.exception.CreateException;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.exception.RemoveException;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.lang.Pair;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A class to manage interactions with issues.
 * <p>
 * All <tt>createIssue()</tt> methods throw a <tt>CreateException</tt> on certain validation errors, for example when issue description or environment exceeds the jira character limit.
 */
@PublicApi
public interface IssueManager extends JiraManager
{
    /**
     * Retrieves an issue by its id.
     *
     * @param id Issue ID.
     * @return An issue {@link GenericValue}.
     * @throws DataAccessException if there is an error in the Data Access Layer.
     * @deprecated Use {@link #getIssueObject(Long)} instead.
     */
    @Deprecated
    GenericValue getIssue(Long id) throws DataAccessException;

    /**
     * Retrieves the issue as a {@link GenericValue} with the given key.
     *
     * @param key the issue key.
     * @return the issue as a {@link GenericValue}.
     * @throws GenericEntityException if there is a problem getting the data
     * @deprecated Use {@link #getIssueObject(String)} instead.
     */
    @Deprecated
    GenericValue getIssue(String key) throws GenericEntityException;

    /**
     * Retrieves an issue {@link GenericValue} given a workflow ID.  This is used when transitioning
     * through the various steps of a workflow.
     *
     * @param wfid workflow ID.
     * @return An issue {@link GenericValue}.
     * @throws GenericEntityException An exception in the ofbiz Entity Engine.
     * @deprecated Use {@link #getIssueObjectByWorkflow(Long)} instead. Since v5.0
     */
    @Deprecated
    GenericValue getIssueByWorkflow(Long wfid) throws GenericEntityException;

    /**
     * Retrieves an issue given a workflow ID.
     * This is used when transitioning through the various steps of a workflow.
     *
     * @param workflowId workflow ID.
     * @return The Issue
     * @throws GenericEntityException An exception in the ofbiz Entity Engine.
     */
    MutableIssue getIssueObjectByWorkflow(Long workflowId) throws GenericEntityException;

    /**
     * Returns true if issue with a given key exists (it looks for issues having current issue key set for the value,
     * and moved issue keys that used the key).
     *
     * @since v6.1
     */
    @ExperimentalApi
    boolean isExistingIssueKey(String issueKey) throws GenericEntityException;

    /**
     * Retrieves an issue by id. This method will always return a new instance of an issue.
     *
     * @param id the id
     * @return A {@link MutableIssue}
     * @throws DataAccessException if there is an error in the Data Access Layer.
     */
    public MutableIssue getIssueObject(Long id) throws DataAccessException;

    /**
     * Retrieves the {@link Issue} that has the given key, or null if no such Issue exists.
     * <p>
     * This method will always return a new instance of an issue object if the issue exists.
     * </p>
     *
     * @param key The Issue key.
     * @return a {@link MutableIssue} with the given key, or null if no such Issue exists.
     * @throws DataAccessException if there is an error in the Data Access Layer.
     */
    MutableIssue getIssueObject(String key) throws DataAccessException;

    /**
     * Retrieves the {@link Issue} that has the given key, or null if no such Issue exists.
     * <p/>
     * This method will ignore case of issue key.
     *
     * @param key The Issue key.
     * @return a {@link MutableIssue} with the given key, or null if no such Issue exists.
     * @throws DataAccessException if there is an error in the Data Access Layer.
     * @since 6.1
     */
    @ExperimentalApi
    MutableIssue getIssueByKeyIgnoreCase(String key) throws DataAccessException;

    /**
     * Retrieves the {@link Issue} that has the given key, or null if no such Issue exists.
     * <p/>
     * This method will strictly only return the issue which has current issue key equal to the one given.
     *
     * @param key The Issue key.
     * @return a {@link MutableIssue} with the given key, or null if no such Issue exists.
     * @throws DataAccessException if there is an error in the Data Access Layer.
     * @since 6.1
     */
    @ExperimentalApi
    MutableIssue getIssueByCurrentKey(String key) throws DataAccessException;

    /**
     * Get issues with the following ids.  The issues are sorted in the order that
     * the ids were given in.
     *
     * @param ids Issue IDs.
     * @return A collection of issue {@link GenericValue}s
     * @deprecated Use {@link #getIssueObjects(java.util.Collection)} instead. Since v5.0
     */
    @Deprecated
    List<GenericValue> getIssues(Collection<Long> ids);

    /**
     * Get issues with the following ids.  The issues are sorted in the order that
     * the ids were given in. Any ids that are not found will be missing from the list.
     * That is list will not contains nulls.
     *
     * @param ids Issue IDs.
     * @return A collection of issue {@link MutableIssue}s
     */
    List<Issue> getIssueObjects(Collection<Long> ids);

    /**
     * Get a list of issues that the user has voted on and can see.
     *
     * @param user The user.
     * @return A list of {@link Issue} objects the user has voted on.
     * @throws GenericEntityException An exception in the ofbiz Entity Engine.
     * @deprecated Use {@link #getVotedIssues(com.atlassian.jira.user.ApplicationUser)} instead. Since v6.0.
     */
    List<Issue> getVotedIssues(User user) throws GenericEntityException;

    /**
     * Get a list of issues that the user has voted on.
     *
     * @param user The user.
     * @return A list of {@link Issue} objects the user has voted on.
     * @throws GenericEntityException An exception in the ofbiz Entity Engine.
     * @since v4.0
     * @deprecated Use {@link #getVotedIssuesOverrideSecurity(com.atlassian.jira.user.ApplicationUser)} instead. Since v6.0.
     */
    List<Issue> getVotedIssuesOverrideSecurity(User user) throws GenericEntityException;

    /**
     * Get a list of issues that the user has voted on and can see.
     *
     * @param user The user.
     * @return A list of {@link Issue} objects the user has voted on.
     * @since v6.0
     */
    List<Issue> getVotedIssues(ApplicationUser user);

    /**
     * Get a list of issues that the user has voted on.
     *
     * @param user The user.
     * @return A list of {@link Issue} objects the user has voted on.
     * @since v6.0
     */
    List<Issue> getVotedIssuesOverrideSecurity(ApplicationUser user);

    /**
     * Return a list of watchers for a particular issue.
     *
     * @param issue the Issue
     * @return A list of {@link User}s.
     * @deprecated Use {@link #getWatchersFor(Issue)} instead. Since v6.0.
     */
    List<User> getWatchers(Issue issue);

    /**
     * Return a list of watchers for a particular issue.
     *
     * @param issue the Issue
     * @return A list of {@link User}s.
     */
    List<ApplicationUser> getWatchersFor(Issue issue);

    /**
     * Get a list of issues that the user is watching and can see.
     *
     * @param user the User.
     * @return A list of {@link Issue} objects
     * @deprecated Use {@link #getWatchedIssues(com.atlassian.jira.user.ApplicationUser)} instead. Since v6.0.
     */
    List<Issue> getWatchedIssues(User user);

    /**
     * Get a list of issues that the user is watching
     *
     * @param user the User.
     * @return A list of {@link Issue} objects
     * @since v4.0
     * @deprecated Use {@link #getWatchedIssuesOverrideSecurity(com.atlassian.jira.user.ApplicationUser)} instead. Since v6.0.
     */
    List<Issue> getWatchedIssuesOverrideSecurity(User user);

    /**
     * Get a list of issues that the user is watching and can see.
     *
     * @param user the User.
     * @return A list of {@link Issue} objects
     * @since v6.0
     */
    List<Issue> getWatchedIssues(ApplicationUser user);

    /**
     * Get a list of issues that the user is watching
     *
     * @param user the User.
     * @return A list of {@link Issue} objects
     * @since v6.0
     */
    List<Issue> getWatchedIssuesOverrideSecurity(ApplicationUser user);

    /**
     * Get a list of entities (versions, components etc) related to this issue.
     *
     * @param relationName A {@link IssueRelationConstants} string indicating some issue relation
     * @param issue        Issue to consider
     * @return A list of entity {@link GenericValue}s associated with the issue.
     * @throws GenericEntityException An exception in the ofbiz Entity Engine.
     */
    List<GenericValue> getEntitiesByIssue(String relationName, GenericValue issue) throws GenericEntityException;

    /**
     * Get a list of entities (versions, components etc) related to this issue.
     *
     * @param relationName A {@link IssueRelationConstants} string indicating some issue relation
     * @param issue        Issue to consider
     * @return A list of entity {@link GenericValue}s associated with the issue.
     * @throws GenericEntityException An exception in the ofbiz Entity Engine.
     */
    // TODO: Eventually this should be replaced by Issue.getComponents() or WorklogManager.getWorklogsForIssue() etc.
    List<GenericValue> getEntitiesByIssueObject(String relationName, Issue issue) throws GenericEntityException;

    /**
     * Get a list of issues related to an entity (version, component etc).
     *
     * @param relationName A {@link IssueRelationConstants} string indicating some relation of entity
     * @param entity       The entity related to the issues we're after
     * @return A list of issue {@link GenericValue}s associated with an entity.
     * @throws GenericEntityException An exception in the ofbiz Entity Engine.
     * @deprecated Use {@link com.atlassian.jira.project.version.VersionManager#getIssueIdsWithAffectsVersion(com.atlassian.jira.project.version.Version)},
     * {@link com.atlassian.jira.project.version.VersionManager#getIssueIdsWithFixVersion(com.atlassian.jira.project.version.Version)}, or
     * {@link com.atlassian.jira.bc.project.component.ProjectComponentManager#getIssueIdsWithComponent(com.atlassian.jira.bc.project.component.ProjectComponent)} instead. Since v6.1.
     */
    List<GenericValue> getIssuesByEntity(String relationName, GenericValue entity) throws GenericEntityException;

    /**
     * Get a list of issues related to an entity (version, component etc).
     *
     * @param relationName A {@link com.atlassian.jira.issue.IssueRelationConstants} string indicating some relation of entity
     * @param entity       The entity related to the issues we're after
     * @return A list of {@link MutableIssue}s associated with an entity.
     * @throws GenericEntityException if it failed to get the list of issues.
     * @deprecated Use {@link com.atlassian.jira.project.version.VersionManager#getIssueIdsWithAffectsVersion(com.atlassian.jira.project.version.Version)},
     * {@link com.atlassian.jira.project.version.VersionManager#getIssueIdsWithFixVersion(com.atlassian.jira.project.version.Version)}, or
     * {@link com.atlassian.jira.bc.project.component.ProjectComponentManager#getIssueIdsWithComponent(com.atlassian.jira.bc.project.component.ProjectComponent)} instead. Since v6.1.
     */
    List<Issue> getIssueObjectsByEntity(String relationName, GenericValue entity) throws GenericEntityException;

    /**
     * Returns all issue keys that are associated with {@link com.atlassian.jira.issue.Issue}.
     *
     * @return all issue keys (including the current one) associated with the issue
     * @see com.atlassian.jira.issue.changehistory.ChangeHistoryManager#getPreviousIssueKeys(Long)
     * @since v6.1
     */
    Set<String> getAllIssueKeys(final Long issueId);

    /**
     * Creates an issue.
     *
     * @param remoteUserName Issue creator
     * @param fields         A map of the issue's immediate field values. See the Issue definition in entitymodel.xml for values.
     * @return GenericValue representing the new issue.
     * @throws CreateException If JIRA is unable to create the issue.
     * @deprecated Use {@link #createIssueObject(String, Map)} instead since v5.0.
     */
    @Deprecated
    GenericValue createIssue(String remoteUserName, Map<String, Object> fields) throws CreateException;

    /**
     * Creates an issue.
     *
     * @param remoteUserName Issue creator
     * @param fields         A map of the issue's immediate field values. See the Issue definition in entitymodel.xml for values.
     * @return representing the new issue.
     * @throws CreateException If JIRA is unable to create the issue.
     */
    Issue createIssueObject(String remoteUserName, Map<String, Object> fields) throws CreateException;

    /**
     * Creates an issue.
     *
     * @param remoteUser Issue creator
     * @param fields     A map of the issue's immediate field values. See the Issue definition in entitymodel.xml for values.
     * @return GenericValue representing the new issue.
     * @throws CreateException If JIRA is unable to create the issue.
     * @deprecated Use {@link #createIssueObject(User, java.util.Map)} instead. Since v5.0.
     */
    @Deprecated
    GenericValue createIssue(User remoteUser, Map<String, Object> fields) throws CreateException;

    /**
     * Creates an issue.
     *
     * @param remoteUser Issue creator
     * @param fields     A map of the issue's immediate field values. See the Issue definition in entitymodel.xml for values.
     * @return the new issue.
     * @throws CreateException If JIRA is unable to create the issue.
     */
    Issue createIssueObject(User remoteUser, Map<String, Object> fields) throws CreateException;

    /**
     * Persists a new issue.
     *
     * @param remoteUser Issue creator
     * @param issue      The new issue.
     * @return GenericValue representing the new issue.
     * @throws CreateException If JIRA is unable to create the issue.
     * @deprecated Use {@link #createIssueObject(User, Issue)} instead. Since v5.0
     */
    @Deprecated
    GenericValue createIssue(User remoteUser, Issue issue) throws CreateException;

    /**
     * Creates an issue.
     *
     * @param remoteUser Issue creator
     * @param issue      The new issue.
     * @return the new issue.
     * @throws CreateException If JIRA is unable to create the issue.
     */
    Issue createIssueObject(User remoteUser, Issue issue) throws CreateException;

    /**
     * This method will store the provided issue to the JIRA datastore. The issue will be saved and re-indexed.
     * This method performs no permission checks.
     * <p/>
     * <p/>
     * This method should be used if you want to exert more control over what happens when JIRA updates an issue. This
     * method will allow you to specify if an event is dispatched and if so which event is dispatched, see
     * {@link com.atlassian.jira.event.type.EventDispatchOption}. This method also allows you to specify if email
     * notifications should be send to notify users of the update.
     *
     * @param user                who is performing the operation
     * @param issue               the issue to update
     * @param eventDispatchOption specifies if an event should be sent and if so which should be sent.
     * @param sendMail            if true mail notifications will be sent, otherwise mail notifications will be suppressed.
     * @return the updated issue.
     * @since v4.0
     */
    public Issue updateIssue(User user, MutableIssue issue, EventDispatchOption eventDispatchOption, boolean sendMail);

    /**
     * This method will store the provided issue to the JIRA datastore. The issue will be saved and re-indexed.
     * This method performs no permission checks.
     * <p/>
     * <p/>
     * This method should be used if you want to exert more control over what happens when JIRA updates an issue.
     *
     * @param user               who is performing the operation
     * @param issue              the issue to update
     * @param updateIssueRequest details about how to perform the update, including user context, event dispatch strategy
     *                           and history metadata
     * @see com.atlassian.jira.issue.UpdateIssueRequest
     * @since JIRA 6.3
     */
    public Issue updateIssue(ApplicationUser user, MutableIssue issue, UpdateIssueRequest updateIssueRequest);

    /**
     * This method will delete an issue from JIRA.
     * <p/>
     * This will clean up all issue associations in JIRA and will de-index the issue.
     * <p/>
     * <p/>
     * This method should be used if you want to exert more control over what happens when JIRA deletes an issue. This
     * method will allow you to specify if an event is dispatched and if so which event is dispatched, see
     * {@link com.atlassian.jira.event.type.EventDispatchOption}. This method also allows you to specify if email
     * notifications should be send to notify users of the deletion.
     *
     * @param user                who is performing the operation
     * @param issue               the issue to delete.
     * @param eventDispatchOption specifies if an event should be sent and if so which should be sent.
     * @param sendMail            if true mail notifications will be sent, otherwise mail notifications will be suppressed.
     * @throws com.atlassian.jira.exception.RemoveException throw if something goes horribly wrong when deleting the issue.
     * @since v5.0
     */
    public void deleteIssue(User user, Issue issue, EventDispatchOption eventDispatchOption, boolean sendMail)
            throws RemoveException;

    /**
     * This method will delete an issue from JIRA. This will clean up all issue associations in JIRA and will de-index
     * the issue.
     * <p/>
     * <p/>
     * This method should be used if you want to exert more control over what happens when JIRA deletes an issue. This
     * method will allow you to specify if an event is dispatched and if so which event is dispatched, see
     * {@link com.atlassian.jira.event.type.EventDispatchOption}. This method also allows you to specify if email
     * notifications should be send to notify users of the deletion.
     *
     * @param user                who is performing the operation
     * @param issue               the issue to delete.
     * @param eventDispatchOption specifies if an event should be sent and if so which should be sent.
     * @param sendMail            if true mail notifications will be sent, otherwise mail notifications will be suppressed.
     * @throws com.atlassian.jira.exception.RemoveException throw if something goes horribly wrong when deleting the issue.
     * @since v4.0
     * @deprecated Use {@link #deleteIssue(com.atlassian.crowd.embedded.api.User, Issue, com.atlassian.jira.event.type.EventDispatchOption, boolean)} instead. Since v5.0.
     */
    public void deleteIssue(User user, MutableIssue issue, EventDispatchOption eventDispatchOption, boolean sendMail)
            throws RemoveException;

    /**
     * Delete issue without firing any events, or sending notifications.
     * <p/>
     * This is preferred in some bulk operations, but normally you would call {@link #deleteIssue(com.atlassian.crowd.embedded.api.User, Issue, com.atlassian.jira.event.type.EventDispatchOption, boolean)}
     *
     * @param issue issue to delete
     * @throws RemoveException if the removal fails
     * @see #deleteIssue(com.atlassian.crowd.embedded.api.User, com.atlassian.jira.issue.Issue, com.atlassian.jira.event.type.EventDispatchOption, boolean)
     */
    public void deleteIssueNoEvent(Issue issue) throws RemoveException;

    /**
     * Delete issue without firing any events, or sending notifications.
     * <p/>
     * This is preferred in some bulk operations, but normally you would call {@link #deleteIssue(com.atlassian.crowd.embedded.api.User, MutableIssue, com.atlassian.jira.event.type.EventDispatchOption, boolean)}
     *
     * @param issue issue to delete
     * @throws RemoveException if the removal fails
     * @see #deleteIssue(com.atlassian.crowd.embedded.api.User, com.atlassian.jira.issue.MutableIssue, com.atlassian.jira.event.type.EventDispatchOption, boolean)
     * @deprecated Use {@link #deleteIssueNoEvent(com.atlassian.jira.issue.Issue)} instead. Since v5.0.
     */
    public void deleteIssueNoEvent(MutableIssue issue) throws RemoveException;

    /**
     * Get all the issues for a given project.
     *
     * @param project the Project
     * @return a List of {@link GenericValue} objects
     * @throws GenericEntityException If there are errors in the persistence layer.
     * @deprecated only used in test cases, should use a search provider instead.
     */
    @Deprecated
    List<GenericValue> getProjectIssues(GenericValue project) throws GenericEntityException;

    /**
     * Returns <code>true</code> if the issue can be edited. This is determined by looking at the workflow step the issue is in.
     *
     * @param issue the Issue.
     * @return <code>true</code> if the issue can be edited. This is determined by looking at the workflow step the issue is in.
     */
    boolean isEditable(Issue issue);

    /**
     * Returns <code>true</code> if the issue can be edited by the current user. This is determined by looking at both the
     * user's permissions and the workflow step the issue is in.
     *
     * @param issue the issue you want to edit
     * @param user  the user who will be performing the edit
     * @return <code>true</code> if the user has permission and the issue is in an editable workflow step
     */
    boolean isEditable(Issue issue, User user);

    /**
     * Returns all issue ids for a particular project. This uses constant memory (i.e. doesn't load
     * all issues into memory.
     *
     * @param projectId Project ID.
     * @return A collection of issue IDs
     * @throws GenericEntityException An exception in the ofbiz Entity Engine.
     */
    Collection<Long> getIssueIdsForProject(Long projectId) throws GenericEntityException;

    /**
     * Returns the number of issues that exist for the provided project id.
     *
     * @param projectId identifies the project which the issues are associated with
     * @return a count of how many issues exist in the project
     */
    long getIssueCountForProject(Long projectId);

    /**
     * Returns a boolean indicating whether there are unassigned issues.
     *
     * @return a boolean indicating whether there are unassigned issues
     * @since 5.1
     */
    boolean hasUnassignedIssues();

    /**
     * Returns the number of unassigned issues.
     *
     * @return the number of unassigned issues
     * @since 5.1
     */
    long getUnassignedIssueCount();

    /**
     * Returns the number of issues.
     *
     * @return the number of issues
     * @since 6.3
     */
    long getIssueCount();

    /**
     * Returns an issue that has been moved by searching on the old issue key.
     *
     * @param oldIssueKey the original key of an issue that has since been moved (moving between projects assigns a new
     *                    key to an issue)
     * @return the moved {@link Issue} object
     */
    @Internal
    Issue findMovedIssue(final String oldIssueKey);

    /**
     * Used internally when we want to record that an Issue has changed its Issue key (because it has moved project).
     *
     * @param oldIssue The issue with it's old issue key
     */
    @Internal
    void recordMovedIssueKey(Issue oldIssue);

    /**
     * Returns a set of project ID / issue type combinations that given issue keys cover.
     * @param issueKeys Set of issue keys
     * @return Project ID / issue type pairs
     */
    @Internal
    @Nonnull
    Set<Pair<Long, String>> getProjectIssueTypePairsByKeys(@Nonnull final Set<String> issueKeys);

    /**
     * Returns a set of project ID / issue type combinations that given issue IDs cover.
     * @param issueIds Set of issue IDs
     * @return Project ID / issue type pairs
     */
    @Internal
    @Nonnull
    Set<Pair<Long, String>> getProjectIssueTypePairsByIds(@Nonnull final Set<Long> issueIds);

    /**
     * Check existence of issues for the given set of keys
     * @param issueKeys Set of issue keys
     * @return Set of invalid keys or the ones that don't represent an issue
     */
    @Internal
    @Nonnull
    Set<String> getKeysOfMissingIssues(@Nonnull final Set<String> issueKeys);

    /**
     * Check existence of issues for the given set of IDs
     * @param issueIds Set of issue IDs
     * @return Set of IDs that don't represent an issue
     */
    @Internal
    @Nonnull
    Set<Long> getIdsOfMissingIssues(@Nonnull final Set<Long> issueIds);
}
