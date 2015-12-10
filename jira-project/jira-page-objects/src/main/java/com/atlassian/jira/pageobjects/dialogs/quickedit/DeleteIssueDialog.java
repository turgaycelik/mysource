package com.atlassian.jira.pageobjects.dialogs.quickedit;

import com.atlassian.jira.pageobjects.dialogs.FormDialog;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.google.inject.Inject;

/**
 * Author: Geoffrey Wong
 * Form dialog which displays when deleting a JIRA issue
 */
public class DeleteIssueDialog extends FormDialog
{
    @ElementBy (id = "delete-issue-submit")
    PageElement submitDelete;

    @Inject
    PageBinder pageBinder;

    public DeleteIssueDialog()
    {
        super("delete-issue-dialog");
    }

    public boolean deleteIssue()
    {
        return submit(submitDelete);
    }
}
