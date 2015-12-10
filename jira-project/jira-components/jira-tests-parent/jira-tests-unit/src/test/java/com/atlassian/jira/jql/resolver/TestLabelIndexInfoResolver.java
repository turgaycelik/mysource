package com.atlassian.jira.jql.resolver;

import java.util.List;

import com.atlassian.jira.issue.label.Label;
import com.atlassian.jira.util.collect.CollectionBuilder;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestLabelIndexInfoResolver
{
    @Test
    public void testGetIndexedValues()
    {
        final Label label = new Label(null, null, "Label5");
        LabelIndexInfoResolver indexInfoResolverLowercasing = new LabelIndexInfoResolver(true);
        final List<String> list = indexInfoResolverLowercasing.getIndexedValues("label1 LABEL1 Label2 label3");
        assertEquals(CollectionBuilder.newBuilder("label1", "label1", "label2", "label3").asList(), list);
        assertEquals("label5", indexInfoResolverLowercasing.getIndexedValue(label));

        LabelIndexInfoResolver indexInfoResolver = new LabelIndexInfoResolver(false);
        final List<String> list2 = indexInfoResolver.getIndexedValues("label1 LABEL1 Label2 label3");
        assertEquals(CollectionBuilder.newBuilder("label1", "LABEL1", "Label2", "label3").asList(), list2);
        assertEquals("Label5", indexInfoResolver.getIndexedValue(label));
    }
}
