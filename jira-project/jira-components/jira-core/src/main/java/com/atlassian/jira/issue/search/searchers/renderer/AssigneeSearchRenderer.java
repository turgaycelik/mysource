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
 * An search renderer for the assignee field.
 *
 * @since v4.0
 */
public class AssigneeSearchRenderer extends AbstractUserSearchRenderer implements SearchRenderer
{
    private final FieldVisibilityManager fieldVisibilityManager;

    public AssigneeSearchRenderer(final String nameKey, final VelocityRequestContextFactory velocityRequestContextFactory,
            final ApplicationProperties applicationProperties, final VelocityTemplatingEngine templatingEngine,
            final UserPickerSearchService searchService, final FieldVisibilityManager fieldVisibilityManager,
            final UserManager userManager,
            final PermissionManager permissionManager)
    {
        super(SystemSearchConstants.forAssignee(), nameKey,
                velocityRequestContextFactory, applicationProperties,
                templatingEngine, searchService, userManager,
                permissionManager);
        this.fieldVisibilityManager = fieldVisibilityManager;
    }

    @Override
    protected List<Map<String, String>> getSelectedListOptions(final User searcher)
    {
        ParameterStore parameterStore = new ParameterStore(searcher);
        return parameterStore.getAssigneeTypes();
    }

    @Override
    protected String getEmptyValueKey()
    {
        return "common.concepts.unassigned";
    }

    /**
     * Returns true or false based on {@link #fieldVisibilityManager} value. Returns false if field is hidden in all
     * schemes, true otherwise.
     *
     * @param user performing this action.
     * @param searchContext search context
     * @return false if hidden in all schemes, true otherwise
     */
    public boolean isShown(final User user, final SearchContext searchContext)
    {
        return !fieldVisibilityManager.isFieldHiddenInAllSchemes(SystemSearchConstants.forAssignee().getFieldId(), searchContext, user);
    }
}
