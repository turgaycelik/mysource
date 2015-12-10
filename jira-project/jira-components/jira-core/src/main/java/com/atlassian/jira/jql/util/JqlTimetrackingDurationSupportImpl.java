package com.atlassian.jira.jql.util;

import java.util.Locale;

import javax.annotation.Nullable;

import com.atlassian.core.util.InvalidDurationException;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.util.InjectableComponent;
import com.atlassian.jira.util.JiraDurationUtils;

import com.google.common.base.Function;

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.document.NumberTools;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * The default implementation of {@link JqlTimetrackingDurationSupport}
 *
 * @since v4.0
 */
@InjectableComponent
public class JqlTimetrackingDurationSupportImpl implements JqlTimetrackingDurationSupport
{
    private final Function<String, Long> parser;

    public JqlTimetrackingDurationSupportImpl(final JiraDurationUtils durationUtils)
    {
        parser = new DurationUtilsParser(durationUtils);
    }

    public boolean validate(final String durationString)
    {
        final String trimDuration = StringUtils.trimToNull(durationString);
        if (trimDuration != null)
        {
            final Long value = parser.apply(trimDuration);
            return value != null && value >= 0;
        }
        else
        {
            return false;
        }
    }

    public String convertToIndexValue(final QueryLiteral rawValue)
    {
        notNull("rawValue", rawValue);
        if (rawValue.getLongValue() != null)
        {
            return convertToIndexValue(rawValue.getLongValue());
        }
        else if (rawValue.getStringValue() != null)
        {
            return convertToIndexValue(rawValue.getStringValue());
        }
        return null;
    }

    /**
     * Convenience method. See {@link #convertToDuration(String)}.
     *
     * Converts the duration in minutes into its duration in seconds.
     *
     * @param durationInMinutes the duration
     * @return the duration in seconds; null if there was a problem
     */
    public Long convertToDuration(final Long durationInMinutes)
    {
        notNull("durationInMinutes", durationInMinutes);
        return convertToDuration(durationInMinutes.toString());
    }

    /**
     * Converts the formatted duration string into its duration in seconds.
     *
     * @param durationString the formatted duration string
     * @return the duration in seconds; null if there was a problem
     */
    public Long convertToDuration(final String durationString)
    {
        notNull("durationString", durationString);
        final String trimDuration = StringUtils.trimToNull(durationString);
        if (trimDuration != null)
        {
            return parser.apply(trimDuration);
        }
        else
        {
            return null;
        }
    }

    /**
     * Convenience method. See {@link #convertToIndexValue(String)}.
     *
     * Converts the specified duration into the format used by the index.
     *
     * @param durationInMinutes the duration
     * @return the index representation of the duration value in seconds
     */
    protected String convertToIndexValue(final Long durationInMinutes)
    {
        final Long l = convertToDuration(durationInMinutes);
        if (l != null)
        {
            return NumberTools.longToString(l);
        }
        return null;
    }

    /**
     * Converts the specified duration into the format used by the index.
     *
     * @param durationString the formatted duration string
     * @return the index representation of the duration value in seconds
     */
    protected String convertToIndexValue(final String durationString)
    {
        final String trimDuration = StringUtils.trimToNull(durationString);
        if (trimDuration != null)
        {
            final Long l = convertToDuration(trimDuration);
            if (l != null)
            {
                return NumberTools.longToString(l);
            }
        }
        return null;
    }

    private static class DurationUtilsParser implements Function<String, Long>
    {
        private final JiraDurationUtils durationUtils;

        public DurationUtilsParser(JiraDurationUtils durationUtils)
        {
            this.durationUtils = durationUtils;
        }

        @Override
        public Long apply(@Nullable String from)
        {
            try
            {
                return durationUtils.parseDuration(from, Locale.ENGLISH);
            }
            catch (InvalidDurationException e)
            {
                return null;
            }
            catch (NumberFormatException e)
            {
                return null;
            }
        }
    }
}
