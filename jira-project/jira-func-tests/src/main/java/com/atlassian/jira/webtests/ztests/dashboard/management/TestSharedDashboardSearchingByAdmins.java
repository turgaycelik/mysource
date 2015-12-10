package com.atlassian.jira.webtests.ztests.dashboard.management;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.parser.dashboard.DashboardItem;
import com.atlassian.jira.functest.framework.parser.dashboard.DashboardItem.Builder;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.google.common.collect.ImmutableList;

import java.util.List;

import static com.google.common.base.Predicates.in;
import static com.google.common.base.Predicates.not;
import static com.google.common.collect.Iterables.all;

/**
 * Responsible for verifying that an administrator is able to search for all the shared Dashboards in JIRA.
 *
 * @since v4.4.1
 */
@WebTest ({ Category.FUNC_TEST, Category.DASHBOARDS })
public class TestSharedDashboardSearchingByAdmins extends FuncTestCase
{
    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        administration.restoreData("TestSharedDashboards.xml");
    }

    public void testAnAdminIsAbleToSearchForDashboardsSharedWithAGroupHeDoesNotBelongTo()
    {
        final List<DashboardItem> expectedDashboardItems =
                ImmutableList.of
                        (
                                new DashboardItem.Builder().
                                        id(10015).
                                        name("Shared Dashboard with group jira-developers owned by developer").
                                        owner("Developer (developer)")
                                .build()
                        );

        final List<DashboardItem> actualDashboardItems = administration.sharedDashboards().goTo().
                searchAll().dashboards().list();

        assertNotNull(actualDashboardItems);
        assertTrue(actualDashboardItems.size() > 0);
        assertTrue(all(expectedDashboardItems, in(actualDashboardItems)));
    }

    public void testAnAdminIsAbleToSearchForDashboardsSharedWithAGroupHeBelongsTo() throws Exception
    {
        final List<DashboardItem> expectedDashboardItems =
                ImmutableList.of
                        (
                                new DashboardItem.Builder().
                                        id(10014).
                                        name("Shared Dashboard with Anyone owned by developer").
                                        owner("Developer (developer)")
                                .build(),
                                new DashboardItem.Builder().
                                        id(10010).
                                        name("Shared Dashboard with group jira-administrators owned by admin").
                                        owner("Administrator (admin)")
                                .build(),
                                new DashboardItem.Builder().
                                        id(10018).
                                        name("Shared Dashboard with group jira-users owned by fred").
                                        owner("Fred Normal (fred)")
                                .build()
                        );

        final List<DashboardItem> actualDashboardItems = administration.sharedDashboards().goTo().
                searchAll().dashboards().list();

        assertNotNull(actualDashboardItems);
        assertTrue(actualDashboardItems.size() > 0);
        assertTrue(all(expectedDashboardItems, in(actualDashboardItems)));
    }

    public void testAnAdminIsAbleToSearchForDashboardsSharedWithARoleHeIsNotPartOf() throws Exception
    {
        final List<DashboardItem> expectedDashboardItems =
                ImmutableList.of
                        (
                                new DashboardItem.Builder().
                                        id(10016).
                                        name("Shared Dashboard with role Developers on homosapiens owned by developer").
                                        owner("Developer (developer)")
                                .build()
                        );

        final List<DashboardItem> actualDashboardItems = administration.sharedDashboards().goTo().
                searchAll().dashboards().list();

        assertNotNull(actualDashboardItems);
        assertTrue(actualDashboardItems.size() > 0);
        assertTrue(all(expectedDashboardItems, in(actualDashboardItems)));
    }

    public void testAnAdminIsAbleToSearchForDashboardsSharedWithARoleHeIsPartOf() throws Exception
    {
        final List<DashboardItem> expectedDashboardItems =
                ImmutableList.of
                        (
                                new DashboardItem.Builder().
                                        id(10019).
                                        name("Shared Dashboard with role Users on homosapien owned by fred").
                                        owner("Fred Normal (fred)")
                                .build(),
                                new DashboardItem.Builder().
                                        id(10013).
                                        name("Shared Dashboard with all roles on Monkey owned by developer").
                                        owner("Developer (developer)")
                                .build()
                        );

        final List<DashboardItem> actualDashboardItems = administration.sharedDashboards().goTo().
                searchAll().dashboards().list();

        assertNotNull(actualDashboardItems);
        assertTrue(actualDashboardItems.size() > 0);
        assertTrue(all(expectedDashboardItems, in(actualDashboardItems)));
    }

    public void testAnAdminIsNotAbleToSearchForDashboardsThatArePrivate() throws Exception
    {
        final List<DashboardItem> nonExpectedDashboardItems =
                ImmutableList.of
                        (
                                new DashboardItem.Builder().
                                        id(10011).
                                        name("Private Dashboard owned by admin").
                                        owner("Administrator (admin)")
                                .build()
                        );

        final List<DashboardItem> actualDashboardItems = administration.sharedDashboards().goTo().
                searchAll().dashboards().list();

        assertNotNull(actualDashboardItems);
        assertTrue(actualDashboardItems.size() > 0);
        assertTrue(all(nonExpectedDashboardItems, not(in(actualDashboardItems))));
    }
}
