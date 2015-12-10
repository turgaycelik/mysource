package com.atlassian.jira.web.bean;

import java.util.List;

import com.atlassian.core.util.collection.EasyList;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestRowPercentageCalculator
{

    @Test
    public void testGetPercentage()
    {
        PercentageGraphModel.RowPercentageCalculator calculator = new PercentageGraphModel.RowPercentageCalculator(4);

        assertEquals(0, calculator.getPercentage(0));
        assertEquals(25, calculator.getPercentage(1));
        assertEquals(50, calculator.getPercentage(2));
        assertEquals(75, calculator.getPercentage(3));
        assertEquals(100, calculator.getPercentage(4));

        calculator = new PercentageGraphModel.RowPercentageCalculator(13);

        assertEquals(0, calculator.getPercentage(0));
        assertEquals(7, calculator.getPercentage(1));
        assertEquals(15, calculator.getPercentage(2));
        assertEquals(23, calculator.getPercentage(3));
        assertEquals(30, calculator.getPercentage(4));
        assertEquals(38, calculator.getPercentage(5));
        assertEquals(46, calculator.getPercentage(6));
        assertEquals(53, calculator.getPercentage(7));
        assertEquals(61, calculator.getPercentage(8));
        assertEquals(69, calculator.getPercentage(9));
        assertEquals(76, calculator.getPercentage(10));
        assertEquals(84, calculator.getPercentage(11));
        assertEquals(92, calculator.getPercentage(12));
        assertEquals(100, calculator.getPercentage(13));
    }

    @Test
    public void testGetLastPercentage()
    {

        PercentageGraphModel.RowPercentageCalculator calculator = new PercentageGraphModel.RowPercentageCalculator(7);

        List numbers = EasyList.build(new Long(6), new Long(1));
        assertEquals(15, calculator.getLastPercentage(numbers.iterator()));

        numbers = EasyList.build(new Long(5), new Long(2));
        assertEquals(29, calculator.getLastPercentage(numbers.iterator()));

        numbers = EasyList.build(new Long(4), new Long(3));
        assertEquals(43, calculator.getLastPercentage(numbers.iterator()));

        numbers = EasyList.build(new Long(3), new Long(4));
        assertEquals(58, calculator.getLastPercentage(numbers.iterator()));

        numbers = EasyList.build(new Long(2), new Long(5));
        assertEquals(72, calculator.getLastPercentage(numbers.iterator()));

        // common case of 33%
        calculator = new PercentageGraphModel.RowPercentageCalculator(3);
        numbers = EasyList.build(new Long(1), new Long(1), new Long(1));
        assertEquals(34, calculator.getLastPercentage(numbers.iterator()));

    }
}
