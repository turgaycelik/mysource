package com.atlassian.jira.pageobjects.pages.admin;

import com.atlassian.jira.pageobjects.pages.AbstractJiraAdminPage;
import com.atlassian.pageobjects.Page;
import com.atlassian.pageobjects.binder.Init;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.Conditions;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Prejects page in the admin section.
 *
 * @since v4.4
 */
public class ViewProjectsPage extends AbstractJiraAdminPage
{

    @ElementBy (id = "project-list")
    private PageElement projectsList;

    @FindBy (id = "add_project")
    private WebElement addProjectLink;

    @ElementBy (id = "noprojects")
    private PageElement noProjectsMessage;

    private final List<ProjectRow> projects;

    private final static String URI = "/secure/project/ViewProjects.jspa";

    public ViewProjectsPage()
    {
        projects = new ArrayList<ProjectRow>();
    }

    @Override
    public TimedCondition isAt()
    {
        return Conditions.or(projectsList.timed().isPresent(), noProjectsMessage.timed().isPresent());
    }

    public String getUrl()
    {
        return URI;
    }

    @Override
    public String linkId()
    {
        return "view_projects";
    }

    @Init
    private void loadProjects()
    {
        List<WebElement> rows = driver.findElements(By.cssSelector("table#project-list > tbody > tr"));

        // TODO BAD! relies on user language
        if (rows.get(0).getText().equals("You do not have the permissions to administer any projects, or there are none created."))
        {
            return;
        }

        for(WebElement row : rows)
        {
            projects.add(pageBinder.bind(ProjectRow.class, row));
        }

    }

    public Page addProject()
    {
        throw new UnsupportedOperationException("addProject for ProjectViewPage has not been implemented");
    }

    public List<ProjectRow> getProjects()
    {
        return Collections.unmodifiableList(projects);
    }

    public boolean isProjectPresent(final String projectName)
    {
        for (ProjectRow project: projects)
        {
            if (projectName.equals(project.getName()))
            {
                return true;
            }
        }
        return false;
    }
}
