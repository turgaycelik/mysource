package com.atlassian.jira.issue.search.searchers.renderer;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.avatar.Avatar;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.bc.user.search.UserPickerSearchService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.issue.search.constants.UserFieldSearchConstants;
import com.atlassian.jira.issue.search.constants.UserFieldSearchConstantsWithEmpty;
import com.atlassian.jira.issue.search.searchers.transformer.UserSearchInput;
import com.atlassian.jira.issue.search.searchers.util.UserSearcherHelper;
import com.atlassian.jira.issue.transport.FieldValuesHolder;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.template.VelocityTemplatingEngine;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.plugin.webresource.WebResourceManager;
import com.atlassian.query.Query;
import com.google.common.collect.Maps;
import webwork.action.Action;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An search renderer for the user fields.
 *
 * @since v4.0
 */
public abstract class AbstractUserSearchRenderer extends AbstractSearchRenderer implements SearchRenderer
{
    public static final String SELECT_LIST_NONE = "select.list.none";
    public static final String SELECT_LIST_USER = "select.list.user";
    public static final String SELECT_LIST_GROUP = "select.list.group";

    private final UserFieldSearchConstants searchConstants;
    private final String emptySelectFlag;
    private final String nameKey;
    private final ApplicationProperties applicationProperties;
    private final UserPickerSearchService searchService;
    private final UserManager userManager;
    protected final PermissionManager permissionManager;
    protected UserSearcherHelper userSearcherHelper;

    public AbstractUserSearchRenderer(UserFieldSearchConstantsWithEmpty searchConstants, String nameKey, VelocityRequestContextFactory velocityRequestContextFactory, ApplicationProperties applicationProperties, VelocityTemplatingEngine templatingEngine, UserPickerSearchService searchService, UserManager userManager,
            final PermissionManager permissionManager)
    {
        this(searchConstants, searchConstants.getEmptySelectFlag(), nameKey, velocityRequestContextFactory, applicationProperties, templatingEngine, searchService, userManager, permissionManager,
                ComponentAccessor.getComponent(UserSearcherHelper.class));
    }

    public AbstractUserSearchRenderer(UserFieldSearchConstants searchConstants, String nameKey, VelocityRequestContextFactory velocityRequestContextFactory, ApplicationProperties applicationProperties, VelocityTemplatingEngine templatingEngine, UserPickerSearchService searchService, UserManager userManager,
            final PermissionManager permissionManager)
    {
        this(searchConstants, null, nameKey, velocityRequestContextFactory, applicationProperties, templatingEngine, searchService, userManager, permissionManager,
                ComponentAccessor.getComponent(UserSearcherHelper.class));
    }

    private AbstractUserSearchRenderer(UserFieldSearchConstants searchConstants,
            final String emptySelectFlag,
            final String nameKey,
            final VelocityRequestContextFactory velocityRequestContextFactory,
            final ApplicationProperties applicationProperties,
            final VelocityTemplatingEngine templatingEngine,
            final UserPickerSearchService searchService,
            final UserManager userManager,
            final PermissionManager permissionManager,
            final UserSearcherHelper userSearcherHelper)
    {
        super(velocityRequestContextFactory, applicationProperties, templatingEngine, searchConstants.getSearcherId(), nameKey);
        this.emptySelectFlag = emptySelectFlag;
        this.searchConstants = searchConstants;
        this.nameKey = nameKey;
        this.applicationProperties = applicationProperties;
        this.searchService = searchService;
        this.userManager = userManager;
        this.permissionManager = permissionManager;
        this.userSearcherHelper = userSearcherHelper;
    }

    public String getEditHtml(final User user, final SearchContext searchContext, final FieldValuesHolder fieldValuesHolder, final Map displayParameters, final Action action)
    {
        final Map<String, Object> velocityParams = getVelocityParams(user, searchContext, null, fieldValuesHolder, displayParameters, action);
        velocityParams.put("selectListOptions", getSelectedListOptions(user));
        return renderEditTemplate("user-searcher-edit.vm", velocityParams);
    }

    public String getViewHtml(final User user, final SearchContext searchContext, final FieldValuesHolder fieldValuesHolder, final Map displayParameters, final Action action)
    {
        final Map<String, Object> velocityParams = getVelocityParams(user, searchContext, null, fieldValuesHolder, displayParameters, action);
        return renderViewTemplate("user-searcher-view.vm", velocityParams);
    }

    public boolean isRelevantForQuery(final User user, final Query query)
    {
        return isRelevantForQuery(searchConstants.getJqlClauseNames(), query);
    }
    // ----------------------------------------------------------------------------------------------------- View Helper

    /**
     * @param searcher performing the action.
     * @return the select list options that are displayed for this user searcher (e.g. SpecificUser, CurrentUser...)
     */
    protected abstract List<Map<String, String>> getSelectedListOptions(final User searcher);

    /**
     * @return the i18n key for the text that describes an empty value for this searcher.
     */
    protected abstract String getEmptyValueKey();

    protected String getTextForuser(final User searcher, final FieldValuesHolder fieldValuesHolder)
    {
        final String selectList = (String) fieldValuesHolder.get(searchConstants.getSelectUrlParameter());
        if (emptySelectFlag != null && emptySelectFlag.equals(selectList))
        {
            return getI18n(searcher).getText(getEmptyValueKey());
        }
        else if (searchConstants.getCurrentUserSelectFlag().equals(selectList))
        {
            return getI18n(searcher).getText("reporter.types.currentuser");
        }
        else
        {
            return (String) fieldValuesHolder.get(searchConstants.getFieldUrlParameter());
        }
    }

    private String getLinkedUser(final User searcher, final FieldValuesHolder fieldValuesHolder)
    {
        final String selectList = (String) fieldValuesHolder.get(searchConstants.getSelectUrlParameter());
        if (searchConstants.getCurrentUserSelectFlag().equals(selectList))
        {
            if (searcher != null)
            {
                return searcher.getName();
            }
        }
        else if (searchConstants.getSpecificUserSelectFlag().equals(selectList))
        {
            return (String) fieldValuesHolder.get(searchConstants.getFieldUrlParameter());
        }

        return null;
    }

    private String getLinkedGroup(final User searcher, final FieldValuesHolder fieldValuesHolder)
    {
        final String selectList = (String) fieldValuesHolder.get(searchConstants.getSelectUrlParameter());
        if (searchConstants.getSpecificGroupSelectFlag().equals(selectList))
        {
            if (ComponentAccessor.getPermissionManager().hasPermission(Permissions.ADMINISTER, searcher))
            {
                return (String) fieldValuesHolder.get(searchConstants.getFieldUrlParameter());
            }
        }

        return null;
    }

    @Override
    protected Map<String, Object> getVelocityParams(final User searcher, final SearchContext searchContext, final FieldLayoutItem fieldLayoutItem, final FieldValuesHolder fieldValuesHolder, final Map displayParameters, final Action action)
    {
        final Map<String, Object> velocityParams = super.getVelocityParams(searcher, searchContext, fieldLayoutItem, fieldValuesHolder, displayParameters, action);
        final JiraServiceContext ctx = new JiraServiceContextImpl(searcher);
        final boolean canPerformAjaxSearch = searchService.canPerformAjaxSearch(ctx);

        velocityParams.put("emptyValueKey", this.getEmptyValueKey());
        velocityParams.put("name", getI18n(searcher).getText(nameKey));
        velocityParams.put("userField", searchConstants.getFieldUrlParameter());
        velocityParams.put("userSelect", searchConstants.getSelectUrlParameter());
        velocityParams.put("hasPermissionToPickUsers", userSearcherHelper.hasUserPickingPermission(searcher));

        final WebResourceManager webResourceManager = ComponentAccessor.getWebResourceManager();
        webResourceManager.requireResource("jira.webresources:autocomplete");

        if (canPerformAjaxSearch)
        {
            velocityParams.put("canPerformAjaxSearch", "true");
            velocityParams.put("ajaxLimit", applicationProperties.getDefaultBackedString(APKeys.JIRA_AJAX_AUTOCOMPLETE_LIMIT));
        }

        velocityParams.putAll(getKickassVelocityParams(fieldValuesHolder, searcher, searchContext));

        return velocityParams;
    }

    /**
     * @param fieldValuesHolder Contains the values we're rendering.
     * @param user The user performing the search.
     * @return A map containing all Kickass-specific velocity parameters.
     */
    @SuppressWarnings ("unchecked")
    private Map<String, Object> getKickassVelocityParams(
            final FieldValuesHolder fieldValuesHolder, final User user, final SearchContext searchContext)
    {
        String key = searchConstants.getFieldUrlParameter();
        List<UserSearchInput> values =
                (List<UserSearchInput>) fieldValuesHolder.get(key);

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("avatarSize", Avatar.Size.SMALL);
        params.put("hasCurrentUser", false);
        params.put("hasEmpty", false);
        params.putAll(addUserGroupSuggestionParams(fieldValuesHolder, user, searchContext, extractUserNames(values)));
        if (values != null)
        {
            for (UserSearchInput value : values)
            {
                if (value.isCurrentUser())
                {
                    params.put("hasCurrentUser", true);
                }
                else if (value.isEmpty())
                {
                    params.put("hasEmpty", true);
                }
                else if (value.isGroup())
                {
                    value.setObject(userManager.getGroup(value.getValue()));
                }
                else if (value.isUser())
                {
                    value.setObject(userManager.getUser(value.getValue()));
                }
            }

            Collections.sort(values);
        }

        params.put("values", values);
        return params;
    }

    /**
     * add user and group suggestions into parameters. subclasses could override it to provide customized suggestions.
     *
     * default implementation returns recently selected users and the first few users from the system, if not enough recently selected users,
     * and returns the groups the user is in, or first few groups in the system, if the user is not specified.
     *
     * @param fieldValuesHolder
     * @param user
     * @param searchContext
     * @param selectedUsers recently selected users
     * @return
     */
    protected Map<String, Object> addUserGroupSuggestionParams(final FieldValuesHolder fieldValuesHolder, final User user, final SearchContext searchContext, final List<String> selectedUsers)
    {
        Map<String, Object> params = Maps.newHashMap();
        userSearcherHelper.addUserGroupSuggestionParams(user, selectedUsers, params);
        return params;
    }

    /**
     * For any InputType.USER values, return their usernames
     */
    private List<String> extractUserNames(List<UserSearchInput> values)
    {
        List<String> result = new ArrayList<String>();
        if (values != null)
        {
            for (UserSearchInput value : values)
            {
                if (value.isUser()) {
                    result.add(value.getValue());
                }
            }
        }
        return result;
    }



}