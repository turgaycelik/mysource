package com.atlassian.jira.jql.query;

import java.util.LinkedHashMap;
import java.util.List;

import com.atlassian.jira.exception.ParseException;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.jql.resolver.MockPriorityResolver;
import com.atlassian.jira.jql.validator.MockJqlOperandResolver;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operand.MultiValueOperand;
import com.atlassian.query.operand.SingleValueOperand;
import com.atlassian.query.operator.Operator;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * Unit test for {@link com.atlassian.jira.jql.query.PriorityClauseQueryFactory}.
 *
 * TODO this should probably be refactored into unit tests that test the classes that the PriorityClauseQueryFactory delegates to.
 *
 * @since v4.0
 */
public class TestPriorityClauseQueryFactory extends MockControllerTestCase
{
    private MockPriorityResolver priorityResolver;

    @Before
    public void setUp() throws Exception
    {
        setUpMocks(createDummyPriorityData());
    }

    @Test
    public void testEquals() throws ParseException
    {
        final TerminalClause constantsClause = new TerminalClauseImpl(IssueFieldConstants.PRIORITY, Operator.EQUALS, new SingleValueOperand("major"));
        ClauseQueryFactory factory = new PriorityClauseQueryFactory(priorityResolver, MockJqlOperandResolver.createSimpleSupport());
        final QueryFactoryResult query = factory.getQuery(null, constantsClause);
        assertEquals("priority:1234", query.getLuceneQuery().toString());
    }

    @Test
    public void testRelational() throws ParseException
    {
        ClauseQueryFactory factory = new PriorityClauseQueryFactory(priorityResolver, MockJqlOperandResolver.createSimpleSupport());
        TerminalClause constantsClause = new TerminalClauseImpl(IssueFieldConstants.PRIORITY, Operator.LESS_THAN, new SingleValueOperand("major"));
        QueryFactoryResult query = factory.getQuery(null, constantsClause);
        assertFalse(query.mustNotOccur());
        assertEquals("priority:1233 priority:1232", query.getLuceneQuery().toString());

        constantsClause = new TerminalClauseImpl(IssueFieldConstants.PRIORITY, Operator.LESS_THAN_EQUALS, new SingleValueOperand("major"));
        query = factory.getQuery(null, constantsClause);
        assertFalse(query.mustNotOccur());
        assertEquals("priority:1234 priority:1233 priority:1232", query.getLuceneQuery().toString());

        constantsClause = new TerminalClauseImpl(IssueFieldConstants.PRIORITY, Operator.GREATER_THAN, new SingleValueOperand("minor"));
        query = factory.getQuery(null, constantsClause);
        assertFalse(query.mustNotOccur());
        assertEquals("priority:1234", query.getLuceneQuery().toString());

        constantsClause = new TerminalClauseImpl(IssueFieldConstants.PRIORITY, Operator.GREATER_THAN_EQUALS, new SingleValueOperand("minor"));
        query = factory.getQuery(null, constantsClause);
        assertFalse(query.mustNotOccur());
        assertEquals("priority:1234 priority:1233", query.getLuceneQuery().toString());
    }

    @Test
    public void testRelationalWithIds() throws ParseException
    {
        ClauseQueryFactory factory = new PriorityClauseQueryFactory(priorityResolver, MockJqlOperandResolver.createSimpleSupport());
        TerminalClause constantsClause = new TerminalClauseImpl(IssueFieldConstants.PRIORITY, Operator.LESS_THAN, new SingleValueOperand(1234L));
        QueryFactoryResult query = factory.getQuery(null, constantsClause);
        assertFalse(query.mustNotOccur());
        assertEquals("priority:1233 priority:1232", query.getLuceneQuery().toString());

        constantsClause = new TerminalClauseImpl(IssueFieldConstants.PRIORITY, Operator.LESS_THAN_EQUALS, new SingleValueOperand(1234L));
        query = factory.getQuery(null, constantsClause);
        assertFalse(query.mustNotOccur());
        assertEquals("priority:1234 priority:1233 priority:1232", query.getLuceneQuery().toString());

        constantsClause = new TerminalClauseImpl(IssueFieldConstants.PRIORITY, Operator.GREATER_THAN, new SingleValueOperand(1233L));
        query = factory.getQuery(null, constantsClause);
        assertFalse(query.mustNotOccur());
        assertEquals("priority:1234", query.getLuceneQuery().toString());

        constantsClause = new TerminalClauseImpl(IssueFieldConstants.PRIORITY, Operator.GREATER_THAN_EQUALS, new SingleValueOperand(1233L));
        query = factory.getQuery(null, constantsClause);
        assertFalse(query.mustNotOccur());
        assertEquals("priority:1234 priority:1233", query.getLuceneQuery().toString());
    }

    @Test
    public void testNulls() throws ParseException
    {
        ClauseQueryFactory factory = new PriorityClauseQueryFactory(priorityResolver, MockJqlOperandResolver.createSimpleSupport());
        TerminalClause constantsClause = new TerminalClauseImpl(IssueFieldConstants.PRIORITY, Operator.EQUALS, new SingleValueOperand("nosuch"));
        QueryFactoryResult query = factory.getQuery(null, constantsClause);
        assertFalse(query.mustNotOccur());
        assertEquals("", query.getLuceneQuery().toString());

        constantsClause = new TerminalClauseImpl(IssueFieldConstants.PRIORITY, Operator.EQUALS, new SingleValueOperand(555L));
        query = factory.getQuery(null, constantsClause);
        assertFalse(query.mustNotOccur());
        assertEquals("", query.getLuceneQuery().toString());
    }

    @Test
    public void testEqualsList() throws ParseException
    {
        // NOTE that the values are coming from the MultiValueOperandHandler, not the priorityResolver
        ClauseQueryFactory factory = new PriorityClauseQueryFactory(priorityResolver, MockJqlOperandResolver.createSimpleSupport());
        TerminalClause priorityClause = new TerminalClauseImpl(IssueFieldConstants.PRIORITY, Operator.EQUALS, new MultiValueOperand("major", "minor"));

        final QueryFactoryResult result = factory.getQuery(null, priorityClause);
        assertEquals(QueryFactoryResult.createFalseResult(), result);
    }

    @Test
    public void testLessThanList() throws ParseException
    {
        // NOTE that the values are coming from the MultiValueOperandHandler, not the priorityResolver
        ClauseQueryFactory factory = new PriorityClauseQueryFactory(priorityResolver, MockJqlOperandResolver.createSimpleSupport());
        TerminalClause priorityClause = new TerminalClauseImpl(IssueFieldConstants.PRIORITY, Operator.LESS_THAN, new MultiValueOperand("major", "minor"));

        final QueryFactoryResult result = factory.getQuery(null, priorityClause);
        assertEquals(QueryFactoryResult.createFalseResult(), result);
    }

    @Test
    public void testNot() throws ParseException
    {
        ClauseQueryFactory factory = new PriorityClauseQueryFactory(priorityResolver, MockJqlOperandResolver.createSimpleSupport());
        TerminalClause constantsClause = new TerminalClauseImpl(IssueFieldConstants.PRIORITY, Operator.NOT_EQUALS, new SingleValueOperand("minor"));
        QueryFactoryResult query = factory.getQuery(null, constantsClause);
        assertFalse(query.mustNotOccur());
        assertEquals("+(-priority:-1 +visiblefieldids:priority) -priority:1233 +visiblefieldids:priority", query.getLuceneQuery().toString());
    }

    @Test
    public void testInOperator() throws ParseException
    {
        // NOTE that the values are coming from the MultiValueOperandHandler, not the priorityResolver
        ClauseQueryFactory factory = new PriorityClauseQueryFactory(priorityResolver, MockJqlOperandResolver.createSimpleSupport());
        TerminalClause constantsClause = new TerminalClauseImpl(IssueFieldConstants.PRIORITY, Operator.IN, new MultiValueOperand("micro", "major"));
        QueryFactoryResult query = factory.getQuery(null, constantsClause);
        assertFalse(query.mustNotOccur());
        assertEquals("priority:1232 priority:1234", query.getLuceneQuery().toString());
    }

    /**
     * Tests a name based lookup where the name is not unique.
     * the expectation may be a little surprising, but this is because the scenario is pretty artificial.
     * however, because "minor" is not a unique name, we expect the lucene query to be generated for two
     * possible interpretations, ORed together. The GREATER_THAN_EQUALS case looks like this:
     * 1. priority >= "minor" as 1233. (1233, 6666, 1234)
     * 2. priority >= "minor" as 6666 (6666, 1234)
     * The two priorities 1233 and 6666 have the same name, but 6666 sorts "higher".
     * Our mock sets up the order (sequence) based on insertion order in the map.
     *
     * @throws ParseException should not happen, so fail the test
     */
    @Test
    public void testMultipleValuesForName() throws ParseException
    {
        final LinkedHashMap<String, List<Long>> map = new LinkedHashMap<String, List<Long>>();
        map.put("major", asList(1234L));
        map.put("minor", asList(1233L, 6666L));
        map.put("micro", asList(1232L));

        setUpMocks(map);
        ClauseQueryFactory factory = new PriorityClauseQueryFactory(priorityResolver, MockJqlOperandResolver.createSimpleSupport());
        TerminalClause constantsClause = new TerminalClauseImpl(IssueFieldConstants.PRIORITY, Operator.EQUALS, new SingleValueOperand("minor"));
        QueryFactoryResult query = factory.getQuery(null, constantsClause);
        assertFalse(query.mustNotOccur());
        assertEquals("priority:1233 priority:6666", query.getLuceneQuery().toString());

        constantsClause = new TerminalClauseImpl(IssueFieldConstants.PRIORITY, Operator.GREATER_THAN_EQUALS, new SingleValueOperand("minor"));
        query = factory.getQuery(null, constantsClause);

        assertEquals("(priority:1234 priority:1233) (priority:1234 priority:1233 priority:6666)", query.getLuceneQuery().toString());
    }

    @Test
    public void testSingleValueOperandForInOperator() throws ParseException
    {
        ClauseQueryFactory factory = new PriorityClauseQueryFactory(priorityResolver, MockJqlOperandResolver.createSimpleSupport());
        TerminalClause constantsClause = new TerminalClauseImpl(IssueFieldConstants.PRIORITY, Operator.IN, new SingleValueOperand("micro"));

        final QueryFactoryResult result = factory.getQuery(null, constantsClause);
        assertEquals(QueryFactoryResult.createFalseResult(), result);
    }

    private LinkedHashMap<String, List<Long>> createDummyPriorityData()
    {
        final LinkedHashMap<String, List<Long>> map = new LinkedHashMap<String, List<Long>>();
        map.put("major", asList(1234L));
        map.put("minor", asList(1233L));
        map.put("micro", asList(1232L));
        return map;
    }

    private void setUpMocks(final LinkedHashMap<String, List<Long>> namesToIds)
    {
        priorityResolver = new MockPriorityResolver(namesToIds);
    }

    private <T> List<T> asList(T ... elements)
    {
        return CollectionBuilder.newBuilder(elements).asList();
    }
}
