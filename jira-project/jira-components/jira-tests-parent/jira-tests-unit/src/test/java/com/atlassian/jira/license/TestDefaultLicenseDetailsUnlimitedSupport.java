package com.atlassian.jira.license;

import com.atlassian.extras.api.LicenseType;
import com.atlassian.extras.api.jira.JiraLicense;
import com.atlassian.extras.decoder.v2.Version2LicenseDecoder;
import com.atlassian.jira.cluster.ClusterManager;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.datetime.DateTimeFormatter;
import com.atlassian.jira.datetime.DateTimeStyle;
import com.atlassian.jira.junit.rules.InitMockitoMocks;
import com.atlassian.jira.util.BuildUtilsInfo;
import com.atlassian.jira.util.ConstantClock;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.web.util.ExternalLinkUtil;
import com.atlassian.jira.web.util.OutlookDate;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.Locale;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;


/**
 * @since v6.0
 */
public class TestDefaultLicenseDetailsUnlimitedSupport
{
    @Rule public final TestRule initMocks = new InitMockitoMocks(this);

    @Mock private DefaultLicenseDetails licenseDetails;
    @Mock private JiraLicense license;
    @Mock private ApplicationProperties applicationProperties;
    @Mock private ExternalLinkUtil externalLinkUtil;
    @Mock private BuildUtilsInfo buildUtilsInfo;
    @Mock private I18nHelper.BeanFactory i18nFactory;
    @Mock private DateTimeFormatter dateTimeFormatter;
    @Mock private OutlookDate outlookDate;
    @Mock private I18nHelper i18Helper;
    @Mock private ClusterManager clusterManager;

    private final String localisedUnlimitedMessage = "aaBBcc1234";

    @Before
    public void setUp() throws Exception
    {
        licenseDetails = new DefaultLicenseDetails(
            license, "licenseStr", applicationProperties, externalLinkUtil, buildUtilsInfo,
            i18nFactory, dateTimeFormatter, new Version2LicenseDecoder(),
            clusterManager, new ConstantClock(10));

        when(dateTimeFormatter.withLocale(any(Locale.class))).thenReturn(dateTimeFormatter);
        when(dateTimeFormatter.withStyle(any(DateTimeStyle.class))).thenReturn(dateTimeFormatter);
        when(i18Helper.getText("common.words.unlimited")).thenReturn(localisedUnlimitedMessage);
        when(i18Helper.getText(anyString(), anyString())).thenAnswer(new Answer<Object>()
        {
            @Override
            public Object answer(final InvocationOnMock invocation) throws Throwable
            {
                return "localised "
                    + invocation.getArguments()[0].toString()
                    + " with: " + invocation.getArguments()[1].toString();
            }
        });
    }

    @Test
    public void getMaintenanceEndStringShouldReturnUnlimitedForNullSupportExpiryDate()
    {
        final String maintenanceEndString = licenseDetails.getMaintenanceEndString(outlookDate);
        Assert.assertThat(maintenanceEndString, CoreMatchers.equalTo("Unlimited"));
    }

    @Test
    public void getLicenseExpiryStatusMessageShouldReturnLocalizedUnlimitedForNullSupportExpiryDate()
    {
        final String statusMessage = licenseDetails.getLicenseExpiryStatusMessage(i18Helper, null);
        Assert.assertThat(statusMessage, CoreMatchers.containsString(localisedUnlimitedMessage));
    }

    @Test
    public void getLicenseExpiryStatusMessageShouldReturnLocalizedUnlimitedForNullSupportExpiryDateWhenEntitledToSupport()
    {
        when(license.getLicenseType()).thenReturn(LicenseType.NON_PROFIT);
        final String statusMessage = licenseDetails.getLicenseExpiryStatusMessage(i18Helper, null);
        Assert.assertThat(statusMessage, CoreMatchers.containsString(localisedUnlimitedMessage));
    }
}
