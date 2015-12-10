package com.atlassian.jira.imports.project.handler;

import com.atlassian.jira.exception.ParseException;
import com.atlassian.jira.external.beans.ExternalNodeAssociation;
import com.atlassian.jira.imports.project.ProjectImportPersister;
import com.atlassian.jira.imports.project.core.BackupSystemInformation;
import com.atlassian.jira.imports.project.core.ProjectImportResults;
import com.atlassian.jira.imports.project.mapper.ProjectImportMapper;
import com.atlassian.jira.imports.project.parser.NodeAssociationParser;
import com.atlassian.jira.imports.project.parser.NodeAssociationParserImpl;
import com.atlassian.jira.imports.project.transformer.ComponentTransformer;
import com.atlassian.jira.imports.project.transformer.ComponentTransformerImpl;
import org.apache.log4j.Logger;

import java.util.Map;
import java.util.concurrent.Executor;

/**
 * Reads, transforms, and stores all component entities from a backup file.
 *
 * It is assumed that all component data that is processed by this handler is relevant and should be saved.
 *
 * @since v3.13
 */
public class ComponentPersisterHandler extends AbstractPersisterHandler implements ImportEntityHandler
{
    private static final Logger log = Logger.getLogger(ComponentPersisterHandler.class);

    private final ProjectImportPersister projectImportPersister;
    private final ProjectImportMapper projectImportMapper;
    private final ProjectImportResults projectImportResults;
    private final BackupSystemInformation backupSystemInformation;
    private NodeAssociationParser nodeAssocationParser;
    private ComponentTransformer componentTransformer;

    public ComponentPersisterHandler(final ProjectImportPersister projectImportPersister, final ProjectImportMapper projectImportMapper, final ProjectImportResults projectImportResults, final BackupSystemInformation backupSystemInformation, final Executor executor)
    {
        super(executor, projectImportResults);
        this.projectImportPersister = projectImportPersister;
        this.projectImportMapper = projectImportMapper;
        this.projectImportResults = projectImportResults;
        this.backupSystemInformation = backupSystemInformation;
    }

    public void handleEntity(final String entityName, final Map<String, String> attributes) throws ParseException, AbortImportException
    {
        if (NodeAssociationParser.NODE_ASSOCIATION_ENTITY_NAME.equals(entityName))
        {
            final ExternalNodeAssociation externalComponent = getNodeAssociationParser().parse(attributes);
            if ((externalComponent != null) && NodeAssociationParser.COMPONENT_TYPE.equals(externalComponent.getAssociationType()))
            {
                final ExternalNodeAssociation transformedExternalComponent = getComponentTransformer().transform(projectImportMapper,
                    externalComponent);
                if (transformedExternalComponent != null)
                {
                    if (transformedExternalComponent.getSourceNodeId() != null)
                    {
                        execute(new Runnable()
                        {
                            public void run()
                            {
                                if (!projectImportPersister.createAssociation(transformedExternalComponent))
                                {
                                    final String issueKey = backupSystemInformation.getIssueKeyForId(externalComponent.getSourceNodeId());
                                    final String compName = projectImportMapper.getComponentMapper().getDisplayName(externalComponent.getSinkNodeId());
                                    projectImportResults.addError(projectImportResults.getI18n().getText(
                                        "admin.errors.project.import.component.error", compName, issueKey));
                                }
                            }
                        });
                    }
                    else
                    {
                        final String issueKey = backupSystemInformation.getIssueKeyForId(externalComponent.getSourceNodeId());
                        final String compName = projectImportMapper.getComponentMapper().getDisplayName(externalComponent.getSinkNodeId());
                        log.warn("Not saving component '" + compName + "' it appears that the issue '" + issueKey + "' was not created as part of the import.");
                    }
                }
            }
        }
    }

    ///CLOVER:OFF
    ComponentTransformer getComponentTransformer()
    {
        if (componentTransformer == null)
        {
            componentTransformer = new ComponentTransformerImpl();
        }
        return componentTransformer;
    }

    ///CLOVER:ON

    ///CLOVER:OFF
    NodeAssociationParser getNodeAssociationParser()
    {
        if (nodeAssocationParser == null)
        {
            nodeAssocationParser = new NodeAssociationParserImpl();
        }
        return nodeAssocationParser;
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
