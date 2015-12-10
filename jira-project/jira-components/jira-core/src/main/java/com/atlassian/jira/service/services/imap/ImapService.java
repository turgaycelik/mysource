/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.service.services.imap;

import com.atlassian.annotations.Internal;
import com.atlassian.configurable.ObjectConfiguration;
import com.atlassian.configurable.ObjectConfigurationException;
import com.atlassian.jira.service.AbstractService;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;


/**
 * This class remains here only to prevent alarming exceptions from occuring during system upgrade.
 * Please don't use it for any purpose, extend the MailFetcherService class directly.
 *
 * @deprecated Use MailFetcherService directly
 */
@Deprecated
@Internal
public final class ImapService extends AbstractService
{
    @Override
    public ObjectConfiguration getObjectConfiguration() throws ObjectConfigurationException
    {
        return getObjectConfiguration("MAILFETCHERSERVICE", "services/com/atlassian/jira/service/services/mail/mailfetcherservice.xml", null);
    }

    @Override
    public void run()
    {
        final Logger logger = Logger.getLogger(getClass());
        logger.error("This class is deprecated, any service using it should have been automatically upgraded to use MailFetcherService directly.");
        final String name = StringUtils.defaultString(getName(), "any");
        logger.error("Please remove " + name + " service that uses " + getClass() + " and create new mail handlers from the Incoming Mail Servers screen.");
    }
}
