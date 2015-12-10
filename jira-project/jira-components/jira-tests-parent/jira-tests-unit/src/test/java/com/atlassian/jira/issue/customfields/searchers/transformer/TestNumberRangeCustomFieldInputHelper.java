package com.atlassian.jira.issue.customfields.searchers.transformer;

import java.util.Collections;
import java.util.List;

import com.atlassian.jira.issue.search.ClauseNames;
import com.atlassian.jira.issue.search.searchers.transformer.SimpleNavigatorCollectorVisitor;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.validator.MockJqlOperandResolver;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.query.Query;
import com.atlassian.query.QueryImpl;
import com.atlassian.query.clause.AndClause;
import com.atlassian.query.clause.NotClause;
import com.atlassian.query.clause.OrClause;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operand.MultiValueOperand;
import com.atlassian.query.operator.Operator;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * @since v4.0
 */
public class TestNumberRangeCustomFieldInputHelper
{
    ClauseNames clauseNames = new ClauseNames("cf[100");
    JqlOperandResolver jqlOperandResolver = MockJqlOperandResolver.createSimpleSupport();

    @Test
    public void testGetValuesFromQueryNotValid() throws Exception
    {
        final TerminalClauseImpl clause = new TerminalClauseImpl("blah", Operator.EQUALS, "blah");
        Query query = new QueryImpl(clause);

        final NumberRangeCustomFieldInputHelper inputHelper = new NumberRangeCustomFieldInputHelper(clauseNames, jqlOperandResolver)
        {
            @Override
            protected SimpleNavigatorCollectorVisitor createSimpleNavigatorCollectorVisitor()
            {
                return new MySimpleNavigatorCollectorVisitor(clauseNames, Collections.<TerminalClause>emptyList(), false);
            }
        };

        assertNull(inputHelper.getValuesFromQuery(query));
    }

    @Test
    public void testGetValuesFromQueryNoClauses() throws Exception
    {
        final TerminalClauseImpl clause = new TerminalClauseImpl("blah", Operator.EQUALS, "blah");
        Query query = new QueryImpl(clause);

        final NumberRangeCustomFieldInputHelper inputHelper = new NumberRangeCustomFieldInputHelper(clauseNames, jqlOperandResolver)
        {
            @Override
            protected SimpleNavigatorCollectorVisitor createSimpleNavigatorCollectorVisitor()
            {
                return new MySimpleNavigatorCollectorVisitor(clauseNames, Collections.<TerminalClause>emptyList(), true);
            }
        };

        assertTrue(inputHelper.getValuesFromQuery(query).isEmpty());
    }

    @Test
    public void testGetValuesFromQueryInvalidOperand() throws Exception
    {
        final TerminalClause clause = new TerminalClauseImpl(clauseNames.getPrimaryName(), Operator.LESS_THAN_EQUALS, new MultiValueOperand(10L, 10L));
        Query query = new QueryImpl(clause);

        final NumberRangeCustomFieldInputHelper inputHelper = new NumberRangeCustomFieldInputHelper(clauseNames, jqlOperandResolver)
        {
            @Override
            protected SimpleNavigatorCollectorVisitor createSimpleNavigatorCollectorVisitor()
            {
                return new MySimpleNavigatorCollectorVisitor(clauseNames, CollectionBuilder.newBuilder(clause).asList(), true);
            }
        };
        assertNull(inputHelper.getValuesFromQuery(query));
    }

    @Test
    public void testGetValuesFromQueryInvalidOperator() throws Exception
    {
        final TerminalClause clause = new TerminalClauseImpl(clauseNames.getPrimaryName(), Operator.EQUALS, 10L);
        Query query = new QueryImpl(clause);

        final NumberRangeCustomFieldInputHelper inputHelper = new NumberRangeCustomFieldInputHelper(clauseNames, jqlOperandResolver)
        {
            @Override
            protected SimpleNavigatorCollectorVisitor createSimpleNavigatorCollectorVisitor()
            {
                return new MySimpleNavigatorCollectorVisitor(clauseNames, CollectionBuilder.newBuilder(clause).asList(), true);
            }
        };
        assertNull(inputHelper.getValuesFromQuery(query));
    }

    @Test
    public void testGetValuesFromQuerySameOperators() throws Exception
    {
        final TerminalClause clause1 = new TerminalClauseImpl(clauseNames.getPrimaryName(), Operator.GREATER_THAN_EQUALS, 10L);
        final TerminalClause clause2 = new TerminalClauseImpl(clauseNames.getPrimaryName(), Operator.GREATER_THAN_EQUALS, 10L);
        Query query = new QueryImpl(clause1);

        final NumberRangeCustomFieldInputHelper inputHelper = new NumberRangeCustomFieldInputHelper(clauseNames, jqlOperandResolver)
        {
            @Override
            protected SimpleNavigatorCollectorVisitor createSimpleNavigatorCollectorVisitor()
            {
                return new MySimpleNavigatorCollectorVisitor(clauseNames, CollectionBuilder.newBuilder(clause1, clause2).asList(), true);
            }
        };
        assertNull(inputHelper.getValuesFromQuery(query));
    }

    @Test
    public void testGetValuesFromQueryValidSingle() throws Exception
    {
        final TerminalClause clause1 = new TerminalClauseImpl(clauseNames.getPrimaryName(), Operator.GREATER_THAN_EQUALS, 10L);
        Query query = new QueryImpl(clause1);

        final NumberRangeCustomFieldInputHelper inputHelper = new NumberRangeCustomFieldInputHelper(clauseNames, jqlOperandResolver)
        {
            @Override
            protected SimpleNavigatorCollectorVisitor createSimpleNavigatorCollectorVisitor()
            {
                return new MySimpleNavigatorCollectorVisitor(clauseNames, CollectionBuilder.newBuilder(clause1).asList(), true);
            }
        };
        final List<TerminalClause> result = inputHelper.getValuesFromQuery(query);
        assertEquals(1, result.size());
        assertTrue(result.contains(clause1));
    }
    
    @Test
    public void testGetValuesFromQueryValidTwo() throws Exception
    {
        final TerminalClause clause1 = new TerminalClauseImpl(clauseNames.getPrimaryName(), Operator.GREATER_THAN_EQUALS, 10L);
        final TerminalClause clause2 = new TerminalClauseImpl(clauseNames.getPrimaryName(), Operator.LESS_THAN_EQUALS, 10L);
        Query query = new QueryImpl(clause1);

        final NumberRangeCustomFieldInputHelper inputHelper = new NumberRangeCustomFieldInputHelper(clauseNames, jqlOperandResolver)
        {
            @Override
            protected SimpleNavigatorCollectorVisitor createSimpleNavigatorCollectorVisitor()
            {
                return new MySimpleNavigatorCollectorVisitor(clauseNames, CollectionBuilder.newBuilder(clause1, clause2).asList(), true);
            }
        };
        final List<TerminalClause> result = inputHelper.getValuesFromQuery(query);
        assertEquals(2, result.size());
        assertTrue(result.contains(clause1));
        assertTrue(result.contains(clause2));
    }

    @Test
    public void testGetValuesFromQueryToManyClauses() throws Exception
    {
        final TerminalClause clause1 = new TerminalClauseImpl(clauseNames.getPrimaryName(), Operator.GREATER_THAN_EQUALS, 10L);
        final TerminalClause clause2 = new TerminalClauseImpl(clauseNames.getPrimaryName(), Operator.LESS_THAN_EQUALS, 10L);
        final TerminalClause clause3 = new TerminalClauseImpl(clauseNames.getPrimaryName(), Operator.LESS_THAN_EQUALS, 10L);
        Query query = new QueryImpl(clause1);

        final NumberRangeCustomFieldInputHelper inputHelper = new NumberRangeCustomFieldInputHelper(clauseNames, jqlOperandResolver)
        {
            @Override
            protected SimpleNavigatorCollectorVisitor createSimpleNavigatorCollectorVisitor()
            {
                return new MySimpleNavigatorCollectorVisitor(clauseNames, CollectionBuilder.newBuilder(clause1, clause2, clause3).asList(), true);
            }
        };
        assertNull(inputHelper.getValuesFromQuery(query));
    }

    class MySimpleNavigatorCollectorVisitor extends SimpleNavigatorCollectorVisitor
    {
        private final List<TerminalClause> clauses;
        private final boolean valid;

        public MySimpleNavigatorCollectorVisitor(final ClauseNames clauseNames, List<TerminalClause> clauses, boolean isValid)
        {
            super(clauseNames);
            this.clauses = clauses;
            valid = isValid;
        }

        @Override
        public List<TerminalClause> getClauses()
        {
            return clauses;
        }

        @Override
        public boolean isValid()
        {
            return valid;
        }

        @Override
        public Void visit(final AndClause andClause)
        {
            return null;
        }

        @Override
        public Void visit(final NotClause notClause)
        {
            return null;
        }

        @Override
        public Void visit(final OrClause orClause)
        {
            return null;
        }

        @Override
        public Void visit(final TerminalClause terminalClause)
        {
            return null;
        }
    }
}
