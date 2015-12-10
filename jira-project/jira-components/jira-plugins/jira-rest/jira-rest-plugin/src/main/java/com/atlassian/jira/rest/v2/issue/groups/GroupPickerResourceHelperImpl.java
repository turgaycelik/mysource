package com.atlassian.jira.rest.v2.issue.groups;

import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.jira.bc.group.search.GroupPickerSearchService;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.rest.v2.issue.GroupSuggestionBean;
import com.atlassian.jira.rest.v2.issue.GroupSuggestionsBean;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.util.DelimeterInserter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.opensymphony.util.TextUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;

@Component
public class GroupPickerResourceHelperImpl implements GroupPickerResourceHelper
{

    final Logger LOG = Logger.getLogger(GroupPickerResourceHelperImpl.class);
    public static final String MORE_GROUP_RESULTS_I18N_KEY = "jira.ajax.autocomplete.group.more.results";
    public static final int DEFAULT_MAX_RESULTS = 20;

    private GroupPickerSearchService service;
    private JiraAuthenticationContext authenticationContext;
    private ApplicationProperties applicationProperties;
    private final PermissionManager permissionManager;

    @Autowired
    public GroupPickerResourceHelperImpl(GroupPickerSearchService service, JiraAuthenticationContext authenticationContext, ApplicationProperties applicationProperties, PermissionManager permissionManager)
    {
        this.service = service;
        this.authenticationContext = authenticationContext;
        this.applicationProperties = applicationProperties;
        this.permissionManager = permissionManager;
    }

    /**
     * Gets group search results as a bean
     *
     * @param query -  a String to match groups against
     * @param excludeGroups - groups to exclude from search
     * @return bean that represents the search results with meta data
     */
    @Override
    public GroupSuggestionsBean findGroupsAsBean(String query, List<String> excludeGroups, Integer maxResults)
    {
        final List<GroupSuggestionBean> groupBeans = Lists.newArrayList();
        List<Group> groups = Lists.newArrayList();

        if (excludeGroups == null) {
            excludeGroups = ImmutableList.of();
        }

        if (permissionManager.hasPermission(Permissions.USER_PICKER, authenticationContext.getLoggedInUser())) {
            groups = service.findGroups(query);
        } else {
            Group group = service.getGroupByName(query);
            if (group != null) {
                groups.add(group);
            }
        }

        int total = limitGroupResults(maxResults, groups, excludeGroups, groupBeans, query);

        final String header = buildHeader(groupBeans, total);
        return new GroupSuggestionsBean(total, header, groupBeans);
    }

    private int limitGroupResults(final Integer maxResults, final List<Group> groups, final List<String> excludeGroups, final List<GroupSuggestionBean> groupBeans, final String query)
    {
        int limit = getLimit(maxResults);
        int i = 0;
        int total = groups.size();

        for (final Group group : groups)
        {
            if(i < limit)
            {
                if (!excludeGroups.contains(group.getName())) {
                    final String matchingHtml = buildMatchingHtml(group.getName(), query);
                    groupBeans.add(new GroupSuggestionBean(group.getName(), matchingHtml));
                    ++i;
                } else {
                    --total;
                }
            }
            else
            {
                break;
            }
        }

        return total;
    }

    // get the number of items to display.
    private int getLimit(Integer maxResults)
    {
        //Default limit to 20
        if (maxResults != null)
        {
            return maxResults;
        }
        else
        {
            int limit = DEFAULT_MAX_RESULTS;
            try
            {
                limit = Integer.valueOf(applicationProperties.getDefaultBackedString(APKeys.JIRA_AJAX_AUTOCOMPLETE_LIMIT));
            }
            catch (Exception nfe)
            {
                LOG.error(APKeys.JIRA_AJAX_AUTOCOMPLETE_LIMIT + " does not exist or is an invalid number in jira-application.properties. Using default value " + DEFAULT_MAX_RESULTS, nfe);
            }
            return limit;
        }
    }

    private String buildMatchingHtml(final String group, final String query)
    {
        final DelimeterInserter delimeterInserter = new DelimeterInserter("<b>", "</b>", false);
        final String matchingHtml = delimeterInserter.insert(TextUtils.htmlEncode(group), new String[] { query });
        return matchingHtml;
    }

    private String buildHeader(final Collection<GroupSuggestionBean> groupBeans, int total)
    {
        return authenticationContext.getI18nHelper().getText(MORE_GROUP_RESULTS_I18N_KEY,
                String.valueOf(groupBeans.size()), String.valueOf(total));
    }
}
