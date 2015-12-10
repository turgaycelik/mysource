package com.atlassian.jira.webtest.webdriver.tests.admin.workflow;

import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.atlassian.integrationtesting.runner.restore.Restore;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.pageobjects.BaseJiraWebTest;
import com.atlassian.jira.pageobjects.pages.admin.workflow.EditWorkflowNameAndDescriptionDialog;
import com.atlassian.jira.pageobjects.pages.admin.workflow.PublishDialog;
import com.atlassian.jira.pageobjects.pages.admin.workflow.ViewWorkflowSteps;
import com.atlassian.jira.pageobjects.pages.admin.workflow.WorkflowDesignerPage;
import com.atlassian.jira.pageobjects.pages.admin.workflow.WorkflowHeader;
import com.atlassian.jira.pageobjects.pages.admin.workflow.WorkflowHeaderDelegate;
import com.atlassian.jira.pageobjects.pages.admin.workflow.WorkflowsPage;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

import org.junit.Ignore;
import org.junit.Test;

import static com.google.common.collect.Iterables.any;
import static com.google.common.collect.Iterables.contains;
import static com.google.common.collect.Iterables.transform;
import static org.hamcrest.Matchers.hasEntry;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * @since v5.1
 */
@Restore("WorkflowHeader.xml")
@WebTest ( { Category.WEBDRIVER_TEST, Category.ADMINISTRATION, Category.PLUGINS, Category.PROJECTS, Category.WORKFLOW })
public class TestWorkflowHeader extends BaseJiraWebTest
{
    private static final String SYSTEM_WORKFLOW = "jira";
    private static final String ACTIVE_WORKFLOW = "One Workflow";
    private static final String ACTIVE_WORKFLOW_WITH_DRAFT= "Two Workflow";
    private static final String INACTIVE_WORKFLOW = "\"'<strong>Anon</strong>";
    private static final String ACTIVE_WORKFLOW_CHANGED_BY_ADMIN2 = "<b>Four</b> Workflow";
    private static final String ACTIVE_WORKFLOW_CHANGED_BY_ANON = "Anon2 Workflow";

    @Restore ("noprojects.xml")
    @Test
    public void testInactiveSystemWorkflow()
    {
        final WorkflowHeader header = jira.goTo(ViewWorkflowSteps.class, SYSTEM_WORKFLOW, false);
        header.setCurrentViewMode(WorkflowHeader.WorkflowMode.TEXT);
        assertEquals(SYSTEM_WORKFLOW, header.getWorkflowName());
        assertEquals("The default JIRA workflow.", header.getWorkflowDescription());

        assertTrue(header.isSystem());
        assertFalse(header.canCreateOrEditDraft());
        assertFalse(header.canViewDraft());
        assertFalse(header.canEditNameOrDescription());
        assertInactive(header);
        assertNotDraft(header);

        assertTrue(header.getInfoMessages().contains("System workflows are not editable."));
        assertTrue(header.getWarningMessages().isEmpty());
    }

    @Test
    public void testActiveSystemWorkflow()
    {
        final WorkflowHeader header = jira.goTo(ViewWorkflowSteps.class, SYSTEM_WORKFLOW, false);
        header.setCurrentViewMode(WorkflowHeader.WorkflowMode.TEXT);
        assertEquals(SYSTEM_WORKFLOW, header.getWorkflowName());
        assertEquals("The default JIRA workflow.", header.getWorkflowDescription());

        assertTrue(header.isSystem());
        assertFalse(header.canCreateOrEditDraft());
        assertFalse(header.canViewDraft());
        assertFalse(header.canEditNameOrDescription());

        assertActive(header);
        assertNotDraft(header);

        assertTrue(header.getInfoMessages().contains("System workflows are not editable."));
        assertTrue(header.getWarningMessages().isEmpty());
    }

    @Test
    public void testEditActiveWorkflowWithPublish()
    {
        WorkflowHeader header = jira.goTo(ViewWorkflowSteps.class, ACTIVE_WORKFLOW, false);

        //Lets check the state of the header on an active workflow.
        assertEquals(ACTIVE_WORKFLOW, header.getWorkflowName());
        assertNull(header.getWorkflowDescription());
        assertFalse(header.isSystem());
        assertActive(header);
        assertNotDraft(header);
        assertFalse(header.canEditNameOrDescription());
        assertTrue(header.canCreateOrEditDraft());
        assertFalse(header.canViewDraft());
        assertEquals(ImmutableList.of("One"), header.getSharedProjects());

        //Lets make sure that we have created a draft.
        header = header.createDraftInMode(WorkflowHeaderDelegate.WorkflowMode.DIAGRAM);
        assertEquals(ACTIVE_WORKFLOW, header.getWorkflowName());
        assertNull(header.getWorkflowDescription());

        assertFalse(header.isSystem());
        assertDraft(header);
        assertTrue(header.canEditNameOrDescription());
        assertEquals(ImmutableList.of("One"), header.getSharedProjects());

        //Let try to edit the description on the draft.
        final String newDescription = "<b>A new description for this workflow.</b>";
        final EditWorkflowNameAndDescriptionDialog descriptionDialog = header.editNameOrDescription();
        assertFalse(descriptionDialog.canEditName());
        descriptionDialog.setDescription(newDescription);
        header = descriptionDialog.submit(WorkflowDesignerPage.class, ACTIVE_WORKFLOW, false);

        assertEquals(ACTIVE_WORKFLOW, header.getWorkflowName());
        assertEquals(newDescription, header.getWorkflowDescription());

        assertFalse(header.isSystem());
        assertDraft(header);
        assertTrue(header.canEditNameOrDescription());

        //Lets go back to the draft to make sure the edit link does not overwrite it.
        header = jira.goTo(ViewWorkflowSteps.class, ACTIVE_WORKFLOW, false);
        assertNull(header.getWorkflowDescription());
        assertNotDraft(header);

        //Let go back to our daft and make sure the changes were saved.
        header = header.createDraftInMode(WorkflowHeaderDelegate.WorkflowMode.TEXT);
        assertEquals(newDescription, header.getWorkflowDescription());
        assertDraft(header);

        //Lets publish to draft to make sure that it overwrites the description.
        header = header.openPublishDialog().disableBackup().submitAndGotoViewWorkflow();

        assertEquals(ACTIVE_WORKFLOW, header.getWorkflowName());
        assertEquals(newDescription, header.getWorkflowDescription());

        assertFalse(header.isSystem());
        assertNotDraft(header);
        assertActive(header);
        assertFalse(header.canEditNameOrDescription());
    }

    @Test
    public void testEditActiveWorkflowWithDiscard()
    {
        WorkflowHeader workflowHeader = jira.goTo(ViewWorkflowSteps.class,
                ACTIVE_WORKFLOW_WITH_DRAFT, false);

        workflowHeader.setCurrentViewMode(WorkflowHeader.WorkflowMode.TEXT);

        //Lets check the state of the header on an active workflow.
        assertEquals(ACTIVE_WORKFLOW_WITH_DRAFT, workflowHeader.getWorkflowName());
        assertNull(workflowHeader.getWorkflowDescription());
        assertFalse(workflowHeader.isSystem());
        assertActive(workflowHeader);
        assertNotDraft(workflowHeader);
        assertFalse(workflowHeader.canEditNameOrDescription());
        assertTrue(workflowHeader.canViewDraft());
        assertTrue(workflowHeader.canCreateOrEditDraft());
        assertEquals(ImmutableList.of("Two"), workflowHeader.getSharedProjects());

        //Lets make sure that we have a draft.
        workflowHeader = workflowHeader.createDraftInMode(WorkflowHeaderDelegate.WorkflowMode.TEXT);
        assertEquals(ACTIVE_WORKFLOW_WITH_DRAFT, workflowHeader.getWorkflowName());
        assertNull(workflowHeader.getWorkflowDescription());

        assertFalse(workflowHeader.isSystem());
        assertDraft(workflowHeader);
        assertTrue(workflowHeader.canEditNameOrDescription());
        assertEquals(ImmutableList.of("Two"), workflowHeader.getSharedProjects());

        //Let try to edit the description on the draft.
        final String newDescription = "<b>testEditActiveWorkflowWithDiscard</b>";
        final EditWorkflowNameAndDescriptionDialog descriptionDialog = workflowHeader.editNameOrDescription();
        assertFalse(descriptionDialog.canEditName());
        descriptionDialog.setDescription(newDescription);
        workflowHeader = descriptionDialog.submit(ViewWorkflowSteps.class, ACTIVE_WORKFLOW_WITH_DRAFT, false);

        assertEquals(ACTIVE_WORKFLOW_WITH_DRAFT, workflowHeader.getWorkflowName());
        assertEquals(newDescription, workflowHeader.getWorkflowDescription());

        assertFalse(workflowHeader.isSystem());
        assertDraft(workflowHeader);
        assertTrue(workflowHeader.canEditNameOrDescription());

        //Lets go back to the draft to make sure the edit link does not overwrite it.
        workflowHeader = jira.goTo(WorkflowDesignerPage.class, ACTIVE_WORKFLOW_WITH_DRAFT, false);
        assertNull(workflowHeader.getWorkflowDescription());
        assertNotDraft(workflowHeader);

        workflowHeader = workflowHeader.createDraftInMode(WorkflowHeaderDelegate.WorkflowMode.DIAGRAM);
        assertEquals(newDescription, workflowHeader.getWorkflowDescription());
        assertDraft(workflowHeader);

        //Lets discard to draft to make sure that it does not overwrite the description.
        workflowHeader = workflowHeader.openDiscardDialog().submitAndGotoViewWorkflow();
        assertEquals(ACTIVE_WORKFLOW_WITH_DRAFT, workflowHeader.getWorkflowName());
        assertNull(workflowHeader.getWorkflowDescription());

        assertFalse(workflowHeader.isSystem());
        assertNotDraft(workflowHeader);
        assertActive(workflowHeader);
        assertFalse(workflowHeader.canEditNameOrDescription());

        //Lets create the draft again to make sure we have discarded.
        workflowHeader = jira.goTo(ViewWorkflowSteps.class, ACTIVE_WORKFLOW_WITH_DRAFT, false);

        workflowHeader = workflowHeader.createDraftInMode(WorkflowHeaderDelegate.WorkflowMode.TEXT);

        assertFalse(workflowHeader.isSystem());
        assertDraft(workflowHeader);
        assertTrue(workflowHeader.canEditNameOrDescription());

        assertEquals(ACTIVE_WORKFLOW_WITH_DRAFT, workflowHeader.getWorkflowName());
        assertNull(workflowHeader.getWorkflowDescription());
    }

    @Test
    public void testEditInactiveWorkflow()
    {
        WorkflowHeader header = jira.goTo(ViewWorkflowSteps.class, INACTIVE_WORKFLOW, false);

        //Lets check the state of the header on an active workflow.
        assertEquals(INACTIVE_WORKFLOW, header.getWorkflowName());
        assertNull(header.getWorkflowDescription());
        assertFalse(header.isSystem());
        assertInactive(header);
        assertNotDraft(header);
        assertTrue(header.canEditNameOrDescription());
        assertFalse(header.canCreateOrEditDraft());
        assertFalse(header.canViewDraft());
        assertTrue(header.getSharedProjects().isEmpty());

        //Lets edit the description to make sure it works.
        final String newDescription = "<b>testEditInactiveWorkflow</b>";
        EditWorkflowNameAndDescriptionDialog descriptionDialog = header.editNameOrDescription();
        assertTrue(descriptionDialog.canEditName());
        descriptionDialog.setDescription(newDescription);
        header = descriptionDialog.submit(ViewWorkflowSteps.class, INACTIVE_WORKFLOW, false);

        assertEquals(INACTIVE_WORKFLOW, header.getWorkflowName());
        assertEquals(newDescription, header.getWorkflowDescription());

        //Lets change the name to a duplicate to make sure we get an error.
        descriptionDialog = header.editNameOrDescription();
        descriptionDialog.setName(ACTIVE_WORKFLOW_WITH_DRAFT);
        descriptionDialog = descriptionDialog.submitFail();

        assertThat(descriptionDialog.getFormErrors(),
                hasEntry(EditWorkflowNameAndDescriptionDialog.FIELD_NAME, "A workflow with this name already exists."));

        final String newWorkflowName = "<b>New Workflow</b>";
        descriptionDialog.setName(newWorkflowName);
        header = descriptionDialog.submit(ViewWorkflowSteps.class, newWorkflowName, false);

        assertEquals(newWorkflowName, header.getWorkflowName());
        assertEquals(newDescription, header.getWorkflowDescription());
    }

    @Test
    public void testWorkflowHeaderMessages()
    {
        //Workflow that we last edited.
        WorkflowHeader workflowHeader = jira.goTo(ViewWorkflowSteps.class, ACTIVE_WORKFLOW, false);

        assertTrue(workflowHeader.getWarningMessages().isEmpty());
        List<String> infoMessages = workflowHeader.getInfoMessages();
        assertEquals(1, infoMessages.size());
        Matcher matcher = Pattern.compile("This workflow was last edited by you at (.+)")
                .matcher(infoMessages.get(0));
        assertTrue(matcher.matches());

        //Workflow draft that we edited.
        workflowHeader = jira.goTo(ViewWorkflowSteps.class,
                ACTIVE_WORKFLOW_WITH_DRAFT, true);
        assertTrue(workflowHeader.getWarningMessages().isEmpty());
        infoMessages = workflowHeader.getInfoMessages();
        assertEquals(1, infoMessages.size());
        matcher = Pattern.compile("This draft was last edited by you at (.+?)\\.")
                .matcher(infoMessages.get(0));
        assertTrue(matcher.matches());

        //Go back to the live workflow using the link.
        assertTrue(workflowHeader.hasLinkToLiveWorkflow());
        workflowHeader = workflowHeader.gotoLiveWorkflow();
        assertNotDraft(workflowHeader);
        assertEquals(ACTIVE_WORKFLOW_WITH_DRAFT, workflowHeader.getWorkflowName());

        //Go to a workflow changed by another user.
        workflowHeader = jira.goTo(ViewWorkflowSteps.class, ACTIVE_WORKFLOW_CHANGED_BY_ADMIN2, false);
        workflowHeader.setCurrentViewMode(WorkflowHeader.WorkflowMode.DIAGRAM);
        assertTrue(workflowHeader.getInfoMessages().isEmpty());
        List<String> warningMessages = workflowHeader.getWarningMessages();
        assertEquals(1, warningMessages.size());
        matcher = Pattern.compile("This workflow was last edited by <b>admin2</b> at (.+?)\\.")
                .matcher(warningMessages.get(0));
        assertTrue(matcher.matches());

        //Go to a draft changed by another user.
        workflowHeader = workflowHeader.createDraftInMode(WorkflowHeaderDelegate.WorkflowMode.DIAGRAM);
        assertTrue(workflowHeader.getInfoMessages().isEmpty());
        // In the standard diagram mode, last edited messages are not displayed.
        assertTrue(workflowHeader.getWarningMessages().isEmpty());

        //Go back to the live workflow using the link.
        assertTrue(workflowHeader.hasLinkToLiveWorkflow());
        workflowHeader = workflowHeader.gotoLiveWorkflow();
        assertNotDraft(workflowHeader);
        assertEquals(ACTIVE_WORKFLOW_CHANGED_BY_ADMIN2, workflowHeader.getWorkflowName());

        //Go to a workflow changed by anonymous user.
        workflowHeader = jira.goTo(ViewWorkflowSteps.class, ACTIVE_WORKFLOW_CHANGED_BY_ANON, false);
        assertTrue(workflowHeader.getInfoMessages().isEmpty());
        warningMessages = workflowHeader.getWarningMessages();
        assertEquals(1, warningMessages.size());
        matcher = Pattern.compile("This workflow was last edited by an anonymous user at (.+?)\\.")
                .matcher(warningMessages.get(0));
        assertTrue(matcher.matches());

        //Go to a draft changed by anonymous user.
        workflowHeader = workflowHeader.createDraftInMode(WorkflowHeaderDelegate.WorkflowMode.TEXT);
        assertTrue(workflowHeader.getInfoMessages().isEmpty());
        warningMessages = workflowHeader.getWarningMessages();
        assertEquals(1, warningMessages.size());
        matcher = Pattern.compile("This draft was last edited by an anonymous user at (.+?)\\.")
                .matcher(warningMessages.get(0));
        assertTrue(matcher.matches());

        //Go back to the live workflow using the link.
        assertTrue(workflowHeader.hasLinkToLiveWorkflow());
        workflowHeader = workflowHeader.gotoLiveWorkflow();
        assertNotDraft(workflowHeader);
        assertEquals(ACTIVE_WORKFLOW_CHANGED_BY_ANON, workflowHeader.getWorkflowName());
    }

    @Test
    public void testEditWorkflowNameAndDescription()
    {
        WorkflowHeader header = jira.goTo(ViewWorkflowSteps.class, SYSTEM_WORKFLOW, false);

        // Can't edit default system workflow.
        assertEquals("jira", header.getWorkflowName());
        assertEquals("The default JIRA workflow.", header.getWorkflowDescription());
        assertFalse(header.canEditNameOrDescription());

        // Can't edit active workflow.
        header = jira.goTo(ViewWorkflowSteps.class, ACTIVE_WORKFLOW, false);
        assertEquals(ACTIVE_WORKFLOW, header.getWorkflowName());
        assertEquals(null, header.getWorkflowDescription());
        assertFalse(header.canEditNameOrDescription());

        // Can edit inactive workflow
        header = jira.goTo(WorkflowDesignerPage.class, INACTIVE_WORKFLOW, false);
        assertEquals(INACTIVE_WORKFLOW, header.getWorkflowName());
        assertEquals(null, header.getWorkflowDescription());
        assertTrue(header.canEditNameOrDescription());

        final String newName = "Workflow with a proper name";
        final String newDescription = "This is a decent description";
        header = header.editNameOrDescription()
                .setName(newName).setDescription(newDescription)
                .submit(WorkflowDesignerPage.class, newName, false);
        assertEquals(newName, header.getWorkflowName());
        assertEquals(newDescription, header.getWorkflowDescription());

        final EditWorkflowNameAndDescriptionDialog dialog = header.editNameOrDescription()
                .setName(SYSTEM_WORKFLOW).setDescription("Some description")
                .submitFail();

        assertThat(dialog.getFormErrors(), hasEntry(EditWorkflowNameAndDescriptionDialog.FIELD_NAME,
                "A workflow with this name already exists."));
    }

    @Test
    public void testPublishWorkflow()
    {
        final WorkflowsPage workflows = jira.goTo(WorkflowsPage.class);

        WorkflowsPage.Workflow workflowWithDraft = get(workflows.active(), ACTIVE_WORKFLOW_WITH_DRAFT);
        assertTrue(workflowWithDraft.getStatus() == WorkflowsPage.WorkflowStatus.DRAFT);

        final WorkflowHeader header = jira.goTo(ViewWorkflowSteps.class, ACTIVE_WORKFLOW_WITH_DRAFT, true);
        header.openPublishDialog().disableBackup().submitAndGotoViewWorkflow();

        workflowWithDraft = get(jira.goTo(WorkflowsPage.class).active(), ACTIVE_WORKFLOW_WITH_DRAFT);
        assertFalse(workflowWithDraft.getStatus() == WorkflowsPage.WorkflowStatus.DRAFT);
    }

    @Test
    @Ignore
    public void testPublishWorkflowWithBackup()
    {
        WorkflowsPage workflows = jira.goTo(WorkflowsPage.class);

        WorkflowsPage.Workflow workflowWithDraft = get(workflows.active(), ACTIVE_WORKFLOW_WITH_DRAFT);
        assertTrue(workflowWithDraft.getStatus() == WorkflowsPage.WorkflowStatus.DRAFT);

        final String backupWorkflowName = "Backup Workflow Name";
        final WorkflowHeader header = jira.goTo(WorkflowDesignerPage.class, ACTIVE_WORKFLOW_WITH_DRAFT, true);
        header.openPublishDialog().enableBackup(backupWorkflowName).submitAndGotoViewWorkflow();

        workflows = jira.goTo(WorkflowsPage.class);
        workflowWithDraft = get(workflows.active(), ACTIVE_WORKFLOW_WITH_DRAFT);
        assertFalse(workflowWithDraft.getStatus() == WorkflowsPage.WorkflowStatus.DRAFT);

        assertTrue(any(workflows.inactive(), name(backupWorkflowName)));
    }

    /**
     * A test for JRA-15477
     */
    @Test
    public void testPublishDraftWorkflowUserMustChoose()
    {
        final String backupWorkflowName = "Backup Workflow Name";
        WorkflowsPage workflows = jira.goTo(WorkflowsPage.class);

        //The backup workflow should not be present.
        assertFalse(contains(getNames(workflows.inactive()), backupWorkflowName));

        WorkflowHeader steps = jira.goTo(ViewWorkflowSteps.class, ACTIVE_WORKFLOW, false);
        steps = steps.createDraftInMode(WorkflowHeaderDelegate.WorkflowMode.TEXT);

        //Try to publish without selecting an item.
        final PublishDialog publishDialog = steps.openPublishDialog();
        publishDialog.submitFail();
        assertThat(publishDialog.getFormErrors(), hasEntry(PublishDialog.FIELD_BACKUP,
                "Please select if you'd like to save a backup of the active workflow"));

        //Try to publish without specifying a name.
        publishDialog.enableBackup("").submitFail();
        assertThat(publishDialog.getFormErrors(), hasEntry(PublishDialog.FIELD_NEW_WORKFLOW_NAME,
                "You must specify a workflow name."));

        //We should not have created a backup.
        publishDialog.enableBackup(backupWorkflowName).submitAndGotoViewWorkflow();

        workflows = jira.goTo(WorkflowsPage.class);
        assertTrue(contains(getNames(workflows.inactive()), backupWorkflowName));
    }

    private static Iterable<String> getNames(final Iterable<? extends WorkflowsPage.Workflow> workflows)
    {
        return transform(workflows, new Function<WorkflowsPage.Workflow, String>()
        {
            @Override
            public String apply(final WorkflowsPage.Workflow input)
            {
                return input.getName();
            }
        });
    }

    private static void assertInactive(final WorkflowHeader header)
    {
        assertActiveState(header, false);
    }

    private static void assertActive(final WorkflowHeader header)
    {
        assertActiveState(header, true);
    }

    public static void assertActiveState(final WorkflowHeader header, final boolean active)
    {
        assertEquals(active, header.isActive());
        assertEquals(!active, header.isInactive());
    }

    private static void assertNotDraft(final WorkflowHeader header)
    {
        assertFalse(header.isDraft());
        assertFalse(header.canDiscard());
        assertFalse(header.canPublish());
        assertTrue(header.isActive() || header.isInactive());
    }

    private static void assertDraft(final WorkflowHeader header)
    {
        assertTrue(header.isDraft());
        assertTrue(header.canDiscard());
        assertTrue(header.canPublish());
        assertFalse(header.isActive());
        assertFalse(header.isInactive());
    }

    private static WorkflowsPage.Workflow get(final Collection<? extends WorkflowsPage.Workflow> workflows, final String name)
    {
        return Iterables.find(workflows, name(name));
    }

    private static Predicate<WorkflowsPage.Workflow> name(final String name)
    {
        return new Predicate<WorkflowsPage.Workflow>()
        {
            @Override
            public boolean apply(final WorkflowsPage.Workflow workflow)
            {
                return name.equals(workflow.getName());
            }
        };
    }
}
