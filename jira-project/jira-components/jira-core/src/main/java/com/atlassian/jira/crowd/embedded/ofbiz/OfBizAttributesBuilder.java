package com.atlassian.jira.crowd.embedded.ofbiz;

import com.atlassian.crowd.embedded.api.Attributes;
import com.atlassian.crowd.embedded.impl.ImmutableAttributes;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.ofbiz.core.entity.GenericValue;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Builds an Attributes implementation from OfBiz data.
 */
class OfBizAttributesBuilder
{
    static final String NAME = "name";
    static final String VALUE = "value";

    static final List<String> SUPPORTED_FIELDS = ImmutableList.of(NAME, VALUE);

    static Attributes toAttributes(final List<GenericValue> attributes)
    {
        if (attributes == null)
        {
            return new ImmutableAttributes();
        }
        final Map<String, Set<String>> attributesMap = Maps.newHashMapWithExpectedSize(attributes.size());
        for (final GenericValue attribute : attributes)
        {
            addAttribute(attributesMap, attribute);
        }
        return new ImmutableAttributes(attributesMap);
    }

    private static void addAttribute(final Map<String, Set<String>> attributesMap, final GenericValue attributeGv)
    {
        final String name = attributeGv.getString(NAME);
        Set<String> values = attributesMap.get(name);
        if (values == null)
        {
            values = Sets.newHashSet();
            attributesMap.put(name, values);
        }

        // Convert null attributes to empty strings, because Oracle treats them as equivalent and returns empty strings.
        final String value = attributeGv.getString(VALUE);
        values.add(value == null ? "" : value);
    }
}
