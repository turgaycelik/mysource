package com.atlassian.jira.issue.customfields.impl;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.customfields.converters.StringConverter;
import com.atlassian.jira.issue.customfields.manager.GenericConfigManager;
import com.atlassian.jira.issue.customfields.persistence.CustomFieldValuePersister;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.web.bean.BulkEditBean;

public class ReadOnlyCFType extends GenericTextCFType
{
    public ReadOnlyCFType(CustomFieldValuePersister customFieldValuePersister, GenericConfigManager genericConfigManager)
    {
        super(customFieldValuePersister, genericConfigManager);
    }

    /**
     * @deprecated Use {@link #ReadOnlyCFType(com.atlassian.jira.issue.customfields.persistence.CustomFieldValuePersister, com.atlassian.jira.issue.customfields.manager.GenericConfigManager)} instead. Since v5.0.
     * @param customFieldValuePersister
     * @param stringConverter
     * @param genericConfigManager
     */
    @Deprecated
    public ReadOnlyCFType(CustomFieldValuePersister customFieldValuePersister, StringConverter stringConverter, GenericConfigManager genericConfigManager)
    {
        super(customFieldValuePersister, genericConfigManager);
    }
    
    public void updateValue(CustomField customField, Issue issue, String value)
    {
       if (value != null)
       {
           super.updateValue(customField, issue, value);
       }
    }

    public String getChangelogValue(CustomField field, String value)
    {
        if (value != null)
        {
            return super.getChangelogValue(field, value);
        }
        else
        {
            return null;
        }
    }

    // Read only - not editable
    public String availableForBulkEdit(BulkEditBean bulkEditBean)
    {
        return "bulk.edit.unavailable";
    }

    @Override
    public Object accept(VisitorBase visitor)
    {
        if (visitor instanceof Visitor)
        {
            return ((Visitor) visitor).visitReadOnly(this);
        }

        return super.accept(visitor);
    }

    public interface Visitor<T> extends VisitorBase<T>
    {
        T visitReadOnly(ReadOnlyCFType readOnlyCustomFieldType);
    }
}
