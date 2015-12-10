package com.atlassian.jira.web.bean;

/**
 * Factory to create a {@link BulkEditBean}
 *
 * @since v5.0
 */
public interface BulkEditBeanFactory
{
    /**
     * Creates a {@link BulkEditBean}
     *
     * @return a new instance of a {@link BulkEditBean}
     */
    BulkEditBean createBulkEditBean();
}
