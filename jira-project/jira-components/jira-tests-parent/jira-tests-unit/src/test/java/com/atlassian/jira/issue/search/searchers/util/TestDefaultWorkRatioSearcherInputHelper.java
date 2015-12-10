package com.atlassian.jira.issue.search.searchers.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.JiraDataTypes;
import com.atlassian.jira.issue.search.constants.SimpleFieldSearchConstants;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.validator.MockJqlOperandResolver;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.user.MockUser;
import com.atlassian.query.clause.AndClause;
import com.atlassian.query.clause.Clause;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operand.EmptyOperand;
import com.atlassian.query.operand.MultiValueOperand;
import com.atlassian.query.operand.Operand;
import com.atlassian.query.operand.SingleValueOperand;
import com.atlassian.query.operator.Operator;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * @since v4.0
 */
public class TestDefaultWorkRatioSearcherInputHelper extends MockControllerTestCase
{
    private static final User USER = new MockUser("test");

    @Test
    public void testNullQuery() throws Exception
    {
        SimpleFieldSearchConstants constants = new SimpleFieldSearchConstants("test", Collections.<Operator>emptySet(), JiraDataTypes.NUMBER);
        JqlOperandResolver operandResolver = mockController.getMock(JqlOperandResolver.class);
        final DefaultWorkRatioSearcherInputHelper helper = new DefaultWorkRatioSearcherInputHelper(constants, operandResolver);

        mockController.replay();

        assertInvalidResult(helper, null);

        mockController.verify();
    }

    @Test
    public void testStructureCheckFails() throws Exception
    {
        final Long fromInput = 30L;
        final String fieldId = "test";

        final TerminalClauseImpl clause = new TerminalClauseImpl(fieldId, Operator.GREATER_THAN_EQUALS, fromInput);

        SimpleFieldSearchConstants constants = new SimpleFieldSearchConstants(fieldId, Collections.<Operator>emptySet(), JiraDataTypes.NUMBER);
        JqlOperandResolver operandResolver = mockController.getMock(JqlOperandResolver.class);
        final DefaultWorkRatioSearcherInputHelper helper = new DefaultWorkRatioSearcherInputHelper(constants, operandResolver)
        {
            @Override
            List<TerminalClause> validateClauseStructure(final Clause clause)
            {
                return null;
            }
        };

        mockController.replay();

        assertInvalidResult(helper, clause);

        mockController.verify();
    }

    /**
     * Check that the query 'workratio >= 30' is converted correctly.
     */
    @Test
    public void testMinimum()
    {
        final Long input = 30L;
        final String fieldId = "workratio";

        final TerminalClauseImpl clause = new TerminalClauseImpl(fieldId, Operator.GREATER_THAN_EQUALS, input);

        Map<String, String> expectedHolder = new HashMap<String, String>();
        expectedHolder.put("workratio:min", "30");

        DefaultWorkRatioSearcherInputHelper helper = createHelper(fieldId);
        assertValidResult(helper, clause, expectedHolder);

        mockController.verify();
    }

    /**
     * Check that the query 'workratio >= 30' is converted correctly.
     */
    @Test
    public void testMaximum()
    {
        final Long input = 30L;
        final String fieldId = "workratio";

        final TerminalClauseImpl clause = new TerminalClauseImpl(fieldId, Operator.LESS_THAN_EQUALS, input);

        Map<String, String> expectedHolder = new HashMap<String, String>();
        expectedHolder.put("workratio:max", "30");

        DefaultWorkRatioSearcherInputHelper helper = createHelper(fieldId);
        assertValidResult(helper, clause, expectedHolder);

        mockController.verify();
    }

    /**
     * Check that the query 'workratio <= 1000 and workratio >= "500"' is converted correctly.
     */
    @Test
    public void testMinimumAndMaximum()
    {
        final Long maxInput = 1000L;
        final String maxOutput = "1000";

        final String minInput = "500";
        final String minOutput = "500";

        final String fieldId = "workratio";
        final AndClause clause = new AndClause(
                new TerminalClauseImpl(fieldId, Operator.LESS_THAN_EQUALS, maxInput),
                new TerminalClauseImpl(fieldId, Operator.GREATER_THAN_EQUALS, minInput)
        );

        Map<String, String> expectedHolder = new HashMap<String, String>();
        expectedHolder.put("workratio:min", minOutput);
        expectedHolder.put("workratio:max", maxOutput);

        DefaultWorkRatioSearcherInputHelper helper = createHelper(fieldId);
        assertValidResult(helper, clause, expectedHolder);

        mockController.verify();
    }

    @Test
    public void testTooManyMinimums()
    {
        final Long minInput1 = 10L;
        final Long minInput2 = 20L;

        final String fieldId = "workratio";
        final AndClause clause = new AndClause(
                new TerminalClauseImpl(fieldId, Operator.GREATER_THAN_EQUALS, minInput1),
                new TerminalClauseImpl(fieldId, Operator.GREATER_THAN_EQUALS, minInput2),
                new TerminalClauseImpl(fieldId, Operator.LESS_THAN_EQUALS, "44")
        );

        DefaultWorkRatioSearcherInputHelper helper = createHelper(fieldId);
        assertInvalidResult(helper, clause);

        mockController.verify();
    }

    @Test
    public void testTooManyMaximums()
    {
        final Long maxInput1 = 10L;
        final Long maxInput2 = 20L;

        final String fieldId = "workratio";
        final AndClause clause = new AndClause(
                new TerminalClauseImpl(fieldId, Operator.LESS_THAN_EQUALS, maxInput1),
                new TerminalClauseImpl(fieldId, Operator.LESS_THAN_EQUALS, maxInput2),
                new TerminalClauseImpl(fieldId, Operator.GREATER_THAN_EQUALS, "44")
        );

        DefaultWorkRatioSearcherInputHelper helper = createHelper(fieldId);
        assertInvalidResult(helper, clause);

        mockController.verify();
    }

    /**
     * Check that the query with a bad operand (i.e. no value) does not compute.
     */
    @Test
    public void testBadLiteral()
    {
        final Operand operand = new SingleValueOperand("xxx");
        final String fieldId = "field1";
        final TerminalClauseImpl clause = new TerminalClauseImpl(fieldId, Operator.LESS_THAN_EQUALS, operand);

        final JqlOperandResolver jqlOperandResolver = mockController.getMock(JqlOperandResolver.class);
        jqlOperandResolver.isEmptyOperand(operand);
        mockController.setReturnValue(false);
        jqlOperandResolver.getValues(USER, operand, clause);
        mockController.setReturnValue(Collections.singletonList(new QueryLiteral(new SingleValueOperand("blarg"), (String)null)));

        DefaultWorkRatioSearcherInputHelper helper = createHelper(fieldId);
        assertInvalidResult(helper, clause);

        mockController.verify();
    }

    /**
     * Check that the query with a bad operand (i.e. no value) does not compute.
     */
    @Test
    public void testEmptyLiteral()
    {
        final Operand operand = new SingleValueOperand("xxx");
        final String fieldId = "field1";
        final TerminalClauseImpl clause = new TerminalClauseImpl(fieldId, Operator.LESS_THAN_EQUALS, operand);

        final JqlOperandResolver jqlOperandResolver = mockController.getMock(JqlOperandResolver.class);
        jqlOperandResolver.isEmptyOperand(operand);
        mockController.setReturnValue(false);
        jqlOperandResolver.getValues(USER, operand, clause);
        mockController.setReturnValue(Collections.singletonList(new QueryLiteral()));

        DefaultWorkRatioSearcherInputHelper helper = createHelper(fieldId);
        assertInvalidResult(helper, clause);

        mockController.verify();
    }

    /**
     * Check that the query with a bad operand (i.e. no value) does not compute.
     */
    @Test
    public void testEmptyOperand()
    {
        final Operand operand = new EmptyOperand();
        final String fieldId = "field1";
        final TerminalClauseImpl clause = new TerminalClauseImpl(fieldId, Operator.LESS_THAN_EQUALS, operand);

        final JqlOperandResolver jqlOperandResolver = mockController.getMock(JqlOperandResolver.class);
        jqlOperandResolver.isEmptyOperand(operand);
        mockController.setReturnValue(true);

        DefaultWorkRatioSearcherInputHelper helper = createHelper(fieldId);
        assertInvalidResult(helper, clause);

        mockController.verify();
    }

    /**
     * Check that the query with a bad ratio value still computes.
     */
    @Test
    public void testInvalidRatio()
    {
        final String badRatio = "13/13/2008";

        final String fieldId = "field1";
        final TerminalClauseImpl clause = new TerminalClauseImpl(fieldId, Operator.LESS_THAN_EQUALS, badRatio);

        Map<String, String> expectedHolder = new HashMap<String, String>();
        expectedHolder.put("field1:max", badRatio);

        DefaultWorkRatioSearcherInputHelper helper = createHelper(fieldId);
        assertValidResult(helper, clause, expectedHolder);

        mockController.verify();
    }

    /**
     * Test for when there is no work ratio clauses.
     */
    @Test
    public void testNoWorkRatio()
    {
        final String fieldId = "notTestName";
        final TerminalClauseImpl clause = new TerminalClauseImpl(fieldId, Operator.EQUALS, "something");

        DefaultWorkRatioSearcherInputHelper helper = createHelper(fieldId + "NOT");
        assertInvalidResult(helper, clause);

        mockController.verify();
    }

    /**
     * Test what happens when the operand returns multiple ratios. We don't support this.
     */
    @Test
    public void testTooManyRatios()
    {
        final String fieldId = "created2";
        final TerminalClauseImpl clause = new TerminalClauseImpl(fieldId, Operator.LESS_THAN_EQUALS, new MultiValueOperand("something", "more"));

        DefaultWorkRatioSearcherInputHelper helper = createHelper(fieldId);
        assertInvalidResult(helper, clause);

        mockController.verify();
    }

    private DefaultWorkRatioSearcherInputHelper createHelper(final String fieldId)
    {
        mockController.addObjectInstance(MockJqlOperandResolver.createSimpleSupport());
        mockController.addObjectInstance(new SimpleFieldSearchConstants(fieldId, Collections.<Operator>emptySet(), JiraDataTypes.NUMBER));

        return mockController.instantiate(DefaultWorkRatioSearcherInputHelper.class);
    }

    private void assertInvalidResult(DefaultWorkRatioSearcherInputHelper helper, final Clause clause)
    {
        assertNull(helper.convertClause(clause, USER));
    }

    private void assertValidResult(DefaultWorkRatioSearcherInputHelper helper, final Clause clause, Map<String, String> expectedValues)
    {
        final Map<String, String> actualResult = helper.convertClause(clause, null);
        assertNotNull(actualResult);
        assertEquals(expectedValues, actualResult);
    }
}
