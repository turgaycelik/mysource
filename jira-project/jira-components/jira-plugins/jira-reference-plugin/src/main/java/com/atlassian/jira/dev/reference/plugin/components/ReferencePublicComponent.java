package com.atlassian.jira.dev.reference.plugin.components;

/**
 * Sample public component that returns a simple message. This component will be exported and made available to other
 * plugins installed in the system.
 *
 * @since v4.3
 */
public interface ReferencePublicComponent
{
    String getMessage();
}
