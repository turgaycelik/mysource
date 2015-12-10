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
public class TestChangeSharedFilterOwnerByAdmins extends FuncTestCase
{
    private final static String DEV1 = "developer";
    private final static String DEV2 = "anotherdev";
    private final static String USER = "Fred";

    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        administration.restoreData("TestChangeSharedFilterOwnerByAdmins.xml");
    }

    public void testAnAdminIsAbleToChangeOwnersOfFiltersSharedWithAGroupHeDoesNotBelongTo()
    {
        final List<FilterItem> expectedFilterItems =
                ImmutableList.of
                        (
                                new FilterItem.Builder().
                                        id(10006).
                                        name("Shared Filter With Group jira-developers owned by developer").
                                        owner("Another Developer")
                                .build()
                        );

        final List<FilterItem> actualFilterItems = administration.sharedFilters().goTo().searchAll().
                changeFilterOwner(10006, DEV2).filters().list();

        assertNotNull(actualFilterItems);
        assertTrue(actualFilterItems.size() > 0);
        assertTrue(all(expectedFilterItems, in(actualFilterItems)));
    }

    public void testAnAdminIsAbleToChangeOwnerOfFiltersSharedWithAGroupHeBelongsTo() throws Exception
    {
        final List<FilterItem> expectedFilterItems =
                ImmutableList.of
                        (
                                new FilterItem.Builder().
                                        id(10002).
                                        name("Shared Filter With Group jira-users owned by fred").
                                        owner("Developer")
                                .build()
                        );

        final List<FilterItem> actualFilterItems = administration.sharedFilters().goTo().searchAll().
                   changeFilterOwner(10002, DEV1).filters().list();

        assertNotNull(actualFilterItems);
        assertTrue(actualFilterItems.size() > 0);
        assertTrue(all(expectedFilterItems, in(actualFilterItems)));
    }

    public void testAnAdminIsAbleToChangeOwnerOfFiltersSharedWithARoleHeIsNotPartOf() throws Exception
    {
        final List<FilterItem> expectedFilterItems =
                ImmutableList.of
                        (
                                new FilterItem.Builder().
                                        id(10004).
                                        name("Shared Filter With Role Developers on homosapien owned by developer").
                                        owner("Another Developer")
                                .build()
                        );

        final List<FilterItem> actualFilterItems = administration.sharedFilters().goTo().searchAll().changeFilterOwner(10004, DEV2).filters().list();

        assertNotNull(actualFilterItems);
        assertTrue(actualFilterItems.size() > 0);
        assertTrue(all(expectedFilterItems, in(actualFilterItems)));
    }

    public void testAnAdminIsAbleToChangeOwnerOfFiltersSharedWithARoleHeIsPartOf() throws Exception
    {
        final List<FilterItem> expectedFilterItems =
                ImmutableList.of
                        (
                                new FilterItem.Builder().
                                        id(10001).
                                        name("Shared Filter With Role Users on homosapien owned by fred").
                                        owner("Developer")
                                .build()
                        );

        final List<FilterItem> actualFilterItems = administration.sharedFilters().goTo().searchAll().
                changeFilterOwner(10001, DEV1).filters().list();

        assertNotNull(actualFilterItems);
        assertTrue(actualFilterItems.size() > 0);
        assertTrue(all(expectedFilterItems, in(actualFilterItems)));
    }

    public void testAnAdminCannotChangeOwnerOfFiltersWhenNewOwnerDoesNotHaveSharePermission() throws Exception
    {
        final List<FilterItem> expectedFilterItems =
                ImmutableList.of
                        (
                               new FilterItem.Builder().
                                        id(10004).
                                        name("Shared Filter With Role Developers on homosapien owned by developer").
                                        owner("Fred Normal")
                                .build()
                        );

        final List<FilterItem> actualFilterItems = administration.sharedFilters().goTo().searchAll().
                changeFilterOwner(10004, USER).filters().list();

        assertNotNull(actualFilterItems);
        assertTrue(actualFilterItems.size() > 0);
        assertTrue(all(expectedFilterItems, not(in(actualFilterItems))));
    }

}
