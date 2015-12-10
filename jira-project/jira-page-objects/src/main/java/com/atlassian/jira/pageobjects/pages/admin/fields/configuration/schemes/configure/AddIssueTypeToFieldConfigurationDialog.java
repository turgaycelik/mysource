package com.atlassian.jira.pageobjects.pages.admin.fields.configuration.schemes.configure;

import java.util.List;

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
 * Represents the &quot;add issue type to field configuration association&quot; dialog available from the
 * the {@link ConfigureFieldConfigurationSchemePage}.
 *
 * @since v5.0.1
 */
public class AddIssueTypeToFieldConfigurationDialog extends FormDialog
{
    private PageElement fieldConfigurationSchemeId;

    private SelectElement issueTypeDropdown;

    private SelectElement fieldConfigurationDropdown;

    private PageElement submit;

    public AddIssueTypeToFieldConfigurationDialog()
    {
        super("add-issue-type-field-configuration-association-dialog");
    }

    @Init
    public void init()
    {
        fieldConfigurationSchemeId = find(By.name("id"));
        issueTypeDropdown = find(By.name("issueTypeId"), SelectElement.class);
        fieldConfigurationDropdown = find(By.name("fieldConfigurationId"), SelectElement.class);
        submit = find(By.name("Add"));
    }

    public AddIssueTypeToFieldConfigurationDialog setIssueType(final String issueType)
    {
        return setIssueType(Options.text(issueType));
    }

    public AddIssueTypeToFieldConfigurationDialog setIssueType(final Option issueType)
    {
        assertDialogOpen();
        issueTypeDropdown.select(issueType);
        return this;
    }

    public List<Option> getIssueTypes()
    {
        assertDialogOpen();
        return issueTypeDropdown.getAllOptions();
    }

    public List<Option> getFieldConfigurations()
    {
        assertDialogOpen();
        return fieldConfigurationDropdown.getAllOptions();
    }

    public AddIssueTypeToFieldConfigurationDialog setFieldConfiguration(final String fieldConfiguration)
    {
        return setFieldConfiguration(Options.text(fieldConfiguration));
    }

    public AddIssueTypeToFieldConfigurationDialog setFieldConfiguration(final Option fieldConfiguration)
    {
        assertDialogOpen();
        fieldConfigurationDropdown.select(fieldConfiguration);
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
     * ConfigureFieldConfigurationSchemePage.
     *
     * @return An instance of the ConfigureFieldConfigurationSchemePage.
     */
    public ConfigureFieldConfigurationSchemePage submitSuccess()
    {
        final String fieldConfigurationId = fieldConfigurationSchemeId.getValue();
        submit();
        assertDialogClosed();
        return binder.bind(ConfigureFieldConfigurationSchemePage.class, fieldConfigurationId);
    }

    /**
     * Submits this dialog.
     */
    public void submit()
    {
        submit(submit);
    }
}
