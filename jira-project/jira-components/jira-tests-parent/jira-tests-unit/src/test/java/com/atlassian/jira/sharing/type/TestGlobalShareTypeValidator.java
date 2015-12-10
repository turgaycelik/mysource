package com.atlassian.jira.sharing.type;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.bc.MockJiraServiceContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.sharing.SharePermission;
import com.atlassian.jira.sharing.SharePermissionImpl;
import com.atlassian.jira.sharing.search.GlobalShareTypeSearchParameter;
import com.atlassian.jira.sharing.search.PrivateShareTypeSearchParameter;
import com.atlassian.jira.sharing.type.ShareType.Name;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.web.bean.MockI18nBean;

import com.mockobjects.dynamic.Mock;
import com.mockobjects.dynamic.P;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Unit tests for {@link GlobalShareTypeValidator}
 * 
 * @since v3.13
 */
public class TestGlobalShareTypeValidator
{
    protected User user;
    protected Mock permMgrMock;
    private static final SharePermission GLOBAL_PERM = new SharePermissionImpl(GlobalShareType.TYPE, null, null);
    private static final SharePermission INVALID_PERM = new SharePermissionImpl(new Name("group"), "developers", null);
    protected JiraServiceContext ctx;

    @Before
    public void setUp() throws Exception
    {
        user = new MockUser("admin");
        permMgrMock = new Mock(PermissionManager.class);
        permMgrMock.setStrict(true);
        ctx = new MockJiraServiceContext(user);
    }

    @After
    public void tearDown() throws Exception
    {
        user = null;
        permMgrMock = null;
        ctx = null;
    }

    @Test
    public void testCheckSharePermissionWithValidSharePermissionAndPermission()
    {

        permMgrMock.expectAndReturn("hasPermission", P.args(P.eq(new Integer(Permissions.CREATE_SHARED_OBJECTS)), P.eq(user)), Boolean.TRUE);
        final PermissionManager permissionManager = (PermissionManager) permMgrMock.proxy();
        final ShareTypeValidator validator = new GlobalShareTypeValidator(permissionManager);

        assertTrue(validator.checkSharePermission(ctx, TestGlobalShareTypeValidator.GLOBAL_PERM));

        assertFalse(ctx.getErrorCollection().hasAnyErrors());

        permMgrMock.verify();
    }

    @Test
    public void testCheckSharePermissionWithValidSharePermissionAndNoPermission()
    {
        permMgrMock.expectAndReturn("hasPermission", P.args(P.eq(new Integer(Permissions.CREATE_SHARED_OBJECTS)), P.eq(user)), Boolean.FALSE);
        final PermissionManager permissionManager = (PermissionManager) permMgrMock.proxy();
        final ShareTypeValidator validator = new GlobalShareTypeValidator(permissionManager);

        assertFalse(validator.checkSharePermission(ctx, TestGlobalShareTypeValidator.GLOBAL_PERM));
        assertTrue(ctx.getErrorCollection().hasAnyErrors());

        permMgrMock.verify();
    }

    @Test
    public void testCheckSharePermissionWithNoValidSharePermission()
    {
        // Short circuit means this wont be called
        // permMgrMock.expectAndReturn("hasPermission", P.args(P.eq(new Integer(Permissions.CREATE_SHARED_OBJECTS)), P.eq(user)), Boolean.TRUE);
        final PermissionManager permissionManager = (PermissionManager) permMgrMock.proxy();
        final ShareTypeValidator validator = new GlobalShareTypeValidator(permissionManager);

        try
        {
            validator.checkSharePermission(ctx, TestGlobalShareTypeValidator.INVALID_PERM);
            fail("checkSharePermission should have thrown IllegalArgumentException illegal permission");
        }
        catch (final IllegalArgumentException e)
        {
            // expected
        }

        assertFalse(ctx.getErrorCollection().hasAnyErrors());
        permMgrMock.verify();
    }

    @Test
    public void testCheckSharePermissionNullPermission()
    {
        final ShareTypeValidator validator = new GlobalShareTypeValidator(null);

        try
        {
            validator.checkSharePermission(ctx, null);
            fail("checkSharePermission should not accept null permission");
        }
        catch (final IllegalArgumentException e)
        {
            // expected
        }
        assertFalse(ctx.getErrorCollection().hasAnyErrors());
        permMgrMock.verify();
    }

    @Test
    public void testCheckSharePermissionNullContext()
    {
        final ShareTypeValidator validator = new GlobalShareTypeValidator(null);

        try
        {
            validator.checkSharePermission(null, TestGlobalShareTypeValidator.GLOBAL_PERM);
            fail("checkSharePermission should not accept null ctx");
        }
        catch (final IllegalArgumentException e)
        {
            // expected
        }
        assertFalse(ctx.getErrorCollection().hasAnyErrors());
        permMgrMock.verify();
    }

    @Test
    public void testCheckSharePermissionNullUserInContext()
    {
        permMgrMock.expectAndReturn("hasPermission", P.args(P.eq(new Integer(Permissions.CREATE_SHARED_OBJECTS)), P.IS_NULL), Boolean.FALSE);
        final ShareTypeValidator validator = new GlobalShareTypeValidator((PermissionManager) permMgrMock.proxy());

        final JiraServiceContext nullUserCtx = new JiraServiceContextImpl((User) null) {
            @Override
            public I18nHelper getI18nBean()
            {
                return new MockI18nBean();
            }
        };

        assertFalse(validator.checkSharePermission(nullUserCtx, TestGlobalShareTypeValidator.GLOBAL_PERM));
        assertFalse(ctx.getErrorCollection().hasAnyErrors());

        permMgrMock.expectAndReturn("hasPermission", P.args(P.eq(new Integer(Permissions.CREATE_SHARED_OBJECTS)), P.IS_NULL), Boolean.TRUE);
        assertTrue(validator.checkSharePermission(nullUserCtx, TestGlobalShareTypeValidator.GLOBAL_PERM));
        assertFalse(ctx.getErrorCollection().hasAnyErrors());
        permMgrMock.verify();
    }

    @Test
    public void testCheckGoodUser()
    {
        final ShareTypeValidator validator = new GlobalShareTypeValidator(null);

        final boolean result = validator.checkSearchParameter(ctx, GlobalShareTypeSearchParameter.GLOBAL_PARAMETER);
        assertTrue(result);
    }

    @Test
    public void testCheckAnonymous()
    {
        final ShareTypeValidator validator = new GlobalShareTypeValidator(null);
        final JiraServiceContext nullUserCtx = new JiraServiceContextImpl((User) null);

        final boolean result = validator.checkSearchParameter(nullUserCtx, GlobalShareTypeSearchParameter.GLOBAL_PARAMETER);
        assertTrue(result);

    }

    @Test
    public void testInvalidArgument()
    {
        final ShareTypeValidator validator = new GlobalShareTypeValidator(null);

        try
        {
            validator.checkSearchParameter(ctx, PrivateShareTypeSearchParameter.PRIVATE_PARAMETER);
            fail("Should not accept invalid search parameter.");
        }
        catch (final IllegalArgumentException e)
        {
            // expected.
        }
    }
}
