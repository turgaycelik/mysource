package com.atlassian.jira.license;

import javax.annotation.Nonnull;

import static com.atlassian.jira.util.dbc.Assertions.notNull;
import static com.atlassian.jira.util.dbc.Assertions.stripNotBlank;

/**
 * A mapping from a license role to group.
 *
 * @since 6.3
 */
public class LicenseRoleGroupEntry
{
    private final String licenseRoleName;
    private final String groupId;

    public LicenseRoleGroupEntry(@Nonnull final String licenseRoleName, @Nonnull final String groupId)
    {
        this.licenseRoleName = stripNotBlank("licenseRoleName", licenseRoleName);
        this.groupId = notNull("groupId", groupId);
    }

    public String getLicenseRoleName()
    {
        return licenseRoleName;
    }

    public String getGroupId()
    {
        return groupId;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof LicenseRoleGroupEntry))
        {
            return false;
        }

        final LicenseRoleGroupEntry licenseRoleGroupEntryEntry = (LicenseRoleGroupEntry) o;

        if (!licenseRoleName.equals(licenseRoleGroupEntryEntry.licenseRoleName)) return false;
        if (!groupId.equals(licenseRoleGroupEntryEntry.groupId)) return false;
        return true;
    }

    @Override
    public int hashCode()
    {
        int result;
        result = licenseRoleName != null ? licenseRoleName.hashCode() : 0;
        result = 71 * result + (groupId != null ? groupId.hashCode() : 0);
        return result;
    }

    @Override
    public String toString()
    {
        return "License Role Group: [licenseRole=" + licenseRoleName + "][group=" + groupId + "]";
    }
}
