package com.atlassian.jira.imports.project.parser;

import com.atlassian.jira.exception.ParseException;
import com.atlassian.jira.external.beans.ExternalChangeGroup;
import com.atlassian.jira.imports.project.core.EntityRepresentation;
import com.atlassian.jira.imports.project.core.EntityRepresentationImpl;
import com.atlassian.jira.util.dbc.Null;
import org.apache.commons.lang.StringUtils;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

/**
 * @since v3.13
 */
public class ChangeGroupParserImpl implements ChangeGroupParser
{
    private static final String CREATED = "created";
    private static final String AUTHOR = "author";
    private static final String ID = "id";
    private static final String ISSUE = "issue";

    public ExternalChangeGroup parse(final Map attributes) throws ParseException
    {
        Null.not("attributes", attributes);
        // <ChangeGroup id="10020" issue="10000" author="admin" created="2008-01-22 11:04:10.339"/>

        final String id = (String) attributes.get(ID);
        final String issueId = (String) attributes.get(ISSUE);

        if (StringUtils.isEmpty(id))
        {
            throw new ParseException("A change group must have an id specified.");
        }
        if (StringUtils.isEmpty(issueId))
        {
            throw new ParseException("A change group must have an issue id specified.");
        }

        final String created = (String) attributes.get(CREATED);
        final String author = (String) attributes.get(AUTHOR);

        final ExternalChangeGroup changeGroup = new ExternalChangeGroup();
        changeGroup.setId(id);
        changeGroup.setIssueId(issueId);
        changeGroup.setAuthor(author);

        if (created != null)
        {
            changeGroup.setCreated(java.sql.Timestamp.valueOf(created));
        }
        return changeGroup;
    }

    public EntityRepresentation getEntityRepresentation(final ExternalChangeGroup changeGroup)
    {
        final Map attributes = new HashMap();
        attributes.put(ID, changeGroup.getId());
        attributes.put(ISSUE, changeGroup.getIssueId());
        attributes.put(AUTHOR, changeGroup.getAuthor());
        if (changeGroup.getCreated() != null)
        {
            attributes.put(CREATED, new Timestamp(changeGroup.getCreated().getTime()).toString());
        }
        return new EntityRepresentationImpl(ChangeGroupParser.CHANGE_GROUP_ENTITY_NAME, attributes);
    }
}
