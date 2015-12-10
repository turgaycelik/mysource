package com.atlassian.jira.imports.project.mapper;

import com.atlassian.annotations.PublicApi;
import org.apache.commons.collections.MultiMap;
import org.apache.commons.collections.map.MultiValueMap;
import org.apache.commons.lang.builder.ToStringBuilder;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Mapper for Custom Fields. This Mapper collects information about which custom fields are "in-use" in the backup
 * projects data. The mapper also records what the issue type is for the issue's which contain the custom field data.
 *
 * @since v3.13
 */
@PublicApi
public class CustomFieldMapper extends AbstractMapper implements ProjectImportIdMapper
{
    private final Map idMap;
    private final MultiMap issueToCustomFieldMap;
    private final MultiMap issueTypesInUse;
    private final Map<String, String> issueToIssueTypeMap;
    private final Map<String, String> issueToIssueTypeCache;
    private final Set<String> ignoredCustomFields;

    public CustomFieldMapper()
    {
        idMap = new HashMap();
        issueTypesInUse = MultiValueMap.decorate(new HashMap(), TreeSet.class);
        issueToCustomFieldMap = MultiValueMap.decorate(new HashMap(), HashSet.class);
        issueToIssueTypeMap = new HashMap<String, String>();
        issueToIssueTypeCache = new HashMap<String, String>();
        ignoredCustomFields = new HashSet<String>();
    }

    /**
     * Will return a collection of issue type ids that are in use on issues that have values for the specified
     * custom field id.
     *
     * @param customFieldId the custom field id
     * @return collection of issue type ids that are in use on issues that have values for the specified
     * custom field id
     */
    public Collection /*<String>*/getIssueTypeIdsForRequiredCustomField(final String customFieldId)
    {
        return (Collection) issueTypesInUse.get(customFieldId);
    }

    /**
     * This method is called when we parse the <CustomFieldValue> entities.
     * At this stage, we don't know what Issue Type the issue is, so we temporarily store a mapping of Issue IDs -> Custom Fields.
     * Once the whole parse is done, the {@link #registerIssueTypesInUse()} method should be called to associate issuetypes
     * to custom fields.
     *
     * @param oldCustomFieldId The old CustomFieldId from the backup XML.
     * @param oldIssueId The old issue ID from the backup XML.
     * @see #flagIssueTypeInUse(String, String)
     */
    public void flagValueAsRequired(final String oldCustomFieldId, final String oldIssueId)
    {
        if ((oldCustomFieldId != null) && (oldIssueId != null))
        {
            // register the ID with the parent class
            super.flagValueAsRequired(oldCustomFieldId);
            issueToCustomFieldMap.put(oldIssueId, oldCustomFieldId);
        }
    }

    /**
     * This method is called when we parse the <Issue> entities.  It simply populates the issueToIssueTypeCache for the
     * {@link #registerIssueTypesInUse()} method.
     *
     * @param oldIssueId The old issue ID from the backup XML.
     * @param oldIssueTypeId The old issue type ID from the backup XML.
     * @see #flagValueAsRequired(String, String)
     */
    public void flagIssueTypeInUse(final String oldIssueId, final String oldIssueTypeId)
    {
        issueToIssueTypeCache.put(oldIssueId, oldIssueTypeId);
    }

    /**
     * Associates issue types to custom fields.  This should be called after the parse of all entities is done to give
     * all the custom field values (there may be more than just <CustomFieldValue> entities) a chance to register
     * themselves.
     */
    public void registerIssueTypesInUse()
    {
        for (String oldIssueId : issueToIssueTypeCache.keySet())
        {
            // Find all the customFields associated with this issue
            // We remove the values as we go because we know we will only need them once, and we want to free up memory.
            final Collection customFields = (Collection) issueToCustomFieldMap.remove(oldIssueId);
            if (customFields != null)
            {
                for (final Object customField : customFields)
                {
                    final String customFieldId = (String) customField;
                    // register that this custom field uses the given issue type.
                    final String oldIssueTypeId = issueToIssueTypeCache.get(oldIssueId);
                    issueTypesInUse.put(customFieldId, oldIssueTypeId);
                    // Also store a map to remember the Issue Type for each Issue.
                    issueToIssueTypeMap.put(oldIssueId, oldIssueTypeId);
                }
            }
        }
        //clear the cache...it's no longer needed now!
        issueToIssueTypeCache.clear();
    }

    public void registerOldValue(final String oldId, final String oldKey)
    {
        // Call through to the protected method in the super class.
        super.registerOldValue(oldId, oldKey);
    }

    /**
     * Get the issue type in use for the provided issue id.
     * @param issueId the issue id whose issue type you want to find
     * @return the "old" issue type id that is in use by the issue in the backup data, null if the issue type
     * has not been flagged as in use by a custom field value.
     * @see #flagIssueTypeInUse(String, String)
     */
    public String getIssueTypeForIssue(final String issueId)
    {
        return issueToIssueTypeMap.get(issueId);
    }

    public boolean isIgnoredCustomField(final String customFieldId)
    {
        return ignoredCustomFields.contains(customFieldId);
    }

    public void ignoreCustomField(final String customFieldId)
    {
        ignoredCustomFields.add(customFieldId);
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

        final CustomFieldMapper that = (CustomFieldMapper) o;

        if (idMap != null ? !idMap.equals(that.idMap) : that.idMap != null)
        {
            return false;
        }
        if (issueToCustomFieldMap != null ? !issueToCustomFieldMap.equals(that.issueToCustomFieldMap) : that.issueToCustomFieldMap != null)
        {
            return false;
        }
        if (issueTypesInUse != null ? !issueTypesInUse.equals(that.issueTypesInUse) : that.issueTypesInUse != null)
        {
            return false;
        }
        if (getValuesFromImport() != null ? !getValuesFromImport().equals(that.getValuesFromImport()) : that.getValuesFromImport() != null)
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
        result = (idMap != null ? idMap.hashCode() : 0);
        result = 31 * result + (getValuesFromImport() != null ? getValuesFromImport().hashCode() : 0);
        result = 31 * result + (issueToCustomFieldMap != null ? issueToCustomFieldMap.hashCode() : 0);
        result = 31 * result + (getRequiredOldIds() != null ? getRequiredOldIds().hashCode() : 0);
        result = 31 * result + (issueTypesInUse != null ? issueTypesInUse.hashCode() : 0);
        return result;
    }
    ///CLOVER:ON

}
