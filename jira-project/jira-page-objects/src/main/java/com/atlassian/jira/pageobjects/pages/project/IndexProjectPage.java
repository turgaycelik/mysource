package com.atlassian.jira.pageobjects.pages.project;

import com.atlassian.jira.pageobjects.pages.AbstractJiraPage;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.Poller;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import com.atlassian.pageobjects.elements.query.TimedQuery;

/**
 * Indexing project page with progress bar for indexing project progress.
 *
 * @since v6.1
 */
public class IndexProjectPage extends AbstractJiraPage
{

    private final String uri;

    @ElementBy (id = "project-reindex-form")
    PageElement form;

    @ElementBy (id = "acknowledge_submit")
    PageElement acknowledge;

    @ElementBy (id = "refresh_submit")
    PageElement refresh;

    @ElementBy (id = "cancel_reindex_submit")
    PageElement cancel;

    @ElementBy (id = "project-index-message")
    PageElement message;

    public IndexProjectPage()
    {
        this.uri = null;
    }

    public IndexProjectPage(final Long projectKey)
    {
        this.uri = String.format("/secure/project/IndexProject.jspa?pid=%d", projectKey);
    }

    @Override
    public TimedCondition isAt()
    {
        return form.timed().isPresent();
    }

    @Override
    public String getUrl()
    {
        if (uri == null)
        {
            throw new IllegalStateException("Use the constructor with the project id argument.");
        }
        return uri;
    }

    public TimedQuery<Boolean> isComplited()
    {
        return acknowledge.timed().isPresent();
    }

    @Deprecated
    public TimedQuery<Boolean> isBeingCanceled()
    {
        return message.timed().hasText("Re-Indexing is being cancelled.");
    }

    public TimedQuery<Boolean> isCancelledOrBeingCancelled()
    {
        return message.timed().isPresent();
    }

    public void acknowledge()
    {
        if(!isComplited().now()) {
            refresh.click();
        }
        Poller.waitUntilTrue(isComplited());
        acknowledge.click();
    }

    public void refresh()
    {
        refresh.click();
    }

    public void cancel()
    {
        cancel.click();
    }
}
