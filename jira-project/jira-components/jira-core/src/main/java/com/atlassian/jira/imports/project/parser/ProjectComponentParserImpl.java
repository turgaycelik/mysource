package com.atlassian.jira.imports.project.parser;

import com.atlassian.jira.exception.ParseException;
import com.atlassian.jira.external.beans.ExternalComponent;
import org.apache.commons.lang.StringUtils;

import java.util.Map;

/**
 * @since v3.13
 */
public class ProjectComponentParserImpl implements ProjectComponentParser
{
    public ExternalComponent parse(final Map attributes) throws ParseException
    {
        if (attributes == null)
        {
            throw new IllegalArgumentException("The 'attributes' parameter cannot be null.");
        }
        // <Component id="10010" project="10001" name="comp 1" lead="admin" assigneetype="0"/>

        final String id = (String) attributes.get("id");
        final String name = (String) attributes.get("name");
        final String projectId = (String) attributes.get("project");
        final String lead = (String) attributes.get("lead");
        final String assigneeType = (String) attributes.get("assigneetype");
        final String description = (String) attributes.get("description");

        // Validate the data
        if (StringUtils.isEmpty(id))
        {
            throw new ParseException("No 'id' field for ProjectComponent.");
        }
        if (StringUtils.isEmpty(name))
        {
            throw new ParseException("No 'name' field for ProjectComponent " + id + ".");
        }
        if (StringUtils.isEmpty(projectId))
        {
            throw new ParseException("No 'project' field for ProjectComponent " + id + ".");
        }

        final ExternalComponent externalComponent = new ExternalComponent(name);
        externalComponent.setId(id);
        externalComponent.setProjectId(projectId);
        externalComponent.setLead(lead);
        externalComponent.setAssigneeType(assigneeType);
        externalComponent.setDescription(description);
        return externalComponent;
    }
}
