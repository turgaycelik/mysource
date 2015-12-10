package com.atlassian.jira.comment;

import java.util.Map;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.customfields.OperationContext;
import com.atlassian.jira.issue.fields.OrderableField;
import com.atlassian.jira.issue.fields.layout.field.FieldLayout;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.plugin.webfragment.CacheableContextProvider;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.plugin.PluginParseException;

import webwork.action.Action;

/**
 * Context Provider for Add Comment block on View issue page.
 */
public class AddCommentContextProvider implements CacheableContextProvider
{
    private final FieldLayoutManager fieldLayoutManager;

    public AddCommentContextProvider(FieldLayoutManager fieldLayoutManager)
    {
        this.fieldLayoutManager = fieldLayoutManager;
    }

    @Override
    public void init(Map<String, String> params) throws PluginParseException
    {
    }

    @Override
    public Map<String, Object> getContextMap(Map<String, Object> context)
    {
        final Issue issue = (Issue) context.get("issue");
        final Action action = (Action) context.get("action");

        Map<String, Object> paramsBuilder = MapBuilder.newBuilder(context).toMutableMap();
        paramsBuilder.put("commentHtml", getCommentHtml(issue, action));

        return paramsBuilder;
    }

    @Override
    public String getUniqueContextKey(Map<String, Object> context)
    {
        final Issue issue = (Issue) context.get("issue");
        final User user = (User) context.get("user");

        return issue.getId() + "/" + (user == null ? "" : user.getName());
    }

    private String getCommentHtml(Issue issue, Action action)
    {
        final OperationContext context = (OperationContext) action;

        final MapBuilder<String, Object> displayParams = MapBuilder.newBuilder();

        displayParams.add("theme", "aui");
        displayParams.add("noHeader", true);

        final FieldLayout fieldLayout = fieldLayoutManager.getFieldLayout(issue);
        final FieldLayoutItem commentFieldLayoutItem = fieldLayout.getFieldLayoutItem("comment");
        final OrderableField commentField = commentFieldLayoutItem.getOrderableField();

        return commentField.getCreateHtml(commentFieldLayoutItem, context, action, issue, displayParams.toMap());
    }

}

