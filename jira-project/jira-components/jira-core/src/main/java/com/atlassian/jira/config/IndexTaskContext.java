package com.atlassian.jira.config;

import com.atlassian.annotations.Internal;
import com.atlassian.jira.util.I18nHelper;
import com.google.common.annotations.VisibleForTesting;

/**
 * Context for global index operations. Only one global index operation is
 * allowed at one time. This should be instantiated only for task querying. For
 * submitting task please instantiate its children.
 *
 * @since v3.13
 */
@Internal
@VisibleForTesting
public class IndexTaskContext implements IndexTask
{
    public String buildProgressURL(final Long taskId)
    {
        return "/secure/admin/jira/IndexProgress.jspa?taskId=" + taskId;
    }

    public boolean equals(final Object o)
    {
        return (o != null) && (o instanceof IndexTaskContext);
    }

    public int hashCode()
    {
        return getClass().hashCode();
    }

    @Override
    public String getTaskInProgressMessage(final I18nHelper i18n)
    {
        return null;
    }
}
