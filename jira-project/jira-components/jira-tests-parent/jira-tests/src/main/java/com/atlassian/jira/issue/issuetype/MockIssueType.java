package com.atlassian.jira.issue.issuetype;

import com.atlassian.jira.avatar.Avatar;
import com.atlassian.jira.issue.MockIssueConstant;

/**
 * @since v3.13
 */
public class MockIssueType extends MockIssueConstant implements IssueType
{
    private boolean subTask;
    private Avatar avatar;

    /**
     * Constructs a non subtask IssueType with the given id and name.
     * @param id the id.
     * @param name the name.
     */
    public MockIssueType(String id, String name)
    {
        this(id, name, false);
    }

    /**
     * Constructs a non subtask IssueType with the given id and name.
     * @param id the id.
     * @param name the name.
     */
    public MockIssueType(long id, String name)
    {
        this(String.valueOf(id), name);
    }

    public MockIssueType(String id, String name, boolean subTask)
    {
        super(id, name);
        this.subTask = subTask;
    }

    public MockIssueType(String id, String name, boolean subTask, Avatar avatar)
    {
        super(id, name);
        this.subTask = subTask;
        this.avatar = avatar;
    }

    public boolean isSubTask()
    {
        return subTask;
    }

    @Override
    public Avatar getAvatar()
    {
        return avatar;
    }


}
