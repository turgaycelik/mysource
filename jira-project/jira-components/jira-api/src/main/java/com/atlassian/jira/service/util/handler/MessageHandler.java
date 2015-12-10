/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.service.util.handler;

import com.atlassian.annotations.PublicSpi;

import javax.mail.Message;
import javax.mail.MessagingException;
import java.util.Map;

/**
 * An interface representing a message handler. A handler handles messages. Implementers may extend
 * AbstractMessageHandler (provided by JIRA Mail Plugin) to inherit standard functionality such mail loop detection etc.
 * <p>
 * As of JIRA 5.0 MessageHandler implementations provided by plugins are created using dependency injection, so
 * injecting your dependencies instead of calling them statically is the prefered way to go.
 */
@PublicSpi
public interface MessageHandler
{
    /**
     * Will be called before any messages are to be handled.
     * @param params configuration.
     * @param errorCollector potential problems encountered during initialization of the handler
     * should be reported here. Depending on the run mode it may be e.g. displayed back to the user (when handler
     * is tested from UI) or logged to the file .
     */
    void init(Map<String, String> params, MessageHandlerErrorCollector errorCollector);

    /**
     * Perform the specific work of this handler for the given message.
     * <p/>
     * Return true to indicate that the message was successfully processed and can be deleted.
     * Return false to indicate that the message was not processed.
     * If the message is invalid and cannot be processed you should call {@link com.atlassian.jira.service.util.handler.MessageHandlerExecutionMonitor#markMessageForDeletion(java.lang.String)}.
     * Calling this method will mark the message for deletion, but will also attempt to forward the email on to the admin
     * with the given error message if such a forwarding address has been configured.
     *
     * @param message the message to check for handling.
     * @param context user-friendly message handler should utilize this interface
     * to create issue, comments, users and provide feedback information about messages being
     * processed and problems being encountered. While MessageHandler-s are run
     * from UI (in TEST / dry-run mode) such invocations are not mutative (they do not create any
     * JIRA entities) and information provided by the handler is displayed back to the user
     * as a summary of the dry-run.
     *
     * @return true if the message was successfully processed.
     * @throws MessagingException if anything went wrong.
     */
    boolean handleMessage(Message message, MessageHandlerContext context) throws MessagingException;

}
