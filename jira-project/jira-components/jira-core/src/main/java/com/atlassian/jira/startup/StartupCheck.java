package com.atlassian.jira.startup;

/**
 * Represents an individual check to be done on startup.
 *
 * @since v4.0
 */
public interface StartupCheck
{

    /**
     * Implement this method to return the name of this check
     *
     * @return name
     */
    public String getName();

    /**
     * Implement this method to return true if the check is positive and false in the case of negative result.
     *
     * @return true if positive
     */
    public boolean isOk();

    /**
     * Implement this method to return the description of the fault. This method should return null in the case the
     * check was positive. This message is used to present the user with a message to the console
     *
     * @return fault description
     */
    public String getFaultDescription();

    /**
     * Implement this method to return the error message of the fault. This method should return null in the case the
     * check was positive. This message is used to present the user with a message viewable in a web browser.
     *
     * @return HTML formatted fault description
     */
    public String getHTMLFaultDescription();

    /**
     * Called when the instance is coming down
     */
    void stop();
}
