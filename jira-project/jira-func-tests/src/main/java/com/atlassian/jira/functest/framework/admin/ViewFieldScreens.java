package com.atlassian.jira.functest.framework.admin;

/**
 * Represents functionality of the 'Screens' admin page.
 *
 * @since v4.2
 */
public interface ViewFieldScreens
{

    /**
     * Navigates to the 'Screens' page.
     *
     * @return this ViewFieldScreens instance
     */
    public ViewFieldScreens goTo();


    /**
     * Go to the 'Configure screen' page for a given <tt>screenName</tt>.
     *
     * @param screenName name of the screen to configure
     * @return {@link ConfigureScreen} instance for given screen.
     */
    public ConfigureScreen configureScreen(String screenName);
}
