/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
package com.atlassian.jira.issue.index;

import com.atlassian.jira.index.EntityDocumentFactory;
import com.atlassian.jira.issue.comments.Comment;

import org.apache.lucene.index.Term;

/**
 * Abstracts the means to create a {@link org.apache.lucene.document.Document} for a comment
 * {@link com.atlassian.jira.issue.comments.Comment} and its {@link com.atlassian.jira.issue.Issue}.
 */
public interface CommentDocumentFactory extends EntityDocumentFactory<Comment>
{
    Term getIdentifyingTerm(final Comment comment);
}
