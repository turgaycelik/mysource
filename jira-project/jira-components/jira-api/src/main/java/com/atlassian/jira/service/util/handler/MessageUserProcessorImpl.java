package com.atlassian.jira.service.util.handler;

import com.atlassian.annotations.Internal;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.mail.MailUtils;

import javax.annotation.Nullable;
import javax.mail.Message;
import javax.mail.MessagingException;
import java.util.List;

/**
 * @since v5.0
 */
@Internal
public class MessageUserProcessorImpl implements MessageUserProcessor
{

    private final UserManager userManager;

    public MessageUserProcessorImpl(UserManager userManager)
    {
        this.userManager = userManager;
    }

    /**
     * For each sender of the given message in turn, look up a User first with a case-insensitively equal email address,
     * and failing that, with a username equal to the email address.
     * <p/>
     * JIRA wants to do this because when we create users in email handlers, we set email and username equal. If a user
     * subsequently changes their email address, we must not assume they don't exist and create them with the email
     * address as the username.
     *
     * @param message the message from which to get the User.
     * @return the User matching the sender of the message or null if none found.
     * @throws javax.mail.MessagingException if there's strife getting the message sender.
     */
    @Override
    @Nullable
    public User getAuthorFromSender(final Message message) throws MessagingException
    {

        final List<String> senders = MailUtils.getSenders(message);
        User user = null;
        for (final String emailAddress : senders)
        {
            user = findUserByEmail(emailAddress);
            if (user != null)
            {
                break;
            }
            user = findUserByUsername(emailAddress);
            if (user != null)
            {
                break;
            }
        }

        return user;
    }

    /**
     * Finds the user with the given username or returns null if there is no such User. Convenience method which doesn't
     * throw up.
     *
     * @param username the username.
     * @return the User or null.
     */
    @Nullable
    public User findUserByUsername(final String username)
    {
        return userManager.getUserObject(username);
    }

    /**
     * Returns the first User found with an email address that equals the given emailAddress case insensitively.
     *
     * @param emailAddress the email address to match.
     * @return the User.
     */
    @Nullable
    public User findUserByEmail(final String emailAddress)
    {
        for (final User user : userManager.getAllUsers())
        {
            if (emailAddress.equalsIgnoreCase(user.getEmailAddress()))
            {
                return user;
            }
        }
        return null;
    }
}
