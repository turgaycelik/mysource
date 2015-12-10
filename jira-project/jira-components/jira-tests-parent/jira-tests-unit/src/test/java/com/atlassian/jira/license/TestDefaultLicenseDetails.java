package com.atlassian.jira.license;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.extras.api.LicenseType;
import com.atlassian.extras.decoder.v2.Version2LicenseDecoder;
import com.atlassian.jira.cluster.ClusterManager;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.datetime.DateTimeFormatter;
import com.atlassian.jira.datetime.DateTimeFormatterFactoryStub;
import com.atlassian.jira.mock.MockApplicationProperties;
import com.atlassian.jira.mock.web.util.MockOutlookDate;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.util.BuildUtilsInfo;
import com.atlassian.jira.util.ConstantClock;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.NoopI18nFactory;
import com.atlassian.jira.util.NoopI18nHelper;
import com.atlassian.jira.web.util.ExternalLinkUtil;
import com.atlassian.jira.web.util.OutlookDate;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import static com.atlassian.jira.license.DefaultLicenseDetails.DATACENTER_PROPERTY_NAME;
import static com.atlassian.jira.license.DefaultLicenseDetails.ENABLED;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

/**
 * Tests {@link DefaultLicenseDetails}.
 *
 * @since v6.3
 */
public class TestDefaultLicenseDetails
{
    private static final OutlookDate OUTLOOK_DATE = new MockOutlookDate(Locale.ENGLISH)
    {
        @Override
        @SuppressWarnings("deprecation")
        public String formatDMY(final Date date)
        {
            return date.toString();
        }
    };

    private static final String LICENSE_STRING = "Some license String";
    private static final long GRACE_PERIOD_IN_MILLIS = TimeUnit.DAYS.toMillis(30);

    private long fiftyDaysAgoInMillis;
    private long tenDaysAgoInMillis;
    private long thirtyDaysAgoInMillis;
    private ApplicationProperties applicationProperties;
    private Date now;
    private Date tenDaysFromNow;
    private Date tenSecondsBeforeNow;
    private Date tenSecondsFromNow;
    private Date twentyFourDaysAgo;

    private Date fiftyDaysFromNow;
    private ExternalLinkUtil externalLinkUtil;
    private I18nHelper mockI18nHelper;
    private LicenseDetails licenseDetails;
    private MockLicense mockLicense;
    private User fred;
    private BuildUtilsInfo buildUtilsInfo;
    private ClusterManager clusterManager;

    @Before
    public void setUp() throws Exception
    {
        final DateTimeFormatter dateTimeFormatter = new DateTimeFormatterFactoryStub().formatter();
        buildUtilsInfo = Mockito.mock(BuildUtilsInfo.class);
        clusterManager = Mockito.mock(ClusterManager.class);
        applicationProperties = new MockApplicationProperties();
        externalLinkUtil = new MockExternalLinkUtil();
        mockI18nHelper = new NoopI18nHelper(Locale.ENGLISH);

        now = new Date(1401162923372L);
        tenSecondsFromNow = new Date(now.getTime() + TimeUnit.SECONDS.toMillis(10));
        tenSecondsBeforeNow = new Date(now.getTime() - TimeUnit.SECONDS.toMillis(10));
        tenDaysFromNow = new Date(now.getTime() + TimeUnit.DAYS.toMillis(10));
        twentyFourDaysAgo = new Date(now.getTime() - TimeUnit.DAYS.toMillis(24));
        fiftyDaysFromNow = new Date(now.getTime() + TimeUnit.DAYS.toMillis(50));

        tenDaysAgoInMillis = now.getTime() - TimeUnit.DAYS.toMillis(10);
        thirtyDaysAgoInMillis = now.getTime() - TimeUnit.DAYS.toMillis(30);
        fiftyDaysAgoInMillis = now.getTime() - TimeUnit.DAYS.toMillis(50);

        mockLicense = new MockLicense();
        mockLicense.setLicenseType(LicenseType.COMMERCIAL);
        mockLicense.setMaintenanceExpired(false);
        mockLicense.setMaintenanceExpiryDate(tenSecondsFromNow);

        when(buildUtilsInfo.getCurrentBuildDate()).thenReturn(now);

        licenseDetails = new DefaultLicenseDetails(
            mockLicense, LICENSE_STRING, applicationProperties, externalLinkUtil,
            buildUtilsInfo, new NoopI18nFactory(), dateTimeFormatter, new Version2LicenseDecoder(),
            clusterManager, new ConstantClock(now.getTime()))
        {
            @Override
            User getConfirmedUser()
            {
                return fred;
            }
        };
    }

    @After
    public void tearDown() throws Exception
    {
        applicationProperties = null;
        externalLinkUtil = null;
        now = null;
        mockLicense = null;
        licenseDetails = null;
    }

    private void assertExpiryMessageContains(final String text)
    {
        assertTrue(licenseDetails.getLicenseExpiryStatusMessage(fred).contains(text));
    }

    @Test
    public void testGetLicenseExpiryStatusMessageForExpiredEvaluation()
    {
        mockLicense.setEvaluation(true);
        mockLicense.setExpiryDate(now);
        mockLicense.setExpired(true);

        applicationProperties.setOption(APKeys.JIRA_CONFIRMED_INSTALL_WITH_OLD_LICENSE, true);

        assertExpiryMessageContains("admin.license.expired");
    }

    @Test
    public void testGetLicenseExpiryStatusMessageForNonExpiredEvaluation()
    {
        mockLicense.setEvaluation(true);
        mockLicense.setExpiryDate(tenSecondsFromNow);
        mockLicense.setExpired(false);
        assertExpiryMessageContains("admin.license.expiresin");
    }

    @Test
    public void testGetLicenseExpiryStatusMessageWhenTimestampedForOldLicenseOutsideTheGracePeriod()
    {
        mockLicense.setMaintenanceExpiryDate(tenSecondsBeforeNow); // a time well before the current build date
        setLicenseExtenstionTimestamp(fiftyDaysAgoInMillis);// set the recorded extension timestanmp to be outside the grace period

        assertExpiryMessageContains("admin.license.expired");
    }

    @Test
    public void testGetLicenseExpiryStatusMessageWhenTimestampedForOldLicenseWithinTheGracePeriod()
    {
        mockLicense.setMaintenanceExpiryDate(tenSecondsBeforeNow); // a tim well before the current build date
        setLicenseExtenstionTimestamp(tenDaysAgoInMillis); // set the recorded extension timestanmp to be within the grace period

        assertExpiryMessageContains("admin.license.expiresin");
    }

    @Test
    public void testGetLicenseExpiryStatusMessageForLicenseWithNonExpiredMaintenance()
    {
        mockLicense.setMaintenanceExpired(false);
        mockLicense.setMaintenanceExpiryDate(tenSecondsFromNow);

        assertExpiryMessageContains("admin.support.available.until");
    }

    @Test
    public void testGetLicenseExpiryStatusMessageForCommunityLicenseWithNonExpiredMaintenance()
    {
        mockLicense.setMaintenanceExpired(false);
        mockLicense.setMaintenanceExpiryDate(tenSecondsFromNow);

        // other self renewable but supported licenses
        mockLicense.setLicenseType(LicenseType.COMMUNITY);
        assertExpiryMessageContains("admin.support.available.until");
    }

    @Test
    public void testGetLicenseExpiryStatusMessageForPersonalLicenseWithNonExpiredMaintenance()
    {
        mockLicense.setMaintenanceExpired(false);
        mockLicense.setMaintenanceExpiryDate(tenSecondsFromNow);
        // unsupported license
        mockLicense.setLicenseType(LicenseType.PERSONAL);
        assertExpiryMessageContains("admin.upgrades.available.until");
    }

    @Test
    public void testGetLicenseExpiryStatusMessageForLicenseWithExpiredMaintenance()
    {
        // finally, if expired, message should be null
        mockLicense.setMaintenanceExpired(true);
        mockLicense.setMaintenanceExpiryDate(tenSecondsBeforeNow);
        assertNull(licenseDetails.getLicenseExpiryStatusMessage(fred));
    }

    private void assertBriefMaintenanceMessageContains(final String text)
    {
        assertTrue(licenseDetails.getBriefMaintenanceStatusMessage(mockI18nHelper).contains(text));
    }

    @Test
    public void testGetBriefMaintenanceStatusMessageForExpiredEvaluationLicense()
    {
        mockLicense.setEvaluation(true);
        mockLicense.setExpired(true);
        assertBriefMaintenanceMessageContains("supported.expired");
    }

    @Test
    public void testGetBriefMaintenanceStatusMessageWithValidCommercialLicenseWithinMaintenanceTimeframe()
    {
        assertBriefMaintenanceMessageContains("supported.valid");
    }

    @Test
    public void testGetBriefMaintenanceStatusMessageWithOldLicenseOutsideOfGracePeriod()
    {
        mockLicense.setMaintenanceExpiryDate(tenSecondsBeforeNow);
        setLicenseExtenstionTimestamp(fiftyDaysAgoInMillis); // outside grace period

        assertBriefMaintenanceMessageContains("supported.expired");
    }

    @Test
    public void testGetBriefMaintenanceStatusMessageWithOldLicenseWithinGracePeriod()
    {
        mockLicense.setMaintenanceExpiryDate(tenSecondsBeforeNow);
        setLicenseExtenstionTimestamp(tenDaysAgoInMillis); // within grace period

        assertBriefMaintenanceMessageContains("supported.valid");
    }

    @Test
    public void testGetBriefMaintenanceStatusMessageForValidCommunityLicense()
    {
        mockLicense.setLicenseType(LicenseType.COMMUNITY);
        assertBriefMaintenanceMessageContains("supported.valid");
    }

    @Test
    public void testGetBriefMaintenanceStatusMessageForValidPersonalLicense()
    {
        mockLicense.setLicenseType(LicenseType.PERSONAL);
        assertBriefMaintenanceMessageContains("unsupported");
    }

    @Test
    public void testGetBriefMaintenanceStatusMessageForMaintenanceExpiredPersonalLicense()
    {
        mockLicense.setLicenseType(LicenseType.PERSONAL);
        mockLicense.setMaintenanceExpired(true);
        mockLicense.setMaintenanceExpiryDate(tenSecondsBeforeNow);

        assertBriefMaintenanceMessageContains("unsupported");
    }

    @Test
    public void testGetBriefMaintenanceStatusMessageMaintenanceExpiredLicense()
    {
        mockLicense.setMaintenanceExpired(true);
        mockLicense.setMaintenanceExpiryDate(tenSecondsBeforeNow);
        assertBriefMaintenanceMessageContains("supported.expired");
    }

    @Test
    public void testGetMaintenanceEndStringEvaluation()
    {
        mockLicense.setEvaluation(true);
        mockLicense.setExpiryDate(now);
        assertEquals(now.toString(), licenseDetails.getMaintenanceEndString(OUTLOOK_DATE));

    }

    @Test
    public void testGetMaintenanceEndStringOldNewBuild()
    {
        mockLicense.setMaintenanceExpiryDate(tenSecondsBeforeNow);
        setLicenseExtenstionTimestamp(tenSecondsBeforeNow.getTime());

        Date expected = new Date(tenSecondsBeforeNow.getTime() + GRACE_PERIOD_IN_MILLIS);
        assertEquals(expected.toString(), licenseDetails.getMaintenanceEndString(OUTLOOK_DATE));
    }

    @Test
    public void testGetMaintenanceEndStringNotEvaluationNorOldNewBuild()
    {
        mockLicense.setMaintenanceExpiryDate(tenSecondsBeforeNow);
        assertEquals(tenSecondsBeforeNow.toString(), licenseDetails.getMaintenanceEndString(OUTLOOK_DATE));
    }

    @Test
    public void testGetLicenseStatusMessageEvaluation()
    {
        mockLicense.setEvaluation(true);
        mockLicense.setExpiryDate(now);
        mockLicense.setExpired(true);

        // expired
        assertTrue(licenseDetails.getLicenseStatusMessage(fred, "").contains("admin.license.evaluation.expired"));
    }

    @Test
    public void testGetLicenseStatusMessageEvaluationAlmostExpired()
    {
        // almost expired
        mockLicense.setEvaluation(true);
        mockLicense.setExpiryDate(tenSecondsFromNow);
        assertTrue(licenseDetails.getLicenseStatusMessage(fred, "").contains("admin.license.evaluation.almost.expired"));
    }

    @Test
    public void testGetLicenseStatusMessageEvaluationNotAlmostExpired()
    {
        // not expired
        mockLicense.setEvaluation(true);
        mockLicense.setExpiryDate(tenDaysFromNow);
        assertNull(licenseDetails.getLicenseStatusMessage(fred, ""));
    }

    @Test
    public void testGetLicenseStatusMessageNewBuildOldLicenseForExpiredLicense()
    {
        // user: exists, license: commercial, expired: yes
        fred = new MockUser("fred", "Fred", "fred@example.com");
        mockLicense.setExpiryDate(tenSecondsBeforeNow);
        mockLicense.setMaintenanceExpiryDate(tenSecondsBeforeNow);
        setLicenseExtenstionTimestamp(fiftyDaysAgoInMillis);

        String message = licenseDetails.getLicenseStatusMessage(fred, "");

        assertTrue(message.contains("admin.license.nbol.current.version"));
        assertTrue(message.contains("admin.license.nbol.support.and.updates"));
        assertTrue(message.contains("admin.license.renew.for.support.and.updates"));
        assertTrue(message.contains("admin.license.nbol.evaluation.period.has.expired"));
    }

    @Test
    public void testGetLicenseStatusMessageNewBuildOldLicenseForNonExpiredPersonalLicense()
    {
        mockLicense.setLicenseType(LicenseType.PERSONAL);
        mockLicense.setMaintenanceExpiryDate(tenSecondsBeforeNow);
        setLicenseExtenstionTimestamp(tenDaysAgoInMillis);

        // user: doesnt exist, mockLicense: self renewable, expired: no
        String message = licenseDetails.getLicenseStatusMessage(fred, "");
        assertTrue(message.contains("admin.license.nbol.current.version.user.unknown"));
        assertTrue(message.contains("admin.license.nbol.updates.only"));
        assertTrue(message.contains("admin.license.renew.for.updates.only"));
        assertTrue(message.contains("admin.license.nbol.evaluation.period.will.expire.in"));
    }

    @Test
    public void testGetLicenseStatusMessageNewBuildOldLicenseForNonExpiredNonProfitLicense()
    {
        mockLicense.setLicenseType(LicenseType.NON_PROFIT);
        mockLicense.setMaintenanceExpiryDate(tenSecondsBeforeNow);
        setLicenseExtenstionTimestamp(tenDaysAgoInMillis);
        // user: doesnt exist, mockLicense: other/deprecated, expired: no
        String message = licenseDetails.getLicenseStatusMessage(fred, "");
        assertTrue(message.contains("admin.license.nbol.current.version.user.unknown"));
        assertTrue(message.contains("admin.license.nbol.updates.only"));
        assertTrue(message.contains("admin.license.renew.for.deprecated"));
        assertTrue(message.contains("admin.license.nbol.evaluation.period.will.expire.in"));
    }

    @Test
    public void testGetLicenseStatusMessageForMaintenanceExpiredLicense() throws Exception
    {
        mockLicense.setMaintenanceExpired(true);
        mockLicense.setMaintenanceExpiryDate(tenSecondsBeforeNow);

        String message = licenseDetails.getLicenseStatusMessage(fred, "");
        assertTrue(message.contains("admin.license.support.and.updates.has.ended"));
        assertTrue(message.contains("admin.license.renew.for.support.and.updates"));
    }

    @Test
    public void testGetLicenseStatusMessageNormalForMaintenanceExpiredPersonalLicense() throws Exception
    {
        mockLicense.setLicenseType(LicenseType.PERSONAL);
        mockLicense.setMaintenanceExpired(true);
        mockLicense.setMaintenanceExpiryDate(tenSecondsBeforeNow);

        String message = licenseDetails.getLicenseStatusMessage(fred, "");
        assertTrue(message.contains("admin.license.updates.only.has.ended"));
        assertTrue(message.contains("admin.license.renew.for.updates.only"));
    }

    @Test
    public void testGetLicenseStatusMessageNormalForMaintenanceExpiredNonProfitLicense() throws Exception
    {
        mockLicense.setLicenseType(LicenseType.NON_PROFIT);
        mockLicense.setMaintenanceExpired(true);
        mockLicense.setMaintenanceExpiryDate(tenSecondsBeforeNow);

        String message = licenseDetails.getLicenseStatusMessage(fred, "");
        assertTrue(message.contains("admin.license.updates.only.has.ended"));
        assertTrue(message.contains("admin.license.renew.for.deprecated"));
    }

    @Test
    public void testGetLicenseStatusMessageForAlmostMaintenanceExpiredLicense() throws Exception
    {
        mockLicense.setMaintenanceExpiryDate(tenSecondsFromNow);
        String message = licenseDetails.getLicenseStatusMessage(fred, "");
        assertTrue(message.contains("admin.license.support.and.updates.will.end"));
        assertTrue(message.contains("admin.license.renew.for.support.and.updates.after"));
    }

    @Test
    public void testGetLicenseStatusMessageForAlmostMaintenanceExpiredPersonalLicense() throws Exception
    {
        mockLicense.setLicenseType(LicenseType.PERSONAL);
        mockLicense.setMaintenanceExpiryDate(tenSecondsFromNow);
        String message = licenseDetails.getLicenseStatusMessage(fred, "");
        assertTrue(message.contains("admin.license.updates.only.will.end"));
        assertTrue(message.contains("admin.license.renew.for.updates.only.after"));
    }

    @Test
    public void testGetLicenseStatusMessageForAlmostMaintenanceExpiredNonProfitLicense() throws Exception
    {
        mockLicense.setLicenseType(LicenseType.NON_PROFIT);
        mockLicense.setMaintenanceExpiryDate(tenSecondsFromNow);

        String message = licenseDetails.getLicenseStatusMessage(fred, "");
        assertTrue(message.contains("admin.license.updates.only.will.end"));
        assertTrue(message.contains("admin.license.renew.for.deprecated.after"));
    }

    @Test
    public void testGetLicenseStatusMessageForMaintenanceDefinitelyNotExpired() throws Exception
    {
        mockLicense.setNumberOfDaysBeforeMaintenanceExpiry(50);
        assertNull(licenseDetails.getLicenseStatusMessage(fred, ""));
    }

    @Test
    public void testIsEntitledToSupport() throws Exception
    {
        assertSupportEntitlement(LicenseType.ACADEMIC, true);
        assertSupportEntitlement(LicenseType.COMMUNITY, true);
        assertSupportEntitlement(LicenseType.COMMERCIAL, true);
        assertSupportEntitlement(LicenseType.DEMONSTRATION, false);
        assertSupportEntitlement(LicenseType.DEVELOPER, true);
        assertSupportEntitlement(LicenseType.HOSTED, true);
        assertSupportEntitlement(LicenseType.NON_PROFIT, false);
        assertSupportEntitlement(LicenseType.OPEN_SOURCE, true);
        assertSupportEntitlement(LicenseType.PERSONAL, false);
        assertSupportEntitlement(LicenseType.STARTER, true);
        assertSupportEntitlement(LicenseType.TESTING, false);
    }

    @Test
    public void testIsAlmostExpiredEvaluation() throws Exception
    {
        mockLicense.setEvaluation(true);
        mockLicense.setExpiryDate(tenSecondsFromNow);
        assertTrue(licenseDetails.isLicenseAlmostExpired());
    }

    @Test
    public void testIsNotAlmostExpiredEvaluation() throws Exception
    {
        mockLicense.setEvaluation(true);
        mockLicense.setExpiryDate(tenDaysFromNow);
        assertFalse(licenseDetails.isLicenseAlmostExpired());
    }

    @Test
    public void testIsAlmostExpiredNewBuildOldLicense() throws Exception
    {
        mockLicense.setMaintenanceExpiryDate(tenSecondsBeforeNow);
        setLicenseExtenstionTimestamp(twentyFourDaysAgo.getTime());
        assertTrue(licenseDetails.isLicenseAlmostExpired());
    }

    @Test
    public void testIsNotAlmostExpiredNewBuildOldLicense() throws Exception
    {
        mockLicense.setMaintenanceExpiryDate(tenSecondsBeforeNow);
        setLicenseExtenstionTimestamp(tenDaysAgoInMillis);
        assertFalse(licenseDetails.isLicenseAlmostExpired());
    }

    @Test
    public void testGetLicenseVersion() throws Exception
    {
        mockLicense.setLicenseVersion(666);
        assertEquals(666, licenseDetails.getLicenseVersion());
    }

    private void assertSupportEntitlement(final LicenseType licenceType, final boolean expectedEntitlement)
    {
        mockLicense.setLicenseType(licenceType);
        assertEquals(licenceType.name(), expectedEntitlement, licenseDetails.isEntitledToSupport());
    }

    private void setLicenseExtenstionTimestamp(final long timestamp)
    {
        applicationProperties.setOption(APKeys.JIRA_CONFIRMED_INSTALL_WITH_OLD_LICENSE, true);
        applicationProperties.setString(APKeys.JIRA_CONFIRMED_INSTALL_WITH_OLD_LICENSE_TIMESTAMP, String.valueOf(timestamp));
    }

    @Test
    public void clusteringForScaleShouldBeOffByDefault()
    {
        assertFalse("Clustering for scale should default to off", licenseDetails.isDataCenter());
    }

    @Test
    public void clusteringForScaleShouldBeOnWhenPropertySetToTrue()
    {
        // Set up
        mockLicense.setProperty(DATACENTER_PROPERTY_NAME, ENABLED);

        // Invoke and check
        assertTrue("Clustering for scale should be on", licenseDetails.isDataCenter());
    }

    @Test
    public void clusteringForScaleShouldBeOffWhenPropertySetToFalse()
    {
        // Set up
        mockLicense.setProperty(DATACENTER_PROPERTY_NAME, "false");

        // Invoke and check
        assertFalse("Clustering for scale should be off", licenseDetails.isDataCenter());
    }

    @Test
    public void getDaysToExpiryOnEvaluationLicense()
    {
        mockLicense.setExpiryDate(tenDaysFromNow);
        mockLicense.setEvaluation(true);

        //License expires in exactly 10 days.
        assertEquals(10, licenseDetails.getDaysToLicenseExpiry());

        //License expires in exactly 10 days + 1 ms.
        mockLicense.setExpiryDate(new Date(tenDaysFromNow.getTime() + 1));
        assertEquals(10, licenseDetails.getDaysToLicenseExpiry());

        //License expires in exactly 10 days - 1 ms.
        mockLicense.setExpiryDate(new Date(tenDaysFromNow.getTime() - 1));
        assertEquals(9, licenseDetails.getDaysToLicenseExpiry());

        //License expires now.
        mockLicense.setExpiryDate(now);
        assertEquals(0, licenseDetails.getDaysToLicenseExpiry());

        //License expires 1ms into the future.
        mockLicense.setExpiryDate(new Date(now.getTime() + 1));
        assertEquals(0, licenseDetails.getDaysToLicenseExpiry());

        //License expired 1ms ago.
        mockLicense.setExpiryDate(new Date(now.getTime() - 1));
        assertEquals(-1, licenseDetails.getDaysToLicenseExpiry());

        //License expired a long time ago.
        mockLicense.setExpiryDate(twentyFourDaysAgo);
        mockLicense.setEvaluation(true);

        //License expired exactly 24 days ago.
        assertEquals(-24, licenseDetails.getDaysToLicenseExpiry());

        //License expired exactly 1ms later than 24 days ago.
        mockLicense.setExpiryDate(new Date(twentyFourDaysAgo.getTime() + 1));
        assertEquals(-24, licenseDetails.getDaysToLicenseExpiry());

        //License expired exactly 1ms earlier than 24 days ago.
        mockLicense.setExpiryDate(new Date(twentyFourDaysAgo.getTime() - 1));
        assertEquals(-25, licenseDetails.getDaysToLicenseExpiry());
    }

    @Test
    public void getDaysToExpiryOnLicenseExtension()
    {
        //Make sure JIRA is way out of maintenance.
        when(buildUtilsInfo.getCurrentBuildDate()).thenReturn(fiftyDaysFromNow);

        //We said continue under eval 10 days ago.
        setLicenseExtenstionTimestamp(tenDaysAgoInMillis);
        //This leaves 20 days of eval left.
        assertEquals(20, licenseDetails.getDaysToLicenseExpiry());

        //We said continue under eval 10 days + 1 ms ago.
        setLicenseExtenstionTimestamp(tenDaysAgoInMillis + 1);
        //This leaves 20 days of eval left.
        assertEquals(20, licenseDetails.getDaysToLicenseExpiry());

        //We said continue under eval 10 days - 1 ms ago.
        setLicenseExtenstionTimestamp(tenDaysAgoInMillis - 1);
        //This leaves 19 days of eval left.
        assertEquals(19, licenseDetails.getDaysToLicenseExpiry());

        //We said continue under eval 30 days ago.
        setLicenseExtenstionTimestamp(thirtyDaysAgoInMillis);
        //This leaves 0 days of eval left.
        assertEquals(0, licenseDetails.getDaysToLicenseExpiry());

        //We said continue under eval 30 + 1ms days ago.
        setLicenseExtenstionTimestamp(thirtyDaysAgoInMillis + 1);
        //This leaves 0 days of eval left.
        assertEquals(0, licenseDetails.getDaysToLicenseExpiry());

        //We said continue under eval 30 - 1ms days ago.
        setLicenseExtenstionTimestamp(thirtyDaysAgoInMillis - 1);
        //This leaves -1 days of eval left (i.e. it has expired)
        assertEquals(-1, licenseDetails.getDaysToLicenseExpiry());
    }

    @Test
    public void getDaysToExpiryOnPerpetualLicense()
    {
        mockLicense.setExpiryDate(null);
        mockLicense.setEvaluation(false);

        assertEquals(Integer.MAX_VALUE, licenseDetails.getDaysToLicenseExpiry());
    }

    @Test
    public void getDaysToMaintenanceExpiryOnPerpetualLicense()
    {
        mockLicense.setMaintenanceExpiryDate(null);

        assertEquals(Integer.MAX_VALUE, licenseDetails.getDaysToMaintenanceExpiry());
    }

    @Test
    public void getDaysToMaintenanceExpiryOnEvaluationLicense()
    {
        mockLicense.setMaintenanceExpiryDate(tenDaysFromNow);

        //License expires in exactly 10 days.
        assertEquals(10, licenseDetails.getDaysToMaintenanceExpiry());

        //License expires in exactly 10 days + 1 ms.
        mockLicense.setMaintenanceExpiryDate(new Date(tenDaysFromNow.getTime() + 1));
        assertEquals(10, licenseDetails.getDaysToMaintenanceExpiry());

        //License expires in exactly 10 days - 1 ms.
        mockLicense.setMaintenanceExpiryDate(new Date(tenDaysFromNow.getTime() - 1));
        assertEquals(9, licenseDetails.getDaysToMaintenanceExpiry());

        //License expires now.
        mockLicense.setMaintenanceExpiryDate(now);
        assertEquals(0, licenseDetails.getDaysToMaintenanceExpiry());

        //License expires 1ms into the future.
        mockLicense.setMaintenanceExpiryDate(new Date(now.getTime() + 1));
        assertEquals(0, licenseDetails.getDaysToMaintenanceExpiry());

        //License expired 1ms ago.
        mockLicense.setMaintenanceExpiryDate(new Date(now.getTime() - 1));
        assertEquals(-1, licenseDetails.getDaysToMaintenanceExpiry());

        //License expired a long time ago.
        mockLicense.setMaintenanceExpiryDate(twentyFourDaysAgo);

        //License expired exactly 24 days ago.
        assertEquals(-24, licenseDetails.getDaysToMaintenanceExpiry());

        //License expired exactly 24 days - 1ms ago (negative offset)
        mockLicense.setMaintenanceExpiryDate(new Date(twentyFourDaysAgo.getTime() + 1));
        assertEquals(-24, licenseDetails.getDaysToMaintenanceExpiry());

        //License expired exactly 24 days + 1ms ago (negative offset).
        mockLicense.setMaintenanceExpiryDate(new Date(twentyFourDaysAgo.getTime() - 1));
        assertEquals(-25, licenseDetails.getDaysToMaintenanceExpiry());
    }

    private class MockExternalLinkUtil implements ExternalLinkUtil
    {
        public String getPropertiesFilename()
        {
            return null;
        }

        public String getProperty(final String key)
        {
            return null;
        }

        public String getProperty(final String key, final String value1)
        {
            return null;
        }

        public String getProperty(final String key, final String value1, final String value2)
        {
            return null;
        }

        public String getProperty(final String key, final String value1, final String value2, final String value3)
        {
            return null;
        }

        public String getProperty(final String key, final String value1, final String value2, final String value3, final String value4)
        {
            return null;
        }

        public String getProperty(final String key, final Object parameters)
        {
            return null;
        }
    }
}
