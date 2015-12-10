package com.atlassian.jira.pageobjects.pages.viewissue.watchers;

import com.atlassian.jira.pageobjects.components.fields.MultiSelect;
import com.atlassian.jira.pageobjects.util.TraceContext;
import com.atlassian.jira.pageobjects.util.Tracer;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.binder.Init;
import com.atlassian.pageobjects.binder.WaitUntil;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.google.common.base.Function;
import com.google.inject.Inject;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import static com.atlassian.pageobjects.elements.query.Poller.waitUntilTrue;

/**
 * TODO: Document this class / interface here
 *
 * @since v5.0
 */
public class WatchersComponent
{

    private MultiSelect watchersMultiSelect;

    @ElementBy (id = "inline-dialog-watchers")
    private PageElement watchersDialog;

    @ElementBy (id = "watchers-multi-select")
    private PageElement watcherWithSearch;

    @ElementBy (id = "watcher-data")
    private PageElement watcherCount;


    @ElementBy (id = "watchers-nosearch")
    private PageElement watchersNoSearch;

    @Inject
    private PageBinder binder;

    @Inject
    private TraceContext traceContext;

    @WaitUntil
    private void ready()
    {
        waitUntilTrue(watchersDialog.timed().isVisible());
    }

    @Init
    private void bindElements()
    {

        if (watcherWithSearch.isPresent())
        {
            watchersMultiSelect = binder.bind(MultiSelect.class, "watchers", new Function<String, By>()
            {
                @Override
                public By apply(@Nullable String itemName)
                {
                    //means find all items
                    if(itemName == null)
                    {
                        return By.cssSelector(".recipients li span img");
                    }
                    else
                    {
                        return By.cssSelector(".recipients li[title=\"" + itemName + "\"]");
                    }
                }
            });
        }
    }


    public WatchersComponent removeWatcher(String watcher) {
        Tracer tracer = traceContext.checkpoint();
        // need to execute javascript because it is only visible on :hover
        watchersDialog.find(By.cssSelector("li[data-username=" + watcher + "] .item-delete")).javascript().execute("jQuery(arguments[0]).click()");
        traceContext.waitFor(tracer, "jira.issue.watcher.deleted");
        return this;
    }

    public WatchersComponent addWatcher(String watcher) {

        if (watcherWithSearch.isPresent())
        {
            watchersMultiSelect.add(watcher);
        }
        else if (watchersNoSearch.isPresent())
        {
            watchersNoSearch.type(watcher).type(Keys.ENTER);
        }
        waitUntilTrue(watchersDialog.find(By.cssSelector("li[data-username=" + watcher + "]")).timed().isPresent());

        return this;
    }

    public boolean isReadOnly()
    {
        return !watchersDialog.find(By.className("item-delete")).isPresent() &&
                !watcherWithSearch.isPresent() &&
                !watchersNoSearch.isPresent();
    }

    public List<String> getWatchers() {
        final ArrayList<String> usernames = new ArrayList<String>();
        final List<PageElement> watchers = this.watchersDialog.findAll(By.cssSelector(".recipients li"));
        for (PageElement watcher : watchers)
        {
            usernames.add(watcher.getAttribute("data-username"));
        }
        return usernames;
    }


}
