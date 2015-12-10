package com.atlassian.jira.issue.operation;

import com.atlassian.annotations.PublicApi;

@PublicApi
public interface IssueOperation
{
    /**
     * An i18n key used to display a short description of this operation in the UI. e.g &quot; Watch Issues &quot;
     *
     * @return An i18n key used to display a short description of this operation in the UI
     */
    String getNameKey();

    String getDescriptionKey();
}
