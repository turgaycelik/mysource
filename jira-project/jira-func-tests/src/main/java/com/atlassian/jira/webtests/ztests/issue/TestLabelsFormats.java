package com.atlassian.jira.webtests.ztests.issue;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.assertions.LabelAssertions;
import com.atlassian.jira.functest.framework.labels.Labels;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.util.collect.CollectionBuilder;
import org.w3c.dom.Element;

import java.util.Set;

import static com.atlassian.jira.functest.framework.backdoor.IssuesControl.LIST_VIEW_LAYOUT;

@WebTest ({ Category.FUNC_TEST, Category.BULK_OPERATIONS, Category.FIELDS, Category.ISSUES })
public class TestLabelsFormats extends FuncTestCase
{
    private static final String HSP1_ID = "10000";
    private static final String HSP2_ID = "10001";
    private static final String LABELS_FIELD = "labels";
    private static final String EPIC_FIELD = "customfield_10000";
    private static final Set<String> EMPTY_LABELS = CollectionBuilder.<String>newBuilder().asHashSet();
    private static final Set<String> EXPECTED_LABELS = CollectionBuilder.newBuilder("1111", "2222", "333,333", "444").asHashSet();
    private static final Set<String> EXPECTED_EPICS = CollectionBuilder.newBuilder("aaa", "bbbb", "cccc,ccc", "dddd").asHashSet();
    private static final String NEW = "new_";
    private static final String OLD = "old_";

    public void testViewIssue()
    {
        administration.restoreData("TestLabelsFormat.xml");
        navigation.issue().viewIssue("HSP-1");

        final LabelAssertions labelAssertions = assertions.getLabelAssertions();
        Labels expectedLabels = new Labels(true, true, true, EXPECTED_LABELS);
        Labels expectedEpic = new Labels(true, true, true, EXPECTED_EPICS);

        labelAssertions.assertLabels(HSP1_ID, LABELS_FIELD, expectedLabels);
        labelAssertions.assertLabels(HSP1_ID, EPIC_FIELD, expectedEpic);


        navigation.issue().viewPrintable("HSP-1");
        expectedLabels = new Labels(false, false, false, EXPECTED_LABELS);
        expectedEpic = new Labels(false, false, false, EXPECTED_EPICS);

        labelAssertions.assertLabels(HSP1_ID, LABELS_FIELD, expectedLabels);
        labelAssertions.assertLabels(HSP1_ID, EPIC_FIELD, expectedEpic);

        navigation.issue().viewIssue("HSP-2");
        expectedLabels = new Labels(true, false, false, EMPTY_LABELS);

        labelAssertions.assertLabels(HSP2_ID, LABELS_FIELD, expectedLabels);
        labelAssertions.assertLabelsDontExist(HSP2_ID, EPIC_FIELD);

        navigation.issue().viewPrintable("HSP-2");
        expectedLabels = new Labels(false, false, false, EMPTY_LABELS);
        labelAssertions.assertLabels(HSP2_ID, LABELS_FIELD, expectedLabels);
        labelAssertions.assertLabelsDontExist(HSP2_ID, EPIC_FIELD);

        // Move issue
        navigation.issue().viewIssue("HSP-2");
        tester.clickLink("move-issue");

        tester.selectOption("issuetype", "New Feature");
        tester.clickButton("next_submit");

        navigation.gotoPage("/secure/MoveIssueUpdateFields.jspa?id=10001&" + createUrlString(LABELS_FIELD, EXPECTED_LABELS) + "&" + createUrlString(EPIC_FIELD, EXPECTED_EPICS));

        // new values
        expectedLabels = new Labels(false, false, true, EXPECTED_LABELS);
        expectedEpic = new Labels(false, false, true, EXPECTED_EPICS);
        labelAssertions.assertLabels(HSP2_ID, NEW + LABELS_FIELD, expectedLabels);
        labelAssertions.assertLabels(HSP2_ID, NEW + EPIC_FIELD, expectedEpic);

        // old values
        expectedLabels = new Labels(false, false, false, EMPTY_LABELS);
        expectedEpic = new Labels(false, false, false, EMPTY_LABELS);
        labelAssertions.assertLabels(HSP2_ID, OLD + LABELS_FIELD, expectedLabels);
        labelAssertions.assertLabels(HSP2_ID, OLD + EPIC_FIELD, expectedEpic);


        // convert
        navigation.issue().viewIssue("HSP-2");
        tester.clickLink("issue-to-subtask");

        tester.setFormElement("parentIssueKey", "HSP-1");
        tester.clickButton("next_submit");

        final Element guid = tester.getDialog().getElement("guid");
        final String guidStr = guid.getAttribute("value");

        navigation.gotoPage("/secure/ConvertIssueUpdateFields.jspa?id=10001&guid=" + guidStr + "&" + createUrlString(LABELS_FIELD, EXPECTED_LABELS) + "&" + createUrlString(EPIC_FIELD, EXPECTED_EPICS));
        // new values
        expectedLabels = new Labels(false, false, true, EXPECTED_LABELS);
        expectedEpic = new Labels(false, false, true, EXPECTED_EPICS);
        labelAssertions.assertLabels(HSP2_ID, NEW + LABELS_FIELD, expectedLabels);
        labelAssertions.assertLabels(HSP2_ID, NEW + EPIC_FIELD, expectedEpic);

        // old values
        expectedLabels = new Labels(false, false, false, EMPTY_LABELS);
        expectedEpic = new Labels(false, false, false, EMPTY_LABELS);
        labelAssertions.assertLabels(HSP2_ID, OLD + LABELS_FIELD, expectedLabels);
        labelAssertions.assertLabels(HSP2_ID, OLD + EPIC_FIELD, expectedEpic);

    }

    public void testIssueNav()
    {
        administration.restoreData("TestLabelsFormat.xml");
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, ADMIN_USERNAME);
        navigation.issueNavigator().displayAllIssues();

        final LabelAssertions labelAssertions = assertions.getLabelAssertions();

        // Issue nav
        Labels expectedLabels = new Labels(false, true, true, EXPECTED_LABELS);
        Labels expectedEpic = new Labels(false, true, true, EXPECTED_EPICS);
        labelAssertions.assertLabels(HSP1_ID, LABELS_FIELD, expectedLabels);
        labelAssertions.assertLabels(HSP1_ID, EPIC_FIELD, expectedEpic);

        expectedLabels = new Labels(false, false, false, EMPTY_LABELS);
        expectedEpic = new Labels(false, false, false, EMPTY_LABELS);
        labelAssertions.assertLabels(HSP2_ID, LABELS_FIELD, expectedLabels);
        labelAssertions.assertLabels(HSP2_ID, EPIC_FIELD, expectedEpic);

        // printablle view
        navigation.issueNavigator().displayPrintableAllIssues();
        expectedLabels = new Labels(false, true, true, EXPECTED_LABELS);
        expectedEpic = new Labels(false, true, true, EXPECTED_EPICS);
        labelAssertions.assertLabels(HSP1_ID, LABELS_FIELD, expectedLabels);
        labelAssertions.assertLabels(HSP1_ID, EPIC_FIELD, expectedEpic);

        expectedLabels = new Labels(false, false, false, EMPTY_LABELS);
        expectedEpic = new Labels(false, false, false, EMPTY_LABELS);
        labelAssertions.assertLabels(HSP2_ID, LABELS_FIELD, expectedLabels);
        labelAssertions.assertLabels(HSP2_ID, EPIC_FIELD, expectedEpic);

        // Full content
        navigation.issueNavigator().displayFullContentAllIssues();
        expectedLabels = new Labels(false, false, false, EXPECTED_LABELS);
        expectedEpic = new Labels(false, false, false, EXPECTED_EPICS);
        labelAssertions.assertLabels(HSP1_ID, LABELS_FIELD, expectedLabels);
        labelAssertions.assertLabels(HSP1_ID, EPIC_FIELD, expectedEpic);

        expectedLabels = new Labels(false, false, false, EMPTY_LABELS);
        labelAssertions.assertLabels(HSP2_ID, LABELS_FIELD, expectedLabels);
        labelAssertions.assertLabelsDontExist(HSP2_ID, EPIC_FIELD);
    }

    public void testBulkOperations()
    {
        administration.restoreData("TestLabelsFormat.xml");
        final LabelAssertions labelAssertions = assertions.getLabelAssertions();

        navigation.issueNavigator().bulkEditAllIssues();
        Labels expectedLabels = new Labels(false, true, true, EXPECTED_LABELS);
        Labels expectedEpic = new Labels(false, true, true, EXPECTED_EPICS);
        labelAssertions.assertLabels(HSP1_ID, LABELS_FIELD, expectedLabels);
        labelAssertions.assertLabels(HSP1_ID, EPIC_FIELD, expectedEpic);

        expectedLabels = new Labels(false, false, false, EMPTY_LABELS);
        expectedEpic = new Labels(false, false, false, EMPTY_LABELS);
        labelAssertions.assertLabels(HSP2_ID, LABELS_FIELD, expectedLabels);
        labelAssertions.assertLabels(HSP2_ID, EPIC_FIELD, expectedEpic);

        tester.checkCheckbox("bulkedit_10001");
        tester.submit("Next");

        tester.setFormElement("operation", "bulk.edit.operation.name");
        tester.submit("Next");
        final String url = "/secure/BulkEditDetailsValidation.jspa?actions=labels&actions=customfield_10000&" + createUrlString(LABELS_FIELD, EXPECTED_LABELS) + "&" + createUrlString(EPIC_FIELD, EXPECTED_EPICS);
        navigation.gotoPage(page.addXsrfToken(url));

        expectedLabels = new Labels(false, false, true, EXPECTED_LABELS);
        expectedEpic = new Labels(false, false, true, EXPECTED_EPICS);
        labelAssertions.assertLabels(HSP2_ID, NEW + LABELS_FIELD, expectedLabels);
        labelAssertions.assertLabels(HSP2_ID, NEW + EPIC_FIELD, expectedEpic);

        expectedLabels = new Labels(false, false, false, EMPTY_LABELS);
        expectedEpic = new Labels(false, false, false, EMPTY_LABELS);
        labelAssertions.assertLabels(HSP2_ID, LABELS_FIELD, expectedLabels);
        labelAssertions.assertLabels(HSP2_ID, EPIC_FIELD, expectedEpic);

        navigation.issueNavigator().bulkEditAllIssues();
        tester.checkCheckbox("bulkedit_10001");
        tester.submit("Next");

        tester.setFormElement("operation", "bulk.move.operation.name");
        tester.submit("Next");

        tester.selectOption("10000_1_issuetype", "New Feature");
        tester.submit("Next");
        navigation.gotoPage("/secure/BulkMigrateSetFields.jspa?" + createUrlString(LABELS_FIELD, EXPECTED_LABELS) + "&" + createUrlString(EPIC_FIELD, EXPECTED_EPICS));

        expectedLabels = new Labels(false, false, true, EXPECTED_LABELS);
        expectedEpic = new Labels(false, false, true, EXPECTED_EPICS);
        labelAssertions.assertLabels(HSP2_ID, "10000_1_" + LABELS_FIELD, expectedLabels);
        labelAssertions.assertLabels(HSP2_ID, "10000_1_" + EPIC_FIELD, expectedEpic);

        // bulk transition
        navigation.issueNavigator().bulkEditAllIssues();
        tester.checkCheckbox("bulkedit_10001");
        tester.submit("Next");

        tester.setFormElement("operation", "bulk.workflowtransition.operation.name");
        tester.submit("Next");

        tester.setFormElement("wftransition", "jira_5_5");
        tester.submit("Next");

        navigation.gotoPage("/secure/BulkWorkflowTransitionEditValidation.jspa?actions=resolution&resolution=1&forcedResolution=resolution&actions=labels&actions=customfield_10000&" + createUrlString(LABELS_FIELD, EXPECTED_LABELS) + "&" + createUrlString(EPIC_FIELD, EXPECTED_EPICS));
        expectedLabels = new Labels(false, false, true, EXPECTED_LABELS);
        expectedEpic = new Labels(false, false, true, EXPECTED_EPICS);
        labelAssertions.assertLabels(HSP2_ID, NEW + LABELS_FIELD, expectedLabels);
        labelAssertions.assertLabels(HSP2_ID, NEW + EPIC_FIELD, expectedEpic);


    }

    private String createUrlString(String field, Set<String> labels)
    {
        final StringBuilder stringBuilder = new StringBuilder();
        for (String label : labels)
        {
            stringBuilder.append(field).append("=").append(label).append("&");
        }

        return stringBuilder.substring(0, stringBuilder.length() - 1);
    }

}