package com.atlassian.jira.license;

import com.google.common.collect.ImmutableSet;

import java.util.Set;
import javax.annotation.Nonnull;

import static com.atlassian.jira.util.dbc.Assertions.containsNoNulls;
import static com.atlassian.jira.util.dbc.Assertions.notNull;
import static com.google.common.collect.ImmutableSet.copyOf;

public final class ImmutableLicenseRole implements LicenseRole
{
    private final LicenseRoleDefinition def;
    private final ImmutableSet<String> groups;

    public ImmutableLicenseRole(final LicenseRoleDefinition def, final Iterable<String> groups)
    {
        this.def = notNull("def", def);
        this.groups = copyOf(containsNoNulls("groups", groups));
    }

    @Nonnull
    @Override
    public LicenseRoleId getId()
    {
        return def.getLicenseRoleId();
    }

    @Nonnull
    @Override
    public String getName()
    {
        return def.getName();
    }

    @Nonnull
    @Override
    public Set<String> getGroups()
    {
        return groups;
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }

        final ImmutableLicenseRole that = (ImmutableLicenseRole) o;

        //It is intentional that we only look at def.getLicenseRoleId().
        return def.getLicenseRoleId().equals(that.def.getLicenseRoleId());
    }

    @Override
    public int hashCode()
    {
        //It is intentional that we only look at def.getLicenseRoleId().
        return def.getLicenseRoleId().hashCode();
    }
}
