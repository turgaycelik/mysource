package com.atlassian.jira.pageobjects.components.restfultable;

import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.binder.Init;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.PageElementFinder;
import org.openqa.selenium.By;

import javax.inject.Inject;
import java.util.List;

/**
 * @since v4.4
 */
public class AbstractEditRow
{

    @Inject
    private PageBinder pageBinder;

    @Inject
    private PageElementFinder finder;

    protected PageElement row;

    private final By rowSelector;

    public AbstractEditRow(final By rowSelector)
    {
        this.rowSelector = rowSelector;
    }

    protected PageElement findInRow(final String cssSelector)
    {
        return row.find(By.cssSelector(cssSelector));
    }

    protected PageElement getAddButton()
    {
        // if I use the row to find the button it can't find it. strange!
        return findInRow("input[type=submit]");
    }

    protected PageElement getCancelLink()
    {
        return findInRow(".aui-restfultable-operations").find(By.xpath("a[text()='Cancel']"));
        // TODO AUI-1072, use a better selector:
        // return findInRow(".aui-restfultable-cancel");
    }

    @Init
    public void setRow()
    {
        row = finder.find(rowSelector);
    }

    public static class AbstractField
    {
        protected PageElement cell;

        public AbstractField(PageElement cell)
        {
            this.cell = cell;
        }

        public String getError()
        {
            final PageElement error = cell.find(By.className("error"));

            if (error.isPresent())
            {
                return error.getText();
            }
            else
            {
                return null;
            }
        }
    }

    public static class Field extends AbstractField
    {
        private PageElement field;

        public Field(PageElement cell)
        {
            super(cell);
            this.field = cell.find(By.tagName("input"));
        }

        public String value()
        {
            return field.getValue();
        }

        public Field type(String value)
        {
            field.type(value);
            return this;
        }

        public Field clear()
        {
            field.clear();
            return this;
        }
    }


    public static class SelectField extends AbstractField
    {
        private PageElement field;

        public SelectField(PageElement cell)
        {
            super(cell);
            this.field = cell.find(By.tagName("select"));
        }


        public SelectField select(final String label)
        {
            List<PageElement> options = field.findAll(By.tagName("option"));

            for (PageElement option : options)
            {
                if (option.getText().contains(label))
                {
                    option.select();
                    break;
                }
            }

            return this;
        }

        public SelectField selectByValue(final String value)
        {
            List<PageElement> options = field.findAll(By.tagName("option"));

            for (PageElement option : options)
            {
                if (option.getValue().equals(value))
                {
                    option.select();
                    break;
                }
            }

            return this;
        }
    }
}
