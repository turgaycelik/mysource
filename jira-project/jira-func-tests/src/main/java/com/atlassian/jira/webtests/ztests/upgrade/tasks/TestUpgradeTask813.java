package com.atlassian.jira.webtests.ztests.upgrade.tasks;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.WebTest;

import static com.atlassian.jira.functest.framework.suite.Category.FUNC_TEST;
import static com.atlassian.jira.functest.framework.suite.Category.UPGRADE_TASKS;

/**
 * Responsible for verifying that we clean up all orphaned workflow drafts.
 *
 * Moved from UT761 -> UT813. Need to run it again but no point in running it twice.
 *
 * @since v5.1
 */
@WebTest ({ FUNC_TEST, UPGRADE_TASKS })
public class TestUpgradeTask813 extends FuncTestCase
{
    private static final String SHORT_WORKFLOW_NAME =
            "TestUpgradeTask761/inactive-workflow-drafts-present.xml";

    /**
     * Contains a draft that has a parent workflow with a 255 characters name.
     */
    private static final String LONG_WORKFLOW_NAME =
            "TestUpgradeTask761/inactive-workflow-drafts-present-long-parent-workflow-name.xml";

    /**
     * Contains draft that has a parent workflow with a 255 characters name, and a copy of the parent
     * workflow.
     */
    private static final String LONG_WORKFLOW_NAME_AND_COPY_PRESENT =
            "TestUpgradeTask761/inactive-workflow-drafts-present-long-parent-workflow-name-copy-present.xml";

    public void testBackupCopiesShouldBeCreatedForAllInactiveWorkflowDrafts()
    {
        administration.restoreDataWithBuildNumber(SHORT_WORKFLOW_NAME, 700);

        assertTrue(administration.workflows().goTo().inactive().contains("Copy of Workflow1"));
    }

    public void testAllInactiveWorkflowDraftsAreRemoved()
    {
        administration.restoreDataWithBuildNumber(SHORT_WORKFLOW_NAME, 700);

        assertTrue(administration.workflows().goTo().inactive().drafts().isEmpty());
    }

    public void testNameOfTheADraftCopyDoesNotExceedTheMaxNumberOfAllowedCharsWhenTheNameOfTheParentContainsTheMaxNumberOfAllowedChars()
    {
        final String expectedDraftWorkflowCopyName = "Copy of Lorem ipsum dolor sit amet, consectetur adipiscing elit. "
                + "Morbi condimentum ornare eros ut adipiscing. "
                + "In hac habitasse platea dictumst. "
                + "Cras mattis euismod mi. In elit arcu, placerat at placerat lacinia, molestie id mauris. "
                + "Curabitur eu lacus a...";

        administration.restoreDataWithBuildNumber(LONG_WORKFLOW_NAME, 700);

        assertTrue(administration.workflows().goTo().inactive().contains(expectedDraftWorkflowCopyName));
    }

    public void testNameOfTheADraftCopyDoesNotExceedTheMaxNumberOfAllowedCharsWhenTheNameOfTheParentContainsTheMaxNumberOfAllowedCharsAndACopyOfTheParentAlsoExists()
    {
          final String expectedDraftWorkflowCopyName = "Copy 2 of Lorem ipsum dolor sit amet, consectetur adipiscing elit. "
                  + "Morbi condimentum ornare eros ut adipiscing. In hac habitasse platea dictumst. "
                  + "Cras mattis euismod mi. In elit arcu, placerat at placerat lacinia, molestie id mauris. "
                  + "Curabitur eu lacus...";

        administration.restoreDataWithBuildNumber(LONG_WORKFLOW_NAME_AND_COPY_PRESENT, 700);

        assertTrue(administration.workflows().goTo().inactive().contains(expectedDraftWorkflowCopyName));
    }
}
