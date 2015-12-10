/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.issue.search;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class TestSearchRequest
{
    @Test
    public void testBlankConstructor()
    {
        SearchRequest sr = new SearchRequest();
        assertNotNull(sr.getQuery());
    }
}
