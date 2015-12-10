package com.atlassian.jira.config;

import com.atlassian.jira.task.TaskContext;
import com.atlassian.jira.util.I18nHelper;

/**
 * Interface that should be implemented by index task context classes.
 *
 * @since v6.1
 */
public interface IndexTask extends TaskContext
{
    String getTaskInProgressMessage(I18nHelper i18n);
}
