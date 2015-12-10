/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.issue.status;

import com.atlassian.annotations.PublicApi;
import com.atlassian.jira.issue.IssueConstant;
import com.atlassian.jira.issue.status.category.StatusCategory;
import com.atlassian.jira.util.I18nHelper;

@PublicApi
public interface Status extends IssueConstant
{
    /**
     * Get the {@link com.atlassian.jira.issue.status.category.StatusCategory} for this Status.
     *
     * If there is no status category defined, returns {@link com.atlassian.jira.config.StatusCategoryManager#getDefaultStatusCategory()}.
     *
     * @return The {@link com.atlassian.jira.issue.status.category.StatusCategory} for this Status.
     */
    StatusCategory getStatusCategory();

    void setStatusCategory(StatusCategory statusCategory);

    SimpleStatus getSimpleStatus();

    SimpleStatus getSimpleStatus(I18nHelper i18nHelper);

    /**
     * @deprecated Use {@link #getStatusCategory()} instead. Since v6.1.
     */
    @Deprecated
    String getCompleteIconUrl();

    /**
     * @deprecated Use {@link #getStatusCategory()} instead. Since v6.1.
     */
    @Deprecated
    String getIconUrl();
}
