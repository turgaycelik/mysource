package com.atlassian.jira.license;

import com.atlassian.core.util.Clock;
import com.atlassian.extras.api.AtlassianLicense;
import com.atlassian.extras.api.LicenseManager;
import com.atlassian.extras.api.Product;
import com.atlassian.extras.api.jira.JiraLicense;
import com.atlassian.extras.common.LicenseException;
import com.atlassian.extras.decoder.api.LicenseDecoder;
import com.atlassian.jira.cluster.ClusterManager;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.datetime.DateTimeFormatter;
import com.atlassian.jira.util.BuildUtilsInfo;
import com.atlassian.jira.util.ConstantClock;
import com.atlassian.jira.web.bean.I18nBean;
import com.atlassian.jira.web.util.ExternalLinkUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Date;

import static com.atlassian.extras.api.Product.JIRA;
import static com.atlassian.jira.license.DefaultLicenseDetails.DATACENTER_PROPERTY_NAME;
import static com.atlassian.jira.license.DefaultLicenseDetails.ELA_PROPERTY_NAME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TestLicenseDetailsFactoryImpl    
{
    private static final String GOOD_LICENSE = "A Good License";
    private static final String BAD_LICENSE = "A Bad License";

    @Mock private ApplicationProperties applicationProperties;
    @Mock private AtlassianLicense atlassianLicense;
    @Mock private BuildUtilsInfo buildUtilsInfo;
    @Mock private DateTimeFormatter dateTimeFormatter;
    @Mock private ExternalLinkUtil externalLinkUtil;
    @Mock private I18nBean.BeanFactory i18Factory;
    @Mock private LicenseDecoder decoder;
    @Mock private LicenseManager licenseManager;
    @Mock private ClusterManager clusterManager;
    @Mock private JiraLicense jiraLicense;
    private Clock clock = new ConstantClock(10);
    private LicenseDetailsFactoryImpl licenseDetailsFactory;

    @Before
    public void setup()
    {
        licenseDetailsFactory = new LicenseDetailsFactoryImpl(
            applicationProperties, externalLinkUtil, buildUtilsInfo,
            i18Factory, dateTimeFormatter, decoder, licenseManager, clusterManager, clock);
    }

    @Test
    public void testDecodeFailure()
    {
        when(licenseManager.getLicense(BAD_LICENSE)).thenThrow(new LicenseException());
        assertFalse(licenseDetailsFactory.isDecodeable(BAD_LICENSE));
    }

    @Test
    public void testDecodeBlank()
    {
        assertFalse(licenseDetailsFactory.isDecodeable(""));
    }

    @Test
    public void testDecodeNull()
    {
        assertFalse(licenseDetailsFactory.isDecodeable(null));
    }

    @Test
    public void testDecodeValid()
    {
        when(licenseManager.getLicense(GOOD_LICENSE)).thenReturn(atlassianLicense);
        when(atlassianLicense.getProductLicense(JIRA)).thenReturn(jiraLicense);

        assertTrue(licenseDetailsFactory.isDecodeable(GOOD_LICENSE));
    }

    @Test
    public void testGetNull()
    {
        LicenseDetails details = licenseDetailsFactory.getLicense(null);
        assertEquals(NullLicenseDetails.NULL_LICENSE_DETAILS, details);
    }


    @Test
    public void testEnterpriseLicenseCreation()
    {
        // ensure we get our mocked JiraLicense out
        when(licenseManager.getLicense(GOOD_LICENSE)).thenReturn(atlassianLicense);
        when(atlassianLicense.getProductLicense(JIRA)).thenReturn(jiraLicense);

        // ensure mocked license returns the properties that flag it as a subscription license
        when(jiraLicense.getProperty(DATACENTER_PROPERTY_NAME)).thenReturn("true");
        when(jiraLicense.getProperty(ELA_PROPERTY_NAME)).thenReturn("true");

        // ensure license has a finite expiry date (a pre-requisite of being a subscription license)
        when(jiraLicense.getExpiryDate()).thenReturn(new Date());

        final LicenseDetails license = licenseDetailsFactory.getLicense(GOOD_LICENSE);

        assertTrue("Returned license is a subscription-based license",
            license instanceof SubscriptionLicenseDetails);
    }
}
