package com.atlassian.jira.webtests.ztests.dashboard.reports;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.assertions.LabelAssertions;
import com.atlassian.jira.functest.framework.assertions.TableAssertions;
import com.atlassian.jira.functest.framework.labels.Labels;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.rest.api.issue.IssueCreateResponse;
import com.meterware.httpunit.WebTable;

/**
 * @since v4.2
 */
@WebTest ({ Category.FUNC_TEST, Category.REPORTS })
public class TestSingleLevelGroupByReportByLabels extends FuncTestCase {

    public void testSeveralIssuesWithSharedAndDistinctLabels ()
    {
        administration.restoreDataAndLogin ("TestSeveralIssuesWithSharedAndDistinctLabels.xml", ADMIN_USERNAME);

        // FIRST CHECK THAT THE RESTORE WORKED

        final String ID_BUG_WITH_NO_LABEL               = "10000";
        final String ID_IMPROVEMENT_WITH_NO_LABEL       = "10001";
        final String ID_BUG_WITH_LABEL_A                = "10002";
        final String ID_BUG_WITH_LABEL_B                = "10003";
        final String ID_BUG_WITH_LABELS_A_AND_B         = "10004";
        final String ID_IMPROVEMENT_WITH_LABEL_C        = "10005";
        final String ID_IMPROVEMENT_WITH_LABEL_A        = "10006";
        final String ID_IMPROVEMENT_WITH_LABEL_B        = "10007";
        final String ID_IMPROVEMENT_WITH_LABELS_B_AND_C = "10008";

        final String KEY_BUG_WITH_NO_LABEL               = "KEY-1";
        final String KEY_IMPROVEMENT_WITH_NO_LABEL       = "KEY-2";
        final String KEY_BUG_WITH_LABEL_A                = "KEY-3";
        final String KEY_BUG_WITH_LABEL_B                = "KEY-4";
        final String KEY_BUG_WITH_LABELS_A_AND_B         = "KEY-5";
        final String KEY_IMPROVEMENT_WITH_LABEL_C        = "KEY-6";
        final String KEY_IMPROVEMENT_WITH_LABEL_A        = "KEY-7";
        final String KEY_IMPROVEMENT_WITH_LABEL_B        = "KEY-8";
        final String KEY_IMPROVEMENT_WITH_LABELS_B_AND_C = "KEY-9";

        final Labels LABELS_NONE =    new Labels (true, false, false);
        final Labels LABELS_A       = new Labels (true, true, true, "A");
        final Labels LABELS_B       = new Labels (true, true, true, "B");
        final Labels LABELS_C       = new Labels (true, true, true, "C");
        final Labels LABELS_A_AND_B = new Labels (true, true, true, "A", "B");
        final Labels LABELS_B_AND_C = new Labels (true, true, true, "B", "C");

        LabelAssertions labelAssertions = assertions.getLabelAssertions();

        navigation.issue().viewIssue (KEY_BUG_WITH_NO_LABEL);
        labelAssertions.assertSystemLabels (ID_BUG_WITH_NO_LABEL, LABELS_NONE);

        navigation.issue().viewIssue (KEY_IMPROVEMENT_WITH_NO_LABEL);
        labelAssertions.assertSystemLabels(ID_IMPROVEMENT_WITH_NO_LABEL, LABELS_NONE);

        navigation.issue().viewIssue (KEY_BUG_WITH_LABEL_A);
        labelAssertions.assertSystemLabels (ID_BUG_WITH_LABEL_A, LABELS_A);

        navigation.issue().viewIssue (KEY_BUG_WITH_LABEL_B);
        labelAssertions.assertSystemLabels (ID_BUG_WITH_LABEL_B, LABELS_B);

        navigation.issue().viewIssue (KEY_BUG_WITH_LABELS_A_AND_B);
        labelAssertions.assertSystemLabels (ID_BUG_WITH_LABELS_A_AND_B, LABELS_A_AND_B);

        navigation.issue().viewIssue (KEY_IMPROVEMENT_WITH_LABEL_C);
        labelAssertions.assertSystemLabels (ID_IMPROVEMENT_WITH_LABEL_C, LABELS_C);

        navigation.issue().viewIssue (KEY_IMPROVEMENT_WITH_LABEL_A);
        labelAssertions.assertSystemLabels (ID_IMPROVEMENT_WITH_LABEL_A, LABELS_A);

        navigation.issue().viewIssue (KEY_IMPROVEMENT_WITH_LABEL_B);
        labelAssertions.assertSystemLabels (ID_IMPROVEMENT_WITH_LABEL_B, LABELS_B);

        navigation.issue().viewIssue (KEY_IMPROVEMENT_WITH_LABELS_B_AND_C);
        labelAssertions.assertSystemLabels (ID_IMPROVEMENT_WITH_LABELS_B_AND_C, LABELS_B_AND_C);

        // HOORAY
        // NOW CHECK THAT THE REPORT WORKS

        final long PROJECT_ID = 10010;
        final String REPORT_KEY = "com.atlassian.jira.plugin.system.reports:singlelevelgroupby";
        final long FILTER_ID = 10000;
        final String MAPPER = "labels";

        navigation.runReport (PROJECT_ID, REPORT_KEY, FILTER_ID, MAPPER);

        TableAssertions tableAssertions = assertions.getTableAssertions();

        WebTable webTable = tableAssertions.getWebTable ("single_groupby_report_table");

        tableAssertions.assertTableCellHasText (webTable, 1, 0, "Label: A");
        tableAssertions.assertTableCellHasText (webTable, 2, 1, KEY_IMPROVEMENT_WITH_LABEL_A);
        tableAssertions.assertTableCellHasText (webTable, 3, 1, KEY_BUG_WITH_LABELS_A_AND_B);
        tableAssertions.assertTableCellHasText (webTable, 4, 1, KEY_BUG_WITH_LABEL_A);

        tableAssertions.assertTableCellHasText (webTable, 5, 0, "Label: B");
        tableAssertions.assertTableCellHasText (webTable, 6, 1, KEY_IMPROVEMENT_WITH_LABELS_B_AND_C);
        tableAssertions.assertTableCellHasText (webTable, 7, 1, KEY_IMPROVEMENT_WITH_LABEL_B);
        tableAssertions.assertTableCellHasText (webTable, 8, 1, KEY_BUG_WITH_LABELS_A_AND_B);
        tableAssertions.assertTableCellHasText (webTable, 9, 1, KEY_BUG_WITH_LABEL_B);

        tableAssertions.assertTableCellHasText (webTable, 10, 0, "Label: C");
        tableAssertions.assertTableCellHasText (webTable, 11, 1, KEY_IMPROVEMENT_WITH_LABELS_B_AND_C);
        tableAssertions.assertTableCellHasText (webTable, 12, 1, KEY_IMPROVEMENT_WITH_LABEL_C);

        tester.assertTextNotPresent (KEY_BUG_WITH_NO_LABEL);
        tester.assertTextNotPresent (KEY_IMPROVEMENT_WITH_NO_LABEL);
    }

    public void testXss() throws Exception
    {
        administration.restoreBlankInstance();
        final IssueCreateResponse issue = backdoor.issues().createIssue("HSP", "Label XSS test issue");
        final String xss = "<script>alert(1)</script>";
        backdoor.issues().addLabel(issue.key(), xss);

        final String filter = backdoor.searchRequests().createFilter("admin", "project = HSP", "all HSP", "Everything in the Homosapien project");

        navigation.runReport(10000L, "com.atlassian.jira.plugin.system.reports:singlelevelgroupby", Long.parseLong(filter), "labels");
        tester.assertTextNotPresent(xss);
    }
}
