package com.atlassian.jira.issue.statistics;

import com.atlassian.jira.issue.customfields.statistics.CustomFieldStattable;
import com.atlassian.jira.issue.fields.CustomField;
import com.opensymphony.util.TextUtils;
import org.apache.commons.collections.map.ListOrderedMap;
import org.ofbiz.core.entity.GenericValue;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: detkin
 * Date: Jan 19, 2006
 * Time: 12:52:27 PM
 * To change this template use File | Settings | File Templates.
 */
public class FilterStatisticsCascadingSelectValuesGenerator extends FilterStatisticsValuesGenerator
{

    public Map getValues(Map params)
    {
        Map allValues = new ListOrderedMap();

        allValues.putAll(getValueClassHolderSystemValues());
        final List<CustomField> customFieldObjects = customFieldManager.getCustomFieldObjects();

        for (final CustomField customField : customFieldObjects)
        {
            if (customField.getCustomFieldSearcher() instanceof CustomFieldStattable)
            {
                allValues.put(customField.getId(), getValueClassHolderForCF(customField));
            }
        }
        return allValues;
    }

    private ValueClassHolder getValueClassHolderForCF(CustomField customField)
    {
        StringBuilder className = new StringBuilder();
        int i = 0;
        for (Iterator iterator = customField.getAssociatedProjects().iterator(); iterator.hasNext(); i++)
        {
            // append a separator so we handle multiple projects
            if(i != 0)
            {
                className.append(":");
            }
            GenericValue project = (GenericValue) iterator.next();
            className.append(project.getString("id"));
        }
        if(!TextUtils.stringSet(className.toString()))
        {
            return new ValueClassHolder(customField.getName(), "select");
        }
        else
        {
            return new ValueClassHolder(customField.getName(), className.toString());
        }
    }

    private Map getValueClassHolderSystemValues()
    {
        Map valueClassSystemValue = new HashMap();
        for (String key : systemValues.keySet())
        {
            valueClassSystemValue.put(key, new ValueClassHolder((String) systemValues.get(key), "select"));
        }
        return valueClassSystemValue;
    }

    private static class ValueClassHolder
    {
        private String value;
        private String className;

        public ValueClassHolder(String value, String className)
        {
            this.value = value;
            this.className = className;
        }

        public String getValue()
        {
            return value;
        }

        public String getClassName()
        {
            return className;
        }

        public String toString()
        {
            return value;
        }
    }

}