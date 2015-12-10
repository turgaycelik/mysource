package com.atlassian.jira.upgrade;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.SortedMap;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.bc.license.JiraLicenseService;
import com.atlassian.jira.bean.export.AutoExport;
import com.atlassian.jira.bean.export.IllegalXMLCharactersException;
import com.atlassian.jira.config.FeatureManager;
import com.atlassian.jira.config.ReindexMessageManager;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.index.IndexException;
import com.atlassian.jira.license.LicenseDetails;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.mock.upgrade.MockUpgradeTask;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.task.context.Context;
import com.atlassian.jira.util.BuildUtilsInfo;
import com.atlassian.jira.util.DowngradeUtilsImpl;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.util.index.IndexLifecycleManager;
import com.atlassian.jira.web.util.OutlookDate;
import com.atlassian.jira.web.util.OutlookDateManager;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.ofbiz.core.entity.EntityCondition;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import static com.atlassian.jira.config.properties.APKeys.JIRA_PATCHED_VERSION;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyCollectionOf;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TestUpgradeManagerImpl
{
    private static final String VERSION = "4.0";

    private static final Collection<MockUpgradeTask> ALL_UPGRADES = ImmutableList.of(
            new MockUpgradeTask("1.0", "short desc", false),
            new MockUpgradeTask("1.0.1", "short desc2", true),
            new MockUpgradeTask("1.2", "short desc3", false),
            new MockUpgradeTask("1.2.3", "short desc4", false),
            new MockUpgradeTask("1.3", "short desc5", false),
            new MockUpgradeTask("27", "short desc6", false),
            new MockUpgradeTask("0.9", "short desc7", false),
            new MockUpgradeTask("1.1", "short desc8", false)
    );

    @Mock private ApplicationProperties mockApplicationProperties;
    @Mock private BuildUtilsInfo mockBuildUtilsInfo;
    @Mock private BuildVersionRegistry mockBuildVersionRegistry;
    @Mock private DowngradeUtilsImpl mockDowngradeUtilsImpl;
    @Mock private EventPublisher mockEventPublisher;
    @Mock private FeatureManager mockFeatureManager;
    @Mock private I18nHelper mockI18nHelper;
    @Mock private I18nHelper.BeanFactory mockI18HelperFactory;
    @Mock private IndexLifecycleManager mockIndexLifecycleManager;
    @Mock private JiraLicenseService mockJiraLicenseService;
    @Mock private LicenseDetails mockLicenseDetails;
    @Mock private OfBizDelegator mockDelegator;
    @SuppressWarnings("deprecation")
    @Mock private OutlookDateManager mockDateManager;
    @Mock private ReindexMessageManager mockReindexMessageManager;

    @Before
    public void setUp() throws Exception
    {
        MockitoAnnotations.initMocks(this);
        when(mockJiraLicenseService.getLicense()).thenReturn(mockLicenseDetails);
        when(mockLicenseDetails.isLicenseSet()).thenReturn(false);  // we bypass license checks if it is not set.
        when(mockBuildUtilsInfo.getCurrentBuildNumber()).thenReturn("99999");
    }
    
    private UpgradeManagerImpl createForTest()
    {
        return new UpgradeManagerImpl(mockJiraLicenseService, mockBuildUtilsInfo, mockI18HelperFactory,
                mockApplicationProperties, mockBuildVersionRegistry, mockDelegator, mockEventPublisher,
                mockIndexLifecycleManager, mockDateManager, mockFeatureManager, ALL_UPGRADES, ALL_UPGRADES, mockDowngradeUtilsImpl,
                mockReindexMessageManager);
    }

    private UpgradeManagerImpl createForTest(Iterable<UpgradeTask> upgradeTasks, Iterable<UpgradeTask> setupUpgradeTasks)
    {
        return new UpgradeManagerImpl(mockJiraLicenseService, mockBuildUtilsInfo, mockI18HelperFactory,
                mockApplicationProperties, mockBuildVersionRegistry, mockDelegator, mockEventPublisher,
                mockIndexLifecycleManager, mockDateManager, mockFeatureManager, upgradeTasks, setupUpgradeTasks,
                mockDowngradeUtilsImpl, mockReindexMessageManager);
    }

    private UpgradeManagerImpl createForTest(final AutoExport mockAutoExport)
    {
        return new UpgradeManagerImpl(mockJiraLicenseService, mockBuildUtilsInfo, mockI18HelperFactory,
                mockApplicationProperties, mockBuildVersionRegistry, mockDelegator, mockEventPublisher,
                mockIndexLifecycleManager, mockDateManager, mockFeatureManager, ALL_UPGRADES, ALL_UPGRADES, mockDowngradeUtilsImpl,
                mockReindexMessageManager)
        {
            @Override
            protected AutoExport getAutoExport(String defaultBackupPath)
            {
                return mockAutoExport;
            }
        };
    }

    private UpgradeManagerImpl createForTest(final UpgradeHistoryItem upgradeHistoryItem)
    {
        return new UpgradeManagerImpl(mockJiraLicenseService, mockBuildUtilsInfo, mockI18HelperFactory,
                mockApplicationProperties, mockBuildVersionRegistry, mockDelegator, mockEventPublisher, mockIndexLifecycleManager,
                mockDateManager, mockFeatureManager, ALL_UPGRADES, ALL_UPGRADES, mockDowngradeUtilsImpl,
                mockReindexMessageManager)
        {
            @Override
            UpgradeHistoryItem getUpgradeHistoryItemFromTasks() throws GenericEntityException
            {
                return upgradeHistoryItem;
            }
        };
    }

    @Test
    public void testDoUpdate() throws IllegalXMLCharactersException, IndexException
    {
        when(mockDelegator.findAll("UpgradeHistory")).thenReturn(Collections.<GenericValue>emptyList());

        when(mockApplicationProperties.getString(APKeys.JIRA_SETUP)).thenReturn("true");
        when(mockApplicationProperties.getString(JIRA_PATCHED_VERSION)).thenReturn("100");
        when(mockApplicationProperties.getOption(APKeys.JIRA_AUTO_EXPORT)).thenReturn(true);
        when(mockBuildUtilsInfo.getVersion()).thenReturn(VERSION);
        when(mockBuildUtilsInfo.getApplicationBuildNumber()).thenReturn(400);
        when(mockBuildUtilsInfo.getDatabaseBuildNumber()).thenReturn(100);

        final UpgradeManager tested = createForTest();
        final UpgradeManager.Status status = tested.doUpgradeIfNeededAndAllowed(null, false);

        // we should have no errors
        assertTrue("Unexpected errors: " + status.getErrors(), status.succesful());
    }

    @Test
    public void testDoUpgradeIfNeededAndAllowed_BadLicense_TooOldForBuild() throws IllegalXMLCharactersException, IndexException
    {
        when(mockApplicationProperties.getString(APKeys.JIRA_SETUP)).thenReturn("true");
        when(mockApplicationProperties.getString(JIRA_PATCHED_VERSION)).thenReturn("100");

        final Date theDate = new Date();


        when(mockBuildUtilsInfo.getCurrentBuildDate()).thenReturn(theDate);
        when(mockBuildUtilsInfo.getVersion()).thenReturn("v99");

        when(mockLicenseDetails.isLicenseSet()).thenReturn(true);
        when(mockLicenseDetails.isMaintenanceValidForBuildDate(theDate)).thenReturn(false);
        when(mockLicenseDetails.hasLicenseTooOldForBuildConfirmationBeenDone()).thenReturn(false);
        when(mockLicenseDetails.getMaintenanceEndString(any(OutlookDate.class))).thenReturn("today");

        final UpgradeManager tested = createForTest();
        final UpgradeManager.Status status = tested.doUpgradeIfNeededAndAllowed(null, false);

        // we should have one error
        assertFalse(status.succesful());
        assertEquals(1, status.getErrors().size());
    }

    @Test
    public void testDoUpgradeIfNeededAndAllowed_BadLicense_ValidationFailed()
            throws IllegalXMLCharactersException, IndexException
    {
        when(mockApplicationProperties.getString(APKeys.JIRA_SETUP)).thenReturn("true");
        when(mockApplicationProperties.getString(JIRA_PATCHED_VERSION)).thenReturn("100");

        final Date theDate = new Date();

        SimpleErrorCollection errorCollection = new SimpleErrorCollection();
        errorCollection.addErrorMessage("shite happens");
        JiraLicenseService.ValidationResult validationResult = mock(JiraLicenseService.ValidationResult.class);

        when(mockI18HelperFactory.getInstance(any(Locale.class))).thenReturn(null);

        when(mockBuildUtilsInfo.getCurrentBuildDate()).thenReturn(theDate);
        when(mockBuildUtilsInfo.getVersion()).thenReturn(VERSION);

        when(mockLicenseDetails.isLicenseSet()).thenReturn(true);
        when(mockLicenseDetails.isMaintenanceValidForBuildDate(theDate)).thenReturn(true);

        when(mockLicenseDetails.getLicenseString()).thenReturn("licString");
        when(mockJiraLicenseService.validate(any(I18nHelper.class), eq("licString"))).thenReturn(validationResult);

        when(validationResult.getErrorCollection()).thenReturn(errorCollection);


        final UpgradeManager tested = createForTest();
        final UpgradeManager.Status status = tested.doUpgradeIfNeededAndAllowed(null, false);
        assertFalse(status.succesful());
        assertEquals(1, status.getErrors().size());
    }

    @Test
    public void testUpgradesInOrder()
    {
        when(mockApplicationProperties.getString(JIRA_PATCHED_VERSION)).thenReturn("0.1");
        when(mockBuildUtilsInfo.getVersion()).thenReturn(VERSION);

        final UpgradeManagerImpl tested = createForTest();
        final SortedMap<String, UpgradeTask> upgrades = tested.getRelevantUpgradesFromList(tested.getAllUpgrades());
        final Iterator<UpgradeTask> iterator = upgrades.values().iterator();

        UpgradeTask task = iterator.next();
        assertEquals(task.getShortDescription(), "short desc7");

        task = iterator.next();
        assertEquals(task.getShortDescription(), "short desc");

        task = iterator.next();
        assertEquals(task.getShortDescription(), "short desc2");

        task = iterator.next();
        assertEquals(task.getShortDescription(), "short desc8");

        task = iterator.next();
        assertEquals(task.getShortDescription(), "short desc3");

        task = iterator.next();
        assertEquals(task.getShortDescription(), "short desc4");

        task = iterator.next();
        assertEquals(task.getShortDescription(), "short desc5");

        task = iterator.next();
        assertEquals(task.getShortDescription(), "short desc6");
    }

    @Test
    public void testUpgradesSubset0_9() throws IndexException
    {
        when(mockApplicationProperties.getString(JIRA_PATCHED_VERSION)).thenReturn("0.9");
        when(mockBuildUtilsInfo.getVersion()).thenReturn(VERSION);

        final UpgradeManagerImpl tested = createForTest();
        final SortedMap<String, UpgradeTask> upgrades = tested.getRelevantUpgradesFromList(tested.getAllUpgrades());
        final Iterator<UpgradeTask> iterator = upgrades.values().iterator();

        UpgradeTask task = iterator.next();
        assertEquals(task.getShortDescription(), "short desc");

        task = iterator.next();
        assertEquals(task.getShortDescription(), "short desc2");

        task = iterator.next();
        assertEquals(task.getShortDescription(), "short desc8");

        task = iterator.next();
        assertEquals(task.getShortDescription(), "short desc3");

        task = iterator.next();
        assertEquals(task.getShortDescription(), "short desc4");

        task = iterator.next();
        assertEquals(task.getShortDescription(), "short desc5");

        task = iterator.next();
        assertEquals(task.getShortDescription(), "short desc6");
    }

    @Test
    public void testUpgradesSubset1_1()
    {
        when(mockApplicationProperties.getString(JIRA_PATCHED_VERSION)).thenReturn("1.1");
        when(mockBuildUtilsInfo.getVersion()).thenReturn(VERSION);

        final UpgradeManagerImpl tested = createForTest();

        final SortedMap<String, UpgradeTask> upgrades = tested.getRelevantUpgradesFromList(tested.getAllUpgrades());
        final Iterator<UpgradeTask> iterator = upgrades.values().iterator();

        UpgradeTask task = iterator.next();
        assertEquals(task.getShortDescription(), "short desc3");

        task = iterator.next();
        assertEquals(task.getShortDescription(), "short desc4");

        task = iterator.next();
        assertEquals(task.getShortDescription(), "short desc5");

        task = iterator.next();
        assertEquals(task.getShortDescription(), "short desc6");
    }

    @Test
    public void testUpgradesSubset1_3()
    {
        when(mockApplicationProperties.getString(JIRA_PATCHED_VERSION)).thenReturn("1.3");
        when(mockBuildUtilsInfo.getVersion()).thenReturn(VERSION);

        final UpgradeManagerImpl tested = createForTest();
        final SortedMap<String, UpgradeTask> upgrades = tested.getRelevantUpgradesFromList(tested.getAllUpgrades());
        final Iterator<UpgradeTask> iterator = upgrades.values().iterator();

        final UpgradeTask task = iterator.next();
        assertEquals(task.getShortDescription(), "short desc6");
    }

    @Test
    public void testUpgradesSubset27()
    {
        when(mockApplicationProperties.getString(JIRA_PATCHED_VERSION)).thenReturn("27");
        when(mockBuildUtilsInfo.getVersion()).thenReturn(VERSION);

        final UpgradeManagerImpl tested = createForTest();
        final SortedMap<String, UpgradeTask> upgrades = tested.getRelevantUpgradesFromList(tested.getAllUpgrades());
        assertEquals(upgrades.size(), 0);
    }

    @Test
    public void testDoUpgradeIfNeededNoExport() throws IllegalXMLCharactersException
    {
        when(mockDelegator.findAll("UpgradeHistory")).thenReturn(Collections.<GenericValue>emptyList());

        when(mockApplicationProperties.getString(APKeys.JIRA_SETUP)).thenReturn("true");
        when(mockApplicationProperties.getString(JIRA_PATCHED_VERSION)).thenReturn("100");
        when(mockApplicationProperties.getOption(APKeys.JIRA_AUTO_EXPORT)).thenReturn(true);
        when(mockBuildUtilsInfo.getVersion()).thenReturn(VERSION);
        when(mockBuildUtilsInfo.getApplicationBuildNumber()).thenReturn(400);
        when(mockBuildUtilsInfo.getDatabaseBuildNumber()).thenReturn(100);

        final UpgradeManagerImpl tested = createForTest();
        tested.doUpgradeIfNeededAndAllowed(null, false);
    }

    @Test
    public void testDoUpgradeIfNeededWithExportErrors() throws Exception
    {
        when(mockApplicationProperties.getString(APKeys.JIRA_SETUP)).thenReturn("true");
        when(mockApplicationProperties.getString(JIRA_PATCHED_VERSION)).thenReturn("100");
        when(mockApplicationProperties.getOption(APKeys.JIRA_AUTO_EXPORT)).thenReturn(true);
        when(mockBuildUtilsInfo.getVersion()).thenReturn(VERSION);
        when(mockBuildUtilsInfo.getApplicationBuildNumber()).thenReturn(400);
        when(mockBuildUtilsInfo.getDatabaseBuildNumber()).thenReturn(100);

        final String tempDir = "somedir";

        final AutoExport mockAutoExport = mock(AutoExport.class);
        when(mockAutoExport.exportData()).thenThrow(new Exception("There was an error."));

        final UpgradeManagerImpl tested = createForTest(mockAutoExport);
        final UpgradeManager.Status status = tested.doUpgradeIfNeededAndAllowed(tempDir, false);
        assertFalse(status.succesful());
        final Collection<String> errors = status.getErrors();
        assertEquals(1, errors.size());

        final String message = errors.iterator().next();
        assertTrue(message.startsWith("Error occurred during export before upgrade:"));
    }

    @Test
    public void testDoUpgradeIfNeededWithExportIllegalCharacters() throws Exception
    {
        when(mockApplicationProperties.getString(APKeys.JIRA_SETUP)).thenReturn("true");
        when(mockApplicationProperties.getString(JIRA_PATCHED_VERSION)).thenReturn("100");
        when(mockApplicationProperties.getOption(APKeys.JIRA_AUTO_EXPORT)).thenReturn(true);
        when(mockBuildUtilsInfo.getVersion()).thenReturn(VERSION);
        when(mockBuildUtilsInfo.getApplicationBuildNumber()).thenReturn(400);
        when(mockBuildUtilsInfo.getDatabaseBuildNumber()).thenReturn(100);

        final String expectedExportPath = "somedir";

        final AutoExport mockAutoExport = mock(AutoExport.class);
        when(mockAutoExport.exportData()).thenThrow(new IllegalXMLCharactersException("Bad characters."));

        final UpgradeManagerImpl tested = createForTest(mockAutoExport);
        Collection<String> errors = null;
        try
        {
            tested.doUpgradeIfNeededAndAllowed(expectedExportPath, false);
            fail("IllegalXMLCharactersException should have been thrown.");
        }
        catch (final IllegalXMLCharactersException e)
        {
            assertNull(errors);
            assertEquals("Bad characters.", e.getMessage());
        }
    }

    @Test
    public void testDoUpdateNoAutoExportInOnDemandJRADEV11718() throws Exception
    {
        when(mockDelegator.findAll("UpgradeHistory")).thenReturn(Collections.<GenericValue>emptyList());
        when(mockFeatureManager.isOnDemand()).thenReturn(true);

        when(mockApplicationProperties.getString(APKeys.JIRA_SETUP)).thenReturn("true");
        when(mockApplicationProperties.getString(JIRA_PATCHED_VERSION)).thenReturn("100");
        when(mockApplicationProperties.getOption(APKeys.JIRA_AUTO_EXPORT)).thenReturn(true);
        when(mockBuildUtilsInfo.getVersion()).thenReturn(VERSION);
        when(mockBuildUtilsInfo.getApplicationBuildNumber()).thenReturn(400);
        when(mockBuildUtilsInfo.getDatabaseBuildNumber()).thenReturn(100);

        final AutoExport mockAutoExport = mock(AutoExport.class);

        final UpgradeManager tested = createForTest(mockAutoExport);
        final UpgradeManager.Status status = tested.doUpgradeIfNeededAndAllowed("somedir", false);

        // we should have no errors
        assertTrue("Unexpected errors: " + status.getErrors(), status.succesful());
    }

    @Test
    public void testDoUpgradeIfNeededWithExport() throws Exception
    {
        when(mockDelegator.findAll("UpgradeHistory")).thenReturn(Collections.<GenericValue>emptyList());

        when(mockApplicationProperties.getString(APKeys.JIRA_SETUP)).thenReturn("true");
        when(mockApplicationProperties.getString(JIRA_PATCHED_VERSION)).thenReturn("100");
        when(mockApplicationProperties.getOption(APKeys.JIRA_AUTO_EXPORT)).thenReturn(true);
        when(mockBuildUtilsInfo.getVersion()).thenReturn(VERSION);
        when(mockBuildUtilsInfo.getApplicationBuildNumber()).thenReturn(400);
        when(mockBuildUtilsInfo.getDatabaseBuildNumber()).thenReturn(100);

        final String expectedExportPath = "somedir";

        final AutoExport mockAutoExport = mock(AutoExport.class);
        when(mockAutoExport.exportData()).thenReturn(expectedExportPath);

        final UpgradeManagerImpl tested = createForTest(mockAutoExport);
        final UpgradeManager.Status status = tested.doUpgradeIfNeededAndAllowed(expectedExportPath, false);
        assertTrue("Unexpected errors: " + status.getErrors(), status.succesful());
        assertEquals(expectedExportPath, tested.getExportFilePath());
    }

    @Test
    public void testGetUpgradeHistoryItemFromTasksNone() throws Exception
    {
        when(mockDelegator.findByCondition(anyString(), any(EntityCondition.class), anyCollectionOf(String.class),
                anyListOf(String.class))).thenReturn(Collections.<GenericValue>emptyList());
        final UpgradeManagerImpl tested = createForTest();
        final UpgradeHistoryItem result = tested.getUpgradeHistoryItemFromTasks();
        assertNull(result);
    }

    @Test
    public void testGetUpgradeHistoryItemFromTasksHappyPath() throws Exception
    {
        when(mockDelegator.findByCondition(anyString(), any(EntityCondition.class), anyCollectionOf(String.class),
                anyListOf(String.class))).thenReturn(Collections.<GenericValue>singletonList(
                new MockGenericValue("UpgradeHistoryLastClassForTargetBuild", ImmutableMap.of(
                        "upgradeclass", "UpgradeTask_Build106"
                )))
        );

        final BuildVersionImpl buildVersion = new BuildVersionImpl("106", "XYZ");
        when(mockBuildVersionRegistry.getVersionForBuildNumber("106")).thenReturn(buildVersion);


        final UpgradeManagerImpl tested = createForTest();

        final UpgradeHistoryItem result = tested.getUpgradeHistoryItemFromTasks();
        final UpgradeHistoryItem expected = new UpgradeHistoryItemImpl(null, "106", "XYZ", "106", null, true);
        assertEquals(result, expected);
    }

    @Test
    public void testGetUpgradeHistoryItemFromTasksBadClass() throws Exception
    {
        when(mockDelegator.findByCondition(anyString(), any(EntityCondition.class), anyCollectionOf(String.class),
                anyListOf(String.class))).thenReturn(Collections.<GenericValue>singletonList(
                new MockGenericValue("UpgradeHistoryLastClassForTargetBuild", ImmutableMap.of(
                        "upgradeclass", "BADCLASS"
                )))
        );

        final UpgradeManagerImpl tested = createForTest();
        final UpgradeHistoryItem result = tested.getUpgradeHistoryItemFromTasks();
        assertNull(result);
    }

    @Test
    public void testGetUpgradeHistoryNoPrevious() throws Exception
    {
        when(mockDelegator.findAll(Mockito.anyString(), anyListOf(String.class))).thenReturn(Collections.<GenericValue>singletonList(
                new MockGenericValue("UpgradeVersionHistory", MapBuilder.build(
                        "timeperformed", null,
                        "targetbuild", "400",
                        "targetversion", VERSION
                )))
        );

        final UpgradeManagerImpl tested = createForTest((UpgradeHistoryItem) null);

        final List<UpgradeHistoryItem> result = tested.getUpgradeHistory();
        final UpgradeHistoryItem expected = new UpgradeHistoryItemImpl(null, "400", VERSION, null, null);
        assertEquals(1, result.size());
        assertEquals(expected, result.get(0));
    }

    @Test
    public void testGetUpgradeHistoryPrevious() throws Exception
    {
        when(mockDelegator.findAll(anyString(), anyListOf(String.class))).thenReturn(Collections.<GenericValue>singletonList(
                new MockGenericValue("UpgradeVersionHistory", MapBuilder.build(
                        "timeperformed", null,
                        "targetbuild", "400",
                        "targetversion", VERSION
                )))
        );

        final UpgradeHistoryItem expected1 = new UpgradeHistoryItemImpl(null, "400", VERSION, "300", "3.0");
        final UpgradeHistoryItem expected2 = new UpgradeHistoryItemImpl(null, "300", "3.0", null, null);

        final UpgradeManagerImpl tested = createForTest(expected2);

        final List<UpgradeHistoryItem> result = tested.getUpgradeHistory();
        assertEquals(2, result.size());
        assertEquals(expected1, result.get(0));
        assertEquals(expected2, result.get(1));
    }

    @Test
    public void testUpgradesWithReindex() throws IllegalXMLCharactersException, IndexException
    {
        when(mockDelegator.findAll("UpgradeHistory")).thenReturn(Collections.<GenericValue>emptyList());

        when(mockApplicationProperties.getString(JIRA_PATCHED_VERSION)).thenReturn("0.9");
        when(mockApplicationProperties.getString(APKeys.JIRA_SETUP)).thenReturn("true");
        when(mockApplicationProperties.getOption(APKeys.JIRA_AUTO_EXPORT)).thenReturn(true);
        when(mockBuildUtilsInfo.getVersion()).thenReturn(VERSION);
        when(mockBuildUtilsInfo.getApplicationBuildNumber()).thenReturn(400);
        when(mockBuildUtilsInfo.getDatabaseBuildNumber()).thenReturn(100);
        when(mockIndexLifecycleManager.size()).thenReturn(8);
        when(mockIndexLifecycleManager.reIndexAll(any(Context.class))).thenReturn(1l);

        final UpgradeManagerImpl man = createForTest();
        UpgradeManager.Status status = man.doUpgradeIfNeededAndAllowed(null, false);

        // Need to assert the errors collection here as the upgrade task swallows Throwable
        assertTrue("Unexpected errors: " + status.getErrors(), status.succesful());
    }

    @Test
    public void testUpgradesWithoutReindex() throws IllegalXMLCharactersException, IndexException
    {
        when(mockDelegator.findAll("UpgradeHistory")).thenReturn(Collections.<GenericValue>emptyList());

        when(mockApplicationProperties.getString(JIRA_PATCHED_VERSION)).thenReturn("1.2");
        when(mockApplicationProperties.getString(APKeys.JIRA_SETUP)).thenReturn("true");
        when(mockApplicationProperties.getOption(APKeys.JIRA_AUTO_EXPORT)).thenReturn(true);
        when(mockBuildUtilsInfo.getVersion()).thenReturn(VERSION);
        when(mockBuildUtilsInfo.getApplicationBuildNumber()).thenReturn(400);
        when(mockBuildUtilsInfo.getDatabaseBuildNumber()).thenReturn(100);

        final UpgradeManagerImpl tested = createForTest();
        tested.doUpgradeIfNeededAndAllowed(null, false);
    }

    @Test
    public void testSequencerIsRefreshedAfterStandardUpgrade() throws Exception
    {
        when(mockDelegator.findAll("UpgradeHistory")).thenReturn(Collections.<GenericValue>emptyList());

        when(mockApplicationProperties.getString(APKeys.JIRA_SETUP)).thenReturn("true");
        when(mockApplicationProperties.getString(JIRA_PATCHED_VERSION)).thenReturn("100");
        when(mockApplicationProperties.getOption(APKeys.JIRA_AUTO_EXPORT)).thenReturn(true);
        when(mockBuildUtilsInfo.getVersion()).thenReturn(VERSION);
        when(mockBuildUtilsInfo.getApplicationBuildNumber()).thenReturn(400);
        when(mockBuildUtilsInfo.getDatabaseBuildNumber()).thenReturn(100);

        UpgradeManager tested = createForTest();
        UpgradeManager.Status status = tested.doUpgradeIfNeededAndAllowed(null, false);
        assertTrue("Unexpected errors: " + status.getErrors(), status.succesful());
        verify(mockDelegator).refreshSequencer();
    }

    @Test
    public void testSequencerIsRefreshedAfterSetupUpgrade() throws Exception
    {
        when(mockDelegator.findAll("UpgradeHistory")).thenReturn(Collections.<GenericValue>emptyList());

        when(mockApplicationProperties.getString(APKeys.JIRA_SETUP)).thenReturn("true");
        when(mockApplicationProperties.getString(JIRA_PATCHED_VERSION)).thenReturn("100");
        when(mockApplicationProperties.getOption(APKeys.JIRA_AUTO_EXPORT)).thenReturn(true);
        when(mockBuildUtilsInfo.getVersion()).thenReturn(VERSION);
        when(mockBuildUtilsInfo.getApplicationBuildNumber()).thenReturn(400);
        when(mockBuildUtilsInfo.getDatabaseBuildNumber()).thenReturn(100);

        UpgradeManager tested = createForTest();
        UpgradeManager.Status status = tested.doSetupUpgrade();
        assertTrue("Unexpected errors: " + status.getErrors(), status.succesful());
        verify(mockDelegator).refreshSequencer();
    }

    @Test
    public void testSequencerIsRefreshedAfterUpgradeWithErrors() throws Exception
    {
        when(mockDelegator.findAll("UpgradeHistory")).thenReturn(Collections.<GenericValue>emptyList());

        when(mockApplicationProperties.getString(APKeys.JIRA_SETUP)).thenReturn("true");
        when(mockApplicationProperties.getString(JIRA_PATCHED_VERSION)).thenReturn("100");
        when(mockApplicationProperties.getOption(APKeys.JIRA_AUTO_EXPORT)).thenReturn(true);
        when(mockBuildUtilsInfo.getVersion()).thenReturn(VERSION);
        when(mockBuildUtilsInfo.getApplicationBuildNumber()).thenReturn(400);
        when(mockBuildUtilsInfo.getDatabaseBuildNumber()).thenReturn(100);

        final UpgradeManager tested = createForTest(ImmutableList.of(taskWithError(false)), Collections.<UpgradeTask>emptyList());
        UpgradeManager.Status status = tested.doUpgradeIfNeededAndAllowed(null, false);
        assertFalse(status.succesful());
        assertEquals(1, status.getErrors().size());
        verify(mockDelegator).refreshSequencer();
    }

    @Test
    public void testSequencerIsRefreshedAfterSetupUpgradeWithErrors() throws Exception
    {
        when(mockDelegator.findAll("UpgradeHistory")).thenReturn(Collections.<GenericValue>emptyList());

        when(mockApplicationProperties.getString(APKeys.JIRA_SETUP)).thenReturn("true");
        when(mockApplicationProperties.getString(JIRA_PATCHED_VERSION)).thenReturn("100");
        when(mockApplicationProperties.getOption(APKeys.JIRA_AUTO_EXPORT)).thenReturn(true);
        when(mockBuildUtilsInfo.getVersion()).thenReturn(VERSION);
        when(mockBuildUtilsInfo.getApplicationBuildNumber()).thenReturn(400);
        when(mockBuildUtilsInfo.getDatabaseBuildNumber()).thenReturn(100);

        final UpgradeManager tested = createForTest(Collections.<UpgradeTask>emptyList(), ImmutableList.of(taskWithError(true)));
        UpgradeManager.Status status = tested.doSetupUpgrade();
        assertFalse(status.succesful());
        assertEquals(1, status.getErrors().size());
        verify(mockDelegator).refreshSequencer();
    }

    private UpgradeTask taskWithError(boolean isSetup) throws Exception
    {
        final UpgradeTask answer = mock(UpgradeTask.class);
        when(answer.getBuildNumber()).thenReturn("150");
        when(answer.getShortDescription()).thenReturn("Testing task");
        doThrow(new Exception("Surprise!!!")).when(answer).doUpgrade(isSetup);
        return answer;
    }

    @Test
    public void testIndexingNotPerformedWhenDisabledViaSystemProperty() throws Exception
    {
        System.setProperty("upgrade.reindex.allowed","false");
        when(mockDelegator.findAll("UpgradeHistory")).thenReturn(Collections.<GenericValue>emptyList());

        when(mockApplicationProperties.getString(JIRA_PATCHED_VERSION)).thenReturn("0.9");
        when(mockApplicationProperties.getString(APKeys.JIRA_SETUP)).thenReturn("true");
        when(mockApplicationProperties.getOption(APKeys.JIRA_AUTO_EXPORT)).thenReturn(true);
        when(mockBuildUtilsInfo.getVersion()).thenReturn(VERSION);
        when(mockBuildUtilsInfo.getApplicationBuildNumber()).thenReturn(400);
        when(mockBuildUtilsInfo.getDatabaseBuildNumber()).thenReturn(100);
        when(mockI18HelperFactory.getInstance(Locale.ENGLISH)).thenReturn(mockI18nHelper);

        final UpgradeManagerImpl man = createForTest();
        UpgradeManager.Status status = man.doUpgradeIfNeededAndAllowed(null, false);

        assertTrue("Unexpected errors: " + status.getErrors(), status.succesful());
        assertFalse("Unexpected reindex", status.reindexPerformed());
    }
}