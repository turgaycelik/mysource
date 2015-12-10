package com.atlassian.jira.webtest.webdriver.tests.gagets;

import com.atlassian.integrationtesting.runner.restore.Restore;
import com.atlassian.jira.testkit.client.restclient.SearchClient;
import com.atlassian.jira.testkit.client.restclient.SearchRequest;
import com.atlassian.jira.testkit.client.restclient.SearchResult;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.functest.framework.util.url.URLUtil;
import com.atlassian.jira.pageobjects.gadgets.TwoDimensionalStatsGadget;
import com.atlassian.jira.webtest.webdriver.selenium.PseudoAssertThat;
import com.atlassian.jira.webtest.webdriver.selenium.PseudoSeleniumClient;
import com.atlassian.jira.pageobjects.BaseJiraWebTest;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.PageElementFinder;
import com.atlassian.webdriver.AtlassianWebDriver;
import org.hamcrest.core.StringContains;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.internal.matchers.Contains;
import org.openqa.selenium.By;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

/**
 * WEebdriver test for the Two Dimensional Stats Gadget.
 *
 * @since v5.2
 */

@Ignore("https://jdog.atlassian.net/browse/FLAKY-170")
@WebTest ({ Category.WEBDRIVER_TEST, Category.GADGETS })
public class TestTwoDimensionalStatsGadget extends BaseJiraWebTest
{
    public static final String GADGET_NAME = "gadget-10060";
    public static final String COPY_OF_DEFAULT_FIELD_CONFIGURATION_NAME = "Copy of Default Field Configuration";
    public static final String CUSTOMFIELD_NAME = "customfield_10000";
    public static final String DEFAULT_FIELD_CONFIGURATION_NAME = "Default Field Configuration";
    public static final String FIX_VERSIONS_NAME = "fixVersions";
    public static final String GADGET_ID_10020 = "gadget-10020";
    private SearchClient searchClient;

    private PseudoSeleniumClient client;
    private PseudoAssertThat assertThat;
    @Inject
    private PageElementFinder pageElementFinder;
    @Inject
    private AtlassianWebDriver driver;
    private static final String X_STAT_TYPE = "xstattype";
    private static final String TWO_DIMENSIONAL_FILTER_STATISTICS_GADGET = "Two Dimensional Filter Statistics";
    private static final int TIMEOUT = 3000;
    protected static final int GADGET_DIRECTORY_TIMEOUT = 60000;
    private static final String GADGET_TITLE_LOCATOR = "//h3[@class='dashboard-item-title']";


    // https://jdog.atlassian.net/browse/FLAKY-170
    @Test
    @Restore ("xml/TestTwoDimensionalStatsGadgetIrrelevant.xml")
    public void testIrrelevantIssues()
    {
//        searchClient = new SearchClient(jira.environmentData());
//        GadgetContainer gadgets = jira.quickLoginAsSysadmin(DashboardPage.class).gadgets();
//        TwoDimensionalStatsGadget gadget = gadgets.getGadget(TwoDimensionalStatsGadget.class, GADGET_NAME);
//
//        List<int[]> tableData = new ArrayList<int[]>();
//        tableData.add(new int[] { 1, 0, 0, 0, 0, 1 });
//        tableData.add(new int[] { 0, 0, 0, 1, 0, 1 });
//        tableData.add(new int[] { 0, 1, 1, 0, 0, 2 });
//        tableData.add(new int[] { 0, 0, 0, 0, 3, 3 });
//        tableData.add(new int[] { 1, 1, 1, 1, 3, 7 });
//        String[] keys = { "opt1", "Optoin 1", "None", "Irrelevant", "Total Unique Issues:" };
//        assertTableData(gadget, keys, tableData);
//
//        backdoor.fieldConfiguration().changeFieldVisibility(COPY_OF_DEFAULT_FIELD_CONFIGURATION_NAME, CUSTOMFIELD_NAME, false);
//        backdoor.indexing().reindexAll();
//        gadgets = jira.goTo(DashboardPage.class).gadgets();
//        gadget = gadgets.getGadget(TwoDimensionalStatsGadget.class, GADGET_NAME);
//        tableData = new ArrayList<int[]>();
//        tableData.add(new int[]{1 ,	0,	0,	0,	0,	1});
//        tableData.add(new int[]{0 ,	0,	0,	1,	1,	2});
//        tableData.add(new int[]{0 ,	1,	1,	0,	2,	4});
//        tableData.add(new int[]{1 ,	1,	1,	1,	3,	7});
//        keys = new String[] {"opt1", "Optoin 1", "None", "Total Unique Issues:"};
//        assertTableData(gadget, keys, tableData);
//
//        backdoor.fieldConfiguration().changeFieldVisibility(COPY_OF_DEFAULT_FIELD_CONFIGURATION_NAME, CUSTOMFIELD_NAME, true);
//        backdoor.fieldConfiguration().changeFieldVisibility(COPY_OF_DEFAULT_FIELD_CONFIGURATION_NAME, FIX_VERSIONS_NAME, false);
//        backdoor.indexing().reindexAll();
//        gadgets = jira.goTo(DashboardPage.class).gadgets();
//        gadget = gadgets.getGadget(TwoDimensionalStatsGadget.class, GADGET_NAME);
//        tableData = new ArrayList<int[]>();
//        tableData.add(new int[]{1, 0, 0, 0, 0, 0, 1});
//        tableData.add(new int[]{0, 0, 0, 0, 0, 1, 1});
//        tableData.add(new int[]{0, 1, 1, 0, 0, 0, 2});
//        tableData.add(new int[]{0, 0, 0, 1, 2, 0, 3});
//        tableData.add(new int[]{1, 1, 1, 1, 2, 1, 7});
//        keys = new String[] {"opt1", "Optoin 1", "None", "Irrelevant", "Total Unique Issues:"};
//        assertTableData(gadget, keys, tableData);
//
//        backdoor.fieldConfiguration().changeFieldVisibility(DEFAULT_FIELD_CONFIGURATION_NAME, CUSTOMFIELD_NAME, true);
//        backdoor.fieldConfiguration().changeFieldVisibility(COPY_OF_DEFAULT_FIELD_CONFIGURATION_NAME, FIX_VERSIONS_NAME, true);
//        backdoor.fieldConfiguration().changeFieldVisibility(COPY_OF_DEFAULT_FIELD_CONFIGURATION_NAME, CUSTOMFIELD_NAME, false);
//        backdoor.indexing().reindexAll();
//        gadgets = jira.goTo(DashboardPage.class).gadgets();
//        gadget = gadgets.getGadget(TwoDimensionalStatsGadget.class, GADGET_NAME);
//        tableData = new ArrayList<int[]>();
//        tableData.add(new int[] { 0, 0, 0, 0, 1, 1 });
//        tableData.add(new int[]{0, 0, 0, 0, 2, 2});
//        tableData.add(new int[]{1, 1, 1, 1, 0, 4});
//        tableData.add(new int[]{1, 1, 1, 1, 3, 7});
//        keys = new String[] {"Optoin 1", "None", "Irrelevant", "Total Unique Issues:"};
//        assertTableData(gadget, keys, tableData);
//
//        backdoor.fieldConfiguration().changeFieldVisibility(DEFAULT_FIELD_CONFIGURATION_NAME, FIX_VERSIONS_NAME, true);
//        backdoor.fieldConfiguration().changeFieldVisibility(DEFAULT_FIELD_CONFIGURATION_NAME, CUSTOMFIELD_NAME, false);
//        backdoor.fieldConfiguration().changeFieldVisibility(COPY_OF_DEFAULT_FIELD_CONFIGURATION_NAME, FIX_VERSIONS_NAME, false);
//        backdoor.fieldConfiguration().changeFieldVisibility(COPY_OF_DEFAULT_FIELD_CONFIGURATION_NAME, CUSTOMFIELD_NAME, true);
//        backdoor.indexing().reindexAll();
//        gadgets = jira.goTo(DashboardPage.class).gadgets();
//        gadget = gadgets.getGadget(TwoDimensionalStatsGadget.class, GADGET_NAME);
//        tableData = new ArrayList<int[]>();
//        tableData.add(new int[]{0, 0, 1, 1});
//        tableData.add(new int[]{0, 0, 1, 1});
//        tableData.add(new int[] { 0, 0, 2, 2 });
//        tableData.add(new int[]{1, 2, 0, 3});
//        tableData.add(new int[]{1, 2, 4, 7});
//        keys = new String[] {"opt1", "Optoin 1", "None", "Irrelevant", "Total Unique Issues:"};
//        assertTableData(gadget, keys, tableData);
//
//        backdoor.fieldConfiguration().changeFieldVisibility(DEFAULT_FIELD_CONFIGURATION_NAME, CUSTOMFIELD_NAME, true);
//        backdoor.fieldConfiguration().changeFieldVisibility(COPY_OF_DEFAULT_FIELD_CONFIGURATION_NAME, CUSTOMFIELD_NAME, false);
//        backdoor.indexing().reindexAll();
//        gadgets = jira.goTo(DashboardPage.class).gadgets();
//        gadget = gadgets.getGadget(TwoDimensionalStatsGadget.class, GADGET_NAME);
//        tableData = new ArrayList<int[]>();
//        tableData.add(new int[]{0, 1, 0, 1});
//        tableData.add(new int[]{1, 1, 0, 2});
//        tableData.add(new int[]{0, 0, 4, 4});
//        tableData.add(new int[]{1, 2, 4, 7});
//        keys = new String[] {"Optoin 1", "None", "Irrelevant", "Total Unique Issues:"};
//        assertTableData(gadget, keys, tableData);
//
//        backdoor.fieldConfiguration().changeFieldVisibility(DEFAULT_FIELD_CONFIGURATION_NAME, FIX_VERSIONS_NAME, false);
//        backdoor.indexing().reindexAll();
//        gadgets = jira.goTo(DashboardPage.class).gadgets();
//        gadget = gadgets.getGadget(TwoDimensionalStatsGadget.class, GADGET_NAME);
//        tableData = new ArrayList<int[]>();
//        tableData.add(new int[]{0, 0, 0, 0, 1, 0, 1});
//        tableData.add(new int[]{0, 0, 0, 1, 1, 0, 2});
//        tableData.add(new int[]{1, 1, 1, 0, 0, 1, 4});
//        tableData.add(new int[]{1, 1, 1, 1, 2, 1, 7});
//        keys = new String[] {"Optoin 1", "None", "Irrelevant", "Total Unique Issues:"};
//        assertTableData(gadget, keys, tableData);
//
//        backdoor.fieldConfiguration().changeFieldVisibility(DEFAULT_FIELD_CONFIGURATION_NAME, FIX_VERSIONS_NAME, true);
//        backdoor.fieldConfiguration().changeFieldVisibility(DEFAULT_FIELD_CONFIGURATION_NAME, CUSTOMFIELD_NAME, false);
//        backdoor.indexing().reindexAll();
//        gadgets = jira.goTo(DashboardPage.class).gadgets();
//        gadget = gadgets.getGadget(TwoDimensionalStatsGadget.class, GADGET_NAME);
//        tableData = new ArrayList<int[]>();
//        tableData.add(new int[]{0, 0, 1, 1});
//        tableData.add(new int[]{0, 1, 1, 2});
//        tableData.add(new int[]{1, 1, 2, 4});
//        tableData.add(new int[]{1, 2, 4, 7});
//        keys = new String[] {"opt1", "Optoin 1", "None", "Total Unique Issues:"};
//        assertTableData(gadget, keys, tableData);
//
//        backdoor.fieldConfiguration().changeFieldVisibility(DEFAULT_FIELD_CONFIGURATION_NAME, FIX_VERSIONS_NAME, false);
//        backdoor.fieldConfiguration().associateCustomFieldWithProject(CUSTOMFIELD_NAME, "Another");
//        backdoor.indexing().reindexAll();
//        gadgets = jira.goTo(DashboardPage.class).gadgets();
//        gadget = gadgets.getGadget(TwoDimensionalStatsGadget.class, GADGET_NAME);
//        tableData = new ArrayList<int[]>();
//        tableData.add(new int[]{1, 0, 0, 0, 0, 0, 1});
//        tableData.add(new int[]{0, 0, 0, 0, 0, 1, 1});
//        tableData.add(new int[]{0, 1, 1, 0, 0, 0, 2});
//        tableData.add(new int[]{0, 0, 0, 1, 2, 0, 3});
//        tableData.add(new int[]{1, 1, 1, 1, 2, 1, 7});
//        keys = new String[] {"opt1", "Optoin 1", "None", "Irrelevant", "Total Unique Issues:"};
//        assertTableData(gadget, keys, tableData);
//
//        backdoor.fieldConfiguration().associateCustomFieldWithProject(CUSTOMFIELD_NAME, "Monkey");
//        backdoor.indexing().reindexAll();
//        gadgets = jira.goTo(DashboardPage.class).gadgets();
//        gadget = gadgets.getGadget(TwoDimensionalStatsGadget.class, GADGET_NAME);
//        tableData = new ArrayList<int[]>();
//        tableData.add(new int[]{0, 0, 0, 0, 1, 0, 1});
//        tableData.add(new int[]{0, 0, 0, 1, 1, 0, 2});
//        tableData.add(new int[]{1, 1, 1, 0, 0, 1, 4});
//        tableData.add(new int[]{1, 1, 1, 1, 2, 1, 7});
//        keys = new String[] {"Optoin 1", "None", "Irrelevant", "Total Unique Issues:"};
//        assertTableData(gadget, keys, tableData);
    }

    // https://jdog.atlassian.net/browse/FLAKY-134
//    @Test
//    @Restore ("xml/TestTwoDimensionalStatsGadget.xml")
//    public void testConfigureAndView()
//    {
//        client = new PseudoSeleniumClient(jira, pageElementFinder);
//        assertThat = new PseudoAssertThat(driver);
//        searchClient = new SearchClient(jira.environmentData());
//
//        addGadget(TWO_DIMENSIONAL_FILTER_STATISTICS_GADGET);
//        waitForGadgetConfiguration();
//
//        _testBasicConfig();
//        _testBadConfig();
//        _testProjectVsAssignee();
//        _testGroupPickerCf();
//        _testProjectVsAssigneeReversedDirection();
//        _testFixVersion();
//        _testReporter();
//        _testIssueType();
//        _testComponents();
//        _testEmptyFilter();
//        _testFilterLink();
//        _testXofYShowing();
//        _testMoreOrLessLink();
//    }

    public void addGadget(final String gadgetTitle)
    {
        assertThat.elementPresentByTimeout("add-gadget", TIMEOUT);
        client.click("add-gadget");
        // This can occasionally take > 30s, so the normal TIMEOUT will not suffice
        assertThat.elementPresentByTimeout("category-all", GADGET_DIRECTORY_TIMEOUT);
        final String pseudoId = "macro-" + gadgetTitle.replaceAll("\\W*", "");
        client.click("jquery=#" + pseudoId + " .macro-button-add");
        client.click("css=button.finish");
        assertThat.elementPresentByTimeout(GADGET_TITLE_LOCATOR, TIMEOUT);
        client.switchToFrame(GADGET_ID_10020);
    }

    protected void waitForGadgetConfiguration()
    {
        assertThat.elementPresentByTimeout("css=form.aui",TIMEOUT);
    }

    private void _testMoreOrLessLink()
    {
        final TwoDimensionalStatsGadget gadget = pageBinder.bind(TwoDimensionalStatsGadget.class, GADGET_ID_10020);
        final List<int[]> tableData1 = new ArrayList<int[]>();
        tableData1.add(new int[]{0, 0, 4, 4});
        tableData1.add(new int[]{1, 2, 4, 7});
        assertTableData(gadget, tableData1);
        gadget.showMore();
        checkShowingXofY(2, 2);

        final List<int[]> tableData2 = new ArrayList<int[]>();
        tableData2.add(new int[]{0, 0, 4, 4});
        tableData2.add(new int[]{1, 2, 0, 3});
        tableData2.add(new int[]{1, 2, 4, 7});
        assertTableData(gadget, tableData2);
        gadget.showMore();
        checkShowingXofY(1, 2);
        assertTableData(gadget, tableData1);
    }

    private void _testXofYShowing()
    {
        jira.gotoHomePage();
        client.waitForPageToLoad();
        editConfiguration();
        client.setTextField("numberToShow", "1");
        submitGadgetConfig();
        checkShowingXofY(1, 2);

        editConfiguration();
        assertThat.visibleByTimeout("//input[@id='numberToShow' and @value='1']", TIMEOUT);
        client.setTextField("numberToShow", "2");
        submitGadgetConfig();
        checkShowingXofY(2, 2);

        editConfiguration();
        client.setTextField("numberToShow", "5");
        submitGadgetConfig();
        // only 2 available
        checkShowingXofY(2, 2);

        editConfiguration();
        client.setTextField("numberToShow", "1");
        submitGadgetConfig();
    }


    private void _testGroupPickerCf()
    {
        editConfiguration();
        client.select(X_STAT_TYPE, "Group Picker CF");
        submitGadgetConfig();

        final TwoDimensionalStatsGadget gadget = pageBinder.bind(TwoDimensionalStatsGadget.class, GADGET_ID_10020);
        assertEquals("jira-administrators", gadget.getHeader(0, 1).getText());
        assertEquals("None", gadget.getHeader(1, 1).getText());

        assertThat(getRawJqlFor(gadget.getHeader(0, 1)), new StringContains("%22Group+Picker+CF%22+%3D+jira-administrators"));
        assertThat(getRawJqlFor(gadget.getHeader(1, 1)), new StringContains("%22Group+Picker+CF%22+is+EMPTY"));

        assertCorrectNumberIssues(0, 2, 1, gadget);
        assertCorrectNumberIssues(3, 2, 2, gadget);
        assertCorrectNumberIssues(1, 3, 1, gadget);
        assertCorrectNumberIssues(3, 3, 2, gadget);
    }

    private String getRawJqlFor(final PageElement cell)
    {
        final PageElement link = cell.find(By.cssSelector("a"));
        return getRawJqlFromUrl(link);
    }

    private void editConfiguration()
    {
        client.switchToDefaultContent();
        driver.executeScript("AJS.$(\"#" + GADGET_ID_10020 + "-renderbox .aui-dropdown .configure a.item-link\").click()");
        client.switchToFrame(GADGET_ID_10020);
        driver.waitUntilElementIsVisible(By.cssSelector("input.save"));
    }

    private void _testBadConfig()
    {
        client.setTextField("id=quickfind", "");
        submitIncorrectGadgetConfig();
        assertThat.elementPresentByTimeout("//div[@class='error' and contains(text(), 'filter specified')]", 5);

        client.setTextField("id=quickfind", "somefilterthatdoesntexist");
        submitIncorrectGadgetConfig();
        assertThat.elementPresentByTimeout("//div[@class='error' and contains(text(), 'filter specified')]", 5);

        client.setTextField("id=numberToShow", "0");
        submitIncorrectGadgetConfig();
        assertThat.elementPresentByTimeout("//div[@class='error' and contains(text(), 'greater than 0')]", 5);

        client.setTextField("id=numberToShow", "notanumber");
        submitIncorrectGadgetConfig();
        assertThat.elementPresentByTimeout("//div[@class='error' and contains(text(), 'must be an integer')]", 5);

        client.setTextField("id=numberToShow", "10");
    }

    private void submitIncorrectGadgetConfig()
    {
        client.click(By.className("save"));
    }

    private void submitGadgetConfig()
    {
        client.click(By.className("save"));
        client.switchToDefaultContent();
        driver.waitUntilElementIsVisible(By.id(GADGET_ID_10020 + "-renderbox"));
        client.switchToFrame(GADGET_ID_10020);
    }

    private void _testEmptyFilter()
    {
        editConfiguration();
        client.selectOptionFromAutocompleteTextField("quickfind", "empty");
        submitGadgetConfig();
        assertThat.textPresentByTimeout("The filter for this gadget did not return any issues", 5000);
    }

    private void _testComponents()
    {
        editConfiguration();
        client.select(X_STAT_TYPE, "Components");
        submitGadgetConfig();
        final TwoDimensionalStatsGadget gadget = pageBinder.bind(TwoDimensionalStatsGadget.class, GADGET_ID_10020);

        assertEquals("New Component 1", gadget.getHeader(0, 1).getText());
        assertEquals("New Component 2", gadget.getHeader(1, 1).getText());
        assertEquals("No component", gadget.getHeader(2, 1).getText());
        final String firstComponentMarkup = getRawJqlFor(gadget.getHeader(0, 1));
        final String secondComponentMarkup = getRawJqlFor(gadget.getHeader(1, 1));
        final String noComponentMarkup = getRawJqlFor(gadget.getHeader(2, 1));

        assertThat(firstComponentMarkup, new StringContains("component+%3D+%22New+Component+1%22+AND+project+%3D+HSP"));
        assertThat(secondComponentMarkup, new StringContains("component+%3D+%22New+Component+2%22+AND+project+%3D+HSP"));
        assertThat(noComponentMarkup, new StringContains("component+is+EMPTY"));

        assertCorrectNumberIssues(0, 2, 1, gadget);
        assertCorrectNumberIssues(0, 2, 2, gadget);
        assertCorrectNumberIssues(4, 2, 3, gadget);
        assertCorrectNumberIssues(1, 3, 1, gadget);
        assertCorrectNumberIssues(2, 3, 2, gadget);
        assertCorrectNumberIssues(0, 3, 3, gadget);
    }

    private void _testIssueType()
    {
        editConfiguration();
        client.select(X_STAT_TYPE, "Issue Type");
        submitGadgetConfig();
        final TwoDimensionalStatsGadget gadget = pageBinder.bind(TwoDimensionalStatsGadget.class, GADGET_ID_10020);

        assertEquals("Bug", gadget.getHeader(0, 1).getText());
        assertEquals("New Feature", gadget.getHeader(1, 1).getText());
        assertEquals("Task", gadget.getHeader(2, 1).getText());

        assertCorrectNumberIssues(1, 2, 1, gadget);
        assertCorrectNumberIssues(0, 2, 2, gadget);
        assertCorrectNumberIssues(3, 2, 3, gadget);

        assertCorrectNumberIssues(2, 3, 1, gadget);
        assertCorrectNumberIssues(1, 3, 2, gadget);
        assertCorrectNumberIssues(0, 3, 3, gadget);
    }


    private void _testReporter()
    {
        editConfiguration();
        client.select(X_STAT_TYPE, "Reporter");
        submitGadgetConfig();

        final TwoDimensionalStatsGadget gadget = pageBinder.bind(TwoDimensionalStatsGadget.class, GADGET_ID_10020);
        //Only one reporter, so it will be every issue in each project
        assertCorrectNumberIssues(4, 2, 1, gadget);
        assertCorrectNumberIssues(3, 3, 1, gadget);
    }

    private void _testFixVersion()
    {
        editConfiguration();
        client.select(X_STAT_TYPE, "Fix For Versions (all)");
        submitGadgetConfig();

        final TwoDimensionalStatsGadget gadget = pageBinder.bind(TwoDimensionalStatsGadget.class, GADGET_ID_10020);
        //Should have versions on the x axis (row 1) now
        assertEquals("homosapien 1", gadget.getHeader(0, 1).getText());
        assertEquals("homosapien 2", gadget.getHeader(1, 1).getText());
        assertEquals("monkey 1", gadget.getHeader(2, 1).getText());
        assertEquals("monkey 2", gadget.getHeader(3, 1).getText());
        assertEquals("Unscheduled", gadget.getHeader(4, 1).getText());
        assertEquals("T:", gadget.getHeader(5, 1).getText());
    }


    private void _testProjectVsAssigneeReversedDirection()
    {
        //Flip the direction
        editConfiguration();
        client.select("sortDirection", "Descending");
        client.select("showTotals", "Yes");
        client.select("ystattype", "Project");
        client.select("showTotals", "Yes");
        client.select(X_STAT_TYPE, "Assignee");
        submitGadgetConfig();

        final TwoDimensionalStatsGadget gadget = pageBinder.bind(TwoDimensionalStatsGadget.class, GADGET_ID_10020);
        assertCorrectNumberIssues(1, 2, 1, gadget);
        assertCorrectNumberIssues(3, 2, 2, gadget);
        //Total for monkey
        assertCorrectNumberIssues(4, 2, 3, gadget);
        //Total for homosapien
        assertCorrectNumberIssues(3, 3, 3, gadget);
        //Total for admin
        assertCorrectNumberIssues(4, 4, 1, gadget);
        //Total for Dr Zaius
        assertCorrectNumberIssues(3, 4, 2, gadget);
        //Total for everything
        assertEquals("7", gadget.getCell(3, 4).getText());
    }


    private void _testProjectVsAssignee()
    {
        // X : proj, Y : assignee
        client.select("ystattype", "Project");
        client.selectOptionFromAutocompleteTextField("id=quickfind", "test");
        submitGadgetConfig();

        assertThat.textPresentByTimeout("testFilter", 5);
        assertThat.elementPresentByTimeout("twodstatstable", 5);

        final TwoDimensionalStatsGadget gadget = pageBinder.bind(TwoDimensionalStatsGadget.class, GADGET_ID_10020);

        assertEquals("Project", gadget.getHeader(0, 0).getText());
        assertEquals("Assignee", gadget.getHeader(1, 0).getText());

        assertEquals("Administrator", gadget.getHeader(0, 1).getText());
        assertEquals("drzaius", gadget.getHeader(1, 1).getText());

        assertEquals("homosapien", gadget.getCell(0, 2).getText());
        assertEquals("monkey", gadget.getCell(0, 3).getText());

        assertCorrectNumberIssues(3, 2, 1, gadget);
        assertCorrectNumberIssues(0, 2, 2, gadget);

        assertCorrectNumberIssues(1, 3, 1, gadget);
        assertCorrectNumberIssues(3, 3, 2, gadget);
    }

    private void _testBasicConfig()
    {
        assertThat.textPresent("Saved Filter:");
        assertThat.textPresent("XAxis");
        assertThat.textPresent("YAxis");

        assertThat.textPresent("Sort By");
        assertThat.textPresent("Sort Direction");
        assertThat.textPresent("Show Totals");
        assertThat.textPresent("Number of Results");

        final String[] statValues = { "Assignee", "Components", "Issue Type", "Fix For Versions (non-archived)", "Fix For Versions (all)", "Priority", "Raised In Versions (non-archived)", "Raised In Versions (all)", "Reporter", "Resolution", "Status" };
        assertFieldOptionValuesPresent(X_STAT_TYPE, statValues);
        assertFieldOptionValuesPresent("ystattype", statValues);
    }

    private void _testFilterLink()
    {
        editConfiguration();
        client.selectOptionFromAutocompleteTextField("quickfind", "test");
        submitGadgetConfig();
        final TwoDimensionalStatsGadget gadget = pageBinder.bind(TwoDimensionalStatsGadget.class, GADGET_ID_10020);
        gadget.clickFilter();
        client.waitForPageToLoad();
        final int numResults = 7;
        assertThat.textPresent("to " + numResults + " of");
    }


    private void assertTableData(final TwoDimensionalStatsGadget gadget, final String[] keys, final List<int[]> tableData)
    {
        int y = 2;
        for (final String key : keys)
        {
            assertEquals(key, gadget.getCell(0, y).getText());
            y++;
        }
        assertTableData(gadget, tableData);
    }

    private void assertTableData(final TwoDimensionalStatsGadget gadget, final List<int[]> tableData)
    {
        int y = 2;
        for (final int[] row : tableData)
        {
            int x = 1;
            for (final int cellValue : row)
            {
                assertCorrectNumberIssues(cellValue, y, x, gadget);
                x++;
            }
            y++;
        }
    }

    private void assertCorrectNumberIssues(final int cellValue, final int y, final int x, final TwoDimensionalStatsGadget gadget)
    {
        final PageElement cell = gadget.getCell(x, y);
        assertEquals(Long.valueOf(cellValue), Long.valueOf(cell.getText()));
        assertCorrectJqlResults(cellValue, cell);
    }

    private void assertCorrectJqlResults(final int cellValue, final PageElement cell)
    {
        final PageElement url = cell.find(By.cssSelector("a"));
        if (url.isPresent())
        {
            final SearchRequest searchRequest = new SearchRequest();
            searchRequest.jql(getJqlFromUrl(url));
            final SearchResult result = searchClient.postSearch(searchRequest);
            assertEquals(cellValue, result.issues.size());
        }
    }

    private String getJqlFromUrl(final PageElement url)
    {
        return URLUtil.decode(getRawJqlFromUrl(url));
    }

    private String getRawJqlFromUrl(final PageElement url)
    {
        final String link = url.getAttribute("href");
        return URLUtil.getQueryParamValueFromUrl(link, "jqlQuery");
    }

    private void assertFieldOptionValuesPresent(final String field, final String[] optionValues)
    {
        for (final String optionValue : optionValues)
        {
            final String fieldValue = client.getElement(field).getText();
            assertThat(fieldValue, new Contains(optionValue));
        }
    }

    private void checkShowingXofY(final int x, final int y)
    {
        assertThat.elementPresentByTimeout("//div[@class='table-footer']/div[1]", 5);
        final PageElement element = pageElementFinder.find(By.xpath("//div[@class='table-footer']/div[1]"));
        assertEquals("Showing " + x + " of " + y + " statistics.", element.getText());
        final PageElement firstNumber = pageElementFinder.find(By.xpath("//div[@class='table-footer']/div[1]/strong[1]"));
        assertEquals(Integer.toString(x), firstNumber.getText());
        final PageElement totalNumber = pageElementFinder.find(By.xpath("//div[@class='table-footer']/div[1]/strong[2]"));
        assertEquals(Integer.toString(y), totalNumber.getText());
    }

}
