package com.atlassian.jira.imports.project.populator;

import com.atlassian.jira.exception.ParseException;
import com.atlassian.jira.external.beans.ExternalVersion;
import com.atlassian.jira.imports.project.core.BackupOverviewBuilder;
import com.atlassian.jira.imports.project.parser.ProjectVersionParser;
import com.atlassian.jira.imports.project.parser.ProjectVersionParserImpl;

import java.util.Map;

/**
 * Populates the {@link com.atlassian.jira.imports.project.core.BackupOverview} with version information.
 *
 * @since v3.13
 */
public class ProjectVersionPopulator implements BackupOverviewPopulator
{
    private ProjectVersionParser projectVersionParser;

    public void populate(final BackupOverviewBuilder backupOverviewBuilder, final String elementName, final Map attributes) throws ParseException
    {
        if (ProjectVersionParser.VERSION_ENTITY_NAME.equals(elementName))
        {
            final ExternalVersion externalVersion = getProjectVersionParser().parse(attributes);
            backupOverviewBuilder.addVersion(externalVersion);
        }
    }

    ProjectVersionParser getProjectVersionParser()
    {
        if (projectVersionParser == null)
        {
            projectVersionParser = new ProjectVersionParserImpl();
        }
        return projectVersionParser;
    }
}
