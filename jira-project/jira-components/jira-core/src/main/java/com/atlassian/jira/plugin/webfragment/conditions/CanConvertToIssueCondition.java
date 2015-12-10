package com.atlassian.jira.plugin.webfragment.conditions;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.bc.subtask.conversion.IssueConversionService;
import com.atlassian.jira.bc.subtask.conversion.SubTaskToIssueConversionService;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.util.SimpleErrorCollection;
import org.apache.log4j.Logger;

/**
 * Condition that determines whether the current user can convert the current subtask to
 * an issue.
 * <p/>
 * An issue must be in the JiraHelper context params.
 *
 * @since v4.1
 */
public class CanConvertToIssueCondition extends AbstractIssueCondition
{
    private static final Logger log = Logger.getLogger(CanConvertToIssueCondition.class);
    private final IssueConversionService conversionService;


    public CanConvertToIssueCondition(SubTaskToIssueConversionService conversionService)
    {
        this.conversionService = conversionService;
    }

    public boolean shouldDisplay(User user, Issue issue, JiraHelper jiraHelper)
    {
        JiraServiceContext context = new JiraServiceContextImpl(user, new SimpleErrorCollection());
        return conversionService.canConvertIssue(context, issue);

    }

}