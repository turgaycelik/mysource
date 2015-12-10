/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.service.services.mail;

import com.atlassian.configurable.ObjectConfiguration;
import com.atlassian.configurable.ObjectConfigurationException;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.service.AbstractService;
import com.atlassian.jira.service.services.LocalService;
import com.atlassian.mail.queue.MailQueue;

import com.opensymphony.module.propertyset.PropertySet;

public class MailQueueService extends AbstractService implements LocalService
{
    public void run()
    {
        final MailQueue queue = ComponentAccessor.getComponent(MailQueue.class);

        log.debug("Attempting to run mail queue service");

        if (queue.size() < 1)
            return;
        if (!queue.isSending())
        {
            log.debug("Starting to send items in the mail queue.");
            queue.sendBuffer();
        }
        else if (log.isDebugEnabled())
        {
            log.debug("Skipping mail queue service run, service seems to be running already.");
            String detail = String.format("Queue Information: \n\n Number of elements: %d\n"
                    + "Number of elements in error: %d\nItem being sent: %s",
                    queue.size(), queue.errorSize(), queue.getItemBeingSent()
            );
            log.debug(detail);
        }
    }

    public void init(PropertySet props) throws ObjectConfigurationException
    {
        log.debug("initialising MailQueueService");
    }

    public boolean isUnique()
    {
        return true;
    }

    public boolean isInternal()
    {
        return true;
    }

    public void destroy()
    {
        log.debug("destroying MailQueueService, running one last time to ensure we send the queue");
        run();
    }

    public ObjectConfiguration getObjectConfiguration() throws ObjectConfigurationException
    {
        return getObjectConfiguration("MAILQUEUESERVICE", "services/com/atlassian/jira/service/services/mail/mailservice.xml", null);
    }
}
