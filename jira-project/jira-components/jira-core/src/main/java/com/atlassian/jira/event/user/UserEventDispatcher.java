/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.event.user;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.util.collect.MapBuilder;

import java.util.HashMap;
import java.util.Map;

public class UserEventDispatcher
{
    public static void dispatchEvent(int type, User user)
    {
        dispatchEvent(type, user, new HashMap<String,Object>());
    }

    public static void dispatchEvent(int type, User user, Map<String,Object> params)
    {
        final MapBuilder<String, Object> mapBuilder = MapBuilder.newBuilder(params);
        mapBuilder.add("baseurl", ComponentAccessor.getApplicationProperties().getString(APKeys.JIRA_BASEURL));
        UserEvent event = new UserEvent(mapBuilder.toMap(), user, type);

        ComponentAccessor.getComponentOfType(EventPublisher.class).publish(event);
    }
}
