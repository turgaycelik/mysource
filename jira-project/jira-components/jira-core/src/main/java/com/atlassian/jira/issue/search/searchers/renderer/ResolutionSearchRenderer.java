package com.atlassian.jira.issue.search.searchers.renderer;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.resolution.Resolution;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.issue.transport.FieldValuesHolder;
import com.atlassian.jira.template.VelocityTemplatingEngine;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.jira.web.FieldVisibilityManager;
import webwork.action.Action;

import java.util.Collection;
import java.util.Map;

/**
 * A search renderer for the resolution.
 *
 * @since v4.0
 */
public class ResolutionSearchRenderer extends IssueConstantsSearchRenderer<Resolution>
{
    private final ConstantsManager constantsManager;

    public ResolutionSearchRenderer(final String searcherNameKey, final ConstantsManager constantsManager,
            final VelocityRequestContextFactory velocityRequestContextFactory, final ApplicationProperties applicationProperties,
            final VelocityTemplatingEngine templatingEngine, final FieldVisibilityManager fieldVisibilityManager)
    {
        super(SystemSearchConstants.forResolution(), searcherNameKey, constantsManager, velocityRequestContextFactory,
                applicationProperties, templatingEngine, fieldVisibilityManager);
        this.constantsManager = constantsManager;
    }

    public Collection<Resolution> getSelectListOptions(final SearchContext searchContext)
    {
        return constantsManager.getResolutionObjects();
    }

    @Override
    protected Map getVelocityParams(final User searcher, SearchContext searchContext, FieldLayoutItem fieldLayoutItem, FieldValuesHolder fieldValuesHolder, Map displayParameters, Action action)
    {
        final Map velocityParams = super.getVelocityParams(searcher, searchContext, fieldLayoutItem, fieldValuesHolder, displayParameters, action);
        velocityParams.put("extraOption", EasyMap.build("value", getI18n(searcher).getText("common.status.unresolved"), "key", "-1"));
        return velocityParams;
    }

}
