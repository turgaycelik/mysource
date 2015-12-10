package com.atlassian.jira.imports.project.handler;

import com.atlassian.jira.exception.ParseException;
import com.atlassian.jira.external.beans.ExternalWorklog;
import com.atlassian.jira.imports.project.ProjectImportPersister;
import com.atlassian.jira.imports.project.core.BackupSystemInformation;
import com.atlassian.jira.imports.project.core.ProjectImportResults;
import com.atlassian.jira.imports.project.mapper.ProjectImportMapper;
import com.atlassian.jira.imports.project.parser.WorklogParser;
import com.atlassian.jira.imports.project.parser.WorklogParserImpl;
import com.atlassian.jira.imports.project.transformer.WorklogTransformer;
import com.atlassian.jira.imports.project.transformer.WorklogTransformerImpl;
import org.apache.log4j.Logger;

import java.util.Map;
import java.util.concurrent.Executor;

/**
 * Reads, transforms, and stores all worklog entities from a backup file.
 *
 * It is assumed that all worklog data that is processed by this handler is relevant and should be saved.
 *
 * @since v3.13
 */
public class WorklogPersisterHandler extends AbstractPersisterHandler implements ImportEntityHandler
{
    private static final Logger log = Logger.getLogger(WorklogPersisterHandler.class);

    private final ProjectImportPersister projectImportPersister;
    private final ProjectImportMapper projectImportMapper;
    private final ProjectImportResults projectImportResults;
    private final BackupSystemInformation backupSystemInformation;
    private WorklogParser worklogParser;
    private WorklogTransformer worklogTransformer;

    public WorklogPersisterHandler(final ProjectImportPersister projectImportPersister, final ProjectImportMapper projectImportMapper, final ProjectImportResults projectImportResults, final BackupSystemInformation backupSystemInformation, final Executor executor)
    {
        super(executor, projectImportResults);

        this.projectImportPersister = projectImportPersister;
        this.projectImportMapper = projectImportMapper;
        this.projectImportResults = projectImportResults;
        this.backupSystemInformation = backupSystemInformation;
    }

    public void handleEntity(final String entityName, final Map<String, String> attributes) throws ParseException, AbortImportException
    {
        if (WorklogParser.WORKLOG_ENTITY_NAME.equals(entityName))
        {
            final ExternalWorklog externalWorklog = getWorklogParser().parse(attributes);
            final ExternalWorklog transformedExternalWorklog = getWorklogTransformer().transform(projectImportMapper, externalWorklog);
            if (transformedExternalWorklog.getIssueId() != null)
            {
                execute(new Runnable()
                {
                    public void run()
                    {
                        final Long worklogId = projectImportPersister.createEntity(getWorklogParser().getEntityRepresentation(
                            transformedExternalWorklog));
                        if (worklogId == null)
                        {
                            final String issueKey = backupSystemInformation.getIssueKeyForId(externalWorklog.getIssueId());
                            projectImportResults.addError(projectImportResults.getI18n().getText("admin.errors.project.import.worklog.error",
                                externalWorklog.getId(), issueKey));
                        }
                    }
                });
            }
            else
            {
                final String issueKey = backupSystemInformation.getIssueKeyForId(externalWorklog.getIssueId());
                log.warn("Not saving worklog '" + externalWorklog.getId() + "' it appears that the issue '" + issueKey + "' was not created as part of the import.");
            }
        }
    }

    ///CLOVER:OFF
    WorklogParser getWorklogParser()
    {
        if (worklogParser == null)
        {
            worklogParser = new WorklogParserImpl();
        }
        return worklogParser;
    }

    ///CLOVER:ON

    ///CLOVER:OFF
    WorklogTransformer getWorklogTransformer()
    {
        if (worklogTransformer == null)
        {
            worklogTransformer = new WorklogTransformerImpl();
        }
        return worklogTransformer;
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
