package com.atlassian.jira.license;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.junit.rules.InitMockitoMocks;
import com.atlassian.jira.mock.MockFeatureManager;
import com.atlassian.jira.mock.security.MockSimpleAuthenticationContext;
import com.atlassian.jira.permission.GlobalPermissionKey;
import com.atlassian.jira.security.GlobalPermissionManager;
import com.atlassian.jira.template.soy.SoyTemplateRendererProvider;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.MockApplicationUser;
import com.atlassian.jira.user.MockUserPropertyManager;
import com.atlassian.jira.web.util.MockExternalLinkUtil;
import com.atlassian.soy.renderer.SoyException;
import com.atlassian.soy.renderer.SoyTemplateRenderer;
import com.google.common.collect.ImmutableMap;
import com.opensymphony.module.propertyset.PropertySet;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

public class LicenseBannerHelperImplTest
{
    private static final String CORRECT_RENDER = "CorrectArguments";

    private static final String EXPIRY_KEY = "license.expiry.remindme";
    private static final String MAINTENANCE_KEY = "license.maintenance.remindme";

    private static final String SALES_URL = "sales";
    private static final String MAC_URL = "my";

    private static final String TEMPLATES_MODULE = "jira.webresources:soy-templates";
    private static final String SUBSCRIPTION_BANNER = "JIRA.Templates.LicenseBanner.expiryBanner";
    private static final String MAINTENANCE_BANNER = "JIRA.Templates.LicenseBanner.maintenanceBanner";
    private static final String MAC_TEMPLATE = "%s?utm_source=jira_banner&utm_medium=renewals_reminder&utm_campaign=renewals_%d_reminder";

    @Rule public InitMockitoMocks init = new InitMockitoMocks(this);

    private ApplicationUser user = new MockApplicationUser("admin");
    private ApplicationUser otherAdmin = new MockApplicationUser("otherUser");

    private MockSimpleAuthenticationContext context = MockSimpleAuthenticationContext.createNoopContext(user);
    @Mock private GlobalPermissionManager globalPermissionManager;
    private MockUserPropertyManager userPropertyManager = new MockUserPropertyManager();
    @Mock private JiraLicenseManager licenseManager;
    @Mock private SoyTemplateRendererProvider provider;
    @Mock private SoyTemplateRenderer renderer;
    private MockExternalLinkUtil linkUtil = new MockExternalLinkUtil();
    private MockLicenseDetails details = new MockLicenseDetails();
    private LicenseBannerHelperImpl helper;
    private MockFeatureManager featureManager = new MockFeatureManager();

    @Before
    public void setup()
    {
        when(globalPermissionManager.hasPermission(GlobalPermissionKey.ADMINISTER, user)).thenReturn(true);
        when(licenseManager.getLicense()).thenReturn(details);
        when(provider.getRenderer()).thenReturn(renderer);

        linkUtil.addLink("external.link.atlassian.sales.mail.to", SALES_URL);
        linkUtil.addLink("external.link.jira.license.renew", MAC_URL);

        userPropertyManager.createOrGetForUser(user);

        helper = new LicenseBannerHelperImpl(context, globalPermissionManager,
                userPropertyManager, licenseManager, provider, linkUtil, featureManager);
    }

    @Test
    public void bannerIsEmptyForAnonymous()
    {
        context.setLoggedInUser((User)null);
        assertThat(helper.getBanner(), Matchers.equalTo(""));
    }

    @Test
    public void bannerIsEmptyInOnDemand()
    {
        featureManager.setOnDemand(true);
        assertThat(helper.getBanner(), Matchers.equalTo(""));
    }

    @Test
    public void bannerIsEmptyForNonAdmin()
    {
        when(globalPermissionManager.hasPermission(GlobalPermissionKey.ADMINISTER, user))
            .thenReturn(false);

        assertThat(helper.getBanner(), Matchers.equalTo(""));
    }

    @Test
    public void bannerIsEmptyWhenLicenseNotSet()
    {
        details.makeUnset();
        assertThat(helper.getBanner(), Matchers.equalTo(""));
    }

    @Test
    public void bannerIsEmptyForEvaulationLicense()
    {
        details.setDaysToLicenseExpiry(10).setEvaluation(true);
        assertThat(helper.getBanner(), Matchers.equalTo(""));
    }

    @Test
    public void bannerIsEmptyForDeveloperLicense()
    {
        details.setDaysToLicenseExpiry(10).setDeveloper(true);
        assertThat(helper.getBanner(), Matchers.equalTo(""));
    }

    @Test
    public void bannerIsEmptyForLicenseWith46DaysToExpiry()
    {
        setExpiryRemindMeForUser(1000);

        details.setDaysToLicenseExpiry(46);
        assertThat(helper.getBanner(), Matchers.equalTo(""));
        assertThat(getExpiryRemindMeForUser(), Matchers.nullValue());
    }

    @Test
    public void bannerIsEmptyForLicenseWith46DaysToMaintenance()
    {
        setMaintenanceRemindMeForUser(1000);

        details.setDaysToMaintenanceExpiry(46);
        assertThat(helper.getBanner(), Matchers.equalTo(""));
        assertThat(getMaintenanceRemindMeForUser(), Matchers.nullValue());
    }

    @Test
    public void bannerDisplayedForLicenseWith45DaysToExpiry()
    {
        assertSubscriptionBanner(45);
    }

    @Test
    public void bannerDisplayedForLicenseWith45DaysToMaintenance()
    {
        assertMaintenanceBanner(45);
    }

    @Test
    public void bannerHiddenForLicenseWith45DaysToExpiryOnRemindMe()
    {
        assertSubscriptionRemindLater(45);
    }

    @Test
    public void bannerHiddenForLicenseWith45DaysToMaintenanceOnRemindMe()
    {
        assertMaintenanceRemindLater(45);
    }

    @Test
    public void bannerDisplayedForLicenseWith30DaysToExpiry()
    {
        assertSubscriptionBanner(30);
    }

    @Test
    public void bannerDisplayedForLicenseWith30DaysToMaintenance()
    {
        assertMaintenanceBanner(30);
    }

    @Test
    public void bannerHiddenForLicenseWith30DaysToExpiryOnRemindMe()
    {
        assertSubscriptionRemindLater(30);
    }

    @Test
    public void bannerHiddenForLicenseWith30DaysToMaintenanceOnRemindMe()
    {
        assertMaintenanceRemindLater(30);
    }

    @Test
    public void bannerDisplayedForLicenseWith15DaysToExpiry()
    {
        assertSubscriptionBanner(15);
    }

    @Test
    public void bannerDisplayedForLicenseWith15DaysToMaintenance()
    {
        assertMaintenanceBanner(15);
    }

    @Test
    public void bannerHiddenForLicenseWith15DaysToMaintenanceOnRemindMe()
    {
        assertMaintenanceRemindLater(15);
    }

    @Test
    public void bannerHiddenForLicenseWith15DaysToExpiryOnRemindMe()
    {
        assertSubscriptionRemindLater(15);
    }

    @Test
    public void bannerDisplayedForLicenseWith7DaysToExpiry()
    {
        assertSubscriptionRemindLaterIgnored(7);
    }

    @Test
    public void bannerDisplayedForLicenseWith7DaysToMaintenance()
    {
        assertMaintenanceBanner(7);
    }

    @Test
    public void bannerHiddenForLicenseWith7DaysToMaintenanceOnRemindMe()
    {
        assertMaintenanceRemindLater(7);
    }

    @Test
    public void bannerDisplayedForLicenseWith0DaysToExpiry()
    {
        assertSubscriptionRemindLaterIgnored(0);
    }

    @Test
    public void bannerDisplayedForLicenseWith0DaysToMaintenance()
    {
        assertMaintenanceBanner(0);
    }

    @Test
    public void bannerHiddenForLicenseWith0DaysToMaintenanceOnRemindMe()
    {
        assertMaintenanceRemindLater(0);
    }

    @Test
    public void bannerDisplayedForLicenseAfterExpiry()
    {
        assertSubscriptionRemindLaterIgnored(-1);
    }

    @Test
    public void bannerDisplayedForLicenseAfterMaintenanceExpired()
    {
        assertMaintenanceBanner(-1);
    }

    @Test
    public void bannerNotDisplayedForLicenseAfterMaintenanceOnRemindMe()
    {
        assertMaintenanceRemindLater(-1);
    }

    @Test
    public void bannerNotDisplayedOnRuntimeException()
    {
        when(provider.getRenderer()).thenThrow(new RuntimeException("What are you doing."));
        assertThat(helper.getBanner(), Matchers.equalTo(""));
    }

    @Test
    public void remindMeLaterIgnoredForAnonymous()
    {
        context.setLoggedInUser((User)null);
        //Just make sure no exception occurs.
        helper.remindMeLater();
    }

    @Test
    public void remindMeIgnoredForOnDemand()
    {
        setExpiryRemindMeForUser(90);
        setMaintenanceRemindMeForUser(90);

        featureManager.setOnDemand(true);
        helper.remindMeLater();

        assertThat(getExpiryRemindMeForUser(), Matchers.equalTo(90));
        assertThat(getMaintenanceRemindMeForUser(), Matchers.equalTo(90));
    }

    @Test
    public void remindMeLaterRemovedForUnsetLicense()
    {
        setExpiryRemindMeForUser(0);
        setMaintenanceRemindMeForUser(1);

        details.makeUnset().setDaysToLicenseExpiry(30);
        helper.remindMeLater();
        assertThat(getExpiryRemindMeForUser(), Matchers.nullValue());
        assertThat(getMaintenanceRemindMeForUser(), Matchers.nullValue());
    }

    @Test
    public void remindMeLaterRemovedEvaluationLicense()
    {
        setExpiryRemindMeForUser(-2);
        setMaintenanceRemindMeForUser(5);

        details.setEvaluation(true).setDaysToLicenseExpiry(30).setDaysToMaintenanceExpiry(30);
        helper.remindMeLater();
        assertThat(getExpiryRemindMeForUser(), Matchers.nullValue());
        assertThat(getMaintenanceRemindMeForUser(), Matchers.nullValue());
    }

    @Test
    public void remindMeLaterRemovedDeveloperLicense()
    {
        setExpiryRemindMeForUser(-2);
        setMaintenanceRemindMeForUser(5);

        details.setDeveloper(true).setDaysToLicenseExpiry(30).setDaysToMaintenanceExpiry(30);
        helper.remindMeLater();
        assertThat(getExpiryRemindMeForUser(), Matchers.nullValue());
        assertThat(getMaintenanceRemindMeForUser(), Matchers.nullValue());
    }

    @Test
    public void remindMeLaterExpiryRemovedAt46Days()
    {
        setExpiryRemindMeForUser(45);
        setMaintenanceRemindMeForUser(0);

        details.setDaysToLicenseExpiry(46);
        helper.remindMeLater();
        assertThat(getExpiryRemindMeForUser(), Matchers.nullValue());
    }

    @Test
    public void remindMeLaterMaintenanceRemovedAt46Days()
    {
        setExpiryRemindMeForUser(45);
        setMaintenanceRemindMeForUser(45);

        details.setDaysToLicenseExpiry(46);
        helper.remindMeLater();
        assertThat(getExpiryRemindMeForUser(), Matchers.nullValue());
        assertThat(getMaintenanceRemindMeForUser(), Matchers.nullValue());
    }

    @Test
    public void remindMeLaterExpirySetTo30At45Days()
    {
        assertRemindMeExpiry(45, 30);
    }


    @Test
    public void remindMeLaterExpirySetTo30At40Days()
    {
        assertRemindMeExpiry(40, 30);
    }

    @Test
    public void remindMeLaterMaintenanceSetTo30At45Days()
    {
        assertRemindMeMaintenance(45, 30);
    }

    @Test
    public void remindMeLaterMaintenanceSetTo30At31Days()
    {
        assertRemindMeMaintenance(31, 30);
    }

    @Test
    public void remindMeLaterExpirySetTo15At30Days()
    {
        assertRemindMeExpiry(30, 15);
    }

    @Test
    public void remindMeLaterExpirySetTo15At25Days()
    {
        assertRemindMeExpiry(25, 15);
    }

    @Test
    public void remindMeLaterMaintenanceSetTo15At30Days()
    {
        assertRemindMeMaintenance(30, 15);
    }

    @Test
    public void remindMeLaterMaintenanceSetTo15At25Days()
    {
        assertRemindMeMaintenance(25, 15);
    }

    @Test
    public void remindMeLaterExpirySetTo7At15Days()
    {
        assertRemindMeExpiry(15, 7);
    }

    @Test
    public void remindMeLaterExpirySetTo7At10Days()
    {
        assertRemindMeExpiry(10, 7);
    }

    @Test
    public void remindMeLaterMaintenanceSetTo7At15Days()
    {
        assertRemindMeMaintenance(15, 7);
    }

    @Test
    public void remindMeLaterMaintenanceSetTo7At13Days()
    {
        assertRemindMeMaintenance(13, 7);
    }

    @Test
    public void remindMeLaterExpiryRemovedAt7Days()
    {
        assertRemindMeExpiryRemoved(7);
    }

    @Test
    public void remindMeLaterMaintenanceSetTo0At7Days()
    {
        assertRemindMeMaintenance(7, 0);
    }

    @Test
    public void remindMeLaterMaintenanceSetTo0At6Days()
    {
        assertRemindMeMaintenance(6, 0);
    }

    @Test
    public void remindMeLaterMaintenanceSetToMinus7At0Days()
    {
        assertRemindMeMaintenance(0, -7);
    }

    @Test
    public void remindMeLaterMaintenanceSetToMinus8AtMinus1Days()
    {
        assertRemindMeMaintenance(-1, -8);
    }

    @Test
    public void remindMeLaterExpiryRemovedAt0Days()
    {
        details.setDaysToLicenseExpiry(0);
        helper.remindMeLater();
        assertThat(getExpiryRemindMeForUser(), Matchers.nullValue());
    }

    @Test
    public void remindMeLaterExpiryRemovedWhenExpired()
    {
        details.setDaysToLicenseExpiry(-1);
        helper.remindMeLater();
        assertThat(getExpiryRemindMeForUser(), Matchers.nullValue());
    }

    @Test
    public void remindMeNeverIgnoredForOnDemand()
    {
        setExpiryRemindMeForUser(90);
        setMaintenanceRemindMeForUser(90);

        featureManager.setOnDemand(true);
        helper.remindMeNever();

        assertThat(getExpiryRemindMeForUser(), Matchers.equalTo(90));
        assertThat(getMaintenanceRemindMeForUser(), Matchers.equalTo(90));
    }

    @Test
    public void remindMeNeverIgnoredAnonymous()
    {
        context.setLoggedInUser((ApplicationUser)null);
        //No exceptions occur.
        helper.remindMeNever();
    }

    @Test
    public void remindMeNeverIgnoredWhenLicenseNotSet()
    {
        setExpiryRemindMeForUser(7);
        setMaintenanceRemindMeForUser(30);
        details.makeUnset();

        helper.remindMeNever();

        assertThat(getExpiryRemindMeForUser(), Matchers.nullValue());
        assertThat(getMaintenanceRemindMeForUser(), Matchers.nullValue());
    }

    @Test
    public void remindMeNeverIgnoredForEvaluationLicense()
    {
        setExpiryRemindMeForUser(7);
        setMaintenanceRemindMeForUser(30);
        details.setEvaluation(true);

        helper.remindMeNever();

        assertThat(getExpiryRemindMeForUser(), Matchers.nullValue());
        assertThat(getMaintenanceRemindMeForUser(), Matchers.nullValue());
    }


    @Test
    public void remindMeNeverIgnoredForDeveloperLicense()
    {
        setExpiryRemindMeForUser(7);
        setMaintenanceRemindMeForUser(30);
        details.setDeveloper(true);

        helper.remindMeNever();

        assertThat(getExpiryRemindMeForUser(), Matchers.nullValue());
        assertThat(getMaintenanceRemindMeForUser(), Matchers.nullValue());
    }

    @Test
    public void remindMeNeverIgnoredForLicenseExpiry()
    {
        setExpiryRemindMeForUser(7);
        setMaintenanceRemindMeForUser(30);

        details.setDaysToLicenseExpiry(15);
        details.setDaysToMaintenanceExpiry(15);

        helper.remindMeNever();
        assertThat(getExpiryRemindMeForUser(), Matchers.equalTo(7));
        assertThat(getMaintenanceRemindMeForUser(), Matchers.equalTo(30));
    }

    @Test
    public void remindMeNeverForLicenseMaintenance()
    {
        setExpiryRemindMeForUser(7);
        setMaintenanceRemindMeForUser(30);

        details.setDaysToMaintenanceExpiry(-1);

        helper.remindMeNever();
        assertThat(getExpiryRemindMeForUser(), Matchers.nullValue());
        assertThat(getMaintenanceRemindMeForUser(), Matchers.equalTo(Integer.MIN_VALUE));
    }

    @Test
    public void remindMeNeverRemovesSettingsForValidLicense()
    {
        setExpiryRemindMeForUser(45);
        setMaintenanceRemindMeForUser(30);

        helper.remindMeNever();
        assertThat(getExpiryRemindMeForUser(), Matchers.nullValue());
        assertThat(getMaintenanceRemindMeForUser(), Matchers.nullValue());
    }

    @Test
    public void clearRemindMeIgnoredForAnonymousUser()
    {
        context.setLoggedInUser((ApplicationUser)null);
        //No exceptions occur.
        helper.clearRemindMe();
    }

    @Test
    public void clearRemindMeIgnoredInOnDemand()
    {
        setExpiryRemindMeForUser(90);
        setMaintenanceRemindMeForUser(90);

        featureManager.setOnDemand(true);
        helper.clearRemindMe();

        assertThat(getExpiryRemindMeForUser(), Matchers.equalTo(90));
        assertThat(getMaintenanceRemindMeForUser(), Matchers.equalTo(90));
    }

    @Test
    public void clearRemindMe()
    {
        setExpiryRemindMeForUser(45);
        setMaintenanceRemindMeForUser(30);

        helper.clearRemindMe();
        assertThat(getExpiryRemindMeForUser(), Matchers.nullValue());
        assertThat(getMaintenanceRemindMeForUser(), Matchers.nullValue());
    }

    private void setExpiryRemindMeForUser(int days)
    {
        userPropertyManager.getPropertySet(user).setInt(EXPIRY_KEY, days);
    }

    private void setMaintenanceRemindMeForUser(int days)
    {
        userPropertyManager.getPropertySet(user).setInt(MAINTENANCE_KEY, days);
    }

    private Integer getExpiryRemindMeForUser()
    {
        return getRemindMeForUser(EXPIRY_KEY);
    }

    private Integer getMaintenanceRemindMeForUser()
    {
        return getRemindMeForUser(MAINTENANCE_KEY);
    }

    private Integer getRemindMeForUser(final String key)
    {
        PropertySet propertySet = userPropertyManager.getPropertySet(user);
        if (propertySet.exists(key))
        {
            return propertySet.getInt(key);
        }
        else
        {
            return null;
        }
    }

    private void mockSubscriptionRender(int days)
    {
        try
        {
            ImmutableMap<String, Object> data = ImmutableMap.<String, Object>of("days", days,
                    "mac", generateMacUrl(days),
                    "sales", SALES_URL);

            when(renderer.render(TEMPLATES_MODULE, SUBSCRIPTION_BANNER, data)).thenReturn(CORRECT_RENDER);
        }
        catch (SoyException e)
        {
            throw new RuntimeException(e);
        }
    }

    private void mockMaintenanceRender(int days)
    {
        try
        {
            ImmutableMap<String, Object> data = ImmutableMap.<String, Object>of("days", days,
                    "mac", generateMacUrl(days));

            when(renderer.render(TEMPLATES_MODULE, MAINTENANCE_BANNER, data)).thenReturn(CORRECT_RENDER);
        }
        catch (SoyException e)
        {
            throw new RuntimeException(e);
        }
    }

    private void assertSubscriptionBanner(final int days)
    {
        setExpiryRemindMeForUser(76);
        setMaintenanceRemindMeForUser(100);
        mockSubscriptionRender(days);

        details.setDaysToLicenseExpiry(days).setDaysToMaintenanceExpiry(1);

        assertThat(helper.getBanner(), Matchers.equalTo(CORRECT_RENDER));
        assertThat(getExpiryRemindMeForUser(), Matchers.equalTo(76));
        assertThat(getMaintenanceRemindMeForUser(), Matchers.equalTo(100));
    }

    private void assertMaintenanceBanner(final int days)
    {
        setExpiryRemindMeForUser(100);
        setMaintenanceRemindMeForUser(76);
        mockMaintenanceRender(days);

        details.setDaysToMaintenanceExpiry(days);
        assertThat(helper.getBanner(), Matchers.equalTo(CORRECT_RENDER));
        assertThat(getExpiryRemindMeForUser(), Matchers.nullValue());
        assertThat(getMaintenanceRemindMeForUser(), Matchers.equalTo(76));
    }

    private void assertSubscriptionRemindLater(final int days)
    {
        setExpiryRemindMeForUser(days - 1);
        details.setDaysToLicenseExpiry(days);
        assertThat(helper.getBanner(), Matchers.equalTo(""));
        assertThat(getExpiryRemindMeForUser(), Matchers.equalTo(days - 1));
        Mockito.verifyZeroInteractions(renderer);
    }

    private void assertMaintenanceRemindLater(final int days)
    {
        setExpiryRemindMeForUser(100);
        setMaintenanceRemindMeForUser(days - 1);
        details.setDaysToMaintenanceExpiry(days);
        assertThat(helper.getBanner(), Matchers.equalTo(""));
        assertThat(getExpiryRemindMeForUser(), Matchers.nullValue());
        assertThat(getMaintenanceRemindMeForUser(), Matchers.equalTo(days - 1));
    }

    private void assertSubscriptionRemindLaterIgnored(final int days)
    {
        setExpiryRemindMeForUser(0);
        mockSubscriptionRender(days);

        details.setDaysToLicenseExpiry(days);
        assertThat(helper.getBanner(), Matchers.equalTo(CORRECT_RENDER));
        assertThat(getExpiryRemindMeForUser(), Matchers.nullValue());
    }

    private void assertRemindMeExpiry(int days, int expectedRemindMe)
    {
        details.setDaysToLicenseExpiry(days).setDaysToMaintenanceExpiry(0);
        setMaintenanceRemindMeForUser(0);

        helper.remindMeLater();
        assertThat(getExpiryRemindMeForUser(), Matchers.equalTo(expectedRemindMe));
        assertThat(getMaintenanceRemindMeForUser(), Matchers.equalTo(0));
    }

    private void assertRemindMeExpiryRemoved(int days)
    {
        details.setDaysToLicenseExpiry(days).setDaysToMaintenanceExpiry(0);
        setMaintenanceRemindMeForUser(0);

        helper.remindMeLater();
        assertThat(getExpiryRemindMeForUser(), Matchers.nullValue());
        assertThat(getMaintenanceRemindMeForUser(), Matchers.equalTo(0));
    }

    private void assertRemindMeMaintenance(final int days, final int nextRemind)
    {
        details.setDaysToMaintenanceExpiry(days);
        setExpiryRemindMeForUser(0);

        helper.remindMeLater();
        assertThat(getExpiryRemindMeForUser(), Matchers.nullValue());
        assertThat(getMaintenanceRemindMeForUser(), Matchers.equalTo(nextRemind));
    }

    private String generateMacUrl(int days)
    {
        int dayCategory;
        if (days <= 7)
        {
            dayCategory = 7;
        }
        else if (days <= 15)
        {
            dayCategory = 15;
        }
        else if (days <= 30)
        {
            dayCategory = 30;
        }
        else if (days <= 45)
        {
            dayCategory = 45;
        }
        else
        {
            throw new IllegalArgumentException();
        }
        return String.format(MAC_TEMPLATE, MAC_URL, dayCategory);
    }
}