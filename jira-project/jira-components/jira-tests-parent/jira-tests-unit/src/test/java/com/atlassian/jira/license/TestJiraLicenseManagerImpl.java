package com.atlassian.jira.license;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.extras.api.AtlassianLicense;
import com.atlassian.extras.api.Product;
import com.atlassian.extras.api.jira.JiraLicense;
import com.atlassian.extras.common.LicenseException;
import com.atlassian.jira.config.CoreFeatures;
import com.atlassian.jira.config.FeatureManager;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.datetime.DateTimeFormatter;
import com.atlassian.jira.util.BuildUtilsInfo;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.web.util.ExternalLinkUtil;
import com.atlassian.license.SIDManager;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.atlassian.jira.license.DefaultLicenseDetails.DATACENTER_PROPERTY_NAME;
import static com.atlassian.jira.license.DefaultLicenseDetails.ELA_PROPERTY_NAME;
import static com.atlassian.jira.license.SubscriptionLicenseDetails.WARNING_PERIOD_IN_MSEC;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

/**
 * Tests {@link JiraLicenseManager}.
 *
 * @since v6.3
 */
@RunWith (MockitoJUnitRunner.class)
public class TestJiraLicenseManagerImpl
{
    public static final CoreFeatures DARK_FEATURE = CoreFeatures.LICENSE_ROLES_ENABLED;
    private static final String BAD_LICENSE = "I'm a bad license";
    private static final String GOOD_LICENSE = "I'm a good license";
    private static final String USER_NAME = "userName";

    @Mock private JiraLicenseStore licenseStore;
    @Mock private AtlassianLicense atlassianLicense;
    @Mock private JiraLicense jiraLicense;
    @Mock private BuildUtilsInfo buildUtilsInfo;
    @Mock private ApplicationProperties applicationProperties;
    @Mock private SIDManager sidManager;
    @Mock private EventPublisher eventPublisher;
    @Mock private ExternalLinkUtil externalLinkUtil;
    @Mock private DateTimeFormatter dateTimeFormatter;
    @Mock private I18nHelper.BeanFactory i18nFactory;
    @Mock private MultiLicenseStore multiLicenseStore;
    @Mock private LicenseDetailsFactory licenseDetailsFactory;
    @Mock private FeatureManager featureManager;

    @Mock private LicenseDetails licenseDetails;
    @Mock private LicenseDetails licenseDetails2;
    @Mock private LicenseRoleDetails licenseRoleDetails;
    @Mock private LicenseRoleDetails licenseRoleDetails2;

    private JiraLicenseManager jiraLicenseManager;

    @Before
    public void setUp()
    {
        jiraLicenseManager = new JiraLicenseManagerImpl( licenseStore, buildUtilsInfo, sidManager, eventPublisher,
            multiLicenseStore, featureManager, licenseDetailsFactory);
    }

    @Test
    public void testBlankLicenseStringIsInvalid()
    {
        assertFalse(jiraLicenseManager.isDecodeable(""));
    }

    @Test
    public void testGettingAnInvalidStoredLicenseThrowsException()
    {
        when(multiLicenseStore.retrieve()).thenReturn(newArrayList(BAD_LICENSE));
        when(licenseDetailsFactory.getLicense(BAD_LICENSE)).thenThrow(new LicenseException());

        try
        {
            jiraLicenseManager.getLicense();
            fail("Should have barfed");
        }
        catch (LicenseException expected) {}
    }

    @Test
    public void testGettingLicenseReturnsLicense()
    {
        when(multiLicenseStore.retrieve()).thenReturn(newArrayList(GOOD_LICENSE));
        when(licenseDetailsFactory.getLicense(GOOD_LICENSE)).thenReturn(licenseDetails);
        when(atlassianLicense.getProductLicense(Product.JIRA)).thenReturn(jiraLicense);

        final LicenseDetails actualLicenseDetails = jiraLicenseManager.getLicense(GOOD_LICENSE);
        assertNotNull(actualLicenseDetails);
        assertEquals(licenseDetails, actualLicenseDetails);

        verify(licenseDetailsFactory).getLicense(GOOD_LICENSE);
    }

    @Test
    public void testGettingInvalidLicenseStringThrowException()
    {
        when(licenseDetailsFactory.getLicense(BAD_LICENSE)).thenThrow(new LicenseException());

        try
        {
            jiraLicenseManager.getLicense(BAD_LICENSE);
            fail("Should have barfed");
        }
        catch (LicenseException expected) {}
    }

    @Test
    public void testLicenseManagerDelegatesLookup() throws Exception
    {
        jiraLicenseManager.getLicense(GOOD_LICENSE);
        verify(licenseDetailsFactory).getLicense(GOOD_LICENSE);
        verifyNoMoreInteractions(licenseStore, atlassianLicense, jiraLicense, buildUtilsInfo, sidManager,
                eventPublisher, multiLicenseStore, featureManager, licenseDetailsFactory, licenseDetails);
    }

    @Test
    public void testGettingProvidedLicenseReturnsLicense() throws Exception
    {
        when(licenseDetailsFactory.getLicense(GOOD_LICENSE)).thenReturn(licenseDetails);
        when(atlassianLicense.getProductLicense(Product.JIRA)).thenReturn(jiraLicense);

        LicenseDetails actualLicenseDetails = jiraLicenseManager.getLicense(GOOD_LICENSE);

        assertNotNull(actualLicenseDetails);
        assertEquals(licenseDetails, actualLicenseDetails);
    }

    @Test
    public void testSettingLicenseWhichNeedsConfirmationReset_63()
    {
        when(licenseDetailsFactory.isDecodeable(anyString())).thenReturn(true);
        assertLicenseCanBeSetAndTestConfirmationPaths_63(true, true);
    }

    @Test
    public void testSetLicense_HappyAndNeedsConfirmationReset_70() throws Exception
    {
        when(featureManager.isEnabled(DARK_FEATURE)).thenReturn(true);
        when(licenseDetailsFactory.isDecodeable(anyString())).thenReturn(true);
        assertLicenseCanBeSetAndTestConfirmationPaths_70(true, true);
    }

    @Test
    public void testSetLicense_InvalidLicenseInput()
    {
        when(licenseDetailsFactory.getLicense(BAD_LICENSE)).thenThrow(new LicenseException());

        try
        {
            jiraLicenseManager.setLicense(BAD_LICENSE);
            fail("Should have barfed");
        }
        catch (IllegalArgumentException expected)
        {
        }
    }

    @Test
    public void testSetLicense_InvalidLicenseInput70()
    {
        when(featureManager.isEnabled(DARK_FEATURE)).thenReturn(true);
        when(licenseDetailsFactory.getLicense(BAD_LICENSE)).thenThrow(new LicenseException());

        try
        {
            jiraLicenseManager.setLicense(BAD_LICENSE);
            fail("Should have barfed");
        }
        catch (IllegalArgumentException expected)
        {
        }
    }

    @Test
    public void testSetLicense_HappyMayNeedNeedsConfirmationReset() throws Exception
    {
        when(licenseDetailsFactory.isDecodeable(anyString())).thenReturn(true);
        assertLicenseCanBeSetAndTestConfirmationPaths_63(true, false);
    }


    @Test
    public void testSetLicense_HappyMayNeedNeedsConfirmationReset_70() throws Exception
    {
        when(featureManager.isEnabled(DARK_FEATURE)).thenReturn(true);
        when(licenseDetailsFactory.isDecodeable(anyString())).thenReturn(true);
        assertLicenseCanBeSetAndTestConfirmationPaths_70(true, false);
    }

    @Test
    public void testSettingLicenseWhichMayNeedConfirmationReset2()
    {
        when(licenseDetailsFactory.isDecodeable(anyString())).thenReturn(true);
        assertLicenseCanBeSetAndTestConfirmationPaths_63(false, true);
    }

    @Test
    public void testSetLicense_HappyMayNeedNeedsConfirmationReset2_70() throws Exception
    {
        jiraLicenseManager.confirmProceedUnderEvaluationTerms(USER_NAME);
        when(featureManager.isEnabled(DARK_FEATURE)).thenReturn(true);
        when(licenseDetailsFactory.isDecodeable(anyString())).thenReturn(true);
        assertLicenseCanBeSetAndTestConfirmationPaths_70(false, true);
    }

    @Test
    public void testConfirmProceed()
    {
        jiraLicenseManager.confirmProceedUnderEvaluationTerms(USER_NAME);
        verify(licenseStore).confirmProceedUnderEvaluationTerms(USER_NAME);
    }

    private void assertLicenseCanBeSetAndTestConfirmationPaths_63(final boolean isMaintenanceValidForBuildDate, final boolean isUsingOldLicenseToRunNewJiraBuild)
    {
        when(licenseDetailsFactory.getLicense(GOOD_LICENSE)).thenReturn(licenseDetails);
        when(atlassianLicense.getProductLicense(Product.JIRA)).thenReturn(jiraLicense);

        final Date date = new Date();

        when(buildUtilsInfo.getCurrentBuildDate()).thenReturn(date);
        if (isMaintenanceValidForBuildDate)
        {
            when(licenseDetails.isMaintenanceValidForBuildDate(date)).thenReturn(true);
            when(licenseDetails.hasLicenseTooOldForBuildConfirmationBeenDone()).thenReturn(true);
        }


        final LicenseDetails actualLicenseDetails = jiraLicenseManager.setLicense(GOOD_LICENSE);


        if (isMaintenanceValidForBuildDate && isUsingOldLicenseToRunNewJiraBuild)
        {
            verify(licenseStore).resetOldBuildConfirmation();
        }
        assertNotNull(actualLicenseDetails);
        assertEquals(licenseDetails, actualLicenseDetails);

        verify(licenseDetailsFactory).getLicense(GOOD_LICENSE);
        verify(eventPublisher).publish(any());
        verify(multiLicenseStore).store(eq(newArrayList(GOOD_LICENSE)));
    }

    private void assertLicenseCanBeSetAndTestConfirmationPaths_70(final boolean isMaintenanceValidForBuildDate, final boolean isUsingOldLicenseToRunNewJiraBuild)
    {
        when(licenseDetailsFactory.getLicense(GOOD_LICENSE)).thenReturn(licenseDetails);
        when(atlassianLicense.getProductLicense(Product.JIRA)).thenReturn(jiraLicense);
        when(featureManager.isEnabled(DARK_FEATURE)).thenReturn(true);
        when(licenseDetails.getLicenseRoles()).thenReturn(licenseRoleDetails);
        when(multiLicenseStore.retrieve()).thenReturn(new ArrayList<String>());

        final Date date = new Date();

        when(buildUtilsInfo.getCurrentBuildDate()).thenReturn(date);
        if (isMaintenanceValidForBuildDate)
        {
            when(licenseDetails.isMaintenanceValidForBuildDate(date)).thenReturn(true);
            when(licenseDetails.hasLicenseTooOldForBuildConfirmationBeenDone()).thenReturn(true);
        }

        final LicenseDetails actualLicenseDetails = jiraLicenseManager.setLicense(GOOD_LICENSE);

        assertNotNull(actualLicenseDetails);
        assertEquals(licenseDetails, actualLicenseDetails);

        verify(licenseDetailsFactory, times(2)).getLicense(GOOD_LICENSE);
        verify(eventPublisher).publish(any());
        verify(multiLicenseStore).store(eq(newArrayList(GOOD_LICENSE)));

        if (isMaintenanceValidForBuildDate && isUsingOldLicenseToRunNewJiraBuild)
        {
            verify(licenseStore).resetOldBuildConfirmation();
        }

    }

    @Test
    public void testGetServerIdWithExistingServerId()
    {
        final String expectedServerId = "A server ID";

        when(multiLicenseStore.retrieveServerId()).thenReturn(expectedServerId);

        final String actualSID = jiraLicenseManager.getServerId();
        assertEquals(expectedServerId, actualSID);
        verify(multiLicenseStore, never()).storeServerId(expectedServerId);
    }

    @Test
    public void testGetServerIdWithNonExistingServerId()
    {
        final String expectedServerId = "A server ID";
        when(multiLicenseStore.retrieveServerId()).thenReturn(null);
        when(sidManager.generateSID()).thenReturn(expectedServerId);

        final String actual = jiraLicenseManager.getServerId();

        verify(multiLicenseStore).storeServerId(expectedServerId);
        assertEquals(expectedServerId, actual);
    }

    @Test
    public void identicalLicenseShouldNotBeStoredTwice()
    {
        when(licenseDetailsFactory.isDecodeable("example")).thenReturn(true);
        when(featureManager.isEnabled(CoreFeatures.LICENSE_ROLES_ENABLED)).thenReturn(true);
        when(licenseDetailsFactory.getLicense("example")).thenReturn(licenseDetails);
        when(licenseDetails.getLicenseRoles()).thenReturn(licenseRoleDetails);

        LicenseRoleId licenseRoleId = new LicenseRoleId("role-1");
        Set<LicenseRoleId> licenseRoleIds = new HashSet<LicenseRoleId>();
        licenseRoleIds.add(licenseRoleId);
        when(licenseRoleDetails.getLicenseRoles()).thenReturn(licenseRoleIds);


        when(multiLicenseStore.retrieve()).thenReturn(ImmutableList.of("example"));

        LicenseDetails details = jiraLicenseManager.setLicenseNoEvent("example");

        assertEquals(details, licenseDetails);
        verify(multiLicenseStore).store(newArrayList("example"));
    }

    @Test
    public void existingDifferentLicenseShouldNotBeOverridden()
    {
        when(licenseDetailsFactory.isDecodeable("example")).thenReturn(true);
        when(featureManager.isEnabled(CoreFeatures.LICENSE_ROLES_ENABLED)).thenReturn(true);

        when(licenseDetailsFactory.getLicense("example")).thenReturn(licenseDetails);
        when(licenseDetails.getLicenseRoles()).thenReturn(licenseRoleDetails);
        when(licenseDetails.getLicenseString()).thenReturn("example");
        when(licenseRoleDetails.getLicenseRoles()).thenReturn(newHashSet(new LicenseRoleId("role-1")));

        when(licenseDetailsFactory.getLicense("different")).thenReturn(licenseDetails2);
        when(licenseDetails2.getLicenseRoles()).thenReturn(licenseRoleDetails2);
        when(licenseDetails2.getLicenseString()).thenReturn("different");
        when(licenseRoleDetails2.getLicenseRoles()).thenReturn(newHashSet(new LicenseRoleId("role-2")));


        when(multiLicenseStore.retrieve()).thenReturn(ImmutableList.of("different"));

        LicenseDetails details = jiraLicenseManager.setLicenseNoEvent("example");

        assertEquals(details, licenseDetails);
        verify(multiLicenseStore).store(newArrayList("example", "different"));
    }

    @Test
    public void existingDifferentLicenseWithSameRoleShouldBeReplaced()
    {
        when(licenseDetailsFactory.isDecodeable("example")).thenReturn(true);
        when(featureManager.isEnabled(CoreFeatures.LICENSE_ROLES_ENABLED)).thenReturn(true);

        when(licenseDetailsFactory.getLicense("example")).thenReturn(licenseDetails);
        when(licenseDetails.getLicenseRoles()).thenReturn(licenseRoleDetails);
        when(licenseRoleDetails.getLicenseRoles()).thenReturn(newHashSet(new LicenseRoleId("role-1")));

        when(licenseDetailsFactory.getLicense("different")).thenReturn(licenseDetails2);
        when(licenseDetails2.getLicenseRoles()).thenReturn(licenseRoleDetails2);
        when(licenseRoleDetails2.getLicenseRoles()).thenReturn(newHashSet(new LicenseRoleId("role-1")));


        when(multiLicenseStore.retrieve()).thenReturn(ImmutableList.of("different"));

        LicenseDetails details = jiraLicenseManager.setLicenseNoEvent("example");

        assertEquals(details, licenseDetails);
        verify(multiLicenseStore).store(newArrayList("example"));
    }

}
