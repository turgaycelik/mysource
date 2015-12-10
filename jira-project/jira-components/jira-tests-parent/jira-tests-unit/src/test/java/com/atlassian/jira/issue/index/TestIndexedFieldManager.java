package com.atlassian.jira.issue.index;

import java.util.Collection;

import com.atlassian.jira.issue.history.AssigneeDateRangeBuilder;
import com.atlassian.jira.issue.history.DateRangeBuilder;
import com.atlassian.jira.issue.history.ReporterDateRangeBuilder;
import com.atlassian.jira.issue.history.StatusDateRangeBuilder;

import com.google.common.collect.Sets;

import org.easymock.classextension.EasyMock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.easymock.classextension.EasyMock.expect;
import static org.easymock.classextension.EasyMock.replay;


public class TestIndexedFieldManager
{
    ChangeHistoryFieldConfigurationManager mockChangeHistoryFieldConfigurationManager = EasyMock.createMock(ChangeHistoryFieldConfigurationManager.class);

    IndexedChangeHistoryFieldManager manager = new DefaultIndexedChangeHistoryFieldManager(mockChangeHistoryFieldConfigurationManager);

    @Before
    public void setupMocks()
    {
        expect(mockChangeHistoryFieldConfigurationManager.getAllFieldNames()).andStubReturn(Sets.<String>newHashSet("status", "assignee", "reporter"));
        expect(mockChangeHistoryFieldConfigurationManager.getDateRangeBuilder("status")).andStubReturn(new StatusDateRangeBuilder());
        expect(mockChangeHistoryFieldConfigurationManager.getDateRangeBuilder("assignee")).andStubReturn(new AssigneeDateRangeBuilder());
        expect(mockChangeHistoryFieldConfigurationManager.getDateRangeBuilder("reporter")).andStubReturn(new ReporterDateRangeBuilder());
        replay(mockChangeHistoryFieldConfigurationManager);
    }

    @Test
    public void testGetSupportedFields()
    {
        Collection<IndexedChangeHistoryField> indexedFields = manager.getIndexedChangeHistoryFields();
        Assert.assertEquals("Should support status, assignee and reporter", 3, indexedFields.size());
    }

    @Test
    public void testDeleteSupportedFields()
    {
        Collection<IndexedChangeHistoryField> indexedFields = manager.getIndexedChangeHistoryFields();
        manager.deleteIndexedChangeHistoryField(indexedFields.iterator().next());
        indexedFields = manager.getIndexedChangeHistoryFields();
        Assert.assertEquals("Should be 2 fields left", 2, indexedFields.size());
    }

    @Test
    public void testAddSupportedFields()
    {
        DateRangeBuilder mockBuilder = EasyMock.createMock(DateRangeBuilder.class);
        IndexedChangeHistoryField indexedField = new IndexedChangeHistoryField("fixVersion", mockBuilder);
        EasyMock.replay(mockBuilder);

        manager.addIndexedChangeHistoryField(indexedField);
        Collection<IndexedChangeHistoryField> indexedFields = manager.getIndexedChangeHistoryFields();
        Assert.assertEquals("Should have added fixVersion field", 4, indexedFields.size());
        EasyMock.verify(mockBuilder);
    }

    @Test
    public void testAddDuplicateFields()
    {
        DateRangeBuilder mockBuilder = EasyMock.createMock(DateRangeBuilder.class);
        IndexedChangeHistoryField indexedField = new IndexedChangeHistoryField("assignee", mockBuilder);
        EasyMock.replay(mockBuilder);

        manager.addIndexedChangeHistoryField(indexedField);
        Collection<IndexedChangeHistoryField> indexedFields = manager.getIndexedChangeHistoryFields();
        Assert.assertEquals("Should not add  duplicate assignee field", 3, indexedFields.size());
        EasyMock.verify(mockBuilder);
    }

}
