package com.atlassian.jira.issue.customfields.impl;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;

import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.customfields.SortableCustomField;
import com.atlassian.jira.issue.customfields.converters.DoubleConverter;
import com.atlassian.jira.issue.customfields.manager.GenericConfigManager;
import com.atlassian.jira.issue.customfields.persistence.CustomFieldValuePersister;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.web.bean.BulkEditBean;

public class ImportIdLinkCFType extends NumberCFType implements SortableCustomField<Double>
{
    public static final String BUGZILLA_ID_TYPE = "importid";
    public static final String BUGZILLA_ID_SEARCHER = "exactnumber";
    public static final String BUGZILLA_ID_CF_NAME = "Bugzilla Id";
    private final ApplicationProperties applicationProperties;

    public ImportIdLinkCFType(final CustomFieldValuePersister customFieldValuePersister, final DoubleConverter doubleConverter, final ApplicationProperties applicationProperties, final GenericConfigManager genericConfigManager)
    {
        super(customFieldValuePersister, doubleConverter, genericConfigManager);
        this.applicationProperties = applicationProperties;
    }

    @Override
    public void updateValue(final CustomField customField, final Issue issue, final Double value)
    {
        if (value != null)
        {
            super.updateValue(customField, issue, value);
        }
    }

    @Nonnull
    @Override
    public Map<String, Object> getVelocityParameters(final Issue issue, final CustomField field, final FieldLayoutItem fieldLayoutItem)
    {
        final Map<String, Object> params = new HashMap<String, Object>();
        params.putAll(super.getVelocityParameters(issue, field, fieldLayoutItem));
        final String urlPrefix = applicationProperties.getDefaultBackedString(APKeys.IMPORT_ID_PREFIX);
        if (urlPrefix != null)
        {
            if (urlPrefix.equals(APKeys.IMPORT_ID_PREFIX_UNCONFIGURED))
            {
                params.put("unconfigured", Boolean.TRUE);
            }
            params.put("urlPrefix", urlPrefix);
        }
        return params;
    }

    // Read only - not editable
    @Override
    public String availableForBulkEdit(final BulkEditBean bulkEditBean)
    {
        return "bulk.edit.unavailable";
    }

    // This needs to be here since this is a read-only custom field, JRA-8864
    @Override
    public String getChangelogValue(final CustomField field, final Double value)
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

    @Override
    public Object accept(VisitorBase visitor)
    {
        if (visitor instanceof Visitor)
        {
            return ((Visitor) visitor).visitImportLink(this);
        }

        return super.accept(visitor);
    }

    public interface Visitor<X> extends VisitorBase<X>
    {
        X visitImportLink(ImportIdLinkCFType importIdLinkCustomFieldType);
    }
}
