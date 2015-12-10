package com.atlassian.jira.web.action.admin.scheme.distiller;

import com.atlassian.jira.bc.scheme.distiller.SchemeDistillerService;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.scheme.Scheme;
import com.atlassian.jira.scheme.SchemeFactory;
import com.atlassian.jira.scheme.SchemeManagerFactory;
import com.atlassian.jira.scheme.distiller.DistilledSchemeResult;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.PredicateUtils;
import webwork.action.ActionContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * This action is the second step in the wizard for scheme merging, it prompts you to select which schemes to merge.
 */
@WebSudoRequired
public class SchemeMergeAction extends AbstractMergeAction
{
    private String[] selectedDistilledSchemes;

    public SchemeMergeAction(SchemeManagerFactory schemeManagerFactory, SchemeFactory schemeFactory, ApplicationProperties applicationProperties, SchemeDistillerService schemeDistiller)
    {
        super(schemeManagerFactory, schemeFactory, applicationProperties, schemeDistiller);
    }

    public String doDefault() throws Exception
    {
        getDistilledSchemeResults();
        return INPUT;
    }

    protected void doValidation()
    {
        Map params = ActionContext.getParameters();

        setSelectedStateOnDistilledResults(false);

        if(selectedDistilledSchemes==null || selectedDistilledSchemes.length ==0)
        {
            addErrorMessage(getText("admin.scheme.merge.action.must.select.one.scheme"));
        }

        List<String> newSchemeNames = validateInputFields(params);

        checkForDuplicates(newSchemeNames);
    }

    protected String doExecute() throws Exception
    {
        // All validation has been passed, this is ready to go
        setSelectedStateOnDistilledResults(true);

        return forceRedirect("SchemeMergePreview!default.jspa?selectedSchemeType=" + getSelectedSchemeType() + "&typeOfSchemesToDisplay=" + getTypeOfSchemesToDisplay());
    }

    public String getEditPage()
    {
        String editPage = null;
        if (SchemeManagerFactory.NOTIFICATION_SCHEME_MANAGER.equals(getDistilledSchemeResults().getSchemeType()))
        {
            editPage = "EditNotifications";
        }
        else if (SchemeManagerFactory.PERMISSION_SCHEME_MANAGER.equals(getDistilledSchemeResults().getSchemeType()))
        {
            editPage = "EditPermissions";
        }

        return editPage;
    }

    public int getTotalDistilledFromSchemes()
    {
        int i = 0;
        for (final Object o : distilledSchemeResults.getDistilledSchemeResults())
        {
            DistilledSchemeResult distilledSchemeResult = (DistilledSchemeResult) o;
            i += distilledSchemeResult.getOriginalSchemes().size();
        }
        return i;
    }

    public String[] getSelectedDistilledSchemes()
    {
        return selectedDistilledSchemes;
    }

    public void setSelectedDistilledSchemes(String[] selectedDistilledSchemes)
    {
        this.selectedDistilledSchemes = selectedDistilledSchemes;
    }

    public List getSelectedDistilledSchemesAsList()
    {
        if (selectedDistilledSchemes == null)
        {
            return Collections.EMPTY_LIST;
        }
        else
        {
            return Arrays.asList(selectedDistilledSchemes);
        }
    }

    private List<String> validateInputFields(Map params)
    {
        List<String> newSchemeNames = new ArrayList<String>();
        for (final Object o : getDistilledSchemeResults().getDistilledSchemeResults())
        {
            DistilledSchemeResult distilledSchemeResult = (DistilledSchemeResult) o;
            Scheme scheme = distilledSchemeResult.getResultingScheme();
            if (getSelectedDistilledSchemesAsList().contains(scheme.getName()))
            {
                String[] newSchemeNamesParam = (String[]) params.get(scheme.getName());
                if (newSchemeNamesParam == null || newSchemeNamesParam[0].trim().length() == 0)
                {
                    addError(scheme.getName(), getText("admin.scheme.merge.action.name.for.scheme"));
                }
                else
                {
                    String newSchemeName = newSchemeNamesParam[0].trim();
                    newSchemeNames.add(newSchemeName);
                    distilledSchemeResult.setResultingSchemeTempName(newSchemeName);
                }
            }
        }
        return newSchemeNames;
    }

    private void setSelectedStateOnDistilledResults(boolean nukeTempName)
    {
        for (final Object o : getDistilledSchemeResults().getDistilledSchemeResults())
        {
            DistilledSchemeResult distilledSchemeResult = (DistilledSchemeResult) o;
            if (getSelectedDistilledSchemesAsList().contains(distilledSchemeResult.getResultingScheme().getName()))
            {
                distilledSchemeResult.setSelected(true);
            }
            else
            {
                distilledSchemeResult.setSelected(false);
                if (nukeTempName)
                {
                    distilledSchemeResult.setResultingSchemeTempName("");
                }
            }
        }
    }

    private void checkForDuplicates(List<String> newSchemeNames)
    {
        for (final Object o : getDistilledSchemeResults().getDistilledSchemeResults())
        {
            DistilledSchemeResult distilledSchemeResult = (DistilledSchemeResult) o;
            Scheme scheme = distilledSchemeResult.getResultingScheme();
            if (getSelectedDistilledSchemesAsList().contains(scheme.getName()))
            {
                String resultingSchemeTempName = distilledSchemeResult.getResultingSchemeTempName();
                if (CollectionUtils.countMatches(newSchemeNames, PredicateUtils.equalPredicate(resultingSchemeTempName)) > 1)
                {
                    addError(scheme.getName(), getText("admin.scheme.merge.action.dup.name"));
                }

                // Check to see that the scheme name is not already in use in the system
                schemeDistiller.isValidNewSchemeName(getLoggedInUser(), scheme.getName(), resultingSchemeTempName, getSelectedSchemeType(), this);
            }
        }
    }

}
