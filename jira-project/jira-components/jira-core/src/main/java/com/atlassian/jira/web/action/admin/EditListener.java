/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.admin;

import java.util.Map;

import com.atlassian.core.ofbiz.util.OFBizPropertyUtils;
import com.atlassian.core.util.ClassLoaderUtils;
import com.atlassian.jira.event.JiraListener;
import com.atlassian.jira.event.ListenerManager;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.sal.api.websudo.WebSudoRequired;

import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.util.TextUtils;

import org.ofbiz.core.entity.GenericValue;

import webwork.action.ParameterAware;

@WebSudoRequired
public class EditListener extends JiraWebActionSupport implements ParameterAware
{
    Long id;
    GenericValue listener;
    Map<String, String[]> params;
    PropertySet propertySet;

    private final PluginAccessor pluginAccessor;
    private final ListenerManager listenerManager;

    public EditListener(final PluginAccessor pluginAccessor, final ListenerManager listenerManager)
    {
        this.pluginAccessor = pluginAccessor;
        this.listenerManager = listenerManager;
    }

    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        try
        {
            final PropertySet ps = getPropertySet();

            // create a dummy listener
            final JiraListener listener = getJiraListener();

            // set all the possible accepted parameters - blank or missing params are set to null
            for (final String paramName : listener.getAcceptedParams())
            {
                String paramValue = params.get(paramName)[0];
                if (!TextUtils.stringSet(paramValue))
                {
                    paramValue = null;
                }

                if (paramValue != null)
                {
                    ps.setString(paramName, paramValue);
                }
                else
                {
                    ps.remove(paramName);
                }
            }

            // now update the listeners loaded
            listenerManager.refresh();
        }
        catch (Exception e)
        {
            log.error("Error occurred trying to update listener properties: " + e, e);
            addErrorMessage(getText("admin.errors.updating.listener.properties") + ' ' + e);
        }

        if (getHasErrorMessages())
            return ERROR;
        else
            return getRedirect("ViewListeners!default.jspa");
    }

    protected void doValidation()
    {
        if (getListener() == null)
        {
            addErrorMessage(getText("admin.errors.listener.does.not.exist"));
        }

        super.doValidation();
    }

    public String[] getAcceptedParams()
    {
        try
        {
            return getJiraListener().getAcceptedParams();
        }
        catch (Exception e)
        {
            log.error("Error getting accepted params: " + e.getMessage(), e);
            return new String[0];
        }
    }

    public JiraListener getJiraListener() throws ClassNotFoundException, IllegalAccessException, InstantiationException
    {
        return (JiraListener) ClassLoaderUtils.loadClass(getListener().getString("clazz"), pluginAccessor.getClassLoader()).newInstance();
    }

    public String getParamValue(String s)
    {
        return getPropertySet().getString(s);
    }

    private PropertySet getPropertySet()
    {
        if (propertySet == null)
        {
            propertySet = OFBizPropertyUtils.getCachingPropertySet(getListener());
        }
        return propertySet;
    }

    public GenericValue getListener()
    {
        if (listener == null)
        {
            try
            {
                listener = getOfBizDelegator().findById("ListenerConfig", id);
            }
            catch (DataAccessException e)
            {
                log.error("Error getting ListenerConfig with id "+id, e);
            }
        }

        return listener;
    }

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    @SuppressWarnings("unchecked")
    public void setParameters(final Map map)
    {
        this.params = map;
    }
}
