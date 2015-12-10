package com.atlassian.jira.webtests.zsuites;

import com.atlassian.jira.functest.framework.FuncTestSuite;
import com.atlassian.jira.webtests.ztests.filter.TestDanglingGroups;
import com.atlassian.jira.webtests.ztests.filter.TestEditFilterInvalidShares;
import com.atlassian.jira.webtests.ztests.filter.TestFavouriteAndMyFilters;
import com.atlassian.jira.webtests.ztests.filter.TestFilterActions;
import com.atlassian.jira.webtests.ztests.filter.TestFilterHoldsItsSorting;
import com.atlassian.jira.webtests.ztests.filter.TestFilterPicker;
import com.atlassian.jira.webtests.ztests.filter.TestFilterRelatedEntitiesDelete;
import com.atlassian.jira.webtests.ztests.filter.TestFilterSortUserRename;
import com.atlassian.jira.webtests.ztests.filter.TestFilterSubscription;
import com.atlassian.jira.webtests.ztests.filter.TestFilterSubscriptionXss;
import com.atlassian.jira.webtests.ztests.filter.TestFilterWarnings;
import com.atlassian.jira.webtests.ztests.filter.TestPopularFilters;
import com.atlassian.jira.webtests.ztests.filter.TestSearchFilters;
import com.atlassian.jira.webtests.ztests.filter.TestSearchFiltersShareType;
import com.atlassian.jira.webtests.ztests.filter.management.TestChangeSharedFilterOwnerByAdmins;
import com.atlassian.jira.webtests.ztests.filter.management.TestDeleteSharedFilterByAdmins;
import com.atlassian.jira.webtests.ztests.filter.management.TestSharedFilterSearchingByAdmins;
import com.atlassian.jira.webtests.ztests.navigator.TestSearchRequestViewSecurity;
import junit.framework.Test;

/**
 * A func test suite for Filters
 *
 * @since v4.0
 */
public class FuncTestSuiteFilters extends FuncTestSuite
{
    /**
     * A static declaration of this particular FuncTestSuite
     */
    public static final FuncTestSuite SUITE = new FuncTestSuiteFilters();

    /**
     * The pattern in JUnit/IDEA JUnit runner is that if a class has a static suite() method that returns a Test, then
     * this is the entry point for running your tests.  So make sure you declare one of these in the FuncTestSuite
     * implementation.
     *
     * @return a Test that can be run by as JUnit TestRunner
     */
    public static Test suite()
    {
        return SUITE.createTest();
    }

    public FuncTestSuiteFilters()
    {
        addTest(TestFilterSortUserRename.class);
        addTest(TestDanglingGroups.class);
        addTest(TestFilterRelatedEntitiesDelete.class);
        addTest(TestFavouriteAndMyFilters.class);
        addTest(TestPopularFilters.class);
        addTest(TestSearchFilters.class);
        addTest(TestSearchFiltersShareType.class);
        addTest(TestFilterActions.class);
        addTest(TestFilterSubscription.class);
        addTest(TestFilterSubscriptionXss.class);
        addTest(TestEditFilterInvalidShares.class);
        addTest(TestSearchRequestViewSecurity.class);
        addTest(TestFilterHoldsItsSorting.class);
        addTest(TestFilterPicker.class);
        addTest(TestFilterWarnings.class);
        addTest(TestSharedFilterSearchingByAdmins.class);
        addTest(TestDeleteSharedFilterByAdmins.class);
        addTest(TestChangeSharedFilterOwnerByAdmins.class);
    }
}
