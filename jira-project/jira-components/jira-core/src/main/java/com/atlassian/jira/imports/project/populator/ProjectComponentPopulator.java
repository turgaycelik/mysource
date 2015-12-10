package com.atlassian.jira.imports.project.populator;

import com.atlassian.jira.exception.ParseException;
import com.atlassian.jira.external.beans.ExternalComponent;
import com.atlassian.jira.imports.project.core.BackupOverviewBuilder;
import com.atlassian.jira.imports.project.parser.ProjectComponentParser;
import com.atlassian.jira.imports.project.parser.ProjectComponentParserImpl;

import java.util.Map;

/**
 * Populates a {@link com.atlassian.jira.imports.project.core.BackupOverview} with project component information.
 *
 * @since v3.13
 */
public class ProjectComponentPopulator implements BackupOverviewPopulator
{
    private ProjectComponentParser projectComponentParser;

    public void populate(final BackupOverviewBuilder backupOverviewBuilder, final String elementName, final Map attributes) throws ParseException
    {
        if (ProjectComponentParser.COMPONENT_ENTITY_NAME.equals(elementName))
        {
            final ExternalComponent externalComponent = getProjectComponentParser().parse(attributes);
            backupOverviewBuilder.addComponent(externalComponent);
        }
    }

    ProjectComponentParser getProjectComponentParser()
    {
        if (projectComponentParser == null)
        {
            projectComponentParser = new ProjectComponentParserImpl();
        }
        return projectComponentParser;
    }
}
