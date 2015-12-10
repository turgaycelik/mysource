package com.atlassian.jira.pageobjects.pages.viewissue;

import com.atlassian.jira.pageobjects.components.fields.AssigneeField;
import com.atlassian.jira.pageobjects.dialogs.FormDialog;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.binder.Init;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import org.openqa.selenium.By;

import javax.inject.Inject;

/**
 * Represents the assign issue dialog triggered with the 'a' keyboard shortcut.
 *
 * @since v5.0
 */
public class AssignIssueDialog extends FormDialog
{
    @ElementBy (id = "comment")
    private PageElement comment;

    @Inject
    private PageBinder pageBinder;
    
    private AssigneeField assigneeField;

    public AssignIssueDialog()
    {
        super("assign-dialog");
    }
    
    @Init
    public void init()
    {
        this.assigneeField = pageBinder.bind(AssigneeField.class);
    }

    public AssignIssueDialog setAssignee(String assignee)
    {
        this.assigneeField.setAssignee(assignee);
        return this;
    }

     public AssignIssueDialog typeAssignee(String assignee)
    {
        this.assigneeField.typeAssignee(assignee);
        return this;
    }

    public String getAssignee()
    {
        return this.assigneeField.getAssignee();
    }

    public AssignIssueDialog assignToMe()
    {
        assigneeField.assignToMe();
        return this;
    }

    public AssignIssueDialog addComment(String text)
    {
        comment.clear().type(text);
        return this;
    }

    public boolean isAutoComplete()
    {
        return assigneeField.isAutocomplete();
    }

    public boolean submit()
    {
        return super.submit(By.id("assign-issue-submit"));
    }
}
