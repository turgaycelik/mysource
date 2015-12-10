package com.atlassian.jira.license;

import com.atlassian.annotations.ExperimentalApi;

import java.util.Set;
import javax.annotation.Nonnull;

/**
 * Represents a License Role in JIRA.
 *
 * @since v6.3
 */
@ExperimentalApi
public interface LicenseRole
{
    /**
     * Returns the canonical {@link LicenseRoleId} that uniquely identifies this {@link LicenseRole}.
     *
     * @return the canonical {@link LicenseRoleId} that uniquely identifies this {@link LicenseRole}.
     */
    @Nonnull
    LicenseRoleId getId();

    /**
     * Return the name of the license role. The name is i18ned for the calling user.
     * @return the name of the license role. The name is i18ned for the calling user.
     */
    @Nonnull
    String getName();

    /**
     * Return the set of Group Ids associated with the role.
     *
     * @return the groups ids associated with the role.
     */
    @Nonnull
    Set<String> getGroups();
}
