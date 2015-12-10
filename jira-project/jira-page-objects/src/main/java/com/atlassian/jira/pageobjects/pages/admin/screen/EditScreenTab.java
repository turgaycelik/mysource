package com.atlassian.jira.pageobjects.pages.admin.screen;

import com.atlassian.jira.pageobjects.components.fields.SingleSelect;
import com.atlassian.jira.pageobjects.framework.actions.DragAndDrop;
import com.atlassian.jira.pageobjects.framework.elements.ExtendedElementFinder;
import com.atlassian.jira.pageobjects.framework.elements.PageElements;
import com.atlassian.jira.pageobjects.util.TraceContext;
import com.atlassian.jira.pageobjects.util.Tracer;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.binder.WaitUntil;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.PageElementFinder;
import com.atlassian.pageobjects.elements.query.Conditions;
import com.atlassian.pageobjects.elements.query.Poller;
import com.atlassian.pageobjects.elements.query.Queries;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import com.atlassian.pageobjects.elements.query.TimedQuery;
import com.atlassian.pageobjects.elements.timeout.Timeouts;
import com.atlassian.webdriver.AtlassianWebDriver;
import org.openqa.selenium.By;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

import static com.atlassian.jira.functest.framework.matchers.IterableMatchers.iterableWithSize;
import static com.atlassian.pageobjects.elements.query.Poller.waitUntilTrue;

public class EditScreenTab
{

    @Inject protected AtlassianWebDriver driver;
    @Inject protected PageBinder pageBinder;
    @Inject protected PageElementFinder elementFinder;
    @Inject protected ExtendedElementFinder extendedFinder;
    @Inject protected TraceContext traceContext;
    @Inject protected Timeouts timeouts;
    @Inject protected DragAndDrop dragAndDrop;

    protected final long tabId;

    protected SingleSelect fieldPicker;
    protected PageElement table;
    protected PageElement deleteTab;
    protected PageElement tab;

    public EditScreenTab(final long tabId)
    {
        this.tabId = tabId;
    }

    @WaitUntil
    public void isAt()
    {
        // TODO wrong place to initialize stuff
        table = elementFinder.find(By.id("tab-" + tabId));
        Poller.waitUntilTrue(table.timed().isPresent());
        Poller.waitUntilFalse(table.timed().hasClass("loading"));
        fieldPicker = pageBinder.bind(SingleSelect.class, elementFinder.find(By.className("available-fields")));
        tab = elementFinder.find(By.cssSelector(".menu-item[data-tab='" + tabId + "']"));
        tab.javascript().execute("jQuery(arguments[0]).addClass('wd-activate-hover')");
        deleteTab = tab.find(By.className("delete-tab"));
    }

    // TODO should be timed queries

    public int getFieldCount()
    {
        return elementFinder.findAll(By.cssSelector(".aui-restfultable-readonly")).size();
    }

    public String getName()
    {
        return elementFinder.find(By.cssSelector(".menu-item[data-tab='" + tabId + "']")).getAttribute("data-name");
    }


    // TODO Field should be a page object itself, or rather this class should be using a restfultable page object

    /**
     *
     *
     * @return list of field names at any given moment
     * @deprecated use {@link #getFieldNames()} as this method is prone to return wrong results due to race
     * conditions. Using {@link #getFieldNames()}} will enforce clients to execute timed assertions and improve
     * reliability of the tests
     */
    @Deprecated
    public List<String> getFields()
    {
        final List<String> fields = new ArrayList<String>();
        final List<PageElement> fieldEls = elementFinder.findAll(By.cssSelector(".aui-restfultable-readonly"));
        for (PageElement fieldEl : fieldEls)
        {
            fields.add(fieldEl.getAttribute("data-name"));
        }
        return fields;
    }

    public TimedQuery<Iterable<String>> getFieldNames()
    {
        return Queries.forSupplier(timeouts, extendedFinder.newQuery(By.className("aui-restfultable-readonly"))
                .transform(PageElements.getAttribute("data-name"))
                .supplier());
    }

    public SingleSelect getFieldsPicker()
    {
        return fieldPicker;
    }


    public TimedCondition hasField(String field)
    {
        return Conditions.forMatcher(Queries.forSupplier(timeouts, queryForRow(field).supplier()),
                iterableWithSize(1, PageElement.class));
    }


    /**
     * Will only work if {@link #hasField(String)} evaluates to <code>true</code>.
     *
     * @param field field to find the row for
     * @return page element representing the row
     */
    protected PageElement findRow(String field)
    {
        waitUntilTrue(hasField(field));
        return queryForRow(field).searchOne();
    }

    private ExtendedElementFinder.QueryBuilder<PageElement> queryForRow(String field)
    {
        return extendedFinder.newQuery(By.className("aui-restfultable-row"))
                .filter(PageElements.hasDataAttribute("name", field));
    }

    public void rename(String name) {
        final Tracer checkpoint = traceContext.checkpoint();
        elementFinder.find(By.cssSelector(".tab-edit[data-tab='" + tabId + "']")).click();
        if (name.length() == 0) {
            tab.find(By.className("tab-name")).clear();
        } else {
            tab.find(By.className("tab-name")).type(name);
        }

        tab.find(By.className("aui-button")).click();
        traceContext.waitFor(checkpoint, "tab.rename.complete");
    }

    public String renameExpectingError(String name)
    {
        rename(name);
        final PageElement error = elementFinder.find(By.id("inline-dialog-rename-error")).find(By.className("error"));
        waitUntilTrue(error.timed().isPresent());
        return error.getText();
    }


    public EditScreenTab removeField(String field)
    {
        final PageElement row = findRow(field);
        row.javascript().execute("jQuery(arguments[0]).addClass('active')"); // hacking css :hover
        row.find(By.className("aui-restfultable-delete")).click();
        Poller.waitUntilFalse(hasField(field));
        return this;
    }



    public EditScreenTab addField(String field)
    {
        fieldPicker.select(field);
        Poller.waitUntilTrue(hasField(field));
        return this;
    }

    public void removeTab()
    {
        deleteTab.click();
        final DeleteTabDialog dialog = pageBinder.bind(DeleteTabDialog.class, deleteTab.getAttribute("data-tab"));
        dialog.submit();
        Poller.waitUntilFalse(table.timed().isPresent());
    }


    public void moveFieldBelow(final String field, final String target)
    {
        dragAndDrop.dragAndDrop(getDragHandle(field)).below(getDragHandle(target)).execute();
    }
    public void moveFieldAbove(final String field, final String target)
    {
        dragAndDrop.dragAndDrop(getDragHandle(field)).to(getDragHandle(target)).execute();
    }

    private PageElement getDragHandle(String field)
    {
        return findRow(field).find(By.className("aui-restfultable-draghandle"));
    }

}
