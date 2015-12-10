package com.atlassian.jira.functest.framework.page;

import com.atlassian.jira.webtests.table.HtmlTable;

import java.util.ArrayList;
import java.util.List;

/**
 * @since v6.0
 */
public class ViewVotersPage extends AbstractWebTestPage
{
    @Override
    public String baseUrl()
    {
        return "ViewVoters!default.jspa";
    }

    public List<String> getCurrentVoters()
    {
        HtmlTable table = getTableWithId("voter-list");
        List<String> viewers = new ArrayList<String>(table.getRowCount() - 1);
        for (HtmlTable.Row row : table.getRows())
        {
            if (row.getRowIndex() > 0)
            {
                viewers.add(row.getCellAsText(0));
            }
        }
        return viewers;
    }
}
