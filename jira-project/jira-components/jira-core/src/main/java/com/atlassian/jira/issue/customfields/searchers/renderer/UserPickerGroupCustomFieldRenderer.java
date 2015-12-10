package com.atlassian.jira.issue.customfields.searchers.renderer;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.user.search.UserPickerSearchService;
import com.atlassian.jira.bc.user.search.UserSearchParams;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.customfields.CustomFieldUtils;
import com.atlassian.jira.user.ApplicationUsers;
import com.atlassian.jira.user.UserFilter;
import com.atlassian.jira.user.UserFilterManager;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.index.DocumentConstants;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.issue.search.constants.UserFieldSearchConstants;
import com.atlassian.jira.issue.search.searchers.renderer.AbstractUserSearchRenderer;
import com.atlassian.jira.issue.search.searchers.renderer.SearchRenderer;
import com.atlassian.jira.issue.transport.FieldValuesHolder;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.template.VelocityTemplatingEngine;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.jira.web.FieldVisibilityManager;
import com.atlassian.jira.web.bean.I18nBean;

import com.google.common.collect.Maps;
import webwork.action.Action;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @since v4.0
 */
public class UserPickerGroupCustomFieldRenderer extends AbstractUserSearchRenderer implements SearchRenderer
{
    private static final String KEY_VALUE = "value";
    private static final String KEY_KEY = "key";
    private static final String KEY_RELATED = "related";

    private final CustomField field;
    private final FieldVisibilityManager fieldVisibilityManager;
    private final UserFilterManager userFilterManager;


    public UserPickerGroupCustomFieldRenderer(CustomField field, final UserFieldSearchConstants searchConstants, final String nameKey,
            final VelocityRequestContextFactory velocityRequestContextFactory, final ApplicationProperties applicationProperties,
            final VelocityTemplatingEngine templatingEngine, final UserPickerSearchService searchService, final FieldVisibilityManager fieldVisibilityManager,
            final UserManager userManager,
            final PermissionManager permissionManager, final UserFilterManager userFilterManager)
    {
        super(searchConstants, nameKey, velocityRequestContextFactory,
                applicationProperties, templatingEngine, searchService,
                userManager, permissionManager);
        this.field = field;
        this.fieldVisibilityManager = fieldVisibilityManager;
        this.userFilterManager = userFilterManager;
    }

    public boolean isShown(final User user, final SearchContext searchContext)
    {
        return CustomFieldUtils.isShownAndVisible(getCustomField(), user, searchContext, fieldVisibilityManager);
    }

    /**
     * Returns a list of select box options
     * @param searcher  performing this action.
     * @return a list of select box options
     */
    @Override
    protected List<Map<String, String>> getSelectedListOptions(final User searcher)
    {
        final List<Map<String, String>> types = new ArrayList<Map<String, String>>();

        final I18nHelper i18n = new I18nBean(searcher);

        types.add(MapBuilder.<String, String>newBuilder().add(KEY_VALUE, i18n.getText("assignee.types.anyuser")).add(KEY_KEY, null).add(KEY_RELATED, AbstractUserSearchRenderer.SELECT_LIST_NONE).toHashMap());

        // Note that its not possible to search custom fields for no value, and hence the option for 'no value' is missing.

        // If the current user is null (not logged in) do not include the "Current User" as one of the options
        // Fixes: JRA-3341
        if (searcher != null)
        {
            types.add(MapBuilder.<String, String>newBuilder().add(KEY_VALUE, i18n.getText("assignee.types.currentuser"))
                    .add(KEY_KEY, DocumentConstants.ISSUE_CURRENT_USER).add(KEY_RELATED, AbstractUserSearchRenderer.SELECT_LIST_NONE).toHashMap());
        }

        types.add(MapBuilder.<String, String>newBuilder().add(KEY_VALUE, i18n.getText("assignee.types.specifyuser")).add(KEY_KEY, DocumentConstants.SPECIFIC_USER).add(KEY_RELATED,
            AbstractUserSearchRenderer.SELECT_LIST_USER).toHashMap());
        types.add(MapBuilder.<String, String>newBuilder().add(KEY_VALUE, i18n.getText("assignee.types.specifygroup")).add(KEY_KEY, DocumentConstants.SPECIFIC_GROUP).add(KEY_RELATED,
            AbstractUserSearchRenderer.SELECT_LIST_GROUP).toHashMap());

        return types;
    }
    @Override
    protected Map<String, Object> getVelocityParams(final User searcher, final SearchContext searchContext,
            final FieldLayoutItem fieldLayoutItem, final FieldValuesHolder fieldValuesHolder,
            final Map displayParameters, final Action action)
    {
        Map<String, Object> params = super.getVelocityParams(searcher, searchContext, fieldLayoutItem, fieldValuesHolder, displayParameters, action);
        params.put("description", field.getDescriptionProperty().getViewHtml());

        // extract only project and issue types, the field names "projectIds" and "issueTypeIds" match the parameter name of /rest/api/2/groupuserpicker
        Map<String, Object> fieldValuesMap = Maps.newHashMap();
        fieldValuesMap.put("fieldId", field.getId());
        if (fieldValuesHolder != null && !fieldValuesHolder.isEmpty())
        {
            String projectIdKey = SystemSearchConstants.forProject().getUrlParameter();
            if (fieldValuesHolder.containsKey(projectIdKey))
            {
                fieldValuesMap.put("projectId", fieldValuesHolder.get(projectIdKey));
            }
            String issueTypeIdKey = SystemSearchConstants.forIssueType().getUrlParameter();
            if (fieldValuesHolder.containsKey(issueTypeIdKey))
            {
                fieldValuesMap.put("issueTypeId", fieldValuesHolder.get(issueTypeIdKey));
            }
        }
        params.put("fieldValuesMap", fieldValuesMap);
        return params;
    }

    @Override
    protected Map<String, Object> addUserGroupSuggestionParams(final FieldValuesHolder fieldValuesHolder, final User user, final SearchContext searchContext, final List<String> selectedUsers)
    {
        final UserSearchParams userSearchParams = getUserSearchParamsFromSearchContext(user, searchContext);

        final Map<String, Object> params = Maps.newHashMap();
        // if userSearchParams is null, suggestedUsers field will be excluded, but the other fields would still be returned
        userSearcherHelper.addUserGroupSuggestionParams(user, selectedUsers, userSearchParams, params);
        return params;
    }

    /**
     * Try to construct the user filter configuration for the user picker.
     * null if the {@code searchContext} does not match any custom field context.
     */
    private UserSearchParams getUserSearchParamsFromSearchContext(final User user, final SearchContext searchContext)
    {
        final FieldConfig fieldConfig = field.getReleventConfig(searchContext);
        if (fieldConfig != null)
        {
            final UserFilter filter = userFilterManager.getFilter(fieldConfig);
            if (filter != null)
            {
                final Collection<Long> projectIds = CustomFieldUtils.getProjectIdsForUser(
                        ApplicationUsers.from(user), searchContext.getProjectIds(), permissionManager, filter);
                return UserSearchParams.builder().allowEmptyQuery(true)
                        .filter(filter).filterByProjectIds(projectIds).build();
            }
        }

        return null;
    }

    @Override
    protected String getEmptyValueKey()
    {
        return "userpicker.empty";
    }

    private CustomField getCustomField()
    {
        return field;
    }
}
