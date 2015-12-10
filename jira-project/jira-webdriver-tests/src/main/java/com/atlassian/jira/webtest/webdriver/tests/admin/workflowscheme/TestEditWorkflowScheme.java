package com.atlassian.jira.webtest.webdriver.tests.admin.workflowscheme;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;
import javax.inject.Inject;

import com.atlassian.integrationtesting.runner.restore.RestoreOnce;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.pageobjects.config.LoginAs;
import com.atlassian.jira.pageobjects.dialogs.admin.ViewWorkflowTextDialog;
import com.atlassian.jira.pageobjects.pages.JiraLoginPage;
import com.atlassian.jira.pageobjects.pages.admin.ViewProjectsPage;
import com.atlassian.jira.pageobjects.pages.admin.workflow.AddWorkflowToSchemeDialog;
import com.atlassian.jira.pageobjects.pages.admin.workflow.AssignWorkflowToSchemeDialog;
import com.atlassian.jira.pageobjects.pages.admin.workflow.EditWorkflowScheme;
import com.atlassian.jira.pageobjects.pages.admin.workflow.StartDraftWorkflowSchemeMigrationPage;
import com.atlassian.jira.pageobjects.util.UserSessionHelper;
import com.atlassian.jira.pageobjects.websudo.DecoratedJiraWebSudo;
import com.atlassian.jira.pageobjects.websudo.JiraWebSudo;
import com.atlassian.jira.pageobjects.websudo.JiraWebSudoPage;
import com.atlassian.jira.testkit.beans.WorkflowSchemeData;
import com.atlassian.jira.pageobjects.BaseJiraWebTest;
import com.atlassian.jira.webtest.webdriver.util.admin.AbstractAddWorkflowDialogHelper;
import com.atlassian.jira.webtest.webdriver.util.admin.AssignIssueTypeDialogHelper;
import com.atlassian.pageobjects.DelayedBinder;

import com.google.common.base.Function;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import org.junit.Test;

import static com.google.common.collect.Iterables.transform;
import static java.util.Arrays.asList;
import static org.apache.commons.lang.StringUtils.defaultString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @since v6.0
 */
@WebTest ({ Category.WEBDRIVER_TEST, Category.ADMINISTRATION, Category.PLUGINS, Category.WORKFLOW })
@RestoreOnce ("TestEditWorkflowScheme.xml")
public class TestEditWorkflowScheme extends BaseJiraWebTest
{
    @Inject
    private UserSessionHelper sessionHelper;

    @Test
    public void testViewScheme()
    {
        //Implicit default on the workflow scheme.
        final AssignableWorkflowScheme scheme =
                new AssignableWorkflowScheme().setName("testViewScheme")
                        .setMapping(IssueType.BUG, Workflow.JIRA)
                        .setMapping(IssueType.IMPROVEMENT, Workflow.JIRA)
                        .setMapping(IssueType.TASK, Workflow.XSS)
                        .create();

        final Helper helper = createHelper().checkViewDialogs()
                .setScheme(scheme).gotoScheme()
                .sort().assertScreen();

        //Explicit default + shared.
        scheme.defaultDefaultWorkflow(Workflow.SIMPLE).update();
        final Project project = new Project("TVS", "testViewScheme").createProject().setWorkflowScheme(scheme);
        final Project project2 = new Project("TVSP", "testViewScheme plus").createProject().setWorkflowScheme(scheme);
        helper.setProjects(project, project2).gotoScheme().sort().assertScreen();

        //Lets test the draft scheme and make sure that we render the draft.
        final DraftWorkflowScheme draftScheme = scheme.createDraftScheme();
        draftScheme.setMapping(IssueType.TASK, Workflow.SIMPLE).update();
        helper.gotoScheme().assertScreen();

        //View original.
        helper.viewOriginal().assertScreen().viewDraft().assertScreen();
    }

    @Test
    public void testEditScheme()
    {
        //Check the current state of the scheme.
        final AssignableWorkflowScheme scheme =
                new AssignableWorkflowScheme().setName("testEditScheme")
                        .setMapping(IssueType.BUG, Workflow.JIRA)
                        .setMapping(IssueType.IMPROVEMENT, Workflow.JIRA)
                        .setMapping(IssueType.TASK, Workflow.XSS)
                        .create();

        final Helper helper = createHelper()
                .setScheme(scheme).gotoScheme()
                .sort().notChanged().assertScreen();

        backdoor.websudo().enable();
        //Scheme: {JIRA -> {Default, BUG, IMPROVEMENT}, XSS -> {TASK}}.
        checkWebSudo(helper, new Function<Helper, JiraWebSudo>()
        {
            @Override
            public JiraWebSudo apply(@Nullable final Helper input)
            {
                return helper.removeIssueTypesWebsudo(Workflow.XSS, IssueType.TASK);
            }
        });
        //Scheme: {JIRA -> {Default, BUG, IMPROVEMENT}}.

        helper.removeIssueTypes(Workflow.JIRA, IssueType.IMPROVEMENT).assertScreen();
        //Scheme: {JIRA -> {Default, IMPROVEMENT}}.

        helper.removeIssueTypes(Workflow.JIRA, IssueType.BUG).assertScreen();
        //Scheme: {JIRA -> {Default}}.

        checkWebSudo(helper, new Function<Helper, JiraWebSudo>()
        {
            @Override
            public JiraWebSudo apply(@Nullable final Helper input)
            {
                return helper.assignWorkflowWebsudo(Workflow.JIRA, false, IssueType.BUG, IssueType.FEATURE);
            }
        });
        //Scheme: {JIRA -> {Default, BUG, FEATURE}}.

        helper.assignWorkflow(Workflow.JIRA, false, IssueType.IMPROVEMENT, IssueType.TASK, IssueType.XSS).assertScreen();
        //Scheme: {JIRA -> {Default, BUG, FEATURE, TASK, XSS, IMPROVEMENT}}.

        //Add workflow with default.
        helper.addWorkflow(Workflow.SIMPLE, true).assertScreen();
        //Scheme: {JIRA -> {BUG, FEATURE, TASK, XSS, IMPROVEMENT}, SIMPLE->{Default}}

        checkWebSudo(helper, new Function<Helper, JiraWebSudo>()
        {
            @Override
            public JiraWebSudo apply(@Nullable final Helper input)
            {
                return helper.addWorkflowWebsudo(Workflow.XSS, false, IssueType.BUG, IssueType.TASK);
            }
        });
        //Scheme: {JIRA -> {XSS, FEATURE, IMPROVEMENT}, SIMPLE->{Default}, XSS->{BUG, TASK}}


        checkWebSudo(helper, new Function<Helper, JiraWebSudo>()
        {
            @Override
            public JiraWebSudo apply(@Nullable final Helper input)
            {
                return helper.removeWorkflowWebsudo(Workflow.SIMPLE);
            }
        });
        //Scheme: {JIRA -> {Default, XSS, FEATURE, IMPROVEMENT}, XSS->{BUG, TASK}}

        helper.addWorkflow(Workflow.SIMPLE, false, IssueType.BUG, IssueType.FEATURE).assertScreen();
        //Scheme: {JIRA -> {Default, XSS, IMPROVEMENT}, XSS->{TASK}, SIMPLE->{BUG, FEATURE}}

        helper.removeWorkflow(Workflow.SIMPLE).assertScreen();
        //Scheme: {JIRA -> {Default, XSS, IMPROVEMENT}, XSS->{TASK}}

        helper.addWorkflow(Workflow.SIMPLE, false, IssueType.TASK, IssueType.IMPROVEMENT).assertScreen();
        //Scheme: {JIRA -> {Default, XSS}, SIMPLE -> {IMPROVEMENT, TASK}}

        helper.assignWorkflow(Workflow.JIRA, false, IssueType.FEATURE).assertScreen();
        //Scheme: {JIRA -> {Default, XSS, FEATURE}, SIMPLE -> {IMPROVEMENT, TASK}}

        helper.assignWorkflow(Workflow.JIRA, false, IssueType.TASK, IssueType.BUG).assertScreen();
        //Scheme: {JIRA -> {Default, XSS, FEATURE, BUG, TASK}, SIMPLE -> {IMPROVEMENT}}

        helper.assignWorkflow(Workflow.SIMPLE, true, IssueType.XSS, IssueType.FEATURE, IssueType.BUG, IssueType.TASK).assertScreen();
        //Scheme: {SIMPLE -> {Default, IMPROVEMENT, XSS, BUG, FEATURE, TASK}}
    }

    @Test
    @LoginAs (user = "fred")
    public void testNoAdminCantSee()
    {
        final AssignableWorkflowScheme scheme =
                new AssignableWorkflowScheme().setName("testNoAdminCantSee")
                        .setMapping(IssueType.BUG, Workflow.JIRA)
                        .setMapping(IssueType.IMPROVEMENT, Workflow.JIRA)
                        .setMapping(IssueType.TASK, Workflow.XSS)
                        .create();

        //View the page as non-admin will take us to login.
        jira.visitDelayed(EditWorkflowScheme.class, scheme.id);

        //Goto the login page and login as sys-admin. This should take us to the correct page.
        final JiraLoginPage bind = pageBinder.bind(JiraLoginPage.class);
        bind.loginAsSystemAdminAndFollowRedirect(EditWorkflowScheme.class, scheme.id);
    }

    @Test
    public void testEditDraftWorkflowScheme()
    {
        final AssignableWorkflowScheme scheme =
                new AssignableWorkflowScheme().setName("testEditDraftWorkflowScheme")
                        .setMapping(IssueType.BUG, Workflow.JIRA)
                        .setMapping(IssueType.IMPROVEMENT, Workflow.JIRA)
                        .setMapping(IssueType.TASK, Workflow.XSS)
                        .create().sort();

        final Project project = new Project("TEDWS", "testEditDraftWorkflowScheme")
                .createProject().setWorkflowScheme(scheme);

        final Helper helper = createHelper()
                .setScheme(scheme).gotoScheme()
                .setProjects(project).notChanged().assertScreen();

        //Check that adding a workflow triggers a draft.
        helper.addWorkflow(Workflow.SIMPLE, true, IssueType.IMPROVEMENT).assertScreen()
                .discardDraft().assertScreen();

        //Check that removing issue types triggers a draft.
        helper.removeIssueTypes(Workflow.JIRA, IssueType.IMPROVEMENT).assertScreen()
                .discardDraft().assertScreen();

        //Check that assigning issues types triggers a draft.
        helper.assignWorkflow(Workflow.JIRA, false, IssueType.TASK).assertScreen()
                .discardDraft().assertScreen();

        //Check that removing workflow triggers draft.
        helper.removeWorkflow(Workflow.JIRA).assertScreen();

        //Discard the draft with websudo prompt.
        backdoor.websudo().enable();
        checkWebSudo(helper, new Function<Helper, JiraWebSudo>()
        {
            @Override
            public JiraWebSudo apply(final Helper input)
            {
                return input.discardDraftWebsudo();
            }
        });
        backdoor.websudo().disable();
    }

    @Test
    public void testChangesByOtherUser()
    {
        final AssignableWorkflowScheme scheme =
                new AssignableWorkflowScheme().setName("testChangesByOtherUser")
                        .setMapping(IssueType.BUG, Workflow.JIRA)
                        .setMapping(IssueType.IMPROVEMENT, Workflow.JIRA)
                        .setMapping(IssueType.TASK, Workflow.XSS)
                        .create().sort();

        final Helper helper = createHelper()
                .setScheme(scheme).gotoScheme()
                .notChanged().assertScreen();

        //Scheme: {JIRA -> {Debug, BUG, IMPROVEMENT}, XSS -> {TASK}}

        //Firstly, lets check that compatible changes by other user are fine.
        //Other user has removed improvement.
        scheme.removeIssueTypes(IssueType.IMPROVEMENT).update();
        helper.removeIssueTypes(Workflow.JIRA, IssueType.IMPROVEMENT).assertScreen();
        //Scheme: {JIRA -> {Debug, BUG}, XSS -> {TASK}}

        //Other user has removed workflow that we also removed.
        scheme.removeWorkflow(Workflow.XSS).update();
        helper.removeWorkflow(Workflow.XSS).assertScreen();
        //Scheme: {JIRA -> {Debug, BUG}}

        //Other user added issue type that we added.
        scheme.addMappings(Workflow.JIRA, false, IssueType.IMPROVEMENT).update();
        helper.assignWorkflow(Workflow.JIRA, false, IssueType.IMPROVEMENT).assertScreen();
        //Scheme: {JIRA -> {Debug, BUG, IMPROVEMENT}}

        //Other user added issue type that we added.
        scheme.addMappings(Workflow.XSS, true, IssueType.IMPROVEMENT).update();
        helper.addWorkflow(Workflow.XSS, true, IssueType.IMPROVEMENT).assertScreen();
        //Scheme: {JIRA -> {BUG}, XSS->{Default, IMPROVEMENT}}

        scheme.removeIssueTypes(IssueType.BUG).update();
        helper.changed().removeIssueTypes(Workflow.XSS, IssueType.IMPROVEMENT).sort().assertScreen();
        //Scheme: {XSS->{Default}}

        scheme.addMappings(Workflow.JIRA, true, IssueType.BUG).update();
        helper.changed().addWorkflow(Workflow.SIMPLE, false, IssueType.BUG).sort().assertScreen();
        //Scheme: {JIRA->{Default}, SIMPLE->{BUG}}

        scheme.addMappings(Workflow.SIMPLE, false, IssueType.IMPROVEMENT).update();
        helper.changed().assignWorkflow(Workflow.JIRA, false, IssueType.TASK).sort().assertScreen();
        //Scheme: {JIRA->{Default, TASK}, SIMPLE->{BUG, IMP}}

        scheme.removeWorkflow(Workflow.JIRA).update();
        helper.changed().removeWorkflow(Workflow.SIMPLE).sort().assertScreen();
        //Scheme: {JIRA->{Default}}
    }

    @Test
    public void testAssignDialog()
    {
        final AssignableWorkflowScheme scheme =
                new AssignableWorkflowScheme().setName("testAssignDialog")
                        .setMapping(IssueType.BUG, Workflow.JIRA)
                        .setMapping(IssueType.IMPROVEMENT, Workflow.JIRA)
                        .setMapping(IssueType.TASK, Workflow.XSS)
                        .create().sort();

        final EditWorkflowScheme editScheme
                = jira.goTo(EditWorkflowScheme.class, scheme.getId());

        assertAssignIssueTypesDialog(editScheme, scheme);
    }

    @Test
    public void testAddWorkflowDialog()
    {
        final AssignableWorkflowScheme scheme =
                new AssignableWorkflowScheme().setName("testAddWorkflowDialog")
                        .setMapping(IssueType.BUG, Workflow.JIRA)
                        .setMapping(IssueType.IMPROVEMENT, Workflow.JIRA)
                        .create().sort();

        final Helper helper = createHelper()
                .setScheme(scheme).gotoScheme().notChanged();

        assertAssignWorkflow(helper, Workflow.JIRA);
        helper.addWorkflow(Workflow.SIMPLE, true).removeWorkflow(Workflow.JIRA);
        assertAssignWorkflow(helper, Workflow.SIMPLE);
        helper.addWorkflow(Workflow.XSS, false, IssueType.IMPROVEMENT);
        assertAssignWorkflow(helper, Workflow.SIMPLE, Workflow.XSS);
    }

    private void assertAssignWorkflow(final Helper helper, final Workflow... excludes)
    {
        final Set<Workflow> workflows = EnumSet.allOf(Workflow.class);
        workflows.removeAll(asList(excludes));
        final AddWorkflowToSchemeDialog dialog = helper.page.addWorkflowDialog();
        new AddWorkflowHelper().dialog(dialog).workflows(workflows).assertDialog();
        dialog.close();
    }

    @Test
    public void testBadSchemeId()
    {
        final EditWorkflowScheme bind = jira.goTo(EditWorkflowScheme.class, 1);
        assertThat(bind.getErrorMessage(), equalTo("The workflow scheme does not exist."));
    }

    @Test
    public void testNoSchemeId()
    {
        final EditWorkflowScheme bind = jira.goTo(BadEditWorkflowScheme.class);
        assertThat(bind.getErrorMessage(), equalTo("Workflow scheme not specified."));
    }

    @Test
    public void testPublishScheme()
    {
        final AssignableWorkflowScheme scheme = new AssignableWorkflowScheme().setName("testPublishScheme").create().sort();
        final Project project = new Project("TPS", "testPublishScheme").createProject().setWorkflowScheme(scheme);

        final Helper helper = createHelper()
                .setScheme(scheme).gotoScheme()
                .setProjects(project).notChanged().assertScreen();

        helper.addWorkflow(Workflow.SIMPLE, true, IssueType.IMPROVEMENT).assertScreen();

        final StartDraftWorkflowSchemeMigrationPage migrationPage = helper.publishDraft();

        assertThat(migrationPage.isDraftMigration(), equalTo(true));
        assertThat(helper.getEditScheme().update().getId(), equalTo(migrationPage.getSchemeId()));
        assertThat(migrationPage.isSubmitPresent(), equalTo(true));
    }

    @Test
    public void testAddWorkflowViaParameter()
    {
        final AssignableWorkflowScheme scheme =
                new AssignableWorkflowScheme().setName("testAddWorkflowViaParameter").create().sort();

        final Helper helper = createHelper()
                .setScheme(scheme).gotoSchemeAddWorkflowViaParameter(Workflow.SIMPLE, false, IssueType.IMPROVEMENT).changed();

        assertAssignWorkflow(helper, Workflow.JIRA, Workflow.SIMPLE);
    }

    @Test
    public void testInlineEdit()
    {
        final AssignableWorkflowScheme scheme = new AssignableWorkflowScheme().setName("testInlineEdit").create().sort();

        final Helper helper = createHelper().setScheme(scheme).gotoScheme();

        helper.setName("new name for testInlineEdit").assertScreen();
        helper.setName("").assertScreen();
        helper.setName(" ").assertScreen();

        helper.setDescription("some description").assertScreen();
        helper.setDescription("").assertScreen();
        helper.setDescription(" ").assertScreen();
    }

    @Test
    public void importBundleButtonLabelIsChooseFromMpacForJiraAdmin()
    {
        final AssignableWorkflowScheme scheme = new AssignableWorkflowScheme().setName("testImportBundle").create().sort();

        final EditWorkflowScheme page = jira.quickLogin("jiraadmin", "jiraadmin", EditWorkflowScheme.class, scheme.getId());

        assertEquals("Choose From Marketplace", page.getImportBundleButtonLabel());
        assertTrue(page.canImportBundle());
    }

    private void checkWebSudo(final Helper helper, final Function<Helper, JiraWebSudo> operation)
    {
        //Cancel will redirect to a page that does not required websudo.
        sessionHelper.clearWebSudo();
        JiraWebSudo websudo = operation.apply(helper);
        websudo.cancel(ViewProjectsPage.class);

        //We have reloaded the page. Make sure things make sense.
        helper.gotoSchemeWebsudo("admin").sort().assertScreen();

        sessionHelper.clearWebSudo();
        websudo = operation.apply(helper);
        websudo.authenticateFail("random").authenticate("admin");
        helper.assertScreen();
    }

    private Helper createHelper()
    {
        return new Helper();
    }

    private static class AddWorkflowHelper extends AbstractAddWorkflowDialogHelper<AddWorkflowToSchemeDialog, Workflow>
    {
        @Override
        protected String lastModifiedUser(final Workflow workflow)
        {
            if (workflow == Workflow.JIRA)
            {
                return null;
            }
            else
            {
                return "Administrator";
            }
        }

        @Override
        protected String description(final Workflow workflow)
        {
            return workflow.description;
        }

        @Override
        protected String displayName(final Workflow workflow)
        {
            return workflow.displayName;
        }

        @Override
        protected AddWorkflowToSchemeDialog nextAndBack(final AddWorkflowToSchemeDialog dialog)
        {
            return dialog.next().back();
        }
    }

    private static class WorkflowAssignIssueTypeDialogHelper extends AssignIssueTypeDialogHelper<Workflow>
    {
        @Override
        protected String asDisplayed(final Workflow data)
        {
            return data != null ? data.getDisplayName() : null;
        }

        @Override
        protected List<String> getAllIssueTypes()
        {
            final List<String> expectedTypes = Lists.newArrayList();
            expectedTypes.add(null);
            expectedTypes.addAll(Lists.transform(asList(IssueType.values()), IssueType.GET_NAME));

            return expectedTypes;
        }
    }

    private void assertAssignIssueTypesDialog(final EditWorkflowScheme newScheme, final WorkflowScheme<?> scheme)
    {
        final List<EditWorkflowScheme.Workflow> panels = newScheme.getWorkflows();

        assertThat(scheme.mappings.size(), equalTo(panels.size()));

        final WorkflowAssignIssueTypeDialogHelper helper = new WorkflowAssignIssueTypeDialogHelper();
        for (final WorkflowMapping entry : scheme.mappings)
        {
            for (final IssueType issueType : entry.types)
            {
                helper.addMapping(issueType.getName(), entry.workflow);
            }
            if (entry.defaultWorkflow)
            {
                helper.addMapping(null, entry.workflow);
            }
        }

        if (panels.size() > 1)
        {
            final Iterator<WorkflowMapping> iterator = scheme.mappings.iterator();
            //Check the assign dialog for each panel.
            for (final EditWorkflowScheme.Workflow actualPanel : panels)
            {
                final WorkflowMapping expectedEntry = iterator.next();
                final AssignWorkflowToSchemeDialog assignIssueTypesDialog = actualPanel.assignDialog();
                helper.dialog(assignIssueTypesDialog).withoutBack().exclude(expectedEntry.workflow).assertDialog();
                assignIssueTypesDialog.close();
            }
        }

        final AssignWorkflowToSchemeDialog next = newScheme.addWorkflowDialog().next();
        helper.exclude(null).withBack().dialog(next).assertDialog();
        next.close();
    }

    private class Helper
    {
        private AssignableWorkflowScheme scheme;
        private EditWorkflowScheme page;
        private List<Project> projects = Lists.newArrayList();
        private boolean checkViewDialogs = false;
        private boolean viewOriginal;
        private Function<Void, Boolean> changedRule;

        public Helper gotoScheme()
        {
            page = pageBinder.navigateToAndBind(EditWorkflowScheme.class, scheme.id);
            return this;
        }

        public Helper gotoSchemeWebsudo(final String password)
        {
            final DelayedBinder<EditWorkflowScheme> delayedBinder = jira.visitDelayed(EditWorkflowScheme.class, scheme.id);

            final JiraWebSudoPage page = pageBinder.bind(JiraWebSudoPage.class);
            page.authenticate(password);

            this.page = delayedBinder.bind();
            return this;
        }

        public Helper gotoSchemeAddWorkflowViaParameter(final Workflow workflow, final boolean makeDefault, final IssueType... types)
        {
            this.page = jira.goTo(EditWorkflowScheme.class, scheme.id, workflow.getName());
            page.assignIssueTypesToWorkflowViaParameter(makeDefault, transform(asList(types), IssueType.GET_NAME));
            getEditScheme().addMappings(workflow, makeDefault, sortTypes(types));
            return this;
        }

        public Helper checkViewDialogs()
        {
            checkViewDialogs = true;
            return this;
        }

        public Helper sort()
        {
            scheme.getActive().sort();
            return this;
        }

        public Helper notChanged()
        {
            changedRule = new Function<Void, Boolean>()
            {
                private final Function<Void, Boolean> after = page.changedAfterCondition();

                @Override
                public Boolean apply(@Nullable final Void input)
                {
                    return !after.apply(input);
                }
            };
            return this;
        }

        public Helper changed()
        {
            changedRule = page.changedAfterCondition();
            return this;
        }

        public Helper assertScreen()
        {
            final WorkflowScheme<?> active = viewOriginal ? scheme : scheme.getActive();

            if (changedRule != null)
            {
                assertThat(changedRule.apply(null), equalTo(true));
            }
            assertThat(page.getName(), equalTo(active.getName()));
            assertThat(page.getDescription(), equalTo(active.getDescription()));
            assertThat(page.getSharedProjects(), equalTo(asSortedList(projects, new Function<Project, String>()
            {
                @Override
                public String apply(final Project input)
                {
                    return input.name;
                }
            })));

            if (viewOriginal)
            {
                assertThat(page.isDraft(), equalTo(false));
                assertThat(page.canViewOriginal(), equalTo(false));
                assertThat(page.canPublish(), equalTo(false));
                assertThat(page.canDiscard(), equalTo(false));
                assertThat(page.canViewDraft(), equalTo(true));
            }
            else
            {
                assertThat(page.canDiscard(), equalTo(active.isDraft()));
                assertThat(page.canViewOriginal(), equalTo(active.isDraft()));
                assertThat(page.canPublish(), equalTo(active.isDraft()));
                assertThat(page.canViewDraft(), equalTo(false));
            }

            final List<WorkflowMapping> expectedMappings = toMappings(active);
            final List<EditWorkflowScheme.Workflow> actualMappings = page.getWorkflows();

            final int workflowCount = actualMappings.size();
            assertThat(workflowCount, equalTo(expectedMappings.size()));

            assertThat(page.canAddExistingWorfklow(), equalTo(!viewOriginal && workflowCount < Workflow.values().length));
            assertThat(page.canImportBundle(), equalTo(!viewOriginal));
            assertThat(page.getImportBundleButtonLabel(), equalTo("Import From Bundle"));

            final Iterator<EditWorkflowScheme.Workflow> actualMappingIter = actualMappings.iterator();
            for (final WorkflowMapping expectedMapping : expectedMappings)
            {
                final EditWorkflowScheme.Workflow workflow = actualMappingIter.next();
                expectedMapping.assertMapping(workflow, viewOriginal);

                if (checkViewDialogs)
                {
                    final ViewWorkflowTextDialog textDialog = workflow.viewAsText();
                    assertThat(textDialog.getWorkflowName(), equalTo(expectedMapping.workflow.getDisplayName()));
                    textDialog.close();

                    final EditWorkflowScheme.WorkflowDesigner diagram = workflow.viewAsDiagram();
                    assertThat(diagram.getTitle(), equalTo(expectedMapping.workflow.getName()));
                    diagram.close();
                }

                //Only able to delete workflows when there is more than one workflow.
                assertThat(workflow.canDelete(), equalTo(workflowCount > 1 && !viewOriginal));

                //Only see assign when we have more than one workflow or the single workflow does not have all
                //issue types assigned.
                assertThat(workflow.canAssign(), equalTo(!viewOriginal && (workflowCount > 1 || workflow.getIssueTypes().size() < IssueType.values().length)));
            }

            if (active.isDraft())
            {
                assertThat(page.getLastModifiedTime(), notNullValue());
                assertThat(page.getLastModifiedUser(), notNullValue());
            }
            else
            {
                assertThat(page.getLastModifiedTime(), nullValue());
                assertThat(page.getLastModifiedUser(), nullValue());
            }

            return this;
        }

        private List<WorkflowMapping> toMappings(final WorkflowScheme<?> scheme)
        {
            return scheme.mappings;
        }

        private Helper setScheme(final AssignableWorkflowScheme scheme)
        {
            this.scheme = scheme;
            return this;
        }

        public Helper setProjects(final Project... projects)
        {
            this.projects = Lists.newArrayList(asList(projects));
            return this;
        }

        public Helper removeIssueTypes(final Workflow workflow, final IssueType... types)
        {
            page.getWorkflow(workflow.getName()).removeIssueTypes(transform(asList(types), IssueType.GET_NAME));
            getEditScheme().removeIssueTypes(types);
            return this;
        }

        public JiraWebSudo removeIssueTypesWebsudo(final Workflow workflow, final IssueType type)
        {
            final JiraWebSudo jiraWebSudo = page.getWorkflow(workflow.getName()).removeIssueTypeWebsudo(type.getName());
            return new DecoratedJiraWebSudo(jiraWebSudo)
            {
                @Override
                protected void afterAuthenticate()
                {
                    getEditScheme().removeIssueTypes(type);
                }
            };
        }

        public Helper assignWorkflow(final Workflow workflow, final boolean makeDefault, final IssueType... types)
        {
            page.getWorkflow(workflow.getName()).assignIssueTypes(makeDefault, transform(asList(types), IssueType.GET_NAME));
            getEditScheme().addMappings(workflow, makeDefault, sortTypes(types));
            return this;
        }

        public JiraWebSudo assignWorkflowWebsudo(final Workflow workflow, final boolean makeDefault, final IssueType... types)
        {
            final JiraWebSudo jiraWebSudo = page.getWorkflow(workflow.getName())
                    .assignIssueTypesWebsudo(makeDefault, transform(asList(types), IssueType.GET_NAME));

            return new DecoratedJiraWebSudo(jiraWebSudo)
            {
                @Override
                protected void afterAuthenticate()
                {
                    getEditScheme().addMappings(workflow, makeDefault, types);
                }
            };
        }

        private Iterable<IssueType> sortTypes(final IssueType... types)
        {
            final List<IssueType> lists = Lists.newArrayList(types);
            Collections.sort(lists);
            return lists;
        }

        public Helper addWorkflow(final Workflow workflow, final boolean makeDefault, final IssueType... types)
        {
            page.addWorkflow(workflow.getDisplayName(), makeDefault, transform(asList(types), IssueType.GET_NAME));
            getEditScheme().addMappings(workflow, makeDefault, sortTypes(types));
            return this;
        }

        public JiraWebSudo addWorkflowWebsudo(final Workflow workflow, final boolean makeDefault, final IssueType... types)
        {
            final JiraWebSudo jiraWebSudo = page.addWorkflowWebsudo(workflow.getDisplayName(), makeDefault, transform(asList(types), IssueType.GET_NAME));
            return new DecoratedJiraWebSudo(jiraWebSudo)
            {
                @Override
                protected void afterAuthenticate()
                {
                    getEditScheme().addMappings(workflow, makeDefault, types);
                }
            };
        }

        public Helper removeWorkflow(final Workflow workflow)
        {
            page.getWorkflow(workflow.getName()).delete();
            getEditScheme().removeWorkflow(workflow);

            return this;
        }

        public JiraWebSudo removeWorkflowWebsudo(final Workflow workflow)
        {
            final JiraWebSudo jiraWebSudo = page.getWorkflow(workflow.getName()).deleteWebsudo();
            return new DecoratedJiraWebSudo(jiraWebSudo)
            {
                @Override
                protected void afterAuthenticate()
                {
                    getEditScheme().removeWorkflow(workflow);
                }
            };
        }

        private WorkflowScheme<?> getEditScheme()
        {
            if (!projects.isEmpty())
            {
                return scheme.getDraftScheme();
            }
            else
            {
                return scheme.getActive();
            }
        }

        public Helper viewOriginal()
        {
            viewOriginal = true;
            page.viewOriginal();
            return this;
        }

        public Helper viewDraft()
        {
            viewOriginal = false;
            page.viewDraft();
            return this;
        }

        public StartDraftWorkflowSchemeMigrationPage publishDraft()
        {
            return page.publishDraft();
        }

        public Helper discardDraft()
        {
            page.discardDraft();
            scheme.discardDraftLocal();
            return this;
        }

        public JiraWebSudo discardDraftWebsudo()
        {
            final JiraWebSudo webSudo = page.discardDraftWebsudo();
            return new DecoratedJiraWebSudo(webSudo)
            {
                @Override
                protected void afterAuthenticate()
                {
                    scheme.discardDraftLocal();
                }
            };
        }

        public Helper setName(String name)
        {
            page.setName(name);
            name = name.trim();
            if (!name.isEmpty())
            {
                getEditScheme().setName(name);
            }
            return this;
        }

        public Helper setDescription(final String description)
        {
            page.setDescription(description);
            getEditScheme().setDescription(description);
            return this;
        }
    }

    private static class WorkflowMapping implements Comparable<WorkflowMapping>
    {
        private final Workflow workflow;
        private Set<IssueType> types = Sets.newLinkedHashSet();
        private boolean defaultWorkflow;

        private WorkflowMapping(final Workflow workflow)
        {
            this.workflow = workflow;
        }

        private WorkflowMapping(final WorkflowMapping mapping)
        {
            this.workflow = mapping.workflow;
            this.types = Sets.newLinkedHashSet(mapping.types);
            this.defaultWorkflow = mapping.defaultWorkflow;
        }

        private WorkflowMapping addIssueType(final IssueType type)
        {
            types.add(type);
            return this;
        }

        private WorkflowMapping addIssueTypes(final Collection<IssueType> type)
        {
            types.addAll(type);
            return this;
        }

        private WorkflowMapping setDefaultWorkflow(final boolean defaultWorkflow)
        {
            this.defaultWorkflow = defaultWorkflow;
            return this;
        }

        private boolean isEmpty()
        {
            return !defaultWorkflow && types.isEmpty();
        }

        private WorkflowMapping assertMapping(final EditWorkflowScheme.Workflow pageWorkflow, final boolean readOnly)
        {
            assertThat(pageWorkflow.getName(), equalTo(workflow.getDisplayName()));
            assertThat(pageWorkflow.getDescription(), equalTo(workflow.getDescription()));
            assertThat(defaultWorkflow, equalTo(pageWorkflow.isDefault()));

            final List<String> issueTypeNames = asStringList(types, IssueType.GET_NAME);
            assertThat(pageWorkflow.getIssueTypes(), equalTo(issueTypeNames));
            for (final String issueTypeName : issueTypeNames)
            {
                assertThat(pageWorkflow.canDeleteIssueType(issueTypeName), equalTo(!readOnly));
            }

            return this;
        }

        @Override
        public int compareTo(final WorkflowMapping o)
        {
            if (defaultWorkflow != o.defaultWorkflow)
            {
                return defaultWorkflow ? -1 : 1;
            }
            else
            {
                return workflow.name.compareToIgnoreCase(o.workflow.name);
            }
        }

        public WorkflowMapping removeIssueTypes(final IssueType... type)
        {
            types.removeAll(Arrays.asList(type));
            return this;
        }

        public WorkflowMapping sort()
        {
            final ArrayList<IssueType> issueTypes = Lists.newArrayList(types);
            Collections.sort(issueTypes);
            types = Sets.newLinkedHashSet(issueTypes);
            return this;
        }

        public WorkflowMapping sync(final Iterable<IssueType> issueTypes)
        {
            types.addAll(Lists.newArrayList(issueTypes));

            return this;
        }
    }

    private static <I> List<String> asStringList(final Iterable<? extends I> iterable, final Function<? super I, String> transform)
    {
        return Lists.newArrayList(transform(iterable, transform));
    }

    private static <I> List<String> asSortedList(final Iterable<? extends I> iterable, final Function<? super I, String> transform)
    {
        final List<String> strings = Lists.newArrayList(transform(iterable, transform));
        Collections.sort(strings);
        return strings;
    }

    private class Project
    {
        private final String key;
        private final String name;

        public Project(final String key, final String name)
        {
            this.key = key;
            this.name = name;
        }

        public Project createProject()
        {
            backdoor.project().addProject(name, key, "admin");
            return this;
        }

        public Project setWorkflowScheme(final WorkflowScheme<AssignableWorkflowScheme> scheme)
        {
            backdoor.project().setWorkflowScheme(key, scheme.getId());
            return this;
        }

        public void delete()
        {
            backdoor.project().deleteProject(key);
        }
    }

    private abstract class WorkflowScheme<T extends WorkflowScheme<T>> implements Iterable<WorkflowMapping>
    {
        List<WorkflowMapping> mappings = Lists.newArrayList();

        WorkflowScheme()
        {
            mappings.add(new WorkflowMapping(Workflow.JIRA));
        }

        WorkflowScheme(final WorkflowScheme<?> scheme)
        {
            for (final WorkflowMapping mapping : scheme)
            {
                mappings.add(new WorkflowMapping(mapping));
            }
        }

        T setMapping(final IssueType type, final Workflow workflow)
        {
            boolean added = false;
            for (Iterator<WorkflowMapping> iterator = mappings.iterator(); iterator.hasNext(); )
            {
                final WorkflowMapping mapping = iterator.next();
                if (mapping.workflow == workflow)
                {
                    mapping.addIssueType(type);
                    added = true;
                }
                else
                {
                    mapping.removeIssueTypes(type);
                    if (mapping.isEmpty())
                    {
                        iterator.remove();
                    }
                }
            }
            if (!added)
            {
                mappings.add(new WorkflowMapping(workflow).addIssueType(type));
            }
            return getThis();
        }

        WorkflowSchemeData toRestBean()
        {
            final WorkflowSchemeData schemeData = new WorkflowSchemeData()
                    .setName(getName())
                    .setId(getId());

            for (final WorkflowMapping mapping : mappings)
            {
                if (mapping.defaultWorkflow)
                {
                    schemeData.setDefaultWorkflow(mapping.workflow.getName());
                }
                for (final IssueType type : mapping.types)
                {
                    schemeData.setMapping(type.getName(), mapping.workflow.getName());
                }
            }
            return schemeData;
        }

        T sync(final WorkflowSchemeData data)
        {
            defaultDefaultWorkflow(Workflow.fromName(data.getDefaultWorkflow()));

            final Multimap<Workflow, IssueType> scheme = ArrayListMultimap.create();
            for (final Map.Entry<String, String> entry : data.getMappings().entrySet())
            {
                scheme.put(Workflow.fromName(entry.getValue()), IssueType.fromName(entry.getKey()));
            }

            for (Iterator<WorkflowMapping> iterator = mappings.iterator(); iterator.hasNext(); )
            {
                final WorkflowMapping mapping = iterator.next();
                final Collection<IssueType> issueTypes = scheme.removeAll(mapping.workflow);
                if (issueTypes != null)
                {
                    mapping.sync(issueTypes);
                }
                else
                {
                    iterator.remove();
                }
            }

            for (final Map.Entry<Workflow, Collection<IssueType>> entry : scheme.asMap().entrySet())
            {
                mappings.add(new WorkflowMapping(entry.getKey()).addIssueTypes(entry.getValue()));
            }

            return getThis();
        }

        T defaultDefaultWorkflow(Workflow defaultWorkflow)
        {
            boolean set = false;
            defaultWorkflow = defaultWorkflow == null ? Workflow.JIRA : defaultWorkflow;
            for (Iterator<WorkflowMapping> iterator = mappings.iterator(); iterator.hasNext(); )
            {
                final WorkflowMapping mapping = iterator.next();
                if (mapping.workflow == defaultWorkflow)
                {
                    set = true;
                    mapping.setDefaultWorkflow(true);
                }
                else
                {
                    if (mapping.types.isEmpty())
                    {
                        iterator.remove();
                    }
                    else
                    {
                        mapping.setDefaultWorkflow(false);
                    }
                }
            }

            if (!set)
            {
                mappings.add(new WorkflowMapping(defaultWorkflow).setDefaultWorkflow(true));
            }

            return getThis();
        }

        T removeIssueTypes(final IssueType... types)
        {
            for (Iterator<WorkflowMapping> iterator = mappings.iterator(); iterator.hasNext(); )
            {
                final WorkflowMapping next = iterator.next().removeIssueTypes(types);
                if (next.isEmpty())
                {
                    iterator.remove();
                }
            }
            return getThis();
        }

        T addMappings(final Workflow workflow, final boolean makeDefault, final IssueType... types)
        {
            return addMappings(workflow, makeDefault, asList(types));
        }

        T addMappings(final Workflow workflow, final boolean makeDefault, final Iterable<IssueType> types)
        {
            for (final IssueType type : types)
            {
                setMapping(type, workflow);
            }
            if (makeDefault)
            {
                defaultDefaultWorkflow(workflow);
            }
            return getThis();
        }

        T sort()
        {
            return sortWorkflows().sortIssueTypes();
        }

        T sortWorkflows()
        {
            Collections.sort(mappings);
            return getThis();
        }

        T sortIssueTypes()
        {
            for (final WorkflowMapping mapping : mappings)
            {
                mapping.sort();
            }
            return getThis();
        }

        T removeWorkflow(final Workflow workflow)
        {
            for (Iterator<WorkflowMapping> iterator = mappings.iterator(); iterator.hasNext(); )
            {
                final WorkflowMapping mapping = iterator.next();
                if (mapping.workflow == workflow)
                {
                    iterator.remove();
                    if (mapping.defaultWorkflow)
                    {
                        if (mappings.isEmpty())
                        {
                            mappings.add(new WorkflowMapping(Workflow.JIRA).setDefaultWorkflow(true));
                        }
                        else
                        {
                            mappings.get(0).setDefaultWorkflow(true);
                        }
                    }
                    break;
                }
            }
            return getThis();
        }

        @Override
        public Iterator<WorkflowMapping> iterator()
        {
            return mappings.iterator();
        }

        abstract T getThis();

        abstract boolean isDraft();

        abstract String getName();

        abstract String getDescription();

        abstract T setName(String name);

        abstract T setDescription(String description);

        abstract long getId();

        abstract T update();
    }

    private class AssignableWorkflowScheme extends WorkflowScheme<AssignableWorkflowScheme>
    {
        private DraftWorkflowScheme draftScheme;
        private long id;
        private String name;
        private String description;

        AssignableWorkflowScheme create()
        {
            return sync(backdoor.workflowSchemes().createScheme(toRestBean()));
        }

        @Override
        AssignableWorkflowScheme update()
        {
            return sync(backdoor.workflowSchemes().updateScheme(toRestBean()));
        }

        DraftWorkflowScheme createDraftScheme()
        {
            final WorkflowSchemeData draftScheme = backdoor.workflowSchemes().createDraftScheme(id);
            return this.draftScheme = new DraftWorkflowScheme(this, draftScheme);
        }

        DraftWorkflowScheme getDraftScheme()
        {
            if (draftScheme == null)
            {
                this.draftScheme = new DraftWorkflowScheme(this, null);
            }
            return draftScheme;
        }

        @Override
        AssignableWorkflowScheme getThis()
        {
            return this;
        }

        @Override
        boolean isDraft()
        {
            return false;
        }

        @Override
        String getName()
        {
            return name;
        }

        @Override
        String getDescription()
        {
            return defaultString(description);
        }

        AssignableWorkflowScheme setName(final String name)
        {
            this.name = name;
            return this;
        }

        AssignableWorkflowScheme setDescription(final String description)
        {
            this.description = description;
            return this;
        }

        @Override
        long getId()
        {
            return id;
        }

        WorkflowScheme<?> getActive()
        {
            return draftScheme == null ? this : draftScheme;
        }

        private AssignableWorkflowScheme discardDraftLocal()
        {
            draftScheme = null;
            return this;
        }

        @Override
        AssignableWorkflowScheme sync(final WorkflowSchemeData data)
        {
            id = data.getId();
            name = data.getName();
            description = data.getDescription();

            return super.sync(data);
        }
    }

    private class DraftWorkflowScheme extends WorkflowScheme<DraftWorkflowScheme>
    {
        private final AssignableWorkflowScheme parentScheme;
        private String lastModifiedUser;
        private String lastModifiedTime;
        private Long id;

        private DraftWorkflowScheme(final AssignableWorkflowScheme parentScheme, final WorkflowSchemeData data)
        {
            super(parentScheme);

            this.parentScheme = parentScheme;
            this.id = parentScheme.getId();
            if (data != null)
            {
                sync(data);
            }
        }

        @Override
        DraftWorkflowScheme update()
        {
            return sync(backdoor.workflowSchemes().updateDraftScheme(parentScheme.id, toRestBean()));
        }

        @Override
        DraftWorkflowScheme getThis()
        {
            return this;
        }

        @Override
        boolean isDraft()
        {
            return true;
        }

        @Override
        String getName()
        {
            return parentScheme.getName();
        }

        @Override
        String getDescription()
        {
            return parentScheme.getDescription();
        }

        @Override
        DraftWorkflowScheme setName(final String name)
        {
            parentScheme.setName(name);
            return this;
        }

        @Override
        DraftWorkflowScheme setDescription(final String description)
        {
            parentScheme.setDescription(description);
            return this;
        }

        @Override
        long getId()
        {
            return id;
        }

        @Override
        DraftWorkflowScheme sync(final WorkflowSchemeData data)
        {
            super.sync(data);
            id = data.getId();
            lastModifiedTime = data.getLastModified();
            lastModifiedUser = data.getLastModifiedUser();

            return this;
        }
    }

    private enum Workflow
    {
        JIRA("jira", "The default JIRA workflow.", "JIRA Workflow (jira)"),
        XSS("\">'><script>alert('xss');</script>", "\">'><script>alert('xss');</script>"),
        SIMPLE("Simple Workflow", "This is a really simple workflow");

        private final String name;
        private final String description;
        private final String displayName;

        private Workflow(final String name, final String description)
        {
            this(name, description, name);
        }

        private Workflow(final String name, final String description, final String displayName)
        {
            this.name = name;
            this.displayName = displayName;
            this.description = description;
        }

        public String getDescription()
        {
            return description;
        }

        public String getName()
        {
            return name;
        }

        public String getDisplayName()
        {
            return displayName;
        }

        public static Workflow fromName(final String name)
        {
            if (name == null)
            {
                return null;
            }
            for (final Workflow workflow : Workflow.values())
            {
                if (workflow.getName().equals(name))
                {
                    return workflow;
                }
            }
            throw new IllegalArgumentException("Can't find workflow by name '" + name + "'.");
        }
    }

    private enum IssueType
    {
        XSS("\">'><script>alert('xss');</script>", ">'><script>alert('xss');</script>"),
        BUG("Bug", "A problem which impairs or prevents the functions of the product."),
        IMPROVEMENT("Improvement", "An improvement or enhancement to an existing feature or task."),
        FEATURE("New Feature", "A new feature of the product, which has yet to be developed."),
        TASK("Task", "A task that needs to be done.");

        public static final Function<IssueType, String> GET_NAME = new Function<IssueType, String>()
        {
            @Override
            public String apply(@Nullable final IssueType input)
            {
                if (input == null)
                {
                    return null;
                }
                else
                {
                    return input.getName();
                }
            }
        };

        private final String name;
        private final String description;

        private IssueType(final String name, final String description)
        {
            this.name = name;
            this.description = description;
        }

        public String getDescription()
        {
            return description;
        }

        public String getName()
        {
            return name;
        }

        public static IssueType fromName(final String name)
        {
            if (name == null)
            {
                return null;
            }
            for (final IssueType issueType : IssueType.values())
            {
                if (name.equals(issueType.getName()))
                {
                    return issueType;
                }
            }
            throw new IllegalArgumentException("Can't find issue type by name '" + name + "'.");
        }
    }

    public static class BadEditWorkflowScheme extends EditWorkflowScheme
    {
        public BadEditWorkflowScheme()
        {
            super(-1);
        }

        @Override
        public String getUrl()
        {
            return "/secure/admin/EditWorkflowScheme.jspa";
        }
    }
}
