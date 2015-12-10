package com.atlassian.jira.issue.fields.screen.tab;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.Field;
import com.atlassian.jira.issue.fields.screen.FieldScreenTab;
import com.atlassian.jira.issue.operation.IssueOperation;
import com.atlassian.jira.util.Predicate;

public class IssueTabRendererDto
{
    private final Issue issue;
    private final IssueOperation operation;
    private final Predicate<? super Field> condition;
    private final int currentTabPosition;
    private final FieldScreenTab fieldScreenTab;

    public IssueTabRendererDto(final Issue issue, final IssueOperation operation, final Predicate<? super Field> condition, final int currentTabPosition, final FieldScreenTab fieldScreenTab)
    {
        this.issue = issue;
        this.operation = operation;
        this.condition = condition;
        this.currentTabPosition = currentTabPosition;
        this.fieldScreenTab = fieldScreenTab;
    }

    public Issue getIssue()
    {
        return issue;
    }

    public IssueOperation getOperation()
    {
        return operation;
    }

    public Predicate<? super Field> getCondition()
    {
        return condition;
    }

    public int getCurrentTabPosition()
    {
        return currentTabPosition;
    }

    public FieldScreenTab getFieldScreenTab()
    {
        return fieldScreenTab;
    }
}
