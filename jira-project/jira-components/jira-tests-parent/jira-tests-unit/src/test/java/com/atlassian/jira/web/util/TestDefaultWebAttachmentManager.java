package com.atlassian.jira.web.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import com.atlassian.core.test.util.DuckTypeProxy;
import com.atlassian.core.util.FileSize;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.issue.attachment.AttachmentService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.exception.RemoveException;
import com.atlassian.jira.issue.AttachmentManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.TemporaryAttachmentsMonitorLocator;
import com.atlassian.jira.issue.attachment.Attachment;
import com.atlassian.jira.issue.attachment.TemporaryAttachment;
import com.atlassian.jira.issue.history.ChangeItemBean;
import com.atlassian.jira.mock.MockAttachmentManager;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.mock.issue.MockIssue;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.mock.security.MockSimpleAuthenticationContext;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.LimitedOutputStream;
import com.atlassian.jira.util.NoopI18nFactory;
import com.atlassian.jira.util.NoopI18nHelper;
import com.atlassian.jira.util.collect.IteratorEnumeration;
import com.atlassian.jira.web.action.issue.TemporaryAttachmentsMonitor;
import com.atlassian.jira.web.bean.I18nBean;
import com.atlassian.jira.web.bean.MockI18nBean;

import com.google.common.collect.Lists;

import org.apache.commons.io.FileUtils;
import org.easymock.classextension.EasyMock;
import org.easymock.classextension.IMocksControl;
import org.junit.Before;
import org.junit.Test;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import webwork.multipart.MultiPartRequestWrapper;

import static org.easymock.classextension.EasyMock.createControl;
import static org.easymock.classextension.EasyMock.createNiceMock;
import static org.easymock.classextension.EasyMock.expect;
import static org.easymock.classextension.EasyMock.replay;
import static org.easymock.classextension.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @since v4.0
 */
public class TestDefaultWebAttachmentManager
{
    private static final String ASCII = "US-ASCII";
    
    private IMocksControl control;
    private AttachmentManager attachmentManager;
    private I18nHelper.BeanFactory factory;
    private JiraAuthenticationContext authCtx;
    private TemporaryAttachmentsMonitorLocator locator;
    private AttachmentService service;

    @Before
    public void setUp() throws Exception
    {
        control = EasyMock.createControl();
        attachmentManager = control.createMock(AttachmentManager.class);
        factory = new NoopI18nFactory();
        authCtx = new MockSimpleAuthenticationContext(new MockUser("bbain"), Locale.ENGLISH, new NoopI18nHelper());
        locator = control.createMock(TemporaryAttachmentsMonitorLocator.class);
        service = control.createMock(AttachmentService.class);
        ComponentAccessor.initialiseWorker(new MockComponentWorker());
    }

    @Test
    public void testCreateAttachment() throws GenericEntityException, AttachmentException, IOException
    {
        IMocksControl control = createControl();

        final String fileParam = "TestDefaultWebAttachmentManager";
        final String fileName = "a-file-name.txt";
        final String contentType = "text/plain";
        final File file = File.createTempFile("testCreateAttachment", ".txt");
        file.deleteOnExit();

        final MultiPartRequestWrapper wrapper = control.createMock(MultiPartRequestWrapper.class);
        expect(wrapper.getFile(fileParam)).andReturn(file);
        expect(wrapper.getFilesystemName(fileParam)).andReturn(fileName);
        expect(wrapper.getContentType(fileParam)).andReturn(contentType);

        final User user = new MockUser("user");
        final GenericValue issueGV = new MockGenericValue("issue");
        final Map<String, Object> map = new HashMap<String, Object>();

        final ChangeItemBean changeBean = control.createMock(ChangeItemBean.class);

        final AttachmentManager attachmentManager = new MockAttachmentManager()
        {
            @Override
            public Attachment getAttachment(final Long id) throws DataAccessException
            {
                return null;
            }

            @Override
            public List<Attachment> getAttachments(final Issue issue) throws DataAccessException
            {
                return null;
            }

            @Override
            public List<Attachment> getAttachments(final Issue issue, final Comparator<? super Attachment> comparator)
                    throws DataAccessException
            {
                return null;
            }

            @Override
            public ChangeItemBean createAttachment(File file, String filename, String contentType, User author, Issue issue)
                    throws AttachmentException
            {
                throw new UnsupportedOperationException("not implemented");
            }

            @Override
            public Attachment createAttachmentCopySourceFile(final File file, final String filename, final String contentType, final String attachmentAuthor, final Issue issue, final Map attachmentProperties, final Date createdTime)
                    throws AttachmentException
            {
                return null;
            }

            @Override
            public ChangeItemBean createAttachment(final File file, final String filename, final String contentType, final User remoteUser, final GenericValue issue, final Map attachmentProperties, final Date createdTime)
            {
                return changeBean;
            }

            @Override
            public ChangeItemBean createAttachment(final File file, final String filename, final String contentType, final User remoteUser, final Issue issue, final Map attachmentProperties, final Date createdTime)
            {
                return changeBean;
            }

            @Override
            public ChangeItemBean createAttachment(final File file, final String filename, final String contentType, final User remoteUser, final GenericValue issue)
                    throws AttachmentException
            {
                return null;
            }
            @Override
            public Attachment createAttachment(final GenericValue issue, final User author, final String mimetype, final String filename, final Long filesize, final Map attachmentProperties, final Date createdTime)
            {
                return null;
            }

            @Override
            public void deleteAttachment(final Attachment attachment) throws RemoveException
            {
            }

            @Override
            public void deleteAttachmentDirectory(final Issue issue)
            {
            }

            @Override
            public boolean attachmentsEnabled()
            {
                return false;
            }

            @Override
            public boolean isScreenshotAppletEnabled()
            {
                return false;
            }

            @Override
            public boolean isScreenshotAppletSupportedByOS()
            {
                return false;
            }

            @Override
            public List<ChangeItemBean> convertTemporaryAttachments(final User user, final Issue issue, final List<Long> selectedAttachments, final TemporaryAttachmentsMonitor temporaryAttachmentsMonitor)
                    throws AttachmentException
            {
                return null;
            }
        };

        control.replay();

        WebAttachmentManager manager = new DefaultWebAttachmentManager(attachmentManager, null, null, null, null);
        final ChangeItemBean attachment = manager.createAttachment(wrapper, user, issueGV, fileParam, map);
        assertTrue(attachment == changeBean);

        control.verify();

        FileUtils.deleteQuietly(file);
    }

    @Test
    public void testValidateAttachmentIfExistsRequest() throws AttachmentException, IOException
    {
        final String param = "testParam";
        final String contentType = "text/plain";
        final File file = File.createTempFile("testValidateAttachmentIfExistsFile.", ".test");
        file.deleteOnExit();
        final FileWriter fileWriter = new FileWriter(file);
        fileWriter.write("text that goes in the attachment");
        fileWriter.close();

        final long maxSize = 1024;

        WebAttachmentManager manager = new DefaultWebAttachmentManager(null, null, null, null, null)
        {
            protected I18nBean getI18n()
            {
                return new MockI18nBean();
            }

            protected long getMaxAttachmentSize()
            {
                return maxSize;
            }
        };

        final MultiPartRequestWrapper request = createNiceMock(MultiPartRequestWrapper.class);

        expect(request.getFile(param)).andReturn(file);
        expect(request.getFilesystemName(param)).andReturn(file.getName());
        expect(request.getContentType(param)).andReturn(contentType);
        replay(request);
        manager.validateAttachmentIfExists(request, param, true);
        verify(request);
    }

    @Test
    public void testValidateAttachmentIfExistsFile() throws IOException, AttachmentException
    {
        final long maxSize = 1024;
        DefaultWebAttachmentManager manager = new DefaultWebAttachmentManager(null, null, null, null, null)
        {
            protected I18nBean getI18n()
            {
                return new MockI18nBean();
            }

            protected long getMaxAttachmentSize()
            {
                return maxSize;
            }
        };

        // check that a null File returns false
        assertFalse(manager.assertAttachmentIfExists(null, null, false, 0));

        final File file = File.createTempFile("testValidateAttachmentIfExistsFile.", ".test");
        file.deleteOnExit();

        // ensure we get an exception if we specify a file name but don't provide a file
        try
        {
            manager.assertAttachmentIfExists(null, file.getName(), false, 100000000);
            fail();
        }
        catch (AttachmentException expected)
        {
            assertEquals(expected.getMessage(), new MockI18nBean().getText("attachfile.error.file.large", new Object[] { file.getName(), FileSize.format(maxSize) }));
        }

        // our temp file is still zero-length, make sure we get an exception from that
        try
        {
            manager.assertAttachmentIfExists(file, file.getName(), false, 0);
            fail();
        }
        catch (AttachmentException expected)
        {
            assertEquals(expected.getMessage(), new MockI18nBean().getText("attachfile.error.file.zero", file.getName()));
        }

        // put some data in our temp file so that we can get past the zero-length check
        final FileWriter fileWriter = new FileWriter(file);
        fileWriter.write("text that goes in the attachment");
        fileWriter.close();

        for (char badChar : FileNameCharacterCheckerUtil.getInvalidCharacters())
        {
            String badName = "badchars" + badChar;
            try
            {
                manager.assertAttachmentIfExists(file, badName, false, 0);
                fail();
            }
            catch (AttachmentException expected)
            {
                assertEquals(expected.getMessage(), new MockI18nBean().getText("attachfile.error.invalidcharacter", badName, Character.toString(badChar)));
            }
        }

        try
        {
            manager.assertAttachmentIfExists(file, "", false, 0);
            fail("Expecing exception.");
        }
        catch (AttachmentException expected)
        {
            assertEquals(expected.getMessage(), new MockI18nBean().getText("attachfile.error.no.name"));
        }

        try
        {
            manager.assertAttachmentIfExists(file, "    ", false, 0);
            fail("Expecing exception.");
        }
        catch (AttachmentException expected)
        {
            assertEquals(expected.getMessage(), new MockI18nBean().getText("attachfile.error.no.name"));
        }

        // finally we get to the happy path
        manager.assertAttachmentIfExists(file, file.getName(), false, 0);
    }

    @Test
    public void testTemporaryStreamZeroSize() throws Exception
    {
        DefaultWebAttachmentManager manager = new DefaultWebAttachmentManager(attachmentManager, factory, authCtx, locator, service);

        String fileName = "name";

        control.replay();
        try
        {
            manager.createTemporaryAttachment(getInputStreamForString("something"), fileName, "type", 0, new MockIssue(5), null, null);
            fail("Expected to fail on the bad filename");
        }
        catch (AttachmentException e)
        {
            assertEquals(NoopI18nHelper.makeTranslation("attachfile.error.file.zero", fileName), e.getMessage());
        }

        control.verify();
    }

    @Test
    public void testTemporaryStreamCantAttach() throws Exception
    {
        final String errorMessage = "message";
        final Issue attachIssue = new MockIssue(5);
        DefaultWebAttachmentManager manager = new DefaultWebAttachmentManager(attachmentManager, factory, authCtx, locator, service)
        {
            @Override
            void assertCanAttach(Issue issue, Project project) throws AttachmentException
            {
                assertNull(project);
                assertSame(attachIssue, issue);
                throw new AttachmentException(errorMessage);
            }

            @Override
            long getMaxAttachmentSize()
            {
                return 50;
            }
        };

        control.replay();
        try
        {
            manager.createTemporaryAttachment(getInputStreamForString("1"), "name", "type", 1, attachIssue, null, null);
            fail("Expected to fail on the bad filename");
        }
        catch (AttachmentException e)
        {
            assertEquals(errorMessage, e.getMessage());
        }

        control.verify();
    }

    @Test
    public void testTemporaryStreamBadFileName() throws Exception
    {
        final String errorMessage = "message";
        final String expectedFile = "name";
        final Issue attachIssue = new MockIssue(5);
        DefaultWebAttachmentManager manager = new DefaultWebAttachmentManager(attachmentManager, factory, authCtx, locator, service)
        {
            @Override
            void assertCanAttach(Issue issue, Project project) throws AttachmentException
            {
            }

            @Override
            void assertFileNameIsValid(String filename) throws AttachmentException
            {
                assertEquals(expectedFile, filename);
                throw new AttachmentException(errorMessage);
            }

            @Override
            long getMaxAttachmentSize()
            {
                return 50;
            }
        };

        control.replay();
        try
        {
            manager.createTemporaryAttachment(getInputStreamForString("1"), expectedFile, "type", 1, attachIssue, null, null);
            fail("Expected to fail on the bad filename");
        }
        catch (AttachmentException e)
        {
            assertEquals(errorMessage, e.getMessage());
        }

        control.verify();
    }

    @Test
    public void testTemporaryStreamTooSmall() throws Exception
    {
        final DefaultWebAttachmentManager.UniqueFile tempFile = createUniqueFile();
        final String fileName = "name";
        DefaultWebAttachmentManager manager = new DefaultWebAttachmentManager(attachmentManager, factory, authCtx, locator, service)
        {
            @Override
            long getMaxAttachmentSize()
            {
                return 50;
            }

            @Override
            void assertCanAttach(Issue issue, Project project) throws AttachmentException
            {
            }

            @Override
            UniqueFile createUniqueFile(String fileName)
            {
                return tempFile;
            }
        };

        control.replay();
        try
        {
            manager.createTemporaryAttachment(getInputStreamForString("1"), fileName, "type", 20, new MockIssue(5), null, null);
            fail("Expected to fail.");
        }
        catch (AttachmentException e)
        {
            assertEquals(NoopI18nHelper.makeTranslation("attachfile.error.io.bad.size", fileName, 1, 20), e.getMessage());
            assertFalse(tempFile.getFile().exists());
        }

        control.verify();
    }

    @Test
    public void testTemporaryStreamNoData() throws Exception
    {
        final DefaultWebAttachmentManager.UniqueFile tempFile = createUniqueFile();
        final String fileName = "name";
        DefaultWebAttachmentManager manager = new DefaultWebAttachmentManager(attachmentManager, factory, authCtx, locator, service)
        {
            @Override
            long getMaxAttachmentSize()
            {
                return 50;
            }

            @Override
            void assertCanAttach(Issue issue, Project project) throws AttachmentException
            {
            }

            @Override
            UniqueFile createUniqueFile(String fileName)
            {
                return tempFile;
            }
        };

        control.replay();
        try
        {
            manager.createTemporaryAttachment(getInputStreamForString(""), fileName, "type", 20, new MockIssue(5), null, null);
            fail("Expected to fail.");
        }
        catch (AttachmentException e)
        {
            assertEquals(NoopI18nHelper.makeTranslation("attachfile.error.io.bad.size.zero", fileName), e.getMessage());
            assertFalse(tempFile.getFile().exists());
        }

        control.verify();
    }

    @Test
    public void testTemporaryStreamTooBig() throws Exception
    {
        final DefaultWebAttachmentManager.UniqueFile tempFile = createUniqueFile();
        final String fileName = "name";

        String data = "justtoobig";
        InputStream stream = getInputStreamForString(data);

        DefaultWebAttachmentManager manager = new DefaultWebAttachmentManager(attachmentManager, factory, authCtx, locator, service)
        {
            @Override
            long getMaxAttachmentSize()
            {
                return 50;
            }

            @Override
            void assertCanAttach(Issue issue, Project project) throws AttachmentException
            {
            }

            @Override
            UniqueFile createUniqueFile(String fileName)
            {
                return tempFile;
            }
        };

        control.replay();
        try
        {
            manager.createTemporaryAttachment(stream, fileName, "type", data.length() - 1, new MockIssue(5), null, null);
            fail("Expected to fail.");
        }
        catch (AttachmentException e)
        {
            assertEquals(NoopI18nHelper.makeTranslation("attachfile.error.file.large", fileName, FileSize.format(data.length())), e.getMessage());
            assertFalse(tempFile.getFile().exists());
        }

        control.verify();
    }

    @Test
    public void testTemporaryStreamIOErrorOnClose() throws Exception
    {
        final DefaultWebAttachmentManager.UniqueFile tempFile = createUniqueFile();
        final String fileName = "name";
        final String errorMessage = "Test IO Error";

        ByteArrayInputStream bis = new ByteArrayInputStream(new byte[] { 1 })
        {
            @Override
            public void close() throws IOException
            {
                throw new IOException(errorMessage);
            }
        };

        DefaultWebAttachmentManager manager = new DefaultWebAttachmentManager(attachmentManager, factory, authCtx, locator, service)
        {
            @Override
            long getMaxAttachmentSize()
            {
                return 50;
            }

            @Override
            void assertCanAttach(Issue issue, Project project) throws AttachmentException
            {
            }

            @Override
            UniqueFile createUniqueFile(String fileName)
            {
                return tempFile;
            }

            @Override
            LimitedOutputStream wrapOutputStream(OutputStream fos, long size)
            {
                return new LimitedOutputStream(fos, size)
                {
                    @Override
                    public void close() throws IOException
                    {
                        throw new IOException(errorMessage);
                    }
                };
            }
        };

        control.replay();
        try
        {
            manager.createTemporaryAttachment(bis, fileName, "type", 1, new MockIssue(5), null, null);
            fail("Expected to fail.");
        }
        catch (AttachmentException e)
        {
            assertEquals(NoopI18nHelper.makeTranslation("attachfile.error.io.error", fileName, errorMessage), e.getMessage());
            assertFalse(tempFile.getFile().exists());
        }

        control.verify();
    }

    @Test
    public void testTemporaryStreamIOErrorOnCopy() throws Exception
    {
        final DefaultWebAttachmentManager.UniqueFile tempFile = createUniqueFile();
        final String fileName = "name";
        final String errorMessage = "Test IO Error";

        InputStream stream = new InputStream()
        {
            @Override
            public int read() throws IOException
            {
                throw new IOException(errorMessage);
            }
        };

        DefaultWebAttachmentManager manager = new DefaultWebAttachmentManager(attachmentManager, factory, authCtx, locator, service)
        {
            @Override
            long getMaxAttachmentSize()
            {
                return 50;
            }

            @Override
            void assertCanAttach(Issue issue, Project project) throws AttachmentException
            {
            }

            @Override
            UniqueFile createUniqueFile(String fileName)
            {
                return tempFile;
            }
        };
        control.replay();
        try
        {
            manager.createTemporaryAttachment(stream, fileName, "type", 1, new MockIssue(5), null, null);
            fail("Expected to fail.");
        }
        catch (AttachmentException e)
        {
            assertEquals(NoopI18nHelper.makeTranslation("attachfile.error.io.error", fileName, errorMessage), e.getMessage());
            assertFalse(tempFile.getFile().exists());
        }

        control.verify();
    }

    @Test
    public void testTemporaryStreamHappyPath() throws Exception
    {
        final DefaultWebAttachmentManager.UniqueFile tempFile = createUniqueFile();
        final String name = "name";
        final String type = "type";
        final long size = 1;
        final Issue issue = new MockIssue(5);
        final AtomicReference<TemporaryAttachment> monitorAttachment = new AtomicReference<TemporaryAttachment>();

        DefaultWebAttachmentManager manager = new DefaultWebAttachmentManager(attachmentManager, factory, authCtx, locator, service)
        {
            @Override
            long getMaxAttachmentSize()
            {
                return 50;
            }

            @Override
            void assertCanAttach(Issue issue, Project project) throws AttachmentException
            {
            }

            @Override
            UniqueFile createUniqueFile(String fileName)
            {
                return tempFile;
            }

            @Override
            void addToMonitor(TemporaryAttachment temporaryAttachment) throws AttachmentException
            {
                monitorAttachment.set(temporaryAttachment);
            }
        };

        control.replay();

        TemporaryAttachment actualAttachment = manager.createTemporaryAttachment(getInputStreamForString("1"), name, type, size, issue, null, null);
        TemporaryAttachment expectedAttachment = new TemporaryAttachment(tempFile.getId(), tempFile.getFile(), name, type, null);
        assertAttachmentEquals(expectedAttachment, actualAttachment);
        assertAttachmentEquals(expectedAttachment, monitorAttachment.get());

        control.verify();
    }

    @Test
    public void testTemporaryStreamNoMonitor() throws Exception
    {
        final DefaultWebAttachmentManager.UniqueFile tempFile = createUniqueFile();
        final String name = "name";
        final String type = "type";
        final long size = 1;
        final Issue issue = new MockIssue(5);
        final TemporaryAttachment expectedAttachment = new TemporaryAttachment(tempFile.getId(), tempFile.getFile(), name, type, null);
        final String expectedError = "Here Is another expected error";

        DefaultWebAttachmentManager manager = new DefaultWebAttachmentManager(attachmentManager, factory, authCtx, locator, service)
        {
            @Override
            long getMaxAttachmentSize()
            {
                return 50;
            }

            @Override
            void assertCanAttach(Issue issue, Project project) throws AttachmentException
            {
            }

            @Override
            UniqueFile createUniqueFile(String fileName)
            {
                return tempFile;
            }

            @Override
            void addToMonitor(TemporaryAttachment temporaryAttachment) throws AttachmentException
            {
                assertAttachmentEquals(expectedAttachment, temporaryAttachment);
                throw new AttachmentException(expectedError);
            }
        };

        control.replay();

        try
        {
            manager.createTemporaryAttachment(getInputStreamForString("1"), name, type, size, issue, null, null);
            fail("Expected an error.");
        }
        catch (AttachmentException e)
        {
            assertEquals(expectedError, e.getMessage());
        }

        FileUtils.deleteQuietly(tempFile.getFile());

        control.verify();
    }

    @Test
    public void testAddToMonitorHappy() throws Exception
    {
        TemporaryAttachment expectedAttachment = new TemporaryAttachment(56L, new File("random"), "ranme", "type", null);
        TemporaryAttachmentsMonitor monitor = new TemporaryAttachmentsMonitor();
        expect(locator.get(true)).andReturn(monitor);

        DefaultWebAttachmentManager manager = new DefaultWebAttachmentManager(attachmentManager, factory, authCtx, locator, service);

        control.replay();

        manager.addToMonitor(expectedAttachment);
        assertSame(monitor.getById(expectedAttachment.getId()), expectedAttachment);

        control.verify();
    }

    @Test
    public void testAddToMonitorSad() throws Exception
    {
        File tempFile = File.createTempFile("testAddToMonitorSad", "test");
        tempFile.deleteOnExit();

        TemporaryAttachment expectedAttachment = new TemporaryAttachment(56L, tempFile, "ranme", "type", null);
        expect(locator.get(true)).andReturn(null);

        DefaultWebAttachmentManager manager = new DefaultWebAttachmentManager(attachmentManager, factory, authCtx, locator, service);
        control.replay();

        try
        {
            manager.addToMonitor(expectedAttachment);
            fail("Exepcted an exception.");
        }
        catch (AttachmentException e)
        {
            assertEquals(NoopI18nHelper.makeTranslation("attachfile.error.session.error", expectedAttachment.getFilename()), e.getMessage());
            assertFalse(tempFile.exists());
        }

        control.verify();
    }

    @Test
    public void testAssertCanAttachIssueServiceSaysNoooooo() throws Exception
    {
        final MockIssue mockIssue = new MockIssue(10L);
        final String errorMessage = "erro";

        AttachmentService attachmentService = (AttachmentService) DuckTypeProxy.getProxy(AttachmentService.class, new Object()
        {
            public boolean canCreateTemporaryAttachments(JiraServiceContext ctx, Issue issue)
            {
                assertEquals(authCtx.getLoggedInUser(), ctx.getLoggedInUser());
                assertSame(mockIssue, issue);

                ctx.getErrorCollection().addError("field", errorMessage);
                return false;
            }
        });

        DefaultWebAttachmentManager manager = new DefaultWebAttachmentManager(attachmentManager, factory, authCtx, locator, attachmentService);
        control.replay();

        try
        {
            manager.assertCanAttach(mockIssue, null);
            fail("Exepcted an exception.");
        }
        catch (AttachmentException e)
        {
            assertEquals(errorMessage, e.getMessage());
        }

        control.verify();

    }

    @Test
    public void testAssertCanAttachIssueHappy() throws Exception
    {
        final MockIssue mockIssue = new MockIssue(10L);

        expect(service.canCreateTemporaryAttachments(EasyMock.<JiraServiceContext>notNull(), EasyMock.eq(mockIssue))).andReturn(true);

        DefaultWebAttachmentManager manager = new DefaultWebAttachmentManager(attachmentManager, factory, authCtx, locator, service);
        control.replay();

        manager.assertCanAttach(mockIssue, null);

        control.verify();
    }

    @Test
    public void testAssertCanAttachProjectServiceSaysNoooooo() throws Exception
    {
        final MockProject mockProject = new MockProject(10L);
        final String errorMessage = "erro";

        AttachmentService attachmentService = (AttachmentService) DuckTypeProxy.getProxy(AttachmentService.class, new Object()
        {
            public boolean canCreateAttachments(JiraServiceContext ctx, Project project)
            {
                assertEquals(authCtx.getLoggedInUser(), ctx.getLoggedInUser());
                assertSame(mockProject, project);

                ctx.getErrorCollection().addErrorMessage(errorMessage);
                return false;
            }
        });

        DefaultWebAttachmentManager manager = new DefaultWebAttachmentManager(attachmentManager, factory, authCtx, locator, attachmentService);
        control.replay();

        try
        {
            manager.assertCanAttach(null, mockProject);
            fail("Exepcted an exception.");
        }
        catch (AttachmentException e)
        {
            assertEquals(errorMessage, e.getMessage());
        }

        control.verify();

    }

    @Test
    public void testAssertCanAttachProjectHappy() throws Exception
    {
        final MockProject mockProject = new MockProject(10L);

        expect(service.canCreateAttachments(EasyMock.<JiraServiceContext>notNull(), EasyMock.eq(mockProject))).andReturn(true);

        DefaultWebAttachmentManager manager = new DefaultWebAttachmentManager(attachmentManager, factory, authCtx, locator, service);

        control.replay();
        manager.assertCanAttach(null, mockProject);
        control.verify();
    }

    @Test
    public void testTemporaryMultipartNoFile() throws Exception
    {
        final String fileParam = "fileParam";

        DefaultWebAttachmentManager manager = new DefaultWebAttachmentManager(attachmentManager, factory, authCtx, locator, service)
        {
            @Override
            public boolean validateAttachmentIfExists(MultiPartRequestWrapper requestWrapper, String actaulFileParam, boolean required)
                    throws AttachmentException
            {
                assertEquals(fileParam, actaulFileParam);
                assertFalse(required);

                return true;
            }
        };

        MultiPartRequestWrapper request = control.createMock(MultiPartRequestWrapper.class);

        expect(request.getFile(fileParam)).andReturn(null);
        expect(request.getFileNames()).andReturn(IteratorEnumeration.fromIterable(Lists.<Object>newArrayList()));

        control.replay();

        TemporaryAttachment temporaryAttachment = manager.createTemporaryAttachment(request, fileParam, new MockIssue(76L), null, null);
        assertNull(temporaryAttachment);

        control.verify();
    }

    @Test
    public void testTemporaryMultipartHappy() throws Exception
    {
        final String fileParam = "fileParam";
        final String fileName = "filename";
        final String contentType = "test/html";
        final String data = "data";

        final MultiPartRequestWrapper request = control.createMock(MultiPartRequestWrapper.class);
        final Project mockProject = new MockProject(1012838);
        final File sourceFile = getTemporaryFile(true);
        final DefaultWebAttachmentManager.UniqueFile target = createUniqueFile(false);
        final AtomicReference<TemporaryAttachment> attachRef = new AtomicReference<TemporaryAttachment>();

        FileUtils.writeStringToFile(sourceFile, data, ASCII);

        DefaultWebAttachmentManager manager = new DefaultWebAttachmentManager(attachmentManager, factory, authCtx, locator, service)
        {
            @Override
            void assertCanAttach(Issue issue, Project project) throws AttachmentException
            {
                assertNull(null);
                assertSame(mockProject, project);
            }

            @Override
            public boolean validateAttachmentIfExists(MultiPartRequestWrapper actualRequest, String actualParamName, boolean actualRequired)
                    throws AttachmentException
            {
                assertSame(request, actualRequest);
                assertEquals(fileParam, actualParamName);
                assertFalse(actualRequired);

                return true;
            }

            @Override
            UniqueFile createUniqueFile(String fileName)
            {
                return target;
            }

            @Override
            void addToMonitor(TemporaryAttachment temporaryAttachment) throws AttachmentException
            {
                attachRef.set(temporaryAttachment);
            }
        };

        expect(request.getFile(fileParam)).andReturn(sourceFile);
        expect(request.getFilesystemName(fileParam)).andReturn(fileName);
        expect(request.getContentType(fileParam)).andReturn(contentType);

        control.replay();

        TemporaryAttachment actualAttachment = new TemporaryAttachment(target.getId(), target.getFile(), fileName, contentType, null);
        TemporaryAttachment expectedAttachment = manager.createTemporaryAttachment(request, fileParam, null, mockProject, null);

        assertAttachmentEquals(expectedAttachment, actualAttachment);
        assertAttachmentEquals(expectedAttachment, attachRef.get());

        assertFalse(sourceFile.exists());
        assertTrue(target.getFile().exists());
        assertEquals(data, FileUtils.readFileToString(target.getFile(), ASCII));

        control.verify();
    }

    @Test
    public void testNoContentTypeRejected() throws Exception
    {
        final String param = "testParam";
        final String contentType = "";
        final File file = File.createTempFile("testValidateAttachmentIfExistsFile.", ".test");
        file.deleteOnExit();
        final FileWriter fileWriter = new FileWriter(file);
        fileWriter.write("text that goes in the attachment");
        fileWriter.close();

        final long maxSize = 1024;

        WebAttachmentManager manager = new DefaultWebAttachmentManager(null, null, null, null, null)
        {
            protected I18nBean getI18n()
            {
                return new MockI18nBean();
            }

            protected long getMaxAttachmentSize()
            {
                return maxSize;
            }
        };

        final MultiPartRequestWrapper request = createNiceMock(MultiPartRequestWrapper.class);

        expect(request.getFile(param)).andReturn(file);
        expect(request.getFilesystemName(param)).andReturn(file.getName());
        expect(request.getContentType(param)).andReturn(contentType);
        replay(request);
        assertFalse(manager.validateAttachmentIfExists(request, param, true));
        verify(request);
    }


    private DefaultWebAttachmentManager.UniqueFile createUniqueFile() throws IOException
    {
        return createUniqueFile(true);
    }

    private DefaultWebAttachmentManager.UniqueFile createUniqueFile(boolean create) throws IOException
    {
        File tempFile = getTemporaryFile(create);
        return new DefaultWebAttachmentManager.UniqueFile(tempFile, 547985374L);
    }

    private File getTemporaryFile(boolean create) throws IOException
    {
        File dir = new File(System.getProperty("java.io.tmpdir")), tmpFile;
        int count = 0;
        do
        {
            tmpFile = new File(dir, "TestDefaultWebAttachmentManager.tmp." + (count++));
        }
        while (tmpFile.exists());

        if (create)
        {
            if (!tmpFile.createNewFile())
            {
                throw new IOException();
            }
        }
        tmpFile.deleteOnExit();
        return tmpFile;
    }

    private InputStream getInputStreamForString(String data) throws UnsupportedEncodingException
    {
        return new ByteArrayInputStream(data.getBytes(ASCII));
    }

    private void assertAttachmentEquals(TemporaryAttachment one1, TemporaryAttachment two1)
    {
        assertEquals(one1.getFilename(), two1.getFilename());
        assertEquals(one1.getId(), two1.getId());
        assertEquals(one1.getContentType(), two1.getContentType());
        assertEquals(one1.getFile(), two1.getFile());
        assertEquals(one1.getFormToken(), two1.getFormToken());
    }
}
