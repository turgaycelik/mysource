package com.atlassian.jira.bc.license;

import com.atlassian.jira.bc.ServiceOutcome;
import com.atlassian.jira.bc.ServiceOutcomeImpl;
import com.atlassian.jira.license.LicenseRole;
import com.atlassian.jira.license.LicenseRoleId;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.ErrorCollection;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class MockLicenseRoleService implements LicenseRoleService
{
    public static final String NOT_FOUND = "Not Found";

    private Map<LicenseRoleId, MockLicenseRole> roles = Maps.newHashMap();

    @Override
    public boolean userHasRole(@Nullable final ApplicationUser user, @Nonnull final LicenseRoleId licenseRoleId)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Nonnull
    @Override
    public ServiceOutcome<Set<LicenseRole>> getRoles()
    {
        return ServiceOutcomeImpl.<Set<LicenseRole>>ok(Sets.<LicenseRole>newHashSet(roles.values()));
    }

    @Nonnull
    @Override
    public ServiceOutcome<LicenseRole> getRole(@Nonnull final LicenseRoleId licenseRoleId)
    {
        final MockLicenseRole mockLicenseRole = roles.get(licenseRoleId);
        if (mockLicenseRole == null)
        {
            return notFound();
        }
        else
        {
            return ServiceOutcomeImpl.<LicenseRole>ok(mockLicenseRole);
        }
    }

    @Nonnull
    @Override
    public ServiceOutcome<LicenseRole> setGroups(@Nonnull final LicenseRoleId licenseRoleId, @Nonnull final Iterable<String> groups)
    {
        final MockLicenseRole mockLicenseRole = roles.get(licenseRoleId);
        if (mockLicenseRole == null)
        {
            return notFound();
        }
        else
        {
            mockLicenseRole.setGroups(groups);
            return ServiceOutcomeImpl.<LicenseRole>ok(mockLicenseRole);
        }
    }

    private ServiceOutcomeImpl<LicenseRole> notFound()
    {
        return ServiceOutcomeImpl.error(NOT_FOUND, ErrorCollection.Reason.NOT_FOUND);
    }

    public MockLicenseRole addLicenseRole(String id)
    {
        final MockLicenseRole role = new MockLicenseRole().setId(id);
        roles.put(role.getId(), role);
        return role;
    }
}
