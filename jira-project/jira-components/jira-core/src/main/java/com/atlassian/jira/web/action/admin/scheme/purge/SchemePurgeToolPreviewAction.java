package com.atlassian.jira.web.action.admin.scheme.purge;

import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.scheme.SchemeFactory;
import com.atlassian.jira.scheme.SchemeManagerFactory;
import com.atlassian.sal.api.websudo.WebSudoRequired;

import java.util.List;

/**
 * 
 */
@WebSudoRequired
public class SchemePurgeToolPreviewAction extends AbstractSchemePurgeAction
{
    public SchemePurgeToolPreviewAction(SchemeManagerFactory schemeManagerFactory, SchemeFactory schemeFactory, ApplicationProperties applicationProperties)
    {
        super(schemeManagerFactory, schemeFactory, applicationProperties);
    }

    public String doDefault() throws Exception
    {
        return INPUT;
    }

    public List getSelectedSchemes()
    {
        return getSchemeObjs();
    }
}
