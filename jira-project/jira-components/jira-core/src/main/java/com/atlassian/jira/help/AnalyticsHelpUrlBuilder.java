package com.atlassian.jira.help;

import com.atlassian.jira.license.JiraLicenseManager;
import com.atlassian.jira.license.LicenseDetails;
import com.atlassian.jira.util.BuildUtilsInfo;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;

import java.util.Map;
import javax.annotation.Nonnull;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * @since v6.2.4
 */
public class AnalyticsHelpUrlBuilder extends HelpUrlBuilderTemplate
{
    private static final String OD_EVAL_LICENSE_CAMPAIGN = "qAwr63Ru";
    private static final String OD_STARTER_LICENSE_CAMPAIGN = "nEJAsw6b";
    private static final String OD_FULL_LICENSE_CAMPAIGN = "r4BuneYU";
    private static final String OD_ENTERPRISE_LICENSE_CAMPAIGN = "yuX3vawa";

    private final JiraLicenseManager licenseManager;

    @VisibleForTesting
    AnalyticsHelpUrlBuilder(String prefix, String suffix, final JiraLicenseManager licenseManager)
    {
        super(prefix, suffix);
        this.licenseManager = notNull("licenseManager", licenseManager);
    }

    @Nonnull
    @Override
    Map<String, String> getExtraParameters()
    {
        final String licenseType = getLicenseType();
        if (licenseType != null)
        {
            return ImmutableMap.of("utm_campaign", licenseType, "utm_medium", "navbar", "utm_source", "inproduct");
        }
        else
        {
            return ImmutableMap.of();
        }
    }

    @Override
    HelpUrlBuilder newInstance()
    {
        return new AnalyticsHelpUrlBuilder(getPrefix(), getSuffix(), licenseManager);
    }

    private String getLicenseType()
    {
        final LicenseDetails licenseDetails = licenseManager.getLicense();
        if (licenseDetails == null)
        {
            return null;
        }
        if (licenseDetails.isEvaluation())
        {
            return OD_EVAL_LICENSE_CAMPAIGN;
        }
        else
        {
            final int maxUsers = licenseDetails.getMaximumNumberOfUsers();
            if (maxUsers == 10)
            {
                return OD_STARTER_LICENSE_CAMPAIGN;
            }
            else if (maxUsers > 500 || licenseDetails.isUnlimitedNumberOfUsers())
            {
                return OD_ENTERPRISE_LICENSE_CAMPAIGN;
            }
            else if (maxUsers > 10)
            {
                return OD_FULL_LICENSE_CAMPAIGN;
            }
            else
            {
                return null;
            }
        }
    }

    public static class Factory extends HelpUrlBuilderFactoryTemplate
    {
        private final JiraLicenseManager licenseManager;

        public Factory(final BuildUtilsInfo info, final JiraLicenseManager licenseManager)
        {
            super(info);
            this.licenseManager = notNull("licenseManager", licenseManager);
        }

        @Override
        HelpUrlBuilder newUrlBuilder(final String prefix, final String suffix)
        {
            return new AnalyticsHelpUrlBuilder(prefix, suffix, licenseManager);
        }
    }
}
