package com.atlassian.jira.pageobjects.pages.admin.customfields;

import javax.inject.Inject;

import com.atlassian.jira.pageobjects.components.fields.SingleSelect;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.binder.Init;
import com.atlassian.pageobjects.binder.WaitUntil;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.Conditions;

import static com.atlassian.pageobjects.elements.query.Poller.waitUntilTrue;

/**
 * TODO: Document this class / interface here
 *
 * @since v6.1
 */
public class FieldWisherDialog
{
    @Inject
    private PageBinder binder;

    @ElementBy (id = "customfields-field-wisher")
    private PageElement dialog;

    @ElementBy (id = "customfields-field-wisher-next")
    private PageElement next;

    @ElementBy (cssSelector = "#customfields-field-wisher form")
    private PageElement fieldParent;

    private SingleSelect fieldSelect;

    @WaitUntil
    public void await()
    {
        waitUntilTrue(Conditions.and(dialog.timed().isPresent(), dialog.timed().isVisible()));
    }

    @Init
    public void init()
    {
        fieldSelect = binder.bind(SingleSelect.class, fieldParent);
    }

    /**
     * Select an existing custom field.
     * @param name the name of the custom field to select
     */
    public void select(final String name)
    {
        fieldSelect.select(name);
    }

    /**
     * Clicks the next button.
     */
    public void clickNext()
    {
        next.click();
    }

    /**
     * Choose to create a new custom field.
     *
     * @return The next step in the process.
     */
    public TypeSelectionCustomFieldDialog create(final String name)
    {
        fieldSelect.select(name);
        next.click();
        return binder.bind(TypeSelectionCustomFieldDialog.class);
    }

    /**
     * @return the next button's text
     */
    public String getNextButtonText()
    {
        return next.getText();
    }
}