package com.atlassian.jira.pageobjects.project.add;

import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.binder.Init;
import com.atlassian.pageobjects.elements.PageElement;
import com.google.inject.Inject;
import org.openqa.selenium.By;

public class AddProjectWizardConfirmSelection extends AddProjectWizardPage
{

    private PageElement submit;

    @Inject
    private PageBinder binder;

    @Init
    public void init()
    {
        submit = find(By.cssSelector(".template-info-dialog-create-button.pt-submit-button"));
    }

    public AddProjectWizardProjectDetails confirm()
    {
        submit.click();
        return binder.bind(AddProjectWizardProjectDetails.class);
    }
}
