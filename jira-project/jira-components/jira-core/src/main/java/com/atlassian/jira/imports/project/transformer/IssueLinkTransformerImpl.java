package com.atlassian.jira.imports.project.transformer;

import com.atlassian.jira.external.beans.ExternalLink;
import com.atlassian.jira.imports.project.core.BackupSystemInformation;
import com.atlassian.jira.imports.project.mapper.ProjectImportMapper;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;

/**
 * @since v3.13
 */
public class IssueLinkTransformerImpl implements IssueLinkTransformer
{
    private final IssueManager issueManager;
    private final BackupSystemInformation backupSystemInformation;

    public IssueLinkTransformerImpl(final IssueManager issueManager, final BackupSystemInformation backupSystemInformation)
    {
        this.issueManager = issueManager;
        this.backupSystemInformation = backupSystemInformation;
    }

    public ExternalLink transform(final ProjectImportMapper projectImportMapper, final ExternalLink oldIssueLink)
    {
        // try to get a new Issue ID for the source
        final String newSourceID = getNewIssueID(oldIssueLink.getSourceId(), projectImportMapper);
        if (newSourceID == null)
        {
            return null;
        }
        // try to get a new Issue ID for the destination
        final String newDestinationID = getNewIssueID(oldIssueLink.getDestinationId(), projectImportMapper);
        if (newDestinationID == null)
        {
            return null;
        }
        final ExternalLink newLink = new ExternalLink();
        // Get the new Link Type ID from the Mapper
        newLink.setLinkType(projectImportMapper.getIssueLinkTypeMapper().getMappedId(oldIssueLink.getLinkType()));
        newLink.setSourceId(newSourceID);
        newLink.setDestinationId(newDestinationID);
        newLink.setSequence(oldIssueLink.getSequence());
        // we don't set the ID, as this will be created when we add it to DB.
        // We don't set the linkname, as this is just an alternative to the linktype that is used for imports from non-JIRA systems.
        return newLink;
    }

    private String getNewIssueID(final String oldIssueId, final ProjectImportMapper projectImportMapper)
    {
        // Firstly try to use the Issue Mapper.
        // This will work for all issues in the imported project, and be quick because it doesn't have to go to the DB.
        final String newIssueId = projectImportMapper.getIssueMapper().getMappedId(oldIssueId);
        if (newIssueId != null)
        {
            return newIssueId;
        }

        // This must be a link to an issue in another project.
        // We look it up by key in the current system.
        final String key = backupSystemInformation.getIssueKeyForId(oldIssueId);
        if (key == null)
        {
            // This would only happen for orphan data - the Issue ID in the link does not have an Issue in the backup.
            return null;
        }
        final Issue issue = issueManager.getIssueObject(key);
        if (issue == null)
        {
            // No issue with the given key in the current system - we return null to indicate "nothing to import".
            return null;
        }
        return issue.getId().toString();
    }
}
