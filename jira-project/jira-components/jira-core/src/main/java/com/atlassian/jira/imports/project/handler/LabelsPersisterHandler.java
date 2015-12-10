package com.atlassian.jira.imports.project.handler;

import com.atlassian.jira.exception.ParseException;
import com.atlassian.jira.external.beans.ExternalLabel;
import com.atlassian.jira.imports.project.ProjectImportPersister;
import com.atlassian.jira.imports.project.core.BackupSystemInformation;
import com.atlassian.jira.imports.project.core.ProjectImportResults;
import com.atlassian.jira.imports.project.mapper.ProjectImportMapper;
import com.atlassian.jira.imports.project.parser.LabelParser;
import com.atlassian.jira.imports.project.parser.LabelParserImpl;
import com.atlassian.jira.imports.project.transformer.LabelTransformer;
import com.atlassian.jira.imports.project.transformer.LabelTransformerImpl;
import com.atlassian.jira.issue.label.OfBizLabelStore;

import java.util.Map;
import java.util.concurrent.Executor;

/**
 * Reads, transforms, and stores all label entities from a backup file for the labels system field.
 * <p/>
 * It is assumed that all label data that is processed by this handler is relevant and should be saved.
 *
 * @since v4.2
 */
public class LabelsPersisterHandler extends AbstractPersisterHandler implements ImportEntityHandler
{
    private final ProjectImportPersister projectImportPersister;
    private final ProjectImportMapper projectImportMapper;
    private final ProjectImportResults projectImportResults;
    private final BackupSystemInformation backupSystemInformation;
    private LabelParser labelParser;
    private LabelTransformer labelTransformer;

    public LabelsPersisterHandler(final ProjectImportPersister projectImportPersister, final ProjectImportMapper projectImportMapper, final ProjectImportResults projectImportResults, final BackupSystemInformation backupSystemInformation, final Executor executor)
    {
        super(executor, projectImportResults);

        this.projectImportResults = projectImportResults;
        this.projectImportPersister = projectImportPersister;
        this.projectImportMapper = projectImportMapper;
        this.backupSystemInformation = backupSystemInformation;
    }

    public void handleEntity(final String entityName, final Map<String, String> attributes) throws ParseException, AbortImportException
    {
        if (OfBizLabelStore.TABLE.equals(entityName))
        {
            final ExternalLabel externalLabel = getLabelParser().parse(attributes);
            final ExternalLabel transformedExternalLabel = getLabelTransformer().transform(projectImportMapper, externalLabel);
            //only worry about persisting labels for the system field.
            if (transformedExternalLabel.getCustomFieldId() == null)
            {
                execute(new Runnable()
                {
                    public void run()
                    {
                        final Long labelId = projectImportPersister.createEntity(getLabelParser().getEntityRepresentation(transformedExternalLabel));
                        if (labelId == null)
                        {
                            final String issueKey = backupSystemInformation.getIssueKeyForId(externalLabel.getIssueId());
                            projectImportResults.addError(projectImportResults.getI18n().getText("admin.errors.project.import.label.error",
                                    externalLabel.getId(), issueKey));
                        }
                    }
                });
            }
        }
    }

    public void startDocument()
    {
    }

    public void endDocument()
    {
    }

    ///CLOVER:OFF
    LabelParser getLabelParser()
    {
        if (labelParser == null)
        {
            labelParser = new LabelParserImpl();
        }
        return labelParser;
    }
    ///CLOVER:ON

    ///CLOVER:OFF
    LabelTransformer getLabelTransformer()
    {
        if (labelTransformer == null)
        {
            labelTransformer = new LabelTransformerImpl();
        }
        return labelTransformer;
    }
}
