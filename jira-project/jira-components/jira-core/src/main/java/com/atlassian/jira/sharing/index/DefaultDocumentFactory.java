/**
 * Copyright 2008 Atlassian Pty Ltd
 */
package com.atlassian.jira.sharing.index;

import com.atlassian.jira.sharing.SharedEntity;
import com.atlassian.jira.sharing.type.ShareTypeFactory;
import com.google.common.collect.ImmutableList;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * @since v3.13
 */
class DefaultDocumentFactory implements SharedEntityDocumentFactory
{
    static DefaultDocumentFactory create(final ShareTypeFactory shareTypeFactory)
    {
        return new DefaultDocumentFactory
                (
                        ImmutableList.<SharedEntityFieldFactory>builder().
                                addAll(SharedEntityFieldFactory.Default.BUILDERS).
                                add(new IsSharedFieldFactory()).
                                add(new ShareTypePermissionsFieldFactory(shareTypeFactory)).build()
                );
    }

    private final List<SharedEntityFieldFactory> fieldBuilders;

    private DefaultDocumentFactory(final List<SharedEntityFieldFactory> fieldBuilders)
    {
        this.fieldBuilders = fieldBuilders;
    }

    public Document create(final SharedEntity entity)
    {
        final Collection<Field> fields = new LinkedList<Field>();
        for (final SharedEntityFieldFactory builder : fieldBuilders)
        {
            fields.addAll(builder.getField(entity));
        }
        final Document document = new Document();
        for (final Field field : fields)
        {
            document.add(field);
        }
        return document;
    }
}
