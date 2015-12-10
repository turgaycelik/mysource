package com.atlassian.jira.ofbiz;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * A Map wrapper that contains only Objects of types that Ofbiz supports (basically primitive wrapper objects)
 *
 * @since v3.13
 */
public class PrimitiveMap
{
    //
    // static factory methods
    //

    public static final Builder builder()
    {
        return new Builder();
    }

    public static final Map<String, ?> build(final String k1, final String v1)
    {
        return builder().add(k1, v1).toMap();
    }

    public static final Map<String, ?> build(final String k1, final Long v1)
    {
        return builder().add(k1, v1).toMap();
    }

    //
    // members
    //

    private final Map<String, Object> map;

    //
    // ctors
    //

    PrimitiveMap(final Map<String, Object> map)
    {
        this.map = Collections.unmodifiableMap(new HashMap<String, Object>(map));
    }

    //
    // methods
    //

    public Map<String, Object> toMap()
    {
        return map;
    }

    //
    // inner classes
    //

    public static class Builder
    {
        private final Map<String, Object> map = new HashMap<String, Object>();

        public Builder add(final String key, final String value)
        {
            map.put(key, value);
            return this;
        }

        public Builder add(final String key, final Long value)
        {
            map.put(key, value);
            return this;
        }

        public Map<String, Object> toMap()
        {
            return new HashMap<String, Object>(map);
        }
    }
}
