package com.atlassian.jira.imports.project.mapper;

import com.atlassian.annotations.Internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Abstract mapper that will manage most of the generic data for mappers.
 * Complicated mappers can extend this abstract class and provide public methods with required method signatures, which
 * then maintain their special data and pass standard data to this mapper with the protected methods.
 *
 * @since v3.13
 */
@Internal
public abstract class AbstractMapper implements ProjectImportIdMapper
{
    private final Set<String> requiredOldIds;
    private final Map<String, String> mappedIds;
    private final Map<String, String> oldValuesMap;

    public AbstractMapper()
    {
        requiredOldIds = new HashSet<String>();
        // This needs to be a concurrent hashmap because the mapping of id's is multithreaded when it is mapped
        // from a persister (as happens for issues and change group ids).
        mappedIds = new ConcurrentHashMap<String, String>();
        oldValuesMap = new HashMap<String, String>();
    }

    public Collection<String> getRequiredOldIds()
    {
        return Collections.unmodifiableSet(requiredOldIds);
    }

    public Collection<String> getRegisteredOldIds()
    {
        return Collections.unmodifiableSet(oldValuesMap.keySet());
    }

    /**
     * This is an internal method for use by Mappers extending AbstractMapper and should not be called from other classes.
     * MapperHandlers should call the public method specific to the concrete class they use.
     * @param oldId The ID of the required object from the import file.
     */
    protected void flagValueAsRequired(final String oldId)
    {
        if (oldId != null)
        {
            requiredOldIds.add(oldId);
        }
    }

    public void mapValue(final String oldId, final String newId)
    {
        if ((newId != null) && (oldId != null))
        {
            mappedIds.put(oldId, newId);
        }
    }

    public String getMappedId(final String oldId)
    {
        if (oldId == null)
        {
            return null;
        }
        return mappedIds.get(oldId);
    }

    public Collection<String> getAllMappedIds()
    {
        return mappedIds.values();
    }

    /**
     * This is an internal method for use by Mappers extending AbstractMapper and should not be called from other classes.
     * MapperHandlers should call the public method specific to the concrete class they use.
     * @param oldId The ID of the required object from the import file.
     * @param oldKey The unique key of the required object from the import file.
     */
    protected void registerOldValue(final String oldId, final String oldKey)
    {
        oldValuesMap.put(oldId, oldKey);
    }

    public String getDisplayName(final String oldId)
    {
        final String oldKey = oldValuesMap.get(oldId);
        if (oldKey == null)
        {
            return '[' + oldId + ']';
        }
        else
        {
            return oldKey;
        }
    }

    public String getKey(final String oldId)
    {
        return oldValuesMap.get(oldId);
    }

    /**
     * Returns a collection of <code>IdKeyPair<code> representing objects from the import file.
     *
     * @return a collection of <code>IdKeyPair<code> representing objects from the import file.
     * @see IdKeyPair
     * @deprecated Use getRegisteredOldIds(). Since 5.0
     */
    @Deprecated
    public Collection<IdKeyPair> getValuesFromImport()
    {
        final Collection<IdKeyPair> oldValues = new ArrayList<IdKeyPair>(oldValuesMap.size());
        for (final Map.Entry<String, String> entry : oldValuesMap.entrySet())
        {
            oldValues.add(new IdKeyPair(entry.getKey(), entry.getValue()));
        }
        return oldValues;
    }

    public void clearMappedValues()
    {
        mappedIds.clear();
    }
}
