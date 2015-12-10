package com.atlassian.jira.pageobjects.pages.admin;

import com.atlassian.jira.pageobjects.pages.AbstractJiraPage;
import com.atlassian.jira.pageobjects.project.ProjectSharedBy;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.Conditions;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import org.hamcrest.Matchers;

/**
 * Edit notification scheme page
 *
 * @since v4.4
 */
public class EditNotificationsPage extends AbstractJiraPage
{

    @ElementBy(id = "notificationSchemeTable")
    private PageElement schemeTable;

    @ElementBy (className = "shared-by")
    private PageElement sharedBy;

    private long schemeId;

    public EditNotificationsPage(final long schemeId)
    {
        this.schemeId = schemeId;
    }

    @Override
    public TimedCondition isAt()
    {
        return Conditions.and(
                schemeTable.timed().isPresent(),
                Conditions.forMatcher(schemeTable.timed().getAttribute("data-schemeid"), Matchers.equalTo(String.valueOf(schemeId)))
        );
    }

    @Override
    public String getUrl()
    {
        return String.format("/secure/admin/EditNotifications!default.jspa?schemeId=%s", schemeId);
    }

    public ProjectSharedBy getSharedBy()
    {
        return pageBinder.bind(ProjectSharedBy.class, sharedBy);
    }

}
