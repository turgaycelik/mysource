package com.atlassian.jira.webtest.webdriver.selenium;

import com.atlassian.webdriver.AtlassianWebDriver;
import com.atlassian.webdriver.utils.by.ByJquery;
import com.atlassian.webdriver.utils.element.ElementIsVisible;
import com.atlassian.webdriver.utils.element.ElementLocated;
import com.atlassian.webdriver.utils.element.ElementNotLocated;
import com.atlassian.webdriver.utils.element.ElementNotVisible;
import org.apache.commons.lang.StringUtils;
import org.openqa.selenium.By;

/**
* Simple class to make it easier to translate selenium tests to webdriver.
*
* @since v5.1
*/
public class PseudoAssertThat
{
    private final AtlassianWebDriver driver;
    public static final int DROP_DOWN_WAIT = 5000;

    static By getBy(String element) {
        if (StringUtils.startsWith(element, "name=")) {
            return By.name(StringUtils.removeStart(element, "name="));
        }
        if (StringUtils.startsWith(element, "jquery=")) {
            return ByJquery.$(StringUtils.removeStart(element, "jquery="));
        }
        if (StringUtils.startsWith(element, "//")) {
            return By.xpath(element);
        }
        if (StringUtils.startsWith(element, "id=")) {
            return By.id(StringUtils.remove(element, "id="));
        }
        if (StringUtils.startsWith(element, "css=")) {
            return By.cssSelector(StringUtils.remove(element, "css="));
        }
        return By.id(element);
    }

    public PseudoAssertThat(AtlassianWebDriver driver) {
        this.driver = driver;
    }

    public void visibleByTimeout(String element, int timeout) {
        driver.waitUntil(new ElementIsVisible(getBy(element), null), timeout);
    }

    public void notVisibleByTimeout(String element, int timeout) {
        driver.waitUntil(new ElementNotVisible(getBy(element), null), timeout);
    }

    public void elementNotPresentByTimeout(String element, int timeout) {
        driver.waitUntil(new ElementNotLocated(getBy(element), null), timeout);
    }

    public void elementPresentByTimeout(String element, int timeout) {
        elementPresentByTimeout(getBy(element), timeout);
    }

    public void elementPresent(String element) {
        driver.waitUntil(new ElementLocated(getBy(element)));
    }

    public void elementPresentByTimeout(By by, int timeout) {
        driver.waitUntil(new ElementLocated(by, null), timeout);
    }

    public void elementNotPresent(String element) {
        driver.waitUntil(new ElementNotLocated(getBy(element), null));
    }

    public void textPresentByTimeout(String text, int timeout) {
        driver.waitUntil(new PageContainsCondition(text), timeout);
    }

    public void textPresent(String text) {
        driver.waitUntil(new PageContainsCondition(text));
    }
}
