package com.atlassian.jira.issue.managers;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.atlassian.core.util.WebRequestUtils;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.config.util.AttachmentPathManager;
import com.atlassian.jira.config.util.MockAttachmentPathManager;
import com.atlassian.jira.issue.IssueFactory;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.attachment.Attachment;
import com.atlassian.jira.issue.attachment.AttachmentConstants;
import com.atlassian.jira.issue.attachment.AttachmentStore;
import com.atlassian.jira.issue.attachment.CreateAttachmentParamsBean;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockComponentContainer;
import com.atlassian.jira.local.runner.ListeningMockitoRunner;
import com.atlassian.jira.mock.issue.MockIssue;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.MockApplicationUser;
import com.atlassian.jira.user.UserKeyService;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.ComponentLocator;
import com.atlassian.jira.util.mime.MimeManager;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.opensymphony.module.propertyset.PropertySet;

import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith (ListeningMockitoRunner.class)
public class TestDefaultAttachmentManager
{

    @Rule
    public MockComponentContainer container = new MockComponentContainer(this);

    final GenericValue attachment2 = new MockGenericValue(AttachmentConstants.ATTACHMENT_ENTITY_NAME, EasyMap.build("id", new Long(101), "issue", new Long(1), "filename", "C:\temp"));

    @Mock
    private ApplicationProperties applicationProperties;

    @Mock
    @AvailableInContainer
    private UserKeyService userKeyService;

    @AvailableInContainer
    private AttachmentPathManager attachmentPathManager = new MockAttachmentPathManager();

    @Mock
    private OfBizDelegator ofBizDelegator;

    @Mock
    private UserManager userManager;

    @Mock
    private ComponentLocator componentLocator;

    @Mock
    private IssueFactory issueFactory;

    @Mock
    private MimeManager mimeManager;

    @Mock
    private AttachmentStore attachmentStore;

    private DefaultAttachmentManager attachmentManager;

    @Before
    public void init() throws Exception
    {
        when(componentLocator.getComponent(IssueFactory.class)).thenReturn(issueFactory);

        DefaultAttachmentManager manager = new DefaultAttachmentManager(null, ofBizDelegator, mimeManager, applicationProperties,
                attachmentPathManager, componentLocator, null, userManager, attachmentStore);
        attachmentManager = spy(manager);
        PropertySet propset = mock(PropertySet.class);
        doReturn(propset).when(attachmentManager).createAttachmentPropertySet(any(GenericValue.class), any(Map.class));
        //plug methods which perform real IO operations
        doNothing().when(attachmentManager).createAttachmentOnDiskCopySourceFile(any(Attachment.class), any(File.class));
        doNothing().when(attachmentManager).createAttachmentOnDisk(any(Attachment.class), any(File.class), any(User.class));
    }

    private void setUserOsAndLinuxScreenshotApplet(int userOS, boolean screenshotAppletLinuxEnabled)
    {
        when(applicationProperties.getOption(APKeys.JIRA_SCREENSHOTAPPLET_LINUX_ENABLED)).thenReturn(screenshotAppletLinuxEnabled);
        // WARNING: when(attachmentManger.getUsersOS()).thenReturn(userOS); is not reliable.  Read JavaDocs for spy!
        doReturn(userOS).when(attachmentManager).getUsersOS();
    }

    @Test
    public void testIsAttachmentsEnabledAndPathSetHappyPath()
    {
        when(applicationProperties.getOption(APKeys.JIRA_OPTION_ALLOWATTACHMENTS)).thenReturn(true);
        assertTrue(attachmentManager.attachmentsEnabled());
    }

    @Test
    public void testIsAttachmentsEnabledAndPathSetAttachmentsDisabled()
    {
        when(applicationProperties.getOption(APKeys.JIRA_OPTION_ALLOWATTACHMENTS)).thenReturn(false);
        assertFalse(attachmentManager.attachmentsEnabled());
    }

    @Test
    public void testIsScreenshotAppletEnabledPropertyTrue()
    {
        when(applicationProperties.getOption(APKeys.JIRA_SCREENSHOTAPPLET_ENABLED)).thenReturn(true);
        assertTrue(attachmentManager.isScreenshotAppletEnabled());
    }

    @Test
    public void testIsScreenshotAppletEnabledPropertyFalse()
    {
        when(applicationProperties.getOption(APKeys.JIRA_SCREENSHOTAPPLET_ENABLED)).thenReturn(false);
        assertFalse(attachmentManager.isScreenshotAppletEnabled());
    }

    @Test
    public void testIsScreenshotAppleSupportedByOSOSX()
    {
        setUserOsAndLinuxScreenshotApplet(WebRequestUtils.MACOSX, false);
        assertTrue(attachmentManager.isScreenshotAppletSupportedByOS());
    }

    @Test
    public void testIsScreenshotAppleSupportedByOSLinuxDisabled()
    {
        setUserOsAndLinuxScreenshotApplet(WebRequestUtils.LINUX, false);
        assertFalse(attachmentManager.isScreenshotAppletSupportedByOS());
    }

    @Test
    public void testIsScreenshotAppleSupportedByOSLinuxEnabled()
    {
        setUserOsAndLinuxScreenshotApplet(WebRequestUtils.LINUX, true);
        assertTrue(attachmentManager.isScreenshotAppletSupportedByOS());
    }

    @Test
    public void testIsScreenshotAppleSupportedByOSWindows()
    {
        setUserOsAndLinuxScreenshotApplet(WebRequestUtils.WINDOWS, false);
        assertTrue(attachmentManager.isScreenshotAppletSupportedByOS());
    }

    @Test
    public void testIsScreenshotAppletSupportedByOSUnsupportedOSLinuxDisabled()
    {
        setUserOsAndLinuxScreenshotApplet(WebRequestUtils.OTHER, false);
        assertFalse(attachmentManager.isScreenshotAppletSupportedByOS());
    }

    @Test
    public void testIsScreenshotAppletSupportedByOSUnsupportedOSLinuxEnabled()
    {
        setUserOsAndLinuxScreenshotApplet(WebRequestUtils.OTHER, true);
        assertTrue(attachmentManager.isScreenshotAppletSupportedByOS());
    }


    @Test
    public void testCreateAttachmentCopySourceFile() throws Exception
    {
        CreateAttachmentParamsBean params = prepareCreateAttachmentParamsBean()
                .attachmentProperties(ImmutableMap.<String, Object>of())
                .build();
        attachmentManager.createAttachmentCopySourceFile(params.getFile(), params.getFilename(), params.getContentType(),
                params.getAuthor().getUsername(), params.getIssue(), params.getAttachmentProperties(),
                params.getCreatedTime());
        verifyStoreParamsToDB(params);
        verifyCreateAttachmentCopyIOOperations(params);
    }

    @Test
    public void testCreateAttachmentCopySourceFileWithParamsAsBean() throws Exception
    {
        CreateAttachmentParamsBean params = prepareCreateAttachmentParamsBean()
                .zip(false).thumbnailable(false).attachmentProperties(ImmutableMap.<String, Object>of())
                .copySourceFile(true)
                .build();
        attachmentManager.createAttachment(params);
        verifyStoreParamsToDB(params);
        verifyCreateAttachmentCopyIOOperations(params);
    }


    @Test
    public void testCreateAttachmentWithParamsAsBeanFullCall() throws Exception{
        CreateAttachmentParamsBean params = prepareCreateAttachmentParamsBean()
                .zip(false).thumbnailable(true).attachmentProperties(ImmutableMap.<String, Object>of())
                .copySourceFile(false)
                .build();
        attachmentManager.createAttachment(params);
        verifyStoreParamsToDB(params);
        verifyCreateAttachmentIOOperations(params);
    }


    @Test
    public void testCreateAttachmentWithParamsAsBeanSimplestCall() throws Exception{
        CreateAttachmentParamsBean params = prepareCreateAttachmentParamsBean().build();
        attachmentManager.createAttachment(params);
        verifyStoreParamsToDB(params);
        verifyCreateAttachmentIOOperations(params);
    }


    /**
     * Tests {@link DefaultAttachmentManager#createAttachment(org.ofbiz.core.entity.GenericValue,
     * com.atlassian.crowd.embedded.api.User, String, String, Long, java.util.Map, java.util.Date)}
     */
    @Test
    public void shouldCreateDatabaseObjectsWhenWeCreateAttachment() throws Exception
    {
        CreateAttachmentParamsBean params = prepareCreateAttachmentParamsBean()
                .attachmentProperties(ImmutableMap.<String, Object>of())
                .build();

        attachmentManager.createAttachment(params.getIssue().getGenericValue(), params.getAuthor().getDirectoryUser(),
                params.getContentType(), params.getFilename(), params.getFile().length(), params.getAttachmentProperties(),
                params.getCreatedTime());

        verifyStoreParamsToDB(params);
        verify(attachmentManager, never()).createAttachmentOnDisk(any(Attachment.class), any(File.class), any(User.class));


    }

    /**
     * Tests {@link DefaultAttachmentManager#createAttachment(java.io.File, String, String,
     * com.atlassian.crowd.embedded.api.User, com.atlassian.jira.issue.Issue, java.util.Map, java.util.Date)}
     */
    @Test
    public void testCreateAttachmentMethodWithObjects() throws Exception
    {
        CreateAttachmentParamsBean params = prepareCreateAttachmentParamsBean()
                .attachmentProperties(ImmutableMap.<String, Object>of())
                .build();
        attachmentManager.createAttachment(params.getFile(), params.getFilename(), params.getContentType(), params.getAuthor().getDirectoryUser(), params.getIssue(), params.getAttachmentProperties(), params.getCreatedTime());
        verifyStoreParamsToDB(params);
        verifyCreateAttachmentIOOperations(params);
    }

    /**
     * Tests {@link DefaultAttachmentManager#createAttachment(java.io.File, String, String,
     * com.atlassian.crowd.embedded.api.User, com.atlassian.jira.issue.Issue, Boolean, Boolean, java.util.Map,
     * java.util.Date)}
     */
    @Test
    public void testCreateAttachmentWithObjectsFullCall() throws Exception
    {
        CreateAttachmentParamsBean params = prepareCreateAttachmentParamsBean()
                .zip(true).thumbnailable(true).attachmentProperties(ImmutableMap.<String, Object>of())
                .build();
        attachmentManager.createAttachment(params.getFile(), params.getFilename(), params.getContentType(), params.getAuthor().getDirectoryUser(), params.getIssue(), params.getZip(), params.getThumbnailable(), params.getAttachmentProperties(), params.getCreatedTime());
        verifyStoreParamsToDB(params);
        verifyCreateAttachmentIOOperations(params);
    }

    /**
     * Tests {@link DefaultAttachmentManager#createAttachment(java.io.File, String, String,
     * com.atlassian.crowd.embedded.api.User, org.ofbiz.core.entity.GenericValue, java.util.Map, java.util.Date)}
     */
    @Test
    public void testCreateAttachmentWithIssueAsGenericValue() throws Exception
    {
        CreateAttachmentParamsBean params = prepareCreateAttachmentParamsBean()
                .attachmentProperties(ImmutableMap.<String, Object>of())
                .build();
        when(issueFactory.getIssue(params.getIssue().getGenericValue())).thenReturn((MutableIssue)params.getIssue());
        attachmentManager.createAttachment(params.getFile(), params.getFilename(), params.getContentType(), params.getAuthor().getDirectoryUser(), params.getIssue().getGenericValue(), params.getAttachmentProperties(), params.getCreatedTime());
        verifyStoreParamsToDB(params);
        verifyCreateAttachmentIOOperations(params);
    }

    /**
     * Tests {@link DefaultAttachmentManager#createAttachment(java.io.File, String, String,
     * com.atlassian.crowd.embedded.api.User, org.ofbiz.core.entity.GenericValue, Boolean, Boolean, java.util.Map,
     * java.util.Date)}
     */
    @Test
    public void testCreateAttachmentWithIssueAsGenericValueFullCall() throws Exception
    {
        CreateAttachmentParamsBean params = prepareCreateAttachmentParamsBean()
                .zip(true).thumbnailable(false).attachmentProperties(ImmutableMap.<String, Object>of())
                .build();
        when(issueFactory.getIssue(params.getIssue().getGenericValue())).thenReturn((MutableIssue)params.getIssue());
        attachmentManager.createAttachment(params.getFile(), params.getFilename(), params.getContentType(), params.getAuthor().getDirectoryUser(), params.getIssue().getGenericValue(), params.getZip(), params.getThumbnailable(), params.getAttachmentProperties(), params.getCreatedTime());
        verifyStoreParamsToDB(params);
        verifyCreateAttachmentIOOperations(params);
    }

    /**
     * Tests {@link DefaultAttachmentManager#createAttachment(java.io.File, String, String, com.atlassian.crowd.embedded.api.User, org.ofbiz.core.entity.GenericValue)}
     */
    @Test
    public void testCreateAttachmentShortWithIssueAsGenericValue() throws Exception
    {
        CreateAttachmentParamsBean params = prepareCreateAttachmentParamsBean().build();
        when(issueFactory.getIssue(params.getIssue().getGenericValue())).thenReturn((MutableIssue)params.getIssue());
        attachmentManager.createAttachment(params.getFile(), params.getFilename(), params.getContentType(), params.getAuthor().getDirectoryUser(), params.getIssue().getGenericValue());
        verifyStoreParamsToDB(params);
        verifyCreateAttachmentIOOperations(params);
    }


    /**
     * Tests {@link DefaultAttachmentManager#createAttachment(java.io.File, String, String, com.atlassian.crowd.embedded.api.User, com.atlassian.jira.issue.Issue)}
     */
    @Test
    public void testCreateAttachmentShortWithIssueAsObject() throws Exception
    {
        CreateAttachmentParamsBean params = prepareCreateAttachmentParamsBean().build();
        attachmentManager.createAttachment(params.getFile(), params.getFilename(), params.getContentType(), params.getAuthor().getDirectoryUser(), params.getIssue());
        verifyStoreParamsToDB(params);
        verifyCreateAttachmentIOOperations(params);
    }



    private CreateAttachmentParamsBean.Builder prepareCreateAttachmentParamsBean()
    {
        final File file = mock(File.class);
        when(file.length()).thenReturn(555L);

        final String filename = "myAttachmentFilename";
        final String contentType = "contentType";
        final ApplicationUser user = new MockApplicationUser("userKey", "username");
        final MockIssue issue = new MockIssue(123L);
        issue.setProjectObject(new MockProject(999L, "PKEY"));
        issue.setKey("PKEY-444");
        final CreateAttachmentParamsBean.Builder builder = new CreateAttachmentParamsBean.Builder(file, filename, contentType, user, issue);

        when(ofBizDelegator.createValue(eq(AttachmentConstants.ATTACHMENT_ENTITY_NAME), any(Map.class))).thenAnswer(new Answer<GenericValue>()
        {
            @Override
            public GenericValue answer(InvocationOnMock invocation) throws Throwable
            {
                HashMap<Object, Object> result = Maps.newHashMap((Map<?, ?>) invocation.getArguments()[1]);
                result.put("id", 1L);
                return new MockGenericValue(AttachmentConstants.ATTACHMENT_ENTITY_NAME, result);
            }
        });

        when(mimeManager.getSanitisedMimeType(contentType, filename)).thenReturn(contentType);
        when(userManager.getUserByName(user.getUsername())).thenReturn(user);
        when(userKeyService.getKeyForUsername(user.getUsername())).thenReturn(user.getKey());
        return builder;
    }

    private void verifyStoreParamsToDB(CreateAttachmentParamsBean params)
    {

        List<Matcher<? super Map<String, Object>>> paramsMatchers = new ArrayList<Matcher<? super Map<String, Object>>>();
        paramsMatchers.add(Matchers.<String, Object>hasEntry("issue", params.getIssue().getId()));
        paramsMatchers.add(Matchers.<String, Object>hasEntry("author", params.getAuthor().getKey()));
        paramsMatchers.add(Matchers.<String, Object>hasEntry("mimetype", params.getContentType()));
        paramsMatchers.add(Matchers.<String, Object>hasEntry("filesize", params.getFile().length()));
        paramsMatchers.add(Matchers.<String, Object>hasEntry("filename", params.getFilename()));
        if (params.getZip() != null)
        {
            paramsMatchers.add(Matchers.<String, Object>hasEntry("zip", params.getZip() ? 1 : 0));
        }
        if (params.getThumbnailable() != null)
        {
            paramsMatchers.add(Matchers.<String, Object>hasEntry("thumbnailable", params.getThumbnailable() ? 1 : 0));
        }
        final Matcher<Map<String, Object>> allOfMatchers = Matchers.allOf(paramsMatchers);
        verify(ofBizDelegator).createValue(eq(AttachmentConstants.ATTACHMENT_ENTITY_NAME), argThat(allOfMatchers));
    }


    private void verifyCreateAttachmentIOOperations(CreateAttachmentParamsBean params) throws Exception{
        verify(attachmentManager).createAttachmentOnDisk(any(Attachment.class), same(params.getFile()), same(params.getAuthor().getDirectoryUser()));
        verify(attachmentManager, never()).createAttachmentOnDiskCopySourceFile(any(Attachment.class), any(File.class));

        if(params.getAttachmentProperties() != null)
            verify(attachmentManager).createAttachmentPropertySet(any(GenericValue.class), same(params.getAttachmentProperties()));
    }


    private void verifyCreateAttachmentCopyIOOperations(CreateAttachmentParamsBean params) throws Exception{
        verify(attachmentManager).createAttachmentOnDiskCopySourceFile(any(Attachment.class), same(params.getFile()));
        verify(attachmentManager, never()).createAttachmentOnDisk(any(Attachment.class), any(File.class), any(User.class));

        if(params.getAttachmentProperties() != null)
            verify(attachmentManager).createAttachmentPropertySet(any(GenericValue.class), same(params.getAttachmentProperties()));
    }


    @Test
    public void testGetAttachment() throws GenericEntityException
    {
        when(ofBizDelegator.findById(AttachmentConstants.ATTACHMENT_ENTITY_NAME, 101L)).thenReturn(attachment2);

        Attachment attachment = attachmentManager.getAttachment(new Long(101));
        assertNotNull(attachment);
        assertEquals(new Long(101), attachment.getId());
    }


}
