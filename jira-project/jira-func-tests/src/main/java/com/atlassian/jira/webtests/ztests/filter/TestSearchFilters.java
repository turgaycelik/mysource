package com.atlassian.jira.webtests.ztests.filter;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.locator.XPathLocator;
import com.atlassian.jira.functest.framework.navigation.FilterNavigation;
import com.atlassian.jira.functest.framework.parser.filter.FilterItem;
import com.atlassian.jira.functest.framework.parser.filter.FilterParser;
import com.atlassian.jira.functest.framework.parser.filter.WebTestSharePermission;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.google.common.collect.ImmutableList;
import org.apache.commons.collections.Closure;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.google.common.collect.Lists.newArrayList;

/**
 * A func test for the Search Filters functionality in the brave new world of shared entities.
 *
 * @since v3.13
 */
@WebTest ({ Category.FUNC_TEST, Category.FILTERS })
public class TestSearchFilters extends FuncTestCase
{
    private static final Logger log = Logger.getLogger(TestSearchFilters.class);

    private List<FilterItem> expectedList;
    private List<FilterItem> actualList;
    private String currentUserName;

    private static final int DEV_ID_1 = 10040;
    private static final List<WebTestSharePermission> GLOBAL_SHARE =
            ImmutableList.of(new WebTestSharePermission(WebTestSharePermission.GLOBAL_TYPE, null, null));
    private static final String DEVELOPER_THE_GREAT = "developer the great and wise (developer)";
    private static final String ADMINISTRATOR_ADMIN = ADMIN_FULLNAME + " (admin)";
    private static final String SEARCH_TABLE_ID = FilterParser.TableId.SEARCH_TABLE;
    private static final DecimalFormat ZEROZERO_FORMAT = new DecimalFormat("00");
    private static final String PREVIOUS = "<< Previous";
    private static final String NEXT = "Next >>";
    private static final String DEVELOPER_FILTER = "developer filter";
    private FilterNavigation[] filterNavigationScreen;

    protected void setUpTest()
    {
        currentUserName = ADMIN_USERNAME;
        filterNavigationScreen = new FilterNavigation[] { navigation.manageFilters(), navigation.filterPickerPopup() };
    }

    protected void tearDownTest()
    {
        currentUserName = null;
        actualList = null;
        expectedList = null;
        filterNavigationScreen = null;
        super.tearDownTest();
    }

    public void testInitialScreenAppearance() throws Exception
    {
        for (FilterNavigation aFilterNavigationScreen : filterNavigationScreen)
        {
            testInitialScreenAppearance(aFilterNavigationScreen);
        }
    }

    public void testNoSearchYet()
    {
        for (FilterNavigation aFilterNavigationScreen : filterNavigationScreen)
        {
            testNoSearchYet(aFilterNavigationScreen);
        }
    }

    public void testBasicNameSearch() throws Exception
    {
        for (FilterNavigation aFilterNavigationScreen : filterNavigationScreen)
        {
            testBasicNameSearch(aFilterNavigationScreen);
        }
    }

    public void testBasicPaging() throws Exception
    {
        for (FilterNavigation aFilterNavigationScreen : filterNavigationScreen)
        {
            testBasicPaging(aFilterNavigationScreen);
        }
    }

    public void testMultiPageSort() throws Exception
    {
        for (FilterNavigation aFilterNavigationScreen : filterNavigationScreen)
        {
            testMultiPageSort(aFilterNavigationScreen);
        }
    }


    public void testBasicSorting() throws Exception
    {
        for (FilterNavigation aFilterNavigationScreen : filterNavigationScreen)
        {
            testBasicSorting(aFilterNavigationScreen);
        }
    }

    public void testSortingByUserName() throws Exception
    {
        for (FilterNavigation aFilterNavigationScreen : filterNavigationScreen)
        {
            testSortingByUserName(aFilterNavigationScreen);
        }
    }

    public void testInitialScreenAppearance(FilterNavigation filterNavigation) throws Exception
    {
        administration.restoreData("BaseProfessionalFilters.xml");

        filterNavigation.searchFilters();

        assertInputSearchFormHasThisShape("");

        // ok now do a search and ensure that the values are "reflected back
        submitSimpleSearch("", filterNavigation);

        // now set some values in
        submitSimpleSearch("cat", filterNavigation);
        submitSimpleSearch("dog", filterNavigation);
    }

    public void testNoSearchYet(FilterNavigation filterNavigation)
    {
        administration.restoreBlankInstance();
        // no search yet
        filterNavigation.searchFilters();

        //search with no results
        submitSimpleSearch("zz", filterNavigation);
        tester.assertTextPresent("Your search criteria did not match any filters.");
    }

    public void testXss() throws Exception
    {
        for (FilterNavigation navigation1 : filterNavigationScreen)
        {
            testXss(navigation1);
        }
    }

    public void testXss(FilterNavigation filterNavigation) throws Exception
    {
        log.info("doing testXss with " + filterNavigation);
        navigation.gotoDashboard();
        administration.restoreData("BaseProfessionalFiltersWithXSS.xml");

        filterNavigation.searchFilters();

        String baseURL = filterNavigation.getActionBaseUrl() + "?filterView=search&searchName=&searchOwnerUserName=&searchShareType=&groupShare=&projectShare=&roleShare=&Search=Search";
        String url;

        // test group name
        url = StringUtils.replace(baseURL, "searchShareType=&", "searchShareType=group&");
        url = StringUtils.replace(url, "groupShare=&", "groupShare=<script>alert('XSSGroup')</script>&");
        tester.gotoPage(url);

        actualList = parse.filter().parseFilterList(SEARCH_TABLE_ID).getFilterItems();

        List<FilterItem> expectedFilters = newArrayList();
        final WebTestSharePermission groupShare = new WebTestSharePermission(WebTestSharePermission.GROUP_TYPE, "<script>alert('XSSGroup')</script>", null);
        final FilterItem groupFilterItem = new FilterItem(10040L, "xss group filter", "" , ADMIN_FULLNAME + " (admin)", Collections.singletonList(groupShare), true, 0, null,1);

        expectedFilters.add(groupFilterItem);
        expectedFilters = filterNavigation.sanitiseSearchFilterItems(expectedFilters);

        assertions.assertEquals("Filter List is wrongo", expectedFilters, actualList);

        // test role name
        url = StringUtils.replace(baseURL, "searchShareType=&", "searchShareType=project&");
        url = StringUtils.replace(url, "projectShare=&", "projectShare=10000&");
        url = StringUtils.replace(url, "roleShare=&", "roleShare=10020&");
        tester.gotoPage(url);

        actualList = parse.filter().parseFilterList(SEARCH_TABLE_ID).getFilterItems();
        final WebTestSharePermission roleShare = new WebTestSharePermission(WebTestSharePermission.PROJECT_TYPE, "homosapien", "<script>alert('XSSRole')</script>");

        // TODO remove old cranky filter assertions that follow
        expectedFilters = newArrayList();
        final FilterItem roleFilterItem = new FilterItem(10041L, "xss project role filter", "", ADMIN_FULLNAME + " (admin)", ImmutableList.of(roleShare), true, 0, null, 1);
        expectedFilters.add(roleFilterItem);
        expectedFilters = filterNavigation.sanitiseSearchFilterItems(expectedFilters);
        assertions.assertEquals("Filter list is wrong", expectedFilters, actualList);
    }

    public void testBasicNameSearch(final FilterNavigation filterNavigation) throws Exception
    {
        administration.restoreData("BaseProfessionalFilters.xml");

        Closure basicNameSearchTest1 = new Closure()
        {
            public void execute(final Object userNameObj)
            {
                login(userNameObj);
                navigation.manageFilters().searchFilters();

                submitSimpleSearch("z", filterNavigation);

                expectedList = ImmutableList.of(newFI('z'));
                // check the expected versus the actual
                assertFilterItemLists();

                // now search with a user name as well as name
                submitSimpleSearch("b", filterNavigation);

                expectedList = ImmutableList.of(newFI('b'));
                // check the expected versus the actual
                assertFilterItemLists();

                // check when nothing is found based on name
                submitSimpleSearch("the evil zurg", filterNavigation);
                assertInputSearchFormHasThisShape("the evil zurg");
                assertNull("Should have found nothing", parse.filter().parseFilterList(SEARCH_TABLE_ID));

                //
                // search for multiple words
                submitSimpleSearch("b m w", filterNavigation);

                expectedList = ImmutableList.of(newFI('b'), newFI('m'), newFI('w'));
                // check the expected versus the actual
                assertFilterItemLists();
            }
        };

        // run the above test closure as admin and anonymous
        basicNameSearchTest1.execute(null);
        basicNameSearchTest1.execute(ADMIN_USERNAME);

        // now login as someone else which will add a few filters

        administration.restoreData("BaseProfessionalFiltersWithDeveloper.xml");

        final AtomicBoolean favFlag = new AtomicBoolean(false);
        final AtomicBoolean devFavFlag = new AtomicBoolean(false);
        Closure basicNameSearchTest2 = new Closure()
        {
            public void execute(Object userNameObj)
            {
                login(userNameObj);
                navigation.manageFilters().searchFilters();

                // search for multiple words
                submitSimpleSearch("b d* m z", filterNavigation);

                final boolean isFav = favFlag.get();
                final boolean isDevFav = devFavFlag.get();

                expectedList = ImmutableList.of(newFI('b').setFav(isFav),
                        newFI('d').setFav(isFav),
                        newFI(DEV_ID_1, DEVELOPER_FILTER, DEVELOPER_THE_GREAT).setFav(isDevFav),
                        newFI('m').setFav(isFav),
                        newFI('z').setFav(isFav));
                // check the expected versus the actual
                assertFilterItemLists();
            }
        };

        favFlag.set(false);
        devFavFlag.set(false);
        basicNameSearchTest2.execute(null);

        favFlag.set(false);
        devFavFlag.set(true);
        basicNameSearchTest2.execute("developer");

        favFlag.set(true);
        devFavFlag.set(false);
        basicNameSearchTest2.execute(ADMIN_USERNAME);
    }

    public void testBasicPaging(final FilterNavigation filterNavigation) throws Exception
    {
        navigation.gotoDashboard();
        administration.restoreData("BaseProfessionalFilters.xml");

        Closure basicPagingTest = new Closure()
        {
            public void execute(final Object userNameObj)
            {
                login(userNameObj);
                navigation.manageFilters().searchFilters();

                submitSimpleSearch("", filterNavigation);

                expectedList = buildExpectedList('a', 't');
                // check the expected versus the actual
                assertFilterItemLists();

                // check the paging links
                String pageURL = assertSinglePagingLinksPresent(1, 2, NEXT, "1 - 20 of 26", "");

                // now follow the link and see what it tells us on the next page
                tester.gotoPage(pageURL);

                expectedList = buildExpectedList('u', 'z');
                // check the expected versus the actual
                assertFilterItemLists();

                pageURL = assertSinglePagingLinksPresent(0, 2, PREVIOUS, "21 - 26 of 26", "");

                // now follow the link back and make sure it looks like the previous screen and that the URLs are stable
                tester.gotoPage(pageURL);

                expectedList = buildExpectedList('a', 't');
                // check the expected versus the actual
                assertFilterItemLists();

                pageURL = assertSinglePagingLinksPresent(1, 2, NEXT, "1 - 20 of 26", "");
                // follow the link again and see that the URLs are stable
                tester.gotoPage(pageURL);

                expectedList = buildExpectedList('u', 'z');
                // check the expected versus the actual
                assertFilterItemLists();

                assertSinglePagingLinksPresent(0, 2, PREVIOUS, "21 - 26 of 26", "");
            }
        };

        basicPagingTest.execute(null);
        basicPagingTest.execute(ADMIN_USERNAME);
    }

    public void testMultiPageSort(final FilterNavigation filterNavigation) throws Exception
    {
        administration.restoreData("BaseProfessionalFiltersWithDeveloperZFilters.xml");

        Closure multiPageSortTest = new Closure()
        {
            public void execute(final Object userNameObj)
            {
                login(userNameObj);
                navigation.manageFilters().searchFilters();


                submitSimpleSearch("", filterNavigation);

                expectedList = buildExpectedList('a', 't');
                // check the expected versus the actual
                assertFilterItemLists();
                assertSinglePagingLinksPresent(1, 2, NEXT, "1 - 20 of 45", "");

                tester.clickLinkWithText(NEXT);

                expectedList = buildExpectedList('u', 'z');
                addDeveloperZSeriesFilters(expectedList, 0, 13);
                // check the expected versus the actual
                assertFilterItemLists();
                assertDualPagingLinksPresent(2, 2, "21 - 40 of 45", "", "");

                tester.clickLinkWithText(NEXT);

                expectedList = newArrayList();
                addDeveloperZSeriesFilters(expectedList, 14, 18);
                // check the expected versus the actual
                assertFilterItemLists();
                assertSinglePagingLinksPresent(1, 2, PREVIOUS, "41 - 45 of 45", "");

                // now go backwards
                tester.clickLinkWithText(PREVIOUS);

                expectedList = buildExpectedList('u', 'z');
                addDeveloperZSeriesFilters(expectedList, 0, 13);
                // check the expected versus the actual
                assertFilterItemLists();
                assertDualPagingLinksPresent(2, 2, "21 - 40 of 45", "", "");

                tester.clickLinkWithText(PREVIOUS);
                expectedList = buildExpectedList('a', 't');
                // check the expected versus the actual
                assertFilterItemLists();
                assertSinglePagingLinksPresent(1, 2, NEXT, "1 - 20 of 45", "");
            }
        };
        multiPageSortTest.execute(null);
        multiPageSortTest.execute(ADMIN_USERNAME);
    }

    public void testBasicSorting(final FilterNavigation filterNavigation) throws Exception
    {
        navigation.gotoDashboard();
        administration.restoreData("BaseProfessionalFilters.xml");
        navigation.manageFilters().searchFilters();

        submitSimpleSearch("", filterNavigation);

        expectedList = buildExpectedList('a', 't');
        // check the expected versus the actual
        assertFilterItemLists();

        String sortURL = assertSortUrls("Name", false, "");
        assertNotNull(sortURL);
        // now sort on that URL
        tester.gotoPage(sortURL);
        expectedList = buildExpectedList('z', 'g');
        // check the expected versus the actual
        assertFilterItemLists();
        sortURL = assertSortUrls("Name", true, "");
        assertNotNull(sortURL);

        // ok so sort on say Owner and then detect what it looks like
        sortURL = getSortURL("Owner");
        assertNotNull(sortURL);
        tester.gotoPage(sortURL);
        expectedList = buildExpectedList('a', 't');
        // check the expected versus the actual
        assertFilterItemLists();
        sortURL = assertSortUrls("Owner", false, "");
        assertNotNull(sortURL);

        // ok flip Author field order
        tester.gotoPage(sortURL);
        expectedList = buildExpectedList('a', 't');
        // check the expected versus the actual
        assertFilterItemLists();
        sortURL = assertSortUrls("Author", true, "");
        assertNotNull(sortURL);

        // ok so sort on say Popularity and then detect what it looks like
        sortURL = getSortURL("Popularity");
        assertNotNull(sortURL);
        tester.gotoPage(sortURL);
        expectedList = buildExpectedList('a', 't');
        // check the expected versus the actual
        assertFilterItemLists();
        sortURL = assertSortUrls("Popularity", true, "");
        assertNotNull(sortURL);

        // ok flip Popularity field order
        tester.gotoPage(sortURL);
        expectedList = buildExpectedList('a', 't');
        // check the expected versus the actual
        assertFilterItemLists();
        sortURL = assertSortUrls("Popularity", false, "");
        assertNotNull(sortURL);

        // ok mutate popularity so that the order changes and a goes to the back
        navigation.manageFilters().removeFavourite(getEntityId('a'));
        navigation.manageFilters().searchFilters();
        submitSimpleSearch("", filterNavigation);

        // now click on Popularity and hence 'a' goes to the back
        sortURL = getSortURL("Popularity");
        assertNotNull(sortURL);
        tester.gotoPage(sortURL);

        tester.gotoPage(sortURL);
        expectedList = buildExpectedList('b', 'u');
        // check the expected versus the actual
        assertFilterItemLists();

        tester.clickLinkWithText(NEXT);
        List<FilterItem> suffixList = ImmutableList.of(newFI('a').setFav(false).setFavCount(0));
        expectedList = buildExpectedList(null, 'v', 'z', suffixList);
        // check the expected versus the actual
        assertFilterItemLists();

        // and make sure that the URLs are stable
        tester.clickLinkWithText(PREVIOUS);
        expectedList = buildExpectedList('b', 'u');
        // check the expected versus the actual
        assertFilterItemLists();

        sortURL = getSortURL("Popularity");
        assertNotNull(sortURL);
        tester.gotoPage(sortURL);

        List<FilterItem> prependList = ImmutableList.of(newFI('a').setFav(false).setFavCount(0));
        expectedList = buildExpectedList(prependList, 'b', 't', null);
        // check the expected versus the actual
        assertFilterItemLists();

        tester.clickLinkWithText(NEXT);
        expectedList = buildExpectedList('u', 'z');
        // check the expected versus the actual
        assertFilterItemLists();
    }

    public void testSortingByUserName(final FilterNavigation filterNavigation) throws Exception
    {
        // ok load up some data with a filter by another person.  we can then better test the sorting
        // by author
        navigation.gotoDashboard();
        administration.restoreData("BaseProfessionalFiltersWithDeveloper.xml");

        Closure sortingByUserNameTest1 = new Closure()
        {
            public void execute(final Object userNameObj)
            {
                login(userNameObj);
                navigation.manageFilters().searchFilters();

                submitSimpleSearch("", filterNavigation);

                final FilterItem developerFI = newFI(DEV_ID_1, DEVELOPER_FILTER, DEVELOPER_THE_GREAT).setFav(false);

                expectedList = buildExpectedList('a', 'd');
                expectedList.add(developerFI);
                expectedList.addAll(buildExpectedList('e', 's'));
                // check the expected versus the actual
                assertFilterItemLists();

                String sortURL = assertSortUrls("Name", false, "");
                assertNotNull(sortURL);

                sortURL = getSortURL("Owner");
                tester.gotoPage(sortURL);

                expectedList = buildExpectedList('a', 't');
                // check the expected versus the actual
                assertFilterItemLists();

                tester.clickLinkWithText(NEXT);
                expectedList = buildExpectedList('u', 'z');
                expectedList.add(developerFI);
                // check the expected versus the actual
                assertFilterItemLists();

                // now reverse sort by author
                sortURL = getSortURL("Owner");
                tester.gotoPage(sortURL);

                expectedList = newArrayList();
                expectedList.add(developerFI);
                expectedList.addAll(buildExpectedList('a', 's'));
                // check the expected versus the actual
                assertFilterItemLists();

            }
        };
        sortingByUserNameTest1.execute(null);
        sortingByUserNameTest1.execute(ADMIN_USERNAME);
    }

    public void testDummyXmlResponse()
            throws IOException, ParserConfigurationException, SAXException, TransformerException
    {
        administration.restoreData("TestSearchRequestURLHandler.xml");
        navigation.login(FRED_USERNAME, FRED_PASSWORD);
        String searchURL = "/sr/jira.issueviews:searchrequest-xml/10000/SearchRequest-10000.xml?tempMax=1000";
        tester.gotoPage(searchURL);
        final String responseText = tester.getDialog().getResponseText();
        Document doc = XMLUnit.buildControlDocument(responseText);

        XMLAssert.assertXpathExists("/rss/channel/build-info", doc);
        XMLAssert.assertXpathExists("/rss/channel/build-info/version", doc);
        XMLAssert.assertXpathExists("/rss/channel/build-info/build-number", doc);
        XMLAssert.assertXpathExists("/rss/channel/build-info/build-date", doc);
    }

    private void assertDualPagingLinksPresent(int expectedNextPageOffset, int expectedLinks, String expectedXOfY, String searchName, String searchOwnerUserName)
    {
        XPathLocator pageLocator;
        pageLocator = xPathLocator("//div[@class='pagination aui-item']/a/span[text() = '" + NEXT + "']");
        assertEquals(expectedLinks, pageLocator.getNodes().length);
        for (int i = 0; i < expectedLinks; i++)
        {
            assertEquals(NEXT, pageLocator.getText(pageLocator.getNodes()[i]));
        }

        pageLocator = xPathLocator("//div[@class='pagination aui-item']/a/span[text() = '" + PREVIOUS + "']");
        assertEquals(expectedLinks, pageLocator.getNodes().length);
        for (int i = 0; i < expectedLinks; i++)
        {
            assertEquals(PREVIOUS, pageLocator.getText(pageLocator.getNodes()[i]));
        }

        pageLocator = xPathLocator("//div[@class='pagination aui-item']/span");
        assertEquals(expectedLinks, pageLocator.getNodes().length);
        for (int i = 0; i < expectedLinks; i++)
        {
            final String actualXOfY = pageLocator.getText(pageLocator.getNodes()[i]);
            assertEquals(expectedXOfY, actualXOfY);
        }

        assertSortUrls("Name", false, searchName);

        pageLocator = xPathLocator("//div[@class='pagination aui-item']/a[contains(@class, 'icon-next')]/@href");
        assertEquals(expectedLinks, pageLocator.getNodes().length);
        for (int i = 0; i < expectedLinks; i++)
        {
            String hrefUrl = pageLocator.getText(pageLocator.getNodes()[i]);
            assertNotNull(hrefUrl);
            assertTrue(hrefUrl.contains("&filterView=search"));
            assertTrue("Next URL does not have right page offset : " + expectedNextPageOffset, hrefUrl.contains("&pagingOffset=" + expectedNextPageOffset));
            assertTrue(hrefUrl.contains("&pagingOffset=" + expectedNextPageOffset));
            assertTrue(hrefUrl.contains("&searchName=" + searchName + "&"));
            assertTrue(hrefUrl.contains("&searchOwnerUserName=" + searchOwnerUserName + "&"));
        }

        pageLocator = xPathLocator("//div[@class='pagination aui-item']/a[contains(@class, 'icon-previous')]/@href");
        assertEquals(expectedLinks, pageLocator.getNodes().length);
        final int expectedPrevOffset = expectedNextPageOffset - 2;
        for (int i = 0; i < expectedLinks; i++)
        {
            String hrefUrl = pageLocator.getText(pageLocator.getNodes()[i]);
            assertNotNull(hrefUrl);
            assertTrue(hrefUrl.contains("&filterView=search"));
            assertTrue("Previous URL does not have right page offset : " + expectedPrevOffset, hrefUrl.contains("&pagingOffset=" + expectedPrevOffset));
            assertTrue(hrefUrl.contains("&searchName=" + searchName + "&"));
            assertTrue(hrefUrl.contains("&searchOwnerUserName=" + searchOwnerUserName + "&"));
        }
    }

    private String assertSinglePagingLinksPresent(int expectedPageOffset, int expectedLinks, String expectedLinkText, String expectedXOfY, String searchName)
    {
        String returnURL = "/this/will/fail/unless/it/changes";

        XPathLocator pageLocator = xPathLocator("//div[@class='pagination aui-item']//a");
        assertEquals(expectedLinks, pageLocator.getNodes().length);
        for (int i = 0; i < expectedLinks; i++)
        {
            assertEquals(expectedLinkText, pageLocator.getText(pageLocator.getNodes()[i]));
        }

        pageLocator = xPathLocator("//div[@class='pagination aui-item']/span");
        assertEquals(expectedLinks, pageLocator.getNodes().length);
        for (int i = 0; i < expectedLinks; i++)
        {
            assertEquals(expectedXOfY, pageLocator.getText(pageLocator.getNodes()[i]));
        }

        assertSortUrls("Name", false, searchName);

        pageLocator = xPathLocator("//div[@class='pagination aui-item']//a/@href");
        assertEquals(expectedLinks, pageLocator.getNodes().length);
        for (int i = 0; i < expectedLinks; i++)
        {
            String hrefUrl = pageLocator.getText(pageLocator.getNodes()[i]);
            returnURL = hrefUrl;
            assertNotNull(hrefUrl);
            assertTrue(hrefUrl.contains("&filterView=search"));
            assertTrue("Does not have expected pagingOffset : " + expectedPageOffset, hrefUrl.contains("&pagingOffset=" + expectedPageOffset));
            assertTrue(hrefUrl.contains("&searchName=" + searchName + "&"));
        }
        return returnURL;
    }

    final static String[] SORT_COL_NAMES = new String[] { "Name", "Owner", "Popularity" };
    final static String[] SORT_PARAM_VALUES = new String[] { "name", "owner", "favcount" };
    final static int[] SORT_TD_OFFSETS = new int[] { 1, 2, 5 };
    final static boolean[] SORT_DEFAULT_SORTING = new boolean[] { true, true, false };

    private String getSortURL(String selectedColumnName)
    {
        int offset = 0;
        for (int i = 0; i < SORT_COL_NAMES.length; i++)
        {
            if (SORT_COL_NAMES[i].equalsIgnoreCase(selectedColumnName))
            {
                offset = SORT_TD_OFFSETS[i];
                break;
            }
        }
        XPathLocator colNameLocator = xPathLocator("//table[@id='" + SEARCH_TABLE_ID + "']/thead/tr/th[" + offset + "]/a");
        assertEquals(selectedColumnName, colNameLocator.getText());
        return ((Element) colNameLocator.getNode()).getAttribute("href");
    }

    private String assertSortUrls(String selectedColumnName, final boolean selectedSortAscending, final String searchName)
    {
        String returnURL = "/this/will/fail/unless/set";

        for (int i = 0; i < SORT_COL_NAMES.length; i++)
        {
            String columnName = SORT_COL_NAMES[i];
            XPathLocator colNameLocator = xPathLocator("//table[@id='" + SEARCH_TABLE_ID + "']/thead/tr/th[" + SORT_TD_OFFSETS[i] + "]/a");
            assertEquals(columnName, colNameLocator.getText());

            String hrefUrl = ((Element) colNameLocator.getNode()).getAttribute("href");

            assertNotNull(hrefUrl);
            text.assertTextPresent(hrefUrl, "filterView=search");
            text.assertTextPresent(hrefUrl, "pagingOffset=0");
            text.assertTextPresent(hrefUrl, "pagingOffset=0");
            text.assertTextPresent(hrefUrl, "searchName=" + searchName + "&");
            text.assertTextPresent(hrefUrl, "sortColumn=" + SORT_PARAM_VALUES[i]);

            if (columnName.equalsIgnoreCase(selectedColumnName))
            {
                text.assertTextPresent(hrefUrl, "sortAscending=" + String.valueOf(selectedSortAscending));
                returnURL = hrefUrl;
            }
            else
            {
                text.assertTextPresent(hrefUrl, "sortAscending=" + String.valueOf(SORT_DEFAULT_SORTING[i]));
            }
        }
        return returnURL;
    }

    private List <FilterItem> buildExpectedList(final char startC, final char endCInclusive)
    {
        return buildExpectedList(null, startC, endCInclusive, null);
    }

    private List <FilterItem> buildExpectedList(final List<FilterItem> prependList, final char startC,
            final char endCInclusive, final List<FilterItem> suffixList)
    {
        List<FilterItem> finalList = newArrayList();
        if (prependList != null)
        {
            finalList.addAll(prependList);
        }
        List<FilterItem> expectedList = newArrayList();
        char sC = startC;
        char eC = endCInclusive;
        if (startC > endCInclusive)
        {
            sC = endCInclusive;
            eC = startC;
        }
        for (int i = sC; i <= eC; i++)
        {
            expectedList.add(newFI((char) i));
        }
        if (startC > endCInclusive)
        {
            Collections.reverse(expectedList);
        }
        finalList.addAll(expectedList);
        if (suffixList != null)
        {
            finalList.addAll(suffixList);
        }
        return finalList;

    }

    /**
     * Submits a search and asserts that the name and user name field are the same afterwards
     */
    private void submitSimpleSearch(final String searchName, FilterNavigation filterNavigation)
    {
        filterNavigation.findFilters(searchName);
        assertInputSearchFormHasThisShape(searchName);
    }

    private void assertInputSearchFormHasThisShape(String searchName)
    {
        text.assertTextPresent(xPathLocator("//form[@id='filterSearchForm']"), "Search");
        assertTrue(xPathLocator("//form[@id='filterSearchForm']//input[@name='searchName']").hasNodes());
        assertEquals(searchName, tester.getDialog().getFormParameterValue("searchName"));

        text.assertTextPresent(xPathLocator("//form[@id='filterSearchForm']"), "Owner");
        assertTrue(xPathLocator("//form[@id='filterSearchForm']//*[@name='searchOwnerUserName']").hasNodes());

        // the share types are present but invisible from a display point of view
        if (currentUserName != null)
        {
            assertTrue(xPathLocator("//form[@id='filterSearchForm']//select[@name='searchShareType']").hasNodes());
            assertTrue(xPathLocator("//form[@id='filterSearchForm']//select[@name='groupShare']").hasNodes());
            assertTrue(xPathLocator("//form[@id='filterSearchForm']//select[@name='projectShare']").hasNodes());
            assertTrue(xPathLocator("//form[@id='filterSearchForm']//select[@name='roleShare']").hasNodes());
        }

        assertTrue(xPathLocator("//form[@id='filterSearchForm']//input[@name='Search']").hasNodes());
    }

    private void assertFilterItemLists()
    {

        // check the expected versus the actual
        actualList = parse.filter().parseFilterList(SEARCH_TABLE_ID).getFilterItems();
        if (expectedList == null || actualList == null)
        {
            assertTrue("If one filter list is null they both should be", actualList == null && expectedList == null);
            return;
        }
        assertEquals("The filter lists should have the same size", expectedList.size(), actualList.size());
        // if its the anonymous user we have to turn off Favourites attribute in the expected list.
        // this makes it easier to build the expected list this way
        if (currentUserName == null)
        {
            for (FilterItem filterItem : expectedList)
            {
                filterItem.setFav(false);
            }
        }
        int index = 0;
        for (FilterItem expectedFI : expectedList)
        {
            FilterItem actualFI = actualList.get(index++);
            assertEquals("The FilterItems are not equal", expectedFI, actualFI);
        }
    }

    private int getEntityId(final char filterName)
    {
        // filter named a is entity id 10005
        return (10005) + Math.abs('a' - filterName);
    }

    private XPathLocator xPathLocator(final String xpathStr)
    {
        return new XPathLocator(tester, xpathStr);
    }

    private FilterItem newFI(final long filterId, final String filterName, final String authorName)
    {
        return new FilterItem(filterId, filterName, "", authorName, GLOBAL_SHARE, Boolean.TRUE, 0L, null, 1L);
    }

    private FilterItem newFI(final char filterName)
    {
        return newFI(filterName, ADMINISTRATOR_ADMIN, Boolean.TRUE);
    }

    private FilterItem newFI(final char filterName, String authorName, final Boolean isFavourited)
    {
        Long id = (long) getEntityId(filterName);
        return new FilterItem(id, Character.toString(filterName), "", authorName, GLOBAL_SHARE, isFavourited, 0L, null, 1L);
    }

    private void addDeveloperZSeriesFilters(final List<FilterItem> expectedList, final int startFilterNum, final int endFilterNumInclusive)
    {
        for (int i = startFilterNum; i <= endFilterNumInclusive; i++)
        {
            String filterName = "z" + ZEROZERO_FORMAT.format(i);
            long id = DEV_ID_1 + i;
            expectedList.add(newFI(id, filterName, DEVELOPER_THE_GREAT).setFav(false));
        }
    }

    private void login(final Object userNameObj)
    {
        logout();
        if (userNameObj != null)
        {
            currentUserName = String.valueOf(userNameObj);
            navigation.login(currentUserName, currentUserName);
        }
    }

    private void logout()
    {
        navigation.logout();
        currentUserName = null;
    }
}
