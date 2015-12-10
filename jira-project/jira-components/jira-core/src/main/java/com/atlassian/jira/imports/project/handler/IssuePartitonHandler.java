package com.atlassian.jira.imports.project.handler;

import java.io.PrintWriter;
import java.util.Map;

import com.atlassian.jira.exception.ParseException;
import com.atlassian.jira.imports.project.core.BackupProject;
import com.atlassian.jira.imports.project.parser.IssueParser;

import org.ofbiz.core.entity.DelegatorInterface;
import org.ofbiz.core.entity.GenericEntity;
import org.ofbiz.core.entity.model.ModelEntity;

/**
 * Parses an XML import file and creates a reduced XML file with just the Issues for the given project.
 *
 * @since v3.13
 */
public class IssuePartitonHandler extends AbstractImportPartitionHandler
{
    private final BackupProject backupProject;
    private final ModelEntity modelEntity;
    private final PrintWriter printWriter;
    private int entityCount;

    /**
     * @param backupProject contains the issue id's that we are interested in partitioning.
     * @param printWriter the partitioned writer that should be written to if the entity being processed should be
     * written.
     * @param modelEntity is the ModelEntity for the "Issue" entity
     * @param encoding is the encoding that the partitioned files are going to writen in.
     * @param delegatorInterface required for persistence
     */
    public IssuePartitonHandler(final BackupProject backupProject, final PrintWriter printWriter, final ModelEntity modelEntity, final String encoding, final DelegatorInterface delegatorInterface)
    {
        super(printWriter, encoding, delegatorInterface);
        assertModelEntityForName(modelEntity, IssueParser.ISSUE_ENTITY_NAME);

        this.backupProject = backupProject;
        this.modelEntity = modelEntity;
        this.printWriter = printWriter;
    }

    public void handleEntity(final String entityName, final Map<String, String> attributes) throws ParseException
    {
        if (IssueParser.ISSUE_ENTITY_NAME.equals(entityName))
        {
            // check if it is in our project
            if (backupProject.containsIssue(getId(attributes)))
            {
                // Create a GenericEntity
                final GenericEntity genericEntity = new GenericEntity(delegator, modelEntity, attributes);
                genericEntity.writeXmlText(printWriter, null);
                entityCount++;
            }
        }
    }

    public int getEntityCount()
    {
        return entityCount;
    }

    String getId(final Map attributes)
    {
        return (String) attributes.get("id");
    }

}
