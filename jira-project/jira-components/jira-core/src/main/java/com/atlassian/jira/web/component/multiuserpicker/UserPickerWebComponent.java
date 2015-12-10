package com.atlassian.jira.web.component.multiuserpicker;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.bc.user.search.UserPickerSearchService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.template.VelocityTemplatingEngine;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.JiraVelocityUtils;
import com.atlassian.jira.web.component.AbstractWebComponent;
import com.atlassian.jira.web.component.WebComponentUtils;
import com.atlassian.plugin.webresource.WebResourceManager;
import com.atlassian.util.profiling.UtilTimerStack;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserPickerWebComponent extends AbstractWebComponent
{
    private final JiraAuthenticationContext authenticationContext = ComponentAccessor.getComponentOfType(JiraAuthenticationContext.class);
    private final UserPickerSearchService searchService;

    public UserPickerWebComponent(final VelocityTemplatingEngine templatingEngine, final ApplicationProperties applicationProperties, final UserPickerSearchService searchService)
    {
        super(templatingEngine, applicationProperties);
        this.searchService = searchService;
    }

    public String getHtml(final UserPickerLayoutBean layoutBean, final Collection<User> currentUsers, final boolean canEdit, final Long id)
    {
        final List<UserBean> userBeans = UserBean.convertUsersToUserBeans(authenticationContext.getLocale(), currentUsers);
        return getHtml(layoutBean, userBeans, canEdit, id, getI18nBean());
    }

    public String getHtmlForUsernames(final UserPickerLayoutBean layoutBean, final List<String> usernames, final boolean canEdit, final Long id)
    {
        final List<UserBean> userBeans = UserBean.convertUsernamesToUserBeans(authenticationContext.getLocale(), usernames);
        return getHtml(layoutBean, userBeans, canEdit, id, getI18nBean());
    }

    private String getHtml(final UserPickerLayoutBean layoutBean, final List<UserBean> currentUsers, final boolean canEdit, final Long id, final I18nHelper i18n)
    {
        try
        {
            UtilTimerStack.push("UserPickerHtml");

            User loggedInUser = authenticationContext.getLoggedInUser();
            final boolean canPickUsers = ComponentAccessor.getPermissionManager().hasPermission(Permissions.USER_PICKER, loggedInUser);
            final Map<String, Object> startingParams = new HashMap<String, Object>();
            startingParams.put("userUtil", ComponentAccessor.getUserUtil());
            startingParams.put("layout", layoutBean);
            startingParams.put("currentSelections", currentUsers);
            startingParams.put("i18n", i18n);
            startingParams.put("canEdit", canEdit);
            startingParams.put("id", id);
            startingParams.put("canPick", canPickUsers);
            startingParams.put("windowName", "UserPicker");
            final Map<String, Object> velocityParams = JiraVelocityUtils.getDefaultVelocityParams(startingParams, authenticationContext);

            final JiraServiceContext ctx = new JiraServiceContextImpl(loggedInUser);

            final boolean canPerformAjaxSearch = searchService.canPerformAjaxSearch(ctx);
            if (canPerformAjaxSearch)
            {
                velocityParams.put("canPerformAjaxSearch", "true");
                velocityParams.put("ajaxLimit", applicationProperties.getDefaultBackedString(APKeys.JIRA_AJAX_AUTOCOMPLETE_LIMIT));
            }
            final WebResourceManager webResourceManager = ComponentAccessor.getWebResourceManager();
            webResourceManager.requireResource("jira.webresources:autocomplete");
            return getHtml("templates/jira/multipicker/pickertable.vm", velocityParams);
        }
        finally
        {
            UtilTimerStack.pop("UserPickerHtml");
        }
    }

    public static Collection<String> getUserNamesToRemove(final Map<String,?> params, final String paramPrefix)
    {
        return WebComponentUtils.getRemovalValues(params, paramPrefix);
    }

    public static Collection<String> getUserNamesToAdd(final String rawUserNames)
    {
        return WebComponentUtils.convertStringToCollection(rawUserNames);
    }

    private I18nHelper getI18nBean()
    {
        return authenticationContext.getI18nHelper();
    }

}
