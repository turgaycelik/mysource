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
public class TestDeleteSharedDashboardsByAdmins extends FuncTestCase
{
    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        administration.restoreData("TestSharedDashboards.xml");
    }

    public void testAnAdminIsAbleToDeleteDashboardsSharedWithAGroupHeDoesNotBelongTo()
    {
        final List<DashboardItem> expectedDashboardItems =
                ImmutableList.of
                        (
                                new Builder().
                                        id(10015).
                                        name("Shared Dashboard with group jira-developers owned by developer").
                                        owner("Developer (developer)")
                                .build()
                        );

        final List<DashboardItem> actualDashboardItems = administration.sharedDashboards().goTo().
                searchAll().deleteDashboard(10015).dashboards().list();

        assertNotNull(actualDashboardItems);
        assertTrue(actualDashboardItems.size() > 0);
        assertTrue(all(expectedDashboardItems, not(in(actualDashboardItems))));
    }

    public void testAnAdminIsAbleToDeleteDashboardsSharedWithAGroupHeBelongsTo() throws Exception
    {
        final List<DashboardItem> expectedDashboardItems =
                ImmutableList.of
                        (
                                new Builder().
                                        id(10014).
                                        name("Shared Dashboard with Anyone owned by developer").
                                        owner("Developer (developer)")
                                .build(),
                                new Builder().
                                        id(10010).
                                        name("Shared Dashboard with group jira-administrators owned by admin").
                                        owner("Administrator (admin)")
                                .build(),
                                new Builder().
                                        id(10018).
                                        name("Shared Dashboard with group jira-users owned by fred").
                                        owner("Fred Normal (fred)")
                                .build()
                        );

        final List<DashboardItem> actualDashboardItems = administration.sharedDashboards().goTo().
                searchAll().deleteDashboard(10014).deleteDashboard(10010).deleteDashboard(10018).dashboards().list();

        assertNotNull(actualDashboardItems);
        assertTrue(actualDashboardItems.size() > 0);
        assertTrue(all(expectedDashboardItems, not(in(actualDashboardItems))));
    }

    public void testAnAdminIsAbleToDeleteDashboardsSharedWithARoleHeIsNotPartOf() throws Exception
    {
        final List<DashboardItem> expectedDashboardItems =
                ImmutableList.of
                        (
                                new Builder().
                                        id(10016).
                                        name("Shared Dashboard with role Developers on homosapiens owned by developer").
                                        owner("Developer (developer)")
                                .build()
                        );

        final List<DashboardItem> actualDashboardItems = administration.sharedDashboards().goTo().
                searchAll().deleteDashboard(10016).dashboards().list();

        assertNotNull(actualDashboardItems);
        assertTrue(actualDashboardItems.size() > 0);
        assertTrue(all(expectedDashboardItems, not(in(actualDashboardItems))));
    }

    public void testAnAdminIsAbleToDeleteDashboardsSharedWithARoleHeIsPartOf() throws Exception
    {
        final List<DashboardItem> expectedDashboardItems =
                ImmutableList.of
                        (
                                new Builder().
                                        id(10019).
                                        name("Shared Dashboard with role Users on homosapien owned by fred").
                                        owner("Fred Normal (fred)")
                                .build(),
                                new Builder().
                                        id(10013).
                                        name("Shared Dashboard with all roles on Monkey owned by developer").
                                        owner("Developer (developer)")
                                .build()
                        );

        final List<DashboardItem> actualDashboardItems = administration.sharedDashboards().goTo().
                searchAll().deleteDashboard(10019).deleteDashboard(10013).dashboards().list();

        assertNotNull(actualDashboardItems);
        assertTrue(actualDashboardItems.size() > 0);
        assertTrue(all(expectedDashboardItems, not(in(actualDashboardItems))));
    }
}
