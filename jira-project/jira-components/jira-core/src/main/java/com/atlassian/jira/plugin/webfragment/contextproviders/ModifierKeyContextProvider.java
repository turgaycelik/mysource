package com.atlassian.jira.plugin.webfragment.contextproviders;

import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.BrowserUtils;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.web.ContextProvider;

import java.util.Map;

/**
 * Provides:
 * the modifier key. Referenced by "${modifierKey}"
 * the submit access key. Referenced by "${submitAccessKey}"
 * the submit title. Referenced by "${submitTitle}"
 *
 * the cancel access key. Referenced by "${cancelAccessKey}"
 * the cancel title. Referenced by "${cancelTitle}"
 *
 * @since v4.4
 */
public class ModifierKeyContextProvider implements ContextProvider
{

    private final JiraAuthenticationContext authenticationContext;

    public ModifierKeyContextProvider(JiraAuthenticationContext authenticationContext)
    {
        this.authenticationContext = authenticationContext;
    }

    @Override
    public void init(Map<String, String> params) throws PluginParseException
    {
    }

    @Override
    public Map<String, Object> getContextMap(Map<String, Object> context)
    {
        final I18nHelper i18n = authenticationContext.getI18nHelper();

        final MapBuilder<String, Object> paramsBuilder = MapBuilder.newBuilder(context);

        final String modifierKey = BrowserUtils.getModifierKey();
        paramsBuilder.add("modifierKey", modifierKey);

        final String submitAccessKey = i18n.getText("AUI.form.submit.button.accesskey");
        paramsBuilder.add("submitAccessKey", submitAccessKey);
        paramsBuilder.add("submitTitle", i18n.getText("AUI.form.submit.button.tooltip", submitAccessKey, modifierKey));

        final String cancelAccessKey = i18n.getText("AUI.form.cancel.link.accesskey");
        paramsBuilder.add("cancelAccessKey", cancelAccessKey);
        paramsBuilder.add("cancelTitle", i18n.getText("AUI.form.cancel.link.tooltip", cancelAccessKey, modifierKey));

        return paramsBuilder.toMap();
    }
}
