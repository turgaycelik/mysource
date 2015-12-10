package com.atlassian.jira.functest.config.sharing;

/**
 * Represents and config entity that can be shared.
 *
 * @since v4.2
 */
public interface ConfigSharedEntity
{
    String getEntityType();
    Long getId();
}
