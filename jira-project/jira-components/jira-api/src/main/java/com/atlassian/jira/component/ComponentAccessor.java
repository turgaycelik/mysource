package com.atlassian.jira.component;

import javax.annotation.Nonnull;

import com.atlassian.annotations.ExperimentalApi;
import com.atlassian.annotations.Internal;
import com.atlassian.annotations.PublicApi;
import com.atlassian.crowd.embedded.api.CrowdService;
import com.atlassian.jira.avatar.AvatarManager;
import com.atlassian.jira.avatar.AvatarService;
import com.atlassian.jira.bc.issue.IssueService;
import com.atlassian.jira.bc.project.component.ProjectComponentManager;
import com.atlassian.jira.bulkedit.BulkOperationManager;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.config.LocaleManager;
import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.config.util.AttachmentPathManager;
import com.atlassian.jira.config.util.IndexPathManager;
import com.atlassian.jira.event.ListenerManager;
import com.atlassian.jira.event.issue.IssueEventManager;
import com.atlassian.jira.event.type.EventTypeManager;
import com.atlassian.jira.issue.AttachmentManager;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.IssueFactory;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.RendererManager;
import com.atlassian.jira.issue.changehistory.ChangeHistoryManager;
import com.atlassian.jira.issue.comments.CommentManager;
import com.atlassian.jira.issue.customfields.manager.OptionsManager;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.config.manager.FieldConfigSchemeManager;
import com.atlassian.jira.issue.fields.config.manager.IssueTypeSchemeManager;
import com.atlassian.jira.issue.fields.layout.column.ColumnLayoutManager;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.issue.fields.screen.FieldScreenManager;
import com.atlassian.jira.issue.fields.screen.FieldScreenRendererFactory;
import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenSchemeManager;
import com.atlassian.jira.issue.index.IssueIndexManager;
import com.atlassian.jira.issue.link.IssueLinkManager;
import com.atlassian.jira.issue.security.IssueSecurityLevelManager;
import com.atlassian.jira.issue.subscription.SubscriptionManager;
import com.atlassian.jira.issue.vote.VoteManager;
import com.atlassian.jira.issue.watchers.WatcherManager;
import com.atlassian.jira.issue.worklog.WorklogManager;
import com.atlassian.jira.mail.MailThreadManager;
import com.atlassian.jira.movesubtask.MoveSubTaskOperationManager;
import com.atlassian.jira.notification.NotificationSchemeManager;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.permission.PermissionContextFactory;
import com.atlassian.jira.permission.PermissionSchemeManager;
import com.atlassian.jira.plugin.ComponentClassManager;
import com.atlassian.jira.project.ProjectFactory;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.project.version.VersionManager;
import com.atlassian.jira.security.GlobalPermissionManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.service.ServiceManager;
import com.atlassian.jira.user.UserKeyService;
import com.atlassian.jira.user.UserPropertyManager;
import com.atlassian.jira.user.preferences.UserPreferencesManager;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.JiraDurationUtils;
import com.atlassian.jira.util.VelocityParamFactory;
import com.atlassian.jira.web.action.admin.translation.TranslationManager;
import com.atlassian.jira.workflow.WorkflowManager;
import com.atlassian.jira.workflow.WorkflowSchemeManager;
import com.atlassian.mail.queue.MailQueue;
import com.atlassian.mail.server.MailServerManager;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.PluginController;
import com.atlassian.plugin.event.PluginEventManager;
import com.atlassian.plugin.webresource.WebResourceManager;
import com.atlassian.plugin.webresource.WebResourceUrlProvider;
import com.atlassian.scheduler.SchedulerService;
import com.atlassian.velocity.VelocityManager;

import org.quartz.Scheduler;

/**
 * Provides static methods for accessing JIRA's managed components &mdash; that is, the components in
 * the PicoContainer.
 * <p/>
 * Normally, developers should get the dependencies injected into the constructor of the calling class
 * by Pico; however, this utility provides access for when that is impossible or impractical.  Examples
 * include:
 * <ul>
 * <li>Components with circular dependencies between them, as one of them must be resolved
 *      first without the other dependency available for injection yet</li>
 * <li>Classes that are not injectable components but are instead explicitly constructed
 *      in a long chain of classes that would not otherwise need the target component</li>
 * <li>Static-only utility classes, which are never constructed at all</li>
 * </ul>
 * <p/>
 * Plugin developers that are trying to figure out how to fix an {@code IllegalStateException} that they
 * are getting in a unit test should read the documentation for the {@code MockComponentWorker} in the
 * {@code jira-tests} artifact for instructions.
 *
 * @since v4.3
 */
@SuppressWarnings ( { "UnusedDeclaration" })
@PublicApi
public class ComponentAccessor
{
    private static final String ERROR_MSG = "ComponentAccessor has not been initialised.\n\n"
            + "This is not expected to occur on a production system.  Developers that\n"
            + "encounter this message within a unit test should read the documentation\n"
            + "for MockComponentWorker in the jira-tests artifact for more information\n"
            + "about what causes this error and how to address it.\n\n";

    private volatile static Worker _worker;

    /**
     * Returns the core component which is stored in JIRA's Dependency Injection container under the key that is the given class.
     * <p>
     * In practise, this is the same as {@link #getComponentOfType(Class)} except it will fail faster if the
     * given Class is not a known component key (it also has a shorter and more meaningful name).
     * <p>
     * Please note that this method only gets components from JIRA's core Pico Container.
     * That is, it retrieves core components and components declared in Plugins1 plugins, but not components declared in Plugins2 plugins.
     *
     * @param componentClass class to find a component instance by
     * @return the dependency injection component
     * @see #getComponentOfType(Class)
     */
    public static <T> T getComponent(Class<T> componentClass)
    {
        return getWorker().getComponent(componentClass);
    }

    /**
     * Returns a thread-safe, {@code Serializable} lazy reference to the core component which is stored in JIRA's
     * Dependency Injection container under the key that is the given class.
     *
     * @param componentClass class to find a component instance by
     * @return the dependency injection component reference
     * @see ComponentReference
     */
    @ExperimentalApi
    public static <T> ComponentReference<T> getComponentReference(@Nonnull Class<T> componentClass)
    {
        return new ComponentReference<T>(componentClass);
    }

    /**
     * Returns the core component of the given Type (a Class or an Interface) which is stored in JIRA's
     * Dependency Injection container.
     * <p>
     * First it tries to find the component using the given Class as a key (like {@link #getComponent(Class)}),
     * however, if this fails then it will try to find a <em>unique</em> component that implements/extends the given Class.
     * This seems unlikely to be useful, but is included for now, for completeness and backward compatibility.
     * <p>
     * Please note that this method only gets components from JIRA's core Pico Container.
     * That is, it retrieves core components and components declared in Plugins1 plugins, but not components declared in Plugins2 plugins.
     *
     * @param componentClass class to find a component instance by
     * @return the dependency injection component
     * @see #getComponent(Class)
     */
    public static <T> T getComponentOfType(Class<T> componentClass)
    {
        return getWorker().getComponentOfType(componentClass);
    }

    /**
     * Retrieves and returns a public component from OSGi land via its class name.  This method can be used to retrieve
     * a component provided via a plugins2 OSGi bundle.  Please note that components returned via this method should
     * <strong>NEVER</strong> be cached (for example, by saving it in a static field) as they may be refreshed at
     * any time as a plugin is enabled/disabled or the componentManager is reinitialised (after an XML import).
     * <p/>
     * It is important to note that this only works for public components. That is components with {@code public="true"}
     * declared in their XML configuration.  This means that they are available for other plugins to import.
     * <p/>
     * A example use case for this method is the dashboards plugin.  In several areas in JIRA we may want to
     * render gadgets via the {@link com.atlassian.gadgets.view.GadgetViewFactory}.  Whilst the interface for this
     * component is available in JIRA core, the implementation is provided by the dashboards OSGi bundle.  This method
     * will allow us to access it.
     *
     * @param componentClass class to find an OSGi component instance for
     * @return found component
     * @see #getComponentOfType(Class)
     */
    public static <T> T getOSGiComponentInstanceOfType(Class<T> componentClass)
    {
        return getWorker().getOSGiComponentInstanceOfType(componentClass);
    }

    public static ProjectManager getProjectManager()
    {
        return getComponent(ProjectManager.class);
    }

    public static ApplicationProperties getApplicationProperties()
    {
        return getComponent(ApplicationProperties.class);
    }

    public static JiraAuthenticationContext getJiraAuthenticationContext()
    {
        return getComponent(JiraAuthenticationContext.class);
    }

    public static JiraDurationUtils getJiraDurationUtils() {
        return getComponent(JiraDurationUtils.class);
    }

    public static ConstantsManager getConstantsManager()
    {
        return getComponent(ConstantsManager.class);
    }

    public static VelocityManager getVelocityManager()
    {
        return getComponent(VelocityManager.class);
    }       

    public static VelocityParamFactory getVelocityParamFactory()
    {
        return getComponent(VelocityParamFactory.class);
    }

    public static I18nHelper.BeanFactory getI18nHelperFactory()
    {
        return getComponent(I18nHelper.BeanFactory.class);
    }

    public static FieldManager getFieldManager()
    {
        return getComponent(FieldManager.class);
    }

    public static IssueManager getIssueManager()
    {
        return getComponent(IssueManager.class);
    }

    public static AttachmentManager getAttachmentManager()
    {
        return getComponent(AttachmentManager.class);
    }

    public static UserManager getUserManager()
    {
        return getComponent(UserManager.class);
    }

    public static UserKeyService getUserKeyService()
    {
        return getComponent(UserKeyService.class);
    }

    public static PermissionManager getPermissionManager()
    {
        return getComponent(PermissionManager.class);
    }

    public static PermissionContextFactory getPermissionContextFactory()
    {
        return getComponent(PermissionContextFactory.class);
    }

    public static CustomFieldManager getCustomFieldManager()
    {
        return getComponent(CustomFieldManager.class);
    }

    public static FieldConfigSchemeManager getFieldConfigSchemeManager()
    {
        return getComponent(FieldConfigSchemeManager.class);
    }

    public static UserUtil getUserUtil()
    {
        return getComponent(UserUtil.class);
    }

    public static GroupManager getGroupManager()
    {
        return getComponent(GroupManager.class);
    }

    public static EventTypeManager getEventTypeManager()
    {
        return getComponent(EventTypeManager.class);
    }

    public static IssueEventManager getIssueEventManager()
    {
        return getComponent(IssueEventManager.class);
    }

    public static WorkflowManager getWorkflowManager()
    {
        return getComponent(WorkflowManager.class);
    }

    public static IssueFactory getIssueFactory()
    {
        return getComponent(IssueFactory.class);
    }

    public static VersionManager getVersionManager()
    {
        return getComponent(VersionManager.class);
    }

    public static CommentManager getCommentManager()
    {
        return getComponent(CommentManager.class);
    }

    public static MailThreadManager getMailThreadManager()
    {
        return getComponent(MailThreadManager.class);
    }    

    /**
     * Retrieves and returns the web resource manager instance
     *
     * @return web resource manager
     */
    public static WebResourceManager getWebResourceManager()
    {
        return getComponent(WebResourceManager.class);
    }

    /**
     * Retrieves and returns the web resource URL provider instance
     *
     * @return web resource URL provider
     */
    public static WebResourceUrlProvider getWebResourceUrlProvider()
    {
        return getComponent(WebResourceUrlProvider.class);
    }

    /**
     * Retrieves and return the bulk operation manager instance
     *
     * @return bulk operation manager
     */
    public static BulkOperationManager getBulkOperationManager()
    {
        return getComponent(BulkOperationManager.class);
    }

    /**
     * Retrieves and returns the move subtask operation manager instance
     *
     * @return move subtask operation manager
     */
    public static MoveSubTaskOperationManager getMoveSubTaskOperationManager()
    {
        return getComponent(MoveSubTaskOperationManager.class);
    }

    /**
     * Retrieves and returns the worklog manager instance
     *
     * @return worklog manager
     */
    public static WorklogManager getWorklogManager()
    {
        return getComponent(WorklogManager.class);
    }

    /**
     * Retrieves and returns the project factory instance
     *
     * @return project factory
     */
    public static ProjectFactory getProjectFactory()
    {
        return getComponent(ProjectFactory.class);
    }

    /**
     * Retrieves and returns the issue type scheme manager instance
     *
     * @return issue type scheme manager
     */
    public static IssueTypeSchemeManager getIssueTypeSchemeManager()
    {
        return getComponent(IssueTypeSchemeManager.class);
    }

    /**
     * Retrieves and returns the issue type screen scheme manager instance
     *
     * @return issue type screen scheme manager
     */
    public static IssueTypeScreenSchemeManager getIssueTypeScreenSchemeManager()
    {
        return getComponent(IssueTypeScreenSchemeManager.class);
    }

    /**
     * Retrieves and returns the subtask manager instance
     *
     * @return subtask manager
     */
    public static SubTaskManager getSubTaskManager()
    {
        return getComponent(SubTaskManager.class);
    }

    /**
     * Returns the IssueLinkManager component.
     *
     * @return the IssueLinkManager component.
     */
    public static IssueLinkManager getIssueLinkManager()
    {
        return getComponent(IssueLinkManager.class);
    }

    public static CrowdService getCrowdService()
    {
        return getComponent(CrowdService.class);
    }

    /**
     * Retrieves and returns the field layout manager
     *
     * @return field layout manager
     */
    public static FieldLayoutManager getFieldLayoutManager()
    {
        return getComponent(FieldLayoutManager.class);
    }

    /**
     * Retrieves and returns the column layout manager instance
     *
     * @return column layout manager
     */
    public static ColumnLayoutManager getColumnLayoutManager()
    {
        return getComponent(ColumnLayoutManager.class);
    }

    /**
     * Retrieves and returns the vote manager instance
     *
     * @return vote manager
     */
    public static VoteManager getVoteManager()
    {
        return getComponent(VoteManager.class);
    }

    public static PluginAccessor getPluginAccessor()
    {
        return getComponent(PluginAccessor.class);
    }

    public static PluginEventManager getPluginEventManager()
    {
        return getComponent(PluginEventManager.class);
    }

    public static ComponentClassManager getComponentClassManager()
    {
        return getComponent(ComponentClassManager.class);
    }

    public static PluginController getPluginController()
    {
        return getComponent(PluginController.class);
    }

    /**
     * Retrieves the RendererManager component.
     *
     * @return the RendererManager component.
     */
    public static RendererManager getRendererManager()
    {
        return getComponent(RendererManager.class);
    }

    /**
     * Retrieves and returns the field screen renderer factory instance
     *
     * @return field screen renderer factory
     */
    public static FieldScreenRendererFactory getFieldScreenRendererFactory()
    {
        return getComponent(FieldScreenRendererFactory.class);
    }

    /**
     * Retrieves and returns the workflow scheme manager instance
     *
     * @return workflow scheme manager
     */
    public static WorkflowSchemeManager getWorkflowSchemeManager()
    {
        return getComponent(WorkflowSchemeManager.class);
    }

    /**
     * Returns the IssueIndexManager component.
     *
     * @return the IssueIndexManager component.
     */
    public static IssueIndexManager getIssueIndexManager()
    {
        return getComponent(IssueIndexManager.class);
    }

    /**
     * Retrieves and returns the issue service instance
     *
     * @return issue service
     */
    public static IssueService getIssueService()
    {
        return getComponent(IssueService.class);
    }

    /**
     * Retrieves and returns the index path manager instance
     *
     * @return index path manager
     */
    public static IndexPathManager getIndexPathManager()
    {
        return getComponent(IndexPathManager.class);
    }

    /**
     * Retrieves and returns the attachment path instance
     *
     * @return attachment path manager
     */
    public static AttachmentPathManager getAttachmentPathManager()
    {
        return getComponent(AttachmentPathManager.class);
    }

    /**
     * Retrieves and returns the translation manager instance
     *
     * @return translation manager
     */
    public static TranslationManager getTranslationManager()
    {
        return getComponent(TranslationManager.class);
    }

    /**
     * Retrieves and returns the watcher manager instance
     *
     * @return watcher manager
     */
    public static WatcherManager getWatcherManager()
    {
        return getComponent(WatcherManager.class);
    }

    /**
     * Retrieves and returns the field screen manager instance
     *
     * @return field screen manager
     */
    public static FieldScreenManager getFieldScreenManager()
    {
        return getComponent(FieldScreenManager.class);
    }

    /**
     * Retrieves and returns the mail server manager instance
     *
     * @return mail server manager
     */
    public static MailServerManager getMailServerManager()
    {
        return getComponent(MailServerManager.class);
    }

    /**
     * Retrieves and returns the project component manager instance
     *
     * @return project component manager
     */
    public static ProjectComponentManager getProjectComponentManager()
    {
        return getComponent(ProjectComponentManager.class);
    }

    /**
     * Retrieves and returns the {@link com.atlassian.jira.issue.changehistory.ChangeHistoryManager} manager instance
     *
     * @return ChangeHistoryManager
     */
    public static ChangeHistoryManager getChangeHistoryManager()
    {
        return getComponent(ChangeHistoryManager.class);
    }

    /**
     * Retrieves and returns the user preferences manager instance
     *
     * @return user preferences manager
     */
    public static UserPreferencesManager getUserPreferencesManager()
    {
        return getComponent(UserPreferencesManager.class);
    }

    /**
     * Retrieves and returns the user preferences manager instance
     *
     * @return user preferences manager
     */
    public static UserPropertyManager getUserPropertyManager()
    {
        return getComponent(UserPropertyManager.class);
    }

    public static AvatarService getAvatarService()
    {
        return getComponent(AvatarService.class);
    }

    public static AvatarManager getAvatarManager()
    {
        return getComponent(AvatarManager.class);
    }    

    public static ListenerManager getListenerManager()
    {
        return getComponent(ListenerManager.class);
    }

    public static MailQueue getMailQueue()
    {
        return getComponent(MailQueue.class);
    }

    public static NotificationSchemeManager getNotificationSchemeManager()
    {
        return getComponent(NotificationSchemeManager.class);
    }

    public static PermissionSchemeManager getPermissionSchemeManager()
    {
        return getComponent(PermissionSchemeManager.class);
    }

    /**
     * Returns the Quartz scheduler.
     *
     * @return the Quartz scheduler
     * @deprecated since v6.2; to be removed in v7.0.  Please use the {@link SchedulerService} instead of accessing
     *          Quartz directly.  It should be injected if possible; otherwise, use
     *          {@link #getComponent(Class) getComponent(SchedulerService.class)} to obtain it.
     */
    @Deprecated
    public static Scheduler getScheduler()
    {
        return getComponent(Scheduler.class);
    }

    public static IssueSecurityLevelManager getIssueSecurityLevelManager()
    {
        return getComponent(IssueSecurityLevelManager.class);
    }

    public static ServiceManager getServiceManager()
    {
        return getComponent(ServiceManager.class);
    }

    public static SubscriptionManager getSubscriptionManager()
    {
        return getComponent(SubscriptionManager.class);
    }

    public static GlobalPermissionManager getGlobalPermissionManager()
    {
        return getComponent(GlobalPermissionManager.class);
    }

    public static LocaleManager getLocaleManager()
    {
        return getComponent(LocaleManager.class);
    }

    public static OptionsManager getOptionsManager()
    {
        return getComponent(OptionsManager.class);
    }

    public static OfBizDelegator getOfBizDelegator()
    {
        return getComponent(OfBizDelegator.class);
    }

    private static Worker getWorker()
    {
        final Worker worker = _worker;
        if (worker == null)
        {
            throw new IllegalStateException(ERROR_MSG);
        }
        return worker;
    }

    /**
     * This is called during system bootstrap to initialise this static helper class.
     *
     * Plugin developers should never call this in production code, although it is useful to put a mock Worker in here
     * inside unit tests.
     *
     * @param componentAccessorWorker The worker that this static class delegates to in order to do actual work.
     * @return the passed worker (a convenience for unit test method chaining)
     */
    @Internal
    public static Worker initialiseWorker(Worker componentAccessorWorker)
    {
        Worker currentWorker = _worker;
        _worker = componentAccessorWorker;
        return currentWorker;
    }

    @Internal
    public static interface Worker
    {
        <T> T getComponent(Class<T> componentClass);

        <T> T getComponentOfType(Class<T> componentClass);

        <T> T getOSGiComponentInstanceOfType(Class<T> componentClass);
    }
}
