package com.atlassian.jira.pageobjects.pages.viewissue;

import com.atlassian.jira.pageobjects.pages.admin.roles.AbstractActorSelectionPage;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.TimedCondition;

/**
 * Page representing the Manage Watchers Page
 *
 * @since v5.2
 */
public class ManageWatchersPage extends AbstractActorSelectionPage
{
    private static final String URI = "/secure/ManageWatchers!default.jspa";
    private static final String PICKER_ID = "userNames";

    @ElementBy (id = "watch")
    protected PageElement watch;

    @ElementBy (id = "userNames_container")
    protected PageElement pickerContainer;

    private final String issueId;

    public ManageWatchersPage(String issueId)
    {
        this.issueId = issueId;
    }

    @Override
    public String getUrl()
    {
        return URI + "?id=" + issueId;
    }

    @Override
    protected String pickerId()
    {
        return PICKER_ID;
    }

    @Override
    public TimedCondition isAt()
    {
        return watch.timed().isPresent();
    }
}
