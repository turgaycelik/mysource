package com.atlassian.jira.imports.project.handler;

import com.atlassian.jira.exception.ParseException;
import com.atlassian.jira.external.beans.ExternalNodeAssociation;
import com.atlassian.jira.imports.project.core.BackupProject;
import com.atlassian.jira.imports.project.mapper.SimpleProjectImportIdMapper;
import com.atlassian.jira.imports.project.parser.NodeAssociationParser;
import com.atlassian.jira.imports.project.parser.NodeAssociationParserImpl;
import com.atlassian.jira.imports.project.parser.ProjectParser;

import java.util.Map;

/**
 * This will populate the IssueSecurityLevelMapper with projects issue security levels. This gets
 * all issue security levels for the specified project and puts them into the mapper as old values. The
 * {@link com.atlassian.jira.imports.project.handler.IssueMapperHandler} is responsible for flagging security levels as
 * required for a specific project.
 *
 * @since v3.13
 */
public class ProjectIssueSecurityLevelMapperHandler implements ImportEntityHandler
{
    private final BackupProject backupProject;
    private final SimpleProjectImportIdMapper issueSecurityLevelMapper;
    private NodeAssociationParser nodeAssocationParser;
    private String issueSecurityLevelSchemeId;

    public static final String SCHEME_ISSUE_SECURITY_LEVELS_ENTITY_NAME = "SchemeIssueSecurityLevels";
    public static final String ISSUE_SECURITY_LEVEL_SCHEME = "scheme";
    public static final String ISSUE_SECURITY_LEVEL_ID = "id";
    public static final String ISSUE_SECURITY_LEVEL_NAME = "name";
    public static final String NODE_ASSOCIATION_ISSUE_SECURITY_SCHEME = "IssueSecurityScheme";

    public ProjectIssueSecurityLevelMapperHandler(final BackupProject backupProject, final SimpleProjectImportIdMapper issueSecurityLevelMapper)
    {
        this.backupProject = backupProject;
        this.issueSecurityLevelMapper = issueSecurityLevelMapper;
    }

    public void handleEntity(final String entityName, final Map<String, String> attributes) throws ParseException
    {
        // <NodeAssociation sourceNodeId="10001" sourceNodeEntity="Project" sinkNodeId="10000" sinkNodeEntity="IssueSecurityScheme" associationType="ProjectScheme"/>
        if (NodeAssociationParser.NODE_ASSOCIATION_ENTITY_NAME.equals(entityName))
        {
            // Keep an eye out for the selected projects issue security scheme
            handleNodeAssociations(attributes);
        }
        // <SchemeIssueSecurityLevels id="10001" name="level2" description="" scheme="10000"/>
        else if (SCHEME_ISSUE_SECURITY_LEVELS_ENTITY_NAME.equals(entityName))
        {
            // Since the SchemeIssueSecurityLevels are encountered after the scheme id we need to look for security
            // levels that are related to our projects security level, if it has one
            handleSchemeIssueSecurityLevels(attributes);
        }
    }

    private void handleSchemeIssueSecurityLevels(final Map attributes)
    {
        final String schemeId = (String) attributes.get(ISSUE_SECURITY_LEVEL_SCHEME);
        if ((issueSecurityLevelSchemeId != null) && issueSecurityLevelSchemeId.equals(schemeId))
        {
            final String securityLevelId = (String) attributes.get(ISSUE_SECURITY_LEVEL_ID);
            final String securityLevelName = (String) attributes.get(ISSUE_SECURITY_LEVEL_NAME);
            // store the security level since this is one for our project
            issueSecurityLevelMapper.registerOldValue(securityLevelId, securityLevelName);
        }
    }

    private void handleNodeAssociations(final Map attributes) throws ParseException
    {
        final ExternalNodeAssociation externalNodeAssociation = getNodeAssociationParser().parse(attributes);
        if (ProjectParser.PROJECT_ENTITY_NAME.equals(externalNodeAssociation.getSourceNodeEntity()) && NODE_ASSOCIATION_ISSUE_SECURITY_SCHEME.equals(externalNodeAssociation.getSinkNodeEntity()))
        {
            final String projectId = externalNodeAssociation.getSourceNodeId();
            // We only want to get the issue security scheme associated with the project we care about
            if (backupProject.getProject().getId().equals(projectId))
            {
                issueSecurityLevelSchemeId = externalNodeAssociation.getSinkNodeId();
            }
        }
    }

    ///CLOVER:OFF
    private NodeAssociationParser getNodeAssociationParser()
    {
        if (nodeAssocationParser == null)
        {
            nodeAssocationParser = new NodeAssociationParserImpl();
        }
        return nodeAssocationParser;
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

    ///CLOVER:OFF - used for tests
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

        final ProjectIssueSecurityLevelMapperHandler that = (ProjectIssueSecurityLevelMapperHandler) o;

        if (backupProject != null ? !backupProject.equals(that.backupProject) : that.backupProject != null)
        {
            return false;
        }
        if (issueSecurityLevelMapper != null ? !issueSecurityLevelMapper.equals(that.issueSecurityLevelMapper) : that.issueSecurityLevelMapper != null)
        {
            return false;
        }
        if (issueSecurityLevelSchemeId != null ? !issueSecurityLevelSchemeId.equals(that.issueSecurityLevelSchemeId) : that.issueSecurityLevelSchemeId != null)
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
        result = (backupProject != null ? backupProject.hashCode() : 0);
        result = 31 * result + (issueSecurityLevelMapper != null ? issueSecurityLevelMapper.hashCode() : 0);
        result = 31 * result + (issueSecurityLevelSchemeId != null ? issueSecurityLevelSchemeId.hashCode() : 0);
        return result;
    }
    ///CLOVER:ON

}