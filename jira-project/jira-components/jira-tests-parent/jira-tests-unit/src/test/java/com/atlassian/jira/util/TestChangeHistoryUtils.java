/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.util;

import com.atlassian.jira.web.util.ChangeHistoryUtils;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestChangeHistoryUtils
{
    String colour = "#ffffff";

    @Test
    public void testChangeHistoryChangedFrom()
    {
        String change = "[foo] changed from [bar] to [baz]";
        assertEquals("[<font color=#ffffff>foo</font>] changed from [<font color=#ffffff>bar</font>] to [<font color=#ffffff>baz</font>]\n", ChangeHistoryUtils.highlight(change, colour));
    }

    @Test
    public void testChangeHistorySetTo()
    {
        String change = "[foo] set to [bar]";
        assertEquals("[<font color=#ffffff>foo</font>] set to [<font color=#ffffff>bar</font>]\n", ChangeHistoryUtils.highlight(change, colour));
    }

    @Test
    public void testChangeHistoryWasAdded()
    {
        String change = "[foo] [1.0] was added";
        assertEquals("[<font color=#ffffff>foo</font>] [<font color=#ffffff>1.0</font>] was added\n", ChangeHistoryUtils.highlight(change, colour));
    }

    @Test
    public void testChangeHistoryWasRemoved()
    {
        String change = "[foo] [1.0] was removed";
        assertEquals("[<font color=#ffffff>foo</font>] [<font color=#ffffff>1.0</font>] was removed\n", ChangeHistoryUtils.highlight(change, colour));
    }

    @Test
    public void testChangeHistorySpaceInFieldName()
    {
        String change = "[foo bar] set to [baz]";
        assertEquals("[<font color=#ffffff>foo bar</font>] set to [<font color=#ffffff>baz</font>]\n", ChangeHistoryUtils.highlight(change, colour));
    }
}
