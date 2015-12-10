package com.atlassian.jira.issue.statistics;

import com.atlassian.jira.issue.customfields.searchers.transformer.CustomFieldInputHelper;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.JiraAuthenticationContext;

/**
 * @since v4.0
 */
public class CustomFieldProjectStatisticsMapper extends ProjectStatisticsMapper
{
    private CustomField customField;
    private CustomFieldInputHelper customFieldInputHelper;
    private JiraAuthenticationContext authenticationContext;

    public CustomFieldProjectStatisticsMapper(ProjectManager projectManager, CustomField customField,
            CustomFieldInputHelper customFieldInputHelper, final JiraAuthenticationContext authenticationContext)
    {
        super(projectManager, customField.getClauseNames().getPrimaryName(), customField.getId());
        this.customField = customField;
        this.customFieldInputHelper = customFieldInputHelper;
        this.authenticationContext = authenticationContext;
    }

    @Override
    protected String getClauseName()
    {
        return customFieldInputHelper.getUniqueClauseName(authenticationContext.getLoggedInUser(), customField.getClauseNames().getPrimaryName(), customField.getName());
    }
}
