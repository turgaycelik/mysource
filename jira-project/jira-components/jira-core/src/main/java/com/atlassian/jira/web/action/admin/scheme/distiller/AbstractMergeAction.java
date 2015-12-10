package com.atlassian.jira.web.action.admin.scheme.distiller;

import com.atlassian.jira.bc.scheme.distiller.SchemeDistillerService;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.scheme.SchemeFactory;
import com.atlassian.jira.scheme.SchemeManagerFactory;
import com.atlassian.jira.scheme.distiller.DistilledSchemeResult;
import com.atlassian.jira.scheme.distiller.DistilledSchemeResults;
import com.atlassian.jira.web.action.admin.scheme.AbstractSchemeToolAction;
import webwork.action.ActionContext;

import java.util.Collection;

/**
 * A baseclass for all the merge tool actions.
 */
public abstract class AbstractMergeAction extends AbstractSchemeToolAction
{
    protected DistilledSchemeResults distilledSchemeResults;
    protected SchemeDistillerService schemeDistiller;
    public static final String DISTILLED_SCHEMES_SESSION_KEY = "__distilledSchemesKey";
    private String typeOfSchemesToDisplay;

    protected AbstractMergeAction(SchemeManagerFactory schemeManagerFactory, SchemeFactory schemeFactory,
                                  ApplicationProperties applicationProperties, SchemeDistillerService schemeDistiller)
    {
        super(schemeManagerFactory, schemeFactory, applicationProperties);
        this.schemeDistiller = schemeDistiller;
    }

    public DistilledSchemeResults getDistilledSchemeResults()
    {
        if (distilledSchemeResults == null)
        {
            distilledSchemeResults = (DistilledSchemeResults) ActionContext.getSession().get(DISTILLED_SCHEMES_SESSION_KEY);
            if (distilledSchemeResults == null)
            {
                Collection schemesToMerge =
                        (Collection) ActionContext.getSession().get(SchemeTypePickerAction.SELECTED_SCHEMES_SESSION_KEY);
                if (schemesToMerge != null)
                {
                    distilledSchemeResults = schemeDistiller.distillSchemes(getLoggedInUser(), schemesToMerge, this);
                    ActionContext.getSession().put(DISTILLED_SCHEMES_SESSION_KEY, distilledSchemeResults);
                }
            }
        }
        return distilledSchemeResults;
    }

    public String getSchemeTypeDisplayName(String schemeType)
    {
        if (SchemeManagerFactory.NOTIFICATION_SCHEME_MANAGER.equals(schemeType))
        {
            return getText("admin.scheme.picker.notification.schemes.type");
        }
        else if (SchemeManagerFactory.PERMISSION_SCHEME_MANAGER.equals(schemeType))
        {
            return getText("admin.scheme.picker.permission.schemes.type");
        }
        return "";
    }

    public String getTypeOfSchemesToDisplay()
    {
        return typeOfSchemesToDisplay;
    }

    public void setTypeOfSchemesToDisplay(String typeOfSchemesToDisplay)
    {
        this.typeOfSchemesToDisplay = typeOfSchemesToDisplay;
    }

    public int getNumberOfSelectedSchemes()
    {
        int count = 0;
        for (final Object o : getDistilledSchemeResults().getDistilledSchemeResults())
        {
            DistilledSchemeResult distilledSchemeResult = (DistilledSchemeResult) o;
            if (distilledSchemeResult.isSelected())
            {
                count++;
            }
        }

        return count;
    }
}
