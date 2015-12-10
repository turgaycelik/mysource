package com.atlassian.jira.plugin.viewissue.conditions;

import java.util.Map;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.plugin.webfragment.conditions.AbstractJiraCondition;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.web.FieldVisibilityManager;
import com.atlassian.plugin.PluginAccessor;

import org.apache.commons.lang.StringUtils;

/**
 * Shows the description field if the issue-nav-plugin and inline edit is enabled OR we have a value.
 * Hides the description field if field is hidden in configuration OR we don't have issue-nav-plugin enabled and there
 * is no value
 */
public class ShowDescriptionCondition extends AbstractJiraCondition
{
    private final PluginAccessor pluginAccessor;
    private final FieldVisibilityManager fieldVisibilityManager;
    private final IssueManager issueManager;
    private final ApplicationProperties applicationProperties;

    public ShowDescriptionCondition(final PluginAccessor pluginAccessor, final FieldVisibilityManager fieldVisibilityManager,
            final IssueManager issueManager, final ApplicationProperties applicationProperties)
    {
        this.pluginAccessor = pluginAccessor;
        this.fieldVisibilityManager = fieldVisibilityManager;
        this.issueManager = issueManager;
        this.applicationProperties = applicationProperties;
    }

    @Override
    public boolean shouldDisplay(User user, JiraHelper jiraHelper)
    {
        final Map<String, Object> params = jiraHelper.getContextParams();
        final Issue issue = (Issue) params.get("issue");
        if (issue != null)
        {
            if (fieldVisibilityManager.isFieldHidden(IssueFieldConstants.DESCRIPTION, issue))
            {
                return false;
            }

            if (StringUtils.isNotBlank(issue.getDescription()))
            {
                return true;
            }
            else if (pluginAccessor.isPluginEnabled("com.atlassian.jira.jira-issue-nav-plugin") &&
                    !applicationProperties.getOption(APKeys.JIRA_OPTION_DISABLE_INLINE_EDIT) &&
                    issueManager.isEditable(issue, user))
            {
                return true;
            }
        }
        return false;
    }
}

