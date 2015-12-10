package com.atlassian.jira.plugin.jql.function;

import com.atlassian.core.util.Clock;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.query.QueryCreationContext;
import com.atlassian.jira.timezone.TimeZoneManager;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.operand.FunctionOperand;

import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * Function that produces the end of the week as the value.
 *
 * @since v4.3
 */
public class EndOfWeekFunction extends AbstractDateFunction
{
    public static final String FUNCTION_END_OF_WEEK = "endOfWeek";

    EndOfWeekFunction(final Clock clock, final TimeZoneManager timeZoneManager)
    {
        super(clock, timeZoneManager);
    }

    public EndOfWeekFunction(final TimeZoneManager timeZoneManager)
    {
        super(timeZoneManager);
    }
    
    public List<QueryLiteral> getValues(final QueryCreationContext queryCreationContext, final FunctionOperand operand, final TerminalClause terminalClause)
    {
        int lastDayOfWeek;
        Calendar cal = new GregorianCalendar();
        if (cal.getFirstDayOfWeek() == Calendar.MONDAY)
        {
            lastDayOfWeek = Calendar.SUNDAY;
        }
        else
        {
            lastDayOfWeek = Calendar.SATURDAY;
        }
        boolean hasAdjustment = operand.getArgs().size() == 1;
        int unit = 0;
        int incrementValue = 0;
        if (hasAdjustment)
        {
            final String duration = operand.getArgs().get(0);
            unit = getDurationUnit(duration);
            if (unit == -1)
            {
            	unit = Calendar.WEEK_OF_YEAR;
            }
            incrementValue = getDurationAmount(duration);
        }

        cal.setTimeZone(timeZoneManager.getLoggedInUserTimeZone());
        cal.setTime(clock.getCurrentDate());
        //Force the calendar to re-calculate date fields.
        cal.getTime();
        
        // For year, month and week we go back or forward a year or month and get the real end of that week
        // Moving by month or year is probably mostly meaningless in this context 
        if (hasAdjustment)
        {
            if (unit == Calendar.YEAR || unit == Calendar.MONTH)
            {
            	cal.add(unit, incrementValue);
                // Need to get the time to  force the calendar to compute the date properly as
                // DAY_OF_WEEK confuses things.
                cal.getTimeInMillis();
            }
        }

        cal.set(Calendar.DAY_OF_WEEK, lastDayOfWeek);
        // Need to get the time to  force the calendar to compute the date properly as
        // DAY_OF_WEEK confuses things.
        cal.getTimeInMillis();
        cal.set(Calendar.HOUR_OF_DAY, cal.getActualMaximum(Calendar.HOUR_OF_DAY));
        cal.set(Calendar.MINUTE, cal.getActualMaximum(Calendar.MINUTE));
        cal.set(Calendar.SECOND, cal.getActualMaximum(Calendar.SECOND));
        cal.set(Calendar.MILLISECOND, cal.getActualMaximum(Calendar.MILLISECOND));
        
        // For increments < week we want the to adjust from the end of the week
        if (hasAdjustment)
        {
            if (!(unit == Calendar.YEAR || unit == Calendar.MONTH))
            {
            	cal.add(unit, incrementValue);
            }
        }

        return Collections.singletonList(new QueryLiteral(operand, cal.getTimeInMillis()));
    }
}
