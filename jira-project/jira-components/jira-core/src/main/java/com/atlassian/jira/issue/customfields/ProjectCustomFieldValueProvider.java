package com.atlassian.jira.issue.customfields;

import com.atlassian.jira.issue.customfields.converters.ProjectConverter;
import com.atlassian.jira.issue.customfields.impl.FieldValidationException;
import com.atlassian.jira.issue.customfields.view.CustomFieldParams;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.transport.FieldValuesHolder;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericValue;

import java.util.List;

/**
 * An implementation of the {@link com.atlassian.jira.issue.customfields.CustomFieldValueProvider}
 * for retreiving the project value.
 *
 * @since v4.0
 */
public class ProjectCustomFieldValueProvider implements CustomFieldValueProvider
{
    private static final Logger log = Logger.getLogger(ProjectCustomFieldValueProvider.class);
    private final ProjectConverter projectConverter;

    public ProjectCustomFieldValueProvider(ProjectConverter projectConverter)
    {
        this.projectConverter = projectConverter;
    }

    public Object getStringValue(CustomField customField, FieldValuesHolder fieldValuesHolder)
    {
        GenericValue project = (GenericValue)getValue(customField, fieldValuesHolder);
        if (project != null)
        {
            return project.getLong("id").toString();
        }
        else
        {
            return null;
        }
    }

    public Object getValue(CustomField customField, FieldValuesHolder fieldValuesHolder)
    {
        final CustomFieldParams customFieldParams = customField.getCustomFieldValues(fieldValuesHolder);
        final Object obj = customField.getCustomFieldType().getStringValueFromCustomFieldParams(customFieldParams);
        Object id = null;
        if (obj instanceof List)
        {
            if (!((List) obj).isEmpty())
            {
                id = ((List) obj).get(0);
            }
        }
        else
        {
            id = obj;
        }

        if (id instanceof String)
        {
            return getProjectGeneric((String)id);
        }
        return null;
    }
    
    private GenericValue getProjectGeneric(String projectId)
    {
        try
        {
            GenericValue project = projectConverter.getProject(projectId);
            if (project != null)
                return project;
        }
        catch (FieldValidationException e)
        {
            log.warn(projectId + " not a valid project id");
        }
        return null;
    }
}