package com.atlassian.jira.issue.search;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.issue.fields.layout.column.ColumnLayoutManager;
import com.atlassian.jira.issue.fields.layout.column.ColumnLayoutStorageException;
import com.atlassian.jira.issue.subscription.SubscriptionManager;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.sharing.IndexableSharedEntity;
import com.atlassian.jira.sharing.ShareManager;
import com.atlassian.jira.sharing.SharePermission;
import com.atlassian.jira.sharing.SharePermissionImpl;
import com.atlassian.jira.sharing.SharedEntity;
import com.atlassian.jira.sharing.SharedEntity.SharePermissions;
import com.atlassian.jira.sharing.index.MockSharedEntityIndexer;
import com.atlassian.jira.sharing.type.GlobalShareType;
import com.atlassian.jira.sharing.type.GroupShareType;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.MockApplicationUser;
import com.atlassian.jira.user.util.MockUserManager;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.util.collect.EnclosedIterable;
import com.atlassian.jira.util.collect.MockCloseableIterable;
import com.atlassian.query.Query;
import com.atlassian.query.QueryImpl;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operator.Operator;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.ofbiz.core.entity.GenericValue;

import static com.atlassian.jira.issue.search.TestDefaultSearchRequestManager.Answers.byReturningArgumentNo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link DefaultSearchRequestManager}
 *
 * @since v3.13
 */
public class TestDefaultSearchRequestManager
{
    private static final String NICKS_FILTER = "nicks filter";
    private static final String USER_NAME = "admin";


    private SearchRequestStore mockSearchRequestStore;
    private MockApplicationUser user;

    private ShareManager mockShareManager;
    private SharePermission perm1;
    private SharePermission perm2;
    private SearchRequest searchRequest1;
    private SearchRequest searchRequest2;
    private SearchRequest searchRequest3;

    private SubscriptionManager mockSubscriptionManager;

    private ColumnLayoutManager mockColumnLayoutManager;
    private SearchService mockSearchService;
    private UserUtil mockUserUtil;

    @Before
    public void setUp() throws Exception
    {

        mockSearchRequestStore = mock(SearchRequestStore.class);
        mockShareManager = mock(ShareManager.class);
        mockSubscriptionManager = mock(SubscriptionManager.class);
        mockColumnLayoutManager = mock(ColumnLayoutManager.class);
        mockSearchService =  mock(SearchService.class);
        mockUserUtil = mock(UserUtil.class);

        MockUserManager userManager = new MockUserManager();
        user = new MockApplicationUser(USER_NAME);
        userManager.addUser(user);

        perm1 = new SharePermissionImpl(GroupShareType.TYPE, "jira-user", null);
        perm2 = new SharePermissionImpl(GlobalShareType.TYPE, null, null);

        new MockComponentWorker().addMock(UserManager.class, userManager).init();
        searchRequest1 = new SearchRequest(new QueryImpl(), user, "one", "one description", 1L, 0L);
        searchRequest2 = new SearchRequest(new QueryImpl(), user, "two", "two description", 2L, 0L);
        searchRequest3 = new SearchRequest(new QueryImpl(), user, "three", "three description", 3L, 0L);

        MockComponentWorker mockComponentWorker = new MockComponentWorker();
        mockComponentWorker.addMock(UserUtil.class, mockUserUtil).addMock(UserManager.class, userManager);
        ComponentAccessor.initialiseWorker(mockComponentWorker);
    }

    private DefaultSearchRequestManager createDefaultSearchRequestManager()
    {
        return new DefaultSearchRequestManager(mockColumnLayoutManager, mockSubscriptionManager, mockShareManager, mockSearchRequestStore,
            new MockSharedEntityIndexer(), mockSearchService, mockUserUtil);
    }

    @Test
    public void testGetAllDelegatesToTheUnderlyingSearchRequestStore()
    {
        final EnclosedIterable<SearchRequest> expectedAllSearchRequests =
                new MockCloseableIterable<SearchRequest>(Collections.<SearchRequest> emptyList());

        when(mockSearchRequestStore.getAll()).thenReturn(expectedAllSearchRequests);

        final DefaultSearchRequestManager searchRequestManager = createDefaultSearchRequestManager();

        assertNotNull(searchRequestManager.getAll());
        verify(mockSearchRequestStore).getAll();
    }

    @Test
    public void testGetAllIndexableSharedEntitiesDelegatesToTheUnderlyingStore()
    {
        final EnclosedIterable<IndexableSharedEntity<SearchRequest>> expectedIndexableSharedEntities =
                new MockCloseableIterable<IndexableSharedEntity<SearchRequest>>
                        (Collections.<IndexableSharedEntity<SearchRequest>> emptyList());

        when(mockSearchRequestStore.getAllIndexableSharedEntities()).thenReturn(expectedIndexableSharedEntities);

        final DefaultSearchRequestManager searchRequestManager = createDefaultSearchRequestManager();

        assertNotNull(searchRequestManager.getAllIndexableSharedEntities());
        verify(mockSearchRequestStore).getAllIndexableSharedEntities();
    }

    @Test
    public void testSetSanitisedQuerySanitisedIsSame() throws Exception
    {
        final Query query = mock(Query.class);

        final SearchRequest sr = mock(SearchRequest.class);
        when(sr.getQuery()).thenReturn(query);

        when(mockSearchService.sanitiseSearchQuery(user.getDirectoryUser(), query)).thenReturn(query);

        final DefaultSearchRequestManager searchRequestManager = createDefaultSearchRequestManager();
        searchRequestManager.setSanitisedQuery(user, sr);

        assertEquals(query, sr.getQuery());
    }

    @Test
    public void testSetSanitisedQuerySanitisedIsDifferentIsNotModified() throws Exception
    {
        final Query queryOld = new QueryImpl(new TerminalClauseImpl("field", Operator.EQUALS, "value"));
        final Query querySanitised = new QueryImpl(new TerminalClauseImpl("field", Operator.EQUALS, "valueSanitised"));

        final SearchRequest searchRequestForOldQuery = mock(SearchRequest.class);
        when(searchRequestForOldQuery.getQuery()).thenReturn(queryOld);

        when(mockSearchService.sanitiseSearchQuery(user.getDirectoryUser(), queryOld)).thenReturn(querySanitised);

        when(searchRequestForOldQuery.isModified()).thenReturn(false);

        final DefaultSearchRequestManager searchRequestManager = createDefaultSearchRequestManager();
        searchRequestManager.setSanitisedQuery(user, searchRequestForOldQuery);

        verify(searchRequestForOldQuery).setModified(false);
    }

    @Test
    public void testSetSanitisedQuerySanitisedIsDifferentIsModified() throws Exception
    {

        final Query queryOld = new QueryImpl(new TerminalClauseImpl("field", Operator.EQUALS, "value"));
        final Query querySanitised = new QueryImpl(new TerminalClauseImpl("field", Operator.EQUALS, "valueSanitised"));

        final SearchRequest searchRequestForOldQuery = mock(SearchRequest.class);
        when(searchRequestForOldQuery.getQuery()).thenReturn(queryOld);

        when(mockSearchService.sanitiseSearchQuery(user.getDirectoryUser(), queryOld)).thenReturn(querySanitised);

        when(searchRequestForOldQuery.isModified()).thenReturn(true);

        final DefaultSearchRequestManager searchRequestManager = createDefaultSearchRequestManager();
        searchRequestManager.setSanitisedQuery(user, searchRequestForOldQuery);

        verify(searchRequestForOldQuery).setModified(true);
    }

    @Test
    public void testGetOwnerForMissingUserNotNull()
    {
        MockApplicationUser missingUser = new MockApplicationUser("key-and-dummy-name", "old-name-that-is-gone");
        SearchRequest searchRequestWithMissingOwner = new SearchRequest(new QueryImpl(), missingUser, "User is Missing", "User is Missing");
        when(mockSearchRequestStore.getSearchRequest(1L)).thenReturn(searchRequestWithMissingOwner);
        final DefaultSearchRequestManager searchRequestManager = createDefaultSearchRequestManager();
        ApplicationUser dummyUser = searchRequestManager.getSearchRequestOwner(1L);
        assertNotNull("Missing search owner should result in a dummy user, not a null", dummyUser);
        assertNotSame("Test in error: search owner should be inacessible to simulate non-existent user", "old-name-that-is-gone", dummyUser.getUsername());
    }

    @Test
    public void testGetAllOwnedSearchRequestsNullUser()
    {
        when(mockSearchRequestStore.getAllOwnedSearchRequests((ApplicationUser) null)).thenReturn(Collections.<SearchRequest>emptyList());
        final SearchRequestManager searchRequestManager = createDefaultSearchRequestManager();
        final Collection results = searchRequestManager.getAllOwnedSearchRequests((ApplicationUser) null);
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    @Test
    public void testGetAllOwnedSearchRequestsNullReturned()
    {
        when(mockSearchRequestStore.getAllOwnedSearchRequests(user)).thenReturn(null);

        final SearchRequestManager searchRequestManager = createDefaultSearchRequestManager();

        final Collection results = searchRequestManager.getAllOwnedSearchRequests(user);
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    @Test
    public void testGetAllOwnedSearchRequestsEmptyList()
    {
        when(mockSearchRequestStore.getAllOwnedSearchRequests(user)).thenReturn(Collections.<SearchRequest>emptyList());

        final SearchRequestManager searchRequestManager = createDefaultSearchRequestManager();

        final Collection results = searchRequestManager.getAllOwnedSearchRequests(user);
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    @Test
    public void getAllOwnedSearchRequestsSetsTheSharePermissionsOnTheSearchRequestOwnedByTheUser()
    {
        final List<SearchRequest> expectedSearchRequests = ImmutableList.of(searchRequest1, searchRequest2, searchRequest3);

        when(mockSearchRequestStore.getAllOwnedSearchRequests(user)).thenReturn(expectedSearchRequests);

        final SharePermissions searchRequest2Perm = new SharePermissions(ImmutableSet.of(perm2));
        final SharePermissions searchRequest1Perm = new SharePermissions(ImmutableSet.of(perm1));

        when(mockShareManager.getSharePermissions(searchRequest1)).thenReturn(searchRequest1Perm);
        when(mockShareManager.getSharePermissions(searchRequest2)).thenReturn(searchRequest2Perm);
        when(mockShareManager.getSharePermissions(searchRequest3)).thenReturn(SharePermissions.PRIVATE);

        when(mockSearchService.sanitiseSearchQuery(eq(user.getDirectoryUser()), isA(Query.class))).thenAnswer(byReturningArgumentNo(2));
        final SearchRequestManager searchRequestManager = createDefaultSearchRequestManager();

        final Collection<SearchRequest> results = searchRequestManager.getAllOwnedSearchRequests(user);
        assertNotNull(results);
        assertEquals(3, results.size());

        final Iterator<SearchRequest> iterator = results.iterator();
        SearchRequest result = iterator.next();
        assertEquals(searchRequest1, result);
        assertEquals(searchRequest1Perm, result.getPermissions());

        result = iterator.next();
        assertEquals(searchRequest2, result);
        assertEquals(searchRequest2Perm, result.getPermissions());

        result = iterator.next();
        assertEquals(searchRequest3, result);
        assertEquals(SharePermissions.PRIVATE, result.getPermissions());
    }

    @Test
    public void getRequestAuthorAndNameReturnsNullForANullUser()
    {
        when(mockSearchRequestStore.getRequestByAuthorAndName(null, NICKS_FILTER)).thenReturn(null);

        final SearchRequestManager searchRequestManager = createDefaultSearchRequestManager();

        assertNull(searchRequestManager.getOwnedSearchRequestByName((ApplicationUser) null, NICKS_FILTER));
    }

    @Test(expected = IllegalArgumentException.class)
    public void getRequestAuthorAndNameShouldNotAcceptANullName()
    {
        final SearchRequestManager searchRequestManager = createDefaultSearchRequestManager();
        searchRequestManager.getOwnedSearchRequestByName(user, null);
    }

    @Test
    public void getRequestAuthorAndNameShouldReturnNullGivenThatThereAreNoStoredRequestsForThatUserWithThatName()
    {
        when(mockSearchRequestStore.getRequestByAuthorAndName(user, NICKS_FILTER)).thenReturn(null);

        final SearchRequestManager searchRequestManager = createDefaultSearchRequestManager();

        assertNull(searchRequestManager.getOwnedSearchRequestByName(user, NICKS_FILTER));
    }

    @Test
    public void getRequestAuthorAndNameReturnsASearchRequestWithAPrivateSharePermissionWhenTheRequestHasNotBeenShared()
    {
        when(mockSearchRequestStore.getRequestByAuthorAndName(user, NICKS_FILTER)).thenReturn(searchRequest1);
        when(mockShareManager.getSharePermissions(searchRequest1)).thenReturn(SharePermissions.PRIVATE);

        when(mockSearchService.sanitiseSearchQuery(eq(user.getDirectoryUser()), isA(Query.class))).thenAnswer(byReturningArgumentNo(2));

        final SearchRequestManager searchRequestManager = createDefaultSearchRequestManager();

        final SearchRequest actualSearchRequest = searchRequestManager.getOwnedSearchRequestByName(user, NICKS_FILTER);

        assertNotNull(actualSearchRequest);
        assertEquals(searchRequest1, actualSearchRequest);
        assertEquals(SharePermissions.PRIVATE, actualSearchRequest.getPermissions());
    }

    @Test
    public void getRequestAuthorAndNameReturnsAllStoredSharesForAGivenSharedSearchRequest()
    {
        final SharePermissions expectedSharePermissions = new SharePermissions(ImmutableSet.of(perm2));

        when(mockSearchRequestStore.getRequestByAuthorAndName(user, NICKS_FILTER)).thenReturn(searchRequest1);
        when(mockShareManager.getSharePermissions(searchRequest1)).thenReturn(expectedSharePermissions);

        when(mockSearchService.sanitiseSearchQuery(eq(user.getDirectoryUser()), isA(Query.class))).thenAnswer(byReturningArgumentNo(2));
        final SearchRequestManager searchRequestManager = createDefaultSearchRequestManager();

        final SearchRequest request = searchRequestManager.getOwnedSearchRequestByName(user, NICKS_FILTER);
        assertNotNull(request);
        assertEquals(searchRequest1, request);
        assertEquals(expectedSharePermissions, request.getPermissions());
    }

    @Test(expected = IllegalArgumentException.class)
    public void getSearchRequestShouldNotAcceptANullSearchRequestId()
    {
        final SearchRequestManager searchRequestManager = createDefaultSearchRequestManager();
        searchRequestManager.getSearchRequestById(user, null);
    }

    @Test
    public void getSearchRequestByIdReturnsNullGivenThatTheRequestHasNotBeenSharedWithTheSpecifiedUser()
    {
        when(mockSearchRequestStore.getSearchRequest(searchRequest1.getId())).thenReturn(searchRequest1);

        when(mockShareManager.isSharedWith((ApplicationUser) null, searchRequest1)).thenReturn(false);

        final SearchRequestManager searchRequestManager = createDefaultSearchRequestManager();

        final SearchRequest request = searchRequestManager.getSearchRequestById((ApplicationUser) null, searchRequest1.getId());
        assertNull(request);
    }

    @Test
    public void getSearchRequestByIdReturnsASearchRequestObjectForANullUserWhenThatRequestHasBeenSharedWithThatUser()
    {
        when(mockSearchRequestStore.getSearchRequest(searchRequest2.getId())).thenReturn(searchRequest2);

        when(mockShareManager.isSharedWith((ApplicationUser) null, searchRequest2)).thenReturn(true);

        final SharePermissions permSet2 = new SharePermissions(ImmutableSet.of(perm2));

        when(mockShareManager.getSharePermissions(searchRequest2)).thenReturn(permSet2);

        when(mockSearchService.sanitiseSearchQuery(Matchers.<User>anyObject(), isA(Query.class))).
                thenAnswer(byReturningArgumentNo(2));

        final SearchRequestManager searchRequestManager = createDefaultSearchRequestManager();

        final SearchRequest request = searchRequestManager.getSearchRequestById((ApplicationUser) null, searchRequest2.getId());

        assertNotNull(request);
        assertEquals(searchRequest2, request);
        assertEquals(permSet2, request.getPermissions());
    }

    @Test
    public void getSearchRequestByIdReturnsNullWhenTheRequestHasNotBeenSharedWithTheSpecifiedUser()
    {
        when(mockSearchRequestStore.getSearchRequest(searchRequest1.getId())).thenReturn(searchRequest1);

        when(mockShareManager.isSharedWith(user, searchRequest1)).thenReturn(false);

        final SearchRequestManager searchRequestManager = createDefaultSearchRequestManager();
        final SearchRequest request = searchRequestManager.getSearchRequestById(user, searchRequest1.getId());

        assertNull(request);
    }

    @Test
    public void getSearchRequestByIdReturnsTheRequestStoredWithThatIdWhenTheRequestHasBeenSharedWithTheSpecifiedUser()
    {
        when(mockSearchRequestStore.getSearchRequest(searchRequest1.getId())).thenReturn(searchRequest1);

        when(mockShareManager.isSharedWith(user, searchRequest1)).thenReturn(true);

        final SharePermissions permSet1 = new SharePermissions(ImmutableSet.of(perm1));

        when(mockShareManager.getSharePermissions(searchRequest1)).thenReturn(permSet1);

        when(mockSearchService.sanitiseSearchQuery(Matchers.<User>anyObject(), isA(Query.class))).
                thenAnswer(byReturningArgumentNo(2));

        final SearchRequestManager searchRequestManager = createDefaultSearchRequestManager();
        final SearchRequest request = searchRequestManager.getSearchRequestById(user, searchRequest1.getId());

        assertNotNull(request);
        assertEquals(searchRequest1, request);
        assertEquals(permSet1, request.getPermissions());
    }

    @Test(expected = IllegalArgumentException.class)
    public void createShouldNotAcceptANullSearchRequest()
    {
        final SearchRequestManager searchRequestManager = createDefaultSearchRequestManager();
        searchRequestManager.create(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createShouldNotAcceptASearchRequestWithNoOwner()
    {
        final SearchRequestManager searchRequestManager = createDefaultSearchRequestManager();
        final SearchRequest request = new SearchRequest();
        searchRequestManager.create(request);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createShouldNotAcceptASearchRequestWithNoName()
    {
        final SearchRequestManager searchRequestManager = createDefaultSearchRequestManager();
        final SearchRequest request = new SearchRequest(new QueryImpl(), user, null, "description");

        searchRequestManager.create(request);
    }

    @Test
    public void createShouldReturnASearchRequestWithNoPermissionsGivenASearchRequestThatIsPrivate()
    {
        searchRequest1.setPermissions(SharePermissions.PRIVATE);
        when(mockSearchRequestStore.create(searchRequest1)).thenReturn(searchRequest1);

        when(mockShareManager.updateSharePermissions(searchRequest1)).thenReturn(SharePermissions.PRIVATE);

        when(mockUserUtil.getUserObject(USER_NAME)).thenReturn(user.getDirectoryUser());

        when(mockSearchService.sanitiseSearchQuery(eq(user.getDirectoryUser()), isA(Query.class))).thenAnswer(byReturningArgumentNo(2));

        final SearchRequestManager searchRequestManager = createDefaultSearchRequestManager();

        final SearchRequest request = searchRequestManager.create(searchRequest1);
        assertNotNull(request);
        assertEquals(searchRequest1, request);
        assertTrue(request.getPermissions().isEmpty());
    }

    @Test
    public void createReturnsASearchRequestWithTheSharePermissionsSpecifiedForTheRequest()
    {
        final Set<SharePermission> permSet1 = ImmutableSet.of(perm1, perm2);
        final SharePermissions expectedSharePermissions = new SharePermissions(permSet1);
        searchRequest1.setPermissions(expectedSharePermissions);

        when(mockSearchRequestStore.create(searchRequest1)).thenReturn(searchRequest1);

        when(mockShareManager.updateSharePermissions(searchRequest1)).thenReturn(expectedSharePermissions);

        when(mockUserUtil.getUserObject(USER_NAME)).thenReturn(user.getDirectoryUser());

        when(mockSearchService.sanitiseSearchQuery(eq(user.getDirectoryUser()), isA(Query.class))).thenAnswer(byReturningArgumentNo(2));

        final SearchRequestManager searchRequestManager = createDefaultSearchRequestManager();

        final SearchRequest request = searchRequestManager.create(searchRequest1);

        assertNotNull(request);
        assertEquals(searchRequest1, request);
        assertEquals(expectedSharePermissions, request.getPermissions());
    }

    @Test(expected = IllegalArgumentException.class)
    public void updateShouldNotAcceptANullSearchRequest()
    {
        final SearchRequestManager searchRequestManager = createDefaultSearchRequestManager();
        searchRequestManager.update(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void updateShouldNotAcceptASearchRequestWithNoOwner()
    {
        final SearchRequestManager searchRequestManager = createDefaultSearchRequestManager();
        final SearchRequest request = new SearchRequest();
        searchRequestManager.update(request);
    }

    @Test(expected = IllegalArgumentException.class)
    public void updateShouldNotAcceptASearchRequestWithNoName()
    {
        final SearchRequestManager searchRequestManager = createDefaultSearchRequestManager();
        final SearchRequest request = new SearchRequest(new QueryImpl(), user, null, "description");

        searchRequestManager.update(request);
    }

    @Test(expected = IllegalArgumentException.class)
    public void updateShouldNotAcceptASearchRequestWithNoId()
    {
        final SearchRequestManager searchRequestManager = createDefaultSearchRequestManager();
        final SearchRequest notIdRequest = new SearchRequest(new QueryImpl(), user, "name", "description");
        searchRequestManager.update(notIdRequest);
    }

    @Test
    public void updateShouldReturnASearchRequestWithNoPermissionsGivenASearchRequestThatIsPrivate()
    {
        searchRequest3.setPermissions(SharePermissions.PRIVATE);
        when(mockSearchRequestStore.update(searchRequest3)).thenReturn(searchRequest3);

        when(mockShareManager.updateSharePermissions(searchRequest3)).thenReturn(SharePermissions.PRIVATE);

        when(mockUserUtil.getUserObject(USER_NAME)).thenReturn(user.getDirectoryUser());

        when(mockSearchService.sanitiseSearchQuery(eq(user.getDirectoryUser()), isA(Query.class))).thenAnswer(byReturningArgumentNo(2));

        final SearchRequestManager searchRequestManager = createDefaultSearchRequestManager();

        final SearchRequest request = searchRequestManager.update(searchRequest3);

        assertNotNull(request);
        assertEquals(searchRequest3, request);
        assertTrue(request.getPermissions().isEmpty());
    }

    @Test
    public void updateReturnsASearchRequestWithTheSharePermissionsSpecifiedForTheRequest()
    {
        final SharePermissions expectedSharePermissions = new SharePermissions(ImmutableSet.of(perm2));
        searchRequest3.setPermissions(expectedSharePermissions);

        when(mockSearchRequestStore.update(searchRequest3)).thenReturn(searchRequest3);

        when(mockShareManager.updateSharePermissions(searchRequest3)).thenReturn(expectedSharePermissions);

        when(mockUserUtil.getUserObject(USER_NAME)).thenReturn(user.getDirectoryUser());

        when(mockSearchService.sanitiseSearchQuery(eq(user.getDirectoryUser()), isA(Query.class))).thenAnswer(byReturningArgumentNo(2));
        final SearchRequestManager searchRequestManager = createDefaultSearchRequestManager();

        final SearchRequest request = searchRequestManager.update(searchRequest3);

        assertNotNull(request);
        assertEquals(searchRequest3, request);
        assertEquals(expectedSharePermissions, request.getPermissions());
    }

    @Test(expected = IllegalArgumentException.class)
    public void deleteShouldNotAcceptANullSearchRequestId()
    {
        final SearchRequestManager searchRequestManager = createDefaultSearchRequestManager();
        searchRequestManager.delete(null);
        fail("Should not accept null id.");
    }

    @Test
    public void testDeleteDoesNotDeleteTheRequestFromTheStoreGivenANonExistingSearchRequest()
    {
        when(mockSearchRequestStore.getSearchRequest(searchRequest1.getId())).thenReturn(null);

        final SearchRequestManager manager = createDefaultSearchRequestManager();
        manager.delete(searchRequest1.getId());

        verify(mockSearchRequestStore, never()).delete(searchRequest1.getId());
        verify(mockShareManager, never()).deletePermissions(searchRequest1);
    }

    @Test
    public void deletingAnExistingSearchRequestShouldRemoveItFromTheStore() throws Exception
    {
        final Long subscriptionId1 = 2L;
        final Long subscriptionId2 = 3L;
        final MockGenericValue subscriptionGv1 = new MockGenericValue("Subscription", ImmutableMap.of("id", subscriptionId1));
        final MockGenericValue subscriptionGv2 = new MockGenericValue("Subscription", ImmutableMap.of("id", subscriptionId2));

        when(mockSearchRequestStore.getSearchRequest(searchRequest1.getId())).thenReturn(searchRequest1);

        when(mockSubscriptionManager.getAllSubscriptions(searchRequest1.getId())).
                thenReturn(ImmutableList.<GenericValue>of(subscriptionGv1, subscriptionGv2));


        when(mockColumnLayoutManager.hasColumnLayout(searchRequest1)).thenReturn(true);

        final SearchRequestManager searchRequestManager = createDefaultSearchRequestManager();

        searchRequestManager.delete(searchRequest1.getId());

        verify(mockSubscriptionManager).deleteSubscription(subscriptionId1);
        verify(mockSubscriptionManager).deleteSubscription(subscriptionId2);
        verify(mockColumnLayoutManager).restoreSearchRequestColumnLayout(searchRequest1);
        verify(mockShareManager).deletePermissions(searchRequest1);
        verify(mockSearchRequestStore).delete(searchRequest1.getId());
    }

    @Test
    public void deleteDoesNotRestoreTheColumnLayoutWhenTheSearchRequestHasNoLayout() throws Exception
    {
        final Long subscriptionId1 = 2L;
        final MockGenericValue subscriptionGv1 = new MockGenericValue("Subscription", ImmutableMap.of("id", subscriptionId1));

        when(mockSearchRequestStore.getSearchRequest(searchRequest1.getId())).thenReturn(searchRequest1);

        when(mockSubscriptionManager.getAllSubscriptions(searchRequest1.getId())).
                thenReturn(ImmutableList.<GenericValue>of(subscriptionGv1));

        when(mockColumnLayoutManager.hasColumnLayout(searchRequest1)).thenReturn(false);

        final SearchRequestManager searchRequestManager = createDefaultSearchRequestManager();

        searchRequestManager.delete(searchRequest1.getId());

        verify(mockColumnLayoutManager, never()).restoreSearchRequestColumnLayout(searchRequest1);
    }

    @Test
    public void deleteDoesNotDeleteSubscriptionsWhenTheSearchRequestHasNotBeenSubscribedTo() throws Exception
    {
        when(mockSearchRequestStore.getSearchRequest(searchRequest1.getId())).thenReturn(searchRequest1);

        when(mockSubscriptionManager.getAllSubscriptions(searchRequest1.getId())).thenReturn(null);

        final SearchRequestManager searchRequestManager = createDefaultSearchRequestManager();

        searchRequestManager.delete(searchRequest1.getId());

        verify(mockSubscriptionManager, never()).deleteSubscription(anyLong());
    }

    @Test(expected = DataAccessException.class)
    public void deleteThrowsADataAccessExceptionWhenAnExceptionIsThrownFromTheSubscriptionManager() throws Exception
    {
        when(mockSearchRequestStore.getSearchRequest(searchRequest1.getId())).thenReturn(searchRequest1);
        doThrow(new RuntimeException()).when(mockSubscriptionManager).getAllSubscriptions(searchRequest1.getId());

        final SearchRequestManager searchRequestManager = createDefaultSearchRequestManager();

        searchRequestManager.delete(searchRequest1.getId());
    }

    @Test(expected = DataAccessException.class)
    public void deleteThrowsADataAccessExceptionWhenQueryingForTheColumnLayoutOfTheRequestThrowsAColumnStorageException()
            throws Exception
    {
        when(mockSearchRequestStore.getSearchRequest(searchRequest1.getId())).thenReturn(searchRequest1);

        doThrow(new ColumnLayoutStorageException()).when(mockColumnLayoutManager).hasColumnLayout(searchRequest1);

        final SearchRequestManager searchRequestManager = createDefaultSearchRequestManager();

        searchRequestManager.delete(searchRequest1.getId());
    }

    @Test(expected = IllegalArgumentException.class)
    public void adjustFavouriteCountShouldNotAcceptANullSearchRequest()
    {
        final SearchRequestManager manager = createDefaultSearchRequestManager();
        manager.adjustFavouriteCount(null, 1);
    }

    @Test
    public void adjustFavouriteCountStoresTheNewCountWhenAdustingByAPositiveNumber()
    {
        final SearchRequestManager manager = createDefaultSearchRequestManager();
        manager.adjustFavouriteCount(searchRequest1, 10);
        verify(mockSearchRequestStore).adjustFavouriteCount(searchRequest1.getId(), 10);
    }

    @Test
    public void adjustFavouriteCountStoresTheNewCountWhenAdustingByANegativeNumber()
    {
        final SearchRequestManager manager = createDefaultSearchRequestManager();
        manager.adjustFavouriteCount(searchRequest2, -3);

        verify(mockSearchRequestStore).adjustFavouriteCount(searchRequest2.getId(), -3);
    }

    @Test
    public void getSearchRequestOwnerReturnsTheOwnerStoredForTheSearchRequest()
    {
        when(mockSearchRequestStore.getSearchRequest(searchRequest1.getId())).thenReturn(searchRequest1);

        final SearchRequestManager manager = createDefaultSearchRequestManager();

        assertEquals(user, manager.getSearchRequestOwner(searchRequest1.getId()));
    }

    @Test
    public void getSearchRequestOwnerReturnsNullGivenASearchRequestThatIsNotStored()
    {
        final Long searchId = Long.MAX_VALUE >>> 3;

        when(mockSearchRequestStore.getSearchRequest(searchId)).thenReturn(null);

        final SearchRequestManager manager = createDefaultSearchRequestManager();
        assertNull(manager.getSearchRequestOwner(searchId));
    }

    @Test(expected = IllegalArgumentException.class)
    public void getSearchOwnerShouldNotAcceptANullSearchRequestId()
    {
        final SearchRequestManager manager = createDefaultSearchRequestManager();
        manager.getSearchRequestOwner(null);
    }

    @Test
    public void getSharedEntityReturnsTheStoredEntityGivenItExistsInTheStoreWithTheSpecifiedId()
    {
        when(mockSearchRequestStore.getSearchRequest(searchRequest1.getId())).thenReturn(searchRequest1);
        when(mockShareManager.getSharePermissions(searchRequest1)).thenReturn(SharePermissions.PRIVATE);

        final SearchRequestManager searchRequestManager = createDefaultSearchRequestManager();
        final SharedEntity actualSearchRequest = searchRequestManager.getSharedEntity(searchRequest1.getId());

        assertEquals(searchRequest1, actualSearchRequest);
    }

    @Test
    public void getSharedEntityReturnsNullGivenThereIsNoEntityStoredWithTheSpecifiedId()
    {
        when(mockSearchRequestStore.getSearchRequest(searchRequest1.getId())).thenReturn(null);

        final SearchRequestManager searchRequestManager = createDefaultSearchRequestManager();

        assertNull(searchRequestManager.getSharedEntity(searchRequest1.getId()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void getSharedEntityShouldNotAcceptANullEntityId()
    {
        final SearchRequestManager searchRequestManager = createDefaultSearchRequestManager();
        searchRequestManager.getSharedEntity(null);
    }

    @Test
    public void getSharedEntityShouldDelegateToGetSearchRequestById()
    {
        final AtomicInteger getSearchRequestByIdCount = new AtomicInteger(0);
        final SearchRequestManager searchRequestManager = new DefaultSearchRequestManager(mockColumnLayoutManager, mockSubscriptionManager, mockShareManager,
                mockSearchRequestStore, new MockSharedEntityIndexer(), mockSearchService, mockUserUtil)
        {
            @Override
            public SearchRequest getSearchRequestById(final ApplicationUser actualUser, final Long id)
            {
                assertEquals(searchRequest3.getId(), id);
                assertEquals(actualUser, user);

                getSearchRequestByIdCount.incrementAndGet();

                return searchRequest3;
            }
        };

        final SharedEntity actualSharedEntity = searchRequestManager.getSharedEntity(user.getDirectoryUser(), searchRequest3.getId());

        assertEquals(1, getSearchRequestByIdCount.get());
        assertSame(searchRequest3, actualSharedEntity);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetSharedEntityByUserShouldNotAcceptANullEntityId()
    {
        final SearchRequestManager searchRequestManager = createDefaultSearchRequestManager();
            searchRequestManager.getSharedEntity(null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void hasPermissionShouldNotAcceptANullSharedEntity()
    {
        final SearchRequestManager searchRequestManager = createDefaultSearchRequestManager();
        searchRequestManager.hasPermissionToUse(null, null);
    }

    @Test
    public void hasPermissionsToUseReturnsTrueWhenTheSearchHasBeenSharedWithTheSpecifiedUser()
    {
        when(mockShareManager.isSharedWith(user, searchRequest2)).thenReturn(true);

        final SearchRequestManager searchRequestManager = createDefaultSearchRequestManager();
        final boolean actualResult = searchRequestManager.hasPermissionToUse(user.getDirectoryUser(), searchRequest2);

        assertTrue(actualResult);
    }

    @Test
    public void hasPermissionsToUseReturnsFalseWhenTheSearchHasNotBeenSharedWithTheSpecifiedUser()
    {
        when(mockShareManager.isSharedWith(user, searchRequest3)).thenReturn(false);

        final SearchRequestManager searchRequestManager = createDefaultSearchRequestManager();
        final boolean actualResult = searchRequestManager.hasPermissionToUse(user.getDirectoryUser(), searchRequest3);

        assertFalse(actualResult);
    }

    static class Answers
    {
        static Answer<Object> byReturningArgumentNo(int argumentNumber)
        {
            return new ByReturningAnSpecifiedArgument(argumentNumber - 1);
        }

        private static class ByReturningAnSpecifiedArgument implements Answer<Object>
        {
            private final int argumentNumber;

            public ByReturningAnSpecifiedArgument(int argumentNumber)
            {
                this.argumentNumber = argumentNumber;
            }

            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable
            {
                return invocation.getArguments()[argumentNumber];
            }
        }
    }
}
