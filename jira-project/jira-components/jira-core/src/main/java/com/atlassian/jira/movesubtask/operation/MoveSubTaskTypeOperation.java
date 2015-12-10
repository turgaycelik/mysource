/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.movesubtask.operation;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.fields.config.manager.IssueTypeSchemeManager;
import com.atlassian.jira.web.bean.MoveIssueBean;

public class MoveSubTaskTypeOperation extends AbstractMoveSubTaskOperation
{
    public static final String NAME = "MoveSubTaskType";
    public static final String NAME_KEY = "move.subtask.type.operation.name";
    private static final String DESCRIPTION_KEY = "move.subtask.type.operation.description";
    private static final String CANNOT_PERFORM_MESSAGE_GENERIC = "move.subtask.type.operation.cannotperform";
    private static final String CANNOT_PERFORM_MESSAGE_NO_SUBTASK = "movesubtask.nosubtasktypes";
    private static final String CANNOT_PERFORM_MESSAGE_NO_SUBTASK_IN_PROJECT = "movesubtask.nosubtasktypes.for.project";
    private final IssueManager issueManager;
    private final IssueTypeSchemeManager issueTypeSchemeManager;
    private final ConstantsManager constantsManager;

    public MoveSubTaskTypeOperation(IssueManager issueManager, IssueTypeSchemeManager issueTypeSchemeManager,
                                    ConstantsManager constantsManager)
    {
        super(issueManager);
        this.issueManager = issueManager;
        this.issueTypeSchemeManager = issueTypeSchemeManager;
        this.constantsManager = constantsManager;
    }

    public boolean canPerform(MoveIssueBean moveIssueBean, User remoteUser)
    {
        final MutableIssue issue = issueManager.getIssueObject(moveIssueBean.getIssueId());

        return super.canPerform(moveIssueBean, remoteUser) && hasProjectMoreThanOneSubTaskType(issue);
    }

    private boolean hasProjectMoreThanOneSubTaskType(MutableIssue issue)
    {
        return issueTypeSchemeManager.getSubTaskIssueTypesForProject(issue.getProjectObject()).size() > 1;
    }

    public String getNameKey()
    {
        return NAME_KEY;
    }

    public String getDescriptionKey()
    {
        return DESCRIPTION_KEY;
    }

    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof MoveSubTaskTypeOperation))
        {
            return false;
        }

        return true;
    }

    public String getOperationName()
    {
        return NAME;
    }

    public String getCannotPerformMessageKey(MoveIssueBean moveIssueBean)
    {
        if (!(constantsManager.getSubTaskIssueTypeObjects().size() > 1))
        {
            return CANNOT_PERFORM_MESSAGE_NO_SUBTASK;
        }
        final MutableIssue issue = issueManager.getIssueObject(moveIssueBean.getIssueId());

        return hasProjectMoreThanOneSubTaskType(issue) ? CANNOT_PERFORM_MESSAGE_GENERIC : CANNOT_PERFORM_MESSAGE_NO_SUBTASK_IN_PROJECT;
    }
}
