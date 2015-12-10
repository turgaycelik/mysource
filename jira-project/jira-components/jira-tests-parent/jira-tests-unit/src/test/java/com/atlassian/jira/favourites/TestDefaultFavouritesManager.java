package com.atlassian.jira.favourites;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.exception.PermissionException;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.sharing.ShareManager;
import com.atlassian.jira.sharing.SharedEntity;
import com.atlassian.jira.sharing.SharedEntityAccessor;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.MockApplicationUser;

import com.mockobjects.dynamic.Mock;
import com.mockobjects.dynamic.P;

import org.easymock.MockControl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class TestDefaultFavouritesManager
{
    private static final Long ENTITY_ID = 999L;
    private SharedEntity entity;

    private Mock favouritesStoreMock;
    private Mock sharedEntityAccessorFactoryMock;
    private Mock sharedEntityAccessorMock;

    private MockControl mockShareManager;
    private ShareManager shareManager;

    private FavouritesStore store;
    private SharedEntityAccessor sharedEntityAccessor;
    private SharedEntityAccessor.Factory sharedEntityAccessorFactory;
    private ApplicationUser user;

    @Before
    public void setUp() throws Exception
    {
        ComponentAccessor.initialiseWorker(new MockComponentWorker());
        favouritesStoreMock = new Mock(FavouritesStore.class);
        favouritesStoreMock.setStrict(true);
        sharedEntityAccessorMock = new Mock(SharedEntityAccessor.class);
        sharedEntityAccessorMock.setStrict(true);
        sharedEntityAccessorFactoryMock = new Mock(SharedEntityAccessor.Factory.class);
        sharedEntityAccessorFactoryMock.setStrict(true);

        mockShareManager = MockControl.createStrictControl(ShareManager.class);
        shareManager = (ShareManager) mockShareManager.getMock();

        user = new MockApplicationUser("admin");
        entity = new SharedEntity.Identifier(TestDefaultFavouritesManager.ENTITY_ID, SearchRequest.ENTITY_TYPE, user);
    }

    @After
    public void tearDown() throws Exception
    {
        favouritesStoreMock = null;
        sharedEntityAccessorMock = null;
        sharedEntityAccessorFactoryMock = null;

        store = null;
        sharedEntityAccessor = null;
        sharedEntityAccessorFactoryMock = null;
        user = null;
        mockShareManager = null;
        shareManager = null;
    }

    private void verifyMocks()
    {
        sharedEntityAccessorMock.verify();
        sharedEntityAccessorFactoryMock.verify();
        favouritesStoreMock.verify();
        mockShareManager.verify();

    }

    private void assignMocks()
    {
        store = (FavouritesStore) favouritesStoreMock.proxy();
        sharedEntityAccessor = (SharedEntityAccessor) sharedEntityAccessorMock.proxy();
        sharedEntityAccessorFactory = (SharedEntityAccessor.Factory) sharedEntityAccessorFactoryMock.proxy();
        mockShareManager.replay();
    }

    @Test
    public void testAddFavSuccess() throws PermissionException
    {
        favouritesStoreMock.expectAndReturn("addFavourite", P.args(P.eq(user), P.eq(entity)), Boolean.TRUE);
        sharedEntityAccessorMock.expectVoid("adjustFavouriteCount", P.args(P.eq(entity), P.eq(1)));

        shareManager.isSharedWith(user, entity);
        mockShareManager.setReturnValue(true);

        assignMocks();
        sharedEntityAccessorFactoryMock.expectAndReturn("getSharedEntityAccessor", P.args(P.eq(entity.getEntityType())), sharedEntityAccessor);

        final FavouritesManager manager = new DefaultFavouritesManager(store, sharedEntityAccessorFactory, shareManager);
        manager.addFavourite(user, entity);

        verifyMocks();

    }

    @Test
    public void testAddFavInPosFail() throws PermissionException
    {
        favouritesStoreMock.expectAndReturn("addFavourite", P.args(P.eq(user), P.eq(entity)), Boolean.FALSE);

        shareManager.isSharedWith(user, entity);
        mockShareManager.setReturnValue(true);

        assignMocks();

        final FavouritesManager manager = new DefaultFavouritesManager(store, sharedEntityAccessorFactory, shareManager);
        manager.addFavouriteInPosition(user, entity, 1);

        verifyMocks();

    }

    @Test
    public void testAddFavInPosNoPerm()
    {

        shareManager.isSharedWith(user, entity);
        mockShareManager.setReturnValue(false);

        assignMocks();
        final FavouritesManager manager = new DefaultFavouritesManager(store, sharedEntityAccessorFactory, shareManager);
        try
        {
            manager.addFavouriteInPosition(user, entity, 1);
            fail("PermissionException should have been thrown");
        }
        catch (final PermissionException e)
        {
            // good
        }

        verifyMocks();
    }

    @Test
    public void testAddFavInPosNullEntityForAdd() throws PermissionException
    {
        final FavouritesManager manager = new DefaultFavouritesManager(null, null, null);
        try
        {
            manager.addFavouriteInPosition(user, null, 1);
            fail("IAE expected");
        }
        catch (final IllegalArgumentException yay)
        {
            // good
        }
    }

    @Test
    public void testFavInPosNullUserForAdd() throws PermissionException
    {
        final FavouritesManager manager = new DefaultFavouritesManager(null, null, null);
        try
        {
            manager.addFavouriteInPosition((ApplicationUser) null, entity, 1);
            fail("IAE expected");
        }
        catch (final IllegalArgumentException yay)
        {
            // good
        }
    }

    @Test
    public void testAddFavInPosHappyPath() throws Exception
    {
        favouritesStoreMock.expectAndReturn("addFavourite", P.args(P.eq(user), P.eq(entity)), Boolean.TRUE);
        sharedEntityAccessorMock.expectVoid("adjustFavouriteCount", P.args(P.eq(entity), P.eq(1)));

        shareManager.isSharedWith(user, entity);
        mockShareManager.setReturnValue(true);

        assignMocks();
        sharedEntityAccessorFactoryMock.expectAndReturn("getSharedEntityAccessor", P.args(P.eq(entity.getEntityType())), sharedEntityAccessor);

        final AtomicBoolean reorderCalled = new AtomicBoolean(false);
        final FavouritesManager manager = new DefaultFavouritesManager(store, sharedEntityAccessorFactory, shareManager)
        {
            @Override
            void reorderFavourites(final ApplicationUser user, final SharedEntity entity, final DefaultFavouritesManager.FavouriteReordererCommand favouriteReordererCommand)
            {
                reorderCalled.set(true);
                assertTrue(favouriteReordererCommand instanceof InsertInPositionReorderCommand);
                assertEquals(1, ((InsertInPositionReorderCommand) favouriteReordererCommand).position);
            }
        };

        manager.addFavouriteInPosition(user, entity, 1);

        assertTrue(reorderCalled.get());
        verifyMocks();
    }

    @Test
    public void testInsertInPositionReorderCommand() throws Exception
    {
        final List oldList = EasyList.build("One", "Two", "Four", "Three");
        final DefaultFavouritesManager.InsertInPositionReorderCommand positionReorderCommand = new DefaultFavouritesManager.InsertInPositionReorderCommand(
            2);
        positionReorderCommand.reorderFavourites(oldList, null);
        assertEquals("One", oldList.get(0));
        assertEquals("Two", oldList.get(1));
        assertEquals("Three", oldList.get(2));
        assertEquals("Four", oldList.get(3));
    }

    @Test
    public void testInsertInPositionReorderCommandNegativeNumber() throws Exception
    {
        final List oldList = EasyList.build("One", "Two", "Four", "Three");
        final DefaultFavouritesManager.InsertInPositionReorderCommand positionReorderCommand = new DefaultFavouritesManager.InsertInPositionReorderCommand(
            -1);
        positionReorderCommand.reorderFavourites(oldList, null);
        assertEquals("One", oldList.get(0));
        assertEquals("Two", oldList.get(1));
        assertEquals("Four", oldList.get(2));
        assertEquals("Three", oldList.get(3));
    }

    @Test
    public void testInsertInPositionReorderCommandLargerNumberThanSize() throws Exception
    {
        final List oldList = EasyList.build("One", "Two", "Four", "Three");
        final DefaultFavouritesManager.InsertInPositionReorderCommand positionReorderCommand = new DefaultFavouritesManager.InsertInPositionReorderCommand(
            70);
        positionReorderCommand.reorderFavourites(oldList, null);
        assertEquals("One", oldList.get(0));
        assertEquals("Two", oldList.get(1));
        assertEquals("Four", oldList.get(2));
        assertEquals("Three", oldList.get(3));
    }

    @Test
    public void testAddFavFail() throws PermissionException
    {
        favouritesStoreMock.expectAndReturn("addFavourite", P.args(P.eq(user), P.eq(entity)), Boolean.FALSE);

        shareManager.isSharedWith(user, entity);
        mockShareManager.setReturnValue(true);

        assignMocks();

        final FavouritesManager manager = new DefaultFavouritesManager(store, sharedEntityAccessorFactory, shareManager);
        manager.addFavourite(user, entity);

        verifyMocks();

    }

    @Test
    public void testAddFavNoPerm()
    {

        shareManager.isSharedWith(user, entity);
        mockShareManager.setReturnValue(false);

        assignMocks();
        final FavouritesManager manager = new DefaultFavouritesManager(store, sharedEntityAccessorFactory, shareManager);
        try
        {
            manager.addFavourite(user, entity);
            fail("PermissionException should have been thrown");
        }
        catch (final PermissionException e)
        {
            // good
        }

        verifyMocks();
    }

    @Test
    public void testNullEntityForAdd() throws PermissionException
    {
        final FavouritesManager manager = new DefaultFavouritesManager(null, null, null);
        try
        {
            manager.addFavourite(user, null);
            fail("IAE expected");
        }
        catch (final IllegalArgumentException yay)
        {
            // good
        }
    }

    @Test
    public void testNullUserForAdd() throws PermissionException
    {
        final FavouritesManager manager = new DefaultFavouritesManager(null, null, null);
        try
        {
            manager.addFavourite((ApplicationUser) null, entity);
            fail("IAE expected");
        }
        catch (final IllegalArgumentException yay)
        {
            // good
        }
    }

    @Test
    public void testNullTypeForSharableEntityIdentifierType()
    {
        try
        {
            new SharedEntity.Identifier(TestDefaultFavouritesManager.ENTITY_ID, null, (ApplicationUser) null);
            fail("IllegalArgumentException should have been thrown for entity type");
        }
        catch (final IllegalArgumentException e)
        {
            // good
        }

    }

    @Test
    public void testNullIdForSharableEntityIdentifierType()
    {
        try
        {
            new SharedEntity.Identifier(null, SearchRequest.ENTITY_TYPE, (ApplicationUser) null);
            fail("IllegalArgumentException should have been thrown for entity user");
        }
        catch (final IllegalArgumentException e)
        {
            // good
        }
    }

    @Test
    public void testRemoveSuccess()
    {
        favouritesStoreMock.expectAndReturn("removeFavourite", P.args(P.eq(user), P.eq(entity)), Boolean.TRUE);
        sharedEntityAccessorMock.expectVoid("adjustFavouriteCount", P.args(P.eq(entity), P.eq(-1)));

        assignMocks();

        sharedEntityAccessorFactoryMock.expectAndReturn("getSharedEntityAccessor", P.args(P.eq(entity.getEntityType())), sharedEntityAccessor);

        final FavouritesManager manager = new DefaultFavouritesManager(store, sharedEntityAccessorFactory, shareManager);
        manager.removeFavourite(user, entity);

        verifyMocks();
    }

    @Test
    public void testRemoveFail()
    {
        favouritesStoreMock.expectAndReturn("removeFavourite", P.args(P.eq(user), P.eq(entity)), Boolean.FALSE);

        assignMocks();
        final FavouritesManager manager = new DefaultFavouritesManager(store, sharedEntityAccessorFactory, shareManager);
        manager.removeFavourite(user, entity);

        verifyMocks();
    }

    public void removeFavouritesForUser()
    {
        final List removedIds = new ArrayList();
        final List returnList = EasyList.build(1L, 999L);
        favouritesStoreMock.expectAndReturn("getFavouriteIds", P.args(P.eq(user), P.eq(entity.getEntityType())), returnList);

        assignMocks();

        final FavouritesManager manager = new DefaultFavouritesManager(store, sharedEntityAccessorFactory, shareManager)
        {
            @Override
            public void removeFavourite(final ApplicationUser innerUser, final SharedEntity entity)
            {
                assertEquals(user, innerUser);
                assertEquals(entity.getEntityType(), entity.getEntityType());
                removedIds.add(entity.getId());
            }
        };

        manager.removeFavouritesForUser(user, entity.getEntityType());

        assertEquals(removedIds, returnList);
        verifyMocks();
    }

    public void removeFavouritesForUserNullParams()
    {

        assignMocks();

        final FavouritesManager manager = new DefaultFavouritesManager(store, sharedEntityAccessorFactory, shareManager);

        try
        {
            manager.removeFavouritesForUser((ApplicationUser) null, entity.getEntityType());
            fail("null pointer should have been thrown");
        }
        catch (final NullPointerException e)
        {
            // good
        }
        try
        {
            manager.removeFavouritesForUser(user, null);
            fail("null pointer should have been thrown");
        }
        catch (final NullPointerException e)
        {
            // good
        }

        verifyMocks();
    }

    @Test
    public void testRemoveFavouritesForEntity()
    {
        favouritesStoreMock.expectVoid("removeFavouritesForEntity", P.args(P.eq(entity)));

        assignMocks();

        final FavouritesManager manager = new DefaultFavouritesManager(store, sharedEntityAccessorFactory, shareManager);
        manager.removeFavouritesForEntityDelete(entity);

        verifyMocks();
    }

    @Test
    public void testRemoveFavouritesForEntityNullParam()
    {
        assignMocks();
        final FavouritesManager manager = new DefaultFavouritesManager(store, sharedEntityAccessorFactory, shareManager);
        try
        {
            manager.removeFavouritesForEntityDelete(null);
            fail("IllegalArgumentEx should have been thrown");
        }
        catch (final IllegalArgumentException e)
        {
            // good
        }
        verifyMocks();
    }

    @Test
    public void testRemoveFavouritesForEntityNullEntityId()
    {
        assignMocks();
        final FavouritesManager manager = new DefaultFavouritesManager(store, sharedEntityAccessorFactory, shareManager);
        try
        {
            manager.removeFavouritesForEntityDelete(new SharedEntity()
            {

                public Long getId()
                {
                    return null;
                }

                public SharedEntity.TypeDescriptor getEntityType()
                {
                    return null;
                }

                public ApplicationUser getOwner()
                {
                    return null;
                }

                public String getName()
                {
                    return null;
                }

                public String getDescription()
                {
                    return null;
                }

                @Override
                public String getOwnerUserName()
                {
                    return null;
                }

                public SharePermissions getPermissions()
                {
                    throw new UnsupportedOperationException();
                }

                public Long getFavouriteCount()
                {
                    return null;
                }
            });
            fail("IllegalArgumentException should have been thrown");
        }
        catch (final IllegalArgumentException e)
        {
            // good
        }
        verifyMocks();
    }

    @Test
    public void testRemoveFavouritesForEntityNullEntityType()
    {
        assignMocks();
        final FavouritesManager manager = new DefaultFavouritesManager(store, sharedEntityAccessorFactory, shareManager);
        try
        {
            manager.removeFavouritesForEntityDelete(new SharedEntity()
            {

                public Long getId()
                {
                    return new Long(1);
                }

                public SharedEntity.TypeDescriptor getEntityType()
                {
                    return null;
                }

                public ApplicationUser getOwner()
                {
                    return null;
                }

                public String getName()
                {
                    return null;
                }

                public String getDescription()
                {
                    return null;
                }

                @Override
                public String getOwnerUserName()
                {
                    return null;
                }

                public SharePermissions getPermissions()
                {
                    throw new UnsupportedOperationException();
                }

                public Long getFavouriteCount()
                {
                    return null;
                }
            });
            fail("Null pointer should have been thrown");
        }
        catch (final IllegalArgumentException e)
        {
            // good
        }
        verifyMocks();
    }

    @Test
    public void testNullAllParamsForRemove()
    {
        final FavouritesManager manager = new DefaultFavouritesManager(null, null, null);
        try
        {
            manager.removeFavourite((ApplicationUser) null, entity);
            fail("IllegalArgumentException should have been thrown for user");
        }
        catch (final IllegalArgumentException e)
        {
            // good
        }
    }

    @Test
    public void testIsFavSuccess() throws PermissionException
    {
        shareManager.isSharedWith(user, entity);
        mockShareManager.setReturnValue(true);

        favouritesStoreMock.expectAndReturn("isFavourite", P.args(P.eq(user), P.eq(entity)), Boolean.TRUE);

        assignMocks();
        final FavouritesManager manager = new DefaultFavouritesManager(store, sharedEntityAccessorFactory, shareManager);
        assertTrue(manager.isFavourite(user, entity));

        verifyMocks();
    }

    @Test
    public void testIsFavFail() throws PermissionException
    {
        shareManager.isSharedWith(user, entity);
        mockShareManager.setReturnValue(true);

        favouritesStoreMock.expectAndReturn("isFavourite", P.args(P.eq(user), P.eq(entity)), Boolean.FALSE);

        assignMocks();

        final FavouritesManager manager = new DefaultFavouritesManager(store, sharedEntityAccessorFactory, shareManager);
        assertFalse(manager.isFavourite(user, entity));

        verifyMocks();
    }

    @Test
    public void testIsFavNoPerm()
    {

        shareManager.isSharedWith(user, entity);
        mockShareManager.setReturnValue(false);

        assignMocks();
        final FavouritesManager manager = new DefaultFavouritesManager(store, sharedEntityAccessorFactory, shareManager);
        try
        {
            manager.isFavourite(user, entity);
            fail("IllegalArgumentException should have been thrown");
        }
        catch (final PermissionException e)
        {
            // good
        }
        verifyMocks();
    }

    @Test
    public void testNullParamsForIs() throws PermissionException
    {
        final FavouritesManager manager = new DefaultFavouritesManager(null, null, null);
        try
        {
            manager.isFavourite(user, null);
            fail("IllegalArgumentException should have been thrown");
        }
        catch (final IllegalArgumentException e)
        {
            // good
        }
    }

    @Test
    public void testGetFavouriteIdsSuccess()
    {

        final List returnList = EasyList.build(1L, 999L);
        favouritesStoreMock.expectAndReturn("getFavouriteIds", P.args(P.eq(user), P.eq(entity.getEntityType())), returnList);

        assignMocks();
        final FavouritesManager manager = new DefaultFavouritesManager(store, sharedEntityAccessorFactory, shareManager);
        final Collection ids = manager.getFavouriteIds(user, entity.getEntityType());

        assertNotNull(ids);
        assertEquals(returnList, ids);

        verifyMocks();
    }

    @Test
    public void testGetFavouriteIdsNoneStored()
    {

        final List returnList = EasyList.build();
        favouritesStoreMock.expectAndReturn("getFavouriteIds", P.args(P.eq(user), P.eq(entity.getEntityType())), returnList);

        assignMocks();
        final FavouritesManager manager = new DefaultFavouritesManager(store, sharedEntityAccessorFactory, shareManager);
        final Collection ids = manager.getFavouriteIds(user, entity.getEntityType());

        assertNotNull(ids);
        assertTrue(ids.isEmpty());

        verifyMocks();
    }

    @Test
    public void testGetFavouriteIdsNullUserParam()
    {
        assignMocks();
        final FavouritesManager manager = new DefaultFavouritesManager(store, sharedEntityAccessorFactory, shareManager);
        try
        {
            manager.getFavouriteIds((ApplicationUser) null, entity.getEntityType());
            fail("IllegalArgumentException should have been thrown");
        }
        catch (final IllegalArgumentException e)
        {
            // good
        }
        verifyMocks();
    }

    @Test
    public void testGetFavouriteIdsNullEntityTypeParam()
    {
        assignMocks();
        final FavouritesManager manager = new DefaultFavouritesManager(store, sharedEntityAccessorFactory, shareManager);
        try
        {
            manager.getFavouriteIds(user, null);
            fail("IllegalArgumentException should have been thrown");
        }
        catch (final IllegalArgumentException e)
        {
            // good
        }
        verifyMocks();
    }
}
