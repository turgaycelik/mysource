package com.atlassian.jira.imports.project.handler;

import com.atlassian.jira.exception.ParseException;
import com.atlassian.jira.external.beans.ExternalComment;
import com.atlassian.jira.imports.project.ProjectImportPersister;
import com.atlassian.jira.imports.project.core.BackupSystemInformation;
import com.atlassian.jira.imports.project.core.ProjectImportResults;
import com.atlassian.jira.imports.project.mapper.ProjectImportMapper;
import com.atlassian.jira.imports.project.parser.CommentParser;
import com.atlassian.jira.imports.project.parser.CommentParserImpl;
import com.atlassian.jira.imports.project.transformer.CommentTransformer;
import com.atlassian.jira.imports.project.transformer.CommentTransformerImpl;
import org.apache.log4j.Logger;

import java.util.Map;
import java.util.concurrent.Executor;

/**
 * Reads, transforms, and stores all comments entities from a backup file.
 *
 * It is assumed that all comment data that is processed by this handler is relevant and should be saved.
 *
 * @since v3.13
 */
public class CommentPersisterHandler extends AbstractPersisterHandler implements ImportEntityHandler
{
    private static final Logger log = Logger.getLogger(CommentPersisterHandler.class);

    private final ProjectImportPersister projectImportPersister;
    private final ProjectImportMapper projectImportMapper;
    private final ProjectImportResults projectImportResults;
    private final BackupSystemInformation backupSystemInformation;
    private CommentParser commentParser;
    private CommentTransformer commentTransformer;

    public CommentPersisterHandler(final ProjectImportPersister projectImportPersister, final ProjectImportMapper projectImportMapper, final ProjectImportResults projectImportResults, final BackupSystemInformation backupSystemInformation, final Executor executor)
    {
        super(executor, projectImportResults);
        this.projectImportPersister = projectImportPersister;
        this.projectImportMapper = projectImportMapper;
        this.projectImportResults = projectImportResults;
        this.backupSystemInformation = backupSystemInformation;
    }

    public void handleEntity(final String entityName, final Map<String, String> attributes) throws ParseException, AbortImportException
    {
        if (CommentParser.COMMENT_ENTITY_NAME.equals(entityName))
        {
            final ExternalComment externalComment = getCommentParser().parse(attributes);
            // We only handle comments, there can be the case where we have a different "type" stored in the action table
            if (externalComment != null)
            {
                final ExternalComment transformedExternalComment = getCommentTransformer().transform(projectImportMapper, externalComment);
                if (transformedExternalComment.getIssueId() != null)
                {
                    execute(new Runnable()
                    {
                        public void run()
                        {
                            final Long commentId = projectImportPersister.createEntity(getCommentParser().getEntityRepresentation(transformedExternalComment));
                            if (externalComment.getId() != null)
                            {
                                projectImportMapper.getCommentMapper().mapValue(String.valueOf(externalComment.getId()), String.valueOf(commentId));
                            }

                            if (commentId == null)
                            {
                                final String issueKey = backupSystemInformation.getIssueKeyForId(externalComment.getIssueId());
                                projectImportResults.addError(projectImportResults.getI18n().getText("admin.errors.project.import.comment.error",
                                    externalComment.getId(), issueKey));
                            }
                        }
                    });
                }
                else
                {
                    final String issueKey = backupSystemInformation.getIssueKeyForId(externalComment.getIssueId());
                    log.warn("Not saving comment '" + externalComment.getId() + "' it appears that the issue '" + issueKey + "' was not created as part of the import.");
                }
            }
        }
    }

    ///CLOVER:OFF
    CommentParser getCommentParser()
    {
        if (commentParser == null)
        {
            commentParser = new CommentParserImpl();
        }
        return commentParser;
    }

    ///CLOVER:ON

    ///CLOVER:OFF
    CommentTransformer getCommentTransformer()
    {
        if (commentTransformer == null)
        {
            commentTransformer = new CommentTransformerImpl();
        }
        return commentTransformer;
    }

    ///CLOVER:ON

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
}
