package com.atlassian.jira.crowd.embedded.ofbiz;

import org.ofbiz.core.entity.GenericValue;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyMap;

class DirectoryAttributeEntity
{
    static final String ENTITY = "DirectoryAttribute";
    static final String DIRECTORY_ID = "directoryId";
    static final String NAME = "name";
    static final String VALUE = "value";

    private DirectoryAttributeEntity()
    {}

    static Map<String, Object> getData(final Long directoryId, final String name, final String value)
    {
        final PrimitiveMap.Builder data = PrimitiveMap.builder();
        data.put(DIRECTORY_ID, directoryId);
        data.put(NAME, name);
        data.put(VALUE, value);
        return data.build();
    }

    static Map<String, String> toAttributes(final List<GenericValue> attributes)
    {
        if (attributes == null)
        {
            return emptyMap();
        }
        final Map<String, String> attributesMap = new HashMap<String, String>();
        for (final GenericValue attribute : attributes)
        {
            attributesMap.put(attribute.getString(NAME), attribute.getString(VALUE));
        }
        return Collections.unmodifiableMap(attributesMap);
    }
}
