package com.atlassian.jira.oauth.consumer;

import org.easymock.IArgumentMatcher;

import java.util.Map;

/**
 * EasyMock Matcher that matches ofbiz argment maps ignoring the 'created' field.
 *
 * @since v4.0
 */
public class OfBizMapArgsEqual implements IArgumentMatcher
{
    private final Map<String, Object> expected;

    public OfBizMapArgsEqual(Map<String, Object> expected)
    {
        this.expected = expected;
    }

    public boolean matches(final Object argument)
    {
        if (!(argument instanceof Map))
        {
            return false;
        }
        //everything has to equal except for the "created" date
        Map argMap = (Map) argument;
        if (argMap.size() != expected.size())
        {
            return false;
        }
        for (Map.Entry<String, Object> expectedEntry : expected.entrySet())
        {
            if (!argMap.containsKey(expectedEntry.getKey()))
            {
                return false;
            }
            if (expectedEntry.getKey().equals("created"))
            {
                continue;
            }

            final Object expectedValue = expectedEntry.getValue();
            final Object argValue = argMap.get(expectedEntry.getKey());
            if (expectedValue != null && argValue != null)
            {
                if (!expectedValue.equals(argValue))
                {
                    return false;
                }
            }
            else if (expectedValue == null && argValue == null)
            {
                continue;
            }
            else
            {
                return false;
            }
        }

        return true;
    }

    public void appendTo(final StringBuffer buffer)
    {
        buffer.append("eqOfBizMapArg(");
        buffer.append(expected).append(")");
    }
}