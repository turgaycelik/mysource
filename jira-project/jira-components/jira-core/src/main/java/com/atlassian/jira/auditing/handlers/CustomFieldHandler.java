package com.atlassian.jira.auditing.handlers;

import com.atlassian.jira.auditing.AssociatedItem;
import com.atlassian.jira.auditing.AuditingCategory;
import com.atlassian.jira.auditing.ChangedValue;
import com.atlassian.jira.auditing.RecordRequest;
import com.atlassian.jira.event.issue.field.CustomFieldCreatedEvent;
import com.atlassian.jira.event.issue.field.CustomFieldDeletedEvent;
import com.atlassian.jira.event.issue.field.CustomFieldDetails;
import com.atlassian.jira.event.issue.field.CustomFieldUpdatedEvent;

import java.util.List;

/**
 * @since v6.2
 */
public class CustomFieldHandler
{
    public static RecordRequest onCustomFieldCreatedEvent(final CustomFieldCreatedEvent event)
    {
        return new RecordRequest(AuditingCategory.FIELDS, "jira.auditing.customfield.created")
                .forObject(AssociatedItem.Type.CUSTOM_FIELD, event.getCustomField().getUntranslatedName(), event.getCustomField().getId())
                .withChangedValues(buildChangedValues(event.getCustomField()));

    }

    public static RecordRequest onCustomFieldUpdatedEvent(final CustomFieldUpdatedEvent event)
    {
        return new RecordRequest(AuditingCategory.FIELDS, "jira.auditing.customfield.updated")
                .forObject(AssociatedItem.Type.CUSTOM_FIELD, event.getCustomField().getUntranslatedName(), event.getCustomField().getId())
                .withChangedValues(buildChangedValues(event.getOriginalCustomField(), event.getCustomField()));
    }

    public static RecordRequest onCustomFieldDeletedEvent(final CustomFieldDeletedEvent event)
    {
        return new RecordRequest(AuditingCategory.FIELDS, "jira.auditing.customfield.deleted")
                .forObject(AssociatedItem.Type.CUSTOM_FIELD, event.getCustomField().getUntranslatedName(), event.getCustomField().getId());

    }

    private static List<ChangedValue> buildChangedValues(final CustomFieldDetails originalCustomField, final CustomFieldDetails currentCustomField)
    {
        final ChangedValuesBuilder changedValues = new ChangedValuesBuilder();
        changedValues.addIfDifferent("common.words.name", originalCustomField == null ? null : originalCustomField.getUntranslatedName(), currentCustomField.getUntranslatedName());
        changedValues.addIfDifferent("common.words.description", originalCustomField == null ? null : originalCustomField.getUntranslatedDescription(), currentCustomField.getUntranslatedDescription());
        changedValues.addIfDifferent("common.words.type", originalCustomField == null ? null : originalCustomField.getFieldTypeName(), currentCustomField.getFieldTypeName());
        return changedValues.build();
    }

    private static List<ChangedValue> buildChangedValues(final CustomFieldDetails currentCustomField)
    {
        return buildChangedValues(null, currentCustomField);
    }
}
