/**
 * Copyright 2008 Atlassian Pty Ltd 
 */
package com.atlassian.jira.sharing.index;

import com.atlassian.jira.sharing.SharedEntity;
import org.apache.lucene.document.Document;

/**
 * Build a Document for a {@link SharedEntity}
 * 
 * @since v3.13
 */
public interface SharedEntityDocumentFactory
{
    Document create(/* S */SharedEntity entity);
}
