package com.atlassian.jira;

import com.atlassian.applinks.host.OsgiServiceProxyFactory;
import com.atlassian.configurable.XMLObjectConfigurationFactory;
import com.atlassian.core.ofbiz.CoreFactory;
import com.atlassian.crowd.embedded.api.CrowdService;
import com.atlassian.crowd.embedded.core.util.StaticCrowdServiceFactory;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.association.NodeAssociationStore;
import com.atlassian.jira.avatar.AvatarManager;
import com.atlassian.jira.cluster.ClusterServicesManager;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.config.LocaleManager;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.entity.property.JsonEntityPropertyManager;
import com.atlassian.jira.event.ListenerManager;
import com.atlassian.jira.issue.AttachmentManager;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.customfields.manager.OptionsManager;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.index.IssueIndexManager;
import com.atlassian.jira.issue.search.SearchRequestManager;
import com.atlassian.jira.issue.security.IssueSecurityLevelManager;
import com.atlassian.jira.issue.security.IssueSecuritySchemeManager;
import com.atlassian.jira.issue.subscription.SubscriptionManager;
import com.atlassian.jira.mail.MailThreadManager;
import com.atlassian.jira.memoryinspector.MemoryInspector;
import com.atlassian.jira.notification.NotificationSchemeManager;
import com.atlassian.jira.notification.NotificationTypeManager;
import com.atlassian.jira.permission.PermissionSchemeManager;
import com.atlassian.jira.permission.PermissionTypeManager;
import com.atlassian.jira.project.DefaultProjectManager;
import com.atlassian.jira.project.ProjectCategoryStore;
import com.atlassian.jira.project.ProjectFactory;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.project.util.ProjectKeyStore;
import com.atlassian.jira.project.util.ProjectKeyStoreImpl;
import com.atlassian.jira.security.GlobalPermissionManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.SecurityTypeManager;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.service.ServiceManager;
import com.atlassian.jira.sharing.index.DirectoryFactory;
import com.atlassian.jira.sharing.index.MemoryDirectoryFactory;
import com.atlassian.jira.task.TaskManager;
import com.atlassian.jira.tenancy.TenantImpl;
import com.atlassian.jira.transaction.TransactionSupport;
import com.atlassian.jira.upgrade.UpgradeManager;
import com.atlassian.jira.upgrade.UpgradeManagerImpl;
import com.atlassian.jira.user.UserPropertyManager;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.index.IndexLifecycleManager;
import com.atlassian.jira.web.action.admin.customfields.CustomFieldValidator;
import com.atlassian.jira.web.servlet.rpc.AxisServletProvider;
import com.atlassian.jira.web.util.JiraLocaleUtils;
import com.atlassian.jira.web.util.OutlookDateManager;
import com.atlassian.jira.workflow.WorkflowManager;
import com.atlassian.jira.workflow.WorkflowSchemeManager;
import com.atlassian.mail.queue.MailQueue;
import com.atlassian.tenancy.api.Tenant;
import com.atlassian.tenancy.api.event.TenantArrivedEvent;
import com.atlassian.velocity.VelocityManager;

import org.apache.log4j.Logger;
import org.picocontainer.ComponentAdapter;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoContainer;
import org.quartz.Scheduler;

import webwork.util.ValueStack;

/**
 * Provides static methods for obtaining 'Manager' classes, though which much of JIRA's functionality is exposed.
 *
 * @deprecated Use {@link ComponentAccessor} instead. Since v4.4.
 */
@SuppressWarnings ( { "JavaDoc" })
public class ManagerFactory
{
    private static final Logger log = Logger.getLogger(ManagerFactory.class);
    private static volatile ManagerFactory instance = new ManagerFactory();

    /**
     * This method will refresh all the caches in JIRA (hopefully! :)) <strong>This method should not be called by
     * anyone</strong>
     *
     * @deprecated This shit is only being used in "unit" tests that extend from LegacyJiraMockTestCase, don't use this
     *             anymore. Please write your unit tests using mocks.
     */
    public static synchronized void quickRefresh()
    {
        quickRefresh(null);
    }

    /**
     * This method will refresh all the caches in JIRA (hopefully! :)) <strong>This method should not be called by
     * anyone</strong>
     *
     * @deprecated This shit is only being used in "unit" tests that extend from LegacyJiraMockTestCase, don't use this
     *             anymore. Please write your unit tests using mocks.
     */
    @Deprecated
    public static synchronized void quickRefresh(IssueIndexManager indexManager)
    {
        log.debug("ManagerFactory.quickRefresh");

        final IndexLifecycleManager oldIndexManager = getIndexLifecycleManager();
        if (oldIndexManager != null)
        {
            // the tests null this guy because they put in the MemoryIndexManager, should fix the tests but this is a BIG job
            oldIndexManager.shutdown();
        }
        final TaskManager taskManager = ComponentManager.getInstance().getTaskManager();
        if (taskManager != null)
        {
            taskManager.shutdownAndWait(0);
        }

        /*
         * first we shutdown, THEN we unregister listener. why? because some listeners are listening for
         * shutdown-related events.
         */
        ComponentManager.getInstance().stop();
        ComponentManager.getComponentInstanceOfType(EventPublisher.class).unregisterAll();
        ComponentManager.getInstance().dispose();
        ComponentManager.getInstance().initialise(false);
        sendTenantArrivedEvent();

        // Need to let tests put the CrowdReadWrite Service in before we start all our components
        // This needs be be before ProjectManager, else we will be too late and CrowdService will have been instantiated.
        if (StaticCrowdServiceFactory.getCrowdService() != null)
        {
            ManagerFactory.addService(CrowdService.class, StaticCrowdServiceFactory.getCrowdService());
        }

        // reset it to a non-caching version
        addService(ProjectKeyStore.class, new ProjectKeyStoreImpl(ComponentAccessor.getOfBizDelegator()));

        //because we often use projectmanager and we don't want it cached, then we should set it here (for tests only)
        final DefaultProjectManager projectManager = new DefaultProjectManager(ComponentAccessor.getOfBizDelegator(), ComponentAccessor.getComponentOfType(NodeAssociationStore.class),
                ComponentAccessor.getComponentOfType(ProjectFactory.class), ComponentAccessor.getComponentOfType(ProjectRoleManager.class),
                ComponentAccessor.getComponentOfType(IssueManager.class),
                ComponentAccessor.getComponentOfType(AvatarManager.class), ComponentAccessor.getComponentOfType(UserManager.class),
                ComponentAccessor.getComponentOfType(ProjectCategoryStore.class), ComponentAccessor.getApplicationProperties(),
                ComponentAccessor.getComponentOfType(ProjectKeyStore.class), ComponentAccessor.getComponentOfType(TransactionSupport.class), null,
                ComponentAccessor.getComponentOfType(JsonEntityPropertyManager.class),ComponentAccessor.getComponentOfType(EventPublisher.class));
        addService(ProjectManager.class, projectManager);

        // we want the shared entities to use a memory based DirectoryFactory
        addService(DirectoryFactory.class, new MemoryDirectoryFactory());

        addService(OsgiServiceProxyFactory.class, new OsgiServiceProxyFactory((OsgiServiceProxyFactory.ServiceTrackerFactory) null));

        // Need to let tests put the memory index manager in before we start all of our components
        if (indexManager != null)
        {
            ManagerFactory.addService(IssueIndexManager.class, indexManager);
        }

        // Need to bootstrap all the components that need initialising
        ComponentManager.getInstance().start();
        CoreFactory.globalRefresh();

        getConstantsManager().refresh();
    }

    /**
     * This should *never* be called, except in tests, or if you are importing or seting up for the first time. The
     * reason this is called is to ensure that all the managers are reinitialised after the license has changed.
     * <p/>
     * Note: Make sure the scheduler is shutdown
     */
    public static synchronized void globalRefresh()
    {
        log.debug("ManagerFactory.globalRefresh");

        getIndexLifecycleManager().shutdown();

        // shutdown task manager
        final TaskManager taskManager = ComponentManager.getInstance().getTaskManager();
        taskManager.shutdownAndWait(0);

        ComponentAccessor.getComponent(ClusterServicesManager.class).stopServices();
        ComponentManager.getInstance().stop();
        //TODO [PICO] permgen-prev? - deregister events?
        ComponentManager.getInstance().dispose();

        // Look for possible memory leaks
        new MemoryInspector().inspectMemoryAfterJiraShutdown();

        // Rebuild all the registered objects
        ComponentManager.getInstance().initialise();
        sendTenantArrivedEvent();

        // clear the method cache for JRA-16750
        ValueStack.clearMethods();

        // Need to bootstrap all the components that need initialising
        ComponentManager.getInstance().start();
        CoreFactory.globalRefresh();
        ComponentManager.getComponentInstanceOfType(AxisServletProvider.class).reset();

        getConstantsManager().refresh();
    }

    private static void sendTenantArrivedEvent()
    {
        // Until we have a proper mechanism to tenant an instance, this event is sent during JIRA startup (see
        // DefaultJiraLauncher and TenancyLauncher. Since the xxxRefresh() methods destroy and recreate the Pico container
        // we need to resend the event here too.
        final String baseUrl = ComponentAccessor.getComponent(ApplicationProperties.class).getDefaultBackedString(APKeys.JIRA_BASEURL);
        ComponentAccessor.getComponent(EventPublisher.class).publish(new TenantArrivedEvent(new TenantImpl(baseUrl)));
    }

    // registry stuff - see Registry pattern in Martin Fowler's Enterprise Patterns book
    private static ManagerFactory getInstance()
    {
        return instance;
    }

    private ManagerFactory()
    {
    }

    private <T> void setService(final Class<T> clazz, final T instance)
    {
        addService(clazz, instance);
    }

    /**
     * @deprecated This shit is only being used in "unit" tests that extend from LegacyJiraMockTestCase, don't use this
     *             anymore. Please write your unit tests using mocks.
     */
    @Deprecated
    public static <T> ComponentAdapter<T> addService(final Class<T> clazz, final T instance)
    {
        final PicoContainer container = getContainer();
        final MutablePicoContainer mutableContainer = (MutablePicoContainer) container;
        //unregister current implemenation (if one exists) - prevent duplicate registration exception
        final ComponentAdapter<T> result = mutableContainer.removeComponent(clazz);
        if (instance != null)
        {
            mutableContainer.addComponent(clazz, instance);
        }
        return result;
    }

    /**
     * @deprecated This shit is only being used in "unit" tests that extend from LegacyJiraMockTestCase, don't use this
     *             anymore. Please write your unit tests using mocks.
     */
    @Deprecated
    public static void removeService(final Class<?> clazz)
    {
        addService(clazz, null);
    }

    /**
     * This method resets the registry.
     */
    public static void initialise()
    {
        instance = new ManagerFactory();
    }

    /**
     * @deprecated This shit is only being used in "unit" tests that extend from LegacyJiraMockTestCase, don't use this
     *             anymore. Please write your unit tests using mocks.
     */
    @Deprecated
    public static void refreshIssueManager()
    {
        getInstance().setService(IssueManager.class, ComponentManager.getInstance().getIssueManager());
    }

    /**
     * Create a new UpgradeManager.  This may be needed if more upgrade tasks are added, or if the license has been
     * changed.
     *
     * @deprecated Use {@link ComponentManager#refreshUpgradeManager()} instead. That method is as nasty as this one.
     */
    @Deprecated
    public static void refreshUpgradeManager()
    {
        //this is very ugly.  We should find a way to get Pico to reload its classes.
        final PicoContainer container = getContainer();
        final MutablePicoContainer mutableContainer = (MutablePicoContainer) container;
        mutableContainer.removeComponent(UpgradeManager.class);
        mutableContainer.addComponent(UpgradeManager.class, UpgradeManagerImpl.class);
    }

    @Deprecated
    /**
     * @deprecated Use {@link ComponentAccessor#getApplicationProperties()} instead. Since v5.0.
     */
    public static ApplicationProperties getApplicationProperties()
    {
        return getContainer().getComponent(ApplicationProperties.class);
    }

    @Deprecated
    /**
     * @deprecated Use {@link ComponentAccessor#getAttachmentManager()} instead. Since v5.0.
     */
    public static AttachmentManager getAttachmentManager()
    {
        return ComponentManager.getInstance().getAttachmentManager();
    }

    @Deprecated
    /**
     * @deprecated Use {@link ComponentAccessor#getConstantsManager()} instead. Since v5.0.
     */
    public static ConstantsManager getConstantsManager()
    {
        return getContainer().getComponent(ConstantsManager.class);
    }

    @Deprecated
    /**
     * @deprecated Use {@link ComponentAccessor#getCustomFieldManager()} instead. Since v5.0.
     */
    public static CustomFieldManager getCustomFieldManager()
    {
        return getContainer().getComponent(CustomFieldManager.class);
    }

    @Deprecated
    /**
     * @deprecated Use {@link ComponentAccessor#getFieldManager()} instead. Since v5.0.
     */
    public static FieldManager getFieldManager()
    {
        return getContainer().getComponent(FieldManager.class);
    }

    @Deprecated
    public static IndexLifecycleManager getIndexLifecycleManager()
    {
        return ComponentManager.getInstance().getIndexLifecycleManager();
    }

    @Deprecated
    /**
     * @deprecated Use {@link ComponentAccessor#getIssueIndexManager()} instead. Since v5.0.
     */
    public static IssueIndexManager getIndexManager()
    {
        return ComponentManager.getInstance().getIndexManager();
    }

    @Deprecated
    /**
     * @deprecated Use {@link ComponentAccessor#getIssueManager()} instead. Since v4.4.
     */
    public static IssueManager getIssueManager()
    {
        return getContainer().getComponent(IssueManager.class);
    }

    @Deprecated
    public static IssueSecuritySchemeManager getIssueSecuritySchemeManager()
    {
        return getContainer().getComponent(IssueSecuritySchemeManager.class);
    }

    @Deprecated
    public static SecurityTypeManager getIssueSecurityTypeManager()
    {
        return getContainer().getComponent(SecurityTypeManager.class);
    }

    @Deprecated
    /**
     * @deprecated Use {@link ComponentAccessor#getListenerManager()} instead. Since v5.0.
     */
    public static ListenerManager getListenerManager()
    {
        return getContainer().getComponent(ListenerManager.class);
    }

    @Deprecated
    /**
     * @deprecated Use {@link ComponentAccessor#getMailQueue()} instead. Since v5.0.
     */
    public static MailQueue getMailQueue()
    {
        return getContainer().getComponent(MailQueue.class);
    }

    @Deprecated
    /**
     * @deprecated Use {@link ComponentAccessor#getNotificationSchemeManager()} instead. Since v5.0.
     */
    public static NotificationSchemeManager getNotificationSchemeManager()
    {
        return getContainer().getComponent(NotificationSchemeManager.class);
    }

    @Deprecated
    public static NotificationTypeManager getNotificationTypeManager()
    {
        return getContainer().getComponent(NotificationTypeManager.class);
    }

    @Deprecated
    public static XMLObjectConfigurationFactory getObjectConfigurationFactory()
    {
        return getContainer().getComponent(XMLObjectConfigurationFactory.class);
    }

    @Deprecated
    /**
     * @deprecated Use {@link ComponentAccessor#getPermissionManager()} instead. Since v5.0.
     */
    public static PermissionManager getPermissionManager()
    {
        return ComponentManager.getInstance().getPermissionManager();
    }

    @Deprecated
    /**
     * @deprecated Use {@link ComponentAccessor#getPermissionSchemeManager()} instead. Since v5.0.
     */
    public static PermissionSchemeManager getPermissionSchemeManager()
    {
        return getContainer().getComponent(PermissionSchemeManager.class);
    }

    @Deprecated
    public static PermissionTypeManager getPermissionTypeManager()
    {
        return getContainer().getComponent(PermissionTypeManager.class);
    }

    /**
     * @deprecated Use {@link ComponentAccessor#getProjectManager()} instead. Since v5.0.
     */
    @Deprecated
    public static ProjectManager getProjectManager()
    {
        return getContainer().getComponent(ProjectManager.class);
    }

    /**
     * @return a Quartz scheduler
     * @deprecated Use {@link ComponentAccessor#getScheduler()} instead. Since v5.0.  Since v6.3, you should
     *          use the {@code SchedulerService} instead.
     */
    @Deprecated
    public static Scheduler getScheduler()
    {
        return getContainer().getComponent(Scheduler.class);
    }

    /**
     * @deprecated Use {@link ComponentAccessor#getIssueSecurityLevelManager()} instead. Since v5.0.
     */
    @Deprecated
    public static IssueSecurityLevelManager getIssueSecurityLevelManager()
    {
        return getContainer().getComponent(IssueSecurityLevelManager.class);
    }

    @Deprecated
    public static SearchRequestManager getSearchRequestManager()
    {
        return getContainer().getComponent(SearchRequestManager.class);
    }

    /**
     * @deprecated Use {@link ComponentAccessor#getServiceManager()} instead. Since v5.0.
     */
    @Deprecated
    public static ServiceManager getServiceManager()
    {
        return getContainer().getComponent(ServiceManager.class);
    }

    /**
     * @deprecated Use {@link ComponentAccessor#getSubscriptionManager()} instead. Since v5.0.
     */
    @Deprecated
    public static SubscriptionManager getSubscriptionManager()
    {
        return getContainer().getComponent(SubscriptionManager.class);
    }

    /**
     * @deprecated Use {@link com.atlassian.jira.ComponentManager#getUpgradeManager()} instead. Since v5.0.
     */
    @Deprecated
    public static UpgradeManager getUpgradeManager()
    {
        return getContainer().getComponent(UpgradeManager.class);
    }

    /**
     * @deprecated Use {@link ComponentAccessor#getGlobalPermissionManager()} instead. Since v5.0.
     */
    @Deprecated
    public static GlobalPermissionManager getGlobalPermissionManager()
    {
        return getContainer().getComponent(GlobalPermissionManager.class);
    }

    /**
     * @deprecated Use {@link ComponentAccessor#getVelocityManager()} instead. Since v5.0.
     */
    @Deprecated
    public static VelocityManager getVelocityManager()
    {
        return getContainer().getComponent(VelocityManager.class);
    }

    @Deprecated
    public static OutlookDateManager getOutlookDateManager()
    {
        return getContainer().getComponent(OutlookDateManager.class);
    }

    /**
     * @deprecated Use {@link ComponentAccessor#getWorkflowManager()} instead. Since v5.0.
     */
    @Deprecated
    public static WorkflowManager getWorkflowManager()
    {
        return ComponentManager.getInstance().getWorkflowManager();
    }

    /**
     * @deprecated Use {@link ComponentAccessor#getLocaleManager()} instead. Since v5.0.
     */
    @Deprecated
    public static LocaleManager getLocaleManager()
    {
        return getContainer().getComponent(LocaleManager.class);
    }

    @Deprecated
    public static JiraLocaleUtils getJiraLocaleUtils()
    {
        return getContainer().getComponent(JiraLocaleUtils.class);
    }

    /**
     * @return MailThreadManager
     *
     * @deprecated Use {@link ComponentAccessor#getMailThreadManager()} instead. Since v4.4.
     */
    @Deprecated
    public static MailThreadManager getMailThreadManager()
    {
        return getContainer().getComponent(MailThreadManager.class);
    }

    /**
     * @deprecated Use {@link ComponentAccessor#getWorkflowSchemeManager()} instead. Since v5.0.
     */
    @Deprecated
    public static WorkflowSchemeManager getWorkflowSchemeManager()
    {
        return getContainer().getComponent(WorkflowSchemeManager.class);
    }

    /**
     * @deprecated Use {@link ComponentAccessor#getOptionsManager()} instead. Since v5.0.
     */
    @Deprecated
    public static OptionsManager getOptionsManager()
    {
        return getContainer().getComponent(OptionsManager.class);
    }

    @Deprecated
    public static CustomFieldValidator getCustomFieldValidator()
    {
        return getContainer().getComponent(CustomFieldValidator.class);
    }

    /**
     * @deprecated Use {@link ComponentAccessor#getUserManager()} instead. Since v5.0.
     */
    @Deprecated
    public static UserManager getUserManager()
    {
        return getContainer().getComponent(UserManager.class);
    }

    /**
     * @deprecated Use {@link ComponentAccessor#getGroupManager()} instead. Since v5.0.
     */
    @Deprecated
    public static GroupManager getGroupManager()
    {
        return getContainer().getComponent(GroupManager.class);
    }

    /**
     * @deprecated Use {@link ComponentAccessor#getUserPropertyManager()} instead. Since v5.0.
     */
    @Deprecated
    public static UserPropertyManager getUserPropertyManager()
    {
        return getContainer().getComponent(UserPropertyManager.class);
    }

    private static PicoContainer getContainer()
    {
        return ComponentManager.getInstance().getContainer();
    }
}
