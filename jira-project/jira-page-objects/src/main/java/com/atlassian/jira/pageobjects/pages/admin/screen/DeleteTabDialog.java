package com.atlassian.jira.pageobjects.pages.admin.screen;

import com.atlassian.jira.pageobjects.dialogs.FormDialog;
import org.openqa.selenium.By;

public class DeleteTabDialog extends FormDialog
{
    public DeleteTabDialog(String id)
    {
        super("delete-tab-" + id);
    }
    public boolean submit()
    {
        return super.submit(By.cssSelector("input[type=submit]"));
    }
}
