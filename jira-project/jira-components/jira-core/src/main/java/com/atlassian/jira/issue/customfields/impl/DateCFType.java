package com.atlassian.jira.issue.customfields.impl;

import java.sql.Timestamp;
import java.util.Date;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.atlassian.jira.cluster.ClusterSafe;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.datetime.DateTimeFormatter;
import com.atlassian.jira.datetime.DateTimeFormatterFactory;
import com.atlassian.jira.datetime.DateTimeStyle;
import com.atlassian.jira.imports.project.customfield.NoTransformationCustomFieldImporter;
import com.atlassian.jira.imports.project.customfield.ProjectCustomFieldImporter;
import com.atlassian.jira.imports.project.customfield.ProjectImportableCustomField;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.customfields.SortableCustomField;
import com.atlassian.jira.issue.customfields.converters.DatePickerConverter;
import com.atlassian.jira.issue.customfields.impl.rest.DateCustomFieldOperationsHandler;
import com.atlassian.jira.issue.customfields.manager.GenericConfigManager;
import com.atlassian.jira.issue.customfields.persistence.CustomFieldValuePersister;
import com.atlassian.jira.issue.customfields.persistence.PersistenceFieldType;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.DateField;
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
import com.atlassian.jira.issue.history.DateTimeFieldChangeLogHelper;
import com.atlassian.jira.rest.Dates;
import com.atlassian.jira.util.DateFieldFormat;
import com.atlassian.util.concurrent.LazyReference;

/**
 * Custom Field to allow setting of a Date
 *
 * <dl>
 * <dt><strong>Transport Object Type</strong></dt>
 * <dd>{@link Date}</dd>
 * <dt><Strong>Database Storage Type</Strong></dt>
 * <dd>{@link Timestamp}</dd>
 * </dl>
 */
public class DateCFType extends AbstractSingleFieldType<Date>
        implements SortableCustomField<Date>, ProjectImportableCustomField, DateField, RestAwareCustomFieldType, RestCustomFieldTypeOperations
{
    protected final DatePickerConverter dateConverter;
    private final DateTimeFieldChangeLogHelper dateTimeFieldChangeLogHelper;
    private final ProjectCustomFieldImporter projectCustomFieldImporter;
    private final DateFieldFormat dateFieldFormat;
    private final DateTimeFormatterFactory dateTimeFormatterFactory;
    @ClusterSafe
    private final LazyReference<DateTimeFormatter> iso8601Formatter = new LazyIso8601DateFormatter();

    public DateCFType(CustomFieldValuePersister customFieldValuePersister, DatePickerConverter dateConverter, GenericConfigManager genericConfigManager, DateTimeFieldChangeLogHelper dateTimeFieldChangeLogHelper, DateFieldFormat dateFieldFormat, DateTimeFormatterFactory dateTimeFormatterFactory)
    {
        super(customFieldValuePersister, genericConfigManager);
        this.dateConverter = dateConverter;
        this.dateTimeFieldChangeLogHelper = dateTimeFieldChangeLogHelper;
        this.projectCustomFieldImporter = new NoTransformationCustomFieldImporter();
        this.dateFieldFormat = dateFieldFormat;
        this.dateTimeFormatterFactory = dateTimeFormatterFactory;
    }

    /**
     * @deprecated since v4.4. Use {@link DateCFType #} instead.
     */
    public DateCFType(CustomFieldValuePersister customFieldValuePersister, DatePickerConverter dateConverter, GenericConfigManager genericConfigManager)
    {
      this(customFieldValuePersister, dateConverter, genericConfigManager, ComponentAccessor.getComponentOfType(DateTimeFieldChangeLogHelper.class), ComponentAccessor.getComponentOfType(DateFieldFormat.class), ComponentAccessor.getComponentOfType(DateTimeFormatterFactory.class));
    }

    @Nonnull
    protected PersistenceFieldType getDatabaseType()
    {
        return PersistenceFieldType.TYPE_DATE;
    }

    protected Object getDbValueFromObject(Date customFieldObject)
    {
        return customFieldObject;
    }

    protected Date getObjectFromDbValue(@Nonnull Object databaseValue) throws FieldValidationException
    {
        return (Timestamp) databaseValue;
    }

    @Override
    public String getChangelogString(CustomField field, Date value)
    {
        if (value == null)
            return "";
        return getStringFromSingularObject(value);
    }

    @Override
    public String getChangelogValue(CustomField field, Date value)
    {
        if (value == null)
            return "";
        return dateTimeFieldChangeLogHelper.createChangelogValueForDateField(value);
    }

    public String getStringFromSingularObject(Date customFieldObject)
    {
        return dateConverter.getString(customFieldObject);
    }

    public Date getSingularObjectFromString(String string) throws FieldValidationException
    {
        return dateConverter.getTimestamp(string);
    }

    public int compare(@Nonnull Date v1, @Nonnull Date v2, FieldConfig fieldConfig)
    {
        return v1.compareTo(v2);
    }

    public Date getDefaultValue(FieldConfig fieldConfig)
    {
        Date defaultValue = (Date) genericConfigManager.retrieve(DEFAULT_VALUE_TYPE, fieldConfig.getId().toString());
        if (isUseNow(defaultValue))
        {
            defaultValue = new Timestamp(new Date().getTime());
        }

        return defaultValue;
    }

    // -------------------------------------------------------------------------------------------------- View Helpers

    public boolean isUseNow(Date date)
    {
        return DatePickerConverter.USE_NOW_DATE.equals(date);
    }

    public boolean isUseNow(FieldConfig fieldConfig)
    {
        Date defaultValue = (Date) genericConfigManager.retrieve(DEFAULT_VALUE_TYPE, fieldConfig.getId().toString());
        return isUseNow(defaultValue);
    }

    public String getNow()
    {
        return dateConverter.getString(new Date());
    }

    public ProjectCustomFieldImporter getProjectImporter()
    {
        return this.projectCustomFieldImporter;
    }

    @Nonnull
    @Override
    public Map<String, Object> getVelocityParameters(Issue issue, CustomField field, FieldLayoutItem fieldLayoutItem)
    {
        Map<String, Object> velocityParameters = super.getVelocityParameters(issue, field, fieldLayoutItem);
        velocityParameters.put("dateFieldFormat", dateFieldFormat);
        velocityParameters.put("iso8601Formatter", iso8601Formatter.get());

        return velocityParameters;
    }

    @Override
    public Object accept(VisitorBase visitor)
    {
        if (visitor instanceof Visitor)
        {
            return ((Visitor) visitor).visitDate(this);
        }

        return super.accept(visitor);
    }

    public interface Visitor<T> extends VisitorBase<T>
    {
        T visitDate(DateCFType dateCustomFieldType);
    }

    private class LazyIso8601DateFormatter extends LazyReference<DateTimeFormatter>
    {
        @Override
        protected DateTimeFormatter create()
        {
            return dateTimeFormatterFactory.formatter().withStyle(DateTimeStyle.ISO_8601_DATE).withSystemZone();
        }
    }

    @Override
    public FieldTypeInfo getFieldTypeInfo(FieldTypeInfoContext fieldTypeInfoContext)
    {
        return new FieldTypeInfo(null, null);
    }

    @Override
    public JsonType getJsonSchema(CustomField customField)
    {
        return JsonTypeBuilder.custom(JsonType.DATE_TYPE, getKey(), customField.getIdAsLong());
    }

    @Override
    public FieldJsonRepresentation getJsonFromIssue(CustomField field, Issue issue, boolean renderedVersionRequested, @Nullable FieldLayoutItem fieldLayoutItem)
    {
        Date date = getValueFromIssue(field, issue);
        if (date == null)
        {
            return new FieldJsonRepresentation(new JsonData(null));
        }

        FieldJsonRepresentation pair = new FieldJsonRepresentation(new JsonData(Dates.asDateString(date)));
        if (renderedVersionRequested)
        {
            pair.setRenderedData(new JsonData(dateFieldFormat.format(date)));
        }
        return pair;
    }

    @Override
    public RestFieldOperationsHandler getRestFieldOperation(CustomField field)
    {
        return new DateCustomFieldOperationsHandler(field, dateFieldFormat, getI18nBean());
    }
}
