package com.atlassian.jira.help;

import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.license.JiraLicenseManager;
import com.atlassian.jira.license.LicenseDetails;
import com.atlassian.jira.license.MockLicenseDetails;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.mockito.Mock;

import java.util.Collections;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.Mockito.when;

/**
 * @since v6.2.4
 */
public class AnalyticsHelpUrlBuilderTest
{
    private static final String OD_EVAL_LICENSE_CAMPAIGN = "qAwr63Ru";
    private static final String OD_STARTER_LICENSE_CAMPAIGN = "nEJAsw6b";
    private static final String OD_FULL_LICENSE_CAMPAIGN = "r4BuneYU";
    private static final String OD_ENTERPRISE_LICENSE_CAMPAIGN = "yuX3vawa";

    private static final int STARTER = 10;
    private static final int MAX_FULL = 500;

    @Rule
    public RuleChain mocksInContainer = MockitoMocksInContainer.forTest(this);

    @Mock
    private JiraLicenseManager jiraLicenseManager;

    private AnalyticsHelpUrlBuilder builder;

    @Before
    public void setup()
    {
        builder = new AnalyticsHelpUrlBuilder("prefix", "suffix", jiraLicenseManager);
    }

    @Test
    public void createsNewBlankInstance()
    {
        builder.title("ignoreMe");

        final HelpUrlBuilder newBuilder = builder.newInstance();
        assertThat(newBuilder, instanceOf(AnalyticsHelpUrlBuilder.class));

        final HelpUrlBuilder newBuilder2 = builder.newInstance();
        assertThat(newBuilder, instanceOf(AnalyticsHelpUrlBuilder.class));
        assertThat(newBuilder2, not(sameInstance(newBuilder)));
    }

    @Test
    public void getExtraParametersInOdCorrectlyClassifiesTheInstanceType()
    {
        for (LicenseDetails licenseDetails : createLicenses())
        {
            when(jiraLicenseManager.getLicense()).thenReturn(licenseDetails);
            assertThat(builder.getExtraParameters(), matcher(true, licenseDetails));
        }
    }

    @Test
    public void getExtraParametersDoesNotErrorOutWithNoLicense()
    {
        assertThat(builder.getExtraParameters(), equalTo(Collections.<String, String>emptyMap()));
    }

    private Iterable<LicenseDetails> createLicenses()
    {
        return ImmutableList.<LicenseDetails>of(new MockLicenseDetails().setEvaluation(true),
                new MockLicenseDetails().setMaxUsers(STARTER - 1), //nothing
                new MockLicenseDetails().setMaxUsers(STARTER), //starter
                new MockLicenseDetails().setMaxUsers(STARTER + 1), //full
                new MockLicenseDetails().setMaxUsers(MAX_FULL), //full
                new MockLicenseDetails().setMaxUsers(MAX_FULL + 1), //enterprise
                new MockLicenseDetails().setUnlimitedUsers()); //enterprise
    }

    private Matcher<? super Map<String, String>> matcher(boolean od, LicenseDetails details)
    {
        return equalTo(od ? createOdMap(details) : Collections.<String, String>emptyMap());
    }

    private Map<String, String> createOdMap(LicenseDetails details)
    {
        final String type = getType(details);
        if (type == null)
        {
            return Collections.emptyMap();
        }
        else
        {
            return ImmutableMap.of("utm_campaign", type, "utm_medium", "navbar", "utm_source", "inproduct");
        }
    }

    private static String getType(final LicenseDetails details)
    {
        if (details.isEvaluation())
        {
            return OD_EVAL_LICENSE_CAMPAIGN;
        }
        else if (details.getMaximumNumberOfUsers() == STARTER)
        {
            return OD_STARTER_LICENSE_CAMPAIGN;
        }
        else if (details.getMaximumNumberOfUsers() > MAX_FULL || details.isUnlimitedNumberOfUsers())
        {
            return OD_ENTERPRISE_LICENSE_CAMPAIGN;
        }
        else if (details.getMaximumNumberOfUsers() > STARTER)
        {
            return OD_FULL_LICENSE_CAMPAIGN;
        }
        else
        {
            return null;
        }
    }
}
