package com.atlassian.jira.functest.framework.navigator;

import com.meterware.httpunit.WebTable;
import net.sourceforge.jwebunit.WebTester;
import org.apache.commons.lang.StringUtils;
import org.xml.sax.SAXException;

import static org.junit.Assert.fail;

/**
 * Checks that the passed column is visible in the issue navigator.
 *
 * @since v4.0
 */
public class IssueTableHasColumnCondition implements SearchResultsCondition
{
    private final String columnName;

    public IssueTableHasColumnCondition(final String columnName)
    {
        this.columnName = columnName;
    }

    @Override
    public void assertCondition(final WebTester tester)
    {
        WebTable table = getIssueTable(tester);
        if (table == null)
        {
            fail("Unable to find issue table");
        }

        if (!isColumnPresent(table))
        {
            fail("Unable to find column " + columnName + " visible on issue table");
        }
    }

    WebTable getIssueTable(final WebTester tester)
    {
        try
        {
            return tester.getDialog().getResponse().getTableWithID("issuetable");
        }
        catch (SAXException e)
        {
            return null;
        }
    }

    boolean isColumnPresent(final WebTable table)
    {
        for (int i = 0; i < table.getColumnCount(); i++)
        {
            String cellText = StringUtils.trimToNull(table.getCellAsText(0, i));
            if (columnName.equals(cellText))
            {
                return true;
            }
        }
        return false;
    }
}
