package com.atlassian.jira.dev.reference.plugin.jql;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.JiraDataType;
import com.atlassian.jira.JiraDataTypes;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.query.QueryCreationContext;
import com.atlassian.jira.plugin.jql.function.AbstractJqlFunction;
import com.atlassian.jira.util.MessageSet;
import javax.annotation.Nonnull;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.operand.FunctionOperand;
import com.google.common.collect.Iterables;
import org.apache.commons.lang.StringUtils;

import java.util.Collections;
import java.util.List;

/**
 * Echoes the the reverse of the string passed in as an argument. Used to test upgrades.
 *
 * @since v4.4
 */
public class EchoJqlFunction extends AbstractJqlFunction
{
    public MessageSet validate(User searcher, @Nonnull FunctionOperand operand, @Nonnull TerminalClause terminalClause)
    {
        return validateNumberOfArgs(operand, 1);
    }

    public List<QueryLiteral> getValues(@Nonnull QueryCreationContext queryCreationContext,
            @Nonnull FunctionOperand operand, @Nonnull TerminalClause terminalClause)
    {
        String argumentAsString = Iterables.get(operand.getArgs(), 0);
        return Collections.singletonList(new QueryLiteral(operand, StringUtils.reverse(argumentAsString)));
    }

    public int getMinimumNumberOfExpectedArguments()
    {
        return 1;
    }

    public JiraDataType getDataType()
    {
        return JiraDataTypes.TEXT;
    }
}
