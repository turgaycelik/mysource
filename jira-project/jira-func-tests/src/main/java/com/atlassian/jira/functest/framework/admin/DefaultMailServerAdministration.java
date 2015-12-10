package com.atlassian.jira.functest.framework.admin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.atlassian.jira.functest.framework.LocatorFactory;
import com.atlassian.jira.functest.framework.Navigation;
import com.atlassian.jira.functest.framework.locator.CssLocator;
import com.atlassian.jira.functest.framework.locator.Locator;
import com.atlassian.jira.functest.framework.locator.XPathLocator;

import org.apache.commons.lang.NotImplementedException;
import org.w3c.dom.Node;

import net.sourceforge.jwebunit.WebTester;

/**
 * @since v4.3
 */
public class DefaultMailServerAdministration implements MailServerAdministration
{
    public static final String SMTP_SERVERS_ADMINISTRATION_LINK_ID = "outgoing_mail";
    private static final String POP_SERVERS_ADMINISTRATION_LINK_ID = "incoming_mail";

    private final Navigation navigation;

    private final SmtpMailServerAdministration smtpMailServerAdministration;
    private final PopMailServerAdministration popMailServerAdministration;

    public DefaultMailServerAdministration(final WebTester tester, final Navigation navigation, final LocatorFactory locator)
    {
        this.navigation = navigation;

        this.smtpMailServerAdministration = new DefaultSmtpMailServerAdministration(this, tester, locator);
        this.popMailServerAdministration = new DefaultPopMailServerAdministration(this, locator, tester);
    }

    @Override
    public SmtpMailServerAdministration Smtp()
    {
        return smtpMailServerAdministration;
    }

    @Override
    public PopMailServerAdministration Pop()
    {
        return popMailServerAdministration;
    }

    /**
     * @since v4.3
     */
    class DefaultSmtpMailServerAdministration implements SmtpMailServerAdministration
    {
        private final MailServerAdministration mailServerAdministration;

        private final WebTester tester;
        private final LocatorFactory locator;

        public DefaultSmtpMailServerAdministration(final MailServerAdministration mailServerAdministration,
                final WebTester tester, final LocatorFactory locator)
        {
            this.mailServerAdministration = mailServerAdministration;
            this.tester = tester;
            this.locator = locator;
        }

        @Override
        public boolean isPresent()
        {
            return locator.css("#smtp-mail-servers-panel").exists();
        }

        @Override
        public boolean isConfigured()
        {
            if (isPresent())
            {
                return locator.css("#smtp-mail-servers-panel tr td").getText().contains
                        ("You do not currently have an SMTP server configured.");
            }
            return false;
        }

        @Override
        public MailServerAdministration add(final String name, final String fromAddress, final String emailPrefix,
                final String hostName)
        {
            tester.clickLink("add-new-smtp-server");
            tester.setWorkingForm("jiraform");
            tester.setFormElement("name", name);
            tester.setFormElement("from", fromAddress);
            tester.setFormElement("prefix", emailPrefix);
            tester.setFormElement("serverName", hostName);
            tester.submit("Add");
            return mailServerAdministration;
        }

        @Override
        public MailServerConfiguration get()
        {
            throw new NotImplementedException("Not implemented yet");
        }

        @Override
        public SmtpMailServerAdministration goTo()
        {
            navigation.gotoAdminSection(SMTP_SERVERS_ADMINISTRATION_LINK_ID);
            return this;
        }

        @Override
        public SendTestEmail sendTestEmail()
        {
            tester.clickLink("sendTestEmail");
            return new DefaultSendTestEmail(locator, tester);
        }

        @Override
        public boolean isEnabled()
        {
            goTo();
            return locator.css("#outgoing-mail-status.status-active").exists();
        }

        @Override
        public boolean isDisabled()
        {
            goTo();
            return locator.css("#outgoing-mail-toggle.enable-outgoing").exists();
        }

        @Override
        public void enable()
        {
            tester.clickLink("outgoing-mail-toggle");
        }
    }

    /**
     * @since v4.3
     */
    class DefaultPopMailServerAdministration implements PopMailServerAdministration
    {
        private MailServerAdministration mailServerAdministration;

        private final LocatorFactory locator;
        private WebTester tester;

        public DefaultPopMailServerAdministration(final MailServerAdministration mailServerAdministration,
                final LocatorFactory locator, WebTester tester)
        {
            this.mailServerAdministration = mailServerAdministration;
            this.locator = locator;
            this.tester = tester;
        }

        @Override
        public MailServerAdministration add(String name, String hostName, String userName, String password)
        {
            tester.clickLink("add-pop-mail-server");
            tester.setWorkingForm("jiraform");
            tester.setFormElement("name", name);
            tester.setFormElement("serverName", hostName);
            tester.setFormElement("username", userName);
            tester.setFormElement("password", password);
            tester.submit("Add");
            return mailServerAdministration;
        }

        @Override
        public boolean isPresent()
        {
            return locator.css("#pop-mail-servers-panel").exists();
        }

        @Override
        public PopMailServerAdministration goTo()
        {
            navigation.gotoAdminSection(POP_SERVERS_ADMINISTRATION_LINK_ID);
            return this;
        }


        @Override
        public List<MailServerConfiguration> list()
        {
            final Locator popMailServersItemsLocator = locator.css("#pop-mail-servers-table tr");

            final List<MailServerConfiguration> results = new ArrayList<MailServerConfiguration>();

            if (isEmpty())
            {
                return Collections.emptyList();
            }

            for (Node mailServerRow : popMailServersItemsLocator.getNodes())
            {
                if (!isMailServerTableHeader(mailServerRow))
                {
                    final MailServerConfiguration mailServerConfigurationForRow =
                            new MailServerConfiguration
                                    (
                                            extractServerName(mailServerRow), extractHost(mailServerRow),
                                            extractUserName(mailServerRow)
                                    );
                    results.add(mailServerConfigurationForRow);
                }
            }
            return results;
        }

        @Override
        public EditPopServerConfiguration edit(String mailServerName)
        {
            final Node mailServerRow = discoverRowInServersTableFor(mailServerName);

            final Locator editServerLinkLocator = new XPathLocator(mailServerRow, "//*[contains(@id,'edit-pop')]");

            final String editServerLinkId = editServerLinkLocator.getNode().getAttributes().getNamedItem("id").
                    getNodeValue();

            tester.clickLink(editServerLinkId);

            return new DefaultEditPopServerConfiguration(mailServerAdministration, tester);
        }

        @Override
        public MailServerAdministration delete(String mailServerName)
        {
            final Node mailServerRow = discoverRowInServersTableFor(mailServerName);

            final Locator deleteServerLinkLocator = new XPathLocator(mailServerRow, "//*[contains(@id,'delete-pop')]");

            final String deleteServerLinkId = deleteServerLinkLocator.getNode().getAttributes().getNamedItem("id").
                    getNodeValue();

            tester.clickLink(deleteServerLinkId);
            tester.submit("Delete");
            return mailServerAdministration;
        }

        private Node discoverRowInServersTableFor(String mailServerName)
        {
            final XPathLocator serverRowsLocator = locator.xpath("//*[@id='pop-mail-servers-table']//*[@class='mail-server-name']");

            Node serverRowNodeToFind = null;

            for (Node serverRow : serverRowsLocator.getNodes())
            {
                if (serverRowsLocator.getText(serverRow).equals(mailServerName))
                {
                    serverRowNodeToFind = serverRow.getParentNode().getParentNode();
                    break;
                }
            }

            if (serverRowNodeToFind == null)
            {
                throw new CouldNotFindPopMailServerException(
                        String.format
                                (
                                        "The specified POP/IMAP Server could not be found on the page. Server Name: %s",
                                        mailServerName
                                )
                );
            }

            return serverRowNodeToFind;
        }

        private boolean isEmpty()
        {
            return locator.css("#pop-mail-servers-table tr td").getText().equals("You do not currently have any POP / IMAP servers configured.");
        }

        private boolean isMailServerTableHeader(Node mailServerRow)
        {
            return new CssLocator(mailServerRow, "th").exists();
        }

        private String extractServerName(final Node mailServerRow)
        {
            return new CssLocator(mailServerRow, "td .mail-server-name").getText();
        }

        private String extractHost(final Node mailServerRow)
        {
            return new CssLocator(mailServerRow, "td .mail-server-host").getText();
        }

        private String extractUserName(final Node mailServerRow)
        {
            return new CssLocator(mailServerRow, "td .mail-server-username").getText();
        }

        private class CouldNotFindPopMailServerException extends RuntimeException
        {
            public CouldNotFindPopMailServerException(String message)
            {
                super(message);
            }
        }
    }

    public class DefaultSendTestEmail implements SendTestEmail
    {
        private final LocatorFactory locator;
        private WebTester tester;

        public DefaultSendTestEmail(final LocatorFactory locator, final WebTester tester)
        {
            this.locator = locator;
            this.tester = tester;
        }

        @Override
        public void send()
        {
            tester.setWorkingForm("jiraform");
            tester.clickButton("send_submit");
        }

        @Override
        public void assertMessageSentInformationDisplayed()
        {
            tester.assertTextInTable("mail-log-table", "Your test message has been sent");
        }

    }
}
