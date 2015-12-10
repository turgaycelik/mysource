package com.atlassian.jira.license;

import com.google.common.collect.ImmutableSet;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

public class ImmutableLicenseRoleTest
{
    @Test
    public void equalsOnlyUsesTheRoleId() throws Exception
    {
        final MockLicenseRoleDefinition def1 = new MockLicenseRoleDefinition("role1", "Name 1");
        final MockLicenseRoleDefinition def2 = new MockLicenseRoleDefinition("role1", "Name 2");
        final MockLicenseRoleDefinition def3 = new MockLicenseRoleDefinition("role2", "Name 1");

        final ImmutableLicenseRole role1 = new ImmutableLicenseRole(def1, ImmutableSet.of("What"));
        final ImmutableLicenseRole role2 = new ImmutableLicenseRole(def2, ImmutableSet.of("Equal"));
        final ImmutableLicenseRole role3 = new ImmutableLicenseRole(def3, ImmutableSet.of("What"));

        assertThat(role1, equalTo(role1));
        assertThat(role1, equalTo(role2));
        assertThat(role2, equalTo(role1));
        assertThat(role2, equalTo(role2));

        assertThat(role3, equalTo(role3));
        assertThat(role3, not(equalTo(role1)));
        assertThat(role3, not(equalTo(role2)));
        assertThat(role1, not(equalTo(role3)));
        assertThat(role2, not(equalTo(role3)));
    }

    @Test
    public void hashCodeOnlyUsesTheRoleId() throws Exception
    {
        final MockLicenseRoleDefinition def1 = new MockLicenseRoleDefinition("def1");
        final ImmutableLicenseRole role1 = new ImmutableLicenseRole(def1, ImmutableSet.of("What"));
        final ImmutableLicenseRole role2 = new ImmutableLicenseRole(def1, ImmutableSet.of("Equal"));

        assertThat(role1.hashCode(), equalTo(role2.hashCode()));
    }
}