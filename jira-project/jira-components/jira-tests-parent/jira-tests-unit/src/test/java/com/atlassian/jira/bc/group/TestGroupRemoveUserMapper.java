package com.atlassian.jira.bc.group;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.atlassian.core.util.collection.EasyList;

import org.apache.commons.collections.IteratorUtils;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Tests the GroupRemoveChildMapper class.
 *
 * @since v3.12
 */
public class TestGroupRemoveUserMapper
{
    @Test
    public void testConstructor()
    {
        try
        {
            new GroupRemoveChildMapper(null);
            fail("expected exception");
        }
        catch (Exception yay)
        {
        }
    }

    @Test
    public void testRegisterAll()
    {
        GroupRemoveChildMapper groupRemoveChildMapper = new GroupRemoveChildMapper(EasyList.build("foogroup"));
        groupRemoveChildMapper.register("frank");
        assertTrue(groupRemoveChildMapper.isRemoveFromAllSelected("frank"));
        Iterator groupsIterator = groupRemoveChildMapper.getGroupsIterator("frank");
        assertEquals("foogroup", groupsIterator.next());
        assertFalse(groupsIterator.hasNext());

        Iterator users = groupRemoveChildMapper.childIterator();
        assertEquals("frank", users.next());
        assertFalse(users.hasNext());

        assertEquals(EasyList.build("foogroup"), groupRemoveChildMapper.getDefaultGroupNames());
    }

    @Test
    public void testRegisterMultiple()
    {
        GroupRemoveChildMapper groupRemoveChildMapper = new GroupRemoveChildMapper(EasyList.build("foogroup"));
        groupRemoveChildMapper.register("frank");
        groupRemoveChildMapper.register("barney", "barneysGroup");
        
        assertTrue(groupRemoveChildMapper.isRemoveFromAllSelected("frank"));
        assertFalse(groupRemoveChildMapper.isRemoveFromAllSelected("barney"));
        Iterator groupsIterator = groupRemoveChildMapper.getGroupsIterator("frank");
        assertEquals("foogroup", groupsIterator.next());
        assertFalse(groupsIterator.hasNext());

        Iterator users = groupRemoveChildMapper.childIterator();
        List userList = (List) IteratorUtils.toList(users);

        assertTrue(userList.contains("frank"));
        assertTrue(userList.contains("barney"));
        assertEquals(2, userList.size());
    }

    @Test
    public void testRegisterUserToRemoveFromGroup()
    {
        GroupRemoveChildMapper groupRemoveChildMapper = new GroupRemoveChildMapper(Collections.EMPTY_LIST);
        groupRemoveChildMapper.register("user1", "group1");
        groupRemoveChildMapper.register("user1", "group2");
        groupRemoveChildMapper.register("user2", "group1");

        assertFalse(groupRemoveChildMapper.isRemoveFromAllSelected("user1"));
        assertFalse(groupRemoveChildMapper.isRemoveFromAllSelected("user2"));

        List user1Groups = IteratorUtils.toList(groupRemoveChildMapper.getGroupsIterator("user1"));
        assertTrue(user1Groups.contains("group1"));
        assertTrue(user1Groups.contains("group2"));
        assertEquals(2, user1Groups.size());

        List user2Groups = IteratorUtils.toList(groupRemoveChildMapper.getGroupsIterator("user2"));
        assertTrue(user2Groups.contains("group1"));
        assertEquals(1, user2Groups.size());

    }
}
