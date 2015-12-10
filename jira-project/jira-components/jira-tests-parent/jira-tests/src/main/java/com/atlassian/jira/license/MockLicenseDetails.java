package com.atlassian.jira.license;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.extras.api.jira.JiraLicense;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.web.util.OutlookDate;

import java.util.Collection;
import java.util.Date;
import javax.annotation.Nullable;

/**
* @since v6.2.3
*/
public class MockLicenseDetails implements LicenseDetails
{
    private boolean evaluation;
    private int maxUsers;
    private int daysToExpiry = Integer.MAX_VALUE;
    private int daysMaintenanceExpiry = Integer.MAX_VALUE;
    private boolean isSet = true;
    private boolean developer;

    @Override
    public boolean isLicenseSet()
    {
        return isSet;
    }

    @Override
    public int getLicenseVersion()
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public boolean isEntitledToSupport()
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public boolean isLicenseAlmostExpired()
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public JiraLicense getJiraLicense()
    {
        throw new UnsupportedOperationException("Not implemented");
    }


    @Override
    public LicenseRoleDetails getLicenseRoles()
    {
        throw new UnsupportedOperationException("Not implemented");
    }


    @Override
    public LicenseStatusMessage getLicenseStatusMessage(final I18nHelper i18n)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public String getLicenseStatusMessage(@Nullable final User user, final String delimiter)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public String getLicenseStatusMessage(final I18nHelper i18n, @Nullable final OutlookDate ignored, final String delimiter)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public String getLicenseExpiryStatusMessage(@Nullable final User user)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public String getLicenseExpiryStatusMessage(final I18nHelper i18n, @Nullable final OutlookDate ignored)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public String getBriefMaintenanceStatusMessage(final I18nHelper i18n)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public String getSupportRequestMessage(final User user)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public String getSupportRequestMessage(final I18nHelper i18n, @Nullable final OutlookDate ignored)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public String getMaintenanceEndString(final OutlookDate outlookDate)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    public MockLicenseDetails setUnlimitedUsers()
    {
        maxUsers = Integer.MIN_VALUE;
        return this;
    }

    @Override
    public boolean isUnlimitedNumberOfUsers()
    {
        return maxUsers == Integer.MIN_VALUE;
    }

    @Override
    public int getMaximumNumberOfUsers()
    {
        return maxUsers;
    }

    public MockLicenseDetails setMaxUsers(int users)
    {
        maxUsers = users;
        return this;
    }

    @Override
    public String getDescription()
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public String getPartnerName()
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public boolean isExpired()
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public String getPurchaseDate(final OutlookDate outlookDate)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    public MockLicenseDetails setEvaluation(boolean eval)
    {
        this.evaluation = eval;
        return this;
    }

    @Override
    public boolean isEvaluation()
    {
        return evaluation;
    }

    @Override
    public boolean isStarter()
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public boolean isCommercial()
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public boolean isPersonalLicense()
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public boolean isCommunity()
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public boolean isOpenSource()
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public boolean isNonProfit()
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public boolean isDemonstration()
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public boolean isOnDemand()
    {
        throw new UnsupportedOperationException("Not implemented");
    }


    @Override
    public boolean isDataCenter()
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public boolean isEnterpriseLicenseAgreement()
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public boolean isDeveloper()
    {
        return developer;
    }

    public MockLicenseDetails setDeveloper(boolean developer)
    {
        this.developer = developer;
        return this;
    }

    @Override
    public String getOrganisation()
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public boolean hasLicenseTooOldForBuildConfirmationBeenDone()
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public String getLicenseString()
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public boolean isMaintenanceValidForBuildDate(final Date currentBuildDate)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public String getSupportEntitlementNumber()
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Collection<LicenseContact> getContacts()
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public int getDaysToLicenseExpiry()
    {
        return daysToExpiry;
    }

    public MockLicenseDetails setDaysToLicenseExpiry(int days)
    {
        this.daysToExpiry = days;
        return this;
    }

    @Override
    public int getDaysToMaintenanceExpiry()
    {
        return daysMaintenanceExpiry;
    }

    public MockLicenseDetails setDaysToMaintenanceExpiry(final int days)
    {
        this.daysMaintenanceExpiry = days;
        return this;
    }

    public MockLicenseDetails makeUnset()
    {
        this.isSet = false;
        return this;
    }
}
