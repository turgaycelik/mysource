/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
package com.atlassian.jira.issue.util;

import java.util.ArrayList;
import java.util.Collection;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.issue.IssueFactory;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;

import com.google.common.collect.Lists;
import com.mockobjects.dynamic.Mock;

import org.junit.Test;
import org.ofbiz.core.entity.GenericValue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class TestIssueGVsIssueIterable
{
    private final Mock mockIssueFactory = new Mock(IssueFactory.class);

    @Test
    public void testToString()
    {
        mockIssueFactory.setStrict(true);

        final Collection<GenericValue> issueGVs = Lists.<GenericValue>newArrayList(new MockGenericValue("Issue", EasyMap.build("id", new Long(1), "key", "TST-1")), new MockGenericValue("Issue", EasyMap.build("id", new Long(54), "key", "TST-10")), new MockGenericValue("Issue", EasyMap.build("id", new Long(956), "key", "ABC-24")), new MockGenericValue("Issue", EasyMap.build("id", new Long(926), "key", "TST-100")), new MockGenericValue("Issue", EasyMap.build("id", new Long(739), "key", "ANO-3")));

        final IssueGVsIssueIterable issueIterable = new IssueGVsIssueIterable(issueGVs, (IssueFactory) mockIssueFactory.proxy());
        assertEquals(IssueGVsIssueIterable.class.getName() + " (5 items): [TST-1, TST-10, ABC-24, TST-100, ANO-3]", issueIterable.toString());

        mockIssueFactory.verify();
    }

    @Test
    public void testNullCollection()
    {
        try
        {
            new IssueGVsIssueIterable(null, (IssueFactory) mockIssueFactory.proxy());
            fail("Cannot operate with a null id collection");
        }
        catch (final NullPointerException yay)
        {}
        mockIssueFactory.verify();
    }

    @Test
    public void testNullFactory()
    {
        try
        {
            new IssueGVsIssueIterable(new ArrayList<GenericValue>(), null);
            fail("Cannot operate with a null factory");
        }
        catch (final NullPointerException yay)
        {}
        mockIssueFactory.verify();
    }
}
