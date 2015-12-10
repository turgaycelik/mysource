package com.atlassian.jira.pageobjects.pages.admin.issuetype.screen.schemes;

import com.atlassian.jira.pageobjects.dialogs.FormDialog;
import com.atlassian.jira.pageobjects.pages.admin.issuetype.screen.schemes.configure.ConfigureIssueTypeScreenSchemePage;
import com.atlassian.pageobjects.binder.Init;
import com.atlassian.pageobjects.elements.Option;
import com.atlassian.pageobjects.elements.Options;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.SelectElement;
import com.google.common.base.Function;
import org.openqa.selenium.By;

import static com.google.common.collect.ImmutableList.copyOf;
import static com.google.common.collect.Iterables.transform;

/**
 * Represents the &quot;add issue type screen scheme&quot; dialog available from the the
 * {@link ViewIssueTypeScreenSchemesPage}.
 *
 * @since v5.0.2
 */
public class AddIssueTypeScreenSchemeDialog extends FormDialog
{
    private PageElement schemeName;

    private PageElement schemeDescription;

    private SelectElement screenSchemeDropDown;

    private PageElement submit;

    public AddIssueTypeScreenSchemeDialog()
    {
        super("add-issue-type-screen-scheme-dialog");
    }

    @Init
    public void init()
    {
        schemeName = find(By.name("schemeName"));
        schemeDescription = find(By.name("schemeDescription"));
        screenSchemeDropDown = find(By.name("fieldScreenSchemeId"), SelectElement.class);
        submit = find(By.name("Add"));
    }
    
    public AddIssueTypeScreenSchemeDialog setName(final String name)
    {
        assertDialogOpen();
        setElement(schemeName, name);
        return this;
    }

    public AddIssueTypeScreenSchemeDialog setDescription(final String description)
    {
        assertDialogOpen();
        setElement(schemeDescription, description);
        return this;
    }

    public AddIssueTypeScreenSchemeDialog setScreenScheme(final String screenScheme)
    {
        assertDialogOpen();
        screenSchemeDropDown.select(Options.text(screenScheme));
        return this;
    }

    public Iterable<String> getSelectableScreenSchemes()
    {
        return copyOf(transform(screenSchemeDropDown.getAllOptions(), new Function<Option, String>()
        {
            @Override
            public String apply(final Option screenSchemeOption)
            {
                return screenSchemeOption.text();
            }
        }));
    }

    /**
     * Submits the dialog expecting a success response and a redirect to the
     * ConfigureFieldConfigurationSchemePage.
     *
     * @return An instance of the ConfigureFieldConfigurationSchemePage.
     */
    public ConfigureIssueTypeScreenSchemePage submitSuccess()
    {
        submit();
        assertDialogClosed();
        return binder.bind(ConfigureIssueTypeScreenSchemePage.class);
    }

    /**
     * Submits this dialog.
     */
    public void submit()
    {
        submit(submit);
    }
}
