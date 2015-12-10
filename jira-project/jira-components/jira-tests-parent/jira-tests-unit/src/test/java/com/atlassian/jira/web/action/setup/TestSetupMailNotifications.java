package com.atlassian.jira.web.action.setup;

import com.atlassian.crowd.embedded.api.CrowdService;
import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.event.ListenerManager;
import com.atlassian.jira.event.listeners.mail.MailListener;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.mock.ofbiz.MockOfBizDelegator;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.plugin.webresource.JiraWebResourceManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.MockCrowdService;
import com.atlassian.jira.user.MockGroup;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.util.FileFactory;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.web.HttpServletVariables;
import com.atlassian.mail.server.MailServer;
import com.atlassian.mail.server.MailServerManager;
import com.atlassian.mail.server.SMTPMailServer;
import com.atlassian.plugin.webresource.WebResourceManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.Collection;
import java.util.Map;
import javax.servlet.http.HttpSession;

import static com.atlassian.jira.config.properties.APKeys.JIRA_SETUP;
import static com.atlassian.jira.web.action.setup.AbstractSetupAction.DEFAULT_GROUP_ADMINS;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static webwork.action.Action.INPUT;
import static webwork.action.Action.SUCCESS;

/**
 * Unit test of {@link SetupMailNotifications}.
 *
 * @since 6.2
 */
public class TestSetupMailNotifications
{
    private static final String CUSTOM_DESC = "Server Description";
    private static final String CUSTOM_FROM = "defaultfrom@atlassian.com";
    private static final String CUSTOM_PASSWORD = "Password";
    private static final String CUSTOM_SERVER_NAME = "mail.atlassian.com";
    private static final String CUSTOM_USER_NAME = "Username";
    private static final String ERROR_MESSAGE = "Some error!";

    private static <E> void assertSingleElementCollection(final Collection<E> actual, final E expected) {
        assertEquals(1, actual.size());
        assertEquals(expected, actual.iterator().next());
    }

    private SetupMailNotifications setupMailNotifications;
    @Mock private FileFactory mockFileFactory;
    @Mock private I18nHelper mockI18nHelper;
    @Mock private ListenerManager mockListenerManager;
    @Mock private MailServerManager mockMailServerManager;
    @Mock private UserUtil mockUserUtil;
    @Mock private HttpServletVariables servletVariables;

    @Before
    @SuppressWarnings("deprecation")
    public void setUp() throws Exception
    {
        MockitoAnnotations.initMocks(this);
        final JiraAuthenticationContext mockJiraAuthenticationContext = mock(JiraAuthenticationContext.class);
        when(mockJiraAuthenticationContext.getI18nHelper()).thenReturn(mockI18nHelper);
        new MockComponentWorker().init()
                .addMock(CrowdService.class, new MockCrowdService())
                .addMock(JiraAuthenticationContext.class, mockJiraAuthenticationContext)
                .addMock(ListenerManager.class, mockListenerManager)
                .addMock(OfBizDelegator.class, new MockOfBizDelegator())
                .addMock(WebResourceManager.class, mock(WebResourceManager.class));
        setupMailNotifications = new SetupMailNotifications(mockUserUtil, mockFileFactory, mock(JiraWebResourceManager.class), servletVariables) {
            @Override
            MailServerManager getMailServerManager()
            {
                // Because in production, it's looked up statically.
                return mockMailServerManager;
            }
        };
    }

    @After
    public void tearDownWorker()
    {
        ComponentAccessor.initialiseWorker(null);
    }

    @Test
    public void testGetSets()
    {
        assertNull(setupMailNotifications.getName());
        assertNull(setupMailNotifications.getDesc());
        assertNull(setupMailNotifications.getFrom());
        assertNull(setupMailNotifications.getJndiLocation());
        assertNull(setupMailNotifications.getServerName());
        assertNull(setupMailNotifications.getUsername());
        assertNull(setupMailNotifications.getPassword());
        assertNull(setupMailNotifications.getProtocol());

        final String tmpServerName = "Server Name";
        setupMailNotifications.setName(tmpServerName);
        assertEquals(tmpServerName, setupMailNotifications.getName());

        final String tmpServerDescription = "Server Description";
        setupMailNotifications.setDesc(tmpServerDescription);
        assertEquals(tmpServerDescription, setupMailNotifications.getDesc());

        final String tmpEmail = "test@atlassian.com";
        setupMailNotifications.setFrom(tmpEmail);
        assertEquals(tmpEmail, setupMailNotifications.getFrom());

        final String tmpServer = "Server";
        setupMailNotifications.setServerName(tmpServer);
        assertEquals(tmpServer, setupMailNotifications.getServerName());

        final String tmpSession = "Session";
        setupMailNotifications.setJndiLocation(tmpSession);
        assertEquals(tmpSession, setupMailNotifications.getJndiLocation());

        final String tmpUsername = "Username";
        setupMailNotifications.setUsername(tmpUsername);
        assertEquals(tmpUsername, setupMailNotifications.getUsername());

        final String tmpPassword = "Password";
        setupMailNotifications.setPassword(tmpPassword);
        assertEquals(tmpPassword, setupMailNotifications.getPassword());

        final String tmpSmtp = "smtp";
        setupMailNotifications.setProtocol(tmpSmtp);
        assertEquals(tmpSmtp, setupMailNotifications.getProtocol());
    }

    @Test
    public void testDoDefaultSetupAlready() throws Exception
    {
        setupMailNotifications.getApplicationProperties().setString(JIRA_SETUP, "true");
        assertEquals("setupalready", setupMailNotifications.doDefault());
    }

    @Test
    public void testDoDefaultValues() throws Exception
    {
        // Set up
        final String tmpEmail = "test@atlassian.com";
        final User user = createMockUser("test user", "test user fullname", tmpEmail);
        final Group group = createMockGroup(DEFAULT_GROUP_ADMINS);
        addUserToGroup(user, group);
        setupMailNotifications.getApplicationProperties().setString(JIRA_SETUP, null);
        when(mockUserUtil.getJiraAdministrators()).thenReturn(singletonList(user));


        HttpSession session = Mockito.mock(HttpSession.class);
        when(servletVariables.getHttpSession()).thenReturn(session);
        when(session.getAttribute(SetupSharedVariables.SETUP_CHOOSEN_BUNDLE)).thenReturn("any");

        // Invoke
        final String result = setupMailNotifications.doDefault();

        // Check
        assertEquals(INPUT, result);
        assertEquals("Default SMTP Server", setupMailNotifications.getName());
        assertEquals("This server is used for all outgoing mail.", setupMailNotifications.getDesc());
        assertEquals(tmpEmail, setupMailNotifications.getFrom());
    }

    private void addUserToGroup(final User user, final Group group) throws Exception
    {
        ComponentAccessor.getCrowdService().addUserToGroup(user, group);
    }

    private Group createMockGroup(final String groupName) throws Exception
    {
        final Group group = new MockGroup(groupName);
        final CrowdService crowdService = ComponentAccessor.getCrowdService();
        crowdService.addGroup(group);
        return group;
    }

    private User createMockUser(final String userName, final String name, final String email) throws Exception
    {
        final User user = new MockUser(userName, name, email);
        ComponentAccessor.getCrowdService().addUser(user, "password");
        return user;
    }

    @Test
    public void testDoValidationName() throws Exception
    {
        // Set up
        setAllValidData();
        setupMailNotifications.setName(null);
        setUpErrorMessage("setup3.error.servername.required");

        // Invoke and check
        checkSingleError("name");
    }

    private void setUpErrorMessage(final String messageCode)
    {
        when(mockI18nHelper.getText(messageCode)).thenReturn(ERROR_MESSAGE);
    }

    @Test
    public void testDoValidationFromWhenFromIsNull() throws Exception
    {
        // Set up
        setAllValidData();
        setupMailNotifications.setFrom(null);
        setUpErrorMessage("setup3.error.validemail");

        // Invoke and check
        checkSingleError("from");
    }

    @Test
    public void testDoValidationFromWhenFromIsInvalid() throws Exception
    {
        // Set up
        setAllValidData();
        setupMailNotifications.setFrom("abc");
        setUpErrorMessage("setup3.error.validemail");

        // Invoke and check
        checkSingleError("from");
    }

    @Test
    public void testDoValidationNoServer() throws Exception
    {
        // Set up
        setAllValidData();
        setupMailNotifications.setServerName(null);
        setupMailNotifications.setJndiLocation(null);
        setUpErrorMessage("setup3.error.required");

        // Invoke and check
        assertEquals(INPUT, setupMailNotifications.execute());
        assertSingleElementCollection(setupMailNotifications.getErrorMessages(), ERROR_MESSAGE);
    }

    @Test
    public void testDoValidationBothServerSession() throws Exception
    {
        // Set up
        setAllValidData();
        setupMailNotifications.setJndiLocation("test session");
        setUpErrorMessage("setup3.error.onlysetup");

        // Invoke and check
        assertEquals(INPUT, setupMailNotifications.execute());
        assertSingleElementCollection(setupMailNotifications.getErrorMessages(), ERROR_MESSAGE);
    }

    @Test
    public void testDoValidationServer() throws Exception
    {
        setAllValidData();
        setupMailNotifications.setServerName("a");
        setUpErrorMessage("setup3.error.tooshort");

        assertEquals(INPUT, setupMailNotifications.execute());
        checkSingleError("serverName");
    }

    @Test
    public void testDoExecuteSuccessNoEmail() throws Exception
    {
        setupMailNotifications.setNoemail(true);
        assertEquals(SUCCESS, setupMailNotifications.execute());
    }

    @Test
    public void testDoExecuteSuccessServer() throws Exception
    {
        // Set up
        setAllValidData();

        // Invoke
        final String result = setupMailNotifications.execute();

        // Check
        assertEquals(SUCCESS, result);
        verify(mockListenerManager).createListener("Email Listener", MailListener.class);
        final ArgumentCaptor<MailServer> mailServerCaptor = ArgumentCaptor.forClass(MailServer.class);
        verify(mockMailServerManager).create(mailServerCaptor.capture());
        final MailServer mailServer = mailServerCaptor.getValue();
        assertEquals("Server Name", mailServer.getName());
        assertEquals(CUSTOM_DESC, mailServer.getDescription());
        assertEquals(CUSTOM_FROM, ((SMTPMailServer) mailServer).getDefaultFrom());
        assertEquals(CUSTOM_SERVER_NAME, mailServer.getHostname());
        assertEquals(CUSTOM_USER_NAME, mailServer.getUsername());
        assertEquals(CUSTOM_PASSWORD, mailServer.getPassword());
    }

    @Test
    public void testDoExecuteFailSetupAlready() throws Exception
    {
        setupMailNotifications.getApplicationProperties().setString(JIRA_SETUP, "true");
        assertEquals("setupalready", setupMailNotifications.execute());
    }

    private void checkSingleError(final String key) throws Exception
    {
        assertEquals(INPUT, setupMailNotifications.execute());
        final Map<String,String> errors = setupMailNotifications.getErrors();
        assertEquals(1, errors.size());
        assertEquals(ERROR_MESSAGE, errors.get(key));
    }

    private void setAllValidData()
    {
        final String customName = "Server Name";
        setupMailNotifications.setName(customName);
        setupMailNotifications.setDesc(CUSTOM_DESC);
        setupMailNotifications.setFrom(CUSTOM_FROM);
        setupMailNotifications.setServerName(CUSTOM_SERVER_NAME);
        setupMailNotifications.setUsername(CUSTOM_USER_NAME);
        setupMailNotifications.setPassword(CUSTOM_PASSWORD);
        final String customProtocol = "smtp";
        setupMailNotifications.setProtocol(customProtocol);
    }
}
