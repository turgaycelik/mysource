package com.atlassian.jira.issue.label;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class TestLabelComparator
{
    @Test
    public void testCompare()
    {
        Label label1 = new Label(10000L, 10200L, "aaa");
        Label label2 = new Label(10000L, 10200L, "bbb");
        Label label3 = new Label(10000L, 10200L, "bbb");
        Label label4 = new Label(10000L, 10200L, "AAA");
        final LabelComparator instance = LabelComparator.INSTANCE;
        assertTrue(instance.compare(null, null) == 0);
        assertTrue(instance.compare(null, label1) > 0);
        assertTrue(instance.compare(label1, null) < 0);
        assertTrue(instance.compare(label1, label2) < 0);
        assertTrue(instance.compare(label3, label2) == 0);
        assertTrue(instance.compare(label2, label1) > 0);
        assertTrue(instance.compare(label1, label4) > 0);
    }
}
