package com.atlassian.jira.imports.project.handler;

import com.atlassian.jira.exception.ParseException;
import com.atlassian.jira.imports.project.ProjectImportPersister;
import com.atlassian.jira.imports.project.core.BackupSystemInformation;
import com.atlassian.jira.imports.project.core.ProjectImportResults;
import com.atlassian.jira.imports.project.customfield.ExternalCustomFieldValue;
import com.atlassian.jira.imports.project.customfield.ExternalCustomFieldValueImpl;
import com.atlassian.jira.imports.project.mapper.ProjectImportMapper;
import com.atlassian.jira.imports.project.parser.CustomFieldValueParser;
import com.atlassian.jira.imports.project.transformer.CustomFieldValueTransformer;
import com.atlassian.jira.imports.project.transformer.CustomFieldValueTransformerImpl;
import com.atlassian.jira.issue.CustomFieldManager;
import org.apache.log4j.Logger;

import java.util.Map;
import java.util.concurrent.Executor;

/**
 * Reads, transforms, and stores all custom field value entities from a backup file.
 *
 * It is assumed that all custom field values data that is processed by this handler is relevant and should be saved.
 *
 * @since v3.13
 */
public class CustomFieldValuePersisterHandler extends AbstractPersisterHandler implements ImportEntityHandler
{

    private static final Logger log = Logger.getLogger(CustomFieldValuePersisterHandler.class);
    private final ProjectImportPersister projectImportPersister;
    private final ProjectImportMapper projectImportMapper;
    private final CustomFieldManager customFieldManager;
    private final Long newProjectId;
    private final ProjectImportResults projectImportResults;
    private final BackupSystemInformation backupSystemInformation;
    private final Map<String, CustomFieldValueParser> parsers;
    private CustomFieldValueTransformer customFieldTransformer;

    public CustomFieldValuePersisterHandler(final ProjectImportPersister projectImportPersister, final ProjectImportMapper projectImportMapper, final CustomFieldManager customFieldManager, final Long newProjectId, final ProjectImportResults projectImportResults, final BackupSystemInformation backupSystemInformation, final Executor executor, final Map<String, CustomFieldValueParser> parsers)
    {
        super(executor, projectImportResults);
        this.projectImportPersister = projectImportPersister;
        this.projectImportMapper = projectImportMapper;
        this.customFieldManager = customFieldManager;
        this.newProjectId = newProjectId;
        this.projectImportResults = projectImportResults;
        this.backupSystemInformation = backupSystemInformation;
        this.parsers = parsers;
    }

    public void handleEntity(final String entityName, final Map<String, String> attributes) throws ParseException, AbortImportException
    {
        if(parsers.containsKey(entityName))
        {
            final ExternalCustomFieldValue externalCustomFieldValue = parsers.get(entityName).parse(attributes);
            if(externalCustomFieldValue != null)
            {
                final ExternalCustomFieldValue transformedExternalCustomFieldValue = getCustomFieldValueTransformer().transform(projectImportMapper,
                        externalCustomFieldValue, newProjectId);
                if (transformedExternalCustomFieldValue != null)
                {
                    execute(new Runnable()
                    {
                        public void run()
                        {
                            final Long customFieldValueId = projectImportPersister.createEntity(parsers.get(entityName).getEntityRepresentation(transformedExternalCustomFieldValue));
                            if (customFieldValueId == null)
                            {
                                final String issueKey = backupSystemInformation.getIssueKeyForId(externalCustomFieldValue.getIssueId());
                                projectImportResults.addError(projectImportResults.getI18n().getText(
                                        "admin.errors.project.import.custom.field.value.error", externalCustomFieldValue.getId(), issueKey));
                            }
                        }
                    });
                }
                else
                {
                    final String issueKey = backupSystemInformation.getIssueKeyForId(externalCustomFieldValue.getIssueId());
                    log.debug("Not persisting custom field value with old id '" + externalCustomFieldValue.getId() + "' for issue '" + issueKey + "', the transformer did not produce a transformed custom field value.");
                }
            }
        }
    }

    ///CLOVER:OFF
    CustomFieldValueTransformer getCustomFieldValueTransformer()
    {
        if (customFieldTransformer == null)
        {
            customFieldTransformer = new CustomFieldValueTransformerImpl(customFieldManager);
        }
        return customFieldTransformer;
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
