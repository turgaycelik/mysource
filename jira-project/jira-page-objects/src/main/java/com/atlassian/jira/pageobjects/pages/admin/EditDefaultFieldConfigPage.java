package com.atlassian.jira.pageobjects.pages.admin;

import com.atlassian.jira.pageobjects.pages.AbstractJiraPage;
import com.atlassian.jira.pageobjects.project.ProjectSharedBy;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.TimedCondition;

/**
 * Represents the Edit Field Configuration page for the Default Field Configuration. Has a different url to
 * {@link EditFieldConfigPage}
 *
 * @since v4.4
 */
public class EditDefaultFieldConfigPage extends AbstractJiraPage
{
    private String URI = "/secure/project/ViewIssueFields.jspa";

    @ElementBy(className = "jiraformbody")
    private PageElement jiraFormBody;

    @ElementBy (className = "shared-by")
    private PageElement sharedBy;

    @ElementBy (id = "field-layout-name")
    private PageElement name;


    public String getName()
    {
        return name.getText();
    }

    @Override
    public TimedCondition isAt()
    {
        return jiraFormBody.timed().isPresent();
    }

    @Override
    public String getUrl()
    {
        return URI;
    }

    public ProjectSharedBy getSharedBy()
    {
        return pageBinder.bind(ProjectSharedBy.class, sharedBy);
    }
}
