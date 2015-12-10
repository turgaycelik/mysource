package com.atlassian.jira.web.action.admin;

import com.atlassian.core.ofbiz.util.OFBizPropertyUtils;
import com.atlassian.jira.event.JiraListener;
import com.atlassian.jira.event.ListenerManager;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.plugin.ComponentClassManager;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import com.google.common.collect.ImmutableMap;
import com.opensymphony.module.propertyset.PropertySet;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;

import static java.lang.String.format;

@WebSudoRequired
public class ViewListeners extends JiraWebActionSupport
{
    private Collection<GenericValue> listeners;
    private Long delete;
    private String name;
    private String clazz;
    private static final Logger LOG = Logger.getLogger(ViewListeners.class);
    private final ComponentClassManager componentClassManager;
    private final ListenerManager listenerManager;

    public ViewListeners(final ComponentClassManager componentClassManager, final ListenerManager listenerManager)
    {
        this.componentClassManager = componentClassManager;
        this.listenerManager = listenerManager;
    }

    public ListenerManager getListenerManager()
    {
        return listenerManager;
    }

    public void setDelete(Long delete)
    {
        this.delete = delete;
    }

    protected void doValidation()
    {
        if (delete == null)
        {
            //only do validation if we are not deleting.
            if (name == null || "".equals(name.trim()))
            {
                addError("name", getText("admin.errors.specify.name.for.listener"));
            }

            if (clazz == null || "".equals(clazz.trim()))
            {
                addError("clazz", getText("admin.errors.specify.class.for.listener"));
            }

            boolean listenerExists = false;

            //check that no listener exists with the same name.
            for (GenericValue listenerGv : getListeners())
            {
                if (name.equalsIgnoreCase(listenerGv.getString("name")))
                {
                    addError("name", getText("admin.errors.listener.already.exists", name));
                }
                if (clazz.equals(listenerGv.getString("clazz")))
                {
                    listenerExists = true;
                }
            }

            //don't lookup the classname unless there are no errors with the above
            if (!getHasErrors())
            {
                try
                {
                    JiraListener listener = componentClassManager.newInstance(clazz);

                    //if another listener exists with the same class, then check if uniqueness should be enforced
                    if (listenerExists && listener.isUnique())
                    {
                        addError("clazz", getText("admin.errors.cannot.add.listener"));
                    }
                }
                catch (ClassNotFoundException ex)
                {
                    addError("clazz", getText("admin.errors.class.not.found", clazz));
                    log.debug("User tried to add listener via the admin UI. The specified class [" + clazz + "] was not found when trying to add the listener", ex);
                }
                catch (ClassCastException e)
                {
                    addError("clazz", getText("admin.errors.class.is.not.listener", clazz));
                    log.debug("User tried to add listener via the admin UI. The specified class [" + clazz + "] is not of type JiraListener", e);
                }
                catch (Exception e)
                {
                    addError("clazz", getText("admin.errors.exception.loading.class") + " [" + e.getMessage() + "].");
                    log.error("User tried to add listener via the admin UI. Exception loading the specified class: [" + e.getMessage() + "]", e);
                }
            }
        }
    }

    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        if (name != null && clazz != null)
        {
            // We already checked for ClassNotFoundException in doValidation()
            JiraListener listener = componentClassManager.newInstance(clazz);
            listenerManager.createListener(name, listener.getClass());

            name = null;
            clazz = null;
            listeners = null;
        }

        return getRedirect("ViewListeners!default.jspa");
    }

    @RequiresXsrfCheck
    public String doDelete()
    {
        getOfBizDelegator().removeByAnd("ListenerConfig", ImmutableMap.of("id", delete));
        getListenerManager().refresh();
        return getRedirect("ViewListeners!default.jspa");
    }

    /**
     * Get all the listeners in the system.
     * @return A collection of GenericValues representing listeners
     */
    public Collection<GenericValue> getListeners()
    {
        if (listeners == null)
        {
            try
            {
                listeners = getOfBizDelegator().findAll("ListenerConfig");
            }
            catch (DataAccessException e)
            {
                LOG.error("DataAccessException", e);
            }
        }

        return listeners;
    }

    public JiraListener getListenerImplementation(GenericValue listener)
    {
        try
        {
            return componentClassManager.newInstance(listener.getString("clazz"));
        }
        catch (ClassNotFoundException listenerClassNotFoundException)
        {
            if (log.isDebugEnabled())
            {
                log.debug
                        (
                                format
                                        (
                                                "Unable to instantiate an object for the provided listener:%s",
                                                listener.getString("clazz")
                                        ),
                                listenerClassNotFoundException
                        );
            }
            return null;
        }
    }

    public boolean isListenerDeletable(GenericValue listener)
    {
        JiraListener listenerImp = getListenerImplementation(listener);
        if (listenerImp != null)
        {
            return !listenerImp.isInternal();
        }
        else
        {
            return true;
        }
    }

    public boolean isListenerEditable(GenericValue listener)
    {
        JiraListener listenerImp = getListenerImplementation(listener);
        if (listenerImp != null)
        {
            return listenerImp.getAcceptedParams().length > 0;
        }
        else
        {
            return true;
        }
    }

    public PropertySet getPropertySet(GenericValue gv)
    {
        return OFBizPropertyUtils.getCachingPropertySet(gv);
    }

    public void setName(String name)
    {
        this.name = name.trim();
    }

    public void setClazz(String clazz)
    {
        this.clazz = clazz.trim();
    }

    public String getName()
    {
        return name;
    }

    public String getClazz()
    {
        return clazz;
    }
}
