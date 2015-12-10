/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.issue;

import com.atlassian.core.util.thumbnail.Thumber;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestThumbnailManager
{

    @Test
    public void testScaleSize()
    {
        _testScaleSize(200, 200, 400, 300, 200, 150);//should scale using width
        _testScaleSize(200, 200, 300, 400, 150, 200);//should scale using height
        _testScaleSize(200, 200, 400, 400, 200, 200);//should scale using either
        _testScaleSize(400, 400, 200, 200, 200, 200);//shouldn't scale at all
        _testScaleSize(200, 200, 200, 200, 200, 200);//shouldn't scale at all

    }

    private void _testScaleSize(int maxWidth, int maxHeight, int originalWidth, int originalHeight, int expectedWidth, int expectedHeight)
    {
        Thumber t = new Thumber();
        Thumber.WidthHeightHelper widthHeightHelper = t.determineScaleSize(maxWidth, maxHeight, originalWidth, originalHeight);
        assertEquals(expectedWidth, widthHeightHelper.getWidth());
        assertEquals(expectedHeight, widthHeightHelper.getHeight());
    }
}
