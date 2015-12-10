package com.atlassian.jira.rest.util;

import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.xsrf.XsrfCheckResult;
import com.atlassian.jira.security.xsrf.XsrfInvocationChecker;
import com.atlassian.jira.util.I18nHelper;
import org.easymock.classextension.IMocksControl;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.classextension.EasyMock.createControl;
import static org.junit.Assert.assertEquals;

/**
 * @since v4.4
 */
public class TestAttachmentHelper
{
    IMocksControl control;
    private HttpServletRequest request;

    @Before
    public void createDeps()
    {
        control = createControl();
        request = control.createMock(HttpServletRequest.class);
    }

    @Test
    public void testBadXsrf()
    {

        XsrfInvocationChecker xsrfChecker = control.createMock(XsrfInvocationChecker.class);
        expect(xsrfChecker.checkWebRequestInvocation(request)).andReturn(new Result(true, false));
        JiraAuthenticationContext jiraAuthenticationContext = control.createMock(JiraAuthenticationContext.class);
        I18nHelper i18nHelper = control.createMock(I18nHelper.class);
        expect(i18nHelper.getText(eq("xsrf.error.title"), anyObject())).andReturn("xsrf.error.title").anyTimes();
        expect(jiraAuthenticationContext.getI18nHelper()).andReturn(i18nHelper);

        control.replay();

        AttachmentHelper attachmentHelper = new AttachmentHelper(xsrfChecker, jiraAuthenticationContext);
        AttachmentHelper.ValidationResult validationResult = attachmentHelper.validate(request, "test.jpg", 15430L);

        assertEquals(validationResult.getErrorType(), AttachmentHelper.ValidationError.XSRF_TOKEN_INVALID);

        control.verify();
    }


    @Test
    public void testNoFileName()
    {
        XsrfInvocationChecker xsrfChecker = control.createMock(XsrfInvocationChecker.class);
        JiraAuthenticationContext jiraAuthenticationContext = control.createMock(JiraAuthenticationContext.class);
        expect(xsrfChecker.checkWebRequestInvocation(request)).andReturn(new Result(true, true));

        control.replay();

        AttachmentHelper attachmentHelper = new AttachmentHelper(xsrfChecker, jiraAuthenticationContext);
        AttachmentHelper.ValidationResult validationResult = attachmentHelper.validate(request, null, 15430L);

        assertEquals(validationResult.getErrorType(), AttachmentHelper.ValidationError.FILENAME_BLANK);

        control.verify();
    }

    @Test
    public void testIOException() throws IOException
    {
        XsrfInvocationChecker xsrfChecker = control.createMock(XsrfInvocationChecker.class);
        JiraAuthenticationContext jiraAuthenticationContext = control.createMock(JiraAuthenticationContext.class);
        I18nHelper i18nHelper = control.createMock(I18nHelper.class);
        expect(i18nHelper.getText(eq("attachfile.error.io.error"), anyObject())).andReturn("attachfile.error.io.error").anyTimes();

        expect(jiraAuthenticationContext.getI18nHelper()).andReturn(i18nHelper);
        
        expect(xsrfChecker.checkWebRequestInvocation(request)).andReturn(new Result(true, true));
        expect(request.getInputStream()).andThrow(new IOException());

        control.replay();

        AttachmentHelper attachmentHelper = new AttachmentHelper(xsrfChecker, jiraAuthenticationContext) {
            @Override
            String getMaxAttachmentSize()
            {
                return "10000000";
            }
        };

        AttachmentHelper.ValidationResult validationResult = attachmentHelper.validate(request, "test.jpg", 15430L);

        assertEquals(validationResult.getErrorType(), AttachmentHelper.ValidationError.ATTACHMENT_IO_UNKNOWN);

        control.verify();
    }

     @Test
    public void testAttachmentSizeToLarge() throws IOException
    {
        XsrfInvocationChecker xsrfChecker = control.createMock(XsrfInvocationChecker.class);
        JiraAuthenticationContext jiraAuthenticationContext = control.createMock(JiraAuthenticationContext.class);
        I18nHelper i18nHelper = control.createMock(I18nHelper.class);
        expect(i18nHelper.getText(eq("avatarpicker.upload.size.toobig"), anyObject())).andReturn("avatarpicker.upload.size.toobig").anyTimes();
        expect(jiraAuthenticationContext.getI18nHelper()).andReturn(i18nHelper);
        expect(xsrfChecker.checkWebRequestInvocation(request)).andReturn(new Result(true, true));

        control.replay();

        AttachmentHelper attachmentHelper = new AttachmentHelper(xsrfChecker, jiraAuthenticationContext) {
            @Override
            String getMaxAttachmentSize()
            {
                return "100";
            }
        };

        AttachmentHelper.ValidationResult validationResult = attachmentHelper.validate(request, "test.jpg", 15430L);
        assertEquals(validationResult.getErrorType(), AttachmentHelper.ValidationError.ATTACHMENT_TO_LARGE);

        control.verify();
    }


    @Test
    public void testBadSize()
    {
        XsrfInvocationChecker xsrfChecker = control.createMock(XsrfInvocationChecker.class);
        expect(xsrfChecker.checkWebRequestInvocation(request)).andReturn(new Result(true, true));
           JiraAuthenticationContext jiraAuthenticationContext = control.createMock(JiraAuthenticationContext.class);
        I18nHelper i18nHelper = control.createMock(I18nHelper.class);
        expect(i18nHelper.getText(eq("attachfile.error.io.size"), anyObject())).andReturn("attachfile.error.io.size").anyTimes();

        expect(jiraAuthenticationContext.getI18nHelper()).andReturn(i18nHelper);

        expect(request.getContentLength()).andReturn(-1);

        control.replay();

        AttachmentHelper attachmentHelper = new AttachmentHelper(xsrfChecker, jiraAuthenticationContext);
        AttachmentHelper.ValidationResult validationResult = attachmentHelper.validate(request, "test.jpg", null);

        assertEquals(validationResult.getErrorType(), AttachmentHelper.ValidationError.ATTACHMENT_IO_SIZE);

        control.verify();
    }

    private static class Result implements XsrfCheckResult
    {
        private final boolean required;
        private final boolean valid;

        private Result(boolean required, boolean valid)
        {
            this.required = required;
            this.valid = valid;
        }

        public boolean isRequired()
        {
            return required;
        }

        public boolean isValid()
        {
            return valid;
        }

        public boolean isGeneratedForAuthenticatedUser()
        {
            return false;
        }
    }

}
