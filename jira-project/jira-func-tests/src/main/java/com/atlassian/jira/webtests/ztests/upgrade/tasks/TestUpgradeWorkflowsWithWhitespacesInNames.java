package com.atlassian.jira.webtests.ztests.upgrade.tasks;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.WebTest;
import org.hamcrest.collection.IsIterableWithSize;

import static com.atlassian.jira.functest.framework.suite.Category.FUNC_TEST;
import static com.atlassian.jira.functest.framework.suite.Category.UPGRADE_TASKS;
import static org.junit.Assert.assertThat;

/**
 * @since 5.1.8
 */
@WebTest ({ FUNC_TEST, UPGRADE_TASKS })
public class TestUpgradeWorkflowsWithWhitespacesInNames extends FuncTestCase
{
    private static final String WHITESPACES_BUILD_664 =
            "TestUpgradeWorkflowsWithWhitespacesInNames/TestRemoveWhiteSpacesFromWorkflowNamesBuild664.xml";
    private static final String WHITESPACES_BUILD_713 =
            "TestUpgradeWorkflowsWithWhitespacesInNames/TestRemoveWhiteSpacesFromWorkflowNamesBuild713.xml";

    /**
     * this test if upgrade task UpgradeTask_Build701 will not fail
     */
    public void testUpgradeFrom4xShouldSuccedWhenWhitespacesInWOrkflowNamesPresnt()
    {
        administration.restoreDataWithBuildNumber(WHITESPACES_BUILD_664, 664);
        assertThat(backdoor.workflow().getWorkflows(), IsIterableWithSize.<String>iterableWithSize(6));
    }

    /**
     * this test if upgrade task UpgradeTask_Build813 will not fail
     */
    public void testUpgradeFrom50ShouldSuccedWhenWhitespacesInWOrkflowNamesPresnt()
    {
        administration.restoreDataWithBuildNumber(WHITESPACES_BUILD_713, 713);
        assertThat(backdoor.workflow().getWorkflows(), IsIterableWithSize.<String>iterableWithSize(7));
    }

}
