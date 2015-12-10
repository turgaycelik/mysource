package com.atlassian.jira.plugin.jql.function;

import com.atlassian.core.util.Clock;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.JiraDataType;
import com.atlassian.jira.JiraDataTypes;
import com.atlassian.jira.jql.validator.NumberOfArgumentsValidator;
import com.atlassian.jira.timezone.TimeZoneManager;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.util.RealClock;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.operand.FunctionOperand;

import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Function that produces the end of the month as the value.
 *
 * @since v4.3
 */
public abstract class AbstractDateFunction extends AbstractJqlFunction
{
    protected final Clock clock;
    protected final TimeZoneManager timeZoneManager;
    private static final int MIN_EXPECTED_ARGS = 0;
    private static final int MAX_EXPECTED_ARGS = 1;
    private static final Pattern DURATION_PATTERN = Pattern.compile("([-\\+]?)(\\d+)([mhdwMy]?)");

    protected enum UNIT {YEAR, MONTH, WEEK, DAY, HOUR, MINUTE};

    AbstractDateFunction(TimeZoneManager timeZoneManager)
    {
    	this(RealClock.getInstance(), timeZoneManager);
    }
    
    AbstractDateFunction(Clock clock, TimeZoneManager timeZoneManager)
    {
    	this.clock = clock;
        this.timeZoneManager = timeZoneManager;
    }
    
	public MessageSet validate(User searcher, FunctionOperand operand, final TerminalClause terminalClause)
    {
		I18nHelper i18n = getI18n();
		final MessageSet messageSet = new NumberOfArgumentsValidator(MIN_EXPECTED_ARGS, MAX_EXPECTED_ARGS, i18n).validate(operand);

        if (operand.getArgs().size() == 1)
        {
            final String duration = operand.getArgs().get(0);
            if (!DURATION_PATTERN.matcher(duration).matches())
            {
            	messageSet.addErrorMessage(i18n.getText("jira.jql.date.function.duration.incorrect", operand.getName()));
            }
        }
        return messageSet;
    }

	protected int getDurationAmount(String duration)
	{
		Matcher matcher = DURATION_PATTERN.matcher(duration);
		try
        {
			if (matcher.matches())
			{
		        if (matcher.groupCount() > 1)
		        {
			        if (matcher.group(1).equals("+"))
			        {
				        return Integer.parseInt(matcher.group(2));
			        }
			        if (matcher.group(1).equals("-"))
			        {
				        return -Integer.parseInt(matcher.group(2));
			        }
		        }
			}
	        return Integer.parseInt(matcher.group(2));
        }
        catch (NumberFormatException e)
        {
	        // This should never happen as we have already formatted.
        	// But can when JQL calls getValues even after a validation failure
        	return 0;
        } 
		
	}

	protected int getDurationUnit(String duration)
	{
		Matcher matcher = DURATION_PATTERN.matcher(duration);
		if (matcher.matches())
		{
	        if (matcher.groupCount() > 2)
	        {
		        String unitGroup = matcher.group(3);
				if (unitGroup.equalsIgnoreCase("y"))
		        {
		        	return Calendar.YEAR;
		        } 
		        else if (unitGroup.equals("M")) 
		        {
		        	return Calendar.MONTH;
		        } 
		        else if (unitGroup.equalsIgnoreCase("w")) 
		        {
		        	return Calendar.WEEK_OF_MONTH;
		        } 
		        else if (unitGroup.equalsIgnoreCase("d")) 
		        {
		        	return Calendar.DAY_OF_MONTH;
		        } 
		        else if (unitGroup.equalsIgnoreCase("h")) 
		        {
		        	return Calendar.HOUR_OF_DAY;
		        } 
		        else if (unitGroup.equals("m")) 
		        {
		        	return Calendar.MINUTE;
		        }
	        }
		}
        return -1;
	}
	
    public int getMinimumNumberOfExpectedArguments()
    {
        return 0;
    }

    public JiraDataType getDataType()
    {
        return JiraDataTypes.DATE;
    }
}
