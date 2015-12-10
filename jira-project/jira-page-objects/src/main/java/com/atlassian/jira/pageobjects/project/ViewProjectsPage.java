package com.atlassian.jira.pageobjects.project;

import com.atlassian.jira.pageobjects.project.add.AddProjectWizardProjectTypeSelection;
import com.atlassian.jira.pageobjects.project.summary.EditProjectDialog;
import org.openqa.selenium.By;

import com.atlassian.jira.pageobjects.framework.elements.PageElements;
import com.atlassian.jira.pageobjects.framework.util.JiraLocators;
import com.atlassian.jira.pageobjects.pages.AbstractJiraPage;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.Conditions;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.inject.Inject;

import static org.junit.Assert.assertTrue;

/**
 * View Projects Page. Still minimal and needs to be filled in as time goes by
 * =)
 * 
 * @since v4.4
 */
public class ViewProjectsPage extends AbstractJiraPage
{
    private static final String URI = "/secure/project/ViewProjects.jspa";

    @ElementBy(id = "project-list")
    private PageElement table;

    @ElementBy(id = "add_project")
    private PageElement addProject;

    @ElementBy(id = "noprojects")
    private PageElement noProjects;

    @ElementBy(cssSelector = "tbody > tr", within = "table")
    private Iterable<PageElement> projectRows;

    public boolean isRowPresent(final String projectName)
    {
        final PageElement row = table.find(getProjectRowByNameLocator(projectName));
        return row.isPresent();
    }

    @Override
    public String getUrl()
    {
        return URI;
    }

    private static By getProjectRowByNameLocator(final String projectName)
    {
        return By.linkText(projectName);
    }

    @Override
    public TimedCondition isAt()
    {
        return Conditions.or(table.timed().isPresent(), noProjects.timed().isPresent());
    }

    public boolean hasProjects()
    {
        return table.isPresent();
    }

    public boolean canCreateProject()
    {
        return addProject.isPresent();
    }

    public AddProjectWizardProjectTypeSelection openCreateProjectDialog()
    {
        assertTrue("Add project link not present.", addProject.isPresent());
        addProject.click();
        return pageBinder.bind(AddProjectWizardProjectTypeSelection.class);
    }

    public Iterable<ProjectRow> getProjectRows()
    {
        return Iterables.transform(projectRows, PageElements.bind(pageBinder, ProjectRow.class));
    }

    public ProjectRow findProject(final String key)
    {
        return Iterables.find(getProjectRows(), new Predicate<ProjectRow>()
        {
            @Override
            public boolean apply(final ProjectRow projectRow)
            {
                return projectRow != null && projectRow.getProjectKey().equals(key);
            }
        });
    }

    public static final class ProjectRow
    {
        @Inject
        PageBinder binder;

        private final String projectKey;
        private final PageElement editLink;
        private final PageElement deleteLink;

        public ProjectRow(final PageElement parent)
        {
            editLink = parent.find(By.linkText("Edit"));
            deleteLink = parent.find(By.linkText("Delete"));
            projectKey = parent.find(JiraLocators.byCellType("key")).getText();
        }

        public String getProjectKey()
        {
            return projectKey;
        }

        public boolean canEdit()
        {
            return editLink.isPresent();
        }

        public boolean canDelete()
        {
            return deleteLink.isPresent();
        }

        public EditProjectDialog edit()
        {
            editLink.click();
            return binder.bind(EditProjectDialog.class);
        }
    }
}
