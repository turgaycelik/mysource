package com.atlassian.jira.local.testutils;

import java.sql.Connection;
import java.util.Collection;
import java.util.Collections;

import javax.servlet.ServletContextEvent;

import com.atlassian.applinks.host.OsgiServiceProxyFactory;
import com.atlassian.core.ofbiz.CoreFactory;
import com.atlassian.core.ofbiz.util.CoreTransactionUtil;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.JiraTestUtil;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.component.ComponentAccessorWorker;
import com.atlassian.jira.config.database.DatabaseConfig;
import com.atlassian.jira.config.database.DatabaseConfigurationManager;
import com.atlassian.jira.config.database.JdbcDatasource;
import com.atlassian.jira.index.DefaultSearchExtractorRegistrationManager;
import com.atlassian.jira.index.SearchExtractorRegistrationManager;
import com.atlassian.jira.issue.index.ChangeHistoryDocumentFactory;
import com.atlassian.jira.issue.index.CommentDocumentFactory;
import com.atlassian.jira.issue.index.DefaultChangeHistoryDocumentFactory;
import com.atlassian.jira.issue.index.DefaultCommentDocumentFactory;
import com.atlassian.jira.issue.index.DefaultIssueDocumentFactory;
import com.atlassian.jira.issue.index.IssueDocumentFactory;
import com.atlassian.jira.issue.index.MemoryIndexManager;
import com.atlassian.jira.license.JiraLicenseManager;
import com.atlassian.jira.mock.servlet.MockServletContext;
import com.atlassian.jira.osgi.MockOsgiServiceProxyFactory;
import com.atlassian.jira.security.JiraAuthenticationContextImpl;
import com.atlassian.jira.startup.JiraHomePathLocator;
import com.atlassian.jira.startup.JiraStartupChecklist;
import com.atlassian.jira.util.TempDirectoryUtil;
import com.atlassian.jira.util.index.IndexLifecycleManager;
import com.atlassian.jira.util.index.MockIndexLifecycleManager;
import com.atlassian.jira.web.ServletContextProviderListener;
import com.atlassian.jira.web.SessionKeys;

import com.opensymphony.workflow.TypeResolver;

import org.apache.log4j.Logger;
import org.hsqldb.jdbcDriver;
import org.ofbiz.core.entity.GenericDelegator;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.model.ModelEntity;
import org.ofbiz.core.entity.model.ModelReader;
import org.ofbiz.core.entity.model.ModelViewEntity;

import webwork.action.ActionContext;

public class UtilsForTestSetup
{
    // this license is copied from com.atlassian.jira.webtests.LicenseKeys
    private static final String V2_LICENSE = "AAABBA0ODAoPeNptj1FPgzAUhd/7K5r4jGmZmkDSBwaNogMWYM4svtTuOmtGR27L4v69bOiL8ek+n\n"
            + "JzvO/dqDVuaDDvKbim/i2cs5iGVTUtDxiKSgdNoem8OVjzmdUKl9YA9GgevMU0PXQeojdrTBvAIS\n"
            + "MqhewOs3lcO0ImAkxRBnduZ8iBCNosCHgaMk0+D6nphNFgHcmsuAlm2sl7WeSOnWGlvjiA8DkAKZ\n"
            + "UazVVaD/OoNniZgFEUj7QyscKescReZSPxeOWeUJdOuPBPz+6gNXlbPN8HTZvMQzBlfk0aWogXnx"
            + "0t+t/wL/wnbUw+l6kCkVVHIOs2TBVkOqD+Ug78PfgPSNG6BMC0CFBZN4P8f7FRdIKPx3oWYkJwGf\n"
            + "M9tAhUAllLN8BrljKaWmkSeRtRCiwHCZ7g=X02dd";
    private static String tempJiraHome = null;
    private static String tempWebAppDir = null;

    private static final Logger log = Logger.getLogger(UtilsForTestSetup.class);

    /**
     * Register the default {@link TypeResolver} with OSWorkflow instead of the production
     * {@link com.atlassian.jira.workflow.DefaultOSWorkflowConfigurator.JiraTypeResolverDelegator} which is initialised by the
     * {@link com.atlassian.jira.workflow.DefaultOSWorkflowConfigurator}.
     */
    public static void configureOsWorkflow()
    {
        TypeResolver.setResolver(new TypeResolver());
    }

    public static void deleteAllEntities() throws GenericEntityException
    {
        loadDatabaseDriver();
        CoreTransactionUtil.setUseTransactions(true);
        CoreTransactionUtil.setIsolationLevel(Connection.TRANSACTION_READ_UNCOMMITTED);
        final boolean beganTransaction = CoreTransactionUtil.begin();
        try
        {
            final GenericDelegator delegator = CoreFactory.getGenericDelegator();
            final ModelReader reader = delegator.getModelReader();
            final Collection<String> entityNames = reader.getEntityNames();
            for (final String entityName : entityNames)
            {
                final ModelEntity modelEntity = delegator.getModelReader().getModelEntity(entityName);
                // Delete only normal (non-view) entities
                if (!(modelEntity instanceof ModelViewEntity))
                {
                    delegator.removeByAnd(entityName, Collections.EMPTY_MAP);
                }
            }
        }
        finally
        {
            CoreTransactionUtil.commit(beganTransaction);
        }
    }

    public static void loadDatabaseDriver()
    {
        // referencing org.hsqldb.jdbcDriver causes a static block to run that registers the hsql driver
        // with the java.sql.DriverManager. If the driver doesn't get registered then apache.commons.dbcp.DriverManagerConnectionFactory
        // can't create any connections when ofbiz's DBCPConnectionFactory wants one. This really only needs to be
        // done once per invocation.
        new jdbcDriver();
    }

    public static void mockTestCaseSetup(final ServiceOverrider serviceOverrider) throws GenericEntityException
    {
        log.debug("_______ Legacy Test setup invoked - Today JDBC, Tomorrow the World!");

        //clearing the world - some tests are not nice and are leaving the mess behind...
        final ComponentManager instance = ComponentManager.getInstance();
        if (instance.getState().isContainerInitialised())
        {
            instance.shutdown();
            JiraStartupChecklist.stop();
        }
        setupJiraHome();

        final long then = System.currentTimeMillis();
        loadDatabaseDriver();
        deleteAllEntities();
        CoreFactory.getGenericDelegator().clearAllCaches();
        CoreTransactionUtil.setUseTransactions(false);

        JiraTestUtil.setupMockSequenceUtil();

        final long dbTime = System.currentTimeMillis();

        ComponentAccessor.initialiseWorker(new ComponentAccessorWorker());
        //we initialise ComponentManager only for proper construction of MemoryIndexManager. then quickrefresh restarts it
        instance.bootstrapInitialise();
        ManagerFactory.addService(IndexLifecycleManager.class, new MockIndexLifecycleManager());
        ManagerFactory.addService(OsgiServiceProxyFactory.class, new MockOsgiServiceProxyFactory());
        final SearchExtractorRegistrationManager searchExtractorRegistrationManager = new DefaultSearchExtractorRegistrationManager();

        ManagerFactory.addService(IssueDocumentFactory.class, new DefaultIssueDocumentFactory(searchExtractorRegistrationManager));
        ManagerFactory.addService(CommentDocumentFactory.class, new DefaultCommentDocumentFactory(searchExtractorRegistrationManager));
        ManagerFactory.addService(ChangeHistoryDocumentFactory.class, new DefaultChangeHistoryDocumentFactory(searchExtractorRegistrationManager));
        ManagerFactory.addService(SearchExtractorRegistrationManager.class, searchExtractorRegistrationManager);
        ManagerFactory.quickRefresh(new MemoryIndexManager());
        serviceOverrider.override();

        // we use the manager for convenience here
        ComponentManager.getComponent(JiraLicenseManager.class).setLicense(V2_LICENSE);

        JiraAuthenticationContextImpl.clearRequestCache();
        ComponentAccessor.getJiraAuthenticationContext().clearLoggedInUser();

        serviceOverrider.override();
        setupInMemoryDatabase();

        final long picoTime = System.currentTimeMillis();
        log.debug("_______ Legacy Test Setup completed in " + (System.currentTimeMillis() - then) + " ms. db=" + (dbTime-then) + "ms, pico="+ (picoTime-dbTime) + "ms" );

    }

    private static void setupInMemoryDatabase()
    {
        DatabaseConfig inMemoryConfig = getDatabaseConfig();
        ComponentAccessor.getComponent(DatabaseConfigurationManager.class).setDatabaseConfiguration(inMemoryConfig);
    }

    public static DatabaseConfig getDatabaseConfig()
    {
        final JdbcDatasource datasource = new JdbcDatasource("jdbc:hsqldb:mem:jiradb", "org.hsqldb.jdbcDriver", "sa", "", 10, null, 4000L, 5000L);
        return new DatabaseConfig("mysql", "PUBLIC", datasource);
    }

    public static void setupJiraHome()
    {
        if (tempJiraHome == null)
        {
            tempJiraHome = TempDirectoryUtil.createTempDirectory(JiraHomePathLocator.Property.JIRA_HOME).getAbsolutePath();
        }
        System.getProperties().setProperty(JiraHomePathLocator.Property.JIRA_HOME,tempJiraHome);

        //
        // This will mock out our servlet context for code that needs it
        //
        if (tempWebAppDir == null)
        {
            tempWebAppDir = TempDirectoryUtil.createTempDirectory(JiraHomePathLocator.Property.JIRA_HOME).getAbsolutePath();
        }
        ServletContextProviderListener fakeContextProviderListener = new ServletContextProviderListener();
        fakeContextProviderListener.contextInitialized(new ServletContextEvent(new MockServletContext(tempWebAppDir)));

    }

    public static interface ServiceOverrider
    {
        /**
         * Override services by setting {@link ManagerFactory#addService(Class, Object)} for any objects you want to
         * override
         */
        public void override();
    }

    public static void mockTestCaseTearDown()
     {
        // remove all possible mock managers!
        ActionContext.getSession().remove(SessionKeys.SEARCH_PAGER);
        ActionContext.getSession().remove(SessionKeys.SEARCH_SORTER);
        ActionContext.getSession().remove(SessionKeys.SEARCH_REQUEST);
        CoreFactory.getGenericDelegator().clearAllCaches();
        ComponentManager.getInstance().stop();
        ComponentManager.getComponentInstanceOfType(EventPublisher.class).unregisterAll();
        JiraAuthenticationContextImpl.getRequestCache().clear();
        try
        {
            // clean-up any detritus we may have created
            deleteAllEntities();
        }
        catch (final GenericEntityException e)
        {
            throw new RuntimeException(e);
        }
        CoreTransactionUtil.setUseTransactions(false);
    }
}
