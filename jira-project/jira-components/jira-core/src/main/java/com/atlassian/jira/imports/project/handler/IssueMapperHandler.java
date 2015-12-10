package com.atlassian.jira.imports.project.handler;

import com.atlassian.jira.exception.ParseException;
import com.atlassian.jira.external.beans.ExternalIssue;
import com.atlassian.jira.imports.project.core.BackupProject;
import com.atlassian.jira.imports.project.mapper.CustomFieldMapper;
import com.atlassian.jira.imports.project.mapper.IssueTypeMapper;
import com.atlassian.jira.imports.project.mapper.ProjectImportMapper;
import com.atlassian.jira.imports.project.mapper.SimpleProjectImportIdMapper;
import com.atlassian.jira.imports.project.mapper.StatusMapper;
import com.atlassian.jira.imports.project.mapper.UserMapper;
import com.atlassian.jira.imports.project.parser.IssueParser;
import com.atlassian.jira.imports.project.parser.IssueParserImpl;

import java.util.Map;

/**
 * This will flag values as required for the various Issue related mappers based on the values set in the
 * issues for the selected project.
 *
 * @since v3.13
 */
public class IssueMapperHandler implements ImportEntityHandler
{
    private IssueParser issueParser;
    private final BackupProject backupProject;
    private final IssueTypeMapper issueTypeMapper;
    private final StatusMapper statusMapper;
    private final SimpleProjectImportIdMapper priorityMapper;
    private final SimpleProjectImportIdMapper resolutionMapper;
    private final SimpleProjectImportIdMapper issueSecurityLevelMapper;
    private final UserMapper userMapper;
    private final CustomFieldMapper customFieldMapper;

    public IssueMapperHandler(final BackupProject backupProject, final ProjectImportMapper projectImportMapper)
    {
        this.backupProject = backupProject;
        statusMapper = projectImportMapper.getStatusMapper();
        issueTypeMapper = projectImportMapper.getIssueTypeMapper();
        priorityMapper = projectImportMapper.getPriorityMapper();
        resolutionMapper = projectImportMapper.getResolutionMapper();
        issueSecurityLevelMapper = projectImportMapper.getIssueSecurityLevelMapper();
        userMapper = projectImportMapper.getUserMapper();
        customFieldMapper = projectImportMapper.getCustomFieldMapper();
    }

    public void handleEntity(final String entityName, final Map<String, String> attributes) throws ParseException
    {
        // We only want to handle the issue entities
        if (IssueParser.ISSUE_ENTITY_NAME.equals(entityName))
        {
            final ExternalIssue issue = getIssueParser().parse(attributes);
            // Lets only deal with the issues for this project
            if (backupProject.containsIssue(issue.getId()))
            {
                issueTypeMapper.flagValueAsRequired(issue.getIssueType());
                statusMapper.flagValueAsRequired(issue.getStatus(), issue.getIssueType());
                priorityMapper.flagValueAsRequired(issue.getPriority());
                resolutionMapper.flagValueAsRequired(issue.getResolution());
                issueSecurityLevelMapper.flagValueAsRequired(issue.getSecurityLevel());
                userMapper.flagUserAsMandatory(issue.getAssignee());
                userMapper.flagUserAsMandatory(issue.getReporter());
                customFieldMapper.flagIssueTypeInUse(issue.getId(), issue.getIssueType());
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
    IssueParser getIssueParser()
    {
        if (issueParser == null)
        {
            issueParser = new IssueParserImpl();
        }
        return issueParser;
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

        final IssueMapperHandler that = (IssueMapperHandler) o;

        if (backupProject != null ? !backupProject.equals(that.backupProject) : that.backupProject != null)
        {
            return false;
        }
        if (customFieldMapper != null ? !customFieldMapper.equals(that.customFieldMapper) : that.customFieldMapper != null)
        {
            return false;
        }
        if (issueParser != null ? !issueParser.equals(that.issueParser) : that.issueParser != null)
        {
            return false;
        }
        if (issueSecurityLevelMapper != null ? !issueSecurityLevelMapper.equals(that.issueSecurityLevelMapper) : that.issueSecurityLevelMapper != null)
        {
            return false;
        }
        if (issueTypeMapper != null ? !issueTypeMapper.equals(that.issueTypeMapper) : that.issueTypeMapper != null)
        {
            return false;
        }
        if (priorityMapper != null ? !priorityMapper.equals(that.priorityMapper) : that.priorityMapper != null)
        {
            return false;
        }
        if (resolutionMapper != null ? !resolutionMapper.equals(that.resolutionMapper) : that.resolutionMapper != null)
        {
            return false;
        }
        if (statusMapper != null ? !statusMapper.equals(that.statusMapper) : that.statusMapper != null)
        {
            return false;
        }
        if (userMapper != null ? !userMapper.equals(that.userMapper) : that.userMapper != null)
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
        result = (issueParser != null ? issueParser.hashCode() : 0);
        result = 31 * result + (backupProject != null ? backupProject.hashCode() : 0);
        result = 31 * result + (issueTypeMapper != null ? issueTypeMapper.hashCode() : 0);
        result = 31 * result + (statusMapper != null ? statusMapper.hashCode() : 0);
        result = 31 * result + (priorityMapper != null ? priorityMapper.hashCode() : 0);
        result = 31 * result + (resolutionMapper != null ? resolutionMapper.hashCode() : 0);
        result = 31 * result + (issueSecurityLevelMapper != null ? issueSecurityLevelMapper.hashCode() : 0);
        result = 31 * result + (userMapper != null ? userMapper.hashCode() : 0);
        result = 31 * result + (customFieldMapper != null ? customFieldMapper.hashCode() : 0);
        return result;
    }
    ///CLOVER:ON
}
