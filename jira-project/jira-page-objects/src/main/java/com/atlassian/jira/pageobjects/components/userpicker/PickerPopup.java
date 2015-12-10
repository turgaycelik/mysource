package com.atlassian.jira.pageobjects.components.userpicker;

import com.atlassian.jira.pageobjects.JiraTestedProduct;
import com.atlassian.jira.pageobjects.WindowSession;
import com.atlassian.jira.pageobjects.framework.elements.ExtendedElementFinder;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.binder.Init;
import com.atlassian.pageobjects.elements.CheckboxElement;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.Conditions;
import com.atlassian.pageobjects.elements.query.Poller;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import com.atlassian.pageobjects.elements.query.TimedQuery;
import com.google.common.collect.ImmutableList;
import org.openqa.selenium.By;

import javax.inject.Inject;
import java.util.List;

import static com.atlassian.jira.pageobjects.framework.elements.PageElements.hasDataAttribute;
import static com.atlassian.jira.pageobjects.framework.elements.PageElements.isVisible;
import static com.atlassian.pageobjects.elements.query.Poller.waitUntilFalse;
import static com.atlassian.pageobjects.elements.query.Poller.waitUntilTrue;
import static com.atlassian.jira.util.dbc.Assertions.stateTrue;

/**
 * Popup dialog associated with {@link LegacyPicker}.
 *
 * @since v5.0
 */
public class PickerPopup<R extends PickerPopup.PickerRow>
{

    @Inject protected PageBinder pageBinder;
    @Inject protected JiraTestedProduct jira;
    @Inject protected ExtendedElementFinder extendedFinder;

    @ElementBy(name = "selectorform")
    protected PageElement selectorForm;

    @ElementBy(name = "multiSelect")
    protected PageElement isMultiselect;

    @ElementBy(tagName = "table")
    private PageElement table;


    private WindowSession.BrowserWindow popupWindow;

    private final PickerType pickerType;
    private final Class<R> rowClass;
    private final LegacyTriggerPicker parent;

    public PickerPopup(LegacyTriggerPicker parent, PickerType pickerType, Class<R> rowClass)
    {
        this.parent = parent;
        this.pickerType = pickerType;
        this.rowClass = rowClass;
    }

    @Init
    private void init()
    {
        popupWindow = jira.windowSession().getWindow(pickerType.windowName());
    }

    /**
     * Opens the popup and switches the focus to the popup's window
     *
     * @return this popup instance
     */
    public PickerPopup<R> open()
    {
        if (!isOpen().now())
        {
            parent.getTrigger().click();
            Poller.waitUntilTrue(isOpen());
        }
        switchToPopupWindow();
        return this;
    }


    public TimedCondition isOpen()
    {
        return popupWindow.isOpen();
    }

    public TimedQuery<Boolean> isClosed()
    {
        return Conditions.not(popupWindow.isOpen());
    }

    public WindowSession.BrowserWindow getPopupWindow()
    {
        return popupWindow;
    }

    public PickerPopup doInPopup(Runnable runnable)
    {
        popupWindow.doInWindow(runnable);
        return this;
    }

    public void switchToPopupWindow()
    {
        popupWindow.switchTo();
    }

    public void switchBack()
    {
        popupWindow.switchBack();
    }

    public boolean isMultiselect()
    {
        return Boolean.TRUE.toString().equals(isMultiselect.getValue());
    }

    private void checkIsMultiselect()
    {
        stateTrue("Not a multi-select picker", isMultiselect());
    }

    public MultiSelect multiSelect()
    {
        return new MultiSelect();
    }

    public Iterable<R> getAllRows()
    {
        ImmutableList.Builder<R> builder = ImmutableList.builder();
        for (PageElement rowElement : allRowElements())
        {
            builder.add(createRow(rowElement));
        }
        return builder.build();
    }

    public R getRowByIndex(int index)
    {
        return createRow(allRowElements().get(index));
    }

    private List<PageElement> allRowElements()
    {
        return extendedFinder.within(table).findAll(By.tagName("tr"), hasDataAttribute("row-for"));
    }

    protected R createRow(PageElement rowElement)
    {
        return pageBinder.bind(rowClass, this, rowElement);
    }

    public final class MultiSelect
    {
        private final CheckboxElement selectAll;
        private final PageElement submit;

        private MultiSelect()
        {
            checkIsMultiselect();
            this.selectAll = selectorForm.find(By.name("all"), CheckboxElement.class);
            this.submit = selectorForm.find(By.id("multiselect-submit"));
        }


        public CheckboxElement getSelectAllCheckbox()
        {
            return selectAll;
        }

        public MultiSelect selectAll()
        {
            if (selectAll.isSelected())
            {
                selectAll.click();
            }
            // our lovely JS expects a click!
            selectAll.click();
            for (R row : getAllRows())
            {
                waitUntilTrue(row.getSelect().timed().isSelected());
            }
            return this;
        }

        public MultiSelect deselectAll()
        {
            if (!selectAll.isSelected())
            {
                selectAll.click();
            }
            // our lovely JS expects a click!
            selectAll.click();
            for (R row : getAllRows())
            {
                waitUntilFalse(row.getSelect().timed().isSelected());
            }
            return this;
        }

        public LegacyTriggerPicker submitSelect()
        {
            submit.click();
            switchBack();
            // we might want to rebind it...
            return parent;
        }
    }

    public static class PickerRow<S extends PickerRow<S>>
    {
        @Inject protected ExtendedElementFinder extendedFinder;

        protected final PickerPopup<S> owner;
        protected final PageElement rowElement;

        public PickerRow(PickerPopup<S> owner, PageElement rowElement)
        {
            this.owner = owner;
            this.rowElement = rowElement;
        }

        CheckboxElement getSelect()
        {
            owner.checkIsMultiselect();
            return rowElement.find(By.name("userchecks"), CheckboxElement.class);
        }

        /**
         * Selects the row. Depending on the mode it will either select this single row and close the popup
         * (single mode), or just check the checkbox associated with this row (multi-mode).
         *
         */
        public void select()
        {
            if (owner.isMultiselect())
            {
                getSelect().check();
            }
            else
            {
                // click first available td
                extendedFinder.within(rowElement).find(By.tagName("td"), isVisible()).click();
                owner.switchBack();
            }
        }

    }




}
