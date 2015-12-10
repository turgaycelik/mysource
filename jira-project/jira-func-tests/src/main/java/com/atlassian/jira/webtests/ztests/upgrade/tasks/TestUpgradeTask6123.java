package com.atlassian.jira.webtests.ztests.upgrade.tasks;

import java.util.List;

import javax.annotation.Nullable;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.testkit.beans.WorkflowSchemeData;
import com.atlassian.jira.testkit.client.restclient.IssueTransitionsMeta;

import com.google.common.base.Predicate;

import org.apache.commons.lang.StringUtils;

import static com.atlassian.jira.functest.framework.suite.Category.FUNC_TEST;
import static com.atlassian.jira.functest.framework.suite.Category.RENAME_USER;
import static com.atlassian.jira.functest.framework.suite.Category.UPGRADE_TASKS;
import static com.atlassian.jira.testkit.client.restclient.Issue.Expand.transitions;
import static com.google.common.collect.Iterables.any;

/**
 * @since v6.0
 */
@WebTest ({ FUNC_TEST, RENAME_USER, UPGRADE_TASKS })
public class TestUpgradeTask6123 extends FuncTestCase
{

    @Override
    public void setUpTest()
    {
        administration.restoreDataWithBuildNumber("JRADEV-21303-start-progress-without-assigning.xml", 6096);
    }

    public void testProjectWithoutWorkflowIsMigrated()
    {
        WorkflowSchemeData defaultScheme = backdoor.project().getSchemes("NSAA").workflowScheme;
        assertEquals("classic", defaultScheme.getName());
    }

    public void testProjectSchemesMigratedToClassicWorkflow()
    {
        WorkflowSchemeData existingScheme = backdoor.workflowSchemes()
                .getWorkflowSchemeByName("Some jira, some custom, implicit default jira");
        assertEquals("classic default workflow", existingScheme.getDefaultWorkflow());
        assertEquals("classic default workflow",
                existingScheme.getMappings().get("New Feature"));
        assertEquals("custom",
                existingScheme.getMappings().get("Bug"));
    }

    public void testDraftProjectSchemesMigratedToClassicWorkflow()
    {
        WorkflowSchemeData schemeDraft = backdoor.workflowSchemes()
                .getWorkflowSchemeDraftByProjectName("I have a draft");
        assertEquals("classic default workflow", schemeDraft.getDefaultWorkflow());
        assertEquals("classic default workflow", schemeDraft.getMappings().get("Bug"));
        assertEquals("custom", schemeDraft.getMappings().get("New Feature"));
    }

    // Added to ensure TF-337 is fixed - the id of this draft scheme is not shared by any real schemes
    public void testDraftDefaultProjectSchemeGetsMigrated()
    {
        WorkflowSchemeData schemeDraft = backdoor.workflowSchemes()
                .getWorkflowSchemeDraftByProjectName("All explicit jira default");
        assertEquals("classic default workflow", schemeDraft.getDefaultWorkflow());
    }

    public void testProjectSchemesWithDefaultKeepDefault()
    {
        WorkflowSchemeData existingScheme = backdoor.workflowSchemes()
                .getWorkflowSchemeByName("Some jira, some custom, explicit default custom");
        assertEquals("custom", existingScheme.getDefaultWorkflow());
    }

    public void testNewProjectGetsNewDefaultWorkflow()
    {
        backdoor.project().addProject("I'm new", "IMNEW", "admin");
        assertEquals("Default Workflow Scheme", backdoor.project().getSchemes("IMNEW").workflowScheme.getName());
    }

    public void testOldProjectRetainsStartAndStopProgressRestrictions()
    {
        backdoor.usersAndGroups().addUser("danny", "danny", "Danny Developer", "danny@example.com");
        backdoor.usersAndGroups().addUserToGroup("danny", "jira-developers");
        backdoor.issues().createIssue("NSAA", "brand new issue");
        assertFalse("fred (not the assignee) shouldn't be able to start progress", userCanSeeTransition("fred", "Start Progress", "NSAA-1"));
        assertFalse("danny (not the assignee) shouldn't be able to start progress", userCanSeeTransition("danny", "Start Progress", "NSAA-1"));
        navigation.login("admin");
        navigation.issue().gotoIssue("NSAA-1");
        tester.clickLinkWithText("Start Progress");
        assertFalse("fred (not the assignee) shouldn't be able to stop progress", userCanSeeTransition("fred", "Stop Progress", "NSAA-1"));
        assertFalse("danny (not the assignee) shouldn't be able to stop progress", userCanSeeTransition("danny", "Stop Progress", "NSAA-1"));
    }

    public void testNewProjectRelaxesStartAndStopProgressRestrictions()
    {
        backdoor.usersAndGroups().addUser("danny", "danny", "Danny Developer", "danny@example.com");
        backdoor.usersAndGroups().addUserToGroup("danny", "jira-developers");
        backdoor.project().addProject("I'm new", "IMNEW", "admin");
        backdoor.issues().createIssue("IMNEW", "brand new issue");
        assertFalse("fred (not assignable) should not be able to start progress", userCanSeeTransition("fred", "Start Progress", "IMNEW-1"));
        assertTrue("danny (not the assignee) should be able to start progress", userCanSeeTransition("danny", "Start Progress", "IMNEW-1"));
        navigation.login("admin");
        navigation.issue().gotoIssue("IMNEW-1");
        tester.clickLinkWithText("Start Progress");
        assertFalse("fred (not assignable) should not be able to stop progress", userCanSeeTransition("fred", "Stop Progress", "IMNEW-1"));
        assertTrue("danny (assignable) should be able to stop progress", userCanSeeTransition("danny", "Stop Progress", "IMNEW-1"));
    }

    private boolean userCanSeeTransition(final String username, final String transitionTitle, String issueKey)
    {
        List<IssueTransitionsMeta.Transition> availableTransitions =
                backdoor.issues().loginAs(username).getIssue(issueKey, transitions).transitions;
        return any(availableTransitions, new Predicate<IssueTransitionsMeta.Transition>()
        {
            @Override
            public boolean apply(@Nullable IssueTransitionsMeta.Transition input)
            {
                return input != null && StringUtils.equals(transitionTitle, input.name);
            }
        });
    }
}