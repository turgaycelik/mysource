package com.atlassian.jira.pageobjects.components;

import com.atlassian.jira.pageobjects.pages.admin.screen.EditScreenTab;
import com.atlassian.jira.pageobjects.util.TraceContext;
import com.atlassian.jira.pageobjects.util.Tracer;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.binder.Init;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.PageElementFinder;
import com.atlassian.pageobjects.elements.query.Poller;
import com.atlassian.pageobjects.elements.query.TimedQuery;
import com.atlassian.webdriver.AtlassianWebDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;

import static com.atlassian.pageobjects.elements.query.Poller.waitUntilTrue;

/**
 *
 * @since v6.2
 */
public class ScreenEditor
{
    @Inject
    private PageElementFinder elementFinder;

    @Inject
    private PageBinder pageBinder;

    @Inject
    private TraceContext traceContext;

    @Inject
    private AtlassianWebDriver driver;

    private PageElement table;

    @Init
    public void initialize()
    {
        table = elementFinder.find(By.className("fields-table"));
        waitUntilTrue(isPresent());
    }

    public TimedQuery<Boolean> isPresent()
    {
        return table.timed().isPresent();
    }

    public EditScreenTab getCurrentTab() {
        return pageBinder.bind(EditScreenTab.class, Long.parseLong(table.getAttribute("data-id"), 10));
    }

    public EditScreenTab openTab(String tabName) {
        final PageElement pageElement = elementFinder.find(By.cssSelector(".menu-item[data-name='" + tabName + "']")).click();
        Poller.waitUntilTrue(pageElement.timed().hasClass("active-tab"));
        return pageBinder.bind(EditScreenTab.class, Long.parseLong(pageElement.getAttribute("data-tab")));
    }

    public EditScreenTab addTab(String tab)
    {
        AddTabDialog addTabDialog = pageBinder.bind(AddTabDialog.class, elementFinder.find(By.className("add-tab")), "add-tab");
        return addTabDialog.addTabSuccess(tab);
    }


    public String addTabExpectingFail(String tab)
    {
        AddTabDialog addTabDialog = pageBinder.bind(AddTabDialog.class, elementFinder.find(By.className("add-tab")), "add-tab");
        return addTabDialog.addTabFail(tab);
    }


    public List<String> getTabs()
    {
        final List<String> tabs = new ArrayList<String>();
        final List<PageElement> tabEls = elementFinder.findAll(By.cssSelector(".screen-editor .tabs-menu .menu-item"));
        for (PageElement tabEl : tabEls)
        {
            tabs.add(tabEl.getAttribute("data-name"));
        }
        return tabs;
    }

    public void moveTabAfter(final String tab, final String target)
    {
        Tracer tracer = traceContext.checkpoint();

        final WebElement sourceDragHandle = getDragHandle(tab);
        final WebElement targetDragHandle = getDragHandle(target);

        final WebElement targetTabElement = driver.findElement(By.cssSelector(".menu-item[data-name='" + target + "']"));
        int widthOffset = (int) (targetTabElement.getSize().getWidth() / 2.0);

        Point sourceLocation = sourceDragHandle.getLocation();
        Point targetLocation = targetDragHandle.getLocation();

        Actions action = new Actions(driver).dragAndDropBy(sourceDragHandle, targetLocation.x - sourceLocation.x + widthOffset, 0);
        action.perform();

        traceContext.waitFor(tracer, "screen.tab.order.updated");
    }

    private WebElement getDragHandle(String tab)
    {
        final PageElement tabEl = elementFinder.find(By.cssSelector(".menu-item[data-name='" + tab + "']"));
        tabEl.javascript().execute("jQuery(arguments[0]).addClass('wd-activate-hover')"); // hacking css :hover
        return driver.findElement(By.cssSelector(".menu-item[data-name='" + tab + "'] .tab-draghandle"));
    }


    public static class AddTabDialog extends InlineDialog {

        @Inject
        PageElementFinder elementFinder;

        @Inject
        PageBinder pageBinder;

        public AddTabDialog(final PageElement trigger, final String contentsId)
        {
            super(trigger, contentsId);
        }

        @Override
        public AddTabDialog open()
        {
            super.open();
            return this;
        }

        @Override
        public AddTabDialog close()
        {
            super.close();
            return this;
        }


        public EditScreenTab addTabSuccess(String tabName)
        {
            addTab(tabName);
            final PageElement pageElement = elementFinder.find(By.cssSelector(".menu-item[data-name='" + tabName + "'].active-tab"));
            return pageBinder.bind(EditScreenTab.class, Long.parseLong(pageElement.getAttribute("data-tab")));

        }

        private void addTab(String tabName)
        {
            this.open();
            this.getDialogContents().find(By.name("name")).type(tabName);
            this.getDialogContents().find(By.cssSelector("input[type='submit']")).click();
        }

        public String addTabFail(String tabName)
        {
            addTab(tabName);
            final PageElement errorEl = this.getDialogContents().find(By.className("error"));
            waitUntilTrue(errorEl.timed().isPresent());
            return  errorEl.getText();
        }
    }
}
