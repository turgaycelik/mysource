package com.atlassian.jira.pageobjects.pages.admin.mail;

import org.openqa.selenium.By;

import com.atlassian.jira.pageobjects.pages.AbstractJiraAdminPage;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.TimedCondition;

public class ViewOutgoingMailServersPage extends AbstractJiraAdminPage
{
    @ElementBy(cssSelector = "#smtp-mail-servers-panel table.aui.aui-table-rowhover")
    private PageElement serversTable;

    @ElementBy(linkText = "Edit", within = "serversTable")
    private PageElement editLink;

    @Override
    public String getUrl()
    {
        return "/secure/admin/OutgoingMailServers.jspa";
    }

    @Override
    public String linkId()
    {
        return "outgoing_mail";
    }

    @Override
    public TimedCondition isAt()
    {
        return elementFinder.find(By.id("smtp-mail-servers-panel")).timed().isPresent();
    }

    public boolean existSmtpMailServer()
    {
        return editLink != null;
    }

    public EditOutgoingMailServersPage editSmtpMailServer()
    {
        final String idAtt = editLink.getAttribute("id");
        final long id = Long.parseLong(idAtt.split("_")[1]);
        editLink.click();
        return pageBinder.bind(EditOutgoingMailServersPage.class, id);
    }

}
