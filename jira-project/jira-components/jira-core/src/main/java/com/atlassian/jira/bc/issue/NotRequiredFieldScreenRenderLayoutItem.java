package com.atlassian.jira.bc.issue;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.customfields.OperationContext;
import com.atlassian.jira.issue.fields.OrderableField;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.screen.FieldScreenLayoutItem;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderLayoutItem;
import webwork.action.Action;

import java.util.Map;

/**
 * A "fake" {@link FieldScreenRenderLayoutItem} that is not requireable.
 * <p/>
 * This is used when we are "skipping screen checks" in
 *      {@link IssueService#validateUpdate(com.atlassian.crowd.embedded.api.User, Long, com.atlassian.jira.issue.IssueInputParameters)}.
 *
 * @since v5.0
 */
class NotRequiredFieldScreenRenderLayoutItem implements FieldScreenRenderLayoutItem
{
    /**
     * The only method we care about - we are never required
     */
    @Override
    public boolean isRequired()
    {
        return false;
    }

    @Override
    public OrderableField getOrderableField()
    {
        throw new UnsupportedOperationException("This is not a real FieldScreenRenderLayoutItem!");
    }

    @Override
    public String getEditHtml(Action action, OperationContext operationContext, Issue issue)
    {
        throw new UnsupportedOperationException("This is not a real FieldScreenRenderLayoutItem!");
    }

    @Override
    public String getCreateHtml(Action action, OperationContext operationContext, Issue issue)
    {
        throw new UnsupportedOperationException("This is not a real FieldScreenRenderLayoutItem!");
    }

    @Override
    public String getViewHtml(Action action, OperationContext operationContext, Issue issue)
    {
        throw new UnsupportedOperationException("This is not a real FieldScreenRenderLayoutItem!");
    }

    @Override
    public String getEditHtml(Action action, OperationContext operationContext, Issue issue, Map<String, Object> displayParams)
    {
        throw new UnsupportedOperationException("This is not a real FieldScreenRenderLayoutItem!");
    }

    @Override
    public String getCreateHtml(Action action, OperationContext operationContext, Issue issue, Map<String, Object> displayParams)
    {
        throw new UnsupportedOperationException("This is not a real FieldScreenRenderLayoutItem!");
    }

    @Override
    public String getViewHtml(Action action, OperationContext operationContext, Issue issue, Map<String, Object> displayParams)
    {
        throw new UnsupportedOperationException("This is not a real FieldScreenRenderLayoutItem!");
    }

    @Override
    public boolean isShow(Issue issue)
    {
        throw new UnsupportedOperationException("This is not a real FieldScreenRenderLayoutItem!");
    }

    @Override
    public void populateDefaults(Map fieldValuesHolder, Issue issue)
    {
        throw new UnsupportedOperationException("This is not a real FieldScreenRenderLayoutItem!");
    }

    @Override
    public void populateFromIssue(Map fieldValuesHolder, Issue issue)
    {
        throw new UnsupportedOperationException("This is not a real FieldScreenRenderLayoutItem!");
    }

    @Override
    public String getRendererType()
    {
        throw new UnsupportedOperationException("This is not a real FieldScreenRenderLayoutItem!");
    }

    @Override
    public FieldLayoutItem getFieldLayoutItem()
    {
        throw new UnsupportedOperationException("This is not a real FieldScreenRenderLayoutItem!");
    }

    @Override
    public FieldScreenLayoutItem getFieldScreenLayoutItem()
    {
        throw new UnsupportedOperationException("This is not a real FieldScreenRenderLayoutItem!");
    }
}
