package com.atlassian.jira.plugin.report.impl;

/**
 * Conditions allow things to be checked at runtime.
 *
 * @since v3.11
 */
public interface Condition
{
    boolean enabled();
}