package com.atlassian.jira.webtests.ztests.email;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.JIRAWebTest;
import org.apache.commons.lang.StringUtils;

@WebTest ({ Category.FUNC_TEST, Category.BROWSING, Category.EMAIL })
// passing as well, however this test is flaky as it sometimes fails depending on test execution order and it has ugly
// assertions on the raw html output. This should be rewritten.
public class TestMailServer extends JIRAWebTest
{
    protected static final String FIELD_NAME        = "name";
    protected static final String FIELD_FROM        = "from";
    protected static final String FIELD_PREFIX      = "prefix";
    protected static final String FIELD_SERVER_NAME = "serverName";
    protected static final String FIELD_USERNAME    = "username";
    protected static final String FIELD_PASSWORD    = "password";
    private static final String FIELD_PORT        = "port";

    protected static final String VALUE_NAME_SMTP   = "name SMTP";
    protected static final String VALUE_NAME_POP    = "name POP";
    protected static final String VALUE_FROM        = "from@atlassian.com";
    protected static final String VALUE_PREFIX      = "prefix";
    protected static final String VALUE_SERVER_NAME = "server name";
    protected static final String VALUE_USERNAME    = "username";
    protected static final String VALUE_PASSWORD    = "password";

    protected static final String TITLE_SMTP_MAIL_SERVERS    = "<h3 class=\"formtitle\">SMTP Mail Server</h3>";
    protected static final String TITLE_ADD_SMTP_MAIL_SERVER = "<h3 class=\"formtitle\">Add SMTP Mail Server</h3>";
    protected static final String TITLE_POP_MAIL_SERVERS     = "<h3>POP / IMAP Mail Servers</h3>";
    protected static final String TITLE_ADD_POP_MAIL_SERVER  = "<h3 class=\"formtitle\">Add POP / IMAP Mail Server</h3>";
    protected static final String TITLE_DELETE_MAIL_SERVER   = "<h3 class=\"formtitle\">Delete Mail Server</h3>";
    protected static final String TITLE_SUPPORT_REQUEST      = "<h3 class=\"formtitle\">Support Request</h3>";
    protected static final String TITLE_SEND_EMAIL           = "<h3 class=\"formtitle\">Send Email</h3>";
    protected static final String TITLE_MAIL_QUEUE           = "<h3 class=\"formtitle\">Mail Queue</h3>";

    protected static final String LINK_TEXT_CONFIG_NEW_SMTP = "Configure new SMTP mail server";
    protected static final String LINK_TEXT_CONFIG_NEW_POP  = "Add POP / IMAP mail server";

    protected static final String LINK_DELETE_SMTP = "deleteSMTP";

    protected static final String LINK_PAGE_MAIL_INCOMING     = "incoming_mail";
    protected static final String LINK_PAGE_MAIL_OUTGOING     = "outgoing_mail";
    protected static final String LINK_PAGE_SEND_EMAIL      = "send_email";
    protected static final String LINK_PAGE_MAIL_QUEUE      = "mail_queue";

    protected static final String LABEL_NO_SMTP_MAIL_SERVER     = "You do not currently have an SMTP server configured.";
    protected static final String LABEL_NO_SMTP_SUPPORT_REQUEST_PART_ONE = "<span class=\"note\">Note</span>: To send a support request you need to";
    protected static final String LABEL_NO_SMTP_SUPPORT_REQUEST_PART_TWO = "configure</a> a mail server.";
    protected static final String LABEL_NO_SMTP_SEND_EMAIL_PART_ONE      = "To send email you need to";
    protected static final String LABEL_NO_SMTP_SEND_EMAIL_PART_TWO      = "configure</a> a mail server.";
    protected static final String LABEL_NO_SMTP_MAIL_QUEUE_PART_ONE      = "There is no default ";
    protected static final String LABEL_NO_SMTP_MAIL_QUEUE_PART_TWO      = "mail server</a>, so mails will not be sent.";
    protected static final String LABEL_NO_POP_MAIL_SERVER      = "You do not currently have any POP / IMAP servers configured.";

    protected static final String BUTTON_CANCEL = "cancelButton";
    protected static final String BUTTON_ADD    = "Add";
    protected static final String BUTTON_DELETE = "Delete";
    private static final String BUTTON_UPDATE = "Update";

    protected static final String ERROR_SPECIFY_SERVER          = "You must specify the name of this Mail Server.";
    protected static final String ERROR_SPECIFY_ADDRESS         = "You must specify a valid from address";
    protected static final String ERROR_SPECIFY_PREFIX          = "You must specify an email prefix";
    protected static final String ERROR_SPECIFY_SERVER_DETAILS  = "You must specify a host name or a JNDI location.";
    protected static final String ERROR_SPECIFY_SERVER_LOCATION = "You must specify the location of the server.";
    protected static final String ERROR_SPECIFY_USERNAME        = "You must specify a username";
    protected static final String ERROR_SPECIFY_PASSWORD        = "You must specify a password";
    private static final String ERROR_ILLEGAL_PORT              = "SMTP port must be a number between 0 and 65535";

    public TestMailServer(String name)
    {
        super(name);
    }

    public void testPOPServerConfiguration()
    {
        getBackdoor().restoreBlankInstance();
        configureNewPOPServer();
        checkPOPServerExists();
        deletePOPServer();
    }

    public void testSMTPServerConfiguration()
    {
        getBackdoor().restoreBlankInstance();
        validateSMTPForm();
        configureNewSMTPServer();
        checkSMTPServerExists();
        deleteSMTPServer();
        checkNoSMTPServer();
    }

    /**
     * Check 'Add SMTP Mail Server' form validation<br>
     * Check cancel button
     */
    private void validateSMTPForm()
    {
        log("Mail Server - Validating empty form for new SMTP mail server setup");
        navigation.gotoAdminSection(LINK_PAGE_MAIL_OUTGOING);
        assertTextPresent(TITLE_SMTP_MAIL_SERVERS);

        // try to meet the assumptions of this test - no existing mail servers
        if (tester.getDialog().isLinkPresent(LINK_DELETE_SMTP)) {
            tester.clickLink(LINK_DELETE_SMTP);
            assertTextPresent(TITLE_DELETE_MAIL_SERVER);
            assertTextPresent("Are you sure you want to delete");
            tester.submit(BUTTON_DELETE);
            assertTextPresent(LABEL_NO_SMTP_MAIL_SERVER);
        }
        tester.clickLinkWithText(LINK_TEXT_CONFIG_NEW_SMTP);
        assertTextPresent(TITLE_ADD_SMTP_MAIL_SERVER);
        tester.submit(BUTTON_ADD);
        assertTextPresent(ERROR_SPECIFY_SERVER);
        assertTextPresent(ERROR_SPECIFY_ADDRESS);
        assertTextPresent(ERROR_SPECIFY_PREFIX);

        tester.setFormElement(FIELD_NAME, VALUE_NAME_SMTP);
        tester.setFormElement(FIELD_FROM, VALUE_FROM);
        tester.setFormElement(FIELD_PREFIX, VALUE_PREFIX);
        tester.submit(BUTTON_ADD);
        assertTextPresent(ERROR_SPECIFY_SERVER_DETAILS);

        tester.setFormElement(FIELD_SERVER_NAME, VALUE_SERVER_NAME);
        tester.setFormElement(FIELD_USERNAME, VALUE_USERNAME);
        tester.setFormElement(FIELD_PASSWORD, VALUE_PASSWORD);
        tester.setFormElement(FIELD_PORT, String.valueOf(0xFFFF + 1));
        tester.submit(BUTTON_ADD);
        assertTextPresent(ERROR_ILLEGAL_PORT);

        tester.submit(BUTTON_ADD);
        assertTextPresent(ERROR_ILLEGAL_PORT);

        tester.setFormElement(FIELD_PORT, String.valueOf(-1));
        tester.submit(BUTTON_ADD);
        assertTextPresent(ERROR_ILLEGAL_PORT);
    }

    /**
     * Add a new SMTP mail server<br>
     * Check that it has been added and correct details displayed<br>
     * Check no more SMTP servers can be added
     */
    private void configureNewSMTPServer()
    {
        log("Mail Server - Configuring a new SMTP mail server");
        navigation.gotoAdminSection(LINK_PAGE_MAIL_OUTGOING);
        assertTextPresent(TITLE_SMTP_MAIL_SERVERS);

        tester.clickLinkWithText(LINK_TEXT_CONFIG_NEW_SMTP);
        assertTextPresent(TITLE_ADD_SMTP_MAIL_SERVER);

        tester.setFormElement(FIELD_NAME, VALUE_NAME_SMTP);
        tester.setFormElement(FIELD_FROM, VALUE_FROM);
        tester.setFormElement(FIELD_PREFIX, VALUE_PREFIX);
        tester.setFormElement(FIELD_SERVER_NAME, VALUE_SERVER_NAME);
        tester.setFormElement(FIELD_USERNAME, VALUE_USERNAME);
        tester.setFormElement(FIELD_PASSWORD, VALUE_PASSWORD);
        tester.setFormElement(FIELD_PORT, String.valueOf(0xFFFF));
        tester.submit(BUTTON_ADD);
        assertTextPresent(TITLE_SMTP_MAIL_SERVERS);
        assertTextPresent(VALUE_NAME_SMTP);
        assertTextSequence(new String[]
                {"From:", VALUE_FROM,
                        "Prefix:", VALUE_PREFIX,
                        "Host:", VALUE_SERVER_NAME,
                        "SMTP Port:", String.valueOf(0xFFFF),
                        "Username:", VALUE_USERNAME});
        tester.assertLinkNotPresentWithText(LINK_TEXT_CONFIG_NEW_SMTP);

        // sometimes the id is different than expected edit_10000, harvest it first
        final String deleteLink = tester.getDialog().getElement(LINK_DELETE_SMTP).getAttribute("href");
        final String editId = "edit_" + StringUtils.substringAfter(deleteLink, "id=");
        tester.clickLink(editId);
        tester.setFormElement(FIELD_PORT, String.valueOf(0));
        tester.setFormElement(FIELD_PASSWORD, VALUE_PASSWORD);
        tester.submit(BUTTON_UPDATE);
        assertTextSequence(new String[] { "SMTP Port:", "0" });
    }

    /**
     * Assumptions: SMTP Mail Server set<br>
     * Checks that SMTP server exists<br>
     * Checks that the following pages have NO message about no SMTP mail server set<br>
     * <li>Support Request
     * <li>Send Email
     * <li>Mail Queue
     */
    private void checkSMTPServerExists()
    {
        log("Mail Server - Checking labels when a SMTP server is setup");
        navigation.gotoAdminSection(LINK_PAGE_MAIL_OUTGOING);
        assertTextNotPresent(LABEL_NO_SMTP_MAIL_SERVER);
        assertLinkNotPresentWithText(LINK_TEXT_CONFIG_NEW_SMTP);

        navigation.gotoAdminSection(LINK_PAGE_SEND_EMAIL);
        assertTextPresent(TITLE_SEND_EMAIL);
        assertTextNotPresent(LABEL_NO_SMTP_SEND_EMAIL_PART_ONE);
        assertTextNotPresent(LABEL_NO_SMTP_SEND_EMAIL_PART_TWO);

        navigation.gotoAdminSection(LINK_PAGE_MAIL_QUEUE);
        assertTextPresent(TITLE_MAIL_QUEUE);
        assertTextNotPresent(LABEL_NO_SMTP_MAIL_QUEUE_PART_ONE);
        assertTextNotPresent(LABEL_NO_SMTP_MAIL_QUEUE_PART_TWO);
    }

    /**
     * Deletes the SMTP Server<br>
     * Checks if a new SMTP server can be configured
     */
    private void deleteSMTPServer()
    {
        log("Mail Server - Deleting the SMTP server");
        navigation.gotoAdminSection(LINK_PAGE_MAIL_OUTGOING);
        assertTextPresent(TITLE_SMTP_MAIL_SERVERS);

        tester.clickLink(LINK_DELETE_SMTP);
        assertTextPresent(TITLE_DELETE_MAIL_SERVER);
        assertTextPresent("Are you sure you want to delete <b>" + VALUE_NAME_SMTP + "</b>?");
        tester.submit(BUTTON_DELETE);
        assertTextPresent(LABEL_NO_SMTP_MAIL_SERVER);
        assertLinkPresentWithText(LINK_TEXT_CONFIG_NEW_SMTP);
    }

    /**
     * Assumptions: NO SMTP Mail Server set<br>
     * Checks that SMTP server NOT exists<br>
     * Checks that the following pages have a message about no SMTP mail server set<br>
     * <li>Support Request
     * <li>Send Email
     * <li>Mail Queue
     */
    private void checkNoSMTPServer()
    {
        log("Mail Server - Checking labels when a SMTP server is NOT setup");
        navigation.gotoAdminSection(LINK_PAGE_MAIL_OUTGOING);
        assertTextPresent(LABEL_NO_SMTP_MAIL_SERVER);
        assertLinkPresentWithText(LINK_TEXT_CONFIG_NEW_SMTP);

        navigation.gotoAdminSection(LINK_PAGE_SEND_EMAIL);
        assertTextPresent(TITLE_SEND_EMAIL);
        assertTextPresent(LABEL_NO_SMTP_SEND_EMAIL_PART_ONE);
        assertTextPresent(LABEL_NO_SMTP_SEND_EMAIL_PART_TWO);

        navigation.gotoAdminSection(LINK_PAGE_MAIL_QUEUE);
        assertTextPresent(TITLE_MAIL_QUEUE);
        assertTextPresent(LABEL_NO_SMTP_MAIL_QUEUE_PART_ONE);
        assertTextPresent(LABEL_NO_SMTP_MAIL_QUEUE_PART_TWO);
    }

    /**
     * Check 'Add POP / IMAP Mail Server' form validation<br>
     * Check cancel button
     */
    public void testValidatePOPForm()
    {
        log("Mail Server - Validayting empty form for new POP mail server setup");
        getBackdoor().restoreBlankInstance();
        navigation.gotoAdminSection(LINK_PAGE_MAIL_INCOMING);
        assertTextPresent(TITLE_POP_MAIL_SERVERS);

        tester.clickLinkWithText(LINK_TEXT_CONFIG_NEW_POP);
        assertTextPresent(TITLE_ADD_POP_MAIL_SERVER);
        tester.submit(BUTTON_ADD);
        assertTextPresent(ERROR_SPECIFY_SERVER);
        assertTextPresent(ERROR_SPECIFY_SERVER_LOCATION);
        assertTextPresent(ERROR_SPECIFY_USERNAME);
        assertTextPresent(ERROR_SPECIFY_PASSWORD);
        tester.setFormElement(FIELD_NAME, VALUE_NAME_POP);
        tester.setFormElement(FIELD_SERVER_NAME, VALUE_SERVER_NAME);
        tester.setFormElement(FIELD_USERNAME, VALUE_USERNAME);
        tester.setFormElement(FIELD_PASSWORD, VALUE_PASSWORD);
        tester.clickLink(BUTTON_CANCEL);
        //same propblem as testVakudateSMTPForm
//        assertTextPresent(TITLE_POP_MAIL_SERVERS);
    }

    /**
     * Add a new POP mail server<br>
     * Check that it has been added and correct details displayed<br>
     * Check more POP servers can be added
     */
    private void configureNewPOPServer()
    {
        restoreBlankInstance();
        log("Mail Server - Configuring a new POP mail server");
        navigation.gotoAdminSection(LINK_PAGE_MAIL_INCOMING);
        assertTextPresent(TITLE_POP_MAIL_SERVERS);

        configurePopServer(VALUE_NAME_POP, VALUE_SERVER_NAME, "110", VALUE_USERNAME, VALUE_PASSWORD);       
        assertTextPresent(TITLE_POP_MAIL_SERVERS);
        assertTextPresent(VALUE_NAME_POP);
        text.assertTextPresent(locator.page(), "Host: " + VALUE_SERVER_NAME);
        text.assertTextPresent(locator.page(), "Username: " + VALUE_USERNAME);
        tester.clickLinkWithText(LINK_TEXT_CONFIG_NEW_POP);
    }

    /**
     * Assumptions: POP Mail Server set<br>
     * Checks that POP server does not exist<br>
     */
    private void checkPOPServerExists()
    {
        log("Mail Server - Checking labels when a POP server is setup");
        navigation.gotoAdminSection(LINK_PAGE_MAIL_INCOMING);
        assertTextNotPresent(LABEL_NO_POP_MAIL_SERVER);
    }

    /**
     * Deletes the POP Server with the VALUE_NAME_POP<br>
     * Checks if a new POP server can be configured
     */
    private void deletePOPServer()
    {
        log("Mail Server - Deleting the POP server");
        navigation.gotoAdminSection(LINK_PAGE_MAIL_INCOMING);
        assertTextPresent(TITLE_POP_MAIL_SERVERS);

        tester.clickLink("delete-pop-10000");
        assertTextPresent(TITLE_DELETE_MAIL_SERVER);
        assertTextPresent("Are you sure you want to delete <b>" + VALUE_NAME_POP + "</b>?");
        tester.submit(BUTTON_DELETE);
        assertTextPresent(LABEL_NO_POP_MAIL_SERVER);
        tester.assertLinkPresentWithText(LINK_TEXT_CONFIG_NEW_POP);
    }

    /**
     * Assumptions: NO POP Mail Server set<br>
     * Checks that POP server NOT exists<br>
     */
    public void testNoPOPServer()
    {
        getBackdoor().restoreBlankInstance();
        log("Mail Server - Checking labels when a POP server is NOT setup");
        navigation.gotoAdminSection(LINK_PAGE_MAIL_INCOMING);
        assertTextPresent(LABEL_NO_POP_MAIL_SERVER);
        tester.assertLinkPresentWithText(LINK_TEXT_CONFIG_NEW_POP);
    }

    public void testPortSetOnUpdatePopServer()
    {
        getBackdoor().restoreBlankInstance();
        navigation.gotoAdminSection(LINK_PAGE_MAIL_INCOMING);
        configurePopServer(VALUE_NAME_POP, VALUE_SERVER_NAME, "222", VALUE_USERNAME, VALUE_PASSWORD);
        tester.clickLink("edit-pop-10000");
        assertFormElementEquals("port", "222");
    }

    private void configurePopServer(String name, String hostName, String port, String username, String password)
    {
        tester.clickLinkWithText(LINK_TEXT_CONFIG_NEW_POP);
        assertTextPresent(TITLE_ADD_POP_MAIL_SERVER);
        tester.setFormElement(FIELD_NAME, name);
        tester.setFormElement(FIELD_SERVER_NAME, hostName);
        tester.setFormElement(FIELD_PORT, port);
        tester.setFormElement(FIELD_USERNAME, username);
        tester.setFormElement(FIELD_PASSWORD, password);
        tester.submit(BUTTON_ADD);
    }
}
