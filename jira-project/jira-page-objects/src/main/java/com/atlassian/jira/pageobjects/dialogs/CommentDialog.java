package com.atlassian.jira.pageobjects.dialogs;

import org.openqa.selenium.By;

public class CommentDialog extends FormDialog
{
    public CommentDialog()
    {
        super("comment-add-dialog");
    }

    public void setComment(String comment)
    {
        getDialogElement().find(By.id("comment")).type(comment);
    }

    public boolean submit()
    {
        return super.submit(By.id("comment-add-submit"));
    }
}
