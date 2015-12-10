package com.atlassian.jira.issue.search.searchers.impl;

import java.util.Set;

import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.query.clause.AndClause;
import com.atlassian.query.clause.Clause;
import com.atlassian.query.clause.NotClause;
import com.atlassian.query.clause.OrClause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operator.Operator;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @since v4.0
 */
public class TestOperatorCollectingVisitor
{
    @Test
    public void testOperatorCollector() throws Exception
    {
        Clause clause = new AndClause(new OrClause(new TerminalClauseImpl("blah", Operator.EQUALS, "blah"), new TerminalClauseImpl("safsd", Operator.GREATER_THAN, "blah")), new NotClause(new TerminalClauseImpl("sas", Operator.EQUALS, "sfsd")));
        OperatorCollectingVisitor visitor = new OperatorCollectingVisitor();
        clause.accept(visitor);

        Set<Operator> expectedResult = CollectionBuilder.newBuilder(Operator.EQUALS, Operator.GREATER_THAN).asSet();
        final Set<Operator> result = visitor.getOperators();
        assertEquals(expectedResult, result);
    }

}
