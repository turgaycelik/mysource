package com.atlassian.jira.pageobjects.pages.admin.mail;

import com.atlassian.jira.pageobjects.pages.AbstractJiraPage;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.TimedCondition;

public class EditOutgoingMailServersPage extends AbstractJiraPage
{
    private final long serverId;

    @ElementBy(cssSelector = "form[name=jiraform]")
    private PageElement serverForm;

    @ElementBy(cssSelector = "input[name=prefix]", within = "serverForm")
    private PageElement inputPrefix;

    public EditOutgoingMailServersPage(long serverId)
    {
        super();
        this.serverId = serverId;
    }

    @Override
    public String getUrl()
    {
        return "/secure/admin/UpdateSmtpMailServer!default.jspa?id=" + serverId;
    }

    @Override
    public TimedCondition isAt()
    {
        return serverForm.timed().isPresent();
    }

    public String getPrefixValue()
    {
        return inputPrefix.getValue();
    }
}
