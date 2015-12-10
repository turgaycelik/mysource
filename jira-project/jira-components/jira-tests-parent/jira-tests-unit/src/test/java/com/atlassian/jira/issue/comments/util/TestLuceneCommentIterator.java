package com.atlassian.jira.issue.comments.util;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;

import com.atlassian.jira.bc.issue.comment.CommentService;
import com.atlassian.jira.index.LuceneVersion;
import com.atlassian.jira.issue.comments.Comment;
import com.atlassian.jira.issue.comments.MockComment;
import com.atlassian.jira.issue.index.DocumentConstants;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.MockApplicationUser;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.util.collect.CollectionBuilder;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TopFieldDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.junit.After;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Test for {@link com.atlassian.jira.issue.comments.util.LuceneCommentIterator}.
 */
public class TestLuceneCommentIterator extends MockControllerTestCase
{
    private final Analyzer ANALYZER_FOR_INDEXING = new StandardAnalyzer(LuceneVersion.get());
    private Directory commentsDir = null;
    private IndexSearcher searcher = null;

    @After
    public void tearDown() throws Exception
    {
        closeDirectory();
    }

    /*
     * Validate the behaviour of the iterator when a null hits object is passed in.
     */
    @Test
    public void testNoHits() throws Exception
    {
        final CommentService service = mockController.getMock(CommentService.class);
        mockController.replay();

        LuceneCommentIterator iter = new LuceneCommentIterator(null, service, null, searcher);
        assertEquals(0, iter.size());
        assertIteratorFinished(iter);

        iter.close();

        mockController.verify();
    }

    /*
     * Validate the behaviour of the iterator when no lucene hits are returned.
     */
    @Test
    public void testHitsWithNoResults() throws Exception
    {
        final CommentService service = mockController.getMock(CommentService.class);
        mockController.replay();

        addCommentIdsToIndex(Collections.<Integer>emptyList());
        LuceneCommentIterator iter = new LuceneCommentIterator(null, service, searchAll(), searcher);

        assertEquals(0, iter.size());
        assertIteratorFinished(iter);

        iter.close();

        mockController.verify();
    }

    /*
     * Validate the behavour of the iterator when comments are returned. This is done as the anonymous user.
     */
    @Test
    public void testHitsWithResultsAnonymous() throws Exception
    {
        final List<Integer> commentIds = CollectionBuilder.newBuilder(261, 0, 1, 2002).asArrayList();
        _testIteratorWithListAndUser(commentIds, null);
    }

    /*
     * Validate the behavour of the iterator when comments are returned.
     */
    @Test
    public void testHitsWithResults() throws Exception
    {
        final MockApplicationUser testUser = new MockApplicationUser("test");
        final List<Integer> commentIds = CollectionBuilder.newBuilder(373782, 0, 3347849, Integer.MAX_VALUE).asArrayList();
        _testIteratorWithListAndUser(commentIds, testUser);
    }

    /*
     * Make sure the iterator throws an error when trying to remove a comment.
     */
    @Test
    public void testRemoveComment() throws Exception
    {
        addCommentIdsToIndex(Collections.singleton(1));
        final CommentService service = getMock(CommentService.class);
        expect(service.getCommentById(this.<ApplicationUser>anyObject(), anyLong(), this.<ErrorCollection>anyObject())).andStubReturn(new MockComment("me", "body"));
        replay();

        LuceneCommentIterator iter = new LuceneCommentIterator(null, service, searchAll(), searcher);
        iter.next();
        try
        {
            iter.remove();
            fail("Exception an exception to be thrown.");
        }
        catch (UnsupportedOperationException expected)
        {
        }
        iter.close();
        mockController.verify();
    }

    private void _testIteratorWithListAndUser(final List<Integer> commentIds, final ApplicationUser user) throws Exception
    {
        addCommentIdsToIndex(commentIds);
        Collections.sort(commentIds);

        final CommentService service = mockController.getMock(CommentService.class);
        for (Integer commentId : commentIds)
        {
            service.getCommentById(user, (long) commentId, new SimpleErrorCollection());
            if (commentId != 0)
            {
                mockController.setReturnValue(new MockComment((long) commentId, "me", "Comment body " + commentId, null, null, new Date(), null));
            }
            else
            {
                //Simulate the Comment Manager returning null for security reasons.
                mockController.setReturnValue(null);
            }
        }
        mockController.replay();

        LuceneCommentIterator iter = new LuceneCommentIterator(user, service, searchAll(), searcher);
        try
        {
            assertEquals(commentIds.size(), iter.size());

            for (Integer commentId : commentIds)
            {
                assertTrue(iter.hasNext());
                if (commentId != 0)
                {
                    Comment comment = iter.next();
                    assertNotNull(comment);
                    assertEquals(new Long(commentId), comment.getId());
                }
            }
            assertIteratorFinished(iter);
        }
        finally
        {
            iter.close();
        }

        mockController.verify();
    }

    private void assertIteratorFinished(final LuceneCommentIterator iter)
    {
        assertFalse(iter.hasNext());

        try
        {
            iter.next();
            fail("Exception should have been thrown.");
        }
        catch (NoSuchElementException ignored)
        {
        }

        try
        {
            iter.nextComment();
            fail("Exception should have been thrown.");
        }
        catch (NoSuchElementException ignored)
        {
        }
    }

    private void closeDirectory() throws Exception
    {
        closeSearcher();

        if (commentsDir != null)
        {
            commentsDir.close();
            commentsDir = null;
        }
    }

    private void closeSearcher() throws IOException
    {
        if (searcher != null)
        {
            searcher.close();
            searcher = null;
        }
    }

    private void addCommentIdsToIndex(Collection<Integer> commentIds) throws Exception
    {
        closeSearcher();
        ensureOpen();

        IndexWriterConfig conf = new IndexWriterConfig(LuceneVersion.get(), ANALYZER_FOR_INDEXING);
        conf.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
        IndexWriter writer = new IndexWriter(commentsDir, conf);
        try
        {
            for (int commentId : commentIds)
            {
                Document document = new Document();
                document.add(new Field(DocumentConstants.COMMENT_ID, Integer.toString(commentId), Field.Store.YES, Field.Index.NOT_ANALYZED));
                writer.addDocument(document);
            }
        }
        finally
        {
            writer.close();
        }
    }

    private void ensureOpen() throws Exception
    {
        if (commentsDir == null)
        {
            commentsDir = new RAMDirectory();
        }
    }

    private IndexSearcher getSearcher() throws Exception
    {
        ensureOpen();
        if (searcher == null)
        {
            searcher = new IndexSearcher(commentsDir);
        }
        return searcher;
    }

    private TopFieldDocs searchAll() throws Exception
    {
        ensureOpen();

        IndexSearcher searcher = getSearcher();
        return searcher.search(new MatchAllDocsQuery(), Integer.MAX_VALUE, new Sort(new SortField(DocumentConstants.COMMENT_ID, SortField.INT)));
    }
}
