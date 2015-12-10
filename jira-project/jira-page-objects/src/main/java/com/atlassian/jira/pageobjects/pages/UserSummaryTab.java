package com.atlassian.jira.pageobjects.pages;

import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.TimedCondition;

/**
 * @since v6.1
 */
public class UserSummaryTab implements ViewProfileTab
{
    @ElementBy (id = "up-user-title")
    private PageElement userTitle;

    @Override
    public String linkId()
    {
        return "up_user-profile-summary-panel_a";
    }

    @Override
    public TimedCondition isOpen()
    {
        return userTitle.timed().isPresent();
    }

    @Override
    public String getUrlPart()
    {
        return "selectedTab=jira.user.profile.panels:user-profile-summary-panel";
    }
}
