package com.atlassian.jira.imports.project.parser;

import com.atlassian.jira.exception.ParseException;
import com.atlassian.jira.imports.project.core.EntityRepresentation;
import com.atlassian.jira.imports.project.core.EntityRepresentationImpl;
import com.atlassian.jira.imports.project.customfield.ExternalCustomFieldValue;
import com.atlassian.jira.imports.project.customfield.ExternalCustomFieldValueImpl;
import com.atlassian.jira.util.dbc.Null;
import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @since v3.13
 */
public class CustomFieldValueParserImpl implements CustomFieldValueParser
{

    private static final String ID = "id";
    private static final String ISSUE = "issue";
    private static final String CUSTOMFIELD = "customfield";
    private static final String STRINGVALUE = "stringvalue";
    private static final String DATEVALUE = "datevalue";
    private static final String NUMBERVALUE = "numbervalue";
    private static final String TEXTVALUE = "textvalue";
    private static final String PARENTKEY = "parentkey";

    public ExternalCustomFieldValue parse(final Map attributes) throws ParseException
    {
        Null.not("attributes", attributes);

        // <CustomFieldValue id="10015" issue="10025" customfield="10000" stringvalue="Future"/>

        final String id = (String) attributes.get(ID);
        final String issueId = (String) attributes.get(ISSUE);
        final String customFieldId = (String) attributes.get(CUSTOMFIELD);

        // Validate the data
        if (StringUtils.isEmpty(id))
        {
            throw new ParseException("No 'id' field for CustomFieldValue.");
        }
        if (StringUtils.isEmpty(issueId))
        {
            throw new ParseException("No 'issue' field for CustomFieldValue '" + id + "'.");
        }
        if (StringUtils.isEmpty(customFieldId))
        {
            throw new ParseException("No 'customfield' field for CustomFieldValue '" + id + "'.");
        }

        final ExternalCustomFieldValueImpl customFieldValue = new ExternalCustomFieldValueImpl(id, customFieldId, issueId);
        customFieldValue.setStringValue((String) attributes.get(STRINGVALUE));
        customFieldValue.setDateValue((String) attributes.get(DATEVALUE));
        customFieldValue.setNumberValue((String) attributes.get(NUMBERVALUE));
        customFieldValue.setTextValue((String) attributes.get(TEXTVALUE));
        customFieldValue.setParentKey((String) attributes.get(PARENTKEY));
        return customFieldValue;
    }

    public EntityRepresentation getEntityRepresentation(final ExternalCustomFieldValue customFieldValue)
    {
        final Map values = new HashMap();
        values.put(ISSUE, customFieldValue.getIssueId());
        values.put(CUSTOMFIELD, customFieldValue.getCustomFieldId());
        values.put(STRINGVALUE, customFieldValue.getStringValue());
        values.put(NUMBERVALUE, customFieldValue.getNumberValue());
        values.put(DATEVALUE, customFieldValue.getDateValue());
        values.put(TEXTVALUE, customFieldValue.getTextValue());
        values.put(PARENTKEY, customFieldValue.getParentKey());
        return new EntityRepresentationImpl(CUSTOM_FIELD_VALUE_ENTITY_NAME, values);
    }
}
