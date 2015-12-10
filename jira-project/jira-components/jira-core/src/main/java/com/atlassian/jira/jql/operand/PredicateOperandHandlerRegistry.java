package com.atlassian.jira.jql.operand;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.index.ChangeHistoryFieldConfigurationManager;
import com.atlassian.jira.jql.operand.registry.JqlFunctionHandlerRegistry;
import com.atlassian.jira.jql.query.QueryCreationContextImpl;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.ApplicationUsers;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operand.EmptyOperand;
import com.atlassian.query.operand.FunctionOperand;
import com.atlassian.query.operand.MultiValueOperand;
import com.atlassian.query.operand.Operand;
import com.atlassian.query.operand.SingleValueOperand;
import com.atlassian.query.operator.Operator;

import com.google.common.collect.Lists;

import org.apache.log4j.Logger;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Has the standard handlers for dealing with history predicates
 *
 * @since v4.3
 */
public class PredicateOperandHandlerRegistry
{
    private static final Logger log = Logger.getLogger(PredicateOperandHandlerRegistry.class);

    private final JqlFunctionHandlerRegistry functionRegistry;
    private final ChangeHistoryFieldConfigurationManager changeHistoryFieldConfigurationManager;


    public PredicateOperandHandlerRegistry(final JqlFunctionHandlerRegistry functionRegistry, ChangeHistoryFieldConfigurationManager changeHistoryFieldConfigurationManager)
    {
        this.changeHistoryFieldConfigurationManager = changeHistoryFieldConfigurationManager;
        this.functionRegistry = notNull("functionRegistry", functionRegistry);
    }

    public PredicateOperandHandler getHandler(User searcher, String field, Operand operand)
    {
        if (operand instanceof SingleValueOperand)
        {
            return new SingleValuePredicateOperandHandler(searcher,(SingleValueOperand)operand);
        }
        else if (operand instanceof EmptyOperand)
        {
            return new EmptyPredicateOperandHandler(changeHistoryFieldConfigurationManager, searcher, field, (EmptyOperand)operand);
        }
        else if (operand instanceof MultiValueOperand)
        {
            return new MultiValuePredicateOperandHandler(searcher,this, field, (MultiValueOperand)operand);
        }
        else if (operand instanceof FunctionOperand)
        {
            return new FunctionPredicateOperandHandler(searcher, (FunctionOperand)operand, functionRegistry);
        }
        else
        {
            log.debug(String.format("Unknown operand type '%s' with name '%s'", operand.getClass(), operand.getDisplayString()));
            return null;
        }

    }


    final static class SingleValuePredicateOperandHandler
            implements PredicateOperandHandler
    {
        private final SingleValueOperand singleValueOperand;
        private final User searcher;

        SingleValuePredicateOperandHandler(User searcher, SingleValueOperand singleValueOperand)
        {
            this.singleValueOperand = singleValueOperand;
            this.searcher = searcher;
        }

        @Override
        public List<QueryLiteral> getValues()
        {
            if (singleValueOperand.getLongValue() == null)
            {
                return Collections.singletonList(new QueryLiteral(singleValueOperand, singleValueOperand.getStringValue()));
            }
            else
            {
                return Collections.singletonList(new QueryLiteral(singleValueOperand, singleValueOperand.getLongValue()));
            }
        }

        @Override
        public boolean isEmpty()
        {
            return false;
        }

        @Override
        public boolean isList()
        {
            return false;
        }

        @Override
        public boolean isFunction()
        {
            return false;
        }
    }

    final static class EmptyPredicateOperandHandler implements PredicateOperandHandler
    {

        private final EmptyOperand emptyOperand;
        private final User searcher;
        private final String field;
        private final ChangeHistoryFieldConfigurationManager changeHistoryFieldConfigurationManager;

        EmptyPredicateOperandHandler(ChangeHistoryFieldConfigurationManager changeHistoryFieldConfigurationManager, User searcher, String field, EmptyOperand emptyOperand)
        {
            this.changeHistoryFieldConfigurationManager = changeHistoryFieldConfigurationManager;
            this.searcher = searcher;
            this.field = field;
            this.emptyOperand = emptyOperand;
        }

        @Override
        public List<QueryLiteral> getValues()
        {
            List<QueryLiteral> literals = Lists.newArrayList();
            if (emptyOperand != null)
            {
                literals.add(new QueryLiteral(emptyOperand, getStringValueForEmpty(field)));
            }
            return literals;
        }

        @Override
        public boolean isEmpty()
        {
            return true;
        }

        @Override
        public boolean isList()
        {
            return false;
        }

        @Override
        public boolean isFunction()
        {
            return false;
        }

        private String getStringValueForEmpty(String field)
        {
            return (field != null) ? changeHistoryFieldConfigurationManager.getEmptyValue(field.toLowerCase()) : null;
        }
    }

    final static class MultiValuePredicateOperandHandler implements PredicateOperandHandler
    {
        private final PredicateOperandHandlerRegistry handlerRegistry;
        private final MultiValueOperand operand;
        private final String field;
        private final User searcher;

        MultiValuePredicateOperandHandler(User searcher, PredicateOperandHandlerRegistry handlerRegistry, String field, MultiValueOperand operand)
        {
            this.searcher = searcher;
            this.handlerRegistry = handlerRegistry;
            this.field = field;
            this.operand = operand;
        }

        @Override
        public List<QueryLiteral> getValues()
        {
            List<QueryLiteral> valuesList = new ArrayList<QueryLiteral>();
            for (Operand subOperand : operand.getValues())
            {
                final List<QueryLiteral> vals = handlerRegistry.getHandler(searcher, field, subOperand).getValues();
                if (vals != null)
                {
                    valuesList.addAll(vals);
                }
            }
            return valuesList;
        }

        @Override
        public boolean isEmpty()
        {
            return false;
        }

        @Override
        public boolean isList()
        {
            return true;
        }

        @Override
        public boolean isFunction()
        {
            return false;
        }
    }

    final static class FunctionPredicateOperandHandler implements PredicateOperandHandler
    {

        private final FunctionOperand operand;
        private final ApplicationUser searcher;
        private final JqlFunctionHandlerRegistry functionRegistry;

        FunctionPredicateOperandHandler(User searcher, FunctionOperand operand, JqlFunctionHandlerRegistry functionRegistry)
        {
            this.searcher = ApplicationUsers.from(searcher);
            this.operand = operand;
            this.functionRegistry = functionRegistry;
        }

        @Override
        public List<QueryLiteral> getValues()
        {
            FunctionOperandHandler handler = functionRegistry.getOperandHandler(operand);
            return handler != null ?
                    handler.getValues(new QueryCreationContextImpl(searcher), operand, new TerminalClauseImpl("PredicateOperandClause", Operator.EQUALS, operand)) :
                    Collections.<QueryLiteral>emptyList();
        }

        @Override
        public boolean isEmpty()
        {
            return false;
        }

        @Override
        public boolean isList()
        {
            return false;
        }

        @Override
        public boolean isFunction()
        {
            return true;
        }
    }
}
