package com.atlassian.jira.plugin.webfragment.conditions;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.bc.subtask.conversion.IssueConversionService;
import com.atlassian.jira.bc.subtask.conversion.IssueToSubTaskConversionService;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.util.SimpleErrorCollection;
import org.apache.log4j.Logger;

/**
 * Condition that determines whether the current user can convert the current issue to a sub task.
 * <p/>
 * An issue must be in the JiraHelper context params.
 *
 * @since v4.1
 */
public class CanConvertToSubTaskCondition extends AbstractIssueCondition
{
    private static final Logger log = Logger.getLogger(CanConvertToSubTaskCondition.class);
    private final IssueConversionService conversionService;


    public CanConvertToSubTaskCondition(IssueToSubTaskConversionService conversionService)
    {
        this.conversionService = conversionService;
    }

    public boolean shouldDisplay(User user, Issue issue, JiraHelper jiraHelper)
    {
        JiraServiceContext context = new JiraServiceContextImpl(user, new SimpleErrorCollection());
        return conversionService.canConvertIssue(context, issue);

    }

}