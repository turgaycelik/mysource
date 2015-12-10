package com.atlassian.jira.license;

import org.junit.Test;

import java.util.Date;

import static com.atlassian.jira.license.NullLicenseDetails.NULL_LICENSE_DETAILS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * Unit test of NullLicenseDetails.
 *
 * @since 6.2
 */
public class TestNullLicenseDetails
{
    @Test
    public void shouldNotAllowClusteringForScale()
    {
        assertFalse("Should not allow clustering for scale", NULL_LICENSE_DETAILS.isDataCenter());
    }

    @Test
    public void alwaysOutOfMaintenance()
    {
        assertFalse(NULL_LICENSE_DETAILS.isMaintenanceValidForBuildDate(new Date()));
        assertEquals(-1, NULL_LICENSE_DETAILS.getDaysToMaintenanceExpiry());
    }

    @Test
    public void alwaysOnExpiryDay()
    {
        assertFalse(NULL_LICENSE_DETAILS.isExpired());
        assertEquals(0, NULL_LICENSE_DETAILS.getDaysToLicenseExpiry());
    }
}
