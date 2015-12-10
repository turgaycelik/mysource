package com.atlassian.jira.license;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import javax.annotation.Nonnull;

public class MockLicenseRoleDefinition implements LicenseRoleDefinition
{
    private LicenseRoleId id;
    private String name;

    public MockLicenseRoleDefinition()
    {
    }

    public MockLicenseRoleDefinition(final LicenseRoleId id, final String name)
    {
        this.id = id;
        this.name = name;
    }

    public MockLicenseRoleDefinition(final String id)
    {
        this(id, id);
    }

    public MockLicenseRoleDefinition(final String id, String name)
    {
        this(new LicenseRoleId(id), name);
    }

    @Nonnull
    @Override
    public LicenseRoleId getLicenseRoleId()
    {
        return id;
    }

    @Nonnull
    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("id", id)
                .append("name", name)
                .toString();
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }

        final MockLicenseRoleDefinition that = (MockLicenseRoleDefinition) o;

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
