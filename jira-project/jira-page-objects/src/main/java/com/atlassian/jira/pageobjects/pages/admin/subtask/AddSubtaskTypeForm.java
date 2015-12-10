package com.atlassian.jira.pageobjects.pages.admin.subtask;

import com.atlassian.jira.pageobjects.components.IconPicker;
import com.atlassian.jira.pageobjects.form.FormUtils;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.binder.Init;
import com.atlassian.pageobjects.binder.WaitUntil;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.PageElementFinder;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import org.openqa.selenium.By;

import javax.inject.Inject;
import java.util.Map;

import static com.atlassian.pageobjects.elements.query.Poller.waitUntilTrue;

/**
 * @since v5.0.1
 */
public class AddSubtaskTypeForm
{
    @Inject 
    private PageElementFinder locator;

    @Inject
    private PageBinder binder;

    private final By formLocator;
    private PageElement nameElement;
    private PageElement descriptionElement;
    private PageElement addButton;
    private PageElement cancelLink;
    private IconPicker iconPicker;
    private PageElement formElement;

    public AddSubtaskTypeForm()
    {
        formLocator = By.id("add-subtask-issue-type-form");
    }

    @WaitUntil
    public void waitUntilForm()
    {
        waitUntilTrue(isAt());
    }

    TimedCondition isAt()
    {
        return locator.find(formLocator).timed().isPresent();
    }

    @Init
    public void init()
    {
        formElement = locator.find(formLocator);
        nameElement = formElement.find(By.name("name"));
        descriptionElement = formElement.find(By.name("description"));
        addButton = formElement.find(By.name("Add"));
        cancelLink = formElement.find(By.cssSelector("a.cancel"));
        iconPicker = binder.bind(IconPicker.class, "subtask-type-icon-picker");
    }

    AddSubtaskTypeForm setName(String name)
    {
        FormUtils.setElement(nameElement, name);
        return this;
    }

    AddSubtaskTypeForm setDescription(String name)
    {
        FormUtils.setElement(descriptionElement, name);
        return this;
    }

    AddSubtaskTypeForm setIconUrl(String iconUrl)
    {
        iconPicker.setIconUrl(iconUrl);
        return this;
    }

    String getIconUrl()
    {
        return iconPicker.getIconUrl();
    }

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
    
    Map<String, String> getErrors()
    {
        return FormUtils.getAuiFormErrors(formElement);
    }
}
