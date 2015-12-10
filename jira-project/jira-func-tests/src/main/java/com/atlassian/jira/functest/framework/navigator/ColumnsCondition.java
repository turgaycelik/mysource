package com.atlassian.jira.functest.framework.navigator;

import com.meterware.httpunit.TableCell;
import com.meterware.httpunit.WebTable;
import static junit.framework.Assert.fail;
import net.sourceforge.jwebunit.WebTester;
import org.apache.commons.lang.StringUtils;
import org.xml.sax.SAXException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/**
 * Checks that the passed columns appear in the issue navigator.
 *
 * @since v4.0
 */
public class ColumnsCondition implements SearchResultsCondition
{
    private final Collection<String> columns;

    public ColumnsCondition(Collection<String> columns)
    {
        this.columns = new ArrayList<String>(columns);
    }

    public ColumnsCondition(String... columns)
    {
        this(Arrays.asList(columns));
    }

    public void assertCondition(final WebTester tester)
    {

        WebTable table = null;
        try
        {
            table = tester.getDialog().getResponse().getTableWithID("issuetable");
        }
        catch (SAXException e)
        {
            fail("Expected columns '" + columns + "' to be visible: Unable to find issue table -" + e.getMessage());
        }

        if (table == null)
        {
            if (!columns.isEmpty())
            {
                fail("Expected columns '" + columns + "' to be visible: Issue table not visible.");
            }
        }
        else
        {
            int columnMax = table.getColumnCount();
            int columnPos = 0;

            final TableCell lastCell = table.getTableCell(0, columnMax-1);
            if (!StringUtils.contains(lastCell.getClassName(), "headerrow-actions"))
            {
                fail("Expected actions column to be visible, but was not -- last column is '" + StringUtils.trimToNull(lastCell.asText()) + "' (classes '" + lastCell.getClassName() + "')");
            }

            columnMax--; // now that we've checked the actions column is there.

            for (String column : columns)
            {
                if (columnPos >= columnMax)
                {
                    fail("Expected columns '" + columns + "' to be visible. Unable to find column '" + column + ".");
                }

                final String content = StringUtils.trimToNull(table.getCellAsText(0, columnPos));
                if (!column.equals(content))
                {
                    fail("Expected columns '" + columns + "' to be visible: Found column '" + content + "' instead of '" + column + "'.");
                }
                columnPos++;
            }

            if (columnPos < columnMax)
            {
                fail("Expected columns '" + columns + "' to be visible: Found extra column '" + StringUtils.trimToNull(table.getCellAsText(0, columnPos)) + "'.");
            }
        }
    }
}
