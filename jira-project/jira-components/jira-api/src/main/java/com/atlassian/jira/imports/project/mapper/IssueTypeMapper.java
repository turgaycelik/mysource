package com.atlassian.jira.imports.project.mapper;

import com.atlassian.annotations.PublicApi;
import org.apache.commons.lang.builder.ToStringBuilder;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * The issue type mapper. This is different from simple mappers because we need to keep track if an issue type is
 * defined as a sub-task issue type or not.
 *
 * @since v3.13
 */
@PublicApi
public class IssueTypeMapper extends AbstractMapper implements ProjectImportIdMapper
{
    private final Map idMap;
    private final Map oldValuesMap;
    private final Set<String> oldValuesSubTask;

    public IssueTypeMapper()
    {
        idMap = new HashMap();
        oldValuesMap = new HashMap();
        oldValuesSubTask = new HashSet<String>();
    }

    public boolean isSubTask(final String id)
    {
        return oldValuesSubTask.contains(id);
    }

    public void flagValueAsRequired(final String oldId)
    {
        super.flagValueAsRequired(oldId);
    }

    public void registerOldValue(final String oldId, final String oldKey, final boolean subTask)
    {
        // We remember if it was a subtask
        if (subTask)
        {
            oldValuesSubTask.add(oldId);
        }
        // And use the generic super class to remember the key for this id.
        super.registerOldValue(oldId, oldKey);
    }

    ///CLOVER:OFF - this is only here for testing purposes
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this);
    }

    ///CLOVER:ON

    ///CLOVER:OFF
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if ((o == null) || (getClass() != o.getClass()))
        {
            return false;
        }

        final IssueTypeMapper that = (IssueTypeMapper) o;

        if (idMap != null ? !idMap.equals(that.idMap) : that.idMap != null)
        {
            return false;
        }
        if (oldValuesMap != null ? !oldValuesMap.equals(that.oldValuesMap) : that.oldValuesMap != null)
        {
            return false;
        }
        if (getRequiredOldIds() != null ? !getRequiredOldIds().equals(that.getRequiredOldIds()) : that.getRequiredOldIds() != null)
        {
            return false;
        }

        return true;
    }

    ///CLOVER:ON

    ///CLOVER:OFF
    public int hashCode()
    {
        int result;
        result = (getRequiredOldIds() != null ? getRequiredOldIds().hashCode() : 0);
        result = 31 * result + (idMap != null ? idMap.hashCode() : 0);
        result = 31 * result + (oldValuesMap != null ? oldValuesMap.hashCode() : 0);
        return result;
    }
    ///CLOVER:ON
}
