package com.atlassian.jira.web.action.admin.scheme.distiller;

import com.atlassian.jira.bc.scheme.distiller.SchemeDistillerService;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.scheme.Scheme;
import com.atlassian.jira.scheme.SchemeFactory;
import com.atlassian.jira.scheme.SchemeManagerFactory;
import com.atlassian.jira.scheme.distiller.DistilledSchemeResult;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import webwork.action.ActionContext;

import java.util.ArrayList;
import java.util.Collection;

/**
 * This is the last step in the scheme merge wizard, it performs the actual persisting of the merge and shows the
 * results.
 */
@WebSudoRequired
public class SchemeMergeResultAction extends AbstractMergeAction
{
    private ErrorCollection persistErrors;
    private Collection<DistilledSchemeResult> persistedDistilledSchemeResults;

    public SchemeMergeResultAction(SchemeManagerFactory schemeManagerFactory, SchemeFactory schemeFactory, ApplicationProperties applicationProperties, SchemeDistillerService schemeDistiller)
    {
        super(schemeManagerFactory, schemeFactory, applicationProperties, schemeDistiller);
    }

    protected String doExecute() throws Exception
    {
        persistErrors = new SimpleErrorCollection();
        persistedDistilledSchemeResults = new ArrayList<DistilledSchemeResult>();
        for (final Object o : getDistilledSchemeResults().getDistilledSchemeResults())
        {
            DistilledSchemeResult distilledSchemeResult = (DistilledSchemeResult) o;
            if (distilledSchemeResult.isSelected())
            {
                Scheme persistedScheme = schemeDistiller.persistNewSchemeMappings(getLoggedInUser(), distilledSchemeResult, persistErrors);
                if (persistedScheme != null)
                {
                    persistedDistilledSchemeResults.add(distilledSchemeResult);
                }
            }
        }

        // Remove the variable from the session, this is cool as long as the session variable has been set into the
        // local variable so the jsp can show the resulting information.
        ActionContext.getSession().remove(DISTILLED_SCHEMES_SESSION_KEY);

        return SUCCESS;
    }

    public Collection getPersistedDistilledSchemeResults()
    {
        return persistedDistilledSchemeResults;
    }

    public ErrorCollection getPersistErrors()
    {
        return persistErrors;
    }

}
