package com.atlassian.jira.webtests.ztests.dashboard;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.jira.functest.framework.Dashboard;
import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.assertions.DashboardAssertions;
import com.atlassian.jira.functest.framework.locator.Locator;
import com.atlassian.jira.functest.framework.locator.XPathLocator;
import com.atlassian.jira.functest.framework.sharing.SharedEntityInfo;
import com.atlassian.jira.functest.framework.sharing.TestSharingPermissionUtils;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import net.sourceforge.jwebunit.WebTester;
import org.w3c.dom.Node;

import java.util.List;

/**
 * Test to ensure that PortalPage ordering works.
 *
 * @since v3.13
 */
@WebTest ({ Category.FUNC_TEST, Category.DASHBOARDS })
public class TestReorderDashboardPages extends FuncTestCase
{
    private static final SharedEntityInfo PAGE_FRED_PUBLIC = new SharedEntityInfo(10011L, "PublicFredDashboard", "This is a dashboard page that can be seen by everyone.", true, TestSharingPermissionUtils.createPublicPermissions());

    private static final SharedEntityInfo PAGE_EXISTS = new SharedEntityInfo(10012L, "Exists", null, true, TestSharingPermissionUtils.createPrivatePermissions());
    private static final SharedEntityInfo PAGE_ADMINNOTFAVOURITE = new SharedEntityInfo(10013L, "AdminNotFavourite", null, false, TestSharingPermissionUtils.createPublicPermissions());
    private static final SharedEntityInfo PAGE_ADMINFAVOURITE = new SharedEntityInfo(10014L, "AdminFavourite", null, true, TestSharingPermissionUtils.createPublicPermissions());


    protected void setUpTest()
    {
        administration.restoreData("BaseProfessionalPortalPage.xml");
    }

    /**
     * Make sure that move down works as expected.
     */
    public void testMoveDown()
    {
        final Dashboard dashboard = navigation.dashboard();
        dashboard.favouriteDashboard(PAGE_FRED_PUBLIC.getId());

        dashboard.navigateToFavourites();
        assertPages(EasyList.build(PAGE_EXISTS, PAGE_ADMINFAVOURITE, PAGE_FRED_PUBLIC));

        //move the EXISTS page down.
        tester.clickLink(generateDownId(0));
        assertPages(EasyList.build(PAGE_ADMINFAVOURITE, PAGE_EXISTS, PAGE_FRED_PUBLIC));

        //move the EXISTS page down again.
        tester.clickLink(generateDownId(1));
        assertPages(EasyList.build(PAGE_ADMINFAVOURITE, PAGE_FRED_PUBLIC, PAGE_EXISTS));

        //move the admin favourite page down.
        tester.clickLink(generateDownId(0));
        assertPages(EasyList.build(PAGE_FRED_PUBLIC, PAGE_ADMINFAVOURITE, PAGE_EXISTS));

        //move the Fred favourite page down. Test to make sure that PortalPages not owned can be moved.
        tester.clickLink(generateDownId(0));
        assertPages(EasyList.build(PAGE_ADMINFAVOURITE, PAGE_FRED_PUBLIC, PAGE_EXISTS));

        dashboard.unFavouriteDashboard(PAGE_EXISTS.getId());
        dashboard.navigateToFavourites();

        //make sure that the page has disappeared.
        assertPages(EasyList.build(PAGE_ADMINFAVOURITE, PAGE_FRED_PUBLIC));

        tester.clickLink(generateDownId(0));

        //make sure reorder works with only two elements.
        assertPages(EasyList.build(PAGE_FRED_PUBLIC, PAGE_ADMINFAVOURITE));

        dashboard.unFavouriteDashboard(PAGE_FRED_PUBLIC.getId());
        dashboard.navigateToFavourites();

        assertPages(EasyList.build(PAGE_ADMINFAVOURITE));
    }

    /**
     * Make sure down reordering works when moving page past one that is no longer a favourite.
     */
    public void testMoveDownFavouriteGap()
    {
        _testMoveFavouriteGap(new MoveDownCommand());
    }

    /**
     * Make sure down reordering works when moving page past one that is no longer shared.
     */
    public void testMoveDownPermissionGap()
    {
        _testMovePastPermissionGap(new MoveDownCommand());
    }

    /**
     * Make sure that move up works as expected.
     */
    public void testMoveUp()
    {
        final Dashboard dashboard = navigation.dashboard();
        dashboard.favouriteDashboard(PAGE_FRED_PUBLIC.getId());

        dashboard.navigateToFavourites();
        assertPages(EasyList.build(PAGE_EXISTS, PAGE_ADMINFAVOURITE, PAGE_FRED_PUBLIC));

        //move the fred page up.
        tester.clickLink(generateUpId(2));
        assertPages(EasyList.build(PAGE_EXISTS, PAGE_FRED_PUBLIC, PAGE_ADMINFAVOURITE));

        //move the fred page up again.
        tester.clickLink(generateUpId(1));
        assertPages(EasyList.build(PAGE_FRED_PUBLIC, PAGE_EXISTS, PAGE_ADMINFAVOURITE));

        //move fred page up.
        tester.clickLink(generateUpId(1));
        assertPages(EasyList.build(PAGE_EXISTS, PAGE_FRED_PUBLIC, PAGE_ADMINFAVOURITE));

        dashboard.unFavouriteDashboard(PAGE_EXISTS.getId());
        dashboard.navigateToFavourites();

        //make sure that the page has disappeared.
        assertPages(EasyList.build(PAGE_FRED_PUBLIC, PAGE_ADMINFAVOURITE));

        tester.clickLink(generateUpId(1));

        //make sure reorder works with only two elements.
        assertPages(EasyList.build(PAGE_ADMINFAVOURITE, PAGE_FRED_PUBLIC));

        dashboard.unFavouriteDashboard(PAGE_FRED_PUBLIC.getId());
        dashboard.navigateToFavourites();

        assertPages(EasyList.build(PAGE_ADMINFAVOURITE));
    }

    /**
     * Make sure up reordering works when moving page past one that is no longer a favourite.
     */
    public void testMoveUpFavouriteGap()
    {
        _testMoveFavouriteGap(new MoveUpCommand());
    }

    /**
     * Make sure up reordering works when moving page past one that is no longer shared.
     */
    public void testMoveUpPermissionGap()
    {
        _testMovePastPermissionGap(new MoveUpCommand());
    }

    /**
     * Make sure that move start works as expected.
     */
    public void testMoveStart()
    {
        final Dashboard dashboard = navigation.dashboard();
        dashboard.favouriteDashboard(PAGE_FRED_PUBLIC.getId());

        dashboard.navigateToFavourites();
        assertPages(EasyList.build(PAGE_EXISTS, PAGE_ADMINFAVOURITE, PAGE_FRED_PUBLIC));

        //move the fred page to start.
        tester.clickLink(generateFirstId(2));
        assertPages(EasyList.build(PAGE_FRED_PUBLIC, PAGE_EXISTS, PAGE_ADMINFAVOURITE));

        //move the exists page to the start.
        tester.clickLink(generateFirstId(1));
        assertPages(EasyList.build(PAGE_EXISTS, PAGE_FRED_PUBLIC, PAGE_ADMINFAVOURITE));

        //move the admin favourite page to the start.
        tester.clickLink(generateFirstId(2));
        assertPages(EasyList.build(PAGE_ADMINFAVOURITE, PAGE_EXISTS, PAGE_FRED_PUBLIC));

        dashboard.unFavouriteDashboard(PAGE_EXISTS.getId());
        dashboard.navigateToFavourites();

        //make sure that the page has disappeared.
        assertPages(EasyList.build(PAGE_ADMINFAVOURITE, PAGE_FRED_PUBLIC));

        tester.clickLink(generateFirstId(1));

        //make sure reorder works with only two elements.
        assertPages(EasyList.build(PAGE_FRED_PUBLIC, PAGE_ADMINFAVOURITE));

        dashboard.unFavouriteDashboard(PAGE_FRED_PUBLIC.getId());
        dashboard.navigateToFavourites();

        assertPages(EasyList.build(PAGE_ADMINFAVOURITE));
    }

    /**
     * Make sure up reordering works when moving page past one that is no longer a favourite.
     */
    public void testMoveStartPastFavouriteGap()
    {
        _testMoveFavouriteGap(new FirstReorderCommand());
    }

    /**
     * Make sure up reordering works when moving page past one that is no longer shared.
     */
    public void testMoveStartPastPermissionGap()
    {
        _testMovePastPermissionGap(new FirstReorderCommand());
    }

    /**
     * Make sure that move to end works as expected.
     */
    public void testMoveEnd()
    {
        final Dashboard dashboard = navigation.dashboard();
        dashboard.favouriteDashboard(PAGE_FRED_PUBLIC.getId());

        dashboard.navigateToFavourites();
        assertPages(EasyList.build(PAGE_EXISTS, PAGE_ADMINFAVOURITE, PAGE_FRED_PUBLIC));

        //move the exists page to end.
        tester.clickLink(generateLastId(0));
        assertPages(EasyList.build(PAGE_ADMINFAVOURITE, PAGE_FRED_PUBLIC, PAGE_EXISTS));

        //move fred to last.
        tester.clickLink(generateLastId(1));
        assertPages(EasyList.build(PAGE_ADMINFAVOURITE, PAGE_EXISTS, PAGE_FRED_PUBLIC));

        //move admin favourite to the end.
        tester.clickLink(generateLastId(0));
        assertPages(EasyList.build(PAGE_EXISTS, PAGE_FRED_PUBLIC, PAGE_ADMINFAVOURITE));

        dashboard.unFavouriteDashboard(PAGE_EXISTS.getId());
        dashboard.navigateToFavourites();

        //make sure that the page has disappeared.
        assertPages(EasyList.build(PAGE_FRED_PUBLIC, PAGE_ADMINFAVOURITE));

        tester.clickLink(generateLastId(0));

        //make sure reorder works with only two elements.
        assertPages(EasyList.build(PAGE_ADMINFAVOURITE, PAGE_FRED_PUBLIC));

        dashboard.unFavouriteDashboard(PAGE_ADMINFAVOURITE.getId());
        dashboard.navigateToFavourites();

        assertPages(EasyList.build(PAGE_FRED_PUBLIC));
    }

    /**
     * Make sure up reordering works when moving page past one that is no longer a favourite.
     */
    public void testMoveEndPastFavouriteGap()
    {
        _testMoveFavouriteGap(new LastReorderCommand());
    }

    /**
     * Make sure up reordering works when moving page past one that is no longer shared.
     */
    public void testMoveEndPastPermissionGap()
    {
        _testMovePastPermissionGap(new LastReorderCommand());
    }

    private void _testMoveFavouriteGap(final ReorderCommand reorderCommand)
    {
        final SharedEntityInfo newFavourite = new SharedEntityInfo(PAGE_ADMINNOTFAVOURITE);
        newFavourite.setFavourite(true);

        final Dashboard dashboard = navigation.dashboard();
        dashboard.favouriteDashboard(newFavourite.getId());

        dashboard.navigateToFavourites();
        assertPages(EasyList.build(PAGE_EXISTS, PAGE_ADMINFAVOURITE, newFavourite));

        //move not favourite to middle.
        tester.clickLink(generateUpId(2));
        assertPages(EasyList.build(PAGE_EXISTS, newFavourite, PAGE_ADMINFAVOURITE));

        //unfavourite the middle page.
        dashboard.unFavouriteDashboard(newFavourite.getId());
        dashboard.navigateToFavourites();
        assertPages(EasyList.build(PAGE_EXISTS, PAGE_ADMINFAVOURITE));

        //make sure that the middle page is ignored. Assumes the command will swap the pages.
        reorderCommand.reorder(tester);
        assertPages(EasyList.build(PAGE_ADMINFAVOURITE, PAGE_EXISTS));
    }

    private void _testMovePastPermissionGap(final ReorderCommand reorderCommand)
    {
        final Dashboard dashboard = navigation.dashboard();
        dashboard.favouriteDashboard(PAGE_FRED_PUBLIC.getId());

        dashboard.navigateToFavourites();
        assertPages(EasyList.build(PAGE_EXISTS, PAGE_ADMINFAVOURITE, PAGE_FRED_PUBLIC));

        //move admin favourite down.
        tester.clickLink(generateDownId(1));
        assertPages(EasyList.build(PAGE_EXISTS, PAGE_FRED_PUBLIC, PAGE_ADMINFAVOURITE));

        makePagePrivate(dashboard, PAGE_FRED_PUBLIC);

        //make sure we can no longer see pages that are not shared.
        dashboard.navigateToFavourites();
        assertPages(EasyList.build(PAGE_EXISTS, PAGE_ADMINFAVOURITE));

        //make sure we can move across the gap. Assumes command will swap the pages.
        reorderCommand.reorder(tester);
        assertPages(EasyList.build(PAGE_ADMINFAVOURITE, PAGE_EXISTS));

        makePagePublic(dashboard, PAGE_FRED_PUBLIC);

        //the favourite should show up again at the end.
        dashboard.navigateToFavourites();
        assertPages(EasyList.build(PAGE_ADMINFAVOURITE, PAGE_EXISTS, PAGE_FRED_PUBLIC));
    }

    private SharedEntityInfo makePagePrivate(final Dashboard dashboard, final SharedEntityInfo page)
    {
        navigation.logout();
        navigation.login(FRED_USERNAME);

        final SharedEntityInfo privatePage = new SharedEntityInfo(page);
        privatePage.setSharingPermissions(TestSharingPermissionUtils.createPrivatePermissions());

        dashboard.editPage(privatePage);

        navigation.logout();
        navigation.login(ADMIN_USERNAME);

        return privatePage;
    }

    private SharedEntityInfo makePagePublic(final Dashboard dashboard, final SharedEntityInfo page)
    {
        navigation.logout();
        navigation.login(FRED_USERNAME);

        final SharedEntityInfo privatePage = new SharedEntityInfo(page);
        privatePage.setSharingPermissions(TestSharingPermissionUtils.createPublicPermissions());

        dashboard.editPage(privatePage);

        navigation.logout();
        navigation.login(ADMIN_USERNAME);

        return privatePage;
    }

    private void assertPages(final List /*<SharedEntityInfo>*/ pages)
    {
        final DashboardAssertions dashboardAssertions = assertions.getDashboardAssertions();
        final Locator dashboardLocator = createFavouriteLocator();

        dashboardAssertions.assertDashboardPages(pages, Dashboard.Table.FAVOURITE);
        assertReorderLinks(pages.size(), dashboardLocator);
    }

    private void assertReorderLinks(final int number, final Locator dashboardLocator)
    {
        //X-path for all the data rows of the table. Skips the first row which is the header.
        final XPathLocator rows = new XPathLocator(dashboardLocator.getNode(), "tbody/tr");
        final Node[] trNodes = rows.getNodes();

        assertEquals("Not enough rows in the table.", number, trNodes.length);

        for (int i = 0; i < trNodes.length; i++)
        {
            final Node currentRow = trNodes[i];

            final Locator firstLocator = createUrlLocator(currentRow, generateFirstId(i));
            final Locator upLocator = createUrlLocator(currentRow, generateUpId(i));
            assertNotNull("Expecting start arrow on row " + i, firstLocator.getNode());
            assertTrue("Expecting start arrow on row " + i, firstLocator.getText().startsWith(page.addXsrfToken("ConfigurePortalPages!moveToStart.jspa?pageId=")));

            assertNotNull("Expecting up arrow on row " + i, upLocator.getNode());
            assertTrue("Expecting up arrow on row " + i, upLocator.getText().startsWith(page.addXsrfToken("ConfigurePortalPages!moveUp.jspa?pageId=")));

            final Locator lastLocator = createUrlLocator(currentRow, generateLastId(i));
            final Locator downLocator = createUrlLocator(currentRow, generateDownId(i));
            assertNotNull("Expecting end arrow on row " + i, lastLocator.getNode());
            assertTrue("Expecting end arrow on row " + i, lastLocator.getText().startsWith(page.addXsrfToken("ConfigurePortalPages!moveToEnd.jspa?pageId=")));

            assertNotNull("Expecting down arrow on row " + i, downLocator.getNode());
            assertTrue("Expecting down arrow on row " + i, downLocator.getText().startsWith(page.addXsrfToken("ConfigurePortalPages!moveDown.jspa?pageId=")));
        }
    }

    private static String generateDownId(final long position)
    {
        return "pos_down_" + position;
    }

    private static String generateLastId(final long position)
    {
        return "pos_last_" + position;
    }

    private static String generateFirstId(final long position)
    {
        return "pos_first_" + position;
    }

    private static String generateUpId(final long position)
    {
        return "pos_up_" + position;
    }

    private static Locator createUrlLocator(final Node root, final String urlId)
    {
        return new XPathLocator(root, "//a[@id='" + urlId + "']/@href");
    }

    private Locator createFavouriteLocator()
    {
        return new XPathLocator(tester, Dashboard.Table.FAVOURITE.toXPath());
    }

    
    private interface ReorderCommand
    {
        public void reorder(WebTester tester);
    }

    private static class MoveDownCommand implements ReorderCommand
    {
        public void reorder(WebTester tester)
        {
            tester.clickLink(generateDownId(0));
        }
    }

    private static class MoveUpCommand implements ReorderCommand
    {
        public void reorder(final WebTester tester)
        {
            tester.clickLink(generateUpId(1));
        }
    }

    private static class FirstReorderCommand implements ReorderCommand
    {
        public void reorder(final WebTester tester)
        {
            tester.clickLink(generateFirstId(1));
        }
    }

    private static class LastReorderCommand implements ReorderCommand
    {
        public void reorder(final WebTester tester)
        {
            tester.clickLink(generateLastId(0));
        }
    }
}
