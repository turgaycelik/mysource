package com.atlassian.jira.jql.parser;

import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.collect.CollectionUtil;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static com.atlassian.jira.util.dbc.Assertions.containsNoNulls;
import static com.atlassian.jira.util.dbc.Assertions.notBlank;
import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Represents a parse error message from the JqlParser. Internally contains a i18n key, its arguments and the position
 * of the error.
 *
 * @since v4.0
 */
public final class JqlParseErrorMessage
{
    private final String key;
    private final List<?> arguments;
    private final int lineNumber;
    private final int columnNumber;

    public JqlParseErrorMessage(final String key, final int lineNumber, final int columnNumber, final Collection<?> arguments)
    {
        containsNoNulls("arguments", arguments);
        this.key = notBlank("key", key);
        this.arguments = CollectionUtil.copyAsImmutableList(arguments);
        this.lineNumber = lineNumber <= 0 ? -1 : lineNumber;
        this.columnNumber = columnNumber <= 0 ? -1 : columnNumber;
    }

    public JqlParseErrorMessage(final String key, final int lineNumber, final int columnNumber, final Object... arguments)
    {
        this(key, lineNumber, columnNumber, Arrays.asList(notNull("arguments", arguments)));
    }

    public String getKey()
    {
        return key;
    }

    public List<?> getArguments()
    {
        return arguments;
    }

    public int getLineNumber()
    {
        return lineNumber;
    }

    public int getColumnNumber()
    {
        return columnNumber;
    }

    public String getLocalizedErrorMessage(I18nHelper helper)
    {
        notNull("helper", helper);
        return helper.getText(key, arguments);
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

        final JqlParseErrorMessage that = (JqlParseErrorMessage) o;

        if (columnNumber != that.columnNumber)
        {
            return false;
        }
        if (lineNumber != that.lineNumber)
        {
            return false;
        }
        if (!arguments.equals(that.arguments))
        {
            return false;
        }
        if (!key.equals(that.key))
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = key.hashCode();
        result = 31 * result + arguments.hashCode();
        result = 31 * result + lineNumber;
        result = 31 * result + columnNumber;
        return result;
    }

    @Override
    public String toString()
    {
         return "Error<" + lineNumber + ":" + columnNumber + "> - " + key + arguments;
    }
}
