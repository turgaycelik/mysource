package com.atlassian.jira.functest.framework.admin;

/**
 * Framework for manipulating resolutions
 *
 * @since v4.0
 */
public interface Resolutions
{
    /**
     * @param name the name of the resolution to add
     * @return the id of the added resolution
     */
    String addResolution(String name);
}
