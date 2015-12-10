package com.atlassian.jira.license;

import com.atlassian.core.util.Clock;
import com.atlassian.core.util.DateUtils;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.extras.api.Contact;
import com.atlassian.extras.api.LicenseType;
import com.atlassian.extras.api.jira.JiraLicense;
import com.atlassian.extras.decoder.api.LicenseDecoder;
import com.atlassian.jira.cluster.ClusterManager;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.datetime.DateTimeFormatter;
import com.atlassian.jira.datetime.DateTimeStyle;
import com.atlassian.jira.user.UserUtils;
import com.atlassian.jira.util.BuildUtilsInfo;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.web.util.ExternalLinkUtil;
import com.atlassian.jira.web.util.OutlookDate;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.opensymphony.util.TextUtils;
import org.apache.log4j.Logger;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Default implementation of the {@link LicenseDetails} interface.
 *
 * @since v3.13
 */
public class DefaultLicenseDetails implements LicenseDetails
{
    /** String value for a license property that indicates that a product or feature is enabled. */
    static final String ENABLED = new String("true");

    /** The license property that enables clustering features when set to {@link #ENABLED}. */
    @VisibleForTesting
    static final String DATACENTER_PROPERTY_NAME = new String("jira.DataCenter");

    /** The license property that signifies that a license is an ELA license when set to {@link #ENABLED}. */
    @VisibleForTesting
    static final String ELA_PROPERTY_NAME = new String("ELA");

    private static final Logger log = Logger.getLogger(DefaultLicenseDetails.class);

    private static final int MAINTENANCE_WARNING_PERIOD_IN_DAYS = 42;
    private static final long GRACE_PERIOD_IN_MILLIS = 30L * 24 * 60 * 60 * 1000; // 30 days

    private final JiraLicense license;
    private final ApplicationProperties applicationProperties;
    private final ExternalLinkUtil externalLinkUtil;
    private final String licenseString;
    private final BuildUtilsInfo buildUtilsInfo;
    private final I18nHelper.BeanFactory i18nFactory;
    private final DateTimeFormatter dateTimeFormatter;
    private final LicenseDecoder licenseDecoder;
    private final ClusterManager clusterManager;
    private final Clock clock;

    /**
     * @param license should never be null - see {@link com.atlassian.jira.license.NullLicenseDetails} instead
     * @param externalLinkUtil external link utils
     * @param clock clock to find the current time.
     */
    DefaultLicenseDetails(
        @Nonnull final JiraLicense license,
        @Nonnull final String licenseString,
        @Nonnull final ApplicationProperties applicationProperties,
        @Nonnull final ExternalLinkUtil externalLinkUtil,
        @Nonnull final BuildUtilsInfo buildUtilsInfo,
        @Nonnull I18nHelper.BeanFactory i18nFactory,
        @Nonnull DateTimeFormatter dateTimeFormatter,
        @Nonnull LicenseDecoder licenseDecoder,
        @Nonnull ClusterManager clusterManager,
        @Nonnull Clock clock)
    {
        this.i18nFactory = i18nFactory;
        this.dateTimeFormatter = dateTimeFormatter;
        this.licenseDecoder = licenseDecoder;
        this.licenseString = notNull("licenseString", licenseString);
        this.license = notNull("license", license);
        this.applicationProperties = notNull("applicationProperties", applicationProperties);
        this.externalLinkUtil = notNull("externalLinkUtil", externalLinkUtil);
        this.buildUtilsInfo = notNull("buildUtilsInfo", buildUtilsInfo);
        this.clusterManager = notNull("clusterManager", clusterManager);
        this.clock = notNull("clock", clock);
    }

    /** Returns true if the given {@link com.atlassian.extras.api.jira.JiraLicense} supports clustering. */
    static boolean isDataCenterLicense(@Nonnull JiraLicense license)
    {
        return ENABLED.equals(license.getProperty(DATACENTER_PROPERTY_NAME));
    }

    /** Returns true if the given {@link com.atlassian.extras.api.jira.JiraLicense} is an ELA (Enterprise License Agreement). */
    static boolean isELA(@Nonnull JiraLicense license)
    {
        return (ENABLED.equals(license.getProperty(ELA_PROPERTY_NAME)));
    }

    /** Returns true if the given {@link com.atlassian.extras.api.jira.JiraLicense} is an "Enterprise" subscription license. */
    static boolean isEnterpriseSubscriptionLicense(@Nonnull JiraLicense license)
    {
        return isDataCenterLicense(license) || isELA(license);
    }

    private boolean isFullLicense()
    {
        return LicenseType.COMMERCIAL.equals(license.getLicenseType()) && !license.isEvaluation();
    }

    @Override
    public boolean isPersonalLicense()
    {
        return LicenseType.PERSONAL.equals(license.getLicenseType());
    }

    private boolean isEvaluationLicense()
    {
        return license.isEvaluation();
    }

    private boolean isAcademicLicense()
    {
        return LicenseType.ACADEMIC.equals(license.getLicenseType());
    }

    private boolean isNonProfitLicense()
    {
        return LicenseType.NON_PROFIT.equals(license.getLicenseType());
    }

    private boolean isCommunityLicense()
    {
        return LicenseType.COMMUNITY.equals(license.getLicenseType());
    }

    private boolean isOpenSourceLicense()
    {
        return LicenseType.OPEN_SOURCE.equals(license.getLicenseType());
    }

    private boolean isDeveloperLicense()
    {
        return LicenseType.DEVELOPER.equals(license.getLicenseType());
    }

    private boolean isDemonstrationLicense()
    {
        return LicenseType.DEMONSTRATION.equals(license.getLicenseType());
    }

    private boolean isCommercialLicense()
    {
        return isFullLicense() || isAcademicLicense() || isEvaluationLicense() || LicenseType.HOSTED.equals(license.getLicenseType());
    }

    private boolean isSelfRenewable()
    {
        return isCommunityLicense() || isOpenSourceLicense() || isDeveloperLicense() || isPersonalLicense();
    }

    private boolean isNonCommercialNonRenewable()
    {
        return isNonProfitLicense() || isDemonstrationLicense() || LicenseType.TESTING.equals(license.getLicenseType());
    }

    private String localisedMaintenanceExpiryDate(final I18nHelper i18n)
    {
        final DateTimeFormatter dmyFormatter = dateTimeFormatter.withLocale(i18n.getLocale()).withStyle(DateTimeStyle.DATE);
        final Date maintenanceExpiryDate = getMaintenanceExpiryDate();
        return maintenanceExpiryDate == null ? i18n.getText("common.words.unlimited") : dmyFormatter.format(maintenanceExpiryDate);
    }

    DateTimeFormatter getDateTimeFormatter()
    {
        return dateTimeFormatter;
    }

    @Override
    public boolean isEntitledToSupport()
    {
        return !(isNonCommercialNonRenewable() || isPersonalLicense());
    }

    /**
     * If the license is Evaluation or Extended (New Build, Old License), returns true if we are within 7 days of the
     * expiry date.
     *
     * @return true if the license is close to expiry; false otherwise.
     * @see {@link #isExpired()}
     */
    @Override
    public boolean isLicenseAlmostExpired()
    {
        if (isEvaluationLicense() || isNewBuildWithOldLicense())
        {
            final Date expiry = getLicenseExpiry();
            return ((expiry != null) && (expiry.getTime() - getCurrentTime() < 7L * DateUtils.DAY_MILLIS));
        }
        return false;
    }

    /**
     * Checks whether the license is either expired for Evaluation or Extended Licenses (New Build, Old License).
     *
     * @return true if has; false otherwise.
     */
    @Override
    public boolean isExpired()
    {
        if (isEvaluationLicense())
        {
            return license.isExpired();
        }
        else if (isNewBuildWithOldLicense())
        {
            return isExtendLicenseExpired();
        }
        return false;
    }

    @Override
    public String getPurchaseDate(final OutlookDate outlookDate)
    {
        return notNull("outlookDate", outlookDate).formatDMY(license.getPurchaseDate());
    }

    @Override
    public boolean isEvaluation()
    {
        boolean isEvaluation = license.isEvaluation();
        // license.isEvaluation() isn't applicable in OD world: this requires the TrialEndDate property to be there
        if (!isEvaluation && !Strings.isNullOrEmpty(license.getProperty("jira.TrialEndDate")))
        {
            isEvaluation = true;
        }

        return isEvaluation;
    }

    @Override
    public boolean isStarter()
    {
        return LicenseType.STARTER.equals(license.getLicenseType());
    }

    @Override
    public boolean isCommercial()
    {
        return LicenseType.COMMERCIAL.equals(license.getLicenseType());
    }

    @Override
    public boolean isCommunity()
    {
        return LicenseType.COMMUNITY.equals(license.getLicenseType());
    }

    @Override
    public boolean isOpenSource()
    {
        return LicenseType.OPEN_SOURCE.equals(license.getLicenseType());
    }

    @Override
    public boolean isNonProfit()
    {
        return LicenseType.NON_PROFIT.equals(license.getLicenseType());
    }

    @Override
    public boolean isDemonstration()
    {
        return LicenseType.DEMONSTRATION.equals(license.getLicenseType());
    }

    @Override
    public boolean isOnDemand()
    {
        return false;
    }


    @Override
    public boolean isDataCenter()
    {
        return isDataCenterLicense(license);
    }

    @Override
    public boolean isEnterpriseLicenseAgreement()
    {
        return isELA(license);
    }

    @Override
    public boolean isDeveloper()
    {
        return LicenseType.DEVELOPER.equals(license.getLicenseType());
    }

    @Override
    public JiraLicense getJiraLicense() {
        return license;
    }

    @Override
    public LicenseRoleDetails getLicenseRoles()
    {
        return new DefaultLicenseRoleDetails(licenseString, licenseDecoder);
    }

    @Override
    public String getOrganisation()
    {
        return license.getOrganisation() == null ? "<Unknown>" : license.getOrganisation().getName();
    }

    @Override
    public boolean hasLicenseTooOldForBuildConfirmationBeenDone()
    {
        return applicationProperties.getOption(APKeys.JIRA_CONFIRMED_INSTALL_WITH_OLD_LICENSE);
    }

    @Override
    public String getLicenseString()
    {
        return licenseString;
    }

    @Override
    public boolean isMaintenanceValidForBuildDate(final Date currentBuildDate)
    {
        return license.getMaintenanceExpiryDate() == null
            || license.getMaintenanceExpiryDate().compareTo(currentBuildDate) >= 0;
    }

    @Override
    public String getSupportEntitlementNumber()
    {
        return license.getSupportEntitlementNumber();
    }

    @Override
    public Collection<LicenseContact> getContacts()
    {
        List<LicenseContact> licenseContacts = Lists.newArrayList();
        for (Contact contact : license.getContacts())
        {
            if (contact != null)
            {
                licenseContacts.add(new DefaultLicenseContact(contact.getName(), contact.getEmail()));
            }
        }
        return licenseContacts;
    }

    @Override
    public int getDaysToLicenseExpiry()
    {
        return getDaysUntilDate(getLicenseExpiry());
    }

    @Override
    public int getDaysToMaintenanceExpiry()
    {
        return getDaysUntilDate(getMaintenanceExpiryDate());
    }

    private int getDaysUntilDate(final Date date)
    {
        if (date == null)
        {
            return Integer.MAX_VALUE;
        }
        else
        {
            final double days = ((double) (date.getTime() - clock.getCurrentDate().getTime())) / TimeUnit.DAYS.toMillis(1);
            return (int)Math.floor(days);
        }
    }

    /**
     * If the license is Evaluation or Extended (New Build, Old License), returns the date when the license will
     * expire.
     *
     * @return a date when this license will "expire", or null if there is no expiry date.
     */
    Date getLicenseExpiry()
    {
        if (isEvaluationLicense())
        {
            return license.getExpiryDate();
        }
        else if (isNewBuildWithOldLicense())
        {
            return getExtendedLicenseExpiry();
        }
        return null;
    }

    private Date getExtendedLicenseExpiry()
    {
        final long installationWithExpiredLicenseDate = Long.parseLong(getConfirmedInstallWithOldLicenseTimestamp());
        return new Date(installationWithExpiredLicenseDate + GRACE_PERIOD_IN_MILLIS);
    }

    private boolean isNewBuildWithOldLicense()
    {
        return !isMaintenanceValidForBuildDate(getCurrentBuildDate()) && hasLicenseTooOldForBuildConfirmationBeenDone();
    }

    private String getTimeUntilExpiry(final I18nHelper i18n)
    {
        return DateUtils.dateDifference(getCurrentTime(), getLicenseExpiry().getTime(), 2, i18n.getDefaultResourceBundle());
    }

    /**
     * @return true if the support period end date has almost passed; false otherwise.
     */
    private boolean isMaintenanceAlmostEnded()
    {
        return license.getNumberOfDaysBeforeMaintenanceExpiry() < MAINTENANCE_WARNING_PERIOD_IN_DAYS;
    }

    /**
     * @return true if the support period end date has passed; false otherwise.
     * @see {@link #getMaintenanceExpiryDate}
     */
    private boolean isMaintenanceExpired()
    {
        return license.isMaintenanceExpired();
    }

    /**
     * Calculates the end of the support period (during which customers are entitled to updates and commercial support)
     *
     * @return the license creation date plus the length of the support period. Return null if maintenance period is
     *         unlimited
     */
    @Nullable
    private Date getMaintenanceExpiryDate()
    {
        return license.getMaintenanceExpiryDate();
    }

    /**
     * Determines if the confirmation of the new build with the old license has occured more than 30 days ago
     *
     * @return true if date confirmed is older than 30 days
     */
    private boolean isExtendLicenseExpired()
    {
        try
        {
            return getCurrentTime() > getExtendedLicenseExpiry().getTime();
        }
        catch (final NumberFormatException e)
        {
            log.debug("The Confirm Install Timestamp does not exist or is in the wrong format.", e);
        }
        return false;
    }

    /**
     * @return the String representation of the time when JIRA was confirmed to be installed with an old license.
     */
    private String getConfirmedInstallWithOldLicenseTimestamp()
    {
        return applicationProperties.getString(APKeys.JIRA_CONFIRMED_INSTALL_WITH_OLD_LICENSE_TIMESTAMP);
    }

    ///CLOVER:OFF
    User getConfirmedUser()
    {
        final String userName = applicationProperties.getString(APKeys.JIRA_CONFIRMED_INSTALL_WITH_OLD_LICENSE_USER);
        User user = null;
        if (userName != null)
        {
            user = UserUtils.getUser(userName);
            if (user == null)
            {
                log.warn("Could not find user [" + userName + "]");
            }
        }
        return user;
    }
    ///CLOVER:ON

    final long getCurrentTime()
    {
        return clock.getCurrentDate().getTime();
    }

    private String getCurrentVersion()
    {
        return buildUtilsInfo.getVersion();
    }

    private Date getCurrentBuildDate()
    {
        return buildUtilsInfo.getCurrentBuildDate();
    }

    @Override
    public String getSupportRequestMessage(com.atlassian.crowd.embedded.api.User user)
    {
        return getSupportRequestMessage(i18nFactory.getInstance(user), null);
    }

    @Override
    public String getSupportRequestMessage(final I18nHelper i18n, @Nullable OutlookDate ignored)
    {
        final StringBuilder msg = new StringBuilder();

        if (isEntitledToSupport())
        {
            if (isExpired())
            {
                msg.append("<p>").append(i18n.getText("admin.supportrequest.success.description")).append("</p>");
                msg.append("<p>").append(i18n.getText("admin.supportrequest.success.supported.expired")).append("</p>");
                msg.append("<p>- ").append(i18n.getText("admin.supportrequest.atlassian.team")).append("</p>");
            }
            else
            {
                final String mailTo = externalLinkUtil.getProperty("external.link.jira.support.mail.to");
                final String mailToUrl = "mailto:" + mailTo;
                msg.append("<p>").append(i18n.getText("admin.supportrequest.success.description")).append("</p>");
                msg.append("<p>").append(i18n.getText("admin.supportrequest.success.supported.next")).append("</p>");
                msg.append("<p>").append(
                        i18n.getText("admin.supportrequest.success.supported.email", "<a href=\"" + mailToUrl + "\">", mailTo, "</a>")).append("</p>");
                msg.append("<p>- ").append(i18n.getText("admin.supportrequest.atlassian.team")).append("</p>");
            }
        }
        else
        {
            final Link urlDocumentation = new Link("external.link.jira.documentation", getCurrentVersion());
            final Link urlForums = new Link("external.link.atlassian.forums");
            final Link urlPurchaseJira = new Link(isPersonalLicense() ? "external.link.jira.personal.site" : "external.link.jira.license.new");
            final List<String> urls = Arrays.asList(urlDocumentation.getStart(), urlDocumentation.getEnd(), urlForums.getStart(), urlForums.getEnd(),
                    urlPurchaseJira.getStart(), urlPurchaseJira.getEnd());

            msg.append("<p>").append(i18n.getText("admin.supportrequest.not.supported", license.getDescription())).append("</p>");
            msg.append("<p>").append(i18n.getText("admin.supportrequest.not.supported.links", urls)).append("</p>");
            msg.append("<p>- ").append(i18n.getText("admin.supportrequest.atlassian.team")).append("</p>");
        }
        return msg.toString();
    }

    @Override
    public String getMaintenanceEndString(final OutlookDate outlookDate)
    {
        final Date end;
        if (isEvaluationLicense() || isNewBuildWithOldLicense())
        {
            end = getLicenseExpiry();
        }
        else
        {
            end = getMaintenanceExpiryDate();
        }
        return end == null ? "Unlimited" : outlookDate.formatDMY(end);
    }

    @Override
    public boolean isUnlimitedNumberOfUsers()
    {
        return license.isUnlimitedNumberOfUsers();
    }

    @Override
    public int getMaximumNumberOfUsers()
    {
        return license.getMaximumNumberOfUsers();
    }

    @Override
    public boolean isLicenseSet()
    {
        return true;
    }

    @Override
    public int getLicenseVersion()
    {
        return license.getLicenseVersion();
    }

    @Override
    public String getDescription()
    {
        return license.getDescription();
    }

    @Override
    public String getPartnerName()
    {
        return license.getPartner() == null ? null : license.getPartner().getName();
    }

    @Override
    public String getLicenseExpiryStatusMessage(@Nullable com.atlassian.crowd.embedded.api.User user)
    {
        return getLicenseExpiryStatusMessage(i18nFactory.getInstance(user), null);
    }

    @Override
    public String getLicenseExpiryStatusMessage(final I18nHelper i18n, @Nullable OutlookDate outlookDate)
    {
        final String msg;
        if (isEvaluationLicense() || isNewBuildWithOldLicense())
        {
            if (isExpired())
            {
                msg = i18n.getText("admin.license.expired");
            }
            else
            {
                final DateTimeFormatter dmyFormatter = dateTimeFormatter.withLocale(i18n.getLocale()).withStyle(DateTimeStyle.DATE);
                msg = i18n.getText("admin.license.expiresin", getTimeUntilExpiry(i18n), dmyFormatter.format(getLicenseExpiry()));
            }
        }
        else if (!isMaintenanceExpired())
        {
            if (isEntitledToSupport())
            {
                msg = i18n.getText("admin.support.available.until", "<b>" + localisedMaintenanceExpiryDate(i18n) + "</b>");
            }
            else
            {
                msg = i18n.getText("admin.upgrades.available.until", "<b>" + localisedMaintenanceExpiryDate(i18n) + "</b>");
            }
        }
        else
        {
            return null;
        }
        return "(" + msg + ")";
    }

    @Override
    public String getBriefMaintenanceStatusMessage(final I18nHelper i18n)
    {
        String msg;
        if (!isEntitledToSupport())
        {
            msg = i18n.getText("admin.license.maintenance.status.unsupported");
        }
        else
        {
            msg = i18n.getText("admin.license.maintenance.status.supported.valid");
            // if eval or new build old license, check license expiry
            if ((isEvaluationLicense() || isNewBuildWithOldLicense()))
            {
                if (isExpired())
                {
                    msg = i18n.getText("admin.license.maintenance.status.supported.expired");
                }
            }
            // otherwise (regular license), check maintenance end date
            else if (isMaintenanceExpired())
            {
                msg = i18n.getText("admin.license.maintenance.status.supported.expired");
            }
        }
        return msg;
    }

    @Override
    public String getLicenseStatusMessage(@Nullable com.atlassian.crowd.embedded.api.User user, String delimiter)
    {
        return getLicenseStatusMessage(i18nFactory.getInstance(user), null, delimiter);
    }

    @Override
    public String getLicenseStatusMessage(final I18nHelper i18n, @Nullable final OutlookDate ignored, final String delimiter)
    {
        final LicenseStatusMessage licenseStatusMessage = getLicenseStatusMessage(i18n);
        if(licenseStatusMessage != null)
            return licenseStatusMessage.getAllMessages(delimiter);
        return null;
    }

    @Override
    public LicenseStatusMessage getLicenseStatusMessage(final I18nHelper i18n)
    {
        final Link urlRenew = new Link("external.link.jira.license.new");
        final Link urlEvalExpired = new Link("external.link.jira.license.expiredeval");
        final Link urlSelfRenew = new Link("external.link.jira.license.renew.noncommercial");
        final Link urlContact = new Link("external.link.jira.license.renew.contact");
        final Link urlWhyRenew = new Link("external.link.jira.license.whyrenew");

        // first case: license is purely an evaluation license
        if (isEvaluationLicense())
        {
            final DateTimeFormatter dmyFormatter = dateTimeFormatter.withLocale(i18n.getLocale()).withStyle(DateTimeStyle.DATE);
            final String licenseExpiry = "<strong>" + TextUtils.htmlEncode(dmyFormatter.format(getLicenseExpiry())) + "</strong>";

            // expired
            if (isExpired())
            {
                // Your JIRA evaluation period expired on <date>. You are not able to create new issues in JIRA.
                //
                // To reactivate JIRA, please _purchase JIRA_ (link to order form).

                return DefaultLicenseStatusMessage.builder()
                    .add("admin.license.evaluation", i18n.getText("admin.license.evaluation.expired", licenseExpiry))
                    .add("admin.license.evaluation.renew", i18n.getText("admin.license.evaluation.expired.renew", urlEvalExpired.getStart(), urlEvalExpired.getEnd()))
                    .build();
            }

            // almost expired
            if (isLicenseAlmostExpired())
            {
                // Your JIRA evaluation period will expire on <date>. You will not be
                // able to create new issues in JIRA.
                //
                // Please consider _purchasing JIRA_ (link to order form).

                return DefaultLicenseStatusMessage.builder()
                    .add("admin.license.evaluation", i18n.getText("admin.license.evaluation.almost.expired", licenseExpiry))
                    .add("admin.license.evaluation.renew",  i18n.getText("admin.license.evaluation.almost.expired.renew", urlEvalExpired.getStart(), urlEvalExpired.getEnd()))
                    .build();
            }

            // license is valid and within its supported period - no message
            return null;
        }

        // second case: license is not evaluation, but the maintenance period has expired, and the current build of JIRA
        // is more recent than the expiry date of the maintenance period of the license
        else if (isNewBuildWithOldLicense())
        {
            final String maintenanceEnd = "<strong>" + TextUtils.htmlEncode(localisedMaintenanceExpiryDate(i18n)) + "</strong>";
            final String extendedDaysLeft = getTimeUntilExpiry(i18n);
            // COMMERCIAL: Your JIRA support and updates for this license have ended on <date>. You are currently running a version of JIRA that was created after that date.
            // OTHER: Your JIRA updates for this license have ended on <date>. You are currently running a version of JIRA that was created after that date.

            // The current version of JIRA (<version>) was installed by <user full name> (<username>)
            // on <date>. As your current license is not valid for this version, your use of JIRA should be
            // considered an evaluation.

            // NOT EXPIRED: Your evaluation period will expire in <timeremaining>. After this date you will not be able to create new issues in JIRA.
            // EXPIRED: Your evaluation period has expired. You are not able to create new issues in JIRA.

            // ENTITLED TO SUPPORT: If you wish to have access to support and updates, please _renew your maintenance_ (link to order form/my.atlassian.com).
            // NOT ENTITLED TO SUPPORT (SELF RENEW): If you wish to have access to updates, please _renew your maintenance_ (link to my.atlassian.com).
            // NOT ENTITLED TO SUPPORT (OTHER): If you wish to have access to support and updates, please _contact Atlassian_ (link to contact page) for purchase and upgrade details.
            // Renewing your maintenance allows you _continued access to great benefits_ (link to the why renew page).

            final String supportAndUpdates;
            final String currentVersion;
            final User user = getConfirmedUser();
            if (user != null)
            {
                currentVersion = i18n.getText("admin.license.nbol.current.version", getCurrentVersion(), user.getDisplayName(), user.getName());
            }
            else
            {
                currentVersion = i18n.getText("admin.license.nbol.current.version.user.unknown", getCurrentVersion());
            }

            final String expired;
            final String renew;
            final String renewKey;
            final Link renewLink;
            if (isEntitledToSupport())
            {
                supportAndUpdates = i18n.getText("admin.license.nbol.support.and.updates", maintenanceEnd);
                renewKey = "admin.license.renew.for.support.and.updates";
            }
            else
            {
                supportAndUpdates = i18n.getText("admin.license.nbol.updates.only", maintenanceEnd);
                renewKey = isSelfRenewable() ? "admin.license.renew.for.updates.only" : "admin.license.renew.for.deprecated";
            }
            if (isCommercialLicense())
            {
                renewLink = urlRenew;
            }
            else
            {
                renewLink = isSelfRenewable() ? urlSelfRenew : urlContact;
            }
            renew = i18n.getText(renewKey, renewLink.getStart(), renewLink.getEnd());

            // expired
            if (isExpired())
            {
                expired = i18n.getText("admin.license.nbol.evaluation.period.has.expired");
            }
            // otherwise
            else
            {
                expired = i18n.getText("admin.license.nbol.evaluation.period.will.expire.in", "<strong>" + extendedDaysLeft + "</strong>");
            }

            final String whyRenew = i18n.getText("admin.license.why.renew", urlWhyRenew.getStart(), urlWhyRenew.getEnd());

            return DefaultLicenseStatusMessage.builder()
                .add("admin.license.support.and.updates", supportAndUpdates)
                .add("admin.license.nbol.current.version", currentVersion)
                .add("admin.license.nbol.evaluation.expiration", expired)
                .add("admin.license.renewal.target", Joiner.on(" ").join(renew, whyRenew))
                .build();
        }

        // other licenses
        else
        {
            final String maintenancePeriodEnd = "<strong>" + TextUtils.htmlEncode(localisedMaintenanceExpiryDate(i18n)) + "</strong>";
            // maintenance ended
            if (isMaintenanceExpired())
            {
                // COMMERCIAL: Your JIRA support and updates for this license have ended on {0}. JIRA updates created after {0} are not valid for this license.
                // OTHER:      JIRA updates for this license ended on {0}. JIRA updates created after {0} are not valid for this license.
                //
                // ENTITLED TO SUPPORT: If you wish to have access to support and updates, please _renew your maintenance_ (link to order form/my.atlassian.com).
                // NOT ENTITLED TO SUPPORT (SELF RENEW): If you wish to have access to updates, please _renew your maintenance_ (link to my.atlassian.com).
                // NOT ENTITLED TO SUPPORT (OTHER): If you wish to have access to support and updates, please _contact Atlassian_ (link to contact page) for purchase and upgrade details.
                // Renewing your maintenance allows you _continued access to great benefits_ (link to the why renew page).

                final String supportAndUpdates;
                final String renew;
                final String renewKey;
                final Link renewLink;
                if (isEntitledToSupport())
                {
                    supportAndUpdates = i18n.getText("admin.license.support.and.updates.has.ended", maintenancePeriodEnd);
                    renewKey = "admin.license.renew.for.support.and.updates";
                }
                else
                {
                    supportAndUpdates = i18n.getText("admin.license.updates.only.has.ended", maintenancePeriodEnd);
                    renewKey = isSelfRenewable() ? "admin.license.renew.for.updates.only" : "admin.license.renew.for.deprecated";
                }
                if (isCommercialLicense())
                {
                    renewLink = urlRenew;
                }
                else
                {
                    renewLink = isSelfRenewable() ? urlSelfRenew : urlContact;
                }
                renew = i18n.getText(renewKey, renewLink.getStart(), renewLink.getEnd());
                final String whyRenew = i18n.getText("admin.license.why.renew", urlWhyRenew.getStart(), urlWhyRenew.getEnd());

                return DefaultLicenseStatusMessage.builder()
                    .add("admin.license.support.and.updates", supportAndUpdates)
                    .add("admin.license.renewal.target", Joiner.on(" ").join(renew, whyRenew))
                    .build();
            }
            // almost expired
            else if (isMaintenanceAlmostEnded())
            {
                // COMMERCIAL: Your JIRA support and updates for this license will end on {0}. JIRA updates created after {0} will not be valid for this license.
                // OTHER:      JIRA updates for this license will end on {0}. JIRA updates created after {0} will not be valid for this license.
                //
                // ENTITLED TO SUPPORT: If you wish to have access to support and updates after this date, please _renew your maintenance_ (link to order form/my.atlassian.com).
                // NOT ENTITLED TO SUPPORT (SELF RENEW): If you wish to have access to updates after this date, please _renew your maintenance_ (link to my.atlassian.com).
                // NOT ENTITLED TO SUPPORT (OTHER): If you wish to have access to support and updates after this date, please _contact Atlassian_ (link to contact page) for purchase and upgrade details.
                // Renewing your maintenance allows you _continued access to great benefits_ (link to the why renew page).

                final String supportAndUpdates;
                final String renew;
                final String renewKey;
                final Link renewLink;
                if (isEntitledToSupport())
                {
                    supportAndUpdates = i18n.getText("admin.license.support.and.updates.will.end", maintenancePeriodEnd);
                    renewKey = "admin.license.renew.for.support.and.updates.after";
                }
                else
                {
                    supportAndUpdates = i18n.getText("admin.license.updates.only.will.end", maintenancePeriodEnd);
                    renewKey = isSelfRenewable() ? "admin.license.renew.for.updates.only.after" : "admin.license.renew.for.deprecated.after";
                }
                if (isCommercialLicense())
                {
                    renewLink = urlRenew;
                }
                else
                {
                    renewLink = isSelfRenewable() ? urlSelfRenew : urlContact;
                }
                renew = i18n.getText(renewKey, renewLink.getStart(), renewLink.getEnd());
                final String whyRenew = i18n.getText("admin.license.why.renew", urlWhyRenew.getStart(), urlWhyRenew.getEnd());

                return DefaultLicenseStatusMessage.builder()
                        .add("admin.license.support.and.updates", supportAndUpdates)
                        .add("admin.license.renewal.target", Joiner.on(" ").join(renew, whyRenew))
                        .build();
            }
            else if (!isDataCenter() && clusterManager.isClustered())
            {
                final String supportAndUpdates = i18n.getText("admin.data.center.unlicensed");
                String MAC = getExternalLinkUtil().getProperty("external.link.jira.license.data.center.contact");
                final String renew = i18n.getText("admin.data.center.unlicensed.buy.now", "<a href='" + MAC + "'>", "</a>");

                return DefaultLicenseStatusMessage.builder()
                        .add("admin.license.support.and.updates", supportAndUpdates)
                        .add("admin.license.renewal.target", renew)
                        .build();
            }
            // otherwise
            else
            {
                // license is valid and within its supported period - no message
                return null;
            }
        }
    }

    ExternalLinkUtil getExternalLinkUtil()
    {
        return externalLinkUtil;
    }

    /**
     * Utility class for HTML anchor tags
     */
    public class Link
    {
        private final String key;
        private final String param0;

        private Link(final String key)
        {
            this.key = key;
            param0 = null;
        }

        private Link(final String key, final String param)
        {
            this.key = key;
            param0 = param;
        }

        public String getStart()
        {
            final String url = param0 == null ? externalLinkUtil.getProperty(key) : externalLinkUtil.getProperty(key, param0);
            return "<a href=\"" + url + "\">";
        }

        public String getEnd()
        {
            return "</a>";
        }
    }
}
