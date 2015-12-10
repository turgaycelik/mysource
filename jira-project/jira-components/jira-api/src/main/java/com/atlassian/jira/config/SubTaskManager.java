package com.atlassian.jira.config;

import com.atlassian.annotations.PublicApi;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bean.SubTaskBean;
import com.atlassian.jira.exception.CreateException;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.exception.RemoveException;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.issue.link.IssueLink;
import com.atlassian.jira.issue.util.IssueUpdateBean;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;
import java.util.List;

/**
 * Manages SubTasks - issues that are "part of" other issues.
 */
@PublicApi
public interface SubTaskManager
{
    public static final String SUB_TASK_ISSUE_TYPE_STYLE = "jira_subtask";

    public static final String SUB_TASK_LINK_TYPE_NAME = "jira_subtask_link";
    public static final String SUB_TASK_LINK_TYPE_STYLE = "jira_subtask";
    public static final String SUB_TASK_LINK_TYPE_INWARD_NAME = "jira_subtask_inward";
    public static final String SUB_TASK_LINK_TYPE_OUTWARD_NAME = "jira_subtask_outward";

    // General Sub-Task methods
    public void enableSubTasks() throws CreateException;

    public boolean isSubTasksEnabled();

    public void disableSubTasks();

    public boolean isSubTask(GenericValue issue);

    public boolean isSubTaskIssueType(GenericValue issueType);

    public Long getParentIssueId(GenericValue issue);

    /**
     * Get the parent issue from a subtask
     * @param issue subtask
     * @return the parent issue form a subtask
     *
     * @deprecated Use {@link com.atlassian.jira.issue.Issue#getParentObject()} instead. Since v5.0.
     */
    public GenericValue getParentIssue(GenericValue issue);

    /**
     * Returns the SubTaskBean for the given parent issue in the context of the given user.
     *
     * @param issue the Issue
     * @param remoteUser the user
     *
     * @return the SubTaskBean for the given parent issue in the context of the given user.
     *
     * @deprecated Use {@link #getSubTaskBean(com.atlassian.jira.issue.Issue, com.atlassian.crowd.embedded.api.User)} instead. Since v5.0.
     */
    public SubTaskBean getSubTaskBean(GenericValue issue, User remoteUser);

    /**
     * Returns the SubTaskBean for the given parent issue in the context of the given user.
     *
     * @param issue the Issue
     * @param remoteUser the user
     *
     * @return the SubTaskBean for the given parent issue in the context of the given user.
     */
    public SubTaskBean getSubTaskBean(Issue issue, User remoteUser);

    /**
     * move SubTask
     * @param issue
     * @param currentSequence
     * @param sequence
     *
     * @deprecated Use {@link #moveSubTask(com.atlassian.jira.issue.Issue, Long, Long)} instead. Since v5.0.
     */
    public void moveSubTask(GenericValue issue, Long currentSequence, Long sequence);

    public void moveSubTask(Issue issue, Long currentSequence, Long sequence);

    public void resetSequences(Issue issue);

    // Subtask Issue Types

    /**
     * create SubTask IssueType
     * 
     * @param name
     * @param sequence
     * @param description
     * @param iconurl
     * @return
     * @throws CreateException
     *
     * @deprecated Use {@link #insertSubTaskIssueType(String, Long, String, String)} instead. Since v5.0.
     */
    public GenericValue createSubTaskIssueType(String name, Long sequence, String description, String iconurl) throws CreateException;

    /**
     * Create new issue type and adds it to default scheme.
     *
     * @deprecated Use {@link #insertSubTaskIssueType(String, Long, String, Long)} since v6.3.
     */
    @Deprecated
    public IssueType insertSubTaskIssueType(String name, Long sequence, String description, String iconurl) throws CreateException;

    /**
     * Create new issue type and adds it to default scheme.
     *
     * @since v6.3
     */
    public IssueType insertSubTaskIssueType(String name, Long sequence, String description, Long avatarId) throws CreateException;

    /**
     * @deprecated Use {@link #updateSubTaskIssueType(String, String, Long, String, Long)} instead. Since v6.3.
     */
    @Deprecated
    public void updateSubTaskIssueType(String id, String name, Long sequence, String description, String iconurl) throws DataAccessException;

    /**
     * Update existing sub-task issue type.
     *
     * @since v6.3
     */
    public void updateSubTaskIssueType(String id, String name, Long sequence, String description, Long avatarId) throws DataAccessException;

    public void removeSubTaskIssueType(String name) throws RemoveException;

    /**
     * Retrieves all the sub-task issue types
     *
     * @return A Collection of all sub-task {@link IssueType}s.
     * @since 4.1
     */
    public Collection<IssueType> getSubTaskIssueTypeObjects();

    public boolean issueTypeExistsById(String id);

    public boolean issueTypeExistsByName(String name);

    public void moveSubTaskIssueTypeUp(String id) throws DataAccessException;

    public void moveSubTaskIssueTypeDown(String id) throws DataAccessException;

    /**
     * Returns the SubTask IssueType with the given ID.
     * @param id the ID
     * @return SubTask IssueType with the given ID.
     *
     * @deprecated Use {@link #getSubTaskIssueType(String)} instead. Since v5.0.
     */
    public GenericValue getSubTaskIssueTypeById(String id);

    /**
     * Returns the SubTask IssueType with the given ID.
     * @param id the ID
     * @return SubTask IssueType with the given ID.
     */
    public IssueType getSubTaskIssueType(String id);

    // Sub-Task Issue Links

    /**
     * create SubTask IssueLink
     * @param parentIssue
     * @param subTaskIssue
     * @param remoteUser
     * @throws CreateException
     *
     * @deprecated Use {@link #createSubTaskIssueLink(com.atlassian.jira.issue.Issue, com.atlassian.jira.issue.Issue, com.atlassian.crowd.embedded.api.User)} instead. Since v5.0.
     */
    public void createSubTaskIssueLink(GenericValue parentIssue, GenericValue subTaskIssue, User remoteUser) throws CreateException;

    public void createSubTaskIssueLink(Issue parentIssue, Issue subTaskIssue, User remoteUser) throws CreateException;

    public Collection<Long> getAllSubTaskIssueIds();

    /**
     * Returns a list of issue links associated with the issue
     *
     * @param issueId issue id
     * @return a list of issue links
     */
    public List<IssueLink> getSubTaskIssueLinks(Long issueId);

    /**
     * @param issue the issue
     * @deprecated Use {@link #getSubTaskObjects(com.atlassian.jira.issue.Issue)} instead.
     * @return subtasks as GenericValues
     */
    public Collection<GenericValue> getSubTasks(GenericValue issue);

    public Collection<Issue> getSubTaskObjects(Issue issue);

    /**
     * Change the parent of the given subtask to the given new parent on behalf
     * of the given user.
     *
     * @param subTask The SubTask
     * @param parentIssue The parent Issue
     * @param currentUser The user
     * @return an IssueUpdateBean representing the change action.
     * @throws RemoveException if there's a problem unlinking original parent.
     * @throws CreateException if there's a problem linking new parent.
     *
     * @deprecated Use {@link #changeParent(com.atlassian.jira.issue.Issue, com.atlassian.jira.issue.Issue, com.atlassian.crowd.embedded.api.User)} instead. Since v5.0.
     */
    public IssueUpdateBean changeParent(GenericValue subTask, GenericValue parentIssue, User currentUser)
            throws RemoveException, CreateException;

    /**
     * Change the parent of the given subtask to the given new parent on behalf
     * of the given user.
     *
     * @param subTask The SubTask
     * @param parentIssue The parent Issue
     * @param currentUser The user
     * @return an IssueUpdateBean representing the change action.
     * @throws RemoveException if there's a problem unlinking original parent.
     * @throws CreateException if there's a problem linking new parent.
     */
    public IssueUpdateBean changeParent(Issue subTask, Issue parentIssue, User currentUser)
            throws RemoveException, CreateException;
}
