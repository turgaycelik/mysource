package com.atlassian.jira.plugin.index;

import java.util.Set;

import com.atlassian.jira.index.CommentSearchExtractor;
import com.atlassian.jira.issue.comments.Comment;

import com.google.common.collect.ImmutableSet;

import org.apache.lucene.document.Document;

public final class CommentSearchExtractorImpl implements CommentSearchExtractor
{
    @Override
    public Set<String> indexEntity(final Context<Comment> ctx, final Document doc)
    {
        return ImmutableSet.of();
    }
}
