package com.atlassian.jira.bc.security.login;

import org.apache.log4j.Logger;

/**
 * This class is a place holder for login related loggers
 *
 * @since v4.1
 */
public class LoginLoggers
{
    private static final String PREFIX = "com.atlassian.jira.login.";

    /**
     * A specific logger for the login.jsp page
     */
    public static final Logger LOGIN_PAGE_LOG = Logger.getLogger(PREFIX + "loginpage");
    /**
     * A specific logger for the login gadget mechanism
     */
    public static final Logger LOGIN_GADGET_LOG = Logger.getLogger(PREFIX + "logingadget");
    /**
     * A specific logger for the JIRA auth context
     */
    public static final Logger LOGIN_SETAUTHCTX_LOG = Logger.getLogger(PREFIX + "setauthctx");
    /**
     * A specific logger for the JIRA login cookies
     */
    public static final Logger LOGIN_COOKIE_LOG = Logger.getLogger(PREFIX + "cookies");

    /**
     * A specific logger for the JIRA security events
     */
    public static final Logger LOGIN_SECURITY_EVENTS = Logger.getLogger(PREFIX + "security");
}