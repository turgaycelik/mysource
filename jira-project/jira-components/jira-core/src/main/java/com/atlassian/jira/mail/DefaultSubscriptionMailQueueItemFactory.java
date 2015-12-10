/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.mail;

import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.template.TemplateManager;
import com.atlassian.jira.user.util.UserManager;
import org.ofbiz.core.entity.GenericValue;

public class DefaultSubscriptionMailQueueItemFactory implements SubscriptionMailQueueItemFactory
{
    private final MailingListCompiler mailingListCompiler;
    private final SearchService searchService;
    private final TemplateManager templateManager;
    private final UserManager userManager;
    private final GroupManager groupManager;

    public DefaultSubscriptionMailQueueItemFactory(final MailingListCompiler mailingListCompiler,
            final SearchService searchService, final TemplateManager templateManager, final UserManager userManager,
            final GroupManager groupManager)
    {
        this.mailingListCompiler = mailingListCompiler;
        this.searchService = searchService;
        this.templateManager = templateManager;
        this.userManager = userManager;
        this.groupManager = groupManager;
    }

    public SubscriptionMailQueueItem getSubscriptionMailQueueItem(final GenericValue sub)
    {
        return new SubscriptionMailQueueItem(sub, mailingListCompiler, searchService, templateManager, userManager, groupManager);
    }
}
