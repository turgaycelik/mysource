package com.atlassian.jira.issue.index.indexers.impl;

import java.util.Collections;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.mock.project.MockVersion;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.web.FieldVisibilityManager;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @since v4.0
 */
public class TestVersionCustomFieldIndexer extends MockControllerTestCase
{
    @Test
    public void testAddIndexNullValue() throws Exception
    {
        final Issue theIssue = null;
        final Document doc = new Document();

        final FieldConfig fieldConfig = mockController.getMock(FieldConfig.class);

        final CustomField customField = mockController.getMock(CustomField.class);
        customField.getId();
        mockController.setReturnValue("blah");
        customField.getRelevantConfig(theIssue);
        mockController.setReturnValue(fieldConfig);
        customField.getValue(theIssue);
        mockController.setReturnValue(null);

        final FieldVisibilityManager visibilityManager = mockController.getMock(FieldVisibilityManager.class);
        visibilityManager.isFieldVisible("blah", theIssue);
        mockController.setReturnValue(true);

        VersionCustomFieldIndexer indexer = new VersionCustomFieldIndexer(visibilityManager, customField);

        mockController.replay();
        
        indexer.addIndex(doc, theIssue);
        
        assertTrue(doc.getFields().isEmpty());
    }

    @Test
    public void testAddIndexValueNotCollection() throws Exception
    {
        final Issue theIssue = null;
        final Document doc = new Document();

        final FieldConfig fieldConfig = mockController.getMock(FieldConfig.class);

        final CustomField customField = mockController.getMock(CustomField.class);
        customField.getId();
        mockController.setReturnValue("blah");
        customField.getRelevantConfig(theIssue);
        mockController.setReturnValue(fieldConfig);
        customField.getValue(theIssue);
        mockController.setReturnValue("NotACollection");

        final FieldVisibilityManager visibilityManager = mockController.getMock(FieldVisibilityManager.class);
        visibilityManager.isFieldVisible("blah", theIssue);
        mockController.setReturnValue(true);

        VersionCustomFieldIndexer indexer = new VersionCustomFieldIndexer(visibilityManager, customField);

        mockController.replay();

        indexer.addIndex(doc, theIssue);
        
        assertTrue(doc.getFields().isEmpty());
    }

    @Test
    public void testAddIndexHappyPath() throws Exception
    {
        final Issue theIssue = null;
        final Document doc = new Document();
        final String customFieldId = "customField";

        final Version version = new MockVersion(123L, "Test");

        final FieldConfig fieldConfig = mockController.getMock(FieldConfig.class);

        final CustomField customField = mockController.getMock(CustomField.class);
        customField.getId();
        mockController.setReturnValue(customFieldId);
        customField.getRelevantConfig(theIssue);
        mockController.setReturnValue(fieldConfig);
        customField.getValue(theIssue);
        mockController.setReturnValue(Collections.singleton(version));
        customField.getId();
        mockController.setReturnValue(customFieldId);

        final FieldVisibilityManager visibilityManager = mockController.getMock(FieldVisibilityManager.class);
        visibilityManager.isFieldVisible(customFieldId, theIssue);
        mockController.setReturnValue(true);

        VersionCustomFieldIndexer indexer = new VersionCustomFieldIndexer(visibilityManager, customField);

        mockController.replay();

        indexer.addIndex(doc, theIssue);

        final Field field = doc.getField(customFieldId);
        assertEquals("123", field.stringValue());
    }
}
