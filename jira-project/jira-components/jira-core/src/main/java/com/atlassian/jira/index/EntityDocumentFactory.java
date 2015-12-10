package com.atlassian.jira.index;

import java.util.Collection;
import java.util.Set;

import com.atlassian.fugue.Option;
import com.atlassian.jira.issue.index.indexers.impl.FieldIndexerUtil;

import com.google.common.base.Function;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;


/**
 * Converts provided entity to lucene documents for indexing
 *
 * @since v6.2
 */
public interface EntityDocumentFactory<T> extends Function<T, Option<Document>>
{
    abstract class EntityDocumentBuilder<T, C extends EntityDocumentBuilder<T, C>>
    {
        protected final Document doc = new Document();
        private final GenericSearchExtractorContext<T> context;

        protected EntityDocumentBuilder(final T entity, final String indexName)
        {
            this.context = new GenericSearchExtractorContext<T>(entity, indexName);
        }

        public C addAllExtractors(Collection<EntitySearchExtractor<T>> extractors)
        {
            for (final EntitySearchExtractor<T> extractor : extractors)
            {
                try
                {
                    fieldsAddedByExtractor(extractor.indexEntity(context, doc));
                }
                catch (final RuntimeException re)
                {

                }
                catch (final LinkageError err)
                {
                    // Binary incompatibilities between JIRA and a plugin would generally show up as some variety
                    // of LinkageError (usually NoSuchMethodError or something similar).  We would not want to
                    // catch all Error classes, but this subset should be safe to block.
                }
            }
            return cast();

        }

        public C addField(String name, String value, Field.Store store, Field.Index index)
        {
            doc.add(new Field(name, value, store, index));
            return cast();
        }

        public C addKeywordWithDefault(final String name, final String value, final String defaultValue)
        {
            FieldIndexerUtil.indexKeywordWithDefault(doc, name, value, defaultValue);
            return cast();
        }

        public C addKeywordWithDefault(final String name, final Long value, final String defaultValue)
        {
            FieldIndexerUtil.indexKeywordWithDefault(doc, name, value, defaultValue);
            return cast();
        }

        /*
        This method may be overridden if Builder wants to be notified about field ids added by entity extractor
        to processed documents
         */
        protected void fieldsAddedByExtractor(final Set<String> fieldIds)
        {

        }

        private C cast()
        {//noinspection unchecked
            return (C) this;
        }

        public Option<Document> build()
        {
            return Option.some(doc);
        }
    }
}
