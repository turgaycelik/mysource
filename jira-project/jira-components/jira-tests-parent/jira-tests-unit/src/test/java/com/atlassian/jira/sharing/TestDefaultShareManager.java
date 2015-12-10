package com.atlassian.jira.sharing;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.sharing.SharedEntity.SharePermissions;
import com.atlassian.jira.sharing.type.GlobalShareType;
import com.atlassian.jira.sharing.type.GroupShareType;
import com.atlassian.jira.sharing.type.ShareType;
import com.atlassian.jira.sharing.type.ShareTypeFactory;
import com.atlassian.jira.sharing.type.ShareTypePermissionChecker;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.DelegatingApplicationUser;
import com.atlassian.jira.user.MockUser;

import com.mockobjects.dynamic.Mock;
import com.mockobjects.dynamic.P;

import org.apache.commons.collections.set.ListOrderedSet;
import org.easymock.MockControl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Test for {@link com.atlassian.jira.sharing.TestDefaultShareManager}.
 *
 * @since v3.13
 */

public class TestDefaultShareManager
{
    private static final SharePermission GLOBAL_PERM = new SharePermissionImpl(new ShareType.Name("global"), null, null);
    private static final SharePermission GROUP_PERM = new SharePermissionImpl(new ShareType.Name("group"), "jira-user", null);
    private SharedEntity SEARCH_ENTITY_1;
    private User user;
    private User other;
    private MockControl shareTypeCtrl;
    private ShareType shareType;
    private MockControl shareTypeFactoryCtrl;
    private ShareTypeFactory shareTypeFactory;
    private MockControl checkerCtrl;
    private ShareTypePermissionChecker checker;

    @Before
    public void setUp() throws Exception
    {
        new MockComponentWorker().init();
        user = new MockUser("admin");
        other = new MockUser("other");
        SEARCH_ENTITY_1 = new StupidEntity(new Long(1), SearchRequest.ENTITY_TYPE, user);
        shareTypeCtrl = MockControl.createStrictControl(ShareType.class);
        shareType = (ShareType) shareTypeCtrl.getMock();
        shareTypeFactoryCtrl = MockControl.createStrictControl(ShareTypeFactory.class);
        shareTypeFactory = (ShareTypeFactory) shareTypeFactoryCtrl.getMock();
        checkerCtrl = MockControl.createStrictControl(ShareTypePermissionChecker.class);
        checker = (ShareTypePermissionChecker) checkerCtrl.getMock();
    }

    @After
    public void tearDown() throws Exception
    {
        user = null;
        other = null;
        SEARCH_ENTITY_1 = null;
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldNotBeAbleToInstantiateAShareManagerWithNullParameters()
    {
        new DefaultShareManager(null, null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldNotBeAbleToInstantiateAShareManagerWithANullStore()
    {
        final Mock factory = new Mock(ShareTypeFactory.class);
        factory.setStrict(true);
        final Mock reindexer = new Mock(SharePermissionReindexer.class);
        reindexer.setStrict(true);

        new DefaultShareManager(null, ((ShareTypeFactory) factory.proxy()), ((SharePermissionReindexer) reindexer.proxy()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldNotBeAbleToInstantiateAShareManagerWithANullFactory()
    {
        final Mock store = new Mock(SharePermissionStore.class);
        store.setStrict(true);
        final Mock reindexer = new Mock(SharePermissionReindexer.class);
        reindexer.setStrict(true);

        new DefaultShareManager(((SharePermissionStore) store.proxy()), null, (SharePermissionReindexer) reindexer.proxy());
    }

    DefaultShareManager createShareManager()
    {
        final Mock store = new Mock(SharePermissionStore.class);
        store.setStrict(true);
        final Mock factory = new Mock(ShareTypeFactory.class);
        factory.setStrict(true);
        final Mock reindexer = new Mock(SharePermissionReindexer.class);
        reindexer.setStrict(true);
        return new DefaultShareManager(((SharePermissionStore) store.proxy()), (ShareTypeFactory) factory.proxy(), (SharePermissionReindexer) reindexer.proxy());
    }

    @Test
    public void testGetSharePermissions()
    {
        final SharePermissions expectedReturnedSet = new SharePermissions(Collections.singleton(TestDefaultShareManager.GLOBAL_PERM));
        final Mock store = new Mock(SharePermissionStore.class);
        store.setStrict(true);
        store.expectAndReturn("getSharePermissions", P.args(P.eq(SEARCH_ENTITY_1)), expectedReturnedSet);

        final Mock factory = new Mock(ShareTypeFactory.class);
        factory.setStrict(true);
        final Mock reindexer = new Mock(SharePermissionReindexer.class);
        reindexer.setStrict(true);
        final DefaultShareManager mgr = new DefaultShareManager(((SharePermissionStore) store.proxy()), (ShareTypeFactory) factory.proxy(), (SharePermissionReindexer) reindexer.proxy());

        final SharePermissions actualReturnedSet = mgr.getSharePermissions(SEARCH_ENTITY_1);

        assertNotNull(actualReturnedSet);
        assertEquals(expectedReturnedSet, actualReturnedSet);

        store.verify();
    }

    @Test
    public void testUpdateSharePermissionsWithNullPermissions()
    {
        final Mock store = new Mock(SharePermissionStore.class);
        store.setStrict(true);
        store.expectAndReturn("deleteSharePermissions", P.args(P.eq(SEARCH_ENTITY_1)), new Integer(10));

        final Mock factory = new Mock(ShareTypeFactory.class);
        factory.setStrict(true);
        final Mock reindexer = new Mock(SharePermissionReindexer.class);
        reindexer.setStrict(true);
        final DefaultShareManager mgr = new DefaultShareManager(((SharePermissionStore) store.proxy()), (ShareTypeFactory) factory.proxy(), (SharePermissionReindexer) reindexer.proxy());

        final SharePermissions actualReturnedSet = mgr.updateSharePermissions(SEARCH_ENTITY_1);

        assertNotNull(actualReturnedSet);
        assertTrue(actualReturnedSet.isEmpty());

        store.verify();
    }

    @Test
    public void testUpdateSharePermissionsWithEmptyPermissions()
    {

        final Mock store = new Mock(SharePermissionStore.class);
        store.setStrict(true);
        store.expectAndReturn("deleteSharePermissions", P.args(P.eq(SEARCH_ENTITY_1)), new Integer(5));

        final Mock factory = new Mock(ShareTypeFactory.class);
        factory.setStrict(true);
        final Mock reindexer = new Mock(SharePermissionReindexer.class);
        reindexer.setStrict(true);
        final DefaultShareManager mgr = new DefaultShareManager(((SharePermissionStore) store.proxy()), (ShareTypeFactory) factory.proxy(), (SharePermissionReindexer) reindexer.proxy());

        final SharePermissions actualReturnedSet = mgr.updateSharePermissions(SEARCH_ENTITY_1);

        assertNotNull(actualReturnedSet);
        assertTrue(actualReturnedSet.isEmpty());

        store.verify();
    }

    @Test
    public void testUpdateSharePermissions()
    {
        final SharePermissions expectedPermissions = new SharePermissions(new HashSet(EasyList.build(TestDefaultShareManager.GLOBAL_PERM)));
        final SharedEntity entity = new StupidEntity(new Long(1), SearchRequest.ENTITY_TYPE, user, expectedPermissions);

        final Mock store = new Mock(SharePermissionStore.class);
        store.setStrict(true);
        store.expectAndReturn("storeSharePermissions", P.args(P.eq(entity)), expectedPermissions);

        final Mock factory = new Mock(ShareTypeFactory.class);
        factory.setStrict(true);
        final Mock reindexer = new Mock(SharePermissionReindexer.class);
        reindexer.setStrict(true);
        final DefaultShareManager mgr = new DefaultShareManager(((SharePermissionStore) store.proxy()), (ShareTypeFactory) factory.proxy(), (SharePermissionReindexer) reindexer.proxy());

        final SharePermissions actualReturnedSet = mgr.updateSharePermissions(entity);

        assertNotNull(actualReturnedSet);
        assertEquals(expectedPermissions, actualReturnedSet);

        store.verify();
    }

    @Test
    public void testHasPermissionNullEntity()
    {
        try
        {
            createShareManager().hasPermission(user, null);
            fail("Can not check permission for null entity");
        }
        catch (final IllegalArgumentException e)
        {
            // expected
        }
    }

    @Test
    public void testHasPermissionUserIsOwner()
    {
        assertTrue(createShareManager().hasPermission(user, SEARCH_ENTITY_1));
    }

    @Test
    public void testHasPermissionUserIsNotOwnerNoPermissions()
    {
        final SharePermissions expectedReturnedSet = SharePermissions.PRIVATE;
        final Mock store = new Mock(SharePermissionStore.class);
        store.setStrict(true);
        store.expectAndReturn("getSharePermissions", P.args(P.eq(SEARCH_ENTITY_1)), expectedReturnedSet);

        final Mock factory = new Mock(ShareTypeFactory.class);
        factory.setStrict(true);
        final Mock reindexer = new Mock(SharePermissionReindexer.class);
        reindexer.setStrict(true);
        final DefaultShareManager mgr = new DefaultShareManager(((SharePermissionStore) store.proxy()), (ShareTypeFactory) factory.proxy(), (SharePermissionReindexer) reindexer.proxy());

        assertFalse(mgr.hasPermission(other, SEARCH_ENTITY_1));
        store.verify();
    }

    @Test
    public void testHasPermissionUserIsNotOwnerNullPermissions()
    {
        final Mock store = new Mock(SharePermissionStore.class);
        store.setStrict(true);
        store.expectAndReturn("getSharePermissions", P.args(P.eq(SEARCH_ENTITY_1)), null);

        final Mock factory = new Mock(ShareTypeFactory.class);
        factory.setStrict(true);
        final Mock reindexer = new Mock(SharePermissionReindexer.class);
        reindexer.setStrict(true);
        final DefaultShareManager mgr = new DefaultShareManager(((SharePermissionStore) store.proxy()), (ShareTypeFactory) factory.proxy(), (SharePermissionReindexer) reindexer.proxy());

        assertFalse(mgr.hasPermission(other, SEARCH_ENTITY_1));
        store.verify();
    }

    @Test
    public void testHasPermissionUserIsNotOwnerHasPermission()
    {
        final SharePermissions expectedPermissions = SharePermissions.GLOBAL;
        final Mock shareStoreMock = new Mock(SharePermissionStore.class);
        shareStoreMock.setStrict(true);
        shareStoreMock.expectAndReturn("getSharePermissions", P.args(P.eq(SEARCH_ENTITY_1)), expectedPermissions);

        final SharePermissionStore store = (SharePermissionStore) shareStoreMock.proxy();

        shareTypeFactory.getShareType(GlobalShareType.TYPE);
        shareTypeFactoryCtrl.setReturnValue(shareType);

        shareType.getPermissionsChecker();
        shareTypeCtrl.setReturnValue(checker);

        checker.hasPermission(other, TestDefaultShareManager.GLOBAL_PERM);
        checkerCtrl.setReturnValue(true);

        shareTypeCtrl.replay();
        shareTypeFactoryCtrl.replay();
        checkerCtrl.replay();

        final Mock reindexer = new Mock(SharePermissionReindexer.class);
        reindexer.setStrict(true);
        final DefaultShareManager mgr = new DefaultShareManager(store, shareTypeFactory, (SharePermissionReindexer) reindexer.proxy());

        assertTrue(mgr.hasPermission(other, SEARCH_ENTITY_1));

        shareStoreMock.verify();
        shareTypeCtrl.verify();
        shareTypeFactoryCtrl.verify();
        checkerCtrl.verify();

    }

    @Test
    public void testHasPermissionUserIsNotOwnerNoPermission()
    {
        final SharePermissions expectedPermissions = SharePermissions.GLOBAL;
        final Mock shareStoreMock = new Mock(SharePermissionStore.class);
        shareStoreMock.setStrict(true);
        shareStoreMock.expectAndReturn("getSharePermissions", P.args(P.eq(SEARCH_ENTITY_1)), expectedPermissions);

        final SharePermissionStore store = (SharePermissionStore) shareStoreMock.proxy();

        shareTypeFactory.getShareType(GlobalShareType.TYPE);
        shareTypeFactoryCtrl.setReturnValue(shareType);

        shareType.getPermissionsChecker();
        shareTypeCtrl.setReturnValue(checker);

        checker.hasPermission(other, TestDefaultShareManager.GLOBAL_PERM);
        checkerCtrl.setReturnValue(false);

        shareTypeCtrl.replay();
        shareTypeFactoryCtrl.replay();
        checkerCtrl.replay();
        final Mock reindexer = new Mock(SharePermissionReindexer.class);
        reindexer.setStrict(true);

        final DefaultShareManager mgr = new DefaultShareManager(store, shareTypeFactory, (SharePermissionReindexer) reindexer.proxy());

        assertFalse(mgr.hasPermission(other, SEARCH_ENTITY_1));

        shareStoreMock.verify();
        shareTypeCtrl.verify();
        shareTypeFactoryCtrl.verify();
        checkerCtrl.verify();
    }

    @Test
    public void testHasPermissionUserIsNotOwnerSomePermissions()
    {
        // Check shot circuit check
        final Set perms = new ListOrderedSet();
        perms.addAll(EasyList.build(TestDefaultShareManager.GLOBAL_PERM, TestDefaultShareManager.GROUP_PERM));
        final SharePermissions expectedPermissions = new SharePermissions(perms);
        final Mock shareStoreMock = new Mock(SharePermissionStore.class);
        shareStoreMock.setStrict(true);
        shareStoreMock.expectAndReturn("getSharePermissions", P.args(P.eq(SEARCH_ENTITY_1)), expectedPermissions);

        final SharePermissionStore store = (SharePermissionStore) shareStoreMock.proxy();

        shareTypeFactory.getShareType(GlobalShareType.TYPE);
        shareTypeFactoryCtrl.setReturnValue(shareType);

        shareType.getPermissionsChecker();
        shareTypeCtrl.setReturnValue(checker);

        checker.hasPermission(other, TestDefaultShareManager.GLOBAL_PERM);
        checkerCtrl.setReturnValue(false);

        shareTypeFactory.getShareType(GroupShareType.TYPE);
        shareTypeFactoryCtrl.setReturnValue(shareType);

        shareType.getPermissionsChecker();
        shareTypeCtrl.setReturnValue(checker);

        checker.hasPermission(other, TestDefaultShareManager.GROUP_PERM);
        checkerCtrl.setReturnValue(true);

        shareTypeCtrl.replay();
        shareTypeFactoryCtrl.replay();
        checkerCtrl.replay();

        final Mock reindexer = new Mock(SharePermissionReindexer.class);
        reindexer.setStrict(true);

        final DefaultShareManager mgr = new DefaultShareManager(store, shareTypeFactory, (SharePermissionReindexer) reindexer.proxy());

        assertTrue(mgr.hasPermission(other, SEARCH_ENTITY_1));

        shareStoreMock.verify();
        shareTypeCtrl.verify();
        shareTypeFactoryCtrl.verify();
        checkerCtrl.verify();

    }

    @Test
    public void testDeleteSharePermissionsLike() throws Exception
    {
        final MockControl mockSharePermissionsStore = MockControl.createStrictControl(SharePermissionStore.class);
        final SharePermissionStore sharePermissionStore = (SharePermissionStore) mockSharePermissionsStore.getMock();
        sharePermissionStore.deleteSharePermissionsLike(TestDefaultShareManager.GROUP_PERM);
        mockSharePermissionsStore.setReturnValue(1);
        mockSharePermissionsStore.replay();

        final Mock factory = new Mock(ShareTypeFactory.class);
        factory.setStrict(true);
        final Mock reindexer = new Mock(SharePermissionReindexer.class);
        reindexer.setStrict(true);
        reindexer.expectVoid("reindex", TestDefaultShareManager.GROUP_PERM);

        final ShareManager shareManager = new DefaultShareManager(sharePermissionStore, (ShareTypeFactory) factory.proxy(), (SharePermissionReindexer) reindexer.proxy());

        shareManager.deleteSharePermissionsLike(TestDefaultShareManager.GROUP_PERM);

        mockSharePermissionsStore.verify();
    }

    private static class StupidEntity implements SharedEntity
    {
        private final Long id;
        private final SharedEntity.TypeDescriptor entityType;
        private final User owner;
        private final SharePermissions sharePermissions;

        StupidEntity(final Long id, final String entityType)
        {
            this.id = id;
            this.entityType = SharedEntity.TypeDescriptor.Factory.get().create(entityType);
            sharePermissions = SharePermissions.PRIVATE;
            owner = null;
        }

        StupidEntity(final Long id, final TypeDescriptor entityType, final User owner)
        {
            this.id = id;
            this.entityType = entityType;
            this.owner = owner;
            sharePermissions = SharePermissions.PRIVATE;
        }

        StupidEntity(final Long id, final TypeDescriptor entityType, final User owner, final SharePermissions sharePermissions)
        {
            this.id = id;
            this.entityType = entityType;
            this.owner = owner;
            this.sharePermissions = sharePermissions;
        }

        public Long getId()
        {
            return id;
        }

        public SharedEntity.TypeDescriptor getEntityType()
        {
            return entityType;
        }

        public String getOwnerUserName()
        {
            return owner.getName();
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
        public ApplicationUser getOwner()
        {
            return new DelegatingApplicationUser(owner.getName(), owner);
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
