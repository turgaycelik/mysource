package com.atlassian.jira.web.bean;

import java.util.Iterator;
import java.util.List;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class TestPercentageGraphModel
{
    @Test
    public void testDefaults()
    {
        PercentageGraphModel model = new PercentageGraphModel();
        assertEquals(0, model.getRows().size());
        assertEquals(0, model.getTotal());
        assertTrue(model.isTotalZero());
    }

    @Test
    public void testRowsListUnmodifiable()
    {
        PercentageGraphModel model = new PercentageGraphModel();
        model.addRow("color", 2, "some description");
        List rows = model.getRows();
        try
        {
            rows.add("test");
            fail("list modification should not be supported");
        }
        catch (UnsupportedOperationException e)
        {
            // expected
        }
        Iterator iterator = rows.iterator();
        assertNotNull(iterator.next());
        try
        {
            iterator.remove();
            fail("iterator removal should not be supported");
        }
        catch (UnsupportedOperationException e)
        {
            // expected
        }
    }

    @Test
    public void testModelWithRows()
    {
        PercentageGraphModel model = new PercentageGraphModel();

        model.addRow("color", 2, "some description");
        assertFalse(model.isTotalZero());
        assertEquals(2, model.getTotal());
        List rows = model.getRows();
        assertEquals(1, rows.size());
        assertRowPresentAtIndex(rows, 0, "color", 2, "some description");

        assertEquals(200, model.getPercentage(new PercentageGraphRow("whatever", 4, "desc", null)));

        model.addRow("none", 3, "no description");
        assertFalse(model.isTotalZero());
        assertEquals(5, model.getTotal());
        rows = model.getRows();
        assertEquals(2, rows.size());
        assertRowPresentAtIndex(rows, 0, "color", 2, "some description");
        assertRowPresentAtIndex(rows, 1, "none", 3, "no description");

        assertEquals(80, model.getPercentage(new PercentageGraphRow("whatever", 4, "desc", null)));

        model.addRow("white", 1, "long description", "statii");
        assertFalse(model.isTotalZero());
        assertEquals(6, model.getTotal());
        rows = model.getRows();
        assertEquals(3, rows.size());
        assertRowPresentAtIndex(rows, 0, "color", 2, "some description");
        assertRowPresentAtIndex(rows, 1, "none", 3, "no description");
        assertRowPresentAtIndex(rows, 2, "white", 1, "long description", "statii");

        assertEquals(166, model.getPercentage(new PercentageGraphRow("whatever", 10, "desc", null)));
        assertEquals(17, model.getPercentage(new PercentageGraphRow("white", 1, "long description", "statii")));
    }

    private void assertRowPresentAtIndex(List rows, int index, String color, long number, String description)
    {
        assertRowPresentAtIndex(rows, index, color, number, description, null);
    }

    private void assertRowPresentAtIndex(List rows, int index, String color, long number, String description, String statuses)
    {
        assertNotNull(rows);
        assertTrue(index >= 0 && index < rows.size());
        PercentageGraphRow row = (PercentageGraphRow) rows.get(index);
        assertEquals(color, row.getColor());
        assertEquals(number, row.getNumber());
        assertEquals(description, row.getDescription());
        assertEquals(statuses, row.getStatuses());
    }

}
