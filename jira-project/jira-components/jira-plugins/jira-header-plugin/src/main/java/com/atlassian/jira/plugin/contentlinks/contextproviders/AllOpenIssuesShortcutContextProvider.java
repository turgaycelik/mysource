package com.atlassian.jira.plugin.contentlinks.contextproviders;

import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.jql.util.JqlStringSupport;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.web.ContextProvider;
import com.atlassian.query.order.SortOrder;
import com.google.common.collect.ImmutableMap;

import java.util.Map;

import static com.atlassian.jira.util.JiraUrlCodec.encode;

/**
 * Provides the velocity context keys required to render the &quot;All Open Issues&quot; content link.
 *
 * @since v6.0
 */
public class AllOpenIssuesShortcutContextProvider implements ContextProvider
{
    private final JqlStringSupport jqlStringSupport;

    public AllOpenIssuesShortcutContextProvider(final JqlStringSupport jqlStringSupport)
    {
        this.jqlStringSupport = jqlStringSupport;
    }

    @Override
    public void init(Map<String, String> params) throws PluginParseException {}

    @Override
    public Map<String, Object> getContextMap(final Map<String, Object> context)
    {
        final JqlQueryBuilder builder =
                JqlQueryBuilder.newBuilder().
                        where().project().eq((String) context.get("key")).and().unresolved().endWhere().
                        orderBy().priority(SortOrder.DESC).endOrderBy();

        return ImmutableMap.<String, Object>of
                (
                        "openIssuesQuery",
                        encode(jqlStringSupport.generateJqlString(builder.buildQuery()))
                );
    }
}
