package com.atlassian.jira.license;

import com.atlassian.jira.config.FeatureManager;
import com.atlassian.jira.permission.GlobalPermissionKey;
import com.atlassian.jira.security.GlobalPermissionManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.template.soy.SoyTemplateRendererProvider;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.UserPropertyManager;
import com.atlassian.jira.user.util.Users;
import com.atlassian.jira.web.util.ExternalLinkUtil;
import com.atlassian.soy.renderer.SoyException;
import com.google.common.collect.ImmutableMap;
import com.opensymphony.module.propertyset.PropertySet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;

/**
 * @since v6.3
 */
public class LicenseBannerHelperImpl implements LicenseBannerHelper
{
    private static final Logger LOG = LoggerFactory.getLogger(LicenseBannerHelperImpl.class);

    private static final String EXPIRY_KEY = "license.expiry.remindme";
    private static final String MAINTENANCE_KEY = "license.maintenance.remindme";

    private final JiraAuthenticationContext context;
    private final GlobalPermissionManager globalPermissionManager;
    private final UserPropertyManager propertyManager;
    private final JiraLicenseManager jiraLicenseManager;
    private final SoyTemplateRendererProvider rendererProvider;
    private final ExternalLinkUtil externalLinkUtil;
    private final FeatureManager featureManager;

    public LicenseBannerHelperImpl(final JiraAuthenticationContext context,
            final GlobalPermissionManager globalPermissionManager, UserPropertyManager propertyManager,
            JiraLicenseManager jiraLicenseManager, SoyTemplateRendererProvider rendererProvider,
            ExternalLinkUtil externalLinkUtil, FeatureManager featureManager)
    {
        this.context = context;
        this.globalPermissionManager = globalPermissionManager;
        this.propertyManager = propertyManager;
        this.jiraLicenseManager = jiraLicenseManager;
        this.rendererProvider = rendererProvider;
        this.externalLinkUtil = externalLinkUtil;
        this.featureManager = featureManager;
    }

    @Nonnull
    @Override
    public String getBanner()
    {
        try
        {
            if (!havePermission() || featureManager.isOnDemand())
            {
                return "";
            }

            LicenseDetails license = jiraLicenseManager.getLicense();
            if (ignoreLicense(license))
            {
                return "";
            }

            int daysToLicenseExpiry = license.getDaysToLicenseExpiry();

            //If expiry is less than 45 days then the user is going to be interested about expiry.
            if (daysToLicenseExpiry <= 45)
            {
                //Expiry message is always displayed a week out. It cannot be hidden or dismissed.
                if (daysToLicenseExpiry <= 7)
                {
                    clearHideUntilExpiry();
                    return renderExpiryBanner(daysToLicenseExpiry);
                }
                else if (getHideUntilExpiry() >= daysToLicenseExpiry)
                {
                    //In (7, 45] days the banner can be dismissed. This we only show the banner if the user has
                    //not dismissed it.
                    return renderExpiryBanner(daysToLicenseExpiry);
                }
            }
            else
            {
                //The license is not within 45 days of expiry. Clear out any remind-me settings.
                clearHideUntilExpiry();

                final int daysToMaintenanceExpiry = license.getDaysToMaintenanceExpiry();
                if (daysToMaintenanceExpiry <= 45)
                {
                    //In <= 45 days the banner can be dismissed. This we only show the banner if the user has
                    //not dismissed it.
                    if (getHideUntilMaintenance() >= daysToMaintenanceExpiry)
                    {
                        return renderMaintenanceBanner(daysToMaintenanceExpiry);
                    }
                }
                else
                {
                    //The license is not within 45 days of maintenance expiry. Clear out any remind-me settings.
                    clearHideUtilMaintenance();
                }
            }
        }
        catch (RuntimeException e)
        {
            LOG.debug("Unable to render license header.", e);
            //Don't stop JIRA rendering for some random problem. Fall through and return nothing.
        }
        return "";
    }

    @Override
    public void remindMeLater()
    {
        if (Users.isAnonymous(context.getUser()) || featureManager.isOnDemand())
        {
            return;
        }

        final LicenseDetails license = jiraLicenseManager.getLicense();
        if (ignoreLicense(license))
        {
            clearHideUntilExpiry();
            clearHideUtilMaintenance();
            return;
        }

        final int daysToLicenseExpiry = license.getDaysToLicenseExpiry();

        //If expiry is less than 45 days then the user is going to be interested about expiry (i.e. ignore maintenance)
        if (daysToLicenseExpiry <= 45)
        {
            if (daysToLicenseExpiry <= 7)
            {
                //Can't remind-me later to a expiry <= 7 days.
                clearHideUntilExpiry();
            }
            else if (daysToLicenseExpiry <= 15)
            {
                //If within 15 days, then remind again at 7 days.
                setHideUtilExpiry(7);
            }
            else if (daysToLicenseExpiry <= 30)
            {
                //If within 30 days, then remind again at 15 days.
                setHideUtilExpiry(15);
            }
            else
            {
                //If within 45 days, then remind again at 30 days.
                setHideUtilExpiry(30);
            }
        }
        else
        {
            //Expiry is outside of 45 days, so just forget any remember-me settings.
            clearHideUntilExpiry();

            final int daysToLicenseMaintenanceExpiry = license.getDaysToMaintenanceExpiry();
            //If maintenance expiry is less than 45 days then the user is going to be interested about maintenance.
            if (daysToLicenseMaintenanceExpiry <= 45)
            {
                if (daysToLicenseMaintenanceExpiry <= 0)
                {
                    //Remind 7 days from now if the maintenance has already expired.
                    setHideUtilMaintenance(daysToLicenseMaintenanceExpiry - 7);
                }
                else if (daysToLicenseMaintenanceExpiry <= 7)
                {
                    //Remind on maintenance expiration when within 7 days of it.
                    setHideUtilMaintenance(0);
                }
                else if (daysToLicenseMaintenanceExpiry <= 15)
                {
                    //Remind 7 days from maintenance expiry when within 15 days of it.
                    setHideUtilMaintenance(7);
                }
                else if (daysToLicenseMaintenanceExpiry <= 30)
                {
                    //Remind 15 days from maintenance expiry when within 30 days of it.
                    setHideUtilMaintenance(15);
                }
                else
                {
                    //Remind 30 days from maintenance expiry when within 45 days of it.
                    setHideUtilMaintenance(30);
                }
            }
            else
            {
                //Maintenance expiry is outside of 45 days, so just forget any remember-me settings.
                clearHideUtilMaintenance();
            }
        }
    }

    @Override
    public void remindMeNever()
    {
        if (Users.isAnonymous(context.getUser()) || featureManager.isOnDemand())
        {
            return;
        }

        final LicenseDetails license = jiraLicenseManager.getLicense();
        if (ignoreLicense(license))
        {
            clearHideUntilExpiry();
            clearHideUtilMaintenance();
            return;
        }

        final int daysToLicenseExpiry = license.getDaysToLicenseExpiry();
        //Not possible to force a permanent banner hide when license is about to expire.
        if (daysToLicenseExpiry > 45)
        {
            //Expiry is outside of 45 days, so just forget any remember-me settings.
            clearHideUntilExpiry();

            final int maintenanceExpiry = license.getDaysToMaintenanceExpiry();

            //Permanently hide the maintenance expiry banner if within 45 days.
            if (maintenanceExpiry <= 45)
            {
                setHideUtilMaintenance(Integer.MIN_VALUE);
            }
            else
            {
                clearHideUtilMaintenance();
            }
        }
    }

    @Override
    public void clearRemindMe()
    {
        if (Users.isAnonymous(context.getUser()) || featureManager.isOnDemand())
        {
            return;
        }

        clearHideUntilExpiry();
        clearHideUtilMaintenance();
    }

    private String renderExpiryBanner(int days)
    {
        try
        {
            return rendererProvider.getRenderer().render("jira.webresources:soy-templates",
                    "JIRA.Templates.LicenseBanner.expiryBanner", ImmutableMap.<String, Object>of(
                        "days", days,
                        "mac", getMacUrl(days),
                        "sales", getSalesUrl()
                    ));
        }
        catch (SoyException e)
        {
            LOG.debug("Unable to render banner.", e);
            return "";
        }
    }

    private String renderMaintenanceBanner(int days)
    {
        try
        {
            return rendererProvider.getRenderer().render("jira.webresources:soy-templates",
                    "JIRA.Templates.LicenseBanner.maintenanceBanner", ImmutableMap.<String, Object>of(
                        "days", days,
                        "mac", getMacUrl(days)
                    ));
        }
        catch (SoyException e)
        {
            LOG.debug("Unable to render banner.", e);
            return "";
        }
    }

    private String getSalesUrl()
    {
        return externalLinkUtil.getProperty("external.link.atlassian.sales.mail.to");
    }

    private String getMacUrl(int days)
    {
        StringBuilder builder = new StringBuilder(externalLinkUtil.getProperty("external.link.jira.license.renew"));
        builder.append("?utm_source=jira_banner&utm_medium=renewals_reminder&utm_campaign=");
        if (days <= 7)
        {
            builder.append("renewals_7_reminder");
        }
        else if (days <= 15)
        {
            builder.append("renewals_15_reminder");
        }
        else if (days <= 30)
        {
            builder.append("renewals_30_reminder");
        }
        else
        {
            builder.append("renewals_45_reminder");
        }
        return builder.toString();
    }

    private void setHideUtilExpiry(int days)
    {
        setRemindMeDays(days, EXPIRY_KEY);
    }

    private void setHideUtilMaintenance(final int days)
    {
        setRemindMeDays(days, MAINTENANCE_KEY);
    }

    private void setRemindMeDays(int days, final String key)
    {
        PropertySet propertySet = getPropertySet();
        propertySet.setInt(key, days);
    }

    private void clearHideUntilExpiry()
    {
        clearRemindMeDays(EXPIRY_KEY);
    }

    private void clearHideUtilMaintenance()
    {
        clearRemindMeDays(MAINTENANCE_KEY);
    }

    private void clearRemindMeDays(final String key)
    {
        PropertySet propertySet = getPropertySet();
        if (propertySet != null && propertySet.exists(key))
        {
            propertySet.remove(key);
        }
    }

    private int getHideUntilMaintenance()
    {
        return getRemindMeDays(MAINTENANCE_KEY);
    }

    private int getHideUntilExpiry()
    {
        return getRemindMeDays(EXPIRY_KEY);
    }

    private int getRemindMeDays(final String key)
    {
        PropertySet propertySet = getPropertySet();
        if (propertySet != null && propertySet.exists(key))
        {
            return propertySet.getInt(key);
        }
        else
        {
            return Integer.MAX_VALUE;
        }
    }

    private PropertySet getPropertySet()
    {
        return propertyManager.getPropertySet(context.getUser());
    }

    private boolean ignoreLicense(final LicenseDetails license)
    {
        return !license.isLicenseSet() || license.isEvaluation() || license.isDeveloper();
    }

    private boolean havePermission()
    {
        ApplicationUser user = context.getUser();
        return !Users.isAnonymous(user) && globalPermissionManager.hasPermission(GlobalPermissionKey.ADMINISTER, user);
    }
}
