package com.atlassian.jira.crowd.embedded.ofbiz;

import java.util.Map;

class UserAttributeEntity
{
    static final String ENTITY = "UserAttribute";
    static final String USER_ID = "userId";
    static final String DIRECTORY_ID = "directoryId";
    static final String NAME = "name";
    static final String VALUE = "value";
    static final String LOWER_VALUE = "lowerValue";

    private UserAttributeEntity()
    {}

    static Map<String, Object> getData(final Long directoryId, final Long userId, final String name, final String value)
    {
        final PrimitiveMap.Builder data = PrimitiveMap.builder();
        data.put(DIRECTORY_ID, directoryId);
        data.put(USER_ID, userId);
        data.put(NAME, name);
        data.put(VALUE, value);
        data.putCaseInsensitive(LOWER_VALUE, value);
        return data.build();
    }
}
