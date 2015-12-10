package com.atlassian.jira.bc.filter;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.embedded.impl.ImmutableUser;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.bc.MockJiraServiceContext;
import com.atlassian.jira.exception.PermissionException;
import com.atlassian.jira.favourites.FavouritesManager;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.issue.search.SearchRequestManager;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.sharing.ShareTypeValidatorUtils;
import com.atlassian.jira.sharing.SharedEntity.SharePermissions;
import com.atlassian.jira.sharing.search.PrivateShareTypeSearchParameter;
import com.atlassian.jira.sharing.search.SharedEntitySearchParameters;
import com.atlassian.jira.sharing.search.SharedEntitySearchParametersBuilder;
import com.atlassian.jira.sharing.search.SharedEntitySearchResult;
import com.atlassian.jira.timezone.TimeZoneManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.MockApplicationUser;
import com.atlassian.jira.user.util.MockUserManager;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.collect.MockCloseableIterable;
import com.atlassian.query.QueryImpl;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operator.Operator;

import com.google.common.collect.ImmutableList;

import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TestDefaultSearchRequestService
{
    private ShareTypeValidatorUtils mockShareTypeValidatorUtils;
    private SearchRequestManager mockSearchRequestManager;
    private FavouritesManager<SearchRequest> mockFavouritesManager;
    private UserUtil mockUserUtil;
    private PermissionManager mockPermissionManager;
    private ApplicationUser loggedInUser;
    private SearchRequest filter1;
    private SearchRequest filter2;
    private SearchRequest filter3;

    @Before
    public void setUp() throws Exception
    {
        mockSearchRequestManager = mock(SearchRequestManager.class);
        mockFavouritesManager = mock(FavouritesManager.class);
        mockUserUtil = mock(UserUtil.class);
        mockShareTypeValidatorUtils = mock(ShareTypeValidatorUtils.class);
        mockPermissionManager = mock(PermissionManager.class);

        loggedInUser = new MockApplicationUser("admin");
        MockUserManager userManager = new MockUserManager();
        userManager.addUser(loggedInUser);

        filter1 = new SearchRequest(new QueryImpl(), loggedInUser, "filter1", "filterter1Desc", 1L, 0L);
        filter2 = new SearchRequest(new QueryImpl(), loggedInUser, "filter2", "filterter2Desc", 2L, 0L);
        filter3 = new SearchRequest(new QueryImpl(), loggedInUser, "filter3", "filterter3Desc", 3L, 0L);

        new MockComponentWorker()
                .addMock(TimeZoneManager.class, mock(TimeZoneManager.class))
                .addMock(UserManager.class, userManager)
                .init();
    }

    private DefaultSearchRequestService createSearchRequestService()
    {
        return new DefaultSearchRequestService(mockSearchRequestManager, mockFavouritesManager, mockShareTypeValidatorUtils,
                mockUserUtil, mockPermissionManager);
    }

    static JiraServiceContext createContext(final User user)
    {
        return new MockJiraServiceContext(user)
        {
            public I18nHelper getI18nBean()
            {
                return Stubs.keyReturningI18nHelper();
            }
        };
    }

    private JiraServiceContext createServiceContext()
    {
        return createContext(loggedInUser.getDirectoryUser());
    }

    @Test
    public void testFavouriteFilters()
    {
        final List<SearchRequest> searchRequests = ImmutableList.of(filter2, filter3);
        final List<Long> favouriteIds = ImmutableList.of(filter3.getId(), filter2.getId(), filter1.getId()); // return is reverse order to test sorting

        when(mockFavouritesManager.getFavouriteIds(loggedInUser, SearchRequest.ENTITY_TYPE)).thenReturn(favouriteIds);

        when(mockSearchRequestManager.getSearchRequestById(loggedInUser, filter3.getId())).thenReturn(filter3);
        when(mockSearchRequestManager.getSearchRequestById(loggedInUser, filter2.getId())).thenReturn(filter2);
        when(mockSearchRequestManager.getSearchRequestById(loggedInUser, filter1.getId())).thenReturn(null);

        final SearchRequestService service = createSearchRequestService();

        final Collection results = service.getFavouriteFilters(loggedInUser);

        assertNotNull(results);
        assertEquals(searchRequests, results);
    }

    @Test
    public void testFavouriteFiltersNullUser()
    {
        final SearchRequestService service = createSearchRequestService();

        final Collection results = service.getFavouriteFilters((ApplicationUser) null);

        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    @Test
    public void testFavouriteFiltersNoFavourites()
    {
        when(mockFavouritesManager.getFavouriteIds(loggedInUser, SearchRequest.ENTITY_TYPE)).
                thenReturn(Collections.<Long>emptyList());

        final SearchRequestService service = createSearchRequestService();

        final Collection results = service.getFavouriteFilters(loggedInUser);

        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    @Test
    public void testOwnedFilters()
    {
        when(mockSearchRequestManager.getAllOwnedSearchRequests(loggedInUser)).thenReturn(ImmutableList.of(filter3, filter2));

        final SearchRequestService service = createSearchRequestService();

        final Collection results = service.getOwnedFilters(loggedInUser);

        assertNotNull(results);
        assertEquals(EasyList.build(filter2, filter3), results);
    }

    @Test
    public void testDeleteFilter()
    {
        when(mockSearchRequestManager.getSearchRequestOwner(filter1.getId())).
                thenReturn(filter1.getOwner());
        when(mockSearchRequestManager.getSearchRequestById(loggedInUser, filter1.getId())).thenReturn(filter1);

        final SearchRequestService service = createSearchRequestService();

        final JiraServiceContext ctx = createServiceContext();

        service.deleteFilter(ctx, filter1.getId());

        assertFalse(ctx.getErrorCollection().hasAnyErrors());

        verify(mockSearchRequestManager).delete(filter1.getId());
        verify(mockFavouritesManager).removeFavouritesForEntityDelete(filter1);
    }

    @Test
    public void testDeleteFilterNoFilter()
    {
        final Long filterToDeleteId = 1L;

        when(mockSearchRequestManager.getSearchRequestOwner(filter1.getId())).thenReturn(null);

        final SearchRequestService service = createSearchRequestService();
        final JiraServiceContext ctx = createServiceContext();

        service.deleteFilter(ctx, filterToDeleteId);

        final ErrorCollection errorCollection = ctx.getErrorCollection();
        assertTrue(errorCollection.hasAnyErrors());
        assertEquals(1, errorCollection.getErrorMessages().size());
        assertTrue(errorCollection.getErrorMessages().contains("admin.errors.filters.not.saved"));
    }

    @Test
    public void testDeleteFilterNotAuthor()
    {
        when(mockSearchRequestManager.getSearchRequestOwner(filter1.getId())).thenReturn(new MockApplicationUser("other"));

        final SearchRequestService service = createSearchRequestService();

        final JiraServiceContext ctx = createServiceContext();

        service.deleteFilter(ctx, filter1.getId());

        final ErrorCollection errorCollection = ctx.getErrorCollection();
        assertTrue(errorCollection.hasAnyErrors());
        assertEquals(1, errorCollection.getErrorMessages().size());
        assertTrue(errorCollection.getErrorMessages().contains("admin.errors.filters.must.be.owner"));
    }

    @Test
    public void testDeleteFilterAsAnonymous()
    {
        final SearchRequestService service = createSearchRequestService();

        final JiraServiceContext ctx = createContext(null);

        service.deleteFilter(ctx, filter1.getId());

        final ErrorCollection errorCollection = ctx.getErrorCollection();
        assertTrue(errorCollection.hasAnyErrors());
        assertEquals(1, errorCollection.getErrorMessages().size());
        assertTrue(errorCollection.getErrorMessages().contains("admin.errors.filters.owned.anonymous.user"));
    }

    @Test
    public void testDeleteFilterNotReturnedFromManager()
    {
        when(mockSearchRequestManager.getSearchRequestOwner(filter1.getId())).
                thenReturn(filter1.getOwner());
        when(mockSearchRequestManager.getSearchRequestById(loggedInUser, filter1.getId())).thenReturn(null);

        final SearchRequestService service = createSearchRequestService();

        final JiraServiceContext serviceContext = createServiceContext();

        service.deleteFilter(serviceContext, filter1.getId());

        final ErrorCollection errorCollection = serviceContext.getErrorCollection();
        assertTrue(errorCollection.hasAnyErrors());
        assertEquals(1, errorCollection.getErrorMessages().size());
        assertTrue(errorCollection.getErrorMessages().contains("admin.errors.filters.cannot.delete.filter"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void deleteFilterShouldNotAcceptANullFilterId()
    {
        final SearchRequestService service = createSearchRequestService();
        final JiraServiceContext ctx = createServiceContext();
        service.deleteFilter(ctx, null);
    }

    @Test
    public void testGetFilter()
    {
        when(mockSearchRequestManager.getSearchRequestById(loggedInUser, filter1.getId())).thenReturn(filter1);

        final SearchRequestService service = createSearchRequestService();
        final JiraServiceContext serviceContext = createServiceContext();

        final SearchRequest result = service.getFilter(serviceContext, filter1.getId());

        assertEquals(filter1, result);
        assertFalse(serviceContext.getErrorCollection().hasAnyErrors());
    }

    @Test
    public void gettingANonExistingFilterReturnsANullReferenceAndPopulatesAnErrorInTheServiceContext()
    {
        final Long nonExistentId = 303L;

        when(mockSearchRequestManager.getSearchRequestById(loggedInUser, nonExistentId)).thenReturn(null);

        final SearchRequestService searchRequestService = createSearchRequestService();
        final JiraServiceContext serviceContext = createServiceContext();

        final SearchRequest request = searchRequestService.getFilter(serviceContext, nonExistentId);
        assertNull("A search request was returned for a non-existing filter id, a null reference was expected", request);

        assertTrue
                (
                        "An error collection without errors was returned when requesting a non-existing search request",
                        serviceContext.getErrorCollection().hasAnyErrors()
                );

        final Collection<String> errorMessages = serviceContext.getErrorCollection().getErrorMessages();
        assertEquals(1, errorMessages.size());
        assertTrue(errorMessages.contains("admin.errors.filters.nonexistent"));
    }

    @Test
    public void anonymousUsersAreAbleToGetAnExistingFilter()
    {
        final ApplicationUser anAnonymousUser = null;
        when(mockSearchRequestManager.getSearchRequestById(anAnonymousUser, filter1.getId())).thenReturn(filter1);

        final SearchRequestService service = createSearchRequestService();
        final JiraServiceContext serviceContext = createContext(null);
        final SearchRequest result = service.getFilter(serviceContext, filter1.getId());

        assertEquals(filter1, result);
        assertFalse(serviceContext.getErrorCollection().hasAnyErrors());
    }

    @Test
    public void testGetFilterAnonymousUserDoesntExist()
    {
        final ApplicationUser anAnonymousUser = null;
        final Long nonExistentId = 303L;

        when(mockSearchRequestManager.getSearchRequestById(anAnonymousUser, nonExistentId)).thenReturn(null);

        final SearchRequestService searchRequestService = createSearchRequestService();
        final JiraServiceContext serviceContext = createContext(null);

        final SearchRequest request = searchRequestService.getFilter(serviceContext, nonExistentId);

        assertNull(request);
        assertTrue(serviceContext.getErrorCollection().hasAnyErrors());

        final Collection errorMessages = serviceContext.getErrorCollection().getErrorMessages();
        assertEquals(1, errorMessages.size());
        assertTrue(errorMessages.contains("admin.errors.filters.nonexistent"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void getFilterShouldNotAcceptANullFilterId()
    {
        final SearchRequestService service = createSearchRequestService();

        final JiraServiceContext serviceContext = new JiraServiceContextImpl(loggedInUser);
        service.getFilter(serviceContext, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getFilterShouldNotAcceptANullServiceContext()
    {
        final SearchRequestService service = createSearchRequestService();
        service.getFilter(null, 1L);
    }

    @Test
    public void testGetNonPrivateFiltersAll()
    {
        final List<SearchRequest> searchRequests = ImmutableList.of(filter2, filter1, filter3);

        filter1.setPermissions(SharePermissions.GLOBAL);
        filter2.setPermissions(SharePermissions.GLOBAL);

        when(mockSearchRequestManager.getAllOwnedSearchRequests(loggedInUser)).thenReturn(searchRequests);

        final SearchRequestService service = createSearchRequestService();

        final Collection results = service.getNonPrivateFilters(loggedInUser);

        assertEquals(ImmutableList.of(filter1, filter2), results);
    }

    @Test
    public void testGetNonPrivateFiltersNone()
    {
        final List<SearchRequest> searchRequests = ImmutableList.of(filter1, filter2);

        when(mockSearchRequestManager.getAllOwnedSearchRequests(loggedInUser)).thenReturn(searchRequests);

        final SearchRequestService service = createSearchRequestService();

        final Collection<SearchRequest> results = service.getNonPrivateFilters(loggedInUser);

        assertEquals(Collections.<SearchRequest>emptyList(), results);
    }

    @Test
    public void testGetNonPrivateFiltersAllBut1()
    {
        filter3.setPermissions(SharePermissions.GLOBAL);

        when(mockSearchRequestManager.getAllOwnedSearchRequests(loggedInUser)).
                thenReturn(ImmutableList.of(filter1, filter2, filter3));

        final SearchRequestService service = createSearchRequestService();

        final Collection results = service.getNonPrivateFilters(loggedInUser);

        assertEquals(ImmutableList.of(filter3), results);
    }

    @Test
    public void testGetNonPrivateFiltersNullUser()
    {
        final ApplicationUser aNullUser = null;
        when(mockSearchRequestManager.getAllOwnedSearchRequests(aNullUser)).thenReturn(Collections.<SearchRequest>emptyList());

        final SearchRequestService service = createSearchRequestService();

        final Collection results = service.getNonPrivateFilters((ApplicationUser) null);

        assertEquals(Collections.<SearchRequest>emptyList(), results);
    }

    @Test
    public void testGetFiltersFavouritedByOthers()
    {
        final SearchRequest filter1 = new SearchRequest(new QueryImpl(), loggedInUser, "filter1", "filterter1Desc", 1L, 3L);
        final SearchRequest filter2 = new SearchRequest(new QueryImpl(), loggedInUser, "filter2", "filterter2Desc", 2L, 3L);
        final SearchRequest filter3 = new SearchRequest(new QueryImpl(), loggedInUser, "filter3", "filterter3Desc", 3L, 3L);

        filter1.setPermissions(SharePermissions.GLOBAL);
        filter2.setPermissions(SharePermissions.GLOBAL);
        filter3.setPermissions(SharePermissions.GLOBAL);

        when(mockSearchRequestManager.getAllOwnedSearchRequests(loggedInUser)).
                thenReturn(ImmutableList.of(filter2, filter3, filter1));

        when(mockFavouritesManager.getFavouriteIds(loggedInUser, SearchRequest.ENTITY_TYPE)).
                thenReturn(ImmutableList.of(1L, 2L, 3L));

        final SearchRequestService service = createSearchRequestService();
        final Collection result = service.getFiltersFavouritedByOthers(loggedInUser);

        assertEquals(ImmutableList.of(filter1, filter2, filter3), result);
    }

    @Test
    public void testGetFiltersFavouritedByOthersFiltered()
    {
        final SearchRequest filter1 = new SearchRequest(new QueryImpl(), loggedInUser, "filter1", "filterter1Desc", 1L, 1L);
        final SearchRequest filter2 = new SearchRequest(new QueryImpl(), loggedInUser, "filter2", "filterter2Desc", 2L, 1L);
        final SearchRequest filter3 = new SearchRequest(new QueryImpl(), loggedInUser, "filter3", "filterter3Desc", 3L, 0L);

        filter2.setPermissions(SharePermissions.GLOBAL);

        when(mockSearchRequestManager.getAllOwnedSearchRequests(loggedInUser)).
                thenReturn(ImmutableList.of(filter1, filter2, filter3));

        when(mockFavouritesManager.getFavouriteIds(loggedInUser, SearchRequest.ENTITY_TYPE)).thenReturn(ImmutableList.of(1L));

        final SearchRequestService service = createSearchRequestService();
        final Collection result = service.getFiltersFavouritedByOthers(loggedInUser);

        assertEquals(ImmutableList.of(filter2), result);
    }

    @Test
    public void testGetFiltersFavouritedByOthersMoreFiltered()
    {
        final SearchRequest filter1 = new SearchRequest(new QueryImpl(), loggedInUser, "filter1", "filterter1Desc", 1L, 2L);
        final SearchRequest filter2 = new SearchRequest(new QueryImpl(), loggedInUser, "filter2", "filterter2Desc", 2L, 1L);
        final SearchRequest filter3 = new SearchRequest(new QueryImpl(), loggedInUser, "filter3", "filterter3Desc", 3L, 3L);

        final List<SearchRequest> searchRequests = ImmutableList.of(filter2, filter1, filter3);
        final List<SearchRequest> compareList = ImmutableList.of(filter1, filter3);
        filter1.setOwner(loggedInUser);

        filter1.setPermissions(SharePermissions.GLOBAL);
        filter3.setPermissions(SharePermissions.GLOBAL);

        when(mockSearchRequestManager.getAllOwnedSearchRequests(loggedInUser)).thenReturn(searchRequests);

        when(mockFavouritesManager.getFavouriteIds(loggedInUser, SearchRequest.ENTITY_TYPE)).
                thenReturn(ImmutableList.of(1L, 2L));

        final SearchRequestService service = createSearchRequestService();
        final Collection result = service.getFiltersFavouritedByOthers(loggedInUser);

        assertEquals(compareList, result);
    }

    @Test
    public void testGetFiltersFavouritedByOthersNullUser()
    {
        final ApplicationUser aNullUser = null;
        when(mockSearchRequestManager.getAllOwnedSearchRequests(aNullUser)).thenReturn(Collections.<SearchRequest>emptyList());

        final SearchRequestService service = createSearchRequestService();

        final Collection<SearchRequest> filters = service.getFiltersFavouritedByOthers((ApplicationUser) null);
        assertTrue(filters.isEmpty());
    }
    @Test
    public void testDeleteAllFiltersForUsers()
    {
        when(mockSearchRequestManager.getAllOwnedSearchRequests(loggedInUser)).
                thenReturn(ImmutableList.of(filter1, filter2, filter3));

        final SearchRequestService service = createSearchRequestService();

        service.deleteAllFiltersForUser(createServiceContext(), loggedInUser);

        verify(mockFavouritesManager).removeFavouritesForEntityDelete(filter1);
        verify(mockSearchRequestManager).delete(filter1.getId());

        verify(mockFavouritesManager).removeFavouritesForEntityDelete(filter2);
        verify(mockSearchRequestManager).delete(filter2.getId());

        verify(mockFavouritesManager).removeFavouritesForEntityDelete(filter3);
        verify(mockSearchRequestManager).delete(filter3.getId());

        verify(mockFavouritesManager).removeFavouritesForUser(loggedInUser, SearchRequest.ENTITY_TYPE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void validateFilterForUpdateShouldNotAcceptANullServiceContext()
    {
        final SearchRequestService service = createSearchRequestService();

        service.validateFilterForUpdate(null, filter1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void validateFilterForUpdateShouldNotAcceptANullSearchRequest()
    {
        final JiraServiceContext ctx = new JiraServiceContextImpl(loggedInUser);
        final SearchRequestService service = createSearchRequestService();

        service.validateFilterForUpdate(ctx, null);
    }

    @Test
    public void testValidateFilterForUpdateNotSaved()
    {
        final JiraServiceContext ctx = createContext(null);
        final SearchRequest unSavedFilter = mock(SearchRequest.class);
        when(unSavedFilter.getId()).thenReturn(null);

        when(mockShareTypeValidatorUtils.isValidSharePermission(ctx, filter2)).thenReturn(true);

        final SearchRequestService service = createSearchRequestService();
        service.validateFilterForUpdate(ctx, Stubs.SearchRequests.unsaved());

        assertTrue(ctx.getErrorCollection().hasAnyErrors());
        assertTrue(ctx.getErrorCollection().getErrorMessages().contains("admin.errors.filters.not.saved"));
    }

    @Test
    public void testValidateFilterForUpdateWithNullUser()
    {
        final JiraServiceContext ctx = createContext(null);

        filter1.setPermissions(SharePermissions.PRIVATE);
        when(mockShareTypeValidatorUtils.isValidSharePermission(ctx, filter1)).thenReturn(true);

        final SearchRequestService service = createSearchRequestService();
        service.validateFilterForUpdate(ctx, filter1);

        assertTrue(ctx.getErrorCollection().hasAnyErrors());
        assertTrue(ctx.getErrorCollection().getErrorMessages().contains("admin.errors.filters.owned.anonymous.user"));
    }

    @Test
    public void testValidateFilterForUpdateWithEmptyName()
    {
        final JiraServiceContext serviceContext = createServiceContext();
        filter1.setName("");
        filter1.setPermissions(SharePermissions.GLOBAL);

        when(mockShareTypeValidatorUtils.isValidSharePermission(serviceContext, filter1)).thenReturn(true);

        when(mockSearchRequestManager.getSearchRequestOwner(filter1.getId())).
                thenReturn(filter1.getOwner());

        final SearchRequestService service = createSearchRequestService();
        service.validateFilterForUpdate(serviceContext, filter1);

        assertTrue(serviceContext.getErrorCollection().hasAnyErrors());
        final Map map = serviceContext.getErrorCollection().getErrors();
        assertTrue(map.containsKey("filterName"));
        assertEquals(map.get("filterName"), "admin.errors.filters.must.specify.name");
    }

    @Test
    public void testValidateFilterForUpdateNotInDatabase()
    {
        final JiraServiceContext ctx = createServiceContext();

        filter1.setPermissions(SharePermissions.PRIVATE);

        when(mockShareTypeValidatorUtils.isValidSharePermission(ctx, filter1)).thenReturn(true);

        when(mockSearchRequestManager.getSearchRequestOwner(filter1.getId())).thenReturn(null);

        when(mockSearchRequestManager.getOwnedSearchRequestByName(loggedInUser, filter1.getName())).thenReturn(null);

        final SearchRequestService service = createSearchRequestService();
        service.validateFilterForUpdate(ctx, filter1);

        assertTrue(ctx.getErrorCollection().hasAnyErrors());
        assertTrue(ctx.getErrorCollection().getErrorMessages().contains("admin.errors.filters.not.saved"));
    }

    @Test
    public void testValidateFilterForUpdateNotOwner()
    {
        final ApplicationUser owner = new MockApplicationUser("user");

        filter1.setOwner(owner);

        final JiraServiceContext serviceContext = createServiceContext();
        filter1.setPermissions(SharePermissions.PRIVATE);

        when(mockShareTypeValidatorUtils.isValidSharePermission(serviceContext, filter1)).thenReturn(true);

        when(mockSearchRequestManager.getSearchRequestOwner(filter1.getId())).thenReturn(owner);

        when(mockSearchRequestManager.getOwnedSearchRequestByName(loggedInUser, filter1.getName())).thenReturn(filter1);

        final SearchRequestService service = createSearchRequestService();
        service.validateFilterForUpdate(serviceContext, filter1);

        assertTrue(serviceContext.getErrorCollection().hasAnyErrors());
        assertTrue(serviceContext.getErrorCollection().getErrorMessages().contains("admin.errors.filters.must.be.owner"));
    }

    @Test
    public void testValidateFilterForUpdateWithValidFilter()
    {
        final JiraServiceContext ctx = new JiraServiceContextImpl(loggedInUser);

        when(mockSearchRequestManager.getSearchRequestOwner(filter1.getId())).thenReturn(filter1.getOwner());

        when(mockSearchRequestManager.getOwnedSearchRequestByName(loggedInUser, filter1.getName())).thenReturn(null);
        filter1.setPermissions(SharePermissions.PRIVATE);

        when(mockShareTypeValidatorUtils.isValidSharePermission(ctx, filter1)).thenReturn(true);


        final SearchRequestService service = createSearchRequestService();
        service.validateFilterForUpdate(ctx, filter1);

        assertFalse(ctx.getErrorCollection().hasAnyErrors());
    }

    @Test
    public void testValidateFilterForUpdateWithBadNameAndSaved()
    {
        final JiraServiceContext ctx = createServiceContext();

        final SearchRequest newRequest = new SearchRequest(new QueryImpl(), loggedInUser, "testValidateFilterWithBadNameAndSaved", null, 1L, 0L);
        final SearchRequest oldRequest = new SearchRequest(new QueryImpl(), loggedInUser, "crapName", null, 2L, 0L);

        when(mockSearchRequestManager.getSearchRequestOwner(newRequest.getId())).
                thenReturn(newRequest.getOwner());

        when(mockSearchRequestManager.getOwnedSearchRequestByName(loggedInUser, newRequest.getName())).thenReturn(oldRequest);

        newRequest.setPermissions(SharePermissions.PRIVATE);
        when(mockShareTypeValidatorUtils.isValidSharePermission(ctx, newRequest)).thenReturn(true);

        final SearchRequestService service = createSearchRequestService();
        service.validateFilterForUpdate(ctx, newRequest);

        assertTrue(ctx.getErrorCollection().hasAnyErrors());
        final Map map = ctx.getErrorCollection().getErrors();
        assertTrue(map.containsKey("filterName"));
        assertEquals(map.get("filterName"), ctx.getI18nBean().getText("admin.errors.filters.same.name"));
    }

    @Test
    public void testValidateFilterForUpdateUpdateName()
    {
        final JiraServiceContext ctx = createServiceContext();

        final SearchRequest oldRequest = new SearchRequest(new QueryImpl(), loggedInUser, "crapName", null, 1L, 0L);

        when(mockSearchRequestManager.getSearchRequestOwner(filter1.getId())).
                thenReturn(filter1.getOwner());

        when(mockSearchRequestManager.getOwnedSearchRequestByName(loggedInUser, filter1.getName())).
                thenReturn(oldRequest);

        when(mockShareTypeValidatorUtils.isValidSharePermission(ctx, filter1)).thenReturn(true);


        final SearchRequestService service = createSearchRequestService();
        service.validateFilterForUpdate(ctx, filter1);

        assertFalse(ctx.getErrorCollection().hasAnyErrors());
    }

    @Test
    public void testValidateFilterForUpdateUpdateNameThatsTooLong()
    {
        final JiraServiceContext ctx = createServiceContext();

        final SearchRequest oldRequest = new SearchRequest(new QueryImpl(), loggedInUser, "crapName", null, 1L, 0L);

        when(mockSearchRequestManager.getSearchRequestOwner(filter1.getId())).
                thenReturn(filter1.getOwner());

        when(mockSearchRequestManager.getOwnedSearchRequestByName(loggedInUser, filter1.getName())).
                thenReturn(oldRequest);

        when(mockShareTypeValidatorUtils.isValidSharePermission(ctx, filter1)).thenReturn(true);

        final SearchRequestService service = createSearchRequestService();
        filter1.setName(createFilternameThatsTooLong());
        service.validateFilterForUpdate(ctx, filter1);

        assertTrue(ctx.getErrorCollection().hasAnyErrors());
        final Map map = ctx.getErrorCollection().getErrors();
        assertTrue(map.containsKey("filterName"));
        assertEquals(map.get("filterName"), ctx.getI18nBean().getText("admin.errors.filters.name.toolong"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void validateUpdateSearchParametersShouldNotAcceptANullServiceContext()
    {
        final SearchRequestService service = createSearchRequestService();

        service.validateUpdateSearchParameters(null, filter1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void validateUpdateSearchParametersShouldNotAcceptANullSearchRequest()
    {
        final JiraServiceContext ctx = new JiraServiceContextImpl(loggedInUser);
        final SearchRequestService service = createSearchRequestService();
        service.validateUpdateSearchParameters(ctx, null);
    }

    @Test
    public void testValidateUpdateSearchParametersNotSaved()
    {
        final JiraServiceContext ctx = createContext(null);
        SearchRequest unsavedFilter = Stubs.SearchRequests.unsaved();
        unsavedFilter.setPermissions(SharePermissions.PRIVATE);

        when(mockShareTypeValidatorUtils.isValidSharePermission(ctx, unsavedFilter)).thenReturn(true);

        final SearchRequestService service = createSearchRequestService();
        assertFalse(service.validateUpdateSearchParameters(ctx, unsavedFilter));

        assertTrue(ctx.getErrorCollection().hasAnyErrors());
        assertTrue(ctx.getErrorCollection().getErrorMessages().contains("admin.errors.filters.not.saved"));
    }

    @Test
    public void testValidateUpdateSearchParametersWithNullUser()
    {
        final JiraServiceContext ctx = createContext(null);

        filter1.setPermissions(SharePermissions.PRIVATE);
        when(mockShareTypeValidatorUtils.isValidSharePermission(ctx, filter1)).thenReturn(true);

        final SearchRequestService service = createSearchRequestService();
        assertFalse(service.validateUpdateSearchParameters(ctx, filter1));

        assertTrue(ctx.getErrorCollection().hasAnyErrors());
        assertTrue(ctx.getErrorCollection().getErrorMessages().contains("admin.errors.filters.owned.anonymous.user"));
    }

    @Test
    public void testValidateUpdateSearchParametersNotInDatabase()
    {
        final JiraServiceContext ctx = createServiceContext();

        filter1.setPermissions(SharePermissions.PRIVATE);

        when(mockSearchRequestManager.getSearchRequestOwner(filter1.getId())).thenReturn(null);

        final SearchRequestService service = createSearchRequestService();
        assertFalse(service.validateUpdateSearchParameters(ctx, filter1));

        assertTrue(ctx.getErrorCollection().hasAnyErrors());
        assertTrue(ctx.getErrorCollection().getErrorMessages().contains("admin.errors.filters.not.saved"));
    }

    @Test
    public void testValidateUpdateSearchParametersNotOwner()
    {
        final ApplicationUser owner = new MockApplicationUser("user");

        filter1.setOwner(owner);

        final JiraServiceContext ctx = createServiceContext();
        filter1.setPermissions(SharePermissions.PRIVATE);

        when(mockSearchRequestManager.getSearchRequestOwner(filter1.getId())).thenReturn(owner);

        final SearchRequestService service = createSearchRequestService();
        assertFalse(service.validateUpdateSearchParameters(ctx, filter1));

        assertTrue(ctx.getErrorCollection().hasAnyErrors());
        assertTrue(ctx.getErrorCollection().getErrorMessages().contains("admin.errors.filters.must.be.owner"));
    }

    @Test
    public void testValidateUpdateSearchParametersWithValidFilter()
    {
        final JiraServiceContext ctx = new JiraServiceContextImpl(loggedInUser);

        when(mockSearchRequestManager.getSearchRequestOwner(filter1.getId())).thenReturn(filter1.getOwner());


        filter1.setPermissions(SharePermissions.PRIVATE);

        final SearchRequestService service = createSearchRequestService();
        assertTrue(service.validateUpdateSearchParameters(ctx, filter1));

        assertFalse(ctx.getErrorCollection().hasAnyErrors());
    }

    @Test(expected = IllegalArgumentException.class)
    public void validateFilterForCreateShouldNotAcceptANullServiceContext()
    {
        final SearchRequestService service = createSearchRequestService();
        service.validateFilterForCreate(null, filter1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void validateFilterForCreateShouldNotAcceptANullSearchRequest()
    {
        final JiraServiceContext serviceContext = new JiraServiceContextImpl(loggedInUser);
        final SearchRequestService service = createSearchRequestService();

        service.validateFilterForCreate(serviceContext, null);
    }

    @Test
    public void testValidateFilterForCreateWithNullUser()
    {
        final JiraServiceContext ctx = createContext(null);

        filter1.setPermissions(SharePermissions.PRIVATE);
        when(mockShareTypeValidatorUtils.isValidSharePermission(ctx, filter1)).thenReturn(true);

        final SearchRequestService service = createSearchRequestService();
        service.validateFilterForCreate(ctx, filter1);

        assertTrue(ctx.getErrorCollection().hasAnyErrors());
        assertTrue(ctx.getErrorCollection().getErrorMessages().contains("admin.errors.filters.owned.anonymous.user"));
    }

    @Test
    public void testValidateFilterForCreateWithOtherUser()
    {
        final ApplicationUser otherUser = new MockApplicationUser("other");

        final JiraServiceContext ctx = createServiceContext();

        filter1.setOwner(otherUser);
        filter1.setPermissions(SharePermissions.PRIVATE);

        when(mockShareTypeValidatorUtils.isValidSharePermission(ctx, filter1)).thenReturn(true);

        when(mockSearchRequestManager.getOwnedSearchRequestByName(loggedInUser, filter1.getName())).thenReturn(null);

        final SearchRequestService service = createSearchRequestService();
        service.validateFilterForCreate(ctx, filter1);

        assertTrue(ctx.getErrorCollection().hasAnyErrors());
        assertTrue(ctx.getErrorCollection().getErrorMessages().contains("admin.errors.filters.must.be.owner"));
    }

    @Test
    public void testValidateFilterForCreateWithEmptyName()
    {
        final JiraServiceContext ctx = new MockJiraServiceContext(loggedInUser.getDirectoryUser());
        final SearchRequest request = new SearchRequest();
        request.setPermissions(SharePermissions.GLOBAL);

        when(mockShareTypeValidatorUtils.isValidSharePermission(ctx, request)).thenReturn(true);

        final SearchRequestService service = createSearchRequestService();
        service.validateFilterForCreate(ctx, request);

        assertTrue(ctx.getErrorCollection().hasAnyErrors());
        final Map map = ctx.getErrorCollection().getErrors();
        assertTrue(map.containsKey("filterName"));
        assertEquals(map.get("filterName"), ctx.getI18nBean().getText("admin.errors.filters.must.specify.name"));
    }

    @Test
    public void testValidateFilterForCreateWithValidFilter()
    {
        final JiraServiceContext ctx = new JiraServiceContextImpl(loggedInUser);

        when(mockSearchRequestManager.getOwnedSearchRequestByName((loggedInUser), filter1.getName())).thenReturn(null);


        filter1.setPermissions(SharePermissions.PRIVATE);
        when(mockShareTypeValidatorUtils.isValidSharePermission(ctx, filter1)).thenReturn(true);

        final SearchRequestService service = createSearchRequestService();
        service.validateFilterForCreate(ctx, filter1);

        assertFalse(ctx.getErrorCollection().hasAnyErrors());
    }

    @Test
    public void testValidateFilterForCreateWithBadNameAndNotSaved()
    {
        final JiraServiceContext ctx = new MockJiraServiceContext(loggedInUser.getDirectoryUser());

        final SearchRequest notSavedFilter = new SearchRequest(new QueryImpl(), loggedInUser, "NotSaved", null);

        when(mockSearchRequestManager.getOwnedSearchRequestByName(loggedInUser, notSavedFilter.getName())).
                thenReturn(notSavedFilter);
        notSavedFilter.setPermissions(SharePermissions.PRIVATE);

        when(mockShareTypeValidatorUtils.isValidSharePermission(ctx, notSavedFilter)).thenReturn(true);

        final SearchRequestService service = createSearchRequestService();
        service.validateFilterForCreate(ctx, notSavedFilter);

        assertTrue(ctx.getErrorCollection().hasAnyErrors());
        final Map map = ctx.getErrorCollection().getErrors();
        assertTrue(map.containsKey("filterName"));
        assertEquals(map.get("filterName"), ctx.getI18nBean().getText("admin.errors.filters.same.name"));
    }

    @Test
    public void testValidateFilterForCreateWithBadNameAndSaved()
    {
        final JiraServiceContext ctx = new MockJiraServiceContext(loggedInUser.getDirectoryUser());

        final SearchRequest newRequest = new SearchRequest(new QueryImpl(), loggedInUser, "testValidateFilterWithBadNameAndSaved", null, 1L, 0L);
        final SearchRequest oldRequest = new SearchRequest(new QueryImpl(), loggedInUser, "crapName", null, 1L, 0L);

        when(mockSearchRequestManager.getOwnedSearchRequestByName(loggedInUser, newRequest.getName())).thenReturn(oldRequest);
        newRequest.setPermissions(SharePermissions.PRIVATE);

        when(mockShareTypeValidatorUtils.isValidSharePermission(ctx, newRequest)).thenReturn(true);

        final SearchRequestService service = createSearchRequestService();
        service.validateFilterForCreate(ctx, newRequest);

        assertTrue(ctx.getErrorCollection().hasAnyErrors());
        final Map map = ctx.getErrorCollection().getErrors();
        assertTrue(map.containsKey("filterName"));
        assertEquals(map.get("filterName"), ctx.getI18nBean().getText("admin.errors.filters.same.name"));
    }

    @Test
    public void testValidateFilterForCreateWithNameThatsTooLong()
    {
        final JiraServiceContext ctx = new MockJiraServiceContext(loggedInUser.getDirectoryUser());
        final String name = createFilternameThatsTooLong();

        final SearchRequest newRequest = new SearchRequest(new QueryImpl(), loggedInUser, name, null, 1L, 0L);
        when(mockShareTypeValidatorUtils.isValidSharePermission(ctx, newRequest)).thenReturn(true);

        final SearchRequestService service = createSearchRequestService();
        service.validateFilterForCreate(ctx, newRequest);

        assertTrue(ctx.getErrorCollection().hasAnyErrors());
        final Map map = ctx.getErrorCollection().getErrors();
        assertTrue(map.containsKey("filterName"));
        assertEquals(map.get("filterName"), ctx.getI18nBean().getText("The entered filter name is too long, it must be less than 255 chars."));
    }

    @Test
    public void testValidateFilterForCreateWithFilterOfSameId()
    {
        final JiraServiceContext ctx = new MockJiraServiceContext(loggedInUser.getDirectoryUser());

        final SearchRequest newRequest = new SearchRequest(new QueryImpl(), loggedInUser, "testValidateFilterWithBadNameAndSaved", null, 1L, 0L);
        final SearchRequest oldRequest = new SearchRequest(new QueryImpl(), loggedInUser, "crapName", null, 1L, 0L);

        when(mockSearchRequestManager.getOwnedSearchRequestByName(loggedInUser, newRequest.getName())).thenReturn(oldRequest);
        newRequest.setPermissions(SharePermissions.PRIVATE);

        when(mockShareTypeValidatorUtils.isValidSharePermission(ctx, newRequest)).thenReturn(true);

        final SearchRequestService service = createSearchRequestService();
        service.validateFilterForCreate(ctx, newRequest);

        assertTrue(ctx.getErrorCollection().hasAnyErrors());
        final Map map = ctx.getErrorCollection().getErrors();
        assertTrue(map.containsKey("filterName"));
        assertEquals(map.get("filterName"), ctx.getI18nBean().getText("admin.errors.filters.same.name"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void validateForDeleteShouldNotAcceptANullServiceContext()
    {
        final SearchRequestService service = createSearchRequestService();
        service.validateForDelete(null, filter1.getId());
    }

    @Test(expected = IllegalArgumentException.class)
    public void validateForDeleteShouldNotAcceptANullFilterId()
    {
        final SearchRequestService service = createSearchRequestService();
        service.validateForDelete(createServiceContext(), null);
    }

    @Test
    public void validateForDeleteShouldReturnAnErrorForAnonymousUsers()
    {
        final SearchRequestService service = createSearchRequestService();
        final JiraServiceContext serviceContext = createContext(null);

        service.validateForDelete(serviceContext, filter1.getId());

        final ErrorCollection errorCollection = serviceContext.getErrorCollection();
        assertTrue(errorCollection.hasAnyErrors());
        assertTrue(errorCollection.getErrorMessages().contains("admin.errors.filters.owned.anonymous.user"));
    }

    @Test
    public void validateForDeleteShouldReturnAnErrorForNonExistingFilters()
    {
        when(mockSearchRequestManager.getSearchRequestOwner(filter1.getId())).thenReturn(null);

        final SearchRequestService service = createSearchRequestService();
        final JiraServiceContext serviceContext = createServiceContext();

        service.validateForDelete(serviceContext, filter1.getId());

        final ErrorCollection errorCollection = serviceContext.getErrorCollection();
        assertTrue(errorCollection.hasAnyErrors());
        assertTrue(errorCollection.getErrorMessages().contains("admin.errors.filters.not.saved"));
    }

    @Test
    public void validateForDeleteShouldReturnAnErrorGivenThatTheUserDoesNotOwnTheFilterAndIsNotAJiraAdministrator()
    {
        final ApplicationUser otherUser = new MockApplicationUser("otherUser");

        when(mockSearchRequestManager.getSearchRequestOwner(filter1.getId())).thenReturn(otherUser);
        expectThatTheLoggedInUserIsNotAnAdministrator();

        final SearchRequestService service = createSearchRequestService();
        final JiraServiceContext serviceContext = createServiceContext();

        service.validateForDelete(serviceContext, filter1.getId());

        final ErrorCollection errorCollection = serviceContext.getErrorCollection();
        assertTrue(errorCollection.hasAnyErrors());
        assertTrue(errorCollection.getErrorMessages().contains("admin.errors.filters.must.be.owner"));
    }

    private void expectThatTheLoggedInUserIsNotAnAdministrator()
    {
        when(mockPermissionManager.hasPermission(Permissions.ADMINISTER, loggedInUser)).thenReturn(false);
    }

    @Test
    public void validateForDeleteShouldReturnNoErrorsGivenThatTheUserIsAJiraAdministrator()
    {
        final ApplicationUser otherUser = new MockApplicationUser("otherUser");

        when(mockSearchRequestManager.getSearchRequestOwner(filter1.getId())).thenReturn(otherUser);
        expectThatTheLoggedInUserIsAnAdministrator();

        final SearchRequestService service = createSearchRequestService();
        final JiraServiceContext serviceContext = createServiceContext();

        service.validateForDelete(serviceContext, filter1.getId());

        final ErrorCollection errorCollection = serviceContext.getErrorCollection();
        assertFalse(errorCollection.hasAnyErrors());
    }

    private void expectThatTheLoggedInUserIsAnAdministrator()
    {
        when(mockPermissionManager.hasPermission(Permissions.ADMINISTER, loggedInUser)).thenReturn(true);
    }

    @Test
    public void testValidateForDelete()
    {
        when(mockSearchRequestManager.getSearchRequestOwner(filter1.getId())).thenReturn(filter1.getOwner());


        final SearchRequestService service = createSearchRequestService();
        final JiraServiceContext serviceContext = createServiceContext();

        service.validateForDelete(serviceContext, filter1.getId());

        final ErrorCollection errorCollection = serviceContext.getErrorCollection();
        assertFalse(errorCollection.hasAnyErrors());
    }

    @Test
    public void testCreateFilter()
    {
        final JiraServiceContext ctx = createServiceContext();

        when(mockShareTypeValidatorUtils.isValidSharePermission(ctx, filter3)).thenReturn(true);

        when(mockSearchRequestManager.create(filter3)).thenReturn(filter2);


        final SearchRequestService service = createSearchRequestService();

        final SearchRequest result = service.createFilter(ctx, filter3);
        assertFalse(ctx.getErrorCollection().hasAnyErrors());
        assertSame(filter2, result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateFilterShouldNotAcceptANullServiceContext()
    {
        final SearchRequestService service = createSearchRequestService();
        service.createFilter(null, filter1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateFilterShouldNotAcceptANullSearchRequest()
    {
        final SearchRequestService service = createSearchRequestService();
        service.createFilter(createServiceContext(), null);
    }

    @Test
    public void testCreateFilterAsAnonymous()
    {
        final User anAnnonymousUser = null;
        final JiraServiceContext context = createContext(anAnnonymousUser);

        when(mockShareTypeValidatorUtils.isValidSharePermission(context, filter1)).thenReturn(true);

        final SearchRequestService service = createSearchRequestService();

        service.createFilter(context, filter1);

        final ErrorCollection errorCollection = context.getErrorCollection();
        assertTrue(errorCollection.hasAnyErrors());
        assertTrue(errorCollection.getErrorMessages().contains("admin.errors.filters.owned.anonymous.user"));
    }

    @Test
    public void testCreateFilterNotAsOwner()
    {
        final JiraServiceContext context = createContext(ImmutableUser.newUser().name("otherUser").toUser());
        when(mockShareTypeValidatorUtils.isValidSharePermission(context, filter1)).thenReturn(true);

        final SearchRequestService service = createSearchRequestService();

        service.createFilter(context, filter1);

        final ErrorCollection errorCollection = context.getErrorCollection();
        assertTrue(errorCollection.hasAnyErrors());
        assertTrue(errorCollection.getErrorMessages().contains("admin.errors.filters.must.be.owner"));
    }

    @Test
    public void testUpdateFilter()
    {
        final JiraServiceContext ctx = createServiceContext();

        when(mockSearchRequestManager.getSearchRequestOwner(filter1.getId())).thenReturn(filter1.getOwner());

        when(mockSearchRequestManager.update(filter1)).thenReturn(filter2);

        when(mockShareTypeValidatorUtils.isValidSharePermission(ctx, filter1)).thenReturn(true);


        final SearchRequestService service = createSearchRequestService();

        final SearchRequest result = service.updateFilter(ctx, filter1);
        assertFalse(ctx.getErrorCollection().hasAnyErrors());
        assertSame(filter2, result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void updateFilterShouldNotAcceptANullServiceContext()
    {
        final SearchRequestService service = createSearchRequestService();
        service.updateFilter(null, filter1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void updateFilterShouldNotAcceptANullSearchRequest()
    {
        final SearchRequestService service = createSearchRequestService();
        service.updateFilter(createServiceContext(), null);
    }

    @Test
    public void testUpdateFilterNotSaved()
    {
        final JiraServiceContext context = createServiceContext();

        final SearchRequest unSavedFilter = Stubs.SearchRequests.unsaved();
        when(mockShareTypeValidatorUtils.isValidSharePermission(context, unSavedFilter)).thenReturn(true);

        final SearchRequestService service = createSearchRequestService();

        service.updateFilter(context, unSavedFilter);

        final ErrorCollection errorCollection = context.getErrorCollection();
        assertTrue(errorCollection.hasAnyErrors());
        assertTrue(errorCollection.getErrorMessages().contains("admin.errors.filters.not.saved"));
    }

    @Test
    public void testUpdateFilterAsAnonymous()
    {
        final JiraServiceContext context = createContext(null);

        when(mockShareTypeValidatorUtils.isValidSharePermission(context, filter1)).thenReturn(true);

        final SearchRequestService service = createSearchRequestService();

        service.updateFilter(context, filter1);

        final ErrorCollection errorCollection = context.getErrorCollection();
        assertTrue(errorCollection.hasAnyErrors());
        assertTrue(errorCollection.getErrorMessages().contains("admin.errors.filters.owned.anonymous.user"));
    }

    @Test
    public void testUpdateFilterNotNewOwner()
    {
        final JiraServiceContext context = createContext(ImmutableUser.newUser().name("notMe").toUser());

        when(mockShareTypeValidatorUtils.isValidSharePermission(context, filter1)).thenReturn(true);

        final SearchRequestService service = createSearchRequestService();

        service.updateFilter(context, filter1);

        final ErrorCollection errorCollection = context.getErrorCollection();
        assertTrue(errorCollection.hasAnyErrors());
        assertTrue(errorCollection.getErrorMessages().contains("admin.errors.filters.must.be.owner"));
    }

    @Test
    public void testUpdateFilterNotInDb()
    {
        final JiraServiceContext context = createServiceContext();

        when(mockSearchRequestManager.getSearchRequestOwner(filter1.getId())).thenReturn(null);

        when(mockShareTypeValidatorUtils.isValidSharePermission(context, filter1)).thenReturn(true);


        final SearchRequestService service = createSearchRequestService();

        service.updateFilter(context, filter1);

        final ErrorCollection errorCollection = context.getErrorCollection();
        assertTrue(errorCollection.hasAnyErrors());
        assertTrue(errorCollection.getErrorMessages().contains("admin.errors.filters.not.saved"));
    }

    @Test
    public void testUpdateFilterNotOwnerInDb()
    {
        final JiraServiceContext context = createServiceContext();

        when(mockSearchRequestManager.getSearchRequestOwner(filter1.getId())).thenReturn(new MockApplicationUser("notMe"));

        when(mockShareTypeValidatorUtils.isValidSharePermission(context, filter1)).thenReturn(true);

        final SearchRequestService service = createSearchRequestService();

        service.updateFilter(context, filter1);

        final ErrorCollection errorCollection = context.getErrorCollection();
        assertTrue(errorCollection.hasAnyErrors());
        assertTrue(errorCollection.getErrorMessages().contains("admin.errors.filters.must.be.owner"));
    }

    @Test
    public void testUpdateSearchParametersFilter()
    {
        final JiraServiceContext ctx = createServiceContext();

        //this is the filter with the new parameter that we pass to be saved.
        final SearchRequest newFilter = new SearchRequest(filter1);

        JqlQueryBuilder builder = JqlQueryBuilder.newBuilder(newFilter.getQuery());
        builder.where().defaultAnd().addClause(new TerminalClauseImpl(IssueFieldConstants.ISSUE_TYPE, Operator.EQUALS, "test"));

        newFilter.setQuery(builder.buildQuery());
        newFilter.setName("testUpdateSearchParametersFilter");
        newFilter.setPermissions(SharePermissions.GLOBAL);

        when(mockSearchRequestManager.getSearchRequestOwner(newFilter.getId())).thenReturn(newFilter.getOwner());

        when(mockSearchRequestManager.getSearchRequestById(ctx.getLoggedInApplicationUser(), newFilter.getId())).thenReturn(filter1);

        final SearchRequest expectedFilter = new SearchRequest(filter1);
        builder = JqlQueryBuilder.newBuilder(expectedFilter.getQuery());
        builder.where().defaultAnd().addClause(new TerminalClauseImpl(IssueFieldConstants.ISSUE_TYPE, Operator.EQUALS, "test"));
        expectedFilter.setQuery(builder.buildQuery());

        when(mockSearchRequestManager.update(expectedFilter)).thenReturn(filter2);


        final SearchRequestService service = createSearchRequestService();

        final SearchRequest result = service.updateSearchParameters(ctx, newFilter);
        assertFalse(ctx.getErrorCollection().hasAnyErrors());
        assertSame(filter2, result);
    }

    @Test
    public void testUpdateSearchParametersFilterDoesNotExist()
    {
        final JiraServiceContext ctx = createServiceContext();

        //this is the filter with the new parameter that we pass to be saved.
        final SearchRequest newFilter = new SearchRequest(filter1);
        JqlQueryBuilder builder = JqlQueryBuilder.newBuilder(newFilter.getQuery());
        builder.where().defaultAnd().addClause(new TerminalClauseImpl(IssueFieldConstants.ISSUE_TYPE, Operator.EQUALS, "test"));
        newFilter.setName("testUpdateSearchParametersFilterDoesNotExist");
        newFilter.setPermissions(SharePermissions.GLOBAL);

        when(mockSearchRequestManager.getSearchRequestOwner(newFilter.getId())).thenReturn(newFilter.getOwner());

        when(mockSearchRequestManager.getSearchRequestById(ctx.getLoggedInApplicationUser(), newFilter.getId())).thenReturn(null);


        final SearchRequestService service = createSearchRequestService();

        final SearchRequest result = service.updateSearchParameters(ctx, newFilter);
        assertNull(result);
        assertTrue(ctx.getErrorCollection().hasAnyErrors());
        assertTrue(ctx.getErrorCollection().getErrorMessages().contains("admin.errors.filters.not.saved"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void updateSearchParametersFilterShouldNotAcceptANullServiceContext()
    {
        final SearchRequestService service = createSearchRequestService();
        service.updateSearchParameters(null, filter1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void updateSearchParametersFilterShouldNotAcceptANullSearchRequest()
    {
        final SearchRequestService service = createSearchRequestService();
        service.updateSearchParameters(createServiceContext(), null);
    }

    @Test
    public void testUpdateSearchParametersFilterNotSaved()
    {
        final JiraServiceContext context = createServiceContext();

        final SearchRequestService service = createSearchRequestService();

        final SearchRequest unSavedFilter = Stubs.SearchRequests.unsaved();
        service.updateSearchParameters(context, unSavedFilter);

        final ErrorCollection errorCollection = context.getErrorCollection();
        assertTrue(errorCollection.hasAnyErrors());
        assertTrue(errorCollection.getErrorMessages().contains("admin.errors.filters.not.saved"));
    }

    @Test
    public void testUpdateSearchParametersAsAnonymous()
    {
        final JiraServiceContext context = createContext(null);

        final SearchRequestService service = createSearchRequestService();

        service.updateSearchParameters(context, filter1);

        final ErrorCollection errorCollection = context.getErrorCollection();
        assertTrue(errorCollection.hasAnyErrors());
        assertTrue(errorCollection.getErrorMessages().contains("admin.errors.filters.owned.anonymous.user"));
    }

    @Test
    public void testUpdateSearchParametersNotNewOwner()
    {
        final JiraServiceContext context = createContext(ImmutableUser.newUser().name("notMe").toUser());

        final SearchRequestService service = createSearchRequestService();

        service.updateSearchParameters(context, filter1);

        final ErrorCollection errorCollection = context.getErrorCollection();
        assertTrue(errorCollection.hasAnyErrors());
        assertTrue(errorCollection.getErrorMessages().contains("admin.errors.filters.must.be.owner"));
    }

    @Test
    public void testUpdateSearchParametersNotInDb()
    {
        final JiraServiceContext context = createServiceContext();

        when(mockSearchRequestManager.getSearchRequestOwner(filter1.getId())).thenReturn(null);


        final SearchRequestService service = createSearchRequestService();

        service.updateSearchParameters(context, filter1);

        final ErrorCollection errorCollection = context.getErrorCollection();
        assertTrue(errorCollection.hasAnyErrors());
        assertTrue(errorCollection.getErrorMessages().contains("admin.errors.filters.not.saved"));
    }

    @Test
    public void testUpdateSearchParametersNotOwnerInDb()
    {
        final JiraServiceContext context = createServiceContext();

        when(mockSearchRequestManager.getSearchRequestOwner(filter1.getId())).thenReturn(new MockApplicationUser("notMe"));

        final SearchRequestService service = createSearchRequestService();

        service.updateSearchParameters(context, filter1);

        final ErrorCollection errorCollection = context.getErrorCollection();
        assertTrue(errorCollection.hasAnyErrors());
        assertTrue(errorCollection.getErrorMessages().contains("admin.errors.filters.must.be.owner"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateFilterAsFavouriteShouldNotAcceptANullServiceContext()
    {
        final SearchRequestService service = createSearchRequestService();
        service.createFilter(null, filter1, true);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateFilterAsFavouriteShouldNotAcceptANullSearchRequest()
    {
        final SearchRequestService service = createSearchRequestService();
        service.createFilter(createServiceContext(), null, true);
    }

    @Test
    public void testCreateFilterFavouriteAsAnonymous()
    {
        final JiraServiceContext context = createContext(null);

        when(mockShareTypeValidatorUtils.isValidSharePermission(context, filter1)).thenReturn(true);

        final SearchRequestService service = createSearchRequestService();

        service.createFilter(context, filter1, true);

        final ErrorCollection errorCollection = context.getErrorCollection();
        assertTrue(errorCollection.hasAnyErrors());
        assertTrue(errorCollection.getErrorMessages().contains("admin.errors.filters.owned.anonymous.user"));
    }

    @Test
    public void testCreateFilterFavouriteNotAsOwner()
    {
        final JiraServiceContext context = createContext(ImmutableUser.newUser().name("otherUser").toUser());

        when(mockShareTypeValidatorUtils.isValidSharePermission(context, filter1)).thenReturn(true);

        final SearchRequestService service = createSearchRequestService();

        service.createFilter(context, filter1, true);

        final ErrorCollection errorCollection = context.getErrorCollection();
        assertTrue(errorCollection.hasAnyErrors());
        assertTrue(errorCollection.getErrorMessages().contains("admin.errors.filters.must.be.owner"));
    }

    @Test
    public void testCreateFilterFavouriteWithAddFavourite() throws PermissionException
    {
        final JiraServiceContext ctx = createServiceContext();

        when(mockShareTypeValidatorUtils.isValidSharePermission(ctx, filter3)).thenReturn(true);

        when(mockSearchRequestManager.create(filter3)).thenReturn(filter2);


        mockFavouritesManager.addFavourite(loggedInUser, filter2);

        final SearchRequestService service = createSearchRequestService();
        final SearchRequest resultFilter = service.createFilter(ctx, filter3, true);
        assertSame(filter2, resultFilter);
        assertFalse(ctx.getErrorCollection().hasAnyErrors());
    }

    @Test
    public void testCreateFilterFavouriteWithAddFavouriteError() throws PermissionException
    {
        final JiraServiceContext ctx = createServiceContext();

        when(mockShareTypeValidatorUtils.isValidSharePermission(ctx, filter3)).thenReturn(true);

        when(mockSearchRequestManager.create(filter3)).thenReturn(filter2);


        doThrow(new PermissionException("ahhhh")).when(mockFavouritesManager).addFavourite(loggedInUser, filter2);

        final SearchRequestService service = createSearchRequestService();
        final SearchRequest resultFilter = service.createFilter(ctx, filter3, true);
        assertSame(filter2, resultFilter);
        assertTrue(ctx.getErrorCollection().hasAnyErrors());
    }

    @Test
    public void testCreateFilterFavouriteWithRemoveFavourite()
    {
        final JiraServiceContext ctx = createServiceContext();

        when(mockShareTypeValidatorUtils.isValidSharePermission(ctx, filter3)).thenReturn(true);

        when(mockSearchRequestManager.create(filter3)).thenReturn(filter2);


        mockFavouritesManager.removeFavourite(loggedInUser, filter2);

        final SearchRequestService service = createSearchRequestService();
        final SearchRequest resultFilter = service.createFilter(ctx, filter3, false);
        assertSame(filter2, resultFilter);
        assertFalse(ctx.getErrorCollection().hasAnyErrors());
    }

    @Test(expected = IllegalArgumentException.class)
    public void updateFilterAsFavouriteShouldNotAcceptANullServiceContext()
    {
        final SearchRequestService service = createSearchRequestService();
        service.updateFilter(null, filter1, true);
    }

    @Test(expected = IllegalArgumentException.class)
    public void updateFilterAsFavouriteShouldNotAcceptANullSearchContext()
    {
        final SearchRequestService service = createSearchRequestService();
        service.updateFilter(createServiceContext(), null, true);
    }

    @Test
    public void testUpdateFilterFavouriteNotSaved()
    {
        final JiraServiceContext context = createServiceContext();

        when(mockShareTypeValidatorUtils.isValidSharePermission(context, filter2)).thenReturn(true);


        final SearchRequestService service = createSearchRequestService();

        service.updateFilter(context, filter2, true);

        final ErrorCollection errorCollection = context.getErrorCollection();
        assertTrue(errorCollection.hasAnyErrors());
        assertTrue(errorCollection.getErrorMessages().contains("admin.errors.filters.not.saved"));
    }

    @Test
    public void testUpdateFilterFavouriteAsAnonymous()
    {
        final JiraServiceContext context = createContext(null);

        when(mockShareTypeValidatorUtils.isValidSharePermission(context, filter1)).thenReturn(true);

        final SearchRequestService service = createSearchRequestService();

        service.updateFilter(context, filter1, true);

        final ErrorCollection errorCollection = context.getErrorCollection();
        assertTrue(errorCollection.hasAnyErrors());
        assertTrue(errorCollection.getErrorMessages().contains("admin.errors.filters.owned.anonymous.user"));
    }

    @Test
    public void testUpdateFilterFavouriteNotNewOwner()
    {
        final JiraServiceContext context = createContext(ImmutableUser.newUser().name("notMe").toUser());

        when(mockShareTypeValidatorUtils.isValidSharePermission(context, filter1)).thenReturn(true);

        final SearchRequestService service = createSearchRequestService();

        service.updateFilter(context, filter1, true);

        final ErrorCollection errorCollection = context.getErrorCollection();
        assertTrue(errorCollection.hasAnyErrors());
        assertTrue(errorCollection.getErrorMessages().contains("admin.errors.filters.must.be.owner"));
    }

    @Test
    public void testUpdateFilterFavouriteNotInDb()
    {
        final JiraServiceContext context = createServiceContext();

        when(mockSearchRequestManager.getSearchRequestOwner(filter1.getId())).thenReturn(null);

        when(mockShareTypeValidatorUtils.isValidSharePermission(context, filter1)).thenReturn(true);


        final SearchRequestService service = createSearchRequestService();

        service.updateFilter(context, filter1, false);

        final ErrorCollection errorCollection = context.getErrorCollection();
        assertTrue(errorCollection.hasAnyErrors());
        assertTrue(errorCollection.getErrorMessages().contains("admin.errors.filters.not.saved"));
    }

    @Test
    public void testUpdateFilterFavouriteWithAddFavourite() throws PermissionException
    {
        final JiraServiceContext ctx = createServiceContext();

        when(mockSearchRequestManager.getSearchRequestOwner(filter1.getId())).thenReturn(filter1.getOwner());

        when(mockSearchRequestManager.update(filter1)).thenReturn(filter1);

        when(mockShareTypeValidatorUtils.isValidSharePermission(ctx, filter1)).thenReturn(true);


        mockFavouritesManager.addFavourite(loggedInUser, filter1);

        final SearchRequestService service = createSearchRequestService();
        final SearchRequest resultFilter = service.updateFilter(ctx, filter1, true);
        assertSame(filter1, resultFilter);
        assertFalse(ctx.getErrorCollection().hasAnyErrors());
    }

    @Test
    public void testUpdateFilterFavouriteWithAddFavouriteError() throws PermissionException
    {
        final JiraServiceContext ctx = createServiceContext();

        when(mockSearchRequestManager.getSearchRequestOwner(filter1.getId())).thenReturn(filter1.getOwner());

        when(mockSearchRequestManager.update(filter1)).thenReturn(filter1);

        when(mockShareTypeValidatorUtils.isValidSharePermission(ctx, filter1)).thenReturn(true);


        doThrow(new PermissionException("ahhhh")).when(mockFavouritesManager).addFavourite(loggedInUser, filter1);

        final SearchRequestService service = createSearchRequestService();
        final SearchRequest resultFilter = service.updateFilter(ctx, filter1, true);
        assertSame(filter1, resultFilter);
        assertTrue(ctx.getErrorCollection().hasAnyErrors());
    }

    @Test
    public void testUpdateFilterFavouriteWithRemoveFavourite() throws PermissionException
    {
        final JiraServiceContext ctx = createServiceContext();

        when(mockSearchRequestManager.getSearchRequestOwner(filter1.getId())).thenReturn(filter1.getOwner());

        when(mockSearchRequestManager.update(filter1)).thenReturn(filter1);

        when(mockShareTypeValidatorUtils.isValidSharePermission(ctx, filter1)).thenReturn(true);


        mockFavouritesManager.removeFavourite(loggedInUser, filter1);

        final SearchRequestService service = createSearchRequestService();
        final SearchRequest resultFilter = service.updateFilter(ctx, filter1, false);
        assertSame(filter1, resultFilter);
        assertFalse(ctx.getErrorCollection().hasAnyErrors());
    }

    @Test
    public void testSearchParameters()
    {
        final SharedEntitySearchParametersBuilder parameters = new SharedEntitySearchParametersBuilder();
        final SharedEntitySearchParameters sharedEntitySearchParameters = parameters.toSearchParameters();

        final SharedEntitySearchResult expectedResult = new SharedEntitySearchResult(new MockCloseableIterable(
            Collections.emptyList()), true, 0);

        when(mockSearchRequestManager.search(sharedEntitySearchParameters, loggedInUser, 0, 10)).thenReturn(expectedResult);

        final JiraServiceContext ctx = createServiceContext();
        final SearchRequestService service = createSearchRequestService();
        final SharedEntitySearchResult actualResult = service.search(ctx, sharedEntitySearchParameters, 0, 10);

        assertSame(expectedResult, actualResult);
    }

    @Test
    public void testSearchNullUser()
    {
        final ApplicationUser aNullUser = null;
        final SharedEntitySearchParametersBuilder parameters = new SharedEntitySearchParametersBuilder();
        final SharedEntitySearchParameters sharedEntitySearchParameters = parameters.toSearchParameters();

        final SharedEntitySearchResult expectedResult = new SharedEntitySearchResult(new MockCloseableIterable(
            Collections.emptyList()), true, 0);

        when(mockSearchRequestManager.search(sharedEntitySearchParameters, aNullUser, 0, 10)).thenReturn(expectedResult);

        final JiraServiceContext ctx = createContext(null);
        final SearchRequestService service = createSearchRequestService();
        final SharedEntitySearchResult actualResult = service.search(ctx, sharedEntitySearchParameters, 0, 10);

        assertSame(expectedResult, actualResult);
    }

    @Test(expected = IllegalArgumentException.class)
    public void searchShouldNotAcceptNullSearchParameters()
    {
        final DefaultSearchRequestService service = createSearchRequestService();
        service.search(createServiceContext(), null, 0, 10);
    }

    @Test (expected = IllegalArgumentException.class)
    public void searchShouldNotAcceptAnInvalidPosition()
    {
        final DefaultSearchRequestService service = createSearchRequestService();
        service.search(createServiceContext(), new SharedEntitySearchParametersBuilder().toSearchParameters(), -1, 10);
    }

    @Test (expected = IllegalArgumentException.class)
    public void searchShouldNotAcceptZeroWidth()
    {
        final DefaultSearchRequestService service = createSearchRequestService();
        service.search(createServiceContext(), new SharedEntitySearchParametersBuilder().toSearchParameters(), 0, 0);
    }

    @Test (expected = IllegalArgumentException.class)
    public void searchShouldNotAcceptNegativeWidth()
    {
        final DefaultSearchRequestService service = createSearchRequestService();
        service.search(createServiceContext(), new SharedEntitySearchParametersBuilder().toSearchParameters(), 0, -1);
    }

    @Test (expected = IllegalArgumentException.class)
    public void searchShouldNotAcceptANullServiceContext()
    {
        final DefaultSearchRequestService service = createSearchRequestService();
        service.search(null, new SharedEntitySearchParametersBuilder().toSearchParameters(), 0, 10);
    }

    @Test
    public void testValidateSearchAnyShareTypeInvalidUserWhenNotLoggedIn() throws Exception
    {
        final JiraServiceContext ctx = createContext(null);
        final SearchRequestService service = createSearchRequestService();

        final SharedEntitySearchParametersBuilder searchTemplate = new SharedEntitySearchParametersBuilder();
        searchTemplate.setUserName("fred");

        service.validateForSearch(ctx, searchTemplate.toSearchParameters());
        assertFalse(ctx.getErrorCollection().hasAnyErrors());
    }

    @Test
    public void testValidateSearchAnyShareTypeInvalidUserWhenNoUserPickerPermission() throws Exception
    {
        final String searchUserName = "fred";
        when(mockUserUtil.userExists(searchUserName)).thenReturn(false);
        when(mockPermissionManager.hasPermission(Permissions.USER_PICKER, loggedInUser)).thenReturn(false);

        final JiraServiceContext ctx = createServiceContext();
        final SearchRequestService service = createSearchRequestService();

        final SharedEntitySearchParametersBuilder searchTemplate = new SharedEntitySearchParametersBuilder();
        searchTemplate.setUserName(null);

        service.validateForSearch(ctx, searchTemplate.toSearchParameters());
        assertFalse(ctx.getErrorCollection().hasAnyErrors());
    }

    @Test
    public void testValidateSearchAnyShareTypeValidUser() throws Exception
    {
        final String searchUserName = "fred";
        when(mockUserUtil.userExists(searchUserName)).thenReturn(true);

        final JiraServiceContext ctx = createContext(null);
        final SearchRequestService service = createSearchRequestService();

        final SharedEntitySearchParametersBuilder searchTemplate = new SharedEntitySearchParametersBuilder();
        searchTemplate.setUserName(searchUserName);

        service.validateForSearch(ctx, searchTemplate.toSearchParameters());
        assertFalse(ctx.getErrorCollection().hasAnyErrors());
    }

    @Test
    public void testValidateSearchCallsThroughToValidators() throws Exception
    {
        final JiraServiceContext ctx = createContext(null);

        when(mockShareTypeValidatorUtils.isValidSearchParameter(ctx, PrivateShareTypeSearchParameter.PRIVATE_PARAMETER)).
                thenReturn(true);

        final SharedEntitySearchParametersBuilder searchTemplate = new SharedEntitySearchParametersBuilder();
        searchTemplate.setShareTypeParameter(PrivateShareTypeSearchParameter.PRIVATE_PARAMETER);

        final SearchRequestService service = createSearchRequestService();

        service.validateForSearch(ctx, searchTemplate.toSearchParameters());
    }

    /**
     * Useful helper that creates a string filled with 'x' thats greater than the allowed length for filter names.
     *
     * @return The really long string
     */
    private static String createFilternameThatsTooLong()
    {
        final char[] nameThatsTooLong = new char[500];
        Arrays.fill(nameThatsTooLong, 'x');
        return new String(nameThatsTooLong);
    }

    private static final class Stubs
    {
        /**
         * Instantiantes an I18nHelper that always returns the passed in key.
         * @return an I18nHelper that always returns the passed in key.
         */
        private static I18nHelper keyReturningI18nHelper()
        {
            final I18nHelper mockI18nHelper = mock(I18nHelper.class);
            when(mockI18nHelper.getText(anyString())).thenAnswer(new Answers.ByReturningTheFirstArgument());
            when(mockI18nHelper.getText(anyString(), anyString())).thenAnswer(new Answers.ByReturningTheFirstArgument());
            return mockI18nHelper;
        }

        private static final class SearchRequests
        {
            static SearchRequest unsaved()
            {
                return new SearchRequest(new QueryImpl(), (ApplicationUser) null, "filter1", "filterter1Desc", null, 0L);
            }
        }

        private static class Answers
        {
            private static class ByReturningTheFirstArgument implements Answer<Object>
            {
                @Override
                public Object answer(InvocationOnMock invocation) throws Throwable
                {
                    return invocation.getArguments()[0];
                }
            }
        }
    }
}
