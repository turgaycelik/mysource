package com.atlassian.jira.imports.project.populator;

import com.atlassian.jira.exception.ParseException;
import com.atlassian.jira.external.beans.ExternalIssue;
import com.atlassian.jira.imports.project.core.BackupOverviewBuilder;
import com.atlassian.jira.imports.project.parser.IssueParser;
import com.atlassian.jira.imports.project.parser.IssueParserImpl;

import java.util.Map;

/**
 * Populates the issue id's in the BackupOverview object
 *
 * @since v3.13
 */
public class IssueIdPopulator implements BackupOverviewPopulator
{
    private IssueParser issueParser;

    public void populate(final BackupOverviewBuilder backupOverviewBuilder, final String elementName, final Map attributes) throws ParseException
    {
        if (IssueParser.ISSUE_ENTITY_NAME.equals(elementName))
        {
            final ExternalIssue issue = getIssueParser().parse(attributes);
            backupOverviewBuilder.addIssue(issue);
        }
    }

    IssueParser getIssueParser()
    {
        if (issueParser == null)
        {
            issueParser = new IssueParserImpl();
        }
        return issueParser;
    }
}
