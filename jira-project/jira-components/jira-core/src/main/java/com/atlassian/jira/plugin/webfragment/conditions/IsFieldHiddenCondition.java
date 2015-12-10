package com.atlassian.jira.plugin.webfragment.conditions;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.web.FieldVisibilityManager;
import com.atlassian.plugin.PluginParseException;

import java.util.Map;

/**
 * Checks if the specified {@link #field} is hidden in at least one scheme
 * associated with the selected project and {@link #issuetype}
 * <p>
 * {@link #field} and {@link #issuetype} is initialised in {@link #init(java.util.Map params)}.
 * <li>{@link #field} is required
 * <li>{@link #issuetype} will default to {@link FieldVisibilityManager#ALL_ISSUE_TYPES} if it is not set
 */
public class IsFieldHiddenCondition extends AbstractJiraCondition
{
    protected String field;
    protected String issuetype;
    private final FieldVisibilityManager fieldVisibilityManager;

    public IsFieldHiddenCondition(FieldVisibilityManager fieldVisibilityManager)
    {
        this.fieldVisibilityManager = fieldVisibilityManager;
    }

    public void init(Map params) throws PluginParseException
    {
        field = (String) params.get("field");
        if (field == null)
        {
            throw new PluginParseException("No 'field' parameter specified for condition: " + this.getClass().getName());
        }
        issuetype = (String) params.get("issuetype");
        if (issuetype == null)
        {
            issuetype = FieldVisibilityManager.ALL_ISSUE_TYPES;
        }
    }

    public boolean shouldDisplay(User user, JiraHelper jiraHelper)
    {
        final Map<String, Object> params = jiraHelper.getContextParams();

        final Issue issue = (Issue) params.get("issue");

        if (issue == null)
        {
            if (jiraHelper.getProject() != null)
            {
                return fieldVisibilityManager.isFieldHidden(jiraHelper.getProjectObject().getId(), field, issuetype);
            }
        }
        else
        {
            return fieldVisibilityManager.isFieldHidden(field, issue);
        }
        return false;
    }
}
