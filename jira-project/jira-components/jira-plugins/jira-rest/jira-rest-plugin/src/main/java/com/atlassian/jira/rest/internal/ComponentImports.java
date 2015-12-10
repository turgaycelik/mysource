package com.atlassian.jira.rest.internal;

import com.atlassian.jira.avatar.CroppingAvatarImageDataProviderFactory;
import com.atlassian.jira.avatar.UniversalAvatarsService;
import com.atlassian.jira.bc.issue.comment.property.CommentPropertyService;
import com.atlassian.jira.bc.license.LicenseRoleService;
import com.atlassian.jira.license.LicenseBannerHelper;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import org.springframework.stereotype.Component;

/**
 * Since v6.2.1
 *
 * Special class listing all component imports so atlassian-spring-scanner-annotation will be able to pick them up and change them into proper OSGI/spring imports.
 */
@SuppressWarnings ("unused")
@Component
public class ComponentImports
{
    @ComponentImport
    CommentPropertyService commentPropertyService;
    @ComponentImport
    com.atlassian.jira.util.I18nHelper.BeanFactory beanFactory;
    @ComponentImport
    com.atlassian.jira.issue.fields.rest.json.beans.IssueLinksBeanBuilderFactory issueLinksBeanBuilderFactory;
    @ComponentImport
    com.atlassian.plugins.rest.common.util.RestUrlBuilder restUrlBuilder;
    @ComponentImport
    com.atlassian.jira.jql.operand.registry.PredicateRegistry predicateRegistry;
    @ComponentImport
    com.atlassian.jira.issue.fields.rest.IssueLinkTypeFinder issueLinkTypeFinder;
    @ComponentImport
    com.atlassian.jira.notification.AdhocNotificationService notificationService;
    @ComponentImport
    com.atlassian.jira.config.StatusManager statusManager;
    @ComponentImport
    com.atlassian.jira.user.util.UserUtil userUtil;
    @ComponentImport
    com.atlassian.jira.bc.project.component.ProjectComponentManager projectComponentManager;
    @ComponentImport
    com.atlassian.jira.bc.issue.worklog.WorklogService worklogService;
    @ComponentImport
    com.atlassian.jira.bc.customfield.CustomFieldService customFieldService;
    @ComponentImport
    com.atlassian.jira.web.util.WebAttachmentManager webAttachmentManager;
    @ComponentImport
    com.atlassian.jira.issue.IssueFactory issueFactory;
    @ComponentImport
    com.atlassian.jira.config.IssueTypeManager issueTypeManager;
    @ComponentImport
    com.atlassian.jira.project.ProjectManager projectManager;
    @ComponentImport
    com.atlassian.jira.issue.RendererManager rendererManager;
    @ComponentImport
    com.atlassian.event.api.EventPublisher eventPublisher;
    @ComponentImport
    com.atlassian.jira.issue.link.IssueLinkManager issueLinkManager;
    @ComponentImport
    com.atlassian.jira.bc.issue.vote.VoteService voteService;
    @ComponentImport
    com.atlassian.jira.security.groups.GroupManager groupManager;
    @ComponentImport
    com.atlassian.jira.bc.security.login.LoginService loginService;
    @ComponentImport
    com.atlassian.jira.datetime.DateTimeFormatterFactory dateTimeFormatterFactory;
    @ComponentImport
    com.atlassian.jira.bc.issue.search.IssuePickerSearchService issuePickerSearchService;
    @ComponentImport
    com.atlassian.jira.entity.property.JsonEntityPropertyManager jsonEntityPropertyManager;
    @ComponentImport
    com.atlassian.jira.bc.issue.link.RemoteIssueLinkService remoteIssueLinkService;
    @ComponentImport
    com.atlassian.jira.bc.project.version.remotelink.RemoteVersionLinkService remoteVersionLinkService;
    @ComponentImport
    com.atlassian.jira.bc.filter.FilterSubscriptionService filterSubscriptionService;
    @ComponentImport
    com.atlassian.jira.avatar.AvatarManager avatarManager;
    @ComponentImport
    com.atlassian.jira.user.UserProjectHistoryManager userProjectHistoryManager;
    @ComponentImport
    com.atlassian.jira.issue.fields.layout.column.ColumnLayoutManager columnLayoutManager;
    @ComponentImport
    com.atlassian.jira.web.action.issue.IssueSearchLimits issueSearchLimits;
    @ComponentImport
    com.atlassian.jira.sharing.SharedEntityAccessor.Factory SharedEntityAccessor$Factory;
    @ComponentImport
    com.atlassian.jira.sharing.type.ShareTypeFactory shareTypeFactory;
    @ComponentImport
    com.atlassian.jira.issue.AttachmentManager attachmentManager;
    @ComponentImport
    com.atlassian.jira.bc.issue.link.IssueLinkTypeService issueLinkTypeService;
    @ComponentImport
    com.atlassian.jira.bc.config.ConstantsService constantsService;
    @ComponentImport
    com.atlassian.jira.bc.group.search.GroupPickerSearchService groupPickerSearchService;
    @ComponentImport
    com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager fieldLayoutManager;
    @ComponentImport
    com.atlassian.jira.config.FeatureManager featureManager;
    @ComponentImport
    com.atlassian.jira.issue.changehistory.ChangeHistoryManager changeHistoryManager;
    @ComponentImport
    com.atlassian.jira.util.velocity.VelocityRequestContextFactory velocityRequestContextFactory;
    @ComponentImport
    com.atlassian.jira.bc.user.search.UserPickerSearchService userPickerSearchService;
    @ComponentImport
    com.atlassian.jira.config.SubTaskManager subTaskManager;
    @ComponentImport
    com.atlassian.jira.bc.issue.attachment.AttachmentService attachmentService;
    @ComponentImport
    com.atlassian.jira.config.ConstantsManager constantsManager;
    @ComponentImport
    com.atlassian.jira.image.separator.HeaderSeparatorService headerSeparatorService;
    @ComponentImport
    com.atlassian.jira.user.util.UserManager userManager;
    @ComponentImport
    com.atlassian.jira.bc.issue.watcher.WatcherService watcherService;
    @ComponentImport
    com.atlassian.jira.bc.favourites.FavouritesService favouritesService;
    @ComponentImport
    com.atlassian.jira.issue.security.IssueSecurityLevelManager issueSecurityLevelManager;
    @ComponentImport
    com.atlassian.jira.security.GlobalPermissionManager globalPermissionManager;
    @ComponentImport
    com.atlassian.jira.bc.issue.link.IssueLinkService issueLinkService;
    @ComponentImport
    com.atlassian.jira.bc.issue.comment.CommentService commentService;
    @ComponentImport
    com.atlassian.jira.bc.user.UserService userService;
    @ComponentImport
    com.atlassian.jira.avatar.AvatarService avatarService;
    @ComponentImport
    com.atlassian.jira.plugin.webfragment.SimpleLinkManager simpleLinkManager;
    @ComponentImport
    com.atlassian.jira.issue.IssueManager issueManager;
    @ComponentImport
    com.atlassian.jira.permission.PermissionSchemeManager permissionSchemeManager;
    @ComponentImport
    com.atlassian.jira.issue.fields.option.OptionSetManager optionSetManager;
    @ComponentImport
    com.atlassian.jira.user.preferences.UserPreferencesManager userPreferencesManager;
    @ComponentImport
    com.atlassian.plugin.PluginAccessor pluginAccessor;
    @ComponentImport
    com.atlassian.jira.util.EmailFormatter emailFormatter;
    @ComponentImport
    com.atlassian.jira.security.PermissionManager permissionManager;
    @ComponentImport
    com.atlassian.jira.security.xsrf.XsrfInvocationChecker xsrfInvocationChecker;
    @ComponentImport
    com.atlassian.jira.plugin.keyboardshortcut.KeyboardShortcutManager keyboardShortcutManager;
    @ComponentImport
    com.atlassian.jira.security.xsrf.XsrfTokenGenerator xsrfTokenGenerator;
    @ComponentImport
    com.atlassian.jira.ofbiz.OfBizDelegator ofBizDelegator;
    @ComponentImport
    com.atlassian.jira.bc.issue.IssueService issueService;
    @ComponentImport
    com.atlassian.jira.user.UserAdminHistoryManager userAdminHistoryManager;
    @ComponentImport
    com.atlassian.jira.config.properties.ApplicationProperties applicationProperties;
    @ComponentImport
    com.atlassian.jira.favourites.FavouritesManager favouritesManager;
    @ComponentImport
    com.atlassian.jira.avatar.AvatarPickerHelper avatarPickerHelper;
    @ComponentImport
    com.atlassian.jira.timezone.TimeZoneManager timeZoneManager;
    @ComponentImport
    com.atlassian.jira.web.component.jql.AutoCompleteJsonGenerator autoCompleteJsonGenerator;
    @ComponentImport
    com.atlassian.jira.user.util.UserSharingPreferencesUtil userSharingPreferencesUtil;
    @ComponentImport
    com.atlassian.jira.workflow.IssueWorkflowManager issueWorkflowManager;
    @ComponentImport
    com.atlassian.jira.bc.project.version.VersionService versionService;
    @ComponentImport
    com.atlassian.jira.image.dropdown.DropDownCreatorService dropDownCreatorService;
    @ComponentImport
    com.atlassian.jira.issue.thumbnail.ThumbnailManager thumbnailManager;
    @ComponentImport
    com.atlassian.jira.security.websudo.InternalWebSudoManager internalWebSudoManager;
    @ComponentImport
    com.atlassian.jira.bc.issue.fields.ColumnService columnService;
    @ComponentImport
    com.atlassian.jira.util.DateFieldFormat dateFieldFormat;
    @ComponentImport
    com.atlassian.jira.issue.customfields.manager.OptionsManager optionsManager;
    @ComponentImport
    com.atlassian.jira.issue.fields.rest.IssueFinder issueFinder;
    @ComponentImport
    com.atlassian.jira.bc.filter.SearchRequestService searchRequestService;
    @ComponentImport
    com.atlassian.mail.server.MailServerManager mailServerManager;
    @ComponentImport
    com.atlassian.jira.issue.fields.screen.FieldScreenManager fieldScreenManager;
    @ComponentImport
    com.atlassian.jira.plugin.user.PasswordPolicyManager passwordPolicyManager;
    @ComponentImport
    com.atlassian.jira.issue.fields.rest.json.beans.JiraBaseUrls jiraBaseUrls;
    @ComponentImport
    com.atlassian.jira.bc.issue.worklog.TimeTrackingConfiguration timeTrackingConfiguration;
    @ComponentImport
    com.atlassian.jira.issue.search.managers.SearchHandlerManager searchHandlerManager;
    @ComponentImport
    com.atlassian.jira.security.roles.ProjectRoleManager projectRoleManager;
    @ComponentImport
    com.atlassian.jira.datetime.DateTimeFormatter dateTimeFormatter;
    @ComponentImport
    com.atlassian.jira.bc.project.component.ProjectComponentService projectComponentService;
    @ComponentImport
    com.atlassian.jira.user.UserPropertyManager userPropertyManager;
    @ComponentImport
    com.atlassian.jira.util.I18nHelper I18nHelper;
    @ComponentImport
    com.atlassian.jira.bc.portal.PortalPageService portalPageService;
    @ComponentImport
    com.atlassian.jira.issue.fields.screen.FieldScreenRendererFactory fieldScreenRendererFactory;
    @ComponentImport
    com.atlassian.jira.bc.issue.label.LabelService labelService;
    @ComponentImport
    com.atlassian.jira.issue.watchers.WatcherManager watcherManager;
    @ComponentImport
    com.atlassian.jira.issue.util.IssueUpdater issueUpdater;
    @ComponentImport
    com.atlassian.jira.bc.project.ProjectService projectService;
    @ComponentImport
    com.atlassian.jira.jql.util.JqlStringSupport jqlStringSupport;
    @ComponentImport
    com.atlassian.jira.issue.fields.FieldManager fieldManager;
    @ComponentImport
    com.atlassian.jira.bc.user.search.AssigneeService assigneeService;
    @ComponentImport
    com.atlassian.jira.issue.fields.config.manager.IssueTypeSchemeManager issueTypeSchemeManager;
    @ComponentImport
    com.atlassian.jira.bc.workflow.WorkflowSchemeService workflowSchemeService;
    @ComponentImport
    com.atlassian.jira.security.JiraAuthenticationContext jiraAuthenticationContext;
    @ComponentImport
    com.atlassian.jira.bc.license.JiraLicenseService jiraLicenseService;
    @ComponentImport
    com.atlassian.jira.workflow.WorkflowManager workflowManager;
    @ComponentImport
    com.atlassian.jira.bc.workflow.WorkflowService workflowService;
    @ComponentImport
    com.atlassian.jira.bc.projectroles.ProjectRoleService projectRoleService;
    @ComponentImport
    com.atlassian.jira.bc.issue.search.SearchService searchService;
    @ComponentImport
    com.atlassian.jira.bc.admin.ApplicationPropertiesService applicationPropertiesService;
    @ComponentImport
    com.atlassian.jira.util.BuildUtilsInfo buildUtilsInfo;
    @ComponentImport
    com.atlassian.jira.bc.issue.properties.IssuePropertyService issuePropertyService;
    @ComponentImport
    com.atlassian.jira.bc.project.property.ProjectPropertyService projectPropertyService;
    @ComponentImport
    com.atlassian.crowd.embedded.api.CrowdService crowdService;
    @ComponentImport
    com.atlassian.jira.bc.group.GroupService groupService;
    @ComponentImport
    com.atlassian.jira.issue.CustomFieldManager customFieldManager;
    @ComponentImport
    com.atlassian.jira.issue.fields.config.manager.FieldConfigManager fieldConfigManager;
    @ComponentImport
    com.atlassian.jira.issue.fields.config.manager.FieldConfigSchemeManager fieldConfigSchemeManager;
    @ComponentImport
    com.atlassian.jira.user.UserFilterManager userFilterManager;
    @ComponentImport
    com.atlassian.jira.util.index.IndexLifecycleManager indexLifecycleManager;
    @ComponentImport
    com.atlassian.jira.task.TaskManager taskManager;
    @ComponentImport
    com.atlassian.jira.workflow.WorkflowPropertyEditor.WorkflowPropertyEditorFactory workflowPropertyEditorFactory;
    @ComponentImport
    com.atlassian.jira.issue.fields.screen.ProjectFieldScreenHelper projectFieldScreenHelper;
    @ComponentImport
    com.atlassian.jira.auditing.AuditingManager auditingManager;
    @ComponentImport
    com.atlassian.jira.user.SecureUserTokenManager secureUserTokenManager;
    @ComponentImport
    UniversalAvatarsService universalAvatars;
    @ComponentImport
    com.atlassian.jira.auditing.AuditingService auditingService;
    @ComponentImport
    com.atlassian.jira.issue.changehistory.metadata.HistoryMetadataManager historyMetadataManager;
    @ComponentImport
    com.atlassian.jira.avatar.TemporaryAvatars temporaryAvatars;
    @ComponentImport
    CroppingAvatarImageDataProviderFactory croppingAvatarImageDataProviderFactory;
    @ComponentImport
    LicenseRoleService licenseRoleService;
    @ComponentImport
    LicenseBannerHelper licenseBannerHelper;
}
