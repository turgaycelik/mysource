package com.atlassian.jira.issue.search.searchers.renderer;

import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.priority.Priority;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.template.VelocityTemplatingEngine;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.jira.web.FieldVisibilityManager;

import java.util.Collection;

/**
 * Search renderer for Priority.
 *
 * @since v4.0
 */
public class PrioritySearchRenderer extends IssueConstantsSearchRenderer<Priority> implements SearchRenderer
{
    private final ConstantsManager constantsManager;

    public PrioritySearchRenderer(final String searcherNameKey, final ConstantsManager constantsManager, final VelocityRequestContextFactory velocityRequestContextFactory, final ApplicationProperties applicationProperties, final VelocityTemplatingEngine templatingEngine, final FieldVisibilityManager fieldVisibilityManager)
    {
        super(SystemSearchConstants.forPriority(), searcherNameKey, constantsManager, velocityRequestContextFactory,
                applicationProperties, templatingEngine, fieldVisibilityManager);
        this.constantsManager = constantsManager;
    }

    public Collection<Priority> getSelectListOptions(final SearchContext searchContext)
    {
        return constantsManager.getPriorityObjects();
    }
}
