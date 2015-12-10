package com.atlassian.jira.imports.project.handler;

import com.atlassian.jira.exception.ParseException;
import com.atlassian.jira.external.beans.ExternalIssueLinkType;
import com.atlassian.jira.external.beans.ExternalLink;
import com.atlassian.jira.imports.project.core.BackupProject;
import com.atlassian.jira.imports.project.core.BackupSystemInformation;
import com.atlassian.jira.imports.project.mapper.IssueLinkTypeMapper;
import com.atlassian.jira.imports.project.parser.IssueLinkParser;
import com.atlassian.jira.imports.project.parser.IssueLinkParserImpl;
import com.atlassian.jira.imports.project.parser.IssueLinkTypeParser;
import com.atlassian.jira.imports.project.parser.IssueLinkTypeParserImpl;
import com.atlassian.jira.issue.IssueManager;

import java.util.Map;

/**
 * Populates mappers used in importing Issue Links.
 * This actually affects the IssueLinkType mapper, as this is the mapper used for Issue Link importing.
 *
 * @since v3.13
 */
public class IssueLinkMapperHandler implements ImportEntityHandler
{
    private IssueLinkParser issueLinkParser;
    private IssueLinkTypeParser issueLinkTypeParser;
    private final BackupSystemInformation backupSystemInformation;
    private final IssueManager issueManager;
    private final IssueLinkTypeMapper issueLinkTypeMapper;
    private final BackupProject backupProject;

    public IssueLinkMapperHandler(final BackupProject backupProject, final BackupSystemInformation backupSystemInformation, final IssueManager issueManager, final IssueLinkTypeMapper issueLinkTypeMapper)
    {
        this.backupProject = backupProject;
        this.backupSystemInformation = backupSystemInformation;
        this.issueManager = issueManager;
        this.issueLinkTypeMapper = issueLinkTypeMapper;
    }

    public void handleEntity(final String entityName, final Map<String, String> attributes) throws ParseException
    {
        // IssueLink
        if (IssueLinkParser.ISSUE_LINK_ENTITY_NAME.equals(entityName))
        {
            // Parse the IssueLink
            final ExternalLink issueLink = getIssueLinkParser().parse(attributes);
            // Rubbish data can sometimes contain a null source or destination, but this will be OK, as the containsIssue() returns false.
            // Now check if the Source Issue is in our project.
            if (backupProject.containsIssue(issueLink.getSourceId()))
            {
                // Source is in imported project. Check the Destination.
                if (backupProject.containsIssue(issueLink.getDestinationId()))
                {
                    // Both the source and destination are in the imported project - we definitely import this link.
                    issueLinkTypeMapper.flagValueAsRequired(issueLink.getLinkType());
                }
                else
                {
                    // Destination is external - we only import this link if we have the destination issue in the current system.
                    if (currentSystemContainsIssueKey(backupSystemInformation.getIssueKeyForId(issueLink.getDestinationId())))
                    {
                        issueLinkTypeMapper.flagValueAsRequired(issueLink.getLinkType());
                    }
                }
            }
            else
            {
                // Source is in an external project - check the destination
                if (backupProject.containsIssue(issueLink.getDestinationId()))
                {
                    // Source is external and Destination is in the imported project - we only import this link if we have the Source issue in the current system.
                    if (currentSystemContainsIssueKey(backupSystemInformation.getIssueKeyForId(issueLink.getSourceId())))
                    {
                        issueLinkTypeMapper.flagValueAsRequired(issueLink.getLinkType());
                    }
                }
            }
        }
        // IssueLinkType
        else if (IssueLinkTypeParser.ISSUE_LINK_TYPE_ENTITY_NAME.equals(entityName))
        {
            // Parse the IssueLinkType
            final ExternalIssueLinkType issueLinkType = getIssueLinkTypeParser().parse(attributes);
            // register this IssueLinkType in the Mapper
            issueLinkTypeMapper.registerOldValue(issueLinkType.getId(), issueLinkType.getLinkname(), issueLinkType.getStyle());
        }
    }

    public void startDocument()
    {
    // No-op
    }

    public void endDocument()
    {
    // No-op
    }

    private boolean currentSystemContainsIssueKey(final String issueKey)
    {
        return issueManager.getIssueObject(issueKey) != null;
    }

    ///CLOVER:OFF
    IssueLinkParser getIssueLinkParser()
    {
        if (issueLinkParser == null)
        {
            issueLinkParser = new IssueLinkParserImpl();
        }
        return issueLinkParser;
    }

    ///CLOVER:ON

    ///CLOVER:OFF
    IssueLinkTypeParser getIssueLinkTypeParser()
    {
        if (issueLinkTypeParser == null)
        {
            issueLinkTypeParser = new IssueLinkTypeParserImpl();
        }
        return issueLinkTypeParser;
    }
    ///CLOVER:ON
}
