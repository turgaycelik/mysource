package com.atlassian.jira.webtests.table;

import java.util.ArrayList;
import java.util.List;

/**
 * Convinience class to create a table (two dimensional array).
 * This class should make extending the possible operations on tables easy as well.
 */
public class TableData
{
    private List rows = new ArrayList();

    public TableData addRow(Object[] row)
    {
        rows.add(row);
        return this;
    }

    public Object[][] toArray()
    {
        Object[][] table = new Object[rows.size()][];
        for (int i = 0; i < rows.size(); i++)
        {
            table[i] = (Object[]) rows.get(i);
        }
        return table;
    }
}