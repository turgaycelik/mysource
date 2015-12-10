package com.atlassian.jira.issue.search.searchers.renderer;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.issue.search.searchers.transformer.SearchInputTransformer;
import com.atlassian.jira.issue.transport.FieldValuesHolder;
import com.atlassian.jira.template.VelocityTemplatingEngine;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.jira.web.FieldVisibilityManager;
import com.atlassian.query.Query;
import webwork.action.Action;

import java.util.Map;

/**
 * Renderer the produces a simple text input.
 *
 * @since v5.2
 */
public class GenericTextSearchRenderer extends AbstractSearchRenderer implements SearchRenderer
{
    private final String id;
    private final String fieldId;
    private final SearchInputTransformer searchInputTransformer;
    private final FieldVisibilityManager fieldVisibilityManager;

    public GenericTextSearchRenderer(final String id, final String labelKey, final String fieldId,
            final VelocityRequestContextFactory velocityRequestContextFactory,
            final ApplicationProperties applicationProperties, final VelocityTemplatingEngine templatingEngine,
            final SearchInputTransformer searchInputTransformer,
            final FieldVisibilityManager fieldVisibilityManager)
    {
        super(velocityRequestContextFactory, applicationProperties, templatingEngine, id, labelKey);
        this.id = id;
        this.fieldId = fieldId;
        this.searchInputTransformer = searchInputTransformer;
        this.fieldVisibilityManager = fieldVisibilityManager;
    }

    public String getEditHtml(final User user, SearchContext searchContext, FieldValuesHolder fieldValuesHolder, Map displayParameters, Action action)
    {
        String value = "";
        if (fieldValuesHolder != null)
        {
            value = (String) fieldValuesHolder.get(id);
        }

        // else we render the text field
        final Map<String, Object> velocityParams = getVelocityParams(user, searchContext, null, fieldValuesHolder, displayParameters, action);
        velocityParams.put("value", value);
        velocityParams.put("name", id);

        return renderEditTemplate("textquery-edit.vm", velocityParams);
    }

    public boolean isShown(final User user, final SearchContext searchContext)
    {
        // Only displayed in the new issue nav
        return !fieldVisibilityManager.isFieldHiddenInAllSchemes(fieldId, searchContext, user);
    }

    public String getViewHtml(final User user, SearchContext searchContext, FieldValuesHolder fieldValuesHolder, Map displayParameters, Action action)
    {
        String value = "";
        if (fieldValuesHolder != null)
        {
            value = (String) fieldValuesHolder.get(id);
        }

        final Map<String, Object> velocityParams = getVelocityParams(user, searchContext, null, fieldValuesHolder, displayParameters, action);
        velocityParams.put("value", value);
        velocityParams.put("name", id);
        velocityParams.put("fieldId", id);

        return renderViewTemplate("textquery-view.vm", velocityParams);
    }

    public boolean isRelevantForQuery(final User user, final Query query)
    {
        return searchInputTransformer.doRelevantClausesFitFilterForm(user, query, null);

    }

}
