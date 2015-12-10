package com.atlassian.jira.imports.project.parser;

import com.atlassian.jira.exception.ParseException;
import com.atlassian.jira.external.beans.ExternalIssue;
import com.atlassian.jira.external.beans.ExternalIssueImpl;
import com.atlassian.jira.imports.project.core.EntityRepresentation;
import com.atlassian.jira.imports.project.core.EntityRepresentationImpl;
import com.atlassian.jira.util.dbc.Null;
import org.apache.commons.lang.StringUtils;

import java.util.Map;

/**
 * Converts issue xml in a JIRA backup to an object representation and converts the object representation
 * into XML.
 * <p/>
 * NOTE: This was not used in the spike, it may need some modifications
 *
 * @since v3.13
 */
public class IssueParserImpl implements IssueParser
{

    public ExternalIssue parse(final Map attributes) throws ParseException
    {
        Null.not("The 'attributes' parameter cannot be null.", attributes);

        //<Issue id="10001" key="HSP-2" project="10000" reporter="admin" assignee="admin" type="1" summary="CLONE -test" description="asdfasdfasdfasfaf" environment="asdf" priority="3" status="1" created="2008-01-07 16:24:54.23" updated="2008-01-07 16:41:19.602" votes="0" workflowId="10001"/>

        final String idStr = (String) attributes.get("id");
        final String summary = (String) attributes.get("summary");
        final String key = (String) attributes.get("key");
        final String project = (String) attributes.get("project");
        final String type = (String) attributes.get("type");
        final String status = (String) attributes.get("status");
        final String resolution = (String) attributes.get("resolution");
        final String reporter = (String) attributes.get("reporter");
        final String assignee = (String) attributes.get("assignee");
        final String creator = (String) attributes.get("creator");
        final String description = (String) attributes.get("description");
        final String environment = (String) attributes.get("environment");
        final String priority = (String) attributes.get("priority");
        final String created = (String) attributes.get("created");
        final String updated = (String) attributes.get("updated");
        final String duedate = (String) attributes.get("duedate");
        final String resolutionDate = (String) attributes.get("resolutiondate");
        final String votesStr = (String) attributes.get("votes");
        final String timespentStr = (String) attributes.get("timespent");
        final String timeoriginalestimate = (String) attributes.get("timeoriginalestimate");
        final String timeestimate = (String) attributes.get("timeestimate");
        final String securityLevel = (String) attributes.get("security");

        Long id = null;
        Long votes = null;
        Long timeSpent = null;
        Long originalEstimate = null;
        Long estimate = null;

        // Validate the data
        if (StringUtils.isEmpty(idStr))
        {
            throw new ParseException("No 'id' field for Issue.");
        }
        try
        {
            id = new Long(idStr);
        }
        catch (final NumberFormatException e)
        {
            throw new ParseException("Unable to parse the Issue id '" + idStr + "' into a long.");
        }
        if (StringUtils.isEmpty(key))
        {
            throw new ParseException("No 'key' field for Issue " + id + ".");
        }
        if (StringUtils.isEmpty(project))
        {
            throw new ParseException("No 'project' field for Issue " + id + ".");
        }
        if (StringUtils.isEmpty(type))
        {
            throw new ParseException("No 'type' field for Issue " + id + ".");
        }
        if (StringUtils.isEmpty(status))
        {
            throw new ParseException("No 'status' field for Issue " + id + ".");
        }
        if (votesStr != null)
        {
            try
            {
                votes = new Long(votesStr);
            }
            catch (final NumberFormatException e)
            {
                throw new ParseException("Unable to parse the Vote count '" + votesStr + "' for Issue '" + idStr + "'");
            }
        }
        if (timespentStr != null)
        {
            try
            {
                timeSpent = new Long(timespentStr);
            }
            catch (final NumberFormatException e)
            {
                throw new ParseException("Unable to parse the TimeSpent '" + timespentStr + "' for Issue '" + idStr + "'");
            }
        }
        if (timeoriginalestimate != null)
        {
            try
            {
                originalEstimate = new Long(timeoriginalestimate);
            }
            catch (final NumberFormatException e)
            {
                throw new ParseException("Unable to parse the OriginalEstimate '" + timeoriginalestimate + "' for Issue '" + idStr + "'");
            }
        }
        if (timeestimate != null)
        {
            try
            {
                estimate = new Long(timeestimate);
            }
            catch (final NumberFormatException e)
            {
                throw new ParseException("Unable to parse the Time Estimate '" + timeestimate + "' for Issue '" + idStr + "'");
            }
        }

        final ExternalIssueImpl issue = new ExternalIssueImpl(creator);
        issue.setId(idStr);
        issue.setKey(key);
        issue.setSummary(summary);
        issue.setProject(project);
        issue.setIssueType(type);
        issue.setStatus(status);
        issue.setResolution(resolution);
        issue.setReporter(reporter);
        issue.setAssignee(assignee);
        issue.setDescription(description);
        issue.setEnvironment(environment);
        issue.setPriority(priority);

        if (created != null)
        {
            issue.setCreated(java.sql.Timestamp.valueOf(created));
        }
        if (updated != null)
        {
            issue.setUpdated(java.sql.Timestamp.valueOf(updated));
        }
        if (duedate != null)
        {
            issue.setDuedate(java.sql.Timestamp.valueOf(duedate));
        }
        if (resolutionDate != null)
        {
            issue.setResolutionDate(java.sql.Timestamp.valueOf(resolutionDate));
        }

        issue.setVotes(votes);
        issue.setTimeSpent(timeSpent);
        issue.setOriginalEstimate(originalEstimate);
        issue.setEstimate(estimate);
        issue.setSecurityLevel(securityLevel);

        return issue;
    }

}
