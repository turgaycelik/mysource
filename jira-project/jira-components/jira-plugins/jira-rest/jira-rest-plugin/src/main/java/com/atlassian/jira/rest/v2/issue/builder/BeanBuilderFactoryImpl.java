package com.atlassian.jira.rest.v2.issue.builder;

import com.atlassian.jira.bc.filter.FilterSubscriptionService;
import com.atlassian.jira.config.StatusManager;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFactory;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.attachment.Attachment;
import com.atlassian.jira.issue.changehistory.ChangeHistoryManager;
import com.atlassian.jira.issue.changehistory.metadata.HistoryMetadataManager;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.config.manager.IssueTypeSchemeManager;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.issue.fields.rest.json.beans.IssueLinksBeanBuilderFactory;
import com.atlassian.jira.issue.fields.rest.json.beans.JiraBaseUrls;
import com.atlassian.jira.issue.fields.screen.FieldScreenRendererFactory;
import com.atlassian.jira.issue.link.RemoteIssueLink;
import com.atlassian.jira.issue.security.IssueSecurityLevelManager;
import com.atlassian.jira.issue.thumbnail.ThumbnailManager;
import com.atlassian.jira.jql.util.JqlStringSupport;
import com.atlassian.jira.permission.PermissionSchemeManager;
import com.atlassian.jira.plugin.webfragment.SimpleLinkManager;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.rest.v2.issue.AttachmentBeanBuilder;
import com.atlassian.jira.rest.v2.issue.CreateMetaBeanBuilder;
import com.atlassian.jira.rest.v2.issue.EditMetaBeanBuilder;
import com.atlassian.jira.rest.v2.issue.IncludedFields;
import com.atlassian.jira.rest.v2.issue.IssueBeanBuilder;
import com.atlassian.jira.rest.v2.issue.OpsbarBeanBuilder;
import com.atlassian.jira.rest.v2.issue.RemoteIssueLinkBeanBuilder;
import com.atlassian.jira.rest.v2.issue.ResourceUriBuilder;
import com.atlassian.jira.rest.v2.issue.TransitionMetaBeanBuilder;
import com.atlassian.jira.rest.v2.issue.context.ContextUriInfo;
import com.atlassian.jira.rest.v2.issue.project.ProjectBeanFactory;
import com.atlassian.jira.rest.v2.issue.project.ProjectRoleBeanFactory;
import com.atlassian.jira.rest.v2.issue.version.VersionBeanFactory;
import com.atlassian.jira.rest.v2.search.FilterBeanBuilder;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.sharing.type.ShareTypeFactory;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.EmailFormatter;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.jira.workflow.IssueWorkflowManager;
import com.atlassian.jira.workflow.WorkflowManager;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Default implementation for BeanBuilderFactory.
 *
 * @since v4.2
 */
@ExportAsService
@Component
public class BeanBuilderFactoryImpl implements BeanBuilderFactory
{
    private final UserManager userManager;
    private final ThumbnailManager thumbnailManager;
    private final VersionBeanFactory versionBeanFactory;
    private final ProjectBeanFactory projectBeanFactory;
    private final FieldLayoutManager fieldLayoutManager;
    private final JiraAuthenticationContext authContext;
    private final FieldManager fieldManager;
    private final ResourceUriBuilder uriBuilder;
    private final ContextUriInfo contextUriInfo;
    private final VelocityRequestContextFactory velocityRequestContextFactory;
    private final ProjectManager projectManager;
    private final ProjectRoleManager projectRoleManager;
    private final IssueTypeSchemeManager issueTypeSchemeManager;
    private final PermissionManager permissionManager;
    private final FieldScreenRendererFactory fieldScreenRendererFactory;
    private final IssueManager issueManager;
    private final JiraBaseUrls baseUrls;
    private final IssueLinksBeanBuilderFactory issueLinkBeanBuilderFactory;
    private final IssueWorkflowManager issueWorkflowManager;
    private final WorkflowManager workflowManager;
    private final StatusManager statusManager;
    private final IssueFactory issueFactory;
    private final ChangeHistoryManager changeHistoryManager;
    private final ApplicationProperties applicationProperties;
    private final SimpleLinkManager simpleLinkManager;
    private final I18nHelper i18nHelper;
    private final PluginAccessor pluginAccessor;
    private final ShareTypeFactory shareTypeFactory;
    private final JqlStringSupport jqlStringSupport;
    private final GroupManager groupManager;
    private final PermissionSchemeManager permissionSchemeManager;
    private final FilterSubscriptionService filterSubscriptionService;
    private final JiraBaseUrls jiraBaseUrls;
    private final ProjectRoleBeanFactory projectRoleBeanFactory;
    private final IssueSecurityLevelManager issueSecurityLevelManager;
    private final HistoryMetadataManager historyMetadataManager;
    private final EmailFormatter emailFormatter;

    @Autowired
    public BeanBuilderFactoryImpl(
            final UserManager userManager,
            final ThumbnailManager thumbnailManager,
            final VersionBeanFactory versionBeanFactory,
            final ProjectBeanFactory projectBeanFactory,
            final FieldLayoutManager fieldLayoutManager,
            final JiraAuthenticationContext authContext,
            final FieldManager fieldManager,
            final ResourceUriBuilder uriBuilder,
            final ContextUriInfo contextUriInfo,
            final VelocityRequestContextFactory velocityRequestContextFactory,
            final ProjectManager projectManager,
            final ProjectRoleManager projectRoleManager,
            final IssueTypeSchemeManager issueTypeSchemeManager,
            final PermissionManager permissionManager,
            final FieldScreenRendererFactory fieldScreenRendererFactory,
            final IssueManager issueManager, final JiraBaseUrls baseUrls,
            final IssueLinksBeanBuilderFactory issueLinkBeanBuilderFactory,
            final IssueWorkflowManager issueWorkflowManager,
            final WorkflowManager workflowManager,
            final StatusManager statusManager, IssueFactory issueFactory, ChangeHistoryManager changeHistoryManager,
            final ApplicationProperties applicationProperties,
            final SimpleLinkManager simpleLinkManager,
            final I18nHelper i18nHelper,
            final PluginAccessor pluginAccessor,
            final ShareTypeFactory shareTypeFactory,
            final JqlStringSupport jqlStringSupport,
            final GroupManager groupManager,
            final PermissionSchemeManager permissionSchemeManager,
            final FilterSubscriptionService filterSubscriptionService,
            final JiraBaseUrls jiraBaseUrls,
            final ProjectRoleBeanFactory projectRoleBeanFactory,
            final IssueSecurityLevelManager issueSecurityLevelManager,
            final HistoryMetadataManager historyMetadataManager, final EmailFormatter emailFormatter)
    {
        this.userManager = userManager;
        this.thumbnailManager = thumbnailManager;
        this.versionBeanFactory = versionBeanFactory;
        this.projectBeanFactory = projectBeanFactory;
        this.fieldLayoutManager = fieldLayoutManager;
        this.authContext = authContext;
        this.fieldManager = fieldManager;
        this.uriBuilder = uriBuilder;
        this.contextUriInfo = contextUriInfo;
        this.velocityRequestContextFactory = velocityRequestContextFactory;
        this.projectManager = projectManager;
        this.projectRoleManager = projectRoleManager;
        this.issueTypeSchemeManager = issueTypeSchemeManager;
        this.permissionManager = permissionManager;
        this.fieldScreenRendererFactory = fieldScreenRendererFactory;
        this.issueManager = issueManager;
        this.baseUrls = baseUrls;
        this.issueLinkBeanBuilderFactory = issueLinkBeanBuilderFactory;
        this.issueWorkflowManager = issueWorkflowManager;
        this.workflowManager = workflowManager;
        this.statusManager = statusManager;
        this.issueFactory = issueFactory;
        this.changeHistoryManager = changeHistoryManager;
        this.applicationProperties = applicationProperties;
        this.simpleLinkManager = simpleLinkManager;
        this.i18nHelper = i18nHelper;
        this.pluginAccessor = pluginAccessor;
        this.shareTypeFactory = shareTypeFactory;
        this.jqlStringSupport = jqlStringSupport;
        this.groupManager = groupManager;
        this.permissionSchemeManager = permissionSchemeManager;
        this.filterSubscriptionService = filterSubscriptionService;
        this.jiraBaseUrls = jiraBaseUrls;
        this.projectRoleBeanFactory = projectRoleBeanFactory;
        this.issueSecurityLevelManager = issueSecurityLevelManager;
        this.historyMetadataManager = historyMetadataManager;
        this.emailFormatter = emailFormatter;
    }

    /**
     * Returns a new AttachmentBeanBuilder.
     *
     * @param attachment an Attachment
     * @return an AttachmentBeanBuilder
     */
    @Override
    public AttachmentBeanBuilder newAttachmentBeanBuilder(final Attachment attachment)
    {
        return new AttachmentBeanBuilder(jiraBaseUrls, thumbnailManager, attachment);
    }

    /**
     * Returns a new instance of an IssueBeanBuilder.
     *
     * @return an IssueBeanBuilder
     */
    @Override
    public IssueBeanBuilder newIssueBeanBuilder(final Issue issue, IncludedFields include)
    {
        return new IssueBeanBuilder(fieldLayoutManager, authContext,
                fieldManager, uriBuilder, this,
                contextUriInfo, issue, include, issueLinkBeanBuilderFactory, issueWorkflowManager);
    }

    /**
     * Returns a new instance of an IssueLinkBeanBuilder.
     *
     * @return an IssueLinkBeanBuilder
     * @param issue
    @Override
    public IssueLinkBeanBuilder newIssueLinkBeanBuilder(final Issue issue)
    {
        return new IssueLinkBeanBuilder(applicationProperties, issueLinkManager, authContext, baseUrls, issue);
    }
     */

    /**
     * Returns a new instance of a CreateMetaBeanBuilder.
     *
     * @return a CreateMetaBeanBuilder
     */
    @Override
    public CreateMetaBeanBuilder newCreateMetaBeanBuilder()
    {
        return new CreateMetaBeanBuilder(authContext, projectManager, fieldLayoutManager,
                velocityRequestContextFactory, contextUriInfo, issueTypeSchemeManager,
                permissionManager, versionBeanFactory, baseUrls, issueFactory, fieldScreenRendererFactory,
                fieldManager, issueSecurityLevelManager);
    }

    /**
     * Returns a new instance of a EditMetaBeanBuilder.
     *
     * @return a EditMetaBeanBuilder
     */
    @Override
    public EditMetaBeanBuilder newEditMetaBeanBuilder()
    {
        return new EditMetaBeanBuilder(authContext, fieldLayoutManager, velocityRequestContextFactory, contextUriInfo, versionBeanFactory, baseUrls, permissionManager, fieldScreenRendererFactory, fieldManager);
    }

    /**
     * Returns a new instance of a TransitionMetaBeanBuilder.
     *
     * @return a TransitionMetaBeanBuilder
     */
    @Override
    public TransitionMetaBeanBuilder newTransitionMetaBeanBuilder()
    {
        return new TransitionMetaBeanBuilder(fieldScreenRendererFactory, authContext, fieldLayoutManager, velocityRequestContextFactory, contextUriInfo, versionBeanFactory, baseUrls, workflowManager, statusManager);
    }

    @Override
    public OpsbarBeanBuilder newOpsbarBeanBuilder(final Issue issue)
    {
        return new OpsbarBeanBuilder(issue, applicationProperties, simpleLinkManager, authContext, i18nHelper, issueManager, pluginAccessor, permissionManager);
    }

    /**
     * Returns a new instance of a RemoteIssueLinkBeanBuilder.
     *
     * @return a RemoteIssueLinkBeanBuilder
     */
    @Override
    public RemoteIssueLinkBeanBuilder newRemoteIssueLinkBeanBuilder(final RemoteIssueLink remoteIssueLink)
    {
        return new RemoteIssueLinkBeanBuilder(contextUriInfo, issueManager, remoteIssueLink);
    }

    @Override
    public ChangelogBeanBuilder newChangelogBeanBuilder()
    {
        return new ChangelogBeanBuilder(baseUrls, changeHistoryManager, historyMetadataManager, authContext, emailFormatter);
    }

    @Override
    public FilterBeanBuilder newFilterBeanBuilder()
    {
        return new FilterBeanBuilder(authContext, projectManager, permissionManager,
                projectRoleManager, projectBeanFactory,
                shareTypeFactory, userManager, jqlStringSupport, groupManager, permissionSchemeManager,
                filterSubscriptionService, jiraBaseUrls, projectRoleBeanFactory);
    }
}
