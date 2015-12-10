package com.atlassian.jira.web.component.schemepicker;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.template.VelocityTemplatingEngine;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.JiraVelocityUtils;
import com.atlassian.jira.web.component.AbstractWebComponent;
import com.atlassian.util.profiling.UtilTimerStack;
import org.ofbiz.core.entity.GenericEntityException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

/**
 * This is meant to be a reusable UI component that allows you to select schemes based on their type.
 */
public class SchemePickerWebComponent extends AbstractWebComponent
{
    private final JiraAuthenticationContext authenticationContext = ComponentAccessor.getComponentOfType(JiraAuthenticationContext.class);
    private final Map schemesByType;
    private final Map schemeTypes;
    private final String selectedSchemeType;
    private final String[] selectedSchemeIds;

    public SchemePickerWebComponent(final Map schemesByType, final Map schemeTypes, final String defaultSchemeType, final String[] selectedSchemeIds)
    {
        super(ComponentAccessor.getComponent(VelocityTemplatingEngine.class), ComponentAccessor.getApplicationProperties());
        this.schemesByType = schemesByType;
        this.schemeTypes = schemeTypes;
        selectedSchemeType = defaultSchemeType;
        this.selectedSchemeIds = selectedSchemeIds;
    }

    public String getHtml(final String actionNameFori18n)
    {
        try
        {
            UtilTimerStack.push("SchemePickerHtml");

            final I18nHelper i18n = authenticationContext.getI18nHelper();

            final Map startingParams = EasyMap.build("i18n", i18n, "schemePickerWebComponent", this, "windowName", "SchemePicker");
            final Map params = JiraVelocityUtils.getDefaultVelocityParams(startingParams, authenticationContext);
            return getHtml("templates/jira/schemes/schemepicker/schemepickertable.vm", params);
        }
        finally
        {
            UtilTimerStack.pop("SchemePickerHtml");
        }
    }

    public static Collection getSelectedSchemeIds(final Map params)
    {
        Collection selectedSchemes = new ArrayList();

        if (params != null)
        {
            final String[] selectedSchemeParams = (String[]) params.get("selectedSchemeIds");

            if (selectedSchemeParams != null)
            {
                selectedSchemes = Arrays.asList(selectedSchemeParams);
            }
        }
        return selectedSchemes;
    }

    public static String getSchemeType(final Map params)
    {
        String schemeType = null;
        if (params != null)
        {
            final String[] schemeTypeParam = (String[]) params.get("selectedSchemeType");
            if (schemeTypeParam != null)
            {
                schemeType = schemeTypeParam[0];
            }
        }
        return schemeType;
    }

    public Map getSchemes() throws GenericEntityException
    {
        return schemesByType;
    }

    public Map getSchemeTypes()
    {
        return schemeTypes;
    }

    public String getSelectedSchemeType()
    {
        return selectedSchemeType;
    }

    public boolean isSchemeSelected(final Long schemeId)
    {
        //ignore null scheme id's
        if ((schemeId == null) || (selectedSchemeIds == null))
        {
            return false;
        }

        for (final String selectedSchemeId : selectedSchemeIds)
        {
            if (selectedSchemeId.equals(schemeId.toString()))
            {
                return true;
            }
        }
        return false;
    }
}
