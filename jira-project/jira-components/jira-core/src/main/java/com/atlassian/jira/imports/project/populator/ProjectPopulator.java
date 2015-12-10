package com.atlassian.jira.imports.project.populator;

import com.atlassian.jira.exception.ParseException;
import com.atlassian.jira.external.beans.ExternalProject;
import com.atlassian.jira.imports.project.core.BackupOverviewBuilder;
import com.atlassian.jira.imports.project.parser.ProjectParser;
import com.atlassian.jira.imports.project.parser.ProjectParserImpl;

import java.util.Map;

/**
 * Converts the project information and sets it into {@link com.atlassian.jira.imports.project.core.BackupOverview}.
 *
 * @since v3.13
 */
public class ProjectPopulator implements BackupOverviewPopulator
{
    private ProjectParser projectParser;

    public void populate(final BackupOverviewBuilder backupOverviewBuilder, final String elementName, final Map attributes) throws ParseException
    {
        // Check if this is a "Project" entity element.
        if (ProjectParser.PROJECT_ENTITY_NAME.equals(elementName))
        {
            // Let the ProjectParser create the ExternalProject value from the XML attributes.
            final ExternalProject project = getProjectParser().parseProject(attributes);
            // Add this project to the BackupOverviewBuilder
            backupOverviewBuilder.addProject(project);
        }
        else
        {
            // The parser needs to be able to read OSProperty elements as well - in order to get the email from address.
            getProjectParser().parseOther(elementName, attributes);
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

}
