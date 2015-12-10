package com.atlassian.jira.bc.issue.properties;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.entity.property.BaseEntityWithKeyPropertyService;
import com.atlassian.jira.entity.property.DelegatingEntityWithKeyPropertyService;
import com.atlassian.jira.entity.property.JsonEntityPropertyManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.util.I18nHelper;

/**
 * @since v6.2
 */
public class DefaultIssueEntityWithKeyPropertyService extends DelegatingEntityWithKeyPropertyService<Issue> implements IssuePropertyService
{
    public DefaultIssueEntityWithKeyPropertyService(JsonEntityPropertyManager jsonEntityPropertyManager, I18nHelper i18nHelperDelegate,
            EventPublisher eventPublisher, IssuePropertyHelper propertyHelper)
    {
        super(new BaseEntityWithKeyPropertyService<Issue>(jsonEntityPropertyManager, i18nHelperDelegate, eventPublisher, propertyHelper));
    }
}
