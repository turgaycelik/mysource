package com.atlassian.jira.sharing.type;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.sharing.SharePermission;
import com.atlassian.jira.sharing.SharePermissionImpl;
import com.atlassian.jira.sharing.type.ShareType.Name;
import com.atlassian.jira.user.MockUser;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;


/**
 * Test for {@link com.atlassian.jira.sharing.type.GlobalShareTypePermissionChecker}.
 * 
 * @since v3.13
 */
public class TestGlobalShareTypePermissionChecker
{
    protected User user;
    private static final SharePermission GLOBAL_PERM = new SharePermissionImpl(GlobalShareType.TYPE, null, null);
    private static final SharePermission INVALID_PERM = new SharePermissionImpl(new Name("group"), "developers", null);

    @Before
    public void setUp() throws Exception
    {
        user = new MockUser("admin");
    }

    @After
    public void tearDown() throws Exception
    {
        user = null;
    }

    @Test
    public void testHasPermissionValidSharePermission()
    {
        final ShareTypePermissionChecker validator = new GlobalShareTypePermissionChecker();
        assertTrue(validator.hasPermission(user, TestGlobalShareTypePermissionChecker.GLOBAL_PERM));

    }

    @Test
    public void testHasPermissionValidSharePermissionAndNullUser()
    {
        final ShareTypePermissionChecker validator = new GlobalShareTypePermissionChecker();
        assertTrue(validator.hasPermission(null, TestGlobalShareTypePermissionChecker.GLOBAL_PERM));
    }

    @Test
    public void testHasPermissionInvalidShareType()
    {
        final ShareTypePermissionChecker validator = new GlobalShareTypePermissionChecker();

        try
        {
            validator.hasPermission(user, TestGlobalShareTypePermissionChecker.INVALID_PERM);
            fail("Permission checker should only accept global permissions.");
        }
        catch (final IllegalArgumentException e)
        {
            // expected.
        }
    }

    @Test
    public void testHasPermissionNullPermission()
    {
        final GlobalShareTypePermissionChecker validator = new GlobalShareTypePermissionChecker();

        try
        {
            validator.hasPermission(user, null);
            fail("hasPermission should not accept null permission");
        }
        catch (final IllegalArgumentException e)
        {
            // expected
        }
    }
}
