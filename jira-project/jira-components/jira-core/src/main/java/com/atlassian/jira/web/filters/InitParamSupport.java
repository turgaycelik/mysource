package com.atlassian.jira.web.filters;

import org.apache.commons.lang.StringUtils;

import javax.servlet.FilterConfig;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Support for retrieving init parameters for filters.
 *
 * @since v4.2
 */
final class InitParamSupport
{
    static InitParamSupport required(String key)
    {
        return new InitParamSupport(key, true);
    }

    static InitParamSupport optional(String key)
    {
        return new InitParamSupport(key, false);
    }

    private final String key;
    private final boolean required;

    private InitParamSupport(final String key, final boolean required)
    {
        this.key = notNull("key", key);
        this.required = required;
    }

    String key()
    {
        return key;
    }

    String get(FilterConfig config)
    {
        String value = config.getInitParameter(key);
        if (StringUtils.isEmpty(value) && required)
        {
            throw new IllegalStateException("Required init parameter missing <" + key + ">");
        }
        return value;
    }
}
