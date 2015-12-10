package com.atlassian.jira.issue.customfields.persistence;

import com.atlassian.jira.issue.fields.CustomField;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface CustomFieldValuePersister
{
    void createValues(CustomField field, Long issueId, PersistenceFieldType persistenceFieldType, Collection value);
    void createValues(CustomField field, Long issueId, PersistenceFieldType persistenceFieldType, Collection values, @Nullable String parentKey);

    void updateValues(CustomField field, Long issueId, PersistenceFieldType persistenceFieldType, Collection values);
    void updateValues(CustomField field, Long issueId, PersistenceFieldType persistenceFieldType, Collection values, @Nullable String parentKey);

    /**
     * Removes a specific custom field value for the given custom field, from a given issue, of a particular type.
     * @param field the custom field.
     * @param issueId the issue.
     * @param persistenceFieldType the data type of the value
     * @param value the value to delete.
     * @return returns the set of ids of issues that were affected for some reason (should be 1, just the given issue!).
     */
    Set<Long> removeValue(CustomField field, Long issueId, PersistenceFieldType persistenceFieldType, Object value);

    List<Object> getValues(CustomField field, Long issueId, PersistenceFieldType persistenceFieldType);
    List<Object> getValues(CustomField field, Long issueId, PersistenceFieldType persistenceFieldType, String parentKey);

    /**
     * Return a set of issue ids that have a certain value.
     * This is used when you need to do a global 'swap' of a certain custom field value.
     *
     * @param field CustomField
     * @param persistenceFieldType PersistenceFieldType
     * @param value Value object
     * @return Set of Issue IDs
     */
    Set<Long> getIssueIdsWithValue(CustomField field, PersistenceFieldType persistenceFieldType, Object value);

    /**
     * Called when removing a field. Removes all the customfield values linked to the customfield
     * Id provided.
     *
     * @return issue IDs affected.
     * @param customFieldId the id of the custom field
     */
    Set<Long> removeAllValues(String customFieldId);

}
