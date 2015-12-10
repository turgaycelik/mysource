package com.atlassian.jira.web.action.setup;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.util.AttachmentPathManager;
import com.atlassian.jira.config.util.IndexPathManager;
import com.atlassian.jira.config.util.JiraHome;
import com.atlassian.jira.config.util.MockAttachmentPathManager;
import com.atlassian.jira.issue.index.IssueIndexManager;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.plugin.webresource.JiraWebResourceManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.service.ServiceManager;
import com.atlassian.jira.task.context.Context;
import com.atlassian.jira.util.BuildUtilsInfo;
import com.atlassian.jira.util.FileFactory;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.index.Contexts;
import com.atlassian.jira.util.system.JiraSystemRestarter;
import com.atlassian.jira.web.util.ExternalLinkUtil;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import webwork.action.Action;
import webwork.action.ServletActionContext;

import static com.atlassian.jira.config.properties.APKeys.JIRA_BASEURL;
import static com.atlassian.jira.config.properties.APKeys.JIRA_MODE;
import static com.atlassian.jira.config.properties.APKeys.JIRA_SETUP;
import static com.atlassian.jira.config.properties.APKeys.JIRA_TITLE;
import static com.atlassian.jira.config.properties.APKeys.JIRA_WEBWORK_ENCODING;
import static com.atlassian.jira.config.util.AttachmentPathManager.Mode.DEFAULT;
import static com.atlassian.jira.config.util.MockAttachmentPathManager.DEFAULT_PATH;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static webwork.action.Action.INPUT;

@RunWith (MockitoJUnitRunner.class)
public class TestSetupApplicationProperties
{
    @Rule public final RuleChain ruleChain = MockitoMocksInContainer.forTest(this);

    private static final String scheme = "scheme";
    private static final String serverName = "server";
    private static final int serverPort = 1;
    private static final String contextPath = "contextPath";
    private static final String errorSpecificTitle = "You must specify a title.";
    private static final String errorBaseUrl = "The URL specified is not valid.";
    private static final String errorMode = "Invalid mode specified.";

    private AttachmentPathManager mockAttachmentPathManager;
    private SetupApplicationProperties setupApplicationProperties;
    @Mock private IssueIndexManager mockIssueIndexManager;
    @Mock private ServiceManager mockServiceManager;
    @Mock private IndexPathManager mockIndexPathManager;
    @Mock private JiraHome mockJiraHome;
    @Mock private BuildUtilsInfo mockBuildUtilsInfo;
    @Mock private JiraSystemRestarter mockJiraSystemRestarter;
    @Mock private FileFactory mockFileFactory;
    @Mock private ExternalLinkUtil mockEternalLinkUtil;
    @Mock private JiraWebResourceManager mockWebResourceManager;
    @Mock private EventPublisher mockEventPublisher;
    @AvailableInContainer @Mock private I18nHelper mockI18nHelper;
    @AvailableInContainer @Mock private JiraAuthenticationContext mockJiraAuthenticationContext;

    @Before
    public void setUp() throws Exception
    {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getScheme()).thenReturn(scheme);
        when(request.getServerName()).thenReturn(serverName);
        when(request.getServerPort()).thenReturn(serverPort);
        when(request.getContextPath()).thenReturn(contextPath);
        ServletActionContext.setRequest(request);
        final HttpServletResponse response = mock(HttpServletResponse.class);
        ServletActionContext.setResponse(response);

        final HttpSession session = mock(HttpSession.class);
        when(request.getSession()).thenReturn(session);
        mockAttachmentPathManager = new MockAttachmentPathManager();
        ComponentAccessor.getApplicationProperties().setString(JIRA_WEBWORK_ENCODING, "UTF-8");
        when(mockJiraAuthenticationContext.getI18nHelper()).thenReturn(mockI18nHelper);
        when(mockI18nHelper.getText("setup.error.specifytitle")).thenReturn(errorSpecificTitle);
        when(mockI18nHelper.getText("setup.error.baseURL")).thenReturn(errorBaseUrl);
        when(mockI18nHelper.getText("setup.error.mode")).thenReturn(errorMode);

        when(mockIssueIndexManager.isIndexingEnabled()).thenReturn(true);
        setupApplicationProperties = new SetupApplicationProperties(
                mockIssueIndexManager, mockServiceManager, mockIndexPathManager, mockAttachmentPathManager,
                mockJiraHome, mockBuildUtilsInfo, mockJiraSystemRestarter, mockFileFactory, mockEternalLinkUtil,
                mockWebResourceManager, mockEventPublisher);

        setupApplicationProperties.setNextStep("true");
    }

    @After
    public void tearDown()
    {
        ServletActionContext.setRequest(null);
        ServletActionContext.setResponse(null);
    }

    @Test
    public void testGetSets()
    {
        assertNull(setupApplicationProperties.getAttachmentPath());
        assertNull(setupApplicationProperties.getIndexPath());
        assertNull(setupApplicationProperties.getTitle());
        assertEquals("public", setupApplicationProperties.getMode());

        // the mode
        final String tmpMode = "private";
        setupApplicationProperties.setMode(tmpMode);
        assertEquals(tmpMode, setupApplicationProperties.getMode());

        // base URL
        assertEquals(scheme + "://" + serverName + ":" + serverPort + contextPath, setupApplicationProperties.getBaseURL());
        final String tmpBaseUrl = "http://www.google.com";
        setupApplicationProperties.setBaseURL(tmpBaseUrl);
        assertEquals(tmpBaseUrl, setupApplicationProperties.getBaseURL());

        // titles
        final String tmpTitle = "Test";
        setupApplicationProperties.setTitle(tmpTitle);
        assertEquals(tmpTitle, setupApplicationProperties.getTitle());
    }

    @Test
    public void testDoDefaultSetupAlready() throws Exception
    {
        setupApplicationProperties.getApplicationProperties().setString(JIRA_SETUP, "false");
        assertEquals("setupalready", setupApplicationProperties.doDefault());
    }

    @Test
    public void testDoDefaultValues() throws Exception
    {
        assertEquals(INPUT, setupApplicationProperties.doDefault());
        assertEquals("Your Company JIRA", setupApplicationProperties.getTitle());
        assertEquals("public", setupApplicationProperties.getMode());
        assertEquals("UTF-8", setupApplicationProperties.getApplicationProperties().getString(JIRA_WEBWORK_ENCODING));
    }

    @Test
    public void testDoDefaultCustomValues() throws Exception
    {
        final String tmpTitle = "Test Title";
        final String tmpBaseUrl = "http://www.google.com";
        final String tmpMode = "private";
        final String tmpAttachmentPath = "c:/testAttachmentPath";
        final String tmpIndexPath = "c:/testPathIndex";
        setupApplicationProperties.getApplicationProperties().setString(JIRA_TITLE, tmpTitle);
        setupApplicationProperties.getApplicationProperties().setString(JIRA_BASEURL, tmpBaseUrl);
        setupApplicationProperties.getApplicationProperties().setString(JIRA_MODE, tmpMode);
        mockAttachmentPathManager.setCustomAttachmentPath(tmpAttachmentPath);
        when(mockIndexPathManager.getIndexRootPath()).thenReturn(tmpIndexPath);

        assertEquals(INPUT, setupApplicationProperties.doDefault());
        assertEquals(tmpTitle, setupApplicationProperties.getTitle());
        assertEquals(tmpMode, setupApplicationProperties.getMode());
        assertEquals(tmpAttachmentPath, setupApplicationProperties.getAttachmentPath());
        assertEquals(tmpIndexPath, setupApplicationProperties.getIndexPath());
        assertEquals(tmpBaseUrl, setupApplicationProperties.getBaseURL());
    }

    @Test
    public void testDoValidationAlreadySetup() throws Exception
    {
        setupApplicationProperties.getApplicationProperties().setString(JIRA_SETUP, "true");
        assertEquals("setupalready", setupApplicationProperties.execute());
    }

    @Test
    public void testdoValidationTitle() throws Exception
    {
        setAllValidData();
        setupApplicationProperties.setTitle(null);
        checkSingleError("title", errorSpecificTitle);
    }

    @Test
    public void testdoValidationURL() throws Exception
    {
        setAllValidData();
        setupApplicationProperties.setBaseURL(null);
        checkSingleError("baseURL", errorBaseUrl);
        setupApplicationProperties.setBaseURL("abc");
        checkSingleError("baseURL", errorBaseUrl);
    }

    @Test
    public void testdoValidationMode() throws Exception
    {
        setAllValidData();
        setupApplicationProperties.setMode(null);
        checkSingleError("mode", errorMode);
        setupApplicationProperties.setMode("invalid mode");
        checkSingleError("mode", errorMode);
    }

    @Test
    public void testExecuteFine() throws Exception
    {
        // Set up
        when(mockIssueIndexManager.activate(mock(Context.class))).thenReturn(0L);
        when(mockIssueIndexManager.size()).thenReturn(42);
        setAllValidData();

        // Invoke
        final String result = setupApplicationProperties.execute();

        // Check
        assertEquals(Action.NONE, result);
    }

    @Test
    public void testExecuteFineJiraHomeAttachments() throws Exception
    {
        // Set up
        when(mockIssueIndexManager.activate(mock(Context.class))).thenReturn(0L);
        when(mockIssueIndexManager.size()).thenReturn(42);
        setAllValidData(DEFAULT, IndexPathManager.Mode.DEFAULT);

        // Invoke
        final String result = setupApplicationProperties.execute();

        // Check
        assertEquals(Action.NONE, result);
        assertEquals(DEFAULT_PATH, mockAttachmentPathManager.getAttachmentPath());
        assertTrue(mockAttachmentPathManager.getUseDefaultDirectory());
    }

    @Test
    public void testExecuteFail() throws Exception
    {
        setAllValidData();
        final String message = "something went wrong";
        final String errorIndexing = "Could not activate indexing: " + message;
        when(mockIssueIndexManager.activate(Contexts.percentageLogger(mockIssueIndexManager, mock(Logger.class)))).thenThrow(new RuntimeException(message));
        when(mockIssueIndexManager.activate(any(Context.class))).thenThrow(new RuntimeException(message));
        when(mockIssueIndexManager.size()).thenReturn(42);
        when(mockI18nHelper.getText("setup.error.indexpath.activate_error", message)).thenReturn(errorIndexing);

        assertEquals(Action.ERROR, setupApplicationProperties.execute());
        assertEquals(errorIndexing, setupApplicationProperties.getErrors().get("indexPath"));
    }

    private void setAllValidData()
    {
        setAllValidData(AttachmentPathManager.Mode.CUSTOM, IndexPathManager.Mode.CUSTOM);
    }

    private void setAllValidData(final AttachmentPathManager.Mode attachments, final IndexPathManager.Mode indexes)
    {
        setupApplicationProperties.setTitle("Test Title");
        setupApplicationProperties.setBaseURL("http://www.atlassian.com");
        setupApplicationProperties.setMode("public");

        setupApplicationProperties.setAttachmentPathOption(attachments.toString());
        setupApplicationProperties.setIndexPathOption(indexes.toString());
        setupApplicationProperties.setBackupPathOption("DISABLED");
    }

    private void checkSingleError(final String element, final String error) throws Exception
    {
        assertEquals(INPUT, setupApplicationProperties.execute());
        assertEquals(error, setupApplicationProperties.getErrors().get(element));
        assertEquals(1, setupApplicationProperties.getErrors().size());
    }
}
