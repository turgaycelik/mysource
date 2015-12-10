package com.atlassian.jira.mail;

import java.util.Arrays;
import java.util.List;

import javax.annotation.Nonnull;

import com.atlassian.core.util.StringUtils;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.issue.Issue;
import com.atlassian.mail.MailUtils;

import static com.atlassian.jira.user.util.Users.isAnonymous;
import static com.atlassian.jira.mail.threading.EmailHeaderBuilders.InReplyToHeaderBuilder;
import static com.atlassian.jira.mail.threading.EmailHeaderBuilders.MessageIdBuilder;

/**
 * Helper methods for common mail related operations.
 */
public class JiraMailUtils
{
    private static final String JIRA_HOST_NAME_DEFAULT = "Atlassian.JIRA";

    /**
     * @deprecated Use {@link com.atlassian.mail.server.MailServerManager#isDefaultSMTPMailServerDefined()} instead.
     * Since 5.0
     */
    @Deprecated
    public static boolean isHasMailServer()
    {
        return (ComponentAccessor.getMailServerManager().getDefaultSMTPMailServer() != null);
    }

    /**
     * Returns the specified email address of the issue's project.
     * <p/>
     * @param issue The issue to be notified about
     * @return The email address of the issue's project
     */
    static String getProjectEmailFromIssue(final Issue issue)
    {
        return issue.getProjectObject().getEmail();
    }

    /**
     * Returns the sender's name in the format specified by {@link com.atlassian.jira.config.properties.APKeys#EMAIL_FROMHEADER_FORMAT} ('Joe Bloggs (JIRA)' usually).
     * <p/>
     * @param sender The user sending the email
     * @return The sender's name in the specified format
     */
    static String getFromNameForUser(final User sender)
    {
        String from = ComponentAccessor.getApplicationProperties().getDefaultBackedString(APKeys.EMAIL_FROMHEADER_FORMAT);
        if (from == null)
        {
            return null;
        }

        String name;

        if (isAnonymous(sender))
        {
            name = "Anonymous";
        }
        else
        {
            try
            {
                final String fullName = sender.getDisplayName();
                if (org.apache.commons.lang.StringUtils.isBlank(fullName))
                {
                    name = sender.getName();
                }
                else
                {
                    name = fullName;
                }
            }
            catch (final Exception exception)
            {
                // this should never fail, but incase it does we don't want to imply it was a anonymous sender.
                try
                {
                    name = sender.getName();
                }
                catch (final Exception exception2)
                {
                    name = "";
                }
            }
        }

        String email;
        try
        {
            email = (sender != null ? sender.getEmailAddress() : "");
        }
        catch (final Exception exception)
        {
            email = "";
        }
        final String hostname = ((sender != null) && (email != null) ? email.substring(email.indexOf("@") + 1) : "");

        from = StringUtils.replaceAll(from, "${fullname}", name);
        from = StringUtils.replaceAll(from, "${email}", email);
        from = StringUtils.replaceAll(from, "${email.hostname}", hostname);
        return from;
    }

    private static String getJiraHostname()
    {
        // JDEV-28184: Email threads were broken in cluster because JIRA was putting hostname of the node it was running
        // on in the in-reply-to and references fields of email header.
        // We could use base url to get cluster's hostname, but mail threads would break if it was changed by an admin.
        // The IDs generated for messages are unique enough given that they use both issue ID and creation date.
        return JIRA_HOST_NAME_DEFAULT;
    }

    /**
     * Generates a single ID to identify a specific email sent by JIRA and used in 'message-id' header field.
     * @return A single 'message-id' as in http://www.ietf.org/rfc/rfc2822.txt, cannot be null.
     */
    @Nonnull
    public static String getMessageId(Issue issue, int sequence)
    {
        String hostname = getJiraHostname();

        return new MessageIdBuilder(issue).
                setSequence(sequence).
                setHostName(hostname).
                build();
    }

    /**
     * Returns a list of IDs to be used in 'in-reply-to' and 'references' field of the email header.
     * The first item in this list will be in the format currently in use, followed by all the other formats supported.
     * @return a list of IDs as in http://www.ietf.org/rfc/rfc2822.txt, cannot be null or an empty list.
     */
    @Nonnull
    public static List<String> getReplyToIds(Issue issue)
    {
        String hostname = getJiraHostname();

        // Current format, using a single value for hostname on every node
        final String parentMessageId = new InReplyToHeaderBuilder(issue).
                setHostName(hostname).
                build();

        // Old format
        final String parentMessageIdOld = new InReplyToHeaderBuilder(issue).
                setDropMillis(false).
                setHostName(MailUtils.getLocalHostName()).
                build();

        return Arrays.asList(parentMessageId, parentMessageIdOld);
    }
}
