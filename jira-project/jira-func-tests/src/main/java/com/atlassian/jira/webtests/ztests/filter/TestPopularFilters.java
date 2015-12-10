package com.atlassian.jira.webtests.ztests.filter;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.navigation.FilterNavigation;
import com.atlassian.jira.functest.framework.parser.filter.FilterItem;
import com.atlassian.jira.functest.framework.parser.filter.FilterList;
import com.atlassian.jira.functest.framework.parser.filter.FilterParser;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Test that the Popular Filters tab runs.
 *
 * @since v3.13
 */
@WebTest ({ Category.FUNC_TEST, Category.FILTERS })
public class TestPopularFilters extends FuncTestCase
{
    private static final String POPULAR_TABLE_ID = FilterParser.TableId.POPULAR_TABLE;

    protected void setUpTest()
    {
        administration.restoreData("BaseProfessionalFilters.xml");
    }

    public void testPopularityViewPopup() throws Exception
    {
        _testPopularityView(navigation.filterPickerPopup(), false);
    }

    public void testPopularityViewManageFilters() throws Exception
    {
        _testPopularityView(navigation.manageFilters(), true);
    }

    /**
     * Tests the popularity filter view of the given FilterNavigation.
     *
     * @param filterNavigation
     * @param testAnonymous
     * @throws Exception
     */
    private void _testPopularityView(FilterNavigation filterNavigation, final boolean testAnonymous) throws Exception
    {
        // by default the filter have a popularity of 1 in the data.  eg. there are favourited by admin
        LinkedHashMap expectedPopularityMap = new LinkedHashMap();
        setPopularityMapToOne(expectedPopularityMap);

        filterNavigation.popularFilters();
        assertPopularityAndListOrder(expectedPopularityMap);

        // ---------------------------------------
        // now login as a few different people and favourite things so
        // that the popularity of the things has changed
        // ---------------------------------------
        navigation.logout();
        navigation.login("developer", "developer");

        // should be the same list at this point
        filterNavigation.popularFilters();
        assertPopularityAndListOrder(expectedPopularityMap);

        // if we make a and b a favourite then the popularity list should change
        addFavourite(expectedPopularityMap, 'a', 2);
        addFavourite(expectedPopularityMap, 'b', 2);

        filterNavigation.popularFilters();
        assertPopularityAndListOrder(expectedPopularityMap);

        // ---------------------------------------
        // act as fred
        // ---------------------------------------
        navigation.logout();
        navigation.login(FRED_USERNAME);

        // should be the same list at this point
        filterNavigation.popularFilters();
        assertPopularityAndListOrder(expectedPopularityMap);

        // if we make a and b a favourite then the popularity list should change
        addFavourite(expectedPopularityMap, 'a', 3);
        addFavourite(expectedPopularityMap, 'b', 3);
        addFavourite(expectedPopularityMap, 'c', 2);
        addFavourite(expectedPopularityMap, 'd', 2);

        filterNavigation.popularFilters();
        assertPopularityAndListOrder(expectedPopularityMap);

        // un-favourite things and make sure the list changes again
        removeFavourite(expectedPopularityMap, 'd', 1);
        removeFavourite(expectedPopularityMap, 'c', 1);
        removeFavourite(expectedPopularityMap, 'b', 2);

        filterNavigation.popularFilters();
        assertPopularityAndListOrder(expectedPopularityMap);

        // ---------------------------------------
        // now login as wife and not only favourite a few things but change the order
        // ---------------------------------------
        navigation.logout();
        navigation.login("wife");

        // we now have to build our expected map in a specific insertion order!
        expectedPopularityMap = new LinkedHashMap();
        addExpectedPopularityToMap(expectedPopularityMap, 'a', 3);
        addExpectedPopularityToMap(expectedPopularityMap, 'b', 2);
        addFavourite(expectedPopularityMap, 't', 2);
        // then the rest should all be 1
        setPopularityMapToOne(expectedPopularityMap);

        filterNavigation.popularFilters();
        assertPopularityAndListOrder(expectedPopularityMap);

        // now reverse that change
        expectedPopularityMap = new LinkedHashMap();
        addExpectedPopularityToMap(expectedPopularityMap, 'a', 3);
        addExpectedPopularityToMap(expectedPopularityMap, 'b', 2);
        // then the rest should all be 1
        setPopularityMapToOne(expectedPopularityMap);
        removeFavourite(expectedPopularityMap, 't', 1);

        filterNavigation.popularFilters();
        assertPopularityAndListOrder(expectedPopularityMap);

        if (testAnonymous)
        {
            // ---------------------------------------
            // become the anonymous user
            // ---------------------------------------
            navigation.logout();
            filterNavigation.popularFilters();
            assertPopularityAndListOrder(expectedPopularityMap);
        }
    }

    private void assertPopularityAndListOrder(final LinkedHashMap expectedPopularityMap)
    {
        FilterList actualList = parse.filter().parseFilterList(POPULAR_TABLE_ID);
        List<FilterItem> filterItems = actualList.getFilterItems();
        for (FilterItem filterItem : filterItems)
        {
            String name = filterItem.getName();
            Long actualPopularity = filterItem.getFavCount();
            Long expectedPopularity = (Long) expectedPopularityMap.get(name);

            assertEquals("Asserting has the right popularity for : " + name, expectedPopularity, actualPopularity);
        }
        // the map is also in expected order (LinkedHashMap)
        int i = 0;
        for (Iterator iterator = expectedPopularityMap.keySet().iterator(); iterator.hasNext(); i++)
        {
            FilterItem filterItem = (FilterItem) actualList.getFilterItems().get(i);
            String expectedName = (String) iterator.next();
            assertEquals("Asserting order of filter items order : " + i, expectedName, filterItem.getName());
        }
    }

    private void addFavourite(final LinkedHashMap expectedPopularityMap, final char filterName, final long expectedPopularity)
    {
        addFavourite(filterName);
        expectedPopularityMap.put(Character.toString(filterName), expectedPopularity);
    }

    private void removeFavourite(final LinkedHashMap expectedPopularityMap, final char filterName, final long expectedPopularity)
    {
        removeFavourite(filterName);
        expectedPopularityMap.put(Character.toString(filterName), expectedPopularity);
    }

    private void setPopularityMapToOne(LinkedHashMap expectedPopularityMap)
    {
        for (int i = 'a'; i <= 't'; i++)
        {
            final String key = Character.toString((char) i);
            if (!expectedPopularityMap.containsKey(key))
            {
                addExpectedPopularityToMap(expectedPopularityMap, (char) i, 1);
            }
        }
    }

    private void addExpectedPopularityToMap(final LinkedHashMap expectedPopularityMap, char filterName, long expectedPopularity)
    {
        expectedPopularityMap.put(Character.toString(filterName), expectedPopularity);
    }

    private void addFavourite(char filterName)
    {
        // filter a is 10005
        int id = getEntityId(filterName);
        navigation.manageFilters().addFavourite(id);
    }

    private void removeFavourite(char filterName)
    {
        int id = getEntityId(filterName);
        navigation.manageFilters().removeFavourite(id);
    }

    private int getEntityId(final char filterName)
    {
        // filter named a is entity id 10005
        return (10005) + Math.abs('a' - filterName);
    }
}
