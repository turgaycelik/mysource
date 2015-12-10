package com.atlassian.jira.pageobjects.pages.viewissue;

import java.util.ArrayList;
import java.util.List;

import com.atlassian.pageobjects.binder.WaitUntil;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;

import org.openqa.selenium.By;

import static com.atlassian.pageobjects.elements.query.Poller.waitUntilTrue;

public class VotersComponent
{
    @ElementBy (id = "inline-dialog-voters")
    private PageElement votersDialog;

    @WaitUntil
    private void ready()
    {
        waitUntilTrue(votersDialog.timed().isVisible());
    }

    public List<String> getVoters()
    {
        final ArrayList<String> usernames = new ArrayList<String>();
        final List<PageElement> watchers = this.votersDialog.findAll(By.cssSelector(".recipients li"));
        for (PageElement watcher : watchers)
        {
            usernames.add(watcher.getAttribute("data-username"));
        }
        return usernames;
    }

    public boolean isShowingEmptyMessage(){
        PageElement message = votersDialog.find(By.className("aui-message"));
        return message.isPresent();
    }
}
