package com.atlassian.jira.favourites;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.exception.PermissionException;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.portal.PortalPage;
import com.atlassian.jira.sharing.SharedEntity;
import com.atlassian.jira.sharing.SharedEntityAccessor;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.DelegatingApplicationUser;
import com.atlassian.jira.user.MockApplicationUser;

import org.apache.commons.collections.map.HashedMap;
import org.junit.Before;
import org.junit.Test;

/**
 * Test for the reordering of favourites in the DefaultFavouriteManager
 *
 * @since v3.13
 */
public class TestDefaultFavouritesManagerReordering extends MockControllerTestCase
{
    private ApplicationUser user;
    private static final Long ID_123 = new Long(123);
    private static final Long ID_456 = new Long(456);
    private static final Long ID_789 = new Long(789);
    private static final Long ID_101112 = new Long(101112);
    private static final SharedEntity.TypeDescriptor ENTITY_TYPE = PortalPage.ENTITY_TYPE;

    private SharedEntity sharedEntity123;
    private SharedEntity sharedEntity456;
    private SharedEntity sharedEntity789;
    private SharedEntity sharedEntity101112;
    private Map mapOfEntities;
    private SharedEntityAccessor.Factory sharedEntityAccessorFactory;
    private SharedEntityAccessor sharedEntityAccessor;
    private FavouritesStore favouriteStore;

    @Before
    public void setUp()
    {
        user = new MockApplicationUser("user");

        sharedEntity123 = new SharedEntity.Identifier(ID_123, ENTITY_TYPE, user);
        sharedEntity456 = new SharedEntity.Identifier(ID_456, ENTITY_TYPE, user);
        sharedEntity789 = new SharedEntity.Identifier(ID_789, ENTITY_TYPE, user);
        sharedEntity101112 = new SharedEntity.Identifier(ID_101112, ENTITY_TYPE, user);

        mapOfEntities = new HashedMap();
        mapOfEntities.put(ID_123, sharedEntity123);
        mapOfEntities.put(ID_456, sharedEntity456);
        mapOfEntities.put(ID_789, sharedEntity789);
        mapOfEntities.put(ID_101112, sharedEntity101112);

        sharedEntityAccessorFactory = (SharedEntityAccessor.Factory) mockController.getMock(SharedEntityAccessor.Factory.class);
        sharedEntityAccessor = (SharedEntityAccessor) mockController.getMock(SharedEntityAccessor.class);
        favouriteStore = (FavouritesStore) mockController.getMock(FavouritesStore.class);
    }

    /**
     * Make sure that the page moves up correctly.
     *
     * @throws PermissionException just throw this up the stack in the test.
     */
    @Test
    public void testIncreaseSequence() throws PermissionException
    {
        _testHappyPath(sharedEntity456, EasyList.build(sharedEntity456, sharedEntity123, sharedEntity789, sharedEntity101112), new IncreaseReorderCommand());
    }

    /**
     * Make sure that increasing the first page is a no-op.
     *
     * @throws PermissionException just throw this up the stack in the test.
     */
    @Test
    public void testIncreaseFirstIsNoop() throws PermissionException
    {
        _testHappyPath(sharedEntity123, EasyList.build(sharedEntity123, sharedEntity456, sharedEntity789, sharedEntity101112), new IncreaseReorderCommand());
    }

    /**
     * Make sure that a page moves down correctly.
     *
     * @throws PermissionException just throw this up the stack in the test.
     */
    @Test
    public void testDecreaseSequence() throws PermissionException
    {
        _testHappyPath(sharedEntity456, EasyList.build(sharedEntity123, sharedEntity789, sharedEntity456, sharedEntity101112), new DecreaseReorderCommand());
    }

    /**
     * Make sure that decreasing the last page is a no-op.
     *
     * @throws PermissionException just throw this up the stack in the test.
     */
    @Test
    public void testDecreaseLastIsNoop() throws PermissionException
    {
        _testHappyPath(sharedEntity101112, EasyList.build(sharedEntity123, sharedEntity456, sharedEntity789, sharedEntity101112), new DecreaseReorderCommand());
    }

    /**
     * Make sure that a page moves to start correctly.
     *
     * @throws PermissionException just throw this up the stack in the test.
     */
    @Test
    public void testMoveToStart() throws PermissionException
    {
        _testHappyPath(sharedEntity789, EasyList.build(sharedEntity789, sharedEntity123, sharedEntity456, sharedEntity101112), new StartReorderCommand());
    }

    /**
     * Make moving start page to the start is a no-op.
     *
     * @throws PermissionException just throw this up the stack in the test.
     */
    @Test
    public void testMoveStartToStartIsNoop() throws PermissionException
    {
        _testHappyPath(sharedEntity123, EasyList.build(sharedEntity123, sharedEntity456, sharedEntity789, sharedEntity101112), new StartReorderCommand());
    }

    /**
     * Make sure that a page moves to end correctly.
     *
     * @throws PermissionException just throw this up the stack in the test.
     */
    @Test
    public void testMoveToEnd() throws PermissionException
    {
        _testHappyPath(sharedEntity456, EasyList.build(sharedEntity123, sharedEntity789, sharedEntity101112, sharedEntity456), new EndReorderingCommand());
    }

    /**
     * Make sure that moving the end page to the end is a no-op.
     *
     * @throws PermissionException just throw this up the stack in the test.
     */
    @Test
    public void testMoveEndToEndIsNoop() throws PermissionException
    {
        _testHappyPath(sharedEntity101112, EasyList.build(sharedEntity123, sharedEntity456, sharedEntity789, sharedEntity101112), new EndReorderingCommand());
    }

    /**
     * Dead favourites should be deleted.
     *
     * @throws com.atlassian.jira.exception.PermissionException just pass it up the stack as a test error.
     */
    @Test
    public void testDeleteDeadFavourite() throws PermissionException
    {
        mockTargetFavourite(sharedEntity123);

        final List favList = EasyList.build(ID_123, ID_456, ID_789);
        favouriteStore.getFavouriteIds(user, ENTITY_TYPE);
        mockController.setReturnValue(favList);

        // first entity should be available
        sharedEntityAccessor.getSharedEntity(ID_123);
        mockController.setReturnValue(sharedEntity123);

        sharedEntityAccessor.hasPermissionToUse(user.getDirectoryUser(), sharedEntity123);
        mockController.setReturnValue(true);

        // second entity should not be available.
        sharedEntityAccessor.getSharedEntity(ID_456);
        mockController.setReturnValue(null);

        // third entity should not be available.
        sharedEntityAccessor.getSharedEntity(ID_789);
        mockController.setReturnValue(sharedEntity789);

        sharedEntityAccessor.hasPermissionToUse(user.getDirectoryUser(), sharedEntity789);
        mockController.setReturnValue(true);

        // we should remove this entity.
        favouriteStore.removeFavourite(user, sharedEntity456);
        mockController.setReturnValue(true);

        // we should only update these sequences.
        favouriteStore.updateSequence(user, EasyList.build(sharedEntity789, sharedEntity123));

        final FavouritesManager favouritesManager = (DefaultFavouritesManager) mockController.instantiate(DefaultFavouritesManager.class);
        favouritesManager.decreaseFavouriteSequence(user, sharedEntity123);

        mockController.verify();
    }

    /**
     * Dead favourites should be deleted. Check what happens when the dead favourite is the one being moved.
     *
     * @throws com.atlassian.jira.exception.PermissionException just pass it up the stack as a test error.
     */
    @Test
    public void testDeleteDeadFavouriteWhenMovingIsDead() throws PermissionException
    {
        mockTargetFavourite(sharedEntity123);

        final List favList = EasyList.build(ID_123, ID_789);
        favouriteStore.getFavouriteIds(user, ENTITY_TYPE);
        mockController.setReturnValue(favList);

        // should not return this nothing.
        sharedEntityAccessor.getSharedEntity(ID_123);
        mockController.setReturnValue(null);

        // should return an entity.
        sharedEntityAccessor.getSharedEntity(ID_789);
        mockController.setReturnValue(sharedEntity789);

        sharedEntityAccessor.hasPermissionToUse(user.getDirectoryUser(), sharedEntity789);
        mockController.setReturnValue(true);

        // should remove invalid favourite.
        favouriteStore.removeFavourite(user, sharedEntity123);
        mockController.setReturnValue(true);

        // should only save valid shares.
        favouriteStore.updateSequence(user, EasyList.build(sharedEntity789));

        final FavouritesManager favouritesManager = (FavouritesManager) mockController.instantiate(DefaultFavouritesManager.class);
        favouritesManager.decreaseFavouriteSequence(user, sharedEntity123);

        mockController.verify();
    }

    /**
     * Make sure that reordering up works as expected when you no longer have permission to see a favourite.
     *
     * @throws PermissionException just throw this as a test failure.
     */
    @Test
    public void testIncreaseWithGap() throws PermissionException
    {
        _testReorderWithGap(sharedEntity789, EasyList.build(sharedEntity789, sharedEntity123, sharedEntity101112, sharedEntity456), new IncreaseReorderCommand());
    }

    /**
     * Make sure that reordering up works as expected when you no longer have permission to see a favourite.
     *
     * @throws PermissionException just throw this as a test failure.
     */
    @Test
    public void testDecreaseWithGap() throws PermissionException
    {
        _testReorderWithGap(sharedEntity123, EasyList.build(sharedEntity789, sharedEntity123, sharedEntity101112, sharedEntity456), new DecreaseReorderCommand());
    }

    /**
     * Make sure that reordering up works as expected when you no longer have permission to see a favourite.
     *
     * @throws PermissionException just throw this as a test failure.
     */
    @Test
    public void testMoveToStartWithGap() throws PermissionException
    {
        _testReorderWithGap(sharedEntity101112, EasyList.build(sharedEntity101112, sharedEntity123, sharedEntity789, sharedEntity456), new StartReorderCommand());
    }

    /**
     * Make sure that reordering up works as expected when you no longer have permission to see a favourite.
     *
     * @throws PermissionException just throw this as a test failure.
     */
    @Test
    public void testMoveToEndWithGap() throws PermissionException
    {
        _testReorderWithGap(sharedEntity123, EasyList.build(sharedEntity789, sharedEntity101112, sharedEntity123, sharedEntity456), new EndReorderingCommand());
    }

    /**
     * Make sure that reordering a SharedEntity that is no longer accessible works as expected.
     *
     * @throws com.atlassian.jira.exception.PermissionException just throw for test failure.
     */
    @Test
    public void testReorderOnNoPermission() throws PermissionException
    {
        final List reorderList = EasyList.build(sharedEntity123, sharedEntity789, sharedEntity101112, sharedEntity456);

        _testReorderWithGap(sharedEntity456, reorderList, new IncreaseReorderCommand());
        mockController.reset();
        _testReorderWithGap(sharedEntity456, reorderList, new DecreaseReorderCommand());
        mockController.reset();
        _testReorderWithGap(sharedEntity456, reorderList, new StartReorderCommand());
        mockController.reset();
        _testReorderWithGap(sharedEntity456, reorderList, new EndReorderingCommand());
    }

    private void _testReorderWithGap(final SharedEntity entityUnderTest, final List /* <SharedEntity> */reorderList, final ReorderingCommand command) throws PermissionException
    {
        mockTargetFavourite(entityUnderTest);

        final List favList = EasyList.build(ID_123, ID_456, ID_789, ID_101112);
        favouriteStore.getFavouriteIds(user, ENTITY_TYPE);
        mockController.setReturnValue(favList);

        for (final Iterator iterator = favList.iterator(); iterator.hasNext();)
        {
            final Long favId = (Long) iterator.next();
            final SharedEntity sharedEntity = (SharedEntity) mapOfEntities.get(favId);

            sharedEntityAccessor.getSharedEntity(favId);
            mockController.setReturnValue(sharedEntity);

            sharedEntityAccessor.hasPermissionToUse(user.getDirectoryUser(), sharedEntity);
            mockController.setReturnValue(!favId.equals(ID_456));
        }

        favouriteStore.updateSequence(user, reorderList);
        final FavouritesManager favouritesManager = (DefaultFavouritesManager) mockController.instantiate(DefaultFavouritesManager.class);
        command.resequence(favouritesManager, user.getDirectoryUser(), entityUnderTest);

        mockController.verify();
    }

    private void _testHappyPath(final SharedEntity entityUnderTest, final List /* <SharedEntity> */reorderList, final ReorderingCommand command) throws PermissionException
    {
        mockTargetFavourite(entityUnderTest);

        final List favList = EasyList.build(ID_123, ID_456, ID_789, ID_101112);
        favouriteStore.getFavouriteIds(user, ENTITY_TYPE);
        mockController.setReturnValue(favList);

        for (final Iterator iterator = favList.iterator(); iterator.hasNext();)
        {
            final Long favId = (Long) iterator.next();
            final SharedEntity sharedEntity = (SharedEntity) mapOfEntities.get(favId);

            sharedEntityAccessor.getSharedEntity(favId);
            mockController.setReturnValue(sharedEntity);

            sharedEntityAccessor.hasPermissionToUse(user.getDirectoryUser(), sharedEntity);
            mockController.setReturnValue(true);
        }

        favouriteStore.updateSequence(user, reorderList);

        final FavouritesManager favouritesManager = (DefaultFavouritesManager) mockController.instantiate(DefaultFavouritesManager.class);
        command.resequence(favouritesManager, user.getDirectoryUser(), entityUnderTest);

        mockController.verify();
    }

    private void mockTargetFavourite(final SharedEntity targetFavourite)
    {
        sharedEntityAccessorFactory.getSharedEntityAccessor(targetFavourite.getEntityType());
        mockController.setReturnValue(sharedEntityAccessor);
    }

    private interface ReorderingCommand
    {
        void resequence(FavouritesManager favouritesManager, User user, SharedEntity entityUnderTest) throws PermissionException;
    }

    private static class IncreaseReorderCommand implements ReorderingCommand
    {
        public void resequence(final FavouritesManager favouritesManager, final User user, final SharedEntity entityUnderTest) throws PermissionException
        {
            favouritesManager.increaseFavouriteSequence(new DelegatingApplicationUser(user.getName(), user), entityUnderTest);
        }
    }

    private static class DecreaseReorderCommand implements ReorderingCommand
    {
        public void resequence(final FavouritesManager favouritesManager, final User user, final SharedEntity entityUnderTest) throws PermissionException
        {
            favouritesManager.decreaseFavouriteSequence(new DelegatingApplicationUser(user.getName(), user), entityUnderTest);
        }
    }

    private static class StartReorderCommand implements ReorderingCommand
    {
        public void resequence(final FavouritesManager favouritesManager, final User user, final SharedEntity entityUnderTest) throws PermissionException
        {
            favouritesManager.moveToStartFavouriteSequence(new DelegatingApplicationUser(user.getName(), user), entityUnderTest);
        }
    }

    private static class EndReorderingCommand implements ReorderingCommand
    {
        public void resequence(final FavouritesManager favouritesManager, final User user, final SharedEntity entityUnderTest) throws PermissionException
        {
            favouritesManager.moveToEndFavouriteSequence(new DelegatingApplicationUser(user.getName(), user), entityUnderTest);
        }
    }
}
