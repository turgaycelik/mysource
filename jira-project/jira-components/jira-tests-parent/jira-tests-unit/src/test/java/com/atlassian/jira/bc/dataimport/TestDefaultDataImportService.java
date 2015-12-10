package com.atlassian.jira.bc.dataimport;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.atlassian.activeobjects.spi.Backup;
import com.atlassian.core.util.ClassLoaderUtils;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.fugue.Option;
import com.atlassian.jira.JiraTestUtil;
import com.atlassian.jira.bc.license.JiraLicenseService;
import com.atlassian.jira.bc.license.JiraLicenseUpdaterService;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.JiraProperties;
import com.atlassian.jira.config.properties.JiraPropertiesImpl;
import com.atlassian.jira.config.properties.SystemPropertiesAccessor;
import com.atlassian.jira.config.util.AttachmentPathManager;
import com.atlassian.jira.config.util.IndexPathManager;
import com.atlassian.jira.config.util.JiraHome;
import com.atlassian.jira.event.JiraEventExecutorFactory;
import com.atlassian.jira.extension.JiraStartedEvent;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.license.LicenseStringFactory;
import com.atlassian.jira.mock.MockApplicationProperties;
import com.atlassian.jira.mock.i18n.MockI18nHelper;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.mock.servlet.MockServletContext;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.task.TaskManager;
import com.atlassian.jira.task.TaskProgressSink;
import com.atlassian.jira.task.context.Context;
import com.atlassian.jira.upgrade.BuildVersionRegistry;
import com.atlassian.jira.upgrade.ConsistencyChecker;
import com.atlassian.jira.upgrade.UpgradeManager;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.user.util.DirectorySynchroniserBarrier;
import com.atlassian.jira.util.BuildUtilsInfo;
import com.atlassian.jira.util.ComponentFactory;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.jira.util.index.IndexLifecycleManager;
import com.atlassian.jira.web.util.ExternalLinkUtil;
import com.atlassian.mail.queue.MailQueue;
import com.atlassian.plugin.event.PluginEventManager;
import com.atlassian.sal.api.message.Message;
import com.atlassian.sal.api.upgrade.PluginUpgradeManager;
import com.atlassian.scheduler.core.LifecycleAwareSchedulerService;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.ofbiz.core.entity.model.ModelEntity;
import org.ofbiz.core.entity.model.ModelReader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.notNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TestDefaultDataImportService
{
    @Rule
    public RuleChain mockAllTheThings = MockitoMocksInContainer.forTest(this);

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Mock public MailQueue mockMailQueue;
    @Mock public IndexPathManager indexPathManager;
    @Mock public AttachmentPathManager attachmentPathManager;
    @Mock public ExternalLinkUtil mockExternalLinkUtil;
    @Mock public JiraLicenseUpdaterService jiraLicenseService;
    @Mock public LicenseStringFactory licenseStringFactory;
    @Mock public BuildUtilsInfo buildUtilsInfo;
    @Mock public I18nHelper.BeanFactory beanFactory;
    @Mock public I18nHelper mockI18nHelper;
    @Mock public PermissionManager permissionManager;
    @Mock public JiraHome mockJiraHome;
    @Mock public TaskManager mockTaskManager;
    @Mock public PluginEventManager pluginEventManager;
    @Mock public ComponentFactory componentFactory;
    @Mock public DirectorySynchroniserBarrier directorySynchroniserBarrier;
    @Mock public BuildVersionRegistry mockBuildVersionRegistry;
    @Mock public JiraEventExecutorFactory executorFactory;
    @Mock public EventPublisher eventPublisher;
    @Mock public JiraLicenseService.ValidationResult validationResult;
    @Mock @AvailableInContainer public LifecycleAwareSchedulerService schedulerService;
    @Mock @AvailableInContainer public OfBizDelegator ofBizDelegator;
    @Mock @AvailableInContainer public ConsistencyChecker consistencyChecker;
    @Mock @AvailableInContainer public UpgradeManager upgradeManager;
    @Mock @AvailableInContainer public IndexLifecycleManager indexManager;
    @Mock @AvailableInContainer public Backup backup;
    @Mock @AvailableInContainer public ModelReader modelReader;

    private File jiraAttachmentsDir;
    private File jiraIndexesDir;
    private User currentUser = new MockUser("admin");
    private MockApplicationProperties applicationProperties;
    private DataImportProductionDependencies dependencies;
    private PluginUpgradeManager pluginUpgradeManager = new PluginUpgradeManager()
    {
        @Override
        public List<Message> upgrade()
        {
            return Collections.emptyList();
        }
    };
    private final JiraProperties jiraProperties = new JiraPropertiesImpl(new SystemPropertiesAccessor());

    @Before
    public void setUpTest() throws Exception
    {
        setupMockManagers();
        jiraAttachmentsDir = new File(tempFolder.getRoot(), "jira-attachments");
        if (!jiraAttachmentsDir.mkdir())
        {
            fail("Cannot create attachments directory " + jiraAttachmentsDir.getAbsolutePath());
        }
        jiraIndexesDir = new File(tempFolder.getRoot(), "jira-indexes");
        if (!jiraIndexesDir.mkdir())
        {
            fail("Cannot create indexes directory " + jiraIndexesDir.getAbsolutePath());
        }
    }

    private void setupMockManagers()
    {
        dependencies = new MockDataImportDependencies(consistencyChecker, pluginEventManager, pluginUpgradeManager);

        applicationProperties = new MockApplicationProperties();

        when(componentFactory.createObject(DirectorySynchroniserBarrier.class)).thenReturn(directorySynchroniserBarrier);
        applicationProperties.setString(APKeys.JIRA_SETUP, "setup");
    }

    private MockGenericValue getMockHappyPath() throws Exception
    {
        //This is called during the first parse of the XML file.  At this stage nothing should have been created yet!
        final MockGenericValue mockGv = new MockGenericValue("someentity");
        when(ofBizDelegator.makeValue(any(String.class))).thenReturn(mockGv);
        when(attachmentPathManager.getDefaultAttachmentPath()).thenReturn(jiraAttachmentsDir.getAbsolutePath());
        when(indexPathManager.getDefaultIndexRootPath()).thenReturn(jiraIndexesDir.getAbsolutePath());
        when(licenseStringFactory.create(any(String.class), any(String.class))).thenReturn("");

        //after the first parse check the build number.
        when(buildUtilsInfo.getCurrentBuildNumber()).thenReturn("99999999");
        when(buildUtilsInfo.getMinimumUpgradableBuildNumber()).thenReturn("0");

        //after the first parse we also verify the license is good.
        when(jiraLicenseService.validate(Mockito.any(I18nHelper.class), Mockito.any(String.class))).thenReturn(validationResult);
        when(validationResult.getLicenseVersion()).thenReturn(2);
        when(validationResult.getErrorCollection()).thenReturn(new SimpleErrorCollection());

        // this gets called during shutdownAndFlushAsyncServices.  After parse and before the import. This shuts down
        // the scheduler
        mockMailQueue.sendBuffer();
        when(mockTaskManager.shutdownAndWait(5)).thenReturn(true);

        //Expect AO to be cleared.
        backup.clear();

        //Once the import is running one of the first things to do is to clear out the old database values.
        when(ofBizDelegator.getModelReader()).thenReturn(modelReader);
        when(modelReader.getEntityNames()).thenReturn(CollectionBuilder.<String>list("Issue", "User"));
        when(modelReader.getModelEntity("Issue")).thenReturn(new ModelEntity());
        when(ofBizDelegator.removeByAnd("Issue", Collections.<String, Object>emptyMap())).thenReturn(10);
        when(modelReader.getModelEntity("User")).thenReturn(new ModelEntity());
        when(ofBizDelegator.removeByAnd("User", Collections.<String, Object>emptyMap())).thenReturn(5);

        //then we go through and create all our GVs (already mocked out during the first parse above)

        //once everything's been imported need to refresh the ofbiz sequencer and check for data consistency.
        ofBizDelegator.refreshSequencer();
        consistencyChecker.checkDataConsistency(new MockServletContext());

        //after the consistency check lets do the upgrade
        when(upgradeManager.doUpgradeIfNeededAndAllowed(null, false)).thenReturn(new UpgradeManager.Status(false, Collections.<String>emptyList()));

        //raise the DataImportFinishedEvent
        pluginEventManager.broadcast(new DataImportFinishedEvent());

        //now do a reindex
        indexManager.deactivate();
        when(indexManager.size()).thenReturn(5);
        when(indexManager.activate(notNull(Context.class))).thenReturn(1L);

        //raise the JiraStartedEvent
        pluginEventManager.broadcast(new JiraStartedEvent());

        return mockGv;
    }

    @Test
    public void testExecuteGoodVersion() throws Exception
    {
        when(directorySynchroniserBarrier.await(20, TimeUnit.SECONDS)).thenReturn(true);
        executorFactory.shutdown();

        when(beanFactory.getInstance(eq(currentUser))).thenReturn(new MockI18nHelper());

        //called during validation!
        when(permissionManager.hasPermission(eq(Permissions.SYSTEM_ADMIN), eq(currentUser))).thenReturn(true);
        final MockGenericValue mockGv = getMockHappyPath();

        final String filePath = getDataFilePath("jira-export-test.xml");
        final DataImportParams params = new DataImportParams.Builder(filePath).build();

        //Finally everything's mocked out.  Run the import!
        executeTest(params, true, false, DataImportService.ImportError.NONE);

        //create() should have been called on our GVs
        assertTrue(mockGv.isCreated());
        //the world should have been rebuilt!
        assertTrue(((MockDataImportDependencies) dependencies).globalRefreshCalled);
    }

    @Test
    public void fileThatDoesNotNeedEscapingSuceedsWhenEscapingIsUsed() throws Exception
    {
        when(directorySynchroniserBarrier.await(20, TimeUnit.SECONDS)).thenReturn(true);
        executorFactory.shutdown();

        when(beanFactory.getInstance(eq(currentUser))).thenReturn(new MockI18nHelper());

        //called during validation!
        when(permissionManager.hasPermission(eq(Permissions.SYSTEM_ADMIN), eq(currentUser))).thenReturn(true);
        final MockGenericValue mockGv = getMockHappyPath();

        final String filePath = getDataFilePath("jira-export-test.xml");
        final DataImportParams params = new DataImportParams.Builder(filePath).build();

        //Finally everything's mocked out.  Run the import!
        executeTest(params, true, true, DataImportService.ImportError.NONE);

        //create() should have been called on our GVs
        assertTrue(mockGv.isCreated());
        //the world should have been rebuilt!
        assertTrue(((MockDataImportDependencies) dependencies).globalRefreshCalled);
    }

    @Test
    public void fileThatNeedsEscapingSuceedsWhenEscapingIsUsed() throws Exception
    {
        when(directorySynchroniserBarrier.await(20, TimeUnit.SECONDS)).thenReturn(true);
        executorFactory.shutdown();

        when(beanFactory.getInstance(eq(currentUser))).thenReturn(new MockI18nHelper());

        //called during validation!
        when(permissionManager.hasPermission(eq(Permissions.SYSTEM_ADMIN), eq(currentUser))).thenReturn(true);
        final MockGenericValue mockGv = getMockHappyPath();


        final String filePath = getDataFilePath("jira-export-test-needs-escaping.xml");
        final DataImportParams params = new DataImportParams.Builder(filePath).build();

        //Finally everything's mocked out.  Run the import!
        executeTest(params, true, true, DataImportService.ImportError.NONE);

        //create() should have been called on our GVs
        assertTrue(mockGv.isCreated());
        //the world should have been rebuilt!
        assertTrue(((MockDataImportDependencies) dependencies).globalRefreshCalled);
    }

    @Test
    public void fileThatNeedsEscapingFailsWhenEscapingIsNotUsed() throws Exception
    {
        when(directorySynchroniserBarrier.await(20, TimeUnit.SECONDS)).thenReturn(true);
        executorFactory.shutdown();

        when(beanFactory.getInstance(eq(currentUser))).thenReturn(new MockI18nHelper());

        //called during validation!
        when(permissionManager.hasPermission(eq(Permissions.SYSTEM_ADMIN), eq(currentUser))).thenReturn(true);
        final MockGenericValue mockGv = getMockHappyPath();


        final String filePath = getDataFilePath("jira-export-test-needs-escaping.xml");
        final DataImportParams params = new DataImportParams.Builder(filePath).build();

        //Finally everything's mocked out.  Run the import!
        applicationProperties.setOption(APKeys.JIRA_IMPORT_CLEAN_XML, false);
        final DefaultDataImportService service = createImportService();

        final DataImportService.ImportValidationResult validationResult = service.validateImport(currentUser, params);
        final DataImportService.ImportResult importResult = service.doImport(currentUser, validationResult, TaskProgressSink.NULL_SINK);

        //import has run with failure
        assertEquals(false, importResult.isValid());
        assertEquals(DataImportService.ImportError.NONE, importResult.getImportError());

        //create() should not have been called on our GVs
        assertFalse(mockGv.isCreated());
        //the world should not have been rebuilt!
        assertFalse(((MockDataImportDependencies) dependencies).globalRefreshCalled);
    }

    @Test
    public void testExecuteQuickImport() throws Exception
    {
        when(directorySynchroniserBarrier.await(20, TimeUnit.SECONDS)).thenReturn(true);
        executorFactory.shutdown();

        when(beanFactory.getInstance(eq(currentUser))).thenReturn(new MockI18nHelper());
        //called during validation!
        when(permissionManager.hasPermission(eq(Permissions.SYSTEM_ADMIN), eq(currentUser))).thenReturn(true);

        //This is called during the first parse of the XML file.  At this stage nothing should have been created yet!
        final MockGenericValue mockGv = new MockGenericValue("someentity");
        when(ofBizDelegator.makeValue(any(String.class))).thenReturn(mockGv);
        when(attachmentPathManager.getDefaultAttachmentPath()).thenReturn(jiraAttachmentsDir.getAbsolutePath());
        when(indexPathManager.getDefaultIndexRootPath()).thenReturn(jiraIndexesDir.getAbsolutePath());
        when(licenseStringFactory.create(any(String.class), any(String.class))).thenReturn("");

        //after the first parse check the build number.
        when(buildUtilsInfo.getCurrentBuildNumber()).thenReturn("99999999");
        when(buildUtilsInfo.getMinimumUpgradableBuildNumber()).thenReturn("0");

        //after the first parse we also verify the license is good.
        when(jiraLicenseService.validate(any(I18nHelper.class), any(String.class))).thenReturn(validationResult);
        when(validationResult.getLicenseVersion()).thenReturn(2);
        when(validationResult.getErrorCollection()).thenReturn(new SimpleErrorCollection());

        // this gets called during shutdownAndFlushAsyncServices.  After parse and before the import. This shuts down
        // the scheduler
        mockMailQueue.sendBuffer();
        when(mockTaskManager.awaitUntilActiveTasksComplete(20)).thenReturn(true);

        //Expect AO to be cleared.
        backup.clear();

        //Once the import is running one of the first things to do is to clear out the old database values.
        when(ofBizDelegator.getModelReader()).thenReturn(modelReader);
        when(modelReader.getEntityNames()).thenReturn(CollectionBuilder.<String>list("Issue", "User"));
        when(modelReader.getModelEntity("Issue")).thenReturn(new ModelEntity());
        when(ofBizDelegator.removeByAnd("Issue", Collections.<String, Object>emptyMap())).thenReturn(10);
        when(modelReader.getModelEntity("User")).thenReturn(new ModelEntity());
        when(ofBizDelegator.removeByAnd("User", Collections.<String, Object>emptyMap())).thenReturn(5);

        //then we go through and create all our GVs (already mocked out during the first parse above)

        //once everything's been imported need to refresh the ofbiz sequencer and check for data consistency.
        ofBizDelegator.refreshSequencer();
        consistencyChecker.checkDataConsistency(new MockServletContext());

        //after the consistency check lets do the upgrade
        when(upgradeManager.doUpgradeIfNeededAndAllowed(null, false)).thenReturn(new UpgradeManager.Status(false, Collections.<String>emptyList()));

        //raise the DataImportFinishedEvent
        pluginEventManager.broadcast(new DataImportFinishedEvent());

        //now do a reindex
        indexManager.deactivate();
        when(indexManager.size()).thenReturn(5);
        when(indexManager.activate(notNull(Context.class))).thenReturn(1L);

        //raise the JiraStartedEvent
        pluginEventManager.broadcast(new JiraStartedEvent());

        final String filePath = getDataFilePath("jira-export-test.xml");
        final DataImportParams params = new DataImportParams.Builder(filePath).setQuickImport(true).build();

        //Finally everything's mocked out.  Run the import!
        executeTest(params, true, false, DataImportService.ImportError.NONE);

        //create() should have been called on our GVs
        assertTrue(mockGv.isCreated());
        //the world should have been rebuilt!
        assertTrue(((MockDataImportDependencies) dependencies).globalRefreshCalled);
    }

    @Test
    public void testExecuteImportWithUpdateTasksReindex() throws Exception
    {
        when(directorySynchroniserBarrier.await(20, TimeUnit.SECONDS)).thenReturn(true);
        executorFactory.shutdown();

        when(beanFactory.getInstance(eq(currentUser))).thenReturn(new MockI18nHelper());
        //called during validation!
        when(permissionManager.hasPermission(eq(Permissions.SYSTEM_ADMIN), eq(currentUser))).thenReturn(true);

        //This is called during the first parse of the XML file.  At this stage nothing should have been created yet!
        final MockGenericValue mockGv = new MockGenericValue("someentity");
        when(ofBizDelegator.makeValue(any(String.class))).thenReturn(mockGv);
        when(attachmentPathManager.getDefaultAttachmentPath()).thenReturn(jiraAttachmentsDir.getAbsolutePath());
        when(indexPathManager.getDefaultIndexRootPath()).thenReturn(jiraIndexesDir.getAbsolutePath());
        when(licenseStringFactory.create(any(String.class), any(String.class))).thenReturn("");

        //after the first parse check the build number.
        when(buildUtilsInfo.getCurrentBuildNumber()).thenReturn("99999999");
        when(buildUtilsInfo.getMinimumUpgradableBuildNumber()).thenReturn("0");

        //after the first parse we also verify the license is good.
        when(jiraLicenseService.validate(any(I18nHelper.class), any(String.class))).thenReturn(validationResult);
        when(validationResult.getLicenseVersion()).thenReturn(2);
        when(validationResult.getErrorCollection()).thenReturn(new SimpleErrorCollection());

        // this gets called during shutdownAndFlushAsyncServices.  After parse and before the import. This shuts down
        // the scheduler
        mockMailQueue.sendBuffer();
        when(mockTaskManager.awaitUntilActiveTasksComplete(20)).thenReturn(true);

        //Expect AO to be cleared.
        backup.clear();

        //Once the import is running one of the first things to do is to clear out the old database values.
        when(ofBizDelegator.getModelReader()).thenReturn(modelReader);
        when(modelReader.getEntityNames()).thenReturn(CollectionBuilder.<String>list("Issue", "User"));
        when(modelReader.getModelEntity("Issue")).thenReturn(new ModelEntity());
        when(ofBizDelegator.removeByAnd("Issue", Collections.<String, Object>emptyMap())).thenReturn(10);
        when(modelReader.getModelEntity("User")).thenReturn(new ModelEntity());
        when(ofBizDelegator.removeByAnd("User", Collections.<String, Object>emptyMap())).thenReturn(5);

        //then we go through and create all our GVs (already mocked out during the first parse above)

        //once everything's been imported need to refresh the ofbiz sequencer and check for data consistency.
        ofBizDelegator.refreshSequencer();
        consistencyChecker.checkDataConsistency(new MockServletContext());

        //after the consistency check lets do the upgrade
        when(upgradeManager.doUpgradeIfNeededAndAllowed(null, false)).thenReturn(new UpgradeManager.Status(true, Collections.<String>emptyList()));

        //raise the DataImportFinishedEvent
        pluginEventManager.broadcast(new DataImportFinishedEvent());

        //raise the JiraStartedEvent
        pluginEventManager.broadcast(new JiraStartedEvent());

        final String filePath = getDataFilePath("jira-export-test.xml");
        final DataImportParams params = new DataImportParams.Builder(filePath).setQuickImport(true).build();

        //Finally everything's mocked out.  Run the import!
        executeTest(params, true, false, DataImportService.ImportError.NONE);

        //create() should have been called on our GVs
        assertTrue(mockGv.isCreated());
        //the world should have been rebuilt!
        assertTrue(((MockDataImportDependencies) dependencies).globalRefreshCalled);
    }

    @Test
    public void testNoPermission() throws Exception
    {
        when(beanFactory.getInstance(eq(currentUser))).thenReturn(mockI18nHelper);
        when(permissionManager.hasPermission(eq(Permissions.SYSTEM_ADMIN), eq(currentUser))).thenReturn(false);
        when(mockI18nHelper.getText("admin.errors.import.permission")).thenReturn("No Permission to import data!");

        try
        {
            final String filePath = getDataFilePath("jira-export-test.xml");
            final DataImportParams params = new DataImportParams.Builder(filePath).build();
            executeTest(params, false, false, DataImportService.ImportError.NONE);
            fail("Calling doImport with invalid validation result should have thrown an exception!");
        }
        catch (IllegalStateException e)
        {
            //yay
        }
    }

    @Test
    public void testNoFileProvided() throws Exception
    {
        when(beanFactory.getInstance(eq(currentUser))).thenReturn(mockI18nHelper);
        when(permissionManager.hasPermission(eq(Permissions.SYSTEM_ADMIN), eq(currentUser))).thenReturn(true);
        when(mockI18nHelper.getText("admin.errors.must.enter.xml2")).thenReturn("Must provide file");

        try
        {
            final DataImportParams params = new DataImportParams.Builder("").build();
            executeTest(params, false, false, DataImportService.ImportError.NONE);
            fail("Calling doImport with invalid validation result should have thrown an exception!");
        }
        catch (IllegalStateException e)
        {
            //yay
        }
    }

    @Test
    public void testSetupImportWhenAlreadySetup() throws Exception
    {
        when(beanFactory.getInstance(eq(currentUser))).thenReturn(mockI18nHelper);
        applicationProperties.setString(APKeys.JIRA_SETUP, "true");
        when(mockI18nHelper.getText("admin.errors.import.already.setup")).thenReturn("Already setup. Should do xml restore");
        when(mockI18nHelper.getText("admin.errors.must.enter.xml2")).thenReturn("Must provide file");

        try
        {
            final DataImportParams params = new DataImportParams.Builder("").setupImport().build();
            executeTest(params, false, false, DataImportService.ImportError.NONE);
            fail("Calling doImport with invalid validation result should have thrown an exception!");
        }
        catch (IllegalStateException e)
        {
            //yay
        }
    }

    @Test
    public void testFileNonExistent() throws Exception
    {
        when(beanFactory.getInstance(eq(currentUser))).thenReturn(mockI18nHelper);
        when(permissionManager.hasPermission(eq(Permissions.SYSTEM_ADMIN), eq(currentUser))).thenReturn(true);
        when(mockJiraHome.getImportDirectory()).thenReturn(new File("somewhere"));
        when(mockI18nHelper.getText(eq("admin.errors.could.not.find.file"), any(String.class))).thenReturn("File does not exist.");

        try
        {
            final DataImportParams params = new DataImportParams.Builder("idontexisthopefully.txt").build();
            executeTest(params, false, false, DataImportService.ImportError.NONE);
            fail("Calling doImport with invalid validation result should have thrown an exception!");
        }
        catch (IllegalStateException e)
        {
            //yay
        }
    }

    @Test (expected = IllegalStateException.class)
    public void testUnsafeFileNonExistent() throws Exception
    {
        when(beanFactory.getInstance(eq(currentUser))).thenReturn(mockI18nHelper);
        when(permissionManager.hasPermission(eq(Permissions.SYSTEM_ADMIN), eq(currentUser))).thenReturn(true);
        when(mockJiraHome.getImportDirectory()).thenReturn(new File("somewhere"));
        when(mockI18nHelper.getText(Mockito.eq("admin.errors.could.not.find.file"), any(String.class))).thenReturn("File does not exist.");

        final DataImportParams params = new DataImportParams.Builder(null)
                .setUnsafeJiraBackup(new File("idontexist.really.really.not")).build();
        executeTest(params, false, false, DataImportService.ImportError.NONE);
    }

    @Test (expected = IllegalStateException.class)
    public void testUnsafeAOFileNonExistent() throws Exception
    {
        when(beanFactory.getInstance(eq(currentUser))).thenReturn(mockI18nHelper);
        when(permissionManager.hasPermission(eq(Permissions.SYSTEM_ADMIN), eq(currentUser))).thenReturn(true);
        when(mockI18nHelper.getText(Mockito.eq("admin.errors.could.not.find.file"), any(String.class))).thenReturn("File does not exist.");

        final File file = tempFolder.newFile("testUnsafeAOFileNonExistent.txt");

        final DataImportParams params = new DataImportParams.Builder(null)
                .setUnsafeJiraBackup(file)
                .setUnsafeAOBackup(new File("I.really.really.don't.exist.and.if.i.did.it.would.be.very.unlucky"))
                .build();
        executeTest(params, false, false, DataImportService.ImportError.NONE);
    }

    @Test
    public void testGetJiraBackupFilesWithFileNameAndNoAOFile() throws IOException
    {
        String f = getDataFilePath("jira-export-test.xml");

        final DefaultDataImportService defaultDataImportService = createImportService();
        final DataImportParams params = new DataImportParams.Builder("jira-export-test.xml").build();
        final File backupFile = defaultDataImportService.getJiraBackupFile(params);
        final File aoBackupFile = defaultDataImportService.getAOBackupFile(params);

        final File expectedFile = new File(f).getCanonicalFile();
        assertEquals(expectedFile, backupFile.getCanonicalFile());
        assertEquals(expectedFile, aoBackupFile.getCanonicalFile());
    }

    @Test
    public void testNoAO() throws Exception
    {
        when(beanFactory.getInstance(eq(currentUser))).thenReturn(mockI18nHelper);
        when(permissionManager.hasPermission(eq(Permissions.SYSTEM_ADMIN), eq(currentUser))).thenReturn(true);
        when(mockJiraHome.getImportDirectory()).thenReturn(new File("somewhere"));
        when(mockI18nHelper.getText("data.import.error.no.ao")).thenReturn("Data Import.");

        backup = null;

        try
        {
            final String filePath = getDataFilePath("jira-export-test.xml");
            final DataImportParams params = new DataImportParams.Builder(filePath).build();
            executeTest(params, false, false, DataImportService.ImportError.NONE);
            fail("Calling doImport with invalid validation result should have thrown an exception!");
        }
        catch (IllegalStateException e)
        {
            //yay
        }
    }

    @Test
    public void testInvalidLicenseProvided() throws Exception
    {
        when(beanFactory.getInstance(eq(currentUser))).thenReturn(mockI18nHelper);
        when(permissionManager.hasPermission(eq(Permissions.SYSTEM_ADMIN), eq(currentUser))).thenReturn(true);
        when(jiraLicenseService.validate(mockI18nHelper, "thisisnotavalidlicensestring")).thenReturn(validationResult);
        final ErrorCollection errors = new SimpleErrorCollection();
        errors.addErrorMessage("Not a valid license");
        when(validationResult.getErrorCollection()).thenReturn(errors);

        try
        {
            final String filePath = getDataFilePath("jira-export-test.xml");
            final DataImportParams params = new DataImportParams.Builder(filePath).setLicenseString("thisisnotavalidlicensestring").build();
            executeTest(params, false, false, DataImportService.ImportError.NONE);
            fail("Calling doImport with invalid validation result should have thrown an exception!");
        }
        catch (IllegalStateException e)
        {
            //yay
        }

    }

    @Test
    public void testVersion1License() throws Exception
    {
        when(beanFactory.getInstance(eq(currentUser))).thenReturn(mockI18nHelper);
        when(mockI18nHelper.getText(eq("data.import.parse.xml"))).thenReturn("Parsing XML");
        when(mockI18nHelper.getText(eq("data.import.parse.progress"), any(String.class), any(String.class))).thenReturn("Parsing progress");

        //called during validation!
        when(permissionManager.hasPermission(Mockito.eq(Permissions.SYSTEM_ADMIN), eq(currentUser))).thenReturn(true);

        JiraLicenseService.ValidationResult result = mock(JiraLicenseService.ValidationResult.class);

        //after the first parse we also verify the license is good.
        when(jiraLicenseService.validate(any(I18nHelper.class), any(String.class))).thenReturn(validationResult);
        when(validationResult.getLicenseVersion()).thenReturn(2);
        when(validationResult.getErrorCollection()).thenReturn(new SimpleErrorCollection());

        when(result.getLicenseVersion()).thenReturn(1);
        when(result.getErrorCollection()).thenReturn(new SimpleErrorCollection());
        when(jiraLicenseService.validate(any(I18nHelper.class), eq("version1license"))).thenReturn(result);

        //This is called during the first parse of the XML file.  At this stage nothing should have been created yet!
        final MockGenericValue mockGv = new MockGenericValue("someentity");
        when(ofBizDelegator.makeValue(any(String.class))).thenReturn(mockGv);
        when(attachmentPathManager.getDefaultAttachmentPath()).thenReturn(jiraAttachmentsDir.getAbsolutePath());
        when(indexPathManager.getDefaultIndexRootPath()).thenReturn(jiraIndexesDir.getAbsolutePath());
        when(licenseStringFactory.create(any(String.class), any(String.class))).thenReturn("");

        //after the first parse check the build number.
        when(buildUtilsInfo.getCurrentBuildNumber()).thenReturn("99999999");
        when(buildUtilsInfo.getMinimumUpgradableBuildNumber()).thenReturn("0");

        final String filePath = getDataFilePath("jira-export-test.xml");
        final DataImportParams params = new DataImportParams.Builder(filePath).setLicenseString("version1license").build();
        executeTest(params, false, false, DataImportService.ImportError.V1_LICENSE_EXCEPTION);
    }


    @Test
    public void jiraShouldNotAllowDowngradeFromNonDowngradableVersion() throws Exception
    {
        when(beanFactory.getInstance(eq(currentUser))).thenReturn(mockI18nHelper);
        when(mockI18nHelper.getText("data.import.parse.xml")).thenReturn("Parsing XML");
        when(mockI18nHelper.getText(Mockito.eq("data.import.parse.progress"), any(String.class), any(String.class))).thenReturn("Parsing progress");

        //called during validation!
        when(permissionManager.hasPermission(eq(Permissions.SYSTEM_ADMIN), eq(currentUser))).thenReturn(true);

        //This is called during the first parse of the XML file.  At this stage nothing should have been created yet!
        final MockGenericValue mockGv = new MockGenericValue("someentity");
        when(ofBizDelegator.makeValue(any(String.class))).thenReturn(mockGv);
        when(attachmentPathManager.getDefaultAttachmentPath()).thenReturn(jiraAttachmentsDir.getAbsolutePath());
        when(indexPathManager.getDefaultIndexRootPath()).thenReturn(jiraIndexesDir.getAbsolutePath());
        when(licenseStringFactory.create(any(String.class), any(String.class))).thenReturn("");

        //after the first parse check the build number.
        when(buildUtilsInfo.getCurrentBuildNumber()).thenReturn("1");
        when(buildUtilsInfo.getMinimumUpgradableBuildNumber()).thenReturn("1");

        //after the first parse we also verify the license is good.
        when(jiraLicenseService.validate(any(I18nHelper.class), any(String.class))).thenReturn(validationResult);
        when(validationResult.getLicenseVersion()).thenReturn(2);
        when(validationResult.getErrorCollection()).thenReturn(new SimpleErrorCollection());
        when(mockI18nHelper.getText("data.import.error.xml.newer.1", null, null)).thenReturn("Data is from a newer version of JIRA");
        when(mockI18nHelper.getText("data.import.error.xml.newer.2", null)).thenReturn("Contact Support");

        final String filePath = getDataFilePath("jira-export-test-too-new.xml");
        final DataImportParams params = new DataImportParams.Builder(filePath).build();

        executeTest(params, false, false, DataImportService.ImportError.NONE);
    }

    @Test
    public void jiraShouldAllowDowngradeFromDowngradableBuildNumber() throws Exception
    {
        when(beanFactory.getInstance(eq(currentUser))).thenReturn(mockI18nHelper);
        when(mockI18nHelper.getText("data.import.parse.xml")).thenReturn("Parsing XML");
        when(mockI18nHelper.getText(Mockito.eq("data.import.parse.progress"), any(String.class), any(String.class))).thenReturn("Parsing progress");

        //called during validation!
        when(permissionManager.hasPermission(eq(Permissions.SYSTEM_ADMIN), eq(currentUser))).thenReturn(true);

        //This is called during the first parse of the XML file.  At this stage nothing should have been created yet!
        final MockGenericValue mockGv = new MockGenericValue("someentity");
        when(ofBizDelegator.makeValue(any(String.class))).thenReturn(mockGv);
        when(attachmentPathManager.getDefaultAttachmentPath()).thenReturn(jiraAttachmentsDir.getAbsolutePath());
        when(indexPathManager.getDefaultIndexRootPath()).thenReturn(jiraIndexesDir.getAbsolutePath());
        when(licenseStringFactory.create(any(String.class), any(String.class))).thenReturn("");

        //after the first parse check the build number.
        when(buildUtilsInfo.getCurrentBuildNumber()).thenReturn("1");
        when(buildUtilsInfo.getMinimumUpgradableBuildNumber()).thenReturn("1");

        //after the first parse we also verify the license is good.
        when(jiraLicenseService.validate(any(I18nHelper.class), any(String.class))).thenReturn(validationResult);
        when(validationResult.getLicenseVersion()).thenReturn(2);
        when(validationResult.getErrorCollection()).thenReturn(new SimpleErrorCollection());
        when(mockI18nHelper.getText("data.import.error.xml.newer.1", null, null)).thenReturn("Data is from a newer version of JIRA");
        when(mockI18nHelper.getText("data.import.error.xml.newer.2", null)).thenReturn("Contact Support");

        final String filePath = getDataFilePath("jira-export-test-too-new.xml");
        final DataImportParams params = new DataImportParams.Builder(filePath).build();

        executeTest(params, false, false, DataImportService.ImportError.NONE);
    }

    @Test
    public void testExecuteBuildNumberTooOldInXml() throws Exception
    {
        when(beanFactory.getInstance(eq(currentUser))).thenReturn(mockI18nHelper);
        when(mockI18nHelper.getText("data.import.parse.xml")).thenReturn("Parsing XML");
        when(mockI18nHelper.getText(Mockito.eq("data.import.parse.progress"), any(String.class), any(String.class))).thenReturn("Parsing progress");

        //called during validation!
        when(permissionManager.hasPermission(eq(Permissions.SYSTEM_ADMIN), eq(currentUser))).thenReturn(true);

        //This is called during the first parse of the XML file.  At this stage nothing should have been created yet!
        final MockGenericValue mockGv = new MockGenericValue("someentity");
        when(ofBizDelegator.makeValue(any(String.class))).thenReturn(mockGv);
        when(attachmentPathManager.getDefaultAttachmentPath()).thenReturn(jiraAttachmentsDir.getAbsolutePath());
        when(indexPathManager.getDefaultIndexRootPath()).thenReturn(jiraIndexesDir.getAbsolutePath());
        when(licenseStringFactory.create(any(String.class), any(String.class))).thenReturn("");

        //after the first parse check the build number.
        when(buildUtilsInfo.getCurrentBuildNumber()).thenReturn("400");
        when(buildUtilsInfo.getMinimumUpgradableBuildNumber()).thenReturn("18");

        //after the first parse we also verify the license is good.
        when(jiraLicenseService.validate(any(I18nHelper.class), any(String.class))).thenReturn(validationResult);
        when(validationResult.getLicenseVersion()).thenReturn(2);
        when(validationResult.getErrorCollection()).thenReturn(new SimpleErrorCollection());

        when(mockExternalLinkUtil.getProperty("external.link.jira.confluence.upgrade.guide.for.old.versions")).thenReturn(
                "http://www.atlassian.com");

        when(mockI18nHelper.getText("data.import.error.xml.too.old", "http://www.atlassian.com")).thenReturn("Data is too old visit http://www.atlassian.com/");

        final String filePath = getDataFilePath("jira-export-test-too-old.xml");
        final DataImportParams params = new DataImportParams.Builder(filePath).build();
        executeTest(params, false, false, DataImportService.ImportError.NONE);
    }

    @Test
    public void testExecuteBuildNumberMissing() throws Exception
    {
        when(directorySynchroniserBarrier.await(20, TimeUnit.SECONDS)).thenReturn(true);
        when(beanFactory.getInstance(eq(currentUser))).thenReturn(new MockI18nHelper());
        executorFactory.shutdown();

        //called during validation!
        when(permissionManager.hasPermission(eq(Permissions.SYSTEM_ADMIN), eq(currentUser))).thenReturn(true);

        //This is called during the first parse of the XML file.  At this stage nothing should have been created yet!
        final MockGenericValue mockGv = new MockGenericValue("someentity");
        when(ofBizDelegator.makeValue(any(String.class))).thenReturn(mockGv);
        when(attachmentPathManager.getDefaultAttachmentPath()).thenReturn(jiraAttachmentsDir.getAbsolutePath());
        when(indexPathManager.getDefaultIndexRootPath()).thenReturn(jiraIndexesDir.getAbsolutePath());
        when(licenseStringFactory.create(any(String.class), any(String.class))).thenReturn("");

        //after the first parse check the build number.
        when(buildUtilsInfo.getCurrentBuildNumber()).thenReturn("1");
        when(buildUtilsInfo.getMinimumUpgradableBuildNumber()).thenReturn("0");

        //after the first parse we also verify the license is good.
        when(jiraLicenseService.validate(any(I18nHelper.class), any(String.class))).thenReturn(validationResult);
        when(validationResult.getLicenseVersion()).thenReturn(2);
        when(validationResult.getErrorCollection()).thenReturn(new SimpleErrorCollection());

        // this gets called during shutdownAndFlushAsyncServices.  After parse and before the import. This shuts down
        // the scheduler
        mockMailQueue.sendBuffer();
        when(mockTaskManager.shutdownAndWait(5)).thenReturn(true);

        //Expect AO to be cleared.
        backup.clear();

        //Once the import is running one of the first things to do is to clear out the old database values.
        when(ofBizDelegator.getModelReader()).thenReturn(modelReader);
        when(modelReader.getEntityNames()).thenReturn(CollectionBuilder.<String>list("Issue", "User"));
        when(modelReader.getModelEntity("Issue")).thenReturn(new ModelEntity());
        when(ofBizDelegator.removeByAnd("Issue", Collections.<String, Object>emptyMap())).thenReturn(10);
        when(modelReader.getModelEntity("User")).thenReturn(new ModelEntity());
        when(ofBizDelegator.removeByAnd("User", Collections.<String, Object>emptyMap())).thenReturn(5);

        //then we go through and create all our GVs (already mocked out during the first parse above)

        //once everything's been imported need to refresh the ofbiz sequencer and check for data consistency.
        ofBizDelegator.refreshSequencer();
        consistencyChecker.checkDataConsistency(new MockServletContext());

        //after the consistency check lets do the upgrade
        when(upgradeManager.doUpgradeIfNeededAndAllowed(null, false)).thenReturn(new UpgradeManager.Status(false, Collections.<String>emptyList()));

        //raise the DataImportFinishedEvent
        pluginEventManager.broadcast(new DataImportFinishedEvent());

        //now do a reindex
        indexManager.deactivate();
        when(indexManager.size()).thenReturn(5);
        when(indexManager.activate(notNull(Context.class))).thenReturn(1L);

        //raise the JiraStartedEvent
        pluginEventManager.broadcast(new JiraStartedEvent());

        final String filePath = getDataFilePath("jira-export-test-no-build-number.xml");
        final DataImportParams params = new DataImportParams.Builder(filePath).build();

        //Finally everything's mocked out.  Run the import!
        executeTest(params, true, false, DataImportService.ImportError.NONE);

        //create() should have been called on our GVs
        assertTrue(mockGv.isCreated());
        //the world should have been rebuilt!
        assertTrue(((MockDataImportDependencies) dependencies).globalRefreshCalled);
    }

    @Test
    public void testImportBackupWithDate() throws Exception
    {
        applicationProperties.setOption(APKeys.JIRA_IMPORT_CLEAN_XML, false);
        final MockGenericValue mockGv = getMockHappyPath();
        when(beanFactory.getInstance(eq(currentUser))).thenReturn(new MockI18nHelper());
        when(permissionManager.hasPermission(eq(Permissions.SYSTEM_ADMIN), eq(currentUser))).thenReturn(true);

        final String filePath = getDataFilePath("jira-export-test-with-time.xml");
        final DataImportParams params = new DataImportParams.Builder(filePath).build();
        final DefaultDataImportService importService = createImportService();
        final DataImportService.ImportValidationResult validationResult = importService.validateImport(currentUser, params);

        importService.doImport(currentUser, validationResult, TaskProgressSink.NULL_SINK);

        verify(eventPublisher).publish(argThat(new ImportEventMatcher(1l, ImportStartedEvent.class)));

        verify(eventPublisher).publish(argThat(new ImportEventMatcher(1l, ImportCompletedEvent.class)));

        assertTrue(mockGv.isCreated());
    }

    private void executeTest(final DataImportParams params, final boolean success, final boolean escapeIllegalCharacters, DataImportService.ImportError specificError)
            throws Exception
    {
        applicationProperties.setOption(APKeys.JIRA_IMPORT_CLEAN_XML, escapeIllegalCharacters);

        final DefaultDataImportService service = createImportService();

        final DataImportService.ImportValidationResult validationResult = service.validateImport(currentUser, params);
        final DataImportService.ImportResult importResult = service.doImport(currentUser, validationResult, TaskProgressSink.NULL_SINK);

        assertEquals(success, importResult.isValid());
        assertEquals(specificError, importResult.getImportError());
    }

    private DefaultDataImportService createImportService()
    {
        return new DefaultDataImportService(dependencies, permissionManager,
                mockJiraHome, jiraLicenseService, beanFactory, ofBizDelegator, licenseStringFactory,
                indexPathManager, attachmentPathManager, mockExternalLinkUtil, applicationProperties, buildUtilsInfo,
                mockTaskManager, mockMailQueue, componentFactory, mockBuildVersionRegistry, executorFactory, jiraProperties)
        {
            @Override
            Backup getAOBackup()
            {
                return backup;
            }

            @Override
            protected EventPublisher getEventPublisher()
            {
                return eventPublisher;
            }
        };
    }

    private String getDataFilePath(String dataFileName)
    {
        // let's do some funky URL stuff to find the real path of this file
        final URL url = ClassLoaderUtils.getResource(JiraTestUtil.TESTS_BASE + "/action/admin/" + dataFileName, TestDefaultDataImportService.class);
        final File f = new File(url.getPath());

        when(mockJiraHome.getImportDirectory()).thenReturn(new File(f.getParent()));
        return f.getAbsolutePath();
    }

    static class MockDataImportDependencies extends DataImportProductionDependencies
    {
        private boolean globalRefreshCalled = false;
        private final ConsistencyChecker consistencyChecker;
        private final PluginEventManager pluginEventManager;
        private final PluginUpgradeManager pluginUpgradeManager;

        MockDataImportDependencies(ConsistencyChecker consistencyChecker, PluginEventManager pluginEventManager,
                PluginUpgradeManager pluginUpgradeManager)
        {
            this.consistencyChecker = consistencyChecker;
            this.pluginEventManager = pluginEventManager;
            this.pluginUpgradeManager = pluginUpgradeManager;
        }

        @Override
        void globalRefresh(boolean quickImport)
        {
            globalRefreshCalled = true;
        }

        @Override
        ConsistencyChecker getConsistencyChecker()
        {
            return consistencyChecker;
        }

        @Override
        PluginEventManager getPluginEventManager()
        {
            return pluginEventManager;
        }

        @Override
        PluginUpgradeManager getPluginUpgradeManager()
        {
            return pluginUpgradeManager;
        }
    }

    private static class ImportEventMatcher extends TypeSafeMatcher<DataImportEvent>
    {
        private final Long expectedXmlExportTime;

        ImportEventMatcher(final Long expectedXmlExportTime, Class<? extends DataImportEvent> clazz)
        {
            super(clazz);
            this.expectedXmlExportTime = expectedXmlExportTime;
        }

        @Override
        protected boolean matchesSafely(final DataImportEvent o)
        {
            final Option<Long> exportXmlTime = o.getXmlExportTime();
            return exportXmlTime.isDefined() && exportXmlTime.get().equals(expectedXmlExportTime);
        }

        @Override
        public void describeTo(final Description description)
        {
            description.appendText("Exported xml file doesn't contain date attribute");
        }
    }
}
