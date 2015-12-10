package com.atlassian.jira.plugin.studio;

import com.atlassian.util.concurrent.Function;

import java.util.Collection;

/**
 * Methods on this interface are called by JIRA when it makes some of its Licensing decisions.
 *
 * All of these methods will need to be thread safe. These method can be called by multiple threads at the same time.
 * The same method can be called by my multiple threads at the same time.
 *
 * All of these methods will need to be performant. JIRA will not do any caching of the results and will probably
 * call a method with the same arguments multiple times (possibly at the same time).
 * 
 * @since v4.4.2
 */
public interface StudioLicenseHooks
{
    /**
     * Called when a call to {@link com.atlassian.jira.user.util.UserUtil#clearActiveUserCount()} is made. JIRA's
     * default implementation of this method can be called by invoking the function passed to the method.
     *
     * @param method function that can be used to invoke JIRAs default implementation of this method.
     */
    void clearActiveUserCount(Function<Void, Void> method);

    /**
     * Called when a call to {@link com.atlassian.jira.user.util.UserUtil#hasExceededUserLimit()} is made. JIRA's
     * default implementation of this method can be called by invoking the function passed to the method.
     *
     * @param method function that can be used to invoke JIRAs default implementation of this method.
     * @return see {@link com.atlassian.jira.user.util.UserUtil#hasExceededUserLimit()}}.
     */
    boolean hasExceededUserLimit(Function<Void, Boolean> method);

    /**
     * Called when a call to {@link com.atlassian.jira.user.util.UserUtil#canActivateNumberOfUsers(int)} is made. JIRA's
     * default implementation of this method can be called by invoking the function passed to the method.
     *
     * @param numUsers see {@link com.atlassian.jira.user.util.UserUtil#canActivateNumberOfUsers(int)}
     * @param method function that can be used to invoke JIRAs default implementation of this method.
     * @return see {@link com.atlassian.jira.user.util.UserUtil#canActivateNumberOfUsers(int)}
     */
    boolean canActivateNumberOfUsers(int numUsers, Function<Integer, Boolean> method);

    /**
     * Called when a call to {@link com.atlassian.jira.user.util.UserUtil#canActivateUsers(java.util.Collection)} is
     * made. JIRA's default implementation of this method can be called by invoking the function passed to the method.
     *
     * @param userNames see {@link com.atlassian.jira.user.util.UserUtil#canActivateUsers(java.util.Collection)}
     * @param method function that can be used to invoke JIRAs default implementation of this method.
     * @return see {@link com.atlassian.jira.user.util.UserUtil#canActivateUsers(java.util.Collection)}
     */
    boolean canActivateUsers(Collection<String> userNames, Function<Collection<String>, Boolean> method);
}
