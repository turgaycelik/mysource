package com.atlassian.jira.security.groups;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.Query;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.user.MockCrowdService;
import com.atlassian.jira.user.MockGroup;
import com.atlassian.jira.user.MockUser;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TestDefaultGroupManager
{
    @Test
    public void testIsUserInGroup() throws Exception
    {
        final MockCrowdService mockCrowdService = new MockCrowdService();
        DefaultGroupManager groupManager = new DefaultGroupManager(mockCrowdService);
        assertFalse(groupManager.isUserInGroup(new MockUser("fred"), new MockGroup("dudes")));

        mockCrowdService.addUserToGroup(new MockUser("fred"), new MockGroup("dudes"));
        assertTrue(groupManager.isUserInGroup(new MockUser("fred"), new MockGroup("dudes")));
    }

    @Test
    public void testIsUserInGroupHandlesNulls() throws Exception
    {
        // Need to handle null user and null group in order to maintain behaviour from OSUser.
        DefaultGroupManager groupManager = new DefaultGroupManager(null);
        assertFalse(groupManager.isUserInGroup(null, new MockGroup("dudes")));
        assertFalse(groupManager.isUserInGroup(new MockUser("fred"), null));
        assertFalse(groupManager.isUserInGroup((String) null, null));
        assertFalse(groupManager.isUserInGroup((User) null, null));
    }

    @Test
    public void testGetAllGroupsPreservesIterableOrder() throws Exception
    {
        final List<Group> orderedGroups = Arrays.<Group>asList(new MockGroup("alpha"), new MockGroup("beta"), new MockGroup("gamma"), new MockGroup("delta"));
        DefaultGroupManager manager = new DefaultGroupManager(new MockCrowdService() {
            {
                for (Group group : orderedGroups) addGroup(group);
            }

            @Override
            public <T> Iterable<T> search(Query<T> query)
            {
                return new Iterable<T>() {
                    @Override
                    public Iterator<T> iterator()
                    {
                        //noinspection unchecked
                        return (Iterator<T>) orderedGroups.iterator();
                    }
                };
            }
        });

        Collection<Group> actualGroups = manager.getAllGroups();
        assertEquals(orderedGroups, new ArrayList<Group>(actualGroups));
    }
}
