package com.atlassian.jira.functest.framework.admin;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import java.util.List;

/**
 * Represents the Mail Servers Administration Page
 *
 * @since v4.3
 */
public interface MailServerAdministration
{
    /**
     * Gets an instance of the Smtp Mail Server Administration Panel.
     * @return An instance of the Smtp Mail Server Administration Panel.
     */
    SmtpMailServerAdministration Smtp();

    /**
     * Gets an instance of the POP/IMAP Mail Server Administration Panel.
     * @return An instance of the POP/IMAP Mail Server Administration Panel.
     */
    PopMailServerAdministration Pop();

    /**
     * Represents the 'Send test Email' panel in the Mail Servers Administration Page.
     * @since 6.3.9
     */
    interface SendTestEmail {

        /**
         * Presses the 'Send' button.
         */
        void send();

        /**
         * Assert the message has been successfully sent, that is to say
         * searches for "Your test message has been sent" in Mail log table.
         */
        void assertMessageSentInformationDisplayed();

    }

    /**
     * Represents the Smtp Mail Server Panel in the Mail Servers Administration Page.
     *
     * @since v4.3
     */
    interface SmtpMailServerAdministration
    {

        /**
         * Determines whether the Smtp Mail Server Administration Panel is on the page.
         * @return true if the Smtp Mail Server Administration Panel is on the page; otherwise, false.
         */
        boolean isPresent();

        /**
         * Gets the current Smtp Mail Server configuration.
         * @return the current Smtp Mail Server configuration. If there is no configured mail server null is returned.
         */
        MailServerConfiguration get();

        /**
         * Determines whether an SMTP Server has been configured.
         * @return true if an Smtp Server has been configured; otherwise, false.
         */
        boolean isConfigured();

        /**
         * Adds a new SMTP Server Configuration
         * @param name The name of this server within JIRA.
         * @param fromAddress The default address this server will use to send emails from.
         * @param emailPrefix The prefix for all outgoing email subjects.
         * @param hostName The SMTP host name of your mail server.
         * @return this instance of the mail servers administration page.
         */
        MailServerAdministration add(String name, String fromAddress, String emailPrefix, String hostName);

        /**
         * Navigates to the Mail Servers Administration Page.
         * @return this instance of the page.
         */
        SmtpMailServerAdministration goTo();

        SendTestEmail sendTestEmail();

        boolean isEnabled();

        boolean isDisabled();

        void enable();
    }

    /**
     * Represents the POP/IMAP Mail Server Panel in the Mail Servers Administration Page.
     *
     * @since v4.3
     */
    interface PopMailServerAdministration
    {
        /**
         * Adds a new POP/IMAP Server configuration
         * @param name The name to use for this server within JIRA.
         * @param hostName The host name of the POP/IMAP server.
         * @param userName The user name for the POP/IMAP account.
         * @param password The password for the POP/IMAP account.
         * @return An instance of the Mail Server Administration page.
         */
        MailServerAdministration add(String name, String hostName, String userName, String password);

        /**
         * Determines whether the POP/IMAP Mail Server Administration Panel is on the page.
         * @return true if the POP/IMAP Mail Server Administration Panel is on the page; otherwise, false.
         */
        boolean isPresent();

        /**
         * Gets the list of current POP/IMAP Mail Server configurations.
         * @return the current POP/IMAP Mail Server configuration. If there is no configured mail servers an empty
         * iterable is returned.
         */
        List<MailServerConfiguration> list();

        /**
         * Navigates to the edit pop server configuration form for a mail server.
         * @param mailServerName The name of the mail server to edit.
         * @return An instance of EditPopServerConfiguration that can be used to update the configuration of the
         * specified mail server.
         */
        EditPopServerConfiguration edit(String mailServerName);

        /**
         * Removes a mail server with a specified name. It confirms the deletion on the confirm delete page for you.
         * @param mailServerName The name of the mail server to delete.
         * @return
         */
        MailServerAdministration delete(String mailServerName);

        /**
         * Navigates to the Mail Servers Administration Page.
         * @return this instance of the page.
         */
        PopMailServerAdministration goTo();
    }

    /**
     * Represents the configuration of a Mail Server in JIRA.
     */
    class MailServerConfiguration
    {
        private final String name;

        private final String hostName;

        private final String userName;

        public MailServerConfiguration(String name, String hostName, final String userName)
        {
            this.name = name;
            this.hostName = hostName;
            this.userName = userName;
        }

        public String getName()
        {
            return name;
        }

        public String getHostName()
        {
            return hostName;
        }

        public String getUserName()
        {
            return userName;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (this == obj) { return true; }

            if (!(obj instanceof MailServerConfiguration)) { return false; }

            MailServerConfiguration rhs = (MailServerConfiguration) obj;

            return new EqualsBuilder().
                    append(name, rhs.name).
                    isEquals();
        }

        @Override
        public int hashCode()
        {
            return new HashCodeBuilder(17,31).
                    append(name).
                    toHashCode();
        }

        @Override
        public String toString()
        {
            return new ToStringBuilder(this).
                    append("name", name).
                    append("hostName", hostName).
                    append("userName", userName).
                    toString();
        }
    }
}
