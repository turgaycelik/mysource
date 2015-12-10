package com.atlassian.jira.issue.search.searchers.util;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.customfields.converters.DateConverter;
import com.atlassian.jira.issue.customfields.converters.DateTimeConverter;
import com.atlassian.jira.issue.search.ClauseNames;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.util.JqlDateSupport;
import com.atlassian.jira.jql.validator.MockJqlOperandResolver;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.timezone.TimeZoneManager;
import com.atlassian.jira.user.MockUser;
import com.atlassian.query.clause.AndClause;
import com.atlassian.query.clause.Clause;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operand.EmptyOperand;
import com.atlassian.query.operand.FunctionOperand;
import com.atlassian.query.operand.MultiValueOperand;
import com.atlassian.query.operand.Operand;
import com.atlassian.query.operand.SingleValueOperand;
import com.atlassian.query.operator.Operator;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * Test for {@link DefaultDateSearcherInputHelper}.
 * <p/>
 * NOTE: in all these tests, the actual values (Longs, Timestamps, Date strings) are not meant to correlate to each
 * other; as long as they are consistent between calls to the helper classes.
 *
 * @since v4.0
 */
public class TestDefaultDateSearcherInputHelper extends MockControllerTestCase
{
    private static final User USER = new MockUser("test");

    private DateTimeConverter dateTimeConverter;
    private TimeZoneManager timeZoneManager;
    private String fieldName = "test";
    private ClauseNames clauseNames;

    @Before
    public void setUp() throws Exception
    {
        dateTimeConverter = mockController.getMock(DateTimeConverter.class);
        clauseNames = new ClauseNames(fieldName);
        timeZoneManager = new TimeZoneManager()
        {
            @Override
            public TimeZone getLoggedInUserTimeZone()
            {
                return TimeZone.getDefault();
            }
            @Override
            public TimeZone getTimeZoneforUser(User user)
            {
                return TimeZone.getDefault();
            }

            @Override
            public TimeZone getDefaultTimezone()
            {
                return TimeZone.getDefault();
            }
        };
        MockComponentWorker componentWorker = new MockComponentWorker();
        componentWorker.registerMock(TimeZoneManager.class, timeZoneManager);
        ComponentAccessor.initialiseWorker(componentWorker);
    }

    @Test
    public void testNullQuery() throws Exception
    {
        DateSearcherConfig config = new DateSearcherConfig("test", clauseNames, fieldName);
        JqlOperandResolver operandResolver = mockController.getMock(JqlOperandResolver.class);
        JqlDateSupport dateSupport = mockController.getMock(JqlDateSupport.class);
        DateConverter dateConverter = mockController.getMock(DateConverter.class);
        final DateSearcherInputHelper helper = new DefaultDateSearcherInputHelper(config, operandResolver, dateSupport, dateConverter, dateTimeConverter, timeZoneManager);

        mockController.replay();

        assertInvalidResult(helper, null);

        mockController.verify();
    }

    @Test
    public void testStructureCheckFails() throws Exception
    {
        final Long fromInput = 1014873287389L;
        final String fieldId = "created";

        final TerminalClauseImpl clause = new TerminalClauseImpl(fieldId, Operator.GREATER_THAN_EQUALS, fromInput);

        DateSearcherConfig config = new DateSearcherConfig("test", clauseNames, fieldName);
        JqlOperandResolver operandResolver = mockController.getMock(JqlOperandResolver.class);
        JqlDateSupport dateSupport = mockController.getMock(JqlDateSupport.class);
        DateConverter dateConverter = mockController.getMock(DateConverter.class);
        final DateSearcherInputHelper helper = new DefaultDateSearcherInputHelper(config, operandResolver, dateSupport, dateConverter, dateTimeConverter, timeZoneManager)
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
     * Check that the query 'created >= 1014873287389' is converted correctly.
     */
    @Test
    public void testAbsoluteFrom()
    {
        final Long fromInput = 1014873287389L;
        final Date fromDate = createDate(1981, 1, 12, 0, 0, 0, 0);
        final String fromOutput = "12/Jan/1981";
        final String fieldId = "created";

        final TerminalClauseImpl clause = new TerminalClauseImpl(fieldId, Operator.GREATER_THAN_EQUALS, fromInput);

        Map<String, String> expectedHolder = new HashMap<String, String>();
        expectedHolder.put("created:after", fromOutput);

        mockController.addObjectInstance(createSimpleOperandSupport());

        final JqlDateSupport support = mockController.getMock(JqlDateSupport.class);
        support.convertToDate(fromInput);
        mockController.setReturnValue(fromDate);

        final DateConverter convert = mockController.getMock(DateConverter.class);
        convert.getString(fromDate);
        mockController.setReturnValue(fromOutput);

        DateSearcherInputHelper helper = createHelper(fieldId, clause);
        assertValidResult(helper, clause, expectedHolder, true);

        mockController.verify();
    }

    /**
     * Check that the query 'created >= 1014873287389' is converted correctly.
     */
    @Test
    public void testAlternateClauseNameIsOkay()
    {
        final Long fromInput = 1014873287389L;
        final Date fromDate = createDate(1981, 1, 12, 0, 0, 0, 0);
        final String fromOutput = "12/Jan/1981";
        final String fieldId = "created";
        final String anotherFieldId = "anotherName";

        final TerminalClauseImpl clause = new TerminalClauseImpl(anotherFieldId, Operator.GREATER_THAN_EQUALS, fromInput);

        Map<String, String> expectedHolder = new HashMap<String, String>();
        expectedHolder.put("created:after", fromOutput);

        mockController.addObjectInstance(createSimpleOperandSupport());

        final JqlDateSupport support = mockController.getMock(JqlDateSupport.class);
        support.convertToDate(fromInput);
        mockController.setReturnValue(fromDate);

        final DateConverter convert = mockController.getMock(DateConverter.class);
        convert.getString(fromDate);
        mockController.setReturnValue(fromOutput);

        DateSearcherInputHelper helper = createHelper(new DateSearcherConfig(fieldId, new ClauseNames(fieldId, anotherFieldId), fieldName), clause);
        assertValidResult(helper, clause, expectedHolder, true);

        mockController.verify();
    }

    private JqlOperandResolver createSimpleOperandSupport()
    {
        return MockJqlOperandResolver.createSimpleSupport();
    }

    /**
     * Check that the query 'created <= 1014873287389' is converted correctly.
     */
    @Test
    public void testAbsoluteTo()
    {
        final Long toInput = 1014873287389L;
        final Date toDate = createDate(1981, 1, 12, 0, 0, 0, 0);
        final String toOutput = "12/Jan/1981";
        final String fieldId = "created";

        final TerminalClauseImpl clause = new TerminalClauseImpl(fieldId, Operator.LESS_THAN_EQUALS, toInput);

        Map<String, String> expectedHolder = new HashMap<String, String>();
        expectedHolder.put("created:before", toOutput);

        mockController.addObjectInstance(createSimpleOperandSupport());

        final JqlDateSupport support = mockController.getMock(JqlDateSupport.class);
        support.convertToDate(toInput);
        mockController.setReturnValue(toDate);

        final DateConverter convert = mockController.getMock(DateConverter.class);
        convert.getString(toDate);
        mockController.setReturnValue(toOutput);

        DateSearcherInputHelper helper = createHelper(fieldId, clause);
        assertValidResult(helper, clause, expectedHolder, true);

        mockController.verify();
    }

    /**
     * Check that the query 'created <= 1000 and created >= "25/12/2008"' is converted correctly.
     */
    @Test
    public void testAbsoluteFromAndTo()
    {
        final Long toInput = 1000L;
        final Date toDate = createDate(1945, 7, 3, 0, 0, 0, 0);
        final String toOutput = "3/July/1945";

        final String fromInput = "25/12/2008";
        final Date fromDate = createDate(2008, 12, 25, 0, 0, 0, 0);
        final String fromOutput = "someArbitraryString";

        final String fieldId = "created";
        final AndClause clause = new AndClause(
                new TerminalClauseImpl(fieldId, Operator.LESS_THAN_EQUALS, toInput),
                new TerminalClauseImpl(fieldId, Operator.GREATER_THAN_EQUALS, fromInput)
        );

        Map<String, String> expectedHolder = new HashMap<String, String>();
        expectedHolder.put("created:after", fromOutput);
        expectedHolder.put("created:before", toOutput);

        mockController.addObjectInstance(createSimpleOperandSupport());

        final JqlDateSupport support = mockController.getMock(JqlDateSupport.class);
        support.convertToDate(toInput);
        mockController.setReturnValue(toDate);
        support.convertToDate(fromInput, TimeZone.getDefault());
        mockController.setReturnValue(fromDate);

        final DateConverter convert = mockController.getMock(DateConverter.class);
        convert.getString(toDate);
        mockController.setReturnValue(toOutput);

        convert.getString(fromDate);
        mockController.setReturnValue(fromOutput);

        DateSearcherInputHelper helper = createHelper(fieldId, clause);
        assertValidResult(helper, clause, expectedHolder, true);

        mockController.verify();
    }

    /**
     * Check that the query 'created >= 10 and created >= 20 and created <= -3w' is converted correctly.
     */
    @Test
    public void testAbsoluteTooManyFrom()
    {
        final Long fromInput = 10L;
        final Date fromDate = createDate(1945, 7, 4, 0, 0, 0, 0);
        final String fromOutput = "4/July/1945";

        final Long fromInput2 = 20L;
        final Date fromDate2 = createDate(1981, 1, 12, 0, 0, 0, 0);
        final String fromOutput2 = "someArbitraryString";

        final String fieldId = "notCreated";
        final AndClause clause = new AndClause(
                new TerminalClauseImpl(fieldId, Operator.GREATER_THAN_EQUALS, fromInput),
                new TerminalClauseImpl(fieldId, Operator.GREATER_THAN_EQUALS, fromInput2),
                new TerminalClauseImpl(fieldId, Operator.LESS_THAN_EQUALS, "-3w")
        );

        mockController.addObjectInstance(createSimpleOperandSupport());

        final JqlDateSupport support = mockController.getMock(JqlDateSupport.class);
        support.convertToDate(fromInput);
        mockController.setReturnValue(fromDate);
        support.convertToDate(fromInput2);
        mockController.setReturnValue(fromDate2);

        final DateConverter convert = mockController.getMock(DateConverter.class);
        convert.getString(fromDate);
        mockController.setReturnValue(fromOutput);

        convert.getString(fromDate2);
        mockController.setReturnValue(fromOutput2);

        DateSearcherInputHelper helper = createHelper(fieldId, clause);
        assertInvalidResult(helper, clause);

        mockController.verify();
    }

    /**
     * Check that the query 'created <= 10001 and created <= 20002' is converted correctly.
     */
    @Test
    public void testAbsoluteTooManyTo()
    {
        final Long toInput = 10001L;
        final Date toDate = createDate(1945, 7, 3, 0, 0, 0, 0);
        final String toOutput = "3/July/1945";

        final Long toInput2 = 20002L;
        final Date toDate2 = createDate(1981, 1, 12, 0, 0, 0, 0);
        final String toOutput2 = "someArbitraryString";

        final String fieldId = "created";
        final AndClause clause = new AndClause(
                new TerminalClauseImpl(fieldId, Operator.LESS_THAN_EQUALS, toInput),
                new TerminalClauseImpl(fieldId, Operator.LESS_THAN_EQUALS, toInput2)
        );
        mockController.addObjectInstance(createSimpleOperandSupport());

        final JqlDateSupport support = mockController.getMock(JqlDateSupport.class);
        support.convertToDate(toInput);
        mockController.setReturnValue(toDate);
        support.convertToDate(toInput2);
        mockController.setReturnValue(toDate2);

        final DateConverter convert = mockController.getMock(DateConverter.class);
        convert.getString(toDate);
        mockController.setReturnValue(toOutput);

        convert.getString(toDate2);
        mockController.setReturnValue(toOutput2);

        DateSearcherInputHelper helper = createHelper(fieldId, clause);
        assertInvalidResult(helper, clause);

        mockController.verify();
    }

    /**
     * Check that the query with a bad operand (i.e. no value) does not compute.
     */
    @Test
    public void testBadLiteral()
    {
        final Operand operand = new SingleValueOperand("-67w");
        final String fieldId = "field1";
        final TerminalClauseImpl clause = new TerminalClauseImpl(fieldId, Operator.LESS_THAN_EQUALS, operand);

        final JqlOperandResolver jqlOperandResolver = mockController.getMock(JqlOperandResolver.class);
        jqlOperandResolver.isEmptyOperand(operand);
        mockController.setReturnValue(false);
        jqlOperandResolver.isFunctionOperand(operand);
        mockController.setReturnValue(false);
        jqlOperandResolver.getValues(USER, operand, clause);
        mockController.setReturnValue(Collections.singletonList(new QueryLiteral(new SingleValueOperand("blarg"), (String)null)));

        DateSearcherInputHelper helper = createHelper(fieldId, clause);
        assertInvalidResult(helper, clause);

        mockController.verify();
    }

    /**
     * Check that the query with a bad operand (i.e. no value) does not compute.
     */
    @Test
    public void testEmptyLiteral()
    {
        final Operand operand = new SingleValueOperand("-67w");
        final String fieldId = "field1";
        final TerminalClauseImpl clause = new TerminalClauseImpl(fieldId, Operator.LESS_THAN_EQUALS, operand);

        final JqlOperandResolver jqlOperandResolver = mockController.getMock(JqlOperandResolver.class);
        jqlOperandResolver.isEmptyOperand(operand);
        mockController.setReturnValue(false);
        jqlOperandResolver.isFunctionOperand(operand);
        mockController.setReturnValue(false);
        jqlOperandResolver.getValues(USER, operand, clause);
        mockController.setReturnValue(Collections.singletonList(new QueryLiteral()));

        DateSearcherInputHelper helper = createHelper(fieldId, clause);
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

        DateSearcherInputHelper helper = createHelper(fieldId, clause);
        assertInvalidResult(helper, clause);

        mockController.verify();
    }

    /**
     * Check that the query with a function operand (e.g. Now()) does not compute.
     */
    @Test
    public void testFunctionOperand()
    {
        final Operand operand = new FunctionOperand("Now");
        final String fieldId = "field1";
        final TerminalClauseImpl clause = new TerminalClauseImpl(fieldId, Operator.LESS_THAN_EQUALS, operand);

        final JqlOperandResolver jqlOperandResolver = mockController.getMock(JqlOperandResolver.class);
        jqlOperandResolver.isEmptyOperand(operand);
        mockController.setReturnValue(true);

        DateSearcherInputHelper helper = createHelper(fieldId, clause);
        assertInvalidResult(helper, clause);

        mockController.verify();
    }

    /**
     * Check that the query with a bad date still computes.
     */
    @Test
    public void testInvalidDate()
    {
        final String badDate = "13/13/2008";

        final String fieldId = "field1";
        final TerminalClauseImpl clause = new TerminalClauseImpl(fieldId, Operator.LESS_THAN_EQUALS, badDate);

        mockController.addObjectInstance(createSimpleOperandSupport());

        final JqlDateSupport support = mockController.getMock(JqlDateSupport.class);
        support.convertToDate(badDate, TimeZone.getDefault());
        mockController.setReturnValue(null);

        DateSearcherInputHelper helper = createHelper(fieldId, clause);
        Map<String, String> expectedHolder = new HashMap<String, String>();
        expectedHolder.put("field1:before", badDate);

        assertValidResult(helper, clause, expectedHolder, true);

        mockController.verify();
    }

    /**
     * Check that when the navigator cannot display the date without loss, the query cannot be handled.
     */
    @Test
    public void testNotLossy()
    {
        final String lossyInput = "2008/12/25 15:00";
        final Date lossyDate = createDate(2008, 12, 25, 15, 0, 0, 0);
        final String okDateString = "6/10/2008";
        final Date okDate = createDate(2008, 10, 6, 0, 0, 0, 0);

        final String fieldId = "field1";
        final AndClause clause = new AndClause(
                new TerminalClauseImpl(fieldId, Operator.LESS_THAN_EQUALS, lossyInput),
                new TerminalClauseImpl(fieldId, Operator.GREATER_THAN_EQUALS, okDateString));

        mockController.addObjectInstance(createSimpleOperandSupport());

        final JqlDateSupport support = mockController.getMock(JqlDateSupport.class);
        support.convertToDate(lossyInput, TimeZone.getDefault());
        mockController.setReturnValue(lossyDate);
        support.convertToDate(okDateString, TimeZone.getDefault());
        mockController.setReturnValue(okDate);

        dateTimeConverter.getString(lossyDate);
        mockController.setReturnValue("blah");

        final DateConverter dateConverter = mockController.getMock(DateConverter.class);
        dateConverter.getString(okDate);
        mockController.setReturnValue("blah2");

        Map<String, String> expectedHolder = new HashMap<String, String>();
        expectedHolder.put("field1:before", "blah");
        expectedHolder.put("field1:after", "blah2");

        DateSearcherInputHelper helper = createHelper(fieldId, clause);
        assertValidResult(helper, clause, expectedHolder, false);

        mockController.verify();
    }

    /**
     * Check that when the navigator cannot display the date without loss, the query cannot be handled.
     */
    @Test
    public void testTimeComponentOkayIfSpecified()
    {
        final String toInput = "2008/12/25 15:00";
        final Date toDate = createDate(2008, 12, 25, 15, 0, 0, 0);
        final String toOutput = "25/Dec/08";

        final String fieldId = "created";
        final TerminalClause clause = new TerminalClauseImpl(fieldId, Operator.LESS_THAN_EQUALS, toInput);

        Map<String, String> expectedHolder = new HashMap<String, String>();
        expectedHolder.put("created:before", toOutput);

        mockController.addObjectInstance(createSimpleOperandSupport());

        final JqlDateSupport support = mockController.getMock(JqlDateSupport.class);
        support.convertToDate(toInput, TimeZone.getDefault());
        mockController.setReturnValue(toDate);

        dateTimeConverter.getString(toDate);
        mockController.setReturnValue(toOutput);

        DateSearcherInputHelper helper = createHelper(fieldId, clause);
        assertValidResultLossyAllowed(helper, clause, expectedHolder);

        mockController.verify();
    }

    /**
     * Check that seconds and milliseconds are not counted in "hasTimeComponent" checks
     */
    @Test
    public void testSecondsAndMillisecondsArentCounted()
    {
        final Long toInput = 1014873287389L;
        final Date toDate = createDate(1981, 1, 12, 0, 0, 5, 66);
        final String toOutput = "12/Jan/1981";
        final String fieldId = "created";

        final TerminalClauseImpl clause = new TerminalClauseImpl(fieldId, Operator.LESS_THAN_EQUALS, toInput);

        Map<String, String> expectedHolder = new HashMap<String, String>();
        expectedHolder.put("created:before", toOutput);

        mockController.addObjectInstance(createSimpleOperandSupport());

        final JqlDateSupport support = mockController.getMock(JqlDateSupport.class);
        support.convertToDate(toInput);
        mockController.setReturnValue(toDate);
        
        dateTimeConverter.getString(toDate);
        mockController.setReturnValue(toOutput);

        DateSearcherInputHelper helper = createHelper(fieldId, clause);
        assertValidResultLossyAllowed(helper, clause, expectedHolder);

        mockController.verify();
    }

    /**
     * Test for when there is no date clauses.
     */
    @Test
    public void testNoDate()
    {
        final String fieldId = "notTestName";
        final TerminalClauseImpl clause = new TerminalClauseImpl(fieldId, Operator.EQUALS, "something");

        DateSearcherInputHelper helper = createHelper(fieldId + "NOT", clause);
        assertInvalidResult(helper, clause);

        mockController.verify();
    }

    /**
     * Test what happens when the operand returns multiple dates. We don't support this.
     */
    @Test
    public void testTooManyDates()
    {
        final String fieldId = "created2";
        final TerminalClauseImpl clause = new TerminalClauseImpl(fieldId, Operator.LESS_THAN_EQUALS, new MultiValueOperand("something", "more"));

        mockController.addObjectInstance(createSimpleOperandSupport());

        DateSearcherInputHelper helper = createHelper(fieldId, clause);
        assertInvalidResult(helper, clause);

        mockController.verify();
    }

    /**
     * Check that the query 'created <= "-3w"' is converted correctly.
     */
    @Test
    public void testRelativeTo()
    {
        final String beforeDate = "-3w";

        final String fieldId = "created";
        final TerminalClauseImpl clause = new TerminalClauseImpl(fieldId, Operator.LESS_THAN_EQUALS, beforeDate);

        Map<String, String> expectedHolder = new HashMap<String, String>();
        expectedHolder.put("created:next", beforeDate);

        mockController.addObjectInstance(createSimpleOperandSupport());

        DateSearcherInputHelper helper = createHelper(fieldId, clause);
        assertValidResult(helper, clause, expectedHolder, true);

        mockController.verify();
    }

    /**
     * Check that the query 'created >= "-2w"' is converted correctly.
     */
    @Test
    public void testRelativeFrom()
    {
        final String afterDate = "-2h";

        final String fieldId = "created";
        final TerminalClauseImpl clause = new TerminalClauseImpl(fieldId, Operator.GREATER_THAN_EQUALS, afterDate);

        Map<String, String> expectedHolder = new HashMap<String, String>();
        expectedHolder.put("created:previous", afterDate);

        mockController.addObjectInstance(createSimpleOperandSupport());

        DateSearcherInputHelper helper = createHelper(fieldId, clause);
        assertValidResult(helper, clause, expectedHolder, true);

        mockController.verify();
    }

    /**
     * Check that the query 'created >= "-2w" and created <= -2h' is converted correctly.
     */
    @Test
    public void testRelativeFromAndTo()
    {
        final String afterDate = "-2w";
        final String beforeDate = "-2h";

        final String fieldId = "created";
        final AndClause clause = new AndClause(
                new TerminalClauseImpl(fieldId, Operator.GREATER_THAN_EQUALS, afterDate),
                new TerminalClauseImpl(fieldId, Operator.LESS_THAN_EQUALS, beforeDate)
        );

        Map<String, String> expectedHolder = new HashMap<String, String>();
        expectedHolder.put("created:previous", afterDate);
        expectedHolder.put("created:next", beforeDate);

        mockController.addObjectInstance(createSimpleOperandSupport());

        DateSearcherInputHelper helper = createHelper(fieldId, clause);
        assertValidResult(helper, clause, expectedHolder, true);

        mockController.verify();
    }

    /**
     * Check that the query 'created <= "-2w" and created <= -2h' is not converted.
     */
    @Test
    public void testRelativeTooManyTo()
    {
        final String beforeDate2 = "-2w";
        final String beforeDate = "-2h";
        final String fieldId = "created";
        final AndClause clause = new AndClause(
                new TerminalClauseImpl(fieldId, Operator.LESS_THAN_EQUALS, beforeDate2),
                new TerminalClauseImpl(fieldId, Operator.LESS_THAN_EQUALS, beforeDate)
        );

        mockController.addObjectInstance(createSimpleOperandSupport());

        DateSearcherInputHelper helper = createHelper(fieldId, clause);
        assertInvalidResult(helper, clause);

        mockController.verify();
    }

    /**
     * Check that the query 'created >= "-2w" and created >= -2h and created<=-5h' is converted correctly.
     */
    @Test
    public void testRelativeTooManyFrom()
    {
        final String afterDate = "-2w";
        final String afterDate2 = "-2h";
        final String beforeDate = "-5h";

        final String fieldId = "created";
        final AndClause clause = new AndClause(
                new TerminalClauseImpl(fieldId, Operator.LESS_THAN_EQUALS, afterDate),
                new TerminalClauseImpl(fieldId, Operator.LESS_THAN_EQUALS, afterDate2),
                new TerminalClauseImpl(fieldId, Operator.GREATER_THAN_EQUALS, beforeDate)
        );

        mockController.addObjectInstance(createSimpleOperandSupport());

        DateSearcherInputHelper helper = createHelper(fieldId, clause);
        assertInvalidResult(helper, clause);

        mockController.verify();
    }

    private DateSearcherInputHelper createHelper(final String fieldId, final Clause clause)
    {
        return createHelper(new DateSearcherConfig(fieldId, new ClauseNames(fieldId), fieldName), clause);
    }

    private DateSearcherInputHelper createHelper(final DateSearcherConfig config, final Clause clause)
    {
        mockController.addObjectInstance(config);
        mockController.addObjectInstance(USER);
        mockController.addObjectInstance(clause);
        mockController.addObjectInstance(timeZoneManager);

        return mockController.instantiate(DefaultDateSearcherInputHelper.class);
    }

    private void assertInvalidResult(DateSearcherInputHelper helper, final Clause clause)
    {
        final DateSearcherInputHelper.ConvertClauseResult convertClauseResult = helper.convertClause(clause, USER, false);
        assertNull(convertClauseResult.getFields());
        assertEquals(false, convertClauseResult.fitsFilterForm());
    }

    private void assertValidResult(DateSearcherInputHelper helper, final Clause clause, Map<String, String> expectedValues, final boolean expectedFits)
    {
        final DateSearcherInputHelper.ConvertClauseResult convertClauseResult = helper.convertClause(clause, null, false);
        assertNotNull(convertClauseResult.getFields());
        assertEquals(expectedValues, convertClauseResult.getFields());
        assertEquals(expectedFits, convertClauseResult.fitsFilterForm());
    }

    private void assertValidResultLossyAllowed(DateSearcherInputHelper helper, final Clause clause, Map<String, String> expectedValues)
    {
        final DateSearcherInputHelper.ConvertClauseResult convertClauseResult = helper.convertClause(clause, null, true);
        assertNotNull(convertClauseResult.getFields());
        assertEquals(expectedValues, convertClauseResult.getFields());
        assertEquals(true, convertClauseResult.fitsFilterForm());
    }

    private Date createDate(int year, int month, int day, int hour, int minute, int second, int millisecond)
    {
        Calendar cal = Calendar.getInstance();
        cal.set(year, month - 1, day, hour, minute, second);
        cal.set(Calendar.MILLISECOND, millisecond);
        return cal.getTime();
    }
}
