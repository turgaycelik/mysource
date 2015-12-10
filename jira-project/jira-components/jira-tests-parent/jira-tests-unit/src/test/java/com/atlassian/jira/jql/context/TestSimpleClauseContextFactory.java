package com.atlassian.jira.jql.context;

import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operator.Operator;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @since v4.0
 */
public class TestSimpleClauseContextFactory
{
    @Test
    public void testGetClauseContext() throws Exception
    {
        SimpleClauseContextFactory factory = new SimpleClauseContextFactory();

        final ClauseContext expectedResult = ClauseContextImpl.createGlobalClauseContext();

        final ClauseContext result = factory.getClauseContext(null, new TerminalClauseImpl("blah", Operator.EQUALS, "blah"));

        assertEquals(expectedResult, result);
    }
}
