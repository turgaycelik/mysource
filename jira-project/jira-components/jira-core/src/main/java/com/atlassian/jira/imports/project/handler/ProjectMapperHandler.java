package com.atlassian.jira.imports.project.handler;

import com.atlassian.jira.exception.ParseException;
import com.atlassian.jira.external.beans.ExternalProject;
import com.atlassian.jira.imports.project.mapper.SimpleProjectImportIdMapper;
import com.atlassian.jira.imports.project.parser.ProjectParser;
import com.atlassian.jira.imports.project.parser.ProjectParserImpl;
import com.atlassian.jira.util.dbc.Null;

import java.util.Map;

/**
 * Gets all the old projects defined in the backup XML and populates the old values into a project mapper.
 *
 * NOTE: this is a bit redundant. We have collected the project information in the first pass of the import
 * but this is quite easy to do again and allows us to discard the memory we are holding with all the other
 * project related information (project custom fields, issue ids, etc).
 *
 * @since v3.13
 */
public class ProjectMapperHandler implements ImportEntityHandler
{
    private ProjectParser projectParser;
    private final SimpleProjectImportIdMapper projectMapper;

    public ProjectMapperHandler(final SimpleProjectImportIdMapper projectMapper)
    {
        this.projectMapper = projectMapper;
    }

    public void handleEntity(final String entityName, final Map<String, String> attributes) throws ParseException
    {
        Null.not("attributes", attributes);

        if (ProjectParser.PROJECT_ENTITY_NAME.equals(entityName))
        {
            final ExternalProject project = getProjectParser().parseProject(attributes);
            // The parser never returns null
            projectMapper.registerOldValue(project.getId(), project.getKey());
        }
    }

    ProjectParser getProjectParser()
    {
        if (projectParser == null)
        {
            projectParser = new ProjectParserImpl();
        }
        return projectParser;
    }

    ///CLOVER:OFF
    public void startDocument()
    {
    // No-op
    }

    ///CLOVER:ON

    ///CLOVER:OFF
    public void endDocument()
    {
    // No-op
    }

    ///CLOVER:ON

    ///CLOVER:OFF
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if ((o == null) || (getClass() != o.getClass()))
        {
            return false;
        }

        final ProjectMapperHandler that = (ProjectMapperHandler) o;

        if (projectMapper != null ? !projectMapper.equals(that.projectMapper) : that.projectMapper != null)
        {
            return false;
        }
        if (projectParser != null ? !projectParser.equals(that.projectParser) : that.projectParser != null)
        {
            return false;
        }

        return true;
    }

    ///CLOVER:ON

    ///CLOVER:OFF
    public int hashCode()
    {
        int result;
        result = (projectParser != null ? projectParser.hashCode() : 0);
        result = 31 * result + (projectMapper != null ? projectMapper.hashCode() : 0);
        return result;
    }
    ///CLOVER:ON
}
