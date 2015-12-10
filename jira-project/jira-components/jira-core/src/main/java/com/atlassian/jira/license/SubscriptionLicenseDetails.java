package com.atlassian.jira.license;

import com.atlassian.core.util.Clock;
import com.atlassian.core.util.DateUtils;
import com.atlassian.extras.api.jira.JiraLicense;
import com.atlassian.extras.decoder.api.LicenseDecoder;
import com.atlassian.jira.cluster.ClusterManager;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.datetime.DateTimeFormatter;
import com.atlassian.jira.datetime.DateTimeStyle;
import com.atlassian.jira.util.BuildUtilsInfo;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.web.util.ExternalLinkUtil;
import com.atlassian.jira.web.util.OutlookDate;

import java.util.Date;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * <p> Subclass of {@link DefaultLicenseDetails} for implementing enterprise license-specific behaviour. There are
 * currently 2 kinds of enterprise license: DataCenter (which enables clustering) and ELA (Enterprise License Agreement,
 * which allows use of all Atlassian products). Both of these are issued on a subscription basis, and behave differently
 * on expiry. </p> <p> As the name suggests, licenses represented by this class have a finite expiry date, representing
 * the end of the current subscription period. </p>
 *
 * @see JiraLicenseManager#getLicense()
 * @since v6.3
 */
public class SubscriptionLicenseDetails extends DefaultLicenseDetails
{
    /**
     * Period of time in milliseconds prior to license expiry that this license is considered to be "almost expired".
     */
    static final long WARNING_PERIOD_IN_MSEC = 6L * 7 * 24 * 60 * 60 * 1000; // 6 weeks

    /** Long constant that indicates to
     * {@link DateUtils#dateDifference(long, long, long, java.util.ResourceBundle)} to compare
     * times at hour resolution.
     *
     * @see #getLicenseExpiryStatusMessage
     */
    private static final long HOUR_RESOLUTION = 2;


    /**
     * {@inheritDoc}
     *
     * @throws IllegalArgumentException if {@link JiraLicense#getExpiryDate()} is null (ie: a perpetual license).
     */
    public SubscriptionLicenseDetails(
        @Nonnull JiraLicense license, @Nonnull String licenseString,
        @Nonnull ApplicationProperties applicationProperties,
        @Nonnull final ExternalLinkUtil externalLinkUtil,
        @Nonnull final BuildUtilsInfo buildUtilsInfo,
        @Nonnull I18nHelper.BeanFactory i18nFactory,
        @Nonnull DateTimeFormatter dateTimeFormatter,
        @Nonnull LicenseDecoder licenseDecoder,
        @Nonnull ClusterManager clusterManager,
        @Nonnull Clock clock)
        throws IllegalArgumentException
    {
        super(
            license, licenseString, applicationProperties, externalLinkUtil, buildUtilsInfo,
            i18nFactory, dateTimeFormatter, licenseDecoder, clusterManager, clock
        );

        // subscription licenses cannot have a non-finite expiry date, by definition
        if (license.getExpiryDate() == null)
        {
            throw new IllegalArgumentException(
                    "Subscription licenses must have a finite expiry date; given license is unlimited");
        }
    }

    /**
     * {@inheritDoc}
     * <p/>
     * This is logically similar to {@link DefaultLicenseDetails}, but these kinds of licenses do not have to worry
     * about legacy version 1 licenses so this logic is simpler.
     */
    @Override
    public boolean isExpired()
    {
        return getLicenseExpiry().getTime() < getCurrentTime();
    }

    @Override
    public boolean isLicenseAlmostExpired()
    {
        return getLicenseExpiry().getTime() > getCurrentTime() - WARNING_PERIOD_IN_MSEC;
    }

    @Override
    @Nonnull
    Date getLicenseExpiry()
    {
        return getJiraLicense().getExpiryDate();
    }

    /**
     * Returns an expiry status message; see also {@link #getLicenseExpiryStatusMessage(I18nHelper, OutlookDate)}.
     * @return An expiry status message.
     */
    @Override
    public LicenseStatusMessage getLicenseStatusMessage(@Nonnull I18nHelper i18n)
    {
        Date expiryDate = getJiraLicense().getExpiryDate();

        String expiry = getDateTimeFormatter().withLocale(i18n.getLocale())
            .withStyle(DateTimeStyle.COMPLETE)
            .format(expiryDate);

        if (isExpired())
        {
            return DefaultLicenseStatusMessage.builder()
                // these two are for display on the license setup page when someone tries to upgrade to a too-new JIRA
                .add("admin.license.support.and.updates", i18n.getText("admin.license.nbol.updates.only", expiry))
                .add("admin.license.renewal.target", renewalMessage(i18n))
                // these two are for display in banners around JIRA once the license is expired
                .add("admin.license.status", i18n.getText("admin.enterprise.license.has.expired", expiry))
                .add("admin.license.renewal", renewalMessage(i18n))
                .build();
        }
        else if (isLicenseAlmostExpired())
        {
            return DefaultLicenseStatusMessage.builder()
                .add("admin.license.status", i18n.getText("admin.enterprise.license.has.almost.expired", expiry))
                .add("admin.license.renewal", renewalMessage(i18n))
                .build();
        }
        else
        {
            // license is valid & within expiry
            return DefaultLicenseStatusMessage.builder()
                .add("admin.license.status", i18n.getText("admin.enterprise.license.status", expiry))
                .build();
        }
    }

    /**
     * Returns a duration-based brief expiry message of form "Expires in XX months, YY days".
     *
     * @param date ignored
     */
    @Override
    public String getLicenseExpiryStatusMessage(@Nonnull I18nHelper i18n, @Nullable final OutlookDate date)
    {
        if (isExpired())
        {
            return "(" + i18n.getText("admin.enterprise.license.expired") + ")";
        }
        else
        {
            String timeUntilExpiry = DateUtils.dateDifference(
                System.currentTimeMillis(),
                getJiraLicense().getExpiryDate().getTime(),
                HOUR_RESOLUTION, i18n.getDefaultResourceBundle());

            return i18n.getText("admin.enterprise.license.expiresin", timeUntilExpiry);
        }
    }

    /** Helper method for rendering a license renewal message, and hiding ugly string interpolation. */
    private String renewalMessage(@Nonnull I18nHelper i18n)
    {
        ExternalLinkUtil linkUtil = getExternalLinkUtil();

        String myAtlassianUrl = linkUtil.getProperty("external.link.jira.license.renew");
        String myAtlassianHref = String.format("<a href=\"%s\">", myAtlassianUrl);

        String salesEmail = linkUtil.getProperty("external.link.atlassian.sales.mail.to");
        String salesHrefOpen = String.format("<a href=\"mailto:%s\">", salesEmail);
        String hrefClose = "</a>";

        return i18n.getText(
            "admin.enterprise.license.renewal", myAtlassianHref, hrefClose, salesHrefOpen, hrefClose);
    }
}
