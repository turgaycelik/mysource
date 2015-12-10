package com.atlassian.jira.issue.search.parameters.lucene.sort;

import java.io.IOException;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import com.atlassian.annotations.ExperimentalApi;

import com.google.common.collect.Lists;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.index.TermEnum;

/**
 * This used to be a cache of values but it was found that it consumed a hell of a lot of memory for no benefit
 * (JRA-10111). So the cache.put was never called.
 * <p/>
 * This has been refactored into a "finder" of terms values for fields within documents.
 */
public class JiraLuceneFieldFinder
{
    private static final JiraLuceneFieldFinder FIELD_FINDER = new JiraLuceneFieldFinder();

    public static JiraLuceneFieldFinder getInstance()
    {
        return FIELD_FINDER;

    }

    /**
     * This is used to retrieve values from the Lucence index.  It returns an array that is the same size as the number
     * of documents in the reader and will have all null values if the field is not present, otherwise it has the values
     * of the field within the document.
     *
     * @param reader the Lucence index reader
     * @param field the name of the field to find
     * @param mappedSortComparator the MappedSortComparator that we are acting on behalf of
     * @return an non null array of values, which may contain null values.
     * @throws IOException if things dont play out well.
     */
    public Object[] getCustom(IndexReader reader, final String field, MappedSortComparator mappedSortComparator)
            throws IOException
    {
        String internedField = field.intern();
        final Object[] retArray = new Object[reader.maxDoc()];
        if (retArray.length > 0)
        {
            TermDocs termDocs = reader.termDocs();
            TermEnum termEnum = reader.terms(new Term(internedField, ""));
            try
            {
                // if we dont have a term in any of the documents
                // then an array of null values is what we should return
                if (termEnum.term() == null)
                {
                    return retArray;
                }
                Comparator comparator = mappedSortComparator.getComparator();
                do
                {
                    Term term = termEnum.term();
                    // Because Lucence interns fields for us this is a bit quicker
                    //noinspection StringEquality
                    if (term.field() != internedField)
                    {
                        // if the next term is not our field then none of those
                        // terms are present in the set of documents and hence
                        // an array of null values is what we should return
                        break;
                    }
                    Object termval = mappedSortComparator.getComparable(term.text());
                    termDocs.seek(termEnum);
                    while (termDocs.next())
                    {
                        Object currentValue = retArray[termDocs.doc()];
                        //only replace the value if it is earlier than the current value
                        //noinspection unchecked
                        if (currentValue == null || comparator.compare(termval, currentValue) < 1)
                        {
                            retArray[termDocs.doc()] = termval;
                        }
                    }
                }
                while (termEnum.next());
            }
            finally
            {
                termDocs.close();
                termEnum.close();
            }
        }
        return retArray;
    }

    /**
     * This method is used to retrieve term values form the Lucene index. It returns a list of the text
     * representation of the terms that are present in a field - this list will be empty if a field has no terms.
     *
     * @param reader the Lucence index reader
     * @param field the name of the field to find
     * @return  a List of strings that represent the term values for a specified field
     * @throws IOException
     */
    public List<String> getTermValuesForField(IndexReader reader, final String field)
            throws IOException
    {
        final List<String> terms  = Lists.newArrayList();
        if (field != null) 
        {
            final String internedField = field.intern();
            TermEnum termEnum = reader.terms(new Term(internedField, ""));
            do {
                Term currTerm = termEnum.term();
                // Because Lucence interns fields for us this is a bit quicker
                //noinspection StringEquality
                if (currTerm == null || currTerm.field() != internedField) {
                    break;
                }
                terms.add(currTerm.text());
            } while (termEnum.next());
        }
        return terms;
    }


    /**
     * This method checks if a field contains a term.  
     *
     * @param reader the Lucence index reader
     * @param field the name of the field to test
     * @param term the term you are looking for in the field
     * @return  a boolean indicating ehether the field contains this term
     * @throws IOException
     */
    public boolean doesFieldContainTerm(IndexReader reader, final String field, final String term) 
            throws IOException
    {
        if (field != null) 
        {
            final String internedField = field.intern();
            final Term searchTerm =  new Term(field, term);
            final TermEnum terms = reader.terms(searchTerm);
            return (searchTerm.equals(terms.term()));
        }
        return false;
    }

    /**
     * For each document in the index, it returns an array of string collections for each matching term.
     * Uses the {@link DefaultMatchHandler}.
     *
     * @param reader the index to read
     * @param field the field to check the documents for
     * @return an array of string collections for each term for each document
     * @throws IOException if things dont play out well.
     */
    public Collection<String>[] getMatches(IndexReader reader, final String field) throws IOException
    {
        final int maxDoc = reader.maxDoc();
        final DefaultMatchHandler handler = new DefaultMatchHandler(maxDoc);
        getMatches(reader, field, handler);
        return handler.getResults();
    }

    /**
     * For each document in the index, it returns an array of string collections for each matching term.
     * Uses the {@link SingleValueMatchHandler}, so any collections that are returned are guaranteed
     * to contain exactly one (possibly <tt>null</tt>) value in them.
     *
     * @param reader the index to read
     * @param field the field to check the documents for
     * @return an array of string collections for each term for each document
     * @throws IOException if things dont play out well.
     */
    @ExperimentalApi
    public List<String>[] getUniqueMatches(IndexReader reader, final String field) throws IOException
    {
        final int maxDoc = reader.maxDoc();
        final SingleValueMatchHandler handler = new SingleValueMatchHandler(maxDoc);
        getMatches(reader, field, handler);
        return handler.getResults();
    }

    /**
     * For each document that has at least one value defined for the specified field,
     * invokes {@link MatchHandler#handleMatchedDocument(int, String)} with the document
     * index and the field value as the argument.
     *
     * @param reader the index to read
     * @param field the field to check the documents for
     * @param handler a handler that will be invoked for each matching term
     * @throws IOException if things dont play out well.
     */
    @ExperimentalApi
    public void getMatches(IndexReader reader, final String field, MatchHandler handler) throws IOException
    {
        if (reader.maxDoc() == 0)
        {
            return;
        }
        String internedField = field.intern();
        TermDocs termDocs = reader.termDocs();
        try
        {
            TermEnum termEnum = reader.terms(new Term(internedField, ""));
            try
            {
                if (termEnum.term() == null)
                {
                    return;
                }
                do
                {
                    Term term = termEnum.term();
                    // Because Lucence interns fields for us this is a bit quicker
                    //noinspection StringEquality
                    if (term.field() != internedField)
                    {
                        break;
                    }
                    String termval = term.text();
                    termDocs.seek(termEnum);
                    while (termDocs.next())
                    {
                        handler.handleMatchedDocument(termDocs.doc(), termval);
                    }
                }
                while (termEnum.next());
            }
            finally
            {
                termEnum.close();
            }
        }
        finally
        {
            termDocs.close();
        }
    }
}
