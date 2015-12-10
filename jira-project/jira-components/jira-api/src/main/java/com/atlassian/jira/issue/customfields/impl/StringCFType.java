package com.atlassian.jira.issue.customfields.impl;

import com.atlassian.annotations.PublicSpi;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.RendererManager;
import com.atlassian.jira.issue.customfields.manager.GenericConfigManager;
import com.atlassian.jira.issue.customfields.persistence.CustomFieldValuePersister;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.rest.FieldJsonRepresentation;
import com.atlassian.jira.issue.fields.rest.FieldTypeInfo;
import com.atlassian.jira.issue.fields.rest.FieldTypeInfoContext;
import com.atlassian.jira.issue.fields.rest.RestAwareCustomFieldType;
import com.atlassian.jira.issue.fields.rest.json.JsonData;
import com.atlassian.jira.issue.fields.rest.json.JsonType;
import com.atlassian.jira.issue.fields.rest.json.JsonTypeBuilder;
import javax.annotation.Nonnull;

import javax.annotation.Nullable;

/**
 * @deprecated Use {@link GenericTextCFType} instead. Since v5.0.
 */
@Deprecated
@PublicSpi
public abstract class StringCFType extends AbstractSingleFieldType implements RestAwareCustomFieldType
{
    public StringCFType(final CustomFieldValuePersister customFieldValuePersister, final GenericConfigManager genericConfigManager)
    {
        super(customFieldValuePersister, genericConfigManager);
    }

    @Override
    protected Object getDbValueFromObject(final Object customFieldObject)
    {
        return getStringFromSingularObject(customFieldObject);
    }

    @Override
    protected Object getObjectFromDbValue(@Nonnull final Object databaseValue) throws FieldValidationException
    {
        return getSingularObjectFromString((String) databaseValue);
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
        T visitString(StringCFType stringCustomFieldType);
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
        Object value = getValueFromIssue(field, issue);
        FieldJsonRepresentation bean = new FieldJsonRepresentation(new JsonData(value));

        if (field.isRenderable() && renderedVersionRequested && fieldLayoutItem != null)
        {
            final String content = ComponentAccessor.getComponent(RendererManager.class).getRenderedContent(fieldLayoutItem, issue);
            bean.setRenderedData(new JsonData(content));
        }

        return bean;
    }
}
