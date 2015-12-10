package com.atlassian.jira.imports.project.handler;

import com.atlassian.jira.exception.ParseException;
import com.atlassian.jira.external.beans.ExternalNodeAssociation;
import com.atlassian.jira.imports.project.ProjectImportPersister;
import com.atlassian.jira.imports.project.core.BackupSystemInformation;
import com.atlassian.jira.imports.project.core.ProjectImportResults;
import com.atlassian.jira.imports.project.mapper.ProjectImportMapper;
import com.atlassian.jira.imports.project.parser.NodeAssociationParser;
import com.atlassian.jira.imports.project.parser.NodeAssociationParserImpl;
import com.atlassian.jira.imports.project.transformer.VersionTransformer;
import com.atlassian.jira.imports.project.transformer.VersionTransformerImpl;
import org.apache.log4j.Logger;

import java.util.Map;
import java.util.concurrent.Executor;

/**
 * Reads, transforms, and stores all version entities from a backup file.
 *
 * It is assumed that all version data that is processed by this handler is relevant and should be saved.
 *
 * @since v3.13
 */
public class VersionPersisterHandler extends AbstractPersisterHandler implements ImportEntityHandler
{
    private static final Logger log = Logger.getLogger(VersionPersisterHandler.class);

    private final ProjectImportPersister projectImportPersister;
    private final ProjectImportMapper projectImportMapper;
    private final ProjectImportResults projectImportResults;
    private final BackupSystemInformation backupSystemInformation;
    private NodeAssociationParser nodeAssocationParser;
    private VersionTransformer versionTransformer;

    public VersionPersisterHandler(final ProjectImportPersister projectImportPersister, final ProjectImportMapper projectImportMapper, final ProjectImportResults projectImportResults, final BackupSystemInformation backupSystemInformation, final Executor executor)
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
            final ExternalNodeAssociation externalVersion = getNodeAssociationParser().parse(attributes);
            if ((externalVersion != null) && (NodeAssociationParser.AFFECTS_VERSION_TYPE.equals(externalVersion.getAssociationType()) || NodeAssociationParser.FIX_VERSION_TYPE.equals(externalVersion.getAssociationType())))
            {
                final ExternalNodeAssociation tranformedExternalVersion = getVersionTransformer().transform(projectImportMapper, externalVersion);
                if (tranformedExternalVersion != null)
                {
                    // If the transformed issue id exists then lets create
                    if (tranformedExternalVersion.getSourceNodeId() != null)
                    {
                        execute(new Runnable()
                        {
                            public void run()
                            {
                                if (!projectImportPersister.createAssociation(tranformedExternalVersion))
                                {
                                    final String issueKey = backupSystemInformation.getIssueKeyForId(externalVersion.getSourceNodeId());
                                    final String versionName = projectImportMapper.getVersionMapper().getDisplayName(externalVersion.getSinkNodeId());
                                    if (NodeAssociationParser.AFFECTS_VERSION_TYPE.equals(externalVersion.getAssociationType()))
                                    {
                                        projectImportResults.addError(projectImportResults.getI18n().getText(
                                            "admin.errors.project.import.version.affects.error", versionName, issueKey));
                                    }
                                    if (NodeAssociationParser.FIX_VERSION_TYPE.equals(externalVersion.getAssociationType()))
                                    {
                                        projectImportResults.addError(projectImportResults.getI18n().getText(
                                            "admin.errors.project.import.version.fixfor.error", versionName, issueKey));
                                    }
                                }
                            }
                        });
                    }
                    else
                    {
                        final String issueKey = backupSystemInformation.getIssueKeyForId(externalVersion.getSourceNodeId());
                        final String versionName = projectImportMapper.getVersionMapper().getDisplayName(externalVersion.getSinkNodeId());
                        if (NodeAssociationParser.AFFECTS_VERSION_TYPE.equals(externalVersion.getAssociationType()))
                        {
                            log.warn("Not saving affects version '" + versionName + "' it appears that the issue '" + issueKey + "' was not created as part of the import.");
                        }
                        if (NodeAssociationParser.FIX_VERSION_TYPE.equals(externalVersion.getAssociationType()))
                        {
                            log.warn("Not saving fix version '" + versionName + "' it appears that the issue '" + issueKey + "' was not created as part of the import.");
                        }
                    }
                }
            }
        }

    }

    ///CLOVER:OFF
    VersionTransformer getVersionTransformer()
    {
        if (versionTransformer == null)
        {
            versionTransformer = new VersionTransformerImpl();
        }
        return versionTransformer;
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
