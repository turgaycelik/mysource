package com.atlassian.jira.config;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bean.SubTaskBean;
import com.atlassian.jira.exception.CreateException;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.exception.RemoveException;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.issue.util.IssueUpdateBean;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;
import java.util.List;
import java.util.Collections;

/**
 * @since v4.0
 */
public class MockSubTaskManager implements SubTaskManager
{
    private boolean subTasksEnabled = true;

    @Override
    public void enableSubTasks() throws CreateException
    {
        subTasksEnabled = true;
    }

    @Override
    public boolean isSubTasksEnabled()
    {
        return subTasksEnabled;
    }

    @Override
    public void disableSubTasks()
    {
        subTasksEnabled = false;
    }

    @Override
    public boolean isSubTask(final GenericValue issue)
    {
        throw new UnsupportedOperationException("Method not implemented yet");
    }

    @Override
    public boolean isSubTaskIssueType(final GenericValue issueType)
    {
        throw new UnsupportedOperationException("Method not implemented yet");
    }

    @Override
    public Long getParentIssueId(final GenericValue issue)
    {
        // simple implementation for now - make this clever if we need it.
        return null;
    }

    @Override
    public GenericValue getParentIssue(final GenericValue issue)
    {
        throw new UnsupportedOperationException("Method not implemented yet");
    }

    @Override
    public SubTaskBean getSubTaskBean(final GenericValue issue, final User remoteUser)
    {
        throw new UnsupportedOperationException("Method not implemented yet");
    }

    @Override
    public SubTaskBean getSubTaskBean(final Issue issue, final User remoteUser)
    {
        throw new UnsupportedOperationException("Method not implemented yet");
    }

    @Override
    public void moveSubTask(final GenericValue issue, final Long currentSequence, final Long sequence)
    {
        throw new UnsupportedOperationException("Method not implemented yet");
    }

    @Override
    public void moveSubTask(final Issue issue, final Long currentSequence, final Long sequence)
    {
        throw new UnsupportedOperationException("Method not implemented yet");
    }

    @Override
    public void resetSequences(final Issue issue)
    {
        throw new UnsupportedOperationException("Method not implemented yet");
    }

    @Override
    public GenericValue createSubTaskIssueType(final String name, final Long sequence, final String description, final String iconurl)
            throws CreateException
    {
        throw new UnsupportedOperationException("Method not implemented yet");
    }

    @Override
    public IssueType insertSubTaskIssueType(String name, Long sequence, String description, String iconurl)
            throws CreateException
    {
        throw new UnsupportedOperationException("Method not implemented yet");
    }

    @Override
    public IssueType insertSubTaskIssueType(final String name, final Long sequence, final String description, final Long avatarId)
            throws CreateException
    {
        throw new UnsupportedOperationException("Method not implemented yet");
    }

    @Override
    public void updateSubTaskIssueType(final String id, final String name, final Long sequence, final String description, final String iconurl)
    {
        throw new UnsupportedOperationException("Method not implemented yet");
    }

    @Override
    public void updateSubTaskIssueType(final String id, final String name, final Long sequence, final String description, final Long avatarId)
            throws DataAccessException
    {
        throw new UnsupportedOperationException("Method not implemented yet");
    }

    @Override
    public void removeSubTaskIssueType(final String name) throws RemoveException
    {
        throw new UnsupportedOperationException("Method not implemented yet");
    }

    @Override
    public Collection<IssueType> getSubTaskIssueTypeObjects()
    {
        throw new UnsupportedOperationException("Method not implemented yet");
    }

    @Override
    public boolean issueTypeExistsById(final String id)
    {
        throw new UnsupportedOperationException("Method not implemented yet");
    }

    @Override
    public boolean issueTypeExistsByName(final String name)
    {
        throw new UnsupportedOperationException("Method not implemented yet");
    }

    @Override
    public void moveSubTaskIssueTypeUp(final String id)
    {
        throw new UnsupportedOperationException("Method not implemented yet");
    }

    @Override
    public void moveSubTaskIssueTypeDown(final String id)
    {
        throw new UnsupportedOperationException("Method not implemented yet");
    }

    @Override
    public GenericValue getSubTaskIssueTypeById(final String id)
    {
        throw new UnsupportedOperationException("Method not implemented yet");
    }

    @Override
    public IssueType getSubTaskIssueType(String id)
    {
        throw new UnsupportedOperationException("Method not implemented yet");
    }

    @Override
    public void createSubTaskIssueLink(final GenericValue parentIssue, final GenericValue subTaskIssue, final User remoteUser)
            throws CreateException
    {
        throw new UnsupportedOperationException("Method not implemented yet");
    }

    @Override
    public void createSubTaskIssueLink(final Issue parentIssue, final Issue subTaskIssue, final User remoteUser)
            throws CreateException
    {
        throw new UnsupportedOperationException("Method not implemented yet");
    }

    @Override
    public Collection getAllSubTaskIssueIds()
    {
        throw new UnsupportedOperationException("Method not implemented yet");
    }

    @Override
    public List getSubTaskIssueLinks(final Long issueId)
    {
        throw new UnsupportedOperationException("Method not implemented yet");
    }

    @Override
    public Collection getSubTasks(final GenericValue issue)
    {
        throw new UnsupportedOperationException("Method not implemented yet");
    }

    @Override
    public Collection getSubTaskObjects(final Issue issue)
    {
        // Dumb implementation for no Issues with subtasks - fix this if/when required for tests.
        return Collections.emptyList();
    }

    @Override
    public IssueUpdateBean changeParent(final GenericValue subTask, final GenericValue parentIssue, final User currentUser)
            throws RemoveException, CreateException
    {
        throw new UnsupportedOperationException("Method not implemented yet");
    }

    @Override
    public IssueUpdateBean changeParent(Issue subTask, Issue parentIssue, User currentUser)
            throws RemoveException, CreateException
    {
        throw new UnsupportedOperationException("Method not implemented yet");
    }
}
