package com.atlassian.jira.issue.index.indexers.impl;

import java.sql.Timestamp;
import java.util.List;
import java.util.Locale;

import com.atlassian.jira.association.NodeAssociationStore;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.datetime.LocalDate;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueRelationConstants;
import com.atlassian.jira.issue.index.DocumentConstants;
import com.atlassian.jira.issue.index.indexers.FieldIndexer;
import com.atlassian.jira.util.CaseFolding;
import com.atlassian.jira.util.LuceneUtils;
import com.atlassian.jira.web.FieldVisibilityManager;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.NumberTools;

public abstract class BaseFieldIndexer implements FieldIndexer
{
    // ------------------------------------------------------------------------------------------------------- Constants
    // ------------------------------------------------------------------------------------------------- Type Properties
    // ---------------------------------------------------------------------------------------------------- Dependencies
    private static final Logger log = Logger.getLogger(BaseFieldIndexer.class);

    private final FieldVisibilityManager fieldVisibilityManager;

    // ---------------------------------------------------------------------------------------------------- Constructors
    protected BaseFieldIndexer(final FieldVisibilityManager fieldVisibilityManager)
    {
        this.fieldVisibilityManager = fieldVisibilityManager;
    }

    // -------------------------------------------------------------------------------------------------- Public Methods

    Field.Index unanalyzed(final Issue issue)
    {
        return (isFieldVisibleAndInScope(issue)) ? Field.Index.NOT_ANALYZED_NO_NORMS : Field.Index.NO;
    }

    /**
     * Index a single keyword field
     * @param doc the document to add the field to.
     * @param indexField the document field name to user.
     * @param fieldValue the value to index. This value will NOT be folded before adding it to the document.
     * @param issue the issue that defines the context and contains the value we are indexing.
     */
    public void indexKeyword(final Document doc, final String indexField, final String fieldValue, final Issue issue)
    {
        if (StringUtils.isNotBlank(fieldValue))
        {
            doc.add(new Field(indexField, fieldValue, Field.Store.YES, unanalyzed(issue)));
        }
    }

    /**
     * Case fold the passed keyword and add it to the passed document.
     *
     * @param doc the document to add the field to.
     * @param indexField the document field name to use.
     * @param fieldValue the value to index. This value will be folded before adding it to the document.
     * @param locale the locale to use in the case folding. Null can be passed to use the Locale given in {@link java.util.Locale#getDefault()}.
     * @param issue the issue that defines the context and contains the value we are indexing.
     *
     * @see com.atlassian.jira.util.CaseFolding
     */
    public void indexFoldedKeyword(final Document doc, final String indexField, final String fieldValue, final Locale locale, final Issue issue)
    {
        if (StringUtils.isNotBlank(fieldValue))
        {
            final Locale actualLocale = locale == null ? Locale.getDefault() : locale;
            if (isFieldVisibleAndInScope(issue))
            {
                doc.add(new Field(indexField, CaseFolding.foldString(fieldValue, actualLocale), Field.Store.NO, Field.Index.NOT_ANALYZED_NO_NORMS));
            }
            // Since we don't store we don't need to add it at all if it is not visible
        }
    }

    /**
     * Index a single keyword field, with a default if the issue field is not set
     *
     * shared with CommentDocumentFactory
     */
    public void indexKeywordWithDefault(final Document doc, final String indexField, final String fieldValue, final String defaultValue, final Issue issue)
    {
        FieldIndexerUtil.indexKeywordWithDefault(doc, indexField, fieldValue, defaultValue, isFieldVisibleAndInScope(issue));
    }

    public void indexKeywordWithDefault(final Document doc, final String indexField, final Long aLong, final String defaultValue, final Issue issue)
    {
        final String value = aLong != null ? aLong.toString() : null;
        indexKeywordWithDefault(doc, indexField, value, defaultValue, issue);
    }

    /**
     * Useful for storing a {@link Long} value for range query searches.
     * Uses {@link org.apache.lucene.document.NumberTools#longToString(long)} to pad and convert the value to base 36
     * before indexing. Does not pad the default value.
     *
     * @param doc the document
     * @param indexField the index field id
     * @param aLong the long value to be converted
     * @param defaultValue the default value if aLong is null
     * @param issue the issue that defines the context and contains the value we are indexing.
     */
    public void indexLongAsPaddedKeywordWithDefault(final Document doc, final String indexField, final Long aLong, final String defaultValue, final Issue issue)
    {
        final String value = aLong != null ? NumberTools.longToString(aLong) : null;
        indexKeywordWithDefault(doc, indexField, value, defaultValue, issue);
    }

    /**
     * Useful for storing a {@link Long} value for range query searches.
     * Uses {@link org.apache.lucene.document.NumberTools#longToString(long)} to pad and convert the values to base 36
     * before indexing. Also pads and converts the default Value.
     *
     * @param doc the document
     * @param indexField the index field id
     * @param aLong the long value to be converted
     * @param defaultLong the default value if aLong is null
     * @param issue the issue that defines the context and contains the value we are indexing.
     */
    public void indexLongAsPaddedKeywordWithDefault(final Document doc, final String indexField, final Long aLong, final Long defaultLong, final Issue issue)
    {
        final String value = aLong != null ? NumberTools.longToString(aLong) : null;
        final String defaultValue = defaultLong != null ? NumberTools.longToString(defaultLong) : null;
        indexKeywordWithDefault(doc, indexField, value, defaultValue, issue);
    }

    /**
     * Index a single text field
     */
    public void indexText(final Document doc, final String indexField, final String fieldValue, final Issue issue)
    {
        if (StringUtils.isNotBlank(fieldValue))
        {
            if (isFieldVisibleAndInScope(issue))
            {
                doc.add(new Field(indexField, fieldValue, Field.Store.YES, Field.Index.ANALYZED));
            }
            else
            {
                doc.add(new Field(indexField, fieldValue, Field.Store.YES, Field.Index.NO));
            }
        }
    }

    /**
     * Index a single keyword field, with a date-time value
     */
    public void indexDateField(final Document doc, final String indexField, final Timestamp date, final Issue issue)
    {
        final Field.Index indexType = unanalyzed(issue);
        if (date != null)
        {
            doc.add(new Field(indexField, LuceneUtils.dateToString(date), Field.Store.YES, indexType));
        }
        if (indexType == Field.Index.NOT_ANALYZED_NO_NORMS)
        {
            doc.add(new Field(DocumentConstants.LUCENE_SORTFIELD_PREFIX + indexField, LuceneUtils.dateToString(date), Field.Store.NO, unanalyzed(issue)));
        }
    }

    /**
     * Index a single keyword field, with a LocalDate value
     */
    public void indexLocalDateField(final Document doc, final String indexField, final LocalDate localDate, final Issue issue)
    {
        final Field.Index indexType = unanalyzed(issue);
        if (localDate != null)
        {
            doc.add(new Field(indexField, LuceneUtils.localDateToString(localDate), Field.Store.YES, indexType));
        }
        if (indexType == Field.Index.NOT_ANALYZED_NO_NORMS)
        {
            doc.add(new Field(DocumentConstants.LUCENE_SORTFIELD_PREFIX + indexField, LuceneUtils.localDateToString(localDate), Field.Store.NO, unanalyzed(issue)));
        }
    }

    /**
     * Index the dependent entities of an issue.
     */
    public void indexDependentEntities(final Issue issue, final Document doc, final String associationType, final String indexField)
    {
        // This code is copied from DefaultIssueManager.getEntitiesByIssue(), as this enables us to *just* get back the ids that we require,
        // and not have to load up the GenericValues
        String sinkName;
        if (associationType.equals(IssueRelationConstants.COMPONENT))
        {
            sinkName = "Component";
        }
        else if (associationType.equals(IssueRelationConstants.FIX_VERSION))
        {
            sinkName = "Version";
        }
        else if (associationType.equals(IssueRelationConstants.VERSION))
        {
            sinkName = "Version";
        }
        else
        {
            throw new IllegalArgumentException("Assocation Type " + associationType + " not handled");
        }

        final List<Long> ids = ComponentAccessor.getComponent(NodeAssociationStore.class).getSinkIdsFromSource(issue.getGenericValue(), sinkName, associationType);

        if ((ids == null) || ids.isEmpty())
        {
            doc.add(new Field(indexField, NO_VALUE_INDEX_VALUE, Field.Store.YES, unanalyzed(issue)));
        }
        else
        {
            final Field.Index index = unanalyzed(issue);
            for (final Long id : ids)
            {
                doc.add(new Field(indexField, id.toString(), Field.Store.YES, index));
            }
        }
    }

    public void indexLongAsKeyword(final Document doc, final String indexField, final Long fieldValue, final Issue issue)
    {
        if (fieldValue != null)
        {
            doc.add(new Field(indexField, fieldValue.toString(), Field.Store.YES, unanalyzed(issue)));
        }
    }

    /**
     * Index a single text field
     */
    public void indexTextForSorting(final Document doc, final String indexField, final String fieldValue, final Issue issue)
    {
        final String string = FieldIndexerUtil.getValueForSorting(fieldValue);
        if (StringUtils.isNotBlank(string) && isFieldVisibleAndInScope(issue))
        {
            doc.add(new Field(indexField, string, Field.Store.NO, Field.Index.NOT_ANALYZED_NO_NORMS));
        }
    }

    @Override
    public boolean equals(final Object o)
    {
        if (!(o instanceof FieldIndexer))
        {
            return false;
        }
        final FieldIndexer rhs = (FieldIndexer) o;
        return new EqualsBuilder().append(getId(), rhs.getId()).isEquals();
    }

    @Override
    public int hashCode()
    {
        return getId().hashCode() * 41;
    }

    public int compareTo(final Object obj)
    {
        final FieldIndexer o = (FieldIndexer) obj;
        return new CompareToBuilder().append(getId(), o.getId()).toComparison();
    }

    public boolean isFieldVisibleAndInScope(final Issue issue)
    {
        return !fieldVisibilityManager.isFieldHidden(getId(), issue);
    }
}
