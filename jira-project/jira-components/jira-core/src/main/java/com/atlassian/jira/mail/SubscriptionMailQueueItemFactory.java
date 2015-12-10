/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.mail;

import org.ofbiz.core.entity.GenericValue;

public interface SubscriptionMailQueueItemFactory
{
    public SubscriptionMailQueueItem getSubscriptionMailQueueItem(GenericValue sub);
}
