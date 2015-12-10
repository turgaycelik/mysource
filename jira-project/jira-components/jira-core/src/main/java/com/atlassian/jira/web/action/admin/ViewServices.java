/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.admin;

import com.atlassian.jira.configurableobjects.ConfigurableObjectUtil;
import com.atlassian.jira.plugin.ComponentClassManager;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.service.InBuiltServiceTypes;
import com.atlassian.jira.service.JiraServiceContainer;
import com.atlassian.jira.service.ServiceManager;
import com.atlassian.jira.service.ServiceTypes;
import com.atlassian.jira.service.services.file.FileService;
import com.atlassian.jira.service.services.mail.MailFetcherService;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import org.apache.log4j.Logger;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Map;

@WebSudoRequired
public class ViewServices extends JiraWebActionSupport
{
    private static final Logger LOG = Logger.getLogger(ViewServices.class);

    private final ServiceManager serviceManager;
    private final ComponentClassManager componentClassManager;
    private final ServiceTypes serviceTypes;
    private final InBuiltServiceTypes inBuiltServiceTypes;

    private Long delete;
    private String name;
    private String clazz;
    private long delay = 1; //delay in minutes

    public ViewServices(ServiceManager serviceManager, final ComponentClassManager componentClassManager,
            final InBuiltServiceTypes inBuiltServiceTypes, ServiceTypes serviceTypes)
    {
        this.serviceManager = serviceManager;
        this.componentClassManager = componentClassManager;
        this.inBuiltServiceTypes = inBuiltServiceTypes;
        this.serviceTypes = serviceTypes;
    }

    protected void doValidation()
    {
        if (delete == null)
        { //only do validation if we are not deleting.
            if (name == null || "".equals(name.trim()))
            {
                addError("name", getText("admin.errors.specify.service.name"));
            }

            if (clazz == null || "".equals(clazz.trim()))
            {
                addError("clazz", getText("admin.errors.specify.service.class"));
            }

            boolean serviceExists = false;

            //check that no service exists with the same name.
            for (final JiraServiceContainer service : serviceManager.getServices())
            {
                if (name.equalsIgnoreCase(service.getName()))
                {
                    addError("name", getText("admin.errors.service.with.name.exists", name));
                }
                if (clazz.equals(service.getServiceClass()))
                {
                    serviceExists = service.isUnique();
                }
            }

            if (!getHasErrors())
            {
                try
                {
                    // Instantiate the class so we can see if any exceptions are generated and then report a more
                    // specific error to the user.
                    componentClassManager.newInstance(clazz);

                    //if another service exists with the same class, then check if uniqueness should be enforced
                    if (serviceExists)
                    {
                        addError("clazz", getText("admin.errors.cannot.add.service"));
                    }
                }
                catch (ClassNotFoundException ex)
                {
                    addError("clazz", getText("admin.errors.class.not.found", clazz));
                    log.debug("Class [" + clazz + "] was not found when adding service", ex);
                }
                catch (ClassCastException e)
                {
                    addError("clazz", getText("admin.errors.incorrect.class.type", clazz));
                    log.debug("Class [" + clazz + "] is not of type JiraService", e);
                }
                catch (Exception e)
                {
                    addError("clazz", getText("admin.errors.exception.loading.class") + " [" + e.getMessage() + "].");
                    log.debug("Exception loading class: [" + e.getMessage() + "]", e);
                }
            }
            if (delay < 1)
            {
                addError("delay", getText("admin.errors.delay.too.short"));
            }
        }
    }

    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        if (delete != null)
        {
            if (canDeleteService(delete))
            {
                LOG.debug("Removing Service with id " + delete);
                serviceManager.removeService(delete);
                return getRedirect("ViewServices!default.jspa");
            }
            else
            {
                return "securitybreach";
            }
        }

        if (name != null && clazz != null)
        {
            if (canAddService(clazz))
            {
                try
                {
                    JiraServiceContainer serviceContainer = serviceManager.addService(getName(), getClazz(), (getDelay() * 60 * 1000));
                    delay = 1;
                    return getRedirect("EditService!default.jspa?id=" + serviceContainer.getId());
                }
                catch (Exception e)
                {
                    log.error("Unable to add service: " + getName(), e);
                    addErrorMessage(getText("admin.errors.error.adding.service")+ " " + e.toString() + ".");
                }
            }
            else
            {
                return "securitybreach";
            }
        }

        return getRedirect("ViewServices!default.jspa");
    }

    private boolean canAddService(final String clazz)
    {
        return serviceTypes.isManageableBy(getLoggedInUser(), clazz);
    }

    private boolean canDeleteService(final Long serviceId) throws Exception
    {
        return Iterables.any(serviceManager.getServicesManageableBy(getLoggedInUser()), new Predicate<JiraServiceContainer>()
        {
            @Override
            public boolean apply(@Nullable JiraServiceContainer aServiceManageableByTheUser)
            {
                return serviceId.equals(aServiceManageableByTheUser.getId());
            }
        });
//        return Iterables.contains(serviceManager.getServicesManageableBy(getLoggedInUser()), serviceToDelete);
    }

    public Collection<JiraServiceContainer> getServices()
    {
        return ImmutableList.copyOf(serviceManager.getServicesManageableBy(getLoggedInUser()));
    }

    public Collection getInBuiltServiceTypes()
    {
        return ImmutableSet.copyOf(Iterables.filter(inBuiltServiceTypes.manageableBy(getLoggedInUser()), new Predicate<InBuiltServiceTypes.InBuiltServiceType>()
        {
            @Override
            public boolean apply(InBuiltServiceTypes.InBuiltServiceType input)
            {
                return input.getType() != null && !isMailRelatedServiceClass(input.getType());
            }
        }));
    }

    public Map getPropertyMap(JiraServiceContainer serviceContainer) throws Exception
    {
        try
        {
            return ConfigurableObjectUtil.getPropertyMap(serviceContainer);
        }
        catch (Exception ex)
        {
            log.error(ex.getMessage(), ex);
            // No point in re-throwing the error: This will just cause webwork to log it again.
            return MapBuilder.build("ERROR", "Error occurred getting properties. See log file for details.");
        }
    }

    public long getDelayInMins(JiraServiceContainer serviceContainer)
    {
        return serviceContainer.getDelay() / 60000;
    }

    public void setName(String name)
    {
        this.name = name.trim();
    }

    public void setClazz(String clazz)
    {
        this.clazz = clazz.trim();
    }

    public void setDelay(long delay)
    {
        this.delay = delay;
    }

    public String getName()
    {
        return name;
    }

    public String getClazz()
    {
        return clazz;
    }

    public long getDelay()
    {
        return delay;
    }

    public void setDelete(Long delete)
    {
        this.delete = delete;
    }

    public boolean isHandlerUsingObsoleteSettings(JiraServiceContainer service) {
        final Map<String, String> params;
        try
        {
            params = ConfigurableObjectUtil.getPropertyMap(service);
            return params != null && params.containsKey("handler.params")
                    && (params.get("handler.params").contains("port=") || params.get("handler.params").contains("usessl="));
        }
        catch (Exception e)
        {
            return false;
        }
    }

    private boolean isMailRelatedServiceClass(Class<?> clazz) {
        return MailFetcherService.class.isAssignableFrom(clazz) || FileService.class.isAssignableFrom(clazz);
    }
    
    public boolean isEditable(JiraServiceContainer serviceContainer)
    {
        if (serviceContainer == null || serviceContainer.getServiceClass() == null) {
            return false;
        }
        try
        {
            Class<?> serviceClass = componentClassManager.loadClass(serviceContainer.getServiceClass());
            return !isMailRelatedServiceClass(serviceClass);
        }
        catch (ClassNotFoundException e)
        {
            return true; // let's allow editing in such unlikely case
        }
    }
}
