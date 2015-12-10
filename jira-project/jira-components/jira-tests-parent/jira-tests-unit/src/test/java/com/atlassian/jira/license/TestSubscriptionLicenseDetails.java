package com.atlassian.jira.license;

import com.atlassian.core.util.Clock;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.extras.api.AtlassianLicense;
import com.atlassian.extras.api.LicenseManager;
import com.atlassian.extras.decoder.api.LicenseDecoder;
import com.atlassian.jira.cluster.ClusterManager;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.datetime.DateTimeFormatter;
import com.atlassian.jira.mock.MockApplicationProperties;
import com.atlassian.jira.util.BuildUtilsInfo;
import com.atlassian.jira.util.ConstantClock;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.web.util.ExternalLinkUtil;
import com.atlassian.license.SIDManager;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import static com.atlassian.jira.license.DefaultLicenseDetails.DATACENTER_PROPERTY_NAME;
import static com.atlassian.jira.license.DefaultLicenseDetails.ELA_PROPERTY_NAME;
import static com.atlassian.jira.license.DefaultLicenseDetails.ENABLED;
import static com.atlassian.jira.license.SubscriptionLicenseDetails.WARNING_PERIOD_IN_MSEC;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;


/**
 * Tests {@link com.atlassian.jira.license.SubscriptionLicenseDetails}.
 *
 * @see com.atlassian.jira.license.TestJiraLicenseManagerImpl
 * @since v6.3
 */
@RunWith(MockitoJUnitRunner.class)
public class TestSubscriptionLicenseDetails
{
    private static final String GOOD_LICENSE = "I'm a good license";

    @Mock private JiraLicenseStore licenseStore;
    @Mock private LicenseManager licenseManager;
    @Mock private AtlassianLicense atlassianLicense;
    private MockLicense jiraLicense = new MockLicense();
    @Mock private BuildUtilsInfo buildUtilsInfo;
    private ApplicationProperties applicationProperties = new MockApplicationProperties();
    @Mock private SIDManager sidManager;
    @Mock private EventPublisher eventPublisher;
    @Mock private ExternalLinkUtil externalLinkUtil;
    @Mock private DateTimeFormatter dateTimeFormatter;
    @Mock private I18nHelper.BeanFactory i18nFactory;
    @Mock private LicenseDecoder licenseDecoder;
    @Mock private ClusterManager clusterManager;

    private final long now = 1401234631000L;
    private Clock clock = new ConstantClock(now);

    @Test
    public void testConstructionFailsWithoutFiniteExpiryDate()
    {
        try
        {
            createLicense(null);
            fail("jiraLicense does not have a finite expiry date; should have failed");
        }
        catch (IllegalArgumentException expected)
        {
            // expected
        }
    }

    @Test
    public void testIsDataCenter()
    {
        jiraLicense.setProperty(DATACENTER_PROPERTY_NAME, ENABLED);
        SubscriptionLicenseDetails license = createLicense(now);

        assertTrue("License is a datacenter license", license.isDataCenter());
        assertFalse("License is not an ELA license", license.isEnterpriseLicenseAgreement());
    }

    @Test
    public void testIsELA()
    {
        jiraLicense.setProperty(ELA_PROPERTY_NAME, ENABLED);
        SubscriptionLicenseDetails license = createLicense(now);

        assertTrue("License is an ELA license", license.isEnterpriseLicenseAgreement());
        assertFalse("License is not a datacenter license", license.isDataCenter());
    }

    @Test
    public void testEnterpriseLicensesExpireCorrectly()
    {
        jiraLicense.setProperty(ELA_PROPERTY_NAME, ENABLED);
        jiraLicense.setProperty(DATACENTER_PROPERTY_NAME, ENABLED);
        SubscriptionLicenseDetails license = createLicense(now - 1);

        assertTrue("License is a ELA", license.isEnterpriseLicenseAgreement());
        assertTrue("License enables DataCenter", license.isDataCenter());
        assertTrue("License is expired", license.isExpired());
    }

    @Test
    public void testEnterpriseLicensesAlmostExpireCorrectly()
    {
        // ensure mocked license returns the properties that flag it as a subscription license
        jiraLicense.setProperty(ELA_PROPERTY_NAME, ENABLED);
        jiraLicense.setProperty(DATACENTER_PROPERTY_NAME, ENABLED);
        SubscriptionLicenseDetails license = createLicense(now - WARNING_PERIOD_IN_MSEC + 1);

        assertTrue("License is a ELA", license.isEnterpriseLicenseAgreement());
        assertTrue("License enables DataCenter", license.isDataCenter());
        assertTrue("License is \"almost expired\"", license.isLicenseAlmostExpired());
    }

    @Test
    public void getDaysToExpiry()
    {
        final SubscriptionLicenseDetails license = createLicense(now);
        final long tenDaysFromNow = now + TimeUnit.DAYS.toMillis(10);
        final long twentyFourDaysAgo = now - TimeUnit.DAYS.toMillis(24);

        //License expires in exactly 10 days.
        jiraLicense.setExpiryDate(tenDaysFromNow);
        assertFalse(license.isExpired());
        assertEquals(10, license.getDaysToLicenseExpiry());

        //License expires in exactly 10 days + 1 ms.
        jiraLicense.setExpiryDate(tenDaysFromNow + 1);
        assertFalse(license.isExpired());
        assertEquals(10, license.getDaysToLicenseExpiry());

        //License expires in exactly 10 days - 1 ms.
        jiraLicense.setExpiryDate(tenDaysFromNow -1);
        assertFalse(license.isExpired());
        assertEquals(9, license.getDaysToLicenseExpiry());

        //License expires now.
        jiraLicense.setExpiryDate(now);
        assertFalse(license.isExpired());
        assertEquals(0, license.getDaysToLicenseExpiry());

        //License expires 1ms into the future.
        jiraLicense.setExpiryDate(now + 1);
        assertFalse(license.isExpired());
        assertEquals(0, license.getDaysToLicenseExpiry());

        //License expired 1ms ago.
        jiraLicense.setExpiryDate(now - 1);
        assertTrue(license.isExpired());
        assertEquals(-1, license.getDaysToLicenseExpiry());

        //License expired a long time ago.
        jiraLicense.setExpiryDate(twentyFourDaysAgo);

        //License expired exactly 24 days ago.
        assertEquals(-24, license.getDaysToLicenseExpiry());
        assertTrue(license.isExpired());

        //License expired exactly 24 days - 1ms ago (negative offset)
        jiraLicense.setExpiryDate(twentyFourDaysAgo + 1);
        assertTrue(license.isExpired());
        assertEquals(-24, license.getDaysToLicenseExpiry());

        //License expired exactly 24 days + 1ms ago (negative offset).
        jiraLicense.setExpiryDate(twentyFourDaysAgo - 1);
        assertTrue(license.isExpired());
        assertEquals(-25, license.getDaysToLicenseExpiry());
    }

    @Test
    public void getDaysToExpiryIgnoresAnyEvaluationExtensionInDb()
    {
        final SubscriptionLicenseDetails license = createLicense(now);
        final long tenDaysAgo = now - TimeUnit.DAYS.toMillis(10);
        final long fiftyDaysAgo = now - TimeUnit.DAYS.toMillis(50);

        //Make sure JIRA is way out of maintenance.
        when(buildUtilsInfo.getCurrentBuildDate()).thenReturn(new Date(fiftyDaysAgo));

        //We said continue under eval just now which would normally give an expiry of +30, however under
        //these subscription licenses this is ignored and we just use the license.
        setLicenseExtenstionTimestamp(now);
        jiraLicense.setExpiryDate(tenDaysAgo);
        assertEquals(-10, license.getDaysToLicenseExpiry());
    }

    @Test
    public void getDaysToMaintenanceExpiry()
    {
        final SubscriptionLicenseDetails license = createLicense(now);
        final long tenDaysFromNow = now + TimeUnit.DAYS.toMillis(10);
        final long twentyFourDaysAgo = now - TimeUnit.DAYS.toMillis(24);


        jiraLicense.setMaintenanceExpiryDate(tenDaysFromNow);

        //License expires in exactly 10 days.
        assertEquals(10, license.getDaysToMaintenanceExpiry());

        //License expires in exactly 10 days + 1 ms.
        jiraLicense.setMaintenanceExpiryDate(tenDaysFromNow + 1);
        assertEquals(10, license.getDaysToMaintenanceExpiry());

        //License expires in exactly 10 days - 1 ms.
        jiraLicense.setMaintenanceExpiryDate(tenDaysFromNow - 1);
        assertEquals(9, license.getDaysToMaintenanceExpiry());

        //License expires now.
        jiraLicense.setMaintenanceExpiryDate(now);
        assertEquals(0, license.getDaysToMaintenanceExpiry());

        //License expires 1ms into the future.
        jiraLicense.setMaintenanceExpiryDate(now + 1);
        assertEquals(0, license.getDaysToMaintenanceExpiry());

        //License expired 1ms ago.
        jiraLicense.setMaintenanceExpiryDate(now - 1);
        assertEquals(-1, license.getDaysToMaintenanceExpiry());

        //License expired a long time ago.
        jiraLicense.setMaintenanceExpiryDate(twentyFourDaysAgo);

        //License expired exactly 24 days ago.
        assertEquals(-24, license.getDaysToMaintenanceExpiry());

        //License expired exactly 24 days - 1ms ago (negative offset)
        jiraLicense.setMaintenanceExpiryDate(twentyFourDaysAgo + 1);
        assertEquals(-24, license.getDaysToMaintenanceExpiry());

        //License expired exactly 24 days + 1ms ago (negative offset).
        jiraLicense.setMaintenanceExpiryDate(twentyFourDaysAgo - 1);
        assertEquals(-25, license.getDaysToMaintenanceExpiry());
    }

    private void setLicenseExtenstionTimestamp(final long timestamp)
    {
        applicationProperties.setOption(APKeys.JIRA_CONFIRMED_INSTALL_WITH_OLD_LICENSE, true);
        applicationProperties.setString(APKeys.JIRA_CONFIRMED_INSTALL_WITH_OLD_LICENSE_TIMESTAMP, String.valueOf(timestamp));
    }

    private SubscriptionLicenseDetails createLicense(long expiry)
    {
        return createLicense(new Date(expiry));
    }

    private SubscriptionLicenseDetails createLicense(Date expiry)
    {
        jiraLicense.setExpiryDate(expiry);

        return new SubscriptionLicenseDetails(
            jiraLicense, GOOD_LICENSE, applicationProperties, externalLinkUtil,
            buildUtilsInfo, i18nFactory, dateTimeFormatter, licenseDecoder, clusterManager, clock);
    }
}
