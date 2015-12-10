package com.atlassian.jira.license;

import org.junit.Test;

public class LicenseRoleGroupEntryTest
{
    @Test (expected = IllegalArgumentException.class)
    public void cantConstructWithNullRole()
    {
        new LicenseRoleGroupEntry(null, "group");
    }

    @Test (expected = IllegalArgumentException.class)
    public void cantConstructWithBlankRole()
    {
        new LicenseRoleGroupEntry("    \t\n", "group");
    }

    @Test (expected = IllegalArgumentException.class)
    public void cantConstructWithNullGroup()
    {
        new LicenseRoleGroupEntry("role", null);
    }
}