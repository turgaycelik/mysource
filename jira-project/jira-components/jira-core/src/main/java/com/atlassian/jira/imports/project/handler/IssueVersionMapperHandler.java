package com.atlassian.jira.imports.project.handler;

import com.atlassian.jira.exception.ParseException;
import com.atlassian.jira.external.beans.ExternalNodeAssociation;
import com.atlassian.jira.imports.project.core.BackupProject;
import com.atlassian.jira.imports.project.mapper.SimpleProjectImportIdMapper;
import com.atlassian.jira.imports.project.parser.NodeAssociationParser;
import com.atlassian.jira.imports.project.parser.NodeAssociationParserImpl;

import java.util.Map;

/**
 * Populates which versions (fix for, and affects) that are in use by the backup project.
 *
 * @since v3.13
 */
public class IssueVersionMapperHandler implements ImportEntityHandler
{
    private NodeAssociationParser nodeAssocationParser;
    private final SimpleProjectImportIdMapper versionMapper;
    private final BackupProject backupProject;

    public IssueVersionMapperHandler(final BackupProject backupProject, final SimpleProjectImportIdMapper versionMapper)
    {
        this.backupProject = backupProject;
        this.versionMapper = versionMapper;
    }

    public void handleEntity(final String entityName, final Map<String, String> attributes) throws ParseException
    {
        if (NodeAssociationParser.NODE_ASSOCIATION_ENTITY_NAME.equals(entityName))
        {
            final ExternalNodeAssociation nodeAssociation = getNodeAssociationParser().parse(attributes);
            if (NodeAssociationParser.AFFECTS_VERSION_TYPE.equals(nodeAssociation.getAssociationType()) || NodeAssociationParser.FIX_VERSION_TYPE.equals(nodeAssociation.getAssociationType()))
            {
                if (backupProject.containsIssue(nodeAssociation.getSourceNodeId()))
                {
                    versionMapper.flagValueAsRequired(nodeAssociation.getSinkNodeId());
                }
            }
        }
    }

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

    ///CLOVER:OFF
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if ((o == null) || (getClass() != o.getClass()))
        {
            return false;
        }

        final IssueVersionMapperHandler that = (IssueVersionMapperHandler) o;

        if (backupProject != null ? !backupProject.equals(that.backupProject) : that.backupProject != null)
        {
            return false;
        }
        if (nodeAssocationParser != null ? !nodeAssocationParser.equals(that.nodeAssocationParser) : that.nodeAssocationParser != null)
        {
            return false;
        }
        if (versionMapper != null ? !versionMapper.equals(that.versionMapper) : that.versionMapper != null)
        {
            return false;
        }

        return true;
    }

    ///CLOVER:ON

    ///CLOVER:OFF
    public int hashCode()
    {
        int result;
        result = (nodeAssocationParser != null ? nodeAssocationParser.hashCode() : 0);
        result = 31 * result + (versionMapper != null ? versionMapper.hashCode() : 0);
        result = 31 * result + (backupProject != null ? backupProject.hashCode() : 0);
        return result;
    }
    ///CLOVER:ON
}
