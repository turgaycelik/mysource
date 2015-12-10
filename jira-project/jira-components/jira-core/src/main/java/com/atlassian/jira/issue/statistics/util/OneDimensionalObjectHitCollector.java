package com.atlassian.jira.issue.statistics.util;

import com.atlassian.annotations.ExperimentalApi;
import com.atlassian.jira.issue.search.parameters.lucene.sort.JiraLuceneFieldFinder;
import com.atlassian.jira.issue.statistics.StatisticsMapper;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.Scorer;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

/**
 * A HitCollector that creates a doc -> object mapping.  This is useful for collecting documents where there are a
 * limited number of terms.  The caching also ensures that if multiple searches sort on the same terms, the doc ->
 * object mapping is maintained.
 * <p/>
 * This HitCollector can be quite memory intensive, however the cache is stored with a weak reference, so it will be
 * garbage collected.
 * <p/>
 * This HitCollector differs from {@link OneDimensionalTermHitCollector} in that it performs the term -> object
 * conversion here, rather than later.  This is more expensive, but useful for StatisticsMappers that perform some sort
 * of runtime conversion / translation (eg a StatisticsMapper that groups dates by Month, or groups users by email
 * domain name).
 */
public class OneDimensionalObjectHitCollector extends Collector
{
    private StatisticsMapper statisticsMapper;
    private final Map<Object, Integer> result;
    private Collection<String>[] docToTerms;
    private int docBase = 0;
    private final boolean isUnique;

    /**
     * Creates a normal one-dimensional object hit collector.  This is equivalent to
     * {@link #OneDimensionalObjectHitCollector(StatisticsMapper, Map, boolean)}} with
     * <tt>false</tt> provided for <tt>isUnique</tt>, which should be safe for use with all fields.
     * However, if it is known in advance that the field contains at most one value for the field,
     * then specifying <tt>isUnique</tt> will be more efficient.
     *
     * @param statisticsMapper provides the information required to map the field
     * @param result the map to which the result will be published
     */
    public OneDimensionalObjectHitCollector(StatisticsMapper statisticsMapper, Map result)
    {
        this(statisticsMapper, result, false);
    }

    /**
     * Allows the caller to specify whether or not the hit collector may assume that the
     * field will never have more than one value for any issue.  This allows the hit collector
     * to be much more efficient in gathering the data for that field, but it will give incorrect
     * results if the field can contain multiple values.  Examples of suitable fields where
     * <tt>isUnique</tt> should be set include the project id, issue type, created date, and
     * resolved date.
     *
     * @param statisticsMapper provides the information required to map the field
     * @param result the map to which the result will be published
     * @param isUnique <tt>true</tt> if the field can safely be assumed to have at most one value
     *      for any issue; <tt>false</tt> if the field might be allowed to contain multiple values
     *      per issue
     */
    @ExperimentalApi
    public OneDimensionalObjectHitCollector(StatisticsMapper statisticsMapper, Map result, boolean isUnique)
    {
        //noinspection unchecked
        this.result = result;
        this.statisticsMapper = statisticsMapper;
        this.isUnique = isUnique;
    }

    public void collect(int i)
    {
        adjustMapForValues(result, docToTerms[i]);
    }

    @Override
    public void setScorer(Scorer scorer) throws IOException
    {
        // Do nothing
    }

    @Override
    public void setNextReader(IndexReader reader, int docBase) throws IOException
    {
        this.docBase = docBase;
        try
        {
            if (isUnique)
            {
                docToTerms = JiraLuceneFieldFinder.getInstance().getUniqueMatches(reader, statisticsMapper.getDocumentConstant());
            }
            else
            {
                docToTerms = JiraLuceneFieldFinder.getInstance().getMatches(reader, statisticsMapper.getDocumentConstant());
            }
        }
        catch (IOException e)
        {
            //ignore
        }
    }

    @Override
    public boolean acceptsDocsOutOfOrder()
    {
        return true;
    }

    private void adjustMapForValues(Map<Object, Integer> map, Collection<String> terms)
    {
        if (terms == null)
        {
            return;
        }
        for (String term : terms)
        {
            Object object = statisticsMapper.getValueFromLuceneField(term);
            if (object == null)  // We actually index null dates in a special way so they sort to the end.
            {
                continue;
            }
            Integer count = map.get(object);
            map.put(object, (count == null) ? 1 : (count + 1));
        }
    }

}
