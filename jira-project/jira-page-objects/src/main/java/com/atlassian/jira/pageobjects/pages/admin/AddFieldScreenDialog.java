package com.atlassian.jira.pageobjects.pages.admin;

import com.atlassian.jira.pageobjects.dialogs.FormDialog;
import com.atlassian.pageobjects.binder.Init;
import com.atlassian.pageobjects.elements.PageElement;
import org.openqa.selenium.By;

/**
 * Represents the &quot;add field screen&quot; dialog available from the
 * the {@link ViewFieldScreensPage}.
 *
 * @since v5.0.1
 */
public class AddFieldScreenDialog extends FormDialog
{
    private PageElement fieldScreenName;

    private PageElement fieldScreenDescription;

    private PageElement submit;

    public AddFieldScreenDialog()
    {
        super("add-field-screen-dialog");
    }

    @Init
    public void init()
    {
        fieldScreenName = find(By.name("fieldScreenName"));
        fieldScreenDescription = find(By.name("fieldScreenDescription"));
        submit = find(By.name("Add"));
    }

    public AddFieldScreenDialog setName(final String name)
    {
        assertDialogOpen();
        setElement(fieldScreenName, name);
        return this;
    }

    public AddFieldScreenDialog setDescription(final String description)
    {
        assertDialogOpen();
        setElement(fieldScreenDescription, description);
        return this;
    }

    /**
     * Submits the dialog expecting a success response and a redirect to the
     * ConfigureScreen page.
     *
     * @return An instance of the ConfigureScreen page.
     */
    public ConfigureScreen submitSuccess()
    {
        submit();
        assertDialogClosed();
        return binder.bind(ConfigureScreen.class);
    }

    /**
     * Submits this dialog.
     */
    public void submit()
    {
        submit(submit);
    }
}
