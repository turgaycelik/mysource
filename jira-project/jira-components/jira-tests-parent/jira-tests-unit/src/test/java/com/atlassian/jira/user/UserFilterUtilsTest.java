package com.atlassian.jira.user;

import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.jira.mock.MockProjectRoleManager;
import com.atlassian.jira.security.groups.MockGroupManager;
import com.google.common.collect.ImmutableSet;
import org.junit.Before;
import org.junit.Test;

import java.util.Set;

import static com.atlassian.jira.mock.MockProjectRoleManager.PROJECT_ROLE_TYPE_1;
import static com.atlassian.jira.mock.MockProjectRoleManager.PROJECT_ROLE_TYPE_2;
import static com.atlassian.jira.mock.MockProjectRoleManager.PROJECT_ROLE_TYPE_3;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

/**
 * @since v6.2
 */
public class UserFilterUtilsTest
{
    private MockGroupManager groupManager = new MockGroupManager();
    private MockProjectRoleManager projectRoleManager = new MockProjectRoleManager();

    private Group group1 = new MockGroup("group1");
    private Group group2 = new MockGroup("group2");
    private Group group3 = new MockGroup("group3");

    @Before
    public void setUp() throws Exception
    {
        groupManager.addGroup("group1");
        groupManager.addGroup("group2");
        // MockProjectRoleManager has 3 default roles
    }

    @Test
    public void testFilterRemovedGroupsNoRemovedGroup()
    {
        final Set<String> expected = ImmutableSet.of("group1", "group2");
        assertThat(UserFilterUtils.filterRemovedGroups(ImmutableSet.<String>of("group1", "group2"), ImmutableSet.<Group>of(group1, group2, group3)),
                equalTo(expected));
    }

    @Test
    public void testFilterRemovedGroupsRemoveGroup()
    {
        final Set<String> expected = ImmutableSet.of("group1", "group2");
        assertThat(UserFilterUtils.filterRemovedGroups(ImmutableSet.<String>of("group1", "group2", "group4"), ImmutableSet.<Group>of(group1, group2, group3)),
                equalTo(expected));
    }

    @Test
    public void testFilterRemovedGroupsEmptyGroups()
    {
        final Set<String> expected = ImmutableSet.of();
        assertThat(UserFilterUtils.filterRemovedGroups(ImmutableSet.<String>of(), ImmutableSet.<Group>of(group1, group2, group3)),
                equalTo(expected));
    }

    @Test
    public void testFilterRemovedRoleIdsNoRemoval()
    {
        final Set<Long> expected = ImmutableSet.of(PROJECT_ROLE_TYPE_2.getId(), PROJECT_ROLE_TYPE_1.getId());
        assertThat(UserFilterUtils.filterRemovedRoleIds(ImmutableSet.of(PROJECT_ROLE_TYPE_1.getId(), PROJECT_ROLE_TYPE_2.getId()), projectRoleManager),
                equalTo(expected));
    }

    @Test
    public void testFilterRemovedRoleIdsRemoval()
    {
        final Set<Long> expected = ImmutableSet.of(PROJECT_ROLE_TYPE_2.getId(), PROJECT_ROLE_TYPE_1.getId());
        assertThat(UserFilterUtils.filterRemovedRoleIds(ImmutableSet.of(PROJECT_ROLE_TYPE_1.getId(), PROJECT_ROLE_TYPE_2.getId(), PROJECT_ROLE_TYPE_3.getId() + 1), projectRoleManager),
                equalTo(expected));
    }

    @Test
    public void testFilterRemovedRoleIdsEmpty()
    {
        final Set<Long> expected = ImmutableSet.of();
        assertThat(UserFilterUtils.filterRemovedRoleIds(ImmutableSet.<Long>of(), projectRoleManager),
                equalTo(expected));
    }
}
