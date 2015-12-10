/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.movesubtask.operation;

import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.web.bean.MoveIssueBean;

public class MoveSubTaskParentOperation extends AbstractMoveSubTaskOperation
{
    public static final String NAME = "MoveSubTaskParent";
    public static final String NAME_KEY = "move.subtask.parent.operation.name";
    private static final String DESCRIPTION_KEY = "move.subtask.parent.operation.description";
    private static final String CANNOT_PERFORM_MESSAGE_KEY = "move.subtask.parent.operation.cannotperform";

    public MoveSubTaskParentOperation(IssueManager issueManager)
    {
        super(issueManager);
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
        if (!(o instanceof MoveSubTaskParentOperation))
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
        return CANNOT_PERFORM_MESSAGE_KEY;
    }
}
