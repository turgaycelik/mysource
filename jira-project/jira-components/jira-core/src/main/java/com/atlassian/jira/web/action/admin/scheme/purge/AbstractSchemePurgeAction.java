package com.atlassian.jira.web.action.admin.scheme.purge;

import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.scheme.SchemeFactory;
import com.atlassian.jira.scheme.SchemeManagerFactory;
import com.atlassian.jira.web.action.admin.scheme.AbstractSchemeToolAction;
import webwork.action.ActionContext;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 
 */
public abstract class AbstractSchemePurgeAction extends AbstractSchemeToolAction
{
    public static final String SELECTED_SCHEME_IDS_TO_DELETE_KEY = "__selectedSchemeIdsToDelete";

    public AbstractSchemePurgeAction(SchemeManagerFactory schemeManagerFactory, SchemeFactory schemeFactory, ApplicationProperties applicationProperties)
    {
        super(schemeManagerFactory, schemeFactory, applicationProperties);
    }

    public List getSelectedSchemeIdsAsList()
    {
        List<String> selectedSchemeIdsAsList;
        if (getSelectedSchemeIds() != null)
        {
            selectedSchemeIdsAsList = Arrays.asList(getSelectedSchemeIds());
        }
        else
        {
            selectedSchemeIdsAsList = Collections.emptyList();
        }
        return selectedSchemeIdsAsList;
    }

    public String[] getSelectedSchemeIds()
    {
        if (super.getSelectedSchemeIds() == null)
        {
            super.setSelectedSchemeIds((String[]) ActionContext.getSession().get(SELECTED_SCHEME_IDS_TO_DELETE_KEY));
        }
        return super.getSelectedSchemeIds();
    }
}
