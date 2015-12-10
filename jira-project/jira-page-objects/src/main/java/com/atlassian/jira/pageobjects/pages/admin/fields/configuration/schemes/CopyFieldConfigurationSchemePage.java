package com.atlassian.jira.pageobjects.pages.admin.fields.configuration.schemes;

import com.atlassian.jira.pageobjects.pages.AbstractJiraPage;
import com.atlassian.pageobjects.binder.Init;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.TimedCondition;

import org.openqa.selenium.By;

/**
 * Represents the &quot;add field configuration&quot; dialog available from the
 * the {@link com.atlassian.jira.pageobjects.pages.admin.ViewFieldConfigurationsPage}.
 *
 * @since v5.0.1
 */
public class CopyFieldConfigurationSchemePage extends AbstractJiraPage
{
    @ElementBy(name = "fieldLayoutSchemeName")
    PageElement fieldLayoutSchemeName;

    @ElementBy(name = "fieldLayoutSchemeDescription")
    PageElement fieldLayoutSchemeDescription;

    PageElement submit;

    @Init
    public void onInit()
    {
        submit = elementFinder.find(By.id("copy_submit"));
    }

    public CopyFieldConfigurationSchemePage setName(final String name)
    {
        fieldLayoutSchemeName.clear().type(name);
        return this;
    }

    public CopyFieldConfigurationSchemePage setDescription(final String description)
    {
        fieldLayoutSchemeDescription.clear().type(description);
        return this;
    }

    /**
     * Submits the dialog expecting a success response and a redirect to the
     * ViewFieldConfigurationsPage.
     *
     * @return An instance of the ViewFieldConfigurationsPage.
     */
    public ViewFieldConfigurationSchemesPage submitSuccess()
    {
        submit();
        return pageBinder.bind(ViewFieldConfigurationSchemesPage.class);
    }

    /**
     * Submits this dialog.
     */
    public void submit()
    {
        submit.click();
    }

    @Override
    public TimedCondition isAt()
    {
        return elementFinder.find(By.id("copy_submit")).timed().isVisible();
    }

    @Override
    public String getUrl()
    {
        return null;
    }
}
