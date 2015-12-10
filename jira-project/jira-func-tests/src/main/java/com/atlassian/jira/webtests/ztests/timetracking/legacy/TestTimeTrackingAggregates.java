package com.atlassian.jira.webtests.ztests.timetracking.legacy;

import com.atlassian.jira.functest.framework.locator.IdLocator;
import com.atlassian.jira.functest.framework.locator.XPathLocator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.JIRAWebTest;
import com.meterware.httpunit.WebTable;
import org.xml.sax.SAXException;

import java.util.Arrays;

import static com.atlassian.jira.functest.framework.backdoor.IssuesControl.LIST_VIEW_LAYOUT;

/**
 * Functional tests for log work
 */
@WebTest ({ Category.FUNC_TEST, Category.BROWSING, Category.TIME_TRACKING })
public class TestTimeTrackingAggregates extends JIRAWebTest
{

    public TestTimeTrackingAggregates(String name)
    {
        super(name);
    }

    public void setUp()
    {
        super.setUp();
        restoreData("TestTimeTrackingAggregates.xml");
    }

    public void testAllTest() throws SAXException
    {
        _testNoValues();
        _testNoOrigValue();
        _testNoTimeSpent();
        _testAllValuesTotalGreaterThanOrigNoSubs();
        _testNoValuesWithSubstasks();
        _testWithNoValueSubTaskAsStartingPoint();
        _testWithValuesWithSubstasksWithValues();
        _testWithRemainingZeroValue();
        _testPermissionedSubtasks();
        _testCantSeeSubTasks();
    }

    public void testTimeTrackingDisabled()
    {
        deactivateTimeTracking();
        gotoIssue("HSP-1");
        assertHeadingNotPresent();
        assertTextNotPresent("Estimated:");
        assertTextNotPresent("subtasks_resolution_percentage");

        gotoIssue("HSP-6");
        assertHeadingNotPresent();
        assertTextNotPresent("Estimated:");
        assertTextPresent("subtasks_resolution_percentage");
    }

    public void testTimeTrackingHidden()
    {
        setHiddenFields("Time Tracking");
        gotoIssue("HSP-1");
        assertTextNotPresent("tt_single_text_orig");
        assertTextNotPresent("tt_aggregate_text_orig");
        gotoIssue("HSP-6");
        assertTextNotPresent("tt_single_text_orig");
        assertTextNotPresent("tt_aggregate_text_orig");
    }

    public void _testNoValues() throws SAXException
    {
        gotoIssue("HSP-2");
        assertEquals(0, new IdLocator(tester, "tt_single_table_info").getNodes().length);
    }

    public void _testNoOrigValue() throws SAXException
    {
        gotoIssue("HSP-3");
        assertHeadingPresent();
        assertTextNotPresent("tt_aggregate_text_orig");

        text.assertTextSequence(new IdLocator(tester, "tt_single_table_info"), "Estimated","Not Specified",
                "Remaining", "2d", "Logged:", "2d");

        assertGraphElementAttribute("tt_single_graph_orig", 0, "width:100%");
        assertGraphElementAttribute("tt_single_graph_remain", 0, "width:50%");
        assertGraphElementAttribute("tt_single_graph_remain", 1, "width:50%");
        assertGraphElementAttribute("tt_single_graph_spent", 0, "width:50%");
        assertGraphElementAttribute("tt_single_graph_spent", 1, "width:50%");
    }

    public void _testNoTimeSpent() throws SAXException
    {
        gotoIssue("HSP-4");
        assertHeadingPresent();
        assertTextNotPresent("tt_aggregate_text_orig");

        text.assertTextSequence(new IdLocator(tester, "tt_single_table_info"), "Estimated","3d",
                "Remaining", "3d", "Logged:", "Not Specified");

        assertGraphElementAttribute("tt_single_graph_orig", 0, "width:100%");
        assertGraphElementAttribute("tt_single_graph_remain", 0, "width:100%");
        assertGraphElementAttribute("tt_single_graph_spent", 0, "width:100%");
    }

    public void _testAllValuesTotalGreaterThanOrigNoSubs() throws SAXException
    {
        gotoIssue("HSP-5");
        assertHeadingPresent();
        assertTextNotPresent("tt_aggregate_text_orig");

        text.assertTextSequence(new IdLocator(tester, "tt_single_table_info"), "Estimated","1w 1d",
                "Remaining", "4d", "Logged:", "4d");


        assertGraphElementAttribute("tt_single_graph_orig", 0, "width:75%");
        assertGraphElementAttribute("tt_single_graph_orig", 1, "width:25%");
        assertGraphElementAttribute("tt_single_graph_remain", 0, "width:50%");
        assertGraphElementAttribute("tt_single_graph_remain", 1, "width:50%");
        assertGraphElementAttribute("tt_single_graph_spent", 0, "width:50%");
        assertGraphElementAttribute("tt_single_graph_spent", 1, "width:50%");
    }


    public void _testNoValuesWithSubstasks() throws SAXException
    {
        gotoIssue("HSP-6");
        assertEquals(0, new IdLocator(tester, "tt_single_table_info").getNodes().length);
        assertEquals(0, new IdLocator(tester, "tt_aggregate_table_info").getNodes().length);
        assertHeadingNotPresent();
        assertTextNotPresent("subtasks_resolution_percentage");

    }

    public void _testWithNoValueSubTaskAsStartingPoint() throws SAXException
    {
        gotoIssue("HSP-7");
        assertHeadingNotPresent();
        assertEquals(0, new IdLocator(tester, "tt_single_table_info").getNodes().length);
    }

    public void _testWithValuesWithSubstasksWithNoValues() throws SAXException
    {
        gotoIssue("HSP-9");
        assertHeadingPresent();
        assertTextPresent("tt_aggregate_text_orig");
        assertTextPresent("Issue & Sub-Tasks");

        text.assertTextSequence(new IdLocator(tester, "tt_single_table_info"), "Estimated","3d",
                "Remaining", "3d", "Logged:", "1d");

        assertGraphElementAttribute("tt_aggregate_graph_orig", 0, "width:75%");
        assertGraphElementAttribute("tt_aggregate_graph_orig", 1, "width:25%");
        assertGraphElementAttribute("tt_aggregate_graph_remain", 0, "width:25%");
        assertGraphElementAttribute("tt_aggregate_graph_remain", 1, "width:75%");
        assertGraphElementAttribute("tt_aggregate_graph_spent", 0, "width:25%");
        assertGraphElementAttribute("tt_aggregate_graph_spent", 1, "width:75%");

        text.assertTextSequence(new IdLocator(tester, "tt_single_table_info"), "Estimated","Not Specified",
                "Remaining", "Not Specified", "Logged:", "Not Specified");

        assertGraphElementAttribute("tt_single_graph_orig", 0, "width:100%");
        assertGraphElementAttribute("tt_single_graph_remain", 0, "width:100%");
        assertGraphElementAttribute("tt_single_graph_spent", 0, "width:100%");

        // check sub task panel as well
        //        String actualStyle = tester.getDialog().getElement(elementId);
        text.assertTextPresent(new IdLocator(tester, "tt_percent_HSP-10"), "50%");
        assertGraphContainerAttribute("tt_graph_inner_HSP-10","width:100%");

        assertGraphElementAttribute("tt_graph_orig_HSP-10", 0, "width:50%");
        assertGraphElementAttribute("tt_graph_orig_HSP-10", 1, "width:50%");
        assertGraphElementAttribute("tt_graph_progress_HSP-10", 0, "width:50%");
        assertGraphElementAttribute("tt_graph_progress_HSP-10", 2, "width:50%");

        text.assertTextNotPresent(new IdLocator(tester, "tt_percent_HSP-11"), "%");
        assertGraphContainerAttribute("tt_graph_inner_HSP-11","width:100%");

        assertGraphElementAttribute("tt_graph_orig_HSP-11", 0, "width:100%");
        assertGraphElementAttribute("tt_graph_progress_HSP-11", 0, "width:100%");
    }

    public void _testWithAllValueSubTaskAsStartingPoint() throws SAXException
    {
        gotoIssue("HSP-10");
        assertHeadingPresent();
        assertTextNotPresent("tt_aggregate_text_orig");

        text.assertTextSequence(new IdLocator(tester, "tt_single_table_info"), "Estimated","1d",
                "Remaining", "1d", "Logged:", "1d");

        assertGraphElementAttribute("tt_single_graph_orig", 0, "width:50%");
        assertGraphElementAttribute("tt_single_graph_orig", 1, "width:50%");
        assertGraphElementAttribute("tt_single_graph_remain", 0, "width:50%");
        assertGraphElementAttribute("tt_single_graph_remain", 1, "width:50%");
        assertGraphElementAttribute("tt_single_graph_spent", 0, "width:50%");
        assertGraphElementAttribute("tt_single_graph_spent", 1, "width:50%");
    }

    public void _testWithValuesWithSubstasksWithValues() throws SAXException
    {
        gotoIssue("HSP-12");
        assertHeadingPresent();
        assertTextPresent("tt_aggregate_text_orig");
        assertTextPresent("Include sub-tasks");

        text.assertTextSequence(new IdLocator(tester, "tt_aggregate_table_info"), "Estimated", "1w 1d",
                "Remaining", "4d", "Logged", "1w");

        assertGraphElementAttribute("tt_aggregate_graph_orig", 0, "width:66%");
        assertGraphElementAttribute("tt_aggregate_graph_orig", 1, "width:34%");
        assertGraphElementAttribute("tt_aggregate_graph_remain", 0, "width:55%");
        assertGraphElementAttribute("tt_aggregate_graph_remain", 1, "width:45%");
        assertGraphElementAttribute("tt_aggregate_graph_spent", 0, "width:55%");
        assertGraphElementAttribute("tt_aggregate_graph_spent", 1, "width:45%");

        text.assertTextSequence(new IdLocator(tester, "tt_single_table_info"), "Estimated", "4d",
                "Remaining", "4d", "Logged:", "1d");

        assertGraphElementAttribute("tt_single_graph_orig", 0, "width:80%");
        assertGraphElementAttribute("tt_single_graph_orig", 1, "width:20%");
        assertGraphElementAttribute("tt_single_graph_remain", 0, "width:20%");
        assertGraphElementAttribute("tt_single_graph_remain", 1, "width:80%");
        assertGraphElementAttribute("tt_single_graph_spent", 0, "width:20%");
        assertGraphElementAttribute("tt_single_graph_spent", 1, "width:80%");

        // check sub task panel as well
        //        String actualStyle = tester.getDialog().getElement(elementId);
        text.assertTextPresent(new IdLocator(tester, "tt_dpb_percent_HSP-13"), "100%");
        assertGraphContainerAttribute("tt_dpb_graph_inner_HSP-13","width:100%");

        assertGraphElementAttribute("tt_dpb_graph_orig_HSP-13", 0, "width:57%");
        assertGraphElementAttribute("tt_dpb_graph_orig_HSP-13", 1, "width:43%");
        assertGraphElementAttribute("tt_dpb_graph_progress_HSP-13", 0, "width:100%");

        //        String actualStyle = tester.getDialog().getElement(elementId);
        text.assertTextPresent(new IdLocator(tester, "tt_dpb_percent_HSP-14"), "100%");
        assertGraphContainerAttribute("tt_dpb_graph_inner_HSP-14","width:14%");

        assertGraphElementAttribute("tt_dpb_graph_orig_HSP-14", 0, "width:0%");
        assertGraphElementAttribute("tt_dpb_graph_progress_HSP-14", 0, "width:100%");
    }


    public void _testWithRemainingZeroValue() throws SAXException
    {
        gotoIssue("HSP-13");
        assertHeadingPresent();
        assertTextNotPresent("tt_aggregate_text_orig");

        text.assertTextSequence(new IdLocator(tester, "tt_single_table_info"), "Estimated","2d",
                "Remaining", "0m", "Logged:", "3d 4h");

        assertGraphElementAttribute("tt_single_graph_orig", 0, "width:57%");
        assertGraphElementAttribute("tt_single_graph_orig", 1, "width:43%");
        assertGraphElementAttribute("tt_single_graph_remain", 0, "width:100%");
        assertGraphElementAttribute("tt_single_graph_spent", 0, "width:100%");
    }

    public void _testPermissionedSubtasks() throws SAXException
    {
        login(FRED_USERNAME, FRED_PASSWORD);
        gotoIssue("HSP-15");
        assertHeadingPresent();
        assertTextPresent("tt_aggregate_text_orig");

        text.assertTextSequence(new IdLocator(tester, "tt_single_table_info"), "Estimated", "2d",
                "Remaining", "2d", "Logged:", "Not Specified");


        assertGraphElementAttribute("tt_single_graph_orig", 0, "width:100%");
        assertGraphElementAttribute("tt_single_graph_remain", 0, "width:100%");
        assertGraphElementAttribute("tt_single_graph_spent", 0, "width:100%");

        text.assertTextSequence(new IdLocator(tester, "tt_aggregate_table_info"), "Estimated", "3d 2h",
                "Remaining", "3d 2h", "Logged:", "Not Specified");

        assertGraphElementAttribute("tt_single_graph_orig", 0, "width:100%");
        assertGraphElementAttribute("tt_single_graph_remain", 0, "width:100%");
        assertGraphElementAttribute("tt_single_graph_spent", 0, "width:100%");

        login(ADMIN_USERNAME, ADMIN_PASSWORD);
        gotoIssue("HSP-15");
        assertHeadingPresent();
        assertTextPresent("tt_aggregate_text_orig");

        text.assertTextSequence(new IdLocator(tester, "tt_single_table_info"), "Estimated", "2d",
                "Remaining", "2d", "Logged:", "Not Specified");

        assertGraphElementAttribute("tt_single_graph_orig", 0, "width:100%");
        assertGraphElementAttribute("tt_single_graph_remain", 0, "width:100%");
        assertGraphElementAttribute("tt_single_graph_spent", 0, "width:100%");

        text.assertTextSequence(new IdLocator(tester, "tt_aggregate_table_info"), "Estimated", "4d",
                "Remaining", "4d", "Logged:", "1d");

        assertGraphElementAttribute("tt_aggregate_graph_orig", 0, "width:80%");
        assertGraphElementAttribute("tt_aggregate_graph_orig", 1, "width:20%");
        assertGraphElementAttribute("tt_aggregate_graph_remain", 0, "width:20%");
        assertGraphElementAttribute("tt_aggregate_graph_remain", 1, "width:80%");
        assertGraphElementAttribute("tt_aggregate_graph_spent", 0, "width:20%");
        assertGraphElementAttribute("tt_aggregate_graph_spent", 1, "width:80%");

        // check sub task panel as well
        //        String actualStyle = tester.getDialog().getElement(elementId);
        text.assertTextPresent(new IdLocator(tester, "tt_dpb_percent_HSP-16"), "33%");
        assertGraphContainerAttribute("tt_dpb_graph_inner_HSP-16", "width:100%");

        assertGraphElementAttribute("tt_dpb_graph_orig_HSP-16", 0, "width:66%");
        assertGraphElementAttribute("tt_dpb_graph_orig_HSP-16", 1, "width:34%");
        assertGraphElementAttribute("tt_dpb_graph_progress_HSP-16", 0, "width:33%");
        assertGraphElementAttribute("tt_dpb_graph_progress_HSP-16", 2, "width:67%");

        // this subtask has no timetracking so should not display graphs
        assertElementNotPresent("tt_dpb_graph_outer_HSP-17");
        assertElementNotPresent("tt_dpb_percent_HSP-17");
        assertElementNotPresent("tt_dpb_graph_inner_HSP-17");

        assertTextNotPresent("tt_dpb_graph_orig_HSP-17_empty_cell");
        assertTextNotPresent("tt_dpb_graph_progress_HSP-17_empty_cell");

        assertTextNotPresent("tt_dpb_graph_orig_HSP-18");
    }


    public void _testCantSeeSubTasks() throws SAXException
    {
        gotoIssue("HSP-19");
        assertHeadingPresent();
        assertTextNotPresent("tt_aggregate_text_orig");
        text.assertTextSequence(new IdLocator(tester, "tt_single_table_info"), "Estimated","1d",
                "Remaining", "1d", "Logged:", "Not Specified");

        assertGraphElementAttribute("tt_single_graph_orig", 0, "width:100%");
        assertGraphElementAttribute("tt_single_graph_remain", 0, "width:100%");
        assertGraphElementAttribute("tt_single_graph_spent", 0, "width:100%");
    }

    public void testProgressNavigableField() throws SAXException
    {
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, ADMIN_USERNAME);
        displayAllIssues();

        //        String actualStyle = tester.getDialog().getElement(elementId);
        text.assertTextPresent(new IdLocator(tester, "tt_dpb_percent_HSP-16"), "33%");
        assertGraphContainerAttribute("tt_dpb_graph_inner_HSP-16","width:100%");

        assertGraphElementAttribute("tt_dpb_graph_orig_HSP-16", 0, "width:66%");
        assertGraphElementAttribute("tt_dpb_graph_orig_HSP-16", 1, "width:34%");
        assertGraphElementAttribute("tt_dpb_graph_progress_HSP-16", 0, "width:33%");
        assertGraphElementAttribute("tt_dpb_graph_progress_HSP-16", 2, "width:67%");

        // this issue has no time tracking and shouldn't have any graph classes present
        assertElementNotPresent("tt_dpb_graph_outer_HSP-17");
        assertElementNotPresent("tt_dpb_percent_HSP-17_");
        assertElementNotPresent("tt_dpb_graph_inner_HSP-17");

        assertTextNotPresent("tt_dpb_graph_orig_HSP-17_empty_cell");
        assertTextNotPresent("tt_dpb_graph_progress_HSP-17_empty_cell");
    }

    public void testAggregateProgressBar() throws SAXException
    {
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, ADMIN_USERNAME);

        navigation.issueNavigator().displayAllIssues();
        assertTextNotPresent("\u03A3 Estimated");

        backdoor.columnControl().addLoggedInUserColumns(Arrays.asList("aggregateprogress"));

        navigation.issueNavigator().displayAllIssues();
        assertTextPresent(" Progress");

        text.assertTextPresent(new IdLocator(tester, "tt_apb_percent_HSP-19"), "0%");
        assertGraphContainerAttribute("tt_apb_graph_inner_HSP-19","width:100%");

        assertGraphElementAttribute("tt_apb_graph_orig_HSP-19", 0, "width:100%");
        assertGraphElementAttribute("tt_apb_graph_progress_HSP-19", 0, "width:100%");

        assertElementNotPresent("tt_apb_graph_outer_HSP-17");
        assertElementNotPresent("tt_apb_percent_HSP-17_");
        assertElementNotPresent("tt_apb_graph_inner_HSP-17");

        assertTextNotPresent("tt_apb_graph_orig_HSP-17_empty_cell");
        assertTextNotPresent("tt_apb_graph_progress_HSP-17_empty_cell");

        text.assertTextPresent(new IdLocator(tester, "tt_apb_percent_HSP-16"), "33%");
        assertGraphContainerAttribute("tt_apb_graph_inner_HSP-16","width:100%");

        assertGraphElementAttribute("tt_apb_graph_orig_HSP-16", 0, "width:66%");
        assertGraphElementAttribute("tt_apb_graph_orig_HSP-16", 1, "width:34%");
        assertGraphElementAttribute("tt_apb_graph_progress_HSP-16", 0, "width:33%");
        assertGraphElementAttribute("tt_apb_graph_progress_HSP-16", 2, "width:67%");

        text.assertTextPresent(new IdLocator(tester, "tt_apb_percent_HSP-12"), "55%");
        assertGraphContainerAttribute("tt_apb_graph_inner_HSP-12","width:100%");

        assertGraphElementAttribute("tt_apb_graph_orig_HSP-12", 0, "width:66%");
        assertGraphElementAttribute("tt_apb_graph_orig_HSP-12", 1, "width:34%");
        assertGraphElementAttribute("tt_apb_graph_progress_HSP-12", 0, "width:55%");
        assertGraphElementAttribute("tt_apb_graph_progress_HSP-12", 2, "width:45%");
    }

    private void assertHeadingPresent()
    {
        text.assertTextPresent(new XPathLocator(tester, "//h2"), "Time Tracking");
    }

    private void assertHeadingNotPresent()
    {
        text.assertTextNotPresent(new XPathLocator(tester, "//h2"), "Time Tracking");
    }

    private void assertGraphElementAttribute(String tableName, int col, String attribute) throws SAXException
    {
        WebTable table = getDialog().getResponse().getTableWithID(tableName);
        String style = table.getTableCell(0, col).getDOM().getAttributes().getNamedItem("style").getNodeValue();
        assertTrue(style.indexOf(attribute) != -1);
    }

    private void assertGraphContainerAttribute(String tableId, String attributeValue) throws SAXException
    {
        String actualStyle = tester.getDialog().getElement(tableId).getAttribute("style");
        assertTrue(actualStyle.contains(attributeValue));
    }

}