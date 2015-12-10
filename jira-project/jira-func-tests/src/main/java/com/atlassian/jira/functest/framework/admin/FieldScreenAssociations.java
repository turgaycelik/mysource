package com.atlassian.jira.functest.framework.admin;

/**
 * <p>A class to help configure associations of one particular field to screens. The functionality offered here is akin to:
 * <ul>
 * <li>navigating to a Field Configuration,
 * <li>clicking the Screens link
 * </ul>
 *
 * @see com.atlassian.jira.functest.framework.admin.FieldConfigurations.FieldConfiguration#getScreens(String)
 * @since v4.2
 */
public interface FieldScreenAssociations
{
    /**
     * Removes the current field from the specified screen.
     * @param screen the name of the screen e.g. <code>Resolve Issue Screen</code>
     */
    void removeFieldFromScreen(String screen);

    /**
     * Adds the current field to the specified screen.
     * @param screen the name of the screen e.g. <code>Resolve Issue Screen</code>
     */
    void addFieldToScreen(String screen);
}
