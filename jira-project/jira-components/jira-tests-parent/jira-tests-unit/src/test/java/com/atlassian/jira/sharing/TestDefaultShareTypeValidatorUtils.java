package com.atlassian.jira.sharing;

import java.util.Comparator;
import java.util.HashSet;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.MockJiraServiceContext;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.sharing.SharedEntity.SharePermissions;
import com.atlassian.jira.sharing.search.PrivateShareTypeSearchParameter;
import com.atlassian.jira.sharing.search.ShareTypeSearchParameter;
import com.atlassian.jira.sharing.type.GlobalShareType;
import com.atlassian.jira.sharing.type.GroupShareType;
import com.atlassian.jira.sharing.type.ShareQueryFactory;
import com.atlassian.jira.sharing.type.ShareType;
import com.atlassian.jira.sharing.type.ShareTypeFactory;
import com.atlassian.jira.sharing.type.ShareTypePermissionChecker;
import com.atlassian.jira.sharing.type.ShareTypeRenderer;
import com.atlassian.jira.sharing.type.ShareTypeValidator;
import com.atlassian.jira.user.MockUser;

import org.easymock.MockControl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class TestDefaultShareTypeValidatorUtils
{
    private User user;
    private MockControl typeFactoryMockCtrl;
    private ShareTypeFactory typeFactory;
    private PermissionManager permissionManager;
    private MockControl permissionManagerCtrl;
    private DefaultShareTypeValidatorUtils validator;
    private HashSet<SharePermission> permissions;

    @Before
    public void setUp() throws Exception
    {
        new MockComponentWorker().init();
        permissions = new HashSet<SharePermission>();
        user = new MockUser("admin");

        typeFactoryMockCtrl = MockControl.createStrictControl(ShareTypeFactory.class);
        typeFactory = (ShareTypeFactory) typeFactoryMockCtrl.getMock();

        permissionManagerCtrl = MockControl.createStrictControl(PermissionManager.class);
        permissionManager = (PermissionManager) permissionManagerCtrl.getMock();

        validator = new DefaultShareTypeValidatorUtils(typeFactory, permissionManager);
    }

    @After
    public void tearDown() throws Exception
    {
        permissions = null;
        user = null;
        typeFactory = null;
        typeFactoryMockCtrl.reset();
        typeFactoryMockCtrl = null;
        permissionManager = null;
        permissionManagerCtrl = null;
        validator = null;
    }

    @Test
    public void testConstructionWithNullTypeFactory()
    {
        initialiseMocks();

        try
        {
            new DefaultShareTypeValidatorUtils(null, permissionManager);
            fail("Not allowed to accept null argument.");
        }
        catch (final IllegalArgumentException e)
        {
            // expected.
        }

        validateMocks();
    }

    @Test
    public void testConstructionWithNullPermissionManager()
    {
        initialiseMocks();

        try
        {
            new DefaultShareTypeValidatorUtils(typeFactory, null);
            fail("Not allowed to accept null argument.");
        }
        catch (final IllegalArgumentException e)
        {
            // expected.
        }

        validateMocks();
    }

    @Test
    public void testIsValidSharePermissionNullEntity()
    {
        initialiseMocks();

        final JiraServiceContext ctx = new MockJiraServiceContext(user);

        try
        {
            validator.isValidSharePermission(ctx, null);
            fail("can't pass in null entity");
        }
        catch (final IllegalArgumentException e)
        {
            // expect
        }

        assertFalse(ctx.getErrorCollection().hasAnyErrors());
        validateMocks();
    }

    @Test
    public void testIsValidSharePermissionPrivatePermissions()
    {
        initialiseMocks();

        final JiraServiceContext ctx = new MockJiraServiceContext((User) null);
        final SharedEntity entity = new MockSharedEntity(1L, SearchRequest.ENTITY_TYPE, user, SharePermissions.PRIVATE);
        validator.isValidSharePermission(ctx, entity);
        assertFalse(ctx.getErrorCollection().hasAnyErrors());

        validateMocks();
    }

    @Test
    public void testIsValidSharePermissionNullPermissionType()
    {
        final JiraServiceContext ctx = new MockJiraServiceContext(user);
        permissions.add(new SharePermissionImpl(new ShareType.Name("Nick"), null, null));

        typeFactory.getShareType(new ShareType.Name("Nick"));
        typeFactoryMockCtrl.setReturnValue(null);

        permissionManager.hasPermission(Permissions.CREATE_SHARED_OBJECTS, user);
        permissionManagerCtrl.setReturnValue(true);

        initialiseMocks();

        final SharedEntity entity = new MockSharedEntity(1L, SearchRequest.ENTITY_TYPE, user, new SharePermissions(permissions));
        validator.isValidSharePermission(ctx, entity);
        assertTrue(ctx.getErrorCollection().hasAnyErrors());

        validateMocks();
    }

    @Test
    public void testIsValidSharePermissionSingletonWithMoreThan1()
    {
        final JiraServiceContext ctx = new MockJiraServiceContext(user);
        permissions.add(new SharePermissionImpl(GlobalShareType.TYPE, null, null));
        permissions.add(new SharePermissionImpl(GlobalShareType.TYPE, "aah", null));

        typeFactory.getShareType(GlobalShareType.TYPE);
        typeFactoryMockCtrl.setReturnValue(getSingletonType(null));
        typeFactory.getShareType(GlobalShareType.TYPE);
        typeFactoryMockCtrl.setReturnValue(getSingletonType(null));

        permissionManager.hasPermission(Permissions.CREATE_SHARED_OBJECTS, user);
        permissionManagerCtrl.setReturnValue(true);

        initialiseMocks();

        final SharedEntity entity = new MockSharedEntity(1L, SearchRequest.ENTITY_TYPE, user, new SharePermissions(permissions));
        validator.isValidSharePermission(ctx, entity);
        assertTrue(ctx.getErrorCollection().hasAnyErrors());

        validateMocks();
    }

    @Test
    public void testIsValidSharePermissionSingletonWith1()
    {
        final JiraServiceContext ctx = new MockJiraServiceContext(user);
        permissions.add(new SharePermissionImpl(GlobalShareType.TYPE, null, null));

        typeFactory.getShareType(GlobalShareType.TYPE);
        typeFactoryMockCtrl.setReturnValue(getSingletonType(null));

        permissionManager.hasPermission(Permissions.CREATE_SHARED_OBJECTS, user);
        permissionManagerCtrl.setReturnValue(true);

        initialiseMocks();

        final SharedEntity entity = new MockSharedEntity(1L, SearchRequest.ENTITY_TYPE, user, new SharePermissions(permissions));
        validator.isValidSharePermission(ctx, entity);
        assertFalse(ctx.getErrorCollection().hasAnyErrors());

        validateMocks();
    }

    @Test
    public void testIsValidSharePermissionSingletonWith1AndError()
    {
        final JiraServiceContext ctx = new MockJiraServiceContext(user);
        permissions.add(new SharePermissionImpl(GlobalShareType.TYPE, null, null));

        typeFactory.getShareType(GlobalShareType.TYPE);
        typeFactoryMockCtrl.setReturnValue(getSingletonType("error"));

        permissionManager.hasPermission(Permissions.CREATE_SHARED_OBJECTS, user);
        permissionManagerCtrl.setReturnValue(true);

        initialiseMocks();

        final SharedEntity entity = new MockSharedEntity(1L, SearchRequest.ENTITY_TYPE, user, new SharePermissions(permissions));
        validator.isValidSharePermission(ctx, entity);
        assertTrue(ctx.getErrorCollection().hasAnyErrors());

        validateMocks();
    }

    @Test
    public void testIsValidSharePermissionMultipleNonSingletons()
    {
        final JiraServiceContext ctx = new MockJiraServiceContext(user);
        permissions.add(new SharePermissionImpl(GroupShareType.TYPE, "jira-user", null));
        permissions.add(new SharePermissionImpl(GroupShareType.TYPE, "jira-developer", null));

        typeFactory.getShareType(GroupShareType.TYPE);
        typeFactoryMockCtrl.setReturnValue(getNonSingletonType(null));

        typeFactory.getShareType(GroupShareType.TYPE);
        typeFactoryMockCtrl.setReturnValue(getNonSingletonType(null));

        permissionManager.hasPermission(Permissions.CREATE_SHARED_OBJECTS, user);
        permissionManagerCtrl.setReturnValue(true);

        initialiseMocks();

        final SharedEntity entity = new MockSharedEntity(1L, SearchRequest.ENTITY_TYPE, user, new SharePermissions(permissions));
        validator.isValidSharePermission(ctx, entity);
        assertFalse(ctx.getErrorCollection().hasAnyErrors());

        validateMocks();
    }

    @Test
    public void testIsValidSharePermissionNonSingletonWith1()
    {
        final JiraServiceContext ctx = new MockJiraServiceContext(user);
        permissions.add(new SharePermissionImpl(GroupShareType.TYPE, "jira-user", null));

        typeFactory.getShareType(GroupShareType.TYPE);
        typeFactoryMockCtrl.setReturnValue(getNonSingletonType(null));

        permissionManager.hasPermission(Permissions.CREATE_SHARED_OBJECTS, user);
        permissionManagerCtrl.setReturnValue(true);

        initialiseMocks();

        final SharedEntity entity = new MockSharedEntity(1L, SearchRequest.ENTITY_TYPE, user, new SharePermissions(permissions));
        validator.isValidSharePermission(ctx, entity);
        assertFalse(ctx.getErrorCollection().hasAnyErrors());

        validateMocks();
    }

    @Test
    public void testIsValidSharePermissionNonSingletonWith1AndError()
    {
        final JiraServiceContext ctx = new MockJiraServiceContext(user);
        permissions.add(new SharePermissionImpl(GroupShareType.TYPE, "jira-user", null));

        typeFactory.getShareType(GroupShareType.TYPE);
        typeFactoryMockCtrl.setReturnValue(getNonSingletonType("error"));

        permissionManager.hasPermission(Permissions.CREATE_SHARED_OBJECTS, user);
        permissionManagerCtrl.setReturnValue(true);

        initialiseMocks();

        final SharedEntity entity = new MockSharedEntity(1L, SearchRequest.ENTITY_TYPE, user, new SharePermissions(permissions));
        validator.isValidSharePermission(ctx, entity);

        assertTrue(ctx.getErrorCollection().hasAnyErrors());

        validateMocks();
    }

    @Test
    public void testIsValidSharePermissionNoSharePermission()
    {
        final JiraServiceContext ctx = new MockJiraServiceContext(user);

        permissions.add(new SharePermissionImpl(GroupShareType.TYPE, "jira-user", null));

        permissionManager.hasPermission(Permissions.CREATE_SHARED_OBJECTS, user);
        permissionManagerCtrl.setReturnValue(false);

        initialiseMocks();

        final SharedEntity entity = new MockSharedEntity(1L, SearchRequest.ENTITY_TYPE, user, new SharePermissions(permissions));
        validator.isValidSharePermission(ctx, entity);

        assertTrue(ctx.getErrorCollection().hasAnyErrors());

        validateMocks();
    }

    @Test
    public void testIsValidSearchParameterNullTypeReturned()
    {
        final JiraServiceContext ctx = new MockJiraServiceContext(user);

        typeFactory.getShareType(PrivateShareTypeSearchParameter.PRIVATE_PARAMETER.getType());
        typeFactoryMockCtrl.setReturnValue(null);

        initialiseMocks();

        final boolean result = validator.isValidSearchParameter(ctx, PrivateShareTypeSearchParameter.PRIVATE_PARAMETER);
        assertFalse(result);
        assertTrue(ctx.getErrorCollection().hasAnyErrors());

        validateMocks();
    }

    @Test
    public void testIsValidSearchParameterCallsShareType()
    {
        final JiraServiceContext ctx = new MockJiraServiceContext(user);

        final MockControl mockShareTypeControl = MockControl.createControl(ShareType.class);
        final ShareType mockShareType = (ShareType) mockShareTypeControl.getMock();

        final MockControl mockShareTypeValidatorControl = MockControl.createControl(ShareTypeValidator.class);
        final ShareTypeValidator mockShareTypeValidator = (ShareTypeValidator) mockShareTypeValidatorControl.getMock();

        typeFactory.getShareType(PrivateShareTypeSearchParameter.PRIVATE_PARAMETER.getType());
        typeFactoryMockCtrl.setReturnValue(mockShareType);

        mockShareType.getValidator();
        mockShareTypeControl.setReturnValue(mockShareTypeValidator);

        mockShareTypeValidator.checkSearchParameter(ctx, PrivateShareTypeSearchParameter.PRIVATE_PARAMETER);
        mockShareTypeValidatorControl.setReturnValue(true);

        initialiseMocks();
        mockShareTypeControl.replay();
        mockShareTypeValidatorControl.replay();

        final boolean result = validator.isValidSearchParameter(ctx, PrivateShareTypeSearchParameter.PRIVATE_PARAMETER);
        assertTrue(result);
        assertFalse(ctx.getErrorCollection().hasAnyErrors());

        validateMocks();
        mockShareTypeControl.verify();
        mockShareTypeValidatorControl.verify();
    }

    @Test
    public void testIsValidSearchParameterBadArgs()
    {
        final JiraServiceContext ctx = new MockJiraServiceContext(user);
        try
        {
            validator.isValidSearchParameter(null, PrivateShareTypeSearchParameter.PRIVATE_PARAMETER);
            fail("Should not allow null context");
        }
        catch (final IllegalArgumentException e)
        {
            // expected
        }
        try
        {
            validator.isValidSearchParameter(ctx, null);
            fail("Should not allow null search parameter");
        }
        catch (final IllegalArgumentException e)
        {
            // expected
        }

    }

    private void initialiseMocks()
    {
        typeFactoryMockCtrl.replay();
        permissionManagerCtrl.replay();
    }

    private void validateMocks()
    {
        typeFactoryMockCtrl.verify();
        permissionManagerCtrl.verify();
    }

    private ShareType getNonSingletonType(final String validatorError)
    {
        return new ShareType()
        {
            public ShareType.Name getType()
            {
                return null;
            }

            public boolean isSingleton()
            {
                return false;
            }

            public int getPriority()
            {
                return 0;
            }

            public ShareTypeRenderer getRenderer()
            {
                return null;
            }

            public ShareTypeValidator getValidator()
            {
                return getShareValidator(validatorError);
            }

            public ShareTypePermissionChecker getPermissionsChecker()
            {
                return null;
            }

            public Comparator<SharePermission> getComparator()
            {
                return null;
            }

            public ShareQueryFactory<? extends ShareTypeSearchParameter> getQueryFactory()
            {
                return null;
            }
        };
    }

    private ShareType getSingletonType(final String validatorError)
    {
        return new ShareType()
        {
            public ShareType.Name getType()
            {
                return null;
            }

            public boolean isSingleton()
            {
                return true;
            }

            public int getPriority()
            {
                return 0;
            }

            public ShareTypeRenderer getRenderer()
            {
                return null;
            }

            public ShareTypeValidator getValidator()
            {
                return getShareValidator(validatorError);
            }

            public ShareTypePermissionChecker getPermissionsChecker()
            {
                return null;
            }

            public Comparator<SharePermission> getComparator()
            {
                return null;
            }

            public ShareQueryFactory<? extends ShareTypeSearchParameter> getQueryFactory()
            {
                return null;
            }
        };
    }

    private ShareTypeValidator getShareValidator(final String error)
    {
        return new ShareTypeValidator()
        {
            public boolean checkSharePermission(final JiraServiceContext ctx, final SharePermission permission)
            {
                if (error != null)
                {
                    ctx.getErrorCollection().addErrorMessage(error);
                    return false;
                }
                else
                {
                    return true;
                }
            }

            public boolean checkSearchParameter(final JiraServiceContext ctx, final ShareTypeSearchParameter searchParameter)
            {
                return false;
            }
        };
    }
}
