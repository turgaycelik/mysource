package com.atlassian.jira.pageobjects.pages.admin;

import com.atlassian.jira.pageobjects.pages.AbstractJiraPage;
import com.atlassian.jira.pageobjects.project.ProjectSharedBy;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.Conditions;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import org.hamcrest.Matchers;

/**
 * @since v4.4
 */
public class EditPermissionScheme extends AbstractJiraPage
{
    @ElementBy (id = "edit_project_permissions")
    private PageElement projectPermissionsElement;

    @ElementBy (className = "shared-by")
    private PageElement sharedBy;

    private final long schemeId;

    public EditPermissionScheme(long schemeId)
    {
        this.schemeId = schemeId;
    }

    @Override
    public String getUrl()
    {
        return "/secure/admin/EditPermissions!default.jspa?schemeId=" + schemeId;
    }

    @Override
    public TimedCondition isAt()
    {
        //This is the only way I can do this now.
        return Conditions.forMatcher(projectPermissionsElement.timed().getAttribute("data-schemeid"),
                Matchers.equalTo(String.valueOf(schemeId)));
    }

    public long getSchemeId()
    {
        return schemeId;
    }

    public ProjectSharedBy getSharedBy()
    {
        return pageBinder.bind(ProjectSharedBy.class, sharedBy);
    }
}
