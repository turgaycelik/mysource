package com.atlassian.jira.issue.statistics;

import com.atlassian.jira.issue.customfields.searchers.transformer.CustomFieldInputHelper;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.jql.util.JqlCustomFieldId;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.util.UserManager;

/**
 * @since v4.0
 */
public class CustomFieldUserStatisticsMapper extends UserStatisticsMapper
{
    private CustomField customField;
    private CustomFieldInputHelper customFieldInputHelper;

    public CustomFieldUserStatisticsMapper(CustomField customField, UserManager userManager,
            JiraAuthenticationContext jiraAuthenticationContext, final CustomFieldInputHelper customFieldInputHelper)
    {
        super(JqlCustomFieldId.toString(customField.getIdAsLong()), null, customField.getId(), userManager, jiraAuthenticationContext);
        this.customField = customField;
        this.customFieldInputHelper = customFieldInputHelper;
    }

    @Override
    protected String getClauseName()
    {
        return customFieldInputHelper.getUniqueClauseName(jiraAuthenticationContext.getLoggedInUser(), customField.getClauseNames().getPrimaryName(), customField.getName());
    }
}
