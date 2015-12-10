package com.atlassian.jira.issue.search.searchers.renderer;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.fields.LabelsSystemField;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.issue.search.constants.SimpleFieldSearchConstantsWithEmpty;
import com.atlassian.jira.issue.transport.FieldValuesHolder;
import com.atlassian.jira.template.VelocityTemplatingEngine;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.jira.web.FieldVisibilityManager;
import com.atlassian.query.Query;
import com.google.common.base.Joiner;
import webwork.action.Action;

import java.util.Map;

/**
 * A search renderer for the Labels searcher
 *
 * @since v4.2
 */
public class LabelsSearchRenderer extends AbstractSearchRenderer implements SearchRenderer
{
    public static final String SEPARATOR_CHAR = ", ";

    private final SimpleFieldSearchConstantsWithEmpty constants;
    private FieldVisibilityManager fieldVisibilityManager;

    public LabelsSearchRenderer(SimpleFieldSearchConstantsWithEmpty constants, VelocityRequestContextFactory velocityRequestContextFactory,
            FieldVisibilityManager fieldVisibilityManager, ApplicationProperties applicationProperties, VelocityTemplatingEngine templatingEngine,
            String nameKey)
    {
        super(velocityRequestContextFactory, applicationProperties, templatingEngine, constants.getSearcherId(), nameKey);
        this.constants = constants;
        this.fieldVisibilityManager = fieldVisibilityManager;
    }

    public String getEditHtml(final User user, SearchContext searchContext, FieldValuesHolder fieldValuesHolder, Map displayParameters, Action action)
    {
        final Map<String, Object> velocityParams = getVelocityParams(user, searchContext, null, fieldValuesHolder, displayParameters, action);
        velocityParams.put("labels", getLabels(fieldValuesHolder));
        return renderEditTemplate("labels-searcher-edit.vm", velocityParams);
    }

    public String getViewHtml(final User user, SearchContext searchContext, FieldValuesHolder fieldValuesHolder, Map displayParameters, Action action)
    {
        final Map<String, Object> velocityParams = getVelocityParams(user, searchContext, null, fieldValuesHolder, displayParameters, action);
        velocityParams.put("labelString", getLabelString(fieldValuesHolder));
        return renderViewTemplate("labels-searcher-view.vm", velocityParams);
    }

    private String getLabelString(final FieldValuesHolder fieldValuesHolder)
    {
        return Joiner.on(SEPARATOR_CHAR).join(getLabels(fieldValuesHolder));
    }

    private Iterable<?> getLabels(FieldValuesHolder fieldValuesHolder)
    {
        return (Iterable<?>) fieldValuesHolder.get("labels");
    }

    public boolean isShown(final User user, final SearchContext searchContext)
    {
        return !fieldVisibilityManager.isFieldHiddenInAllSchemes(constants.getFieldId(), searchContext, user);
    }

    public boolean isRelevantForQuery(final User user, final Query query)
    {
        return isRelevantForQuery(constants.getJqlClauseNames(), query);
    }
}
