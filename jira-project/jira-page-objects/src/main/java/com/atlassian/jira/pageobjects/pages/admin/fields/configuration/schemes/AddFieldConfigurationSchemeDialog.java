package com.atlassian.jira.pageobjects.pages.admin.fields.configuration.schemes;

import com.atlassian.jira.pageobjects.dialogs.FormDialog;
import com.atlassian.jira.pageobjects.pages.admin.fields.configuration.schemes.configure.ConfigureFieldConfigurationSchemePage;
import com.atlassian.pageobjects.binder.Init;
import com.atlassian.pageobjects.elements.PageElement;
import org.openqa.selenium.By;

/**
 * Represents the &quot;add field configuration&quot; dialog available from the
 * the {@link com.atlassian.jira.pageobjects.pages.admin.ViewFieldConfigurationsPage}.
 *
 * @since v5.0.1
 */
public class AddFieldConfigurationSchemeDialog extends FormDialog
{
    private PageElement fieldLayoutSchemeName;

    private PageElement fieldLayoutSchemeDescription;

    private PageElement submit;

    public AddFieldConfigurationSchemeDialog()
    {
        super("add-field-configuration-scheme-dialog");
    }

    @Init
    public void init()
    {
        fieldLayoutSchemeName = find(By.name("fieldLayoutSchemeName"));
        fieldLayoutSchemeDescription = find(By.name("fieldLayoutSchemeDescription"));
        submit = find(By.name("Add"));
    }

    public AddFieldConfigurationSchemeDialog setName(final String name)
    {
        assertDialogOpen();
        setElement(fieldLayoutSchemeName, name);
        return this;
    }

    public AddFieldConfigurationSchemeDialog setDescription(final String description)
    {
        assertDialogOpen();
        setElement(fieldLayoutSchemeDescription, description);
        return this;
    }

    /**
     * Submits the dialog expecting a success response and a redirect to the
     * ViewFieldConfigurationsPage.
     *
     * @return An instance of the ViewFieldConfigurationsPage.
     */
    public ConfigureFieldConfigurationSchemePage submitSuccess()
    {
        submit();
        assertDialogClosed();
        return binder.bind(ConfigureFieldConfigurationSchemePage.class);
    }

    /**
     * Submits this dialog.
     */
    public void submit()
    {
        submit(submit);
    }
}
