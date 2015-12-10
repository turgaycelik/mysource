package com.atlassian.jira.imports.project.parser;

import com.atlassian.jira.exception.ParseException;
import com.atlassian.jira.external.beans.ExternalComment;
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
public class CommentParserImpl implements CommentParser
{
    private static final String COMMENT_TYPE = "comment";
    private static final String TYPE = "type";
    private static final String ID = "id";
    private static final String ISSUE = "issue";
    private static final String BODY = "body";
    private static final String CREATED = "created";
    private static final String UPDATED = "updated";
    private static final String AUTHOR = "author";
    private static final String UPDATEAUTHOR = "updateauthor";
    private static final String ROLELEVEL = "rolelevel";
    private static final String LEVEL = "level";

    public ExternalComment parse(final Map attributes) throws ParseException
    {
        Null.not("attributes", attributes);
        final String type = (String) attributes.get(TYPE);

        // We only handle actions of the type comment
        if (!COMMENT_TYPE.equals(type))
        {
            return null;
        }

        final String id = (String) attributes.get(ID);
        final String issueId = (String) attributes.get(ISSUE);

        if (StringUtils.isEmpty(id))
        {
            throw new ParseException("A comment must have an id specified.");
        }
        if (StringUtils.isEmpty(issueId))
        {
            throw new ParseException("A comment must have an issue id specified.");
        }

        final String body = (String) attributes.get(BODY);
        final String created = (String) attributes.get(CREATED);
        final String updated = (String) attributes.get(UPDATED);
        final String author = (String) attributes.get(AUTHOR);
        final String updateAuthor = (String) attributes.get(UPDATEAUTHOR);
        final String roleLevelIdStr = (String) attributes.get(ROLELEVEL);
        final String groupLevel = (String) attributes.get(LEVEL);
        Long roleLevelId = null;

        if (roleLevelIdStr != null)
        {
            try
            {
                roleLevelId = new Long(roleLevelIdStr);
            }
            catch (final NumberFormatException e)
            {
                throw new ParseException("The comment is restricted by a project role with id '" + roleLevelIdStr + "' which is not a valid long.");
            }
        }
        final ExternalComment comment = new ExternalComment(body);
        comment.setId(id);
        comment.setIssueId(issueId);
        comment.setUsername(author);
        comment.setUpdateAuthor(updateAuthor);
        comment.setGroupLevel(groupLevel);
        if (roleLevelId != null)
        {
            comment.setRoleLevelId(roleLevelId);
        }
        if (created != null)
        {
            comment.setTimePerformed(java.sql.Timestamp.valueOf(created));
        }
        if (updated != null)
        {
            comment.setUpdated(java.sql.Timestamp.valueOf(updated));
        }

        return comment;
    }

    public EntityRepresentation getEntityRepresentation(final ExternalComment comment)
    {
        final Map attributes = new HashMap();
        attributes.put(TYPE, COMMENT_TYPE);
        attributes.put(ID, comment.getId());
        attributes.put(ISSUE, comment.getIssueId());
        attributes.put(BODY, comment.getBody());
        // Put the dates in a string format that OfBiz expects
        if (comment.getTimePerformed() != null)
        {
            attributes.put(CREATED, new Timestamp(comment.getTimePerformed().getTime()).toString());
        }
        if (comment.getUpdated() != null)
        {
            attributes.put(UPDATED, new Timestamp(comment.getUpdated().getTime()).toString());
        }
        attributes.put(AUTHOR, comment.getUsername());
        attributes.put(UPDATEAUTHOR, comment.getUpdateAuthor());
        if (comment.getRoleLevelId() != null)
        {
            attributes.put(ROLELEVEL, comment.getRoleLevelId().toString());
        }
        if (comment.getGroupLevel() != null)
        {
            attributes.put(LEVEL, comment.getGroupLevel());
        }
        return new EntityRepresentationImpl(COMMENT_ENTITY_NAME, attributes);
    }
}
