package com.atlassian.jira.portal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.gadgets.dashboard.Layout;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.sharing.ShareManager;
import com.atlassian.jira.sharing.SharePermission;
import com.atlassian.jira.sharing.SharePermissionImpl;
import com.atlassian.jira.sharing.SharedEntity;
import com.atlassian.jira.sharing.SharedEntity.Identifier;
import com.atlassian.jira.sharing.SharedEntity.SharePermissions;
import com.atlassian.jira.sharing.index.MockSharedEntityIndexer;
import com.atlassian.jira.sharing.type.GlobalShareType;
import com.atlassian.jira.sharing.type.GroupShareType;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.MockApplicationUser;
import com.atlassian.jira.util.collect.CollectionBuilder;

import org.easymock.MockControl;
import org.easymock.classextension.EasyMock;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Unit tests for {@link com.atlassian.jira.issue.search.DefaultSearchRequestManager}
 *
 * @since v3.13
 */
public class TestDefaultPortalPageManager
{
    private static final String PAGE_NAME = "pageName1";
    private static final String GOOD_PORTLET = "goodPortlet";
    private static final Long DEVILS_ID = new Long(666);
    private static final int NEIGHBOUR_OF_THE_BEAST = 664;

    private ApplicationUser user;

    private MockControl portalPageStoreCtrl;
    private PortalPageStore portalPageStore;

    private EventPublisher eventPublisher;
    private JiraAuthenticationContext authenticationContext;

    private MockControl portletConfigurationManagerCtrl;
    private PortletConfigurationManager portletConfigurationManager;

    private MockControl shareManagerCtrl;
    private ShareManager shareManager;

    private SharePermission perm2;
    private SharePermission perm1;

    private PortalPage portalPage1;
    private PortalPage portalPage2;
    private PortalPage portalPage3;
    private List configList1;
    private List configList2;
    private List configList3;

    private PortalPage defaultPage;

    @Before
    public void setUp() throws Exception
    {
        ComponentAccessor.initialiseWorker(new MockComponentWorker());
        portalPageStoreCtrl = MockControl.createStrictControl(PortalPageStore.class);
        portalPageStore = (PortalPageStore) portalPageStoreCtrl.getMock();

        shareManagerCtrl = MockControl.createStrictControl(ShareManager.class);
        shareManager = (ShareManager) shareManagerCtrl.getMock();

        portletConfigurationManagerCtrl = MockControl.createStrictControl(PortletConfigurationManager.class);
        portletConfigurationManager = (PortletConfigurationManager) portletConfigurationManagerCtrl.getMock();

        eventPublisher = EasyMock.createNiceMock(EventPublisher.class);
        authenticationContext = EasyMock.createNiceMock(JiraAuthenticationContext.class);
        EasyMock.replay(eventPublisher, authenticationContext);

        user = new MockApplicationUser("admin");
        perm1 = new SharePermissionImpl(GroupShareType.TYPE, "jira-user", null);
        perm2 = new SharePermissionImpl(GlobalShareType.TYPE, null, null);

        portalPage1 = PortalPage.id(1L).name("one").description("one description").owner(user).build();
        portalPage2 = PortalPage.id(2L).name("two").description("two description").owner(user).build();
        portalPage3 = PortalPage.id(3L).name("three").description("three description").owner(user).build();
        defaultPage = PortalPage.id(23L).name("default").description("desc").systemDashboard().build();

        configList1 = EasyList.build(new PortletConfigurationImpl(new Long(1), portalPage1.getId(), new Integer(1), new Integer(1),
            null, null, Collections.<String,String>emptyMap()), new PortletConfigurationImpl(new Long(2), portalPage1.getId(), new Integer(1), new Integer(2), null, null, Collections.<String,String>emptyMap()));

        configList2 = EasyList.build(new PortletConfigurationImpl(new Long(3), portalPage2.getId(), new Integer(1), new Integer(1),
            null, null, Collections.<String,String>emptyMap()), new PortletConfigurationImpl(new Long(4), portalPage2.getId(), new Integer(1), new Integer(2), null, null, Collections.<String,String>emptyMap()));

        configList3 = EasyList.build(new PortletConfigurationImpl(new Long(5), portalPage3.getId(), new Integer(1), new Integer(1),
            null, null, Collections.<String,String>emptyMap()), new PortletConfigurationImpl(new Long(6), portalPage3.getId(), new Integer(1), new Integer(2), null, null, Collections.<String,String>emptyMap()));

    }

    private PortalPageManager createDefaultPortalPageManager()
    {
        replayMocks();

        return new DefaultPortalPageManager(shareManager, portalPageStore, portletConfigurationManager,
            new MockSharedEntityIndexer(), eventPublisher, authenticationContext);
    }

    private void replayMocks()
    {
        portalPageStoreCtrl.replay();
        shareManagerCtrl.replay();
        portletConfigurationManagerCtrl.replay();
    }

    private void verifyMocks()
    {
        portalPageStoreCtrl.verify();
        shareManagerCtrl.verify();
        portletConfigurationManagerCtrl.verify();
    }

    @Test
    public void testGetAllOwnedPortalPagesNullUser()
    {
        try
        {
            final PortalPageManager portalPageManager = createDefaultPortalPageManager();
            portalPageManager.getAllOwnedPortalPages((ApplicationUser) null);
            fail(" we dont want a null user allowed. ever!");
        }
        catch (final IllegalArgumentException e)
        {
            // expected
        }
    }

    @Test
    public void testGetAllOwnedPortalPagesNullReturned()
    {
        portalPageStore.getAllOwnedPortalPages(user);
        portalPageStoreCtrl.setReturnValue(null);

        final PortalPageManager portalPageManager = createDefaultPortalPageManager();

        final Collection results = portalPageManager.getAllOwnedPortalPages(user);
        assertNotNull(results);
        assertTrue(results.isEmpty());

        verifyMocks();
    }

    @Test
    public void testGetAllOwnedPortalPagesEmptyList()
    {
        portalPageStore.getAllOwnedPortalPages(user);
        portalPageStoreCtrl.setReturnValue(Collections.EMPTY_LIST);

        final PortalPageManager portalPageManager = createDefaultPortalPageManager();

        final Collection results = portalPageManager.getAllOwnedPortalPages(user);
        assertNotNull(results);
        assertTrue(results.isEmpty());

        verifyMocks();
    }

    @Test
    public void testGetAllOwnedPortalPagesNonEmptyList()
    {
        final List returnedList = EasyList.build(portalPage1, portalPage2, portalPage3);

        portalPageStore.getAllOwnedPortalPages(user);
        portalPageStoreCtrl.setReturnValue(returnedList);

        final SharePermissions portalPage2Perm = new SharePermissions(Collections.singleton(perm2));
        final SharePermissions portalPage1Perm = new SharePermissions(Collections.singleton(perm1));

        shareManager.getSharePermissions(portalPage1);
        shareManagerCtrl.setReturnValue(portalPage1Perm);

        shareManager.getSharePermissions(portalPage2);
        shareManagerCtrl.setReturnValue(portalPage2Perm);

        shareManager.getSharePermissions(portalPage3);
        shareManagerCtrl.setReturnValue(SharePermissions.PRIVATE);

        final PortalPageManager portalPageManager = createDefaultPortalPageManager();

        final Collection results = portalPageManager.getAllOwnedPortalPages(user);
        assertNotNull(results);
        assertEquals(3, results.size());

        final Iterator iterator = results.iterator();
        PortalPage result = (PortalPage) iterator.next();
        assertEquals(portalPage1.getId(), result.getId());
        assertEquals(portalPage1Perm, result.getPermissions());

        result = (PortalPage) iterator.next();
        assertEquals(portalPage2.getId(), result.getId());
        assertEquals(portalPage2Perm, result.getPermissions());

        result = (PortalPage) iterator.next();
        assertEquals(portalPage3.getId(), result.getId());
        assertEquals(SharePermissions.PRIVATE, result.getPermissions());

        verifyMocks();
    }

    @Test
    public void testgetPortalPageByNameNullUser()
    {
        final PortalPageManager portalPageManager = createDefaultPortalPageManager();
        try
        {
            portalPageManager.getPortalPageByName((ApplicationUser) null, TestDefaultPortalPageManager.PAGE_NAME);
            fail("Should have barfed on null user");
        }
        catch (final IllegalArgumentException e)
        {}
    }

    @Test
    public void testgetPortalPageByNameNullName()
    {
        try
        {
            final PortalPageManager portalPageManager = createDefaultPortalPageManager();
            portalPageManager.getPortalPageByName(user, (String) null);
            fail("Should not accept null filter name.");
        }
        catch (final IllegalArgumentException e)
        {
            // expected.
        }

        verifyMocks();
    }

    @Test
    public void test_getPortalPageByNameNullPortalPageReturnedByStore()
    {
        portalPageStore.getPortalPageByOwnerAndName(user, TestDefaultPortalPageManager.PAGE_NAME);
        portalPageStoreCtrl.setReturnValue(null);

        final PortalPageManager portalPageManager = createDefaultPortalPageManager();

        final PortalPage portalPage = portalPageManager.getPortalPageByName(user, TestDefaultPortalPageManager.PAGE_NAME);
        assertNull(portalPage);

        verifyMocks();
    }

    @Test
    public void test_getPortalPageByNameWithPortalPageNoShares()
    {
        portalPageStore.getPortalPageByOwnerAndName(user, TestDefaultPortalPageManager.PAGE_NAME);
        portalPageStoreCtrl.setReturnValue(portalPage1);

        shareManager.getSharePermissions(portalPage1);
        shareManagerCtrl.setReturnValue(SharePermissions.PRIVATE);

        final PortalPageManager portalPageManager = createDefaultPortalPageManager();

        final PortalPage portalPage = portalPageManager.getPortalPageByName(user, TestDefaultPortalPageManager.PAGE_NAME);
        assertNotNull(portalPage);
        assertEquals(portalPage1, portalPage);
        assertEquals(SharePermissions.PRIVATE, portalPage.getPermissions());

        verifyMocks();
    }

    @Test
    public void test_getPortalPageByNameWithPortalPageWithShares()
    {
        final SharePermissions permSet2 = new SharePermissions(Collections.singleton(perm2));

        portalPageStore.getPortalPageByOwnerAndName(user, TestDefaultPortalPageManager.PAGE_NAME);
        portalPageStoreCtrl.setReturnValue(portalPage1);

        shareManager.getSharePermissions(portalPage1);
        shareManagerCtrl.setReturnValue(permSet2);

        final PortalPageManager portalPageManager = createDefaultPortalPageManager();

        final PortalPage portalPage = portalPageManager.getPortalPageByName(user, TestDefaultPortalPageManager.PAGE_NAME);
        assertNotNull(portalPage);
        assertEquals(portalPage1.getId(), portalPage.getId());
        assertEquals(permSet2, portalPage.getPermissions());

        verifyMocks();
    }

    @Test
    public void testGetPortalPageNullId()
    {
        try
        {
            final PortalPageManager portalPageManager = createDefaultPortalPageManager();
            portalPageManager.getPortalPage(user, (Long) null);

            fail("Should not accept a null id.");
        }
        catch (final IllegalArgumentException e)
        {
            // excepted.
        }

        verifyMocks();
    }

    @Test
    public void testGetPortalPageNullUserNoPerm()
    {
        portalPageStore.getPortalPage(portalPage1.getId());
        portalPageStoreCtrl.setReturnValue(portalPage1);

        shareManager.isSharedWith((ApplicationUser) null, portalPage1);
        shareManagerCtrl.setReturnValue(false);

        final PortalPageManager portalPageManager = createDefaultPortalPageManager();

        final PortalPage portalPage = portalPageManager.getPortalPage((ApplicationUser) null, portalPage1.getId());
        assertNull(portalPage);

        verifyMocks();
    }

    @Test
    public void testGetPortalPageNullUserHasPerm()
    {
        portalPageStore.getPortalPage(portalPage2.getId());
        portalPageStoreCtrl.setReturnValue(portalPage2);

        shareManager.isSharedWith((ApplicationUser) null, portalPage2);
        shareManagerCtrl.setReturnValue(true);

        final SharePermissions permSet2 = new SharePermissions(Collections.singleton(perm2));

        shareManager.getSharePermissions(portalPage2);
        shareManagerCtrl.setReturnValue(permSet2);

        final PortalPageManager portalPageManager = createDefaultPortalPageManager();
        final PortalPage portalPage = portalPageManager.getPortalPage((ApplicationUser) null, portalPage2.getId());

        assertNotNull(portalPage);
        assertEquals(portalPage2.getId(), portalPage.getId());
        assertEquals(permSet2, portalPage.getPermissions());

        verifyMocks();
    }

    @Test
    public void testGetPortalPageUserNoPerm()
    {
        portalPageStore.getPortalPage(portalPage1.getId());
        portalPageStoreCtrl.setReturnValue(portalPage1);

        shareManager.isSharedWith(user, portalPage1);
        shareManagerCtrl.setReturnValue(false);

        final PortalPageManager portalPageManager = createDefaultPortalPageManager();
        final PortalPage portalPage = portalPageManager.getPortalPage(user, portalPage1.getId());

        assertNull(portalPage);

        verifyMocks();
    }

    @Test
    public void testGetPortalPageUserHasPerm()
    {
        portalPageStore.getPortalPage(portalPage1.getId());
        portalPageStoreCtrl.setReturnValue(portalPage1);

        shareManager.isSharedWith(user, portalPage1);
        shareManagerCtrl.setReturnValue(true);

        final SharePermissions permSet1 = new SharePermissions(Collections.singleton(perm1));

        shareManager.getSharePermissions(portalPage1);
        shareManagerCtrl.setReturnValue(permSet1);

        final PortalPageManager portalPageManager = createDefaultPortalPageManager();
        final PortalPage portalPage = portalPageManager.getPortalPage(user, portalPage1.getId());

        assertNotNull(portalPage);
        assertEquals(portalPage1.getId(), portalPage.getId());
        assertEquals(permSet1, portalPage.getPermissions());

        verifyMocks();
    }

    @Test
    public void testCreateWithNullRequest()
    {
        final PortalPageManager portalPageManager = createDefaultPortalPageManager();
        try
        {
            portalPageManager.create(null);
            fail("Should not accept null search portalPage.");
        }
        catch (final IllegalArgumentException e)
        {
            // exception.
        }
    }

    @Test
    public void testCreateWithNoOwner()
    {
        final PortalPageManager portalPageManager = createDefaultPortalPageManager();
        final PortalPage portalPage = PortalPage.name("name").description("desc").owner((ApplicationUser) null).build();
        try
        {
            portalPageManager.create(portalPage);
            fail("Should not accept null owner.");
        }
        catch (final IllegalArgumentException e)
        {
            // exception.
        }
    }

    @Test
    public void testCreateWithNoName()
    {
        final PortalPageManager portalPageManager = createDefaultPortalPageManager();
        final PortalPage portalPage = PortalPage.name(null).description("desc").owner(user).build();
        try
        {
            portalPageManager.create(portalPage);
            fail("Should not accept null name.");
        }
        catch (final IllegalArgumentException e)
        {
            // exception.
        }
    }

    @Test
    public void testCreateNoPerms()
    {
        portalPageStore.create(portalPage1);
        portalPageStoreCtrl.setReturnValue(portalPage1);

        portalPage1 = PortalPage.portalPage(portalPage1).permissions(SharePermissions.PRIVATE).build();
        shareManager.updateSharePermissions(portalPage1);
        shareManagerCtrl.setReturnValue(SharePermissions.PRIVATE);

        final PortalPageManager portalPageManager = createDefaultPortalPageManager();

        final PortalPage portalPage = portalPageManager.create(portalPage1);
        assertNotNull(portalPage);
        assertEquals(portalPage1, portalPage);
        assertTrue(portalPage.getPermissions().isEmpty());

        verifyMocks();
    }

    @Test
    public void testCreateHasPerms()
    {
        final HashSet permSet1 = new HashSet();
        permSet1.add(perm1);
        permSet1.add(perm2);

        portalPage1 = PortalPage.portalPage(portalPage1).permissions(new SharePermissions(permSet1)).build();

        portalPageStore.create(portalPage1);
        portalPageStoreCtrl.setReturnValue(portalPage1);

        shareManager.updateSharePermissions(portalPage1);
        shareManagerCtrl.setReturnValue(new SharePermissions(permSet1));

        final PortalPageManager portalPageManager = createDefaultPortalPageManager();

        final PortalPage portalPage = portalPageManager.create(portalPage1);
        assertNotNull(portalPage);
        assertEquals(portalPage1, portalPage);
        assertEquals(new SharePermissions(permSet1), portalPage.getPermissions());

        verifyMocks();
    }

    @Test
    public void testUpdateWithNullRequest()
    {
        final PortalPageManager portalPageManager = createDefaultPortalPageManager();
        try
        {
            portalPageManager.update(null);
            fail("Should not accept null search portalPage.");
        }
        catch (final IllegalArgumentException e)
        {
            // exception.
        }
    }

    @Test
    public void testUpdateWithNoOwner()
    {
        final PortalPageManager portalPageManager = createDefaultPortalPageManager();
        final PortalPage portalPage = PortalPage.name("name").description("desc").owner((ApplicationUser) null).build();
        try
        {
            portalPageManager.update(portalPage);
            fail("Should not accept null owner.");
        }
        catch (final IllegalArgumentException e)
        {
            // exception.
        }
    }

    @Test
    public void testUpdateWithNoName()
    {
        final PortalPageManager portalPageManager = createDefaultPortalPageManager();
        final PortalPage portalPage = PortalPage.name(null).description("desc").owner(user).build();
        try
        {
            portalPageManager.update(portalPage);
            fail("Should not accept null name.");
        }
        catch (final IllegalArgumentException e)
        {
            // exception.
        }
    }

    @Test
    public void testUpdateWithNoId()
    {
        final PortalPageManager portalPageManager = createDefaultPortalPageManager();
        final PortalPage notIdRequest = PortalPage.name("user").description("desc").owner(user).build();
        try
        {
            portalPageManager.update(notIdRequest);
            fail("Should not accept null name.");
        }
        catch (final IllegalArgumentException e)
        {
            // exception.
        }
    }

    @Test
    public void testUpdateNoPerms()
    {
        portalPageStore.update(portalPage3);
        portalPageStoreCtrl.setReturnValue(portalPage3);

        portalPage3 = PortalPage.portalPage(portalPage3).permissions(SharePermissions.PRIVATE).build();
        shareManager.updateSharePermissions(portalPage3);
        shareManagerCtrl.setReturnValue(SharePermissions.PRIVATE);

        final PortalPageManager portalPageManager = createDefaultPortalPageManager();

        final PortalPage portalPage = portalPageManager.update(portalPage3);
        assertNotNull(portalPage);
        assertEquals(portalPage3, portalPage);
        assertTrue(portalPage.getPermissions().isEmpty());

        verifyMocks();
    }

    @Test
    public void testUpdateHasPerms()
    {
        final SharePermissions permSet1 = new SharePermissions(Collections.singleton(perm2));
        portalPage3 = PortalPage.portalPage(portalPage3).permissions(permSet1).build();

        portalPageStore.update(portalPage3);
        portalPageStoreCtrl.setReturnValue(portalPage3);

        shareManager.updateSharePermissions(portalPage3);
        shareManagerCtrl.setReturnValue(permSet1);

        final PortalPageManager portalPageManager = createDefaultPortalPageManager();

        final PortalPage portalPage = portalPageManager.update(portalPage3);
        assertNotNull(portalPage);
        assertEquals(portalPage3, portalPage);
        assertEquals(permSet1, portalPage.getPermissions());

        verifyMocks();
    }

    @Test
    public void testUserDefaultDashboardCanBeSaved() throws Exception
    {
        portalPageStore.update(defaultPage);
        portalPageStoreCtrl.setReturnValue(defaultPage);

        shareManager.updateSharePermissions(defaultPage);
        shareManagerCtrl.setReturnValue(SharedEntity.SharePermissions.PRIVATE);

        final PortalPageManager portalPageManager = createDefaultPortalPageManager();

        final PortalPage portalPage = portalPageManager.update(defaultPage);
        assertNotNull(portalPage);
        assertEquals(defaultPage, portalPage);

        verifyMocks();
    }

    @Test
    public void testDeleteNull()
    {
        final PortalPageManager portalPageManager = createDefaultPortalPageManager();
        try
        {
            portalPageManager.delete(null);
            fail("Should not accept null id.");
        }
        catch (final IllegalArgumentException e)
        {
            // expected.
        }

        verifyMocks();
    }

    @Test
    public void testDeleteValid()
    {
        final PortletConfiguration deletedPC = makePC(1, 2, GOOD_PORTLET, 2, 3);

        portletConfigurationManager.getByPortalPage(portalPage1.getId());
        portletConfigurationManagerCtrl.setReturnValue(Collections.singletonList(deletedPC));

        portletConfigurationManager.delete(deletedPC);

        shareManager.deletePermissions(new Identifier(portalPage1.getId(), PortalPage.ENTITY_TYPE, (ApplicationUser) null));

        portalPageStore.delete(portalPage1.getId());

        final PortalPageManager manager = createDefaultPortalPageManager();
        manager.delete(portalPage1.getId());

        verifyMocks();
    }

    @Test
    public void testAdjustFavouriteCountNullEntity()
    {
        final PortalPageManager manager = createDefaultPortalPageManager();
        try
        {
            manager.adjustFavouriteCount(null, 1);
            fail("Should have barfed!");
        }
        catch (final IllegalArgumentException e)
        {
            // expected.
        }

        verifyMocks();
    }

    @Test
    public void testAdjustFavouriteCountInvalidEntity()
    {
        final PortalPageManager manager = createDefaultPortalPageManager();
        final SharedEntity entity = new SharedEntity.Identifier(new Long(1), SearchRequest.ENTITY_TYPE, user);

        try
        {
            manager.adjustFavouriteCount(entity, 1);
            fail("Should have barfed!");
        }
        catch (final IllegalArgumentException e)
        {
            // expected.
        }

        verifyMocks();
    }

    @Test
    public void testAdjustFavouriteCount()
    {
        portalPageStore.adjustFavouriteCount(portalPage1, 1);
        portalPageStoreCtrl.setReturnValue(portalPage1);

        shareManager.getSharePermissions(portalPage1);
        shareManagerCtrl.setReturnValue(SharePermissions.PRIVATE);

        portalPageStore.getPortalPage(1L);
        portalPageStoreCtrl.setReturnValue(portalPage1);

        shareManager.getSharePermissions(portalPage1);
        shareManagerCtrl.setReturnValue(SharePermissions.PRIVATE);

        final PortalPageManager manager = createDefaultPortalPageManager();
        manager.adjustFavouriteCount(portalPage1, 1);
        portalPage1 = manager.getPortalPageById(portalPage1.getId());
        verifyMocks();
    }

    @Test
    public void testAdjustFavouriteCountNegative()
    {
        portalPageStore.adjustFavouriteCount(portalPage1, -3);
        portalPageStoreCtrl.setReturnValue(portalPage1);

        shareManager.getSharePermissions(portalPage1);
        shareManagerCtrl.setReturnValue(SharePermissions.PRIVATE);

        portalPageStore.getPortalPage(portalPage1.getId());
        portalPageStoreCtrl.setReturnValue(portalPage1);

        shareManager.getSharePermissions(portalPage1);
        shareManagerCtrl.setReturnValue(SharePermissions.PRIVATE);

        final PortalPageManager manager = createDefaultPortalPageManager();
        manager.adjustFavouriteCount(portalPage1, -3);

        portalPage1 = manager.getPortalPageById(portalPage1.getId());

        verifyMocks();
    }

    @Test
    public void test_getPortalPageById_NullId() throws Exception
    {
        final PortalPageManager manager = createDefaultPortalPageManager();
        try
        {
            manager.getPortalPageById(null);
            fail("Should have barfed");
        }
        catch (final IllegalArgumentException e)
        {
            // expected
        }
    }

    @Test
    public void test_getPortalPageById() throws Exception
    {
        final SharePermissions sharePermissions = new SharePermissions(Collections.singleton(perm1));

        portalPageStore.getPortalPage(portalPage1.getId());
        portalPageStoreCtrl.setReturnValue(portalPage1);

        shareManager.getSharePermissions(portalPage1);
        shareManagerCtrl.setReturnValue(sharePermissions);

        final PortalPageManager manager = createDefaultPortalPageManager();
        final PortalPage portalPage = manager.getPortalPageById(portalPage1.getId());
        assertEquals(portalPage1.getId(), portalPage.getId());
        assertEquals(sharePermissions, portalPage.getPermissions());

        verifyMocks();
    }

    @Test
    public void test_getPortalPageById_NullReturned() throws Exception
    {
        portalPageStore.getPortalPage(portalPage1.getId());
        portalPageStoreCtrl.setReturnValue(null);

        final PortalPageManager manager = createDefaultPortalPageManager();
        final PortalPage portalPage = manager.getPortalPageById(portalPage1.getId());
        assertNull(portalPage);

        verifyMocks();
    }

    @Test
    public void test_getSystemDefaultPortalPage() throws Exception
    {
        final SharePermissions sharePermissions = SharePermissions.PRIVATE;

        portalPageStore.getSystemDefaultPortalPage();
        portalPageStoreCtrl.setReturnValue(portalPage1);

        shareManager.getSharePermissions(portalPage1);
        shareManagerCtrl.setReturnValue(sharePermissions);

        final PortalPageManager manager = createDefaultPortalPageManager();
        final PortalPage portalPage = manager.getSystemDefaultPortalPage();
        assertEquals(portalPage1.getId(), portalPage.getId());
        assertEquals(sharePermissions, portalPage.getPermissions());

        verifyMocks();
    }

    /**
     * Make sure the configuration is not saved when it does not exist on the page.
     */
    @Test
    public void testSavePortalPagePortletConfigurationNullConfiguration()
    {
        final PortletConfiguration expectedPC = makePC(new Long(NEIGHBOUR_OF_THE_BEAST), DEVILS_ID, GOOD_PORTLET, new Integer(1), new Integer(3));

        portletConfigurationManager.getByPortletId((long) NEIGHBOUR_OF_THE_BEAST);
        portletConfigurationManagerCtrl.setReturnValue(null);

        final PortalPageManager manager = createDefaultPortalPageManager();

        try
        {
            manager.saveLegacyPortletConfiguration(expectedPC);
            fail("Should have thrown exception!");
        }
        catch (IllegalStateException e)
        {
            //yay
        }

        verifyMocks();
    }

    /**
     * Make sure that PortalPageManager.getSharedEntity calls through to correct method.
     */
    @Test
    public void testGetSharedEntityId()
    {
        final AtomicInteger getPortalPageByIdCount = new AtomicInteger(0);
        final PortalPageManager portalPageManager = new DefaultPortalPageManager(shareManager, portalPageStore,
            portletConfigurationManager, new MockSharedEntityIndexer(), eventPublisher, authenticationContext)
        {
            @Override
            public PortalPage getPortalPageById(final Long portalPageId)
            {
                assertEquals(portalPage1.getId(), portalPageId);

                getPortalPageByIdCount.incrementAndGet();

                return portalPage2;
            }
        };

        replayMocks();

        final SharedEntity actualPortalPage = portalPageManager.getSharedEntity(portalPage1.getId());

        assertEquals(1, getPortalPageByIdCount.get());
        assertEquals(portalPage2, actualPortalPage);

        verifyMocks();
    }

    /**
     * Make sure the method works as expected when the id is null.
     */
    @Test
    public void testGetSharedEntityIdNull()
    {
        final PortalPageManager portalPageManager = createDefaultPortalPageManager();
        try
        {
            portalPageManager.getSharedEntity(null);
            fail("Expected an exception to be thrown on illegal argument.");
        }
        catch (final IllegalArgumentException ignored)
        {}

        verifyMocks();
    }

    /**
     * Make sure that PortalPageManager.getSharedEntity calls the correct method.
     */
    @Test
    public void testGetSharedEntityUser()
    {
        final AtomicInteger getPortalPageCount = new AtomicInteger(0);
        final PortalPageManager portalPageManager = new DefaultPortalPageManager(shareManager, portalPageStore,
                portletConfigurationManager, new MockSharedEntityIndexer(), eventPublisher, authenticationContext)
        {
            @Override
            public PortalPage getPortalPage(final ApplicationUser actualUser, final Long id)
            {
                assertEquals(portalPage1.getId(), id);
                assertEquals(actualUser, user);
                getPortalPageCount.incrementAndGet();

                return portalPage3;
            }
        };

        replayMocks();

        final SharedEntity actualPortalPage = portalPageManager.getSharedEntity(user.getDirectoryUser(), portalPage1.getId());

        assertEquals(1, getPortalPageCount.get());
        assertSame(portalPage3, actualPortalPage);

        verifyMocks();
    }

    /**
     * Make sure the method works as expected when the id is null.
     */
    @Test
    public void testGetSharedEntityUserNullId()
    {
        final PortalPageManager portalPageManager = createDefaultPortalPageManager();
        try
        {
            portalPageManager.getSharedEntity(null, null);
            fail("Expected an exception to be thrown on illegal argument.");
        }
        catch (final IllegalArgumentException ignored)
        {}

        verifyMocks();
    }

    /**
     * Make sure that PortalPageManager.getSharedEntity calls the correct method.
     */
    @Test
    public void testGetSharedEntityUserAnonymous()
    {
        final AtomicInteger getPortalPageCount = new AtomicInteger(0);
        final PortalPageManager portalPageManager = new DefaultPortalPageManager(shareManager, portalPageStore,
            portletConfigurationManager, new MockSharedEntityIndexer(), eventPublisher, authenticationContext)
        {
            @Override
            public PortalPage getPortalPage(final ApplicationUser inputUser, final Long id)
            {
                assertEquals(portalPage2.getId(), id);
                assertNull(inputUser);
                getPortalPageCount.incrementAndGet();

                return portalPage1;
            }
        };

        replayMocks();

        final SharedEntity actualPortalPage = portalPageManager.getSharedEntity(null, portalPage2.getId());

        assertEquals(1, getPortalPageCount.get());
        assertSame(portalPage1, actualPortalPage);

        verifyMocks();
    }

    /**
     * Make sure that method throws an error with a null SharedEntity.
     */
    @Test
    public void testHasPermissionToUserNullSharedEntity()
    {
        final PortalPageManager portalPageManager = createDefaultPortalPageManager();

        try
        {
            portalPageManager.hasPermissionToUse(null, null);
            fail("Expected Illegal Argument.");
        }
        catch (final IllegalArgumentException expected)
        {

        }
    }

    /**
     * Make sure the user has permission to use the system default.
     */
    @Test
    public void testHasPermissionsToUseSystemDefault()
    {
        final PortalPageManager portalPageManager = createDefaultPortalPageManager();
        final boolean actualResult = portalPageManager.hasPermissionToUse(user.getDirectoryUser(), defaultPage);

        assertTrue(actualResult);
    }

    /**
     * Make sure method returns true when the user is able to see the shared entity.
     */
    @Test
    public void testHasPermissionsToUseGood()
    {
        shareManager.isSharedWith((ApplicationUser) null, portalPage1);
        shareManagerCtrl.setReturnValue(true);

        final PortalPageManager portalPageManager = createDefaultPortalPageManager();
        final boolean actualResult = portalPageManager.hasPermissionToUse(null, portalPage1);

        assertTrue(actualResult);
    }

    /**
     * Make sure method returns false when the user is not able to see the shared entity.
     */
    @Test
    public void testHasPermissionsToUseBad()
    {
        shareManager.isSharedWith(user, portalPage2);
        shareManagerCtrl.setReturnValue(false);

        final PortalPageManager portalPageManager = createDefaultPortalPageManager();
        final boolean actualResult = portalPageManager.hasPermissionToUse(user.getDirectoryUser(), portalPage2);

        assertFalse(actualResult);
    }

    @Test
    public void testGetPortletConfigurationsNone()
    {
        shareManager.getSharePermissions(portalPage1);
        shareManagerCtrl.setReturnValue(SharedEntity.SharePermissions.GLOBAL);

        portalPageStore.getPortalPage(portalPage1.getId());
        portalPageStoreCtrl.setReturnValue(portalPage1);

        portletConfigurationManager.getByPortalPage(portalPage1.getId());
        portletConfigurationManagerCtrl.setReturnValue(Collections.<PortletConfiguration>emptyList());

        final PortalPageManager portalPageManager = createDefaultPortalPageManager();
        final List<List<PortletConfiguration>> configs = portalPageManager.getPortletConfigurations(portalPage1.getId());
        assertTrue(configs.isEmpty());
    }

    @Test
    public void testGetPortletConfigurations()
    {
        PortalPage portalPage = PortalPage.id(1000L).name("Mine").layout(Layout.AAA).build();
        shareManager.getSharePermissions(portalPage);
        shareManagerCtrl.setReturnValue(SharedEntity.SharePermissions.GLOBAL);

        portalPageStore.getPortalPage(1000L);
        portalPageStoreCtrl.setReturnValue(portalPage);

        portletConfigurationManager.getByPortalPage(portalPage.getId());

        final PortletConfigurationImpl pc1 = new PortletConfigurationImpl(10000L, 1000L, 0, 0, null, null, Collections.<String,String>emptyMap());
        final PortletConfigurationImpl pc2 = new PortletConfigurationImpl(10010L, 1000L, 0, 1, null, null, Collections.<String,String>emptyMap());
        final PortletConfigurationImpl pc3 = new PortletConfigurationImpl(10030L, 1000L, 2, 0, null, null, Collections.<String,String>emptyMap());
        final PortletConfigurationImpl pc4 = new PortletConfigurationImpl(10040L, 1000L, 2, 1, null, null, Collections.<String,String>emptyMap());
        final PortletConfigurationImpl pc5 = new PortletConfigurationImpl(10050L, 1000L, 2, 5, null, null, Collections.<String,String>emptyMap());
        portletConfigurationManagerCtrl.setReturnValue(CollectionBuilder.newBuilder(pc4, pc1, pc2, pc5, pc3).asList());

        final PortalPageManager portalPageManager = createDefaultPortalPageManager();
        final List<List<PortletConfiguration>> configs = portalPageManager.getPortletConfigurations(1000L);
        List<List<PortletConfiguration>> expected = new ArrayList<List<PortletConfiguration>>();
        List<PortletConfiguration> col1 = new ArrayList<PortletConfiguration>();
        col1.add(pc1);
        col1.add(pc2);
        List<PortletConfiguration> col2 = new ArrayList<PortletConfiguration>();
        List<PortletConfiguration> col3 = new ArrayList<PortletConfiguration>();
        col3.add(pc3);
        col3.add(pc4);
        col3.add(pc5);
        expected.add(col1);
        expected.add(col2);
        expected.add(col3);
        assertEquals(expected, configs);
    }

    PortletConfiguration makePC(final long id, final long portalPageId, final String portletKey, final int suggestedCol, final int suggestRow)
    {
        return makePC(new Long(id), new Long(portalPageId), portletKey, new Integer(suggestedCol), new Integer(suggestRow));
    }

    PortletConfiguration makePC(final Long id, final Long portalPageId, final String portletKey, final Integer suggestedCol, final Integer suggestRow)
    {
        return new MockPortletConfiguration(id, suggestedCol, suggestRow, portalPageId, portletKey);
    }
}
