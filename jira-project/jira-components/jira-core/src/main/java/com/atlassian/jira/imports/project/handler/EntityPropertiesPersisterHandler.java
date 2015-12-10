package com.atlassian.jira.imports.project.handler;

import java.util.Map;
import java.util.concurrent.Executor;

import com.atlassian.jira.entity.property.EntityPropertyType;
import com.atlassian.jira.exception.ParseException;
import com.atlassian.jira.external.beans.ExternalEntityProperty;
import com.atlassian.jira.imports.project.ProjectImportPersister;
import com.atlassian.jira.imports.project.core.EntityRepresentation;
import com.atlassian.jira.imports.project.core.ProjectImportResults;
import com.atlassian.jira.imports.project.mapper.SimpleProjectImportIdMapper;
import com.atlassian.jira.imports.project.parser.EntityPropertyParser;
import com.atlassian.jira.imports.project.parser.EntityPropertyParserImpl;

import org.apache.log4j.Logger;

/**
 * Used to inspect issue properties entries in a backup file, transform the entities and persist them to the database.
 *
 * @since v6.2
 */
public class EntityPropertiesPersisterHandler extends AbstractPersisterHandler implements ImportEntityHandler
{

    private static final Logger log = Logger.getLogger(EntityPropertiesPersisterHandler.class);
    private final ProjectImportResults projectImportResults;
    private final ProjectImportPersister projectImportPersister;
    private final EntityPropertyType entityPropertyType;
    private final SimpleProjectImportIdMapper idMapperForType;
    private final EntityPropertyParser entityPropertyParser = new EntityPropertyParserImpl();

    public EntityPropertiesPersisterHandler(final Executor executor, final ProjectImportResults projectImportResults,
            final ProjectImportPersister projectImportPersister, final EntityPropertyType entityPropertyType, SimpleProjectImportIdMapper idMapperForType)
    {
        super(executor, projectImportResults);
        this.projectImportResults = projectImportResults;
        this.projectImportPersister = projectImportPersister;
        this.entityPropertyType = entityPropertyType;
        this.idMapperForType = idMapperForType;
    }

    @Override
    public void handleEntity(final String entityName, final Map<String, String> attributes)
            throws ParseException, AbortImportException
    {
        if (EntityPropertyParser.ENTITY_PROPERTY_ENTITY_NAME.equals(entityName))
        {
            final ExternalEntityProperty externalEntityProperty = getParser().parse(attributes);

            if (entityPropertyType.getDbEntityName().equals(externalEntityProperty.getEntityName()) && externalEntityProperty.getEntityId() != null)
            {
                final String newEntityIdStr = idMapperForType.getMappedId(String.valueOf(externalEntityProperty.getEntityId()));
                if (newEntityIdStr != null)
                {
                    final EntityRepresentation representation = getParser().getEntityRepresentation(externalEntityProperty, Long.valueOf(newEntityIdStr));
                    execute(new Runnable()
                    {
                        @Override
                        public void run()
                        {

                            final Long entityId = projectImportPersister.createEntity(representation);
                            if (entityId == null)
                            {
                                projectImportResults.addError(projectImportResults.getI18n().
                                        getText("admin.errors.project.import.entity.property.error",
                                                String.valueOf(externalEntityProperty.getId()),
                                                externalEntityProperty.getEntityName(),
                                                String.valueOf(externalEntityProperty.getEntityId())));
                            }
                        }
                    });
                }
                else
                {
                    log.debug("Ignoring entity property id=" + externalEntityProperty.getId() + " entityName = " + externalEntityProperty.getEntityName());
                }
            }
        }
    }


    @Override
    public void startDocument()
    {
//        / No-op
    }

    @Override
    public void endDocument()
    {
//        / No-op
    }

    private EntityPropertyParser getParser()
    {
        return entityPropertyParser;
    }
}
