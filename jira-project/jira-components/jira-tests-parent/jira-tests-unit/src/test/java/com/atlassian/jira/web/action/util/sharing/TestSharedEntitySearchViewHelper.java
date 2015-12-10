package com.atlassian.jira.web.action.util.sharing;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.MockJiraServiceContext;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.mock.security.MockSimpleAuthenticationContext;
import com.atlassian.jira.portal.PortalPage;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.sharing.SharePermission;
import com.atlassian.jira.sharing.SharedEntity;
import com.atlassian.jira.sharing.SharedEntityColumn;
import com.atlassian.jira.sharing.search.GroupShareTypeSearchParameter;
import com.atlassian.jira.sharing.search.ProjectShareTypeSearchParameter;
import com.atlassian.jira.sharing.search.SharedEntitySearchContext;
import com.atlassian.jira.sharing.search.SharedEntitySearchParameters;
import com.atlassian.jira.sharing.search.SharedEntitySearchParametersBuilder;
import com.atlassian.jira.sharing.search.SharedEntitySearchResult;
import com.atlassian.jira.sharing.type.GroupShareType;
import com.atlassian.jira.sharing.type.ProjectShareType;
import com.atlassian.jira.sharing.type.ShareQueryFactory;
import com.atlassian.jira.sharing.type.ShareType;
import com.atlassian.jira.sharing.type.ShareTypeFactory;
import com.atlassian.jira.sharing.type.ShareTypePermissionChecker;
import com.atlassian.jira.sharing.type.ShareTypeRenderer;
import com.atlassian.jira.sharing.type.ShareTypeValidator;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.util.collect.MockCloseableIterable;
import com.atlassian.jira.util.json.JSONArray;
import com.atlassian.jira.util.json.JSONException;
import com.atlassian.jira.util.json.JSONObject;
import com.atlassian.jira.web.bean.ShareTypeRendererBean;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.net.URLCodec;
import org.apache.commons.lang.StringUtils;
import org.easymock.classextension.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

/**
 * Test for {@link com.atlassian.jira.web.action.util.sharing.SharedEntitySearchViewHelper}.
 *
 * @since v3.13
 */
public class TestSharedEntitySearchViewHelper
{
    private static final String VIEW_PARAMETER = "view";
    private static final String VIEW_VALUE = "search";
    private static final String URL_PREFIX = "secure/ManageFilters.jspa";
    private static final String CSS_OVER = "colHeaderSortable colHeaderOver";
    private static final String CSS_LINK = "colHeaderSortable colHeaderLink";
    private static final String KEY_TEST_ERROR_MESSAGE = "test.error.message";
    private static final String APPLICATION_CONTEXT = "mycontext";

    private static final URLCodec urlCodec = new URLCodec();

    private ShareTypeFactory shareTypeFactory;
    private User testUser;

    private SharedEntity entity1;
    private SharedEntity entity2;
    private SharedEntity entity3;

    @Before
    public void setUp() throws Exception
    {
        new MockComponentWorker().init();
        shareTypeFactory = EasyMock.createMock(ShareTypeFactory.class);

        testUser = new MockUser("test");
        entity1 = new SharedEntity.Identifier(1L, PortalPage.ENTITY_TYPE, testUser);
        entity2 = new SharedEntity.Identifier(2L, PortalPage.ENTITY_TYPE, testUser);
        entity3 = new SharedEntity.Identifier(3L, PortalPage.ENTITY_TYPE, testUser);
    }

    @After
    public void tearDown() throws Exception
    {
        entity1 = entity2 = entity3 = null;
        shareTypeFactory = null;
    }

    /**
     * Ensure that the initial state of the helper is correct.
     */
    @Test
    public void testInitialState()
    {
        final SharedEntitySearchViewHelper helper = createTestHelper();

        assertNull(helper.getSearchName());
        assertNull(helper.getSearchOwnerUserName());
        assertNull(helper.getSearchShareType());
        assertNull(helper.getGroupShare());
        assertNull(helper.getPagingOffset());
        assertNull(helper.getProjectShare());
        assertNull(helper.getRoleShare());

        assertTrue(helper.isSortAscending());
        assertEquals(SharedEntitySearchViewHelper.SortColumn.DEFAULT_SORT, helper.getSortColumn());

        verifyMocks();
    }

    /**
     * Make sure project id can be correctly set.
     */
    @Test
    public void testSetProjectShareGood()
    {
        final SharedEntitySearchViewHelper helper = createTestHelper();
        helper.setProjectShare("1000");

        assertEquals("1000", helper.getProjectShare());

        verifyMocks();
    }

    /**
     * Make sure bad project id not set on error.
     */
    @Test
    public void testSetProjectShareBad()
    {
        final SharedEntitySearchViewHelper helper = createTestHelper();
        helper.setProjectShare("1000a");

        assertNull(helper.getProjectShare());

        helper.setProjectShare("1000");
        assertEquals("1000", helper.getProjectShare());

        helper.setProjectShare("1000a");

        assertNull(helper.getProjectShare());

        helper.setProjectShare(null);

        assertNull(helper.getProjectShare());

        verifyMocks();
    }

    /**
     * Make sure project id can be correctly set.
     */
    @Test
    public void testSetRoleShareGood()
    {
        final SharedEntitySearchViewHelper helper = createTestHelper();
        helper.setRoleShare("2");

        assertEquals("2", helper.getRoleShare());

        verifyMocks();
    }

    /**
     * Make sure bad project id not set on error.
     */
    @Test
    public void testSetRoleShareBad()
    {
        final SharedEntitySearchViewHelper helper = createTestHelper();
        helper.setRoleShare("56as122");

        assertNull(helper.getProjectShare());

        helper.setRoleShare("1000");
        assertEquals("1000", helper.getRoleShare());

        helper.setRoleShare("456bbaa09494j");
        assertNull(helper.getRoleShare());

        helper.setRoleShare(null);
        assertNull(helper.getRoleShare());

        verifyMocks();
    }

    /**
     * Make sure it is possible to set the sort column.
     */
    @Test
    public void testSetSortColumnGood()
    {
        final SharedEntitySearchViewHelper helper = createTestHelper();

        final List<String>sort = Arrays.asList(SharedEntitySearchViewHelper.SortColumn.NAME, SharedEntitySearchViewHelper.SortColumn.OWNER, SharedEntitySearchViewHelper.SortColumn.DESCRIPTION, SharedEntitySearchViewHelper.SortColumn.FAVCOUNT);

        for (final String sortColumn : sort)
        {
            helper.setSortColumn(sortColumn);

            assertEquals("Unable to set valid sort '" + sortColumn + "'.", sortColumn, helper.getSortColumn());
        }

        helper.setSortColumn(null);
        assertEquals(SharedEntitySearchViewHelper.SortColumn.NAME, helper.getSortColumn());

        helper.setSortColumn("crapName");
        assertEquals(SharedEntitySearchViewHelper.SortColumn.NAME, helper.getSortColumn());

        verifyMocks();
    }

    /**
     * Make sure invalid sort columns are normalized.
     */
    @Test
    public void testSetSortColumnBad()
    {
        final SharedEntitySearchViewHelper helper = createTestHelper();

        helper.setSortColumn("");
        assertEquals(SharedEntitySearchViewHelper.SortColumn.DEFAULT_SORT, helper.getSortColumn());

        helper.setSortColumn("asdads");
        assertEquals(SharedEntitySearchViewHelper.SortColumn.DEFAULT_SORT, helper.getSortColumn());

        helper.setSortColumn(SharedEntitySearchViewHelper.SortColumn.FAVCOUNT);
        assertEquals(SharedEntitySearchViewHelper.SortColumn.FAVCOUNT, helper.getSortColumn());

        helper.setSortColumn("asdads");
        assertEquals(SharedEntitySearchViewHelper.SortColumn.DEFAULT_SORT, helper.getSortColumn());

        verifyMocks();
    }

    /**
     * Make sure the correct beans are returned.
     */
    @Test
    public void testGetShareTypeRendererBeans()
    {
        EasyMock.expect(shareTypeFactory.getShareType(GroupShareType.TYPE)).andReturn(new StupidShareType("group"));
        EasyMock.expect(shareTypeFactory.getShareType(ProjectShareType.TYPE)).andReturn(new StupidShareType("project"));

        final SharedEntitySearchViewHelper<SharedEntity> helper = createTestHelper();

        final List<ShareTypeRendererBean>beans = helper.getShareTypeRendererBeans();
        assertEquals(2, beans.size());
        assertEquals("group", beans.get(0).getShareType());
        assertEquals("project", beans.get(1).getShareType());

        verifyMocks();
    }

    /**
     * Test that the sort URL is generated correctly.
     */
    @Test
    public void testGenerateSortUrl()
    {
        final SharedEntitySearchViewHelper viewHelper = createTestHelper();

        viewHelper.setSortColumn(SharedEntitySearchViewHelper.SortColumn.DESCRIPTION);
        viewHelper.setSortAscending(true);

        // check a simple sort url with no parameters.
        assertSortUrl(viewHelper, SharedEntitySearchViewHelper.SortColumn.NAME, true);

        // changing the current order should not affect the search order unless NAME is
        // the current sorting column (which it isn't).
        viewHelper.setSortAscending(false);
        assertSortUrl(viewHelper, SharedEntitySearchViewHelper.SortColumn.NAME, true);

        // check that same column generates the reverse order.
        viewHelper.setSortColumn(SharedEntitySearchViewHelper.SortColumn.NAME);
        assertSortUrl(viewHelper, SharedEntitySearchViewHelper.SortColumn.NAME, true);

        viewHelper.setSortAscending(true);
        assertSortUrl(viewHelper, SharedEntitySearchViewHelper.SortColumn.NAME, false);

        viewHelper.setPagingOffset(100L);
        viewHelper.setSearchOwnerUserName("brenden");
        viewHelper.setSearchShareType("project");
        viewHelper.setProjectShare("100");
        viewHelper.setSearchName("search for testing");

        // check sort complex sort.
        assertSortUrl(viewHelper, SharedEntitySearchViewHelper.SortColumn.OWNER, true);

        // check that same column generates the reverse order.
        viewHelper.setSortColumn(SharedEntitySearchViewHelper.SortColumn.OWNER);
        assertSortUrl(viewHelper, SharedEntitySearchViewHelper.SortColumn.OWNER, false);

        viewHelper.setSortAscending(false);
        assertSortUrl(viewHelper, SharedEntitySearchViewHelper.SortColumn.OWNER, true);

        // the FAVCOUNT column should be sorted in reverse order when it is not the current column.
        assertSortUrl(viewHelper, SharedEntitySearchViewHelper.SortColumn.FAVCOUNT, false);

        // the FAVCOUNT column should be sorted in reverse order when it is not the current column.
        viewHelper.setSortAscending(true);
        assertSortUrl(viewHelper, SharedEntitySearchViewHelper.SortColumn.FAVCOUNT, false);

        // the order should reverse when it is the current column.
        viewHelper.setSortAscending(false);
        viewHelper.setSortColumn(SharedEntitySearchViewHelper.SortColumn.FAVCOUNT);
        assertSortUrl(viewHelper, SharedEntitySearchViewHelper.SortColumn.FAVCOUNT, true);

        // the order should reverse when it is the current column.
        viewHelper.setSortAscending(true);
        viewHelper.setSortColumn(SharedEntitySearchViewHelper.SortColumn.FAVCOUNT);
        assertSortUrl(viewHelper, SharedEntitySearchViewHelper.SortColumn.FAVCOUNT, false);

        // the order for a bad argument should be the same as name.
        assertSortUrl(viewHelper, null, SharedEntitySearchViewHelper.SortColumn.NAME, true);
        assertSortUrl(viewHelper, "adajshd", SharedEntitySearchViewHelper.SortColumn.NAME, true);

        verifyMocks();
    }

    /**
     * Make sure the code generates the correct CSS class.
     */
    @Test
    public void testGenerateSortCssClass()
    {
        final SharedEntitySearchViewHelper viewHelper = createTestHelper();

        viewHelper.setSortColumn(SharedEntitySearchViewHelper.SortColumn.NAME);
        assertEquals(CSS_OVER, viewHelper.generateSortCssClass(SharedEntitySearchViewHelper.SortColumn.NAME));
        assertEquals(CSS_LINK, viewHelper.generateSortCssClass(SharedEntitySearchViewHelper.SortColumn.FAVCOUNT));
        assertEquals(CSS_OVER, viewHelper.generateSortCssClass(null));

        viewHelper.setSortColumn(SharedEntitySearchViewHelper.SortColumn.FAVCOUNT);
        assertEquals(CSS_LINK, viewHelper.generateSortCssClass(SharedEntitySearchViewHelper.SortColumn.NAME));
        assertEquals(CSS_OVER, viewHelper.generateSortCssClass(SharedEntitySearchViewHelper.SortColumn.FAVCOUNT));
        assertEquals(CSS_LINK, viewHelper.generateSortCssClass(null));

        verifyMocks();
    }

    /**
     * Is the correct JSON state recorded.
     *
     * @throws JSONException just throw this up from the test.
     */
    @Test
    public void testSearchShareTypeJSON() throws JSONException
    {
        final SharedEntitySearchViewHelper viewHelper = createTestHelper();
        JSONObject expectedValue = new JSONObject(MapBuilder.<String, Object>newBuilder("type", "any").toMap());

        // test empty or incorrect parameters.
        assertJSONEquals(expectedValue, viewHelper.getSearchShareTypeJSON());

        viewHelper.setSearchShareType("Randomfkdflks");
        assertJSONEquals(expectedValue, viewHelper.getSearchShareTypeJSON());

        expectedValue = new JSONObject(MapBuilder.<String, Object>newBuilder("type", "group").toMap());

        // test the group parameter.
        viewHelper.setSearchShareType("group");
        assertJSONEquals(expectedValue, viewHelper.getSearchShareTypeJSON());

        expectedValue = new JSONObject(MapBuilder.<String, Object>newBuilder("type", "group").add("param1", "group").toMap());

        viewHelper.setGroupShare("group");
        assertJSONEquals(expectedValue, viewHelper.getSearchShareTypeJSON());

        // test the project share
        viewHelper.setSearchShareType("project");

        expectedValue = new JSONObject(MapBuilder.<String, Object>newBuilder("type", "project").toMap());
        assertJSONEquals(expectedValue, viewHelper.getSearchShareTypeJSON());

        viewHelper.setProjectShare("1000");
        expectedValue = new JSONObject(MapBuilder.<String, Object>newBuilder("type", "project").add("param1", "1000").toMap());
        assertJSONEquals(expectedValue, viewHelper.getSearchShareTypeJSON());

        viewHelper.setRoleShare("999");
        expectedValue = new JSONObject(MapBuilder.<String, Object>newBuilder("type", "project").add("param1", "1000").add("param2", "999").toMap());
        assertJSONEquals(expectedValue, viewHelper.getSearchShareTypeJSON());

        viewHelper.setProjectShare(null);
        expectedValue = new JSONObject(MapBuilder.<String, Object>newBuilder("type", "project").add("param2", "999").toMap());
        assertJSONEquals(expectedValue, viewHelper.getSearchShareTypeJSON());

        verifyMocks();
    }

    /**
     * Does popular filters works as expected.
     */
    @Test
    public void testGetPopularFilters()
    {
        final SharedEntitySearchResult<SharedEntity> expectedResult = new SharedEntitySearchResult<SharedEntity>(new MockCloseableIterable<SharedEntity>(Collections.<SharedEntity>singletonList(entity1)), false, 1);
        final SharedEntitySearchParametersBuilder expectedParameterBuilder = new SharedEntitySearchParametersBuilder();

        final TestEntitySearchViewHelper helper = createTestHelper();
        expectedParameterBuilder.setSortColumn(SharedEntityColumn.FAVOURITE_COUNT, false);

        helper.setSearchResult(expectedResult);

        final MockJiraServiceContext serviceContext = new MockJiraServiceContext(testUser);
        final List actualResults = helper.getPopularFilters(serviceContext);
        assertFalse(serviceContext.getErrorCollection().hasAnyErrors());
        assertSearchArguments(0, 20, expectedParameterBuilder.toSearchParameters(), helper);
        assertEquals(expectedResult.getResults(), actualResults);

        verifyMocks();
    }

    /**
     * Does it work when the popular search does not work.
     */
    @Test
    public void testGetPopularFiltersBad()
    {
        final SharedEntitySearchParametersBuilder expectedParameterBuilder = new SharedEntitySearchParametersBuilder();
        expectedParameterBuilder.setSortColumn(SharedEntityColumn.FAVOURITE_COUNT, false);

        final TestEntitySearchViewHelper helper = createTestHelper();
        final MockJiraServiceContext serviceContext = new MockJiraServiceContext(testUser);
        final List actualResults = helper.getPopularFilters(serviceContext);
        assertTrue(serviceContext.getErrorCollection().hasAnyErrors());
        assertSearchArguments(0, 20, expectedParameterBuilder.toSearchParameters(), helper);
        assertTrue(actualResults.isEmpty());

        verifyMocks();
    }

    /**
     * Does it work when there is no paging.
     */
    @Test
    public void testSearchNoPaging()
    {
        final SharedEntitySearchResult<SharedEntity> expectedResult = new SharedEntitySearchResult<SharedEntity>(new MockCloseableIterable<SharedEntity>(Arrays.asList(entity1, entity2)), false, 2);

        final SharedEntitySearchParametersBuilder expectedParameterBuilder = new SharedEntitySearchParametersBuilder();
        expectedParameterBuilder.setName("testName");
        expectedParameterBuilder.setDescription("testName");
        expectedParameterBuilder.setTextSearchMode(SharedEntitySearchParameters.TextSearchMode.OR);
        expectedParameterBuilder.setSortColumn(SharedEntityColumn.NAME, true);

        final TestEntitySearchViewHelper helper = createTestHelper();
        helper.setSearchName("testName");
        helper.setSearchResult(expectedResult);

        final MockJiraServiceContext serviceContext = new MockJiraServiceContext(testUser);
        final SharedEntitySearchViewHelper.SearchResult actualResult = helper.search(serviceContext);
        assertFalse(serviceContext.getErrorCollection().hasAnyErrors());

        assertSearchArguments(0, 20, expectedParameterBuilder.toSearchParameters(), helper);
        assertSearchResult(expectedResult, actualResult, 0, 20, helper);

        verifyMocks();
    }

    /**
     * Does it work with paging.
     */
    @Test
    public void testSearchPaging()
    {
        SharedEntitySearchResult<SharedEntity> expectedResult = new SharedEntitySearchResult<SharedEntity>(new MockCloseableIterable<SharedEntity>(Arrays.asList(entity1, entity2)), true, 4);

        final SharedEntitySearchParametersBuilder expectedParameterBuilder = new SharedEntitySearchParametersBuilder();
        expectedParameterBuilder.setName("testName");
        expectedParameterBuilder.setDescription("testName");
        expectedParameterBuilder.setTextSearchMode(SharedEntitySearchParameters.TextSearchMode.OR);
        expectedParameterBuilder.setSortColumn(SharedEntityColumn.NAME, true);

        final TestEntitySearchViewHelper helper = createTestHelper();
        helper.setSearchName("testName");
        helper.setSearchResult(expectedResult);

        // check the first page.
        final MockJiraServiceContext serviceContext = new MockJiraServiceContext(testUser);
        SharedEntitySearchViewHelper.SearchResult actualResult = helper.search(serviceContext);
        assertFalse(serviceContext.getErrorCollection().hasAnyErrors());
        assertSearchArguments(0, 20, expectedParameterBuilder.toSearchParameters(), helper);
        assertSearchResult(expectedResult, actualResult, 0, 20, helper);

        // check the second page.
        helper.setPagingOffset(1L);
        actualResult = helper.search(new MockJiraServiceContext(testUser));
        assertFalse(serviceContext.getErrorCollection().hasAnyErrors());
        assertSearchArguments(1, 20, expectedParameterBuilder.toSearchParameters(), helper);
        assertSearchResult(expectedResult, actualResult, 1, 20, helper);

        // check the third page.
        expectedResult = new SharedEntitySearchResult<SharedEntity>(new MockCloseableIterable<SharedEntity>(Collections.singletonList(entity3)), false, 1);
        helper.setSearchResult(expectedResult);
        helper.setPagingOffset(2L);
        actualResult = helper.search(new MockJiraServiceContext(testUser));
        assertFalse(serviceContext.getErrorCollection().hasAnyErrors());
        assertSearchArguments(2, 20, expectedParameterBuilder.toSearchParameters(), helper);
        assertSearchResult(expectedResult, actualResult, 2, 20, helper);

        verifyMocks();
    }

    /**
     * Does it work with group sharing.
     */
    @Test
    public void testSearchGroupShares()
    {
        final SharedEntitySearchResult<SharedEntity> expectedResult = new SharedEntitySearchResult<SharedEntity>(new MockCloseableIterable<SharedEntity>(Arrays.asList(entity3, entity2)), false, 2);

        final SharedEntitySearchParametersBuilder expectedParameterBuilder = new SharedEntitySearchParametersBuilder();
        expectedParameterBuilder.setName("testName");
        expectedParameterBuilder.setDescription("testName");
        expectedParameterBuilder.setTextSearchMode(SharedEntitySearchParameters.TextSearchMode.OR);
        expectedParameterBuilder.setSortColumn(SharedEntityColumn.FAVOURITE_COUNT, false);
        expectedParameterBuilder.setShareTypeParameter(new GroupShareTypeSearchParameter("bad"));

        final TestEntitySearchViewHelper helper = createTestHelper();
        helper.setSearchName("testName");
        helper.setSortColumn(SharedEntitySearchViewHelper.SortColumn.FAVCOUNT);
        helper.setSortAscending(false);
        helper.setGroupShare("bad");
        helper.setSearchShareType("group");

        helper.setSearchResult(expectedResult);

        // make sure everything worked.
        final MockJiraServiceContext serviceContext = new MockJiraServiceContext(testUser);
        final SharedEntitySearchViewHelper.SearchResult actualResult = helper.search(serviceContext);
        assertFalse(serviceContext.getErrorCollection().hasAnyErrors());
        assertSearchArguments(0, 20, expectedParameterBuilder.toSearchParameters(), helper);
        assertSearchResult(expectedResult, actualResult, 0, 20, helper);

        verifyMocks();
    }

    /**
     * Does it work with project sharing.
     */
    @Test
    public void testSearchProject()
    {
        final SharedEntitySearchResult<SharedEntity> expectedResult = new SharedEntitySearchResult<SharedEntity>(new MockCloseableIterable<SharedEntity>(Arrays.asList(entity3, entity2)), false, 2);

        final SharedEntitySearchParametersBuilder expectedParameterBuilder = new SharedEntitySearchParametersBuilder();
        expectedParameterBuilder.setName("testName");
        expectedParameterBuilder.setDescription("testName");
        expectedParameterBuilder.setTextSearchMode(SharedEntitySearchParameters.TextSearchMode.OR);
        expectedParameterBuilder.setSortColumn(SharedEntityColumn.OWNER, true);
        expectedParameterBuilder.setShareTypeParameter(new ProjectShareTypeSearchParameter(100L));

        final TestEntitySearchViewHelper helper = createTestHelper();
        helper.setSearchName("testName");
        helper.setSortColumn(SharedEntitySearchViewHelper.SortColumn.OWNER);
        helper.setSortAscending(true);
        helper.setProjectShare("100");
        helper.setSearchShareType("project");

        helper.setSearchResult(expectedResult);

        // make sure everything worked.
        final MockJiraServiceContext serviceContext = new MockJiraServiceContext(testUser);
        final SharedEntitySearchViewHelper.SearchResult actualResult = helper.search(serviceContext);
        assertFalse(serviceContext.getErrorCollection().hasAnyErrors());
        assertSearchArguments(0, 20, expectedParameterBuilder.toSearchParameters(), helper);
        assertSearchResult(expectedResult, actualResult, 0, 20, helper);

        verifyMocks();
    }

    /**
     * Does it work with role sharing.
     */
    @Test
    public void testSearchRole()
    {
        final SharedEntitySearchResult<SharedEntity> expectedResult = new SharedEntitySearchResult<SharedEntity>(new MockCloseableIterable<SharedEntity>(Arrays.asList(entity3, entity2)), true, 4);

        final SharedEntitySearchParametersBuilder expectedParameterBuilder = new SharedEntitySearchParametersBuilder();
        expectedParameterBuilder.setName("testGetSearchFiltersRole");
        expectedParameterBuilder.setDescription("testGetSearchFiltersRole");
        expectedParameterBuilder.setTextSearchMode(SharedEntitySearchParameters.TextSearchMode.OR);
        expectedParameterBuilder.setSortColumn(SharedEntityColumn.NAME, true);
        expectedParameterBuilder.setShareTypeParameter(new ProjectShareTypeSearchParameter(100L, 678L));

        final TestEntitySearchViewHelper helper = createTestHelper();
        helper.setSearchName("testGetSearchFiltersRole");
        helper.setSortColumn("badSortColumn");
        helper.setSortAscending(true);
        helper.setProjectShare("100");
        helper.setRoleShare("678");
        helper.setSearchShareType("project");
        helper.setPagingOffset(5L);

        helper.setSearchResult(expectedResult);

        // make sure everything worked.
        final MockJiraServiceContext serviceContext = new MockJiraServiceContext(testUser);
        final SharedEntitySearchViewHelper.SearchResult actualResult = helper.search(serviceContext);
        assertFalse(serviceContext.getErrorCollection().hasAnyErrors());
        assertSearchArguments(5, 20, expectedParameterBuilder.toSearchParameters(), helper);
        assertSearchResult(expectedResult, actualResult, 5, 20, helper);

        verifyMocks();
    }

    /**
     * Do we get an error while searching invalid parameters?
     */
    @Test
    public void testSearchBadRole()
    {
        final TestEntitySearchViewHelper helper = createTestHelper();
        helper.setProjectShare("www");
        helper.setRoleShare("678");
        helper.setSearchShareType("project");

        helper.setSearchResult(null);

        // make sure everything worked.
        final MockJiraServiceContext serviceContext = new MockJiraServiceContext(testUser);
        final SharedEntitySearchViewHelper.SearchResult actualResult = helper.search(serviceContext);
        assertNull(actualResult);
        final Map map = serviceContext.getErrorCollection().getErrors();
        assertTrue(map.get("shares").equals("Illegal search parameters."));

        assertEquals(0, helper.getSearchCount());
        assertEquals(0, helper.getValidateCount());

        verifyMocks();
    }

    /**
     * Anonymous user should have shares ignored.
     */
    @Test
    public void testSearchAnonymousIgnored()
    {
        final SharedEntitySearchResult<SharedEntity> expectedResult = new SharedEntitySearchResult<SharedEntity>(new MockCloseableIterable<SharedEntity>(Collections.<SharedEntity>emptyList()), false, 0);

        final SharedEntitySearchParametersBuilder expectedParameterBuilder = new SharedEntitySearchParametersBuilder();
        expectedParameterBuilder.setName("testName");
        expectedParameterBuilder.setDescription("testName");
        expectedParameterBuilder.setTextSearchMode(SharedEntitySearchParameters.TextSearchMode.OR);
        expectedParameterBuilder.setSortColumn(SharedEntityColumn.FAVOURITE_COUNT, false);

        final TestEntitySearchViewHelper helper = createTestHelper(null);
        helper.setSearchName("testName");
        helper.setSortColumn(SharedEntitySearchViewHelper.SortColumn.FAVCOUNT);
        helper.setSortAscending(false);
        helper.setGroupShare("bad");
        helper.setSearchShareType("group");

        helper.setSearchResult(expectedResult);

        // make sure everything worked.
        final MockJiraServiceContext serviceContext = new MockJiraServiceContext((User) null);
        final SharedEntitySearchViewHelper.SearchResult actualResult = helper.search(serviceContext);
        assertFalse(serviceContext.getErrorCollection().hasAnyErrors());
        assertSearchArguments(0, 20, expectedParameterBuilder.toSearchParameters(), helper);
        assertSearchResult(expectedResult, actualResult, 0, 20, helper);

        verifyMocks();
    }

    /**
     * Make sure the code works when the search returns in error.
     */
    @Test
    public void testSearchError()
    {
        final SharedEntitySearchParametersBuilder expectedParameterBuilder = new SharedEntitySearchParametersBuilder();
        expectedParameterBuilder.setName("testName");
        expectedParameterBuilder.setDescription("testName");
        expectedParameterBuilder.setTextSearchMode(SharedEntitySearchParameters.TextSearchMode.OR);
        expectedParameterBuilder.setSortColumn(SharedEntityColumn.NAME, true);

        final TestEntitySearchViewHelper helper = createTestHelper();
        helper.setSearchName("testName");

        helper.setSearchResult(null);

        // make sure that the error was propagated correctly.
        final MockJiraServiceContext serviceContext = new MockJiraServiceContext(testUser);
        final SharedEntitySearchViewHelper.SearchResult actualResult = helper.search(serviceContext);
        assertTrue(serviceContext.getErrorCollection().hasAnyErrors());
        assertNull(actualResult);
        assertSearchArguments(0, 20, expectedParameterBuilder.toSearchParameters(), helper);

        // make sure that search was called.
        assertEquals(1, helper.getSearchCount());
        assertEquals(1, helper.getValidateCount());

        verifyMocks();
    }

    /**
     * Make sure the incorrect validation also causes the search to fail.
     */
    @Test
    public void testSearchValidateError()
    {
        final SharedEntitySearchParametersBuilder expectedParameterBuilder = new SharedEntitySearchParametersBuilder();
        expectedParameterBuilder.setName("testSearchValidateError");
        expectedParameterBuilder.setDescription("testSearchValidateError");
        expectedParameterBuilder.setTextSearchMode(SharedEntitySearchParameters.TextSearchMode.OR);
        expectedParameterBuilder.setSortColumn(SharedEntityColumn.OWNER, false);
        expectedParameterBuilder.setUserName("badUser");

        final TestEntitySearchViewHelper helper = createTestHelper();
        helper.setValidateResult(KEY_TEST_ERROR_MESSAGE);
        helper.setSearchName("testSearchValidateError");
        helper.setSortColumn(SharedEntitySearchViewHelper.SortColumn.OWNER);
        helper.setSortAscending(false);
        helper.setSearchOwnerUserName("badUser");

        final MockJiraServiceContext serviceContext = new MockJiraServiceContext(testUser);
        final SharedEntitySearchViewHelper.SearchResult actualResult = helper.search(serviceContext);
        assertTrue(serviceContext.getErrorCollection().hasAnyErrors());
        assertNull(actualResult);
        assertSearchArguments(0, 20, expectedParameterBuilder.toSearchParameters(), helper);

        // make sure that only validate was called.
        assertEquals(0, helper.getSearchCount());
        assertEquals(1, helper.getValidateCount());

        verifyMocks();
    }

    /**
     * Test the generated sort icon.
     */
    @Test
    public void testGenerateSortIcon()
    {
        final SharedEntitySearchViewHelper helper = createTestHelper();
        helper.setSortColumn(SharedEntitySearchViewHelper.SortColumn.OWNER);

        // should not get sort image when passed column is not active sort column.
        assertEquals("", helper.generateSortIcon(SharedEntitySearchViewHelper.SortColumn.NAME));

        // should get the "down" arrow when sorting in ascending order.
        assertEquals(createUpImage(), helper.generateSortIcon(SharedEntitySearchViewHelper.SortColumn.OWNER));

        // should get the "up" arrow when sorting in descending order.
        helper.setSortAscending(false);
        assertEquals(createDownImage(), helper.generateSortIcon(SharedEntitySearchViewHelper.SortColumn.OWNER));

        helper.setSortColumn(null);

        // should not get any image when not the current sorting order.
        assertEquals("", helper.generateSortIcon(SharedEntitySearchViewHelper.SortColumn.OWNER));

        // should get the "down" arrow when sorting in ascending order.
        assertEquals(createDownImage(), helper.generateSortIcon(SharedEntitySearchViewHelper.SortColumn.NAME));

        // should get the "up" arrow when sorting in descending order.
        helper.setSortAscending(true);
        assertEquals(createUpImage(), helper.generateSortIcon(SharedEntitySearchViewHelper.SortColumn.NAME));

        verifyMocks();
    }

    private String createUpImage()
    {
        return "<img class=\"sortArrow\" src=\"" + APPLICATION_CONTEXT + "/images/icons/icon_sortascending.png\" alt=\"Ascending order - Click to sort in descending order\" />";
    }

    private String createDownImage()
    {
        return "<img class=\"sortArrow\" src=\"" + APPLICATION_CONTEXT + "/images/icons/icon_sortdescending.png\" alt=\"Descending order - Click to sort in ascending order\" />";
    }

    private void assertSearchArguments(final int page, final int pageWidth, final SharedEntitySearchParameters parameters, final TestEntitySearchViewHelper helper)
    {
        assertEquals(page, helper.getActualPageOffset());
        assertEquals(pageWidth, helper.getActualPageWidth());
        assertParametersEqual(parameters, helper.getActualSearchParameters());
    }

    private void assertSearchResult(final SharedEntitySearchResult expectedResult, final SharedEntitySearchViewHelper.SearchResult actualResult, final int page, final int pageWidth, final SharedEntitySearchViewHelper helper)
    {
        assertEquals(expectedResult.getResults(), actualResult.getResults());

        if (!expectedResult.isEmpty())
        {
            final int startingResultNo = page * pageWidth;
            assertEquals(startingResultNo + 1, actualResult.getStartResultPosition());
            assertEquals(startingResultNo + expectedResult.getResults().size(), actualResult.getEndResultPosition());
        }
        else
        {
            assertEquals(-1, actualResult.getStartResultPosition());
            assertEquals(-1, actualResult.getEndResultPosition());
        }

        if (expectedResult.hasMoreResults())
        {
            assertPageUrl(actualResult.getNextUrl(), page + 1, helper);
        }
        else
        {
            assertNull(actualResult.getNextUrl());
        }

        if (page == 0)
        {
            assertNull(actualResult.getPreviousUrl());
        }
        else
        {
            assertPageUrl(actualResult.getPreviousUrl(), page - 1, helper);
        }
    }

    private void assertPageUrl(final String actualUrl, final int page, final SharedEntitySearchViewHelper helper)
    {
        final Map<String,String> parameters = new HashMap<String, String>();
        addStandardParameters(helper, parameters);
        addPageParameters(helper.getSortColumn(), helper.isSortAscending(), page, parameters);

        assertUrl(actualUrl, URL_PREFIX, parameters);
    }

    private static void assertParametersEqual(final SharedEntitySearchParameters expectedEntitySearchParameters, final SharedEntitySearchParameters actualSharedEntitySearchParameters)
    {
        assertEquals(expectedEntitySearchParameters.getName(), actualSharedEntitySearchParameters.getName());
        assertEquals(expectedEntitySearchParameters.getDescription(), actualSharedEntitySearchParameters.getDescription());
        assertSame(expectedEntitySearchParameters.getTextSearchMode(), actualSharedEntitySearchParameters.getTextSearchMode());
        assertEquals(expectedEntitySearchParameters.getFavourite(), actualSharedEntitySearchParameters.getFavourite());
        assertEquals(expectedEntitySearchParameters.getUserName(), actualSharedEntitySearchParameters.getUserName());
        assertEquals(expectedEntitySearchParameters.isAscendingSort(), actualSharedEntitySearchParameters.isAscendingSort());
        assertSame(expectedEntitySearchParameters.getSortColumn(), actualSharedEntitySearchParameters.getSortColumn());
        assertEquals(expectedEntitySearchParameters.getShareTypeParameter(), actualSharedEntitySearchParameters.getShareTypeParameter());
    }

    private void assertJSONEquals(final JSONObject expectedValue, final String actualJson) throws JSONException
    {
        final JSONArray array = new JSONArray(actualJson);
        assertEquals("Expecting JSON array with one element.", 1, array.length());

        final JSONObject actualValue = array.getJSONObject(0);

        assertEquals("Expected and actual JSON object have different sizes.", expectedValue.length(), actualValue.length());

        for (final Iterator iterator = expectedValue.keys(); iterator.hasNext();)
        {
            final String key = (String) iterator.next();
            assertTrue("Actual[" + key + "]", actualValue.has(key));
            if (expectedValue.isNull(key))
            {
                assertTrue("Actual[" + key + "] is not null.", actualValue.isNull(key));
            }
            else
            {
                assertEquals("Expected[" + key + "] != Actual[" + key + "]", expectedValue.get(key), actualValue.get(key));
            }
        }
    }

    private void assertSortUrl(final SharedEntitySearchViewHelper helper, final String sortColumn, final boolean ascending)
    {
        assertSortUrl(helper, sortColumn, sortColumn, ascending);
    }

    private void assertSortUrl(final SharedEntitySearchViewHelper helper, final String sortColumn, final String actualSortColumn, final boolean ascending)
    {
        final Map<String, String> parameters = new HashMap<String, String>();
        addStandardParameters(helper, parameters);
        addSortParameters(actualSortColumn, ascending, parameters);

        assertUrl(helper.generateSortUrl(sortColumn), URL_PREFIX, parameters);
    }

    private void addSortParameters(final String sortColumnName, final boolean ascending, final Map<String, String> parameters)
    {
        addPageParameters(sortColumnName, ascending, 0, parameters);
    }

    private void addPageParameters(final String sortColumnName, final boolean ascending, final int page, final Map<String, String> parameters)
    {
        addParameter(parameters, "sortColumn", sortColumnName == null ? "name" : sortColumnName);
        addParameter(parameters, "sortAscending", String.valueOf(ascending));
        addParameter(parameters, "pagingOffset", String.valueOf(page));
    }

    private void addStandardParameters(final SharedEntitySearchViewHelper helper, final Map<String, String>parameters)
    {
        addParameter(parameters, "Search", "Search");
        addParameter(parameters, VIEW_PARAMETER, VIEW_VALUE);
        addParameter(parameters, "searchName", helper.getSearchName());
        addParameter(parameters, "searchOwnerUserName", helper.getSearchOwnerUserName());
        addParameter(parameters, "searchShareType", helper.getSearchShareType());
        addParameter(parameters, "projectShare", helper.getProjectShare());
        addParameter(parameters, "roleShare", helper.getRoleShare());
        addParameter(parameters, "groupShare", helper.getGroupShare());
    }

    private void addParameter(final Map<String, String> parameters, final String parameter, final Object value)
    {
        parameters.put(parameter, value == null ? "" : value.toString());
    }

    private void assertUrl(final String url, final String expectedPrefix, final Map<String, String> expectedParameters)
    {
        assertTrue("Url '" + url + "' does not begin with '" + expectedPrefix + "'.", url.startsWith(expectedPrefix));

        final Map actualParameters = parseQueryString(url);

        for (final Map.Entry<String, String> entry : expectedParameters.entrySet())
        {
            assertTrue("Parameter '" + entry.getKey() + "' does not exist.", actualParameters.containsKey(entry.getKey()));
            assertEquals("Parameter '" + entry.getKey() + "' does should have value '" + entry.getValue() + "'.", entry.getValue(), actualParameters.get(entry.getKey()));
        }
    }

    private Map<String, String> parseQueryString(final String url)
    {
        final int queryStart = url.indexOf('?');
        final Map<String, String>actualParameters = new HashMap<String, String>();
        if ((queryStart >= 0) && (queryStart + 1 < url.length()))
        {
            final String[] parameters = url.substring(queryStart + 1).split("\\&");
            for (final String parameter : parameters)
            {
                if (StringUtils.isNotBlank(parameter))
                {
                    final int splitIndex = parameter.indexOf('=');
                    if (splitIndex >= 0)
                    {
                        final String parameterName = splitIndex > 0 ? parameter.substring(0, splitIndex) : "";
                        final String parameterValue = splitIndex + 1 < parameter.length() ? parameter.substring(splitIndex + 1) : "";

                        try
                        {
                            actualParameters.put(parameterName, urlCodec.decode(parameterValue, "utf8"));
                        }
                        catch (final DecoderException e)
                        {
                            throw new RuntimeException(e);
                        }
                        catch (final UnsupportedEncodingException e)
                        {
                            throw new RuntimeException(e);
                        }
                    }
                    else
                    {
                        actualParameters.put(parameter, null);
                    }
                }
            }
        }
        return actualParameters;
    }

    private TestEntitySearchViewHelper createTestHelper()
    {
        return createTestHelper(testUser);
    }

    private TestEntitySearchViewHelper createTestHelper(final User user)
    {
        EasyMock.replay(shareTypeFactory);

        return new TestEntitySearchViewHelper(shareTypeFactory, new MockSimpleAuthenticationContext(user));
    }

    private void verifyMocks()
    {
        EasyMock.verify(shareTypeFactory);
    }

    private static class TestEntitySearchViewHelper extends SharedEntitySearchViewHelper<SharedEntity>
    {
        private SharedEntitySearchParameters actualSearchParameters;
        private int actualPageOffset = Integer.MIN_VALUE;
        private int actualPageWidth = Integer.MIN_VALUE;

        private SharedEntitySearchResult<SharedEntity> searchResult = null;
        private String validateResult = null;

        private int searchCount = 0;
        private int validateCount = 0;

        public TestEntitySearchViewHelper(final ShareTypeFactory factory, final JiraAuthenticationContext ctx)
        {
            super(factory, ctx, APPLICATION_CONTEXT, URL_PREFIX, VIEW_PARAMETER, VIEW_VALUE, SearchRequest.ENTITY_TYPE);
        }

        @Override
        public SharedEntitySearchContext getEntitySearchContext()
        {
            return SharedEntitySearchContext.USE;
        }

        protected SharedEntitySearchResult<SharedEntity> doExecuteSearch(final JiraServiceContext ctx, final SharedEntitySearchParameters searchParameters, final int pageOffset, final int pageWidth)
        {
            actualPageOffset = pageOffset;
            actualPageWidth = pageWidth;
            actualSearchParameters = searchParameters;
            searchCount++;

            if (searchResult == null)
            {
                ctx.getErrorCollection().addErrorMessage(KEY_TEST_ERROR_MESSAGE);
            }

            return searchResult;
        }

        protected boolean validateSearchParameters(final JiraServiceContext ctx, final SharedEntitySearchParameters searchParameters, final int pageOffset, final int pageWidth)
        {
            actualPageOffset = pageOffset;
            actualPageWidth = pageWidth;
            actualSearchParameters = searchParameters;
            validateCount++;

            if (validateResult == null)
            {
                return true;
            }
            else
            {
                ctx.getErrorCollection().addErrorMessage(validateResult);
                return false;
            }
        }

        String urlenc(final Object input)
        {
            if (input != null)
            {
                try
                {
                    return urlCodec.encode(input.toString(), "utf8");
                }
                catch (final UnsupportedEncodingException e)
                {
                    throw new RuntimeException("FAIL", e);
                }
            }

            return "";
        }

        public SharedEntitySearchParameters getActualSearchParameters()
        {
            return actualSearchParameters;
        }

        public int getActualPageOffset()
        {
            return actualPageOffset;
        }

        public int getActualPageWidth()
        {
            return actualPageWidth;
        }

        public SharedEntitySearchResult getSearchResult()
        {
            return searchResult;
        }

        public void setSearchResult(final SharedEntitySearchResult<SharedEntity> searchResult)
        {
            this.searchResult = searchResult;
        }

        public String getValidateResult()
        {
            return validateResult;
        }

        public void setValidateResult(final String validateResult)
        {
            this.validateResult = validateResult;
        }

        public int getSearchCount()
        {
            return searchCount;
        }

        public int getValidateCount()
        {
            return validateCount;
        }
    }

    private static class StupidShareType implements ShareType
    {
        private final Name shareType;

        public StupidShareType(final String shareType)
        {
            this.shareType = new ShareType.Name(shareType);
        }

        public Name getType()
        {
            return shareType;
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
            return null;
        }

        public ShareTypePermissionChecker getPermissionsChecker()
        {
            return null;
        }

        public ShareQueryFactory<?> getQueryFactory()
        {
            return null;
        }

        public Comparator<SharePermission> getComparator()
        {
            return null;
        }
    }
}
