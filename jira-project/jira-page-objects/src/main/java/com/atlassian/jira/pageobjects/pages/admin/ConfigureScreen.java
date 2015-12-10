package com.atlassian.jira.pageobjects.pages.admin;

import com.atlassian.jira.pageobjects.pages.AbstractJiraPage;
import com.atlassian.jira.pageobjects.project.ProjectSharedBy;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.TimedCondition;

/**
 *
 * @since v4.4
 */
public class ConfigureScreen extends AbstractJiraPage
{
    @ElementBy (id="screen-editor")
    private PageElement screenId;

    @ElementBy (id="screenName")
    private PageElement screenName;

    @ElementBy (className = "shared-by")
    private PageElement sharedBy;

    private final String uri;

    public ConfigureScreen(long screenId)
    {
        uri = String.format("/secure/admin/ConfigureFieldScreen.jspa?id=%d", screenId);
    }

    public ConfigureScreen()
    {
        uri = null;
    }

    public long getScreenId()
    {
        return Long.parseLong(screenId.getAttribute("data-screen"));
    }

    public String getScreenName()
    {
        return screenName.getText();
    }

    @Override
    public String getUrl()
    {
        if (uri == null)
        {
            throw new IllegalStateException("Need to use the other constructor.");
        }
        return uri;
    }

    @Override
    public TimedCondition isAt()
    {
        return screenId.timed().isPresent();
    }

    public ProjectSharedBy getSharedBy()
    {
        return pageBinder.bind(ProjectSharedBy.class, sharedBy);
    }
}
