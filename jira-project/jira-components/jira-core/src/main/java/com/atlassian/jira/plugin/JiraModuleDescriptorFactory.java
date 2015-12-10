package com.atlassian.jira.plugin;

import com.atlassian.crowd.plugin.descriptors.PasswordEncoderModuleDescriptor;
import com.atlassian.jira.notification.NotificationFilterModuleDescriptor;
import com.atlassian.jira.plugin.aboutpagepanel.AboutPagePanelModuleDescriptorImpl;
import com.atlassian.jira.plugin.comment.CommentFieldRendererModuleDescriptorImpl;
import com.atlassian.jira.plugin.component.ComponentModuleDescriptor;
import com.atlassian.jira.plugin.componentpanel.ComponentTabPanelModuleDescriptorImpl;
import com.atlassian.jira.plugin.contentlinkresolver.ContentLinkResolverDescriptor;
import com.atlassian.jira.plugin.customfield.CustomFieldSearcherModuleDescriptorImpl;
import com.atlassian.jira.plugin.customfield.CustomFieldTypeModuleDescriptorImpl;
import com.atlassian.jira.plugin.decorator.DecoratorMapperModuleDescriptor;
import com.atlassian.jira.plugin.decorator.DecoratorModuleDescriptor;
import com.atlassian.jira.plugin.index.EntityPropertyIndexDocumentModuleDescriptorImpl;
import com.atlassian.jira.plugin.index.EntitySearchExtractorModuleDescriptorImpl;
import com.atlassian.jira.plugin.issuelink.IssueLinkRendererModuleDescriptorImpl;
import com.atlassian.jira.plugin.issuetabpanel.IssueTabPanelModuleDescriptorImpl;
import com.atlassian.jira.plugin.issueview.IssueViewModuleDescriptorImpl;
import com.atlassian.jira.plugin.jql.function.JqlFunctionModuleDescriptorImpl;
import com.atlassian.jira.plugin.keyboardshortcut.KeyboardShortcutModuleDescriptor;
import com.atlassian.jira.plugin.language.LanguageModuleDescriptorImpl;
import com.atlassian.jira.plugin.language.TranslationTransformModuleDescriptorImpl;
import com.atlassian.jira.plugin.license.LicenseRoleModuleDescriptorImpl;
import com.atlassian.jira.plugin.navigation.FooterModuleDescriptorImpl;
import com.atlassian.jira.plugin.navigation.TopNavigationModuleDescriptorImpl;
import com.atlassian.jira.plugin.permission.GlobalPermissionModuleDescriptorImpl;
import com.atlassian.jira.plugin.permission.ProjectPermissionModuleDescriptorImpl;
import com.atlassian.jira.plugin.profile.ViewProfilePanelModuleDescriptorImpl;
import com.atlassian.jira.plugin.projectoperation.ProjectOperationModuleDescriptorImpl;
import com.atlassian.jira.plugin.projectpanel.ProjectTabPanelModuleDescriptorImpl;
import com.atlassian.jira.plugin.renderer.JiraRendererModuleDescriptorImpl;
import com.atlassian.jira.plugin.renderer.MacroModuleDescriptor;
import com.atlassian.jira.plugin.renderercomponent.RendererComponentFactoryDescriptor;
import com.atlassian.jira.plugin.report.ReportModuleDescriptorImpl;
import com.atlassian.jira.plugin.roles.ProjectRoleActorModuleDescriptor;
import com.atlassian.jira.plugin.rpc.SoapModuleDescriptor;
import com.atlassian.jira.plugin.rpc.XmlRpcModuleDescriptor;
import com.atlassian.jira.plugin.searchrequestview.SearchRequestViewModuleDescriptorImpl;
import com.atlassian.jira.plugin.studio.StudioHooksModuleDescriptor;
import com.atlassian.jira.plugin.user.PasswordPolicyModuleDescriptor;
import com.atlassian.jira.plugin.user.PreDeleteUserErrorsModuleDescriptor;
import com.atlassian.jira.plugin.userformat.descriptors.UserFormatModuleDescriptorImpl;
import com.atlassian.jira.plugin.versionpanel.VersionTabPanelModuleDescriptorImpl;
import com.atlassian.jira.plugin.webfragment.descriptors.DefaultSimpleLinkFactoryModuleDescriptor;
import com.atlassian.jira.plugin.webfragment.descriptors.JiraWebItemModuleDescriptor;
import com.atlassian.jira.plugin.webfragment.descriptors.JiraWebSectionModuleDescriptor;
import com.atlassian.jira.plugin.webwork.WebworkModuleDescriptor;
import com.atlassian.jira.plugin.workflow.WorkflowConditionModuleDescriptor;
import com.atlassian.jira.plugin.workflow.WorkflowFunctionModuleDescriptor;
import com.atlassian.jira.plugin.workflow.WorkflowValidatorModuleDescriptor;
import com.atlassian.jira.security.auth.AuthorisationModuleDescriptor;
import com.atlassian.jira.security.plugin.ProjectPermissionOverrideModuleDescriptorImpl;
import com.atlassian.plugin.eventlistener.descriptors.EventListenerModuleDescriptor;
import com.atlassian.plugin.hostcontainer.HostContainer;
import com.atlassian.plugin.schema.impl.DefaultDescribedModuleDescriptorFactory;
import com.atlassian.plugin.servlet.descriptors.ServletContextListenerModuleDescriptor;
import com.atlassian.plugin.servlet.descriptors.ServletContextParamModuleDescriptor;
import com.atlassian.plugin.servlet.descriptors.ServletFilterModuleDescriptor;
import com.atlassian.plugin.servlet.descriptors.ServletModuleDescriptor;
import com.atlassian.plugin.web.descriptors.DefaultWebPanelModuleDescriptor;
import com.atlassian.plugin.web.descriptors.WebItemProviderModuleDescriptor;
import com.atlassian.plugin.web.descriptors.WebPanelRendererModuleDescriptor;
import com.atlassian.plugin.web.descriptors.WebSectionProviderModuleDescriptor;
import com.atlassian.plugin.webresource.WebResourceModuleDescriptor;
import com.atlassian.plugin.webresource.transformer.UrlReadingWebResourceTransformerModuleDescriptor;
import com.atlassian.plugin.webresource.transformer.WebResourceTransformerModuleDescriptor;

public class JiraModuleDescriptorFactory extends DefaultDescribedModuleDescriptorFactory
{
    public JiraModuleDescriptorFactory(final HostContainer hostContainer)
    {
        super(hostContainer);
        addModuleDescriptor("workflow-condition", WorkflowConditionModuleDescriptor.class);
        addModuleDescriptor("workflow-validator", WorkflowValidatorModuleDescriptor.class);
        addModuleDescriptor("workflow-function", WorkflowFunctionModuleDescriptor.class);
        addModuleDescriptor("customfield-type", CustomFieldTypeModuleDescriptorImpl.class);
        addModuleDescriptor("customfield-searcher", CustomFieldSearcherModuleDescriptorImpl.class);
        addModuleDescriptor("issue-tabpanel", IssueTabPanelModuleDescriptorImpl.class);
        addModuleDescriptor("project-operation", ProjectOperationModuleDescriptorImpl.class);
        addModuleDescriptor("web-section", JiraWebSectionModuleDescriptor.class);
        addModuleDescriptor("web-section-provider", WebSectionProviderModuleDescriptor.class);
        addModuleDescriptor("web-item", JiraWebItemModuleDescriptor.class);
        addModuleDescriptor("web-item-provider", WebItemProviderModuleDescriptor.class);
        addModuleDescriptor("simple-link-factory", DefaultSimpleLinkFactoryModuleDescriptor.class);
        addModuleDescriptor("single-issue-view", IssueViewModuleDescriptorImpl.class);
        addModuleDescriptor("search-request-view", SearchRequestViewModuleDescriptorImpl.class);
        addModuleDescriptor("project-tabpanel", ProjectTabPanelModuleDescriptorImpl.class);
        addModuleDescriptor("version-tabpanel", VersionTabPanelModuleDescriptorImpl.class);
        addModuleDescriptor("component-tabpanel", ComponentTabPanelModuleDescriptorImpl.class);
        addModuleDescriptor("project-roleactor", ProjectRoleActorModuleDescriptor.class);
        addModuleDescriptor("report", ReportModuleDescriptorImpl.class);
        addModuleDescriptor("web-resource", WebResourceModuleDescriptor.class);
        addModuleDescriptor("web-resource-transformer", WebResourceTransformerModuleDescriptor.class);
        addModuleDescriptor("url-reading-web-resource-transformer",UrlReadingWebResourceTransformerModuleDescriptor.class);

        addModuleDescriptor("rpc-soap", SoapModuleDescriptor.class);
        addModuleDescriptor("rpc-xmlrpc", XmlRpcModuleDescriptor.class);
        addModuleDescriptor("component", ComponentModuleDescriptor.class);
        addModuleDescriptor("webwork1", WebworkModuleDescriptor.class);

        addModuleDescriptor("jira-renderer", JiraRendererModuleDescriptorImpl.class);
        addModuleDescriptor("macro", MacroModuleDescriptor.class);
        addModuleDescriptor("renderer-component-factory", RendererComponentFactoryDescriptor.class);
        addModuleDescriptor("content-link-resolver", ContentLinkResolverDescriptor.class);
        addModuleDescriptor("top-navigation", TopNavigationModuleDescriptorImpl.class);
        addModuleDescriptor("jira-footer", FooterModuleDescriptorImpl.class);
        addModuleDescriptor("view-profile-panel", ViewProfilePanelModuleDescriptorImpl.class);
        addModuleDescriptor("user-format", UserFormatModuleDescriptorImpl.class);
        addModuleDescriptor("jql-function", JqlFunctionModuleDescriptorImpl.class);
        addModuleDescriptor("keyboard-shortcut", KeyboardShortcutModuleDescriptor.class);

        addModuleDescriptor("issue-link-renderer", IssueLinkRendererModuleDescriptorImpl.class);

        // Crowd integration
        addModuleDescriptor("encoder", PasswordEncoderModuleDescriptor.class);
        addModuleDescriptor("authorisation", AuthorisationModuleDescriptor.class);

        // Notifications
        addModuleDescriptor("notification-filter", NotificationFilterModuleDescriptor.class);

        // User management plugin points
        addModuleDescriptor("password-policy", PasswordPolicyModuleDescriptor.class);
        addModuleDescriptor("pre-delete-user-errors", PreDeleteUserErrorsModuleDescriptor.class);

        // descriptors required by Plugins-2
        addModuleDescriptor("servlet-context-param", ServletContextParamModuleDescriptor.class);
        addModuleDescriptor("servlet", ServletModuleDescriptor.class);
        addModuleDescriptor("servlet-filter", ServletFilterModuleDescriptor.class);
        addModuleDescriptor("servlet-context-listener", ServletContextListenerModuleDescriptor.class);
        addModuleDescriptor("web-panel", DefaultWebPanelModuleDescriptor.class);
        addModuleDescriptor("web-panel-renderer", WebPanelRendererModuleDescriptor.class);
        addModuleDescriptor("listener", EventListenerModuleDescriptor.class);

        // Sitemesh decorators
        addModuleDescriptor("decorator", DecoratorModuleDescriptor.class);
        addModuleDescriptor("decorator-mapper", DecoratorMapperModuleDescriptor.class);

        // language
        addModuleDescriptor("language", LanguageModuleDescriptorImpl.class);
        addModuleDescriptor("translation-transform", TranslationTransformModuleDescriptorImpl.class);

        // special plugin for studio.
        addModuleDescriptor("studio-hooks", StudioHooksModuleDescriptor.class);

        // JRA-32817 - about page
        addModuleDescriptor("about-page-panel", AboutPagePanelModuleDescriptorImpl.class);

        // entity property indexing
        addModuleDescriptor("index-document-configuration", EntityPropertyIndexDocumentModuleDescriptorImpl.class);

        // entity search extractors
        addModuleDescriptor("entity-search-extractor", EntitySearchExtractorModuleDescriptorImpl.class);

        addModuleDescriptor("comment-field-renderer", CommentFieldRendererModuleDescriptorImpl.class);

        // global permissions plugin point
        addModuleDescriptor("global-permission", GlobalPermissionModuleDescriptorImpl.class);

        // project permissions plugin point
        addModuleDescriptor("project-permission", ProjectPermissionModuleDescriptorImpl.class);

        addModuleDescriptor("project-permission-override", ProjectPermissionOverrideModuleDescriptorImpl.class);

        addModuleDescriptor("license-role", LicenseRoleModuleDescriptorImpl.class);
    }
}
