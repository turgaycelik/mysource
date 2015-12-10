package com.atlassian.jira.pageobjects.pages.admin.workflow;

import com.atlassian.jira.pageobjects.pages.AbstractJiraAdminPage;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.Conditions;
import com.atlassian.pageobjects.elements.query.Poller;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import com.atlassian.pageobjects.elements.timeout.TimeoutType;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.openqa.selenium.By;

import java.util.Collections;
import java.util.List;

import static com.google.common.collect.Iterables.concat;
import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Lists.newArrayList;
import static org.junit.Assert.assertTrue;


/**
 * Admin page for viewing workflows.
 *
 * @since v4.4
 */
public class WorkflowsPage extends AbstractJiraAdminPage
{
    @ElementBy (id = "active-workflows-table")
    private PageElement activeWorkflowsTable;

    @ElementBy (id = "active-workflows-module")
    private PageElement activeModule;

    @ElementBy (id = "inactive-workflows-table")
    private PageElement inactiveWorkflowsTable;

    @ElementBy (id = "inactive-workflows-module")
    private PageElement inactiveModule;

    @ElementBy(cssSelector = "h2")
    private PageElement header;

    @ElementBy(id = "add-workflow")
    private PageElement addWorkflowLink;

    @ElementBy(id = "import-bundle")
    private PageElement importFromBundleLink;

    @ElementBy(id = "import-from-mpac")
    private PageElement importFromMarketplaceLink;

    @ElementBy(id = "import-workflow")
    private PageElement importFromXmlLink;

    @ElementBy(id = "copy_jira")
    private PageElement copyJiraLink;

    @Override
    public String getUrl()
    {
        return "/secure/admin/workflows/ListWorkflows.jspa";
    }

    @Override
    public TimedCondition isAt()
    {
        return Conditions.and(header.timed().isPresent(), addWorkflowLink.timed().isPresent());
    }

    @Override
    public String linkId()
    {
        return "workflows_section";
    }

    public boolean isImportFromBundleLinkPresent()
    {
        return importFromBundleLink.isPresent();
    }

    public boolean isImportFromMarketplaceLinkPresent()
    {
        return importFromMarketplaceLink.isPresent();
    }

    public boolean isImportFromXmlLinkPresent()
    {
        return importFromXmlLink.isPresent();
    }

    public CopyWorkflowDialog openCopyJiraDialog()
    {
        copyJiraLink.click();
        return pageBinder.bind(CopyWorkflowDialog.class, "jira");
    }

    public WorkflowDesignerPage openDesigner(final String workflowName)
    {
        return openDesigner(workflowName, false);
    }

    public WorkflowDesignerPage openDesigner(final String workflowName, final boolean isDraft)
    {
        return pageBinder.navigateToAndBind(WorkflowDesignerPage.class, workflowName, isDraft);
    }

    public AddWorkflowDialog openAddWorkflowDialog()
    {
        addWorkflowLink.click();
        return pageBinder.bind(AddWorkflowDialog.class);
    }

    public String getActiveText()
    {
        return elementFinder.find(By.id("active-workflows-val")).getText();
    }

    public String getInactiveText()
    {
        return elementFinder.find(By.id("inactive-workflows-val")).getText();
    }

    public List<Workflow> active()
    {
        openActive();
        return newArrayList(parseActiveTable());
    }

    public List<Workflow> inactive()
    {
        openInactive();
        return newArrayList(parseInactiveTable());
    }

    public List<Workflow> workflows()
    {
        return newArrayList(parseTables());
    }

    public static List<String> names(Iterable<? extends Workflow> workflows)
    {
        return Lists.newArrayList(Iterables.transform(workflows, NameFunction.INSTANCE));
    }

    public Workflow findWorkflow(final String name)
    {
        return Iterables.find(parseTables(), new Predicate<Workflow>()
        {
            @Override
            public boolean apply(Workflow input)
            {
                return input.getName().equals(name);
            }
        }, null);
    }

    public boolean isActiveOpen()
    {
        return isModuleOpen(activeModule);
    }

    public boolean openActive()
    {
        if (isActiveOpen())
        {
            return false;
        }
        else
        {
            toggleModule(activeModule);
            return true;
        }
    }

    public boolean closeActive()
    {
        if (!isActiveOpen())
        {
            return false;
        }
        else
        {
            toggleModule(activeModule);
            return true;
        }
    }

    public boolean isInactiveOpen()
    {
        return isModuleOpen(inactiveModule);
    }

    public boolean openInactive()
    {
        if (isInactiveOpen())
        {
            return false;
        }
        else
        {
            toggleModule(inactiveModule);
            return true;
        }
    }

    public boolean closeInactive()
    {
        if (!isInactiveOpen())
        {
            return false;
        }
        else
        {
            toggleModule(inactiveModule);
            return true;
        }
    }

    private boolean isModuleOpen(PageElement moduleElement)
    {
        return moduleElement.isPresent() && !moduleElement.hasClass("collapsed");
    }

    private void toggleModule(PageElement element)
    {
        element.find(By.tagName("h3")).click();
    }

    private Iterable<Workflow> parseActiveTable()
    {
        return parseWorkflowTable(activeWorkflowsTable, WorkflowStatus.ACTIVE);
    }

    private Iterable<Workflow> parseInactiveTable()
    {
        return parseWorkflowTable(inactiveWorkflowsTable, WorkflowStatus.INACTIVE);
    }

    private Iterable<Workflow> parseTables()
    {
        return concat(parseActiveTable(), parseInactiveTable());
    }

    private Iterable<Workflow> parseWorkflowTable(PageElement table, final WorkflowStatus status)
    {
        if (!table.isPresent())
        {
            return Collections.emptyList();
        }
        final List<PageElement> rows = table.findAll(By.cssSelector("tbody tr"));
        return transform(rows, new Function<PageElement, Workflow>()
        {
            @Override
            public Workflow apply(PageElement input)
            {
                List<PageElement> columns = input.findAll(By.tagName("td"));
                return new Workflow(columns, status);
            }
        });
    }

    public void deleteWorkflow(final String workflowName)
    {
        inactiveWorkflowsTable.find(By.id(String.format("del_%s", workflowName))).click();
        elementFinder.find(By.id("delete-workflow-submit")).click();
    }

    public class Workflow
    {
        private final String name;
        private final String description;
        private final WorkflowStatus status;
        private final int numberOfSteps;
        private PageElement copyElement;

        public Workflow(List<PageElement> columns, WorkflowStatus status)
        {
            if (columns == null || columns.size() < 5)
            {
                throw new IllegalArgumentException("Not valid workflow row");
            }
            final PageElement nameElement = columns.get(0);
            name = nameElement.find(By.tagName("strong")).getText();
            description = nameElement.find(By.className("secondary-text")).getText();
            boolean isDraft = nameElement.find(By.className("icon-draft")).isPresent();
            numberOfSteps = Integer.parseInt(columns.get(3).getText());
            this.status = isDraft ? WorkflowStatus.DRAFT : status;

            final PageElement operationsElements = columns.get(4);
            copyElement = operationsElements.find(By.id(String.format("copy_%s", name)));
        }

        public String getName()
        {
            return name;
        }

        public String getDescription()
        {
            return description;
        }

        public WorkflowStatus getStatus()
        {
            return status;
        }

        public int getNumberOfSteps()
        {
            return numberOfSteps;
        }

        public boolean hasCopyOperation()
        {
            return copyElement.isPresent();
        }

        public CopyWorkflowDialog openCopyDialog()
        {
            assertTrue("The workflow does not have a copy operation.", hasCopyOperation());
            copyElement.click();
            return pageBinder.bind(CopyWorkflowDialog.class, getName());
        }
    }

    private static class NameFunction implements Function<Workflow, String>
    {
        private static final NameFunction INSTANCE = new NameFunction();

        @Override
        public String apply(Workflow input)
        {
            return input.getName();
        }
    }

    public enum WorkflowStatus
    {
        INACTIVE, ACTIVE, DRAFT
    }
}
