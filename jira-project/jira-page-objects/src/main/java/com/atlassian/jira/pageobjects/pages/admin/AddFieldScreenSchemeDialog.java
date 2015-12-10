package com.atlassian.jira.pageobjects.pages.admin;

import com.atlassian.jira.pageobjects.dialogs.FormDialog;
import com.atlassian.jira.pageobjects.pages.admin.screen.ConfigureScreenScheme;
import com.atlassian.pageobjects.binder.Init;
import com.atlassian.pageobjects.elements.PageElement;
import org.openqa.selenium.By;

/**
 * Represents the &quot;add field screen scheme&quot; dialog available from the
 * the {@link com.atlassian.jira.pageobjects.pages.admin.ViewFieldScreenSchemesPage}.
 *
 * @since v5.0.1
 */
public class AddFieldScreenSchemeDialog extends FormDialog
{
    public static final String ID = "add-field-screen-scheme-dialog";
    private PageElement fieldScreenSchemeName;

    private PageElement fieldScreenSchemeDescription;

    private PageElement submit;

    public AddFieldScreenSchemeDialog()
    {
        super(ID);
    }

    @Init
    public void init()
    {
        fieldScreenSchemeName = find(By.name("fieldScreenSchemeName"));
        fieldScreenSchemeDescription = find(By.name("fieldScreenSchemeDescription"));
        submit = find(By.name("Add"));
    }

    public AddFieldScreenSchemeDialog setName(final String name)
    {
        assertDialogOpen();
        setElement(fieldScreenSchemeName, name);
        return this;
    }

    public AddFieldScreenSchemeDialog setDescription(final String description)
    {
        assertDialogOpen();
        setElement(fieldScreenSchemeDescription, description);
        return this;
    }

    /**
     * Submits the dialog expecting a success response and a redirect to the
     * ConfigureScreenScheme page.
     *
     * @return An instance of the ConfigureScreenScheme page.
     */
    public ConfigureScreenScheme submitSuccess()
    {
        submit();
        assertDialogClosed();
        return binder.bind(ConfigureScreenScheme.class);
    }

    /**
     * Submits this dialog.
     */
    public void submit()
    {
        submit(submit);
    }
}
