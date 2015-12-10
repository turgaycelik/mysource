package com.atlassian.jira.issue;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.atlassian.jira.web.ExecutingHttpRequest;
import com.atlassian.jira.web.SessionKeys;
import com.atlassian.jira.web.action.issue.TemporaryAttachmentsMonitor;

import org.easymock.Capture;
import org.easymock.classextension.EasyMock;
import org.easymock.classextension.IMocksControl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.easymock.EasyMock.eq;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;

/**
 * Test for {@link com.atlassian.jira.issue.DefaultTemporaryAttachmentsMonitorLocator}.
 *
 * @since v4.3
 */
public class TestDefaultTemporaryAttachmentsMonitorLocator
{
    private IMocksControl mockControl;
    private HttpServletRequest httpServletRequest;
    private HttpSession httpSession;

    @Before
    public void setup()
    {
        mockControl = EasyMock.createControl();
        httpServletRequest = mockControl.createMock(HttpServletRequest.class);
        httpSession = mockControl.createMock(HttpSession.class);

        ExecutingHttpRequest.set(httpServletRequest, null);
    }

    @After
    public void tearDown()
    {
        mockControl = null;
        httpServletRequest = null;
        httpSession = null;

        ExecutingHttpRequest.clear();
    }

    @Test
    public void testGetForRequestNoCreateNoSession() throws Exception
    {
        EasyMock.expect(httpServletRequest.getSession(false)).andReturn(null);
        mockControl.replay();

        final DefaultTemporaryAttachmentsMonitorLocator locator = new DefaultTemporaryAttachmentsMonitorLocator();
        assertThat(locator.get(false), nullValue());

        mockControl.verify();
    }

    @Test
    public void testGetForRequestNoCreateSessionNoValue() throws Exception
    {
        EasyMock.expect(httpServletRequest.getSession(false)).andReturn(httpSession);
        EasyMock.expect(httpSession.getAttribute(SessionKeys.TEMP_ATTACHMENTS)).andReturn(null);

        mockControl.replay();

        final DefaultTemporaryAttachmentsMonitorLocator locator = new DefaultTemporaryAttachmentsMonitorLocator();
        assertThat(locator.get(false), nullValue());

        mockControl.verify();
    }

    @Test
    public void testGetForRequestNoCreateSessionValue() throws Exception
    {
        final TemporaryAttachmentsMonitor monitor = mockControl.createMock(TemporaryAttachmentsMonitor.class);

        EasyMock.expect(httpServletRequest.getSession(false)).andReturn(httpSession);
        EasyMock.expect(httpSession.getAttribute(SessionKeys.TEMP_ATTACHMENTS)).andReturn(monitor);

        mockControl.replay();

        final DefaultTemporaryAttachmentsMonitorLocator locator = new DefaultTemporaryAttachmentsMonitorLocator();
        assertThat(locator.get(false), sameInstance(monitor));

        mockControl.verify();
    }

    @Test
    public void testGetForRequestCreateSessionNoValue() throws Exception
    {
        Capture<Object> objectCapture = new Capture<Object>();
        EasyMock.expect(httpServletRequest.getSession(true)).andReturn(httpSession);
        EasyMock.expect(httpSession.getAttribute(SessionKeys.TEMP_ATTACHMENTS)).andReturn(null);
        httpSession.setAttribute(eq(SessionKeys.TEMP_ATTACHMENTS), EasyMock.capture(objectCapture));

        mockControl.replay();

        final DefaultTemporaryAttachmentsMonitorLocator locator = new DefaultTemporaryAttachmentsMonitorLocator();
        TemporaryAttachmentsMonitor monitor = locator.get(true);
        assertThat(monitor, notNullValue());
        assertThat(monitor, sameInstance(objectCapture.getValue()));

        mockControl.verify();
    }

    @Test
    public void testGetForRequestCreateSessionValue() throws Exception
    {
        final TemporaryAttachmentsMonitor monitor = mockControl.createMock(TemporaryAttachmentsMonitor.class);

        EasyMock.expect(httpServletRequest.getSession(true)).andReturn(httpSession);
        EasyMock.expect(httpSession.getAttribute(SessionKeys.TEMP_ATTACHMENTS)).andReturn(monitor);

        mockControl.replay();

        final DefaultTemporaryAttachmentsMonitorLocator locator = new DefaultTemporaryAttachmentsMonitorLocator();
        assertThat(locator.get(true), sameInstance(monitor));

        mockControl.verify();
    }

    @Test
    public void testGetForRequestNoRequest() throws Exception
    {
        ExecutingHttpRequest.set(null, null);
        mockControl.replay();

        final DefaultTemporaryAttachmentsMonitorLocator locator = new DefaultTemporaryAttachmentsMonitorLocator();
        assertThat(locator.get(true), nullValue(TemporaryAttachmentsMonitor.class));

        mockControl.verify();
    }
}
