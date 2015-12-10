package com.atlassian.jira.issue.fields.layout.field.enterprise;

import com.atlassian.jira.issue.fields.layout.field.FieldConfigurationScheme;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * An Immutable object representing a FieldConfigurationScheme (aka FieldLayoutScheme).
 *
 * <p> This object was introduced for caching because the mutable cached object {@link com.atlassian.jira.issue.fields.layout.field.FieldLayoutSchemeImpl}
 * was causing performance problems due to contention on its Locks (see JRA-16870).
 *
 * @since v4.0
 */
public class ImmutableFieldConfigurationScheme implements FieldConfigurationScheme
{
    private final Map<String, Long> issueTypeToFieldLayoutMap;
    private final Set<Long> allFieldLayouts;
    private final Set<Long> allFieldLayoutsWithoutDefault;
    private final Long id;
    private final String name;
    private final String description;

    public ImmutableFieldConfigurationScheme(final GenericValue genericValue, Collection<GenericValue> fieldLayoutSchemeEntityGVs)
    {
        this.id = genericValue.getLong("id");
        this.name = genericValue.getString("name");
        this.description = genericValue.getString("description");

        // Build up the IssueType -> FieldLayoutId map
        Map<String, Long> tempMap = new HashMap<String, Long>();
        Set<Long> tempAllWithoutDefault = new HashSet<Long>();
        for (final GenericValue fieldLayoutSchemeEntityGV : fieldLayoutSchemeEntityGVs)
        {
            final String issueTypeId = fieldLayoutSchemeEntityGV.getString("issuetype");
            final Long fieldLayoutId = fieldLayoutSchemeEntityGV.getLong("fieldlayout");
            tempMap.put(issueTypeId, fieldLayoutId);
            if (issueTypeId != null)
            {
                tempAllWithoutDefault.add(fieldLayoutId);
            }
        }
        issueTypeToFieldLayoutMap = Collections.unmodifiableMap(tempMap);
        allFieldLayouts = Collections.unmodifiableSet(new HashSet<Long>(issueTypeToFieldLayoutMap.values()));
        allFieldLayoutsWithoutDefault = Collections.unmodifiableSet(tempAllWithoutDefault);
    }

    public Long getFieldLayoutId(String issueTypeId)
    {
        // JRA-18855 Use containsKey() - the value can be explicitly null
        if (issueTypeToFieldLayoutMap.containsKey(issueTypeId))
        {
            return issueTypeToFieldLayoutMap.get(issueTypeId);
        }
        else
        {
            // There is no specific entry for the given issueTypeId - use the default mapping
            return issueTypeToFieldLayoutMap.get(null);
        }
    }

    public Set<Long> getAllFieldLayoutIds(final Collection<String> allIssueTypeIds)
    {
        // We do this check since IF all the issue types have been mapped we do not really want to let the "catch-all"
        // default affect the fields overall layout (visibility).
        if (issueTypeToFieldLayoutMap.keySet().containsAll(allIssueTypeIds))
        {
            return allFieldLayoutsWithoutDefault;
        }
        return allFieldLayouts;
    }

    public Long getId()
    {
        return id;
    }

    public String getName()
    {
        return name;
    }

    public String getDescription()
    {
        return description;
    }
}
