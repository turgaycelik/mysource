package com.atlassian.jira.issue.customfields.impl;

import com.atlassian.jira.exception.ParseException;
import com.atlassian.jira.external.beans.ExternalLabel;
import com.atlassian.jira.imports.project.core.EntityRepresentation;
import com.atlassian.jira.imports.project.core.EntityRepresentationImpl;
import com.atlassian.jira.imports.project.customfield.ExternalCustomFieldValue;
import com.atlassian.jira.imports.project.customfield.ExternalCustomFieldValueImpl;
import com.atlassian.jira.imports.project.parser.LabelParserImpl;
import com.atlassian.jira.issue.customfields.ProjectImportLabelFieldParser;
import com.atlassian.jira.issue.label.OfBizLabelStore;
import com.atlassian.jira.util.dbc.Null;
import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @since v5.0
 */
public class ProjectImportLabelFieldParserImpl implements ProjectImportLabelFieldParser
{
    public ProjectImportLabelFieldParserImpl()
    {
    }

    @Override
    public String getEntityName()
    {
        return OfBizLabelStore.TABLE;
    }

    @Override
    public ExternalCustomFieldValue parse(Map attributes) throws ParseException
    {
        // <Label id="10037" fieldid="10000" issue="10000" label="TEST"/>
        Null.not("attributes", attributes);

        final String customFieldIdString = (String) attributes.get("fieldid");
        if (StringUtils.isNotBlank(customFieldIdString))
        {
            com.atlassian.jira.imports.project.parser.LabelParser parser = new LabelParserImpl();
            final ExternalLabel externalLabel = parser.parse(attributes);

            //need to convert he label to customfieldvalue represenation
            final ExternalCustomFieldValueImpl label = new ExternalCustomFieldValueImpl(externalLabel.getId(),
                    externalLabel.getCustomFieldId(), externalLabel.getIssueId());
            label.setStringValue(externalLabel.getLabel());

            return label;
        }
        return null;
    }

    @Override
    public EntityRepresentation getEntityRepresentation(ExternalCustomFieldValue customFieldValue)
    {
        final Map<String, String> attributes = new HashMap<String, String>();
        attributes.put("id", customFieldValue.getId());
        attributes.put("issue", customFieldValue.getIssueId());
        attributes.put("fieldid", customFieldValue.getCustomFieldId());
        attributes.put("label", customFieldValue.getStringValue());

        return new EntityRepresentationImpl(getEntityName(), attributes);
    }
}
