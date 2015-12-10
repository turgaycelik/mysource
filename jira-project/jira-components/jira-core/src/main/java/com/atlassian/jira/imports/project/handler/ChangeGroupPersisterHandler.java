package com.atlassian.jira.imports.project.handler;

import com.atlassian.jira.exception.ParseException;
import com.atlassian.jira.external.beans.ExternalChangeGroup;
import com.atlassian.jira.imports.project.ProjectImportPersister;
import com.atlassian.jira.imports.project.core.BackupSystemInformation;
import com.atlassian.jira.imports.project.core.ProjectImportResults;
import com.atlassian.jira.imports.project.mapper.ProjectImportMapper;
import com.atlassian.jira.imports.project.parser.ChangeGroupParser;
import com.atlassian.jira.imports.project.parser.ChangeGroupParserImpl;
import com.atlassian.jira.imports.project.transformer.ChangeGroupTransformer;
import com.atlassian.jira.imports.project.transformer.ChangeGroupTransformerImpl;
import org.apache.log4j.Logger;

import java.util.Map;
import java.util.concurrent.Executor;

/**
 * Reads, transforms, and stores all ChangeGroup entities from a backup file.
 *
 * @since v3.13
 */
public class ChangeGroupPersisterHandler extends AbstractPersisterHandler implements ImportEntityHandler
{
    private static final Logger log = Logger.getLogger(ChangeGroupPersisterHandler.class);

    private final ProjectImportPersister projectImportPersister;
    private final ProjectImportMapper projectImportMapper;
    private final ProjectImportResults projectImportResults;
    private final BackupSystemInformation backupSystemInformation;
    private ChangeGroupParser changegroupParser;
    private ChangeGroupTransformer changeGroupTransformer;

    public ChangeGroupPersisterHandler(final ProjectImportPersister projectImportPersister, final ProjectImportMapper projectImportMapper, final ProjectImportResults projectImportResults, final BackupSystemInformation backupSystemInformation, final Executor executor)
    {
        super(executor, projectImportResults);

        this.projectImportPersister = projectImportPersister;
        this.projectImportMapper = projectImportMapper;
        this.projectImportResults = projectImportResults;
        this.backupSystemInformation = backupSystemInformation;
    }

    public void handleEntity(final String entityName, final Map<String, String> attributes) throws ParseException, AbortImportException
    {
        if (ChangeGroupParser.CHANGE_GROUP_ENTITY_NAME.equals(entityName))
        {
            final ExternalChangeGroup externalChangeGroup = getChangeGroupParser().parse(attributes);
            final ExternalChangeGroup transformedChangeGroup = getChangeGroupTransformer().transform(projectImportMapper, externalChangeGroup);
            // This can be null if the issue was not actually created
            if (transformedChangeGroup.getIssueId() != null)
            {
                execute(new Runnable()
                {
                    public void run()
                    {
                        final Long changeGroupId = projectImportPersister.createEntity(getChangeGroupParser().getEntityRepresentation(
                            transformedChangeGroup));
                        if (changeGroupId != null)
                        {
                            // Map the changeGroupId so that the changeItems can use this information
                            projectImportMapper.getChangeGroupMapper().mapValue(externalChangeGroup.getId(), changeGroupId.toString());
                        }
                        else
                        {
                            final String issueKey = backupSystemInformation.getIssueKeyForId(externalChangeGroup.getIssueId());
                            projectImportResults.addError(projectImportResults.getI18n().getText("admin.errors.project.import.change.group.error",
                                externalChangeGroup.getId(), issueKey));
                        }
                    }
                });
            }
            else
            {
                final String issueKey = backupSystemInformation.getIssueKeyForId(externalChangeGroup.getIssueId());
                log.warn("Not creating change group with id '" + externalChangeGroup.getId() + "' for backup issue '" + issueKey + "', the issue has not been mapped in the new system.");
            }
        }
    }

    ///CLOVER:OFF
    ChangeGroupTransformer getChangeGroupTransformer()
    {
        if (changeGroupTransformer == null)
        {
            changeGroupTransformer = new ChangeGroupTransformerImpl();
        }
        return changeGroupTransformer;
    }

    ///CLOVER:ON

    ///CLOVER:OFF
    ChangeGroupParser getChangeGroupParser()
    {
        if (changegroupParser == null)
        {
            changegroupParser = new ChangeGroupParserImpl();
        }
        return changegroupParser;
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
