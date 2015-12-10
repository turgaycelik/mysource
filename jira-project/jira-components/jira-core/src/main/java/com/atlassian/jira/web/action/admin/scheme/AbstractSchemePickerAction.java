package com.atlassian.jira.web.action.admin.scheme;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.scheme.Scheme;
import com.atlassian.jira.scheme.SchemeFactory;
import com.atlassian.jira.scheme.SchemeManager;
import com.atlassian.jira.scheme.SchemeManagerFactory;
import com.atlassian.jira.web.component.schemepicker.SchemePickerWebComponent;
import org.ofbiz.core.entity.GenericEntityException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 
 */
public abstract class AbstractSchemePickerAction extends AbstractSchemeToolAction
{
    public static final String ASSOCIATED = "associated";
    public static final String ALL = "all";

    private String typeOfSchemesToDisplay;

    protected AbstractSchemePickerAction(SchemeManagerFactory schemeManagerFactory, SchemeFactory schemeFactory, ApplicationProperties applicationProperties)
    {
        super(schemeManagerFactory, schemeFactory, applicationProperties);
    }

    public String doDefault()
    {
        // Default to showing only the associated schemes
        typeOfSchemesToDisplay = ASSOCIATED;
        return INPUT;
    }

    protected void doValidation()
    {
        if (getSelectedSchemeIds() == null || getSelectedSchemeIds().length == 0)
        {
            addErrorMessage(getText("admin.scheme.picker.errpr.select.schemes"));
        }
    }

    protected String doExecute() throws Exception
    {
        StringBuilder redirectString = new StringBuilder(getRedirectPage());
        redirectString.append("?selectedSchemeType=");
        redirectString.append(getSelectedSchemeType());

        storeSelectedSchemeIdsInSession();

        return forceRedirect(redirectString.toString());
    }

    public String getSchemePickerWebComponentHtml() throws GenericEntityException
    {
        return new SchemePickerWebComponent(getSchemes(), getSchemeTypes(), getSelectedSchemeType(), getSelectedSchemeIds()).getHtml("SchemeGroupsToRoleTransformerAction");
    }

    public String doSwitch()
    {
        return INPUT;
    }

    public String getTypeOfSchemesToDisplay()
    {
        return typeOfSchemesToDisplay;
    }

    public void setTypeOfSchemesToDisplay(String typeOfSchemesToDisplay)
    {
        this.typeOfSchemesToDisplay = typeOfSchemesToDisplay;
    }

    public Map getSchemes() throws GenericEntityException
    {
        Map<String, List<Scheme>> schemes = new HashMap<String, List<Scheme>>();
        for (final Object o : getSchemeTypes().values())
        {
            String schemeType = (String) o;
            SchemeManager schemeManager = getSchemeManager(schemeType);

            if (ASSOCIATED.equals(getTypeOfSchemesToDisplay()))
            {
                schemes.put(schemeType, schemeManager.getAssociatedSchemes(false));
            }
            else if (ALL.equals(getTypeOfSchemesToDisplay()))
            {
                schemes.put(schemeType, getSchemeFactory().getSchemes(schemeManager.getSchemes()));
            }
        }

        return schemes;
    }

    // This is hard-coded to work only with notification and permission schemes at the moment.
    public Map getSchemeTypes()
    {
        return EasyMap.build(getText("admin.scheme.picker.notification.schemes.type"), SchemeManagerFactory.NOTIFICATION_SCHEME_MANAGER, getText("admin.scheme.picker.permission.schemes.type"), SchemeManagerFactory.PERMISSION_SCHEME_MANAGER);
    }

    public abstract String getRedirectPage();

    public int getMaxNumberOfSchemesToCompare()
    {
        try
        {
            return Integer.parseInt(applicationProperties.getDefaultBackedString(APKeys.JIRA_MAX_SCHEMES_FOR_COMPARISON));
        }
        catch (NumberFormatException t)
        {
            return 5;
        }
    }

}
