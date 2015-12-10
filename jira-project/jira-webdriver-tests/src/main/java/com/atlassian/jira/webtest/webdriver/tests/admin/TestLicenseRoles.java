package com.atlassian.jira.webtest.webdriver.tests.admin;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.functest.matcher.LicenseRoleBeanMatcher;
import com.atlassian.jira.pageobjects.BaseJiraWebTest;
import com.atlassian.jira.pageobjects.config.ResetDataOnce;
import com.atlassian.jira.pageobjects.pages.admin.licenserole.LicenseRolePage;
import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeDiagnosingMatcher;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;

@WebTest ({ Category.WEBDRIVER_TEST, Category.ADMINISTRATION })
@ResetDataOnce
public class TestLicenseRoles extends BaseJiraWebTest
{
    private static final String GROUP_USERS = "jira-users";
    private static final String GROUP_DEVELOPERS = "jira-developers";
    private static final String GROUP_ADMINS = "jira-administrators";

    /**
     * Happy integration test. Edge cases are tested in QUnit.
     */
    @Test
    public void sanityTest()
    {
        //WD doesn't deal well with empty roles. Just avoid the problem.
        backdoor.licenseRoles().putBusinessUser(GROUP_USERS);

        LicenseRolePage.Role role = jira.goTo(LicenseRolePage.class).role("businessuser");
        assertThat(role, RoleMatcher.forBusinessUser(GROUP_USERS));

        role = role.edit().groups(GROUP_DEVELOPERS, GROUP_ADMINS).save();

        assertThat(role, RoleMatcher.forBusinessUser(GROUP_ADMINS, GROUP_DEVELOPERS));
        assertThat(backdoor.licenseRoles().getBusinessUser(),
                LicenseRoleBeanMatcher.forBusinessUser().setGroups(GROUP_ADMINS, GROUP_DEVELOPERS));
    }

    private static class RoleMatcher extends TypeSafeDiagnosingMatcher<LicenseRolePage.Role>
    {
        private final String name;
        private List<String> groups;

        public static RoleMatcher forBusinessUser(String...groups)
        {
            return new RoleMatcher("Business User", groups);
        }

        private RoleMatcher(String name, String...groups)
        {
            this.name = name;
            this.groups = Lists.newArrayList(Arrays.asList(groups));
        }

        @Override
        protected boolean matchesSafely(final LicenseRolePage.Role item, final Description mismatchDescription)
        {
            if (Objects.equal(name, item.getName()) && Objects.equal(groups, item.getGroups()))
            {
                return true;
            }
            else
            {
                mismatchDescription.appendValue(String.format("[name: %s, groups: %s]", item.getName(), item.getGroups()));
                return false;
            }
        }

        @Override
        public void describeTo(final Description description)
        {
            description.appendText(String.format("[name: %s, groups: %s]", name, groups));
        }
    }
}
