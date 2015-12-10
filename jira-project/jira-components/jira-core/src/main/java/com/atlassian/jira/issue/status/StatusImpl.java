/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.issue.status;

import com.atlassian.jira.config.StatusCategoryManager;
import com.atlassian.jira.issue.IssueConstantImpl;
import com.atlassian.jira.issue.status.category.StatusCategory;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.BaseUrl;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.web.action.admin.translation.TranslationManager;

import com.google.common.base.Preconditions;
import org.ofbiz.core.entity.GenericValue;

public class StatusImpl extends IssueConstantImpl implements Status
{

    private static final String STATUSCATEGORY = "statuscategory";

    private final StatusCategoryManager statusCategoryManager;

    public StatusImpl(GenericValue genericValue, TranslationManager translationManager,
            JiraAuthenticationContext authenticationContext, BaseUrl locator, StatusCategoryManager statusCategoryManager)
    {
        super(genericValue, translationManager, authenticationContext, locator);
        this.statusCategoryManager = statusCategoryManager;
    }

    @Override
    public StatusCategory getStatusCategory()
    {
        if (!statusCategoryManager.isStatusAsLozengeEnabled())
        {
            return null;
        }

        StatusCategory statusCategory = statusCategoryManager.getStatusCategory(getStatusCategoryId());
        if (statusCategory == null)
        {
            return statusCategoryManager.getDefaultStatusCategory();
        }
        return statusCategory;
    }

    @Override
    public void setStatusCategory(final StatusCategory statusCategory)
    {

        final Long id = Preconditions.checkNotNull(statusCategory).getId();
        genericValue.set(STATUSCATEGORY, id);
    }

    public Long getStatusCategoryId()
    {
        return genericValue.getLong(STATUSCATEGORY);
    }

    @Override
    public SimpleStatus getSimpleStatus()
    {
        return new SimpleStatusImpl(this);
    }

    @Override
    public SimpleStatus getSimpleStatus(final I18nHelper i18nHelper)
    {
        return new SimpleStatusImpl(this, i18nHelper);
    }
}
