package com.atlassian.jira.webtests.ztests.subtask;

import java.util.List;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

import com.google.common.collect.Lists;
import com.meterware.httpunit.TableCell;
import com.meterware.httpunit.WebTable;

import org.apache.commons.lang.StringUtils;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.w3c.dom.Node;

@WebTest ({ Category.FUNC_TEST, Category.SUB_TASKS })
public class TestSubTaskProgressBar extends FuncTestCase
{
    public static final int SUBTASK_COUNT = 5;

    /**
     * Verify that subtasks progress bar advances with each closed subtask.
     */
    public void testSubTaskProgressIsNotStuck()
    {
        administration.restoreBlankInstance();
        administration.timeTracking().disable();
        administration.subtasks().enable();

        final String parentIssueKey = navigation.issue().createIssue("homosapien", "Bug", "Parent Summary");

        final List<String> childIssueKeys = Lists.newArrayListWithCapacity(SUBTASK_COUNT);
        for (int i = 0; i < SUBTASK_COUNT; i++)
        {
            childIssueKeys.add(navigation.issue().
                    createSubTask(parentIssueKey, SUB_TASK_DEFAULT_TYPE, "Child summary " + i, "Sub Task Desc"));
        }
        for (int i = 0; i < SUBTASK_COUNT; i++)
        {
            navigation.issue().resolveIssue(childIssueKeys.get(i), "Fixed", "Resolving the Issue as Fixed. This should increase the progress in parent issue.");
            navigation.issue().gotoIssue(parentIssueKey);
            assertSubtaskProgressTableStyleCorrect(i);
        }
    }

    private void assertSubtaskProgressTableStyleCorrect(final int i)
    {
        final WebTable subtasksResolutionPercentage = tester.getDialog().getWebTableBySummaryOrId("subtasks_resolution_percentage");
        final TableCell tableCell = subtasksResolutionPercentage.getTableCell(0, 0);
        final Node attribute = tableCell.getDOM().getAttributes().getNamedItem("style");
        Assert.assertThat(StringUtils.deleteWhitespace(attribute.getNodeValue()), Matchers.is(getStyleForSubtask(i)));
    }

    private String getStyleForSubtask(final int i)
    {
        return String.format("width:%d%%;background-color:#51A825", (i + 1) * (100 / SUBTASK_COUNT));
    }

}
