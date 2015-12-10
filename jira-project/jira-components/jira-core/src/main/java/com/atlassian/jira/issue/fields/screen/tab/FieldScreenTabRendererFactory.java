package com.atlassian.jira.issue.fields.screen.tab;

import com.atlassian.jira.issue.fields.screen.FieldScreenRenderTab;

public interface FieldScreenTabRendererFactory
{
    FieldScreenRenderTab createTabRender(final IssueTabRendererDto issueTabRendererDto);
}
