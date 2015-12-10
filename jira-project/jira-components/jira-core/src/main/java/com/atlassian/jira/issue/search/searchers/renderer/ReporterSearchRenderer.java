package com.atlassian.jira.issue.search.searchers.renderer;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.user.search.UserPickerSearchService;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.template.VelocityTemplatingEngine;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.ParameterStore;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.jira.web.FieldVisibilityManager;

import java.util.List;
import java.util.Map;

/**
 * An search renderer for the reporter field.
 *
 * @since v4.0
 */
public class ReporterSearchRenderer extends AbstractUserSearchRenderer implements SearchRenderer
{
    private final FieldVisibilityManager fieldVisibilityManager;

    public ReporterSearchRenderer(String nameKey,
            VelocityRequestContextFactory velocityRequestContextFactory,
            ApplicationProperties applicationProperties,
            VelocityTemplatingEngine templatingEngine,
            UserPickerSearchService searchService, UserManager userManager,
            final PermissionManager permissionManager,
            final FieldVisibilityManager fieldVisibilityManager)
    {
        super(SystemSearchConstants.forReporter(), nameKey,
                velocityRequestContextFactory, applicationProperties,
                templatingEngine, searchService, userManager,
                permissionManager);

        this.fieldVisibilityManager = fieldVisibilityManager;
    }

    @Override
    protected List<Map<String, String>> getSelectedListOptions(final User searcher)
    {
        ParameterStore parameterStore = new ParameterStore(searcher);
        return parameterStore.getReporterTypes();
    }

    @Override
    protected String getEmptyValueKey()
    {
        return "common.concepts.no.reporter";
    }

    public boolean isShown(final User user, SearchContext searchContext)
    {
        return !fieldVisibilityManager.isFieldHiddenInAllSchemes(
                SystemSearchConstants.forReporter().getFieldId(),
                searchContext, user);
    }
}