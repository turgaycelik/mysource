package com.atlassian.jira.issue.customfields.searchers.renderer;

import com.atlassian.annotations.PublicSpi;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.customfields.CustomFieldUtils;
import com.atlassian.jira.issue.customfields.CustomFieldValueProvider;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.search.ClauseNames;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.issue.search.searchers.impl.NamedTerminalClauseCollectingVisitor;
import com.atlassian.jira.issue.search.searchers.renderer.SearchRenderer;
import com.atlassian.jira.issue.transport.FieldValuesHolder;
import com.atlassian.jira.plugin.customfield.CustomFieldSearcherModuleDescriptor;
import com.atlassian.jira.web.FieldVisibilityManager;
import com.atlassian.query.Query;
import webwork.action.Action;

import java.util.HashMap;
import java.util.Map;

/**
 * An abstract implementation of a renderer for custom fields.
 *
 * It delegates to the searcher module descriptor for actual rendering. The value is retrieved from the
 * field values holder using the provided Custom Field Value Provider.
 *
 * @since v4.0
 */
@PublicSpi
public class CustomFieldRenderer implements SearchRenderer
{
    private final ClauseNames clauseNames;
    private final CustomFieldSearcherModuleDescriptor customFieldSearcherModuleDescriptor;
    private final CustomField field;
    private final CustomFieldValueProvider customFieldValueProvider;
    private final FieldVisibilityManager fieldVisibilityManager;

    public CustomFieldRenderer(ClauseNames clauseNames,
            CustomFieldSearcherModuleDescriptor customFieldSearcherModuleDescriptor, CustomField field,
            CustomFieldValueProvider customFieldValueProvider, FieldVisibilityManager fieldVisibilityManager)
    {
        this.clauseNames = clauseNames;
        this.customFieldSearcherModuleDescriptor = customFieldSearcherModuleDescriptor;
        this.field = field;
        this.customFieldValueProvider = customFieldValueProvider;
        this.fieldVisibilityManager = fieldVisibilityManager;
    }

    public boolean isRelevantForQuery(final User user, Query query)
    {
        return isExistsInQuery(query);
    }

    public String getEditHtml(final User user, SearchContext searchContext, FieldValuesHolder fieldValuesHolder, Map<?, ?> displayParameters, Action action)
    {
        HashMap<String, Object> velocityParams = new HashMap<String, Object>();
        velocityParams.put("fieldkey", field.getCustomFieldType().getKey());
        return getEditHtml(searchContext, fieldValuesHolder, displayParameters, action, velocityParams);
    }

    public String getViewHtml(final User user, SearchContext searchContext, FieldValuesHolder fieldValuesHolder, Map<?, ?> displayParameters, Action action)
    {
        HashMap<String, Object> velocityParams = new HashMap<String, Object>();
        velocityParams.put("fieldkey", field.getCustomFieldType().getKey());
        return getViewHtml(searchContext, fieldValuesHolder, displayParameters, action, velocityParams);
    }

    public CustomField getField()
    {
        return field;
    }

    public boolean isShown(final User user, SearchContext searchContext)
    {
        return CustomFieldUtils.isShownAndVisible(getField(), user, searchContext, fieldVisibilityManager);
    }

    public String getEditHtml(SearchContext searchContext, FieldValuesHolder fieldValuesHolder, Map<?, ?> displayParameters, Action action, Map<String, Object> velocityParams)
    {
        return getDescriptor().getSearchHtml(getField(),
                                             customFieldValueProvider,
                                             searchContext,
                                             fieldValuesHolder,
                                             displayParameters,
                                             action,
                                             velocityParams);
    }

    public String getViewHtml(SearchContext searchContext, FieldValuesHolder fieldValuesHolder, Map<?, ?> displayParameters, Action action, Map<String, Object> velocityParams)
    {
        return getDescriptor().getViewHtml(getField(),
                                           customFieldValueProvider,
                                           searchContext,
                                           fieldValuesHolder,
                                           displayParameters,
                                           action,
                                           velocityParams);
    }

    CustomFieldSearcherModuleDescriptor getDescriptor()
    {
        return customFieldSearcherModuleDescriptor;
    }

    boolean isExistsInQuery(Query query)
    {
        final NamedTerminalClauseCollectingVisitor clauseVisitor = new NamedTerminalClauseCollectingVisitor(clauseNames.getJqlFieldNames());
        if (query != null && query.getWhereClause() != null)
        {
            query.getWhereClause().accept(clauseVisitor);
        }
        return clauseVisitor.containsNamedClause();
    }
}
