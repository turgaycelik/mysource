package com.atlassian.jira.webtests.ztests.filter.management;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.parser.filter.FilterItem;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.google.common.collect.ImmutableList;

import java.util.List;

import static com.google.common.base.Predicates.in;
import static com.google.common.base.Predicates.not;
import static com.google.common.collect.Iterables.all;

/**
 * Responsible for verifying that an administrator is able to search for all the shared filters in JIRA.
 *
 * @since v4.4.1
 */
@WebTest ({ Category.FUNC_TEST, Category.FILTERS })
public class TestDeleteSharedFilterByAdmins extends FuncTestCase
{
    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        administration.restoreData("TestSharedFilterSearchingByAdmins.xml");
    }

    public void testAnAdminIsAbleToDeleteFiltersSharedWithAGroupHeDoesNotBelongTo()
    {
        final List<FilterItem> expectedFilterItems =
                ImmutableList.of
                        (
                                new FilterItem.Builder().
                                        id(10006).
                                        name("Shared Filter With Group jira-developers owned by developer").
                                        owner("Developer")
                                .build()
                        );

        final List<FilterItem> actualFilterItems = administration.sharedFilters().goTo().searchAll().
                deleteFilter(10006).filters().list();

        assertNotNull(actualFilterItems);
        assertTrue(actualFilterItems.size() > 0);
        assertTrue(all(expectedFilterItems, not(in(actualFilterItems))));
    }

    public void testAnAdminIsAbleToDeleteFiltersSharedWithAGroupHeBelongsTo() throws Exception
    {
        final List<FilterItem> expectedFilterItems =
                ImmutableList.of
                        (
                                new FilterItem.Builder().
                                        id(10010).
                                        name("Shared Filter with Anyone owned by developer").
                                        owner("Developer")
                                .build(),
                                new FilterItem.Builder().
                                        id(10009).
                                        name("Shared Filter With group jira-administrators owned by admin").
                                        owner("Administrator")
                                .build(),
                                new FilterItem.Builder().
                                        id(10002).
                                        name("Shared Filter With Group jira-users owned by fred").
                                        owner("Fred Normal")
                                .build()
                        );

        final List<FilterItem> actualFilterItems = administration.sharedFilters().goTo().searchAll().
                deleteFilter(10010).deleteFilter(10009).deleteFilter(10002).filters().list();

        assertNotNull(actualFilterItems);
        assertTrue(actualFilterItems.size() > 0);
        assertTrue(all(expectedFilterItems, not(in(actualFilterItems))));
    }

    public void testAnAdminIsAbleToDeleteFiltersSharedWithARoleHeIsNotPartOf() throws Exception
    {
        final List<FilterItem> expectedFilterItems =
                ImmutableList.of
                        (
                                new FilterItem.Builder().
                                        id(10004).
                                        name("Shared Filter With Role Developers on homosapien owned by developer").
                                        owner("Developer")
                                .build()
                        );

        final List<FilterItem> actualFilterItems = administration.sharedFilters().goTo().searchAll().deleteFilter(10004).filters().list();

        assertNotNull(actualFilterItems);
        assertTrue(actualFilterItems.size() > 0);
        assertTrue(all(expectedFilterItems, not(in(actualFilterItems))));
    }

    public void testAnAdminIsAbleToDeleteFiltersSharedWithARoleHeIsPartOf() throws Exception
    {
        final List<FilterItem> expectedFilterItems =
                ImmutableList.of
                        (
                                new FilterItem.Builder().
                                        id(10001).
                                        name("Shared Filter With Role Users on homosapien owned by fred").
                                        owner("Fred Normal")
                                .build(),
                                new FilterItem.Builder().
                                        id(10007).
                                        name("Shared Filter With All Roles on monkey owned by developer").
                                        owner("Developer")
                                .build(),
                                new FilterItem.Builder().
                                        id(10005).
                                        name("Shared Filter With Role Developers on monkey owned by developer").
                                        owner("Developer")
                                .build()
                        );

        final List<FilterItem> actualFilterItems = administration.sharedFilters().goTo().searchAll().
                deleteFilter(10005).deleteFilter(10001).deleteFilter(10007).filters().list();

        assertNotNull(actualFilterItems);
        assertTrue(actualFilterItems.size() > 0);
        assertTrue(all(expectedFilterItems, not(in(actualFilterItems))));
    }

}
