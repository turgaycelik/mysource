package com.atlassian.jira.pageobjects.navigator;

import com.atlassian.jira.pageobjects.pages.AbstractJiraPage;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import org.apache.commons.lang.StringUtils;
import org.openqa.selenium.By;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * The confirmation page for executing a bulk transition.
 *
 * Try not to freak out if you submit this page and change all your data.
 *
 * @since v6.0
 */
public class TransitionOperationConfirmation extends AbstractJiraPage
{
    @ElementBy ( id = "updatedfields")
    private PageElement updatedFields;

    @ElementBy ( id = "next")
    private PageElement next;

    protected LinkedHashMap<String, String> fields = new LinkedHashMap<String, String>();

    @Override
    public TimedCondition isAt()
    {
        return updatedFields.timed().isPresent();
    }

    @Override
    public String getUrl()
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    public LinkedHashMap<String, String> getUpdatedFields()
    {
        if (null == fields || fields.isEmpty())
        {
            fields = new LinkedHashMap<String, String>();
            final List<PageElement> rows = updatedFields.findAll(By.cssSelector("tbody tr"));

            for(PageElement row : rows)
            {
                final List<PageElement> cells = row.findAll(By.cssSelector("td"));
                fields.put(
                        StringUtils.trim(cells.get(0).getText()),
                        StringUtils.trim(cells.get(1).getText())
                );
            }
        }
        return fields;
    }

    public BulkOperationProgressPage confirm()
    {
        next.click();
        return pageBinder.bind(BulkOperationProgressPage.class);
    }
}
