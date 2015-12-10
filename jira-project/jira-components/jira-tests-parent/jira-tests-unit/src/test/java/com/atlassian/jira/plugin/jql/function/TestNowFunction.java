package com.atlassian.jira.plugin.jql.function;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import com.atlassian.jira.JiraDataTypes;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.timezone.TimeZoneManager;
import com.atlassian.jira.util.ConstantClock;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.web.bean.MockI18nBean;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.operand.FunctionOperand;

import com.google.common.collect.ImmutableList;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.atlassian.jira.util.MessageSetAssert.assert1ErrorNoWarnings;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

/**
 * @since v4.0
 */
@RunWith(MockitoJUnitRunner.class)
public class TestNowFunction
{
    @Mock private TimeZoneManager timeZoneManager;

    private TerminalClause terminalClause = null;

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

        NowFunction nowFunction = new NowFunction(timeZoneManager)
        {
            @Override
            protected I18nHelper getI18n()
            {
                return new MockI18nBean();
            }
        };

        final FunctionOperand function = new FunctionOperand(NowFunction.FUNCTION_NOW,
                ImmutableList.of("should", "not", "be", "here"));
        final MessageSet messageSet = nowFunction.validate(null, function, terminalClause);
        assert1ErrorNoWarnings(messageSet, "Function 'now' expected '0' arguments but received '4'.");
    }

    @Test
    public void testDataType() throws Exception
    {
        when(timeZoneManager.getLoggedInUserTimeZone()).thenReturn(TimeZone.getDefault());
        final NowFunction handler = new NowFunction(timeZoneManager);
        assertEquals(JiraDataTypes.DATE, handler.getDataType());        
    }

    @Test
    public void testGetValues() throws Exception
    {
        when(timeZoneManager.getLoggedInUserTimeZone()).thenReturn(TimeZone.getDefault());
        final Date currentDate = new Date();
        final NowFunction nowFunction = new NowFunction(new ConstantClock(currentDate), timeZoneManager);
        final FunctionOperand function = new FunctionOperand(NowFunction.FUNCTION_NOW, Collections.<String>emptyList());
        final List<QueryLiteral> value = nowFunction.getValues(null, function, terminalClause);
        assertEquals(1, value.size());
        assertEquals(currentDate.getTime(), value.get(0).getLongValue().longValue());
        assertEquals(function, value.get(0).getSourceOperand());
    }

    @Test
    public void testGetMinimumNumberOfExpectedArguments() throws Exception
    {
        when(timeZoneManager.getLoggedInUserTimeZone()).thenReturn(TimeZone.getDefault());
        final Date currentDate = new Date();
        final NowFunction nowFunction = new NowFunction(new ConstantClock(currentDate), timeZoneManager);
        assertEquals(0, nowFunction.getMinimumNumberOfExpectedArguments());
    }
}
