package com.atlassian.jira.web.action.admin.scheme.purge;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.scheme.Scheme;
import com.atlassian.jira.scheme.SchemeFactory;
import com.atlassian.jira.scheme.SchemeManagerFactory;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import webwork.action.ActionContext;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * 
 */
@WebSudoRequired
public class SchemePurgeTypePickerAction extends AbstractSchemePurgeAction
{
    public SchemePurgeTypePickerAction(SchemeManagerFactory schemeManagerFactory, SchemeFactory schemeFactory, ApplicationProperties applicationProperties)
    {
        super(schemeManagerFactory, schemeFactory, applicationProperties);
    }

    public String doDefault() throws Exception
    {
        ActionContext.getSession().remove(SELECTED_SCHEME_IDS_TO_DELETE_KEY);
        setSelectedSchemeType(SchemeManagerFactory.PERMISSION_SCHEME_MANAGER);
        return INPUT;
    }

    public void doValidation()
    {
        if(getSelectedSchemeIdsAsList().isEmpty())
        {
            addErrorMessage(getText("admin.scheme.purge.type.picker.action.no.schemes.selected"));
        }
    }

    public String doReturn() throws Exception
    {
        // Set the local variable of selected ids so that the screen renders correctly
        getSelectedSchemeIds();
        // Clean out the session variable so that if the user does not select anything we
        // will still register it.
        ActionContext.getSession().remove(SELECTED_SCHEME_IDS_TO_DELETE_KEY);
        return INPUT;
    }

    protected String doExecute() throws Exception
    {
        ActionContext.getSession().put(SELECTED_SCHEME_IDS_TO_DELETE_KEY, getSelectedSchemeIds());

        return forceRedirect("SchemePurgeToolPreview!default.jspa?selectedSchemeType=" + getSelectedSchemeType());
    }

    // This is hard-coded to work only with notification and permission schemes at the moment.
    public Map getSchemeTypes()
    {
        return EasyMap.build(getText("admin.scheme.picker.notification.schemes.type"), SchemeManagerFactory.NOTIFICATION_SCHEME_MANAGER,
                getText("admin.scheme.picker.permission.schemes.type"), SchemeManagerFactory.PERMISSION_SCHEME_MANAGER);
    }

    public List getUnassociatedSchemes(String schemeType)
    {
        // Get all the unassociated schemes for the selected scheme type
        List<Scheme> unassociatedList = getSchemeManager(schemeType).getUnassociatedSchemes();
        for (Iterator<Scheme> iterator = unassociatedList.iterator(); iterator.hasNext();)
        {
            Scheme scheme = iterator.next();
            Long id = scheme.getId();
            // The != 0 part is so that we never delete the Default Permission Scheme which is a bit of a special
            // one in JIRA. JRA-11705
            if(scheme.getId() != null && id.longValue() == 0)
            {
                iterator.remove();
            }
        }
        return unassociatedList;
    }

}
