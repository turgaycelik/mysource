package com.atlassian.jira.imports.project.mapper;

import com.atlassian.annotations.PublicApi;

import java.util.HashMap;
import java.util.Map;

/**
 * Holds the mappings for an IssueLinkType.
 * The registration of old values is extended from the standard mapper by adding the "style" value.
 * This is currently only used to mark subtasks as special.
 *
 * @since v3.13
 */
@PublicApi
public class IssueLinkTypeMapper extends AbstractMapper
{
    private final Map<String, String> idToStyleMap = new HashMap<String, String>();

    public void registerOldValue(final String oldId, final String name, final String style)
    {
        // Let the AbstractMapper take care of the name and ID.
        super.registerOldValue(oldId, name);
        // remember the style value for this ID.
        idToStyleMap.put(oldId, style);
    }

    public void flagValueAsRequired(final String oldId)
    {
        super.flagValueAsRequired(oldId);
    }

    public String getStyle(final String oldId)
    {
        return idToStyleMap.get(oldId);
    }
}
