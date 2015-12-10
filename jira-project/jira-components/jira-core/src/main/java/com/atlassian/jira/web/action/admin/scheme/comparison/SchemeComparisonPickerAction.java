package com.atlassian.jira.web.action.admin.scheme.comparison;

import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.scheme.SchemeFactory;
import com.atlassian.jira.scheme.SchemeManagerFactory;
import com.atlassian.jira.web.action.admin.scheme.AbstractSchemePickerAction;
import com.atlassian.sal.api.websudo.WebSudoRequired;

/**
 * The picker action for the scheme comparison tool.
 */
@WebSudoRequired
public class SchemeComparisonPickerAction extends AbstractSchemePickerAction
{
    public static final String COMPARISON_RESULTS_KEY = "__schemecomparisonresults";

    public SchemeComparisonPickerAction(SchemeManagerFactory schemeManagerFactory, SchemeFactory schemeFactory, ApplicationProperties applicationProperties)
    {
        super(schemeManagerFactory, schemeFactory, applicationProperties);
    }

    public String doDefault()
    {
        resetSelectedSchemeIds();
        return super.doDefault();
    }

    protected void doValidation()
    {
        if(getSelectedSchemeIds() == null || getSelectedSchemeIds().length <= 1)
        {
            addErrorMessage(getText("admin.scheme.picker.comparison.action.at.least"));
        }
        if (getSelectedSchemeIds() != null && getSelectedSchemeIds().length > getMaxNumberOfSchemesToCompare())
        {
            addErrorMessage(getText("admin.scheme.picker.comparison.action.too.many", getMaxNumberOfSchemesToCompare() + ""));
        }
    }

    public String getRedirectPage()
    {
        return "SchemeComparisonTool!default.jspa";
    }

    public String getToolName()
    {
        return SchemeComparisonToolAction.SCHEME_TOOL_NAME;
    }
}
