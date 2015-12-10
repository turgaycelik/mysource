package com.atlassian.jira.issue.history;

import com.atlassian.jira.datetime.DateTimeFormatterFactory;
import com.atlassian.jira.datetime.DateTimeStyle;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.util.Date;

/**
 * @since v4.4
 */
public class DateTimeFieldChangeLogHelperImpl implements DateTimeFieldChangeLogHelper
{
    private static final Logger log = Logger.getLogger(DateTimeFieldChangeLogHelperImpl.class);
    private final DateTimeFormatterFactory dateTimeFormatterFactory;

    public DateTimeFieldChangeLogHelperImpl(DateTimeFormatterFactory dateTimeFormatterFactory)
    {
        this.dateTimeFormatterFactory = dateTimeFormatterFactory;
    }

    public String createChangelogValueForDateTimeField(Date date)
    {
        return dateTimeFormatterFactory.formatter().withStyle(DateTimeStyle.ISO_8601_DATE_TIME).format(date);
    }

    public String createChangelogValueForDateField(Date date)
    {
        return dateTimeFormatterFactory.formatter().withStyle(DateTimeStyle.ISO_8601_DATE).withSystemZone().format(date);
    }

    public String renderChangeHistoryValueDate(String dateValue, String dateStr)
    {
        try
        {
            if (StringUtils.isNotEmpty(dateValue))
            {
               Date date = dateTimeFormatterFactory.formatter().withStyle(DateTimeStyle.ISO_8601_DATE).withSystemZone().parse(dateValue);
               return dateTimeFormatterFactory.formatter().forLoggedInUser().withSystemZone().withStyle(DateTimeStyle.DATE).format(date);
            }
            return dateStr;
        }
        catch (IllegalArgumentException ex)
        {
            log.debug("Failed to parse change history date string '" + dateStr + "'. Returning unformatted string.");
            return dateStr;
        }
    }

    public String renderChangeHistoryValueDateTime(String dateTimeValue, String dateTimeString)
    {
        try
        {
            if (StringUtils.isNotEmpty(dateTimeValue))
            {
               Date date = dateTimeFormatterFactory.formatter().withStyle(DateTimeStyle.ISO_8601_DATE_TIME).parse(dateTimeValue);
               return dateTimeFormatterFactory.formatter().withStyle(DateTimeStyle.DATE_TIME_PICKER).forLoggedInUser().format(date);
            }
            return dateTimeString;
        }
        catch (IllegalArgumentException ex)
        {
            log.debug("Failed to parse change history date time string '" + dateTimeString + "'. Returning unformatted string.");
            return dateTimeString;
        }
    }
}
