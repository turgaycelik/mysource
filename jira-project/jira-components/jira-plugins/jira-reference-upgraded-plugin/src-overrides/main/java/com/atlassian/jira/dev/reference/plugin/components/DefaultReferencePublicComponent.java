package com.atlassian.jira.dev.reference.plugin.components;

/**
 * <p>
 * Sample public component that returns a simple message. This component will be exported and made available to other
 * plugins installed in the system.
 *
 * <p>
 * An upgraded version.
 *
 * @since v4.3
 */
public class DefaultReferencePublicComponent implements ReferencePublicComponent
{
    public String getMessage()
    {
        return "This is a simple message exported by the JIRA UPGRADED reference plugin";
    }
}