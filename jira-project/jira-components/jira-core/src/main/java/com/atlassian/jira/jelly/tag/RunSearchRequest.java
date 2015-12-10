/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.jelly.tag;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.issue.search.SearchRequestFactory;
import com.atlassian.jira.issue.search.SearchResults;
import com.atlassian.jira.issue.transport.impl.ActionParamsImpl;
import com.atlassian.jira.jelly.UserAwareDynaBeanTagSupport;
import com.atlassian.jira.security.JiraAuthenticationContextImpl;
import com.atlassian.jira.web.bean.PagerFilter;
import com.opensymphony.util.TextUtils;
import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.XMLOutput;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RunSearchRequest extends UserAwareDynaBeanTagSupport
{
    private static final Logger log = Logger.getLogger(RunSearchRequest.class);
    protected static final String KEY_CUSTOM_FIELD_ID = "cfid";
    protected static final String KEY_CUSTOM_FIELD_VALUE = "cfvalue";
    protected static final String KEY_VARIABLE_NAME = "var";
    protected static final String KEY_SIZE_VARIABLE_NAME = "size-var";
    protected static final String KEY_FILTER_ID = "filterid";

    public void doTag(XMLOutput xmlOutput) throws JellyTagException
    {
        Map requestCacheMap = JiraAuthenticationContextImpl.getRequestCache();
        try
        {
            Collection issues;
            SearchRequest searchRequest = getSearchRequest();
            JiraAuthenticationContextImpl.clearRequestCache(); // jelly runs in one thread, so we should always clear the cached PermissionsFilters here.
            final SearchProvider searchProvider = ComponentAccessor.getComponentOfType(SearchProvider.class);
            final SearchResults results = searchProvider.search((searchRequest != null) ? searchRequest.getQuery() : null, getUser(), PagerFilter.getUnlimitedFilter());
            final List<Issue> issueObjs = results.getIssues();

            issues = getIssueGvs(issueObjs);

            String variableName = (String) getProperties().get(KEY_VARIABLE_NAME);
            getContext().setVariable(variableName, issues);

            String sizeVariableName = (String) getProperties().get(KEY_SIZE_VARIABLE_NAME);
            if (TextUtils.stringSet(sizeVariableName))
            {
                getContext().setVariable(sizeVariableName, new Integer(issues.size()));
            }
        }
        catch (SearchException e)
        {
            log.error(e, e);
            throw new JellyTagException(e);
        }
        finally
        {
            // Always reset the request cache after the operation
            if (requestCacheMap != null)
            {
                JiraAuthenticationContextImpl.getRequestCache().putAll(requestCacheMap);
            }
        }
    }

    private Collection<GenericValue> getIssueGvs(final List<Issue> issueObjs)
    {
        if (issueObjs == null || issueObjs.isEmpty())
        {
            return Collections.emptyList();
        }
        else
        {
            final IssueManager issueManager = ComponentAccessor.getIssueManager();
            List<Long> issueIds = new ArrayList<Long>(issueObjs.size());
            for (Issue issueObj : issueObjs)
            {
                issueIds.add(issueObj.getId());
            }
            return issueManager.getIssues(issueIds);
        }
    }

    protected SearchRequest getSearchRequest() throws JellyTagException
    {
        // Get all available proerties from the tag
        String filterId = (String) getProperties().get(KEY_FILTER_ID);
        String cfId = (String) getProperties().get(KEY_CUSTOM_FIELD_ID);
        String cfValue = (String) getProperties().get(KEY_CUSTOM_FIELD_VALUE);

        // Priority is given to the Filter, not the custom field.
        if (StringUtils.isNotEmpty(filterId))
        {
            return buildSearchRequestFromFilter(filterId);
        }
        else
        {
            return buildSearchRequestFromCustomField(cfId, cfValue);
        }
    }

    private SearchRequest buildSearchRequestFromCustomField(String cfId, String cfValue)
            throws JellyTagException
    {
        Map<String, String []> params = new HashMap<String, String []>();
        if (StringUtils.isNotEmpty(cfId))
        {
            String[] cfValues = new String[] {cfValue};
            CustomField customField = ComponentAccessor.getCustomFieldManager().getCustomFieldObject(cfId);
            if (customField != null)
            {
                params.put(customField.getId(), cfValues);
            }
            else
            {
                throw new JellyTagException("Custom field with id '" + cfId + "' does not exist.");
            }
        }
        return ComponentAccessor.getComponent(SearchRequestFactory.class).createFromParameters(null, getUser(), new ActionParamsImpl(params));
    }

    private SearchRequest buildSearchRequestFromFilter(String filterId)
            throws JellyTagException
    {
        final JiraServiceContext ctx = new JiraServiceContextImpl(getUser());
        final SearchRequest searchRequest = ComponentManager.getInstance().getSearchRequestService().getFilter(ctx, new Long(filterId));
        if (searchRequest != null)
        {
            return searchRequest;
        }
        else
        {
            throw new JellyTagException("Search Filter with id "+filterId+" did not exist");
        }
    }
}
