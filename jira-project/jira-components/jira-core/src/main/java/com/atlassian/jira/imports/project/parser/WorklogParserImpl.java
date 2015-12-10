package com.atlassian.jira.imports.project.parser;

import com.atlassian.jira.exception.ParseException;
import com.atlassian.jira.external.beans.ExternalWorklog;
import com.atlassian.jira.imports.project.core.EntityRepresentation;
import com.atlassian.jira.imports.project.core.EntityRepresentationImpl;
import com.atlassian.jira.issue.worklog.OfBizWorklogStore;
import com.atlassian.jira.util.dbc.Null;
import org.apache.commons.lang.StringUtils;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

/**
 * @since v3.13
 */
public class WorklogParserImpl implements WorklogParser
{
    private static final String BODY = "body";
    private static final String CREATED = "created";
    private static final String UPDATED = "updated";
    private static final String STARTDATE = "startdate";
    private static final String AUTHOR = "author";
    private static final String UPDATEAUTHOR = "updateauthor";
    private static final String ROLELEVEL = "rolelevel";
    private static final String GROUPLEVEL = "grouplevel";
    private static final String TIMEWORKED = "timeworked";
    private static final String ID = "id";
    private static final String ISSUE = "issue";

    public ExternalWorklog parse(final Map attributes) throws ParseException
    {
        Null.not("attributes", attributes);
        // <Worklog id="10000" issue="10000" author="admin" body="" created="2008-01-07 16:30:00.958" updateauthor="admin" updated="2008-01-07 16:30:00.958" startdate="2008-01-07 16:29:00.0" timeworked="57600"/>

        final String id = (String) attributes.get(ID);
        final String issueId = (String) attributes.get(ISSUE);

        if (StringUtils.isEmpty(id))
        {
            throw new ParseException("A worklog must have an id specified.");
        }
        if (StringUtils.isEmpty(issueId))
        {
            throw new ParseException("A worklog must have an issue id specified.");
        }

        final String body = (String) attributes.get(BODY);
        final String created = (String) attributes.get(CREATED);
        final String updated = (String) attributes.get(UPDATED);
        final String startDate = (String) attributes.get(STARTDATE);
        final String author = (String) attributes.get(AUTHOR);
        final String updateAuthor = (String) attributes.get(UPDATEAUTHOR);
        final String roleLevelIdStr = (String) attributes.get(ROLELEVEL);
        final String groupLevel = (String) attributes.get(GROUPLEVEL);
        final String timeWorkedStr = (String) attributes.get(TIMEWORKED);
        Long roleLevelId = null;
        Long timeWorked = null;

        if (roleLevelIdStr != null)
        {
            try
            {
                roleLevelId = new Long(roleLevelIdStr);
            }
            catch (final NumberFormatException e)
            {
                throw new ParseException("The worklog is restricted by a project role with id '" + roleLevelIdStr + "' which is not a valid long.");
            }
        }
        if (timeWorkedStr != null)
        {
            try
            {
                timeWorked = new Long(timeWorkedStr);
            }
            catch (final NumberFormatException e)
            {
                throw new ParseException("The worklog has a time spent that is not a valid long.");
            }
        }

        final ExternalWorklog worklog = new ExternalWorklog();
        worklog.setId(id);
        worklog.setIssueId(issueId);
        worklog.setComment(body);
        worklog.setAuthor(author);
        worklog.setUpdateAuthor(updateAuthor);
        worklog.setGroupLevel(groupLevel);
        worklog.setTimeSpent(timeWorked);
        worklog.setRoleLevelId(roleLevelId);

        if (created != null)
        {
            worklog.setCreated(java.sql.Timestamp.valueOf(created));
        }
        if (updated != null)
        {
            worklog.setUpdated(java.sql.Timestamp.valueOf(updated));
        }
        if (startDate != null)
        {
            worklog.setStartDate(java.sql.Timestamp.valueOf(startDate));
        }
        return worklog;
    }

    public EntityRepresentation getEntityRepresentation(final ExternalWorklog worklog)
    {
        final Map attributes = new HashMap();
        attributes.put(ID, worklog.getId());
        attributes.put(ISSUE, worklog.getIssueId());
        attributes.put(BODY, worklog.getComment());

        if (worklog.getCreated() != null)
        {
            attributes.put(CREATED, new Timestamp(worklog.getCreated().getTime()).toString());
        }
        if (worklog.getUpdated() != null)
        {
            attributes.put(UPDATED, new Timestamp(worklog.getUpdated().getTime()).toString());
        }
        if (worklog.getStartDate() != null)
        {
            attributes.put(STARTDATE, new Timestamp(worklog.getStartDate().getTime()).toString());
        }

        attributes.put(AUTHOR, worklog.getAuthor());
        attributes.put(UPDATEAUTHOR, worklog.getUpdateAuthor());
        if (worklog.getTimeSpent() != null)
        {
            attributes.put(TIMEWORKED, worklog.getTimeSpent().toString());
        }
        if (worklog.getRoleLevelId() != null)
        {
            attributes.put(ROLELEVEL, worklog.getRoleLevelId().toString());
        }
        if (worklog.getGroupLevel() != null)
        {
            attributes.put(GROUPLEVEL, worklog.getGroupLevel());
        }
        return new EntityRepresentationImpl(OfBizWorklogStore.WORKLOG_ENTITY, attributes);
    }
}
