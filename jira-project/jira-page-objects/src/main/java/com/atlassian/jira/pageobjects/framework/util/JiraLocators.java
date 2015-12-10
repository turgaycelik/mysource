package com.atlassian.jira.pageobjects.framework.util;

import com.atlassian.jira.pageobjects.framework.elements.PageElements;
import com.atlassian.webdriver.utils.by.ByDataAttribute;
import org.openqa.selenium.By;

/**
 * JIRA-specific 'By' locators
 *
 * @since v5.1
 */
public final class JiraLocators
{

    public static final String CLASS_AUI_MESSAGE = "aui-message";

    public static final String DATA_CELL_TYPE = "cell-type";

    private JiraLocators()
    {
        throw new AssertionError("Don't instantiate me");
    }

    public static By body()
    {
        return By.tagName(PageElements.BODY);
    }

    /**
     * To locate cells marked with 'data-cell-type' attribute.
     *
     * @param cellType cell type to find
     * @return locator
     */
    public static By byCellType(String cellType)
    {
        return ByDataAttribute.byTagAndData(PageElements.TD, DATA_CELL_TYPE, cellType);
    }

}
