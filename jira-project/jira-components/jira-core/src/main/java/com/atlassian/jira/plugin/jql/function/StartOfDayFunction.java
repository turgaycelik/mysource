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
 * Function that produces the end of the day as the value.
 *
 * @since v4.3
 */
public class StartOfDayFunction extends AbstractDateFunction
{
    public static final String FUNCTION_START_OF_DAY = "startOfDay";

    StartOfDayFunction(final Clock clock, final TimeZoneManager timeZoneManager)
    {
        super(clock, timeZoneManager);
    }

    public StartOfDayFunction(final TimeZoneManager timeZoneManager)
    {
        super(timeZoneManager);
    }
    
    public List<QueryLiteral> getValues(final QueryCreationContext queryCreationContext, final FunctionOperand operand, final TerminalClause terminalClause)
    {
        boolean hasAdjustment = operand.getArgs().size() == 1;
        int unit = 0;
        int incrementValue = 0;
        if (hasAdjustment)
        {
            final String duration = operand.getArgs().get(0);
            unit = getDurationUnit(duration);
            if (unit == -1)
            {
            	unit = Calendar.DAY_OF_MONTH;
            }
            incrementValue = getDurationAmount(duration);
        }

        Calendar cal = new GregorianCalendar();
        cal.setTimeZone(timeZoneManager.getLoggedInUserTimeZone());
        cal.setTime(clock.getCurrentDate());
        //Force the calendar to re-calculate date fields.
        cal.getTime();

        // For year, month and week we go back or forward a year or month and get the real start of that week
        // Moving by month or year is probably mostly meaningless in this context 

        if (hasAdjustment)
        {
            if (unit == Calendar.YEAR || unit == Calendar.MONTH)
            {
            	cal.add(unit, incrementValue);
            }
        }
    	
        cal.set(Calendar.HOUR_OF_DAY, cal.getActualMinimum(Calendar.HOUR_OF_DAY));
        cal.set(Calendar.MINUTE, cal.getActualMinimum(Calendar.MINUTE));
        cal.set(Calendar.SECOND, cal.getActualMinimum(Calendar.SECOND));
        cal.set(Calendar.MILLISECOND, cal.getActualMinimum(Calendar.MILLISECOND));

        // For increments < month we want the to adjust from the start of the day
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
