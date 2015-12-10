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
 * An search renderer for the creator field.
 *
 * @since v6.2
 */
public class CreatorSearchRenderer extends AbstractUserSearchRenderer implements SearchRenderer
{
    private final FieldVisibilityManager fieldVisibilityManager;

    public CreatorSearchRenderer(String nameKey,
            VelocityRequestContextFactory velocityRequestContextFactory,
            ApplicationProperties applicationProperties,
            VelocityTemplatingEngine templatingEngine,
            UserPickerSearchService searchService, UserManager userManager,
            final PermissionManager permissionManager,
            final FieldVisibilityManager fieldVisibilityManager)
    {
        super(SystemSearchConstants.forCreator(), nameKey,
                velocityRequestContextFactory, applicationProperties,
                templatingEngine, searchService, userManager,
                permissionManager);

        this.fieldVisibilityManager = fieldVisibilityManager;
    }

    @Override
    protected List<Map<String, String>> getSelectedListOptions(final User searcher)
    {
        ParameterStore parameterStore = new ParameterStore(searcher);
        return parameterStore.getCreatorTypes();
    }

    @Override
    protected String getEmptyValueKey()
    {
        return "common.concepts.anonymous.creator";
    }

    public boolean isShown(final User user, SearchContext searchContext)
    {
        return !fieldVisibilityManager.isFieldHiddenInAllSchemes(
                SystemSearchConstants.forCreator().getFieldId(),
                searchContext, user);
    }
}