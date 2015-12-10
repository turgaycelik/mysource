package com.atlassian.jira.functest.framework.page;

import com.atlassian.jira.functest.framework.FuncTestHelperFactory;
import com.atlassian.jira.webtests.table.HtmlTable;
import net.sourceforge.jwebunit.WebTester;
import org.xml.sax.SAXException;

/**
 * @since v6.0
 */
public abstract class AbstractWebTestPage implements WebTestPage
{
    protected FuncTestHelperFactory funcTestHelperFactory;

    @Override
    public void setContext(FuncTestHelperFactory funcTestHelperFactory)
    {
        this.funcTestHelperFactory = funcTestHelperFactory;
    }

    protected WebTester getTester()
    {
        return funcTestHelperFactory.getTester();
    }

    protected HtmlTable getTableWithId(String id)
    {
        try
        {
            return new HtmlTable(getTester().getDialog().getResponse().getTableWithID(id));
        }
        catch (SAXException e)
        {
            throw new IllegalStateException(e);
        }
    }

}
