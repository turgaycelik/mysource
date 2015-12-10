package com.atlassian.jira.pageobjects.pages.admin;

import com.atlassian.jira.pageobjects.pages.AbstractJiraPage;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.inject.Inject;

import javax.annotation.Nullable;

public class DeleteGroupPage extends AbstractJiraPage
{
    private final String groupName;
    private final String atlToken;

    @Inject
    private PageBinder binder;

    @ElementBy (id = "group-delete-submit")
    private PageElement submitButton;

    @ElementBy (id = "group-delete-cancel")
    private PageElement cancelButton;

    @ElementBy(cssSelector = ".aui-message.error p")
    private Iterable<PageElement> errors;

    public DeleteGroupPage(final String groupName, @Nullable final String atlToken)
    {
        this.groupName = groupName;
        this.atlToken = atlToken;
    }

    public DeleteGroupPage(final String groupName)
    {
        this(groupName, null);
    }

    @Override
    public TimedCondition isAt()
    {
        // for some groups delete is blocked, but the only constant element is the "cancel"
        return cancelButton.timed().isPresent();
    }

    @Override
    public String getUrl()
    {
        if (atlToken != null)
        {
            return String.format("/secure/admin/user/DeleteGroup!default.jspa?atl_token=%s&name=%s", atlToken, groupName);
        }
        else
        {
            return String.format("/secure/admin/user/DeleteGroup!default.jspa?name=%s", groupName);
        }
    }

    public boolean canDelete()
    {
        return Iterables.isEmpty(errors) && submitButton.isPresent() && submitButton.isVisible();
    }

    public GroupBrowserPage submit()
    {
        if (canDelete())
        {
            submitButton.click();
            return pageBinder.bind(GroupBrowserPage.class);
        }
        throw new IllegalStateException("Cannot click the submit button.");
    }

    public Iterable<String> getErrorMessages()
    {
        return Iterables.transform(errors, new Function<PageElement, String>()
        {
            @Override
            public String apply(final PageElement errorElement)
            {
                return errorElement.getText();
            }
        });
    }
}
