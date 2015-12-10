/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.service.services.mail;

import com.atlassian.configurable.ValuesGenerator;
import com.atlassian.mail.MailFactory;
import com.atlassian.mail.server.PopMailServer;

import java.util.List;
import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;

public class MailServersValuesGenerator implements ValuesGenerator
{
    public Map getValues(Map params)
    {
        final Map<String, String> returnValues = newHashMap();
        List<PopMailServer> pops = MailFactory.getServerManager().getPopMailServers();
        for (PopMailServer mailServer : pops)
        {
            returnValues.put(mailServer.getId().toString(), mailServer.getName());
        }
        return returnValues;
    }
}
