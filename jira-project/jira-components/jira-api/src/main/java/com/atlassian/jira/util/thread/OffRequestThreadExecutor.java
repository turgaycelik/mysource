package com.atlassian.jira.util.thread;

import com.atlassian.annotations.PublicApi;
import com.atlassian.jira.user.ApplicationUser;

import java.util.concurrent.Executor;

/**
 * If you ever need to run code in JIRA off a HTTP request thread then you can sue this class to help setup and clean up
 * the thread environment.
 * <p/>
 * JIRA uses a fair few {@link ThreadLocal} variables setup during HTTP requests to contain common information such as
 * the logged in user and their {@link java.util.Locale} and so on say via {@link com.atlassian.jira.security.JiraAuthenticationContext}
 * <p/>
 * This class will help you run the off request thread code and clean up after it properly.
 *
 * @since v6.0
 */
@PublicApi
public interface OffRequestThreadExecutor extends Executor
{
    /**
     * Executes the code with No user in context via {@link com.atlassian.jira.security.JiraAuthenticationContext}
     *
     * @param command the code to run
     */
    void execute(Runnable command);

    /**
     * Executes the code with the specified user in context via {@link com.atlassian.jira.security.JiraAuthenticationContext}
     *
     * @param runAsUser the user to run the code as
     * @param command the code to run
     */
    void execute(ApplicationUser runAsUser, Runnable command);

}
