package com.atlassian.jira.plugin.jql.function;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

import com.atlassian.core.util.Clock;
import com.atlassian.jira.JiraDataTypes;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.timezone.TimeZoneManager;
import com.atlassian.jira.util.ConstantClock;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.operand.FunctionOperand;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

/**
 * @since v4.3
 */
@RunWith(MockitoJUnitRunner.class)
public abstract class AbstractDateFunctionTestCase
{
    @Mock
    protected TimeZoneManager timeZoneManager;

    private TerminalClause terminalClause;

    @After
    public void tearDown()
    {
        timeZoneManager = null;
        terminalClause = null;
    }

    @Test
    public void testValidateTooManyArguments() throws Exception
    {
        when(timeZoneManager.getLoggedInUserTimeZone()).thenReturn(TimeZone.getDefault());
        AbstractDateFunction dateFunction = getInstanceToTest();

        FunctionOperand function = new FunctionOperand(getFunctionName(), Arrays.asList("1d"));
        MessageSet messageSet = dateFunction.validate(null, function, terminalClause);
        assertFalse(messageSet.hasAnyMessages());

        function = new FunctionOperand(getFunctionName(), Collections.<String>emptyList());
        messageSet = dateFunction.validate(null, function, terminalClause);
        assertFalse(messageSet.hasAnyMessages());

        function = new FunctionOperand(getFunctionName(), Arrays.asList("should", "not", "be", "here"));
        messageSet = dateFunction.validate(null, function, terminalClause);
        assertTrue(messageSet.hasAnyMessages());
        assertTrue(messageSet.hasAnyErrors());
        assertFalse(messageSet.hasAnyWarnings());
        assertEquals(1, messageSet.getErrorMessages().size());
        assertEquals("Function '" + getFunctionName() + "' expected between '0' and '1' arguments but received '4'.", messageSet.getErrorMessages().iterator().next());
    }

    @Test
    public void testValidateIncrements() throws Exception
    {
        when(timeZoneManager.getLoggedInUserTimeZone()).thenReturn(TimeZone.getDefault());
        AbstractDateFunction dateFunction = getInstanceToTest();

        FunctionOperand function = new FunctionOperand(getFunctionName(), Arrays.asList("1d"));
        MessageSet messageSet = dateFunction.validate(null, function, terminalClause);
        assertFalse(messageSet.hasAnyMessages());

        function = new FunctionOperand(getFunctionName(), Arrays.asList("-1d"));
        messageSet = dateFunction.validate(null, function, terminalClause);
        assertFalse(messageSet.hasAnyMessages());

        function = new FunctionOperand(getFunctionName(), Arrays.asList("+1"));
        messageSet = dateFunction.validate(null, function, terminalClause);
        assertFalse(messageSet.hasAnyMessages());

        function = new FunctionOperand(getFunctionName(), Arrays.asList("1w"));
        messageSet = dateFunction.validate(null, function, terminalClause);
        assertFalse(messageSet.hasAnyMessages());

        function = new FunctionOperand(getFunctionName(), Arrays.asList("1m"));
        messageSet = dateFunction.validate(null, function, terminalClause);
        assertFalse(messageSet.hasAnyMessages());

        function = new FunctionOperand(getFunctionName(), Arrays.asList("1M"));
        messageSet = dateFunction.validate(null, function, terminalClause);
        assertFalse(messageSet.hasAnyMessages());

        function = new FunctionOperand(getFunctionName(), Arrays.asList("1y"));
        messageSet = dateFunction.validate(null, function, terminalClause);
        assertFalse(messageSet.hasAnyMessages());

        function = new FunctionOperand(getFunctionName(), Arrays.asList("-3w"));
        messageSet = dateFunction.validate(null, function, terminalClause);
        assertFalse(messageSet.hasAnyMessages());

        function = new FunctionOperand(getFunctionName(), Arrays.asList("+3d"));
        messageSet = dateFunction.validate(null, function, terminalClause);
        assertFalse(messageSet.hasAnyMessages());

        function = new FunctionOperand(getFunctionName(), Arrays.asList("-2778M"));
        messageSet = dateFunction.validate(null, function, terminalClause);
        assertFalse(messageSet.hasAnyMessages());

        function = new FunctionOperand(getFunctionName(), Arrays.asList("-6q"));
        messageSet = dateFunction.validate(null, function, terminalClause);
        assertTrue(messageSet.hasAnyMessages());
        assertTrue(messageSet.hasAnyErrors());
        assertFalse(messageSet.hasAnyWarnings());
        assertEquals(1, messageSet.getErrorMessages().size());
        assertEquals("Duration for function '" + getFunctionName() + "' should have the format (+/-)n(yMwdm), e.g -1M for 1 month earlier.", messageSet.getErrorMessages().iterator().next());

        function = new FunctionOperand(getFunctionName(), Arrays.asList("should", "not", "be", "here"));
        messageSet = dateFunction.validate(null, function, terminalClause);
        assertTrue(messageSet.hasAnyMessages());
        assertTrue(messageSet.hasAnyErrors());
        assertFalse(messageSet.hasAnyWarnings());
        assertEquals(1, messageSet.getErrorMessages().size());
        assertEquals("Function '" + getFunctionName() + "' expected between '0' and '1' arguments but received '4'.", messageSet.getErrorMessages().iterator().next());
    }

    @Test
    public void testDataType() throws Exception
    {
        when(timeZoneManager.getLoggedInUserTimeZone()).thenReturn(TimeZone.getDefault());
        NowFunction handler = new NowFunction(timeZoneManager);
        assertEquals(JiraDataTypes.DATE, handler.getDataType());
    }

    @Test
    public abstract void testGetValues() throws Exception;

    @Test
    public void testGetMinimumNumberOfExpectedArguments() throws Exception
    {
        when(timeZoneManager.getLoggedInUserTimeZone()).thenReturn(TimeZone.getDefault());
        AbstractDateFunction dateFunction = getInstanceToTest();
        assertEquals(0, dateFunction.getMinimumNumberOfExpectedArguments());
    }

    abstract String getFunctionName();

    abstract AbstractDateFunction getInstanceToTest();

    abstract AbstractDateFunction getInstanceToTest(Clock aClock);

    public void doTest(Calendar expected, Calendar now, TimeZone systemTimeZone) throws Exception
    {
        doTest(expected, now, systemTimeZone, Collections.<String>emptyList());
    }

    public void doTestwithIncrement(String increment, Calendar expected, Calendar now)
            throws Exception
    {
        doTest(expected, now, null, Arrays.asList(increment));
    }

    private void doTest(Calendar expected, Calendar now, TimeZone timeZone, Collection<String> functionOperandArgs)
    {
        // Test with a known date
        Clock aClock = new ConstantClock(now.getTime());

        FunctionOperand function = new FunctionOperand(getFunctionName(), functionOperandArgs);
        final List<QueryLiteral> value = getInstanceToTest(aClock).getValues(null, function, terminalClause);
        assertNotNull(value);
        assertEquals(1, value.size());

        GregorianCalendar cal = timeZone == null ? new GregorianCalendar() : new GregorianCalendar(timeZone);
        cal.setTimeInMillis(value.get(0).getLongValue().longValue());
        assertEquals(expected.get(Calendar.YEAR), cal.get(Calendar.YEAR));
        assertEquals(expected.get(Calendar.MONTH), cal.get(Calendar.MONTH));
        assertEquals(expected.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.DAY_OF_MONTH));
        assertEquals(expected.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.HOUR_OF_DAY));
        assertEquals(expected.get(Calendar.MINUTE), cal.get(Calendar.MINUTE));
        assertEquals(expected.get(Calendar.SECOND), cal.get(Calendar.SECOND));
    }

}
