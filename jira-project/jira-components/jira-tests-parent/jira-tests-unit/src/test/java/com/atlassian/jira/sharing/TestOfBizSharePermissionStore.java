package com.atlassian.jira.sharing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.ofbiz.core.entity.GenericValue;

import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.mock.ofbiz.MockOfBizDelegator;
import com.atlassian.jira.ofbiz.PrimitiveMap;
import com.atlassian.jira.sharing.SharedEntity.SharePermissions;
import com.atlassian.jira.sharing.type.ShareType;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.DelegatingApplicationUser;
import com.atlassian.jira.user.MockApplicationUser;

/**
 * Integration test for OfBizStore
 *
 * @since v3.13
 */
public class TestOfBizSharePermissionStore
{

    @Rule
    public RuleChain mockitoMocksInContainer = MockitoMocksInContainer.forTest(this);

    private static final String SHARE_PERMISSIONS = "SharePermissions";
    private static final String ID = "id";
    private static final String ENTITY_ID = "entityId";
    private static final String ENTITY_TYPE = "entityType";
    private static final String TYPE = "type";
    private static final String PARAM1 = "param1";
    private static final String PARAM2 = "param2";

    private static final class Type
    {
        static final ShareType.Name GLOBAL = new ShareType.Name("global");
        static final ShareType.Name GROUP = new ShareType.Name("group");
        static final ShareType.Name PROJECT = new ShareType.Name("project");
    }

    private static final SharePermissionImpl GLOBAL_PERMISSION = new SharePermissionImpl(Type.GLOBAL, null, null);
    private static final SharePermissionImpl GROUP_PERMISSION_1 = new SharePermissionImpl(Type.GROUP, "group1", null);
    private static final SharePermissionImpl GROUP_PERMISSION_2 = new SharePermissionImpl(Type.GROUP, "group2", null);
    private static final SharePermissionImpl PROJECT_PERMISSION_1 = new SharePermissionImpl(Type.PROJECT, "project1", "role1");

    private static final SharePermissionImpl PROJECT_PERMISSION_1_NO_ROLE = new SharePermissionImpl(Type.PROJECT, "project1", null);
    private static final SharePermissionImpl PROJECT_PERMISSION_2_NO_ROLE = new SharePermissionImpl(Type.PROJECT, "project2", null);
    private static final SharePermissionImpl PROJECT_PERMISSION_1_ROLE_1 = new SharePermissionImpl(Type.PROJECT, "project1", "role1");
    private static final SharePermissionImpl PROJECT_PERMISSION_1_ROLE_2 = new SharePermissionImpl(Type.PROJECT, "project1", "role2");
    private static final SharePermissionImpl PROJECT_PERMISSION_2_ROLE_1 = new SharePermissionImpl(Type.PROJECT, "project2", "role1");

    private static final SharePermissionImpl ROLE_1_PERMIMSSION_ONLY = new SharePermissionImpl(Type.PROJECT, "role1");

    private final ApplicationUser user = new MockApplicationUser("admin");

    private SharedEntity SEARCH_ENTITY_1;
    private SharedEntity SEARCH_ENTITY_2;

    @AvailableInContainer
    private MockOfBizDelegator delegator = new MockOfBizDelegator();

    private OfBizSharePermissionStore store = null;

    @Before
    public void setUp() throws Exception
    {
        SEARCH_ENTITY_1 = new StupidEntity(new Long(1), SearchRequest.ENTITY_TYPE, user.getDirectoryUser());
        SEARCH_ENTITY_2 = new SharedEntity.Identifier(new Long(2), SearchRequest.ENTITY_TYPE, user);
        store = new OfBizSharePermissionStore(delegator);

    }

    @Test
    public void testGetSharePermsNullEntity()
    {
        try
        {
            store.getSharePermissions(null);
            fail("Should fail with null entity");
        }
        catch (final IllegalArgumentException e)
        {
            // Expected
        }

    }

    @Test
    public void testGetSharePermsNullEntityId()
    {
        try
        {
            store.getSharePermissions(new StupidEntity(null, "Test"));
            fail("should have failed with null entity Id");
        }
        catch (final IllegalArgumentException e)
        {
            // expected
        }

    }

    @Test
    public void testGetSharePermsNullEntityType()
    {
        try
        {
            store.getSharePermissions(new StupidEntity(new Long(1), null));
            fail("should have failed with null entity type");
        }
        catch (final IllegalArgumentException e)
        {
            // expected
        }
    }

    @Test
    public void testGetSharePermsNoPermissions()
    {
        final SharePermissions perms = store.getSharePermissions(SEARCH_ENTITY_1);
        assertNotNull(perms);
        assertTrue(perms.isEmpty());
    }

    @Test
    public void testGetSharePermsNoMatchedPermissions()
    {
        addSharePermission(TestOfBizSharePermissionStore.GLOBAL_PERMISSION, SEARCH_ENTITY_1);
        addSharePermission(TestOfBizSharePermissionStore.GROUP_PERMISSION_1, SEARCH_ENTITY_1);
        addSharePermission(TestOfBizSharePermissionStore.GROUP_PERMISSION_2, SEARCH_ENTITY_1);

        final SharePermissions returnedPermissions = store.getSharePermissions(SEARCH_ENTITY_2);
        assertNotNull(returnedPermissions);
        assertTrue(returnedPermissions.isEmpty());
    }

    @Test
    public void testGetSharePermsMatchedPermissions()
    {
        addSharePermission(TestOfBizSharePermissionStore.GLOBAL_PERMISSION, SEARCH_ENTITY_1);
        addSharePermission(TestOfBizSharePermissionStore.GROUP_PERMISSION_1, SEARCH_ENTITY_1);
        addSharePermission(TestOfBizSharePermissionStore.GROUP_PERMISSION_2, SEARCH_ENTITY_1);
        addSharePermission(TestOfBizSharePermissionStore.GROUP_PERMISSION_2, SEARCH_ENTITY_2);

        final SharePermissions returnedPermissions = store.getSharePermissions(SEARCH_ENTITY_1);
        assertNotNull(returnedPermissions);
        assertEquals(3, returnedPermissions.size());

        assertTrue(returnedPermissions.getPermissionSet().contains(TestOfBizSharePermissionStore.GLOBAL_PERMISSION));
        assertTrue(returnedPermissions.getPermissionSet().contains(TestOfBizSharePermissionStore.GROUP_PERMISSION_1));
        assertTrue(returnedPermissions.getPermissionSet().contains(TestOfBizSharePermissionStore.GROUP_PERMISSION_2));
    }

    @Test
    public void testDeleteSharePermsInvalidEntity()
    {

        try
        {
            store.deleteSharePermissions((SharedEntity) null);
            fail("Should fail with null entity");
        }
        catch (final IllegalArgumentException e)
        {
            // Expected
        }

        try
        {
            store.deleteSharePermissions(new StupidEntity(null, "Test"));
            fail("should have failed with null entity Id");
        }
        catch (final IllegalArgumentException e)
        {
            // expected
        }

        try
        {
            store.deleteSharePermissions(new StupidEntity(new Long(1), null));
            fail("should have failed with null entity type");
        }
        catch (final IllegalArgumentException e)
        {
            // expected
        }
    }

    @Test
    public void testDeleteSharePermsNoPermissions()
    {
        final int perms = store.deleteSharePermissions(SEARCH_ENTITY_1);
        assertEquals(0, perms);

        final Collection<GenericValue> allPerms = delegator.findAll(TestOfBizSharePermissionStore.SHARE_PERMISSIONS);
        assertNotNull(allPerms);
        assertTrue(allPerms.isEmpty());
    }

    @Test
    public void testDeleteSharePermsNoMatchedPermissions()
    {
        addSharePermission(TestOfBizSharePermissionStore.GLOBAL_PERMISSION, SEARCH_ENTITY_1);
        addSharePermission(TestOfBizSharePermissionStore.GROUP_PERMISSION_1, SEARCH_ENTITY_1);
        addSharePermission(TestOfBizSharePermissionStore.GROUP_PERMISSION_2, SEARCH_ENTITY_1);

        final int perms = store.deleteSharePermissions(SEARCH_ENTITY_2);
        assertEquals(0, perms);

        assertPermissionsSize(3);
        assertPermissionsForEntity(
                SEARCH_ENTITY_1,
                new SharePermissions(new HashSet<SharePermission>(Arrays.asList(TestOfBizSharePermissionStore.GLOBAL_PERMISSION,
                        TestOfBizSharePermissionStore.GROUP_PERMISSION_1, TestOfBizSharePermissionStore.GROUP_PERMISSION_2))));
    }

    @Test
    public void testDeleteSharePermsMatchedPermissions()
    {
        addSharePermission(TestOfBizSharePermissionStore.GLOBAL_PERMISSION, SEARCH_ENTITY_1);
        addSharePermission(TestOfBizSharePermissionStore.GROUP_PERMISSION_1, SEARCH_ENTITY_1);
        addSharePermission(TestOfBizSharePermissionStore.GROUP_PERMISSION_2, SEARCH_ENTITY_1);
        addSharePermission(TestOfBizSharePermissionStore.PROJECT_PERMISSION_1, SEARCH_ENTITY_2);

        final int perms = store.deleteSharePermissions(SEARCH_ENTITY_1);
        assertEquals(3, perms);

        assertPermissionsSize(1);

        assertPermissionsForEntity(SEARCH_ENTITY_1, SharePermissions.PRIVATE);
        assertPermissionsForEntity(SEARCH_ENTITY_2, new SharePermissions(Collections.singleton(TestOfBizSharePermissionStore.PROJECT_PERMISSION_1)));
    }

    @Test
    public void testStoreSharePermsInvalidEntity()
    {
        try
        {
            store.storeSharePermissions(null);
            fail("Should fail with null entity");
        }
        catch (final IllegalArgumentException e)
        {
            // Expected
        }

        try
        {
            store.storeSharePermissions(new StupidEntity(null, "Test"));
            fail("should have failed with null entity Id");
        }
        catch (final IllegalArgumentException e)
        {
            // expected
        }

        try
        {
            store.storeSharePermissions(new StupidEntity(new Long(1), null));
            fail("should have failed with null entity type");
        }
        catch (final IllegalArgumentException e)
        {
            // expected
        }
    }

    @Test
    public void testStoreSharePermsNoneInNullPassed()
    {
        final SharePermissions perms = store.storeSharePermissions(SEARCH_ENTITY_1);
        assertNotNull(perms);
        assertTrue(perms.isEmpty());

        assertPermissionsSize(0);
        assertPermissionsForEntity(SEARCH_ENTITY_1, SharePermissions.PRIVATE);
    }

    @Test
    public void testStoreSharePermsNoneInEmptyCollection()
    {
        final SharePermissions perms = store.storeSharePermissions(SEARCH_ENTITY_1);
        assertNotNull(perms);
        assertTrue(perms.isEmpty());

        assertPermissionsSize(0);
        assertPermissionsForEntity(SEARCH_ENTITY_1, SharePermissions.PRIVATE);
    }

    @Test
    public void testStoreSharePermsAllCreate()
    {
        final SharePermissions expectedPerms = new SharePermissions(new HashSet<SharePermission>(Arrays.asList(TestOfBizSharePermissionStore.GROUP_PERMISSION_1, TestOfBizSharePermissionStore.GROUP_PERMISSION_2)));
        final SharedEntity entity = new StupidEntity(new Long(4), SearchRequest.ENTITY_TYPE, user.getDirectoryUser(), expectedPerms);
        final SharePermissions returnedPerms = store.storeSharePermissions(entity);

        assertPermissionsSize(2);
        assertPermissionsForEntity(entity, expectedPerms);
        assertEquals(expectedPerms, returnedPerms);
    }

    @Test
    public void testStoreSharePermsOverWrite()
    {
        final SharePermissions expectedPerms = new SharePermissions(new HashSet<SharePermission>(Arrays.asList(TestOfBizSharePermissionStore.GROUP_PERMISSION_1, TestOfBizSharePermissionStore.PROJECT_PERMISSION_1, TestOfBizSharePermissionStore.GROUP_PERMISSION_2)));
        final SharedEntity dashBoard = new StupidEntity(new Long(4), SharedEntity.TypeDescriptor.Factory.get().create("Dashboard"), user.getDirectoryUser(), expectedPerms);

        addSharePermission(TestOfBizSharePermissionStore.GLOBAL_PERMISSION, dashBoard);
        addSharePermission(TestOfBizSharePermissionStore.GROUP_PERMISSION_1, dashBoard);

        final SharePermissions actualPerms = store.storeSharePermissions(dashBoard);

        assertPermissionsSize(3);
        assertPermissionsForEntity(dashBoard, expectedPerms);
        assertEquals(expectedPerms, actualPerms);
    }
    
    @Test
    public void testStoreSharePermsOtherEntitiesOverWrite()
    {
        final SharePermissions expectedPermissions = new SharePermissions(new HashSet<SharePermission>(Arrays.asList(TestOfBizSharePermissionStore.GROUP_PERMISSION_1, TestOfBizSharePermissionStore.GROUP_PERMISSION_2)));
        final SharedEntity dashBoard = new StupidEntity(new Long(4), SharedEntity.TypeDescriptor.Factory.get().create("Dashboard"), user.getDirectoryUser(), expectedPermissions);

        addSharePermission(TestOfBizSharePermissionStore.GLOBAL_PERMISSION, SEARCH_ENTITY_1);
        addSharePermission(TestOfBizSharePermissionStore.GROUP_PERMISSION_1, SEARCH_ENTITY_1);
        addSharePermission(TestOfBizSharePermissionStore.PROJECT_PERMISSION_1, dashBoard);

        final SharePermissions actualPermissions = store.storeSharePermissions(dashBoard);

        assertPermissionsSize(4);
        assertPermissionsForEntity(dashBoard, expectedPermissions);
        assertEquals(expectedPermissions, actualPermissions);

        assertPermissionsForEntity(dashBoard, new SharePermissions(Collections.singleton(TestOfBizSharePermissionStore.PROJECT_PERMISSION_1)));
    }

    @Test
    public void testStoreSharePermsOverrideWithNull()
    {
        addSharePermission(TestOfBizSharePermissionStore.GLOBAL_PERMISSION, SEARCH_ENTITY_1);
        addSharePermission(TestOfBizSharePermissionStore.GROUP_PERMISSION_1, SEARCH_ENTITY_1);
        addSharePermission(TestOfBizSharePermissionStore.PROJECT_PERMISSION_1, SEARCH_ENTITY_2);

        final SharePermissions returnedPermissions = store.storeSharePermissions(SEARCH_ENTITY_1);

        assertTrue(returnedPermissions.isEmpty());
        assertPermissionsSize(1);
        assertPermissionsForEntity(SEARCH_ENTITY_1, SharePermissions.PRIVATE);
        assertPermissionsForEntity(SEARCH_ENTITY_2, new SharePermissions(Collections.singleton(TestOfBizSharePermissionStore.PROJECT_PERMISSION_1)));
    }

    @Test
    public void testDeleteSharePermissionsLikeDudImput() throws Exception
    {
        try
        {
            store.deleteSharePermissionsLike(null);
            fail("Should fail with null");
        }
        catch (final IllegalArgumentException e)
        {
            // Expected
        }
        try
        {
            final SharePermission sharePermission = new SharePermission()
            {
                public Long getId()
                {
                    return null;
                }

                public ShareType.Name getType()
                {
                    return null;
                }

                public String getParam1()
                {
                    return null;
                }

                public String getParam2()
                {
                    return null;
                }
            };
            store.deleteSharePermissionsLike(sharePermission);
            fail("Should fail with all null");
        }
        catch (final IllegalArgumentException e)
        {
            // Expected
        }

        try
        {
            final SharePermission sharePermission = new SharePermission()
            {
                public Long getId()
                {
                    return null;
                }

                public ShareType.Name getType()
                {
                    return Type.GROUP;
                }

                public String getParam1()
                {
                    return null;
                }

                public String getParam2()
                {
                    return null;
                }
            };
            store.deleteSharePermissionsLike(sharePermission);
            fail("Should fail with null");
        }
        catch (final IllegalArgumentException e)
        {
            // Expected
        }
    }

    @Test
    public void testDeleteSharePermissionLikeGroups() throws Exception
    {
        addSharePermission(TestOfBizSharePermissionStore.GLOBAL_PERMISSION, SEARCH_ENTITY_1);
        addSharePermission(TestOfBizSharePermissionStore.GROUP_PERMISSION_1, SEARCH_ENTITY_1);
        addSharePermission(TestOfBizSharePermissionStore.GROUP_PERMISSION_2, SEARCH_ENTITY_1);
        addSharePermission(TestOfBizSharePermissionStore.GROUP_PERMISSION_2, SEARCH_ENTITY_2);

        final int affected = store.deleteSharePermissionsLike(TestOfBizSharePermissionStore.GROUP_PERMISSION_2);
        assertEquals(2, affected);

        assertPermissionsSize(2);

        SharePermissions returnedPermissions = store.getSharePermissions(SEARCH_ENTITY_1);
        assertNotNull(returnedPermissions);
        assertEquals(2, returnedPermissions.size());

        assertTrue(returnedPermissions.getPermissionSet().contains(TestOfBizSharePermissionStore.GLOBAL_PERMISSION));
        assertTrue(returnedPermissions.getPermissionSet().contains(TestOfBizSharePermissionStore.GROUP_PERMISSION_1));

        returnedPermissions = store.getSharePermissions(SEARCH_ENTITY_2);
        assertNotNull(returnedPermissions);
        assertEquals(0, returnedPermissions.size());

    }

    @Test
    public void testDeleteSharePermissionLikeProjects() throws Exception
    {
        addSharePermission(TestOfBizSharePermissionStore.GLOBAL_PERMISSION, SEARCH_ENTITY_1);
        addSharePermission(TestOfBizSharePermissionStore.PROJECT_PERMISSION_1_NO_ROLE, SEARCH_ENTITY_1);
        addSharePermission(TestOfBizSharePermissionStore.PROJECT_PERMISSION_2_NO_ROLE, SEARCH_ENTITY_1);
        addSharePermission(TestOfBizSharePermissionStore.PROJECT_PERMISSION_1_ROLE_1, SEARCH_ENTITY_1);
        addSharePermission(TestOfBizSharePermissionStore.PROJECT_PERMISSION_1_ROLE_2, SEARCH_ENTITY_1);
        addSharePermission(TestOfBizSharePermissionStore.GROUP_PERMISSION_2, SEARCH_ENTITY_2);

        int affected = store.deleteSharePermissionsLike(TestOfBizSharePermissionStore.PROJECT_PERMISSION_1_ROLE_1);
        assertEquals(1, affected);

        assertPermissionsSize(5);

        SharePermissions returnedPermissions = store.getSharePermissions(SEARCH_ENTITY_1);
        assertNotNull(returnedPermissions);
        assertEquals(4, returnedPermissions.size());

        assertTrue(returnedPermissions.getPermissionSet().contains(TestOfBizSharePermissionStore.GLOBAL_PERMISSION));
        assertTrue(returnedPermissions.getPermissionSet().contains(TestOfBizSharePermissionStore.PROJECT_PERMISSION_1_NO_ROLE));
        assertTrue(returnedPermissions.getPermissionSet().contains(TestOfBizSharePermissionStore.PROJECT_PERMISSION_2_NO_ROLE));
        assertTrue(returnedPermissions.getPermissionSet().contains(TestOfBizSharePermissionStore.PROJECT_PERMISSION_1_ROLE_2));

        returnedPermissions = store.getSharePermissions(SEARCH_ENTITY_2);
        assertNotNull(returnedPermissions);
        assertEquals(1, returnedPermissions.size());
        assertTrue(returnedPermissions.getPermissionSet().contains(TestOfBizSharePermissionStore.GROUP_PERMISSION_2));

        // now delete a higher level project and make sure it has wider affects
        affected = store.deleteSharePermissionsLike(TestOfBizSharePermissionStore.PROJECT_PERMISSION_1_NO_ROLE);
        assertEquals(2, affected);

        assertPermissionsSize(3);

        returnedPermissions = store.getSharePermissions(SEARCH_ENTITY_1);
        assertNotNull(returnedPermissions);
        assertEquals(2, returnedPermissions.size());

        assertTrue(returnedPermissions.getPermissionSet().contains(TestOfBizSharePermissionStore.GLOBAL_PERMISSION));
        assertTrue(returnedPermissions.getPermissionSet().contains(TestOfBizSharePermissionStore.PROJECT_PERMISSION_2_NO_ROLE));
    }

    /**
     * This test when a role has been deleted across all projects
     */
    @Test
    public void testDeleteSharePermissionLikeProjectRoles() throws Exception
    {
        addSharePermission(TestOfBizSharePermissionStore.GLOBAL_PERMISSION, SEARCH_ENTITY_1);
        addSharePermission(TestOfBizSharePermissionStore.PROJECT_PERMISSION_1_NO_ROLE, SEARCH_ENTITY_1);
        addSharePermission(TestOfBizSharePermissionStore.PROJECT_PERMISSION_2_NO_ROLE, SEARCH_ENTITY_1);
        addSharePermission(TestOfBizSharePermissionStore.PROJECT_PERMISSION_1_ROLE_1, SEARCH_ENTITY_1); // * gonesky
        addSharePermission(TestOfBizSharePermissionStore.PROJECT_PERMISSION_1_ROLE_2, SEARCH_ENTITY_1);
        addSharePermission(TestOfBizSharePermissionStore.PROJECT_PERMISSION_1_ROLE_1, SEARCH_ENTITY_2); // * gonesky
        addSharePermission(TestOfBizSharePermissionStore.PROJECT_PERMISSION_2_ROLE_1, SEARCH_ENTITY_1); // * gonesky
        addSharePermission(TestOfBizSharePermissionStore.PROJECT_PERMISSION_1_ROLE_2, SEARCH_ENTITY_2);
        addSharePermission(TestOfBizSharePermissionStore.GROUP_PERMISSION_2, SEARCH_ENTITY_2);

        final int affected = store.deleteSharePermissionsLike(TestOfBizSharePermissionStore.ROLE_1_PERMIMSSION_ONLY);
        assertEquals(3, affected);

        assertPermissionsSize(6);

        SharePermissions returnedPermissions = store.getSharePermissions(SEARCH_ENTITY_1);
        assertNotNull(returnedPermissions);
        assertEquals(4, returnedPermissions.size());

        assertTrue(returnedPermissions.getPermissionSet().contains(TestOfBizSharePermissionStore.GLOBAL_PERMISSION));
        assertTrue(returnedPermissions.getPermissionSet().contains(TestOfBizSharePermissionStore.PROJECT_PERMISSION_1_NO_ROLE));
        assertTrue(returnedPermissions.getPermissionSet().contains(TestOfBizSharePermissionStore.PROJECT_PERMISSION_2_NO_ROLE));
        assertTrue(returnedPermissions.getPermissionSet().contains(TestOfBizSharePermissionStore.PROJECT_PERMISSION_1_ROLE_2));

        returnedPermissions = store.getSharePermissions(SEARCH_ENTITY_2);
        assertNotNull(returnedPermissions);
        assertEquals(2, returnedPermissions.size());
        assertTrue(returnedPermissions.getPermissionSet().contains(TestOfBizSharePermissionStore.PROJECT_PERMISSION_1_ROLE_2));
        assertTrue(returnedPermissions.getPermissionSet().contains(TestOfBizSharePermissionStore.GROUP_PERMISSION_2));

    }

    @Test
    public void testDeleteSharePermissionsLikeNoMatchMade() throws Exception
    {
        addSharePermission(TestOfBizSharePermissionStore.GLOBAL_PERMISSION, SEARCH_ENTITY_1);
        addSharePermission(TestOfBizSharePermissionStore.PROJECT_PERMISSION_1_NO_ROLE, SEARCH_ENTITY_1);
        addSharePermission(TestOfBizSharePermissionStore.PROJECT_PERMISSION_2_NO_ROLE, SEARCH_ENTITY_1);
        addSharePermission(TestOfBizSharePermissionStore.PROJECT_PERMISSION_1_ROLE_1, SEARCH_ENTITY_1);
        addSharePermission(TestOfBizSharePermissionStore.PROJECT_PERMISSION_1_ROLE_2, SEARCH_ENTITY_1);
        addSharePermission(TestOfBizSharePermissionStore.GROUP_PERMISSION_2, SEARCH_ENTITY_2);

        // group permission is not in
        final int affected = store.deleteSharePermissionsLike(TestOfBizSharePermissionStore.GROUP_PERMISSION_1);
        assertEquals(0, affected);

        assertPermissionsSize(6);
    }

    private void assertPermissionsForEntity(final SharedEntity entity, final SharePermissions expectedPermissions)
    {
        final SharePermissions returnedPermissions = store.getSharePermissions(entity);
        assertNotNull(returnedPermissions);

        for (final Iterator<SharePermission> iterator = returnedPermissions.iterator(); iterator.hasNext();)
        {
            final SharePermission sharePermission = iterator.next();
            assertNotNull(sharePermission.getId());
        }

        final SharePermissions rawRetrieved = getPermissions(entity);
        assertNotNull(rawRetrieved);
        assertEquals(returnedPermissions, rawRetrieved);
    }

    private void assertPermissionsSize(final int size)
    {
        final List<GenericValue> gvs = delegator.findAll(TestOfBizSharePermissionStore.SHARE_PERMISSIONS);
        assertNotNull(gvs);
        assertEquals(size, gvs.size());
    }

    private GenericValue addSharePermission(final SharePermission permission, final SharedEntity entity)
    {
        final PrimitiveMap.Builder builder = new PrimitiveMap.Builder();
        builder.add(TestOfBizSharePermissionStore.ID, permission.getId());
        builder.add(TestOfBizSharePermissionStore.ENTITY_ID, entity.getId());
        builder.add(TestOfBizSharePermissionStore.ENTITY_TYPE, entity.getEntityType().getName());
        builder.add(TestOfBizSharePermissionStore.TYPE, permission.getType().get());
        builder.add(TestOfBizSharePermissionStore.PARAM1, permission.getParam1());
        builder.add(TestOfBizSharePermissionStore.PARAM2, permission.getParam2());

        return UtilsForTests.getTestEntity(TestOfBizSharePermissionStore.SHARE_PERMISSIONS, builder.toMap());
    }

    private SharePermissions getPermissions(final SharedEntity entity)
    {
        final List<GenericValue> gvs = delegator.findByAnd(TestOfBizSharePermissionStore.SHARE_PERMISSIONS, new PrimitiveMap.Builder().add(TestOfBizSharePermissionStore.ENTITY_ID, entity.getId()).add(TestOfBizSharePermissionStore.ENTITY_TYPE, entity.getEntityType().getName()).toMap());
        final Set<SharePermission>permissions = new HashSet<SharePermission>(gvs.size());
        for (final Iterator<GenericValue> iterator = gvs.iterator(); iterator.hasNext();)
        {
            final GenericValue genericValue = (GenericValue) iterator.next();
            permissions.add(makeSharePermission(genericValue));
        }
        return new SharePermissions(permissions);
    }

    private static SharePermission makeSharePermission(final GenericValue gv)
    {
        return new SharePermissionImpl(gv.getLong(TestOfBizSharePermissionStore.ID), new ShareType.Name(gv.getString(TestOfBizSharePermissionStore.TYPE)), gv.getString(TestOfBizSharePermissionStore.PARAM1), gv.getString(TestOfBizSharePermissionStore.PARAM2));
    }

    private static class StupidEntity implements SharedEntity
    {
        private final Long id;
        private final SharedEntity.TypeDescriptor<?> entityType;
        private final ApplicationUser owner;
        private final SharePermissions sharePermissions;

        StupidEntity(final Long id, final String entityType)
        {
            this.id = id;
            this.entityType = SharedEntity.TypeDescriptor.Factory.get().create(entityType);
            sharePermissions = SharePermissions.PRIVATE;
            owner = null;
        }

        StupidEntity(final Long id, final TypeDescriptor<?> entityType, final User owner)
        {
            this.id = id;
            this.entityType = entityType;
            this.owner = new DelegatingApplicationUser(owner.getName(), owner);
            sharePermissions = SharePermissions.PRIVATE;
        }

        StupidEntity(final Long id, final TypeDescriptor<?> entityType, final User owner, final SharePermissions sharePermissions)
        {
            this.id = id;
            this.entityType = entityType;
            this.owner = new DelegatingApplicationUser(owner.getName(), owner);
            this.sharePermissions = sharePermissions;
        }

        public Long getId()
        {
            return id;
        }

        @SuppressWarnings("unchecked")
        public SharedEntity.TypeDescriptor<?> getEntityType()
        {
            return entityType;
        }

        public ApplicationUser getOwner()
        {
            return owner;
        }

        public String getName()
        {
            throw new UnsupportedOperationException();
        }

        public String getDescription()
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getOwnerUserName()
        {
            return owner.getUsername();
        }

        public SharePermissions getPermissions()
        {
            return sharePermissions;
        }

        public Long getFavouriteCount()
        {
            return null;
        }
    }
}
