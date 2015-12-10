package com.atlassian.jira.issue.index;

import java.sql.Timestamp;
import java.util.Date;
import java.util.Set;

import com.atlassian.fugue.Option;
import com.atlassian.jira.index.ChangeHistorySearchExtractor;
import com.atlassian.jira.index.EntitySearchExtractor;
import com.atlassian.jira.index.SearchExtractorRegistrationManager;
import com.atlassian.jira.issue.changehistory.ChangeHistoryGroup;
import com.atlassian.jira.issue.changehistory.ChangeHistoryItem;
import com.atlassian.jira.junit.rules.InitMockitoMocks;
import com.atlassian.jira.util.LuceneUtils;

import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.mockito.Mock;

import static com.atlassian.jira.matchers.LuceneDocumentMatchers.hasStringField;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;


/**
 * Tests that a document is created from a ChangeHistoryGroup
 *
 * @since v4.3
 */
public class TestDefaultChangeHistoryDocumentFactory
{
    @Rule
    public TestRule initMocs = new InitMockitoMocks(this);

    private static final String FIELD = "Status";
    private final long CHANGE_DATE = 100000000;
    private ChangeHistoryDocumentFactory changeHistoryDocumentFactory;
    @Mock
    private SearchExtractorRegistrationManager searchExtractorManager;

    @Before
    public void setUp() throws Exception
    {
        changeHistoryDocumentFactory = new DefaultChangeHistoryDocumentFactory(searchExtractorManager);
    }

    @Test
    public void testGetDocument()
    {
        final ChangeHistoryItem change = new ChangeHistoryItem(1000L, 1l, 1L, 2L, "Key1", FIELD, new Timestamp(CHANGE_DATE), "Open", "Closed", "1", "2", "user");
        final ChangeHistoryGroup changeGroup = new ChangeHistoryGroup(1L, 1L, 2L, "Key1", "user", Lists.newArrayList(change), new Timestamp(CHANGE_DATE));
        final Option<Document> doc = changeHistoryDocumentFactory.apply(changeGroup);

        _assertDocument(doc.getOrElse(fail()), "1", "2", "Key1", "ch-open", "ch-closed", "ch-1", "ch-2", "ch-user");
    }

    @Test
    public void testGetDocumentWithNullValues()
    {
        final ChangeHistoryItem change = new ChangeHistoryItem(1000L, 1L, 1L, 2L, "Key2", FIELD, new Timestamp(CHANGE_DATE), null, "Closed", "1", "2", "user");
        final ChangeHistoryGroup changeGroup = new ChangeHistoryGroup(1L, 1L, 2L, "Key2", "user", Lists.newArrayList(change), new Timestamp(CHANGE_DATE));
        final Option<Document> doc = changeHistoryDocumentFactory.apply(changeGroup);

        _assertDocument(doc.getOrElse(fail()), "1", "2", "Key2", "ch-", "ch-closed", "ch-1", "ch-2", "ch-user");
    }

    @Test
    public void shouldUseSearchExtractorsToAddFieldsToDocument()
    {
        final ChangeHistoryGroup changeGroup = new ChangeHistoryGroup(1L, 1L, 2L, "Key2", "user", ImmutableList.<ChangeHistoryItem>of(), new Timestamp(CHANGE_DATE));
        final String fieldID = "FieldID";
        final String fieldValue = "value";

        when(searchExtractorManager.findExtractorsForEntity(ChangeHistoryGroup.class)).thenReturn(
                ImmutableList.<EntitySearchExtractor<ChangeHistoryGroup>>of(new ChangeHistorySearchExtractor()
                {
                    @Override
                    public Set<String> indexEntity(final Context<ChangeHistoryGroup> ctx, final Document doc)
                    {
                        doc.add(new Field(fieldID, fieldValue, Field.Store.YES, Field.Index.NO));
                        return ImmutableSet.of(fieldID);
                    }
                }));

        final Document d = changeHistoryDocumentFactory.apply(changeGroup).getOrElse(fail());

        assertThat("Field should be indexed",d, hasStringField(fieldID, fieldValue));
    }


    private void _assertDocument(final Document doc, final String projectId, final String issueId, final String issueKey, final String oldString, final String newString, final String oldValue, final String newValue, final String userName)
    {
        assertEquals("Project ID", projectId, doc.getFieldable(DocumentConstants.PROJECT_ID).stringValue());
        assertEquals("Issue ID", issueId, doc.getFieldable(DocumentConstants.ISSUE_ID).stringValue());
        assertEquals("Issue Key", issueKey, doc.getFieldable(DocumentConstants.ISSUE_KEY).stringValue());
        assertEquals("Old String", oldString, doc.getFieldable(FIELD + "." + DocumentConstants.CHANGE_FROM).stringValue());
        assertEquals("New String", newString, doc.getFieldable(FIELD + "." + DocumentConstants.CHANGE_TO).stringValue());
        assertEquals("Old Value", oldValue, doc.getFieldable(FIELD + "." + DocumentConstants.OLD_VALUE).stringValue());
        assertEquals("New Value", newValue, doc.getFieldable(FIELD + "." + DocumentConstants.NEW_VALUE).stringValue());
        assertEquals("Username", userName, doc.getFieldable(DocumentConstants.CHANGE_ACTIONER).stringValue());
        assertEquals("Date", (LuceneUtils.dateToString(new Date(CHANGE_DATE))), doc.getFieldable(DocumentConstants.CHANGE_DATE).stringValue());
    }

    private Supplier<Document> fail()
    {
        return new Supplier<Document>()
        {
            @Override
            public Document get()
            {
                Assert.fail("Expected document do be some");
                return null;
            }
        };
    }
}
