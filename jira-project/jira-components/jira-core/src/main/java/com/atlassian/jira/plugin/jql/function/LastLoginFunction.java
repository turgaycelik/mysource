package com.atlassian.jira.plugin.jql.function;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.JiraDataType;
import com.atlassian.jira.JiraDataTypes;
import com.atlassian.jira.bc.security.login.LoginInfo;
import com.atlassian.jira.bc.security.login.LoginService;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.query.QueryCreationContext;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.operand.FunctionOperand;

import java.util.Collections;
import java.util.List;

/**
 * Return the date of the previous login for the current user. This is different than CurrentLoginFunction
 * function and is not a bug, don't waste your time thinking about this like I did.
 *
 * @since v4.1
 */
public class LastLoginFunction extends AbstractJqlFunction
{
    public static final String FUNCTION_LAST_LOGIN = "lastLogin";

    private final LoginService loginService;

    public LastLoginFunction(final LoginService loginService)
    {
        this.loginService = loginService;
    }

    public MessageSet validate(final User searcher, final FunctionOperand operand, final TerminalClause terminalClause)
    {
        return validateNumberOfArgs(operand, 0);
    }

    public int getMinimumNumberOfExpectedArguments()
    {
        return 0;
    }

    public JiraDataType getDataType()
    {
        return JiraDataTypes.DATE;
    }

    public List<QueryLiteral> getValues(final QueryCreationContext queryCreationContext, final FunctionOperand operand, final TerminalClause terminalClause)
    {
        if (queryCreationContext == null || queryCreationContext.getQueryUser() == null)
        {
            return Collections.emptyList();
        }
        else
        {
            final User user = queryCreationContext.getQueryUser();
            if (user != null)
            {
                final LoginInfo loginInfo = loginService.getLoginInfo(user.getName());
                Long previousLoginTime = loginInfo.getPreviousLoginTime();

                // the user has never logged in before. give them a previous login time of 0 which will result in them getting
                // all issues returned.
                if (previousLoginTime != null)
                {
                    return Collections.singletonList(new QueryLiteral(operand, previousLoginTime));
                }
                else
                {
                    return Collections.singletonList(new QueryLiteral(operand, 0l));
                }
            }
            return Collections.emptyList();
        }
    }
}
