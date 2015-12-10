package com.atlassian.jira.issue.index.indexers;

import com.atlassian.annotations.PublicSpi;
import com.atlassian.jira.issue.Issue;
import org.apache.lucene.document.Document;

/**
 * This is meant to create a portion of a {@link org.apache.lucene.document.Document} that is relevant for a JIRA
 * field.
 *
 * The portion that is added to the Document will be the indexed value that is represented by the value contained
 * in the Issue object when the {@link #addIndex(org.apache.lucene.document.Document, com.atlassian.jira.issue.Issue)}
 * method is called.
 *
 * There is a strong relationship between a FieldIndexer and a {@link com.atlassian.jira.jql.query.ClauseQueryFactory}
 * as the indexer creates a portion of the lucene Document and the query factory assumes it knows what this looks
 * like and can generate a Lucene query that will find relevant issues based on the values that were indexed.
 *
 * If you are writting a CustomFieldSearcher then the glue that binds the FieldIndexer and the ClauseQueryFactory
 * together is the {@link com.atlassian.jira.issue.customfields.CustomFieldSearcher} since it provides both
 * the ClauseHandler and the FieldIndexer. Keep in mind that if you are creating one of these that the Indexer
 * must add fields to the document that the ClauseQueryFactory knows how to search.
 */
@PublicSpi
public interface FieldIndexer
{
    /** General empty token */
    public static final String NO_VALUE_INDEX_VALUE = "-1";
    /** Empty token specific to LabelsIndexer */
    public static final String LABELS_NO_VALUE_INDEX_VALUE = "<EMPTY>";

    /**
     * @return the String representation of the field id that this indexer is indexing, this must be unique for
     * each independant FieldIndexer. If the Indexer does not represent a System or Custom field in JIRA this
     * should still return a unique string that describes the indexer.
     */
    String getId();

    /**
     * @return the String representation of the primary field id that will be added to the
     * {@link org.apache.lucene.document.Document} as a result of a successful call to the
     * {@link #addIndex(org.apache.lucene.document.Document, com.atlassian.jira.issue.Issue)} method.
     */
    String getDocumentFieldId();

    /**
     * This method allows an indexer the opportunity to modifiy the provided Lucene document (by reference) such
     * that it will contain fields that are relevant for searching and storage of the portion of the issue that
     * the FieldIndexer handles.
     *
     * If calling {@link #isFieldVisibleAndInScope(com.atlassian.jira.issue.Issue)} returns false then
     * this method should create fields that have an Indexed type of {@link org.apache.lucene.document.Field.Index#NO}.
     * This allows us to store the value in the index but renderes its value unsearchable.
     *
     * If, for example, the indexer handles indexing an issues summary then this indexer will add a field to
     * the document that represents the stored and searchable summary of the issue.
     *
     * @param doc the lucene document that should be modified by adding relevant fields to.
     * @param issue the issue that contains the data that will be indexed and which can be used to determine
     * the project/issue type context that will allow you to determine if we should add the value as searchable
     * or unsearchable.
     */
    void addIndex(Document doc, Issue issue);

    /**
     * This method is used to determine if the indexer is relevant for the provided issue. This method must check
     * the fields visibility, in relation to the field configuration scheme, must check any global flags that would
     * enable or disable a field (such as enable votes flag), and must check, if the field is a custom field, if
     * the custom field is relevant for this issue.
     *
     * All these checks should take into account the {@link com.atlassian.jira.issue.context.IssueContext} as defined by
     * the passed in issue.
     *
     * If this method returns false then the FieldIndexer, when performing addIndex, should make sure to make the
     * indexed values have an Indexed type of {@link org.apache.lucene.document.Field.Index#NO}.
     *
     * The result of this method is used to determine the correct values that should be returned when performing
     * an empty search.
     *
     * @param issue that is having a document created from.
     * @return if true then this field is relevant for the issue, otherwise it is not.
     */
    boolean isFieldVisibleAndInScope(final Issue issue);
}
