package com.atlassian.jira.imports.project.handler;

import com.atlassian.jira.exception.ParseException;
import com.atlassian.jira.imports.project.customfield.ExternalCustomFieldOption;
import com.atlassian.jira.imports.project.mapper.CustomFieldOptionMapper;
import org.apache.commons.lang.StringUtils;

import java.util.Map;

/**
 * Handles the CustomFieldOption out of the backup data and populates the mapper with the found values.
 *
 * @since v3.13
 */
public class CustomFieldOptionsMapperHandler implements ImportEntityHandler
{
    private final CustomFieldOptionMapper customFieldOptionMapper;

    public static final String CUSTOM_FIELD_OPTION_ENTITY_NAME = "CustomFieldOption";
    private static final String ID = "id";
    private static final String CUSTOMFIELD = "customfield";
    private static final String CUSTOMFIELDCONFIG = "customfieldconfig";
    private static final String PARENTOPTIONID = "parentoptionid";
    private static final String VALUE = "value";

    public CustomFieldOptionsMapperHandler(final CustomFieldOptionMapper customFieldOptionMapper)
    {
        this.customFieldOptionMapper = customFieldOptionMapper;
    }

    public void handleEntity(final String entityName, final Map<String, String> attributes) throws ParseException
    {
        if (CUSTOM_FIELD_OPTION_ENTITY_NAME.equals(entityName))
        {
            //<CustomFieldOption id="10023" customfield="10030" customfieldconfig="10050" parentoptionid="10020" sequence="0" value="cascading select1a"/>
            final String id = attributes.get(ID);
            final String customFieldId = attributes.get(CUSTOMFIELD);
            final String fieldConfigId = attributes.get(CUSTOMFIELDCONFIG);
            final String parentId = attributes.get(PARENTOPTIONID);
            final String value = attributes.get(VALUE);
            // Validate
            if (StringUtils.isBlank(id))
            {
                throw new ParseException("Encountered an entity of type '" + CUSTOM_FIELD_OPTION_ENTITY_NAME + "' with a missing ID.");
            }
            if (StringUtils.isBlank(customFieldId))
            {
                throw new ParseException("The customfield of " + CUSTOM_FIELD_OPTION_ENTITY_NAME + " '" + id + "' is missing.");
            }
            if (StringUtils.isBlank(fieldConfigId))
            {
                throw new ParseException("The customfieldconfig of " + CUSTOM_FIELD_OPTION_ENTITY_NAME + " '" + id + "' is missing.");
            }
            // Populate the mapper
            final ExternalCustomFieldOption externalCustomFieldOption = new ExternalCustomFieldOption(id, customFieldId, fieldConfigId, parentId,
                value);
            customFieldOptionMapper.registerOldValue(externalCustomFieldOption);
        }
    }

    public void startDocument()
    {
    // no-op
    }

    public void endDocument()
    {
    // no-op
    }

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

        final CustomFieldOptionsMapperHandler that = (CustomFieldOptionsMapperHandler) o;

        if (customFieldOptionMapper != null ? !customFieldOptionMapper.equals(that.customFieldOptionMapper) : that.customFieldOptionMapper != null)
        {
            return false;
        }

        return true;
    }

    public int hashCode()
    {
        return (customFieldOptionMapper != null ? customFieldOptionMapper.hashCode() : 0);
    }
}
