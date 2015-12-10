/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.issue.index;

import java.util.Set;

import com.atlassian.jira.index.EntitySearchExtractor;
import com.atlassian.jira.index.IssueSearchExtractor;
import com.atlassian.jira.index.SearchExtractorRegistrationManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.index.indexers.FieldIndexer;
import com.atlassian.jira.issue.index.managers.FieldIndexerManager;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.mock.issue.MockIssue;
import com.atlassian.jira.project.MockProject;

import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Fieldable;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static com.atlassian.jira.matchers.LuceneDocumentMatchers.fieldableHasStringValue;
import static com.atlassian.jira.matchers.LuceneDocumentMatchers.hasStringField;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.arrayContainingInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TestDefaultIssueDocumentFactory
{
    @Rule
    public final RuleChain mockitoMocksInContainer = MockitoMocksInContainer.forTest(this);
    private IssueDocumentFactory documentFactory;
    @Mock
    @AvailableInContainer
    public FieldIndexerManager fieldIndexerManager;
    //dummy indexers
    @Mock
    public FieldIndexer issueKeyIndexer;
    @Mock
    public FieldIndexer issueSummaryIndexer;
    @Mock
    public FieldIndexer issueDescriptionIndexer;
    @Mock
    private SearchExtractorRegistrationManager searchExtractorManager;
    MockIssue issue;
    private ImmutableList<FieldIndexer> indexers;

    @Before
    public void setUp() throws Exception
    {
        indexers = ImmutableList.of(
                issueKeyIndexer, issueSummaryIndexer, issueDescriptionIndexer
        );
        when(fieldIndexerManager.getAllIssueIndexers()).thenReturn(indexers);

        when(issueDescriptionIndexer.getDocumentFieldId()).thenReturn(DocumentConstants.ISSUE_DESC);
        when(issueKeyIndexer.getDocumentFieldId()).thenReturn(DocumentConstants.ISSUE_KEY);
        when(issueSummaryIndexer.getDocumentFieldId()).thenReturn(DocumentConstants.ISSUE_SUMMARY);

        issue = new MockIssue(132, "JRA-2");
        issue.setNumber(2L);
        issue.setProjectObject(new MockProject(21L));
        issue.setSummary("foosum");
        issue.setDescription("foodescription");
        documentFactory = new DefaultIssueDocumentFactory(searchExtractorManager);
    }

    @Test
    public void shouldUseAllIndexersOnIssueToAddValues()
    {
        final Document d = documentFactory.apply(issue).getOrElse(failOption());
        for (final FieldIndexer indexer : indexers)
        {
            verify(indexer).addIndex(d, issue);
        }
    }

    @Test
    public void shouldAddNonEmptyIdsToTheDocument()
    {
        //we will index key and summary
        mockIndexer(issueKeyIndexer, Field.Store.NO, Field.Index.NOT_ANALYZED_NO_NORMS);
        mockIndexer(issueSummaryIndexer, Field.Store.NO, Field.Index.NOT_ANALYZED_NO_NORMS);
        //but we won't index description so it shouldn't show on the result
        mockIndexer(issueDescriptionIndexer, Field.Store.YES, Field.Index.NO);


        final Document d = documentFactory.apply(issue).getOrElse(failOption());

        final Fieldable[] nonEmptyIds = d.getFieldables(DocumentConstants.ISSUE_NON_EMPTY_FIELD_IDS);

        assertThat(nonEmptyIds, arrayContainingInAnyOrder(
                fieldableHasStringValue(equalTo(DocumentConstants.ISSUE_SUMMARY)),
                fieldableHasStringValue(equalTo(DocumentConstants.ISSUE_KEY))
        ));
    }

    @Test
    public void shouldAddVisibleFieldsToTheIndex()
    {
        //we want to have description and key visible, but not summary
        when(issueDescriptionIndexer.isFieldVisibleAndInScope(issue)).thenReturn(true);
        when(issueKeyIndexer.isFieldVisibleAndInScope(issue)).thenReturn(true);
        when(issueSummaryIndexer.isFieldVisibleAndInScope(issue)).thenReturn(false);

        final Document d = documentFactory.apply(issue).getOrElse(failOption());

        final Fieldable[] visibleFields = d.getFieldables(DocumentConstants.ISSUE_VISIBLE_FIELD_IDS);

        assertThat(visibleFields, arrayContainingInAnyOrder(
                fieldableHasStringValue(equalTo(DocumentConstants.ISSUE_DESC)),
                fieldableHasStringValue(equalTo(DocumentConstants.ISSUE_KEY))
        ));
    }

    @Test
    public void shouldAddFieldsDefinedByEntityExtractorsAndAddThemToVisibleFields()
    {
        final String fieldID = "FieldID";
        final String fieldValue = "value";
        when(issueDescriptionIndexer.isFieldVisibleAndInScope(issue)).thenReturn(true);
        when(searchExtractorManager.findExtractorsForEntity(Issue.class)).thenReturn(
                ImmutableList.<EntitySearchExtractor<Issue>>of(new IssueSearchExtractor()
                {
                    @Override
                    public Set<String> indexEntity(final Context<Issue> ctx, final Document doc)
                    {
                        doc.add(new Field(fieldID, fieldValue, Field.Store.YES, Field.Index.NO));
                        return ImmutableSet.of(fieldID);
                    }
                }));

        final Document d = documentFactory.apply(issue).getOrElse(failOption());

        final Fieldable[] visibleFields = d.getFieldables(DocumentConstants.ISSUE_VISIBLE_FIELD_IDS);
        assertThat(visibleFields, arrayContainingInAnyOrder(
                fieldableHasStringValue(equalTo(DocumentConstants.ISSUE_DESC)),
                fieldableHasStringValue(equalTo(fieldID))
        ));
        assertThat(d, hasStringField(fieldID, fieldValue));
    }

    @Test
    public void shouldDropFieldsThatThrowRuntimeException()
    {
        shouldHandleException(new NullPointerException("Bet you didn't see this coming!"));
    }

    @Test
    public void shouldDropFieldsThatThrowLinkageError()
    {
        shouldHandleException(new NoSuchMethodError("Bet you didn't see this coming!"));
    }

    private void shouldHandleException(final Throwable e)
    {
        //we want to have description and key, but summary will throw an exception
        when(issueDescriptionIndexer.isFieldVisibleAndInScope(issue)).thenReturn(true);
        when(issueKeyIndexer.isFieldVisibleAndInScope(issue)).thenReturn(true);
        when(issueSummaryIndexer.isFieldVisibleAndInScope(issue)).thenReturn(true);
        mockIndexer(issueKeyIndexer, Field.Store.NO, Field.Index.NOT_ANALYZED_NO_NORMS);
        mockIndexer(issueDescriptionIndexer, Field.Store.NO, Field.Index.NOT_ANALYZED_NO_NORMS);
        mockIndexer(issueSummaryIndexer, e);

        final Document d = documentFactory.apply(issue).getOrElse(failOption());

        assertThat(d.getFieldable(DocumentConstants.ISSUE_KEY).stringValue(), equalTo("dummy value for " + DocumentConstants.ISSUE_KEY));
        assertThat(d.getFieldable(DocumentConstants.ISSUE_DESC).stringValue(), equalTo("dummy value for " + DocumentConstants.ISSUE_DESC));
        assertThat(d.getFieldable(DocumentConstants.ISSUE_SUMMARY), nullValue());

        final Fieldable[] visibleFields = d.getFieldables(DocumentConstants.ISSUE_VISIBLE_FIELD_IDS);
        assertThat(visibleFields, arrayContainingInAnyOrder(
                fieldableHasStringValue(equalTo(DocumentConstants.ISSUE_DESC)),
                fieldableHasStringValue(equalTo(DocumentConstants.ISSUE_KEY))
        ));

        final Fieldable[] nonEmptyFields = d.getFieldables(DocumentConstants.ISSUE_NON_EMPTY_FIELD_IDS);
        assertThat(nonEmptyFields, arrayContainingInAnyOrder(
                fieldableHasStringValue(equalTo(DocumentConstants.ISSUE_DESC)),
                fieldableHasStringValue(equalTo(DocumentConstants.ISSUE_KEY))
        ));
    }

    @Test
    public void shouldPropagateNonLinkageErrors()
    {
        //we want to have description and key, but summary will throw an exception
        when(issueDescriptionIndexer.isFieldVisibleAndInScope(issue)).thenReturn(true);
        when(issueKeyIndexer.isFieldVisibleAndInScope(issue)).thenReturn(true);
        when(issueSummaryIndexer.isFieldVisibleAndInScope(issue)).thenReturn(true);
        mockIndexer(issueKeyIndexer, Field.Store.NO, Field.Index.NOT_ANALYZED_NO_NORMS);
        mockIndexer(issueDescriptionIndexer, Field.Store.NO, Field.Index.NOT_ANALYZED_NO_NORMS);
        mockIndexer(issueSummaryIndexer, new OutOfMemoryError("Just testing!"));

        try
        {
            fail("Did not expect to succeed in getting a document, but got: " + documentFactory.apply(issue));
        }
        catch (final OutOfMemoryError err)
        {
            assertThat(err.getMessage(), equalTo("Just testing!"));
        }
    }

    private void mockIndexer(FieldIndexer indexer, final Field.Store storePolicy, final Field.Index indexPolicy)
    {
        final String fieldId = indexer.getDocumentFieldId();
        doAnswer(new Answer<Void>()
        {
            @Override
            public Void answer(final InvocationOnMock invocation) throws Throwable
            {
                final Document doc = (Document) invocation.getArguments()[0];
                doc.add(new Field(fieldId, "dummy value for " + fieldId, storePolicy, indexPolicy));
                return null;
            }
        }).when(indexer).addIndex(any(Document.class), same(issue));
    }

    private void mockIndexer(FieldIndexer indexer, final Throwable throwMe)
    {
        doThrow(throwMe).when(indexer).addIndex(any(Document.class), same(issue));
    }

    private Supplier<Document> failOption()
    {
        return new Supplier<Document>()
        {
            @Override
            public Document get()
            {
                fail("Issue document not defined");
                return null;
            }
        };
    }


}
