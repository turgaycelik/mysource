package com.atlassian.jira.pageobjects.project.add;

import com.atlassian.jira.pageobjects.components.fields.SingleSelect;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.binder.Init;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.Poller;
import com.google.inject.Inject;
import org.openqa.selenium.By;

import static org.junit.Assert.assertTrue;

public class AddProjectWizardProjectDetails extends AddProjectWizardPage
{

    private PageElement nameElement;
    private PageElement keyElement;
    private PageElement submit;
    private PageElement leadContainer;
    private SingleSelect leadSelect;

    @Inject
    private PageBinder binder;

    @Init
    public void init()
    {
        nameElement = find(By.name("name"));
        keyElement = find(By.name("key"));
        submit = find(By.cssSelector(".add-project-dialog-create-button.pt-submit-button"));
        leadContainer = find(By.id("lead-picker"));
        leadSelect = binder.bind(SingleSelect.class, leadContainer);

        final PageElement keyManuallyEdited = find(By.name("keyEdited"));
        if (keyManuallyEdited != null)
        {
            // Prevent auto key generation. You have to enter your key manually in tests. See CreateProjectField.js
            driver.executeScript("jQuery('input[name=keyEdited]').val('true')");
        }
    }

    public AddProjectWizardProjectDetails setName(final String name)
    {
        assertDialogOpen();
        setElement(nameElement, name);
        return this;
    }

    public AddProjectWizardProjectDetails setKey(final String key)
    {
        assertDialogOpen();
        setElement(keyElement, key);
        return this;
    }

    public AddProjectWizardProjectDetails setLead(final String lead)
    {
        assertDialogOpen();
        assertTrue("The lead element is not present. Only one user in the system?", isLeadPresent());
        leadSelect.select(lead);
        return this;
    }

    public boolean isLeadPresent()
    {
        return leadContainer.isPresent();
    }

    public AddProjectWizardProjectDetails submitFail()
    {
        submit(submit);
        assertDialogOpen();
        return this;
    }

    public void submitSuccess()
    {
        submit(submit);
        Poller.waitUntilFalse("Dialog is not closed", isOpen());
    }

    public AddProjectWizardPageConfirmOdProjectCreation submitOndemandLinkedProjectCreation()
    {
        submit(submit);
        return binder.bind(AddProjectWizardPageConfirmOdProjectCreation.class);
    }

    public boolean isLeadpickerDisabled()
    {
        return leadSelect.isAutocompleteDisabled();
    }

    public AddProjectWizardProjectDetails odEntityLinkedBambooProject(final boolean whether)
    {
        setCheckbox(whether, find(By.id("capability_bamboo.project")));
        return this;
    }

    public AddProjectWizardProjectDetails odEntityLinkedConfluenceSpace(final boolean whether)
    {
        setCheckbox(whether, find(By.id("capability_confluence.space")));
        return this;
    }

    private void setCheckbox(final boolean whether, final PageElement checkbox)
    {
        if(checkbox.isSelected() ^ whether)
        {
            checkbox.toggle();
        }
    }


}
