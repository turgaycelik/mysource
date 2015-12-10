package com.atlassian.jira.pageobjects.pages.admin.fields.configuration.schemes;

import com.atlassian.pageobjects.binder.Init;
import com.atlassian.pageobjects.elements.query.TimedCondition;

import org.openqa.selenium.By;

/**
 * Represents the &quot;add field configuration&quot; dialog available from the
 * the {@link com.atlassian.jira.pageobjects.pages.admin.ViewFieldConfigurationsPage}.
 *
 * @since v5.0.1
 */
public class EditFieldConfigurationSchemePage extends CopyFieldConfigurationSchemePage
{
    @Init
    public void onInit()
    {
        submit = elementFinder.find(By.id("update_submit"));
    }

    @Override
    public TimedCondition isAt()
    {
        return elementFinder.find(By.id("update_submit")).timed().isVisible();
    }

    public EditFieldConfigurationSchemePage setName(final String name)
    {
        super.setName(name);
        return this;
    }

    public EditFieldConfigurationSchemePage setDescription(final String description)
    {
        super.setDescription(description);
        return this;
    }
}
