package com.atlassian.jira.jql.operand;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.jql.query.QueryCreationContext;
import com.atlassian.jira.plugin.jql.function.JqlFunction;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.util.MessageSetImpl;
import com.atlassian.ozymandias.SafePluginPointAccess;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.operand.FunctionOperand;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

import com.google.common.base.Supplier;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Adapter to convert the plugin point {@link com.atlassian.jira.plugin.jql.function.JqlFunction} into
 * {@link com.atlassian.jira.jql.operand.OperandHandler}.
 *
 * @since v4.0
 */
public class FunctionOperandHandler implements OperandHandler<FunctionOperand>
{
    protected final I18nHelper i18nHelper;
    protected final JqlFunction jqlFunction;

    public FunctionOperandHandler(final JqlFunction jqlFunction, final I18nHelper i18nHelper)
    {
        this.jqlFunction = notNull("jqlFunction", jqlFunction);
        this.i18nHelper = i18nHelper;
    }

    public MessageSet validate(final User searcher, final FunctionOperand operand, final TerminalClause terminalClause)
    {
        return SafePluginPointAccess.call(new Callable<MessageSet>()
        {
            @Override
            public MessageSet call() throws Exception
            {
                return jqlFunction.validate(searcher, operand, terminalClause);
            }
        }).getOrElse(new Supplier<MessageSet>()
        {
            @Override
            public MessageSet get()
            {
                final String functionName = SafePluginPointAccess.call(new Callable<String>() {
                    @Override
                    public String call() throws Exception
                    {
                        return jqlFunction.getFunctionName();
                    }
                }).getOrNull();
                final MessageSetImpl messageSet = new MessageSetImpl();
                messageSet.addErrorMessage(i18nHelper.getText("jira.jql.operand.cannot.validate.function", functionName));
                return messageSet;
            }
        });
    }

    public List<QueryLiteral> getValues(final QueryCreationContext queryCreationContext, final FunctionOperand operand, final TerminalClause terminalClause)
    {
        return SafePluginPointAccess.call(new Callable<List<QueryLiteral>>()
        {
            @Override
            public List<QueryLiteral> call() throws Exception
            {
                return jqlFunction.getValues(queryCreationContext, operand, terminalClause);
            }
        }).getOrElse(Collections.<QueryLiteral>emptyList());
    }

    public boolean isList()
    {
        return SafePluginPointAccess.call(new Callable<Boolean>()
        {
            @Override
            public Boolean call() throws Exception
            {
                return jqlFunction.isList();
            }
        }).getOrElse(false);
    }

    public boolean isEmpty()
    {
        return false;
    }

    public boolean isFunction()
    {
        return true;
    }

    public JqlFunction getJqlFunction()
    {
        return jqlFunction;
    }
}
