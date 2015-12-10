/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.issue.index;

import java.util.Date;
import java.util.Locale;
import java.util.Set;

import com.atlassian.jira.index.CommentSearchExtractor;
import com.atlassian.jira.index.EntitySearchExtractor;
import com.atlassian.jira.index.SearchExtractorRegistrationManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.comments.Comment;
import com.atlassian.jira.issue.comments.MockComment;
import com.atlassian.jira.junit.rules.InitMockitoMocks;
import com.atlassian.jira.mock.issue.MockIssue;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.util.LuceneUtils;

import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.mockito.Mock;

import static com.atlassian.jira.matchers.LuceneDocumentMatchers.hasStringField;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

public class TestDefaultCommentDocumentFactory
{
    @Rule
    public TestRule initMocs = new InitMockitoMocks(this);
    @Mock
    private SearchExtractorRegistrationManager searchExtractorManager;
    private static final String UPDATE_AUTHOR = "updateAuthor";
    private CommentDocumentFactory documentFactory;
    private Issue testissue;

    @Before
    public void setUp() throws Exception
    {
        final MockIssue issue = new MockIssue(1L);
        issue.setSecurityLevelId(1L);
        issue.setProjectObject(new MockProject(2));
        testissue = issue;
        documentFactory = new DefaultCommentDocumentFactory(searchExtractorManager);
    }

    @Test
    public void testGetDocumentNullWithNullCommentBody()
    {
        final MockComment comment = new MockComment("justin", null, "admin", null, new Date());
        comment.setBody(null);
        Assert.assertTrue("Comment document should not be defined", documentFactory.apply(comment).isEmpty());
    }

    @Test
    public void testGetDocumentNoLevels()
    {
        final Date currentDate = new Date();
        final Comment comment = new MockComment(1L, "fooauthor", "foobody", null, null, currentDate, testissue);

        final Document doc = documentFactory.apply(comment).getOrElse(failOption());

        assertThat(doc, Matchers.allOf(
                hasStringField(DocumentConstants.COMMENT_ID, "1"),
                hasStringField(DocumentConstants.COMMENT_AUTHOR, "fooauthor"),
                hasStringField(DocumentConstants.COMMENT_BODY, "foobody"),
                hasStringField(DocumentConstants.ISSUE_ID, "1"),
                hasStringField(DocumentConstants.PROJECT_ID, "2"),
                // Both of these need to return -1
                hasStringField(DocumentConstants.COMMENT_LEVEL, "-1"),
                hasStringField(DocumentConstants.COMMENT_LEVEL_ROLE, "-1"),

                // have to use Date tools, since this is what is internally used by Lucene
                //TODO: JRA-11588 assertEquals((DateTools.dateToString(currentDate, DateTools.Resolution.MILLISECOND)), doc.getField(DocumentConstants.COMMENT_CREATED).stringValue());
                hasStringField(DocumentConstants.COMMENT_CREATED, LuceneUtils.dateToString(currentDate))
        ));


    }

    @Test
    public void testGetDocumentWithGroupLevel()
    {
        final Date currentDate = new Date();
        final Comment comment = new MockComment(1L, "fooauthor", "foobody", "foolevel", null, currentDate, testissue);

        final Document doc = documentFactory.apply(comment).getOrElse(failOption());

        assertThat(doc, Matchers.allOf(
                hasStringField(DocumentConstants.COMMENT_ID, "1"),
                hasStringField(DocumentConstants.COMMENT_LEVEL, "foolevel")
        ));

    }

    @Test
    public void testGetDocumentWithRoleLevel()
    {
        final Comment comment = new MockComment(1L, "fooauthor", "foobody", null, 1L, new Date(), testissue);

        final Document doc = documentFactory.apply(comment).getOrElse(failOption());

        assertThat(doc, Matchers.allOf(
                hasStringField(DocumentConstants.COMMENT_ID, "1"),
                hasStringField(DocumentConstants.COMMENT_LEVEL_ROLE, "1")
        ));
    }

    @Test
    public void shouldUseSearchExtractorsToAddFieldsToDocument()
    {
        final Comment comment = new MockComment(1L, "fooauthor", "foobody", null, 1L, new Date(), testissue);
        final String fieldID = "FieldID";
        final String fieldValue = "value";

        when(searchExtractorManager.findExtractorsForEntity(Comment.class)).thenReturn(
                ImmutableList.<EntitySearchExtractor<Comment>>of(new CommentSearchExtractor()
                {
                    @Override
                    public Set<String> indexEntity(final Context<Comment> ctx, final Document doc)
                    {
                        doc.add(new Field(fieldID, fieldValue, Field.Store.YES, Field.Index.NO));
                        return ImmutableSet.of(fieldID);
                    }
                }));

        final Document d = documentFactory.apply(comment).getOrElse(failOption());
        assertThat("Field should be indexed",d, hasStringField(fieldID, fieldValue));
    }

    @Test
    public void testIssueSecurityLevelExists()
    {
        final Comment comment = new MockComment(1L, "fooauthor", "foobody", null, 1L, new Date(), testissue);

        final Document doc = documentFactory.apply(comment).getOrElse(failOption());

        assertThat(doc, Matchers.allOf(
                hasStringField(DocumentConstants.ISSUE_ID, "1"),
                hasStringField(DocumentConstants.PROJECT_ID, "2")
        ));
    }

    @Test
    public void testDocumentContainsUpdateInformation()
    {
        // We need to test that the document includes the update date and updateAuthor if they are present
        final Date createDate = new Date(100000);
        final Date updateDate = new Date(5000000);
        final Comment comment = new MockComment(1L, "fooauthor", UPDATE_AUTHOR, "foobody", null, 1L, createDate, updateDate, testissue);

        final Document doc = documentFactory.apply(comment).getOrElse(failOption());

        assertThat(doc, Matchers.allOf(
                hasStringField(DocumentConstants.ISSUE_ID, "1"),
                hasStringField(DocumentConstants.PROJECT_ID, "2"),
                hasStringField(DocumentConstants.COMMENT_UPDATE_AUTHOR, UPDATE_AUTHOR.toLowerCase(Locale.ENGLISH)),
                hasStringField(DocumentConstants.COMMENT_UPDATED, LuceneUtils.dateToString(updateDate))
        ));
    }

    private Supplier<Document> failOption()
    {
        return new Supplier<Document>()
        {
            @Override
            public Document get()
            {
                fail("Comment document not defined");
                return null;
            }
        };
    }
}
