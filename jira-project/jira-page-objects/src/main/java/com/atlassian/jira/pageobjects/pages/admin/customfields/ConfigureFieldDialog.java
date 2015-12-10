package com.atlassian.jira.pageobjects.pages.admin.customfields;

import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.binder.WaitUntil;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.Conditions;

import javax.inject.Inject;

import static com.atlassian.pageobjects.elements.query.Poller.waitUntilFalse;
import static com.atlassian.pageobjects.elements.query.Poller.waitUntilTrue;

/**
 * @since v6.1
 */
public class ConfigureFieldDialog
{
    @Inject
    private PageBinder binder;

    @ElementBy(id = "custom-field-name")
    private PageElement nameElement;

    @ElementBy(id = "custom-field-description")
    private PageElement descriptionElement;

    @ElementBy(id = "customfields-configure-next")
    private PageElement next;

    @ElementBy(id = "custom-field-options-input")
    private PageElement optionInput;

    @ElementBy(id = "custom-field-options-add")
    private PageElement optionEdit;

    @WaitUntil
    public void await()
    {
        waitUntilTrue(Conditions.and(nameElement.timed().isPresent(), nameElement.timed().isVisible()));
    }

    public ConfigureFieldDialog name(String name)
    {
        nameElement.clear().type(name);
        return this;
    }

    public ConfigureFieldDialog description(String description)
    {
        descriptionElement.clear().type(description);
        return this;
    }

    public AssociateCustomFieldToScreenPage nextAndThenAssociate()
    {
        next.click();
        return binder.bind(AssociateCustomFieldToScreenPage.class);
    }

    public ConfigureFieldDialog addOption(String option)
    {
        optionInput.type(option);
        optionEdit.click();
        return this;
    }

    public void create()
    {
        next.click();

        //We wait until the dialog closes before we return.
        waitUntilFalse(next.timed().isPresent());
    }
}
