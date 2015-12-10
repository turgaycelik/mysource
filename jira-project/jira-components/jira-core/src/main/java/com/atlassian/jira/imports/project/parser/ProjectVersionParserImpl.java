package com.atlassian.jira.imports.project.parser;

import com.atlassian.jira.exception.ParseException;
import com.atlassian.jira.external.beans.ExternalVersion;
import org.apache.commons.lang.StringUtils;

import java.util.Map;

/**
 * @since v3.13
 */
public class ProjectVersionParserImpl implements ProjectVersionParser
{
    public ExternalVersion parse(final Map attributes) throws ParseException
    {
        if (attributes == null)
        {
            throw new IllegalArgumentException("The 'attributes' parameter cannot be null");
        }
        // <Version id="10001" project="10000" name="New Version 4" description="Test Version Description 4" sequence="2"/>

        final String id = (String) attributes.get("id");
        final String projectId = (String) attributes.get("project");
        final String name = (String) attributes.get("name");
        final String description = (String) attributes.get("description");
        final String sequence = (String) attributes.get("sequence");

        // Validate the data
        if (StringUtils.isEmpty(id))
        {
            throw new ParseException("No 'id' field for ProjectVersion.");
        }
        if (StringUtils.isEmpty(name))
        {
            throw new ParseException("No 'name' field for ProjectVersion " + id + ".");
        }
        if (StringUtils.isEmpty(sequence))
        {
            throw new ParseException("No 'sequence' field for ProjectVersion " + id + ".");
        }
        if (StringUtils.isEmpty(projectId))
        {
            throw new ParseException("No 'project' field for ProjectVersion " + id + ".");
        }

        final ExternalVersion externalVersion = new ExternalVersion(name);
        externalVersion.setDescription(description);
        externalVersion.setSequence(new Long(sequence));
        externalVersion.setId(id);
        externalVersion.setProjectId(projectId);
        externalVersion.setReleased(attributes.get("released") != null);
        String releasedDate = (String) attributes.get("releasedate");
        if (releasedDate != null)
        {
            externalVersion.setReleaseDate(java.sql.Timestamp.valueOf(releasedDate));
        }
        externalVersion.setArchived(attributes.get("archived") != null);
        return externalVersion;
    }
}
