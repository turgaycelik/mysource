package com.atlassian.jira.pageobjects.pages;

import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.TimedCondition;

/**
 * This is a page loads very fast and has no javascript slowing it down or taking up CPU.
 * This page can be switched for another page which is better at being loaded fast and is not flaky (preferably no js).
 * This page needs to work in both BTF and OD incarnations of JIRA.
 */
public class MinimalContentJiraPage extends AbstractJiraPage
{
    @ElementBy (id = "update-keyboard-shortcuts")
    private PageElement mainForm;

    @Override
    public TimedCondition isAt()
    {
        return mainForm.timed().isPresent();
    }

    @Override
    public String getUrl()
    {
        return "/secure/ViewKeyboardShortcuts!default.jspa";
    }
}
