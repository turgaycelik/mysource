package com.atlassian.jira.plugin.jql.function;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.JiraDataType;
import com.atlassian.jira.JiraDataTypes;
import com.atlassian.jira.issue.watchers.WatchedIssuesAccessor;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.query.QueryCreationContext;
import com.atlassian.jira.util.Function;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.util.MessageSetImpl;
import com.atlassian.jira.util.collect.Transformed;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.operand.FunctionOperand;
import com.google.common.collect.ImmutableList;

import java.util.Collections;
import java.util.List;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Returns the issue ids of the watched issues for the current user.
 *
 * This function can only be used if Watching Issues is currently enabled.
 *
 * @since v4.0
 */
public class WatchedIssuesFunction extends AbstractJqlFunction
{
    public static final String FUNCTION_WATCHED_ISSUES = "watchedIssues";

    private final WatchedIssuesAccessor watchAccessor;

    public WatchedIssuesFunction(final WatchedIssuesAccessor watchAccessor)
    {
        this.watchAccessor = notNull("watchAccessor", watchAccessor);
    }

    public MessageSet validate(final User searcher, final FunctionOperand operand, final TerminalClause terminalClause)
    {
        MessageSet messageSet;
        if (!watchAccessor.isWatchingEnabled())
        {
            messageSet = new MessageSetImpl();
            messageSet.addErrorMessage(getI18n().getText("jira.jql.function.watched.issues.disabled", getFunctionName()));
        }
        else if (searcher == null)
        {
            messageSet = new MessageSetImpl();
            messageSet.addErrorMessage(getI18n().getText("jira.jql.function.anonymous.disallowed", getFunctionName()));
        }
        else
        {
            messageSet = validateNumberOfArgs(operand, 0);
        }
        return messageSet;
    }

    public List<QueryLiteral> getValues(final QueryCreationContext queryCreationContext, final FunctionOperand operand, final TerminalClause terminalClause)
    {
        notNull("queryCreationContext", queryCreationContext);

        if (queryCreationContext.getQueryUser() == null)
        {
            return Collections.emptyList();
        }
        return ImmutableList.copyOf(Transformed.iterable(getWatchedIssues(queryCreationContext.getQueryUser(), queryCreationContext.isSecurityOverriden()),
                new Function<Long, QueryLiteral>()
                {
                    public QueryLiteral get(final Long votedIssue)
                    {
                        return new QueryLiteral(operand, votedIssue);
                    }
                }));
    }

    public int getMinimumNumberOfExpectedArguments()
    {
        return 0;
    }

    public JiraDataType getDataType()
    {
        return JiraDataTypes.ISSUE;
    }

    Iterable<Long> getWatchedIssues(final User searcher, final boolean overrideSecurity)
    {
        final WatchedIssuesAccessor.Security security = overrideSecurity ? WatchedIssuesAccessor.Security.OVERRIDE : WatchedIssuesAccessor.Security.RESPECT;
        return watchAccessor.getWatchedIssueIds(searcher, searcher, security);
    }
}
