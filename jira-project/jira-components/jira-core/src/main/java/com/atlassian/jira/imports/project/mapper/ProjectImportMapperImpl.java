package com.atlassian.jira.imports.project.mapper;

import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.user.util.UserUtil;

/**
 * Implementation of ProjectImportMapper. Constructing this object will create all the sub-mappers.
 *
 * @since v3.13
 */
public class ProjectImportMapperImpl implements ProjectImportMapper
{
    private final StatusMapper statusMapper;
    private final SimpleProjectImportIdMapper priorityMapper;
    private final SimpleProjectImportIdMapper resolutionMapper;
    private final IssueTypeMapper issueTypeMapper;
    private final SimpleProjectImportIdMapper issueSecurityLevelMapper;
    private final SimpleProjectImportIdMapper projectRoleMapper;
    private final UserMapper userMapper;
    private final GroupMapper groupMapper;
    private final SimpleProjectImportIdMapper issueMapper;
    private final IssueLinkTypeMapper issueLinkTypeMapper;
    private final SimpleProjectImportIdMapper projectMapper;
    private final SimpleProjectImportIdMapper versionMapper;
    private final SimpleProjectImportIdMapper componentMapper;
    private final SimpleProjectImportIdMapper changeGroupMapper;
    private final CustomFieldMapper customFieldMapper;
    private final CustomFieldOptionMapper customFieldOptionMapper;
    private final ProjectRoleActorMapper projectRoleActorMapper;
    private final SimpleProjectImportIdMapper commentMapper;

    public ProjectImportMapperImpl(final UserUtil userUtil, final GroupManager groupManager)
    {
        versionMapper = new SimpleProjectImportIdMapperImpl();
        componentMapper = new SimpleProjectImportIdMapperImpl();
        issueSecurityLevelMapper = new SimpleProjectImportIdMapperImpl();
        issueTypeMapper = new IssueTypeMapper();
        issueLinkTypeMapper = new IssueLinkTypeMapper();
        priorityMapper = new SimpleProjectImportIdMapperImpl();
        resolutionMapper = new SimpleProjectImportIdMapperImpl();
        statusMapper = new StatusMapper();
        userMapper = new UserMapper(userUtil);
        groupMapper = new GroupMapper(groupManager);
        issueMapper = new SimpleProjectImportIdMapperImpl();
        customFieldMapper = new CustomFieldMapper();
        customFieldOptionMapper = new CustomFieldOptionMapper();
        projectMapper = new SimpleProjectImportIdMapperImpl();
        projectRoleMapper = new SimpleProjectImportIdMapperImpl();
        changeGroupMapper = new SimpleProjectImportIdMapperImpl();
        projectRoleActorMapper = new ProjectRoleActorMapper();
        commentMapper = new SimpleProjectImportIdMapperImpl();
    }

    public SimpleProjectImportIdMapper getIssueMapper()
    {
        return issueMapper;
    }

    public SimpleProjectImportIdMapper getProjectMapper()
    {
        return projectMapper;
    }

    public UserMapper getUserMapper()
    {
        return userMapper;
    }

    public ProjectRoleActorMapper getProjectRoleActorMapper()
    {
        return projectRoleActorMapper;
    }

    public GroupMapper getGroupMapper()
    {
        return groupMapper;
    }

    public SimpleProjectImportIdMapper getIssueSecurityLevelMapper()
    {
        return issueSecurityLevelMapper;
    }

    public SimpleProjectImportIdMapper getVersionMapper()
    {
        return versionMapper;
    }

    public SimpleProjectImportIdMapper getComponentMapper()
    {
        return componentMapper;
    }

    public SimpleProjectImportIdMapper getProjectRoleMapper()
    {
        return projectRoleMapper;
    }

    public CustomFieldMapper getCustomFieldMapper()
    {
        return customFieldMapper;
    }

    public CustomFieldOptionMapper getCustomFieldOptionMapper()
    {
        return customFieldOptionMapper;
    }

    public IssueTypeMapper getIssueTypeMapper()
    {
        return issueTypeMapper;
    }

    public SimpleProjectImportIdMapper getPriorityMapper()
    {
        return priorityMapper;
    }

    public SimpleProjectImportIdMapper getResolutionMapper()
    {
        return resolutionMapper;
    }

    public StatusMapper getStatusMapper()
    {
        return statusMapper;
    }

    public IssueLinkTypeMapper getIssueLinkTypeMapper()
    {
        return issueLinkTypeMapper;
    }

    public SimpleProjectImportIdMapper getChangeGroupMapper()
    {
        return changeGroupMapper;
    }

    public SimpleProjectImportIdMapper getCommentMapper()
    {
        return commentMapper;
    }

    public void clearMappedValues()
    {
        issueMapper.clearMappedValues();
        userMapper.clearMappedValues();
        issueTypeMapper.clearMappedValues();
        issueSecurityLevelMapper.clearMappedValues();
        issueLinkTypeMapper.clearMappedValues();
        priorityMapper.clearMappedValues();
        resolutionMapper.clearMappedValues();
        statusMapper.clearMappedValues();
        customFieldMapper.clearMappedValues();
        customFieldOptionMapper.clearMappedValues();
        projectRoleMapper.clearMappedValues();
        projectMapper.clearMappedValues();
        componentMapper.clearMappedValues();
        versionMapper.clearMappedValues();
        changeGroupMapper.clearMappedValues();
        groupMapper.clearMappedValues();
        commentMapper.clearMappedValues();
        // ProjectRoleActorMapper is a weird one that does not really map values so we don't need to clear it
    }

    ///CLOVER:OFF - this is only here for testing purposes
    public String toString()
    {
        final StringBuilder sb = new StringBuilder();
        sb.append("IssueMapper: ").append(issueMapper.toString()).append("\n");
        sb.append("UserMapper: ").append(userMapper.toString()).append("\n");
        sb.append("IssueTypeMapper: ").append(issueTypeMapper.toString()).append("\n");
        sb.append("IssueSecurityLevelMapper: ").append(issueSecurityLevelMapper.toString()).append("\n");
        sb.append("IssueLinkTypeMapper: ").append(issueLinkTypeMapper.toString()).append("\n");
        sb.append("PriorityMapper: ").append(priorityMapper.toString()).append("\n");
        sb.append("ResolutionMapper: ").append(resolutionMapper.toString()).append("\n");
        sb.append("StatusMapper: ").append(statusMapper.toString()).append("\n");
        sb.append("CustomFieldMapper: ").append(customFieldMapper.toString()).append("\n");
        sb.append("CustomFieldOptionMapper: ").append(customFieldOptionMapper.toString()).append("\n");
        sb.append("ProjectRoleMapper: ").append(projectRoleMapper.toString()).append("\n");
        sb.append("ProjectMapper: ").append(projectMapper.toString()).append("\n");
        sb.append("ComponentMapper: ").append(componentMapper.toString()).append("\n");
        sb.append("VersionMapper: ").append(versionMapper.toString()).append("\n");
        sb.append("ChangeGroupMapper: ").append(changeGroupMapper.toString()).append("\n");
        sb.append("GroupMapper: ").append(groupMapper.toString()).append("\n");
        sb.append("ProjectRoleActorMapper: ").append(projectRoleActorMapper.toString()).append("\n");
        sb.append("CommentMapper: ").append(commentMapper.toString()).append("\n");
        return sb.toString();
    }
    ///CLOVER:ON
}
