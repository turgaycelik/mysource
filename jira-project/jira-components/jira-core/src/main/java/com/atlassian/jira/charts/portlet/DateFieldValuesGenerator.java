package com.atlassian.jira.charts.portlet;

import com.atlassian.configurable.ValuesGenerator;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.customfields.CustomFieldType;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.DateField;
import com.atlassian.jira.issue.fields.Field;
import com.atlassian.jira.issue.fields.FieldException;
import com.atlassian.jira.issue.fields.FieldManager;
import com.google.common.annotations.VisibleForTesting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Used to get a list of all datefields registered in the system. (Marked with the {@link
 * com.atlassian.jira.issue.fields.DateField} marker interface.)
 */
public class DateFieldValuesGenerator implements ValuesGenerator
{
    public Map getValues(Map userParams)
    {
        final Map<String, String> fields = new LinkedHashMap<String, String>();
        final FieldManager fieldManager = getFieldManager();

        try
        {
            @SuppressWarnings ("unchecked")
            final List<Field> navigableFields = new ArrayList<Field>(fieldManager.getAllAvailableNavigableFields());
            //sort the fields to make sure we get a predictable order in the drop down list. 
            Collections.sort(navigableFields);

            for (final Field field : navigableFields)
            {
                if (isDateTypeField(field, fieldManager))
                {
                    fields.put(field.getId(), field.getName());
                }
            }
        }
        catch (FieldException e)
        {
            return fields;
        }

        return fields;
    }

    private boolean isDateTypeField(Field field, FieldManager fieldManager)
    {
        if (fieldManager.isCustomField(field))
        {
            final CustomFieldType customFieldType = ((CustomField) field).getCustomFieldType();
            return customFieldType instanceof DateField;
        }
        else
        {
            return field instanceof DateField;
        }
    }

    //unfortunately this can't be injected since it's objectconfigurable's job to instantiate this.
    @VisibleForTesting
    FieldManager getFieldManager()
    {
        return ComponentAccessor.getComponentOfType(FieldManager.class);
    }
}