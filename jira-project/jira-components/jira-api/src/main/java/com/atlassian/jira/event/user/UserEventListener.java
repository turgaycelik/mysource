/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.event.user;

import com.atlassian.jira.event.JiraListener;

/**
 * The UserEventListener listens for all UserEvents.
 *
 * @see UserEvent
 */
public interface UserEventListener extends JiraListener
{
    /**
     * Fired when a user signs up manually
     *
     * @param event the event in play
     */
    public void userSignup(UserEvent event);

    /**
     * Fired when a user is created automatically
     *
     * @param event the event in play
     */
    public void userCreated(UserEvent event);

    /**
     * Fired when a user indicates they have forgotten their password
     *
     * @param event the event in play
     */
    public void userForgotPassword(UserEvent event);

    /**
     * Fired when a user indicates they have forgotten their username
     *
     * @param event the event in play
     */
    public void userForgotUsername(UserEvent event);

    /**
     * Fired when a user tries to change their password, and the password cannot be updated
     *
     * @param event the event in play
     */
    public void userCannotChangePassword(UserEvent event);
}
