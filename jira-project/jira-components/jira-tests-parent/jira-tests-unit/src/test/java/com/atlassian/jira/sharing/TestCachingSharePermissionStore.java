package com.atlassian.jira.sharing;

import java.util.Collections;

import com.atlassian.cache.memory.MemoryCacheManager;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.portal.PortalPage;
import com.atlassian.jira.sharing.type.GlobalShareType;
import com.atlassian.jira.sharing.type.GroupShareType;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.MockApplicationUser;
import com.atlassian.query.QueryImpl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TestCachingSharePermissionStore
{
    private ApplicationUser user;


    @Mock private SharePermissionStore mockShareStore;
    private PortalPage entity1;
    private static final Long ENTITY1_ID = new Long(10);
    private static final Long ENTITY2_ID = new Long(11);
    private PortalPage entity2;
    private SearchRequest entity3;

    @Before
    public void setUp() throws Exception
    {
        user = new MockApplicationUser("testUser");

        entity1 = PortalPage.id(ENTITY1_ID).name("page1").owner(user).build();
        entity2 = PortalPage.id(ENTITY2_ID).name("page2").owner(user).build();
        entity3 = new SearchRequest(new QueryImpl(), user, null, null, ENTITY1_ID, 0L);
    }

    /**
     * Make sure the call to get is cached after he first call.
     */
    @Test
    public void testGetSharePermissions()
    {
        final SharedEntity.SharePermissions expectedEntity1Permissions = SharedEntity.SharePermissions.GLOBAL;

        when(mockShareStore.getSharePermissions(getIdentifier(entity1))).thenReturn(expectedEntity1Permissions);

        final SharedEntity.SharePermissions expectedEntity2Permissions = SharedEntity.SharePermissions.PRIVATE;

        when(mockShareStore.getSharePermissions(getIdentifier(entity2))).thenReturn(expectedEntity2Permissions);

        final SharedEntity.SharePermissions expectedEntity3Permissions = new SharedEntity.SharePermissions(
            Collections.singleton(new SharePermissionImpl(GroupShareType.TYPE, "random group", null)));
        when(mockShareStore.getSharePermissions(getIdentifier(entity3))).thenReturn(expectedEntity3Permissions);

        final SharePermissionStore sharePermissionStore = createShareStore();

        //this should call the database.
        assertEquals(expectedEntity1Permissions, sharePermissionStore.getSharePermissions(entity1));

        //this should not call the database.
        assertEquals(expectedEntity1Permissions, sharePermissionStore.getSharePermissions(entity1));
        assertEquals(expectedEntity1Permissions, sharePermissionStore.getSharePermissions(entity1));
        assertEquals(expectedEntity1Permissions, sharePermissionStore.getSharePermissions(entity1));
        assertEquals(expectedEntity1Permissions, sharePermissionStore.getSharePermissions(entity1));

        //this should call the databases.
        assertEquals(expectedEntity2Permissions, sharePermissionStore.getSharePermissions(entity2));

        //this should not call the database
        assertEquals(expectedEntity2Permissions, sharePermissionStore.getSharePermissions(entity2));
        assertEquals(expectedEntity2Permissions, sharePermissionStore.getSharePermissions(entity2));
        assertEquals(expectedEntity2Permissions, sharePermissionStore.getSharePermissions(entity2));
        assertEquals(expectedEntity2Permissions, sharePermissionStore.getSharePermissions(entity2));
        assertEquals(expectedEntity2Permissions, sharePermissionStore.getSharePermissions(entity2));

        //this should call the databases.
        assertEquals(expectedEntity3Permissions, sharePermissionStore.getSharePermissions(entity3));

        //this should not call the database
        assertEquals(expectedEntity3Permissions, sharePermissionStore.getSharePermissions(entity3));
        assertEquals(expectedEntity3Permissions, sharePermissionStore.getSharePermissions(entity3));
        assertEquals(expectedEntity3Permissions, sharePermissionStore.getSharePermissions(entity3));
        assertEquals(expectedEntity3Permissions, sharePermissionStore.getSharePermissions(entity3));
        assertEquals(expectedEntity3Permissions, sharePermissionStore.getSharePermissions(entity3));
        // verify only called once
        verify(mockShareStore).getSharePermissions(getIdentifier(entity1));
        verify(mockShareStore).getSharePermissions(getIdentifier(entity2));
        verify(mockShareStore).getSharePermissions(getIdentifier(entity3));

    }

    /**
     * Null share should be replaced with private share.
     */
    @Test
    public void testGetSharePermissionsNull()
    {
        when(mockShareStore.getSharePermissions(getIdentifier(entity1))).thenReturn(null);

        final SharePermissionStore sharePermissionStore = createShareStore();

        //this should call the database.
        assertEquals(SharedEntity.SharePermissions.PRIVATE, sharePermissionStore.getSharePermissions(entity1));
        assertEquals(SharedEntity.SharePermissions.PRIVATE, sharePermissionStore.getSharePermissions(entity1));
        assertEquals(SharedEntity.SharePermissions.PRIVATE, sharePermissionStore.getSharePermissions(entity1));
        // verify only called once
        verify(mockShareStore).getSharePermissions(getIdentifier(entity1));

    }

    /**
     * Make sure delete clears the related cache enity.
     */
    @Test
    public void testDeleteSharePermissions()
    {
        final SharedEntity.SharePermissions expectedEntity1Permissions = SharedEntity.SharePermissions.GLOBAL;

        when(mockShareStore.getSharePermissions(getIdentifier(entity1))).thenReturn(expectedEntity1Permissions);

        final SharedEntity.SharePermissions expectedEntity2Permissions = SharedEntity.SharePermissions.PRIVATE;

        when(mockShareStore.getSharePermissions(getIdentifier(entity2))).thenReturn(expectedEntity2Permissions);

        when(mockShareStore.deleteSharePermissions(entity1)).thenReturn(-1);

        final SharedEntity.SharePermissions expectedEntity1NewPermissions = new SharedEntity.SharePermissions(
            Collections.singleton(new SharePermissionImpl(GroupShareType.TYPE, "random group", null)));
        when(mockShareStore.getSharePermissions(getIdentifier(entity1))).thenReturn(expectedEntity1NewPermissions);

        final SharePermissionStore sharePermissionStore = createShareStore();

        //Priming the cache
        sharePermissionStore.getSharePermissions(entity1);
        sharePermissionStore.getSharePermissions(entity2);

        assertEquals(-1, sharePermissionStore.deleteSharePermissions(entity1));

        //This should go back to the database
        assertEquals(expectedEntity1NewPermissions, sharePermissionStore.getSharePermissions(entity1));

        // This should now be cached
        assertEquals(expectedEntity1NewPermissions, sharePermissionStore.getSharePermissions(entity1));
        assertEquals(expectedEntity1NewPermissions, sharePermissionStore.getSharePermissions(entity1));

        assertEquals(expectedEntity2Permissions, sharePermissionStore.getSharePermissions(entity2));

        // verify invalidated
        verify(mockShareStore,times(2)).getSharePermissions(getIdentifier(entity1));
        // verify only called once
        verify(mockShareStore).getSharePermissions(getIdentifier(entity2));

    }

    /**
     * Make sure that delete by like cleans up the whole cache.
     */
    @Test
    public void testDeleteSharePermissionsLike()
    {
        final SharedEntity.SharePermissions expectedEntity1Permissions = SharedEntity.SharePermissions.GLOBAL;

        when(mockShareStore.getSharePermissions(getIdentifier(entity1))).thenReturn(expectedEntity1Permissions);

        final SharedEntity.SharePermissions expectedEntity2Permissions = SharedEntity.SharePermissions.PRIVATE;

        when(mockShareStore.getSharePermissions(getIdentifier(entity2))).thenReturn(expectedEntity2Permissions);

        final SharePermissionImpl expectedShareType = new SharePermissionImpl(GlobalShareType.TYPE, "p1", "p2");
        when(mockShareStore.deleteSharePermissionsLike(expectedShareType)).thenReturn(-1);

        final SharedEntity.SharePermissions expectedEntity1NewPermissions = new SharedEntity.SharePermissions(
            Collections.singleton(new SharePermissionImpl(GroupShareType.TYPE, "random group", null)));
        when(mockShareStore.getSharePermissions(getIdentifier(entity1))).thenReturn(expectedEntity1NewPermissions);

        when(mockShareStore.getSharePermissions(getIdentifier(entity2))).thenReturn(expectedEntity2Permissions);

        final SharePermissionStore sharePermissionStore = createShareStore();

        //Priming the cache
        sharePermissionStore.getSharePermissions(entity1);
        sharePermissionStore.getSharePermissions(entity2);

        assertEquals(-1, sharePermissionStore.deleteSharePermissionsLike(expectedShareType));

        //This should go back to the database
        assertEquals(expectedEntity1NewPermissions, sharePermissionStore.getSharePermissions(entity1));

        // This should now be cached
        assertEquals(expectedEntity1NewPermissions, sharePermissionStore.getSharePermissions(entity1));
        assertEquals(expectedEntity1NewPermissions, sharePermissionStore.getSharePermissions(entity1));

        //This should go back to the database
        assertEquals(expectedEntity2Permissions, sharePermissionStore.getSharePermissions(entity2));

        //This should now be cached
        assertEquals(expectedEntity2Permissions, sharePermissionStore.getSharePermissions(entity2));
        assertEquals(expectedEntity2Permissions, sharePermissionStore.getSharePermissions(entity2));

        // verify both invalidated
        verify(mockShareStore, times(2)).getSharePermissions(getIdentifier(entity1));
        verify(mockShareStore, times(2)).getSharePermissions(getIdentifier(entity2));

    }

    /**
     * Store share permissions should replace the permissions of the old cache entry.
     */
    @Test
    public void testStoreSharePermissions()
    {
        final SharedEntity.SharePermissions expectedEntity1Permissions = SharedEntity.SharePermissions.GLOBAL;
        final SharedEntity.SharePermissions expectedEntity1NewPermissions = new SharedEntity.SharePermissions(
                Collections.singleton(new SharePermissionImpl(GroupShareType.TYPE, "random group", null)));

        when(mockShareStore.getSharePermissions(getIdentifier(entity1))).thenReturn(expectedEntity1Permissions).thenReturn(expectedEntity1NewPermissions);

        final SharedEntity.SharePermissions expectedEntity2Permissions = SharedEntity.SharePermissions.PRIVATE;

        when(mockShareStore.getSharePermissions(getIdentifier(entity2))).thenReturn(expectedEntity2Permissions);


        when(mockShareStore.storeSharePermissions(entity1)).thenReturn(expectedEntity1NewPermissions);

        final SharePermissionStore sharePermissionStore = createShareStore();

        //Priming the cache
        sharePermissionStore.getSharePermissions(entity1);
        sharePermissionStore.getSharePermissions(entity2);

        assertEquals(expectedEntity1NewPermissions, sharePermissionStore.storeSharePermissions(entity1));

        // This should now be cached
        assertEquals(expectedEntity1NewPermissions, sharePermissionStore.getSharePermissions(entity1));
        assertEquals(expectedEntity1NewPermissions, sharePermissionStore.getSharePermissions(entity1));
        assertEquals(expectedEntity1NewPermissions, sharePermissionStore.getSharePermissions(entity1));

        assertEquals(expectedEntity2Permissions, sharePermissionStore.getSharePermissions(entity2));

        // verify only called twice
        verify(mockShareStore, times(2)).getSharePermissions(getIdentifier(entity1));
        // verify only called once
        verify(mockShareStore).getSharePermissions(getIdentifier(entity2));
    }

    /**
     * Make sure that store replaces null with private permission.
     */
    @Test
    public void testStoreSharePermissionsNullCase()
    {
        final SharedEntity.SharePermissions expectedEntity1Permissions = SharedEntity.SharePermissions.GLOBAL;

        when(mockShareStore.getSharePermissions(getIdentifier(entity1))).thenReturn(expectedEntity1Permissions).thenReturn(null);

        final SharedEntity.SharePermissions expectedEntity2Permissions = SharedEntity.SharePermissions.PRIVATE;

        when(mockShareStore.getSharePermissions(getIdentifier(entity2))).thenReturn(expectedEntity2Permissions);

        final SharePermissionStore sharePermissionStore = createShareStore();

        //Priming the cache
        sharePermissionStore.getSharePermissions(entity1);
        sharePermissionStore.getSharePermissions(entity2);

        assertEquals(SharedEntity.SharePermissions.PRIVATE, sharePermissionStore.storeSharePermissions(entity1));

        // This should now be cached
        assertEquals(SharedEntity.SharePermissions.PRIVATE, sharePermissionStore.getSharePermissions(entity1));
        assertEquals(SharedEntity.SharePermissions.PRIVATE, sharePermissionStore.getSharePermissions(entity1));
        assertEquals(SharedEntity.SharePermissions.PRIVATE, sharePermissionStore.getSharePermissions(entity1));

        assertEquals(expectedEntity2Permissions, sharePermissionStore.getSharePermissions(entity2));

        // verify only called twice
        verify(mockShareStore, times(2)).getSharePermissions(getIdentifier(entity1));
        // verify only called once
        verify(mockShareStore).getSharePermissions(getIdentifier(entity2));
    }

    /**
     * Make sure that store removes entry from cache on error.
     */
    @Test
    public void testStoreSharePermissionsErrorCondition()
    {
        final SharedEntity.SharePermissions expectedEntity1Permissions = SharedEntity.SharePermissions.GLOBAL;

        when(mockShareStore.getSharePermissions(getIdentifier(entity1))).thenReturn(expectedEntity1Permissions);

        final SharedEntity.SharePermissions expectedEntity2Permissions = SharedEntity.SharePermissions.PRIVATE;

        when(mockShareStore.getSharePermissions(getIdentifier(entity2))).thenReturn(expectedEntity2Permissions);

        final SharedEntity.SharePermissions expectedEntity1NewPermissions = new SharedEntity.SharePermissions(
            Collections.singleton(new SharePermissionImpl(GroupShareType.TYPE, "random group", null)));
        when(mockShareStore.storeSharePermissions(entity1)).thenThrow(new RuntimeException("some error"));

        when(mockShareStore.getSharePermissions(getIdentifier(entity1))).thenReturn(expectedEntity1NewPermissions);

        final SharePermissionStore sharePermissionStore = createShareStore();

        //Priming the cache
        sharePermissionStore.getSharePermissions(entity1);
        sharePermissionStore.getSharePermissions(entity2);
        try
        {
            sharePermissionStore.storeSharePermissions(entity1);
            fail("Should have failed");
        }
        catch (final RuntimeException ignore)
        {}

        //This should go back to the database
        assertEquals(expectedEntity1NewPermissions, sharePermissionStore.getSharePermissions(entity1));

        // This should now be cached
        assertEquals(expectedEntity1NewPermissions, sharePermissionStore.getSharePermissions(entity1));
        assertEquals(expectedEntity1NewPermissions, sharePermissionStore.getSharePermissions(entity1));
        assertEquals(expectedEntity1NewPermissions, sharePermissionStore.getSharePermissions(entity1));

        assertEquals(expectedEntity2Permissions, sharePermissionStore.getSharePermissions(entity2));

        // verify called twice
        verify(mockShareStore, times(2)).getSharePermissions(getIdentifier(entity1));
        // verify only called once
        verify(mockShareStore).getSharePermissions(getIdentifier(entity2));
    }

    private SharedEntity getIdentifier(final SharedEntity entity)
    {
        return new SharedEntity.Identifier(entity.getId(), entity.getEntityType(), (ApplicationUser) null);
    }

    private SharePermissionStore createShareStore()
    {
        return new CachingSharePermissionStore(mockShareStore, new MemoryCacheManager());
    }
}
