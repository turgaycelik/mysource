package com.atlassian.jira.bc.license;

import com.atlassian.jira.bc.ServiceOutcome;
import com.atlassian.jira.bc.ServiceOutcomeMatchers;
import com.atlassian.jira.bc.ValueMatcher;
import com.atlassian.jira.license.LicenseRole;
import com.atlassian.jira.license.LicenseRoleDefinition;
import com.atlassian.jira.license.LicenseRoleId;
import com.atlassian.jira.license.LicenseRoleManager;
import com.atlassian.jira.license.MockLicenseRoleDefinition;
import com.atlassian.jira.mock.security.MockSimpleAuthenticationContext;
import com.atlassian.jira.permission.GlobalPermissionKey;
import com.atlassian.jira.security.GlobalPermissionManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.user.MockApplicationUser;
import com.atlassian.jira.util.ErrorCollection;
import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import org.hamcrest.Description;
import org.hamcrest.Matchers;
import org.hamcrest.TypeSafeDiagnosingMatcher;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Set;

import static com.atlassian.jira.util.NoopI18nHelper.makeTranslation;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith (MockitoJUnitRunner.class)
public class TestDefaultLicenseRoleService
{
    private static final MockApplicationUser TEST_USER = new MockApplicationUser("User");
    private static final LicenseRoleId TEST_LICENSE_ROLE_ID = new LicenseRoleId("Role");

    private static final String GROUP_NAME_1 = "Group 1";
    private static final String GROUP_NAME_2 = "Group 2";
    private static final String GROUP_NAME_3 = "Group 3";
    private static final String GROUP_INVALID = "Group That has Been Deleted";

    @Mock
    private GroupManager groupManager;
    @Mock
    private LicenseRoleManager licenseRoleManager;
    @Mock
    private GlobalPermissionManager permissionManager;

    private JiraAuthenticationContext context =
            MockSimpleAuthenticationContext.createNoopContext(TEST_USER.getDirectoryUser());

    private DefaultLicenseRoleService defaultLicenseRoleService;

    @Before
    public void setUp()
    {
        defaultLicenseRoleService = new DefaultLicenseRoleService(groupManager, licenseRoleManager, context, permissionManager);

        when(groupManager.groupExists(GROUP_NAME_1)).thenReturn(true);
        when(groupManager.groupExists(GROUP_NAME_2)).thenReturn(true);
        when(groupManager.groupExists(GROUP_NAME_3)).thenReturn(true);

        //User is an admin.
        when(permissionManager.hasPermission(GlobalPermissionKey.ADMINISTER, TEST_USER))
                .thenReturn(true);
    }

    @Test
    public void userHasRoleReturnsTrueWhenUserBelongsToGroupOfLicenseRole()
    {
        when(licenseRoleManager.getGroupsFor(TEST_LICENSE_ROLE_ID)).thenReturn(ImmutableSet.of(GROUP_NAME_1, GROUP_NAME_2, GROUP_NAME_3));
        when(groupManager.isUserInGroup(TEST_USER.getUsername(), GROUP_NAME_2)).thenReturn(true);

        assertThat(defaultLicenseRoleService.userHasRole(TEST_USER, TEST_LICENSE_ROLE_ID), is(true));
    }

    @Test
    public void userHasRoleReturnsFalseWhenUserDoesNotBelongToGroupOfLicenseRole()
    {
        when(licenseRoleManager.getGroupsFor(TEST_LICENSE_ROLE_ID)).thenReturn(ImmutableSet.of(GROUP_NAME_1, GROUP_NAME_2, GROUP_NAME_3));
        when(groupManager.isUserInGroup(eq(TEST_USER.getUsername()), isA(String.class))).thenReturn(false);

        assertThat(defaultLicenseRoleService.userHasRole(TEST_USER, TEST_LICENSE_ROLE_ID), is(false));
    }

    @Test
    public void userHasRoleReturnsFalseWhenLicenseRoleHasNoGroups()
    {
        when(licenseRoleManager.getGroupsFor(TEST_LICENSE_ROLE_ID)).thenReturn(ImmutableSet.<String>of());
        when(groupManager.isUserInGroup(eq(TEST_USER.getUsername()), isA(String.class))).thenReturn(true);

        assertThat(defaultLicenseRoleService.userHasRole(TEST_USER, TEST_LICENSE_ROLE_ID), is(false));
    }

    @Test
    public void userHasRoleReturnsFalseWhenNoUserProvided()
    {
        when(groupManager.isUserInGroup(isA(String.class), isA(String.class))).thenReturn(true);
        assertThat(defaultLicenseRoleService.userHasRole(null, TEST_LICENSE_ROLE_ID), is(false));
    }

    @Test
    public void getRolesFailsWithForbiddenForNonUsers()
    {
        when(permissionManager.hasPermission(GlobalPermissionKey.ADMINISTER, TEST_USER))
                .thenReturn(false);

        assertForbidden(defaultLicenseRoleService.getRoles());
    }

    @Test
    public void getRolesReturnsRolesCorrectly()
    {
        LicenseRoleDefinition definition1 = new MockLicenseRoleDefinition("def1");
        LicenseRoleDefinition definition2 = new MockLicenseRoleDefinition("def2");

        //These are the valid roles.
        when(licenseRoleManager.getDefinedLicenseRoles())
                .thenReturn(Sets.newHashSet(definition1, definition2));

        //These are the groups for the roles.
        when(licenseRoleManager.getGroupsFor(definition1.getLicenseRoleId()))
                .thenReturn(Sets.newHashSet(GROUP_NAME_1, GROUP_NAME_2));

        when(licenseRoleManager.getGroupsFor(definition2.getLicenseRoleId()))
                .thenReturn(Sets.<String>newHashSet());

        final ServiceOutcome<Set<LicenseRole>> roles = defaultLicenseRoleService.getRoles();
        final ValueMatcher<Set<LicenseRole>> matcher = ServiceOutcomeMatchers.equalTo(
            Matchers.containsInAnyOrder(
                    new LicenseRoleMatcher()
                            .merge(definition1)
                            .addGroups(GROUP_NAME_1, GROUP_NAME_2),
                    new LicenseRoleMatcher()
                            .merge(definition2)
            ));
        assertThat(roles, matcher);
    }

    @Test
    public void getRolesReturnsRolesFiltersOutInvalidGroups()
    {
        LicenseRoleDefinition definition1 = new MockLicenseRoleDefinition("def1");

        //These are the valid roles.
        when(licenseRoleManager.getDefinedLicenseRoles())
                .thenReturn(Sets.newHashSet(definition1));

        //These are the groups for the roles.
        when(licenseRoleManager.getGroupsFor(definition1.getLicenseRoleId()))
                .thenReturn(Sets.newHashSet(GROUP_NAME_1, GROUP_NAME_2, GROUP_INVALID));

        final ServiceOutcome<Set<LicenseRole>> roles = defaultLicenseRoleService.getRoles();
        final ValueMatcher<Set<LicenseRole>> matcher = ServiceOutcomeMatchers.equalTo(
            Matchers.containsInAnyOrder(
                    new LicenseRoleMatcher()
                            .merge(definition1)
                            .addGroups(GROUP_NAME_1, GROUP_NAME_2)
            ));
        assertThat(roles, matcher);
    }

    @Test
    public void getRoleFailsWithForbiddenForNonUsers()
    {
        when(permissionManager.hasPermission(GlobalPermissionKey.ADMINISTER, TEST_USER))
                .thenReturn(false);

        final ServiceOutcome<LicenseRole> outsome = defaultLicenseRoleService.getRole(new LicenseRoleId("w"));
        assertForbidden(outsome);
    }

    @Test
    public void getRoleReturnsRoleThatExistsAndFiltersInvalidGroups()
    {
        LicenseRoleDefinition definition1 = new MockLicenseRoleDefinition("def1");

        //The definition exists.
        when(licenseRoleManager.getLicenseRoleDefinition(definition1.getLicenseRoleId()))
                .thenReturn(Optional.of(definition1));

        //The groups for role1.
        when(licenseRoleManager.getGroupsFor(definition1.getLicenseRoleId()))
                .thenReturn(Sets.newHashSet(GROUP_NAME_1, GROUP_NAME_2, GROUP_INVALID));

        final ServiceOutcome<LicenseRole> role = defaultLicenseRoleService.getRole(definition1.getLicenseRoleId());

        //Need the explict Type for JDK6.
        assertThat(role, ServiceOutcomeMatchers.<LicenseRole>equalTo(new LicenseRoleMatcher()
                .merge(definition1)
                .addGroups(GROUP_NAME_1, GROUP_NAME_2)));
    }

    @Test
    public void getRoleReturnsErrorOnRoleThatDoesNotExist()
    {
        final LicenseRoleId id = new LicenseRoleId("def1");

        //The role does not exist.
        when(licenseRoleManager.getLicenseRoleDefinition(id))
                .thenReturn(Optional.<LicenseRoleDefinition>absent());

        final ServiceOutcome<LicenseRole> role = defaultLicenseRoleService.getRole(id);
        assertRoleDoesNotExist(id, role);
    }

    @Test
    public void setGroupsFailsForNonAdmin()
    {
        //User is an admin.
        when(permissionManager.hasPermission(GlobalPermissionKey.ADMINISTER, TEST_USER))
                .thenReturn(false);

        assertForbidden(defaultLicenseRoleService.setGroups(TEST_LICENSE_ROLE_ID, Sets.newHashSet("gou")));
    }

    @Test
    public void setGroupsFailsWhenRoleDoesNotExist()
    {
        final LicenseRoleId id = new LicenseRoleId("def1");

        //The role does not exist.
        when(licenseRoleManager.getLicenseRoleDefinition(id))
                .thenReturn(Optional.<LicenseRoleDefinition>absent());

        final ServiceOutcome<LicenseRole> outcome = defaultLicenseRoleService.setGroups(id, Sets.newHashSet("gou"));
        assertRoleDoesNotExist(id, outcome);
    }

    @Test
    public void setGroupsFailsWhenGroupsDontExist()
    {
        assertBadGroup("whatGroup");
    }

    @Test
    public void setGroupsFailsWhenGroupsContainsNulls()
    {
        assertBadGroup(null);
    }

    @Test
    public void setGroupsSavesWhenGroupsExist()
    {
        Set<String> groups = Sets.newHashSet("one", "two");
        LicenseRoleDefinition definition1 = new MockLicenseRoleDefinition("def1");
        final LicenseRoleId id = definition1.getLicenseRoleId();

        //The role does exist.
        when(licenseRoleManager.getLicenseRoleDefinition(id))
                .thenReturn(Optional.of(definition1));

        when(licenseRoleManager.getGroupsFor(id))
                .thenReturn(groups);

        //All groups exist.
        when(groupManager.groupExists(Mockito.any(String.class))).thenReturn(true);

        final ServiceOutcome<LicenseRole> outcome = defaultLicenseRoleService.setGroups(id, groups);

        //Have the groups been saved.
        verify(licenseRoleManager).setGroups(id, groups);

        assertThat(outcome, ServiceOutcomeMatchers.<LicenseRole>equalTo(
                new LicenseRoleMatcher()
                        .addGroups(groups)
                        .merge(definition1)
        ));
    }

    private void assertBadGroup(final String badGroup)
    {
        LicenseRoleDefinition definition1 = new MockLicenseRoleDefinition("def1");

        //The role does exist.
        when(licenseRoleManager.getLicenseRoleDefinition(definition1.getLicenseRoleId()))
                .thenReturn(Optional.of(definition1));

        final ServiceOutcome<LicenseRole> outcome = defaultLicenseRoleService.setGroups(definition1.getLicenseRoleId(),
                Sets.newHashSet(badGroup));

        assertThat(outcome, ServiceOutcomeMatchers.errorMatcher()
                .addReason(ErrorCollection.Reason.VALIDATION_FAILED)
                .addError("groups", makeTranslation("licenserole.service.group.does.not.exist", String.valueOf(badGroup))));
    }

    private void assertRoleDoesNotExist(final LicenseRoleId id, final ServiceOutcome<?> role)
    {
        assertThat(role, ServiceOutcomeMatchers.errorMatcher()
                .addErrorMessage(makeTranslation("licenserole.service.role.does.not.exist", id.getName()))
                .addReason(ErrorCollection.Reason.NOT_FOUND));
    }

    private void assertForbidden(final ServiceOutcome<?> outcome)
    {
        assertThat(outcome, ServiceOutcomeMatchers.errorMatcher()
                .addErrorMessage(makeTranslation("licenserole.service.permission.denied"))
                .addReason(ErrorCollection.Reason.FORBIDDEN));
    }

    public static class LicenseRoleMatcher extends TypeSafeDiagnosingMatcher<LicenseRole>
    {
        private String name;
        private Set<String> groups = Sets.newHashSet();
        private LicenseRoleId id;

        public LicenseRoleMatcher()
        {
        }

        public LicenseRoleMatcher merge(LicenseRoleDefinition definition)
        {
            id(definition.getLicenseRoleId());
            name(definition.getName());

            return this;
        }

        public LicenseRoleMatcher id(String id)
        {
            this.id = new LicenseRoleId(id);
            return this;
        }

        public LicenseRoleMatcher id(LicenseRoleId id)
        {
            this.id = id;
            return this;
        }

        public LicenseRoleMatcher name(String name)
        {
            this.name = name;
            return this;
        }

        public LicenseRoleMatcher addGroups(String...groups)
        {
            this.groups.addAll(Arrays.asList(groups));
            return this;
        }

        public LicenseRoleMatcher addGroups(Iterable<String> groups)
        {
            Iterables.addAll(this.groups, groups);
            return this;
        }

        @Override
        protected boolean matchesSafely(final LicenseRole item, final Description mismatchDescription)
        {
            if (Objects.equal(name, item.getName())
                    && Objects.equal(groups, item.getGroups())
                    && Objects.equal(id, item.getId()))
            {
                return true;
            }
            else
            {
                mismatchDescription.appendValue(String.format("[name: %s, groups: %s, id: %s]",
                        item.getName(), item.getGroups(), item.getId()));

                return false;
            }
        }

        @Override
        public void describeTo(final Description description)
        {
            description.appendText(String.format("[name: %s, groups: %s, id: %s]",
                    name, groups, id));
        }
    }
}
