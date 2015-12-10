package com.atlassian.jira.license;

import com.atlassian.annotations.ExperimentalApi;
import com.atlassian.annotations.Internal;

import javax.annotation.Nonnull;

import static com.atlassian.jira.util.CaseFolding.foldString;
import static com.atlassian.jira.util.dbc.Assertions.stripNotBlank;

/**
 * License roles are a hierarchical typing mechanism for users of a license.
 *
 * Licenses in JIRA 6.3 and earlier effectively have a single "license role" --
 * all JIRA users counted equally toward the user limit of the license.
 *
 * In JIRA 7.0 and later, licenses are granted in terms of license roles
 * which take the place of licenses in determining the number of users that may
 * access any given product.
 *
 * @see com.atlassian.jira.bc.license.LicenseRoleService
 * @see com.atlassian.jira.license.JiraLicenseManager
 * @see com.atlassian.jira.license.LicenseDetails
 * @see com.atlassian.jira.license.LicenseRoleDefinition
 * @since v6.3
 */
@ExperimentalApi
public final class LicenseRoleId
{
    private final String name;

    public LicenseRoleId(@Nonnull String name)
    {
        //The name is CaseInsensitive. This is really an ID so we don't care what it looks like to the user.
        this.name = foldString(stripNotBlank("name", name));
    }

    @Internal
    @Nonnull
    public String getName()
    {
        return this.name;
    }

    @Override
    public boolean equals(Object x)
    {
        if (x == null || !(x instanceof LicenseRoleId))
        {
            return false;
        }

        LicenseRoleId that = (LicenseRoleId) x;
        return this.name.equals(that.name);
    }

    @Override
    public int hashCode()
    {
        return this.name.hashCode();
    }

    @Override
    public String toString()
    {
        return getName();
    }

    public static LicenseRoleId valueOf(String id)
    {
        return new LicenseRoleId(id);
    }
}
