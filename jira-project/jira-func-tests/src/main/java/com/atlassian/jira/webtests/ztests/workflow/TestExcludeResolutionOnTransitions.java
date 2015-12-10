package com.atlassian.jira.webtests.ztests.workflow;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.atlassian.jira.functest.framework.FuncTestHelperFactory;
import com.atlassian.jira.functest.framework.Navigation;
import com.atlassian.jira.functest.framework.locator.IdLocator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.Groups;
import com.atlassian.jira.webtests.JIRAWebTest;

/**
 * Copyright (c) 2002-2004
 * All rights reserved.
 * <p/>
 * Used to test the ability to exclude certain resolutions
 * on workflow transitions.
 */
@WebTest ({ Category.FUNC_TEST, Category.BROWSING })
public class TestExcludeResolutionOnTransitions extends JIRAWebTest
{
    private static final String WORKFLOW_NAME = "Exclude Resolution Workflow";
    private static final String WORKFLOW_DESC = "A workflow where we will exclude resolutions.";
    private static final String OPEN_STEP_NAME = "Open";
    private static final String CLOSED_STEP_NAME = "Closed Step";

    protected Navigation navigation;

    public TestExcludeResolutionOnTransitions(String name)
    {
        super(name);
    }

    @Override
    public void setUp()
    {
        super.setUp();
        FuncTestHelperFactory factory = new FuncTestHelperFactory(tester, getEnvironmentData());
        navigation = factory.getNavigation();
    }

    public void testAddExcludeAttribute()
    {
        getAdministration().restoreData("blankWithOldDefault.xml");

        _testCreateWorkflowWithResolutionExcludes();

        _testExcludeResolutionsSingleIssue();

        _testExcludeResolutionBulkTransition();
    }

    private void _testExcludeResolutionsSingleIssue()
    {
        // CReate an issue for testing
        navigation.issue().createIssue(PROJECT_HOMOSAP, "Bug", "Test Issue");

        // We should be on the View Issue page.
        // Try closing an issue
        clickLinkWithText(TRANSIION_NAME_CLOSE);

        // Should not have any extra options just the 'available resolution'.
        List<String> extraOptions = Collections.emptyList();
        assertExcludedOptions(extraOptions);

        selectOption("resolution", "Fixed");

        // Close Issue
        submit();
    }

    private void _testCreateWorkflowWithResolutionExcludes()
    {
        assertTextNotPresent(WORKFLOW_NAME);
        addWorkFlow(WORKFLOW_NAME, WORKFLOW_DESC);
        gotoWorkFlow();

        assertTextPresent(WORKFLOW_NAME);
        assertTextNotPresent(CLOSED_STEP_NAME);
        administration.workflows().goTo().workflowSteps(WORKFLOW_NAME).add(CLOSED_STEP_NAME, "Closed");
        assertTextPresent(CLOSED_STEP_NAME);
        assertTextNotPresent(TRANSIION_NAME_CLOSE);
        addTransition(WORKFLOW_NAME, OPEN_STEP_NAME, TRANSIION_NAME_CLOSE, "Close issue", CLOSED_STEP_NAME, RESOLVE_FIELD_SCREEN_NAME);
        assertTextPresent(TRANSIION_NAME_CLOSE);
        assertTextNotPresent(TRANSIION_NAME_REOPEN);
        addTransition(WORKFLOW_NAME, CLOSED_STEP_NAME, TRANSIION_NAME_REOPEN, "Reopen issue", OPEN_STEP_NAME, ASSIGN_FIELD_SCREEN);
        assertTextPresent(TRANSIION_NAME_REOPEN);

        // Hide some resolutions on the Close transition
        backdoor.workflow().setTransitionProperty(WORKFLOW_NAME, false, 11, "jira.field.resolution.exclude", "2,4");

        // Create project to work with
        addProject(PROJECT_HOMOSAP, PROJECT_HOMOSAP_KEY, ADMIN_USERNAME);

        enableWorkflow();
    }

    private void enableWorkflow()
    {
        // Associate the project with the new workflow
        addWorkFlowScheme(WORKFLOW_SCHEME, "Test workflow scheme.");
        assignWorkflowScheme(10000L, "Bug", WORKFLOW_NAME);
        associateWorkFlowSchemeToProject(PROJECT_HOMOSAP, WORKFLOW_SCHEME);
        waitForSuccessfulWorkflowSchemeMigration(PROJECT_HOMOSAP, WORKFLOW_SCHEME);
    }

    private void assertExcludedOptions(Collection<String> extraOptions)
    {
        Collection<String> options = new ArrayList<String>(extraOptions);
        options.add("1");
        options.add("3");
        options.add("5");

        assertOptionValuesEqual("resolution", options.toArray(new String[options.size()]));

        // Just n case
        assertOptionValueNotPresent("resolution", "Won't Fix");
        assertOptionValueNotPresent("resolution", "Incomplete");
    }


    private void _testExcludeResolutionBulkTransition()
    {
        // Try Bulk Close
        // Create a few issues
        Collection<String> issueKeys = new ArrayList<String>();
        for (int i = 0; i < 5; i++)
        {
            issueKeys.add(navigation.issue().createIssue(PROJECT_HOMOSAP, "Bug", "Test Issue " + i));
        }

        grantGlobalPermission(BULK_CHANGE, Groups.USERS);

        displayAllIssues();
        bulkChangeIncludeAllPages();
        bulkChangeChooseIssuesAll();
        chooseOperationExecuteWorfklowTransition();
        navigation.workflows().assertStepOperationDetails();

        getDialog().setWorkingForm("bulk-transition-details");

        String[] options = getDialog().getForm().getOptions(BULK_TRANSITION_ELEMENT_NAME);
        String[] optionValues = getDialog().getForm().getOptionValues(BULK_TRANSITION_ELEMENT_NAME);

        if (options == null || options.length == 0)
        {
            fail("No options for '" + BULK_TRANSITION_ELEMENT_NAME + "' element.");
        }

        String closeTransitionOptionValue = null;
        for (int i = 0; i < options.length; i++)
        {
            String option = options[i];
            if (option.indexOf(TRANSIION_NAME_CLOSE) > -1)
            {
                closeTransitionOptionValue = optionValues[i];
                break;
            }
        }

        if (closeTransitionOptionValue == null)
        {
            fail("Could not find option for Close Issue transition");
        }

        navigation.workflows().chooseWorkflowAction(closeTransitionOptionValue);

        // On bulk workflow there is a 'please select' option for resolutions.
        Collection<String> extraOptions = new ArrayList<String>();
        extraOptions.add("-1");
        assertExcludedOptions(extraOptions);
        selectOption("resolution", "Fixed");
        navigation.clickOnNext();

        assertTableRowsEqual("updatedfields", 1, new String[][] { { "Resolution", "Fixed" } });
        navigation.clickOnNext();
        waitAndReloadBulkOperationProgressPage();

        // Check that the resolution has been set to fixed for all issues we have bulk transitioned
        for (final String issueKey : issueKeys)
        {
            gotoIssue(issueKey);
            text.assertTextPresent(new IdLocator(tester, "resolution-val"), "Fixed");
        }
    }

}
