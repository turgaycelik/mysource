package com.atlassian.jira.plugin.issueview;

import java.util.Collections;
import java.util.Set;


public class IssueViewFieldParamsImpl implements IssueViewFieldParams
{
    private final boolean customViewRequested;
    private final Set<String> fieldIds;
    private final Set<String> orderableFieldIds;
    private final Set<String> customFieldIds;
    private final boolean allCustomFields;

    public IssueViewFieldParamsImpl(final boolean customViewRequested, final Set<String> fieldIds, final Set<String> orderableFieldIds, final Set<String> customFieldIds, final boolean allCustomFields)
    {
        this.customViewRequested = customViewRequested;
        this.fieldIds = fieldIds;
        this.orderableFieldIds = Collections.unmodifiableSet(orderableFieldIds);
        this.customFieldIds = Collections.unmodifiableSet(customFieldIds);
        this.allCustomFields = allCustomFields;
    }

    public Set<String> getFieldIds()
    {
        return fieldIds;
    }

    public Set<String> getCustomFieldIds()
    {
        return customFieldIds;
    }

    public boolean isAllCustomFields()
    {
        return allCustomFields;
    }

    public Set<String> getOrderableFieldIds()
    {
        return orderableFieldIds;
    }

    public boolean isCustomViewRequested()
    {
        return customViewRequested;
    }

    public boolean isAnyFieldDefined()
    {
        return allCustomFields || fieldIds.size() > 0 || customFieldIds.size() > 0;
    }
}
