package com.atlassian.jira.issue.search.util;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.jql.builder.JqlClauseBuilder;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.mock.component.MockComponentWorker;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Test for {@link com.atlassian.jira.issue.search.util.FreeTextVisitor}.
 *
 * @since v4.0
 */
public class TestFreeTextVisitor
{

    @Before
    public void setUp() throws Exception
    {
        ComponentAccessor.initialiseWorker(new MockComponentWorker());
    }

    @Test
    public void testContainsFreeTextHappyPositive() throws Exception
    {
        JqlClauseBuilder builder = JqlQueryBuilder.newBuilder().where().not().description("blah").and().sub().createdAfter("1d").or().created().ltEq("1w").endsub().and().issue("AA");
        assertTrue(FreeTextVisitor.containsFreeTextCondition(builder.buildClause()));
    }

    @Test
    public void testContainsFreeTextHappyNegative() throws Exception
    {
        JqlClauseBuilder clauseBuilder = JqlQueryBuilder.newBuilder().where().not().component("qwe").and().sub().createdAfter("1d").or().created().ltEq("1w").endsub().and().issue("AA");
        assertFalse(FreeTextVisitor.containsFreeTextCondition(clauseBuilder.buildClause()));
    }

    @Test
    public void testContainsFreeTextNullArgument() throws Exception
    {
        assertFalse(FreeTextVisitor.containsFreeTextCondition(null));
    }
}
