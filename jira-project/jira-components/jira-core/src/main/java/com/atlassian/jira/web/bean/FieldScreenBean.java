package com.atlassian.jira.web.bean;

import com.atlassian.jira.util.collect.MapBuilder;
import webwork.view.taglib.IteratorStatus;

import java.util.Map;

/**
 * <p>A simple bean to help <code>issuefields.jsp</code> do the job of passing the <code>displayParameters</code> map to
 * {@link com.atlassian.jira.issue.fields.screen.FieldScreenRenderLayoutItem#getCreateHtml(webwork.action.Action, com.atlassian.jira.issue.customfields.OperationContext, com.atlassian.jira.issue.Issue, java.util.Map)}.
 *
 * <p>The map passed in as a parameter to the JSP include is augmented with additional display parameters desired by
 * fields when rendering themselves. This is then passed on to the <code>getCreateHtml</code> and <code>getEditHtml</code>
 * methods.
 *
 * @since v4.2
 * @see com.atlassian.jira.issue.fields.screen.FieldScreenRenderLayoutItem#getCreateHtml(webwork.action.Action, com.atlassian.jira.issue.customfields.OperationContext, com.atlassian.jira.issue.Issue, java.util.Map)
 * @see com.atlassian.jira.issue.fields.screen.FieldScreenRenderLayoutItem#getEditHtml(webwork.action.Action, com.atlassian.jira.issue.customfields.OperationContext, com.atlassian.jira.issue.Issue, java.util.Map)
 */
public class FieldScreenBean
{
    /**
     * Given a base map, we add in extra information about the current {@link webwork.view.taglib.IteratorStatus} of the
     * field being rendered. This allows us to pass along information about the position of the field on the {@link com.atlassian.jira.issue.fields.screen.FieldScreenTab}
     * in relation to other fields.
     *
     * @param fieldStatus the status from the <code>iterator</code> tag that is relevant to the current field
     * @param baseDisplayParams the <code>displayParameters</code> map that was passed in to the JSP include as a parameter
     * @return a map which can be passed along as the "displayParameters" map to the field rendering code
     */
    public Map<String, Object> computeDisplayParams(final IteratorStatus fieldStatus, final Map<String, Object> baseDisplayParams)
    {
        final MapBuilder<String, Object> builder = MapBuilder.newBuilder(baseDisplayParams);
        builder.add("isFirstField", fieldStatus.isFirst());
        builder.add("isLastField", fieldStatus.isLast());
        return builder.toMutableMap();
    }
}
