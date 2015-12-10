package com.atlassian.jira.jql.operand;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.issue.search.QueryCache;
import com.atlassian.jira.jql.operand.registry.JqlFunctionHandlerRegistry;
import com.atlassian.jira.jql.query.QueryCreationContext;
import com.atlassian.jira.jql.query.QueryCreationContextImpl;
import com.atlassian.jira.plugin.jql.function.ClauseSanitisingJqlFunction;
import com.atlassian.jira.plugin.jql.function.JqlFunction;
import com.atlassian.jira.user.ApplicationUsers;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.InjectableComponent;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.util.MessageSetImpl;
import com.atlassian.ozymandias.SafePluginPointAccess;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.clause.WasClause;
import com.atlassian.query.operand.EmptyOperand;
import com.atlassian.query.operand.FunctionOperand;
import com.atlassian.query.operand.MultiValueOperand;
import com.atlassian.query.operand.Operand;
import com.atlassian.query.operand.SingleValueOperand;
import com.atlassian.util.profiling.UtilTimerStack;
import org.apache.log4j.Logger;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Default implementation of the {@link JqlOperandResolver} interface.
 *
 * @since v4.0
 */
@InjectableComponent
public final class DefaultJqlOperandResolver implements JqlOperandResolver
{
    private static final Logger log = Logger.getLogger(DefaultJqlOperandResolver.class);

    private final JqlFunctionHandlerRegistry registry;
    private final I18nHelper.BeanFactory factory;
    private final OperandHandler<EmptyOperand> emptyHandler;
    private final OperandHandler<SingleValueOperand> singleHandler;
    private final OperandHandler<MultiValueOperand> multiHandler;
    // this is a request scoped cache
    private final QueryCache queryCache;

    public DefaultJqlOperandResolver(final JqlFunctionHandlerRegistry registry, final I18nHelper.BeanFactory factory, final QueryCache queryCache)
    {
        this.factory = notNull("factory", factory);
        this.registry = notNull("registry", registry);
        this.queryCache = notNull("queryCache", queryCache);
        this.emptyHandler = new EmptyOperandHandler();
        this.singleHandler = new SingleValueOperandHandler();
        this.multiHandler = new MultiValueOperandHandler(this);
    }

    DefaultJqlOperandResolver(final JqlFunctionHandlerRegistry registry, final I18nHelper.BeanFactory factory, final OperandHandler<EmptyOperand> emptyHandler, final OperandHandler<SingleValueOperand> singleHandler, final OperandHandler<MultiValueOperand> multiHandler, final QueryCache queryCache)
    {
        this.registry = notNull("registry", registry);
        this.factory = notNull("factory", factory);
        this.emptyHandler = notNull("emptyHandler", emptyHandler);
        this.singleHandler = notNull("singleHandler", singleHandler);
        this.multiHandler = notNull("multiHandler", multiHandler);
        this.queryCache = notNull("queryCache", queryCache);
    }

    public List<QueryLiteral> getValues(final User searcher, final Operand operand, final TerminalClause terminalClause)
    {
        return getValues(new QueryCreationContextImpl(ApplicationUsers.from(searcher)), operand, terminalClause);
    }

    public List<QueryLiteral> getValues(final QueryCreationContext queryCreationContext, final Operand operand, final TerminalClause terminalClause)
    {
        notNull("operand", operand);
        List<QueryLiteral> values = getValuesFromCache(queryCreationContext, operand, terminalClause);
        if (values == null)
        {
            if (operand instanceof EmptyOperand)
            {
                values = emptyHandler.getValues(queryCreationContext, (EmptyOperand) operand, terminalClause);
            }
            else if (operand instanceof SingleValueOperand)
            {
                values =  singleHandler.getValues(queryCreationContext, (SingleValueOperand) operand, terminalClause);
            }
            else if (operand instanceof MultiValueOperand)
            {
                UtilTimerStack.push("DefaultJqlOperandResolver.getValues() - multivalue");
                values =  multiHandler.getValues(queryCreationContext, (MultiValueOperand) operand, terminalClause);
                UtilTimerStack.pop("DefaultJqlOperandResolver.getValues() - multivalue");
            }
            else if (operand instanceof FunctionOperand)
            {
                UtilTimerStack.push("DefaultJqlOperandResolver.getValues() - function");
                final FunctionOperand funcOperand = (FunctionOperand) operand;
                final OperandHandler<FunctionOperand> handler = registry.getOperandHandler(funcOperand);
                values = handler != null ? handler.getValues(queryCreationContext, funcOperand, terminalClause) : Collections.<QueryLiteral>emptyList();
                UtilTimerStack.pop("DefaultJqlOperandResolver.getValues() - function");
            }
            else
            {
                log.debug(String.format("Unknown operand type '%s' with name '%s'", operand.getClass(), operand.getDisplayString()));
            }
            putValuesInCache(queryCreationContext, operand, terminalClause, values);
        }
        return values;
    }

    public MessageSet validate(final User user, final Operand operand, final TerminalClause terminalClause)
    {
        notNull("operand", operand);
        if (operand instanceof EmptyOperand)
        {
            return emptyHandler.validate(user, (EmptyOperand) operand, terminalClause);
        }
        else if (operand instanceof SingleValueOperand)
        {
            return singleHandler.validate(user, (SingleValueOperand) operand, terminalClause);
        }
        else if (operand instanceof MultiValueOperand)
        {
            return multiHandler.validate(user, (MultiValueOperand) operand, terminalClause);
        }
        else if (operand instanceof FunctionOperand)
        {
            final FunctionOperand funcOperand = (FunctionOperand) operand;
            OperandHandler<FunctionOperand> handler = registry.getOperandHandler(funcOperand);
            if (handler != null)
            {
                return handler.validate(user, funcOperand, terminalClause);
            }
            else
            {
                final MessageSet messageSet = new MessageSetImpl();
                messageSet.addErrorMessage(getI18n(user).getText("jira.jql.operand.illegal.function", operand.getDisplayString()));
                return messageSet;
            }
        }
        else
        {
            log.debug(String.format("Unknown operand type '%s' with name '%s'", operand.getClass(), operand.getDisplayString()));

            final MessageSet messageSet = new MessageSetImpl();
            messageSet.addErrorMessage(getI18n(user).getText("jira.jql.operand.illegal.operand", operand.getDisplayString()));
            return messageSet;
        }
    }

    //todo need to add operand validation
    @Override
    public MessageSet validate(User searcher, Operand operand, WasClause clause)
    {
       return new MessageSetImpl();
    }

    public FunctionOperand sanitiseFunctionOperand(final User searcher, final FunctionOperand funcOperand)
    {
        final FunctionOperandHandler funcHandler = registry.getOperandHandler(funcOperand);
        if (funcHandler != null)
        {
            final JqlFunction jqlFunction = funcHandler.getJqlFunction();
            if (jqlFunction instanceof ClauseSanitisingJqlFunction)
            {
                return SafePluginPointAccess.call(new Callable<FunctionOperand>()
                {
                    @Override
                    public FunctionOperand call() throws Exception
                    {
                        return ((ClauseSanitisingJqlFunction) jqlFunction).sanitiseOperand(searcher, funcOperand);
                    }
                }).getOrElse(funcOperand);
            }
        }

        return funcOperand;
    }

    public QueryLiteral getSingleValue(final User user, final Operand operand, final TerminalClause clause)
    {
        final List<QueryLiteral> list = getValues(user, operand, clause);
        if (list == null || list.isEmpty())
        {
            return null;
        }
        else if (list.size() > 1)
        {
            throw new IllegalArgumentException("Found more than one value in operand '" + operand + "'; values were: " + list);
        }
        else
        {
            return list.get(0);
        }
    }

    public boolean isEmptyOperand(final Operand operand)
    {
        final OperandHandler<?> operandHandler = getOperandHandler(operand);
        return operandHandler != null && operandHandler.isEmpty();
    }

    public boolean isFunctionOperand(final Operand operand)
    {
        final OperandHandler<?> handler = getOperandHandler(operand);
        return handler != null && handler.isFunction();
    }

    public boolean isListOperand(final Operand operand)
    {
        final OperandHandler<?> handler = getOperandHandler(operand);
        return handler != null && handler.isList();
    }

    public boolean isValidOperand(final Operand operand)
    {
        return getOperandHandler(operand) != null;
    }

    private I18nHelper getI18n(final User user)
    {
        return factory.getInstance(user);
    }

    private OperandHandler<?> getOperandHandler(Operand operand)
    {
        notNull("operand", operand);
        if (operand instanceof EmptyOperand)
        {
            return emptyHandler;
        }
        else if (operand instanceof SingleValueOperand)
        {
            return singleHandler;
        }
        else if (operand instanceof MultiValueOperand)
        {
            return multiHandler;
        }
        else if (operand instanceof FunctionOperand)
        {
            final FunctionOperand funcOperand = (FunctionOperand) operand;
            return registry.getOperandHandler(funcOperand);
        }
        else
        {
            log.debug(String.format("Unknown operand type '%s' with name '%s'", operand.getClass(), operand.getDisplayString()));
            return null;
        }
    }

    private List<QueryLiteral> getValuesFromCache(QueryCreationContext ctx, Operand operand, TerminalClause jqlClause)
    {
        return queryCache.getValues(ctx, operand, jqlClause);
    }
    

    private void putValuesInCache(QueryCreationContext ctx, Operand operand, TerminalClause jqlClause, List<QueryLiteral> values)
    {
        if (values!= null)
        {
            queryCache.setValues(ctx, operand, jqlClause, values);
        }
    }
}
