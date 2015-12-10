/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
package com.atlassian.jira.issue.index;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;

import com.atlassian.fugue.Option;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.component.ComponentReference;
import com.atlassian.jira.index.SearchExtractorRegistrationManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.index.indexers.FieldIndexer;
import com.atlassian.jira.issue.index.managers.FieldIndexerManager;
import com.atlassian.jira.issue.search.SearchProviderFactory;
import com.atlassian.jira.util.log.RateLimitingLogger;

import com.google.common.base.Preconditions;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.index.Term;

import static com.atlassian.jira.issue.index.DocumentConstants.ISSUE_NON_EMPTY_FIELD_IDS;
import static com.atlassian.jira.issue.index.DocumentConstants.ISSUE_VISIBLE_FIELD_IDS;
import static com.google.common.collect.Lists.newLinkedList;
import static com.google.common.collect.Sets.newTreeSet;
import static org.apache.lucene.document.Field.Index.NOT_ANALYZED_NO_NORMS;

public class DefaultIssueDocumentFactory implements IssueDocumentFactory
{
    private static final RateLimitingLogger LOG = new RateLimitingLogger(DefaultIssueDocumentFactory.class);

    // Avoiding a cyclic dependency
    private final ComponentReference<FieldIndexerManager> fieldIndexerManagerRef =
            ComponentAccessor.getComponentReference(FieldIndexerManager.class);
    private final SearchExtractorRegistrationManager searchExtractorManager;

    public DefaultIssueDocumentFactory(@Nonnull final SearchExtractorRegistrationManager searchExtractorManager)
    {
        this.searchExtractorManager = Preconditions.checkNotNull(searchExtractorManager,"searchExtractorManager");
    }

    @Override
    public Option<Document> apply(final Issue issueObject)
    {
        if (LOG.isDebugEnabled())
        {
            LOG.debug("Indexing issue: " + issueObject.getKey());
        }

        //noinspection ConstantConditions
        return new Builder(issueObject)
                .addAll(fieldIndexerManagerRef.get().getAllIssueIndexers())
                .addAllExtractors(searchExtractorManager.findExtractorsForEntity(Issue.class))
                .build();
    }

    public Term getIdentifyingTerm(final Issue issue)
    {
        return new Term(DocumentConstants.ISSUE_ID, issue.getId().toString());
    }

    /**
     * Encapsulates the temporary state of building the document.
     */
    static class Builder extends EntityDocumentBuilder<Issue, Builder>
    {

        private final List<String> visibleDocumentFieldIds = newLinkedList();
        private final Issue issue;
        private Set<String> droppedFields;

        private Builder(final Issue issue)
        {
            super(issue, SearchProviderFactory.ISSUE_INDEX);
            this.issue = issue;
        }

        Builder addAll(Collection<? extends FieldIndexer> indexers)
        {
            for (final FieldIndexer indexer : indexers)
            {
                add(indexer);
            }
            return this;
        }

        void add(FieldIndexer indexer)
        {
            String documentFieldId = null;
            try
            {
                documentFieldId = indexer.getDocumentFieldId();
                indexer.addIndex(doc, issue);
                if (indexer.isFieldVisibleAndInScope(issue))
                {
                    visibleDocumentFieldIds.add(documentFieldId);
                }
            }
            catch (final RuntimeException re)
            {
                dropField(documentFieldId, indexer, re);
            }
            catch (final LinkageError err)
            {
                // Binary incompatibilities between JIRA and a plugin would generally show up as some variety
                // of LinkageError (usually NoSuchMethodError or something similar).  We would not want to
                // catch all Error classes, but this subset should be safe to block.
                dropField(documentFieldId, indexer, err);
            }
        }

        @Override
        protected void fieldsAddedByExtractor(final Set<String> fieldIds)
        {
            visibleDocumentFieldIds.addAll(fieldIds);
        }

        public Option<Document> build()
        {
            if (droppedFields != null)
            {
                LOG.warn("Error indexing issue " + issue.getKey() + ": Dropped: " + droppedFields);
            }

            generateNonEmptyFieldIds();

            // Use all the visible field ids and add a new field whose value is name of all the visible field ids
            for (final String documentFieldId : visibleDocumentFieldIds)
            {
                addField(ISSUE_VISIBLE_FIELD_IDS, documentFieldId, Field.Store.NO, NOT_ANALYZED_NO_NORMS);
            }

            return super.build();
        }

        private void dropField(final String documentFieldId, final FieldIndexer indexer, final Throwable e)
        {
            final String description = (documentFieldId != null) ? documentFieldId : indexer.getClass().getName();
            LOG.warn("Error indexing issue " + issue.getKey() + ": Dropping '" + description + '\'', e);
            if (droppedFields == null)
            {
                droppedFields = newTreeSet();
            }
            droppedFields.add(description);
        }

        // Get all the fields in the document and add a new field whose value is the name of all the included fields
        private void generateNonEmptyFieldIds()
        {
            for (final String fieldName : getNonEmptyFieldNames())
            {
                addField(ISSUE_NON_EMPTY_FIELD_IDS, fieldName, Field.Store.NO, NOT_ANALYZED_NO_NORMS);
            }
        }

        private List<String> getNonEmptyFieldNames()
        {
            final List<Fieldable> fields = doc.getFields();
            final List<String> names = new ArrayList<String>(fields.size());
            for (final Fieldable field : fields)
            {
                // NOTE: we do not store the field value since we are never interested in reading the value out of the
                // document, we are just interested in searching it. This will keep us from adding to the size of the issue
                // document.
                if (field.isIndexed())
                {
                    names.add(field.name());
                }
            }
            return names;
        }
    }
}
