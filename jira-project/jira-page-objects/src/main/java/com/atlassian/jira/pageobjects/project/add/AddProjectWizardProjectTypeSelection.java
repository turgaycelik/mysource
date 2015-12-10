package com.atlassian.jira.pageobjects.project.add;

import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.binder.Init;
import com.atlassian.pageobjects.elements.PageElement;
import com.google.inject.Inject;
import org.openqa.selenium.By;


/**
 * This is the start of the "Create Project" accessible from the header menu. To get here, bind to the
 * com.atlassian.jira.pageobjects.components.JiraHeader and click through the menus.
 * @since v4.4
 */
public class AddProjectWizardProjectTypeSelection extends AddProjectWizardPage
{

    private static final String SOFTWARE_DEVELOPMENT_MODULE_KEY = "com.atlassian.jira.plugins.jira-software-plugin:jira-software-development-project-template";
    private static final String ISSUE_TRACKING_MODULE_KEY = "com.atlassian.jira-core-project-templates:jira-issuetracking-item";
    private static final String PROJECT_MANAGEMENT_MODULE_KEY = "com.atlassian.jira-core-project-templates:jira-projectmanagement-item";
    private static final String JIRA_CLASSIC_MODULE_KEY = "com.atlassian.jira-core-project-templates:jira-blank-item";

    private PageElement templateList;
    private PageElement submit;

    @Inject
    private PageBinder binder;

    @Init
    public void init()
    {
        templateList = find(By.className("pt-templates-list"));

        submit = find(By.cssSelector(".create-project-dialog-create-button.pt-submit-button"));
    }

    public AddProjectWizardProjectDetails softwareDevelopment()
    {
        return selectByModuleCompleteKey(SOFTWARE_DEVELOPMENT_MODULE_KEY).acceptAndConfirm();
    }

    public AddProjectWizardProjectDetails issueTracking()
    {
        return selectByModuleCompleteKey(ISSUE_TRACKING_MODULE_KEY).acceptAndConfirm();
    }

    public AddProjectWizardProjectDetails projectManagement()
    {
        return selectByModuleCompleteKey(PROJECT_MANAGEMENT_MODULE_KEY).accept();
    }

    public AddProjectWizardProjectDetails jiraClassic()
    {
        return selectByModuleCompleteKey(JIRA_CLASSIC_MODULE_KEY).accept();
    }

    public AddProjectWizardProjectTypeSelection selectByModuleCompleteKey(final String key)
    {
        templateList.find(By.cssSelector(String.format("li.template[data-item-module-complete-key=\"%s\"]", key))).click();
        return this;
    }

    public AddProjectWizardProjectDetails accept()
    {
        submit.click();
        return binder.bind(AddProjectWizardProjectDetails.class);
    }

    public AddProjectWizardProjectDetails acceptAndConfirm()
    {
        submit.click();
        return binder.bind(AddProjectWizardConfirmSelection.class).confirm();
    }

    public AddProjectWizardProjectDetails acceptWithConfirmation()
    {
        submit.click();
        return binder.bind(AddProjectWizardProjectDetails.class);
    }




}
