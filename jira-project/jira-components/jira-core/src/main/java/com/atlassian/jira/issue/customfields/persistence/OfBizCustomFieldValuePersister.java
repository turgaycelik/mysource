package com.atlassian.jira.issue.customfields.persistence;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.atlassian.jira.issue.customfields.CustomFieldUtils;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.util.dbc.Assertions;

import org.ofbiz.core.entity.GenericValue;

public class OfBizCustomFieldValuePersister implements CustomFieldValuePersister
{
    protected final OfBizDelegator delegator;

    public static final String ENTITY_VALUE_TYPE = "valuetype";
    protected static final String ENTITY_ISSUE_ID = "issue";
    protected static final String ENTITY_CUSTOMFIELD_ID = "customfield";
    protected static final String ENTITY_PARENT_KEY = "parentkey";

    //defaults are stored in the same table as values, but with a type of 'DEFAULT'
    public static final String DEFAULT_VALUE_TYPE = "DEFAULT";

    //column names in the database
    public static final String FIELD_TYPE_STRING = "stringvalue"; //typically 255 characters
    public static final String FIELD_TYPE_TEXT = "textvalue";     //4k or unlimited characters
    public static final String FIELD_TYPE_DATE = "datevalue";     //a timestamp
    public static final String FIELD_TYPE_NUMBER = "numbervalue"; //a double

    private static final Map<PersistenceFieldType, String> DB_FIELD_MAPPING = new HashMap<PersistenceFieldType, String>();

    static
    {
        DB_FIELD_MAPPING.put(PersistenceFieldType.TYPE_DATE, FIELD_TYPE_DATE);
        DB_FIELD_MAPPING.put(PersistenceFieldType.TYPE_DECIMAL, FIELD_TYPE_NUMBER);
        DB_FIELD_MAPPING.put(PersistenceFieldType.TYPE_LIMITED_TEXT, FIELD_TYPE_STRING);
        DB_FIELD_MAPPING.put(PersistenceFieldType.TYPE_UNLIMITED_TEXT, FIELD_TYPE_TEXT);
    }

    protected static final String TABLE_CUSTOMFIELD_VALUE = "CustomFieldValue";
    public static final Long DEFAULT_VALUE_ISSUE_ID = -1L;

    public OfBizCustomFieldValuePersister(OfBizDelegator delegator)
    {
        this.delegator = delegator;
    }


    public List<Object> getValues(CustomField field, Long issueId, PersistenceFieldType persistenceFieldType)
    {
        return getValues(field, issueId, persistenceFieldType, null);
    }

    public List<Object> getValues(CustomField field, Long issueId, PersistenceFieldType persistenceFieldType, String parentKey)
    {
        // JRA-29824 During issue create, we can get passed a null issueId which can cause bad performance for DBs that don't index null values
        if (issueId == null)
            return Collections.emptyList();
        List<GenericValue> genericValues = getValuesForTypeAndParent(field, issueId, parentKey);
        return getValuesFromGenericValues(genericValues, persistenceFieldType);
    }

    public Set<Long> getIssueIdsWithValue(CustomField field, PersistenceFieldType persistenceFieldType, Object value)
    {
        Map<String, Object> limitClause = new HashMap<String, Object>();
        limitClause.put(ENTITY_VALUE_TYPE, null);
        limitClause.put(ENTITY_CUSTOMFIELD_ID, CustomFieldUtils.getCustomFieldId(field.getId()));
        limitClause.put(getColumnName(persistenceFieldType), value);

        List<GenericValue> genericValues = delegator.findByAnd(TABLE_CUSTOMFIELD_VALUE, limitClause);

        Set<Long> ids = new HashSet<Long>();
        for (GenericValue genericValue : genericValues)
        {
            ids.add(genericValue.getLong(ENTITY_ISSUE_ID));
        }
        return ids;
    }

    private static List<Object> getValuesFromGenericValues(List<GenericValue> genericValues, PersistenceFieldType persistenceFieldType)
    {
        List<Object> values = new ArrayList<Object>(genericValues.size());
        for (GenericValue genericValue : genericValues)
        {
            values.add(genericValue.get(getColumnName(persistenceFieldType)));
        }
        return values;
    }

    public void createValues(CustomField field, Long issueId, PersistenceFieldType persistenceFieldType, Collection values)
    {
        createValues(field, issueId, persistenceFieldType, values, null);
    }

    public void createValues(CustomField field, Long issueId, PersistenceFieldType persistenceFieldType, Collection values, String parentKey)
    {
        // This needs to be an update call instead of a create alone. This is so that all the values will be removed
        // first so that if the value has been corrupted by concurrent updates the user can update the value through
        // the UI.
        updateValues(field, issueId, persistenceFieldType, values, parentKey);
    }

    private void createValuesInt(CustomField field, Long issueId, PersistenceFieldType persistenceFieldType, Collection values, String parentKey)
    {
        if (values == null)
            return;

        for (Object value : values)
        {
            if (value == null || "".equals(value) || "-1".equals(value))
            {
                continue;
            } // we don't create null values in the database

            Map<String, Object> entityFields = new HashMap<String, Object>();
            entityFields.put(ENTITY_ISSUE_ID, issueId);
            entityFields.put(ENTITY_PARENT_KEY, parentKey);
            entityFields.put(ENTITY_CUSTOMFIELD_ID, CustomFieldUtils.getCustomFieldId(field.getId()));
            entityFields.put(getColumnName(persistenceFieldType), value);
            delegator.createValue(TABLE_CUSTOMFIELD_VALUE, entityFields);
        }
    }

    public void updateValues(CustomField field, Long issueId, PersistenceFieldType persistenceFieldType, Collection values)
    {
        final List<GenericValue> GVs = getValuesForType(field, issueId);
        delegator.removeAll(GVs);
        if (values != null && !values.isEmpty())
        {
            createValuesInt(field, issueId, persistenceFieldType, values, null);
        }
    }

    public void updateValues(CustomField field, Long issueId, PersistenceFieldType persistenceFieldType, Collection values, String parentKey)
    {
        final List<GenericValue> GVs = getValuesForTypeAndParent(field, issueId, parentKey);
        delegator.removeAll(GVs);
        if (values != null && !values.isEmpty())
        {
            createValuesInt(field, issueId, persistenceFieldType, values, parentKey);
        }
    }

    public Set<Long> removeValue(CustomField field, Long issueId, PersistenceFieldType persistenceFieldType, Object value)
    {
        final Map<String, Object> limitClause =
               MapBuilder.build(ENTITY_CUSTOMFIELD_ID, CustomFieldUtils.getCustomFieldId(field.getId()),
               ENTITY_ISSUE_ID, issueId,
               getColumnName(persistenceFieldType), value);

        List<GenericValue> values = delegator.findByAnd(TABLE_CUSTOMFIELD_VALUE, limitClause);

        Set<Long> issues = new HashSet<Long>();
        List<GenericValue> deleteThese = new ArrayList<GenericValue>();
        for (GenericValue valueGV : values)
        {
            issues.add(valueGV.getLong(ENTITY_ISSUE_ID));
            deleteThese.add(valueGV);
        }
        delegator.removeAll(deleteThese);
        return issues;
    }

    public Set<Long> removeAllValues(String customFieldId)
    {
        Assertions.notNull("customFieldId", customFieldId);

        final Map<String, Object> limitClause = MapBuilder.<String, Object>build(ENTITY_CUSTOMFIELD_ID, CustomFieldUtils.getCustomFieldId(customFieldId));
        List<GenericValue> values = delegator.findByAnd(TABLE_CUSTOMFIELD_VALUE, limitClause);

        Set<Long> issues = new HashSet<Long>();
        List<GenericValue> deleteThese = new ArrayList<GenericValue>();
        for (GenericValue valueGV : values)
        {
            final Long aLong = valueGV.getLong(ENTITY_ISSUE_ID);
            if (!aLong.equals(DEFAULT_VALUE_ISSUE_ID))
            {
                issues.add(aLong);
            }
            deleteThese.add(valueGV);
        }
        delegator.removeAll(deleteThese);
        return issues;
    }

    protected List<GenericValue> getValuesForTypeAndParent(CustomField field, Long issueId, String parentKey)
    {
        final Map<String, Object> limitClause = MapBuilder.<String, Object>build(ENTITY_ISSUE_ID, issueId,
                                               ENTITY_CUSTOMFIELD_ID, CustomFieldUtils.getCustomFieldId(field.getId()),
                                               ENTITY_PARENT_KEY, parentKey);

        return delegator.findByAnd(TABLE_CUSTOMFIELD_VALUE, limitClause);
    }

    protected List<GenericValue> getValuesForType(CustomField field, Long issueId)
    {
        final Map<String, Object> limitClause = MapBuilder.<String, Object>build(ENTITY_ISSUE_ID, issueId,
                                               ENTITY_CUSTOMFIELD_ID, CustomFieldUtils.getCustomFieldId(field.getId()));

        return delegator.findByAnd(TABLE_CUSTOMFIELD_VALUE, limitClause);
    }

    private static String getColumnName(PersistenceFieldType persistenceFieldType)
    {
        final String columnName = DB_FIELD_MAPPING.get(persistenceFieldType);
        if (columnName == null)
            throw new IllegalArgumentException("PersistenceFieldType '" + persistenceFieldType + "' not supported. Supported types: '" + DB_FIELD_MAPPING.keySet() + "'");

        return columnName;
    }
}
