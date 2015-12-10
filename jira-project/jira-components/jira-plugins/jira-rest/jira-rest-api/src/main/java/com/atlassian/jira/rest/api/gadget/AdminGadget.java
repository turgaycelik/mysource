package com.atlassian.jira.rest.api.gadget;

import com.atlassian.jira.rest.api.issue.FieldsSerializer;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.util.List;

/**
 * Admin Gadget bean.
 */
@JsonSerialize (using = FieldsSerializer.class)
public class AdminGadget
{
    public boolean isAdmin;
    public boolean notExternalUserManagement;
    public boolean hasZeroUserLicense;
    public boolean hasExceededUserLimit;
    public boolean hasReachedUserLimit;
    public String dbConfigDocsUrl;
    public boolean isSystemAdministrator;
    public boolean isUsingHsql;
    public String licenseStatusMessage;
    public List<String> warningMessages;
    public boolean nearExpiry;
    public String licenseTypeNiceName;
    public String partnerName;
    public String licenseExpiryStatusMessage;
    public String externalLinkMyAccount;
    public String externalLinkPersonalSite;
    public AdminTaskLists tasks;
    public boolean isOnDemand;
    public boolean isGappsEnabled;
    public String browseDocsUrl;
    public String defineWorkflowsDocsUrl;
    public String customizeFieldsDocsUrl;
    public String customizeScreensDocsUrl;
    public String manageUsersDocsUrl;
    public String timeTrackingDocsUrl;
    public String migrationDocsUrl;

    public String getBrowseDocsUrl()
    {
        return browseDocsUrl;
    }

    public AdminGadget setBrowseDocsUrl(String browseDocsUrl)
    {
        this.browseDocsUrl = browseDocsUrl;
        return this;
    }

    public String getCustomizeFieldsDocsUrl()
    {
        return customizeFieldsDocsUrl;
    }

    public AdminGadget setCustomizeFieldsDocsUrl(String customizeFieldsDocsUrl)
    {
        this.customizeFieldsDocsUrl = customizeFieldsDocsUrl;
        return this;
    }

    public String getCustomizeScreensDocsUrl()
    {
        return customizeScreensDocsUrl;
    }

    public AdminGadget setCustomizeScreensDocsUrl(String customizeScreensDocsUrl)
    {
        this.customizeScreensDocsUrl = customizeScreensDocsUrl;
        return this;
    }

    public String getDbConfigDocsUrl()
    {
        return dbConfigDocsUrl;
    }

    public AdminGadget setDbConfigDocsUrl(String dbConfigDocsUrl)
    {
        this.dbConfigDocsUrl = dbConfigDocsUrl;
        return this;
    }

    public String getDefineWorkflowsDocsUrl()
    {
        return defineWorkflowsDocsUrl;
    }

    public AdminGadget setDefineWorkflowsDocsUrl(String defineWorkflowsDocsUrl)
    {
        this.defineWorkflowsDocsUrl = defineWorkflowsDocsUrl;
        return this;
    }

    public String getExternalLinkMyAccount()
    {
        return externalLinkMyAccount;
    }

    public AdminGadget setExternalLinkMyAccount(String externalLinkMyAccount)
    {
        this.externalLinkMyAccount = externalLinkMyAccount;
        return this;
    }

    public String getExternalLinkPersonalSite()
    {
        return externalLinkPersonalSite;
    }

    public AdminGadget setExternalLinkPersonalSite(String externalLinkPersonalSite)
    {
        this.externalLinkPersonalSite = externalLinkPersonalSite;
        return this;
    }

    public boolean isHasExceededUserLimit()
    {
        return hasExceededUserLimit;
    }

    public AdminGadget setHasExceededUserLimit(boolean hasExceededUserLimit)
    {
        this.hasExceededUserLimit = hasExceededUserLimit;
        return this;
    }

    public boolean isHasReachedUserLimit()
    {
        return hasReachedUserLimit;
    }

    public AdminGadget setHasReachedUserLimit(boolean hasReachedUserLimit)
    {
        this.hasReachedUserLimit = hasReachedUserLimit;
        return this;
    }

    public boolean isHasZeroUserLicense()
    {
        return hasZeroUserLicense;
    }

    public AdminGadget setHasZeroUserLicense(boolean hasZeroUserLicense)
    {
        this.hasZeroUserLicense = hasZeroUserLicense;
        return this;
    }

    public boolean isAdmin()
    {
        return isAdmin;
    }

    public AdminGadget setAdmin(boolean admin)
    {
        isAdmin = admin;
        return this;
    }

    public boolean isOnDemand()
    {
        return isOnDemand;
    }

    public AdminGadget setOnDemand(boolean onDemand)
    {
        isOnDemand = onDemand;
        return this;
    }

    public boolean isSystemAdministrator()
    {
        return isSystemAdministrator;
    }

    public AdminGadget setSystemAdministrator(boolean systemAdministrator)
    {
        isSystemAdministrator = systemAdministrator;
        return this;
    }

    public boolean isUsingHsql()
    {
        return isUsingHsql;
    }

    public AdminGadget setUsingHsql(boolean usingHsql)
    {
        isUsingHsql = usingHsql;
        return this;
    }

    public String getLicenseExpiryStatusMessage()
    {
        return licenseExpiryStatusMessage;
    }

    public AdminGadget setLicenseExpiryStatusMessage(String licenseExpiryStatusMessage)
    {
        this.licenseExpiryStatusMessage = licenseExpiryStatusMessage;
        return this;
    }

    public String getLicenseStatusMessage()
    {
        return licenseStatusMessage;
    }

    public AdminGadget setLicenseStatusMessage(String licenseStatusMessage)
    {
        this.licenseStatusMessage = licenseStatusMessage;
        return this;
    }

    public String getLicenseTypeNiceName()
    {
        return licenseTypeNiceName;
    }

    public AdminGadget setLicenseTypeNiceName(String licenseTypeNiceName)
    {
        this.licenseTypeNiceName = licenseTypeNiceName;
        return this;
    }

    public String getManageUsersDocsUrl()
    {
        return manageUsersDocsUrl;
    }

    public AdminGadget setManageUsersDocsUrl(String manageUsersDocsUrl)
    {
        this.manageUsersDocsUrl = manageUsersDocsUrl;
        return this;
    }

    public String getMigrationDocsUrl()
    {
        return migrationDocsUrl;
    }

    public AdminGadget setMigrationDocsUrl(String migrationDocsUrl)
    {
        this.migrationDocsUrl = migrationDocsUrl;
        return this;
    }

    public boolean isNearExpiry()
    {
        return nearExpiry;
    }

    public AdminGadget setNearExpiry(boolean nearExpiry)
    {
        this.nearExpiry = nearExpiry;
        return this;
    }

    public boolean isNotExternalUserManagement()
    {
        return notExternalUserManagement;
    }

    public AdminGadget setNotExternalUserManagement(boolean notExternalUserManagement)
    {
        this.notExternalUserManagement = notExternalUserManagement;
        return this;
    }

    public String getPartnerName()
    {
        return partnerName;
    }

    public AdminGadget setPartnerName(String partnerName)
    {
        this.partnerName = partnerName;
        return this;
    }

    public AdminTaskLists getTasks()
    {
        return tasks;
    }

    public AdminGadget setTasks(AdminTaskLists tasks)
    {
        this.tasks = tasks;
        return this;
    }

    public String getTimeTrackingDocsUrl()
    {
        return timeTrackingDocsUrl;
    }

    public AdminGadget setTimeTrackingDocsUrl(String timeTrackingDocsUrl)
    {
        this.timeTrackingDocsUrl = timeTrackingDocsUrl;
        return this;
    }

    public List<String> getWarningMessages()
    {
        return warningMessages;
    }

    public AdminGadget setWarningMessages(List<String> warningMessages)
    {
        this.warningMessages = warningMessages;
        return this;
    }

    public boolean isGappsEnabled()
    {
        return isGappsEnabled;
    }

    public AdminGadget setGappsEnabled(boolean gappsEnabled)
    {
        isGappsEnabled = gappsEnabled;
        return this;
    }
}
