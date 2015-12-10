package com.atlassian.jira.pageobjects.pages.viewissue;

import com.atlassian.jira.pageobjects.dialogs.FormDialog;
import com.atlassian.pageobjects.PageBinder;
import org.openqa.selenium.By;

import javax.inject.Inject;

/**
 * Represents the close issue confirmation dialog.
 *
 * @since v5.0
 */
public class CloseIssueDialog extends FormDialog
{
    private final static String DIALOG_ELEMENT_ID = "workflow-transition-2-dialog";

    public CloseIssueDialog()
    {
        super(DIALOG_ELEMENT_ID);
    }

    public boolean submit()
    {
        return super.submit(By.id("issue-workflow-transition-submit"));
    }
}
