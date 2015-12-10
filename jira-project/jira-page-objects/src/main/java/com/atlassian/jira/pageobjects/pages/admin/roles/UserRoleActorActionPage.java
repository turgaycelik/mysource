package com.atlassian.jira.pageobjects.pages.admin.roles;

import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.TimedCondition;

/**
 * Represents the user role actor action page.
 *
 * @since v5.2
 */
public class UserRoleActorActionPage extends AbstractActorSelectionPage
{
    private static final String URI = "/secure/project/UserRoleActorAction.jspa";
    private static final String PICKER_ID = "userNames";

    @ElementBy (id = "userNames_container")
    protected PageElement pickerContainer;

    private final String projectRoleId;

    public UserRoleActorActionPage(String projectRoleId)
    {
        this.projectRoleId = projectRoleId;
    }

    @Override
    public String getUrl()
    {
        return URI + "?projectRoleId=" + projectRoleId;
    }

    @Override
    protected String pickerId()
    {
        return PICKER_ID;
    }

    @Override
    public TimedCondition isAt()
    {
        return pickerContainer.timed().isPresent();
    }
}
