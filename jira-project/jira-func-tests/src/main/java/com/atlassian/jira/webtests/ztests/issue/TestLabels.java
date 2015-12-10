package com.atlassian.jira.webtests.ztests.issue;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.changehistory.ChangeHistoryList;
import com.atlassian.jira.functest.framework.changehistory.ChangeHistoryParser;
import com.atlassian.jira.functest.framework.labels.Labels;
import com.atlassian.jira.functest.framework.locator.TableLocator;
import com.atlassian.jira.functest.framework.locator.WebPageLocator;
import com.atlassian.jira.functest.framework.locator.XPathLocator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.functest.framework.util.form.FormParameterUtil;
import com.atlassian.jira.util.collect.MapBuilder;
import org.hamcrest.Matchers;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Map;

import static com.atlassian.jira.functest.framework.backdoor.IssuesControl.LIST_VIEW_LAYOUT;

import static org.junit.Assert.assertThat;

@WebTest ({ Category.FUNC_TEST, Category.ISSUES, Category.JQL })
public class TestLabels extends FuncTestCase
{
    public void testSearching()
    {
        administration.restoreData("TestLabels.xml");
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, ADMIN_USERNAME);

        _testSearching("labels", "labels", "Label", false);
    }

    public void testCustomFieldSearching()
    {
        administration.restoreData("TestLabels.xml");
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, ADMIN_USERNAME);

        _testSearching("customfield_10000", "Epic", "Epic", true);
    }

    // The "edit" pencil shouldn't show when the issue is closed
    public void testNoEditWhenClosed() throws Exception
    {
        administration.restoreData("TestLabels.xml");
        backdoor.darkFeatures().enableForSite("ka.NO_GLOBAL_SHORTCUT_LINKS");

        navigation.issue().closeIssue("HSP-1", "Fixed", "closing for testing purposes");

        // view issue
        navigation.issue().gotoIssue("HSP-1");
        assertions.assertNodeDoesNotExist("//*[contains(@class, 'edit-labels')]");

        // issue navigator
        navigation.issueNavigator().createSearch("key = \"HSP-1\"");
        assertions.assertNodeDoesNotExist("id('labels-10001-value')//*[contains(@class, 'edit-labels')]");
        assertions.assertNodeDoesNotExist("id('customfield_10000-10001-value')//*[contains(@class, 'edit-labels')]");
    }

    private void _testSearching(String fieldId, String field, String fieldName, boolean custom)
    {
        //check in works case insensitive
        navigation.issueNavigator().createSearch(field + " in (aa, TeSt)");
        TableLocator locator = new TableLocator(tester, "issuetable");
        text.assertTextPresent(locator, "HSP-2");
        text.assertTextPresent(locator, "HSP-1");

        //now check an equals search!
        //check in works case insensitive
        navigation.issueNavigator().createSearch(field + " = \"fIRst\"");
        locator = new TableLocator(tester, "issuetable");
        text.assertTextPresent(locator, "HSP-1");
        text.assertTextNotPresent(locator, "HSP-2");

        //search for empty...shouldn't find anything
        navigation.issueNavigator().createSearch(field + " is empty");
        tester.assertElementNotPresent("issuetable");

        final String issueKey = navigation.issue().createIssue("homosapien", "Bug", "Another test issue!!!!");
        //search for empty...should find the new issue now
        navigation.issueNavigator().createSearch(field + " is empty");
        text.assertTextNotPresent(new WebPageLocator(tester), "No matching issues found. ");
        locator = new TableLocator(tester, "issuetable");
        text.assertTextPresent(locator, issueKey);

        navigation.issueNavigator().createSearch(field + " in (TeST, aa)");
        locator = new TableLocator(tester, "issuetable");
        text.assertTextPresent(locator, "HSP-1");
        text.assertTextPresent(locator, "HSP-2");

        //JRADEV-1342: test not searching
        navigation.issueNavigator().createSearch(field + " != couple");
        locator = new TableLocator(tester, "issuetable");
        text.assertTextPresent(locator, "HSP-2");
        text.assertTextNotPresent(locator, "HSP-1");
        text.assertTextNotPresent(locator, "HSP-3");

        navigation.issueNavigator().createSearch(field + " not in (aa, bb)");
        locator = new TableLocator(tester, "issuetable");
        text.assertTextNotPresent(locator, "HSP-2");
        text.assertTextPresent(locator, "HSP-1");
        text.assertTextNotPresent(locator, "HSP-3");
    }

    public void testEditIssueLabelsDoesCreateChangeItem() throws Exception
    {
        administration.restoreData("TestLabelsHistory.xml");
        navigation.issue().gotoEditIssue("HSP-3");
        FormParameterUtil formParameterUtil = new FormParameterUtil(tester, "issue-edit","Update");
        formParameterUtil.addOptionToHtmlSelect("labels", new String[]{"Label"});
        formParameterUtil.submitForm();
        navigation.issue().gotoIssueChangeHistory("HSP-3");
        assertHistoryContains(ADMIN_FULLNAME, "Labels", "", "Label");
    }

    public void testCreateIssueLabelsDoesNotCreateChangeItem() throws Exception
    {
        administration.restoreData("TestLabelsHistory.xml");
        Map<String,String[]>  params = MapBuilder.<String, String[]>newBuilder().add("labels",new String[]{"label"}).toMap();
        String issueKey = navigation.issue().createIssue("homosapien","Bug","Bug With Labels",params);
        navigation.issue().gotoIssueChangeHistory(issueKey);
        assertNoHistory();
    }

    public void testEditIssueLabelsStandaloneDoesNotSuck()
    {
        final Labels expectedLabels = new Labels(true, true, true, "TEST", "aaafirst", "couple", "first", "labels", "of", "test");
        administration.restoreData("TestLabelsHistory.xml");
        navigation.issue().gotoIssue("HSP-1");
        final String labelsDomId = "labels-10000-labels";
        assertions.getLabelAssertions().assertLabels("10000", "labels", expectedLabels);
        navigation.issue().editLabels(10000);
        assertions.assertNodeExists("//input[@type='hidden'][@name='id']");
        assertions.assertNodeExists("//select[@id='labels']");
        tester.submit("edit-labels-submit");
        // Haven't changed the selected labels, so all should remain.
        assertions.getLabelAssertions().assertLabels("10000", "labels", expectedLabels);
    }

    public void testEditCustomIssueLabelsStandaloneDoesNotSuck()
    {
        final String customFieldName = "customfield_10000";
        final String labelsDomId = "labels-10000-" + customFieldName;
        final Labels expectedLabels = new Labels(true, true, true, "TEST", "aaafirst", "couple", "first", "labels", "of", "test");

        administration.restoreData("TestLabelsHistory.xml");
        navigation.issue().gotoIssue("HSP-1");
        assertions.getLabelAssertions().assertLabels("10000", "labels", expectedLabels);

        navigation.issue().editCustomLabels(10000, 10000);
        assertions.assertNodeExists("//input[@type='hidden'][@name='id']");
        assertions.assertNodeExists("//input[@type='hidden'][@name='customFieldId']");
        assertions.assertNodeExists("//select[@id='" + customFieldName + "']");
        tester.submit("edit-labels-submit");
        // Haven't changed the selected labels, so all should remain.
        assertions.getLabelAssertions().assertLabels("10000", "labels", expectedLabels);
    }

    public void testCancelOnEditLabelsStandaloneNavigatesToIssue()
    {
        administration.restoreData("TestLabelsHistory.xml");
        navigation.issue().gotoIssue("HSP-1");
        navigation.issue().editLabels(10000);
        tester.clickLink("cancel");
        assertions.getViewIssueAssertions().assertOnViewIssuePage("HSP-1");
    }

    public void testEditSystemLabelsValidation() throws Exception
    {
        administration.restoreData("TestLabelsHistory.xml");
        navigation.issue().gotoIssue("HSP-1");
        navigation.issue().editLabels(10000);
        FormParameterUtil parameterUtil = new FormParameterUtil(tester, "edit-labels-form","edit-labels-submit");
        _addInvalidLabel(parameterUtil);
        _assertLabelValidation("labels", parameterUtil);
    }

    public void testEditCustomLabelsValidation() throws Exception
    {
        administration.restoreData("TestLabelsHistory.xml");
        navigation.issue().gotoIssue("HSP-1");
        navigation.issue().editCustomLabels(10000, 10000);
        FormParameterUtil parameterUtil = new FormParameterUtil(tester,"edit-labels-form","edit-labels-submit");
        _addInvalidLabel(parameterUtil);
        _assertLabelValidation("customfield_10000", parameterUtil);
    }

    public void testLabelsJqlLinks() throws UnsupportedEncodingException
    {
        administration.restoreData("TestLabelsWithIssuesForJQL.xml");
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, ADMIN_USERNAME);

        final int issueId = 10000;
        final int customFieldId = 10000;
        final String[] indexToLabel = { "TEST", "aaafirst", "couple", "\"first\"", "labels", "\"of\"", "test" };

        // View issue.
        for (int i = 0; i < indexToLabel.length; i++)
        {
            String issueToCheck = "HSP-" + (i + 4);
            String summaryToCheck = "Issue for " + indexToLabel[i];

            navigation.issue().gotoIssue("HSP-1");
            assertJqlLinkForLabel(issueId, null, i, indexToLabel[i], summaryToCheck, issueToCheck);
            navigation.issue().gotoIssue("HSP-1");
            assertJqlLinkForLabel(issueId, customFieldId, i, indexToLabel[i], summaryToCheck, issueToCheck);
        }
    }
    private void assertJqlLinkForLabel(final int issueId, final Integer customFieldId, final int labelIndex,
                                       final String labelText, String issueSummary, String issue)
            throws UnsupportedEncodingException
    {
        String fieldId = null;
        String fieldName = null;
        if (customFieldId == null)
        {
            fieldId = fieldName = "labels";
        }
        else
        {
            fieldId = "customfield_" + customFieldId;
            fieldName = "cf[" + customFieldId + "]";
        }
        String jqlHref = xpath("//ul[@id='" + fieldId + "-" + issueId + "-value']//a").getNodes()[labelIndex]
                .getAttributes()
                .getNamedItem("href")
                .getNodeValue();
        tester.gotoPage(jqlHref.replace(this.getEnvironmentData().getContext(), ""));

        String url = URLDecoder.decode(tester.getDialog().getResponse().getURL().toString(), "UTF-8");
        assertThat(url, Matchers.containsString(fieldName + " = " + labelText));

        tester.assertTextInElement("issuetable", issueSummary);
        tester.assertTextInElement("issuetable", issue);
    }

    private void _addInvalidLabel(final FormParameterUtil formParameterUtil) {
        formParameterUtil.addOptionToHtmlSelect("labels", new String[]{"A B"});
    }

    private void _assertLabelValidation(final String fieldName, final FormParameterUtil formParameterUtil) throws IOException, SAXException
    {
        final String domId = fieldName + "-error";
        XPathLocator locator = new XPathLocator(formParameterUtil.submitForm(), String.format("//*[@id='%s']", domId));
        assertNotNull(String.format("%s-error should exist", domId), locator.getNode());
    }

    private void assertNoHistory() throws Exception
    {
        ChangeHistoryList actualList = ChangeHistoryParser.getChangeHistory(tester);
        assertTrue(actualList.isEmpty());
    }

    private void assertHistoryContains(String changedBy, String fieldName, String oldValue, String newValue) throws Exception
    {
        ChangeHistoryList expectedList = new ChangeHistoryList();
        expectedList.addChangeSet(changedBy).add(fieldName,oldValue,newValue);
        ChangeHistoryList actualList = ChangeHistoryParser.getChangeHistory(tester);
        actualList.assertContainsChangeHistory(expectedList);
    }
}
