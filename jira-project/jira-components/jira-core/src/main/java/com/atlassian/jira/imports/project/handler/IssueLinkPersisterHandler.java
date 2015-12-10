package com.atlassian.jira.imports.project.handler;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.exception.ParseException;
import com.atlassian.jira.external.beans.ExternalLink;
import com.atlassian.jira.imports.project.ProjectImportPersister;
import com.atlassian.jira.imports.project.core.BackupSystemInformation;
import com.atlassian.jira.imports.project.core.ProjectImportResults;
import com.atlassian.jira.imports.project.mapper.ProjectImportMapper;
import com.atlassian.jira.imports.project.mapper.SimpleProjectImportIdMapper;
import com.atlassian.jira.imports.project.parser.IssueLinkParser;
import com.atlassian.jira.imports.project.parser.IssueLinkParserImpl;
import com.atlassian.jira.imports.project.transformer.IssueLinkTransformer;
import com.atlassian.jira.imports.project.transformer.IssueLinkTransformerImpl;
import com.atlassian.jira.issue.IssueManager;
import org.apache.log4j.Logger;

import java.util.Map;
import java.util.concurrent.Executor;

/**
 * Reads, transforms, and stores all issueLink entities from a backup file.
 *
 * It is assumed that all issueLink data that is processed by this handler is relevant and should be saved.
 *
 * @since v3.13
 */
public class IssueLinkPersisterHandler extends AbstractPersisterHandler implements ImportEntityHandler
{
    private static final Logger log = Logger.getLogger(IssueLinkPersisterHandler.class);

    private final ProjectImportPersister projectImportPersister;
    private final ProjectImportMapper projectImportMapper;
    private final IssueManager issueManager;
    private final ProjectImportResults projectImportResults;
    private final BackupSystemInformation backupSystemInformation;
    private final User importAuthor;
    private IssueLinkTransformer issueLinkTransformer;
    private IssueLinkParser issueLinkParserImpl;

    public IssueLinkPersisterHandler(final ProjectImportPersister projectImportPersister, final ProjectImportMapper projectImportMapper, final IssueManager issueManager, final ProjectImportResults projectImportResults, final BackupSystemInformation backupSystemInformation, final Executor executor, final User importAuthor)
    {
        super(executor, projectImportResults);

        this.projectImportPersister = projectImportPersister;
        this.projectImportMapper = projectImportMapper;
        this.issueManager = issueManager;
        this.projectImportResults = projectImportResults;
        this.backupSystemInformation = backupSystemInformation;
        this.importAuthor = importAuthor;
    }

    public void handleEntity(final String entityName, final Map<String, String> attributes) throws ParseException, AbortImportException
    {
        if (IssueLinkParser.ISSUE_LINK_ENTITY_NAME.equals(entityName))
        {
            final ExternalLink externalIssueLink = getIssueLinkParser().parse(attributes);
            // We have seen real world data with missing destination in a link
            if ((externalIssueLink.getSourceId() == null) || (externalIssueLink.getDestinationId() == null))
            {
                log.warn("Ignoring Issue Link id='" + externalIssueLink.getId() + "'; the source or destination is missing.");
                return;
            }
            // Let the Transformer transform this link
            final ExternalLink transformedIssueLink = getIssueLinkTransformer().transform(projectImportMapper, externalIssueLink);
            // Links to other projects are not always able to be created, so check for null.
            if (transformedIssueLink != null)
            {
                execute(new Runnable()
                {
                    public void run()
                    {
                        final Long linkId = projectImportPersister.createEntity(getIssueLinkParser().getEntityRepresentation(transformedIssueLink));
                        if (linkId == null)
                        {
                            final String sourceKey = backupSystemInformation.getIssueKeyForId(externalIssueLink.getSourceId());
                            final String destKey = backupSystemInformation.getIssueKeyForId(externalIssueLink.getDestinationId());
                            projectImportResults.addError(projectImportResults.getI18n().getText("admin.errors.project.import.issue.link.error",
                                sourceKey, destKey));
                        }
                        else
                        {
                            // Create the change item on the linked issue if it is outside of this project AND we need it
                            final boolean sourceOutsideProject = issueIsOutsideCurrentProject(externalIssueLink.getSourceId());
                            final boolean destOutsideProject = issueIsOutsideCurrentProject(externalIssueLink.getDestinationId());
                            if (sourceOutsideProject || destOutsideProject)
                            {
                                final String issueId = (sourceOutsideProject) ? transformedIssueLink.getSourceId() : transformedIssueLink.getDestinationId();
                                final SimpleProjectImportIdMapper issueMapper = projectImportMapper.getIssueMapper();
                                final String inProjectIssueKey = (sourceOutsideProject) ? backupSystemInformation.getIssueKeyForId(externalIssueLink.getDestinationId()) : backupSystemInformation.getIssueKeyForId(externalIssueLink.getSourceId());
                                final String issueKeyToReindex = projectImportPersister.createChangeItemForIssueLinkIfNeeded(issueId,
                                    transformedIssueLink.getLinkType(), inProjectIssueKey, sourceOutsideProject, importAuthor);
                                if (issueKeyToReindex != null)
                                {
                                    // Add this to the mapped issue keys so that it will be re-indexed
                                    issueMapper.mapValue(issueKeyToReindex, issueKeyToReindex);
                                }
                            }
                        }
                    }
                });
            }
        }
    }

    boolean issueIsOutsideCurrentProject(final String issueId)
    {
        final SimpleProjectImportIdMapper issueMapper = projectImportMapper.getIssueMapper();
        return issueMapper.getMappedId(issueId) == null;
    }

    ///CLOVER:OFF
    IssueLinkTransformer getIssueLinkTransformer()
    {
        if (issueLinkTransformer == null)
        {
            issueLinkTransformer = new IssueLinkTransformerImpl(issueManager, backupSystemInformation);
        }
        return issueLinkTransformer;
    }

    ///CLOVER:ON

    ///CLOVER:OFF
    IssueLinkParser getIssueLinkParser()
    {
        if (issueLinkParserImpl == null)
        {
            issueLinkParserImpl = new IssueLinkParserImpl();
        }
        return issueLinkParserImpl;
    }

    ///CLOVER:ON

    public void startDocument()
    {
    // No-op
    }

    public void endDocument()
    {
    // No-op
    }
}
