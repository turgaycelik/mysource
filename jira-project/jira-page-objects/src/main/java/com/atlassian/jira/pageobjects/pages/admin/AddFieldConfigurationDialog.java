package com.atlassian.jira.pageobjects.pages.admin;

import com.atlassian.jira.pageobjects.dialogs.FormDialog;
import com.atlassian.pageobjects.binder.Init;
import com.atlassian.pageobjects.elements.PageElement;
import org.openqa.selenium.By;

/**
 * Represents the &quot;add field configuration&quot; dialog available from the
 * the {@link ViewFieldConfigurationsPage}.
 *
 * @since v5.0.1
 */
public class AddFieldConfigurationDialog extends FormDialog
{
    private PageElement fieldLayoutName;

    private PageElement fieldLayoutDescription;

    private PageElement submit;

    public AddFieldConfigurationDialog()
    {
        super("add-field-configuration-dialog");
    }

    @Init
    public void init()
    {
        fieldLayoutName = find(By.name("fieldLayoutName"));
        fieldLayoutDescription = find(By.name("fieldLayoutDescription"));
        submit = find(By.name("Add"));
    }

    public AddFieldConfigurationDialog setName(final String name)
    {
        assertDialogOpen();
        setElement(fieldLayoutName, name);
        return this;
    }

    public AddFieldConfigurationDialog setDescription(final String description)
    {
        assertDialogOpen();
        setElement(fieldLayoutDescription, description);
        return this;
    }

    /**
     * Submits the dialog expecting a success response and a redirect to the
     * ViewFieldConfigurationsPage.
     *
     * @return An instance of the ViewFieldConfigurationsPage.
     */
    public EditFieldConfigPage submitSuccess()
    {
        submit();
        assertDialogClosed();
        return binder.bind(EditFieldConfigPage.class);
    }

    /**
     * Submits this dialog.
     */
    public void submit()
    {
        submit(submit);
    }
}
