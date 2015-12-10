package com.atlassian.jira.issue.statistics.util;

import com.atlassian.annotations.Internal;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.search.ReaderCache;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.web.FieldVisibilityManager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * A HitCollector that creates a doc -> term mapping.  This is useful for collecting documents where there are a
 * limited number of terms.  The caching also ensures that if multiple searches sort on the same terms, the doc -> term
 * mapping is maintained.
 * <p>
 * This HitCollector can be quite memory intensive, however the cache is stored with a weak reference, so it will
 * be garbage collected. 
 */
@Internal
public class OneDimensionalTermHitCollector extends AbstractOneDimensionalHitCollector
{
    private final Map<String, Tally> result = new HashMap<String, Tally>();

    public OneDimensionalTermHitCollector(final String fieldId,
            final FieldVisibilityManager fieldVisibilityManager, final ReaderCache readerCache,
            final FieldManager fieldManager, final ProjectManager projectManager)
    {
        super(fieldId, fieldVisibilityManager, readerCache, fieldManager, projectManager);
    }

    protected void collectIrrelevant(final int docId)
    {
        // Do nothing we just want the count
    }

    public  Map<String, Integer> getResult()
    {
        return Maps.transformValues(result, new Function<Tally, Integer>()
        {
            @Override
            public Integer apply(@Nullable final Tally input)
            {
                return input.getTally();
            }
        });
    }


    protected void collectWithTerms(final int docId, final Collection<String> terms)
    {
        if (terms == null)
        {
            incrementCount(null, result);
        }
        else
        {
            for (String term : terms)
            {
                incrementCount(term, result);
            }
        }
    }

    private void incrementCount(final String key, final Map<String, Tally> map)
    {
        Tally tally = map.get(key);

        if (tally == null)
        {
            tally = new Tally();
            map.put(key, tally);
        }
        tally.inc();
    }

    private static class Tally
    {
        int tally = 0;

        private Integer getTally()
        {
            return tally;
        }

        private void inc()
        {
            tally++;
        }
    }

}
