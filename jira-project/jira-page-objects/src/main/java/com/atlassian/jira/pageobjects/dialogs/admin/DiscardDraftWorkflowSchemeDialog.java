package com.atlassian.jira.pageobjects.dialogs.admin;

import com.atlassian.jira.pageobjects.dialogs.FormDialog;
import org.openqa.selenium.By;

/**
 * @since v5.2
 */
public class DiscardDraftWorkflowSchemeDialog extends FormDialog
{
    public DiscardDraftWorkflowSchemeDialog()
    {
        super("discard-draft-dialog");
    }

    public void submit()
    {
        submit(form.find(By.cssSelector("input[type=submit]")));
    }
}
