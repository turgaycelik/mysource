package com.atlassian.jira.issue.customfields.impl;

import com.atlassian.jira.imports.project.customfield.NoTransformationCustomFieldImporter;
import com.atlassian.jira.imports.project.customfield.ProjectCustomFieldImporter;
import com.atlassian.jira.imports.project.customfield.ProjectImportableCustomField;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.customfields.SortableCustomField;
import com.atlassian.jira.issue.customfields.converters.DoubleConverter;
import com.atlassian.jira.issue.customfields.impl.rest.NumberCustomFieldOperationsHandler;
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
import javax.annotation.Nonnull;
import com.atlassian.jira.util.velocity.NumberTool;

import javax.annotation.Nullable;
import java.util.Map;

/**
 * Custom Field Type allowing storae and display of Double values
 * <dl>
 * <dt><strong>Transport Object Type</strong></dt>
 * <dd>{@link Double}</dd>
 * <dt><Strong>Database Storage Type</Strong></dt>
 * <dd>{@link Double}</dd>
 * </dl>
 */
public class NumberCFType extends AbstractSingleFieldType<Double> implements SortableCustomField<Double>, ProjectImportableCustomField, RestAwareCustomFieldType, RestCustomFieldTypeOperations
{
    private final DoubleConverter doubleConverter;
    private final ProjectCustomFieldImporter projectCustomFieldImporter;

    public NumberCFType(final CustomFieldValuePersister customFieldValuePersister, final DoubleConverter doubleConverter, final GenericConfigManager genericConfigManager)
    {
        super(customFieldValuePersister, genericConfigManager);
        this.doubleConverter = doubleConverter;
        projectCustomFieldImporter = new NoTransformationCustomFieldImporter();
    }

    @Nonnull
    @Override
    protected PersistenceFieldType getDatabaseType()
    {
        return PersistenceFieldType.TYPE_DECIMAL;
    }

    public String getStringFromSingularObject(final Double customFieldObject)
    {
        return doubleConverter.getString(customFieldObject);
    }

    public Double getSingularObjectFromString(final String string) throws FieldValidationException
    {
        return doubleConverter.getDouble(string);
    }

    @Override
    public String getChangelogValue(final CustomField field, final Double value)
    {
        if (value == null)
        {
            return "";
        }
        else
        {
            return doubleConverter.getStringForChangelog(value);
        }
    }


    public int compare(@Nonnull final Double customFieldObjectValue1, @Nonnull final Double customFieldObjectValue2, final FieldConfig fieldConfig)
    {
        return customFieldObjectValue1.compareTo(customFieldObjectValue2);
    }

    @Override
    protected Object getDbValueFromObject(final Double customFieldObject)
    {
        return customFieldObject;
    }

    @Override
    protected Double getObjectFromDbValue(@Nonnull final Object databaseValue) throws FieldValidationException
    {
        return (Double) databaseValue;
    }

    @Nonnull
    @Override
    public Map<String, Object> getVelocityParameters(final Issue issue, final CustomField field, final FieldLayoutItem fieldLayoutItem)
    {
        final Map<String, Object> map = super.getVelocityParameters(issue, field, fieldLayoutItem);
        map.put("numberTool", new NumberTool(getI18nBean().getLocale()));
        return map;
    }

    public ProjectCustomFieldImporter getProjectImporter()
    {
        return projectCustomFieldImporter;
    }

    @Override
    public Object accept(VisitorBase visitor)
    {
        if (visitor instanceof Visitor)
        {
            return ((Visitor) visitor).visitNumber(this);
        }

        return super.accept(visitor);
    }

    public interface Visitor<X> extends VisitorBase<X>
    {
        X visitNumber(NumberCFType numberCustomFieldType);
    }

    @Override
    public FieldTypeInfo getFieldTypeInfo(FieldTypeInfoContext fieldTypeInfoContext)
    {
        return new FieldTypeInfo(null, null);
    }

    @Override
    public JsonType getJsonSchema(CustomField customField)
    {
        return JsonTypeBuilder.custom(JsonType.NUMBER_TYPE, getKey(), customField.getIdAsLong());
    }

    @Override
    public FieldJsonRepresentation getJsonFromIssue(CustomField field, Issue issue, boolean renderedVersionRequested, @Nullable FieldLayoutItem fieldLayoutItem)
    {
        Double number = getValueFromIssue(field, issue);
        return new FieldJsonRepresentation(new JsonData(number));
    }

    @Override
    public RestFieldOperationsHandler getRestFieldOperation(CustomField field)
    {
        return new NumberCustomFieldOperationsHandler(field, doubleConverter, getI18nBean());
    }
}
