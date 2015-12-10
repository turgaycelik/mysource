package com.atlassian.jira.web.action.admin.scheme.distiller;

import com.atlassian.jira.bc.scheme.distiller.SchemeDistillerService;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.scheme.SchemeFactory;
import com.atlassian.jira.scheme.SchemeManagerFactory;
import com.atlassian.sal.api.websudo.WebSudoRequired;

/**
 * This action is used to generate the preview of the selected entries for the scheme merge tool.
 */
@WebSudoRequired
public class SchemeMergePreviewAction extends AbstractMergeAction
{
    public SchemeMergePreviewAction(SchemeManagerFactory schemeManagerFactory, SchemeFactory schemeFactory, ApplicationProperties applicationProperties, SchemeDistillerService schemeDistiller)
    {
        super(schemeManagerFactory, schemeFactory, applicationProperties, schemeDistiller);
    }

    public String doDefault() throws Exception
    {
        getDistilledSchemeResults();
        return INPUT;
    }

}
