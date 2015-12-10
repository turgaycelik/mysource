package com.atlassian.jira.functest.framework.page;

import com.atlassian.core.util.StringUtils;
import com.atlassian.jira.webtests.table.HtmlTable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @since v6.0
 */
public class ManageWatchersPage extends AbstractWebTestPage
{
    @Override
    public String baseUrl()
    {
        return "ManageWatchers!default.jspa";
    }

    public List<String> getCurrentWatchers()
    {
        HtmlTable table = getTableWithId("watcher-list");
        List<String> watchers = new ArrayList<String>(table.getRowCount() - 1);
        for (HtmlTable.Row row : table.getRows())
        {
            if (row.getRowIndex() > 0)
            { watchers.add(row.getCellAsText(1)); }
        }
        return watchers;
    }

    public ManageWatchersPage addWatchers(String... usernames)
    {
        getTester().setFormElement("userNames", concat(usernames));
        getTester().submit("add");

        ManageWatchersPage nextPage = new ManageWatchersPage();
        nextPage.setContext(funcTestHelperFactory);
        return nextPage;
    }

    private String concat(String[] usernames)
    {
        return StringUtils.createCommaSeperatedString(Arrays.asList(usernames));
    }
}
