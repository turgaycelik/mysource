package com.atlassian.jira.pageobjects.pages.viewissue;

import com.atlassian.jira.pageobjects.dialogs.FormDialog;
import org.openqa.selenium.By;

public class ResolveIssueDialog extends FormDialog
{
    private final static String DIALOG_ELEMENT_ID = "workflow-transition-5-dialog";

    public ResolveIssueDialog()
    {
        super(DIALOG_ELEMENT_ID);
    }

    public boolean submit()
    {
        return super.submit(By.id("issue-workflow-transition-submit"));
    }
}
