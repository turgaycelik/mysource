package com.atlassian.jira.license;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.extras.api.jira.JiraLicense;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.web.util.OutlookDate;

import java.util.Collection;
import java.util.Date;
import javax.annotation.Nullable;

import static com.google.common.collect.Lists.newArrayList;

/**
 * Implementation of the {@link LicenseDetails} interface used when the {@link JiraLicense} object is {@code null}, to
 * avoid having to do null checks everywhere in the {@link DefaultLicenseDetails} implementation.
 *
 * @since v3.13
 */
class NullLicenseDetails implements LicenseDetails
{
    static final LicenseDetails NULL_LICENSE_DETAILS = new NullLicenseDetails();

    /**
     * Should not be constructed; use {@link NullLicenseDetails#NULL_LICENSE_DETAILS}
     */
    private NullLicenseDetails() {}


    @Override
    public String getSupportRequestMessage(User user)
    {
        return null;
    }

    @Override
    public String getSupportRequestMessage(final I18nHelper i18n, final OutlookDate outlookDate)
    {
        return null;
    }

    @Override
    public String getMaintenanceEndString(final OutlookDate outlookDate)
    {
        return null;
    }

    @Override
    public boolean isUnlimitedNumberOfUsers()
    {
        return false;
    }

    @Override
    public int getMaximumNumberOfUsers()
    {
        return 0;
    }

    @Override
    public boolean isLicenseSet()
    {
        return false;
    }

    @Override
    public JiraLicense getJiraLicense()
    {
        return null;
    }


    @Override
    public LicenseRoleDetails getLicenseRoles()
    {
        return null;
    }


    @Override
    public int getLicenseVersion()
    {
        return 0;
    }

    @Override
    public String getDescription()
    {
        return "";
    }

    @Override
    public String getPartnerName()
    {
        return null;
    }

    @Override
    public boolean isExpired()
    {
        return false;
    }

    @Override
    public String getPurchaseDate(final OutlookDate outlookDate)
    {
        return "";
    }

    @Override
    public boolean isEvaluation()
    {
        return false;
    }

    @Override
    public boolean isStarter()
    {
        return false;
    }

    @Override
    public boolean isCommercial()
    {
        return false;
    }

    @Override
    public boolean isPersonalLicense()
    {
        return false;
    }

    @Override
    public boolean isCommunity()
    {
        return false;
    }

    @Override
    public boolean isOpenSource()
    {
        return false;
    }

    @Override
    public boolean isNonProfit()
    {
        return false;
    }

    @Override
    public boolean isDemonstration()
    {
        return false;
    }

    @Override
    public boolean isOnDemand()
    {
        return false;
    }


    @Override
    public boolean isDataCenter()
    {
        return false;
    }

    @Override
    public boolean isEnterpriseLicenseAgreement()
    {
        return false;
    }

    @Override
    public boolean isDeveloper()
    {
        return false;
    }

    @Override
    public String getOrganisation()
    {
        return "<Unknown>";
    }

    @Override
    public boolean isEntitledToSupport()
    {
        return false;
    }

    @Override
    public boolean isLicenseAlmostExpired()
    {
        return false;
    }

    @Override
    public boolean hasLicenseTooOldForBuildConfirmationBeenDone()
    {
        return false;
    }

    @Override
    public String getLicenseString()
    {
        return "";
    }

    @Override
    public boolean isMaintenanceValidForBuildDate(final Date currentBuildDate)
    {
        return false;
    }

    @Override
    public String getSupportEntitlementNumber()
    {
        return null;
    }

    @Override
    public Collection<LicenseContact> getContacts()
    {
        return newArrayList();
    }

    @Override
    public int getDaysToLicenseExpiry()
    {
        //This license does not expire (see isExpired()).
        return 0;
    }

    @Override
    public int getDaysToMaintenanceExpiry()
    {
        //This license is not in maintenance (see isMaintenanceValidForBuildDate()),
        return -1;
    }

    @Override
    public String getLicenseStatusMessage(@Nullable User user, String delimiter)
    {
        return null;
    }

    @Override
    public String getLicenseStatusMessage(final I18nHelper i18n, final OutlookDate outlookDate, final String delimiter)
    {
        return null;
    }

    @Override
    public LicenseStatusMessage getLicenseStatusMessage(final I18nHelper i18n)
    {
        return null;
    }

    @Override
    public String getLicenseExpiryStatusMessage(@Nullable User user)
    {
        return null;
    }

    @Override
    public String getLicenseExpiryStatusMessage(final I18nHelper i18n, final OutlookDate outlookDate)
    {
        return null;
    }

    @Override
    public String getBriefMaintenanceStatusMessage(final I18nHelper i18n)
    {
        return null;
    }
}
