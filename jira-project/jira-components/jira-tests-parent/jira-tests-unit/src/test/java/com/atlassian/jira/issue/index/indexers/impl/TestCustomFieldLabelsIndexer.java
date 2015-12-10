package com.atlassian.jira.issue.index.indexers.impl;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.label.Label;
import com.atlassian.jira.mock.issue.MockIssue;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.jira.web.FieldVisibilityManager;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.junit.Test;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class TestCustomFieldLabelsIndexer
{
    @Test
    public void testIndex()
    {
        final CustomField mockCustomField = createMock(CustomField.class);
        final FieldVisibilityManager mockFieldVisibilityManager = createMock(FieldVisibilityManager.class);
        final Issue mockIssue = new MockIssue(10000L);

        expect(mockCustomField.getValue(mockIssue)).andReturn(CollectionBuilder.newBuilder(new Label(null, null, "blah"),new Label(null, null, "HUGE")).asListOrderedSet());
        expect(mockCustomField.getId()).andReturn("customfield_10000").anyTimes();

        replay(mockCustomField, mockFieldVisibilityManager);
        final CustomFieldLabelsIndexer labelsIndexer = new CustomFieldLabelsIndexer(mockFieldVisibilityManager, mockCustomField);

        final Document doc = new Document();
        labelsIndexer.addDocumentFieldsSearchable(doc, mockIssue);

        final Field[] fields = doc.getFields("customfield_10000");
        assertEquals("blah", fields[0].stringValue());
        assertEquals("HUGE", fields[1].stringValue());

        final Field[] fieldsFolded = doc.getFields("customfield_10000_folded");
        assertEquals("blah", fieldsFolded[0].stringValue());
        assertEquals("huge", fieldsFolded[1].stringValue());

        verify(mockCustomField, mockFieldVisibilityManager);
    }
    
    @Test
    public void testIndexEmpty()
    {
        final CustomField mockCustomField = createMock(CustomField.class);
        final FieldVisibilityManager mockFieldVisibilityManager = createMock(FieldVisibilityManager.class);
        final Issue mockIssue = new MockIssue(10000L);

        expect(mockCustomField.getValue(mockIssue)).andReturn(null);
        expect(mockCustomField.getId()).andReturn("customfield_10000").anyTimes();

        replay(mockCustomField, mockFieldVisibilityManager);
        final CustomFieldLabelsIndexer labelsIndexer = new CustomFieldLabelsIndexer(mockFieldVisibilityManager, mockCustomField);

        final Document doc = new Document();
        labelsIndexer.addDocumentFieldsSearchable(doc, mockIssue);

        assertEquals("<EMPTY>", doc.getField("customfield_10000_folded").stringValue());
        assertNull(doc.getField("customfield_10000"));

        verify(mockCustomField, mockFieldVisibilityManager);
    }
    
    @Test
    public void testIndexNotSearchable()
    {
        final CustomField mockCustomField = createMock(CustomField.class);
        final FieldVisibilityManager mockFieldVisibilityManager = createMock(FieldVisibilityManager.class);
        final Issue mockIssue = new MockIssue(10000L);

        expect(mockCustomField.getValue(mockIssue)).andReturn(CollectionBuilder.newBuilder(new Label(null, null, "blah"),new Label(null, null, "HUGE")).asListOrderedSet());
        expect(mockCustomField.getId()).andReturn("customfield_10000").anyTimes();

        replay(mockCustomField, mockFieldVisibilityManager);
        final CustomFieldLabelsIndexer labelsIndexer = new CustomFieldLabelsIndexer(mockFieldVisibilityManager, mockCustomField);

        final Document doc = new Document();
        labelsIndexer.addDocumentFieldsNotSearchable(doc, mockIssue);

        final Field[] fields = doc.getFields("customfield_10000");
        assertEquals("blah", fields[0].stringValue());
        assertEquals("HUGE", fields[1].stringValue());

        assertNull(doc.getField("customfield_10000_folded"));

        verify(mockCustomField, mockFieldVisibilityManager);
    }
}
