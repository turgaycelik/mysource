package com.atlassian.jira.issue.search.searchers.renderer;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.issue.search.searchers.transformer.SearchInputTransformer;
import com.atlassian.jira.issue.transport.FieldValuesHolder;
import com.atlassian.jira.template.VelocityTemplatingEngine;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.query.Query;
import com.opensymphony.util.TextUtils;
import webwork.action.Action;

import java.util.Map;

/**
 * Renderer the produces a simple text input or just avalue (rendering is done in Javascript).
 *
 * @since v5.2
 */
public class TextQuerySearchRenderer extends AbstractSearchRenderer implements SearchRenderer
{
    private final String name;
    private final SearchInputTransformer searchInputTransformer;

    public TextQuerySearchRenderer(final String id, final String name, final String labelKey,
            VelocityRequestContextFactory velocityRequestContextFactory,
            ApplicationProperties applicationProperties, VelocityTemplatingEngine templatingEngine,
            SearchInputTransformer searchInputTransformer)
    {
        super(velocityRequestContextFactory, applicationProperties, templatingEngine, id, labelKey);
        this.name = name;
        this.searchInputTransformer = searchInputTransformer;
    }

    public String getEditHtml(final User user, SearchContext searchContext, FieldValuesHolder fieldValuesHolder, Map displayParameters, Action action)
    {
        String value = "";
        if (fieldValuesHolder != null)
        {
            value = (String) fieldValuesHolder.get(name);
        }
        // this searcher html is only used as a transport protocol, the client only needs the value out and renders it client-side
        return TextUtils.htmlEncode(value);
    }

    public boolean isShown(final User user, final SearchContext searchContext)
    {
        return true;
    }

    public String getViewHtml(final User user, SearchContext searchContext, FieldValuesHolder fieldValuesHolder, Map displayParameters, Action action)
    {
        String value = "";
        if (fieldValuesHolder != null)
        {
            value = (String) fieldValuesHolder.get(name);
        }
        // this searcher html is only used as a transport protocol, the client only needs the value out and renders it client-side
        return TextUtils.htmlEncode(value);
    }

    public boolean isRelevantForQuery(final User user, final Query query)
    {
        return searchInputTransformer.doRelevantClausesFitFilterForm(user, query, null);

    }

}
