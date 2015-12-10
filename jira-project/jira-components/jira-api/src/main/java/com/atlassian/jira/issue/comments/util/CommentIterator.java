package com.atlassian.jira.issue.comments.util;

import com.atlassian.annotations.PublicApi;
import com.atlassian.jira.issue.comments.Comment;

import java.util.Iterator;

/**
 * A way of iterating over comments, without necessarily loading them all into memory.
 */
@PublicApi
public interface CommentIterator extends Iterator<Comment>
{
    public Comment nextComment();

    public void close();

    /**
     * Returns the total number of comments this iterator contains.
     *
     * @return the total number of comments this iterator contains.
     */
    public int size();

}
