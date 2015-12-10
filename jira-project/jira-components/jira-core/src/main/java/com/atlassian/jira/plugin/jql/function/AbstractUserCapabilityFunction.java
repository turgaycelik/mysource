package com.atlassian.jira.plugin.jql.function;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.query.QueryCreationContext;
import com.atlassian.jira.jql.validator.NumberOfArgumentsValidator;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.operand.FunctionOperand;

import java.util.Collections;
import java.util.List;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * This is an abstract parent to user based functions that rely on returning a list of a list of QueryLiteral based on
 * either the current user or a user passed as a parameter.
 * <p/>
 * This function expects one or two arguments.
 * The first argument is the capability requested, typically a permission or role etc,
 * The second argument is the user, if this is not supplied the current logged in user will be used.
 *
 * @since v4.2
 */
public abstract class AbstractUserCapabilityFunction extends AbstractJqlFunction
{
    private static final int MIN_EXPECTED_ARGS = 1;
    private static final int MAX_EXPECTED_ARGS = 1;
    protected final UserUtil userUtil;

    public AbstractUserCapabilityFunction(final UserUtil userUtil)
    {
        this.userUtil = notNull("userUtil", userUtil);
    }

    public MessageSet validate(final User searcher, final FunctionOperand functionOperand, final TerminalClause terminalClause)
    {
        I18nHelper i18n = getI18n();
        final MessageSet messageSet = new NumberOfArgumentsValidator(MIN_EXPECTED_ARGS, i18n).validate(functionOperand);
        if (!messageSet.hasAnyErrors())
        {
            if (functionOperand.getArgs().size() == 1 && searcher == null)
            {
                messageSet.addErrorMessage(i18n.getText("jira.jql.function.anonymous.disallowed", getFunctionName()));
            }
// The functionality to run this for a user other than the logged in user has been removed until decisions can be made regarding http://jira.atlassian.com/browse/JRA-21476
//            else
//            {
//                if (functionOperand.getArgs().size() == 2)
//                {
//                    final String username = functionOperand.getArgs().get(1);
//                    if (userUtil.getUser(username) == null)
//                    {
//                        messageSet.addErrorMessage(i18n.getText(getUserNotFoundMessageKey(), functionOperand.getName(), username));
//                    }
//                }
//            }
            messageSet.addMessageSet(validateCapability(functionOperand.getArgs().get(0), i18n));
        }
        return messageSet;
    }

    public int getMinimumNumberOfExpectedArguments()
    {
        return MIN_EXPECTED_ARGS;
    }

    /**
     * Check the capability requested.
     *
     * @param capability The name of the capability being checked. This will be the name of something like a permission
     * or role
     * @param i18n I18Helper
     * @return a {@link com.atlassian.jira.util.MessageSet} which must not be null, but may be empty when there are no
     *         errors.
     */
    protected abstract MessageSet validateCapability(String capability, final I18nHelper i18n);



    public List<QueryLiteral> getValues(final QueryCreationContext queryCreationContext, final FunctionOperand functionOperand, final TerminalClause terminalClause)
    {
        com.atlassian.jira.util.dbc.Assertions.notNull("queryCreationContext", queryCreationContext);

        if (functionOperand.getArgs().size() < MIN_EXPECTED_ARGS)
        {
            return Collections.emptyList();
        }
        final String requiredCapability = functionOperand.getArgs().get(0);

        ApplicationUser user;
//        if (functionOperand.getArgs().size() == 2)
//        {
//            final String username = functionOperand.getArgs().get(1);
//            user = userUtil.getUser(username);
//        }
//        else
//        {
            user = queryCreationContext.getApplicationUser();
//        }

        if (user == null || requiredCapability == null || requiredCapability.length() == 0)
        {
            return Collections.emptyList();
        }
        return getFunctionValuesList(queryCreationContext, functionOperand, user, requiredCapability);
    }

    /**
     * Get the function return values based on the actual user and capability. transformed into index values.
     *
     * @param queryCreationContext the context of query creation
     * @param operand the operand to get values from
     * @param user the user.  This value may be the searcher or a user the searcher is enquiring on behalf of.
     *                        If the 2nd parameter (a User) of the function is supplied then the capability of
     *                        that user is quueried.
     *                        The functionality to run this for a user other than the logged in user has been removed
     *                        until decisions can be made regarding http://jira.atlassian.com/browse/JRA-21476
     * @param capability the capability being requested
     * @return a List of objects that represent this Operands raw values. Cannot be null.
     */
    protected abstract List<QueryLiteral> getFunctionValuesList(QueryCreationContext queryCreationContext, FunctionOperand operand, ApplicationUser user, String capability);

    /**
     * Get the I18n message key for the error when the passed in user does not exist.
     *
     * @return message key.
     */
    protected abstract String getUserNotFoundMessageKey();
}
