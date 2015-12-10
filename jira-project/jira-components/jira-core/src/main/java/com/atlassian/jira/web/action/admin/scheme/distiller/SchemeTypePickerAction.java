package com.atlassian.jira.web.action.admin.scheme.distiller;

import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.scheme.SchemeFactory;
import com.atlassian.jira.scheme.SchemeManager;
import com.atlassian.jira.scheme.SchemeManagerFactory;
import com.atlassian.jira.web.action.admin.scheme.AbstractSchemePickerAction;
import com.atlassian.jira.web.action.admin.scheme.comparison.SchemeComparisonToolAction;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import webwork.action.ActionContext;

/**
 * This is the first step in the scheme merge tool it allows you to select the type of scheme to merge and if
 * the schemes are associated or all schemes.
 */
@WebSudoRequired
public class SchemeTypePickerAction extends AbstractSchemePickerAction
{
    public static final String SELECTED_SCHEMES_SESSION_KEY = "__selectedSchemesKey";

    public SchemeTypePickerAction(SchemeManagerFactory schemeManagerFactory, SchemeFactory schemeFactory, ApplicationProperties applicationProperties)
    {
        super(schemeManagerFactory, schemeFactory, applicationProperties);
    }

    public String doDefault()
    {
        clearSessionVariables();

        return super.doDefault();
    }

    public void doValidation()
    {
        //no validation necessary but this needs to be here to over-ride the base classes method.
    }

    public String doSwitch()
    {
        clearSessionVariables();

        return super.doSwitch();
    }

    protected String doExecute() throws Exception
    {
        //Store the schemes for the selected schemetype in the session
        SchemeManager schemeManager = getSchemeManager(getSelectedSchemeType());
        if (ASSOCIATED.equals(getTypeOfSchemesToDisplay()))
        {
            ActionContext.getSession().put(SELECTED_SCHEMES_SESSION_KEY, schemeManager.getAssociatedSchemes(true));
        }
        else if (ALL.equals(getTypeOfSchemesToDisplay()))
        {
            ActionContext.getSession().put(SELECTED_SCHEMES_SESSION_KEY, getSchemeFactory().getSchemesWithEntitiesComparable(schemeManager.getSchemes()));
        }

        return forceRedirect("SchemeMerge!default.jspa?typeOfSchemesToDisplay=" + getTypeOfSchemesToDisplay() + "&selectedSchemeType=" + getSelectedSchemeType());
    }

    public String doSelectSchemes()
    {
        //JRA-11357: When forwarding on from the comparison tool we need to make sure that the session has been cleared of any
        //temporary results.
        clearSessionVariables();
        // Set the selected schemes that were passed to this action
        ActionContext.getSession().put(SELECTED_SCHEMES_SESSION_KEY, getSchemeObjs());

        // Move on in the wizard
        return forceRedirect("SchemeMerge!default.jspa?typeOfSchemesToDisplay=all&selectedSchemeType=" + getSelectedSchemeType());
    }

    // This is forced upon us by the base class, we are the ugly child in the bunch since we do not show individual
    // schemes to select.
    public String getRedirectPage()
    {
        return null;
    }

    private void clearSessionVariables()
    {
        ActionContext.getSession().remove(SELECTED_SCHEMES_SESSION_KEY);
        ActionContext.getSession().remove(SchemeMergePreviewAction.DISTILLED_SCHEMES_SESSION_KEY);
    }

    /**
     * This is set to {@link SchemeComparisonToolAction#SCHEME_TOOL_NAME} because if the comparison tool finds no
     * difference, it adds a link to the merge tool which needs the selected schemes to be taken from the comparisons
     * session.
     */
    public String getToolName()
    {
        return SchemeComparisonToolAction.SCHEME_TOOL_NAME;
    }
}
