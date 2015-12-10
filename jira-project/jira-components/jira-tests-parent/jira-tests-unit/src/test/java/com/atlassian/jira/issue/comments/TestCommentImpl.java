package com.atlassian.jira.issue.comments;

import com.atlassian.jira.user.MockApplicationUser;
import org.junit.Test;

import java.util.Date;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class TestCommentImpl
{
    private static final String COMMENT_BODY = "This is a comment";
    private static final String COMMENT_AUTHOR = "bob";
    private static final String COMMENT_GROUP_1 = "Group 1";
    private static final Long COMMENT_ROLE_1 = new Long(2);
    private static final Long COMMENT_ID = new Long(23);

    @Test
    public void testInvalidConstruction()
    {
        try
        {
            // null author and body is allowed
            // (with the RPC plubin it is possible to create comments with a null author.
            // It may therefore be possible, that data with null authors exists)
            new CommentImpl(null, null, null, null, null, null, null, null, null);
        }
        catch (IllegalArgumentException e)
        {
            fail();
        }

        try
        {
            // null body is allowed (see JRA-11522)
            new CommentImpl(null, new MockApplicationUser(COMMENT_AUTHOR), null, null, null, null, null, null, null);
        }
        catch (IllegalArgumentException e)
        {
            fail();
        }

        try
        {
            new CommentImpl(null, new MockApplicationUser(COMMENT_AUTHOR), null, COMMENT_BODY, COMMENT_GROUP_1, COMMENT_ROLE_1, null, null, null);
            fail();
        }
        catch (IllegalArgumentException e)
        {
            // setting both group and role level is not allowed
        }
    }

    @Test
    public void testIntegrity()
    {
        CommentImpl comment = new CommentImpl(null, new MockApplicationUser(COMMENT_AUTHOR), null, COMMENT_BODY, COMMENT_GROUP_1, null, null, null, null);
        assertNull(comment.getId());
        assertEquals(COMMENT_AUTHOR, comment.getAuthor());
        assertEquals(COMMENT_BODY, comment.getBody());
        assertEquals(COMMENT_GROUP_1, comment.getGroupLevel());
        assertNull(comment.getRoleLevelId());
        assertNotNull(comment.getCreated());

        comment = new CommentImpl(null, new MockApplicationUser(COMMENT_AUTHOR), null, COMMENT_BODY, null, COMMENT_ROLE_1, null, null, null);
        assertNull(comment.getId());
        assertEquals(COMMENT_AUTHOR, comment.getAuthor());
        assertEquals(COMMENT_BODY, comment.getBody());
        assertNull(comment.getGroupLevel());
        assertEquals(COMMENT_ROLE_1, comment.getRoleLevelId());
        assertNotNull(comment.getCreated());

        Date now = new Date();
        comment = new CommentImpl(null, new MockApplicationUser(COMMENT_AUTHOR), null, COMMENT_BODY, null, COMMENT_ROLE_1, now, null, null);
        assertNull(comment.getId());
        assertEquals(COMMENT_AUTHOR, comment.getAuthor());
        assertEquals(COMMENT_BODY, comment.getBody());
        assertNull(comment.getGroupLevel());
        assertEquals(COMMENT_ROLE_1, comment.getRoleLevelId());
        assertEquals(now, comment.getCreated());
        // The reason we are testing this is that when you specify a create date with no updated date we expect
        // the create date to be the same as the updated date.
        assertEquals(comment.getCreated(), comment.getUpdated());

        Date later = new Date();
        comment = new CommentImpl(null, new MockApplicationUser(COMMENT_AUTHOR), null, COMMENT_BODY, null, COMMENT_ROLE_1, now, later, null);
        assertNull(comment.getId());
        assertEquals(COMMENT_AUTHOR, comment.getAuthor());
        assertEquals(COMMENT_BODY, comment.getBody());
        assertNull(comment.getGroupLevel());
        assertEquals(COMMENT_ROLE_1, comment.getRoleLevelId());
        assertEquals(now, comment.getCreated());
        assertEquals(later, comment.getUpdated());

        comment.setId(COMMENT_ID);
        assertEquals(COMMENT_ID, comment.getId());
    }

    @Test
    public void testNullAuthorFullNameIsNull()
    {
        CommentImpl comment = new CommentImpl(null, null, null, COMMENT_BODY, COMMENT_GROUP_1, null, null, null, null);
        assertNull(comment.getAuthorFullName());
        assertNull(comment.getUpdateAuthorFullName());
    }

    @Test
    public void testSetBodyReplacesNullWithEmptyString()
    {
        CommentImpl comment = new CommentImpl(null, null, null, null, null, null, null, null, null);
        comment.setBody(null);
        assertThat(comment.getBody(), is(""));
    }

    @Test
    public void testConstructorReplacesNullWithEmptyString()
    {
        CommentImpl comment = new CommentImpl(null, null, null, null, null, null, null, null, null);
        assertThat(comment.getBody(), is(""));
    }

    @Test
    public void testCommentBodySingleLFsNotReplacedWithCRLF()
    {
        final String body = "This is a\nmultiline\ncomment";
        CommentImpl comment = new CommentImpl(null, null, null, body, null, null, null, null, null);
        assertThat("Comment body should not have '\\n' changed to '\\r\\n'", comment.getBody(), is("This is a\nmultiline\ncomment"));
    }

    @Test
    public void testCommentBodyCRLFsNotReplacedWithCRLF()
    {
        final String body = "This is a\r\nmultiline\r\ncomment";
        CommentImpl comment = new CommentImpl(null, null, null, body, null, null, null, null, null);
        assertThat("Comment body should be unaltered", comment.getBody(), is(body));
    }
}
