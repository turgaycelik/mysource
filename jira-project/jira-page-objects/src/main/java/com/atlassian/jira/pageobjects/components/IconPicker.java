package com.atlassian.jira.pageobjects.components;

import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.binder.Init;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.PageElementFinder;
import com.atlassian.webdriver.AtlassianWebDriver;
import org.apache.commons.lang.StringUtils;
import org.openqa.selenium.By;

import javax.inject.Inject;
import java.util.List;

/**
 * Drives the icon picker. 
 *
 * @since v5.0.1
 */
public class IconPicker
{
    @Inject
    private PageElementFinder finder;

    @Inject
    private PageBinder binder;

    private final String id;

    private PageElement iconUrlElement;
    private PageElement triggerElement;
    
    public IconPicker(String id)
    {
        this.id = id;
    }

    @Init
    public void init()
    {
        iconUrlElement = finder.find(By.id(id));
        triggerElement = finder.find(By.id(id + "-trigger"));
    }

    public String getIconUrl()
    {
        return iconUrlElement.getValue();
    }
    
    public IconPicker setIconUrl(String url)
    {
        iconUrlElement.clear();
        if (StringUtils.isNotBlank(url))
        {
            iconUrlElement.type(url);
        }
        return this;
    }

    public IconPickerPopup openPopup()
    {
        triggerElement.click();
        return binder.bind(IconPickerPopup.class);
    }

    public static class IconPickerPopup
    {
        public static final String ICON_PICKER_WINDOW = "IconPicker";

        @Inject
        private AtlassianWebDriver driver; 
        
        @Inject
        private PageElementFinder finder;

        private PageElement table;
        private PageElement iconInput;
        private PageElement updateButton;
        private String origHandle;
        
        @Init
        public void init()
        {
            origHandle = driver.getWindowHandle();
            driver.switchTo().window(ICON_PICKER_WINDOW);
            
            table = finder.find(By.id("icon-picker-table"));
            iconInput = finder.find(By.name("iconurl"));
            updateButton = finder.find(By.id("icon-picker-submit"));
        }
        
        public boolean selectIcon(String icon)
        {
            final List<PageElement> iconRows = table.findAll(By.cssSelector("tbody tr"));
            for (PageElement iconRow : iconRows)
            {
                final PageElement img = iconRow.find(By.tagName("img"));
                if (StringUtils.contains(img.getAttribute("src"), icon))
                {
                    iconRow.click();
                    driver.switchTo().window(origHandle);
                    return true;
                }
            }
            return false;
        }
        
        public void submitIconUrl(String url)
        {
            iconInput.clear();
            if (StringUtils.isNotBlank(url))
            {
                iconInput.type(url);
            }
            updateButton.click();
            driver.switchTo().window(origHandle);
        }
    }
}
