package com.atlassian.jira.rest.v2.admin.licenserole;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;

import java.util.List;

/**
 * Examples of the {@link com.atlassian.jira.rest.v2.admin.licenserole.LicenseRoleBean} for documentation.
 */
public class LicenseRoleBeanExamples
{
    public static final LicenseRoleBean DOC_EXAMPLE =
            new LicenseRoleBean("user", "User for JIRA", Sets.newHashSet("users", "admins"));

    public static final LicenseRoleBean DOC_EXAMPLE2 =
            new LicenseRoleBean("admin", "Admin for JIRA", Sets.newHashSet("admins"));

    public static final LicenseRoleBean UPDATE_EXAMPLE =
            new LicenseRoleBean(null, null, Sets.newHashSet("user", "admins"));

    public static final List<LicenseRoleBean> DOC_LIST = ImmutableList.of(DOC_EXAMPLE, DOC_EXAMPLE2);
}
