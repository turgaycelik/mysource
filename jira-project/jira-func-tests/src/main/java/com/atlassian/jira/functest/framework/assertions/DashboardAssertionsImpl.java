package com.atlassian.jira.functest.framework.assertions;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.jira.functest.framework.AbstractNavigationUtil;
import com.atlassian.jira.functest.framework.Dashboard;
import com.atlassian.jira.functest.framework.HtmlPage;
import com.atlassian.jira.functest.framework.dashboard.DashboardPageInfo;
import com.atlassian.jira.functest.framework.dashboard.DashboardPagePortletInfo;
import com.atlassian.jira.functest.framework.locator.Locator;
import com.atlassian.jira.functest.framework.locator.XPathLocator;
import com.atlassian.jira.functest.framework.sharing.SharedEntityInfo;
import com.atlassian.jira.functest.framework.sharing.TestSharingPermission;
import com.atlassian.jira.functest.framework.util.dom.DomKit;
import com.atlassian.jira.webtests.util.JIRAEnvironmentData;
import com.meterware.httpunit.TableCell;
import com.meterware.httpunit.WebLink;
import com.meterware.httpunit.WebTable;
import junit.framework.Assert;
import net.sourceforge.jwebunit.WebTester;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * And implementation of DashboardAssertions
 *
 * @since v3.13
 */
public class DashboardAssertionsImpl extends AbstractNavigationUtil implements DashboardAssertions
{
    private static final Pattern PATTERN_FAVCOUNT = Pattern.compile("(\\d+)\\s+\\d+");
    private HtmlPage page;
    private final URLAssertions urlAssertions;

    public DashboardAssertionsImpl(WebTester tester, JIRAEnvironmentData environmentData, URLAssertions urlAssertions)
    {
        super(tester, environmentData);
        this.urlAssertions = urlAssertions;
        page = new HtmlPage(tester);
    }

    public void assertDashboardPortlets(Long id, DashboardPagePortletInfo dashboardPortletInfo)
    {
        getNavigation().dashboard().navigateToFullConfigure(id);

        assertPortletsOnCurrentPage(dashboardPortletInfo);
    }

    public void assertDefaultDashboardPortlets(final DashboardPagePortletInfo dashboardPortletInfo)
    {
        getNavigation().dashboard().navigateToDefaultFullConfigure();

        assertPortletsOnCurrentPage(dashboardPortletInfo);
    }

    private void assertPortletsOnCurrentPage(final DashboardPagePortletInfo dashboardPortletInfo)
    {
        tester.setWorkingForm("jiraform");
        List leftOptions = EasyList.build(tester.getDialog().getOptionsFor("selectedLeftPortlets"));
        Assert.assertEquals("Portlets on left did not match.", dashboardPortletInfo.getLeftPortlets(), leftOptions);
        List rightOptions = EasyList.build(tester.getDialog().getOptionsFor("selectedRightPortlets"));
        Assert.assertEquals("Portlets on right did not match.", dashboardPortletInfo.getRightPortlets(), rightOptions);
    }

    public void assertColumns(final List<String> colHeaders, final Locator dashboardsLocator)
    {
        Assert.assertEquals(colHeaders, getColumnHeaders(dashboardsLocator));
    }

    public void assertDashboardPages(final List<? extends SharedEntityInfo> pages, final Dashboard.Table table)
    {
        WebTable webTable;
        try
        {
            webTable = tester.getDialog().getResponse().getTableWithID(table.getTableId());
        }
        catch (SAXException e)
        {
            Assert.fail("Unable to locate table '" + table + "'.");
            return;
        }

        Assert.assertEquals("Dashboard incorrect number of pages in table '" + table + "'.", pages.size(), webTable.getRowCount() - 1);

        final int columns = webTable.getColumnCount();
        final Map<String, Integer> columnMap = new HashMap<String, Integer>();
        for (int i = 0; i < columns; i++)
        {
            final String text = webTable.getCellAsText(0, i);
            if (text != null)
            {
                columnMap.put(text.trim(), i);
            }
        }

        XPathLocator locator = new XPathLocator(tester, table.toXPath() + "/tbody/tr");
        Node[] nodes = locator.getNodes();

        int row = 1;
        for (Iterator iterator = pages.iterator(); iterator.hasNext(); row++)
        {
            SharedEntityInfo sharedEntityInfo = (SharedEntityInfo) iterator.next();
            if (sharedEntityInfo.getId() != null)
            {
                checkId(sharedEntityInfo, nodes[row - 1], row);
            }
            if (StringUtils.isNotEmpty(sharedEntityInfo.getName()))
            {
                checkNameColumn(sharedEntityInfo, webTable, row, getColumnNumber("Name", columnMap));
            }
            if (StringUtils.isNotEmpty(sharedEntityInfo.getOwner()))
            {
                checkAuthorColumn(sharedEntityInfo, webTable, row, getColumnNumber("Owner", columnMap));
            }
            if (sharedEntityInfo.getSharingPermissions() != null)
            {
                checkShares(sharedEntityInfo, webTable, row, getColumnNumber("Shared With", columnMap));
            }
            if (sharedEntityInfo.getFavCount() != null)
            {
                checkFavouriteCount(sharedEntityInfo, webTable, row, getColumnNumber("Popularity", columnMap));
            }
            if (sharedEntityInfo instanceof DashboardPageInfo)
            {
                final DashboardPageInfo dashboardPageInfo = (DashboardPageInfo) sharedEntityInfo;
                if (dashboardPageInfo.getOperations() != null)
                {
                    checkOperations(dashboardPageInfo, webTable, row, getColumnNumber("" /* Operations */, columnMap));
                }
            }
        }
    }

    private void checkId(final SharedEntityInfo sharedEntityInfo, final Node node, final int row)
    {
        long id = sharedEntityInfo.getId();
        final Attr idAttribute = (Attr) node.getAttributes().getNamedItem("id");
        Assert.assertNotNull("Expected page with id '" + id + "' in row '" + row + "'.", idAttribute);
        Assert.assertEquals("Expected page with id '" + id + "' in row '" + row + "'.", "pp_" + id, idAttribute.getValue());
    }

    private void checkOperations(final DashboardPageInfo dashboardPageInfo, final WebTable webTable, final int row, final int column)
    {
        TableCell cell = webTable.getTableCell(row, column);
        for (DashboardPageInfo.Operation operation : DashboardPageInfo.Operation.ALL)
        {
            if (dashboardPageInfo.getOperations().contains(operation))
            {
                final WebLink webLink = cell.getLinkWith(operation.getLinkName());
                Assert.assertNotNull("Dasboard at row '" + row + "' does not appear to have operation '" + operation + "'.", webLink);

                final String expectedLink = page.addXsrfToken(operation.getUrl(dashboardPageInfo.getId()));
                urlAssertions.assertURLAreSimilair("Operation '" + operation + "' for dashboard at row '" + row + "' does not link to '" + expectedLink + "' (got link '" + webLink.getURLString() + "').",
                        expectedLink, webLink.getURLString());
            }
            else
            {
                final WebLink webLink = cell.getLinkWith(operation.getLinkName());
                Assert.assertNull("Dasboard at row '" + row + "' does appears to have operation '" + operation + "'.", webLink);
            }
        }
    }

    private void checkFavouriteCount(final SharedEntityInfo dashboardPageInfo, final WebTable webTable, final int row, final int column)
    {
        final String text = webTable.getCellAsText(row, column).trim();
        final Matcher matcher = PATTERN_FAVCOUNT.matcher(text);
        if (matcher.find())
        {
            try
            {
                int count = Integer.parseInt(matcher.group(1));
                Assert.assertEquals("Dashboard at row " + row + " does not appear to have a favourite count of " + dashboardPageInfo.getFavCount() + ".",
                        dashboardPageInfo.getFavCount().intValue(), count);
            }
            catch (NumberFormatException e)
            {
                Assert.fail("Dashboard at row " + row + " does not appear to have a favourite count of " + dashboardPageInfo.getFavCount() + ".");
            }
        }
        else
        {
            Assert.fail("Dashboard at row " + row + " does not appear to have a favourite count of " + dashboardPageInfo.getFavCount() + ".");
        }


    }

    private void checkShares(final SharedEntityInfo dashboardPageInfo, final WebTable webTable, final int row, final int column)
    {
        final String text = webTable.getCellAsText(row, column).trim();
        final Set<? extends TestSharingPermission> permissions = dashboardPageInfo.getSharingPermissions();
        // check sharing
        if (permissions.isEmpty())
        {
            Assert.assertTrue("Dashboard at row " + row + " does not appear to be shared privately.", text.indexOf("Private") >= 0);
        }
        else
        {
            for (TestSharingPermission testSharingPermission : permissions)
            {
                Assert.assertTrue("Dashboard at row " + row + " does not appear to shared as '" + testSharingPermission + "'.",
                        text.indexOf(testSharingPermission.toDisplayFormat()) >= 0);
            }
        }
    }

    private void checkAuthorColumn(final SharedEntityInfo dashboardPageInfo, final WebTable webTable, final int row, final int column)
    {
        final String text = webTable.getCellAsText(row, column).trim();
        Assert.assertTrue("Dashboard at row " + row + " does not appear to owned by '" + dashboardPageInfo.getOwner() + "'.",
                text.indexOf(dashboardPageInfo.getOwner()) >= 0);
    }

    private static void checkNameColumn(final SharedEntityInfo dashboardPageInfo, final WebTable webTable, final int row, final int column)
    {
        final String text = webTable.getCellAsText(row, column).trim();
        Assert.assertTrue("Dashboard at row " + row + " does not appear to be called '" + dashboardPageInfo.getName() + "'.",
                text.indexOf(dashboardPageInfo.getName()) >= 0);

        final TableCell tableCell = webTable.getTableCell(row, column);
        if (dashboardPageInfo.isFavourite())
        {
            Assert.assertTrue("Dashboard at row " + row + " does not appear to be a favourite'.",
                    tableCell.getLinks()[0].getTitle() != null && tableCell.getLinks()[0].getTitle().contains("Remove this dashboard from your favourites"));
        }
        else
        {
            Assert.assertTrue("Dashboard at row " + row + " appears to be a favourite'.",
                    tableCell.getLinks()[0].getTitle() != null && tableCell.getLinks()[0].getTitle().contains("Add this dashboard to your favourites"));
        }

        if (StringUtils.isNotEmpty(dashboardPageInfo.getDescription()))
        {
            Assert.assertTrue("Dashboard at row " + row + " does not have description '" + dashboardPageInfo.getDescription() + "'.",
                    text.indexOf(dashboardPageInfo.getDescription()) >= 0);
        }
    }

    private static int getColumnNumber(final String columnName, final Map /*<String, Integer>*/ columnMap)
    {
        final Integer column = (Integer) columnMap.get(columnName);
        if (column == null)
        {
            Assert.fail("Dashboard table does not appear to have a '" + columnName + "' column.");
        }
        return column;
    }

    private List<String> getColumnHeaders(final Locator dashboardsLocator)
    {
        Node[] colHeaders = getHeaderColumnsLocator(dashboardsLocator).getNodes();
        List<String> results = new ArrayList<String>(colHeaders.length);
        for (final Node colHeader : colHeaders)
        {
            final String o = DomKit.getCollapsedText(colHeader);
            results.add(o != null ? o.trim() : null);
        }

        return results;
    }

    private Locator getHeaderColumnsLocator(final Locator dashboardsLocator)
    {
        final XPathLocator rowLocator = new XPathLocator(dashboardsLocator.getNode(), "thead/tr[1]");
        final Node row = rowLocator.getNode();
        return new XPathLocator(row, "th");
    }
}
