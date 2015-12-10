package com.atlassian.jira.bc.portal;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.bc.MockJiraServiceContext;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.exception.PermissionException;
import com.atlassian.jira.favourites.FavouritesManager;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.mock.i18n.MockI18nHelper;
import com.atlassian.jira.portal.PortalPage;
import com.atlassian.jira.portal.PortalPageManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.sharing.SharePermission;
import com.atlassian.jira.sharing.SharePermissionImpl;
import com.atlassian.jira.sharing.ShareTypeValidatorUtils;
import com.atlassian.jira.sharing.SharedEntity;
import com.atlassian.jira.sharing.type.GlobalShareType;
import com.atlassian.jira.sharing.type.ShareType;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.MockApplicationUser;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;

import org.junit.Before;
import org.junit.Test;

import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class TestDefaultPortalPageService
{
    protected PortalPageManager portalPageManager;
    protected FavouritesManager<PortalPage> favouritesManager;
    protected ShareTypeValidatorUtils shareTypeValidatorUtils;
    protected PermissionManager permissionManager;
    protected UserUtil userUtil;
    protected ApplicationUser user;
    protected PortalPage portalPage1;
    protected PortalPage portalPage2;
    protected PortalPage portalPage3;
    protected PortalPage portalPageClone;
    protected PortalPage portalPageSystemDefault;
    protected PortalPage portalPageNotOwnedByUser;
    protected static final Long CLONE_ID = (long) 666;
    protected static final String BAD_PC = "bad";
    protected static final String GOOD_PC = "good";
    protected ApplicationUser notTheOwner;
    protected static final String EXPECTED_PORTLET_KEY = "expectedPortletKey";

    protected PortalPageService createPortalPageService()
    {
        replay(portalPageManager, favouritesManager, shareTypeValidatorUtils, permissionManager, userUtil);

        return new DefaultPortalPageService(portalPageManager, shareTypeValidatorUtils, favouritesManager, permissionManager, userUtil);
    }

    protected JiraServiceContext createContext(final User user)
    {
        return new MockJiraServiceContext(user)
        {
            public I18nHelper getI18nBean()
            {
                return new MockI18nHelper();
            }
        };
    }

    protected JiraServiceContext createContext(final ApplicationUser user)
    {
        return new MockJiraServiceContext(user.getDirectoryUser())
        {
            public I18nHelper getI18nBean()
            {
                return new MockI18nHelper();
            }
            @Override
            public ApplicationUser getLoggedInApplicationUser()
            {
                return user;
            }
        };
    }

    protected JiraServiceContext createContext()
    {
        return createContext(user);
    }

    @Before
    public void setUp() throws Exception
    {
        portalPageManager = createStrictMock(PortalPageManager.class);
        //noinspection unchecked
        favouritesManager = createStrictMock(FavouritesManager.class);
        shareTypeValidatorUtils = createStrictMock(ShareTypeValidatorUtils.class);
        permissionManager = createStrictMock(PermissionManager.class);
        userUtil = createStrictMock(UserUtil.class);

        user = new MockApplicationUser("admin");
        notTheOwner = new MockApplicationUser("notTheOwner");

        portalPage1 = PortalPage.id(1L).name("one").description("one description").owner(user).build();
        portalPage2 = PortalPage.id(2L).name("two").description("two description").owner(user).build();
        portalPage3 = PortalPage.id(3L).name("three").description("three description").owner(user).build();
        portalPageClone = PortalPage.id(CLONE_ID).name("clone").description("clone description").owner(user).build();
        portalPageSystemDefault = PortalPage.id(CLONE_ID).name("clone").description("clone description").systemDashboard().build();

        final ApplicationUser notUser = new MockApplicationUser("notMe");
        portalPageNotOwnedByUser = PortalPage.id(CLONE_ID).name("notownedbyuser").description("").owner(notUser).build();

        ComponentAccessor.initialiseWorker(new MockComponentWorker());
    }

    @Test
    public void testFavouritePortalPages()
    {
        final List<PortalPage> portalPages = Arrays.asList(portalPage2, portalPage3);
        final List<Long> favouriteIds = Arrays.asList(portalPage3.getId(), portalPage2.getId(), portalPage1.getId()); // return is reverse order to test
        // sorting

        expect(favouritesManager.getFavouriteIds(user, PortalPage.ENTITY_TYPE)).andReturn(favouriteIds);

        expect(portalPageManager.getPortalPage(user, portalPage3.getId())).andReturn(portalPage3);
        expect(portalPageManager.getPortalPage(user, portalPage2.getId())).andReturn(portalPage2);
        expect(portalPageManager.getPortalPage(user, portalPage1.getId())).andReturn(null);

        final PortalPageService service = createPortalPageService();

        final Collection<PortalPage> results = service.getFavouritePortalPages(user);

        assertThesePagesFound(portalPages, results);

        verifyMocks();
    }

    private void verifyMocks()
    {
        verify(portalPageManager, favouritesManager, shareTypeValidatorUtils, permissionManager, userUtil);
    }

    private void assertThesePagesFound(final List<PortalPage> expectedPages, final Collection<PortalPage> results)
    {
        assertNotNull(results);
        assertNotNull(expectedPages);
        assertEquals(results.size(), expectedPages.size());
        for (final PortalPage expectedPage : expectedPages)
        {
            boolean found = false;
            for (final PortalPage resultPage : results)
            {
                if (expectedPage.getId().equals(resultPage.getId()))
                {
                    found = true;
                }
            }
            if (!found)
            {
                fail("Failed to find an expected portalPage");
            }
        }
    }

    @Test
    public void testFavouritePortalPagesNullUser()
    {
        final PortalPageService service = createPortalPageService();

        final Collection results = service.getFavouritePortalPages((ApplicationUser) null);

        assertNotNull(results);
        assertTrue(results.isEmpty());

        verifyMocks();
    }

    @Test
    public void testFavouritePortalPagesNoFavourites()
    {
        expect(favouritesManager.getFavouriteIds(user, PortalPage.ENTITY_TYPE)).andReturn(Collections.<Long>emptyList());

        final PortalPageService service = createPortalPageService();

        final Collection results = service.getFavouritePortalPages(user);

        assertNotNull(results);
        assertTrue(results.isEmpty());

        verifyMocks();
    }

    /**
     * Check that {@link DefaultPortalPageService#isFavourite(User,
     * com.atlassian.jira.portal.PortalPage)} always delegate through to the favourites manager.
     *
     * @throws com.atlassian.jira.exception.PermissionException just re-throw for an error for test failure.
     */
    @Test
    public void testIsFavourite() throws PermissionException
    {
        expect(favouritesManager.isFavourite(user, portalPage1)).andReturn(true);

        expect(favouritesManager.isFavourite(user, portalPage2)).andReturn(false);

        final PortalPageService service = createPortalPageService();

        assertTrue(service.isFavourite(user, portalPage1));
        assertFalse(service.isFavourite(user, portalPage2));

        verifyMocks();
    }

    /**
     * Check that {@link DefaultPortalPageService#isFavourite(User,
     * com.atlassian.jira.portal.PortalPage)} always returns false when the user is anonymous.
     */
    @Test
    public void testIsFavouriteAnonymousUser()
    {
        final PortalPageService service = createPortalPageService();

        assertFalse(service.isFavourite((ApplicationUser) null, portalPage2));
        assertFalse(service.isFavourite((ApplicationUser) null, portalPage3));
    }

    /**
     * Check that {@link DefaultPortalPageService#isFavourite(User,
     * com.atlassian.jira.portal.PortalPage)} always returns false on exception.
     *
     * @throws com.atlassian.jira.exception.PermissionException just re-throw for an error for test failure.
     */
    @Test
    public void testIsFavouriteWithException() throws PermissionException
    {
        expect(favouritesManager.isFavourite(user, portalPage1)).andThrow(new PermissionException("testIsFavouriteWithException"));

        final PortalPageService service = createPortalPageService();

        assertFalse(service.isFavourite(user, portalPage1));
    }

    @Test
    public void testOwnedPortalPages()
    {
        expect(portalPageManager.getAllOwnedPortalPages(user)).andReturn(Arrays.asList(portalPage3, portalPage2));

        final PortalPageService service = createPortalPageService();

        final Collection<PortalPage> results = service.getOwnedPortalPages(user);

        assertThesePagesFound(Arrays.asList(portalPage2, portalPage3), results);

        verifyMocks();
    }

    @Test
    public void testDeleteAllPagesForUser()
    {
        expect(portalPageManager.getAllOwnedPortalPages(user)).andReturn(Arrays.asList(portalPage1, portalPage2, portalPage3));

        portalPageManager.delete(portalPage1.getId());
        favouritesManager.removeFavouritesForEntityDelete(portalPage1);

        portalPageManager.delete(portalPage2.getId());
        favouritesManager.removeFavouritesForEntityDelete(portalPage2);

        portalPageManager.delete(portalPage3.getId());
        favouritesManager.removeFavouritesForEntityDelete(portalPage3);

        favouritesManager.removeFavouritesForUser(user, PortalPage.ENTITY_TYPE);
        final PortalPageService service = createPortalPageService();

        service.deleteAllPortalPagesForUser(user);

        verifyMocks();
    }

    @Test
    public void testDeletePortalPage()
    {
        expect(portalPageManager.getPortalPageById(portalPage1.getId())).andReturn(portalPage1);
        expect(portalPageManager.getPortalPage(user, portalPage1.getId())).andReturn(portalPage1);

        portalPageManager.delete(portalPage1.getId());

        favouritesManager.removeFavouritesForEntityDelete(portalPage1);

        final PortalPageService service = createPortalPageService();

        final JiraServiceContext ctx = createContext();

        service.deletePortalPage(ctx, portalPage1.getId());

        assertFalse(ctx.getErrorCollection().hasAnyErrors());

        verifyMocks();
    }

    @Test
    public void testDeletePortalPageNoPortalPage()
    {
        final Long portalPageToDeleteId = 1L;

        expect(portalPageManager.getPortalPageById(portalPage1.getId())).andReturn(null);

        final PortalPageService service = createPortalPageService();
        final JiraServiceContext ctx = createContext();

        service.deletePortalPage(ctx, portalPageToDeleteId);

        final ErrorCollection errorCollection = ctx.getErrorCollection();
        assertTrue(errorCollection.hasAnyErrors());
        assertEquals(1, errorCollection.getErrorMessages().size());
        assertTrue(errorCollection.getErrorMessages().contains("admin.errors.portalpages.nonexistent"));

        verifyMocks();
    }

    @Test
    public void testDeletePortalPageNotAuthor()
    {
        final PortalPage pageOwnedByAnother = PortalPage.name("name").description("desc").owner(new MockApplicationUser("other")).build();
        expect(portalPageManager.getPortalPageById(portalPage1.getId())).andReturn(pageOwnedByAnother);

        final PortalPageService service = createPortalPageService();

        final JiraServiceContext ctx = createContext();

        service.deletePortalPage(ctx, portalPage1.getId());

        final ErrorCollection errorCollection = ctx.getErrorCollection();
        assertTrue(errorCollection.hasAnyErrors());
        assertEquals(1, errorCollection.getErrorMessages().size());
        assertTrue(errorCollection.getErrorMessages().contains("admin.errors.portalpages.must.be.owner"));

        verifyMocks();
    }

    @Test
    public void testDeletePortalPageAsAnonymous()
    {
        final PortalPageService service = createPortalPageService();

        final JiraServiceContext ctx = createContext((User) null);

        service.deletePortalPage(ctx, portalPage1.getId());

        final ErrorCollection errorCollection = ctx.getErrorCollection();
        assertTrue(errorCollection.hasAnyErrors());
        assertEquals(1, errorCollection.getErrorMessages().size());
        assertTrue(errorCollection.getErrorMessages().contains("admin.errors.portalpages.owned.anonymous.user"));

        verifyMocks();
    }

    @Test
    public void testDeletePortalPageNotReturnedFromManager()
    {
        expect(portalPageManager.getPortalPageById(portalPage1.getId())).andReturn(portalPage1);
        expect(portalPageManager.getPortalPage(user, portalPage1.getId())).andReturn(null);


        final PortalPageService service = createPortalPageService();

        final JiraServiceContext ctx = createContext();

        service.deletePortalPage(ctx, portalPage1.getId());

        final ErrorCollection errorCollection = ctx.getErrorCollection();
        assertTrue(errorCollection.hasAnyErrors());
        assertEquals(1, errorCollection.getErrorMessages().size());
        assertTrue(errorCollection.getErrorMessages().contains("admin.errors.portalpages.nonexistent"));

        verifyMocks();
    }

    @Test
    public void testDeletePortalPageNullPortalPageId()
    {
        final PortalPageService service = createPortalPageService();
        final JiraServiceContext ctx = createContext();

        try
        {
            service.deletePortalPage(ctx, null);
            fail("Should not accept a null id.");
        }
        catch (final IllegalArgumentException e)
        {
            // expected.
        }

        verifyMocks();
    }

    @Test
    public void testDeleteCantDeleteSystemDefaultPage() throws Exception
    {
        expect(portalPageManager.getPortalPageById(portalPageSystemDefault.getId())).andReturn(portalPageSystemDefault);

        final PortalPageService service = createPortalPageService();
        final JiraServiceContext serviceContext = createContext();

        service.deletePortalPage(serviceContext, portalPageSystemDefault.getId());

        final ErrorCollection errorCollection = serviceContext.getErrorCollection();
        assertTrue(errorCollection.hasAnyErrors());
        assertTrue(errorCollection.getErrorMessages().contains("admin.errors.portalpages.not.delete.system.default"));

        verifyMocks();
    }

    @Test
    public void testGetPortalPage()
    {
        expect(portalPageManager.getPortalPage(user, portalPage1.getId())).andReturn(portalPage1);

        final PortalPageService service = createPortalPageService();
        final JiraServiceContext ctx = createContext();
        final PortalPage result = service.getPortalPage(ctx, portalPage1.getId());

        assertEquals(portalPage1, result);
        assertFalse(ctx.getErrorCollection().hasAnyErrors());

        verifyMocks();
    }

    @Test
    public void testGetPortalPageDoesntExist()
    {
        final Long nonExistentId = 303L;

        expect(portalPageManager.getPortalPage(user, nonExistentId)).andReturn(null);

        final PortalPageService searchRequestService = createPortalPageService();
        final JiraServiceContext ctx = createContext();

        final PortalPage portalPage = searchRequestService.getPortalPage(ctx, nonExistentId);
        assertNull("where the hell did this come from?", portalPage);

        assertTrue(ctx.getErrorCollection().hasAnyErrors());
        final Collection errorMessages = ctx.getErrorCollection().getErrorMessages();
        assertEquals(1, errorMessages.size());
        assertTrue(errorMessages.contains("admin.errors.portalpages.no.access"));
        verifyMocks();
    }

    @Test
    public void testGetPortalPageAnonymousUser()
    {
        expect(portalPageManager.getPortalPage((ApplicationUser) null, portalPage1.getId())).andReturn(portalPage1);

        final PortalPageService service = createPortalPageService();
        final JiraServiceContext ctx = createContext((User) null);
        final PortalPage result = service.getPortalPage(ctx, portalPage1.getId());

        assertEquals(portalPage1, result);
        assertFalse(ctx.getErrorCollection().hasAnyErrors());

        verifyMocks();
    }

    @Test
    public void testGetPortalPageAnonymousUserDoesntExist()
    {
        final Long nonExistentId = 303L;

        expect(portalPageManager.getPortalPage((ApplicationUser) null, nonExistentId)).andReturn(null);

        final PortalPageService searchRequestService = createPortalPageService();
        final JiraServiceContext ctx = createContext((User) null);

        searchRequestService.getPortalPage(ctx, nonExistentId);

        assertTrue(ctx.getErrorCollection().hasAnyErrors());
        final Collection errorMessages = ctx.getErrorCollection().getErrorMessages();
        assertEquals(1, errorMessages.size());
        assertTrue(errorMessages.contains("admin.errors.portalpages.no.access"));
        verifyMocks();
    }

    @Test
    public void testGetPortalPageNullId()
    {
        final PortalPageService service = createPortalPageService();

        try
        {
            final JiraServiceContext ctx = new JiraServiceContextImpl(user);
            service.getPortalPage(ctx, null);
            fail("Should not be able to pass in null as portalPage id");
        }
        catch (final IllegalArgumentException e)
        {
            // good
        }

        verifyMocks();
    }

    @Test
    public void testGetPortalPageNullCtx()
    {
        final PortalPageService service = createPortalPageService();

        try
        {
            service.getPortalPage(null, 1L);
            fail("Should not be able to pass in null as context.");
        }
        catch (final IllegalArgumentException e)
        {
            // good
        }

        verifyMocks();
    }

    @Test
    public void testGetNonPrivatePortalPagesAll()
    {
        final SharedEntity.SharePermissions permissions = new SharedEntity.SharePermissions(Collections.singleton(new SharePermissionImpl(GlobalShareType.TYPE, null, null)));
        portalPage1 = PortalPage.portalPage(portalPage1).permissions(permissions).build();
        portalPage2 = PortalPage.portalPage(portalPage2).permissions(permissions).build();

        final List<PortalPage> portalPages = Arrays.asList(portalPage2, portalPage1, portalPage3);
        expect(portalPageManager.getAllOwnedPortalPages(user)).andReturn(portalPages);

        final PortalPageService service = createPortalPageService();

        final Collection results = service.getNonPrivatePortalPages(user);

        assertEquals(EasyList.build(portalPage1, portalPage2), results);

        verifyMocks();
    }

    @Test
    public void testGetNonPrivatePortalPagesNone()
    {
        final List<PortalPage> portalPages = Arrays.asList(portalPage1, portalPage2);

        expect(portalPageManager.getAllOwnedPortalPages(user)).andReturn(portalPages);

        final PortalPageService service = createPortalPageService();

        final Collection results = service.getNonPrivatePortalPages(user);

        assertEquals(Collections.EMPTY_LIST, results);

        verifyMocks();
    }

    @Test
    public void testGetNonPrivatePortalPagesAllBut1()
    {

        final Set<SharePermission> permissions = Collections.<SharePermission>singleton(new SharePermissionImpl(GlobalShareType.TYPE, null, null));

        portalPage3 = PortalPage.portalPage(portalPage3).permissions(new SharedEntity.SharePermissions(permissions)).build();

        expect(portalPageManager.getAllOwnedPortalPages(user)).andReturn(Arrays.asList(portalPage1, portalPage2, portalPage3));

        final PortalPageService service = createPortalPageService();

        final Collection results = service.getNonPrivatePortalPages(user);

        assertEquals(EasyList.build(portalPage3), results);

        verifyMocks();
    }

    @Test
    public void testGetNonPrivatePortalPagesNullUser()
    {
        final PortalPageService service = createPortalPageService();

        final Collection results = service.getNonPrivatePortalPages((ApplicationUser) null);

        assertEquals(Collections.EMPTY_LIST, results);

        verifyMocks();

    }

    @Test
    public void testGetPortalPagesFavouritedByOthersEmpty()
    {
        expect(portalPageManager.getAllOwnedPortalPages(user)).andReturn(Collections.<PortalPage>emptyList());

        final PortalPageService service = createPortalPageService();
        final Collection result = service.getPortalPagesFavouritedByOthers(user);

        assertNotNull(result);
        assertEquals(0, result.size());

        verifyMocks();

    }

    @Test
    public void testGetPortalPagesFavouritedByOthers()
    {
        portalPage1 = PortalPage.id(1L).name("one").description("one description").owner(user).favouriteCount(3L).build();
        portalPage2 = PortalPage.id(2L).name("two").description("two description").owner(user).favouriteCount(3L).build();
        portalPage3 = PortalPage.id(3L).name("three").description("three description").owner(user).favouriteCount(3L).build();

        final SharedEntity.SharePermissions permissions = new SharedEntity.SharePermissions(Collections.singleton(new SharePermissionImpl(GlobalShareType.TYPE, null, null)));

        portalPage1 = PortalPage.portalPage(portalPage1).permissions(permissions).build();
        portalPage2 = PortalPage.portalPage(portalPage2).permissions(permissions).build();
        portalPage3 = PortalPage.portalPage(portalPage3).permissions(permissions).build();

        expect(portalPageManager.getAllOwnedPortalPages(user)).andReturn(Arrays.asList(portalPage2, portalPage3, portalPage1));

        expect(favouritesManager.getFavouriteIds(user, PortalPage.ENTITY_TYPE)).andReturn(Arrays.asList(1L, 2L, 3L));

        final PortalPageService service = createPortalPageService();
        final Collection<PortalPage> result = service.getPortalPagesFavouritedByOthers(user);

        assertThesePagesFound(Arrays.asList(portalPage1, portalPage2, portalPage3), result);

        verifyMocks();

    }

    @Test
    public void testGetPortalPagesFavouritedByOthersPortalPaged()
    {
        portalPage1 = PortalPage.id(1L).name("one").description("one description").owner(user).favouriteCount(3L).build();
        portalPage2 = PortalPage.id(2L).name("two").description("two description").owner(user).favouriteCount(2L).build();
        portalPage3 = PortalPage.id(3L).name("three").description("three description").owner(user).favouriteCount(0L).build();

        final Set<SharePermission> permissions = Collections.<SharePermission>singleton(new SharePermissionImpl(GlobalShareType.TYPE, null, null));

        portalPage2 = PortalPage.portalPage(portalPage2).permissions(new SharedEntity.SharePermissions(permissions)).build();

        expect(portalPageManager.getAllOwnedPortalPages(user)).andReturn(Arrays.asList(portalPage1, portalPage2, portalPage3));

        expect(favouritesManager.getFavouriteIds(user, PortalPage.ENTITY_TYPE)).andReturn(Collections.singletonList(1L));

        final PortalPageService service = createPortalPageService();
        final Collection<PortalPage> result = service.getPortalPagesFavouritedByOthers(user);

        assertThesePagesFound(Collections.singletonList(portalPage2), result);

        verifyMocks();
    }

    @Test
    public void testGetPortalPagesFavouritedByOthersMorePortalPaged()
    {
        portalPage1 = PortalPage.id(1L).name("one").description("one description").owner(user).favouriteCount(2L).build();
        portalPage2 = PortalPage.id(2L).name("two").description("two description").owner(user).favouriteCount(1L).build();
        portalPage3 = PortalPage.id(3L).name("three").description("three description").owner(user).favouriteCount(3L).build();

        final HashSet<SharePermission> permissions = new HashSet<SharePermission>();
        permissions.add(new SharePermissionImpl(GlobalShareType.TYPE, null, null));

        portalPage1 = PortalPage.portalPage(portalPage1).permissions(new SharedEntity.SharePermissions(permissions)).build();
        portalPage3 = PortalPage.portalPage(portalPage3).permissions(new SharedEntity.SharePermissions(permissions)).build();

        final List<PortalPage> portalPages = Arrays.asList(portalPage2, portalPage1, portalPage3);
        final List<PortalPage> compareList = Arrays.asList(portalPage1, portalPage3);

        expect(portalPageManager.getAllOwnedPortalPages(user)).andReturn(portalPages);
        expect(favouritesManager.getFavouriteIds(user, PortalPage.ENTITY_TYPE)).andReturn(Arrays.asList(1L, 2L));

        final PortalPageService service = createPortalPageService();
        final Collection<PortalPage> result = service.getPortalPagesFavouritedByOthers(user);

        assertThesePagesFound(compareList, result);

        verifyMocks();
    }

    @Test
    public void testValidatePortalPageForUpdateWithNullCtx()
    {
        final PortalPageService service = createPortalPageService();
        try
        {
            service.validateForUpdate(null, portalPage1);
            fail("Should not accept null ctx.");
        }
        catch (final IllegalArgumentException e)
        {
            // expected.
        }
    }

    @Test
    public void testValidatePortalPageForUpdateWithNullData()
    {
        final JiraServiceContext ctx = new JiraServiceContextImpl(user);
        final PortalPageService service = createPortalPageService();
        try
        {
            service.validateForUpdate(ctx, null);
            fail("Should not accept null portalPage.");
        }
        catch (final IllegalArgumentException e)
        {
            // expected.
        }
    }

    @Test
    public void testValidatePortalPageForUpdateWithNullUser()
    {
        final JiraServiceContext ctx = createContext((User) null);

        portalPage1 = PortalPage.portalPage(portalPage1).permissions(SharedEntity.SharePermissions.PRIVATE).build();
        expect(shareTypeValidatorUtils.isValidSharePermission(ctx, portalPage1)).andReturn(true);

        final PortalPageService service = createPortalPageService();
        service.validateForUpdate(ctx, portalPage1);

        assertTrue(ctx.getErrorCollection().hasAnyErrors());
        assertTrue(ctx.getErrorCollection().getErrorMessages().contains("admin.errors.portalpages.owned.anonymous.user"));
    }

    @Test
    public void testValidatePortalPageForUpdateWithEmptyName()
    {
        final JiraServiceContext ctx = createContext();
        final Set<SharePermission> permissions = Collections.<SharePermission>singleton(new SharePermissionImpl(new ShareType.Name("exampleType"), null, null));

        portalPage1 = PortalPage.portalPage(portalPage1).name("").permissions(new SharedEntity.SharePermissions(permissions)).build();

        final PortalPageService service = createPortalPageService();
        service.validateForUpdate(ctx, portalPage1);

        assertTrue(ctx.getErrorCollection().hasAnyErrors());
        final Map map = ctx.getErrorCollection().getErrors();
        assertTrue(map.containsKey("portalPageName"));
        assertEquals(map.get("portalPageName"), "admin.errors.portalpages.must.specify.name");

        verifyMocks();
    }

    @Test
    public void testValidatePortalPageForUpdateNotInDatabase()
    {
        final JiraServiceContext ctx = createContext();

        portalPage1 = PortalPage.portalPage(portalPage1).permissions(SharedEntity.SharePermissions.PRIVATE).build();
        expect(shareTypeValidatorUtils.isValidSharePermission(ctx, portalPage1)).andReturn(true);

        expect(portalPageManager.getPortalPageById(portalPage1.getId())).andReturn(null);


        final PortalPageService service = createPortalPageService();
        service.validateForUpdate(ctx, portalPage1);

        assertTrue(ctx.getErrorCollection().hasAnyErrors());
        assertTrue(ctx.getErrorCollection().getErrorMessages().contains("admin.errors.portalpages.not.saved"));

        verifyMocks();
    }

    @Test
    public void testValidatePortalPageForUpdateNotOwner()
    {
        final ApplicationUser owner = new MockApplicationUser("user");
        portalPage1 = PortalPage.id(1L).name("one").description("one description").owner(owner).build();

        final JiraServiceContext ctx = createContext();

        portalPage1 = PortalPage.portalPage(portalPage1).permissions(SharedEntity.SharePermissions.PRIVATE).build();
        expect(shareTypeValidatorUtils.isValidSharePermission(ctx, portalPage1)).andReturn(true);

        final PortalPageService service = createPortalPageService();
        service.validateForUpdate(ctx, portalPage1);

        assertTrue(ctx.getErrorCollection().hasAnyErrors());
        assertTrue(ctx.getErrorCollection().getErrorMessages().contains("admin.errors.portalpages.must.be.owner"));

        verifyMocks();
    }

    @Test
    public void testValidatePortalPageForUpdateWithValidPortalPage()
    {
        final JiraServiceContext ctx = new JiraServiceContextImpl(user);

        expect(portalPageManager.getPortalPageById(portalPage1.getId())).andReturn(portalPage1);

        expect(portalPageManager.getPortalPageByName(user, portalPage1.getName())).andReturn(null);

        portalPage1 = PortalPage.portalPage(portalPage1).permissions(SharedEntity.SharePermissions.PRIVATE).build();
        expect(shareTypeValidatorUtils.isValidSharePermission(ctx, portalPage1)).andReturn(true);

        final PortalPageService service = createPortalPageService();
        service.validateForUpdate(ctx, portalPage1);

        assertFalse(ctx.getErrorCollection().hasAnyErrors());

        verifyMocks();
    }

    @Test
    public void testValidatePortalPageForUpdateWithBadNameAndSaved()
    {
        final JiraServiceContext ctx = createContext();

        PortalPage newRequest = PortalPage.id(1L).name("testValidatePortalPageWithBadNameAndSaved").owner(user).build();
        final PortalPage oldRequest = PortalPage.id(2L).name("crapName").owner(user).build();

        expect(portalPageManager.getPortalPageById(newRequest.getId())).andReturn(newRequest);

        expect(portalPageManager.getPortalPageByName(user, newRequest.getName())).andReturn(oldRequest);

        newRequest = PortalPage.portalPage(newRequest).permissions(SharedEntity.SharePermissions.PRIVATE).build();
        expect(shareTypeValidatorUtils.isValidSharePermission(ctx, newRequest)).andReturn(true);


        final PortalPageService service = createPortalPageService();
        service.validateForUpdate(ctx, newRequest);

        assertTrue(ctx.getErrorCollection().hasAnyErrors());
        final Map map = ctx.getErrorCollection().getErrors();
        assertTrue(map.containsKey("portalPageName"));
        assertEquals(map.get("portalPageName"), ctx.getI18nBean().getText("admin.errors.portalpages.same.name"));

        verifyMocks();
    }

    @Test
    public void testValidatePortalPageForUpdateUpdateName()
    {
        final JiraServiceContext ctx = createContext();

        final PortalPage oldRequest = PortalPage.id(1L).name("crapName").owner(user).build();

        expect(portalPageManager.getPortalPageById(portalPage1.getId())).andReturn(portalPage1);

        expect(portalPageManager.getPortalPageByName(user, portalPage1.getName())).andReturn(oldRequest);

        portalPage1 = PortalPage.portalPage(portalPage1).permissions(SharedEntity.SharePermissions.PRIVATE).build();
        expect(shareTypeValidatorUtils.isValidSharePermission(ctx, portalPage1)).andReturn(true);


        final PortalPageService service = createPortalPageService();
        service.validateForUpdate(ctx, portalPage1);

        assertFalse(ctx.getErrorCollection().hasAnyErrors());

        verifyMocks();
    }

    @Test
    public void testValidateForUpdateSystemDefaultPageNoPermission()
    {
        final JiraServiceContext ctx = createContext();

        expect(permissionManager.hasPermission(Permissions.ADMINISTER, user)).andReturn(false);

        final PortalPageService service = createPortalPageService();
        final boolean ok = service.validateForUpdate(ctx, portalPageSystemDefault);
        assertFalse(ok);
        assertTrue(ctx.getErrorCollection().hasAnyErrors());
        assertTrue(ctx.getErrorCollection().getErrorMessages().contains("admin.errors.portalpages.must.be.admin.change.sysdefault"));

        verifyMocks();
    }

    @Test
    public void testValidateForUpdateSystemDefaultPageAllOK()
    {
        final JiraServiceContext ctx = createContext();

        portalPageSystemDefault = PortalPage.portalPage(portalPageSystemDefault).permissions(SharedEntity.SharePermissions.GLOBAL).build();

        expect(permissionManager.hasPermission(Permissions.ADMINISTER, user)).andReturn(true);

        final PortalPageService service = createPortalPageService();
        final boolean ok = service.validateForUpdate(ctx, portalPageSystemDefault);
        assertTrue(ok);
        assertFalse(ctx.getErrorCollection().hasAnyErrors());

        verifyMocks();
    }

    @Test
    public void testValidatePortalPageForCreateWithNullCtx()
    {
        final PortalPageService service = createPortalPageService();
        try
        {
            service.validateForCreate(null, portalPage1);
            fail("Should not accept null ctx.");
        }
        catch (final IllegalArgumentException e)
        {
            // expected.
        }
    }

    @Test
    public void testValidatePortalPageForCreateWithNullRequest()
    {
        final JiraServiceContext ctx = new JiraServiceContextImpl(user);
        final PortalPageService service = createPortalPageService();
        try
        {
            service.validateForCreate(ctx, null);
            fail("Should not accept null portalPage.");
        }
        catch (final IllegalArgumentException e)
        {
            // expected.
        }
    }

    @Test
    public void testValidatePortalPageForCreateWithNullUser()
    {
        final JiraServiceContext ctx = createContext((User) null);

        portalPage1 = PortalPage.portalPage(portalPage1).permissions(SharedEntity.SharePermissions.PRIVATE).build();
        expect(shareTypeValidatorUtils.isValidSharePermission(ctx, portalPage1)).andReturn(true);

        final PortalPageService service = createPortalPageService();
        service.validateForCreate(ctx, portalPage1);

        assertTrue(ctx.getErrorCollection().hasAnyErrors());
        assertTrue(ctx.getErrorCollection().getErrorMessages().contains("admin.errors.portalpages.owned.anonymous.user"));
    }

    @Test
    public void testValidateForDeleteWithNullCtx()
    {
        final PortalPageService service = createPortalPageService();
        try
        {
            service.validateForDelete(null, portalPage1.getId());
            fail("Should not accept null context.");
        }
        catch (final IllegalArgumentException e)
        {
            // expected.
        }
    }

    @Test
    public void testValidateForDeleteWithNullId()
    {
        final PortalPageService service = createPortalPageService();
        try
        {
            service.validateForDelete(createContext(), null);
            fail("Should not accept null id.");
        }
        catch (final IllegalArgumentException e)
        {
            // expected.
        }
    }

    @Test
    public void testValidateForDeleteWithAnonymousUser()
    {
        final PortalPageService service = createPortalPageService();
        final JiraServiceContext serviceContext = createContext((User) null);

        service.validateForDelete(serviceContext, portalPage1.getId());

        final ErrorCollection errorCollection = serviceContext.getErrorCollection();
        assertTrue(errorCollection.hasAnyErrors());
        assertTrue(errorCollection.getErrorMessages().contains("admin.errors.portalpages.owned.anonymous.user"));
    }

    @Test
    public void testValidateForDeleteNonExistingPortalPage()
    {
        expect(portalPageManager.getPortalPageById(portalPage1.getId())).andReturn(null);

        final PortalPageService service = createPortalPageService();
        final JiraServiceContext serviceContext = createContext();

        service.validateForDelete(serviceContext, portalPage1.getId());

        final ErrorCollection errorCollection = serviceContext.getErrorCollection();
        assertTrue(errorCollection.hasAnyErrors());
        assertTrue(errorCollection.getErrorMessages().contains("admin.errors.portalpages.nonexistent"));

        verifyMocks();
    }

    @Test
    public void testValidateForDeleteCantDeleteSystemDefaultPage()
    {
        expect(portalPageManager.getPortalPageById(portalPageSystemDefault.getId())).andReturn(portalPageSystemDefault);

        final PortalPageService service = createPortalPageService();
        final JiraServiceContext serviceContext = createContext();

        service.validateForDelete(serviceContext, portalPageSystemDefault.getId());

        final ErrorCollection errorCollection = serviceContext.getErrorCollection();
        assertTrue(errorCollection.hasAnyErrors());
        assertTrue(errorCollection.getErrorMessages().contains("admin.errors.portalpages.not.delete.system.default"));

        verifyMocks();
    }

    @Test
    public void testValidateForDeleteNonOwned()
    {
        final ApplicationUser otherUser = new MockApplicationUser("otherUser");
        final PortalPage otherPage = PortalPage.name("name").description("desc").owner(otherUser).build();

        expect(portalPageManager.getPortalPageById(portalPage1.getId())).andReturn(otherPage);

        final PortalPageService service = createPortalPageService();
        final JiraServiceContext serviceContext = createContext();

        service.validateForDelete(serviceContext, portalPage1.getId());

        final ErrorCollection errorCollection = serviceContext.getErrorCollection();
        assertTrue(errorCollection.hasAnyErrors());
        assertTrue(errorCollection.getErrorMessages().contains("admin.errors.portalpages.must.be.owner"));

        verifyMocks();
    }

    @Test
    public void testValidateForDelete()
    {
        expect(portalPageManager.getPortalPageById(portalPage1.getId())).andReturn(portalPage1);

        final PortalPageService service = createPortalPageService();
        final JiraServiceContext serviceContext = createContext();

        service.validateForDelete(serviceContext, portalPage1.getId());

        final ErrorCollection errorCollection = serviceContext.getErrorCollection();
        assertFalse(errorCollection.hasAnyErrors());

        verifyMocks();
    }

    @Test
    public void testCreatePortalPageNullCtx()
    {
        final PortalPageService service = createPortalPageService();
        try
        {
            service.createPortalPage(null, portalPage1);
            fail("Expecting an exception.");
        }
        catch (final IllegalArgumentException e)
        {
            // expected.
        }

        verifyMocks();
    }

    @Test
    public void testCreatePortalPageNullPortalPage()
    {
        final PortalPageService service = createPortalPageService();
        try
        {
            service.createPortalPage(createContext(), null);
            fail("Expecting an exception.");
        }
        catch (final IllegalArgumentException e)
        {
            // expected.
        }

        verifyMocks();
    }

    @Test
    public void testCreatePortalPageAsAnonymous()
    {
        final JiraServiceContext context = createContext((User) null);

        expect(shareTypeValidatorUtils.isValidSharePermission(context, portalPage1)).andReturn(true);

        final PortalPageService service = createPortalPageService();

        service.createPortalPage(context, portalPage1);

        final ErrorCollection errorCollection = context.getErrorCollection();
        assertTrue(errorCollection.hasAnyErrors());
        assertTrue(errorCollection.getErrorMessages().contains("admin.errors.portalpages.owned.anonymous.user"));
    }

    @Test
    public void testCreatePortalPageFavouriteWithNullCtx()
    {
        final PortalPageService service = createPortalPageService();
        try
        {
            service.createPortalPage(null, portalPage1, true);
            fail("Expecting an exception.");
        }
        catch (final IllegalArgumentException e)
        {
            // expected.
        }

        verifyMocks();
    }

    @Test
    public void testCreatePortalPageFavouriteWithNullPortalPage()
    {
        final PortalPageService service = createPortalPageService();
        try
        {
            service.createPortalPage(createContext(), null, true);
            fail("Expecting an exception.");
        }
        catch (final IllegalArgumentException e)
        {
            // expected.
        }

        verifyMocks();
    }

    @Test
    public void testCreatePortalPageFavouriteAsAnonymous()
    {
        final JiraServiceContext context = createContext((User) null);

        expect(shareTypeValidatorUtils.isValidSharePermission(context, portalPage1)).andReturn(true);

        final PortalPageService service = createPortalPageService();

        service.createPortalPage(context, portalPage1, true);

        final ErrorCollection errorCollection = context.getErrorCollection();
        assertTrue(errorCollection.hasAnyErrors());
        assertTrue(errorCollection.getErrorMessages().contains("admin.errors.portalpages.owned.anonymous.user"));
    }

    /*
    * ===================================================================================== CREATE CLONE
    * =====================================================================================
    */

    @Test
    public void testCreateClonePortalPageNullCtx()
    {
        final PortalPageService service = createPortalPageService();
        try
        {
            service.createPortalPageByClone(null, portalPage1, CLONE_ID, false);
            fail("Expecting an exception.");
        }
        catch (final IllegalArgumentException e)
        {
            // expected.
        }

        verifyMocks();
    }

    @Test
    public void testCreateClonePortalPageNullPortalPage()
    {
        final PortalPageService service = createPortalPageService();
        try
        {
            service.createPortalPageByClone(createContext(), null, CLONE_ID, false);
            fail("Expecting an exception.");
        }
        catch (final IllegalArgumentException e)
        {
            // expected.
        }

        verifyMocks();
    }

    @Test
    public void testCreateClonePortalPageNullCloneId()
    {
        final PortalPageService service = createPortalPageService();
        try
        {
            service.createPortalPageByClone(createContext(), portalPage1, null, false);
            fail("Expecting an exception.");
        }
        catch (final IllegalArgumentException e)
        {
            // expected.
        }

        verifyMocks();
    }

    @Test
    public void testCreateClonePortalPageAsAnonymous()
    {
        final JiraServiceContext context = createContext((User) null);

        expect(shareTypeValidatorUtils.isValidSharePermission(context, portalPage1)).andReturn(true);

        final PortalPageService service = createPortalPageService();

        service.createPortalPageByClone(context, portalPage1, CLONE_ID, false);

        final ErrorCollection errorCollection = context.getErrorCollection();
        assertTrue(errorCollection.hasAnyErrors());
        assertTrue(errorCollection.getErrorMessages().contains("admin.errors.portalpages.owned.anonymous.user"));
    }

    @Test
    public void testCreateClonePortalPageFavouriteWithNullCtx()
    {
        final PortalPageService service = createPortalPageService();
        try
        {
            service.createPortalPageByClone(null, portalPage1, CLONE_ID, true);
            fail("Expecting an exception.");
        }
        catch (final IllegalArgumentException e)
        {
            // expected.
        }

        verifyMocks();
    }

    @Test
    public void testCreateClonePortalPageFavouriteWithNullPortalPage()
    {
        final PortalPageService service = createPortalPageService();
        try
        {
            service.createPortalPageByClone(createContext(), null, CLONE_ID, true);
            fail("Expecting an exception.");
        }
        catch (final IllegalArgumentException e)
        {
            // expected.
        }

        verifyMocks();
    }

    @Test
    public void testCreateClonePortalPageFavouriteAsAnonymous()
    {
        final JiraServiceContext context = createContext((User) null);

        expect(shareTypeValidatorUtils.isValidSharePermission(context, portalPage1)).andReturn(true);

        final PortalPageService service = createPortalPageService();

        service.createPortalPageByClone(context, portalPage1, CLONE_ID, true);

        final ErrorCollection errorCollection = context.getErrorCollection();
        assertTrue(errorCollection.hasAnyErrors());
        assertTrue(errorCollection.getErrorMessages().contains("admin.errors.portalpages.owned.anonymous.user"));
    }


    @Test
    public void testUpdatePortalPage()
    {
        final JiraServiceContext ctx = createContext();

        expect(portalPageManager.getPortalPageById(portalPage1.getId())).andReturn(portalPage1);
        expect(portalPageManager.getPortalPageByName(user, portalPage1.getName())).andReturn(null);
        expect(portalPageManager.update(portalPage1)).andReturn(portalPage2);
        expect(shareTypeValidatorUtils.isValidSharePermission(ctx, portalPage1)).andReturn(true);

        favouritesManager.removeFavourite(user, portalPage2);

        final PortalPageService service = createPortalPageService();

        final PortalPage result = service.updatePortalPage(ctx, portalPage1, false);
        assertFalse(ctx.getErrorCollection().hasAnyErrors());
        assertSame(portalPage2, result);

        verifyMocks();
    }

    @Test
    public void testUpdateSystemDefaultPortalPage() throws Exception
    {
        final JiraServiceContext ctx = createContext();

        portalPageSystemDefault = PortalPage.portalPage(portalPageSystemDefault).permissions(SharedEntity.SharePermissions.GLOBAL).build();

        expect(permissionManager.hasPermission(Permissions.ADMINISTER, user)).andReturn(true);
        expect(portalPageManager.update(portalPageSystemDefault)).andReturn(portalPage2);
        favouritesManager.removeFavourite(user, portalPage2);

        final PortalPageService service = createPortalPageService();

        final PortalPage result = service.updatePortalPage(ctx, portalPageSystemDefault, false);
        assertFalse(ctx.getErrorCollection().hasAnyErrors());
        assertSame(result, portalPage2);

        verifyMocks();

    }

    @Test
    public void testUpdatePortalPageNullCtx()
    {
        final PortalPageService service = createPortalPageService();
        try
        {
            service.updatePortalPage(null, portalPage1, false);
            fail("Expecting an exception.");
        }
        catch (final IllegalArgumentException e)
        {
            // expected.
        }

        verifyMocks();
    }

    @Test
    public void testUpdatePortalPageNullPortalPage()
    {
        final PortalPageService service = createPortalPageService();
        try
        {
            service.updatePortalPage(createContext(), null, false);
            fail("Expecting an exception.");
        }
        catch (final IllegalArgumentException e)
        {
            // expected.
        }

        verifyMocks();
    }

    @Test
    public void testUpdatePortalPageAsAnonymous()
    {
        final JiraServiceContext context = createContext((User) null);

        expect(shareTypeValidatorUtils.isValidSharePermission(context, portalPage1)).andReturn(true);

        final PortalPageService service = createPortalPageService();

        service.updatePortalPage(context, portalPage1, false);

        final ErrorCollection errorCollection = context.getErrorCollection();
        assertTrue(errorCollection.hasAnyErrors());
        assertTrue(errorCollection.getErrorMessages().contains("admin.errors.portalpages.owned.anonymous.user"));
    }

    @Test
    public void testUpdatePortalPageNotNewOwner()
    {
        final User notMe = new MockUser("notMe");
        final JiraServiceContext context = createContext(notMe);

        // portalPageManager.getPortalPageByName(user, portalPage1.getName())).andReturn(null);

        expect(shareTypeValidatorUtils.isValidSharePermission(context, portalPage1)).andReturn(true);

        final PortalPageService service = createPortalPageService();

        service.updatePortalPage(context, portalPage1, false);

        final ErrorCollection errorCollection = context.getErrorCollection();
        assertTrue(errorCollection.hasAnyErrors());
        assertTrue(errorCollection.getErrorMessages().contains("admin.errors.portalpages.must.be.owner"));
    }

    @Test
    public void testUpdatePortalPageNotInDb()
    {
        final JiraServiceContext context = createContext();

        expect(portalPageManager.getPortalPageById(portalPage1.getId())).andReturn(null);

        expect(shareTypeValidatorUtils.isValidSharePermission(context, portalPage1)).andReturn(true);


        final PortalPageService service = createPortalPageService();

        service.updatePortalPage(context, portalPage1, false);

        final ErrorCollection errorCollection = context.getErrorCollection();
        assertTrue(errorCollection.hasAnyErrors());
        assertTrue(errorCollection.getErrorMessages().contains("admin.errors.portalpages.not.saved"));
    }

    @Test
    public void testUpdatePortalPageNotOwnerInDb()
    {
        final JiraServiceContext context = createContext();

        expect(portalPageManager.getPortalPageById(portalPage1.getId())).andReturn(portalPageNotOwnedByUser);

        expect(shareTypeValidatorUtils.isValidSharePermission(context, portalPage1)).andReturn(true);

        final PortalPageService service = createPortalPageService();

        service.updatePortalPage(context, portalPage1, false);

        final ErrorCollection errorCollection = context.getErrorCollection();
        assertTrue(errorCollection.hasAnyErrors());
        assertTrue(errorCollection.getErrorMessages().contains("admin.errors.portalpages.must.be.owner"));
    }

    @Test
    public void testUpdatePortalPageFavouriteWithNullCtx()
    {
        final PortalPageService service = createPortalPageService();
        try
        {
            service.updatePortalPage(null, portalPage1, true);
            fail("Expecting an exception.");
        }
        catch (final IllegalArgumentException e)
        {
            // expected.
        }

        verifyMocks();
    }

    @Test
    public void testUpdatePortalPageFavouriteWithNullPortalPage()
    {
        final PortalPageService service = createPortalPageService();
        try
        {
            service.updatePortalPage(createContext(), null, true);
            fail("Expecting an exception.");
        }
        catch (final IllegalArgumentException e)
        {
            // expected.
        }

        verifyMocks();
    }

    @Test
    public void testUpdatePortalPageFavouriteNotExisting()
    {
        final JiraServiceContext context = createContext();

        expect(shareTypeValidatorUtils.isValidSharePermission(context, portalPage2)).andReturn(true);

        expect(portalPageManager.getPortalPageById(portalPage2.getId())).andReturn(null);

        final PortalPageService service = createPortalPageService();

        service.updatePortalPage(context, portalPage2, true);

        final ErrorCollection errorCollection = context.getErrorCollection();
        assertTrue(errorCollection.hasAnyErrors());
        assertTrue(errorCollection.getErrorMessages().contains("admin.errors.portalpages.not.saved"));
    }

    @Test
    public void testUpdatePortalPageFavouriteAsAnonymous()
    {
        final JiraServiceContext context = createContext((User) null);

        expect(shareTypeValidatorUtils.isValidSharePermission(context, portalPage1)).andReturn(true);

        final PortalPageService service = createPortalPageService();

        service.updatePortalPage(context, portalPage1, true);

        final ErrorCollection errorCollection = context.getErrorCollection();
        assertTrue(errorCollection.hasAnyErrors());
        assertTrue(errorCollection.getErrorMessages().contains("admin.errors.portalpages.owned.anonymous.user"));
    }

    @Test
    public void testUpdatePortalPageFavouriteNotNewOwner()
    {
        MockUser notMe = new MockUser("notMe");
        final JiraServiceContext context = createContext(notMe);

        expect(shareTypeValidatorUtils.isValidSharePermission(context, portalPage1)).andReturn(true);

        final PortalPageService service = createPortalPageService();

        service.updatePortalPage(context, portalPage1, true);

        final ErrorCollection errorCollection = context.getErrorCollection();
        assertTrue(errorCollection.hasAnyErrors());
        assertTrue(errorCollection.getErrorMessages().contains("admin.errors.portalpages.must.be.owner"));
    }

    @Test
    public void testUpdatePortalPageFavouriteNotInDb()
    {
        final JiraServiceContext context = createContext();

        expect(portalPageManager.getPortalPageById(portalPage1.getId())).andReturn(null);

        expect(shareTypeValidatorUtils.isValidSharePermission(context, portalPage1)).andReturn(true);

        final PortalPageService service = createPortalPageService();

        service.updatePortalPage(context, portalPage1, false);

        final ErrorCollection errorCollection = context.getErrorCollection();
        assertTrue(errorCollection.hasAnyErrors());
        assertTrue(errorCollection.getErrorMessages().contains("admin.errors.portalpages.not.saved"));
    }

    @Test
    public void testUpdatePortalPageFavouriteWithAddFavourite() throws PermissionException
    {
        final JiraServiceContext ctx = createContext();

        expect(portalPageManager.getPortalPageById(portalPage1.getId())).andReturn(portalPage1);

        expect(portalPageManager.getPortalPageByName(user, portalPage1.getName())).andReturn(null);

        expect(portalPageManager.update(portalPage1)).andReturn(portalPage1);

        expect(shareTypeValidatorUtils.isValidSharePermission(ctx, portalPage1)).andReturn(true);

        favouritesManager.addFavourite(user, portalPage1);

        final PortalPageService service = createPortalPageService();
        final PortalPage resultPortalPage = service.updatePortalPage(ctx, portalPage1, true);
        assertSame(portalPage1, resultPortalPage);
        assertFalse(ctx.getErrorCollection().hasAnyErrors());

        verifyMocks();
    }

    @Test
    public void testUpdatePortalPageFavouriteWithAddFavouriteError() throws PermissionException
    {
        final JiraServiceContext ctx = createContext();

        expect(portalPageManager.getPortalPageById(portalPage1.getId())).andReturn(portalPage1);

        expect(portalPageManager.getPortalPageByName(user, portalPage1.getName())).andReturn(null);

        expect(portalPageManager.update(portalPage1)).andReturn(portalPage1);

        expect(shareTypeValidatorUtils.isValidSharePermission(ctx, portalPage1)).andReturn(true);

        favouritesManager.addFavourite(user, portalPage1);
        expectLastCall().andThrow(new PermissionException("ahhhh"));

        final PortalPageService service = createPortalPageService();
        final PortalPage resultPortalPage = service.updatePortalPage(ctx, portalPage1, true);
        assertSame(portalPage1, resultPortalPage);
        assertTrue(ctx.getErrorCollection().hasAnyErrors());

        verifyMocks();
    }

    @Test
    public void testUpdatePortalPageFavouriteWithRemoveFavourite() throws PermissionException
    {
        final JiraServiceContext ctx = createContext();

        expect(portalPageManager.getPortalPageById(portalPage1.getId())).andReturn(portalPage1);

        expect(portalPageManager.getPortalPageByName(user, portalPage1.getName())).andReturn(null);

        expect(portalPageManager.update(portalPage1)).andReturn(portalPage1);

        expect(shareTypeValidatorUtils.isValidSharePermission(ctx, portalPage1)).andReturn(true);


        favouritesManager.removeFavourite(user, portalPage1);

        final PortalPageService service = createPortalPageService();
        final PortalPage resultPortalPage = service.updatePortalPage(ctx, portalPage1, false);
        assertSame(portalPage1, resultPortalPage);
        assertFalse(ctx.getErrorCollection().hasAnyErrors());

        verifyMocks();
    }

    @Test
    public void testGetSystemDefaultPage() throws Exception
    {
        expect(portalPageManager.getSystemDefaultPortalPage()).andReturn(portalPage1);

        final PortalPageService service = createPortalPageService();
        final PortalPage systemDefault = service.getSystemDefaultPortalPage();
        assertEquals(portalPage1, systemDefault);

        verifyMocks();
    }

    @Test
    public void testValidateForChangePortalPageSequenceAnonymous()
    {
        final JiraServiceContext ctx = createContext((User) null);

        final PortalPageService pps = createPortalPageService();

        final boolean actualResult = pps.validateForChangePortalPageSequence(ctx, portalPage1.getId());
        assertFalse(actualResult);
        assertTrue(ctx.getErrorCollection().hasAnyErrors());
        assertTrue(ctx.getErrorCollection().getErrorMessages().contains("admin.errors.portalpages.owned.anonymous.user"));

        verifyMocks();
    }

    @Test
    public void testValidateForChangePortalPageSequencePageNotExist()
    {
        final JiraServiceContext ctx = createContext();

        expect(portalPageManager.getPortalPage(user, portalPage2.getId())).andReturn(null);

        final PortalPageService pps = createPortalPageService();
        final boolean actualResult = pps.validateForChangePortalPageSequence(ctx, portalPage2.getId());

        assertFalse(actualResult);
        assertTrue(ctx.getErrorCollection().hasAnyErrors());
        assertTrue(ctx.getErrorCollection().getErrorMessages().contains("admin.errors.portalpages.no.access"));

        verifyMocks();
    }

    @Test
    public void testValidateForChangePortalPageSequenceNotFavourite() throws PermissionException
    {
        final JiraServiceContext ctx = createContext(notTheOwner);

        expect(portalPageManager.getPortalPage(notTheOwner, portalPage2.getId())).andReturn(portalPage2);

        expect(favouritesManager.isFavourite(notTheOwner, portalPage2)).andReturn(false);

        final PortalPageService pps = createPortalPageService();
        final boolean actualResult = pps.validateForChangePortalPageSequence(ctx, portalPage2.getId());

        assertFalse(actualResult);
        assertTrue(ctx.getErrorCollection().hasAnyErrors());
        assertTrue(ctx.getErrorCollection().getErrorMessages().contains("admin.errors.portalpages.not.favourite"));

        verifyMocks();
    }

    @Test
    public void testValidateForChangePortalPageSequenceFavouriteError() throws PermissionException
    {
        final JiraServiceContext ctx = createContext(notTheOwner);

        expect(portalPageManager.getPortalPage(notTheOwner, portalPage2.getId())).andReturn(portalPage2);

        expect(favouritesManager.isFavourite(notTheOwner, portalPage2)).andThrow(new PermissionException("some exception"));

        final PortalPageService pps = createPortalPageService();
        final boolean actualResult = pps.validateForChangePortalPageSequence(ctx, portalPage2.getId());

        assertFalse(actualResult);
        assertTrue(ctx.getErrorCollection().hasAnyErrors());
        assertTrue(ctx.getErrorCollection().getErrorMessages().contains("admin.errors.portalpages.not.favourite"));

        verifyMocks();
    }

    @Test
    public void testValidateForChangePortalPageSequenceGood() throws PermissionException
    {
        final JiraServiceContext ctx = createContext(notTheOwner);

        expect(portalPageManager.getPortalPage(notTheOwner, portalPage2.getId())).andReturn(portalPage2);

        expect(favouritesManager.isFavourite(notTheOwner, portalPage2)).andReturn(true);

        final PortalPageService pps = createPortalPageService();
        final boolean actualResult = pps.validateForChangePortalPageSequence(ctx, portalPage2.getId());

        assertTrue(actualResult);
        assertFalse(ctx.getErrorCollection().hasAnyErrors());

        verifyMocks();
    }//

    // INCREASE
    //
    @Test
    public void testIncreaseSequenceExisting() throws Exception
    {
        _testSequenceNotExisting(new IncreaseClosure());
    }

    @Test
    public void testIncreaseSequenceAsAnonymous() throws Exception
    {
        _testSequenceAsAnonymous(new IncreaseClosure());
    }

    @Test
    public void testIncreaseSequenceNotFavourite() throws Exception
    {
        _testSequenceNotFavourite(new IncreaseClosure());
    }

    @Test
    public void testIncreaseSequenceOK() throws Exception
    {
        _testSequenceOK(new OkIncreaseClosure());
    }//

    // DECREASE
    //
    @Test
    public void testDecreaseSequenceExisting() throws Exception
    {
        _testSequenceNotExisting(new DecreaseClosure());
    }

    @Test
    public void testDecreaseSequenceAsAnonymous() throws Exception
    {
        _testSequenceAsAnonymous(new DecreaseClosure());
    }

    @Test
    public void testDecreaseSequenceNotFavourite() throws Exception
    {
        _testSequenceNotFavourite(new DecreaseClosure());
    }

    @Test
    public void testDecreaseSequenceOK() throws Exception
    {
        _testSequenceOK(new OkDecreaseClosure());
    }//

    // MOVE TO START
    //
    @Test
    public void testMoveToStartSequenceExisting() throws Exception
    {
        _testSequenceNotExisting(new StartClosure());
    }

    @Test
    public void testMoveToStartSequenceAsAnonymous() throws Exception
    {
        _testSequenceAsAnonymous(new StartClosure());
    }

    @Test
    public void testMoveToStartSequenceNotFavourite() throws Exception
    {
        _testSequenceNotFavourite(new StartClosure());
    }

    @Test
    public void testMoveToStartSequenceOK() throws Exception
    {
        _testSequenceOK(new OkStartClosure());
    }//

    // MOVE TO END
    //
    @Test
    public void testMoveToEndSequenceExisting() throws Exception
    {
        _testSequenceNotExisting(new EndClosure());
    }

    @Test
    public void testMoveToEndSequenceAsAnonymous() throws Exception
    {
        _testSequenceAsAnonymous(new EndClosure());
    }

    @Test
    public void testMoveToEndSequenceNotFavourite() throws Exception
    {
        _testSequenceNotFavourite(new EndClosure());
    }

    @Test
    public void testMoveToEndSequenceOK() throws Exception
    {
        _testSequenceOK(new OkEndClosure());
    }

    private void _testSequenceNotExisting(final ReorderCommand reorderCommand)
    {
        final JiraServiceContext context = createContext();

        expect(shareTypeValidatorUtils.isValidSharePermission(context, portalPage2)).andReturn(true);

        expect(portalPageManager.getPortalPage(user, portalPage2.getId())).andReturn(null);

        final PortalPageService service = createPortalPageService();

        reorderCommand.execute(service, context, portalPage2);

        final ErrorCollection errorCollection = context.getErrorCollection();
        assertTrue(errorCollection.hasAnyErrors());
        assertTrue(errorCollection.getErrorMessages().contains("admin.errors.portalpages.no.access"));
    }

    private void _testSequenceAsAnonymous(final ReorderCommand reorderCommand)
    {
        final JiraServiceContext context = createContext((User) null);

        expect(shareTypeValidatorUtils.isValidSharePermission(context, portalPage1)).andReturn(true);

        final PortalPageService service = createPortalPageService();

        reorderCommand.execute(service, context, portalPage1);

        final ErrorCollection errorCollection = context.getErrorCollection();
        assertTrue(errorCollection.hasAnyErrors());
        assertTrue(errorCollection.getErrorMessages().contains("admin.errors.portalpages.owned.anonymous.user"));
    }

    private void _testSequenceNotFavourite(final ReorderCommand reorderCommand) throws PermissionException
    {
        final JiraServiceContext context = createContext(notTheOwner);

        expect(shareTypeValidatorUtils.isValidSharePermission(context, portalPage1)).andReturn(true);

        expect(portalPageManager.getPortalPage(notTheOwner, portalPage1.getId())).andReturn(portalPage1);

        expect(favouritesManager.isFavourite(notTheOwner, portalPage1)).andReturn(false);

        final PortalPageService service = createPortalPageService();

        reorderCommand.execute(service, context, portalPage1);

        final ErrorCollection errorCollection = context.getErrorCollection();
        assertTrue(errorCollection.hasAnyErrors());
        assertTrue(errorCollection.getErrorMessages().contains("admin.errors.portalpages.not.favourite"));
    }

    private void _testSequenceOK(final OkReorderCommand reorderCommand) throws Exception
    {
        final JiraServiceContext context = createContext();

        expect(portalPageManager.getPortalPage(user, portalPage1.getId())).andReturn(portalPage1);
        expect(favouritesManager.isFavourite(user, portalPage1)).andReturn(true);
        expect(shareTypeValidatorUtils.isValidSharePermission(context, portalPage1)).andReturn(true);

        reorderCommand.setupMockFavManager(favouritesManager);

        final PortalPageService service = createPortalPageService();

        reorderCommand.execute(service, context, portalPage1);

        final ErrorCollection errorCollection = context.getErrorCollection();
        assertFalse(errorCollection.hasAnyErrors());
    }

    //what used to be professional edition unit tests
    @Test
    public void testValidateForUpdateSystemDefaultPageNotGloballyShared()
    {
        final JiraServiceContext ctx = createContext();

        expect(permissionManager.hasPermission(Permissions.ADMINISTER, user)).andReturn(true);

        final PortalPageService service = createPortalPageService();
        final boolean ok = service.validateForUpdate(ctx, portalPageSystemDefault);
        assertFalse(ok);
        assertTrue(ctx.getErrorCollection().hasAnyErrors());
        assertTrue(ctx.getErrorCollection().getErrorMessages().contains("admin.errors.portalpages.sysdefault.must.be.public"));

        verifyMocks();
    }


    @Test
    public void testValidatePortalPageForCreateWithOtherUser()
    {
        final ApplicationUser otherUser = new MockApplicationUser("other");
        portalPage1 = PortalPage.id(1L).name("one").description("one description").owner(otherUser).build();

        final JiraServiceContext ctx = createContext();

        portalPage1 = PortalPage.portalPage(portalPage1).permissions(SharedEntity.SharePermissions.PRIVATE).build();
        expect(shareTypeValidatorUtils.isValidSharePermission(ctx, portalPage1)).andReturn(true);

        expect(portalPageManager.getPortalPageByName(user, portalPage1.getName())).andReturn(null);

        final PortalPageService service = createPortalPageService();
        service.validateForCreate(ctx, portalPage1);

        assertTrue(ctx.getErrorCollection().hasAnyErrors());
        assertTrue(ctx.getErrorCollection().getErrorMessages().contains("admin.errors.portalpages.must.be.owner"));
    }

    @Test
    public void testValidatePortalPageForCreateWithEmptyName()
    {
        final JiraServiceContext ctx = new MockJiraServiceContext(user.getDirectoryUser());
        PortalPage portalPage = PortalPage.name("").owner(user).build();
        final Set<SharePermission> permissions = Collections.<SharePermission>singleton(new SharePermissionImpl(new ShareType.Name("exampleType"), null, null));
        portalPage = PortalPage.portalPage(portalPage).permissions(new SharedEntity.SharePermissions(permissions)).build();

        expect(shareTypeValidatorUtils.isValidSharePermission(ctx, portalPage)).andReturn(true);


        final PortalPageService service = createPortalPageService();
        service.validateForCreate(ctx, portalPage);

        assertTrue(ctx.getErrorCollection().hasAnyErrors());
        final Map map = ctx.getErrorCollection().getErrors();
        assertTrue(map.containsKey("portalPageName"));
        assertEquals(map.get("portalPageName"), ctx.getI18nBean().getText("admin.errors.portalpages.must.specify.name"));

        verifyMocks();
    }

    @Test
    public void testValidatePortalPageForCreateWithValidPortalPage()
    {
        final JiraServiceContext ctx = new MockJiraServiceContext(user.getDirectoryUser());

        expect(portalPageManager.getPortalPageByName(user, portalPage1.getName())).andReturn(null);

        portalPage1 = PortalPage.portalPage(portalPage1).permissions(SharedEntity.SharePermissions.PRIVATE).build();
        expect(shareTypeValidatorUtils.isValidSharePermission(ctx, portalPage1)).andReturn(true);


        final PortalPageService service = createPortalPageService();
        service.validateForCreate(ctx, portalPage1);

        assertFalse(ctx.getErrorCollection().hasAnyErrors());

        verifyMocks();
    }

    @Test
    public void testValidatePortalPageForCreateWithBadNameAndNotSaved()
    {
        final JiraServiceContext ctx = new MockJiraServiceContext(user.getDirectoryUser());

        final PortalPage notSavedPortalPage = PortalPage.name("NotSaved").owner(user).build();

        expect(portalPageManager.getPortalPageByName(user, notSavedPortalPage.getName())).andReturn(notSavedPortalPage);

        expect(shareTypeValidatorUtils.isValidSharePermission(ctx, notSavedPortalPage)).andReturn(true);


        final PortalPageService service = createPortalPageService();
        service.validateForCreate(ctx, notSavedPortalPage);

        assertTrue(ctx.getErrorCollection().hasAnyErrors());
        final Map map = ctx.getErrorCollection().getErrors();
        assertTrue(map.containsKey("portalPageName"));
        assertEquals(map.get("portalPageName"), ctx.getI18nBean().getText("admin.errors.portalpages.same.name"));

        verifyMocks();
    }

    @Test
    public void testValidatePortalPageForCreateWithBadNameAndSaved()
    {
        final JiraServiceContext ctx = new MockJiraServiceContext(user.getDirectoryUser());

        final PortalPage newRequest = PortalPage.id(1L).name("testValidatePortalPageWithBadNameAndSaved").owner(user).build();
        final PortalPage oldRequest = PortalPage.id(2L).name("crapName").owner(user).build();

        expect(portalPageManager.getPortalPageByName(user, newRequest.getName())).andReturn(oldRequest);

        expect(shareTypeValidatorUtils.isValidSharePermission(ctx, newRequest)).andReturn(true);


        final PortalPageService service = createPortalPageService();
        service.validateForCreate(ctx, newRequest);

        assertTrue(ctx.getErrorCollection().hasAnyErrors());
        final Map map = ctx.getErrorCollection().getErrors();
        assertTrue(map.containsKey("portalPageName"));
        assertEquals(map.get("portalPageName"), ctx.getI18nBean().getText("admin.errors.portalpages.same.name"));

        verifyMocks();
    }

    @Test
    public void testValidatePortalPageForCreateWithPortalPageOfSameId()
    {
        final JiraServiceContext ctx = new MockJiraServiceContext(user.getDirectoryUser());

        final PortalPage newRequest = PortalPage.id(1L).name("testValidatePortalPageWithBadNameAndSaved").owner(user).build();
        final PortalPage oldRequest = PortalPage.id(2L).name("crapName").owner(user).build();

        expect(portalPageManager.getPortalPageByName(user, newRequest.getName())).andReturn(oldRequest);

        expect(shareTypeValidatorUtils.isValidSharePermission(ctx, newRequest)).andReturn(true);


        final PortalPageService service = createPortalPageService();
        service.validateForCreate(ctx, newRequest);

        assertTrue(ctx.getErrorCollection().hasAnyErrors());
        final Map map = ctx.getErrorCollection().getErrors();
        assertTrue(map.containsKey("portalPageName"));
        assertEquals(map.get("portalPageName"), ctx.getI18nBean().getText("admin.errors.portalpages.same.name"));

        verifyMocks();
    }

    @Test
    public void testCreatePortalPage()
    {
        final JiraServiceContext ctx = createContext();

        expect(shareTypeValidatorUtils.isValidSharePermission(ctx, portalPage3)).andReturn(true);

        expect(portalPageManager.create(portalPage3)).andReturn(portalPage2);


        final PortalPageService service = createPortalPageService();

        final PortalPage result = service.createPortalPage(ctx, portalPage3);
        assertFalse(ctx.getErrorCollection().hasAnyErrors());
        assertSame(portalPage2, result);

        verifyMocks();
    }

    @Test
    public void testCreatePortalPageNotAsOwner()
    {
        MockUser otherUser = new MockUser("otherUser");
        final JiraServiceContext context = createContext(otherUser);
        expect(shareTypeValidatorUtils.isValidSharePermission(context, portalPage1)).andReturn(true);

        final PortalPageService service = createPortalPageService();

        service.createPortalPage(context, portalPage1);

        final ErrorCollection errorCollection = context.getErrorCollection();
        assertTrue(errorCollection.hasAnyErrors());
        assertTrue(errorCollection.getErrorMessages().contains("admin.errors.portalpages.must.be.owner"));
    }

    @Test
    public void testCreatePortalPageFavouriteNotAsOwner()
    {
        MockUser otherUser = new MockUser("otherUser");
        final JiraServiceContext context = createContext(otherUser);
        expect(shareTypeValidatorUtils.isValidSharePermission(context, portalPage1)).andReturn(true);

        final PortalPageService service = createPortalPageService();

        service.createPortalPage(context, portalPage1, true);

        final ErrorCollection errorCollection = context.getErrorCollection();
        assertTrue(errorCollection.hasAnyErrors());
        assertTrue(errorCollection.getErrorMessages().contains("admin.errors.portalpages.must.be.owner"));
    }

    @Test
    public void testCreatePortalPageFavouriteWithAddFavourite() throws PermissionException
    {
        final JiraServiceContext ctx = createContext();

        expect(shareTypeValidatorUtils.isValidSharePermission(ctx, portalPage3)).andReturn(true);

        expect(portalPageManager.create(portalPage3)).andReturn(portalPage2);


        favouritesManager.addFavourite(user, portalPage2);

        final PortalPageService service = createPortalPageService();
        final PortalPage resultPortalPage = service.createPortalPage(ctx, portalPage3, true);
        assertSame(portalPage2, resultPortalPage);
        assertFalse(ctx.getErrorCollection().hasAnyErrors());

        verifyMocks();
    }

    @Test
    public void testCreatePortalPageFavouriteWithAddFavouriteError() throws PermissionException
    {
        final JiraServiceContext ctx = createContext();

        expect(shareTypeValidatorUtils.isValidSharePermission(ctx, portalPage3)).andReturn(true);

        expect(portalPageManager.create(portalPage3)).andReturn(portalPage2);


        favouritesManager.addFavourite(user, portalPage2);
        expectLastCall().andThrow(new PermissionException("ahhhh"));

        final PortalPageService service = createPortalPageService();
        final PortalPage resultPortalPage = service.createPortalPage(ctx, portalPage3, true);
        assertSame(portalPage2, resultPortalPage);
        assertTrue(ctx.getErrorCollection().hasAnyErrors());

        verifyMocks();
    }

    @Test
    public void testCreateClonePortalPage()
    {
        final JiraServiceContext ctx = createContext();

        expect(shareTypeValidatorUtils.isValidSharePermission(ctx, portalPage3)).andReturn(true);

        expect(portalPageManager.getPortalPageByName(ctx.getLoggedInApplicationUser(), portalPage3.getName())).andReturn(null);

        expect(portalPageManager.getPortalPage(ctx.getLoggedInApplicationUser(), CLONE_ID)).andReturn(portalPageClone);

        expect(portalPageManager.createBasedOnClone(ctx.getLoggedInApplicationUser(), portalPage3, portalPageClone)).andReturn(portalPage2);


        final PortalPageService service = createPortalPageService();

        final PortalPage result = service.createPortalPageByClone(ctx, portalPage3, CLONE_ID, false);
        assertFalse(ctx.getErrorCollection().hasAnyErrors());
        assertSame(portalPage2, result);

        verifyMocks();
    }

    @Test
    public void testCreateClonePortalPageNotAsOwner()
    {
        final ApplicationUser otherUser = new MockApplicationUser("otherUser");
        final JiraServiceContext context = createContext(otherUser);
        expect(shareTypeValidatorUtils.isValidSharePermission(context, portalPage1)).andReturn(true);

        expect(portalPageManager.getPortalPageByName(otherUser, portalPage1.getName())).andReturn(null);

        final PortalPageService service = createPortalPageService();

        service.createPortalPageByClone(context, portalPage1, CLONE_ID, false);

        final ErrorCollection errorCollection = context.getErrorCollection();
        assertTrue(errorCollection.hasAnyErrors());
        assertTrue(errorCollection.getErrorMessages().contains("admin.errors.portalpages.must.be.owner"));

        verifyMocks();
    }

    @Test
    public void testCreateClonePortalPageFavouriteNotAsOwner()
    {
        final ApplicationUser otherUser = new MockApplicationUser("otherUser");
        final JiraServiceContext context = createContext(otherUser);

        expect(shareTypeValidatorUtils.isValidSharePermission(context, portalPage1)).andReturn(true);

        expect(portalPageManager.getPortalPageByName(otherUser , portalPage1.getName())).andReturn(null);

        final PortalPageService service = createPortalPageService();

        service.createPortalPageByClone(context, portalPage1, CLONE_ID, true);

        final ErrorCollection errorCollection = context.getErrorCollection();
        assertTrue(errorCollection.hasAnyErrors());
        assertTrue(errorCollection.getErrorMessages().contains("admin.errors.portalpages.must.be.owner"));

        verifyMocks();
    }

    @Test
    public void testCreateClonePortalPageFavouriteWithAddFavourite() throws PermissionException
    {
        final JiraServiceContext ctx = createContext();

        expect(shareTypeValidatorUtils.isValidSharePermission(ctx, portalPage3)).andReturn(true);

        expect(portalPageManager.getPortalPageByName(user, portalPage3.getName())).andReturn(null);

        expect(portalPageManager.getPortalPage(user, CLONE_ID)).andReturn(portalPageClone);

        expect(portalPageManager.createBasedOnClone(user, portalPage3, portalPageClone)).andReturn(portalPage2);


        favouritesManager.addFavourite(user, portalPage2);

        final PortalPageService service = createPortalPageService();
        final PortalPage resultPortalPage = service.createPortalPageByClone(ctx, portalPage3, CLONE_ID, true);
        assertSame(portalPage2, resultPortalPage);
        assertFalse(ctx.getErrorCollection().hasAnyErrors());

        verifyMocks();
    }

    @Test
    public void testCreateClonePortalPageFavouriteWithAddFavouriteError() throws PermissionException
    {
        final JiraServiceContext ctx = createContext();

        expect(shareTypeValidatorUtils.isValidSharePermission(ctx, portalPage3)).andReturn(true);

        expect(portalPageManager.getPortalPageByName(user, portalPage3.getName())).andReturn(null);

        expect(portalPageManager.getPortalPage(user, CLONE_ID)).andReturn(portalPageClone);

        expect(portalPageManager.createBasedOnClone(user, portalPage3, portalPageClone)).andReturn(portalPage2);


        favouritesManager.addFavourite(user, portalPage2);
        expectLastCall().andThrow(new PermissionException("ahhhh"));

        final PortalPageService service = createPortalPageService();
        final PortalPage resultPortalPage = service.createPortalPageByClone(ctx, portalPage3, CLONE_ID, true);
        assertSame(portalPage2, resultPortalPage);
        assertTrue(ctx.getErrorCollection().hasAnyErrors());

        verifyMocks();
    }

    @Test
    public void testValidateClonePageDoesNotExist() throws Exception
    {
        final JiraServiceContext ctx = createContext();

        expect(shareTypeValidatorUtils.isValidSharePermission(ctx, portalPage3)).andReturn(true);

        expect(portalPageManager.getPortalPageByName(user, portalPage3.getName())).andReturn(null);

        expect(portalPageManager.getPortalPage(user, CLONE_ID)).andReturn(null);


        final PortalPageService service = createPortalPageService();
        final boolean ok = service.validateForCreatePortalPageByClone(ctx, portalPage3, CLONE_ID);
        assertFalse(ok);
        assertTrue(ctx.getErrorCollection().hasAnyErrors());

        assertTrue(ctx.getErrorCollection().getErrorMessages().contains("admin.errors.portalpages.clone.does.not.exist"));

        verifyMocks();
    }


    @Test
    public void testValidateClonePageMultipleCloningAllowedOnProAndEnt() throws Exception
    {
        final JiraServiceContext ctx = createContext();

        expect(shareTypeValidatorUtils.isValidSharePermission(ctx, portalPage3)).andReturn(true);

        expect(portalPageManager.getPortalPageByName(user, portalPage3.getName())).andReturn(null);

        expect(portalPageManager.getPortalPage(user, CLONE_ID)).andReturn(portalPageClone);


        final PortalPageService service = createPortalPageService();
        final boolean ok = service.validateForCreatePortalPageByClone(ctx, portalPage3, CLONE_ID);
        assertTrue(ok);
        assertFalse(ctx.getErrorCollection().hasAnyErrors());

        verifyMocks();
    }

    @Test
    public void testValidatePortalPageForCreateLongDescription()
    {
        final JiraServiceContext ctx = createContext(user);

        portalPage1 = PortalPage.portalPage(portalPage1).description("Really long descriptionReally long descriptionReally long descriptionReally long descriptionReally long descriptionReally long descriptionReally long descriptionReally long descriptionReally long descriptionReally long descriptionReally long descriptionRea").build();
        expect(portalPageManager.getPortalPageByName(user, portalPage1.getName())).andReturn(null);
        expect(shareTypeValidatorUtils.isValidSharePermission(ctx, portalPage1)).andReturn(true);


        final PortalPageService service = createPortalPageService();
        service.validateForCreate(ctx, portalPage1);

        assertTrue(ctx.getErrorCollection().hasAnyErrors());
        assertEquals("admin.errors.portalpages.description.too.long", ctx.getErrorCollection().getErrors().get("portalPageDescription"));
    }

    @Test
    public void testValidatePortalPageForUpdateLongDescription()
    {
        final JiraServiceContext ctx = createContext(user);

        portalPage1 = PortalPage.portalPage(portalPage1).description("Really long descriptionReally long descriptionReally long descriptionReally long descriptionReally long descriptionReally long descriptionReally long descriptionReally long descriptionReally long descriptionReally long descriptionReally long descriptionRea").build();
        expect(portalPageManager.getPortalPageById(1L)).andReturn(portalPage1);
        expect(portalPageManager.getPortalPageByName(user, portalPage1.getName())).andReturn(null);
        expect(shareTypeValidatorUtils.isValidSharePermission(ctx, portalPage1)).andReturn(true);


        final PortalPageService service = createPortalPageService();
        service.validateForUpdate(ctx, portalPage1);

        assertTrue(ctx.getErrorCollection().hasAnyErrors());
        assertEquals("admin.errors.portalpages.description.too.long", ctx.getErrorCollection().getErrors().get("portalPageDescription"));
    }

    interface ReorderCommand
    {
        void execute(PortalPageService service, JiraServiceContext context, PortalPage portalPage);
    }

    interface OkReorderCommand extends ReorderCommand
    {
        void setupMockFavManager(FavouritesManager<PortalPage> favouritesManager) throws Exception;
    }

    private static class IncreaseClosure implements ReorderCommand
    {
        public void execute(final PortalPageService service, final JiraServiceContext context, final PortalPage portalPage)
        {
            service.increasePortalPageSequence(context, portalPage.getId());
        }
    }

    private static class DecreaseClosure implements ReorderCommand
    {
        public void execute(final PortalPageService service, final JiraServiceContext context, final PortalPage portalPage)
        {
            service.decreasePortalPageSequence(context, portalPage.getId());
        }
    }

    private static class StartClosure implements ReorderCommand
    {
        public void execute(final PortalPageService service, final JiraServiceContext context, final PortalPage portalPage)
        {
            service.moveToStartPortalPageSequence(context, portalPage.getId());
        }
    }

    private static class EndClosure implements ReorderCommand
    {
        public void execute(final PortalPageService service, final JiraServiceContext context, final PortalPage portalPage)
        {
            service.moveToEndPortalPageSequence(context, portalPage.getId());
        }
    }

    private class OkIncreaseClosure extends IncreaseClosure implements OkReorderCommand
    {
        public void setupMockFavManager(final FavouritesManager<PortalPage> favouritesManager)
                throws Exception
        {
            favouritesManager.increaseFavouriteSequence(user, portalPage1);
        }
    }

    private class OkDecreaseClosure extends DecreaseClosure implements OkReorderCommand
    {
        public void setupMockFavManager(final FavouritesManager<PortalPage> favouritesManager)
                throws Exception
        {
            favouritesManager.decreaseFavouriteSequence(user, portalPage1);
        }
    }

    private class OkStartClosure extends StartClosure implements OkReorderCommand
    {
        public void setupMockFavManager(final FavouritesManager<PortalPage> favouritesManager)
                throws Exception
        {
            favouritesManager.moveToStartFavouriteSequence(user, portalPage1);
        }
    }

    private class OkEndClosure extends EndClosure implements OkReorderCommand
    {
        public void setupMockFavManager(final FavouritesManager<PortalPage> favouritesManager)
                throws Exception
        {
            favouritesManager.moveToEndFavouriteSequence(user, portalPage1);
        }
    }
}
