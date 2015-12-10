package com.atlassian.jira.functest.framework.admin;

import com.atlassian.jira.functest.framework.AbstractFuncTestUtil;
import com.atlassian.jira.functest.framework.Navigation;
import com.atlassian.jira.functest.framework.admin.workflows.PublishDraftPage;
import com.atlassian.jira.functest.framework.admin.workflows.ViewWorkflowPage;
import com.atlassian.jira.functest.framework.admin.workflows.WorkflowDesignerPage;
import com.atlassian.jira.functest.framework.locator.CssLocator;
import com.atlassian.jira.functest.framework.locator.Locator;
import com.atlassian.jira.webtests.util.JIRAEnvironmentData;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultiset;

import org.w3c.dom.Node;

import net.sourceforge.jwebunit.WebTester;

import static com.atlassian.jira.functest.framework.util.dom.DomKit.getCollapsedText;
import static com.atlassian.jira.util.dbc.Assertions.notNull;
import static java.lang.Integer.parseInt;
import static java.lang.String.format;

/**
 * Default implementation of {@link com.atlassian.jira.functest.framework.admin.ViewWorkflows}
 *
 * @since v4.3
 */
public class ViewWorkflowsImpl extends AbstractFuncTestUtil implements ViewWorkflows
{
    private static final String NEW_WORKFLOW_NAME_INPUT_NAME = "newWorkflowName";
    private static final String NEW_WORKFLOW_DESCRIPTION_INPUT_NAME = "description";
    private static final String SUBMIT_BUTTON_NAME = "Add";
    private static final String COPY_BUTTON_NAME = "Update";
    private static final String COPY_LINK_PREFIX = "copy_";
    private static final String STEPS_LINK_PREFIX = "steps_live_";
    private static final String CONFIRM_DELETE_BUTTON_NAME = "Delete";

    private final int logIndentLevel;
    private final Navigation navigation;
    private final WorkflowSteps steps;

    public ViewWorkflowsImpl(WebTester tester, JIRAEnvironmentData environmentData, int logIndentLevel, final Navigation navigation)
    {
        super(tester, environmentData, logIndentLevel);
        this.logIndentLevel = logIndentLevel;
        this.navigation = navigation;
        this.steps = new WorkflowStepsImpl(tester, environmentData, childLogIndentLevel());
    }

    @Override
    public ViewWorkflows goTo()
    {
        navigation.gotoWorkflows();
        return this;
    }

    @Override
    public ViewWorkflows addWorkflow(String name, String description)
    {
        notNull("name", name);
        tester.clickLink("add-workflow");
//        tester.setWorkingForm(FunctTestConstants.JIRA_FORM_NAME);
        tester.setFormElement(NEW_WORKFLOW_NAME_INPUT_NAME, name);
        if (description != null)
        {
            tester.setFormElement(NEW_WORKFLOW_DESCRIPTION_INPUT_NAME, description);
        }
        tester.submit(SUBMIT_BUTTON_NAME);
        goTo();
        return this;
    }

    @Override
    public WorkflowDesignerPage edit(final String workflowName)
    {
        tester.clickLink("edit_live_" + workflowName);
        return newDesignerPage();
    }

    @Override
    public ViewWorkflows delete(String name)
    {
        notNull("name", name);
        tester.clickLink("del_" + name);
        tester.submit(CONFIRM_DELETE_BUTTON_NAME);
        goTo();
        return this;
    }

    private WorkflowDesignerPage newDesignerPage()
    {
        return new WorkflowDesignerPage(tester, getEnvironmentData(), logIndentLevel, this);
    }

    @Override
    public WorkflowSteps createDraft(String name)
    {
        edit(name).textView().goTo();
        return steps;
    }

    @Override
    public PublishDraftPage publishDraft(final String name)
    {
        edit(name);
        tester.clickLink("publish_draft_workflow");
        return new PublishDraftPage(tester, this);
    }

    @Override
    public WorkflowDesignerPage copyWorkflow(String nameToCopy, String newWorkflowName)
    {
        tester.clickLink(COPY_LINK_PREFIX + nameToCopy);
        tester.setFormElement(NEW_WORKFLOW_NAME_INPUT_NAME, newWorkflowName);
        tester.submit(COPY_BUTTON_NAME);
        return newDesignerPage();
    }

    @Override
    public WorkflowDesignerPage copyWorkflow(String nameToCopy, String newWorkflowName, String newWorkflowDescription)
    {
        tester.clickLink(COPY_LINK_PREFIX + nameToCopy);
        tester.setFormElement(NEW_WORKFLOW_NAME_INPUT_NAME, newWorkflowName);
        tester.setFormElement("description", newWorkflowDescription);
        tester.submit(COPY_BUTTON_NAME);

        return newDesignerPage();
    }

    @Override
    public WorkflowSteps workflowSteps(String workflowName)
    {
        edit(workflowName).textView().goTo();
        return steps;
    }

    @Override
    public ViewWorkflows launchDesigner(String workflowName)
    {
        edit(workflowName);
        tester.assertTextPresent("Workflow Designer");

        return this;
    }

    @Override
    public boolean isImportWorkflowFromXmlButtonPresent()
    {
        return locators.id("import-workflow").exists();
    }

    @Override
    public WorkflowItemsList active()
    {
        final Locator activeWorkflowsTable = new CssLocator(tester, "#active-workflows-table tbody tr");
        if (activeWorkflowsTable.exists() && activeWorkflowsTable.hasNodes())
        {
            final ImmutableList.Builder<WorkflowItem> results = ImmutableList.builder();
            for (Node node : activeWorkflowsTable.getNodes())
            {
                final boolean isDraft = new CssLocator(node, "[data-cell-type=name] .icon-draft").exists();
                final String name = new CssLocator(node, "[data-cell-type=name] strong").getText();
                final String description = new CssLocator(node, "[data-cell-type=name] .secondary-text").getText();
                final String lastModified = new CssLocator(node, "[data-cell-type=last-modified]").getText();
                final Locator schemes = new CssLocator(node, "[data-cell-type=schemes] li");
                final ImmutableMultiset.Builder<String> schemeItems = ImmutableMultiset.builder();
                if (schemes.exists() && schemes.hasNodes())
                {
                    for (Node scheme : schemes.getNodes())
                    {
                        schemeItems.add(getCollapsedText(scheme));
                    }
                }
                final int steps = parseInt(new CssLocator(node, "[data-cell-type=steps]").getText());
                results.add
                        (
                                new WorkflowItem
                                        (
                                                name, description, lastModified, schemeItems.build(), steps,
                                                isDraft ? WorkflowState.DRAFT : WorkflowState.ACTIVE
                                        )
                        );
            }
            return new WorkflowItemsList(results.build());
        }
        return new WorkflowItemsList(ImmutableList.<WorkflowItem>of());
    }

    @Override
    public WorkflowItemsList inactive()
    {
        final Locator activeWorkflowsTable = new CssLocator(tester, "#inactive-workflows-table tbody tr");
        if (activeWorkflowsTable.exists() && activeWorkflowsTable.hasNodes())
        {
            final ImmutableList.Builder<WorkflowItem> results = ImmutableList.builder();
            for (Node node : activeWorkflowsTable.getNodes())
            {
                final boolean isDraft = new CssLocator(node, "[data-cell-type=name] .icon-draft").exists();
                final String name = new CssLocator(node, "[data-cell-type=name] strong").getText();
                final String description = new CssLocator(node, "[data-cell-type=name] .secondary-text").getText();
                final String lastModified = new CssLocator(node, "[data-cell-type=last-modified]").getText();
                final Locator schemes = new CssLocator(node, "[data-cell-type=schemes] li");
                final ImmutableMultiset.Builder<String> schemeItems = ImmutableMultiset.builder();
                if (schemes.exists() && schemes.hasNodes())
                {
                    for (Node scheme : schemes.getNodes())
                    {
                        schemeItems.add(getCollapsedText(scheme));
                    }
                }
                final int steps = parseInt(new CssLocator(node, "[data-cell-type=steps]").getText());
                results.add
                        (
                                new WorkflowItem
                                        (
                                                name, description, lastModified, schemeItems.build(), steps,
                                                isDraft ? WorkflowState.DRAFT : WorkflowState.INACTIVE
                                        )
                        );
            }
            return new WorkflowItemsList(results.build());
        }
        return new WorkflowItemsList(ImmutableList.<WorkflowItem>of());
    }

    @Override
    public boolean isEditable(final String workflowName)
    {
        final Locator activeWorkflowsTable = new CssLocator(tester, "#active-workflows-table tbody tr");
        for (Node node : activeWorkflowsTable.getNodes())
        {
            final String name = new CssLocator(node, "[data-cell-type=name] strong").getText();
            if (name.equals(workflowName))
            {
                final Locator editOperation = new CssLocator(node, "[data-cell-type=operations] [data-operation=edit]");
                return editOperation.exists();
            }
        }

        final Locator inactiveWorkflowsTable = new CssLocator(tester, "#inactive-workflows-table tbody tr");
        for (Node node : inactiveWorkflowsTable.getNodes())
        {
            final String name = new CssLocator(node, "[data-cell-type=name] strong").getText();
            if (name.equals(workflowName))
            {
                final Locator editOperation = new CssLocator(node, "[data-cell-type=operations] [data-operation=edit]");
                return editOperation.exists();
            }
        }
        throw new IllegalArgumentException
                (
                        format("Unable to find workflow: '%s' in the Workflows Administration page", workflowName)
                );
    }

    @Override
    public ViewWorkflowPage view(String workflowName)
    {
        tester.clickLink(STEPS_LINK_PREFIX + workflowName);
        return new ViewWorkflowPage(tester, environmentData, logIndentLevel);
    }

    @Override
    public WorkflowInitialStep workflowInitialStep(final String workflowName)
    {
        return new WorkflowInitialStepImpl(tester, getEnvironmentData(), childLogIndentLevel(), workflowName);
    }

}
