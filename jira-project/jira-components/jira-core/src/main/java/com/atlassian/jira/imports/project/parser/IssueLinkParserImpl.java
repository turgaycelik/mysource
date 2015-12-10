package com.atlassian.jira.imports.project.parser;

import com.atlassian.jira.exception.ParseException;
import com.atlassian.jira.external.beans.ExternalLink;
import com.atlassian.jira.imports.project.core.EntityRepresentation;
import com.atlassian.jira.imports.project.core.EntityRepresentationImpl;
import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @since v3.13
 */
public class IssueLinkParserImpl implements IssueLinkParser
{
    private static final String ID = "id";
    private static final String LINK_TYPE = "linktype";
    private static final String SOURCE = "source";
    private static final String DESTINATION = "destination";
    private static final String SEQUENCE = "sequence";

    public ExternalLink parse(final Map attributes) throws ParseException
    {
        if (attributes == null)
        {
            throw new IllegalArgumentException("The 'attributes' parameter cannot be null.");
        }

        //     <IssueLink id="10010" linktype="10001" source="10030" destination="10031" sequence="0"/>
        final String id = (String) attributes.get(ID);
        final String linktype = (String) attributes.get(LINK_TYPE);
        final String source = (String) attributes.get(SOURCE);
        final String destination = (String) attributes.get(DESTINATION);
        final String sequence = (String) attributes.get(SEQUENCE);

        // Validate the data
        if (StringUtils.isEmpty(id))
        {
            throw new ParseException("No 'id' field for IssueLink.");
        }
        if (StringUtils.isEmpty(linktype))
        {
            throw new ParseException("No 'linktype' field for IssueLink " + id + ".");
        }
        // sequence is optional and only used for subtasks

        final ExternalLink externalLink = new ExternalLink();
        externalLink.setId(id);
        externalLink.setLinkType(linktype);
        externalLink.setSourceId(source);
        externalLink.setDestinationId(destination);
        externalLink.setSequence(sequence);
        return externalLink;
    }

    public EntityRepresentation getEntityRepresentation(final ExternalLink issueLink)
    {
        final Map entityValues = new HashMap(5);
        entityValues.put(ID, issueLink.getId());
        entityValues.put(LINK_TYPE, issueLink.getLinkType());
        entityValues.put(SOURCE, issueLink.getSourceId());
        entityValues.put(DESTINATION, issueLink.getDestinationId());
        entityValues.put(SEQUENCE, issueLink.getSequence());

        return new EntityRepresentationImpl(IssueLinkParser.ISSUE_LINK_ENTITY_NAME, entityValues);
    }
}
