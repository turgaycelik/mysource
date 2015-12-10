package it.com.atlassian.jira.plugin.headernav.customcontentlinks.admin;


import com.atlassian.jira.projects.pageobjects.webdriver.page.legacy.BrowseProjectPage;
import org.openqa.selenium.By;

public class ProjectShortcutsBrowseProjectPage extends BrowseProjectPage
{
    public ProjectShortcutsBrowseProjectPage(final String projectKey)
    {
        super(projectKey);
    }

    public ProjectShortcutsDialog clickAvatar()
    {
        elementFinder.find(By.id("project-avatar")).click();
        return pageBinder.bind(ProjectShortcutsDialog.class);
    }
}
