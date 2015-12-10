package com.atlassian.jira.imports.project.mapper;

import com.atlassian.annotations.PublicApi;
import org.apache.commons.collections.MultiMap;
import org.apache.commons.collections.map.MultiValueMap;
import org.apache.commons.lang.builder.ToStringBuilder;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * Allows you to map statuses. This is different from the other simple mappers as when we find a status is in use
 * we need to record which issue type it is in use with. 
 *
 * @since v3.13
 */
@PublicApi
public class StatusMapper extends AbstractMapper implements ProjectImportIdMapper, MapperEntityRegister
{
    private final MultiMap requiredOldIds;
    private final Map idMap;

    public StatusMapper()
    {
        requiredOldIds = MultiValueMap.decorate(new HashMap(), HashSet.class);
        idMap = new HashMap();
    }

    public Collection /*<String>*/getIssueTypeIdsForRequiredStatus(final String statusId)
    {
        return (Collection) requiredOldIds.get(statusId);
    }

    public void flagValueAsRequired(final String oldId, final String oldIssueTypeId)
    {
        if ((oldId != null) && (oldIssueTypeId != null))
        {
            super.flagValueAsRequired(oldId);
            requiredOldIds.put(oldId, oldIssueTypeId);
        }
    }

    public void registerOldValue(final String oldId, final String oldKey)
    {
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

        final StatusMapper that = (StatusMapper) o;

        if (idMap != null ? !idMap.equals(that.idMap) : that.idMap != null)
        {
            return false;
        }
        if (getValuesFromImport() != null ? !getValuesFromImport().equals(that.getValuesFromImport()) : that.getValuesFromImport() != null)
        {
            return false;
        }
        if (requiredOldIds != null ? !requiredOldIds.equals(that.requiredOldIds) : that.requiredOldIds != null)
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
        result = (requiredOldIds != null ? requiredOldIds.hashCode() : 0);
        result = 31 * result + (idMap != null ? idMap.hashCode() : 0);
        result = 31 * result + (getValuesFromImport() != null ? getValuesFromImport().hashCode() : 0);
        return result;
    }
    ///CLOVER:ON
}
