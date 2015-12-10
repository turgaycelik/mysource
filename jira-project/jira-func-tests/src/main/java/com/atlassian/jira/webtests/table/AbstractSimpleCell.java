package com.atlassian.jira.webtests.table;

import com.atlassian.jira.testkit.client.log.FuncTestOut;
import com.meterware.httpunit.WebTable;

/**
 * Convinience abstract class for {@link com.atlassian.jira.webtests.table.SimpleCell}
 *
 * @since v3.12
 */
public abstract class AbstractSimpleCell implements SimpleCell
{
    public String getCellAsText(WebTable table, int row, int col)
    {
        return table.getCellAsText(row, col);
    }

    public void log(String msg)
    {
        FuncTestOut.log(msg);
    }
}
