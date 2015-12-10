package com.atlassian.jira.jql.util;

import com.atlassian.jira.util.dbc.Assertions;
import org.apache.commons.lang.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A class that parsers the Jql syntax (e.g. cf[1000]) for Custom Field identifiers.
 *
 * @since v4.0
 */
public final class JqlCustomFieldId
{
    private static final Pattern pattern = Pattern.compile("\\s*cf\\s*\\[\\s*(\\d+)\\s*\\]\\s*", Pattern.CASE_INSENSITIVE);
    private final long id;

    public JqlCustomFieldId(final long id)
    {
        Assertions.not("id", id < 0);
        this.id = id;
    }

    public long getId()
    {
        return id;
    }

    public String getJqlName()
    {
        return JqlCustomFieldId.toString(id);
    }

    @Override
    public String toString()
    {
        return getJqlName();
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        final JqlCustomFieldId that = (JqlCustomFieldId) o;

        return id == that.id;

    }

    @Override
    public int hashCode()
    {
        return (int) (id ^ (id >>> 32));
    }

    public static String toString(long id)
    {
        if (id < 0)
        {
            throw new IllegalArgumentException("id should be >= 0");
        }

        return String.format("cf[%d]", id);
    }

    public static boolean isJqlCustomFieldId(final String fieldName)
    {
        return parseId(fieldName) >= 0;
    }

    public static JqlCustomFieldId parseJqlCustomFieldId(final String fieldName)
    {
        final long fieldId = parseId(fieldName);
        if (fieldId >= 0)
        {
            return new JqlCustomFieldId(fieldId);
        }
        else
        {
            return null;
        }
    }

    public static long parseId(final String fieldName)
    {
        if (StringUtils.isBlank(fieldName))
        {
            return -1;
        }

        final Matcher matcher = pattern.matcher(fieldName.trim());
        if (matcher.matches())
        {
            try
            {
                final long longId = Long.parseLong(matcher.group(1));
                if (longId >= 0)
                {
                    return longId;
                }
            }
            catch (NumberFormatException ignored)
            {
            }
        }
        return Long.MIN_VALUE;
    }
}
