package com.atlassian.jira.pageobjects.project.add;

import com.atlassian.jira.pageobjects.dialogs.FormDialog;
import com.atlassian.pageobjects.elements.query.TimedCondition;

public abstract class AddProjectWizardPage extends FormDialog
{

    /**
     * This particular FormDialog is hacked and does not get the content ready class.
     */
    public TimedCondition isOpen()
    {
        return getDialogElement().timed().isVisible();
    }

    public AddProjectWizardPage()
    {
        super("add-project-dialog");
    }
}
