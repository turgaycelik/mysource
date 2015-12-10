package com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client;

import com.atlassian.jira.rest.api.gadget.AdminGadget;
import com.atlassian.jira.testkit.client.JIRAEnvironmentData;
import com.atlassian.jira.testkit.client.RestApiClient;
import com.sun.jersey.api.client.WebResource;

/**
 * Client for the admin gadget resource
 *
 * @since v6.0
 */
public class AdminGadgetClient extends RestApiClient<NotifyClient>
{
    /**
     * Constructs a new AdminGadgetClient for a JIRA instance.
     *
     * @param environmentData The JIRA environment data
     */
    public AdminGadgetClient(JIRAEnvironmentData environmentData)
    {
        super(environmentData);
    }

    /**
     * GETs the admin gadget.
     *
     * @return an Admin Gadget
     * @throws com.sun.jersey.api.client.UniformInterfaceException if there's a problem getting the admin gadget
     */
    public AdminGadget get() {
        return createResource().get(AdminGadget.class);
    }

    /**
     * PUTs a task as done
     *
     * @param taskName the task which is done
     */
    public void setTaskDone(String taskName) {
        createResource().path("task").path("done").queryParam("name", taskName).put();
    }

    /**
     * PUTs a task as undone
     *
     * @param taskName the task which is undone
     */
    public void setTaskUnDone(String taskName) {
        createResource().path("task").path("undone").queryParam("name", taskName).put();
    }

    /**
     * PUTs a tasklist as done
     *
     * @param taskListName the tasklist which is done
     */
    public void setTaskListDone(String taskListName) {
        createResource().path("tasklist").path("done").queryParam("name", taskListName).put();
    }

    /**
     * PUTs a tasklist as undone
     *
     * @param taskListName the tasklist which is undone
     */
    public void setTaskListUnDone(String taskListName) {
        createResource().path("tasklist").path("undone").queryParam("name", taskListName).put();
    }

    @Override
    protected WebResource createResource()
    {
        return createResourceGadget().path("admin");
    }
}
