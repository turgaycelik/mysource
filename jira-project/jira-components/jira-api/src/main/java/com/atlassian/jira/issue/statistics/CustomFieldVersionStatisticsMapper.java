package com.atlassian.jira.issue.statistics;

import com.atlassian.jira.issue.customfields.searchers.transformer.CustomFieldInputHelper;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.jql.util.JqlCustomFieldId;
import com.atlassian.jira.project.version.VersionManager;
import com.atlassian.jira.security.JiraAuthenticationContext;

/**
 * @since v4.0
 */
public class CustomFieldVersionStatisticsMapper extends VersionStatisticsMapper
{
    private CustomField customField;
    private JiraAuthenticationContext authenticationContext;
    private CustomFieldInputHelper customFieldInputHelper;

    public CustomFieldVersionStatisticsMapper(CustomField customField, VersionManager versionManager,
            JiraAuthenticationContext authenticationContext, CustomFieldInputHelper customFieldInputHelper,
            boolean includeArchived)
    {
        super(JqlCustomFieldId.toString(customField.getIdAsLong()), customField.getId(), versionManager, includeArchived);
        this.customField = customField;
        this.authenticationContext = authenticationContext;
        this.customFieldInputHelper = customFieldInputHelper;
    }

    @Override
    protected String getClauseName()
    {
        return customFieldInputHelper.getUniqueClauseName(authenticationContext.getLoggedInUser(), customField.getClauseNames().getPrimaryName(), customField.getName());
    }
}
