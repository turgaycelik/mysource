package com.atlassian.jira.web.action.func.service;

import java.util.Collection;

import com.atlassian.jira.service.JiraServiceContainer;
import com.atlassian.jira.service.ServiceManager;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.sal.api.websudo.WebSudoRequired;

@WebSudoRequired
public class ServiceExecutor extends JiraWebActionSupport
{
    private final ServiceManager manager;

    public ServiceExecutor(final ServiceManager manager)
    {
        this.manager = manager;
    }

    private long serviceId = 0;

    @Override
    protected void doValidation()
    {
        if (serviceId > 0)
        {
            if (!manager.containsServiceWithId(serviceId))
            {
                addError("serviceId", "No service with this id exists");
            }
        }
    }

    @Override
    protected String doExecute() throws Exception
    {
        if (serviceId > 0)
        {
            manager.runNow(serviceId);
        }
        return super.doExecute();
    }

    public long getServiceId()
    {
        return serviceId;
    }

    public Collection<JiraServiceContainer> getServices()
    {
        return manager.getServices();
    }

    public void setServiceId(final long serviceId)
    {
        this.serviceId = serviceId;
    }
}
