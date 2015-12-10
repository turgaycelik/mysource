package com.atlassian.jira.pageobjects.pages.admin.issuetype.screen.schemes.configure;

import com.atlassian.jira.pageobjects.dialogs.FormDialog;
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
 * Represents the &quot;add issue type to screen scheme association&quot; dialog available from the
 * the {@link ConfigureIssueTypeScreenSchemePage}.
 *
 * @since v5.0.2
 */
public class AddIssueTypeToScreenSchemeAssociationDialog extends FormDialog
{
    private PageElement issueTypeScreenSchemeId;

    private SelectElement issueTypeDropdown;

    private SelectElement screenSchemeDropdown;

    private PageElement submit;

    public AddIssueTypeToScreenSchemeAssociationDialog()
    {
        super("add-issue-type-screen-scheme-configuration-association-dialog");
    }

    @Init
    public void init()
    {
        issueTypeScreenSchemeId = find(By.name("id"));
        issueTypeDropdown = find(By.name("issueTypeId"), SelectElement.class);
        screenSchemeDropdown = find(By.name("fieldScreenSchemeId"), SelectElement.class);
        submit = find(By.name("Add"));
    }
    public AddIssueTypeToScreenSchemeAssociationDialog setIssueType(final String issueType)
    {
        assertDialogOpen();
        issueTypeDropdown.select(Options.text(issueType));
        return this;
    }

    public AddIssueTypeToScreenSchemeAssociationDialog setScreenScheme(final String screenScheme)
    {
        assertDialogOpen();
        screenSchemeDropdown.select(Options.text(screenScheme));
        return this;
    }

    public Iterable<String> getSelectableIssueTypes()
    {
        return copyOf(transform(issueTypeDropdown.getAllOptions(), new Function<Option, String>()
        {
            @Override
            public String apply(final Option issueTypeOption)
            {
                return issueTypeOption.text();
            }
        }));
    }

    /**
     * Submits the dialog expecting a success response and a redirect to the
     * ConfigureIssueTypeScreenSchemePage.
     *
     * @return An instance of the ConfigureIssueTypeScreenSchemePage.
     */
    public ConfigureIssueTypeScreenSchemePage submitSuccess()
    {
        final String issueTypeScreenSchemeId = this.issueTypeScreenSchemeId.getValue();
        submit();
        assertDialogClosed();
        return binder.bind(ConfigureIssueTypeScreenSchemePage.class, issueTypeScreenSchemeId);
    }

    /**
     * Submits this dialog.
     */
    public void submit()
    {
        submit(submit);
    }
}
