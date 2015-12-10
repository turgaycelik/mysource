package com.atlassian.jira.license;

import com.atlassian.extras.api.Contact;
import com.atlassian.extras.api.LicenseEdition;
import com.atlassian.extras.api.LicenseType;
import com.atlassian.extras.api.Organisation;
import com.atlassian.extras.api.Partner;
import com.atlassian.extras.api.Product;
import com.atlassian.extras.api.jira.JiraLicense;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @since v6.3
 */
public class MockLicense implements JiraLicense
{
    private LicenseType licenseType;
    private Date expiryDate;
    private boolean isExpired;
    private boolean evaluation;
    private Date maintenanceExpiryDate;
    private boolean maintenanceExpired;
    private int numberOfDaysBeforeMaintenanceExpiry;
    private int licenseVersion;
    private final Map<String, String> properties = new HashMap<String, String>();

    public int getLicenseVersion()
    {
        return licenseVersion;
    }

    public String getDescription()
    {
        return null;
    }

    public Product getProduct()
    {
        return null;
    }

    public String getServerId()
    {
        return null;
    }

    public Partner getPartner()
    {
        return null;
    }

    public Organisation getOrganisation()
    {
        return null;
    }

    public Collection<Contact> getContacts()
    {
        return null;
    }

    public Date getCreationDate()
    {
        return null;
    }

    public Date getPurchaseDate()
    {
        return null;
    }

    public LicenseType getLicenseType()
    {
        return licenseType;
    }

    public String getProperty(final String name)
    {
        return properties.get(name);
    }

    public boolean isExpired()
    {
        return isExpired;
    }

    public Date getExpiryDate()
    {
        return expiryDate;
    }

    public int getNumberOfDaysBeforeExpiry()
    {
        return 0;
    }

    public String getSupportEntitlementNumber()
    {
        return null;
    }

    public Date getMaintenanceExpiryDate()
    {
        return maintenanceExpiryDate;
    }

    public int getNumberOfDaysBeforeMaintenanceExpiry()
    {
        return numberOfDaysBeforeMaintenanceExpiry;
    }

    public boolean isMaintenanceExpired()
    {
        return maintenanceExpired;
    }

    public int getMaximumNumberOfUsers()
    {
        return 0;
    }

    public boolean isUnlimitedNumberOfUsers()
    {
        return false;
    }

    public boolean isEvaluation()
    {
        return evaluation;
    }

    public void setLicenseType(final LicenseType licenseType)
    {
        this.licenseType = licenseType;
    }

    public void setExpiryDate(final Date date)
    {
        expiryDate = date;
    }

    public void setExpiryDate(long time)
    {
        setExpiryDate(new Date(time));
    }

    public void setExpired(final boolean isExpired)
    {
        this.isExpired = isExpired;
    }

    public LicenseEdition getLicenseEdition()
    {
        return null;
    }

    public void setEvaluation(boolean evaluation)
    {
        this.evaluation = evaluation;
    }

    public void setMaintenanceExpiryDate(long maintenanceExpiryDate)
    {
        setMaintenanceExpiryDate(new Date(maintenanceExpiryDate));
    }

    public void setMaintenanceExpiryDate(final Date maintenanceExpiryDate)
    {
        this.maintenanceExpiryDate = maintenanceExpiryDate;
    }

    public void setMaintenanceExpired(final boolean maintenanceExpired)
    {
        this.maintenanceExpired = maintenanceExpired;
    }

    public void setNumberOfDaysBeforeMaintenanceExpiry(final int numberOfDaysBeforeMaintenanceExpiry)
    {
        this.numberOfDaysBeforeMaintenanceExpiry = numberOfDaysBeforeMaintenanceExpiry;
    }

    public void setLicenseVersion(final int licenseVersion)
    {
        this.licenseVersion = licenseVersion;
    }

    public void setProperty(final String name, final String value)
    {
        properties.put(name, value);
    }
}
