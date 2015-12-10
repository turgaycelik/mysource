package com.atlassian.jira.web.component.multigrouppicker;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.template.VelocityTemplatingEngine;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.JiraVelocityUtils;
import com.atlassian.jira.web.component.AbstractWebComponent;
import com.atlassian.jira.web.component.PickerLayoutBean;
import com.atlassian.jira.web.component.WebComponentUtils;
import com.atlassian.plugin.webresource.WebResourceManager;
import com.atlassian.util.profiling.UtilTimerStack;

import java.util.Collection;
import java.util.Map;

public class GroupPickerWebComponent extends AbstractWebComponent
{
    private final JiraAuthenticationContext authenticationContext = ComponentAccessor.getComponentOfType(JiraAuthenticationContext.class);

    public GroupPickerWebComponent()
    {
        super(ComponentAccessor.getComponent(VelocityTemplatingEngine.class), ComponentAccessor.getApplicationProperties());
    }

    public String getHtml(final PickerLayoutBean layoutBean, final Collection currentGroups, final boolean canEdit, final Long id, final Map startingParams)
    {
        return getHtml(layoutBean, currentGroups, canEdit, id, startingParams, authenticationContext.getI18nHelper());

    }

    public String getHtml(final PickerLayoutBean layoutBean, final Collection currentGroups, final boolean canEdit, final Long id, final Map startingParams, final I18nHelper i18n)
    {
        try
        {
            UtilTimerStack.push("GroupPickerHtml");

            final boolean canPickGroups = ComponentAccessor.getPermissionManager().hasPermission(Permissions.USER_PICKER,
                authenticationContext.getLoggedInUser());
            startingParams.putAll(EasyMap.build("layout", layoutBean, "currentSelections", currentGroups, "i18n", i18n, "canEdit",
                Boolean.valueOf(canEdit), "id", id, "canPick", Boolean.valueOf(canPickGroups), "windowName", "GroupPicker"));
            final Map<String, Object> params = JiraVelocityUtils.getDefaultVelocityParams(startingParams, authenticationContext);

            final WebResourceManager webResourceManager = ComponentAccessor.getWebResourceManager();
            webResourceManager.requireResource("jira.webresources:autocomplete");

            return getHtml("templates/jira/multipicker/pickertable.vm", params);
        }
        finally
        {
            UtilTimerStack.pop("GroupPickerHtml");
        }
    }

    public static Collection<String> getGroupNamesToRemove(final Map params, final String paramPrefix)
    {
        return WebComponentUtils.getRemovalValues(params, paramPrefix);
    }

    public static Collection<String> getGroupNamesToAdd(final String rawGroupNames)
    {
        return WebComponentUtils.convertStringToCollection(rawGroupNames);
    }

}
