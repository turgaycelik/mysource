package com.atlassian.jira.web.util;

/**
 * Support for communication between Webwork actions and checkbox tags.
 * Encapsulates two boolean flags for getting and
 * setting pre- and post-action values of the checkbox. 
 *
 * @since v4.2
 */
public final class CheckboxTagSupport
{
    private boolean preAction;
    private boolean postAction;


    /**
     * Constructor accepting pre-action value of the checkbox. Actions
     * should handle retrieval of this value from appropriate source
     * (e.g. application properties). 
     *
     * @param preAction
     */
    public CheckboxTagSupport(boolean preAction)
    {
        this.preAction = preAction;
    }

    /**
     * Getter for pre-action value of the checkbox.
     * This should be invoked in the action getter
     * to provide value for the checkbox tag via
     * WebWork stack.
     *
     * @return pre-action checkbox value from JIRA
     */
    public boolean preAction()
    {
        return preAction;
    }

    /**
     * Post-action value coming from the reqest. This should be invoked
     * in the action setter.
     *
     * @param postAction post-action checkbox value from the request
     */
    public void postAction(boolean postAction)
    {
        this.postAction = postAction;
    }


    /**
     * Post-action value set from the request. Use it in the business logic
     * in action's #execute methods.
     *
     * @return post-action checkbox value from the request
     */
    public boolean postAction()
    {
        return this.postAction;
    }

    /**
     * Provides information, if the actual value of checkbox was changed.
     * Invoked it only after the actual request was run (i.e. {@link #postAction(boolean)} was
     * invoked).
     *
     * @return <code>true</code>, if the value of checkbox was changed by the last request.
     */
    public boolean hasChanged()
    {
        return preAction != postAction;
    }
}
