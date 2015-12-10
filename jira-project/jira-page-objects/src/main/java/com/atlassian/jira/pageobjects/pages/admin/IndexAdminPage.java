package com.atlassian.jira.pageobjects.pages.admin;

import com.atlassian.jira.pageobjects.pages.AbstractJiraPage;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import com.atlassian.pageobjects.elements.query.TimedQuery;

/**
 * @since v6.1
 */
public class IndexAdminPage extends AbstractJiraPage
{

    @ElementBy (className = "form-body")
    PageElement form;

    @ElementBy (id = "reindex-foreground")
    PageElement foreground;

    @ElementBy (id = "reindex-background")
    PageElement background;

    @ElementBy (id = "indexing-submit")
    PageElement reindex;

    @ElementBy (className = "error")
    PageElement error;

    @ElementBy (cssSelector = ".form-body .info")
    PageElement info;

    @Override
    public TimedCondition isAt()
    {
        return form.timed().hasText("Re-Indexing");
    }

    @Override
    public String getUrl()
    {
        return "/secure/admin/jira/IndexAdmin.jspa";
    }

    public void reindexForeground()
    {
        foreground.click();
        reindex.click();
    }

    public void reindexBackground()
    {
        background.click();
        reindex.click();
    }

    public TimedQuery<String> hasError()
    {
        return error.timed().getText();
    }

    public TimedQuery<String> hasInfo()
    {
        return info.timed().getText();
    }

    public boolean isForegroundReindexDisabled()
    {
        return !foreground.isEnabled();
    }
}
