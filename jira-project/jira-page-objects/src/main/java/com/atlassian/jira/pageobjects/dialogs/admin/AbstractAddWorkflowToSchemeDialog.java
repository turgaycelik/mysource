package com.atlassian.jira.pageobjects.dialogs.admin;

import com.atlassian.jira.pageobjects.dialogs.FormDialog;
import com.atlassian.pageobjects.binder.Init;
import com.atlassian.pageobjects.elements.PageElement;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.openqa.selenium.By;

import java.util.List;

/**
 * @since v6.0
 */
public abstract class AbstractAddWorkflowToSchemeDialog<T extends AbstractAddWorkflowToSchemeDialog<T>> extends FormDialog
{
    private List<Workflow> workflows;

    public AbstractAddWorkflowToSchemeDialog()
    {
        super("add-workflow-dialog");
    }

    @Init
    private void initialize()
    {
        this.workflows = actuallyGetWorkflows();
    }

    private List<Workflow> actuallyGetWorkflows()
    {
        List<PageElement> rows = findAll(By.cssSelector(".project-config-workflow-add-dialog-list li"));
        List<Workflow> workflows = Lists.newArrayListWithExpectedSize(rows.size());

        for (PageElement element : rows)
        {
            workflows.add(new Workflow(element));
        }
        return workflows;
    }

    public T selectWorkflow(String workflowName)
    {
        for (Workflow workflow : getWorkflows())
        {
            if (workflowName.equals(workflow.getDisplayName()))
            {
                workflow.click();
                return getThis();
            }
        }
        throw new IllegalArgumentException("Could not find workflow for name '"+ workflowName +"'");
    }

    public List<Workflow> getWorkflows()
    {
        return workflows;
    }

    public List<String> getWorkflowNames()
    {
        return Lists.newArrayList(Iterables.transform(workflows, Workflow.NAME_FUNCTION));
    }

    public PreviewPanel getPreviewPanel()
    {
        return new PreviewPanel(getDialogElement().find(By.id("project-config-workflow-add-dialog-preview")));
    }

    protected void clickNext()
    {
        find(By.id("add-workflow-next")).click();
    }

    abstract protected T getThis();

    public static class Workflow
    {
        public static Function<Workflow, String> NAME_FUNCTION = new Function<Workflow, String>()
        {
            @Override
            public String apply(Workflow input)
            {
                return input.getDisplayName();
            }
        };

        private PageElement element;

        public Workflow(PageElement element)
        {
            this.element = element;
        }

        public String getDisplayName()
        {
            return element.getText();
        }

        public String getName()
        {
            return element.getAttribute("data-workflowname");
        }

        public void click()
        {
            element.click();
        }
    }

    public static class PreviewPanel
    {
        private final String description;
        private final String lastModifiedDate;
        private final String lastModifiedUser;
        private final String workflowName;

        public PreviewPanel(PageElement pageElement)
        {
            PageElement descElement = pageElement.find(By.id("project-config-workflow-add-dialog-preview-desc"));
            this.description = descElement.isPresent() ? descElement.getText() : null;
            this.lastModifiedDate = pageElement.find(By.id("project-config-workflow-add-dialog-preview-last-modified-date")).getText();

            PageElement lastModifiedUserElement = pageElement.find(By.id("project-config-workflow-add-dialog-preview-last-modified-user"));
            this.lastModifiedUser = lastModifiedUserElement.isPresent() ? lastModifiedUserElement.getText() : null;

            PageElement nameElement = pageElement.find(By.className("project-config-workflow-add-dialog-preview-name"));
            this.workflowName = nameElement.isPresent() ? nameElement.getText() : null;
        }

        public String getLastModifiedDate()
        {
            return lastModifiedDate;
        }

        public String getDescription()
        {
            return description;
        }

        public String getLastModifiedUser()
        {
            return lastModifiedUser;
        }

        public String getWorkflowName()
        {
            return workflowName;
        }
    }
}
