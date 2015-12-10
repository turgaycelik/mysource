package com.atlassian.jira.webtest.webdriver.selenium;

import com.atlassian.jira.pageobjects.JiraTestedProduct;
import com.atlassian.pageobjects.elements.CheckboxElement;
import com.atlassian.pageobjects.elements.Options;
import com.atlassian.pageobjects.elements.PageElementFinder;
import com.atlassian.pageobjects.elements.SelectElement;
import com.atlassian.pageobjects.elements.WebDriverElement;
import com.atlassian.webdriver.AtlassianWebDriver;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import org.apache.commons.lang.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import javax.annotation.Nullable;

import static com.atlassian.jira.webtest.webdriver.selenium.PseudoAssertThat.getBy;

/**
 * Simple class to make it easier to translate selenium tests to webdriver.
 *
 * @since v5.1
 */
public class PseudoSeleniumClient
{
    private final AtlassianWebDriver driver;
    private final PageElementFinder pageElementFinder;

    public PseudoSeleniumClient(JiraTestedProduct jira, PageElementFinder pageElementFinder)
    {
        this.driver = jira.getTester().getDriver();
        this.pageElementFinder = pageElementFinder;
    }

    public void click(String element)
    {
        click(element, false);
    }

    public void click(String element, boolean wait)
    {
        click(getBy(element), wait);
    }

    public void click(By by)
    {
        click(by, false);
    }

    public void click(By by, boolean waitForPageLoad)
    {
        driver.findElement(by).click();
        if (waitForPageLoad)
        {
            waitForPageToLoad();
        }
    }

    public void type(String element, CharSequence text)
    {
        driver.findElement(getBy(element)).sendKeys(text);
    }

    public WebElement getElement(String element)
    {
        return driver.findElement(getBy(element));
    }

    public String getAttribute(String element)
    {
        return getElement(StringUtils.substringBefore(element, "@")).getAttribute(StringUtils.substringAfter(element, "@"));
    }

    public void open(String url)
    {
        driver.navigate().to(url);
    }

    public void waitForPageToLoad()
    {
        driver.waitUntilElementIsLocated(By.id("footer"));
    }

    public void check(String element, final String value)
    {
        Iterables.getOnlyElement(Iterables.filter(pageElementFinder.findAll(getBy(element), CheckboxElement.class), new Predicate<CheckboxElement>()
        {
            @Override
            public boolean apply(@Nullable CheckboxElement input)
            {
                return StringUtils.equals(value, input.getAttribute("value"));
            }
        })).check();
    }

    public void select(String element, final String option)
    {
        final SelectElement select = pageElementFinder.findAll(getBy(element), SelectElement.class).get(0);
        select.select(Options.text(option));
    }

    public void setTextField(String element, final String value)
    {
        final WebDriverElement textField = pageElementFinder.findAll(getBy(element), WebDriverElement.class).get(0);
        textField.clear().type(value).click();
    }

    public void selectOptionFromAutocompleteTextField(String element, String option)
    {
        final WebDriverElement textField = pageElementFinder.findAll(getBy(element), WebDriverElement.class).get(0);
        textField.clear().type(option);
        driver.waitUntilElementIsVisible(By.className("aui-list-item-link"));
        pageElementFinder.find(By.className("aui-list-item-link")).click();
    }

    public void switchToFrame(String id)
    {
        driver.switchTo().frame(id);
    }

    public void switchToDefaultContent()
    {
        driver.switchTo().defaultContent();
    }

}
