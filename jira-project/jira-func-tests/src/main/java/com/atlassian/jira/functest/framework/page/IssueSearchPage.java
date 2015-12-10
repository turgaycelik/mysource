package com.atlassian.jira.functest.framework.page;

import com.atlassian.jira.functest.framework.FuncTestHelperFactory;
import com.atlassian.jira.functest.framework.locator.IdLocator;
import com.atlassian.jira.functest.framework.locator.Locator;
import com.atlassian.jira.webtests.table.HtmlTable;
import com.meterware.httpunit.WebTable;

import java.util.ArrayList;
import java.util.List;

/**
 * @since v6.0
 */
public class IssueSearchPage
{
    private final FuncTestHelperFactory funcTestHelperFactory;

    public IssueSearchPage(FuncTestHelperFactory funcTestHelperFactory)
    {
        this.funcTestHelperFactory = funcTestHelperFactory;
    }

    public boolean hasResultsTable()
    {
        return funcTestHelperFactory.getLocator().id("issuetable").exists();
    }

    public HtmlTable getResultsTable()
    {
        final WebTable webTable = funcTestHelperFactory.getTester().getDialog().getWebTableBySummaryOrId("issuetable");
        return new HtmlTable(webTable);
    }

    public List<String> getResultsIssueKeys()
    {
        final List<String> issueKeys = new ArrayList<String>();
        for (HtmlTable.Row row : getResultsTable().getRows())
        {
            if (row.getRowIndex() > 0)
            {
                issueKeys.add(row.getCellForHeading("Key"));
            }
        }
        return issueKeys;
    }

    public String getWarning()
    {
        final Locator warning = funcTestHelperFactory.getLocator().css("aui-message.warning");
        return warning.exists() ? warning.getText() : null;
    }
}
