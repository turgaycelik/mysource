package com.atlassian.jira.issue.status;

import com.atlassian.jira.issue.status.category.StatusCategory;
import com.atlassian.jira.util.I18nHelper;

/**
 * @since v6.1
 */
public class SimpleStatusImpl implements SimpleStatus
{
    private final String id;
    private final String name;
    private final String description;
    private final StatusCategory statusCategory;
    private final String iconUrl;

    public SimpleStatusImpl(final String id, final String name, final String description, final StatusCategory statusCategory, final String iconUrl)
    {
        this.id = id;
        this.name = name;
        this.description = description;
        this.statusCategory = statusCategory;
        this.iconUrl = iconUrl;
    }

    public SimpleStatusImpl(Status status)
    {
        this(status.getId(), status.getNameTranslation(), status.getDescTranslation(), status.getStatusCategory(), status.getCompleteIconUrl());
    }

    public SimpleStatusImpl(final Status status, final I18nHelper i18nHelper)
    {
        this(status.getId(), status.getNameTranslation(i18nHelper), status.getDescTranslation(i18nHelper), status.getStatusCategory(), status.getCompleteIconUrl());
    }

    @Override
    public String getId()
    {
        return id;
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public String getDescription()
    {
        return description;
    }

    @Override
    public StatusCategory getStatusCategory()
    {
        return statusCategory;
    }

    @Override
    public String getIconUrl()
    {
        return iconUrl;
    }
}
