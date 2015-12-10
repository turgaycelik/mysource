package com.atlassian.jira.pageobjects.project;

import com.atlassian.jira.pageobjects.pages.AbstractJiraPage;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.TimedCondition;

public class AddProjectPage extends AbstractJiraPage
{
    @ElementBy(id = "add-project")
    private PageElement addProjectForm;

    @ElementBy(id = "lead", within = "addProjectForm")
    private PageElement leadPickerSelect;

    @Override
    public String getUrl()
    {
        return "/secure/admin/AddProject!default.jspa";
    }

    public boolean isLeadPickerPresent()
    {
        return leadPickerSelect.isPresent();
    }

    public boolean hasLeadPickerClassName(final String className)
    {
        return leadPickerSelect.getAttribute("class").contains(className);
    }

    public String getLeadPickerClassAttr()
    {
        return leadPickerSelect.getAttribute("class");
    }

    @Override
    public TimedCondition isAt()
    {
        return addProjectForm.timed().isVisible();
    }
}
