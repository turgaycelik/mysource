package com.atlassian.jira.imports.project.handler;

import com.atlassian.jira.exception.ParseException;
import com.atlassian.jira.imports.project.core.BackupProject;
import com.atlassian.jira.imports.project.customfield.ExternalCustomFieldValue;
import com.atlassian.jira.imports.project.mapper.CustomFieldMapper;
import com.atlassian.jira.imports.project.parser.CustomFieldValueParser;

import java.util.Map;

/**
 * Populates the custom field values that are in use into a CustomFieldMapper.
 *
 * @since v3.13
 */
public class CustomFieldMapperHandler implements ImportEntityHandler
{
    private final BackupProject backupProject;
    private final CustomFieldMapper customFieldMapper;
    private final Map<String, CustomFieldValueParser> parsers;

    public CustomFieldMapperHandler(final BackupProject backupProject, final CustomFieldMapper customFieldMapper, final Map<String, CustomFieldValueParser> parsers)
    {
        this.backupProject = backupProject;
        this.customFieldMapper = customFieldMapper;
        this.parsers = parsers;
    }

    public void handleEntity(final String entityName, final Map<String, String> attributes) throws ParseException
    {
        if(parsers.containsKey(entityName))
        {
            final CustomFieldValueParser parser = parsers.get(entityName);
            final ExternalCustomFieldValue externalCustomFieldValue = parser.parse(attributes);
            if ((externalCustomFieldValue != null) && backupProject.containsIssue(externalCustomFieldValue.getIssueId()))
            {
                customFieldMapper.flagValueAsRequired(externalCustomFieldValue.getCustomFieldId(), externalCustomFieldValue.getIssueId());
            }
        }
    }

    ///CLOVER:ON

    ///CLOVER:OFF
    public void startDocument()
    {
    // No-op
    }

    ///CLOVER:ON

    ///CLOVER:OFF
    public void endDocument()
    {
    // No-op
    }

    ///CLOVER:ON

    ///CLOVER:OFF
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if ((o == null) || (getClass() != o.getClass()))
        {
            return false;
        }

        final CustomFieldMapperHandler that = (CustomFieldMapperHandler) o;

        if (backupProject != null ? !backupProject.equals(that.backupProject) : that.backupProject != null)
        {
            return false;
        }
        if (customFieldMapper != null ? !customFieldMapper.equals(that.customFieldMapper) : that.customFieldMapper != null)
        {
            return false;
        }

        return true;
    }

    ///CLOVER:ON

    ///CLOVER:OFF
    public int hashCode()
    {
        int result;
        result = (backupProject != null ? backupProject.hashCode() : 0);
        result = 31 * result + (customFieldMapper != null ? customFieldMapper.hashCode() : 0);
        return result;
    }
    ///CLOVER:ON

}
