/*
 * Copyright (C) 2002-2011 Atlassian
 * All rights reserved.
 */
package com.atlassian.jira.service.util.handler;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

@XmlAccessorType (XmlAccessType.FIELD)
public class MessageHandlerStats
{
    private int issuesCreated;
    private int usersCreated;
    private int commentsCreated;
    private int messages;
    private int attachmentsCreated;
    private int messagesRejected;

    public MessageHandlerStats(int messages, int issuesCreated, int usersCreated, int commentsCreated, int attachmentsCreated,
            int messagesRejected)
    {
        this.messages = messages;
        this.issuesCreated = issuesCreated;
        this.usersCreated = usersCreated;
        this.commentsCreated = commentsCreated;
        this.attachmentsCreated = attachmentsCreated;
        this.messagesRejected = messagesRejected;
    }

    public int getIssuesCreated()
    {
        return issuesCreated;
    }

    public int getUsersCreated()
    {
        return usersCreated;
    }

    public int getCommentsCreated()
    {
        return commentsCreated;
    }

    public int getMessages()
    {
        return messages;
    }

    public int getAttachmentsCreated()
    {
        return attachmentsCreated;
    }

    public int getMessagesRejected()
    {
        return messagesRejected;
    }
}
