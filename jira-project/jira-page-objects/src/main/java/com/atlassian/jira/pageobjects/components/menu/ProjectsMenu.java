package com.atlassian.jira.pageobjects.components.menu;

import com.atlassian.jira.pageobjects.project.add.AddProjectWizardProjectTypeSelection;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.elements.PageElement;
import com.google.inject.Inject;
import org.openqa.selenium.By;


/**
 * @since v6.0
 */
public class ProjectsMenu extends JiraAuiDropdownMenu
{

    @Inject
    private PageBinder binder;

    public ProjectsMenu()
    {
        super(By.id("browse_link"), By.id("browse_link-content"));
    }

    public ProjectsMenu open()
    {
        super.open();
        return this;
    }

    public PageElement getCurrentProject()
    {
        return getDropdown().find(By.id("admin_main_proj_link_lnk"));
    }

    public AddProjectWizardProjectTypeSelection createProject()
    {
        getDropdown().find(By.id("project_template_create_link_lnk")).click();
        return binder.bind(AddProjectWizardProjectTypeSelection.class);
    }
}