package com.atlassian.jira.bc.issue.changehistory.properties;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.entity.property.BaseEntityPropertyService;
import com.atlassian.jira.entity.property.JsonEntityPropertyManager;
import com.atlassian.jira.issue.changehistory.ChangeHistory;
import com.atlassian.jira.util.I18nHelper;

public class DefaultChangeHistoryPropertyService
        extends BaseEntityPropertyService<ChangeHistory> implements ChangeHistoryPropertyService
{
    public DefaultChangeHistoryPropertyService(final JsonEntityPropertyManager jsonEntityPropertyManager,
            final I18nHelper i18n,
            final EventPublisher eventPublisher,
            final ChangeHistoryPropertyHelper entityPropertyHelper)
    {
        super(jsonEntityPropertyManager, i18n, eventPublisher, entityPropertyHelper);
    }
}
