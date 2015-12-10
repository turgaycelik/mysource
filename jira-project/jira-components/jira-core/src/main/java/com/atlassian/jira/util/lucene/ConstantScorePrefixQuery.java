package com.atlassian.jira.util.lucene;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.TermQuery;

/**
 * Creates {@link PrefixQuery} instances that doesn't rewrite into a {@link BooleanQuery} with all matching
 * {@link TermQuery terms} in the field. This query returns a constant score equal
 * to its boost for all documents with the matching prefix term.
 * <p>
 * This can be significantly cheaper and faster if there are a lot of matching terms.
 * It is very slightly slower if the number of matched terms is one or two.
 * <p>
 * @see http://jira.atlassian.com/browse/JRA-17623
 * @since 4.0
 */
public class ConstantScorePrefixQuery
{
    public static PrefixQuery build(final Term term)
    {
        final PrefixQuery prefixQuery = new PrefixQuery(term);
        prefixQuery.setRewriteMethod(PrefixQuery.CONSTANT_SCORE_FILTER_REWRITE);
        return prefixQuery;
    }
}