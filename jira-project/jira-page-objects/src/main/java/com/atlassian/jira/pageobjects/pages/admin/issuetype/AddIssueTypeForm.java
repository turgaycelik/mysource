package com.atlassian.jira.pageobjects.pages.admin.issuetype;

import com.atlassian.jira.pageobjects.components.IconPicker;
import com.atlassian.jira.pageobjects.form.FormUtils;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.binder.Init;
import com.atlassian.pageobjects.binder.WaitUntil;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.PageElementFinder;
import com.atlassian.pageobjects.elements.query.TimedCondition;

import org.hamcrest.Matchers;
import org.openqa.selenium.By;

import javax.inject.Inject;
import java.util.Map;

import static com.atlassian.jira.pageobjects.form.FormUtils.setElement;
import static com.atlassian.pageobjects.elements.query.Poller.waitUntilTrue;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Represents the add issue type form.
 *
 * @since v5.0.1
 */
public class AddIssueTypeForm
{
    private final By formLocator;
    private PageElement formElement;
    
    @Inject
    private PageElementFinder locator;

    @Inject
    private PageBinder pageBinder;
    
    private PageElement nameElement;
    private PageElement descriptionElement;
    private PageElement addButton;
    private PageElement issueTypeRadio;
    private PageElement subtaskRadio;
    private IconPicker iconPicker;
    private PageElement cancelLink;

    public AddIssueTypeForm()
    {
        this.formLocator = By.id("add-issue-type-form");
    }

    @Init
    public void init()
    {
        formElement = locator.find(formLocator);
        nameElement = formElement.find(By.name("name"));
        descriptionElement = formElement.find(By.name("description"));
        addButton = formElement.find(By.name("Add"));
        issueTypeRadio = formElement.find(By.cssSelector("input.radio[value='']"));
        subtaskRadio = formElement.find(By.cssSelector("input.radio[value='jira_subtask']"));
        cancelLink = formElement.find(By.cssSelector("a.cancel"));
        iconPicker = pageBinder.bind(IconPicker.class, "issue-type-icon-picker");
    }

    @WaitUntil
    void waitForForm()
    {
        waitUntilTrue(isAt());
    }

    TimedCondition isAt()
    {
        return locator.find(formLocator).timed().isPresent();
    }

    AddIssueTypeForm setName(String name)
    {
        setElement(nameElement, name);
        return this;
    }

    AddIssueTypeForm setDescription(String name)
    {
        setElement(descriptionElement, name);
        return this;
    }

    @Deprecated
    AddIssueTypeForm setIconUrl(String iconUrl)
    {
        iconPicker.setIconUrl(iconUrl);
        return this;
    }

    AddIssueTypeForm setSubtask(boolean subtask)
    {
        assertSubstasksEnabled();

        if (subtask)
        {
            subtaskRadio.click();
        }
        else
        {
            issueTypeRadio.click();
        }
        return this;
    }

    @Deprecated
    String getIconUrl()
    {
        return iconPicker.getIconUrl();
    }
    
    boolean isSubtasksEnabled()
    {
        return issueTypeRadio.isPresent() && subtaskRadio.isPresent();
    }

    @Deprecated
    IconPicker.IconPickerPopup openIconPickerPopup()
    {
        return iconPicker.openPopup();
    }

    void submit()
    {
        addButton.click();
    }

    void cancel()
    {
        cancelLink.click();
    }

    private void assertSubstasksEnabled()
    {
        assertThat(isSubtasksEnabled(), is(true));
    }

    Map<String, String> getFormErrors()
    {
        return FormUtils.getAuiFormErrors(formElement);
    }
}
