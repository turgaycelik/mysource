package com.atlassian.jira.pageobjects.components;

import com.atlassian.pageobjects.elements.PageElement;

import org.openqa.selenium.By;

/**
 * An abstraction over JIRA's AJS.Dropdown JavaScript control.
 */
public class JiraAjsDropdown extends DropDown
{
    private final String triggerButtonId;

    public JiraAjsDropdown(final String triggerButtonId)
    {
        super(By.id(triggerButtonId), By.id(triggerButtonId + "_drop"));
        this.triggerButtonId = triggerButtonId;
    }

    @Override
    protected PageElement dropDown()
    {
        if (super.dropDown().isPresent())
        {
            return super.dropDown();
        }

        return elementFinder.find(By.cssSelector("#" + triggerButtonId + " + .aui-list"));
    }
}
