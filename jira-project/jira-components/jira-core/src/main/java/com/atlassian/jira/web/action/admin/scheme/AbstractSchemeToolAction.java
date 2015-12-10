package com.atlassian.jira.web.action.admin.scheme;

import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.scheme.Scheme;
import com.atlassian.jira.scheme.SchemeFactory;
import com.atlassian.jira.scheme.SchemeManager;
import com.atlassian.jira.scheme.SchemeManagerFactory;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import org.ofbiz.core.entity.GenericEntityException;
import webwork.action.ActionContext;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 */
public abstract class AbstractSchemeToolAction extends JiraWebActionSupport
{
    private String selectedSchemeType;
    private String[] selectedSchemeIds;
    private SchemeManagerFactory schemeManagerFactory;
    private SchemeFactory schemeFactory;
    private List<Scheme> selectedSchemeObjs;
    protected ApplicationProperties applicationProperties;
    private static final String SESSION_KEY_PREFIX = "__";
    private static final String SESSION_KEY_SUFFIX = "__selectedSchemeIds";

    public AbstractSchemeToolAction(SchemeManagerFactory schemeManagerFactory, SchemeFactory schemeFactory, ApplicationProperties applicationProperties)
    {
        this.schemeManagerFactory = schemeManagerFactory;
        this.schemeFactory = schemeFactory;
        this.applicationProperties = applicationProperties;
    }

    public String getSelectedSchemeType()
    {
        return selectedSchemeType;
    }

    public void setSelectedSchemeType(String selectedSchemeType)
    {
        this.selectedSchemeType = selectedSchemeType;
    }

    /**
     * Checks if there is selected scheme ids in the session. (ignores the cached value)
     * @return true if there are selected scheme ids in session, false otherwise
     */
    public boolean isHasSelectedSchemeIds()
    {
        String[] savedSelectedSchemeIds = (String[]) ActionContext.getSession().get(getSelectedSchemeIdsSessionKey());
        return savedSelectedSchemeIds != null && savedSelectedSchemeIds.length > 0;
    }

    public String[] getSelectedSchemeIds()
    {
        if (selectedSchemeIds == null)
        {
            selectedSchemeIds = (String[]) ActionContext.getSession().get(getSelectedSchemeIdsSessionKey());
        }
        return selectedSchemeIds;
    }

    public void setSelectedSchemeIds(String[] selectedSchemeIds)
    {
        this.selectedSchemeIds = selectedSchemeIds;
    }

    public void storeSelectedSchemeIdsInSession()
    {
        ActionContext.getSession().put(getSelectedSchemeIdsSessionKey(), selectedSchemeIds);
    }

    public void resetSelectedSchemeIds()
    {
        selectedSchemeIds = null;
        ActionContext.getSession().remove(getSelectedSchemeIdsSessionKey());
    }

    /**
     * Used by {@link #getSelectedSchemeIdsSessionKey()} to generate a session key for each set of tools that needs
     * selected scheme id's.
     * @see #getSelectedSchemeIdsSessionKey()
     * @return Name identifying a set of related picker tools uniquely
     */
    public String getToolName()
    {
        return null;
    }

    /**
     * Uses {@link #getToolName()} to create a unique session key to store the selected scheme ids (String[]) for a
     * set of tools (eg. Group to Roles mapper, Scheme comparison etc)
     * @return Scheme tool specific session key to store selected scheme id's
     */
    public String getSelectedSchemeIdsSessionKey()
    {
        return SESSION_KEY_PREFIX + getToolName() + SESSION_KEY_SUFFIX;
    }

    public SchemeFactory getSchemeFactory()
    {
        return schemeFactory;
    }

    public SchemeManagerFactory getSchemeManagerFactory()
    {
        return schemeManagerFactory;
    }

    protected SchemeManager getSchemeManager(String type)
    {
        return getSchemeManagerFactory().getSchemeManager(type);
    }

    protected List<Scheme> getSchemeObjs()
    {
        if (selectedSchemeObjs == null)
        {
            selectedSchemeObjs = new ArrayList<Scheme>();
            if (getSelectedSchemeIds() != null)
            {
                for (int i = 0; i < getSelectedSchemeIds().length; i++)
                {
                    String selectedScheme = getSelectedSchemeIds()[i];
                    try
                    {
                        selectedSchemeObjs.add(getSchemeForId(selectedScheme));
                    }
                    catch (Exception e)
                    {
                        addErrorMessage(getText("admin.scheme.group.role.unable.to.resolve.id", selectedScheme));
                    }
                }
            }
        }
        return selectedSchemeObjs;
    }

    private Scheme getSchemeForId(String stringId) throws GenericEntityException
    {
        Long schemeId = new Long(Long.parseLong(stringId));
        return getSchemeFactory().getSchemeWithEntitiesComparable(getSchemeManager(getSelectedSchemeType()).getScheme(schemeId));
    }
}
