package com.atlassian.jira.plugin.jql.function;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.JiraDataType;
import com.atlassian.jira.JiraDataTypes;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.query.QueryCreationContext;
import com.atlassian.jira.user.UserHistoryItem;
import com.atlassian.jira.user.UserIssueHistoryManager;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.operand.FunctionOperand;
import org.apache.log4j.Logger;

import java.util.LinkedList;
import java.util.List;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * A handler for the "issueHistory" function. This function will return all the issues within the user's history.
 *
 * @since v4.0
 */
public class IssueHistoryFunction extends AbstractJqlFunction
{
    public static final String FUNCTION_ISSUE_HISTORY = "issueHistory";
    private static final Logger log = Logger.getLogger(IssueHistoryFunction.class);

    private final UserIssueHistoryManager userHistoryManager;

    public IssueHistoryFunction(final UserIssueHistoryManager userHistoryManager)
    {
        this.userHistoryManager = notNull("userHistoryManager", userHistoryManager);
    }

    public MessageSet validate(final User searcher, final FunctionOperand operand, final TerminalClause terminalClause)
    {
        return validateNumberOfArgs(operand, 0);
    }

    public List<QueryLiteral> getValues(final QueryCreationContext queryCreationContext, final FunctionOperand operand, final TerminalClause terminalClause)
    {
        notNull("queryCreationContext", queryCreationContext);
        final List<QueryLiteral> literals = new LinkedList<QueryLiteral>();

        final List<UserHistoryItem> history = queryCreationContext.isSecurityOverriden() ?
                userHistoryManager.getFullIssueHistoryWithoutPermissionChecks(queryCreationContext.getQueryUser())
                : userHistoryManager.getFullIssueHistoryWithPermissionChecks(queryCreationContext.getQueryUser());
        for (final UserHistoryItem userHistoryItem : history)
        {
            final String value = userHistoryItem.getEntityId();

            try
            {
                literals.add(new QueryLiteral(operand, Long.parseLong(value)));
            }
            catch (NumberFormatException e)
            {
                log.warn(String.format("User history returned a non numeric issue ID '%s'.", value));
            }
        }

        return literals;
    }

    public int getMinimumNumberOfExpectedArguments()
    {
        return 0;
    }

    public JiraDataType getDataType()
    {
        return JiraDataTypes.ISSUE;
    }

}
