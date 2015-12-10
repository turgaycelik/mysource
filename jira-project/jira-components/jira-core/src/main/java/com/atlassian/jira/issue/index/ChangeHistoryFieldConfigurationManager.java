package com.atlassian.jira.issue.index;

import com.atlassian.jira.cluster.ClusterSafe;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.history.AssigneeDateRangeBuilder;
import com.atlassian.jira.issue.history.DateRangeBuilder;
import com.atlassian.jira.issue.history.PriorityDateRangeBuilder;
import com.atlassian.jira.issue.history.ReporterDateRangeBuilder;
import com.atlassian.jira.issue.history.ResolutionDateRangeBuilder;
import com.atlassian.jira.issue.history.StatusDateRangeBuilder;
import com.atlassian.jira.issue.history.VersionDateRangeBuilder;
import com.atlassian.jira.issue.index.indexers.impl.BaseFieldIndexer;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.jql.resolver.NameResolver;
import com.atlassian.jira.jql.resolver.PriorityResolver;
import com.atlassian.jira.jql.resolver.ResolutionResolver;
import com.atlassian.jira.jql.resolver.StatusResolver;
import com.atlassian.jira.jql.resolver.UserResolver;
import com.atlassian.jira.jql.resolver.VersionResolver;
import com.atlassian.jira.util.ComponentLocator;
import com.atlassian.util.concurrent.LazyReference;
import com.google.common.collect.ImmutableMap;

import java.util.Map;
import java.util.Set;

/**
 *
 *
 * @since v4.4
 */
public class ChangeHistoryFieldConfigurationManager
{
    @ClusterSafe
    private final  LazyReference<Map<String,ChangeHistoryFieldConfiguration>> ref = new LazyReference<Map<String,ChangeHistoryFieldConfiguration>>()
    {

        @Override
        protected Map<String, ChangeHistoryFieldConfiguration> create() throws Exception
        {
            return loadConfiguration();
        }
    };
    private final ComponentLocator componentLocator;

    public ChangeHistoryFieldConfigurationManager(ComponentLocator componentLocator)
    {
        this.componentLocator = componentLocator;
    }

    private Map<String, ChangeHistoryFieldConfiguration> loadConfiguration()
    {
        return ImmutableMap.<String, ChangeHistoryFieldConfiguration>builder().put(IssueFieldConstants.STATUS, new ChangeHistoryFieldConfiguration(new StatusDateRangeBuilder(),BaseFieldIndexer.NO_VALUE_INDEX_VALUE, componentLocator.getComponent(StatusResolver.class), true)).
                                                         put(IssueFieldConstants.ASSIGNEE, new ChangeHistoryFieldConfiguration(new AssigneeDateRangeBuilder(), DocumentConstants.ISSUE_UNASSIGNED,  componentLocator.getComponent(UserResolver.class), true)).
                                                         put(IssueFieldConstants.REPORTER, new ChangeHistoryFieldConfiguration(new ReporterDateRangeBuilder(), DocumentConstants.ISSUE_NO_AUTHOR, componentLocator.getComponent(UserResolver.class), true)).
                                                         put(IssueFieldConstants.RESOLUTION, new ChangeHistoryFieldConfiguration(new ResolutionDateRangeBuilder(), BaseFieldIndexer.NO_VALUE_INDEX_VALUE, componentLocator.getComponent(ResolutionResolver.class), true)).
                                                         put(IssueFieldConstants.PRIORITY, new ChangeHistoryFieldConfiguration(new PriorityDateRangeBuilder(), BaseFieldIndexer.NO_VALUE_INDEX_VALUE, componentLocator.getComponent(PriorityResolver.class), true)).
                                                         put(SystemSearchConstants.FIX_FOR_VERSION,  new ChangeHistoryFieldConfiguration(new VersionDateRangeBuilder(SystemSearchConstants.FIX_FOR_VERSION,
                                                              BaseFieldIndexer.NO_VALUE_INDEX_VALUE), BaseFieldIndexer.NO_VALUE_INDEX_VALUE, componentLocator.getComponent(VersionResolver.class), true)).build();
    }

    public Set<String> getAllFieldNames()
    {
        return ref.get().keySet();
    }

    public  DateRangeBuilder getDateRangeBuilder(String fieldName)
    {
        return  ref.get().get(fieldName).getDateRangeBuilder();
    }

    public String getEmptyValue(String fieldName)
    {
        return  ref.get().get(fieldName).getEmptyValue();
    }

    public boolean supportsIdSearching(String fieldName)
    {
         if (ref.get().get(fieldName) != null) {
            return  ref.get().get(fieldName).supportsIdSearching();
         }
         else
         {
             return false;
         }
    }

    public NameResolver getNameResolver(String fieldName)
    {
        return ref.get().get(fieldName).getNameResolver();
    }
}
