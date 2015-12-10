package com.atlassian.jira.config.util;

/**
 * Useful for mocking out license checks in component tests.
 */
public interface EditionCheck
{
    boolean isEnterprise();
    boolean isProfessional();
}
