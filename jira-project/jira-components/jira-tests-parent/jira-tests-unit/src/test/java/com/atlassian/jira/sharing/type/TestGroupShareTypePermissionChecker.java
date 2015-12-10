package com.atlassian.jira.sharing.type;

import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.security.groups.MockGroupManager;
import com.atlassian.jira.sharing.SharePermission;
import com.atlassian.jira.sharing.SharePermissionImpl;
import com.atlassian.jira.sharing.type.ShareType.Name;
import com.atlassian.jira.user.MockGroup;
import com.atlassian.jira.user.MockUser;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Unit tests for {@link com.atlassian.jira.sharing.type.GroupShareTypePermissionChecker}
 * 
 * @since v3.13
 */
public class TestGroupShareTypePermissionChecker
{
    private static final SharePermission GROUP_PERM = new SharePermissionImpl(GroupShareType.TYPE, "test", null);
    private static final SharePermission OTHER_GROUP_PERM = new SharePermissionImpl(GroupShareType.TYPE, "NicksCoolGroup", null);
    private static final SharePermission INVALID_PERM = new SharePermissionImpl(new Name("global"), null, null);

    private User user;
    protected Group group;
    protected Group notInGroup;
    private MockGroupManager mockGroupManager = new MockGroupManager();

    @Before
    public void setUp() throws Exception
    {
        user = new MockUser("test");
        group = new MockGroup("test");
        group = new MockGroup("NicksCoolGroup");
        mockGroupManager.addGroup("test");
        mockGroupManager.addGroup("NicksCoolGroup");
        mockGroupManager.addMember("test", "test");
    }

    @After
    public void tearDown() throws Exception
    {
        user = null;
        group = null;
        notInGroup = null;
    }

    @Test
    public void testHasPermissionNullPermission()
    {
        final ShareTypePermissionChecker checker = new GroupShareTypePermissionChecker(null);

        try
        {
            checker.hasPermission(user, null);
            fail("permission can not be null");
        }
        catch (final IllegalArgumentException e)
        {
            // expected
        }
    }

    @Test
    public void testHasPermissionWithNullParam1()
    {
        final ShareTypePermissionChecker checker = new GroupShareTypePermissionChecker(null);

        try
        {
            final SharePermission perm = new SharePermissionImpl(GroupShareType.TYPE, null, null);
            checker.hasPermission(user, perm);
            fail("permission.param1 can not be null");
        }
        catch (final IllegalArgumentException e)
        {
            // expected
        }
    }

    @Test
    public void testHasPermissionInvalidPermission()
    {
        final ShareTypePermissionChecker checker = new GroupShareTypePermissionChecker(null);

        try
        {
            checker.hasPermission(user, TestGroupShareTypePermissionChecker.INVALID_PERM);
            fail("permission must be of type: " + GroupShareType.TYPE);
        }
        catch (final IllegalArgumentException e)
        {
            // expected
        }
    }

    @Test
    public void testHasPermissionNullUser()
    {
        final ShareTypePermissionChecker checker = new GroupShareTypePermissionChecker(null);
        assertFalse(checker.hasPermission(null, TestGroupShareTypePermissionChecker.GROUP_PERM));
    }

    @Test
    public void testHasPermissionIsInGroup()
    {
        final ShareTypePermissionChecker checker = new GroupShareTypePermissionChecker(mockGroupManager);
        assertTrue(checker.hasPermission(user, TestGroupShareTypePermissionChecker.GROUP_PERM));
    }

    @Test
    public void testHasPermissionIsNotInGroup()
    {
        final ShareTypePermissionChecker checker = new GroupShareTypePermissionChecker(mockGroupManager);
        assertFalse(checker.hasPermission(user, TestGroupShareTypePermissionChecker.OTHER_GROUP_PERM));
    }
}
