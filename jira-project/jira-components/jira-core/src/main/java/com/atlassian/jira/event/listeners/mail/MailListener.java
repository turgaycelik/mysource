/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.event.listeners.mail;

import java.util.Map;

import com.atlassian.event.api.EventListener;
import com.atlassian.jira.event.issue.AbstractIssueEventListener;
import com.atlassian.jira.event.issue.IssueEvent;
import com.atlassian.jira.event.issue.IssueEventBundle;
import com.atlassian.jira.event.issue.IssueEventBundleFactory;
import com.atlassian.jira.event.issue.IssueEventListener;
import com.atlassian.jira.event.user.UserEvent;
import com.atlassian.jira.event.user.UserEventListener;
import com.atlassian.jira.mail.UserMailQueueItem;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.user.util.UserUtilImpl;
import com.atlassian.jira.util.ImportUtils;
import com.atlassian.mail.queue.MailQueue;
import com.atlassian.mail.queue.MailQueueItem;


/**
 * A listener for emailing notifications.
 * <p>
 * At the moment when this listener is activated it will email the reporter, assignee and any watchers.
 * <p>
 * The content of the emails is governed by Velocity templates (in /templates) and the emails are sent
 * out by the MailSender.
 * <p>
 * Parameters:
 * email.from        - who the emails should be sent from
 * email.server        - the SMTP server to send email through
 * email.session    - the JNDI location of a MailSession to use when sending email
 * subject.prefix    - (optional) any prefix for the subject, like "[FooBar]" -> Subject: [FooBar] BazBat
 *
 * @see DebugMailListener
 */
public class MailListener extends AbstractIssueEventListener implements IssueEventListener, UserEventListener
{
    private final UserManager userManager;
    private final IssueEventBundleMailHandler issueEventBundleMailHandler;
    private final MailQueue mailQueue;
    private final IssueEventBundleFactory issueEventBundleFactory;

    public MailListener(
            UserManager userManager,
            IssueEventBundleMailHandler issueEventBundleMailHandler,
            MailQueue mailQueue,
            IssueEventBundleFactory issueEventBundleFactory
    ) {
        this.userManager = userManager;
        this.issueEventBundleMailHandler = issueEventBundleMailHandler;
        this.mailQueue = mailQueue;
        this.issueEventBundleFactory = issueEventBundleFactory;
    }

    @Override
    public void init(Map params)
    {
        // do nothing
    }

    @Override
    public String[] getAcceptedParams()
    {
        return new String[0];
    }

    @Override
    public boolean isInternal()
    {
        return true;
    }

    @Override
    public boolean isUnique()
    {
        return true;
    }

    @Override
    public String getDescription()
    {
        return "For each user or issue event, generate an appropriate email, and send to the required participants.";
    }

    @Override
    public void userSignup(UserEvent event)
    {
        if (Boolean.TRUE.equals(event.getParams().get(UserUtilImpl.SEND_EMAIL)))
        {
            sendUserMail(event, "Account signup", "template.user.signup.subject", "usersignup.vm");
        }
    }

    @Override
    public void userCreated(UserEvent event)
    {
        if (Boolean.TRUE.equals(event.getParams().get(UserUtilImpl.SEND_EMAIL)))
        {
            if (userManager.canUpdateUserPassword(event.getUser()))
            {
                sendUserMail(event, "Account created", "template.user.created.subject", "usercreated.vm");
            }
            else
            {
                sendUserMail(event, "Account created", "template.user.created.subject", "usercreated-nopassword.vm");
            }
        }
    }

    @Override
    public void userForgotPassword(UserEvent event)
    {
        sendUserMail(event, "Account password", "template.user.forgotpassword.subject", "forgotpassword.vm");
    }

    @Override
    public void userForgotUsername(UserEvent event)
    {
        sendUserMail(event, "Account usernames", "template.user.forgotusername.subject", "forgotusernames.vm");
    }

    @Override
    public void userCannotChangePassword(UserEvent event)
    {
        sendUserMail(event, "Account usernames", "template.user.cannotchangepassword.subject", "cannotchangepassword.vm");
    }

    protected void sendUserMail(UserEvent event, String subject, String subjectKey, String template)
    {
        if (ImportUtils.isEnableNotifications())
        {
            MailQueueItem item = new UserMailQueueItem(event, subject, subjectKey, template);
            mailQueue.addItem(item);
        }
    }

    @EventListener
    public void handleIssueEventBundle(final IssueEventBundle bundle)
    {
        issueEventBundleMailHandler.handle(bundle);
    }

    @Override
    protected void handleDefaultIssueEvent(final IssueEvent event)
    {
        if (event.isRedundant())
        {
            return;
        }
        handleIssueEventBundle(issueEventBundleFactory.wrapInBundle(event));
    }
}
