package com.atlassian.jira.pageobjects.components.fields;

import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.binder.Init;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import org.openqa.selenium.By;

import javax.inject.Inject;

/**
 * Represents the assignee field.
 *
 * @since v5.0
 */
public class AssigneeField
{
    @ElementBy (id = "assignee-container")
    private PageElement assigneeParent;

    private PageElement assignToMeTrigger;

    private SingleSelect assigneeSelect;

    @Inject
    PageBinder pageBinder;

    @Init
    public void init()
    {
        assigneeSelect = pageBinder.bind(SingleSelect.class, assigneeParent);
        assignToMeTrigger = assigneeParent.find(By.id("assign-to-me-trigger"));
    }

    public void setAssignee(String assignee)
    {
        assigneeSelect.select(assignee);
    }
    

    public void typeAssignee(String assignee)
    {
        assigneeSelect.type(assignee);
    }

    public String getAssignee()
    {
        return assigneeSelect.getValue();
    }

    public void assignToMe()
    {
        assignToMeTrigger.click();
    }

    public boolean hasAssignToMe()
    {
        return assignToMeTrigger.isPresent();
    }

    public boolean isAutocomplete()
    {
        return !assigneeSelect.isAutocompleteDisabled();
    }
}
