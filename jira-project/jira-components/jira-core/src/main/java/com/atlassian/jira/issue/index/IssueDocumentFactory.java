/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
package com.atlassian.jira.issue.index;

import com.atlassian.jira.index.EntityDocumentFactory;
import com.atlassian.jira.issue.Issue;

import org.apache.lucene.index.Term;

/**
 * Abstracts the means to create a {@link org.apache.lucene.document.Document} for an {@link com.atlassian.jira.issue.Issue}.
 * 
 * @since v4.0
 */
public interface IssueDocumentFactory extends EntityDocumentFactory<Issue>
{
    Term getIdentifyingTerm(Issue issue);
}
