package com.atlassian.jira.issue.customfields.impl;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.atlassian.annotations.PublicSpi;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.imports.project.customfield.NoTransformationCustomFieldImporter;
import com.atlassian.jira.imports.project.customfield.ProjectCustomFieldImporter;
import com.atlassian.jira.imports.project.customfield.ProjectImportableCustomField;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.RendererManager;
import com.atlassian.jira.issue.customfields.SortableCustomField;
import com.atlassian.jira.issue.customfields.impl.rest.TextCustomFieldOperationsHandler;
import com.atlassian.jira.issue.customfields.manager.GenericConfigManager;
import com.atlassian.jira.issue.customfields.persistence.CustomFieldValuePersister;
import com.atlassian.jira.issue.customfields.persistence.PersistenceFieldType;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.rest.FieldJsonRepresentation;
import com.atlassian.jira.issue.fields.rest.FieldTypeInfo;
import com.atlassian.jira.issue.fields.rest.FieldTypeInfoContext;
import com.atlassian.jira.issue.fields.rest.RestAwareCustomFieldType;
import com.atlassian.jira.issue.fields.rest.RestCustomFieldTypeOperations;
import com.atlassian.jira.issue.fields.rest.RestFieldOperationsHandler;
import com.atlassian.jira.issue.fields.rest.json.JsonData;
import com.atlassian.jira.issue.fields.rest.json.JsonType;
import com.atlassian.jira.issue.fields.rest.json.JsonTypeBuilder;

import org.apache.commons.lang.StringUtils;

/**
 * A CustomFieldType where data is <em>stored</em> and <em>displayed</em> as a single String
 * By default it will only have a Limited Text Field for storage (if you need bigger override {@link #getDatabaseType()})
 *
 * <dl>
 * <dt><strong>Transport Object Type</strong></dt>
 * <dd>{@link String}</dd>
 * <dt><Strong>Database Storage Type</Strong></dt>
 * <dd>{@link String}</dd>
 * </dl>
 * @since v5.0
 */
@PublicSpi
public class GenericTextCFType extends AbstractSingleFieldType<String> implements SortableCustomField<String>, ProjectImportableCustomField, RestAwareCustomFieldType, RestCustomFieldTypeOperations
{
    private ProjectCustomFieldImporter projectCustomFieldImporter;

    protected GenericTextCFType(CustomFieldValuePersister customFieldValuePersister, GenericConfigManager genericConfigManager)
    {
        super(customFieldValuePersister, genericConfigManager);
        projectCustomFieldImporter = new NoTransformationCustomFieldImporter();
    }

    @Override
    protected Object getDbValueFromObject(final String customFieldObject)
    {
        return getStringFromSingularObject(customFieldObject);
    }

    @Override
    protected String getObjectFromDbValue(@Nonnull final Object databaseValue) throws FieldValidationException
    {
        return getSingularObjectFromString((String) databaseValue);
    }

    @Override
    public String getStringFromSingularObject(final String value)
    {
        // convert null to empty string
        return StringUtils.defaultString(value);
    }

    @Override
    public String getSingularObjectFromString(final String string) throws FieldValidationException
    {
        return string;
    }

    @Nonnull
    @Override
    protected PersistenceFieldType getDatabaseType()
    {
        return PersistenceFieldType.TYPE_LIMITED_TEXT;
    }

    @Override
    public int compare(@Nonnull final String customFieldObjectValue1, @Nonnull final String customFieldObjectValue2, final FieldConfig fieldConfig)
    {
        return customFieldObjectValue1.compareTo(customFieldObjectValue2);
    }

    @Override
    public ProjectCustomFieldImporter getProjectImporter()
    {
        return projectCustomFieldImporter;
    }

    @Override
    public Object accept(VisitorBase visitor)
    {
        if (visitor instanceof Visitor)
        {
            return ((Visitor) visitor).visitString(this);
        }

        return super.accept(visitor);
    }

    public interface Visitor<T> extends VisitorBase<T>
    {
        T visitString(GenericTextCFType stringCustomFieldType);
    }

    @Override
    public FieldTypeInfo getFieldTypeInfo(FieldTypeInfoContext fieldTypeInfoContext)
    {
        return new FieldTypeInfo(null, null);
    }

    @Override
    public JsonType getJsonSchema(CustomField customField)
    {
        return JsonTypeBuilder.custom(JsonType.STRING_TYPE, getKey(), customField.getIdAsLong());
    }

    @Override
    public FieldJsonRepresentation getJsonFromIssue(CustomField field, Issue issue, boolean renderedVersionRequested, @Nullable FieldLayoutItem fieldLayoutItem)
    {
        String value = getValueFromIssue(field, issue);
        FieldJsonRepresentation bean = new FieldJsonRepresentation(new JsonData(value));

        if (field.isRenderable() && renderedVersionRequested && fieldLayoutItem != null)
        {
            final String content = ComponentAccessor.getComponent(RendererManager.class).getRenderedContent(fieldLayoutItem, issue);
            bean.setRenderedData(new JsonData(content));
        }

        return bean;
    }

    @Override
    public RestFieldOperationsHandler getRestFieldOperation(CustomField field)
    {
        return new TextCustomFieldOperationsHandler(field, getI18nBean());
    }
}
