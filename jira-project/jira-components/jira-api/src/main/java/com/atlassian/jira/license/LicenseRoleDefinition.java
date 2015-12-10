package com.atlassian.jira.license;

import javax.annotation.Nonnull;

/**
 * Representation of a plugin defined license role.
 */
public interface LicenseRoleDefinition
{
    /**
     * Retrieves the id for this definition.
     *
     * @return a license role id.
     */
    @Nonnull
    LicenseRoleId getLicenseRoleId();

    /**
     * Retrieves the name of the role.
     *
     * @return the name of the role.
     */
    @Nonnull
    String getName();
}
