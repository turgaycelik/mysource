package com.atlassian.jira.webtests.ztests.navigator;

import com.atlassian.jira.functest.framework.assertions.TextAssertionsImpl;
import com.atlassian.jira.functest.framework.locator.CssLocator;
import com.atlassian.jira.functest.framework.locator.IdLocator;
import com.atlassian.jira.functest.framework.locator.Locator;
import com.atlassian.jira.functest.framework.locator.XPathLocator;
import com.atlassian.jira.functest.framework.navigation.IssueNavigatorNavigation;
import com.atlassian.jira.functest.framework.navigator.IssueTypeCondition;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.functest.framework.util.Searcher;
import com.atlassian.jira.webtests.CustomFieldValue;
import com.atlassian.jira.webtests.ztests.navigator.jql.AbstractJqlFuncTest;
import com.google.common.collect.Lists;
import com.meterware.httpunit.WebLink;
import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.atlassian.jira.functest.framework.backdoor.IssuesControl.LIST_VIEW_LAYOUT;

@WebTest ({ Category.FUNC_TEST, Category.ISSUE_NAVIGATOR, Category.ISSUES })
public class TestIssueNavigator extends AbstractJqlFuncTest
{
    private static final String FIELD_FIX_VERSION = "Fix Version";
    private static final String FIELD_AFFECTS_VERSION = "Affects Version";
    private static final String FIELD_COMPONENTS = "Component";
    private static final String CUSTOM_FIELD_GLOBAL = "global custom field";
    private static final String CUSTOM_FIELD_ISSUETYPE = "issueType only custom field";
    private static final String CUSTOM_FIELD_PROJECT = "project only custom field";
    private static final String CUSTOM_FIELD_ISSUETYPE_AND_PROJECT = "issue type & project custom field";
    private static final String PROJECT_DOG = "dog";
    private static final String PROJECT_HOMOSAP = "homosapien";
    private static final String homosapId = "10000";
    private static final Long   homosapIdLong = 1000L;

    private static final Long   hspUnresolvedFilterId = 10010L;

    private static final String GROUP_NAME = "test group";

    @SuppressWarnings("unchecked")
    private static final List<CustomFieldValue>[] cfValuesPerIssue = new ArrayList[]{new ArrayList<CustomFieldValue>(), new ArrayList<CustomFieldValue>(), new ArrayList<CustomFieldValue>()};

    private static final String issueKey = "HSP-1";
    private static final String issueKey2 = "HSP-2";
    private static final String issueKey3 = "HSP-3";
    private static final String issueKey4 = "HSP-4";

    private static final String customFieldIdSelectList = "10000";
    private static final String customFieldIdRadioButton = "10001";
    private static final String customFieldIdMultiSelect = "10002";
    private static final String customFieldIdCheckBox = "10003";
    private static final String customFieldIdTextField = "10004";
    private static final String customFieldIdUserPicker = "10005";
    private static final String customFieldIdDatePicker = "10006";

    private static final String CUSTOM_FIELD_SELECT = "Custom Field Select";
    private static final String CUSTOM_FIELD_RADIO = "Custom Field Radio";
    private static final String CUSTOM_FIELD_MULTI_SELECT = "Custom Field Multi Select";
    private static final String CUSTOM_FIELD_TEXTFIELD = "Custom Field Text Field";
    private static final String CUSTOM_FIELD_CHECKBOX = "Custom Field Check Box";
    private static final String CUSTOM_FIELD_USERPICKER = "Custom Field User Picker";
    private static final String CUSTOM_FIELD_DATEPICKER = "Custom Field Date Picker";

    // test users
    private static final String ABC_USERNAME = "abcuser";

    private static final String DEF_USERNAME = "defuser";

    private static final String GHI_USERNAME = "ghiuser";

    private static final String RESULTS_COUNT_CLASS = ".results-count";



    private static final String[] defaultOptions = new String[]{"abc", "def", "ghi"};
    private static final String[] dateOptions = new String[]{"01/Jan/05", "01/Feb/05", "01/Mar/05"};
    private static final String[] userOptions = new String[]{ABC_USERNAME, DEF_USERNAME, GHI_USERNAME};
    private static final String[] customFieldNames = new String[]{CUSTOM_FIELD_SELECT, CUSTOM_FIELD_RADIO, CUSTOM_FIELD_TEXTFIELD, CUSTOM_FIELD_MULTI_SELECT,
            CUSTOM_FIELD_CHECKBOX, CUSTOM_FIELD_USERPICKER, CUSTOM_FIELD_DATEPICKER};
    private static final String[] customFieldIds = new String[]{customFieldIdSelectList, customFieldIdRadioButton,
            customFieldIdTextField, customFieldIdMultiSelect,
            customFieldIdCheckBox, customFieldIdUserPicker,
            customFieldIdDatePicker};

    public void testSubtaskIssueNavigatorColumn()
    {
        administration.restoreData("TestIssueNavigatorSubtaskColumnView.xml");

        navigation.login(ADMIN_USERNAME, ADMIN_PASSWORD);
        navigation.issueNavigator().createSearch("");
        text.assertTextPresent("HSP-5");
        text.assertTextPresent("HSP-6");
        log("Successfully found subtask issue keys in the subtask issue navigator column");
    }

    protected void removeColumnFromIssueNavigatorByPosition(final int pos)
    {
        tester.clickLink("Profile");
        tester.clickLink("view_nav_columns");

        tester.clickLink("del_col_" + pos);
    }

    public void testNavigatorColumnVisibilityForCustomFields()
    {
        administration.restoreData("TestIssueNavigatorColumnVisibilityForCustomFields.xml");
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, ADMIN_USERNAME);
        final String dogProjectId = "10010";

        // run some issue navigator tests and make sure the columns look right
        // since the user has all the custom fields on its column layout, the custom fields will show up in all his searches
        navigation.issueNavigator().displayAllIssues();
        assertTextInIssueTable(PROJECT_HOMOSAP + " cf");
        assertTextInIssueTable(PROJECT_HOMOSAP + " bug cf");
        assertTextInIssueTable(PROJECT_DOG + " cf");
        assertTextInIssueTable(PROJECT_DOG + " bug cf");

        // search only homosap
        navigation.issueNavigator().createSearch("project=" + PROJECT_HOMOSAP);
        assertTextInIssueTable(PROJECT_HOMOSAP + " cf");
        assertTextInIssueTable(PROJECT_HOMOSAP + " bug cf");
        assertTextInIssueTable(PROJECT_DOG + " cf");
        assertTextInIssueTable(PROJECT_DOG + " bug cf");

        // search homosap bugs
        navigation.issueNavigator().createSearch("project=" + PROJECT_HOMOSAP + " AND issuetype= Bug");
        assertTextInIssueTable(PROJECT_HOMOSAP + " cf");
        assertTextInIssueTable(PROJECT_HOMOSAP + " bug cf");
        assertTextInIssueTable(PROJECT_DOG + " cf");
        assertTextInIssueTable(PROJECT_DOG + " bug cf");

        // search only dog
        navigation.issueNavigator().createSearch("project=" + PROJECT_DOG);
        assertTextInIssueTable(PROJECT_HOMOSAP + " cf");
        assertTextInIssueTable(PROJECT_HOMOSAP + " bug cf");
        assertTextInIssueTable(PROJECT_DOG + " cf");
        assertTextInIssueTable(PROJECT_DOG + " bug cf");

        // search dog bugs
        navigation.issueNavigator().createSearch("project=" + PROJECT_DOG + " AND issuetype= Bug");
        assertTextInIssueTable(PROJECT_HOMOSAP + " cf");
        assertTextInIssueTable(PROJECT_HOMOSAP + " bug cf");
        assertTextInIssueTable(PROJECT_DOG + " cf");
        assertTextInIssueTable(PROJECT_DOG + " bug cf");

        // search both projects
        navigation.issueNavigator().createSearch("project in (" + dogProjectId + ", " + homosapId + ")");
        assertTextInIssueTable(PROJECT_HOMOSAP + " cf");
        assertTextInIssueTable(PROJECT_HOMOSAP + " bug cf");
        assertTextInIssueTable(PROJECT_DOG + " cf");
        assertTextInIssueTable(PROJECT_DOG + " bug cf");

        // search both projects and bugs
        navigation.issueNavigator().createSearch("project in (" + dogProjectId + ", " + homosapId + ") AND issuetype= Bug");
        assertTextInIssueTable(PROJECT_HOMOSAP + " cf");
        assertTextInIssueTable(PROJECT_HOMOSAP + " bug cf");
        assertTextInIssueTable(PROJECT_DOG + " cf");
        assertTextInIssueTable(PROJECT_DOG + " bug cf");
    }

    public void testSearchAfterProjectRemoval()
    {
        log("Issue Navigator: Searching right after removal of the last searched project");
        administration.restoreData("TestIssueNavigatorSearchAfterProjectRemoval.xml");

        tester.gotoPage("/issues/?filter=" + homosapId);

        administration.project().deleteProject(homosapIdLong);

//        // test saved filter that searches deleted project, should not show hits for deleted project
//        dashboard();
//        clickLinkWithText("Monkey and Homosapien Bugs");
//        assertTextPresent("all 1 issue(s)");
//        assertTextPresent("Some Monkey Bug");
//        assertTextPresent("MKY-1");

        administration.project().addProject(PROJECT_HOMOSAP, PROJECT_HOMOSAP_KEY, ADMIN_USERNAME);

        navigation.issueNavigator().displayAllIssues();
        text.assertTextNotPresent("A system error has occurred");
    }

    /**
     * Tests to check if the Status searcher is displayed when the SearchContext is made invalid by specifying bad
     * Project IDs via the URL. Note that, if at least one Project ID is valid, then Status should be visible
     */
    public void testJQLWarningShownForInvalidProject()
    {
        log("Issue Navigator: Test project componenets visibility");
        administration.restoreData("TestIssueNavigatorProjectComponentsVisibility.xml");

        // specify one invalid PID - statuses should not appear

        issueTableAssertions.assertSearchWithError("project=99999", "A value with ID '99999' does not exist for the field 'project'.");

        // specify two invalid PIDs - statuses should not appear
        issueTableAssertions.assertSearchWithErrors("project=99999 AND project=88888", Lists.newArrayList("A value with ID '99999' does not exist for the field 'project'.", "A value with ID '88888' does not exist for the field 'project'."));

        // specify two invalid PIDs and one valid PID - statuses should appear
        issueTableAssertions.assertSearchWithErrors("project=99999 AND project=88888 AND project=10010", Lists.newArrayList("A value with ID '99999' does not exist for the field 'project'.", "A value with ID '88888' does not exist for the field 'project'."));
    }

    /**
     * Tests custom fields visibility on the issue navigator
     */
    public void testCustomfieldVisibility()
    {
        administration.restoreData("TestIssueNavigatorCustomfieldVisibility.xml");

        List<Searcher> searchers = backdoor.searchersClient().allSearchers("");
        assertSearcherPresent(searchers, CUSTOM_FIELD_GLOBAL);
        assertSearcherNotPresent(searchers, CUSTOM_FIELD_ISSUETYPE);
        assertSearcherNotPresent(searchers, CUSTOM_FIELD_PROJECT);
        assertSearcherNotPresent(searchers, CUSTOM_FIELD_ISSUETYPE_AND_PROJECT);

        searchers = backdoor.searchersClient().allSearchers("type=bug");
        assertSearcherPresent(searchers, CUSTOM_FIELD_GLOBAL);
        assertSearcherPresent(searchers, CUSTOM_FIELD_ISSUETYPE);
        assertSearcherNotPresent(searchers, CUSTOM_FIELD_PROJECT);
        assertSearcherNotPresent(searchers, CUSTOM_FIELD_ISSUETYPE_AND_PROJECT);

        searchers = backdoor.searchersClient().allSearchers("project=" + homosapId);
        assertSearcherPresent(searchers, CUSTOM_FIELD_GLOBAL);
        assertSearcherNotPresent(searchers, CUSTOM_FIELD_ISSUETYPE);
        assertSearcherPresent(searchers, CUSTOM_FIELD_PROJECT);
        assertSearcherNotPresent(searchers, CUSTOM_FIELD_ISSUETYPE_AND_PROJECT);

        searchers = backdoor.searchersClient().allSearchers("project=" + homosapId + " and type=bug");
        assertSearcherPresent(searchers, CUSTOM_FIELD_GLOBAL);
        assertSearcherPresent(searchers, CUSTOM_FIELD_ISSUETYPE);
        assertSearcherPresent(searchers, CUSTOM_FIELD_PROJECT);
        assertSearcherPresent(searchers, CUSTOM_FIELD_ISSUETYPE_AND_PROJECT);
    }

    private void assertSearcherPresent(List<Searcher> searchers, String name)
    {
        for (Searcher searcher : searchers)
        {
            if (name.equals(searcher.getName()))
            {
                assertTrue("Expected searcher with name " + name + " is shown", searcher.getShown());
                return;
            }
        }
        fail("Searcher with name " + name + " expected in response");
    }

    private void assertSearcherNotPresent(List<Searcher> searchers, String name)
    {
        for (Searcher searcher : searchers)
        {
            if (name.equals(searcher.getName()))
            {
                assertFalse("Expected searcher with name " + name + " is not shown", searcher.getShown());
            }
        }
    }

    public void testIssueNavigatorSortByCustomField()
    {
        log("Issue Navigator: Test that the filter correctly orders issues for custom fields.");
        administration.restoreData("TestIssueNavigatorCommon.xml");
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, ADMIN_USERNAME);

        navigation.issueNavigator().addColumnToIssueNavigator(customFieldNames);

        navigation.issueNavigator().displayAllIssues();//make sure there's a current search request
        for (int i = 0; i < customFieldIds.length; i++)
        {
            log("Sorting by " + customFieldNames[i]);
            navigation.issueNavigator().sortIssues("cf[" + customFieldIds[i] + "]", "ASC");
            text.assertTextSequence(new IdLocator(tester,"issuetable"),new String[]{issueKey, issueKey2, issueKey3, issueKey4, });

            navigation.issueNavigator().sortIssues("cf[" + customFieldIds[i] + "]", "DESC");
            text.assertTextSequence(new IdLocator(tester,"issuetable"),new String[]{issueKey4, issueKey3,issueKey2, issueKey, });
        }
        navigation.issueNavigator().restoreColumnDefaults();
    }

    public void testIssueNavigatorSortByComponent()
    {
        log("Issue Navigator: Test that the filter correctly orders issues for components.");
        administration.restoreData("TestIssueNavigatorCommon.xml");
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, ADMIN_USERNAME);

        tester.gotoPage("/issues/?jql=ORDER BY component ASC, key ASC");
        text.assertTextSequence(new IdLocator(tester,"issuetable"),new String[]{issueKey4, issueKey2, issueKey, issueKey3});

        tester.gotoPage("/issues/?jql=ORDER BY component DESC, key DESC");
        text.assertTextSequence(new IdLocator(tester,"issuetable"),new String[]{issueKey3, issueKey, issueKey2, issueKey4});
    }

    public void testIssueNavigatorHideReporter()
    {
        log("Issue Navigator: Test that the filter correctly hides the reporter field with full content view.");
        administration.restoreData("TestIssueNavigatorCommon.xml");
        administration.fieldConfigurations().defaultFieldConfiguration().hideFields(REPORTER_FIELD_ID);
        navigation.gotoFullContentView("");
        text.assertTextNotPresent("Reporter");
        tester.clickLinkWithText("test issue 1");

        administration.fieldConfigurations().defaultFieldConfiguration().showFields(REPORTER_FIELD_ID);
        navigation.gotoFullContentView("");
        text.assertTextPresent("Reporter");
        tester.clickLinkWithText("test issue 1");
    }

    public void testIssueNavigatorSelectGroup()
    {
        log("Issue Navigator: Test that all issues are filtered for a specific group");
        administration.restoreData("TestIssueNavigatorCommon.xml");
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, ADMIN_USERNAME);
        backdoor.usersAndGroups().addGroup(GROUP_NAME);
        backdoor.usersAndGroups().addUserToGroup(BOB_USERNAME, GROUP_NAME);
        final String testIssueKey = navigation.issue().createIssue(PROJECT_HOMOSAP,"Bug","test issue 5");

        backdoor.darkFeatures().enableForSite("no.frother.assignee.field");
        try
        {
            navigation.issue().assignIssue(testIssueKey, "Assign to Bob", BOB_FULLNAME);
        }
        finally
        {
            backdoor.darkFeatures().disableForSite("no.frother.assignee.field");
        }

        assertSearchWithResults("assignee in membersOf(\"" + GROUP_NAME + "\")", testIssueKey, issueKey3, issueKey);

        navigation.issue().deleteIssue(testIssueKey);
        administration.usersAndGroups().deleteGroup(GROUP_NAME);
    }

    public void testIssueNavigatorXMLViewWithCustomFields() throws Exception
    {
        log("Issue Navigator: Test that the RSS page correctly shows the custom field information.");
        administration.restoreData("TestIssueNavigatorCommon.xml");
        navigation.issueNavigator().addColumnToIssueNavigator(customFieldNames);
        navigation.gotoXmlView("");

        text.assertTextPresent("An XML representation of a search request");
        text.assertTextPresent("[" + issueKey + "] test issue 1");

        final Document doc = XMLUnit.buildControlDocument(tester.getDialog().getResponse().getText());
        for (final List<CustomFieldValue> values : cfValuesPerIssue)
        {
            for (final CustomFieldValue customFieldValue : values)
            {
                // Not testing the DatePicker because I don't know what format Jira has put the value into
                if (!customFieldValue.getCfType().equals(CUSTOM_FIELD_TYPE_DATEPICKER))
                {
                    log("Searching for existence of xpath " + "//item/customfields/customfield[@id='" + CUSTOM_FIELD_PREFIX + customFieldValue.getCfId() + "'][customfieldname='" + getCustomFieldNameFromType(customFieldValue.getCfType()) + "'][customfieldvalues[customfieldvalue='" + customFieldValue.getCfValue() + "']]");
                    XMLAssert.assertXpathExists("//item/customfields/customfield[@id='" + CUSTOM_FIELD_PREFIX + customFieldValue.getCfId() + "'][customfieldname='" + getCustomFieldNameFromType(customFieldValue.getCfType()) + "'][customfieldvalues[customfieldvalue='" + customFieldValue.getCfValue() + "']]", doc);
                }
            }
        }

    }

    private String getCustomFieldNameFromType(final String type)
    {
        if (type.equals(CUSTOM_FIELD_TYPE_SELECT))
        {
            return CUSTOM_FIELD_SELECT;
        }
        else if (type.equals(CUSTOM_FIELD_TYPE_RADIO))
        {
            return CUSTOM_FIELD_RADIO;
        }
        else if (type.equals(CUSTOM_FIELD_TYPE_MULTISELECT))
        {
            return CUSTOM_FIELD_MULTI_SELECT;
        }
        else if (type.equals(CUSTOM_FIELD_TYPE_CHECKBOX))
        {
            return CUSTOM_FIELD_CHECKBOX;
        }
        else if (type.equals(CUSTOM_FIELD_TYPE_TEXTFIELD))
        {
            return CUSTOM_FIELD_TEXTFIELD;
        }
        else if (type.equals(CUSTOM_FIELD_TYPE_USERPICKER))
        {
            return CUSTOM_FIELD_USERPICKER;
        }
        else if (type.equals(CUSTOM_FIELD_TYPE_DATEPICKER))
        {
            return CUSTOM_FIELD_DATEPICKER;
        }
        else
        {
            return null;
        }
    }



    private void assertExpectedIssueLinksPresent()
    {
        tester.assertLinkNotPresentWithText("test issue 1");
        tester.assertLinkNotPresentWithText("test issue 3");
        tester.assertLinkPresentWithText("test issue 2");
        tester.assertLinkPresentWithText("test issue 4");
    }

    //------------------------------------------------------------------- testBackToPreviousViewLinks helper methods END

    /**
     * Tests that the sorting order of the issue navigator is correct and does not crash
     * as in JRA-12974.
     *
     */
    public void testNavigatorOrdering()
    {
        administration.restoreData("TestIssueNavigatorCommon.xml");
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, ADMIN_USERNAME);

        tester.gotoPage("/issues/?jql=ORDER BY summary ASC");
        text.assertTextSequence(new IdLocator(tester,"issuetable"),new String[]{
                "test issue 1",
                "test issue 2",
                "test issue 3",
                "test issue 4",
        });

        tester.gotoPage("/issues/?jql=ORDER BY assignee ASC");
        text.assertTextSequence(new IdLocator(tester,"issuetable"),new String[] {
                ADMIN_FULLNAME,
                ADMIN_FULLNAME,
                BOB_FULLNAME,
                BOB_FULLNAME,
        });

        tester.gotoPage("/issues/?jql=ORDER BY summary ASC, duedate ASC");
        // what can we test here if nothing is showing?
        text.assertTextSequence(new IdLocator(tester,"issuetable"),new String[]{
                "test issue 1",
                "test issue 2",
                "test issue 3",
                "test issue 4",
        });

        tester.gotoPage("/issues/?jql=ORDER BY summary ASC, workratio ASC");
        // test that the page doest crash?
        text.assertTextSequence(new IdLocator(tester,"issuetable"),new String[]{
                "test issue 1",
                "test issue 2",
                "test issue 3",
                "test issue 4",
        });
    }

    public void testSearchSortDescriptionForInvalidField() throws Exception
    {
        administration.restoreData("TestIssueNavigatorCommon.xml");
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, ADMIN_USERNAME);

        long id = Long.parseLong(backdoor.filters().createFilter("ORDER BY cf[10006] ASC", "My Test Filter"));

        tester.gotoPage("/issues/?filter=" + id);
        text.assertTextSequence(new IdLocator(tester,"issuetable"),new String[]{
                "test issue 1",
                "test issue 2",
                "test issue 3",
                "test issue 4",
        });

        // delete the custom field
        navigation.gotoAdminSection("view_custom_fields");
        tester.clickLink("del_customfield_10006");
        tester.submit("Delete");

        // redisplay the filter
        tester.gotoPage("/issues/?filter=" + id);
        tester.assertElementNotPresent("issuetable");
    }

    public void testNoColumnsDialog() throws Exception
    {
        administration.restoreData("TestIssueNavigatorNoColumns.xml");
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, ADMIN_USERNAME);

        navigation.issueNavigator().displayAllIssues();

        new TextAssertionsImpl().assertTextSequence(tester.getDialog().getResponseText(),
                new String[] { "No columns selected" });
    }

    // JRA-20241
    public void testCanSearchForTextWithDotAndColon() throws Exception
    {
        administration.restoreData("TestIssueNavigatorTextWithDotAndColon.xml");
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, ADMIN_USERNAME);

        navigation.issueNavigator().createSearch("description ~ \"d.dude:123\" order by key desc");
        assertions.getIssueNavigatorAssertions().assertExactIssuesInResults("TST-1");
        // Check that the aliases are there as well
        navigation.issueNavigator().createSearch("description ~ \"dude\" order by key asc");
        assertions.getIssueNavigatorAssertions().assertExactIssuesInResults("TST-1", "TST-2");
    }

    // JRA-14238
    public void testXssInImageUrls() throws Exception {
        administration.restoreData("TestImageUrlXss.xml");
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, ADMIN_USERNAME);
        navigation.issueNavigator().createSearch("");

        // priority icon URL
        tester.assertTextNotPresent("\"'/><script>alert('prioritiezz');</script>");
        tester.assertTextPresent("&quot;'/&gt;&lt;script&gt;alert('prioritiezz');&lt;/script&gt;");

        // issue type icon URL
        tester.assertTextNotPresent("\"'/><script>alert('issue typezz');</script>");
        tester.assertTextPresent("&quot;'/&gt;&lt;script&gt;alert('issue typezz');&lt;/script&gt;");
    }

    private void assertTextNotInIssueTable(String pagetext)
    {
        text.assertTextNotPresent(new XPathLocator(tester,"//table[@id='issuetable']"), pagetext);
    }

    private void assertTextInIssueTable(String pagetext)
    {
        text.assertTextPresent(new XPathLocator(tester,"//table[@id='issuetable']"), pagetext);
    }
}
