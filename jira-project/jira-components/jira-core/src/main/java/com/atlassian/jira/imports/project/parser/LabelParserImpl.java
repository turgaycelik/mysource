package com.atlassian.jira.imports.project.parser;

import com.atlassian.jira.exception.ParseException;
import com.atlassian.jira.external.beans.ExternalLabel;
import com.atlassian.jira.imports.project.core.EntityRepresentation;
import com.atlassian.jira.imports.project.core.EntityRepresentationImpl;
import com.atlassian.jira.issue.label.OfBizLabelStore;
import com.atlassian.jira.util.dbc.Null;
import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @since v4.2
 */
public class LabelParserImpl implements LabelParser
{
    private static final String ID = "id";
    private static final String ISSUE = "issue";
    private static final String LABEL = "label";
    private static final String CUSTOM_FIELD_ID = "fieldid";

    public ExternalLabel parse(final Map attributes) throws ParseException
    {
        Null.not("attributes", attributes);
        // <Label id="10037" fieldid="10000" issue="10000" label="TEST"/>

        final String idString = (String) attributes.get(ID);
        final String issueIdString = (String) attributes.get(ISSUE);
        final String labelString = (String) attributes.get(LABEL);

        if (StringUtils.isEmpty(idString))
        {
            throw new ParseException("A label must have an id specified.");
        }
        if (StringUtils.isEmpty(issueIdString))
        {
            throw new ParseException("A label must have an issue id specified.");
        }
        if (StringUtils.isEmpty(labelString))
        {
            throw new ParseException("A label must have a label specified.");
        }
        if(!com.atlassian.jira.issue.label.LabelParser.isValidLabelName(labelString))
        {
            throw new ParseException("Invalid label '" + labelString + "' specified.");
        }
        final String customFieldIdString = (String) attributes.get(CUSTOM_FIELD_ID);

        final ExternalLabel label = new ExternalLabel();
        label.setId(idString);
        label.setIssueId(issueIdString);
        label.setCustomFieldId(customFieldIdString);
        label.setLabel(labelString);

        return label;
    }

    public EntityRepresentation getEntityRepresentation(final ExternalLabel label)
    {
        final Map<String, String> attributes = new HashMap<String, String>();
        attributes.put(ID, label.getId());
        attributes.put(ISSUE, label.getIssueId());
        attributes.put(CUSTOM_FIELD_ID, label.getCustomFieldId());
        attributes.put(LABEL, label.getLabel());

        return new EntityRepresentationImpl(OfBizLabelStore.TABLE, attributes);
    }
}