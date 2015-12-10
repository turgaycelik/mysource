package com.atlassian.jira.bc.license;

import com.atlassian.jira.license.LicenseRole;
import com.atlassian.jira.license.LicenseRoleId;
import com.google.common.collect.Sets;

import java.util.Arrays;
import java.util.Set;
import javax.annotation.Nonnull;

public class MockLicenseRole implements LicenseRole
{
    private LicenseRoleId id;
    private Set<String> groups;
    private String name;

    public MockLicenseRole()
    {
        this.groups = Sets.newHashSet();
    }

    public MockLicenseRole(final MockLicenseRole copy)
    {
        this.id = copy.id;
        this.groups = Sets.newHashSet(copy.groups);
        this.name = copy.name;
    }

    public MockLicenseRole setId(String id)
    {
        this.id = new LicenseRoleId(id);
        return this;
    }

    public MockLicenseRole setName(String name)
    {
        this.name = name;
        return this;
    }

    public MockLicenseRole addGroups(String... groups)
    {
        this.groups.addAll(Arrays.asList(groups));
        return this;
    }

    @Nonnull
    @Override
    public LicenseRoleId getId()
    {
        return id;
    }

    @Nonnull
    @Override
    public String getName()
    {
        return name;
    }

    @Nonnull
    @Override
    public Set<String> getGroups()
    {
        return groups;
    }

    public MockLicenseRole setGroups(final Iterable<String> groups)
    {
        this.groups = Sets.newHashSet(groups);
        return this;
    }

    public MockLicenseRole copy()
    {
        return new MockLicenseRole(this);
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }

        final MockLicenseRole that = (MockLicenseRole) o;

        //It is intentional that we only look at id.
        return id != null ? id.equals(that.id) : that.id == null;

    }

    @Override
    public int hashCode()
    {
        //It is intentional that we only look at id.
        return id != null ? id.hashCode() : 0;
    }
}
