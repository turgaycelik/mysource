package com.atlassian.jira.plugin.jql.function;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.query.QueryCreationContext;
import com.atlassian.jira.jql.validator.NumberOfArgumentsValidator;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.util.MessageSetImpl;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.operand.FunctionOperand;

import java.util.Collections;
import java.util.List;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * This is an abstract parent to user based functions that rely on returning a list of a list of QueryLiteral based on either the current user or
 * a user passed as a parameter.
 * <p/>
 * This function expects zero or one argument. If zero arguments are supplied the current logged in user will be used.
 * @since v4.2
 */
public abstract class AbstractUserBasedFunction extends AbstractJqlFunction
{
    private static final int MIN_EXPECTED_ARGS = 0;
    private static final int MAX_EXPECTED_ARGS = 1;
    protected final UserUtil userUtil;

    public AbstractUserBasedFunction(final UserUtil userUtil)
    {
        this.userUtil = notNull("userUtil", userUtil);
    }

    public MessageSet validate(final User searcher, final FunctionOperand functionOperand, final TerminalClause terminalClause)
    {
        final MessageSet messageSet;
        I18nHelper i18n = getI18n();
        if (functionOperand.getArgs().size() == 0 && searcher == null)
        {
            messageSet = new MessageSetImpl();
            messageSet.addErrorMessage(i18n.getText("jira.jql.function.anonymous.disallowed", getFunctionName()));
        }
        else
        {
            MessageSet messages = validateNumberOfArgs(functionOperand, i18n);
            if (!messages.hasAnyErrors())
            {
                if (functionOperand.getArgs().size() == 1)
                {
                    final String username = functionOperand.getArgs().get(0);
                    if (userUtil.getUserByName(username) == null)
                    {
                        messages.addErrorMessage(i18n.getText(getUserNotFoundMessageKey(), functionOperand.getName(), username));
                    }
                }
            }
            return messages;
        }
        return messageSet;
    }

    private MessageSet validateNumberOfArgs(FunctionOperand functionOperand, I18nHelper i18n)
    {
        return new NumberOfArgumentsValidator(MIN_EXPECTED_ARGS, MAX_EXPECTED_ARGS, i18n).validate(functionOperand);
    }

    public int getMinimumNumberOfExpectedArguments()
    {
        return MIN_EXPECTED_ARGS;
    }

    public List<QueryLiteral> getValues(final QueryCreationContext queryCreationContext, final FunctionOperand functionOperand, final TerminalClause terminalClause)
    {
        com.atlassian.jira.util.dbc.Assertions.notNull("queryCreationContext", queryCreationContext);

        ApplicationUser user;
        if (functionOperand.getArgs().size() == 1)
        {
            final String username = functionOperand.getArgs().get(0);
            user = userUtil.getUserByName(username);
        }
        else
        {
            user = queryCreationContext.getApplicationUser();
        }

        if (user == null)
        {
            return Collections.emptyList();
        }
        return getFunctionValuesList(queryCreationContext, functionOperand, user);
    }

    /**
     * Get the function return values based on the actual user.
     * transformed into index values.
     * @param queryCreationContext the context of query creation
     * @param operand the operand to get values from
     * @param user the user
     * @return a List of objects that represent this Operands raw values. Cannot be null.
     */
    protected abstract List<QueryLiteral> getFunctionValuesList(QueryCreationContext queryCreationContext, FunctionOperand operand, ApplicationUser user);

    /**
     * Get the I18n message key for the error when the passed in user does not exist.
     * @return message key.
     */
    protected abstract String getUserNotFoundMessageKey();
}
