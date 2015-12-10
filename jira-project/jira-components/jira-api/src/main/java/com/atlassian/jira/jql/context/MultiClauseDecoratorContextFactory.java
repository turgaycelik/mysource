package com.atlassian.jira.jql.context;

import com.atlassian.annotations.Internal;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.validator.OperatorUsageValidator;
import javax.annotation.Nonnull;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operand.EmptyOperand;
import com.atlassian.query.operand.MultiValueOperand;
import com.atlassian.query.operand.Operand;
import com.atlassian.query.operand.SingleValueOperand;
import com.atlassian.query.operator.Operator;
import com.atlassian.util.profiling.UtilTimerStack;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Decorates a {@link com.atlassian.jira.jql.context.ClauseContextFactory} to ensure that:
 *
 * <pre>
 * context(k in (a, b, c)) &lt;=&gt; context(k = a or k = b or k = c)
 * context(k not in (a, b, c)) &lt;=&gt; context(k != a and k != b and k != c)
 * </pre>
 *
 * It does this by intercepting calls to {@link #getClauseContext(User, com.atlassian.query.clause.TerminalClause)}
 * with a terminal clause that contains the {@link com.atlassian.query.operator.Operator#IN} or {@link com.atlassian.query.operator.Operator#NOT_IN}
 * operator and converts it into equivalent multiple calls to the delegate factory.
 *
 * @since v4.0
 */
@Internal
public class MultiClauseDecoratorContextFactory implements ClauseContextFactory
{
    private final ClauseContextFactory delegate;
    private final JqlOperandResolver jqlOperandResolver;
    private final ContextSetUtil contextSetUtil;

    public MultiClauseDecoratorContextFactory(final JqlOperandResolver jqlOperandResolver,
            final ClauseContextFactory delegate)
    {
        this(jqlOperandResolver, delegate, ContextSetUtil.getInstance());
    }

    public MultiClauseDecoratorContextFactory(final JqlOperandResolver jqlOperandResolver,
            final ClauseContextFactory delegate, final ContextSetUtil contextSetUtil)
    {
        this.contextSetUtil = notNull("contextSetUtil", contextSetUtil);
        this.jqlOperandResolver = notNull("jqlOperandResolver", jqlOperandResolver);
        this.delegate = notNull("delegate", delegate);
    }

    public final ClauseContext getClauseContext(final User searcher, final TerminalClause terminalClause)
    {
        UtilTimerStack.push("MultiClauseDecoratorContextFactory.getClauseContext()");
        try
        {
            final Operand operand = terminalClause.getOperand();
            final List<QueryLiteral> literals = jqlOperandResolver.getValues(searcher, operand, terminalClause);
            if (literals == null || literals.isEmpty())
            {
                return ClauseContextImpl.createGlobalClauseContext();
            }

            final Operator convertedOperator = convertListOperator(terminalClause.getOperator());
            if (convertedOperator == null || !jqlOperandResolver.isListOperand(operand))
            {
                return delegate.getClauseContext(searcher, terminalClause);
            }
            else
            {
                UtilTimerStack.push("MultiClauseDecoratorContextFactory.getClauseContext() - looping");
                final Set<ClauseContext> ctxs = new HashSet<ClauseContext>();
                MultiValueOperand operands = new MultiValueOperand(literals.toArray(new QueryLiteral[literals.size()]));
                final TerminalClause listClause = new TerminalClauseImpl(terminalClause.getName(), convertedOperator, operands);
                UtilTimerStack.pop("MultiClauseDecoratorContextFactory.getClauseContext() - looping");
                UtilTimerStack.push("MultiClauseDecoratorContextFactory.getClauseContext() - delegate");
                ctxs.add(delegate.getClauseContext(searcher, listClause));
                UtilTimerStack.pop("MultiClauseDecoratorContextFactory.getClauseContext() - delegate");

                if (convertedOperator == Operator.EQUALS)
                {
                    return contextSetUtil.union(ctxs);
                }
                else
                {
                    return contextSetUtil.intersect(ctxs);
                }
            }
        }
        finally
        {
            UtilTimerStack.pop("MultiClauseDecoratorContextFactory.getClauseContext()");
        }
    }

    private static Operator convertListOperator(Operator operator)
    {
        if (operator == Operator.IN)
        {
            return Operator.EQUALS;
        }
        else if (operator == Operator.NOT_IN)
        {
            return Operator.NOT_EQUALS;
        }
        else
        {
            return null;
        }
    }

    /**
     * Factory to create a {@link MultiClauseDecoratorContextFactory} given a
     * {@link com.atlassian.jira.jql.context.ClauseContextFactory} to wrap.
     *
     * @since 4.0
     */
    public static class Factory
    {
        private final OperatorUsageValidator validator;
        private final JqlOperandResolver resolver;
        private final ContextSetUtil contextSetUtil;

        public Factory(final OperatorUsageValidator validator, final JqlOperandResolver resolver, final ContextSetUtil contextSetUtil)
        {
            this.contextSetUtil = notNull("contextSetUtil", contextSetUtil);
            this.validator = notNull("validator", validator);
            this.resolver = notNull("resolver", resolver);
        }

        /**
         * Same as calling {@code create(delegate, true)}.
         *
         * @param delegate the ClauseContextFactory to wrap.
         * @return the wrapped clause context factory.
         */
        @Nonnull
        public ClauseContextFactory create(@Nonnull ClauseContextFactory delegate)
        {
            return create(delegate, true);
        }

        /**
         * Wrap the passed {@link com.atlassian.jira.jql.context.ClauseContextFactory} in a {@link com.atlassian.jira.jql.context.MultiClauseDecoratorContextFactory}.
         * When validating is set to true, the returned ClauseContextFactory will also perform a validation step on the passed clause when
         * generating the clause context.
         *
         * @param delegate the factory to wrap. Cannot be null.
         * @param validating true if the returned factory should perform a validation step, or false otherwise.
         * @return the wrapped clause context factory.
         */
        @Nonnull
        public ClauseContextFactory create(@Nonnull ClauseContextFactory delegate, boolean validating)
        {
            ClauseContextFactory factory = new MultiClauseDecoratorContextFactory(resolver, notNull("delegate", delegate), contextSetUtil);
            if (validating)
            {
                factory = new ValidatingDecoratorContextFactory(validator, factory);
            }
            return factory;
        }
    }
}
