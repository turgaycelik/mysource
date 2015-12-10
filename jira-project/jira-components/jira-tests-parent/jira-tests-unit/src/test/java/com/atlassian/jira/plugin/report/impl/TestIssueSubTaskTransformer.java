package com.atlassian.jira.plugin.report.impl;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.atlassian.jira.issue.Issue;

import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.PredicateUtils;
import org.apache.commons.collections.set.ListOrderedSet;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class TestIssueSubTaskTransformer
{
    private Predicate includeSubTasksPredicate = PredicateUtils.truePredicate();

    @Test
    public void testInvalidInput()
    {
        try
        {
            new IssueSubTaskTransformer(includeSubTasksPredicate).getIssues(null);
            fail("Null set of issue should not be acceptable by IssueSubTaskTransformer");
        }
        catch (NullPointerException e)
        {
            // expected
        }
    }

    @Test
    public void testSimpleNoSubTasks()
    {
        MockSubTaskedIssue.Factory gen = new MockSubTaskedIssue.Factory();
        MockSubTaskedIssue parent1 = gen.get();
        MockSubTaskedIssue parent2 = gen.get();

        assertEquals(0, parent1.getSubTaskObjects().size());

        Set issues = new HashSet(Arrays.asList(new MockSubTaskedIssue[]{parent1, parent2}));

        Set transformedIssues = new IssueSubTaskTransformer(includeSubTasksPredicate).getIssues(issues);
        assertEquals(2, transformedIssues.size());
        assertEqualsSubTaskCount(0, parent1, transformedIssues);
    }

    @Test
    public void testSimpleParent()
    {
        MockSubTaskedIssue.Factory factory = new MockSubTaskedIssue.Factory();
        MockSubTaskedIssue parent = factory.get();
        MockSubTaskedIssue child = factory.get();
        child.setParent(parent);

        assertEquals(0, parent.getSubTaskObjects().size());

        Set issues = new HashSet(Arrays.asList(new MockSubTaskedIssue[]{parent, child}));
        Set transformedIssues = new IssueSubTaskTransformer(includeSubTasksPredicate).getIssues(issues);
        assertEquals(1, transformedIssues.size());
        assertEqualsSubTaskCount(1, parent, transformedIssues);
    }

    @Test
    public void testSimpleParentWithOrphan()
    {
        MockSubTaskedIssue.Factory gen = new MockSubTaskedIssue.Factory();
        MockSubTaskedIssue parent = gen.get();
        MockSubTaskedIssue child = gen.get();
        MockSubTaskedIssue orphan = gen.get();
        orphan.setParent(gen.get());
        child.setParent(parent);

        assertEquals(0, parent.getSubTaskObjects().size());

        Set issues = new HashSet(Arrays.asList(new MockSubTaskedIssue[]{parent, child, orphan}));
        Set transformedIssues = new IssueSubTaskTransformer(includeSubTasksPredicate).getIssues(issues);
        assertEquals(2, transformedIssues.size());
        assertEqualsSubTaskCount(1, parent, transformedIssues);
    }

    @Test
    public void testComplicated()
    {
        MockSubTaskedIssue.Factory gen = new MockSubTaskedIssue.Factory();
        MockSubTaskedIssue parent1 = gen.get();
        MockSubTaskedIssue parent2 = gen.get();
        MockSubTaskedIssue parent3 = gen.get();
        MockSubTaskedIssue parent4 = gen.get();

        MockSubTaskedIssue child1p1 = gen.get();
        child1p1.setParent(parent1);
        MockSubTaskedIssue child2p1 = gen.get();
        child2p1.setParent(parent1);

        MockSubTaskedIssue child3p3 = gen.get();
        child3p3.setParent(parent3);

        MockSubTaskedIssue orphan = gen.get();
        MockSubTaskedIssue parent4Unused = gen.get();
        orphan.setParent(parent4Unused);

        assertEquals(0, parent1.getSubTaskObjects().size());

        Set issues = new ListOrderedSet();
        issues.addAll(Arrays.asList(new MockSubTaskedIssue[]{parent1, parent2, parent3, child1p1, child2p1, child3p3, orphan, parent4}));
        Set transformedIssues = new IssueSubTaskTransformer(includeSubTasksPredicate).getIssues(issues);
        assertEquals(5, transformedIssues.size());
        ListOrderedSet orderedIssues = new ListOrderedSet();
        orderedIssues.addAll(transformedIssues);
        assertEquals(parent1, orderedIssues.get(0));
        assertEquals(parent2, orderedIssues.get(1));
        assertEquals(parent3, orderedIssues.get(2));
        assertEquals(orphan, orderedIssues.get(3));
        assertEquals(parent4, orderedIssues.get(4));

        assertEqualsSubTaskCount(2, parent1, transformedIssues);
        assertEqualsSubTaskCount(0, parent2, transformedIssues);
        assertEqualsSubTaskCount(1, parent3, transformedIssues);
        assertEqualsSubTaskCount(0, parent4, transformedIssues);
    }

    void assertEqualsSubTaskCount(int expected, Issue parent, Collection transformedIssues)
    {
        Issue issue = null;
        for (Iterator it = transformedIssues.iterator(); it.hasNext();)
        {
            Issue i = (Issue) it.next();
            if (parent.getKey().equals(i.getKey()))
            {
                issue = i;
            }
        }
        if (issue == null)
        {
            fail(parent + " is not in set");
        }
        assertEquals(expected, issue.getSubTaskObjects().size());
    }
}
