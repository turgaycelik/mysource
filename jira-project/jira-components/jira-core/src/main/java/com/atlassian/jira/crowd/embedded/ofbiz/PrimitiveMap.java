package com.atlassian.jira.crowd.embedded.ofbiz;

import com.atlassian.crowd.embedded.impl.IdentifierUtils;
import org.apache.commons.lang.BooleanUtils;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * A Map builder that allows only values of types that Ofbiz supports (basically primitive wrapper objects)
 * and will automatically convert other known types for us.
 * <p>
 * Not thread-safe or designed for reuse as it does not copy the underlying map when {@link Builder#build() build} is called.
 */
class PrimitiveMap
{
    static Builder builder()
    {
        return new Builder();
    }

    static Map<String, Object> of(final String key, final long value)
    {
        return builder().put(key, value).build();
    }

    static Map<String, Object> of(final String key, final String value)
    {
        return builder().put(key, value).build();
    }

    static Map<String, Object> of(final String key1, final long value1, final String key2, final String value2)
    {
        return builder().put(key1, value1).put(key2, value2).build();
    }

    public static Map<String, Object> of(final String key1, final long value1, final String key2, final long value2)
    {
        return builder().put(key1, value1).put(key2, value2).build();
    }

    static Map<String, Object> caseInsensitive(final String key, final String value)
    {
        return builder().putCaseInsensitive(key, value).build();
    }

    static class Builder
    {
        final Map<String, Object> map = new HashMap<String, Object>();

        public Builder put(final String key, final String value)
        {
            map.put(key, value);
            return this;
        }

        public Builder putCaseInsensitive(final String key, final String value)
        {
            map.put(key, toLowerCase(value));
            return this;
        }

        public Builder putEmptyForNull(final String key, final String value)
        {
            map.put(key, emptyForNull(value));
            return this;
        }

        public Builder put(final String key, final Long value)
        {
            map.put(key, value);
            return this;
        }

        public Builder put(final String key, final boolean value)
        {
            map.put(key, BooleanUtils.toInteger(value));
            return this;
        }

        public Builder put(final String key, final Date date)
        {
            map.put(key, new Timestamp(date.getTime()));
            return this;
        }

        public Map<String, Object> build()
        {
            return Collections.unmodifiableMap(map);
        }
    }

    private static String emptyForNull(final String input)
    {
        return (input == null) ? "" : input;
    }

    /**
     * Converts the String parameter to lower case using the configured locale. This method is null
     * safe and will return {@code null} if the String parameter is {@code null}.
     * @param s the String to convert to lower case
     * @return the lower cased String.
     */
    private static String toLowerCase(final String s)
    {
        return s != null ? IdentifierUtils.toLowerCase(s) : null;
    }

    private PrimitiveMap()
    {}
}
