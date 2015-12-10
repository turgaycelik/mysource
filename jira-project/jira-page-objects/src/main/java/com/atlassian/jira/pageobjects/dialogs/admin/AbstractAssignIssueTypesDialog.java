package com.atlassian.jira.pageobjects.dialogs.admin;

import com.atlassian.jira.pageobjects.dialogs.FormDialog;
import com.atlassian.pageobjects.binder.Init;
import com.atlassian.pageobjects.elements.PageElement;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.commons.lang.StringUtils;
import org.openqa.selenium.By;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.Arrays.asList;

/**
 * @since v6.0
 */
public abstract class AbstractAssignIssueTypesDialog<T extends AbstractAssignIssueTypesDialog<T>> extends FormDialog
{
    public List<String> getSelectedIssueTypeNames()
    {
        return Lists.transform(getSelectedIssueTypes(), IssueTypeEntry.NAME_FUNCTION);
    }

    public List<IssueTypeEntry> getSelectedIssueTypes()
    {
        return Lists.newArrayList(Iterables.filter(getIssueTypes(), IssueTypeEntry.SELECTED_FUNCTION));
    }

    public Set<String> getWorkflows()
    {
        final HashSet<String> strings = Sets.newHashSet(Iterables.transform(getIssueTypes(), IssueTypeEntry.WORKFLOW_FUNCTION));
        strings.remove(null);
        return strings;
    }

    public enum CheckBoxState
    {
        CHECKED, INDETERMINATE, UNCHECKED
    }

    private List<IssueTypeEntry> issueTypes;
    private PageElement finishedButton;
    private PageElement allTypes;

    public AbstractAssignIssueTypesDialog()
    {
        super("assign-issue-types-dialog");
    }

    @Init
    private void initialize()
    {
        this.issueTypes = actuallyGetIssueTypes();
        this.allTypes = find(By.id("project-config-select-all-issue-types"));
        this.finishedButton = find(By.id("assign-issue-types-submit"));
    }

    public T clickSelectAll()
    {
        allTypes.click();
        return getThis();
    }

    public T selectAll()
    {
        switch (getSelectAllState())
        {
            case INDETERMINATE:
            case UNCHECKED:
                return clickSelectAll();
            case CHECKED:
                break;
            default:
                throw new IllegalArgumentException("What is this state you talk about?");
        }
        return getThis();
    }

    public T selectNone()
    {
        switch (getSelectAllState())
        {
            case INDETERMINATE:
                clickSelectAll();
                //Fall through. Click once to select all. Click again to select all.
            case CHECKED:
                return clickSelectAll();
            case UNCHECKED:
                break;
            default:
                throw new IllegalArgumentException("What is this state you talk about?");
        }
        return getThis();
    }

    public CheckBoxState getSelectAllState()
    {
        if (allTypes.isSelected())
        {
            return CheckBoxState.CHECKED;
        }
        else if (allTypes.hasClass("project-config-indeterminate"))
        {
            return CheckBoxState.INDETERMINATE;
        }
        else
        {
            return CheckBoxState.UNCHECKED;
        }
    }

    public boolean getFinishEnabled()
    {
        return finishedButton.isPresent() && finishedButton.isEnabled();
    }

    private List<IssueTypeEntry> actuallyGetIssueTypes()
    {
        List<PageElement> rows = findAll(By.className("project-config-workflows-assign-issue-types-row"));
        List<IssueTypeEntry> issueTypes = Lists.newArrayListWithExpectedSize(rows.size());

        for (PageElement element : rows)
        {
            issueTypes.add(new IssueTypeEntry(element));
        }
        return issueTypes;
    }

    public T setIssueTypes(String...issueTypes)
    {
        return setIssueTypes(asList(issueTypes));
    }

    public T setIssueTypes(boolean selectDefault, Iterable<String> issueTypes)
    {
        if (selectDefault)
        {
            issueTypes = Iterables.concat(Collections.<String>singleton(null), issueTypes);
        }
        return setIssueTypes(issueTypes);
    }

    public T setIssueTypes(Iterable<String> issueTypes)
    {
        Set<String> wantedTypes = Sets.newHashSet(issueTypes);
        for (IssueTypeEntry currentType : getIssueTypes())
        {
            if (wantedTypes.remove(currentType.getIssueType()))
            {
                currentType.select();
            }
            else
            {
                currentType.deselect();
            }
        }
        if (!wantedTypes.isEmpty())
        {
            throw new IllegalArgumentException("Unable to find selection for issues types '" + issueTypes + "'.");
        }

        return getThis();
    }

    public List<IssueTypeEntry> getIssueTypes()
    {
        return issueTypes;
    }

    public List<String> getIssueTypeNames()
    {
        return Lists.newArrayList(Iterables.transform(issueTypes, IssueTypeEntry.NAME_FUNCTION));
    }

    public boolean hasBackButton()
    {
        return find(By.cssSelector("button.cancel")).isPresent();
    }

    public Set<String> getWarningWorkflows()
    {
        Set<String> workflows = Sets.newHashSet();
        for (IssueTypeEntry issueType : getIssueTypes())
        {
            if (issueType.getWarningIcon().isVisible())
            {
                workflows.add(issueType.getAssignedWorkflow());
            }
        }

        return workflows;
    }

    protected void clickBack()
    {
        find(By.cssSelector("button.cancel")).click();
    }

    public String getWorkflowName()
    {
        String heading = getDialogElement().find(By.className(HEADING_AREA_CLASS)).getText();
        return heading.substring(heading.indexOf('"')+1, heading.lastIndexOf('"'));
    }

    public void submit()
    {
        submit(By.id("assign-issue-types-submit"));
    }

    @Override
    public void close()
    {
        if (isOpen().now())
        {
            find(By.cssSelector("a.cancel")).click();
        }
    }

    protected abstract T getThis();

    public static class IssueTypeEntry
    {
        public static final Function<AbstractAssignIssueTypesDialog.IssueTypeEntry, String> NAME_FUNCTION = new Function<AbstractAssignIssueTypesDialog.IssueTypeEntry, String>()
        {
            @Override
            public String apply(IssueTypeEntry input)
            {
                return input.getIssueType();
            }
        };

        public static final Function<AbstractAssignIssueTypesDialog.IssueTypeEntry, String> WORKFLOW_FUNCTION = new Function<AbstractAssignIssueTypesDialog.IssueTypeEntry, String>()
        {
            @Override
            public String apply(IssueTypeEntry input)
            {
                return input.getAssignedWorkflow();
            }
        };

        public static final Predicate<AbstractAssignIssueTypesDialog.IssueTypeEntry> SELECTED_FUNCTION = new Predicate<IssueTypeEntry>()
        {
            @Override
            public boolean apply(IssueTypeEntry input)
            {
                return input.isSelected();
            }
        };

        private PageElement checkbox;
        private final boolean allUnassigned;
        private final String issueType;
        private final String assignedWorkflow;
        private PageElement warningIcon;

        public IssueTypeEntry(PageElement element)
        {
            this.checkbox = element.find(By.cssSelector("input[type=checkbox]"));
            if (element.hasClass("admin-all-unassigned"))
            {
                allUnassigned = true;
                issueType = null;
            }
            else
            {
                allUnassigned = false;
                this.issueType = StringUtils.trimToNull(element.find(By.className("project-config-issuetype-name")).getText());
            }

            this.assignedWorkflow = StringUtils.trimToNull(element.find(By.className("project-config-workflows-currently-assigned-name-text")).getText());
            this.warningIcon = element.find(By.className("workflow-warning"));
        }

        public boolean isSelected()
        {
            return checkbox.isSelected();
        }

        public void select()
        {
            checkbox.select();
        }

        public void deselect()
        {
            if (isSelected())
            {
                checkbox.toggle();
            }
        }

        public String getIssueType()
        {
            return issueType;
        }

        public String getAssignedWorkflow()
        {
            return assignedWorkflow;
        }

        public PageElement getWarningIcon()
        {
            return warningIcon;
        }

        public boolean isUnassigned()
        {
            return allUnassigned;
        }
    }
}
