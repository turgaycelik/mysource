/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
package com.atlassian.jira.issue.index;

import java.util.Date;

import com.atlassian.fugue.Option;
import com.atlassian.jira.index.SearchExtractorRegistrationManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.comments.Comment;
import com.atlassian.jira.issue.index.indexers.impl.BaseFieldIndexer;
import com.atlassian.jira.issue.index.indexers.phrase.PhraseQuerySupportField;
import com.atlassian.jira.issue.search.SearchProviderFactory;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.LuceneUtils;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.Term;

public class DefaultCommentDocumentFactory implements CommentDocumentFactory
{
    private final SearchExtractorRegistrationManager searchExtractorManager;

    public DefaultCommentDocumentFactory(final SearchExtractorRegistrationManager searchExtractorManager)
    {
        this.searchExtractorManager = searchExtractorManager;
    }

    public Option<Document> apply(final Comment comment)
    {
        final Issue issue = comment.getIssue();
        final String body = comment.getBody();
        if (body == null)
        {
            return Option.none();
        }
        final Builder builder = new Builder(comment)
                .addField(DocumentConstants.PROJECT_ID, String.valueOf(issue.getProjectObject().getId()), Field.Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS)
                .addField(DocumentConstants.ISSUE_ID, String.valueOf(issue.getId()), Field.Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS)
                .addField(DocumentConstants.COMMENT_ID, String.valueOf(comment.getId()), Field.Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS)
                .addField(DocumentConstants.COMMENT_BODY, body, Field.Store.YES, Field.Index.ANALYZED)
                .addField(PhraseQuerySupportField.forIndexField(DocumentConstants.COMMENT_BODY), body, Field.Store.YES, Field.Index.ANALYZED)
                .addField(DocumentConstants.COMMENT_CREATED, LuceneUtils.dateToString(comment.getCreated()), Field.Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS)
                .addKeywordWithDefault(DocumentConstants.COMMENT_LEVEL, comment.getGroupLevel(), BaseFieldIndexer.NO_VALUE_INDEX_VALUE)
                .addKeywordWithDefault(DocumentConstants.COMMENT_LEVEL_ROLE, comment.getRoleLevel() != null ? comment.getRoleLevel().getId() : null, BaseFieldIndexer.NO_VALUE_INDEX_VALUE)
                .addAllExtractors(searchExtractorManager.findExtractorsForEntity(Comment.class));

        final ApplicationUser author = comment.getAuthorApplicationUser();
        if (author != null) //can't add null keywords
        {
            builder.addField(DocumentConstants.COMMENT_AUTHOR, author.getKey(), Field.Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS);
        }

        // If there is an updateAuthor then index it
        final ApplicationUser updateAuthor = comment.getUpdateAuthorApplicationUser();
        if (updateAuthor != null)
        {
            builder.addField(DocumentConstants.COMMENT_UPDATE_AUTHOR, updateAuthor.getKey(), Field.Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS);
        }

        final Date updated = comment.getUpdated();
        if (updated != null)
        {
            builder.addField(DocumentConstants.COMMENT_UPDATED, LuceneUtils.dateToString(updated), Field.Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS);
        }

        return builder.build();
    }

    private static class Builder extends EntityDocumentBuilder<Comment, Builder>
    {
        private Builder(final Comment entity)
        {
            super(entity, SearchProviderFactory.COMMENT_INDEX);
        }
    }

    public Term getIdentifyingTerm(final Comment comment)
    {
        return new Term(DocumentConstants.COMMENT_ID, comment.getId().toString());
    }
}
