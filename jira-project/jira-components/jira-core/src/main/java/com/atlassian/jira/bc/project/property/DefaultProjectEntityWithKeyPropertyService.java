package com.atlassian.jira.bc.project.property;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.entity.property.BaseEntityWithKeyPropertyService;
import com.atlassian.jira.entity.property.DelegatingEntityWithKeyPropertyService;
import com.atlassian.jira.entity.property.JsonEntityPropertyManager;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.util.I18nHelper;

/**
 * @since v6.2
 */
public class DefaultProjectEntityWithKeyPropertyService extends DelegatingEntityWithKeyPropertyService<Project> implements ProjectPropertyService
{
    public DefaultProjectEntityWithKeyPropertyService(JsonEntityPropertyManager jsonEntityPropertyManager, I18nHelper i18nHelperDelegate,
            EventPublisher eventPublisher, ProjectPropertyHelper propertyHelper)
    {
        super(new BaseEntityWithKeyPropertyService<Project>(jsonEntityPropertyManager, i18nHelperDelegate, eventPublisher, propertyHelper));
    }
}
