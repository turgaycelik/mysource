package com.atlassian.jira.crowd.embedded.ofbiz;

import java.util.Map;

class GroupAttributeEntity
{
    static final String ENTITY = "GroupAttribute";
    static final String GROUP_ID = "groupId";
    static final String DIRECTORY_ID = "directoryId";
    static final String NAME = "name";
    static final String VALUE = "value";
    static final String LOWER_VALUE = "lowerValue";

    private GroupAttributeEntity()
    {}

    static Map<String, Object> getData(final Long directoryId, final Long groupId, final String name, final String value)
    {
        final PrimitiveMap.Builder data = PrimitiveMap.builder();
        data.put(DIRECTORY_ID, directoryId);
        data.put(GROUP_ID, groupId);
        data.put(NAME, name);
        data.put(VALUE, value);
        data.putCaseInsensitive(LOWER_VALUE, value);
        return data.build();
    }
}
