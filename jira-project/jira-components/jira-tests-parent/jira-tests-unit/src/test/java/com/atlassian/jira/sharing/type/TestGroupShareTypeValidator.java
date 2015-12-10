package com.atlassian.jira.sharing.type;

import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.junit.rules.MockComponentContainer;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.security.groups.MockGroupManager;
import com.atlassian.jira.sharing.SharePermission;
import com.atlassian.jira.sharing.SharePermissionImpl;
import com.atlassian.jira.sharing.search.GroupShareTypeSearchParameter;
import com.atlassian.jira.sharing.search.PrivateShareTypeSearchParameter;
import com.atlassian.jira.sharing.type.ShareType.Name;
import com.atlassian.jira.user.MockGroup;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.web.bean.MockI18nBean;

import org.easymock.MockControl;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Unit tests for {@link com.atlassian.jira.sharing.type.GroupShareTypeValidator}
 * 
 * @since v3.13
 */
public class TestGroupShareTypeValidator
{
    private static final SharePermission GROUP_PERM = new SharePermissionImpl(GroupShareType.TYPE, "test", null);
    private static final SharePermission INVALID_PERM = new SharePermissionImpl(new Name("global"), null, null);

    @Rule
    public TestRule testRule = new MockComponentContainer(this);

    private User user;
    private MockControl permissionMgrControl;
    private PermissionManager permissionMgr;
    private JiraServiceContext ctx;
    protected Group group;
    protected Group notInGroup;

    @Before
    public void setUp() throws Exception
    {
        new MockComponentWorker().init();

        user = new MockUser("test");
        group = new MockGroup("test");
        notInGroup = new MockGroup("NicksCoolGroup");
        permissionMgrControl = MockControl.createStrictControl(PermissionManager.class);
        permissionMgr = (PermissionManager) permissionMgrControl.getMock();

        ctx = createServiceContext(user);
    }

    @After
    public void tearDown() throws Exception
    {
        user = null;
        permissionMgr = null;
        permissionMgrControl = null;
        ctx = null;
        group = null;
        notInGroup = null;

    }

    private void initialiseMocks()
    {
        permissionMgrControl.replay();
    }

    private GroupShareTypeValidator createValidator()
    {
        initialiseMocks();

        return new GroupShareTypeValidator(permissionMgr, null);
    }

    private void verifyMocks()
    {
        permissionMgrControl.verify();
    }

    @Test
    public void testCheckSharePermissionNullContext()
    {
        final ShareTypeValidator validator = createValidator();
        try
        {
            validator.checkSharePermission(null, TestGroupShareTypeValidator.GROUP_PERM);
            fail("Should have failed for null ctx");
        }
        catch (final IllegalArgumentException e)
        {
            // expected
        }

        assertFalse(ctx.getErrorCollection().hasAnyErrors());
        verifyMocks();
    }

    @Test
    public void testCheckSharePermissionNullParam1()
    {
        final ShareTypeValidator validator = createValidator();
        try
        {
            validator.checkSharePermission(null, new SharePermissionImpl(new Name("group"), null, null));
            fail("Should have failed for null ctx");
        }
        catch (final IllegalArgumentException e)
        {
            // expected
        }

        assertFalse(ctx.getErrorCollection().hasAnyErrors());
        verifyMocks();
    }

    @Test
    public void testCheckSharePermissionNullUserInContext()
    {
        final ShareTypeValidator validator = createValidator();
        final JiraServiceContext nullUserCtx = createServiceContext(null);

        try
        {
            validator.checkSharePermission(nullUserCtx, TestGroupShareTypeValidator.GROUP_PERM);
            fail("Should have failed for null user in ctx");
        }
        catch (final IllegalArgumentException e)
        {
            // expected
        }
        assertFalse(ctx.getErrorCollection().hasAnyErrors());
        verifyMocks();
    }

    @Test
    public void testCheckSharePermissionNullPermission()
    {
        final ShareTypeValidator validator = createValidator();

        try
        {
            validator.checkSharePermission(ctx, null);
            fail("Should have failed for null permission");
        }
        catch (final IllegalArgumentException e)
        {
            // expected
        }

        assertFalse(ctx.getErrorCollection().hasAnyErrors());
        verifyMocks();
    }

    @Test
    public void testCheckSharePermissionInvalidType()
    {
        final ShareTypeValidator validator = createValidator();

        try
        {
            validator.checkSharePermission(ctx, TestGroupShareTypeValidator.INVALID_PERM);
            fail("Should have failed for invalid permission");
        }
        catch (final IllegalArgumentException e)
        {
            // expected
        }

        assertFalse(ctx.getErrorCollection().hasAnyErrors());
        verifyMocks();
    }

    @Test
    public void testCheckSharePermissionHappy()
    {
        permissionMgr.hasPermission(Permissions.CREATE_SHARED_OBJECTS, ctx.getLoggedInUser());
        permissionMgrControl.setReturnValue(true);

        initialiseMocks();

        final MockGroupManager mockGroupManager = new MockGroupManager();
        mockGroupManager.addMember("test", "test");
        final ShareTypeValidator validator = new GroupShareTypeValidator(permissionMgr, mockGroupManager)
        {
            Group getGroup(final String groupName)
            {
                return group;
            }
        };
        assertTrue(validator.checkSharePermission(ctx, TestGroupShareTypeValidator.GROUP_PERM));
        assertFalse(ctx.getErrorCollection().hasAnyErrors());

        verifyMocks();
    }

    @Test
    public void testCheckSharePermissionNoPermission()
    {
        permissionMgr.hasPermission(Permissions.CREATE_SHARED_OBJECTS, ctx.getLoggedInUser());
        permissionMgrControl.setReturnValue(false);

        final ShareTypeValidator validator = createValidator();
        assertFalse(validator.checkSharePermission(ctx, TestGroupShareTypeValidator.GROUP_PERM));
        assertTrue(ctx.getErrorCollection().hasAnyErrors());

        verifyMocks();
    }

    @Test
    public void testCheckSharePermissionNotInGroup()
    {
        permissionMgr.hasPermission(Permissions.CREATE_SHARED_OBJECTS, ctx.getLoggedInUser());
        permissionMgrControl.setReturnValue(true);

        initialiseMocks();

        final ShareTypeValidator validator = new GroupShareTypeValidator(permissionMgr, new MockGroupManager())
        {
            Group getGroup(final String groupName)
            {
                return notInGroup;
            }
        };
        assertFalse(validator.checkSharePermission(ctx, TestGroupShareTypeValidator.GROUP_PERM));
        assertTrue(ctx.getErrorCollection().hasAnyErrors());

        verifyMocks();
    }

    @Test
    public void testCheckSharePermissionGroupDoesNotExist()
    {
        permissionMgr.hasPermission(Permissions.CREATE_SHARED_OBJECTS, ctx.getLoggedInUser());
        permissionMgrControl.setReturnValue(true);

        initialiseMocks();

        final ShareTypeValidator validator = new GroupShareTypeValidator(permissionMgr, new MockGroupManager())
        {
            Group getGroup(final String groupName)
            {
                return null;
            }
        };
        assertFalse(validator.checkSharePermission(ctx, TestGroupShareTypeValidator.GROUP_PERM));
        assertTrue(ctx.getErrorCollection().hasAnyErrors());

        verifyMocks();
    }

    @Test
    public void testCheckSharePermissionEmptyGroupShare()
    {
        permissionMgr.hasPermission(Permissions.CREATE_SHARED_OBJECTS, ctx.getLoggedInUser());
        permissionMgrControl.setReturnValue(true);

        final SharePermission invalidPerm = new SharePermissionImpl(GroupShareType.TYPE, "", null);
        final ShareTypeValidator validator = createValidator();

        assertFalse(validator.checkSharePermission(ctx, invalidPerm));
        assertTrue(ctx.getErrorCollection().hasAnyErrors());

        verifyMocks();
    }

    @Test
    public void testCheckSharePermissionNullGroupShare()
    {
        permissionMgr.hasPermission(Permissions.CREATE_SHARED_OBJECTS, ctx.getLoggedInUser());
        permissionMgrControl.setReturnValue(true);

        final SharePermission invalidPerm = new SharePermissionImpl(GroupShareType.TYPE, null, null);
        final ShareTypeValidator validator = createValidator();

        assertFalse(validator.checkSharePermission(ctx, invalidPerm));
        assertTrue(ctx.getErrorCollection().hasAnyErrors());

        verifyMocks();
    }

    /**
     * Make sure that it does not accept invalid search parameter.
     */
    @Test
    public void testCheckSearchParameterIllegalArgument()
    {
        final ShareTypeValidator validator = createValidator();

        try
        {
            validator.checkSearchParameter(ctx, PrivateShareTypeSearchParameter.PRIVATE_PARAMETER);
            fail("Should not accept invalid argument.");
        }
        catch (final IllegalArgumentException e)
        {
            // expected.
        }

        verifyMocks();
    }

    /**
     * Make sure that it return error for the anonymous user.
     */
    @Test
    public void testCheckSearchParameterNullUser()
    {
        final ShareTypeValidator validator = createValidator();
        final JiraServiceContext nullUserCtx = createServiceContext(null);

        final boolean result = validator.checkSearchParameter(nullUserCtx, new GroupShareTypeSearchParameter("users"));
        assertFalse(result);
        assertTrue(nullUserCtx.getErrorCollection().hasAnyErrors());

        verifyMocks();
    }

    /**
     * Test to make sure that we validate global group search.
     */
    @Test
    public void testCheckSearchParameterNullGroup()
    {
        final ShareTypeValidator validator = createValidator();
        final boolean result = validator.checkSearchParameter(ctx, new GroupShareTypeSearchParameter(null));
        assertTrue(result);
        assertFalse(ctx.getErrorCollection().hasAnyErrors());

        verifyMocks();
    }

    /**
     * Test to make sure that a parameter works.
     */
    @Test
    public void testCheckSearchParameterGoodParameter()
    {
        initialiseMocks();

        final MockGroupManager mockGroupManager = new MockGroupManager();
        mockGroupManager.addMember("test", "test");
        final ShareTypeValidator validator = new GroupShareTypeValidator(permissionMgr, mockGroupManager)
        {
            Group getGroup(final String groupName)
            {
                return group;
            }
        };

        final boolean result = validator.checkSearchParameter(ctx, new GroupShareTypeSearchParameter(group.getName()));
        assertTrue(result);
        assertFalse(ctx.getErrorCollection().hasAnyErrors());

        verifyMocks();
    }

    /**
     * Test to make sure that an illegal group messes things up.
     */
    @Test
    public void testCheckSearchParameterBadGroup()
    {
        initialiseMocks();

        final ShareTypeValidator validator = new GroupShareTypeValidator(permissionMgr, null)
        {
            @Override
            Group getGroup(final String groupName)
            {
                assertEquals(groupName, group.getName());
                return null;
            }
        };

        final boolean result = validator.checkSearchParameter(ctx, new GroupShareTypeSearchParameter(group.getName()));
        assertFalse(result);
        assertTrue(ctx.getErrorCollection().hasAnyErrors());

        verifyMocks();
    }

    /**
     * Test to make sure that a user can't search using a group they are not a member of.
     */
    @Test
    public void testCheckSearchParameterGroupNotMember()
    {
        initialiseMocks();

        final ShareTypeValidator validator = new GroupShareTypeValidator(permissionMgr, new MockGroupManager())
        {
            Group getGroup(final String groupName)
            {
                assertEquals(groupName, notInGroup.getName());
                return notInGroup;
            }
        };

        final boolean result = validator.checkSearchParameter(ctx, new GroupShareTypeSearchParameter(notInGroup.getName()));
        assertFalse(result);
        assertTrue(ctx.getErrorCollection().hasAnyErrors());

        verifyMocks();
    }

    private JiraServiceContext createServiceContext(final User user)
    {
        return new JiraServiceContextImpl(user, new SimpleErrorCollection())
        {
            public I18nHelper getI18nBean()
            {
                return new MockI18nBean()
                {
                    public String getText(final String key)
                    {
                        return key;
                    }

                    public String getText(final String key, final String value1)
                    {
                        return key;
                    }

                    public String getText(final String key, final String value1, final String value2)
                    {
                        return key;
                    }

                    public String getText(final String key, final String value1, final String value2, final String value3)
                    {
                        return key;
                    }
                };
            }
        };
    }
}
