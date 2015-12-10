package com.atlassian.jira.webtest.webdriver.selenium;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;

import javax.annotation.Nullable;

/**
 * Check if HTML source contains the text.
 *
 * @since v5.1
 */
public class PageContainsCondition implements ExpectedCondition<Boolean>
{
    private String text;

    public PageContainsCondition(String text) {
        this.text = text;
    }

    @Override
    public Boolean apply(@Nullable WebDriver input)
    {
        return input.getPageSource().contains(text);
    }
}
