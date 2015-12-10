/**
 * Copyright 2008 Atlassian Pty Ltd
 */
package com.atlassian.jira.sharing.index;

import com.atlassian.jira.issue.index.indexers.impl.FieldIndexerUtil;
import com.atlassian.jira.sharing.SharedEntity;
import com.atlassian.jira.sharing.SharedEntityColumnDefinition;
import com.google.common.collect.Lists;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;

import javax.annotation.concurrent.Immutable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Build a Field from a {@link SharedEntity}
 *
 * @since v3.13
 */
public interface SharedEntityFieldFactory
{
    String getFieldName();

    Collection<Field> getField(SharedEntity entity);

    /**
     * Default builders.
     */
    @Immutable
    static abstract class Default implements SharedEntityFieldFactory
    {
        static final Default ID = new Default(SharedEntityColumnDefinition.ID.getName(), Field.Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS )
        {
            @Override
            String getValue(final SharedEntity entity)
            {
                return entity.getId().toString();
            }
        };

        static final Default NAME = new Default(SharedEntityColumnDefinition.NAME.getName())
        {
            @Override
            String getValue(final SharedEntity entity)
            {
                return entity.getName();
            }
        };

        static final Default NAME_CASE_INSENSITIVE = new Default(SharedEntityColumnDefinition.NAME.getCaseInsensitiveColumn(), Store.NO, Index.NOT_ANALYZED_NO_NORMS )
        {
            @Override
            String getValue(final SharedEntity entity)
            {
                final String result = entity.getName();
                return (result == null) ? null : result.toLowerCase();
            }
        };

        static final Default NAME_SORT = new Default(SharedEntityColumnDefinition.NAME.getSortColumn(), Store.YES, Index.NOT_ANALYZED_NO_NORMS )
        {
            @Override
            String getValue(final SharedEntity entity)
            {
                final String result = entity.getName();
                return (result == null) ? null : FieldIndexerUtil.getValueForSorting(result.toLowerCase());
            }
        };

        static final Default DESCRIPTION = new Default(SharedEntityColumnDefinition.DESCRIPTION.getName())
        {
            @Override
            String getValue(final SharedEntity entity)
            {
                return entity.getDescription();
            }
        };

        static final Default DESCRIPTION_SORT = new Default(SharedEntityColumnDefinition.DESCRIPTION.getSortColumn())
        {
            @Override
            String getValue(final SharedEntity entity)
            {
                final String result = entity.getDescription();
                return (result == null) ? null : FieldIndexerUtil.getValueForSorting(result.toLowerCase());
            }
        };

        static final Default OWNER = new Default(SharedEntityColumnDefinition.OWNER.getName(), Store.YES, Index.NOT_ANALYZED_NO_NORMS )
        {
            @Override
            String getValue(final SharedEntity entity)
            {
                return entity.getOwner() == null ? null : entity.getOwner().getKey();
            }
        };

        static final Default FAVOURITE_COUNT = new Default(SharedEntityColumnDefinition.FAVOURITE_COUNT.getName())
        {
            @Override
            String getValue(final SharedEntity entity)
            {
                return String.valueOf(entity.getFavouriteCount());
            }
        };

        static final List<Default> BUILDERS = Collections.unmodifiableList(Lists.newArrayList(ID, NAME, NAME_SORT, NAME_CASE_INSENSITIVE, DESCRIPTION, DESCRIPTION_SORT, OWNER, FAVOURITE_COUNT));

        private final String fieldName;
        private final Field.Store store;
        private final Field.Index index;

        protected Default(final String fieldName)
        {
            this(fieldName, Field.Store.YES, Index.ANALYZED);
        }

        protected Default(final String fieldName, final Store store, final Index index)
        {
            this.fieldName = fieldName;
            this.index = index;
            this.store = store;
        }

        public Collection<Field> getField(final SharedEntity entity)
        {
            String value = getValue(entity);
            value = (value == null) ? "" : value;
            return Collections.singleton(new Field(fieldName, value, store, index));
        }

        public String getFieldName()
        {
            return fieldName;
        }

        abstract String getValue(final SharedEntity entity);
    }
}
