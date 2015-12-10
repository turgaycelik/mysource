/*
 * Created by IntelliJ IDEA.
 * User: Mike
 * Date: Oct 1, 2004
 * Time: 5:22:03 PM
 */
package com.atlassian.jira.util;

import com.atlassian.jira.datetime.LocalDate;
import com.atlassian.jira.datetime.LocalDateFactory;
import org.apache.log4j.Logger;
import org.apache.lucene.util.NumericUtils;

import java.util.Date;

/**
 * A simple utility class for our common Lucene usage methods.
 */
public class LuceneUtils
{
    private static final Logger log = Logger.getLogger(LuceneUtils.class);
    private static final String LOCALDATE_MAX_VALUE = "99999999";

    /**
     * do not construct
     */
    private LuceneUtils()
    
    {}

    /**
     * Turns a given {@link LocalDate} value into a String suitable for storing and searching in Lucene.
     * <p>
     * The date  is stored as "YYYYMMDD".  If the date is null we store "99999999"
     * which causes nulls to sort to the end.  (This is traditional JIRA behaviour)
     *
     * @param localDate the date to be converted.  May be null
     * @return a string representing the date
     */
    public static String localDateToString(LocalDate localDate)
    {
        if (localDate == null)
        {
            return LOCALDATE_MAX_VALUE;
        }
        return LocalDateFactory.toIsoBasic(localDate);
    }

    public static LocalDate stringToLocalDate(final String indexValue)
    {
        if (indexValue == null || indexValue.equals(LOCALDATE_MAX_VALUE))
        {
            return null;
        }
        return LocalDateFactory.fromIsoBasicFormat(indexValue);
    }

    /**
     * Turns a given date-time (point in time) value into a String suitable for storing and searching in Lucene.
     * <p>
     * The date-time is stored as the number of seconds.  If the date is null we store the encoded form of Long.MAX_VALUE
     * which causes nulls to sort to the end.  (This is traditional JIRA behaviour)
     *
     * @param date the date to be converted.  May be null
     * @return a string representing the number of seconds
     */
    public static String dateToString(final Date date)
    {
        if (date == null)
        {
            return  NumericUtils.longToPrefixCoded(Long.MAX_VALUE);
        }

        long seconds = date.getTime() / 1000;
        return NumericUtils.longToPrefixCoded(seconds);
    }

    public static Date stringToDate(final String s)
    {
        if (s != null)
        {
            long seconds = NumericUtils.prefixCodedToLong(s);
            if (seconds == Long.MAX_VALUE)
            {
                return null;
            }
            return new Date(seconds * 1000);
        }
        return new Date();
    }
}
