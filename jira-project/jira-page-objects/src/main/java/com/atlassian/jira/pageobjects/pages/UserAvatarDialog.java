package com.atlassian.jira.pageobjects.pages;

import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import com.atlassian.pageobjects.elements.query.TimedQuery;
import com.google.common.collect.Lists;
import org.openqa.selenium.By;

import java.util.List;

import static com.atlassian.pageobjects.elements.query.Poller.waitUntilFalse;

/**
 *
 * @since v5.1
 */
public class UserAvatarDialog extends AbstractJiraPage
{
    @ElementBy (id = "user-avatar-picker")
    private PageElement dialog;

    @ElementBy (id = "aui-dialog-close")
    private PageElement closeButton;

    @Override
    public TimedCondition isAt()
    {
        return dialog.timed().isVisible();
    }

    public void close() {
        closeButton.click();
        waitUntilFalse(isOpen());
    }

    public TimedQuery<Boolean> isOpen()
    {
        return dialog.timed().isVisible();
    }

    @Override
    public String getUrl()
    {
        return null;
    }

    public List<String> getCustomAvatars() {
        final List<String> result = Lists.newArrayList();
        for(PageElement avatar : dialog.findAll(By.cssSelector("ul.jira-avatars li.jira-custom-avatar"))) {
            result.add(avatar.getAttribute("data-id"));
        }
        return result;
    }

}
