package com.atlassian.jira.imports.project.handler;

import com.atlassian.jira.exception.ParseException;
import com.atlassian.jira.external.beans.ExternalChangeItem;
import com.atlassian.jira.imports.project.ProjectImportPersister;
import com.atlassian.jira.imports.project.core.ProjectImportResults;
import com.atlassian.jira.imports.project.mapper.ProjectImportMapper;
import com.atlassian.jira.imports.project.parser.ChangeItemParser;
import com.atlassian.jira.imports.project.parser.ChangeItemParserImpl;
import com.atlassian.jira.imports.project.transformer.ChangeItemTransformer;
import com.atlassian.jira.imports.project.transformer.ChangeItemTransformerImpl;
import org.apache.log4j.Logger;

import java.util.Map;
import java.util.concurrent.Executor;

/**
 * Reads, transforms, and stores all ChangeItem entities from a backup file.
 *
 * @since v3.13
 */
public class ChangeItemPersisterHandler extends AbstractPersisterHandler implements ImportEntityHandler
{
    private static final Logger log = Logger.getLogger(ChangeItemPersisterHandler.class);

    private final ProjectImportPersister projectImportPersister;
    private final ProjectImportMapper projectImportMapper;
    private final ProjectImportResults projectImportResults;
    private ChangeItemTransformer changeItemTransformer;
    private ChangeItemParser changeItemParser;

    public ChangeItemPersisterHandler(final ProjectImportPersister projectImportPersister, final ProjectImportMapper projectImportMapper, final ProjectImportResults projectImportResults, final Executor executor)
    {
        super(executor, projectImportResults);
        this.projectImportPersister = projectImportPersister;
        this.projectImportMapper = projectImportMapper;
        this.projectImportResults = projectImportResults;
    }

    public void handleEntity(final String entityName, final Map<String, String> attributes) throws ParseException, AbortImportException
    {
        if (ChangeItemParser.CHANGE_ITEM_ENTITY_NAME.equals(entityName))
        {
            final ExternalChangeItem oldChangeItem = getChangeItemParser().parse(attributes);
            // Transform this to use the new Change Group ID
            final ExternalChangeItem newChangeItem = getChangeItemTransformer().transform(projectImportMapper, oldChangeItem);
            if (newChangeItem.getChangeGroupId() != null)
            {
                execute(new Runnable()
                {
                    public void run()
                    {
                        final Long changeItemId = projectImportPersister.createEntity(getChangeItemParser().getEntityRepresentation(newChangeItem));
                        if (changeItemId == null)
                        {
                            projectImportResults.addError(projectImportResults.getI18n().getText("admin.errors.project.import.change.item.error",
                                oldChangeItem.getId(), oldChangeItem.getChangeGroupId()));
                        }
                    }
                });
            }
            else
            {
                log.warn("Not creating change item with id '" + oldChangeItem.getId() + "' for backup change group with id '" + oldChangeItem.getChangeGroupId() + "', the change group has not been mapped in the new system.");
            }
        }
    }

    ///CLOVER:OFF
    ChangeItemTransformer getChangeItemTransformer()
    {
        if (changeItemTransformer == null)
        {
            changeItemTransformer = new ChangeItemTransformerImpl();
        }
        return changeItemTransformer;
    }

    ///CLOVER:ON

    ///CLOVER:OFF
    ChangeItemParser getChangeItemParser()
    {
        if (changeItemParser == null)
        {
            changeItemParser = new ChangeItemParserImpl();
        }
        return changeItemParser;
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
