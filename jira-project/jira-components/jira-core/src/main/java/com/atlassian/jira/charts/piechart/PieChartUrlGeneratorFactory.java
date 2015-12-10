package com.atlassian.jira.charts.piechart;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.charts.PieSegment;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.issue.search.SearchRequestAppender;
import com.atlassian.jira.issue.search.util.RedundantClausesQueryOptimizer;
import com.atlassian.jira.util.dbc.Assertions;
import com.atlassian.jira.util.velocity.DefaultVelocityRequestContextFactory;
import com.atlassian.jira.util.velocity.VelocityRequestContext;
import com.atlassian.query.Query;
import org.jfree.chart.urls.CategoryURLGenerator;
import org.jfree.chart.urls.PieURLGenerator;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.general.PieDataset;

import java.util.Collection;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

/**
 * This class provides methods to generate various JFreeChart URLGenerator objects for pie charts.
 *
 * @since v6.0
 */
public class PieChartUrlGeneratorFactory
{
    private final SearchService searchService;
    private final VelocityRequestContext velocityRequestContext;
    private final SearchRequestAppender searchRequestAppender;
    private final User remoteUser;
    private final SearchRequest baseSearchRequest;

    public PieChartUrlGeneratorFactory(SearchService searchService, ApplicationProperties applicationProperties, SearchRequestAppender searchRequestAppender, User remoteUser, SearchRequest baseSearchRequest)
    {
        this.searchService = Assertions.notNull("searchService", searchService);
        this.searchRequestAppender = Assertions.notNull("searchRequestAppender", searchRequestAppender);
        this.remoteUser = remoteUser;
        this.baseSearchRequest = Assertions.notNull("baseSearchRequest", baseSearchRequest);
        velocityRequestContext = new DefaultVelocityRequestContextFactory(applicationProperties).getJiraVelocityRequestContext();
    }

    public CategoryURLGenerator getCategoryUrlGenerator()
    {
        return new CategoryURLGenerator()
        {
            public String generateURL(final CategoryDataset categoryDataset, final int row, final int col)
            {
                final Comparable key = categoryDataset.getColumnKey(col);
                if (key instanceof PieSegment)
                {
                    final Object statisticKey = ((PieSegment) key).getKey();
                    final SearchRequest searchRequest = searchRequestAppender.appendInclusiveSingleValueClause(statisticKey, baseSearchRequest);
                    return createIssueNavigatorUrl(searchRequest, velocityRequestContext, remoteUser);
                }

                return null;
            }
        };
    }

    public PieURLGenerator getPieUrlGenerator(final Comparable otherSegmentKey)
    {
        return new PieURLGenerator()
        {
            public String generateURL(final PieDataset dataset, final Comparable pieSegmentKey, final int pieSectionIdx)
            {
                if (pieSegmentKey instanceof PieSegment && ((PieSegment) pieSegmentKey).isGenerateUrl())
                {
                    final PieSegment pieSegment = (PieSegment) pieSegmentKey;
                    final Object statisticKey = pieSegment.getKey();

                    final SearchRequest searchRequestForPieSegment = searchRequestAppender.appendInclusiveSingleValueClause(statisticKey, baseSearchRequest);
                    return createIssueNavigatorUrl(searchRequestForPieSegment, velocityRequestContext, remoteUser);
                }
                // Deliberate use of == here, since the "Other" key is actually a static final Object passed in from PieChart
                else if (pieSegmentKey == otherSegmentKey)
                {
                    final Iterable allStatisticKeys = getAllStatisticKeys(dataset);
                    final SearchRequest searchRequestForPieSegment = searchRequestAppender.appendExclusiveMultiValueClause(allStatisticKeys, baseSearchRequest);
                    return createIssueNavigatorUrl(searchRequestForPieSegment, velocityRequestContext, remoteUser);
                }
                else
                {
                    return null;
                }
            }

        };
    }

    private static Collection getAllStatisticKeys(PieDataset pieDataset)
    {
        final List statisticKeys = newArrayList();
        final List segmentKeys = pieDataset.getKeys();
        for (final Object segmentKey : segmentKeys)
        {
            if (segmentKey instanceof PieSegment)
            {
                statisticKeys.add(((PieSegment) segmentKey).getKey());
            }
        }
        return statisticKeys;
    }

    private String createIssueNavigatorUrl(final SearchRequest searchRequest, final VelocityRequestContext velocityRequestContext, final User remoteUser)
    {
        if (searchRequest == null)
        {
            return null;
        }
        else
        {
            final Query optimizedQuery = new RedundantClausesQueryOptimizer().optimizeQuery(searchRequest.getQuery());
            return velocityRequestContext.getCanonicalBaseUrl() + "/secure/IssueNavigator.jspa?reset=true" + searchService.getQueryString(remoteUser, optimizedQuery);
        }
    }
}